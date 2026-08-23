package com.peter.emulator.assembly;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.peter.emulator.machinecode.ConditionalOperator;
import com.peter.emulator.machinecode.Set;
import com.peter.emulator.machinecode.Stack;
import com.peter.emulator.MachineCode.MathOperator;
import static com.peter.emulator.MachineCode.*;
import com.peter.emulator.assembly.SymbolFile.FunctionSymbol;
import com.peter.emulator.assembly.SymbolFile.ValueSymbol;
import com.peter.emulator.assembly.SymbolFile.VariableSymbol;
import com.peter.emulator.machinecode.Goto;
import com.peter.emulator.machinecode.Instruction;
import com.peter.emulator.machinecode.Load;
import com.peter.emulator.machinecode.MathInstruction;
import com.peter.emulator.machinecode.Reg;
import com.peter.emulator.machinecode.Store;
import com.peter.emulator.machinecode.Syscall;
import com.peter.emulator.machinecode.Goto.Mode;
import com.peter.emulator.machinecode.Store.Size;

public class Assembler {

    // private Entry[] data = null;
    private ArrayList<Instruction> instructions = null;
    private int memAddr = 0;
    private int lastNonZeroAddress = 0;
    private final HashMap<String, Define> labels = new HashMap<>();
    private final HashMap<String, Define> defines = new HashMap<>();
    private final ArrayList<Define> defineOrder = new ArrayList<>();
    private final ArrayList<Define> defineEmpties = new ArrayList<>();
    // private final ArrayList<MemSet> memSet = new ArrayList<>();
    protected HashMap<String, Integer> syscallDef = new HashMap<>();
    protected HashMap<String, Integer> syscallMap = new HashMap<>();

    protected Linker linker = null;
    protected String source = "[literal]";

    public SymbolFile symbols = new SymbolFile();
    protected int startOffset = 0;

    private static final Pattern DEFINE_ARRAY_PATTERN = Pattern.compile("#(?:define|var)\\s+[\\w\\.]+\\s+\\[([^\\]]+)\\]");
    private static final Pattern DEFINE_ARRAY_VALUE_PATTERN = Pattern.compile("(0x[0-9a-f_]+|\\d+|\\w+)(?:,\\s*)?");
    private static final Pattern STRING_PATTERN = Pattern.compile("\"(.*)\"");
    private static final Pattern ALLOC_PATTERN = Pattern.compile("\\(([^\\)]+)\\)");

    private String[] lines;

    public Assembler(Linker linker) {
        this.linker = linker;
    }

    public Assembler() {
        linker = new Linker();
    }

    public void setSource(String file) {
        lines = file.split("\s*?\n\r?");
    }

    public void setSource(Path file) throws IOException {
        setSource(Files.readString(file));
        source = file.toString();
        symbols.source = source;
    }

    private boolean inSyscall = false;
    public final ArrayList<AssemblerError> errors = new ArrayList<>();

    // protected int getAddress(int wordI) {
    //     return (wordI * 4) + offset;
    // }

    public boolean assemble() {
        errors.clear();
        
        // +=== PRE PASS ==============================================+
        // | Find labels, functions, and defines so we know they exist |
        // +===========================================================+

        FunctionSymbol cFunction = null;
        Define startLabel = null;
        for (int lineN = 0; lineN < lines.length; lineN++) {
            String line = lines[lineN].trim();
            if (line.isBlank() || line.startsWith("//"))
                continue;
            String[] parts = line.split("\s+");
            if (line.charAt(0) == ':') {
                String name = parts[0].substring(1);
                // labels.put(name, getAddress(addr));
                Define label = new Define(name);
                labels.put(name, label);
                if (name.equals("__start")) {
                    startLabel = label;
                }
                continue;
            }
            if (line.startsWith("#")) {
                switch (parts[0].substring(1)) {
                    case "include" -> {
                        if (linker != null) {
                            linker.include(parts[1]);
                        }
                    }
                    case "define" -> {
                        String name = parts[1];
                        String type = parts.length > 3 ? parts[3] : null;
                        if (parts[2].startsWith("\"")) {
                            Matcher m = STRING_PATTERN.matcher(line);
                            m.find();
                            String str = m.group(1);
                            String str2 = "";
                            for (int j = 0; j < str.length(); j++) {
                                char c = str.charAt(j);
                                if (c == '\\') {
                                    if (j + 1 < str.length()) {
                                        char n = str.charAt(j + 1);
                                        switch (n) {
                                            case 'n' -> {
                                                str2 += "\n";
                                                j++;
                                            }
                                            case 't' -> {
                                                str2 += "\t";
                                                j++;
                                            }
                                            case '\\' -> {
                                                str2 += "\\";
                                                j++;
                                            }
                                            case '0' -> {
                                                str2 += "\0";
                                                j++;
                                            }
                                        }
                                    }
                                } else {
                                    str2 += c;
                                }
                            }
                            str = str2;
                            addDefine(new Define(name, str));
                            if (type == null)
                                type = "char*";
                            symbols.addDefinition(new ValueSymbol(name, -1, -1, type, str), lineN + 1);
                        } else if (parts[2].startsWith("[")) {
                            Matcher m = DEFINE_ARRAY_PATTERN.matcher(line);
                            if (!m.find()) {
                                errors.add(new AssemblerError("Invalid array definition", lineN, 8, line, source));
                                continue;
                            }
                            String content = m.group(1);
                            m = DEFINE_ARRAY_VALUE_PATTERN.matcher(content);
                            ArrayList<Integer> arr = new ArrayList<>();
                            while (m.find()) {
                                arr.add(getVal(m.group(1)).value);
                            }
                            int[] val = new int[arr.size()];
                            for (int j = 0; j < val.length; j++) {
                                val[j] = arr.get(j);
                            }
                            addDefine(new Define(name, val));
                            if (type == null)
                                type = "uint32*";
                            symbols.addDefinition(new ValueSymbol(name, -1, -1, type, content), lineN + 1);
                        } else if (parts[2].startsWith("(")) {
                            Matcher m = ALLOC_PATTERN.matcher(parts[2]);
                            m.find();
                            int size = Math.ceilDiv(getVal(m.group(1)).value, 4);
                            addDefine(new Define(name).withSize(size * 4));
                            if (type == null)
                                type = "uint32[" + size + "]*";
                            symbols.addDefinition(new ValueSymbol(name, -1, -1, type, ""),
                                    lineN + 1);
                        } else {
                            int val = getVal(parts[2]).value;
                            addDefine(new Define(name, val));
                            if (type == null)
                                type = "uint32";
                            symbols.addDefinition(
                                    new ValueSymbol(name, -1, -1, "const " + type, val + ""),
                                    lineN + 1);
                        }
                    }
                    case "var" -> {
                        String name = parts[1];
                        String type = parts.length > 3 ? parts[3] : "uint32";
                        if (parts[2].startsWith("\"")) {
                            Matcher m = STRING_PATTERN.matcher(line);
                            m.find();
                            String str = m.group(1);
                            String str2 = "";
                            for (int j = 0; j < str.length(); j++) {
                                char c = str.charAt(j);
                                if (c == '\\') {
                                    if (j + 1 < str.length()) {
                                        char n = str.charAt(j + 1);
                                        switch (n) {
                                            case 'n' -> {
                                                str2 += "\n";
                                                j++;
                                            }
                                            case 't' -> {
                                                str2 += "\t";
                                                j++;
                                            }
                                            case '\\' -> {
                                                str2 += "\\";
                                                j++;
                                            }
                                            case '0' -> {
                                                str2 += "\0";
                                                j++;
                                            }
                                        }
                                    }
                                } else {
                                    str2 += c;
                                }
                            }
                            str = str2;
                            addDefine(new Define(name, str));
                            if (type == null)
                                type = "char*";
                            symbols.addVariable(new VariableSymbol(name, -1, -1, type, str), lineN + 1);
                        } else if (parts[2].startsWith("[")) {
                            Matcher m = DEFINE_ARRAY_PATTERN.matcher(line);
                            if (!m.find()) {
                                errors.add(new AssemblerError("Invalid array definition", lineN, 8, line, source));
                                continue;
                            }
                            String content = m.group(1);
                            m = DEFINE_ARRAY_VALUE_PATTERN.matcher(content);
                            ArrayList<Integer> arr = new ArrayList<>();
                            while (m.find()) {
                                arr.add(getVal(m.group(1)).value);
                            }
                            int[] val = new int[arr.size()];
                            for (int j = 0; j < val.length; j++) {
                                val[j] = arr.get(j);
                            }
                            addDefine(new Define(name, val));
                            if (type == null)
                                type = "uint32*";
                            symbols.addVariable(new VariableSymbol(name, -1, -1, type, content), lineN + 1);
                        } else if (parts[2].startsWith("(")) {
                            Matcher m = ALLOC_PATTERN.matcher(parts[2]);
                            m.find();
                            int size = Math.ceilDiv(getVal(m.group(1)).value, 4);
                            addDefine(new Define(name).withSize(size * 4));
                            if (type == null)
                                type = "uint32[" + size + "]*";
                            symbols.addVariable(new VariableSymbol(name, -1, -1, type, ""),
                                    lineN + 1);
                        } else {
                            int val = getVal(parts[2]).value;
                            if(val == 0) {
                                addDefine(new Define(name));
                            } else {
                                addDefine(new Define(name, new int[] { val }));
                            }
                            if (type == null)
                                type = "uint32";
                            symbols.addVariable(
                                    new VariableSymbol(name, -1, -1, type, val + ""),
                                    lineN + 1);
                        }
                    }
                    case "function" -> {
                        labels.put(parts[1], new Define(parts[1]));
                    }
                    case "syscall" -> {
                        int index = getVal(parts[1]).value;
                        syscallMap.put(parts[2], index);
                        symbols.mapSyscall(parts[2], index);
                        // System.out.println(String.format("Added syscall 0x%x %s", index, parts[2]));
                    }
                    case "breakpoint" -> {

                    }
                }
                continue;
            }
        }
        if (!errors.isEmpty()) {
            System.err.println("Early exit: " + errors.size());
            for (AssemblerError err : errors) {
                System.err.println(err.getPrintString());
            }
            return false;
        }
        instructions = new ArrayList<>();
        memAddr = startOffset;

        // +=== MAIN PASS =============================================================================+
        // | Fill in all instructions, using temp load, store, and goto as needed                      |
        // | Also resolve addresses of labels/functions and updates their associated temp instructions |
        // +===========================================================================================+

        if (startLabel != null) {
            add(new TempGoto(ConditionalOperator.UNCONDITIONAL, Mode.NONE, Reg.R0, startLabel));
        } else {
            System.out.println("File did not have __start");
        }
        for (int lineN = 0; lineN < lines.length; lineN++) {
            try {
                String line = lines[lineN].trim();
                if (line.isBlank() || line.startsWith("//"))
                    continue;
                String[] parts = line.split("\s+");
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].startsWith("//")) {
                        String[] temp = new String[i];
                        System.arraycopy(parts, 0, temp, 0, i);
                        parts = temp;
                        break;
                    }
                }
                if (line.startsWith("#") || line.startsWith(":")) { // compiler instruction
                    if (line.startsWith(":")) {
                        labels.get(parts[0].substring(1)).resolveAt(memAddr);
                    } else if (line.startsWith("#function")) {
                        String functionName = parts[1];
                        labels.get(functionName).resolveAt(memAddr);

                        if (cFunction != null) {
                            errors.add(new AssemblerError("Found #function definition within another function", lineN,
                                    0, line, source));
                            inSyscall = false;
                            continue;
                        }
                        boolean isSyscall = functionName.startsWith("syscall::");
                        if (isSyscall) {
                            String syscallName = functionName.substring(9);
                            syscallDef.put(syscallName, memAddr);
                            inSyscall = true;
                            // System.out.println("Test?: " + functionName);
                        }
                        ArrayList<String> args = new ArrayList<>();
                        int j = 2;
                        String arg = "";
                        while (j < parts.length && !parts[j].startsWith("//")) {
                            String s = parts[j++];
                            if (arg.length() > 0)
                                arg += " ";
                            if (s.endsWith(",")) {
                                arg += s.substring(0, s.length() - 1);
                                args.add(arg);
                                arg = "";
                            } else {
                                arg += s;
                            }
                        }
                        if (arg.length() > 0) {
                            args.add(arg);
                        }
                        cFunction = symbols.addFunction(
                                new FunctionSymbol(functionName, memAddr, -1, args.toArray(String[]::new),
                                        "void",
                                        isSyscall, false),
                                lineN + 1);
                    } else if(line.startsWith("#endfunction")) {
                        if (cFunction == null) {
                            errors.add(new AssemblerError("Found #endfunction without matching #function", lineN, 0,
                                    line, source));
                            continue;
                        }
                        cFunction.end = memAddr;
                        cFunction.endLine = lineN;
                        if (parts.length >= 2 && !parts[1].startsWith("//"))
                            cFunction.rt = parts[1];
                        cFunction = null;
                    } else if (line.startsWith("#breakpoint")) {
                        if (instructions.size() > 0 && instructions.getLast().hasSecond()) {
                            symbols.addBreakpoint(memAddr - 8);
                        } else {
                            symbols.addBreakpoint(memAddr - 4);
                        }
                    } else if (line.startsWith("#lineend")) {
                        if (instructions.size() > 0 && instructions.getLast().hasSecond()) {
                            symbols.endLine(memAddr - 8);
                        } else {
                            symbols.endLine(memAddr - 4);
                        }
                    } else if (line.startsWith("#line")) {
                        symbols.startLine(parts[1], parts[2], memAddr);
                    } else if (line.startsWith("#stackVarClear")) {
                        if (!symbols.endStackVar(parts[1], memAddr)) {
                            errors.add(new AssemblerError("Invalid stackVarClear, no such variable `" + parts[1] + "`",
                                    lineN,
                                    line.length(), line, source));
                            continue;
                        }
                    } else if (line.startsWith("#stackVar")) {
                        int next = 1;
                        String type = parts[next++];
                        while (type.equals("out") || type.equals("const")) {
                            type = parts[next++];
                        }
                        String name = parts[next++];
                        int stackOffset = parts.length > next ? Integer.parseInt(parts[next]) : 0;
                        symbols.addStackVar(type, name, stackOffset, memAddr);
                    }
                    continue;
                }
                switch (parts[0]) {
                    case "HALT" -> {
                        add(Instruction.Halt());
                    }
                    case "LOAD" -> {
                        if (parts.length < 2) {
                            errors.add(new AssemblerError("Invalid load instruction: LOAD <MEM> [rg] [ra|val]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        if (parts[1].equals("MEM")) {
                            if (parts.length < 4) {
                                errors.add(new AssemblerError(
                                        "Invalid load instruction: LOAD MEM <SHORT|BYTE> [rg] [ra]", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            switch (parts[2]) {
                                case "SHORT" -> {
                                    add(Load.MemShort(Reg.from(parts[3]), Reg.from(parts[4])));
                                }
                                case "BYTE" -> {
                                    add(Load.MemByte(Reg.from(parts[3]), Reg.from(parts[4])));
                                }
                                default -> {
                                    add(Load.MemWord(Reg.from(parts[2]), Reg.from(parts[3])));
                                }
                            }
                        } else {
                            if (parts.length < 3) {
                                errors.add(new AssemblerError("Invalid load instruction: LOAD [rg] [val]", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            // int rg = getReg(parts[1]);
                            // data[addr++] = (Entry.Load(rg));
                            // int val = getVal(parts[2]);
                            // data[addr++] = (Entry.Literal(val));
                            add(new TempLoad(Reg.from(parts[1]), getVal(parts[2])));
                        }
                    }
                    case "COPY" -> {
                        if (parts.length < 3) {
                            errors.add(new AssemblerError(
                                    "Invalid copy instruction: COPY [rs] [rd] | COPY MEM <SHORT|BYTE> [rs] [rd] (INC_RS) (INC_RD)",
                                    lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        int next = 1;
                        StoreEntry entry;
                        if (parts[next].equals("MEM")) {
                            next++;
                            if (parts.length < next + 2) {
                                errors.add(new AssemblerError(
                                        "Invalid copy instruction: COPY MEM <SHORT|BYTE> [rs] [rd] (INC_RS) (INC_RD)",
                                        lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            Store.Size size = Size.WORD;
                            switch (parts[next]) {
                                case "SHORT" -> {
                                    size = Size.SHORT;
                                    next++;
                                }
                                case "BYTE" -> {
                                    size = Size.BYTE;
                                    next++;
                                }
                            }
                            int rs = getReg(parts[next++]);
                            int rd = getReg(parts[next++]);
                            entry = new StoreEntry(rs, size.id, STORE_SOURCE_MEM, rd);
                            Store instruction = add(Store.CopyMem(size, Reg.from(rs), Reg.from(rd)));
                            if (parts.length > next) {
                                switch (parts[next++]) {
                                    case "INC_RS" -> {
                                        entry.incRG();
                                        instruction.withIncRG();
                                    }
                                    case "INC_RD" -> {
                                        entry.incRA();
                                        instruction.withIncRA();
                                    }
                                }
                            }
                            if (parts.length > next) {
                                switch (parts[next++]) {
                                    case "INC_RS" -> {
                                        entry.incRG();
                                        instruction.withIncRG();
                                    }
                                    case "INC_RD" -> {
                                        entry.incRA();
                                        instruction.withIncRA();
                                    }
                                }
                            }
                        } else {
                            if (parts.length != 3) {
                                errors.add(new AssemblerError("Invalid copy instruction: COPY [rs] [rd]", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            int rs = getReg(parts[next++]);
                            int rd = getReg(parts[next++]);
                            entry = new StoreEntry(rs, 0x0, STORE_SOURCE_REG_REG, rd);
                            add(Store.CopyReg(Reg.from(rs), Reg.from(rd)));
                        }
                        // data[addr++] = entry;
                    }
                    case "STORE" -> {
                        if (parts.length < 2) {
                            errors.add(new AssemblerError(
                                    "Invalid store instruction: STORE <SHORT|BYTE> VAL [ra] [val] <INC_RA> or STORE <SHORT|BYTE> [val|rg] [ra] <INC_RA>",
                                    lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        Size size = Size.WORD;
                        int next = 1;
                        switch (parts[next]) {
                            case "SHORT" -> {
                                size = Size.SHORT;
                                next++;
                            }
                            case "BYTE" -> {
                                size = Size.BYTE;
                                next++;
                            }
                        }
                        Store store;
                        if (parts[next].equals("VAL")) {
                            if (parts.length < next + 3) {
                                errors.add(new AssemblerError(
                                        "Invalid store instruction: STORE <SHORT|BYTE> VAL [ra] [val] <INC_RA>", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            next++;
                            int ra = getReg(parts[next++]);
                            Define val = getVal(parts[next++]);
                            store = add(new TempStore(size, val, Reg.from(ra)));
                        } else if (!parts[next].startsWith("r")) {
                            if (parts.length < next + 2) {
                                errors.add(new AssemblerError(
                                        "Invalid store instruction: STORE <SHORT|BYTE> [rg|val] [ra] <INC_RA>", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            Define val = getVal(parts[next++]);
                            int ra = getReg(parts[next++]);
                            store = add(new TempStore(size, val, Reg.from(ra)));
                        } else {
                            if (parts.length < next + 2) {
                                errors.add(new AssemblerError(
                                        "Invalid store instruction: STORE <SHORT|BYTE> [rg|val] [ra] <INC_RA>", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            int rg = getReg(parts[next++]);
                            int ra = getReg(parts[next++]);
                            store = add(Store.StoreReg(size, Reg.from(rg), Reg.from(ra)));
                        }
                        if (parts.length > next && parts[next].equals("INC_RA")) {
                            // entry.incRA();
                            store.withIncRA();
                        }
                    }
                    case "ADD" -> {
                        if (parts.length < 4) {
                            errors.add(new AssemblerError("Invalid add instruction: ADD [rd] [ra] [rb]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        add(MathInstruction.Add(Reg.from(parts[1]), Reg.from(parts[2]), Reg.from(parts[3])));
                    }
                    case "SUB" -> {
                        if (parts.length < 4) {
                            errors.add(new AssemblerError("Invalid sub instruction: SUB [rd] [ra] [rb]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        add(MathInstruction.Sub(Reg.from(parts[1]), Reg.from(parts[2]), Reg.from(parts[3])));
                    }
                    case "INC" -> {
                        if (parts.length < 2) {
                            errors.add(new AssemblerError("Invalid inc instruction: INC [rd] <[val]>", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        int inc = 1;
                        if (parts.length >= 3 && !parts[2].startsWith("//")) {
                            inc = getVal(parts[2]).value;
                        }
                        add(MathInstruction.Inc(Reg.from(parts[1]), inc));
                    }
                    case "MUL" -> {
                        if (parts.length < 4) {
                            errors.add(new AssemblerError("Invalid mul instruction: MUL [rd] [ra] [rb]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        add(MathInstruction.Mul(Reg.from(parts[1]), Reg.from(parts[2]), Reg.from(parts[3])));
                    }
                    case "DIV" -> {
                        if (parts.length < 4) {
                            errors.add(new AssemblerError("Invalid mul instruction: DIV [rd] [ra] [rb]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        add(MathInstruction.Div(Reg.from(parts[1]), Reg.from(parts[2]), Reg.from(parts[3])));
                    }
                    case "AND" -> {
                        if (parts.length < 4) {
                            errors.add(new AssemblerError("Invalid AND instruction: AND [rd] [ra] [rb]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        add(MathInstruction.And(Reg.from(parts[1]), Reg.from(parts[2]), Reg.from(parts[3])));
                    }
                    case "NAND" -> {
                        if (parts.length < 4) {
                            errors.add(new AssemblerError("Invalid NAND instruction: NAND [rd] [ra] [rb]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        add(MathInstruction.Nand(Reg.from(parts[1]), Reg.from(parts[2]), Reg.from(parts[3])));
                    }
                    case "OR" -> {
                        if (parts.length < 4) {
                            errors.add(new AssemblerError("Invalid mul instruction: OR [rd] [ra] [rb]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        add(MathInstruction.Or(Reg.from(parts[1]), Reg.from(parts[2]), Reg.from(parts[3])));
                    }
                    case "NOR" -> {
                        if (parts.length < 4) {
                            errors.add(new AssemblerError("Invalid NOR instruction: NOR [rd] [ra] [rb]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        add(MathInstruction.Nor(Reg.from(parts[1]), Reg.from(parts[2]), Reg.from(parts[3])));
                    }
                    case "XOR" -> {
                        if (parts.length < 4) {
                            errors.add(new AssemblerError("Invalid XOR instruction: XOR [rd] [ra] [rb]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        add(MathInstruction.Xor(Reg.from(parts[1]), Reg.from(parts[2]), Reg.from(parts[3])));
                    }
                    case "NOT" -> {
                        if (parts.length != 3) {
                            errors.add(new AssemblerError("Invalid NOT instruction: NOT [rd] [ra]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        add(MathInstruction.Not(Reg.from(parts[1]), Reg.from(parts[2])));
                    }
                    case "LSH" -> {
                        if (parts.length < 4) {
                            errors.add(new AssemblerError("Invalid mul instruction: LSH [rd] [ra] [amt]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        add(MathInstruction.LShift(Reg.from(parts[1]), Reg.from(parts[2]), getVal(parts[3]).value));
                    }
                    case "RSH" -> {
                        if (parts.length < 4) {
                            errors.add(new AssemblerError("Invalid mul instruction: RSH [rd] [ra] [amt]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        add(MathInstruction.RShift(Reg.from(parts[1]), Reg.from(parts[2]), getVal(parts[3]).value));
                    }
                    case "LRT" -> {
                        if (parts.length < 4) {
                            errors.add(new AssemblerError("Invalid mul instruction: LRT [rd] [ra] [amt]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        add(MathInstruction.LRotate(Reg.from(parts[1]), Reg.from(parts[2]), getVal(parts[3]).value));
                    }
                    case "RRT" -> {
                        if (parts.length < 4) {
                            errors.add(new AssemblerError("Invalid mul instruction: RRT [rd] [ra] [amt]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        add(MathInstruction.RRotate(Reg.from(parts[1]), Reg.from(parts[2]), getVal(parts[3]).value));
                    }
                    case "GOTO" -> {
                        if (parts.length < 2) {
                            errors.add(new AssemblerError("Invalid goto instruction", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        boolean push = parts[1].equals("PUSH");
                        boolean pop = parts[1].equals("POP");
                        int nI = 1;
                        if (push || pop) {
                            nI++;
                        }
                        ConditionalOperator cond = ConditionalOperator.UNCONDITIONAL;
                        if (parts.length < nI) {
                            errors.add(new AssemblerError("Invalid goto instruction", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        if (parts.length > nI) {
                            cond = switch (parts[nI]) {
                                case "EQ" -> ConditionalOperator.EQ_ZERO;
                                case "LEQ" -> ConditionalOperator.LEQ_ZERO;
                                case "GT" -> ConditionalOperator.GT_ZERO;
                                case "NEQ" -> ConditionalOperator.NEQ_ZERO;
                                case "LT" -> ConditionalOperator.LT_ZERO;
                                case "GEQ" -> ConditionalOperator.GEQ_ZERO;

                                default -> ConditionalOperator.UNCONDITIONAL;
                            };
                        }
                        Reg ro = Reg.R0;
                        if (cond != ConditionalOperator.UNCONDITIONAL) {
                            nI++;
                            if (parts.length < nI) {
                                errors.add(new AssemblerError("Invalid goto instruction", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            ro = Reg.from(parts[nI++]);
                        }
                        String target = "";
                        Reg ra = Reg.R0;
                        boolean relative = (!pop);
                        if (!pop) {
                            if (parts.length < nI) {
                                errors.add(new AssemblerError("Invalid goto instruction", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            if (parts[nI].startsWith("r")) {
                                ra = Reg.from(parts[nI]);
                                relative = false;
                            } else {
                                target = parts[nI].substring(1);
                            }
                        }
                        if (pop) {
                            add(Goto.Pop(cond, ra));
                        } else if (relative) {
                            add(new TempGoto(cond, push ? Mode.PUSH : Mode.NONE, ro, labels.get(target)));
                        } else {
                            add(Goto.Conditional(cond, push ? Mode.PUSH : Mode.NONE, ra, ro));
                        }
                    }
                    case "SET" -> {
                        boolean forced = false;
                        if (parts.length == 5) {
                            if (!parts[1].equals("FORCE")) {
                                errors.add(new AssemblerError(
                                        "Invalid set instruction: SET (FORCE) <EQ|LEQ|GEQ|NEQ|LT|GEQ> [rg] [rd]", lineN,
                                        0, line, source));
                                continue;
                            }
                            forced = true;
                        }
                        String op = parts[forced ? 2 : 1];
                        Reg rg = Reg.from(parts[forced ? 3 : 2]);
                        Reg rd = Reg.from(parts[forced ? 4 : 3]);
                        ConditionalOperator cond = switch (op) {
                            case "EQ" -> ConditionalOperator.EQ_ZERO;
                            case "LEQ" -> ConditionalOperator.LEQ_ZERO;
                            case "GT" -> ConditionalOperator.GT_ZERO;
                            case "NEQ" -> ConditionalOperator.NEQ_ZERO;
                            case "LT" -> ConditionalOperator.LT_ZERO;
                            case "GEQ" -> ConditionalOperator.GEQ_ZERO;
                            default -> ConditionalOperator.UNKNOWN;
                        };
                        if (cond == ConditionalOperator.UNKNOWN) {
                            errors.add(new AssemblerError(
                                    "Invalid set instruction operator: SET (FORCED) <EQ|LEQ|GEQ|NEQ|LT|GEQ> [rg] [rd]",
                                    lineN, line.length(), line, source));
                            continue;
                        }
                        add(new Set(cond, rg, rd, forced));
                    }
                    case "STACK" -> {
                        if (parts.length == 2) {
                            if (parts[1].equals("INC")) {
                                add(Stack.Inc(1));
                                continue;
                            } else if (parts[1].equals("DEC")) {
                                add(Stack.Dec(1));
                                continue;
                            }
                        }
                        if (parts.length < 3) {
                            errors.add(new AssemblerError(
                                    "Invalid stack instruction: STACK (PUSH|POP) [rg] | STACK (INC|DEC) ([value])",
                                    lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        if (parts[1].equals("INC")) {
                            add(Stack.Inc(getVal(parts[2]).value));
                        } else if (parts[1].equals("DEC")) {
                            add(Stack.Dec(getVal(parts[2]).value));
                        } else {
                            if (parts[1].equals("PUSH")) {
                                add(Stack.Push(Reg.from(parts[2])));
                            } else {
                                add(Stack.Pop(Reg.from(parts[2])));
                            }
                        }
                    }
                    case "SYSCALL" -> {
                        if (parts.length < 2) {
                            errors.add(new AssemblerError("Invalid system call instruction: SYSCALL [function]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        FunctionSymbol funcSym = getFunction(lineN);
                        if (funcSym != null && funcSym.isSyscall) {
                            errors.add(new AssemblerError("Found syscall from within a syscall", lineN,
                                    0, line, source));
                            continue;
                        }
                        int func = -1;
                        if (linker != null)
                            func = linker.getSyscall(parts[1]);
                        if (func == -1)
                            func = getVal(parts[1]).value;
                        add(Syscall.Function(func));
                    }
                    case "SYSRETURN" -> {
                        FunctionSymbol funcSym = getFunction(lineN);
                        if (funcSym != null && !funcSym.isSyscall) {
                            errors.add(new AssemblerError(
                                    "Found syscall return outside of system call; Function was " + funcSym.name, lineN,
                                    0, line, source));
                            continue;
                        }
                        add(Syscall.Return());
                    }
                    case "SYSGOTO" -> {
                        if (parts.length < 2) {
                            errors.add(new AssemblerError("Invalid system goto instruction: SYSGOTO [rg]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        add(Syscall.Goto(Reg.from(parts[1])));
                    }
                    case "INTERRUPT" -> {
                        if (parts.length < 2) {
                            errors.add(new AssemblerError("Invalid interrupt instruction: INTERRUPT <RET|[rg]|[val]>",
                                    lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        if (parts[1].equals("RET")) {
                            add(Syscall.InterruptReturn());
                        } else if (parts[1].startsWith("r")) {
                            add(Syscall.Interrupt(Reg.from(parts[1])));
                        } else {
                            add(Syscall.Interrupt(getVal(parts[1]).value));
                        }
                    }
                    default -> {
                        errors.add(new AssemblerError(String.format("Unknown instruction: `%s`", parts[0]), lineN, 0,
                                line, source));
                        continue;
                    }
                }
            } catch (Exception e) {
                // System.err.println(e);
                // e.printStackTrace();
                System.err.println("Error parsing assembly at line " + (lineN + 1) + " in file " + source);
                throw e;
            }
        }
        if (cFunction != null) {
            errors.add(new AssemblerError("Un-ended function at end of file", lines.length, -1,
                    "[EOF]", source));
            cFunction.end = memAddr;
            cFunction.endLine = lines.length - 1;
        }

        // +=== DATA PASS ===============================================================================+
        // | Append defines which are not single values (ie. strings, arrays, etc.) and static variables |
        // +===========+++===============================================================================+
        
        for (Define define : defineOrder) {
            define.resolveAt(memAddr);
            symbols.updateDefinition(define.name, memAddr, memAddr + (define.valueArr.length * 4) - 1);
            for (int i = 0; i < define.valueArr.length; i++) {
                add(new LiteralInstruction(define.valueArr[i]));
            }
        }
        lastNonZeroAddress = memAddr;
        for (Define define : defineEmpties) {
            define.resolveAt(memAddr);
            symbols.updateDefinition(define.name, memAddr, memAddr + (define.size) - 1);
            memAddr += define.size;
        }
        

        for (Define d : labels.values()) {
            if (!d.resolved) {
                errors.add(new AssemblerError("Unresolved label :" + d.name, lines.length, -1, "[EOF]", source));
            }
        }
        for (Define d : defines.values()) {
            if (d.isAddress && !d.resolved) {
                errors.add(new AssemblerError("Unresolved define " + d.name, lines.length, -1, "[EOF]", source));
            }
        }
        
        if (!errors.isEmpty()) {
            for (AssemblerError err : errors) {
                System.err.println(err.getPrintString());
            }
            return false;
        }
        return true;
    }
    
    protected <T extends Instruction> T add(T instruction) {
        instructions.add(instruction);
        memAddr += instruction.hasSecond() ? 8 : 4;
        if (instruction instanceof TempGoto tg) {
            tg.setAddress(memAddr);
        }
        return instruction;
    }

    protected Define addDefine(Define define) {
        defines.put(define.name, define);
        if (!define.isAddress)
            return define;
        if (define.valueArr == null) { // empty define
            defineEmpties.add(define);
        } else {
            defineOrder.add(define);
        }
        return define;
    }
    
    protected FunctionSymbol getFunction(int lineN) {
        for (FunctionSymbol f : symbols.functions.values()) {
            if (lineN >= f.sourceLine-1 && lineN <= f.endLine) {
                // System.out.println(String.format("Found function %s for line %d (start=%d, end=%d)", f.name, lineN, f.sourceLine-1, f.endLine));
                return f;
            }
        }
        return null;
    }
    
    public int[] build() {
        int[] arr = new int[(lastNonZeroAddress - startOffset) / 4];
        int outI = 0;
        for (Instruction instruction : instructions) {
            arr[outI++] = instruction.getBytecode();
            if (instruction.hasSecond()) {
                arr[outI++] = instruction.getSecondBytecode();
            }
        }
        return arr;
    }

    public static byte[] toBytes(int[] arr) {
        byte[] bArr = new byte[arr.length * 4];
        for (int i = 0; i < arr.length; i++) {
            int v = arr[i];
            bArr[i * 4] = (byte) ((v & 0xff00_0000) >> 24);
            bArr[i * 4 + 1] = (byte) ((v & 0x00ff_0000) >> 16);
            bArr[i * 4 + 2] = (byte) ((v & 0x0000_ff00) >> 8);
            bArr[i * 4 + 3] = (byte) (v & 0x0000_00ff);
        }
        return bArr;
    }

    private int getReg(String reg) {
        int r;
        switch (reg) {
            case "rPgm" -> {
                r = REG_PGM_PNTR;
            }
            case "rStack" -> {
                r = REG_STACK_PNTR;
            }
            case "rAF" -> {
                r = REG_ARITHMETIC_FLAG;
            }

            case "rPID" -> {
                r = REG_PID;
            }
            case "rMTbl" -> {
                r = REG_MEM_TABLE;
            }
            
            case "rPM" -> {
                r = REG_PRIVILEGED_MODE;
            }
            
            case "rIC" -> {
                r = REG_INTERRUPT;
            }
            case "rIH" -> {
                r = REG_INTR_HANDLER;
            }
            
            case "rID" -> {
                r = REG_CPU_ID;
            }

            case "rPgmI" -> {
                r = REG_PGM_PNTR_I;
            }
            case "rStackI" -> {
                r = REG_STACK_PNTR_I;
            }
            case "rAFI" -> {
                r = REG_ARITHMETIC_FLAG_I;
            }

            case "rPIDI" -> {
                r = REG_PID_I;
            }
            case "rMTblI" -> {
                r = REG_MEM_TABLE_I;
            }
            
            case "rPMI" -> {
                r = REG_PRIVILEGED_MODE_I;
            }
        
            default -> {
                if (reg.charAt(0) == 'r') {
                    if(reg.endsWith("I"))
                        r = Integer.parseInt(reg.substring(1,reg.length()-2)) + 0x10;
                    else
                        r = Integer.parseInt(reg.substring(1));
                } else {
                    try {
                        r = Integer.parseInt(reg);
                    } catch(NumberFormatException e) {
                        throw new RuntimeException("Expected register identifier, but found `"+reg+"` instead");
                    }
                }
            }
        }
        return r;
    }

    private Define getVal(String val) {
        int v = 0;
        if (val.equals("true")) {
            v = 1;
        } else if (val.equals("false")) {
            v = 0;
        } else if (defines.containsKey(val)) {
            return defines.get(val);
        } else if (val.startsWith("&")) {
            String n = val.substring(1);
            if (defines.containsKey(n)) {
                if(!defines.get(n).isAddress)
                    throw new RuntimeException("Symbol had no address");
                return defines.get(n);
                // v = symbols.variables.get(n).start;
                // if (v == -1)
                //     throw new RuntimeException("Symbol had no start address");
            } else if (labels.containsKey(n.substring(1))) {
                n = n.substring(1);
                return labels.get(n);
            } else {
                return null;
            }
        } else if (linker != null && linker.hasDefinition(val)) {
            v = linker.getDefinition(val);
        } else if (val.startsWith("0x")) {
            v = Integer.parseInt(val.substring(2).replace("_",""), 16);
        } else if (val.startsWith("0b")) {
            v = Integer.parseInt(val.substring(2).replace("_",""), 2);
        } else if (val.startsWith("'")) {
            if (val.startsWith("'\\")) {
                switch (val) {
                    case "'\\n'" -> {
                        v = (int) '\n';
                    }
                    case "'\\t'" -> {
                        v = (int) '\t';
                    }
                    case "'\\\\'" -> {
                        v = (int) '\\';
                    }
                }
            } else {
                v = (int) val.charAt(1);
            }
        } else {
            v = Integer.parseInt(val.replace("_",""));
        }
        return new Define("<literal>", v);
    }
    
    private static class Entry {

        public int instruction;
        public boolean literal;

        private Entry(int instruction) {
            this.instruction = instruction;
        }

        @SuppressWarnings("unused")
        public String toASMString() {
            if (literal) {
                return Integer.toHexString(instruction);
            }
            return translate(instruction);
        }

        public static Entry Literal(int val) {
            Entry e = new Entry(val);
            e.literal = true;
            return e;
        }

        public static Entry Direct(int instruction) {
            return new Entry(instruction);
        }

        public static Entry Load(int rg) {
            return new Entry(LOAD | (rg << 16));
        }

        public static Entry LoadMem(int rg, int ra) {
            return new Entry(LOAD | (rg << 16) | LOAD_MEM | ra);
        }
        public static Entry LoadMemShort(int rg, int ra) {
            return new Entry(LOAD | (rg << 16) | LOAD_MEM_SHORT | ra);
        }
        public static Entry LoadMemByte(int rg, int ra) {
            return new Entry(LOAD | (rg << 16) | LOAD_MEM_BYTE | ra);
        }

        public static Entry Math(MathOperator op, int rd, int ra, int rb) {
            return new Entry(MATH | op.value | ((rd & 0xf) << 16) | (ra << 8) | rb);
        }

        public static Entry MathInc(int rd, int inc) {
            if (inc < 0) {
                inc *= -1;
                inc &= 0x7fff;
                inc |= 0x8000;
            } else {
                inc -= 1;
                inc &= 0x7fff;
            }
            return new Entry(MATH | MathOperator.INC.value | ((rd & 0xf) << 16) | inc);
        }
        
        public static Entry Stack(boolean push, int rg) {
            return new Entry(STACK | (push ? 0x0 : STACK_POP) | rg);
        }

        public static Entry StackInc(int v) {
            return new Entry(STACK | STACK_INC | (v-1));
        }

        public static Entry StackDec(int v) {
            return new Entry(STACK | STACK_DEC | (v-1));
        }

        public static Entry SysCall(int function) {
            return new Entry(SYSCALL | (function & MASK_SYSCALL_FUNCTION));
        }

        public static Entry SysReturn() {
            return new Entry(SYSCALL | SYSCALL_RETURN);
        }

        public static Entry SysGoto(int rg) {
            return new Entry(SYSCALL | SYSCALL_GOTO | (rg & MASK_SYSCALL_RG));
        }

        public static Entry Interrupt(int op, int rg) {
            return new Entry(SYSCALL | SYSCALL_INTERRUPT | op | (rg & MASK_SYSCALL_RG));
        }

        public static Entry Set(boolean forced, ConditionalOperator op, int rg, int rd) {
            return new Entry(SET | (forced ? SET_FORCED : 0x00) | op.id | (rg << 8) | rd);
        }
    }

    private static class StoreEntry extends Entry {

        public StoreEntry(int rg, int size, int source, int ra) {
            super(STORE | (rg << 16) | size | source | ra);
        }

        public StoreEntry incRG() {
            instruction |= MASK_STORE_FLAG_INC_RG;
            return this;
        }
        public StoreEntry incRA() {
            instruction |= MASK_STORE_FLAG_INC_RA;
            return this;
        }
    }

    private static class GotoEntry extends Entry {

        public String target;
        public Entry next;

        private GotoEntry(int instruction, String target, Entry next) {
            super(instruction);
            this.target = target;
            this.next = next;
        }

        public static GotoEntry Unconditional(boolean relative, int ra, String target, Entry next) {
            return new GotoEntry(GOTO | (relative ? MASK_GOTO_REL : 0) | (ra << 8), target, next);
        }

        public static GotoEntry UnconditionalPush(boolean relative, int ra, String target, Entry next) {
            return new GotoEntry(GOTO | MASK_GOTO_PUSH | (relative ? MASK_GOTO_REL : 0) | (ra<<8), target, next);
        }

        public static GotoEntry UnconditionalPop() {
            return new GotoEntry(GOTO | MASK_GOTO_POP, "", null);
        }

        public static GotoEntry Conditional(boolean relative, ConditionalOperator op, int ra, int ro, String target, Entry next) {
            return new GotoEntry(GOTO | (relative ? MASK_GOTO_REL : 0) | op.id | (ra << 8) | ro, target, next);
        }
        public static GotoEntry ConditionalPush(boolean relative, ConditionalOperator op, int ra, int ro, String target, Entry next) {
            return new GotoEntry(GOTO | MASK_GOTO_PUSH | (relative ? MASK_GOTO_REL : 0) | op.id | (ra << 8) | ro, target, next);
        }
        public static GotoEntry ConditionalPop(ConditionalOperator op, int ro) {
            return new GotoEntry(GOTO | MASK_GOTO_POP | op.id | ro, "", null);
        }

        // public static GotoEntry Zero(boolean relative, int ra, int ro, String target, Entry next) {
        //     return new GotoEntry(GOTO | (relative ? GOTO_REL_EQ_ZERO : GOTO_EQ_ZERO) | (ra << 8) | ro, target, next);
        // }

        // public static GotoEntry ZeroPush(boolean relative, int ra, int ro, String target, Entry next) {
        //     return new GotoEntry(GOTO | (relative ? GOTO_PUSH_REL_EQ_ZERO : GOTO_PUSH_EQ_ZERO) | (ra << 8) | ro,
        //             target, next);
        // }

        // public static GotoEntry ZeroPop(int ro) {
        //     return new GotoEntry(GOTO | GOTO_POP_EQ_ZERO | ro, "", null);
        // }

        // public static GotoEntry LessEqual(boolean relative, int ra, int ro, String target, Entry next) {
        //     return new GotoEntry(GOTO | (relative ? GOTO_REL_LEQ_ZERO : GOTO_LEQ_ZERO) | (ra << 8) | ro, target, next);
        // }

        // public static GotoEntry LessEqualPush(boolean relative, int ra, int ro, String target, Entry next) {
        //     return new GotoEntry(GOTO | (relative ? GOTO_PUSH_REL_LEQ_ZERO : GOTO_PUSH_LEQ_ZERO) | (ra << 8) | ro,
        //             target, next);
        // }

        // public static GotoEntry LessEqualPop(int ro) {
        //     return new GotoEntry(GOTO | GOTO_POP_LEQ_ZERO | ro, "", null);
        // }

        // public static GotoEntry Greater(boolean relative, int ra, int ro, String target, Entry next) {
        //     return new GotoEntry(GOTO | (relative ? GOTO_REL_GT_ZERO : GOTO_GT_ZERO) | (ra << 8) | ro, target, next);
        // }

        // public static GotoEntry GreaterPush(boolean relative, int ra, int ro, String target, Entry next) {
        //     return new GotoEntry(GOTO | (relative ? GOTO_PUSH_REL_GT_ZERO : GOTO_PUSH_GT_ZERO) | (ra << 8) | ro,
        //             target, next);
        // }

        // public static GotoEntry GreaterPop(int ro) {
        //     return new GotoEntry(GOTO | GOTO_POP_GT_ZERO | ro, "", null);
        // }

        // public static GotoEntry NotZero(boolean relative, int ra, int ro, String target, Entry next) {
        //     return new GotoEntry(GOTO | (relative ? GOTO_REL_NOT_ZERO : GOTO_NOT_ZERO) | (ra << 8) | ro, target, next);
        // }

        // public static GotoEntry NotZeroPush(boolean relative, int ra, int ro, String target, Entry next) {
        //     return new GotoEntry(GOTO | (relative ? GOTO_PUSH_REL_NOT_ZERO : GOTO_PUSH_NOT_ZERO) | (ra << 8) | ro,
        //             target, next);
        // }

        // public static GotoEntry NotZeroPop(int ro) {
        //     return new GotoEntry(GOTO | GOTO_POP_NOT_ZERO | ro, "", null);
        // }

        public void setOffset(int offset) {
            next.instruction = offset;
            // instruction |= uint32ToInt8(offset) << 8;
        }

    }

    private static class MemSet {
        public final String name;
        public final int[] values;

        public MemSet(String name, int[] values) {
            this.name = name;
            this.values = values;
        }

        public MemSet(String name, byte[] bytes) {
            this.name = name;
            // bytes = values;
            values = new int[Math.ceilDiv(bytes.length, 4)];
            for (int i = 0; i < bytes.length; i += 4) {
                int v = ((int) bytes[i]) << 24;
                if(i+1 < bytes.length)
                    v |= ((int) bytes[i+1]) << 16;
                if(i+2 < bytes.length)
                    v |= ((int) bytes[i+2]) << 8;
                if(i+3 < bytes.length)
                    v |= (int) bytes[i + 3];
                values[i / 4] = v;
            }
        }

        public MemSet(String name, String str) {
            this.name = name;
            int len = str.length();
            // values = new int[len];
            // for (int i = 0; i < len; i++) {
            //     values[i] = str.charAt(i);
            // }
            
            values = new int[Math.ceilDiv(len, 4)];
            for (int i = 0; i < str.length(); i += 4) {
                int v = ((int) str.charAt(i)) << 24;
                if(i+1 < len)
                    v |= ((int) str.charAt(i+1)) << 16;
                if(i+2 < len)
                    v |= ((int) str.charAt(i+2)) << 8;
                if(i+3 < len)
                    v |= (int) str.charAt(i + 3);
                values[i / 4] = v;
            }
        }
    }

    public java.util.Set<java.util.Map.Entry<String, Integer>> getSyscallMapping() {
        return syscallMap.entrySet();
    }
    public java.util.Set<String> getSyscallMap() {
        return syscallDef.keySet();
    }

    public void setStartOffset(int i) {
        startOffset = i;
    }

    public void setKernalOffset() {
        startOffset = KERNAL_OFFSET;
    }
    public void setProcessOffset() {
        startOffset = PROCESS_OFFSET;
    }
}
