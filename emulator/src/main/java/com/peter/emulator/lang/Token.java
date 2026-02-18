package com.peter.emulator.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Token {

    public final Location startLocation;
    public Location endLocation;
    public ArrayList<Token> subTokens;

    public abstract Token ingest(char c, Location location);
    
    protected Token(Location location) {
        startLocation = location;
        endLocation = location;
        if (location == null)
            throw new NullPointerException("Start location must be non-null");
    }

    public boolean hasSub() {
        return subTokens != null && !subTokens.isEmpty();
    }

    public int subSize() {
        return (subTokens == null) ? 0 : subTokens.size();
    }

    public Token subFirst() {
        return (subTokens == null) ? null : subTokens.getFirst();
    }
    public Token subLast() {
        return (subTokens == null) ? null : subTokens.getLast();
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
        public Token ingest(char c, Location location) {
            if(type.next != null && type.hasNext(c)) {
                type = type.getNext(c);
                return this;
            }
            if (type == Type.INDEX && !indexClosed) {
                if (tk.ingest(c, location))
                    return this;
                if (c == ']') {
                    indexClosed = true;
                    return this;
                }
                throw new TokenizerError("Found unexpected character in index: '" + c + "'");
            }
            return null;
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
        public Token ingest(char c, Location location) {
            if (closed)
                return null;
            if (tk.ingest(c, location)) {
                endLocation = location;
                return this;
            }
            if (c == '}') {
                endLocation = location;
                closed = true;
                return this;
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
        public SetToken index = null;
        public SetToken params = null;
        protected boolean indexClosed = false;

        public IdentifierToken(char c, Location location) {
            super(location);
            value = c + "";
        }

        private boolean nextIsID = false;
        private boolean nextIsDot = false;
        private IdentifierToken nextId = null;
        @Override
        public IdentifierToken ingest(char c, Location location) {
            if (nextId != null) {
                IdentifierToken tkn = nextId.ingest(c, location);
                if (tkn != null) {
                    endLocation = location;
                    nextId = tkn;
                    return this;
                } else {
                    subTokens.add(nextId);
                }
                return null;
            } else if (index != null && !index.closed) {
                SetToken tkn = index.ingest(c, location);
                if (tkn != null) {
                    endLocation = location;
                    index = tkn;
                    return this;
                }
            } else if(params != null && !params.closed) {
                SetToken tkn = params.ingest(c, location);
                if (tkn != null) {
                    endLocation = location;
                    params = tkn;
                    return this;
                }
            }
            if (nextIsID) {
                nextIsID = false;
                if (validStart(c)) {
                    nextId = new IdentifierToken(c, location);
                    subTokens = new ArrayList<>();
                    subTokens.add(nextId);
                    endLocation = location;
                    return this;
                } else {
                    throw new TokenizerError("Invalid identifier");
                }
            } else if (validStart(c) || Character.isDigit(c)) {
                if (nextIsDot)
                    throw new TokenizerError("Unexpected character in identifier, expected `.`");
                value += c;
                endLocation = location;
                return this;
            } else if (c == '.') {
                nextIsID = true;
                endLocation = location;
                nextIsDot = false;
                return this;
            } else if (c == '[') {
                if (index != null || params != null) {
                    throw new TokenizerError("Unexpected `[` in identifier");
                }
                index = new SetToken(SetToken.BracketType.SQUARE_BRACKETS, location);
                return this;
            } else if (c == '(') {
                if (params != null || index != null) {
                    throw new TokenizerError("Unexpected `()` in identifier");
                }
                params = new SetToken(SetToken.BracketType.PARENTHESES, location);
                return this;
            }
            return null;
        }

        @Override
        public String toString() {
            String out = "IdentifierToken{value=\"";
            out += value + "\"";
            if (index != null) {
                out += ", index=" + index.toString();
            }
            if (params != null) {
                out += ", params=" + params.toString();
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

        public Span spanFirst() {
            return startLocation.span(startLocation.add(value.length() - 1));
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
        public Token ingest(char c, Location location) {
            if (c == 'x') {
                if (value.length() == 1) {
                    hex = true;
                    value = "";
                    endLocation = location;
                    return this;
                }
                return null;
            } else if (c == 'b') {
                if (value.length() == 1) {
                    bin = true;
                    value = "";
                    endLocation = location;
                    return this;
                }
                return null;
            }
            if (hex) {
                if (!(Character.isDigit(c) || ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F') || c == '_'))
                    return null;
            } else if (bin) {
                if (!(c == '0' || c == '1' || c == '_'))
                    return null;
            } else {
                if (!(Character.isDigit(c) || c == '.' || c == 'e' || c == '_'))
                    return null;
            }
            value += c;
            endLocation = location;
            numValue = (int)Long.parseLong(value.replace("_",""), bin ? 2 : (hex ? 16 : 10));
            return this;
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
        public Token ingest(char c, Location location) {
            if (closed)
                return null;
            endLocation = location;
            if(escape) {
                if(escapes.containsKey(c)) {
                    value += escapes.get(c);
                } else {
                    value += c;
                }
                escape = false;
                return this;
            } else if (c == '\'' && ch) {
                closed = true;
                return this;
            } else if (c == '"' && !ch) {
                closed = true;
                return this;
            } else if (c == '\\') {
                escape = true;
                return this;
            }
            value += c;
            return this;
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

        protected BracketType type;

        public SetToken(BracketType type, Location location) {
            super(location);
            this.type = type;
            tk = new Tokenizer("", location);
            subTokens = tk.tokens;
        }

        @Override
        public SetToken ingest(char c, Location location) {
            if (closed)
                return null;
            if (tk.ingest(c, location)) {
                endLocation = location;
                return this;
            }
            if (c == type.close) {
                closed = true;
                endLocation = location;
                return this;
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
            return String.format("SetToken{closed=%s, numTokens=%d, type=%s %s}", closed ? "true" : "false",
                    subTokens == null ? 0 : subTokens.size(), type, startLocation);
        }

        @Override
        public String debugString() {
            String out = ""+type.open;
            for (int i = 0; i < subTokens.size(); i++) {
                if (i > 0 && subTokens.get(i).wsBefore())
                    out += " ";
                out += subTokens.get(i).debugString();
            }
            return out + type.close;
        }

        public enum BracketType {
            PARENTHESES('(',')'),
            SQUARE_BRACKETS('[',']'),
            ;
            public final char open;
            public final char close;
            private BracketType(char open, char close) {
                this.open = open;
                this.close = close;
            }
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
        public Token ingest(char c, Location location) {
            boolean isDigit = Character.isDigit(c);
            if (params != null) {
                SetToken tkn = (SetToken)params.ingest(c, location);
                if (tkn != null) {
                    endLocation = location;
                    params = tkn;
                    return this;
                }
                return null;
            } else if (Character.isAlphabetic(c) || Character.isDigit(c) || c == '_') {
                if (name.length() == 0 && isDigit)
                    throw new TokenizerError("Annotation name can not start with a digit");
                name += c;
                endLocation = location;
                return this;
            } else if (c == '(') {
                params = new SetToken(SetToken.BracketType.PARENTHESES, location);
                subTokens = params.subTokens;
                endLocation = location;
                return this;
            }
            return null;
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
