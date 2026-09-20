package com.peter.emulator.assembly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.peter.emulator.assembly.keywords.ASMKeyword;
import com.peter.emulator.assembly.keywords.AddKeyword;
import com.peter.emulator.assembly.keywords.CopyKeyword;
import com.peter.emulator.assembly.keywords.GotoKeyword;
import com.peter.emulator.assembly.keywords.IncKeyword;
import com.peter.emulator.assembly.keywords.LoadKeyword;
import com.peter.emulator.assembly.keywords.MathKeyword;
import com.peter.emulator.assembly.keywords.ShiftKeyword;
import com.peter.emulator.assembly.keywords.StackKeyword;
import com.peter.emulator.assembly.keywords.StoreKeyword;
import com.peter.emulator.lang.ELSymbol;
import com.peter.emulator.lang.Location;
import com.peter.emulator.lang.Span;
import com.peter.emulator.lang.ELAnalysisError.Severity;
import com.peter.emulator.lang.ELSymbol.Type;
import com.peter.emulator.lang.FileProvider;
import com.peter.emulator.machinecode.Instruction;
import com.peter.emulator.machinecode.MathInstruction;
import com.peter.emulator.machinecode.Reg;

public class ASMParser {

    protected static final HashMap<String, ASMKeyword> keywords = new HashMap<>();

    protected static void addKeyword(ASMKeyword keyword) {
        keywords.put(keyword.id, keyword);
    }

    static {
        addKeyword(new ASMKeyword.NoOpKeyword());
        addKeyword(new ASMKeyword.HaltKeyword());

        addKeyword(new AddKeyword(false));
        addKeyword(new AddKeyword(true));
        for (MathInstruction.Operation op : MathKeyword.NAMES.keySet()) {
            addKeyword(new MathKeyword(op));
        }
        addKeyword(new IncKeyword());
        addKeyword(new ShiftKeyword(false, false));
        addKeyword(new ShiftKeyword(false, true));
        addKeyword(new ShiftKeyword(true, false));
        addKeyword(new ShiftKeyword(true, true));
        
        addKeyword(new LoadKeyword());
        addKeyword(new StoreKeyword());
        addKeyword(new CopyKeyword());

        addKeyword(new GotoKeyword());
        
        addKeyword(new StackKeyword());
    }

    public boolean limitedLintOnly = false;

    public ArrayList<AsmError> errors = new ArrayList<>();

    private final HashMap<String, Define> defines;
    private final HashMap<String, Define> labels;

    private final ArrayList<ELSymbol> symbols;

    
    public static final Define TRUE = new Define("true", 1);
    public static final Define FALSE = new Define("false", 0);

    private final ArrayList<Instruction> instructions;

    public final String[] lines;
    public final Location startLoc;
    protected int address = 0;

    protected final FileProvider fileProvider;
    protected final ASMParser parent;

    public ArrayList<ELSymbol> getSymbols() {
        return symbols;
    }

    public ASMParser(FileProvider fileProvider, String text, Location startLoc, boolean limitedLintOnly) {
        this.fileProvider = fileProvider;
        lines = text.split("\r?\n\r?");
        this.startLoc = startLoc;
        this.limitedLintOnly = limitedLintOnly;

        parent = null;
        defines = new HashMap<>();
        labels = new HashMap<>();
        symbols = new ArrayList<>();
        instructions = new ArrayList<>();
    }

    public ASMParser(FileProvider fileProvider, String text, Location startLoc) {
        this.fileProvider = fileProvider;
        lines = text.split("\r?\n\r?");
        this.startLoc = startLoc;

        parent = null;
        defines = new HashMap<>();
        labels = new HashMap<>();
        symbols = new ArrayList<>();
        instructions = new ArrayList<>();
    }

    public ASMParser(FileProvider fileProvider, String text, Location startLoc, int startAddress) {
        this.fileProvider = fileProvider;
        lines = text.split("\r?\n\r?");
        this.startLoc = startLoc;
        address = startAddress;

        parent = null;
        defines = new HashMap<>();
        labels = new HashMap<>();
        symbols = new ArrayList<>();
        instructions = new ArrayList<>();
    }
    
    public ASMParser(String text, Location startLoc, ASMParser parent) {
        this.fileProvider = parent.fileProvider;
        lines = text.split("\r?\n\r?");
        this.startLoc = startLoc;

        this.parent = parent;
        this.defines = parent.defines;
        this.labels = parent.labels;
        this.symbols = parent.symbols;
        this.instructions = parent.instructions;
    }
    
    protected HashMap<String, ASMParser> includes = new HashMap<>();

    protected void prepass() {
        boolean mlc = false;
        Location location = startLoc;
        Location nexLocation = startLoc;
        for (int i = 0; i < lines.length; i++) {
            location = nexLocation;
            nexLocation = new Location(location.file(), location.line() + 1, 2);
            ASMLine line = new ASMLine(lines[i], location);
            if (mlc) {
                if (line.endsWith("*/")) {
                    mlc = false;
                }
                line.symbolAll(Type.COMMENT_BLOCK);
                continue;
            } else if (line.startsWith("/*")) {
                mlc = true;
                line.symbolAll(Type.COMMENT_BLOCK);
                continue;
            }
            if (line.startsWith("//")) {
                line.symbolAll(Type.COMMENT_LINE);
                continue;
            } else if (line.startsWith(":")) {
                String name = line.nextString().substring(1);
                if (labels.containsKey(name)) {
                    line.errorLast(AsmError.error("Duplicate label `%s`", name));
                } else {
                    Define lbl = new Define(name);
                    lbl.isLabel = true;
                    labels.put(name, lbl);
                }
                line.symbolLast(Type.NAMESPACE_NAME);
                line.symbolRest(Type.COMMENT_LINE);
                continue;
            } else if (line.startsWith("#")) {
                String keyword = line.nextString().substring(1);
                line.symbolLast(Type.ANNOTATION);
                switch (keyword) {
                    case "define" -> {
                        String name = line.nextString(AsmError.error("Expected define name"));
                        line.symbolLast(Type.VARIABLE_CONSTANT);
                        if (defines.containsKey(name)) {
                            line.errorLast(AsmError.error("Duplicate define `%s`", name));
                        }
                        Define val = line.nextConst(AsmError.error("Expected define value"));
                        if (val == null)
                            continue;
                        if (!defines.containsKey(name)) {
                            defines.put(name, new Define(name, val.value));
                        }
                    }
                    case "var" -> {
                        String name = line.nextString(AsmError.error("Expected variable name"));
                        line.symbolLast(Type.VARIABLE_NAME);
                        if (defines.containsKey(name)) {
                            line.errorLast(AsmError.error("Duplicate define `%s`", name));
                        }
                        Define val = line.nextConst();
                        if (val != null) {
                            if (!defines.containsKey(name)) {
                                defines.put(name, new Define(name, new int[] { val.value }));
                            }
                            continue;
                        }
                        String valS = line.nextStringLit();
                        if (valS != null) {
                            defines.put(name, new Define(name, valS));
                            continue;
                        }
                        String valSize = line.nextString(AsmError.error("Expected variable value"));
                        if (valSize == null)
                            continue;
                        if (valSize.startsWith("(")) {
                            try {
                                int size = Integer.parseInt(valSize.substring(1, valSize.length() - 2));
                                defines.put(name, new Define(name).withSize(Math.ceilDiv(size, 4) * 4));
                            } catch (NumberFormatException e) {
                                line.errorLast(AsmError.error("Malformed number"));
                            }
                        }
                        line.errorLast(AsmError.error("Expected variable value"));
                        continue;
                        //

                    }
                    case "stackVar" -> {
                        line.errorLast(AsmError.warning("Unimplemented directive"));
                        // TODO add this
                    }
                    case "stackVarClear" -> {
                        line.errorLast(AsmError.warning("Unimplemented directive"));
                        // TODO add this
                    }
                    case "line" -> {
                        line.errorLast(AsmError.warning("Unimplemented directive"));
                        // TODO add this
                    }
                    case "lineend" -> {
                        line.errorLast(AsmError.warning("Unimplemented directive"));
                        // TODO add this
                    }
                    case "function" -> {
                        line.errorLast(AsmError.warning("Unimplemented directive"));
                        // TODO add this
                    }
                    case "include" -> {
                        String path = line.nextString(AsmError.error("Expected include path"));
                        if (path == null)
                            continue;
                        line.symbolLast(Type.NAMESPACE_NAME);

                        if (includes.containsKey(path)) { // ignore it to prevent re-entering
                            continue;
                        }

                        ASMParser parser;
                        if (path.startsWith("<")) { // internal include
                            if(!path.endsWith(">")) {
                                line.errorLast(AsmError.error("Expected `>` and end of include"));
                                continue;
                            }
                            path = path.substring(1, path.length() - 1);
                            InputStream is = ASMParser.class.getResourceAsStream("/asm/" + path.replaceAll("\\.", "/") + ".asm");
                            if (is == null) {
                                is = ASMParser.class.getResourceAsStream("/asm/" + path.replaceAll("\\.", "/") + "/init.asm");
                            }
                            if (is == null) {
                                line.errorLast(AsmError.error("Unknown internal include"));
                                continue;
                            }
                            line.symbolRest(Type.COMMENT_LINE);
                            if (limitedLintOnly) {
                                continue;
                            }
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                                parser = new ASMParser(reader.lines().collect(Collectors.joining("\n")), new Location(path, 1, 2), parent != null ? parent : this);
                            } catch(IOException e) {
                                line.errorLast(AsmError.error("IO error including file"));
                                continue;
                            }
                        } else {
                            line.symbolRest(Type.COMMENT_LINE);
                            if (limitedLintOnly) {
                                continue;
                            }


                            parser = null;
                            continue;
                        }
                        includes.put(path, parser);
                        parser.prepass();
                        continue;
                    }
                    default -> {
                        line.errorLast(AsmError.warning("Unknown directive `%s`", keyword));
                    }
                }
                line.symbolRest(Type.COMMENT_LINE);
                continue;
            }
        }
    }

    protected void mainPass() {
        boolean mlc = false;
        Location location = startLoc;
        Location nexLocation = startLoc;
        for (int i = 0; i < lines.length; i++) {
            location = nexLocation;
            nexLocation = new Location(location.file(), location.line() + 1, 2);
            ASMLine line = new ASMLine(lines[i], location);
            // System.out.println(line);
            if (line.lineLen == 0) {
                continue;
            }
            if (mlc) {
                if (line.endsWith("*/")) {
                    mlc = false;
                }
                continue;
            } else if (line.startsWith("/*")) {
                mlc = true;
                continue;
            }
            if (line.startsWith("//")) {
                continue;
            } else if (line.startsWith(":")) {
                if(limitedLintOnly)
                    continue;
                String name = line.nextString().substring(1);
                labels.get(name).resolveAt(address);
                continue;
            } else if (line.startsWith("#")) {

                continue;
            }

            String keyword = line.nextString();
            if (keyword == null || keyword.length() == 0)
                continue;
            // System.out.println(keyword);
            if (keywords.containsKey(keyword)) {
                ASMKeyword kWrd = keywords.get(keyword);
                symbols.add(new ELSymbol(Type.KEYWORD, line.lastSpan, String.format("`%s`\n\n%s\n\n**Usage**: %s", keyword, kWrd.getDef(), kWrd.getUsage())));
                Instruction instr = kWrd.add(line);
                if (instr == null || limitedLintOnly) {
                    continue;
                }
                instructions.add(instr);
                address += instr.hasSecond() ? 8 : 4;
                line.symbolRest(Type.COMMENT_LINE);
                continue;
            } else {
                line.errorLast(AsmError.error("Unknown keyword `%s`", keyword));
            }
        }
    }
    

    public boolean parse() {
        prepass();
        mainPass();

        for (AsmError error : errors) {
            if (error.severity.atLeast(Severity.ERROR)) {
                return false;
            }
        }
        return true;
    }

    public class ASMLine {
        public final String line;
        public final int lineLen;
        public final Location location;

        protected int col = 0;

        public Span lastSpan = null;

        public ASMLine(String line, Location location) {
            lineLen = line.length();
            boolean inWS = true;
            while (inWS && col < lineLen) {
                switch (line.charAt(col)) {
                    case ' ', '\r', '\t' -> col++;
                    default -> inWS = false;
                }
            }
            this.line = line;
            this.location = location;
        }

        public boolean startsWith(String start) {
            int i2 = 0;
            for (int i = col; i < lineLen; i++) {
                if (line.charAt(i) != start.charAt(i2++))
                    return false;
                if (i2 >= start.length())
                    break;
            }
            return i2 >= start.length();
        }

        public boolean endsWith(String end) {
            return line.endsWith(end);
        }

        public boolean hasNext() {
            return col < lineLen;
        }

        public boolean hasNext(String next) {
            return hasNext(next, null);
        }

        public boolean hasNext(String next, AsmError error) {
            Location startLoc = location.add(col);
            if (col >= lineLen) {
                if(error != null)
                    errors.add(error.at(startLoc.span()));
                return false;
            }
            Span tempSpan = lastSpan;
            int lastCol = col;
            String t = nextString();
            if (!t.equals(next)) {
                if (error != null) {
                    errors.add(error.at(lastSpan));
                } else {
                    lastSpan = tempSpan;
                    col = lastCol;
                }
                return false;
            }
            return true;
        }

        public String nextString() {
            return nextString(null);
        }

        public String nextString(AsmError error) {
            Location startLoc = location.add(col);
            if (col >= lineLen) {
                if(error != null)
                    errors.add(error.at(startLoc.span()));
                return null;
            }
            String t = "";
            int cmt = 0;
            while (col < lineLen) {
                char c = line.charAt(col++);
                if (c == ' ') {
                    break;
                } else if (c == '/') {
                    cmt++;
                    if (cmt == 2) {
                        t = t.substring(0,t.length()-1);
                        break;
                    }
                } else {
                    cmt = 0;
                }
                t += c;
            }
            lastSpan = startLoc.span(location.add(col-1));
            return t;
        }

        public Reg nextReg() {
            return nextReg(null);
        }

        public Reg nextReg(AsmError error) {
            Location startLoc = location.add(col);
            if (col >= lineLen) {
                if (error != null)
                    errors.add(error.at(startLoc.span()));
                return null;
            }
            int startCol = col;
            String t = "";
            while (col < lineLen) {
                char c = line.charAt(col++);
                if (c == ' ') {
                    break;
                }
                t += c;
            }
            Span tempSpan = lastSpan;
            lastSpan = startLoc.span(location.add(col - 1));
            Reg reg = Reg.from(t);
            if (reg == Reg.UNKNOWN) {
                if (error != null)
                    errors.add(error.at(lastSpan));
                else {
                    lastSpan = tempSpan;
                    col = startCol;
                }
                return null;
            }
            symbolLast(Type.PARAMETER);
            return reg;
        }
        
        
        private Define getDefine(String t) {
            if (t.equals("true")) {
                symbolLast(Type.VARIABLE_CONSTANT);
                return TRUE; 
            } else if (t.equals("false")) {
                symbolLast(Type.VARIABLE_CONSTANT);
                return FALSE;
            } else if (t.startsWith("&")) { // address of
                if (limitedLintOnly) {
                    if (t.charAt(1) == ':') {
                        symbolLast(Type.FUNCTION_NAME);
                        return new Define(t.substring(2));
                    }
                    symbolLast(Type.VARIABLE_NAME);
                    return new Define(t.substring(1));
                }
                if (t.charAt(1) == ':') {
                    String lblName = t.substring(2);
                    Define def = labels.getOrDefault(lblName, null);
                    if (def == null) {
                        errors.add(AsmError.error(lastSpan, "Unknown label `:%s`", lblName));
                    }
                    symbolLast(Type.FUNCTION_NAME);
                    return def;
                }
                String defName = t.substring(1);
                Define def = defines.getOrDefault(defName, null);
                if (def == null) {
                    errors.add(AsmError.error(lastSpan, "Unknown define `%s`", defName));
                    return null;
                }
                if (!def.isAddress) {
                    errors.add(AsmError.error(lastSpan, "Define `%s` does not have an address", defName));
                    symbolLast(Type.VARIABLE_CONSTANT);
                } else {
                    symbolLast(Type.VARIABLE_NAME);
                }
                return def;
            } else if (defines.containsKey(t)) { // address of
                Define def = defines.get(t);
                if (def.isAddress) {
                    errors.add(AsmError.warning(lastSpan, "Define is an address, but is accessed like a value"));
                    symbolLast(Type.VARIABLE_NAME);
                } else {
                    symbolLast(Type.VARIABLE_CONSTANT);
                }
                return def;
            } else if (t.startsWith("'")) {
                symbolLast(Type.STRING_LITERAL);
                if (t.length() < 3)
                    return null;
                if (t.charAt(1) == '\\') {
                    if (t.length() != 4 || t.charAt(3) != '\'')
                        return null;
                    symbols.add(new ELSymbol(Type.STRING_LITERAL, lastSpan.start().span()));
                    symbols.add(new ELSymbol(Type.STRING_LITERAL_ESCAPE, lastSpan.start().add(1).span(1)));
                    symbols.add(new ELSymbol(Type.STRING_LITERAL, lastSpan.start().add(3).span()));
                    return new Define("_", switch (t.charAt(2)) {
                        case 'n' -> '\n';
                        case 't' -> '\t';
                        case 'r' -> '\r';
                        case '0' -> '\0';
                        default -> t.charAt(2);
                    });
                }
                symbolLast(Type.STRING_LITERAL);
                if (t.length() != 3 || t.charAt(2) != '\'')
                    return null;
                return new Define("_", t.charAt(1));
            } else if (t.startsWith("0x")) {
                symbolLast(Type.NUMERIC_LITERAL);
                try {
                    return new Define("_", Integer.parseInt(t.substring(2).replaceAll("_",""), 16));
                } catch (NumberFormatException e) {
                    errors.add(AsmError.error(lastSpan, "Malformed hex `%s`", t));
                }
            } else if (t.startsWith("0b")) {
                symbolLast(Type.NUMERIC_LITERAL);
                try {
                    return new Define("_", Integer.parseInt(t.substring(2).replaceAll("_",""), 2));
                } catch (NumberFormatException e) {
                    errors.add(AsmError.error(lastSpan, "Malformed binary `%s`", t));
                }
            } else {
                try {
                    int val = Integer.parseInt(t.replaceAll("_",""));
                    symbolLast(Type.NUMERIC_LITERAL);
                    return new Define("_", val);
                } catch (NumberFormatException e) {
                    if (limitedLintOnly) {
                        symbolLast(Type.VARIABLE_CONSTANT);
                        return new Define(t);
                    }
                }
            }
            return null;
        }

        public Define nextConst() {
            return nextConst(null);
        }

        public Define nextConst(AsmError error) {
            Location startLoc = location.add(col);
            if (col >= lineLen) {
                if (error != null)
                    errors.add(error.at(startLoc.span()));
                return null;
            }
            int startCol = col;
            Span tempSpan = lastSpan;
            String t = nextString();
            Define def = t != null ? getDefine(t) : null;

            if (def == null) {
                if (error != null) {
                    errors.add(new AsmError(error.severity, lastSpan, error.message + "; `"+t+"`"));
                }  else {
                    lastSpan = tempSpan;
                    col = startCol;
                }
                return null;
            }
            return def;
        }
        
        
        public String nextStringLit() {
            return nextStringLit(null);
        }

        public String nextStringLit(AsmError error) {
            Location startLoc = location.add(col);
            if (col >= lineLen) {
                if (error != null)
                    errors.add(error.at(startLoc.span()));
                return null;
            }
            String t = "";
            boolean escape = false;
            if (line.charAt(col) != '"') {
                if (error != null)
                    errors.add(error.at(lastSpan));
                return null;
            }
            symbols.add(new ELSymbol(Type.STRING_LITERAL, startLoc.span()));
            Location lastLocation = startLoc.add(1);
            col++;
            while (col < lineLen) {
                char c = line.charAt(col++);
                if (escape) {
                    t += switch (c) {
                        case 'n' -> '\n';
                        case 't' -> '\t';
                        case 'r' -> '\r';
                        case '0' -> '\0';

                        default -> c;
                    };
                    if(lastLocation.col() == col-3) {
                        symbols.add(new ELSymbol(Type.STRING_LITERAL_ESCAPE, lastLocation.span(2)));
                        lastLocation = lastLocation.add(2);
                    } else {
                        Location newLoc = location.add(col-4);
                        symbols.add(new ELSymbol(Type.STRING_LITERAL, lastLocation.span(newLoc)));
                        symbols.add(new ELSymbol(Type.STRING_LITERAL_ESCAPE, newLoc.add(1).span(2)));
                        lastLocation = location.add(col-1);
                    }
                    continue;
                }
                if (c == '"') {
                    break;
                } else if (c == '\\') {
                    escape = true;
                    continue;
                }
                t += c;
            }
            Location newLoc = location.add(col - 1);
            symbols.add(new ELSymbol(Type.STRING_LITERAL, lastLocation.span(newLoc)));
            lastSpan = startLoc.span(newLoc);
            return t;
        }

        public void errorLast(AsmError error) {
            errors.add(error.at(lastSpan));
        }

        public void symbolLast(ELSymbol.Type type) {
            symbols.add(new ELSymbol(type, lastSpan));
        }

        public void symbolAll(ELSymbol.Type type) {
            symbols.add(new ELSymbol(type, startLoc.span(startLoc.add(lineLen))));
        }

        public void symbolRest(ELSymbol.Type type) {
            if (col == lineLen - 1)
                return;
            symbols.add(new ELSymbol(type, lastSpan.end().add(1).span(startLoc.add(lineLen))));
        }

        @Override
        public String toString() {
            return String.format("ASMLine{ line=\"%s\", lineLen=%d, col=%d, lastSpan=%s }", line, lineLen, col, lastSpan);
        }

        public Define getLabel(String name) {
            if (limitedLintOnly) {
                Define lbl = new Define(name);
                lbl.isLabel = true;
                return lbl;
            }
            return labels.getOrDefault(name, null);
        }
    }
}
