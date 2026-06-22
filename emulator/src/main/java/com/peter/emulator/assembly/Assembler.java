package com.peter.emulator.assembly;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.peter.emulator.MachineCode.*;

import com.peter.emulator.MachineCode.ConditionalOperator;
import com.peter.emulator.MachineCode.MathOperator;
import com.peter.emulator.assembly.SymbolFile.FunctionSymbol;
import com.peter.emulator.assembly.SymbolFile.ValueSymbol;
import com.peter.emulator.assembly.SymbolFile.VariableSymbol;

public class Assembler {

    private Entry[] data = null;
    private final HashMap<String, Integer> labels = new HashMap<>();
    private final HashMap<String, Integer> defines = new HashMap<>();
    private final ArrayList<MemSet> memSet = new ArrayList<>();
    protected HashMap<String, Integer> functions = new HashMap<>();
    protected HashMap<String, Integer> syscallDef = new HashMap<>();
    protected HashMap<String, Integer> syscallMap = new HashMap<>();

    protected Linker linker = null;
    protected String source = "[literal]";

    public SymbolFile symbols = new SymbolFile();

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

    public boolean assemble() {
        errors.clear();
        // mapping
        int addr = 2;
        int valAdd = 0;
        FunctionSymbol cFunction = null;
        for (int lineN = 0; lineN < lines.length; lineN++) {
            String line = lines[lineN].trim();
            if (line.isBlank() || line.startsWith("//"))
                continue;
            String[] parts = line.split("\s+");
            if (line.charAt(0) == ':') {
                String name = parts[0].substring(1);
                labels.put(name, addr*4);
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
                                    if (j+1 < str.length()) {
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
                            memSet.add(new MemSet(name, str));
                            valAdd += Math.ceilDiv(str.length(), 4);
                            // valAdd += str.length();
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
                                arr.add(getVal(m.group(1)));
                            }
                            int[] val = new int[arr.size()];
                            for (int j = 0; j < val.length; j++) {
                                val[j] = arr.get(j);
                            }
                            memSet.add(new MemSet(name, val));
                            valAdd += val.length;
                            if (type == null)
                                type = "uint32*";
                            symbols.addDefinition(new ValueSymbol(name, -1, -1, type, content), lineN + 1);
                        } else if (parts[2].startsWith("(")) {
                            Matcher m = ALLOC_PATTERN.matcher(parts[2]);
                            m.find();
                            int size = Math.ceilDiv(getVal(m.group(1)), 4);
                            memSet.add(new MemSet(name, new int[size]));
                            valAdd += size;
                            if (type == null)
                                type = "uint32[" + size + "]*";
                            symbols.addDefinition(new ValueSymbol(name, -1, -1, type, ""),
                                    lineN + 1);
                        } else {
                            int val = getVal(parts[2]);
                            defines.put(name, val);
                            if (type == null)
                                type = "uint32";
                            symbols.addDefinition(
                                    new ValueSymbol(name, -1, -1, "const "+type, val + ""),
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
                                    if (j+1 < str.length()) {
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
                            memSet.add(new MemSet(name, str));
                            valAdd += Math.ceilDiv(str.length(), 4);
                            // valAdd += str.length();
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
                                arr.add(getVal(m.group(1)));
                            }
                            int[] val = new int[arr.size()];
                            for (int j = 0; j < val.length; j++) {
                                val[j] = arr.get(j);
                            }
                            memSet.add(new MemSet(name, val));
                            valAdd += val.length;
                            if (type == null)
                                type = "uint32*";
                            symbols.addVariable(new VariableSymbol(name, -1, -1, type, content), lineN + 1);
                        } else if (parts[2].startsWith("(")) {
                            Matcher m = ALLOC_PATTERN.matcher(parts[2]);
                            m.find();
                            int size = Math.ceilDiv(getVal(m.group(1)), 4);
                            memSet.add(new MemSet(name, new int[size]));
                            valAdd += size;
                            if (type == null)
                                type = "uint32[" + size + "]*";
                            symbols.addVariable(new VariableSymbol(name, -1, -1, type, ""),
                                    lineN + 1);
                        } else {
                            int val = getVal(parts[2]);
                            memSet.add(new MemSet(name, new int[] {val}));
                            valAdd++;
                            if (type == null)
                                type = "uint32";
                            symbols.addVariable(
                                    new VariableSymbol(name, -1, -1, type, val + ""),
                                    lineN + 1);
                        }
                    }
                    case "function" -> {
                        if (cFunction != null) {
                            errors.add(new AssemblerError("Found #function definition within another function", lineN,
                                    0, line, source));
                            inSyscall = false;
                            continue;
                        }
                        String functionName = parts[1];
                        boolean isSyscall = functionName.startsWith("syscall::");
                        if (isSyscall) {
                            String syscallName = functionName.substring(9);
                            syscallDef.put(syscallName, addr);
                            inSyscall = true;
                            // System.out.println("Test?: " + functionName);
                        }
                        labels.put(functionName, addr*4);
                        functions.put(functionName, addr*4);
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
                                new FunctionSymbol(functionName, addr*4, -1, args.toArray(String[]::new), "void",
                                        isSyscall, false),
                                lineN + 1);
                    }
                    case "endfunction" -> {
                        if (cFunction == null) {
                            errors.add(new AssemblerError("Found #endfunction without matching #function", lineN, 0,
                                    line, source));
                            continue;
                        }
                        cFunction.end = addr*4;
                        cFunction.endLine = lineN;
                        if (parts.length >= 2 && !parts[1].startsWith("//"))
                            cFunction.rt = parts[1];
                        cFunction = null;
                    }
                    case "syscall" -> {
                        int index = getVal(parts[1]);
                        syscallMap.put(parts[2], index);
                        symbols.mapSyscall(parts[2], index);
                        // System.out.println(String.format("Added syscall 0x%x %s", index, parts[2]));
                    }
                    case "breakpoint" -> {

                    }
                }
                continue;
            }
            addr++;
            if (line.startsWith("LOAD") && !line.startsWith("LOAD MEM")) {
                addr++;
            } else if (line.startsWith("STORE")) {
                int next = 1;
                switch(parts[next]) {
                    case "SHORT", "BYTE" -> next++;
                }
                if (!parts[next].startsWith("r")) {
                    addr++;
                }
            } else if (line.startsWith("GOTO")) {
                if (parts.length < 2) {
                    System.err.println("invalid goto");
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
                if(pop)
                    continue;
                if (parts.length < nI) {
                    System.err.println("invalid goto");
                    errors.add(new AssemblerError("Invalid goto instruction", lineN,
                            line.length(), line, source));
                    continue;
                }
                if (parts.length > nI) {
                    switch(parts[nI]) {
                        case "EQ", "LEQ", "GT", "NEQ", "LT", "GEQ" -> nI += 2;
                        default -> {}
                    }
                }
                if (parts.length < nI) {
                    System.err.println("invalid goto");
                    errors.add(new AssemblerError("Invalid goto instruction", lineN,
                            line.length(), line, source));
                    continue;
                }
                if (!parts[nI].startsWith("r")) {
                    addr++;
                }
            }
        }
        if (cFunction != null) {
            errors.add(new AssemblerError("Un-ended function at end of file", lines.length, -1,
                                    "[EOF]", source));
        }
        if (!errors.isEmpty()) {
            System.err.println("Early exit: " + errors.size());
            for (AssemblerError err : errors) {
                System.err.println(err.getPrintString());
            }
            return false;
        }
        data = new Entry[addr + valAdd];
        for (MemSet set : memSet) {
            defines.put(set.name, addr*4);
            symbols.updateDefinition(set.name, addr*4, (addr + set.values.length) * 4 - 1);
            for (int i = 0; i < set.values.length; i++) {
                data[addr++] = Entry.Literal(set.values[i]);
            }
        }
        // converting
        addr = 0;
        if (labels.containsKey("__start")) {
            Entry next = new Entry(0);
            GotoEntry entry = GotoEntry.Unconditional(true, 0, "__start", next);
            data[addr++] = entry;
            data[addr++] = next;
        } else {
            System.out.println("File did not have __start");
            data[addr++] = Entry.Direct(NO_OP);
            data[addr++] = Entry.Direct(NO_OP);
        }
        for (int lineN = 0; lineN < lines.length; lineN++) {
            try {
                String line = lines[lineN].trim();
                if (line.isBlank() || line.startsWith("//"))
                    continue;
                if (line.startsWith("#") || line.startsWith(":")) { // compiler instruction
                    if (line.startsWith("#breakpoint")) {
                        symbols.addBreakpoint(addr * 4 - 4);
                    }
                    continue;
                }
                String[] parts = line.split("\s+");
                switch (parts[0]) {
                    case "HALT" -> {
                        data[addr++] = (Entry.Direct(0xffff_ffff));
                    }
                    case "LOAD" -> {
                        if (parts.length < 2) {
                            errors.add(new AssemblerError("Invalid load instruction: LOAD <MEM> [rg] [ra|val]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        if (parts[1].equals("MEM")) {
                            if (parts.length < 4) {
                                errors.add(new AssemblerError("Invalid load instruction: LOAD MEM <SHORT|BYTE> [rg] [ra]", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            switch (parts[2]) {
                                case "SHORT" -> {
                                    int rg = getReg(parts[3]);
                                    int ra = getReg(parts[4]);
                                    data[addr++] = (Entry.LoadMemShort(rg, ra));
                                }
                                case "BYTE" -> {
                                    int rg = getReg(parts[3]);
                                    int ra = getReg(parts[4]);
                                    data[addr++] = (Entry.LoadMemByte(rg, ra));
                                }
                                default -> {
                                    int rg = getReg(parts[2]);
                                    int ra = getReg(parts[3]);
                                    data[addr++] = (Entry.LoadMem(rg, ra));
                                }
                            }
                        } else {
                            if (parts.length < 3) {
                                errors.add(new AssemblerError("Invalid load instruction: LOAD [rg] [val]", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            int rg = getReg(parts[1]);
                            data[addr++] = (Entry.Load(rg));
                            int val = getVal(parts[2]);
                            data[addr++] = (Entry.Literal(val));
                        }
                    }
                    case "COPY" -> {
                        if (parts.length < 3) {
                            errors.add(new AssemblerError("Invalid copy instruction: COPY <SHORT|BYTE> <MEM> [rs] [rd] <INC_RA>", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        int size = STORE_SIZE_WORD;
                        int next = 1;
                        switch(parts[next]) {
                            case "SHORT" -> {size = STORE_SIZE_SHORT;next++;}
                            case "BYTE" -> {size = STORE_SIZE_BYTE;next++;}
                        }
                        StoreEntry entry;
                        if (parts[next].equals("MEM")) {
                            next++;
                            if (parts.length < next+2) {
                                errors.add(new AssemblerError("Invalid copy instruction: COPY <SHORT|BYTE> MEM [rs] [rd] <INC_RG> <INC_RA>", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            int rs = getReg(parts[next++]);
                            int rd = getReg(parts[next++]);
                            entry = new StoreEntry(rs, size, STORE_SOURCE_MEM, rd);
                        } else {
                            if (parts.length < next+2) {
                                errors.add(new AssemblerError("Invalid copy instruction: COPY <SHORT|BYTE> [rs] [rd] <INC_RG> <INC_RA>", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            int rs = getReg(parts[next++]);
                            int rd = getReg(parts[next++]);
                            entry = new StoreEntry(rs, size, STORE_SOURCE_REG_REG, rd);
                        }
                        data[addr++] = entry;
                        if(parts.length > next) {
                            switch(parts[next++]) {
                                case "INC_RG" -> entry.incRG();
                                case "INC_RA" -> entry.incRA();
                            }
                        }
                        if(parts.length > next) {
                            switch(parts[next++]) {
                                case "INC_RG" -> entry.incRG();
                                case "INC_RA" -> entry.incRA();
                            }
                        }
                    }
                    case "STORE" -> {
                        if (parts.length < 2) {
                            errors.add(new AssemblerError("Invalid copy instruction: STORE <SHORT|BYTE> VAL [ra] [val] <INC_RA> or STORE <SHORT|BYTE> [val|rg] [ra] <INC_RA>", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        int size = STORE_SIZE_WORD;
                        int next = 1;
                        switch(parts[next]) {
                            case "SHORT" -> {size = STORE_SIZE_SHORT;next++;}
                            case "BYTE" -> {size = STORE_SIZE_BYTE;next++;}
                        }
                        StoreEntry entry;
                        if (parts[next].equals("VAL")) {
                            if (parts.length < next+3) {
                                errors.add(new AssemblerError("Invalid copy instruction: STORE <SHORT|BYTE> VAL [ra] [val] <INC_RA>", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            next++;
                            int ra = getReg(parts[next++]);
                            int val = getVal(parts[next++]);
                            entry = new StoreEntry(0, size, STORE_SOURCE_VAL, ra);
                            data[addr++] = entry;
                            data[addr++] = (Entry.Literal(val));
                        } else if (!parts[next].startsWith("r")) {
                            if (parts.length < next+2) {
                                errors.add(new AssemblerError("Invalid copy instruction: STORE <SHORT|BYTE> [rg|val] [ra] <INC_RA>", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            int ra = getReg(parts[next++]);
                            int val = getVal(parts[next++]);
                            entry = new StoreEntry(0, size, STORE_SOURCE_VAL, ra);
                            data[addr++] = entry;
                            data[addr++] = (Entry.Literal(val));
                        } else {
                            if (parts.length < next+2) {
                                errors.add(new AssemblerError("Invalid copy instruction: STORE <SHORT|BYTE> [rg|val] [ra] <INC_RA>", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            int rg = getReg(parts[next++]);
                            int ra = getReg(parts[next++]);
                            entry = new StoreEntry(rg, size, STORE_SOURCE_REG, ra);
                            data[addr++] = entry;
                        }
                        if(parts.length > next) {
                            switch(parts[next++]) {
                                case "INC_RG" -> entry.incRG();
                                case "INC_RA" -> entry.incRA();
                            }
                        }
                        if(parts.length > next) {
                            switch(parts[next++]) {
                                case "INC_RG" -> entry.incRG();
                                case "INC_RA" -> entry.incRA();
                            }
                        }
                    }
                    case "ADD" -> {
                        if (parts.length < 4) {
                            errors.add(new AssemblerError("Invalid add instruction: ADD [rd] [ra] [rb]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        int rd = getReg(parts[1]);
                        int ra = getReg(parts[2]);
                        int rb = getReg(parts[3]);
                        data[addr++] = (Entry.Math(MathOperator.ADD, rd, ra, rb));
                    }
                    case "SUB" -> {
                        if (parts.length < 4) {
                            errors.add(new AssemblerError("Invalid sub instruction: SUB [rd] [ra] [rb]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        int rd = getReg(parts[1]);
                        int ra = getReg(parts[2]);
                        int rb = getReg(parts[3]);
                        data[addr++] = (Entry.Math(MathOperator.SUB, rd, ra, rb));
                    }
                    case "INC" -> {
                        if (parts.length < 2) {
                            errors.add(new AssemblerError("Invalid inc instruction: INC [rd] <[val]>", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        int rd = getReg(parts[1]);
                        int inc = 1;
                        if (parts.length >= 3 && !parts[2].startsWith("//")) {
                            inc = getVal(parts[2]);
                        }
                        data[addr++] = Entry.MathInc(rd, inc);
                    }
                    case "MUL" -> {
                        if (parts.length < 4) {
                            errors.add(new AssemblerError("Invalid mul instruction: MUL [rd] [ra] [rb]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        int rd = getReg(parts[1]);
                        int ra = getReg(parts[2]);
                        int rb = getReg(parts[3]);
                        data[addr++] = (Entry.Math(MathOperator.MUL, rd, ra, rb));
                    }
                    case "AND" -> {
                        if (parts.length < 4) {
                            errors.add(new AssemblerError("Invalid mul instruction: AND [rd] [ra] [rb]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        int rd = getReg(parts[1]);
                        int ra = getReg(parts[2]);
                        int rb = getReg(parts[3]);
                        data[addr++] = (Entry.Math(MathOperator.AND, rd, ra, rb));
                    }
                    case "OR" -> {
                        if (parts.length < 4) {
                            errors.add(new AssemblerError("Invalid mul instruction: OR [rd] [ra] [rb]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        int rd = getReg(parts[1]);
                        int ra = getReg(parts[2]);
                        int rb = getReg(parts[3]);
                        data[addr++] = (Entry.Math(MathOperator.OR, rd, ra, rb));
                    }
                    case "NOR" -> {
                        if (parts.length < 4) {
                            errors.add(new AssemblerError("Invalid mul instruction: NOR [rd] [ra] [rb]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        int rd = getReg(parts[1]);
                        int ra = getReg(parts[2]);
                        int rb = getReg(parts[3]);
                        data[addr++] = (Entry.Math(MathOperator.NOR, rd, ra, rb));
                    }
                    case "LSH" -> {
                        if (parts.length < 4) {
                            errors.add(new AssemblerError("Invalid mul instruction: LSH [rd] [ra] [rb]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        int rd = getReg(parts[1]);
                        int ra = getReg(parts[2]);
                        int rb = getVal(parts[3]);
                        data[addr++] = (Entry.Math(MathOperator.LSHIFT, rd, ra, rb));
                    }
                    case "RSH" -> {
                        if (parts.length < 4) {
                            errors.add(new AssemblerError("Invalid mul instruction: RSH [rd] [ra] [rb]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        int rd = getReg(parts[1]);
                        int ra = getReg(parts[2]);
                        int rb = getVal(parts[3]);
                        data[addr++] = (Entry.Math(MathOperator.RSHIFT, rd, ra, rb));
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
                            cond = switch(parts[nI]) {
                                case "EQ" -> ConditionalOperator.EQ_ZERO;
                                case "LEQ" -> ConditionalOperator.LEQ_ZERO;
                                case "GT" -> ConditionalOperator.GT_ZERO;
                                case "NEQ" -> ConditionalOperator.NEQ_ZERO;
                                case "LT" -> ConditionalOperator.LT_ZERO;
                                case "GEQ" -> ConditionalOperator.GEQ_ZERO;

                                default -> ConditionalOperator.UNCONDITIONAL;
                            };
                        }
                        int ro = 0;
                        if (cond != ConditionalOperator.UNCONDITIONAL) {
                            nI++;
                            if (parts.length < nI) {
                                errors.add(new AssemblerError("Invalid goto instruction", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            ro = getReg(parts[nI++]);
                        }
                        String target = "";
                        int ra = 0;
                        boolean relative = (!pop);
                        if (!pop) {
                            if (parts.length < nI) {
                                errors.add(new AssemblerError("Invalid goto instruction", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            if (parts[nI].startsWith("r")) {
                                ra = getReg(parts[nI]);
                                relative = false;
                            } else {
                                target = parts[nI].substring(1);
                            }
                        }
                        GotoEntry entry;
                        Entry next = relative ? new Entry(0) : null;
                        if (cond == ConditionalOperator.UNCONDITIONAL) {
                            if (push) {
                                entry = GotoEntry.UnconditionalPush(relative, ra, target, next);
                            } else if (pop) {
                                entry = GotoEntry.UnconditionalPop();
                            } else {
                                entry = GotoEntry.Unconditional(relative, ra, target, next);
                            }
                        } else {
                            if (push) {
                                entry = GotoEntry.ConditionalPush(relative, cond, ra, ro, target, next);
                            } else if (pop) {
                                entry = GotoEntry.ConditionalPop(cond, ro);
                            } else {
                                entry = GotoEntry.Conditional(relative, cond, ra, ro, target, next);
                            }
                        }
                        data[addr++] = (entry);
                        if(relative)
                            data[addr++] = next;
                    }
                    case "SET" -> {
                        boolean forced = false;
                        if (parts.length == 5) {
                            if(!parts[1].equals("FORCE")) {
                                errors.add(new AssemblerError("Invalid set instruction: SET (FORCED) <EQ|LEQ|GEQ|NEQ> [rg] [rd]", lineN, 0, line, source));
                                continue;
                            }
                            forced = true;
                        }
                        String op = parts[forced ? 2 : 1];
                        int rg = getReg(parts[forced ? 3 : 2]);
                        int rd = getReg(parts[forced ? 4 : 3]);
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
                            errors.add(new AssemblerError("Invalid set instruction operator: SET (FORCED) <EQ|LEQ|GEQ|NEQ> [rg] [rd]", lineN, line.length(), line, source));
                            continue;
                        }
                        data[addr++] = Entry.Set(forced, cond, rg, rd);
                    }
                    case "STACK" -> {
                        if(parts.length == 2) {
                            if(parts[1].equals("INC")) {
                                data[addr++] = Entry.StackInc(1);
                                continue;
                            } else if(parts[1].equals("DEC")) {
                                data[addr++] = Entry.StackDec(1);
                                continue;
                            }
                        }
                        if (parts.length < 3) {
                            errors.add(new AssemblerError("Invalid stack instruction: STACK (PUSH|POP) [rg] | STACK (INC|DEC) ([value])", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        if(parts[1].equals("INC"))
                                data[addr++] = Entry.StackInc(getVal(parts[2]));
                        else if(parts[1].equals("DEC"))
                            data[addr++] = Entry.StackDec(getVal(parts[2]));
                        else
                            data[addr++] = Entry.Stack(parts[1].equals("PUSH"), getReg(parts[2]));
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
                            func = getVal(parts[1]);
                        data[addr++] = Entry.SysCall(func);
                    }
                    case "SYSRETURN" -> {
                        FunctionSymbol funcSym = getFunction(lineN);
                        if (funcSym != null && !funcSym.isSyscall) {
                            errors.add(new AssemblerError(
                                    "Found syscall return outside of system call; Function was "+funcSym.name, lineN,
                                    0, line, source));
                            continue;
                        }
                        data[addr++] = Entry.SysReturn();
                    }
                    case "SYSGOTO" -> {
                        if (parts.length < 2) {
                            errors.add(new AssemblerError("Invalid system goto instruction: SYSGOTO [rg]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        int rg = getReg(parts[1]);
                        data[addr++] = Entry.SysGoto(rg);
                    }
                    case "INTERRUPT" -> {
                        if (parts.length < 2) {
                            errors.add(new AssemblerError("Invalid interrupt instruction: INTERRUPT <RET|[rg]|[val]>",
                                    lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        if (parts[1].equals("RET")) {
                            // System.out.println("ir");
                            data[addr++] = Entry.Interrupt(SYSCALL_INTERRUPT_RET, 0);
                        } else if (parts[1].startsWith("r")) {
                            data[addr++] = Entry.Interrupt(SYSCALL_INTERRUPT_RG, getReg(parts[1]));
                        } else {
                            data[addr++] = Entry.Interrupt(SYSCALL_INTERRUPT_VAL, 0);
                            data[addr++] = Entry.Literal(getVal(parts[1]));
                        }
                    }
                    default -> {
                        errors.add(new AssemblerError(String.format("Unknown instruction: `%s`", parts[0]), lineN, 0,
                                line, source));
                        continue;
                    }
                }
            } catch (Exception e) {
                System.err.println(e);
                e.printStackTrace();
                System.err.println("Error parsing assembly at line " + (lineN + 1) + " in file " + source);
                throw e;
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
        int[] arr = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            Entry entry = data[i];
            if (entry instanceof GotoEntry ge) {
                if (!ge.target.isBlank()) {
                    if(!labels.containsKey(ge.target))
                        throw new RuntimeException("Invalid label "+ge.target);
                    int tW = labels.get(ge.target);
                    int off = tW - (i*4) - 8;
                    ge.setOffset(off);
                }
            }
            // System.out.println(String.format("[%x] %s", i, entry.toASMString()));
            arr[i] = entry.instruction;
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
        
            default -> {
                if (reg.charAt(0) == 'r') {
                    if(reg.endsWith("I"))
                        r = Integer.parseInt(reg.substring(1,reg.length()-2)) + 0x10;
                    else
                        r = Integer.parseInt(reg.substring(1));
                } else {
                    r = Integer.parseInt(reg);
                }
            }
        }
        return r;
    }

    private int getVal(String val) {
        int v = 0;
        if (val.equals("true")) {
            v = 1;
        } else if (val.equals("false")) {
            v = 0;
        } else if (defines.containsKey(val)) {
            v = defines.get(val);
        } else if (val.startsWith("&")) {
            String n = val.substring(1);
            if (defines.containsKey(n)) {
                v = symbols.variables.get(n).start;
                if (v == -1)
                    throw new RuntimeException("Symbol had no start address");
            } else if (labels.containsKey(n.substring(1))) {
                n = n.substring(1);
                v = labels.get(n);
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
        return v;
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

        // // store a word from reg[ra] into mem[reg[rg]]
        // public static Entry Store(int rg, int ra) {
        //     return new Entry(STORE | (rg << 16) | STORE_SIZE_WORD | STORE_SOURCE_REG | ra);
        // }
        // public static Entry StoreShort(int rg, int ra) {
        //     return new Entry(STORE | (rg << 16) | STORE_SIZE_SHORT | STORE_SOURCE_REG | ra);
        // }
        // public static Entry StoreByte(int rg, int ra) {
        //     return new Entry(STORE | (rg << 16) | STORE_SIZE_BYTE | STORE_SOURCE_REG | ra);
        // }

        // public static Entry StoreVal(int ra) {
        //     return new Entry(STORE | STORE_SIZE_WORD | STORE_SOURCE_VAL | ra);
        // }
        // public static Entry StoreValShort(int ra) {
        //     return new Entry(STORE | STORE_SIZE_SHORT | STORE_SOURCE_VAL | ra);
        // }
        // public static Entry StoreValByte(int ra) {
        //     return new Entry(STORE | STORE_SIZE_BYTE | STORE_SOURCE_VAL | ra);
        // }

        // public static Entry CopyMem(int rs, int rd) {
        //     return new Entry(STORE | (rs << 16) | STORE_SIZE_WORD | STORE_SOURCE_MEM | rd);
        // }
        // public static Entry CopyMemShort(int rs, int rd) {
        //     return new Entry(STORE | (rs << 16) | STORE_SIZE_SHORT | STORE_SOURCE_MEM | rd);
        // }
        // public static Entry CopyMemByte(int rs, int rd) {
        //     return new Entry(STORE | (rs << 16) | STORE_SIZE_BYTE | STORE_SOURCE_MEM | rd);
        // }

        // public static Entry CopyReg(int rs, int rd) {
        //     return new Entry(STORE | (rs << 16) | STORE_SIZE_WORD | STORE_SOURCE_REG_REG | rd);
        // }
        // public static Entry CopyRegShort(int rs, int rd) {
        //     return new Entry(STORE | (rs << 16) | STORE_SIZE_SHORT | STORE_SOURCE_REG_REG | rd);
        // }
        // public static Entry CopyRegByte(int rs, int rd) {
        //     return new Entry(STORE | (rs << 16) | STORE_SIZE_BYTE | STORE_SOURCE_REG_REG | rd);
        // }

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
            return new Entry(STACK | (rg << 16) | (push ? 0x0 : STACK_POP));
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
            return new Entry(SET | (forced ? SET_FORCED : 0x00) | op.value | (rg << 8) | rd);
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
            return new GotoEntry(GOTO | (relative ? MASK_GOTO_REL : 0) | op.value | (ra << 8) | ro, target, next);
        }
        public static GotoEntry ConditionalPush(boolean relative, ConditionalOperator op, int ra, int ro, String target, Entry next) {
            return new GotoEntry(GOTO | MASK_GOTO_PUSH | (relative ? MASK_GOTO_REL : 0) | op.value | (ra << 8) | ro, target, next);
        }
        public static GotoEntry ConditionalPop(ConditionalOperator op, int ro) {
            return new GotoEntry(GOTO | MASK_GOTO_POP | op.value | ro, "", null);
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

    public Set<java.util.Map.Entry<String, Integer>> getSyscallMapping() {
        return syscallMap.entrySet();
    }
    public Set<String> getSyscallMap() {
        return syscallDef.keySet();
    }
}
