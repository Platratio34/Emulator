package com.peter.emulator.lang.tokens;

import java.util.HashMap;

import com.peter.emulator.lang.Location;
import com.peter.emulator.lang.ProgramUnit;

public class OperatorToken extends Token {
    public Type type;
    public boolean indexClosed;
    protected Tokenizer tk;

    public OperatorToken(char c, Location location, ProgramUnit unit) {
        super(location);
        type = Type.get(c + "");
        if (type == Type.INDEX) {
            tk = new Tokenizer("", location, unit);
            subTokens = tk.tokens;
        }
    }

    @Override
    public Token ingest(char c, Location location) {
        if(type.next != null && type.hasNext(c)) {
            type = type.getNext(c);
            endLocation = location;
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

    public static enum Type {
        /** <code>=</code> */
        ASSIGN("="),
        /** <code>.</code> */
        DOT("."),
        /** <code>*</code> */
        POINTER("*"),
        /** <code>;</code> */
        SEMICOLON(";"),
        /** <code>,</code> */
        COMMA(","),
        /** <code>~</code> */
        DESTRUCTOR("~"),
        /** <code>[</code> */
        INDEX("["),
        /** <code>[]</code> */
        ARRAY("[]"),
        /** <code>&lt;</code> */
        ANGLE_LEFT("<"),
        /** <code>&gt;</code> */
        ANGLE_RIGHT(">"),
        /** <code>!</code> */
        NOT("!"),
        /** <code>&gt;=</code> */
        GEQ(">="),
        /** <code>&lt;=</code> */
        LEQ("<="),
        /** <code>==</code> */
        EQ2("=="),
        /** <code>!=</code> */
        NEQ("!="),
        /** <code>+</code> */
        ADD("+"),
        /** <code>+=</code> */
        ADD_ASSIGN("+="),
        /** <code>-</code> */
        SUB("-"),
        /** <code>-=</code> */
        SUB_ASSIGN("-="),
        /** <code>/</code> */
        DIV("/"),
        /** <code>++</code> */
        INC("++"),
        /** <code>--</code> */
        DEC("--"),
        /** <code>&lt;&lt;</code> */
        LEFT_SHIFT("<<"),
        /** <code>&gt;&gt;</code> */
        RIGHT_SHIFT(">>"),
        /** <code>^</code> */
        BITWISE_XOR("^"),
        /** <code>&amp;</code> */
        BITWISE_AND("&"),
        /** <code>|</code> */
        BITWISE_OR("|"),
        /** <code>&amp;&amp;</code> */
        AND("&&"),
        /** <code>||</code> */
        OR("||"),
        /** <code>?</code> */
        TERNARY("?"),
        /** <code>:</code> */
        COLON(":"),
        /** <code>//</code> */
        COMMENT("//"),
        /** <code>/*</code> */
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