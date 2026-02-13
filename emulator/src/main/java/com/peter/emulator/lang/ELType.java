package com.peter.emulator.lang;

import java.util.ArrayList;

import com.peter.emulator.lang.Token.IdentifierToken;
import com.peter.emulator.lang.Token.NumberToken;
import com.peter.emulator.lang.Token.OperatorToken;
import com.peter.emulator.lang.base.ELPrimitives;

public class ELType {

    protected boolean constant;
    protected Identifier baseClass;
    protected ArrayList<ELType> genericTypes = new ArrayList<>();
    protected boolean array;
    protected int arraySize = 0;
    protected boolean pointer;
    protected boolean address;
    protected ELType subType = null;

    public Location location;
    public Location genericLocation = null;
    public Location endLocation;

    public ELClass clazz;

    protected ELType() {

    }

    public ELType(ELType subType) {
        this.subType = subType;
    }

    public ELType(String base) {
        if (base.contains(".")) {
            baseClass = new Identifier(base.split("\\."));
        } else {
            baseClass = new Identifier(base);
        }
        location = new Location("", 0, 0);
        endLocation = location;
    }

    public ELType(String base, ELClass clazz) {
        this.clazz = clazz;
        if (base.contains(".")) {
            baseClass = new Identifier(base.split("\\."));
        } else {
            baseClass = new Identifier(base);
        }
        location = new Location("", 0, 0);
        endLocation = location;
    }

    public ELType(String base, boolean pointer, boolean array) {
        this(base);
        this.pointer = pointer;
        this.array = array;
    }

    public ELType(String base, ArrayList<ELType> genericTypes) {
        this(base);
        this.genericTypes = genericTypes;
    }

    @Override
    public String toString() {
        String out = "ELType{";
        if (subType == null) {
            out += "baseClass=\"" + baseClass + "\"";
        } else {
            out += "subType=" + subType.toString();
        }
        if (!genericTypes.isEmpty()) {
            out += ", generics={";
            boolean f = true;
            for (ELType t : genericTypes) {
                if (!f)
                    out += ",";
                f = false;
                out += t.toString();
            }
            out += "}";
        }
        if (pointer)
            out += ", pointer";
        if (array)
            out += ", array";
        if (address)
            out += ", address";
        return out + "}";
    }

    public Span span() {
        return location.span(endLocation);
    }

    public String qualifiedBaseClass() {
        return baseClass.fullName;
    }

    public String typeString() {
        String out = "";
        if (subType == null) {
            if (constant)
                out += "const ";
            out += baseClass.fullName;
            if (!genericTypes.isEmpty()) {
                out += "<";
                for (int i = 0; i < genericTypes.size(); i++) {
                    if (i > 0)
                        out += ",";
                    out += genericTypes.get(i).typeString();
                }
                out += ">";
            }
        } else {
            out = subType.typeString();
        }
        if (array) {
            if(arraySize > 0) {
                out += String.format("[%d]", arraySize);
            } else {
                out += "[]";
            }
        }
        if (pointer)
            out += "*";
        if (address)
            out += "&";
        return out;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ELType))
            return false;
        return equals(obj, true);
    }

    public boolean equals(Object obj, boolean ignoreConst) {
        if (obj == this)
            return true;
        if (obj == null) {
            // System.out.println("Other was null");
            return false;
        }
        if (obj instanceof ELType ot) {
            if (constant != ot.constant && !ignoreConst) {
                // System.out.println("const mismatch");
                return false;
            } else if (pointer != ot.pointer) {
                // System.out.println("pointer mismatch");
                return false;
            } else if (array != ot.array) {
                // System.out.println("array mismatch");
                return false;
            } else if (arraySize != ot.arraySize) {
                return false;
            } else if (address != ot.address) {
                // System.out.println("address mismatch");
                return false;
            } else if ((subType != null && ot.subType == null) || (subType == null && ot.subType != null)) {
                // System.out.println("subtype presence mismatch");
                return false;
            }
            if (subType == null) {
                if (!baseClass.equals(ot.baseClass)) {
                    // System.out.println("base class mismatch");
                    return false;
                }
            } else if (!subType.equals(ot.subType)) {
                // System.out.println("subtype mismatch");
                return false;
            }
            if (genericTypes.size() != ot.genericTypes.size()) {
                // System.out.println("generics size mismatch");
                return false;
            }
            for (int i = 0; i < genericTypes.size(); i++) {
                if (!genericTypes.get(i).equals(ot.genericTypes.get(i))) {
                    // System.out.println("generic mismatch");
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean isVoid() {
        if (baseClass == null)
            return false;
        return baseClass.equals("void") && !pointer;
    }

    public boolean isVoidPtr() {
        if(subType != null && subType.isVoid())
            return pointer;
        return false;
    }

    public boolean hasGenerics() {
        return !genericTypes.isEmpty();
    }

    public boolean isSimple() {
        return !pointer && genericTypes.isEmpty() && !array && !constant;
    }

    public ELType pointerTo() {
        if (constant)
            throw new ELCompileException("Can not get pointer to constant value");
        ELType t2 = new ELType(this);
        t2.pointer = true;
        return t2;
    }
    
    public boolean isPointer() {
        return pointer;
    }

    @Override
    public int hashCode() {
        return typeString().hashCode();
    }
    
    public static class Builder {

        private ELType type = new ELType();

        int i = 0;
        boolean inTypes;
        Builder subBuilder;
        boolean built = false;
        boolean constant = false;
        boolean baseSet = false;

        public Builder() {

        }

        public Builder(String base) {
            type.baseClass = new Identifier(base);
            baseSet = true;
        }

        public Builder(ELType base) {
            type.baseClass = base.baseClass;
            baseSet = true;
        }

        public boolean ingest(Token token) {
            if (built)
                throw new ELCompileException("Type builder ingest called after build");
            if (inTypes) {
                if (subBuilder.ingest(token))
                    return true;
                else {
                    ELType t2 = subBuilder.build();
                    type.genericTypes.add(t2);
                    subBuilder = null;
                }
                if (token instanceof OperatorToken ot2 && ot2.type == OperatorToken.Type.COMMA) {
                    subBuilder = new Builder();
                    subBuilder.ingest(token);
                    return true;
                }
            }
            switch (token) {
                case IdentifierToken it -> {
                    if (it.value.equals("const")) {
                        constant = true;
                        return true;
                    } else if (!baseSet) {
                        baseSet = true;
                        type.baseClass = new Identifier(it.value);
                        type.location = token.startLocation;
                        type.endLocation = token.endLocation;
                        if (it.hasSub()) {
                            Identifier.Builder b = type.baseClass.builder();
                            while (it.hasSub()) {
                                Token tkn = it.subTokens.get(0);
                                if (tkn instanceof IdentifierToken it2) {
                                    it = it2;
                                    b.ingest(it2.value);
                                    type.endLocation = token.endLocation;
                                } else {
                                    throw new ELCompileException("Unexpected token found, expected identifier: " + tkn);
                                }
                            }
                            type.baseClass = b.build();
                        }
                        i++;
                        return true;
                    }
                    return false;
                }
                case OperatorToken ot -> {
                    if (ot.type == OperatorToken.Type.ANGLE_LEFT) {
                        if (type.pointer || type.array || type.subType != null)
                            throw new ELCompileException("Found < in incorrect position");
                        if (inTypes)
                            throw new ELCompileException("Found < inside of types");
                        if (!type.genericTypes.isEmpty())
                            throw new ELCompileException("Found < past types");
                        inTypes = true;
                        type.genericLocation = ot.startLocation;
                        subBuilder = new Builder();
                        return true;
                    } else if (ot.type == OperatorToken.Type.ANGLE_RIGHT) {
                        if (!inTypes)
                            return false;
                        inTypes = false;
                        type.endLocation = ot.endLocation;
                        return true;
                    } else if (ot.type == OperatorToken.Type.POINTER) {
                        pointer();
                        type.location = token.startLocation;
                        type.endLocation = token.endLocation;
                        return true;
                    } else if (ot.type == OperatorToken.Type.INDEX && ot.indexClosed) {
                        array();
                        type.location = token.startLocation;
                        type.endLocation = token.endLocation;
                        if (!ot.subTokens.isEmpty()) {
                            if (!(ot.subTokens.get(0) instanceof NumberToken))
                                throw ELAnalysisError.error("Expected number token, found "+ot.subTokens.get(0));
                            type.arraySize = ELValue.number(ELPrimitives.UINT32, (NumberToken)ot.subTokens.get(0)).value;
                        }
                        return true;
                    } else if (ot.type == OperatorToken.Type.BITWISE_AND) {
                        address();
                        type.location = token.startLocation;
                        type.endLocation = token.endLocation;
                        return true;
                    }
                }
                default -> {
                }
            }
            return false;
        }

        public Builder pointer() {
            type = new ELType(type);
            type.pointer = true;
            return this;
        }

        public Builder array() {
            type = new ELType(type);
            type.array = true;
            return this;
        }

        public Builder array(int size) {
            type = new ELType(type);
            type.array = true;
            type.arraySize = size;
            return this;
        }

        public Builder address() {
            type = new ELType(type);
            type.address = true;
            return this;
        }

        public Builder location(Location location) {
            ELType t = type;
            if (t.subType != null) {
                t = t.subType;
            }
            t.location = location;
            t.endLocation = location;
            return this;
        }

        public ELType build() {
            if (!baseSet)
                throw new ELCompileException("Base was never set");
            built = true;
            type.constant = constant;
            return type;
        }
    }
    
    public ELType nonConst() {
        if(!constant)
            return this;
        ELType t = new ELType();
        t.baseClass = baseClass;
        t.subType = subType;
        t.genericTypes = genericTypes;
        t.pointer = pointer;
        t.array = array;
        t.address = address;
        t.location = location;
        t.endLocation = location;
        t.genericLocation = location;
        return t;
    }

    public ELType base() {
        if (subType != null)
            return subType.base();
        return this.nonConst();
    }

    public void analyze(ErrorSet errors, Namespace namespace, ProgramUnit unit) {
        if(isVoidPtr())
            return;
        for(ELType type : genericTypes)
            type.analyze(errors, namespace, unit);
        ELType base = base();
        if (ELPrimitives.PRIMITIVE_TYPES.containsKey(base)) {
            clazz = ELPrimitives.PRIMITIVE_TYPES.get(base);
            return;
        }
        ELClass clazz = namespace.getType(base, namespace, unit);
        if (clazz == null) {
            clazz = unit.module.getType(base, namespace, unit);
        }
        if (clazz == null) {
            errors.error("Unknown type: " + base.typeString(), base.span());
            return;
        }
        if (clazz.genericsOrder.isEmpty() && !base.genericTypes.isEmpty()) {
            errors.error("Class " + base.typeString() + " does not have type parameters", base.genericLocation.span(base.endLocation));
            return;
        }
        if (clazz.genericsOrder.size() != base.genericTypes.size()) {
            errors.error("Incorrect number of type parameters: expected "+clazz.genericsOrder.size()+", found "+base.genericTypes.size(), base.endLocation.span());
            return;
        }
        for (int i = 0; i < clazz.genericsOrder.size(); i++) {
            String gN = clazz.genericsOrder.get(i);
            ELType gT = clazz.generics.get(gN);
            ELType pT = base.genericTypes.get(i);
            if(gT == null) // fully generic type
                continue;
            if (!pT.canCastTo(gT)) {
                errors.add(ELAnalysisError
                        .error(String.format("Invalid type parameter type for parameter %s; Found %s, expected %s or child", gN, pT.typeString(), gT.typeString()), pT.span()));

            }
        }
        if(array && arraySize == 0) {
            errors.error("Array type must specify size", location.span());
        }
        this.clazz = clazz;
    }

    public boolean canCastTo(ELType target) {
        // check modifiers
        if ((subType == null) != (target.subType == null)) {
            return false;
        }
        if(subType != null) {
            ELType t = this;
            ELType tt = target;
            while (t.subType != null) {
                if ((t.pointer != tt.pointer || t.array != tt.array) && !(t.array && tt.pointer)) {
                    return false;
                }
                t = t.subType;
                tt = tt.subType;
            }
        }

        // check base
        if (ELPrimitives.OBJECT.equals(target))
            return true;
        ELType base = base();
        ELType tgtBase = base();
        if (base.clazz != null && tgtBase.clazz != null) {
            boolean sameClass = base.clazz != tgtBase.clazz;
            if (!sameClass) {
                ELClass c = base.clazz;
                while (c.parent != null) {
                    c = c.parent;
                    if (c == tgtBase.clazz) {
                        sameClass = true;
                        break;
                    }
                }
                if (!sameClass && clazz.canStaticCast(target)) {
                    return true;
                }
            }
            if (!sameClass) {
                return false;
            }
            if (base.hasGenerics()) {
                if(!tgtBase.hasGenerics())
                    return false;
                if(base.genericTypes.size() != tgtBase.genericTypes.size())
                    return false;
                for(int i = 0; i < base.genericTypes.size(); i++) {
                    if(!base.genericTypes.get(i).canCastTo(tgtBase.genericTypes.get(i)))
                        return false;
                }
                // generics match
            }
        }
        return equals(target);
    }

    public Builder builder() {
        return new Builder(this);
    }
}
