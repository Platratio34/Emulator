package com.peter.emulator.lang;

import java.util.ArrayList;

import com.peter.emulator.lang.Token.IdentifierToken;
import com.peter.emulator.lang.Token.OperatorToken;
import com.peter.emulator.lang.base.ELPrimitives;

public class ELType {

    protected boolean constant;
    protected String baseClass = "";
    protected ArrayList<String> baseClassParents = new ArrayList<>();
    protected ArrayList<ELType> genericTypes = new ArrayList<>();
    protected boolean array;
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
            String[] p = base.split(".");
            for (int i = 0; i < p.length - 1; i++) {
                baseClassParents.add(p[i]);
            }
            baseClass = p[p.length];
        } else {
            baseClass = base;
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
            if (!baseClassParents.isEmpty()) {
                out += "baseClassParents={";
                boolean f = true;
                for (String p : baseClassParents) {
                    if (!f)
                        out += ",";
                    f = false;
                    out += p;
                }
                out += "}, ";
            }
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

    public String qualifiedBaseClass() {
        String out = baseClass;
        for (String c : baseClassParents) {
            out = c + "." + out;
        }
        return out;
    }

    public String typeString() {
        String out = "";
        if (subType == null) {
            if (constant)
                out += "const ";
            for (String p : baseClassParents)
                out += p + ".";
            out += baseClass;
            if (genericTypes.size() > 0) {
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
        if (array)
            out += "[]";
        if (pointer)
            out += "*";
        if (address)
            out += "&";
        return out;
    }
    
    @Override
    public boolean equals(Object obj) {
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
                } else if (baseClassParents.size() != ot.baseClassParents.size()) {
                    // System.out.println("base parents size mismatch");
                    return false;
                }
                for (int i = 0; i < baseClassParents.size(); i++) {
                    if (!baseClassParents.get(i).equals(ot.baseClassParents.get(i))) {
                        // System.out.println("base parents mismatch");
                        return false;
                    }
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
            type.baseClass = base;
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
            if (token instanceof IdentifierToken it) {
                if (it.value.equals("const")) {
                    constant = true;
                    return true;
                } else if (!baseSet) {
                    baseSet = true;
                    type.baseClass = it.value;
                    type.location = token.startLocation;
                    type.endLocation = token.endLocation;
                    while (it.hasSub()) {
                        Token tkn = it.subTokens.get(0);
                        if (tkn instanceof IdentifierToken it2) {
                            it = it2;
                            type.baseClassParents.add(type.baseClass);
                            type.baseClass = it2.value;
                            type.endLocation = token.endLocation;
                        } else {
                            throw new ELCompileException("Unexpected token found, expected identifier: " + tkn);
                        }
                    }
                    i++;
                    return true;
                }
                return false;
            } else if (token instanceof OperatorToken ot) {
                if (ot.type == OperatorToken.Type.ANGLE_LEFT) {
                    if (type.pointer || type.array || type.subType != null)
                        throw new ELCompileException("Found < in incorrect position");
                    if (inTypes)
                        throw new ELCompileException("Found < inside of types");
                    if (type.genericTypes.size() > 0)
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
                    return true;
                } else if (ot.type == OperatorToken.Type.BITWISE_AND) {
                    address();
                    type.location = token.startLocation;
                    type.endLocation = token.endLocation;
                    return true;
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
        t.baseClassParents = baseClassParents;
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

    public void analyze(ArrayList<ELAnalysisError> errors, Namespace namespace, ProgramModule module) {
        if(isVoidPtr())
            return;
        for(ELType type : genericTypes)
            type.analyze(errors, namespace, module);
        ELType base = base();
        if (ELPrimitives.PRIMITIVE_TYPES.contains(base))
            return;
        ELClass clazz = namespace.getType(base, namespace, module);
        if (clazz == null) {
            clazz = module.getType(base, namespace);
        }
        if (clazz == null) {
            errors.add(ELAnalysisError.error("Unknown type: " + base.typeString(), base.location));
            return;
        }
        if (clazz.genericsOrder.isEmpty() && !base.genericTypes.isEmpty()) {
            errors.add(ELAnalysisError.error("Class " + base.typeString() + " does not have type parameters",
                    base.genericLocation));
            return;
        }
        if (clazz.genericsOrder.size() != base.genericTypes.size()) {
            errors.add(ELAnalysisError.error("Incorrect number of type parameters: expected "+clazz.genericsOrder.size()+", found "+base.genericTypes.size(), base.endLocation));
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
                        .error(String.format("Invalid type parameter type for parameter %s; Found %s, expected %s or child", gN, pT.typeString(), gT.typeString()), pT.location));
                continue;
            }
        }
        this.clazz = clazz;
    }

    public boolean canCastTo(ELType target) {
        // check modifiers
        if((subType == null) != (target.subType != null))
            return false;
        if(subType != null) {
            ELType t = this;
            ELType tt = target;
            while (t.subType != null) {
                if(t.pointer != tt.pointer || t.array != tt.array)
                    return false;
            }
        }

        // check base
        if (ELPrimitives.OBJECT.equals(target))
            return true;
        ELType base = base();
        ELType tgtBase = base();
        if (base.clazz != null && tgtBase.clazz != null) {
            boolean sameClass = false;
            if (base.clazz != tgtBase.clazz) {
                ELClass c = base.clazz;
                while (c.parent != null) {
                    c = c.parent;
                    if (c == tgtBase.clazz) {
                        sameClass = true;
                        break;
                    }
                }
            }
            if(!sameClass)
                return false;
            if(base.hasGenerics()) {
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
        if (equals(target))
            return true;
        return false;
    }
}
