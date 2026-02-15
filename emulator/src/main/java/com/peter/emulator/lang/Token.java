package com.peter.emulator.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Token {

    public final Location startLocation;
    public Location endLocation;
    public ArrayList<Token> subTokens;

    public abstract boolean ingest(char c, Location location);
    
    protected Token(Location location) {
        startLocation = location;
        endLocation = location;
        if (location == null)
            throw new NullPointerException("Start location must be non-null");
    }

    public boolean hasSub() {
        return subTokens != null && !subTokens.isEmpty();
    }

    public abstract String debugString();

    public boolean wsBefore() {
        return true;
    }

    public Span span() {
        return new Span(startLocation, endLocation);
    }

    public static class OperatorToken extends Token {
        public Type type;
        public boolean indexClosed;
        protected Tokenizer tk;

        public OperatorToken(char c, Location location) {
            super(location);
            type = Type.get(c + "");
            if (type == Type.INDEX) {
                tk = new Tokenizer("", location);
                subTokens = tk.tokens;
            }
        }

        @Override
        public boolean ingest(char c, Location location) {
            if(type.next != null && type.hasNext(c)) {
                type = type.getNext(c);
                return true;
            }
            if (type == Type.INDEX && !indexClosed) {
                if (tk.ingest(c, location))
                    return true;
                if (c == ']') {
                    indexClosed = true;
                    return true;
                }
                throw new TokenizerError("Found unexpected character in index: '" + c + "'");
            }
            return false;
        }

        @Override
        public String toString() {
            String out = "OperatorToken{";
            if (type == Type.INDEX) {
                out += "[";
                if (indexClosed)
                    out += "]";
                out += ", numTokens=" + subTokens.size();
            } else {
                out += type.value;
            }
            out += ", " + startLocation;
            return out + "}";
        }
        
        @Override
        public String debugString() {
            if (type == Type.INDEX) {
                String out = "[";
                for (int i = 0; i < subTokens.size(); i++) {
                    if (i > 0 && subTokens.get(i).wsBefore())
                        out += " ";
                    out += subTokens.get(i).debugString();
                }
                return out + "]";
            }
            return type.value;
        }
        
        @Override
        public boolean wsBefore() {
            return switch (type) {
                case INDEX -> false;
                case NOT -> false;
                case SEMICOLON -> false;
                case COMMA -> false;
                case INC -> false;
                case DEC -> false;
                case POINTER -> false;
                default -> true;
            };
        }

        public enum Type {
            ASSIGN("="),
            DOT("."),
            POINTER("*"),
            SEMICOLON(";"),
            COMMA(","),
            DESTRUCTOR("~"),
            INDEX("["),
            ARRAY("[]"),
            ANGLE_LEFT("<"),
            ANGLE_RIGHT(">"),
            NOT("!"),
            GEQ(">="),
            LEQ("<="),
            EQ2("=="),
            NEQ("!="),
            ADD("+"),
            ADD_ASSIGN("+="),
            SUB("-"),
            SUB_ASSIGN("-="),
            DIV("/"),
            INC("++"),
            DEC("--"),
            LEFT_SHIFT("<<"),
            RIGHT_SHIFT(">>"),
            BITWISE_NOR("^"),
            BITWISE_AND("&"),
            BITWISE_OR("|"),
            AND("&&"),
            OR("||"),
            TERNARY("?"),
            COLON(":"),
            COMMENT("//"),
            COMMENT_MULTILINE("/*"),
            ;

            static {
                ASSIGN.addNext('=', EQ2);
                ANGLE_LEFT.addNext('=', LEQ);
                ANGLE_LEFT.addNext('<', LEFT_SHIFT);
                ANGLE_RIGHT.addNext('=', GEQ);
                ANGLE_RIGHT.addNext('>', RIGHT_SHIFT);
                NOT.addNext('=', NEQ);
                BITWISE_AND.addNext('&', AND);
                BITWISE_OR.addNext('|', OR);
                ADD.addNext('+', INC);
                ADD.addNext('=', ADD_ASSIGN);
                SUB.addNext('-', DEC);
                SUB.addNext('=', SUB_ASSIGN);
                DIV.addNext('/', COMMENT);
                DIV.addNext('*', COMMENT_MULTILINE);
            }
                
            public final String value;
            private static HashMap<String, Type> values;
            private HashMap<Character, Type> next = null;

            private Type(String value) {
                this.value = value;
                add();
            }

            private void add() {
                if (values == null)
                    values = new HashMap<>();
                values.put(value, this);
            }

            private void addNext(char c, Type n) {
                if(next == null)
                    next = new HashMap<>();
                next.put(c, n);
            }
            public boolean hasNext(char c) {
                return next.containsKey(c);
            }
            public Type getNext(char c) {
                return next.get(c);
            }

            public static boolean contains(String c) {
                return values.containsKey(c);
            }

            public static Type get(String c) {
                return values.get(c);
            }
        }
    }

    public static class BlockToken extends Token {
        public boolean closed = false;
        protected Tokenizer tk;

        public BlockToken(Location location) {
            super(location);
            tk = new Tokenizer("", location);
            subTokens = tk.tokens;
        }

        @Override
        public boolean ingest(char c, Location location) {
            if (closed)
                return false;
            if (tk.ingest(c, location)) {
                endLocation = location;
                return true;
            }
            if (c == '}') {
                endLocation = location;
                closed = true;
                return true;
            }
            throw new TokenizerError("Found unexpected character in block: '" + c + "'");
        }

        @Override
        public String toString() {
            return String.format("BlockToken{closed=%s, numTokens=%d, %s}", closed ? "true" : "false", subTokens == null ? 0 : subTokens.size(), startLocation);
        }

        @Override
        public String debugString() {
            String out = "{";
            for (int i = 0; i < subTokens.size(); i++) {
                if (i > 0 && subTokens.get(i).wsBefore())
                    out += " ";
                out += subTokens.get(i).debugString();
            }
            return out + "}";
        }
    }

    public static class IdentifierToken extends Token {
        public String value;
        public ArrayList<Token> index = null;
        protected Tokenizer indexTokenizer;
        protected boolean indexClosed = false;

        public IdentifierToken(char c, Location location) {
            super(location);
            value = c + "";
        }

        private boolean nextIsID = false;
        private boolean nextIsDot = false;
        private IdentifierToken nextId = null;
        @Override
        public boolean ingest(char c, Location location) {
            if (nextId != null) {
                if (nextId.ingest(c, location)) {
                    endLocation = location;
                    return true;
                }
                return false;
            } else if (indexTokenizer != null) {
                if (!indexTokenizer.ingest(c, location)) {
                    if (c == ']') {
                        indexClosed = true;
                        indexTokenizer = null;
                        nextIsDot = true;
                        return true;
                    }
                    throw new TokenizerError("Unexpected character in index");
                }
                return true;
            }
            if (nextIsID) {
                nextIsID = false;
                if (validStart(c)) {
                    nextId = new IdentifierToken(c, location);
                    subTokens = new ArrayList<>();
                    subTokens.add(nextId);
                    endLocation = location;
                    return true;
                } else {
                    throw new TokenizerError("Invalid identifier");
                }
            } else if (validStart(c) || Character.isDigit(c)) {
                if (nextIsDot)
                    throw new TokenizerError("Unexpected character in identifier, expected `.`");
                value += c;
                endLocation = location;
                return true;
            } else if (c == '.') {
                nextIsID = true;
                endLocation = location;
                nextIsDot = false;
                return true;
            } else if (c == '[') {
                if (indexTokenizer != null || indexClosed) {
                    throw new TokenizerError("Unexpected `[` in identifier");
                }
                indexTokenizer = new Tokenizer("", location);
                index = indexTokenizer.tokens;
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            String out = "IdentifierToken{value=\"";
            out += value + "\"";
            if (index != null) {
                out += ", index=[";
                boolean f = true;
                for (Token t : index) {
                    if (!f)
                        out += ",";
                    f = false;
                    out += t;
                }
                out += "]";
            }
            if (subTokens != null)
                out += ", subTokens=" + subTokens.size();
            out += ", " + startLocation;
            return out + "}";
        }

        @Override
        public String debugString() {
            String out = value;
            if (subTokens != null) {
                for (Token t : subTokens) {
                    out += "." + ((IdentifierToken) t).value;
                }
            }
            return out;
        }

        public static boolean validStart(char c) {
            return Character.isAlphabetic(c) || c == '_';
        }

        public Identifier asId() {
            return new Identifier(this);
        }

        public IdentifierToken sub(int i) {
            return (IdentifierToken)subTokens.get(i);
        }
    }

    public static class NumberToken extends Token {
        public String value;
        public boolean hex;
        public boolean bin;
        public int numValue = 0;

        public NumberToken(char c, Location location) {
            super(location);
            value = c + "";
        }

        @Override
        public boolean ingest(char c, Location location) {
            if (c == 'x') {
                if (value.length() == 1) {
                    hex = true;
                    value = "";
                    endLocation = location;
                    return true;
                }
                return false;
            } else if (c == 'b') {
                if (value.length() == 1) {
                    bin = true;
                    value = "";
                    endLocation = location;
                    return true;
                }
                return false;
            }
            if (hex) {
                if (!(Character.isDigit(c) || ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F') || c == '_'))
                    return false;
            } else if (bin) {
                if (!(c == '0' || c == '1' || c == '_'))
                    return false;
            } else {
                if (!(Character.isDigit(c) || c == '.' || c == 'e' || c == '_'))
                    return false;
            }
            value += c;
            endLocation = location;
            numValue = (int)Long.parseLong(value.replace("_",""), bin ? 2 : (hex ? 16 : 10));
            return true;
        }
        @Override
        public String toString() {
            return String.format("NumberToken{value=%s, hex=%s, bin=%s, %s}", value, hex ? "true" : "false",
                    bin ? "true" : "false", startLocation);
        }

        @Override
        public String debugString() {
            String out = "";
            if (hex)
                out = "0x";
            if (bin)
                out = "0b";
            return out + value;
        }
    }
    
    public static class StringToken extends Token {
        public String value = "";
        public boolean ch = false;
        public boolean escape = false;
        public boolean closed = false;

        protected static final Map<Character, String> escapes = Map.of(
            '\\', "\\",
            'n', "\n",
            't', "\t",
            '"', "\"",
            '\'', "\'",
            '0', "\0"
        );

        public StringToken(char c, Location location) {
            super(location);
            ch = c == '\'';
        }

        @Override
        public boolean ingest(char c, Location location) {
            if (closed)
                return false;
            endLocation = location;
            if(escape) {
                if(escapes.containsKey(c)) {
                    value += escapes.get(c);
                } else {
                    value += c;
                }
                escape = false;
                return true;
            } else if (c == '\'' && ch) {
                closed = true;
                return true;
            } else if (c == '"' && !ch) {
                closed = true;
                return true;
            } else if (c == '\\') {
                escape = true;
                return true;
            }
            value += c;
            return true;
        }

        public String escapedValue() {
            return value.replace("\\","\\\\").replace("\n","\\n").replace("\r","\\r").replace("\"","\\\"").replace("\'","\\'");
        }

        @Override
        public String toString() {
            return String.format("StringToken{value=\"%s\", ch=%s, closed=%s, %s}", escapedValue(), ch ? "true" : "false",
                    closed ? "true" : "false", startLocation);
        }

        @Override
        public String debugString() {
            return String.format(ch ? "'%s'" : "\"%s\"", escapedValue());
        }
    }
    
    public static class SetToken extends Token {
        public boolean closed = false;
        protected Tokenizer tk;

        protected char closer;

        public SetToken(char c, Location location) {
            super(location);
            closer = c;
            tk = new Tokenizer("", location);
            subTokens = tk.tokens;
        }

        @Override
        public boolean ingest(char c, Location location) {
            if (closed)
                return false;
            if (tk.ingest(c, location)) {
                endLocation = location;
                return true;
            }
            if (c == closer) {
                closed = true;
                endLocation = location;
                return true;
            }
            throw new TokenizerError("Found unexpected character in set: '" + c + "'");
        }

        public Token get(int i) {
            if(i > subTokens.size())
                return null;
            return subTokens.get(i);
        }

        @Override
        public String toString() {
            return String.format("SetToken{closed=%s, numTokens=%d, %s}", closed ? "true" : "false",
                    subTokens == null ? 0 : subTokens.size(), startLocation);
        }

        @Override
        public String debugString() {
            String out = "";
            for (int i = 0; i < subTokens.size(); i++) {
                if (i > 0 && subTokens.get(i).wsBefore())
                    out += " ";
                out += subTokens.get(i).debugString();
            }
            return out;
        }
    }
    
    public static class AnnotationToken extends Token {
        public  String name;
        public SetToken params = null;

        public AnnotationToken(Location location) {
            super(location);
            name = "";
        }

        @Override
        public boolean ingest(char c, Location location) {
            boolean isDigit = Character.isDigit(c);
            if (params != null) {
                if (params.ingest(c, location)) {
                    endLocation = location;
                    return true;
                }
                return false;
            } else if (Character.isAlphabetic(c) || Character.isDigit(c) || c == '_') {
                if (name.length() == 0 && isDigit)
                    throw new TokenizerError("Annotation name can not start with a digit");
                name += c;
                endLocation = location;
                return true;
            } else if (c == '(') {
                params = new SetToken(')', location);
                subTokens = params.subTokens;
                endLocation = location;
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("AnnotationToken{name=\"%s\", %s}", name, startLocation);
        }

        @Override
        public String debugString() {
            String out = "@" + name;
            if (params != null) {
                out += "(" + params.debugString() + ")";
            }
            return out;
        }
    }
}
