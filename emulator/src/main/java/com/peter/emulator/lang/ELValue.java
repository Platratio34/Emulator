package com.peter.emulator.lang;

import java.util.ArrayList;

import com.peter.emulator.lang.tokens.BlockToken;
import com.peter.emulator.lang.tokens.IdentifierToken;
import com.peter.emulator.lang.tokens.NumberToken;
import com.peter.emulator.lang.tokens.OperatorToken;
import com.peter.emulator.lang.tokens.StringToken;
import com.peter.emulator.lang.tokens.Token;
import com.peter.emulator.lang.base.ELPrimitives;

public abstract class ELValue {
    public final ELType type;

    protected ELValue(ELType type) {
        this.type = type;
    }

    public abstract String valueString();

    public void resolve(ErrorSet errors) {}

    public static ELValue value(ELType type, Token tkn, Namespace ns, ProgramUnit unit) {
        return switch (tkn) {
            case NumberToken nt -> number(type, nt);
            case StringToken st -> string(type, st);
            case BlockToken bt -> array(type, bt, ns, unit);
            case IdentifierToken it -> ref(type, it, ns, unit);
            default -> null;
        };
    }

    public static class ELNumberValue extends ELValue {
        public final int size;
        public final int value;

        protected ELNumberValue(ELType type, int value) {
            super(type);
            if (type.equals(ELPrimitives.UINT8)) {
                size = 1;
                if (value < 0 || value > 0xff)
                    throw new ELCompileException("Invalid value for uint8");
            } else if (type.equals(ELPrimitives.CHAR)) {
                size = 1;
                if (value < 0 || value > 0xff)
                    throw new ELCompileException("Invalid value for char");
            } else if (type.equals(ELPrimitives.BOOL)) {
                size = 1;
                if (value < 0 || value > 0xff)
                    throw new ELCompileException("Invalid value for bool");
            } else if (type.equals(ELPrimitives.UINT16)) {
                size = 2;
                if (value < 0 || value > 0xffff)
                    throw new ELCompileException("Invalid value for uint16");
            } else if (type.equals(ELPrimitives.UINT32)) {
                size = 4;
            } else if (type.pointer) {
                size = 4;
            } else {
                throw new ELCompileException("Incorrect type for number value: "+type.toString());
            }
            this.value = value;
        }
        
        @Override
        public String valueString() {
            if (value > 0xffff) {
                return String.format("0x%04x_%04x", (value & 0xffff_0000) >> 16, value & 0xffff);
            } else {
                return String.format("0x%04x", value);
            }
        }
    }

    public static ELNumberValue number(ELType type, NumberToken nt) {
        int v;
        String vS = nt.value.replace("_", "");
        if (nt.hex) {
            v = Integer.parseInt(vS, 16);
        } else if (nt.bin) {
            v = Integer.parseInt(vS, 2);
        } else {
            v = Integer.parseInt(vS);
        }
        return new ELNumberValue(type, v);
    }
    
    public static class ELStringValue extends ELValue {
        public final String value;
        public final boolean ch;

        public ELStringValue(String value) {
            super(ELPrimitives.CHAR.pointerTo());
            this.value = value;
            ch = false;
        }

        public ELStringValue(char ch) {
            super(ELPrimitives.CHAR);
            this.value = ch + "";
            this.ch = true;
        }

        public ELStringValue(ELType type, String value, boolean ch) {
            super(type);
            this.value = value;
            this.ch = ch;
            if(!(type.equals(ELPrimitives.CHAR) || type.equals(ELPrimitives.CHAR.pointerTo())))
                throw new ELCompileException("Invalid type for string value: "+type);
        }

        @Override
        public String valueString() {
            return String.format(ch ? "\'%s\'" : "\"%s\"", value);
        }
    }

    public static ELStringValue string(ELType type, StringToken st) {
        return new ELStringValue(type, st.value, st.ch);
    }

    public static class ELArrayValue<T extends ELValue> extends ELValue {

        public final ArrayList<T> values = new ArrayList<>();

        protected ELArrayValue(ELType type) {
            super(type);
        }

        @Override
        public void resolve(ErrorSet errors) {
            for(ELValue v : values)
                v.resolve(errors);
        }

        @Override
        public String valueString() {
            String out = "[";
            for (int i = 0; i < values.size(); i++) {
                if (i > 0)
                    out += ",";
                out += values.get(i).valueString();
            }
            return out + "]";
        }
    }
    
    public static ELArrayValue<ELValue> array(ELType type, BlockToken bt, Namespace ns, ProgramUnit unit) {
        ELArrayValue<ELValue> av = new ELArrayValue<>(type);
        ELType subType = type.resolve(type.span());
        for (Token tkn : bt.subTokens) {
            if (tkn instanceof OperatorToken) {
                continue;
            }
            ELValue v = value(subType, tkn, ns, unit);
            if (v == null)
                throw ELAnalysisError.error("Unable to parse value " + tkn.debugString(), tkn);
            av.values.add(v);
        }
        return av;
    }

    public static class ELRefValue extends ELValue {

        private final IdentifierToken it;
        private final Namespace ns;
        private final ProgramUnit unit;

        private String value = "";

        protected ELRefValue(ELType type, IdentifierToken it, Namespace ns, ProgramUnit unit) {
            super(type);
            this.it = it;
            this.ns = ns;
            this.unit = unit;
        }

        @Override
        public void resolve(ErrorSet errors) {
            ELVariable v = ns.getFirstVar(it, unit);
            if (v == null) {
                errors.error("Unable to resolve variable `" + it.debugString() + '`', it);
                value = it.debugString();
            } else {
                value = v.getQualifiedName();
            }
        }

        @Override
        public String valueString() {
            return value;
        }

    }

    public static ELRefValue ref(ELType type, IdentifierToken it, Namespace ns, ProgramUnit unit) {
        return new ELRefValue(type, it, ns, unit);
    }
}
