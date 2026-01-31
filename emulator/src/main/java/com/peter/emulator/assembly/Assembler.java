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
import com.peter.emulator.assembly.SymbolFile.FunctionSymbol;
import com.peter.emulator.assembly.SymbolFile.ValueSymbol;

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

    private static final Pattern DEFINE_ARRAY_PATTERN = Pattern.compile("#define\\s+\\w+\\s+\\[([^\\]]+)\\]");
    private static final Pattern DEFINE_ARRAY_VALUE_PATTERN = Pattern.compile("(0x[0-9a-f]+|\\d+|\\w+)(?:,\\s*)?");
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
        int addr = 0;
        int valAdd = 0;
        FunctionSymbol cFunction = null;
        for (int lineN = 0; lineN < lines.length; lineN++) {
            String line = lines[lineN].trim();
            if (line.isBlank() || line.startsWith("//"))
                continue;
            String[] parts = line.split("\s+");
            if (line.charAt(0) == ':') {
                String name = parts[0].substring(1);
                labels.put(name, addr);
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
                        if (parts[2].startsWith("\"")) {
                            Matcher m = STRING_PATTERN.matcher(line);
                            m.find();
                            String str = m.group(1);
                            String str2 = "";
                            for (int j = 0; j < str.length(); j++) {
                                char c = str.charAt(j);
                                if (c == '\\') {
                                    if (j > 0 && (str.charAt(j - 1) != '\\')) {
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
                                        }
                                    }
                                } else {
                                    str2 += c;
                                }
                            }
                            str = str2;
                            int[] val = new int[str.length()];
                            for (int j = 0; j < val.length; j++) {
                                val[j] = (int) str.charAt(j);
                            }
                            memSet.add(new MemSet(name, val));
                            valAdd += val.length;
                            symbols.addDefinition(new ValueSymbol(name, -1, -1, "char*", str), lineN + 1);
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
                            symbols.addDefinition(new ValueSymbol(name, -1, -1, "uint32*", content), lineN + 1);
                        } else if (parts[2].startsWith("(")) {
                            Matcher m = ALLOC_PATTERN.matcher(parts[2]);
                            m.find();
                            int size = getVal(m.group(1));
                            memSet.add(new MemSet(name, new int[size]));
                            valAdd += size;
                            symbols.addDefinition(new ValueSymbol(name, -1, -1, "uint32[" + size + "]*", ""),
                                    lineN + 1);
                        } else {
                            int val = getVal(parts[2]);
                            defines.put(name, val);
                            symbols.addDefinition(
                                    new ValueSymbol(name, -1, -1,
                                            parts[2].startsWith("'") ? "const char" : "const uint32", val + ""),
                                    lineN + 1);
                        }
                    }
                    case "var" -> {
                        String name = parts[1];
                        if (parts[2].startsWith("\"")) {
                            Matcher m = STRING_PATTERN.matcher(line);
                            m.find();
                            String str = m.group(1);
                            String str2 = "";
                            for (int j = 0; j < str.length(); j++) {
                                char c = str.charAt(j);
                                if (c == '\\') {
                                    if (j > 0 && (str.charAt(j - 1) != '\\')) {
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
                                        }
                                    }
                                } else {
                                    str2 += c;
                                }
                            }
                            str = str2;
                            int[] val = new int[str.length()];
                            for (int j = 0; j < val.length; j++) {
                                val[j] = (int) str.charAt(j);
                            }
                            memSet.add(new MemSet(name, val));
                            valAdd += val.length;
                            symbols.addDefinition(new ValueSymbol(name, -1, -1, "char*", str), lineN + 1);
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
                            symbols.addDefinition(new ValueSymbol(name, -1, -1, "uint32*", content), lineN + 1);
                        } else if (parts[2].startsWith("(")) {
                            Matcher m = ALLOC_PATTERN.matcher(parts[2]);
                            m.find();
                            int size = getVal(m.group(1));
                            memSet.add(new MemSet(name, new int[size]));
                            valAdd += size;
                            symbols.addDefinition(new ValueSymbol(name, -1, -1, "uint32[" + size + "]*", ""),
                                    lineN + 1);
                        } else {
                            int val = getVal(parts[2]);
                            memSet.add(new MemSet(name, new int[] {val}));
                            valAdd++;
                            symbols.addDefinition(
                                    new ValueSymbol(name, -1, -1,
                                            parts[2].startsWith("'") ? "char" : "uint32", val + ""),
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
                        labels.put(functionName, addr);
                        functions.put(functionName, addr);
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
                                new FunctionSymbol(functionName, addr, -1, args.toArray(String[]::new), "void",
                                        isSyscall, false),
                                lineN + 1);
                    }
                    case "endfunction" -> {
                        if (cFunction == null) {
                            errors.add(new AssemblerError("Found #endfunction without matching #function", lineN, 0,
                                    line, source));
                            continue;
                        }
                        cFunction.end = addr;
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
                }
                continue;
            }
            addr++;
            if (line.startsWith("LOAD") && !line.startsWith("LOAD MEM")) {
                addr++;
            } else if (line.startsWith("STORE")) {
                if (!parts[1].startsWith("r")) {
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
            defines.put(set.name, addr);
            symbols.updateDefinition(set.name, addr, addr + set.value.length - 1);
            for (int i = 0; i < set.value.length; i++) {
                data[addr++] = Entry.Literal(set.value[i]);
            }
        }
        // converting
        addr = 0;
        for (int lineN = 0; lineN < lines.length; lineN++) {
            try {
                String line = lines[lineN].trim();
                if (line.isBlank() || line.startsWith("//"))
                    continue;
                if (line.startsWith("#") || line.startsWith(":")) { // compiler instruction
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
                                errors.add(new AssemblerError("Invalid load instruction: LOAD MEM [rg] [ra]", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            int rg = getReg(parts[2]);
                            int ra = getReg(parts[3]);
                            data[addr++] = (Entry.LoadMem(rg, ra));
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
                        if (parts.length < 2) {
                            errors.add(new AssemblerError("Invalid copy instruction: COPY <MEM> [rs] [rd]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        if (parts[1].equals("MEM")) {
                            if (parts.length < 4) {
                                errors.add(new AssemblerError("Invalid copy instruction: COPY MEM [rs] [rd]", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            int rs = getReg(parts[2]);
                            int rd = getReg(parts[3]);
                            data[addr++] = (Entry.CopyMem(rs, rd));
                        } else {
                            if (parts.length < 3) {
                                errors.add(new AssemblerError("Invalid copy instruction: COPY [rs] [rd]", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            int rs = getReg(parts[1]);
                            int rd = getReg(parts[2]);
                            data[addr++] = (Entry.Copy(rs, rd));
                        }
                    }
                    case "STORE" -> {
                        if (parts.length < 2) {
                            errors.add(new AssemblerError("Invalid copy instruction: COPY VAL [ra] [val] or COPY [val|rg] [ra]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        if (parts[1].equals("VAL")) {
                            if (parts.length < 4) {
                                errors.add(new AssemblerError("Invalid copy instruction: COPY VAL [ra] [val]", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            int ra = getReg(parts[2]);
                            int val = getVal(parts[3]);
                            data[addr++] = (Entry.StoreVal(ra));
                            data[addr++] = (Entry.Literal(val));
                        } else if (!parts[1].startsWith("r")) {
                            if (parts.length < 3) {
                                errors.add(new AssemblerError("Invalid copy instruction: COPY [rg|val] [ra]", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            int ra = getReg(parts[2]);
                            int val = getVal(parts[1]);
                            data[addr++] = (Entry.StoreVal(ra));
                            data[addr++] = (Entry.Literal(val));
                        } else {
                            if (parts.length < 3) {
                                errors.add(new AssemblerError("Invalid copy instruction: COPY [rg|val] [ra]", lineN,
                                        line.length(), line, source));
                                continue;
                            }
                            int rg = getReg(parts[1]);
                            int ra = getReg(parts[2]);
                            data[addr++] = (Entry.Store(rg, ra));
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
                        data[addr++] = (Entry.Math(MATH_ADD, rd, ra, rb));
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
                        data[addr++] = (Entry.Math(MATH_SUB, rd, ra, rb));
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
                            errors.add(new AssemblerError("Invalid mul instruction: SUB [rd] [ra] [rb]", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        int rd = getReg(parts[1]);
                        int ra = getReg(parts[2]);
                        int rb = getReg(parts[3]);
                        data[addr++] = (Entry.Math(MATH_MUL, rd, ra, rb));
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
                        boolean eq = false;
                        boolean leq = false;
                        boolean gt = false;
                        boolean neq = false;
                        if (parts.length < nI) {
                            errors.add(new AssemblerError("Invalid goto instruction", lineN,
                                    line.length(), line, source));
                            continue;
                        }
                        if (parts.length > nI) {
                            eq = parts[nI].equals("EQ");
                            leq = parts[nI].equals("LEQ");
                            gt = parts[nI].equals("GT");
                            neq = parts[nI].equals("NEQ");
                        }
                        int ro = 0;
                        if (eq || leq || gt || neq) {
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
                        boolean relative = true;
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
                        if (eq) {
                            if (push) {
                                entry = GotoEntry.ZeroPush(relative, ra, ro, target);
                            } else if (pop) {
                                entry = GotoEntry.ZeroPop(ro);
                            } else {
                                entry = GotoEntry.Zero(relative, ra, ro, target);
                            }
                        } else if (leq) {
                            if (push) {
                                entry = GotoEntry.LessEqualPush(relative, ra, ro, target);
                            } else if (pop) {
                                entry = GotoEntry.LessEqualPop(ro);
                            } else {
                                entry = GotoEntry.LessEqual(relative, ra, ro, target);
                            }
                        } else if (gt) {
                            if (push) {
                                entry = GotoEntry.GreaterPush(relative, ra, ro, target);
                            } else if (pop) {
                                entry = GotoEntry.GreaterPop(ro);
                            } else {
                                entry = GotoEntry.Greater(relative, ra, ro, target);
                            }
                        } else if (neq) {
                            if (push) {
                                entry = GotoEntry.NotZeroPush(relative, ra, ro, target);
                            } else if (pop) {
                                entry = GotoEntry.NotZeroPop(ro);
                            } else {
                                entry = GotoEntry.NotZero(relative, ra, ro, target);
                            }
                        } else {
                            if (push) {
                                entry = GotoEntry.UnconditionalPush(relative, ra, target);
                            } else if (pop) {
                                entry = GotoEntry.UnconditionalPop();
                            } else {
                                entry = GotoEntry.Unconditional(relative, ra, target);
                            }
                        }
                        data[addr++] = (entry);
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
                    int tW = labels.get(ge.target);
                    int off = tW - i - 1;
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
            case "rIR" -> {
                r = REG_INTR_RSP;
            }
        
            default -> {
                if (reg.charAt(0) == 'r') {
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
        } else if (val.startsWith("&") && defines.containsKey(val.substring(1))) {
            String n = val.substring(1);
            v = symbols.definitions.get(val.substring(1)).start;
            if(v == -1)
                throw new RuntimeException("Symbol had no start address");
        } else if (linker != null && linker.hasDefinition(val)) {
            v = linker.getDefinition(val);
        } else if (val.startsWith("0x")) {
            v = Integer.parseInt(val.substring(2), 16);
        } else if (val.startsWith("0b")) {
            v = Integer.parseInt(val.substring(2), 2);
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
            v = Integer.parseInt(val);
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
            return new Entry(LOAD | (rg << 16) | MASK_LOAD_MEM | ra);
        }

        public static Entry Copy(int rs, int rd) {
            return new Entry(STORE | (rs << 16) | rd);
        }

        public static Entry Store(int rg, int ra) {
            return new Entry(STORE | (rg << 16) | STORE_MEM | ra);
        }

        public static Entry StoreVal(int ra) {
            return new Entry(STORE | STORE_VAL | ra);
        }

        public static Entry CopyMem(int rs, int rd) {
            return new Entry(STORE | (rs << 16) | STORE_MEM_COPY | rd);
        }

        public static Entry Math(int op, int rd, int ra, int rb) {
            return new Entry(MATH | op | ((rd & 0xf) << 16) | (ra << 8) | rb);
        }

        public static Entry MathInc(int rd, int inc) {
            if (inc < 0) {
                inc += 1;
                inc *= -1;
                inc &= 0x7fff;
                inc |= 0x8000;
            } else {
                inc -= 1;
                inc &= 0x7fff;
            }
            return new Entry(MATH | MATH_INC | ((rd & 0xf) << 16) | inc);
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
    }

    private static class GotoEntry extends Entry {

        public String target;

        private GotoEntry(int instruction, String target) {
            super(instruction);
            this.target = target;
        }

        public static GotoEntry Unconditional(boolean relative, int ra, String target) {
            return new GotoEntry(GOTO | (relative ? GOTO_REL_UNCD : GOTO_UNCD) | (ra << 8), target);
        }

        public static GotoEntry UnconditionalPush(boolean relative, int ra, String target) {
            return new GotoEntry(GOTO | (relative ? GOTO_PUSH_REL_UNCD : GOTO_PUSH_UNCD) | (ra << 8), target);
        }

        public static GotoEntry UnconditionalPop() {
            return new GotoEntry(GOTO | GOTO_POP_UNCD, "");
        }

        public static GotoEntry Zero(boolean relative, int ra, int ro, String target) {
            return new GotoEntry(GOTO | (relative ? GOTO_REL_EQ_ZERO : GOTO_EQ_ZERO) | (ra << 8) | ro, target);
        }

        public static GotoEntry ZeroPush(boolean relative, int ra, int ro, String target) {
            return new GotoEntry(GOTO | (relative ? GOTO_PUSH_REL_EQ_ZERO : GOTO_PUSH_EQ_ZERO) | (ra << 8) | ro,
                    target);
        }

        public static GotoEntry ZeroPop(int ro) {
            return new GotoEntry(GOTO | GOTO_POP_EQ_ZERO | ro, "");
        }

        public static GotoEntry LessEqual(boolean relative, int ra, int ro, String target) {
            return new GotoEntry(GOTO | (relative ? GOTO_REL_LEQ_ZERO : GOTO_LEQ_ZERO) | (ra << 8) | ro, target);
        }

        public static GotoEntry LessEqualPush(boolean relative, int ra, int ro, String target) {
            return new GotoEntry(GOTO | (relative ? GOTO_PUSH_REL_LEQ_ZERO : GOTO_PUSH_LEQ_ZERO) | (ra << 8) | ro,
                    target);
        }

        public static GotoEntry LessEqualPop(int ro) {
            return new GotoEntry(GOTO | GOTO_POP_LEQ_ZERO | ro, "");
        }

        public static GotoEntry Greater(boolean relative, int ra, int ro, String target) {
            return new GotoEntry(GOTO | (relative ? GOTO_REL_GT_ZERO : GOTO_GT_ZERO) | (ra << 8) | ro, target);
        }

        public static GotoEntry GreaterPush(boolean relative, int ra, int ro, String target) {
            return new GotoEntry(GOTO | (relative ? GOTO_PUSH_REL_GT_ZERO : GOTO_PUSH_GT_ZERO) | (ra << 8) | ro,
                    target);
        }

        public static GotoEntry GreaterPop(int ro) {
            return new GotoEntry(GOTO | GOTO_POP_GT_ZERO | ro, "");
        }

        public static GotoEntry NotZero(boolean relative, int ra, int ro, String target) {
            return new GotoEntry(GOTO | (relative ? GOTO_REL_NOT_ZERO : GOTO_NOT_ZERO) | (ra << 8) | ro, target);
        }

        public static GotoEntry NotZeroPush(boolean relative, int ra, int ro, String target) {
            return new GotoEntry(GOTO | (relative ? GOTO_PUSH_REL_NOT_ZERO : GOTO_PUSH_NOT_ZERO) | (ra << 8) | ro,
                    target);
        }

        public static GotoEntry NotZeroPop(int ro) {
            return new GotoEntry(GOTO | GOTO_POP_NOT_ZERO | ro, "");
        }

        public void setOffset(int offset) {
            instruction |= uint32ToInt8(offset) << 8;
        }

    }
    
    private static record MemSet(String name, int[] value) {

    }

    public Set<java.util.Map.Entry<String, Integer>> getSyscallMapping() {
        return syscallMap.entrySet();
    }
    public Set<String> getSyscallMap() {
        return syscallDef.keySet();
    }
}
