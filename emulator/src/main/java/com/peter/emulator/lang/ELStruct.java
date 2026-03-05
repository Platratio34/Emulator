package com.peter.emulator.lang;

public class ELStruct extends ELClass {

    public ELStruct(String name, Namespace namespace, ProgramUnit unit) {
        super(name, namespace, unit);
    }

    @Override
    public String getClassType() {
        return "struct";
    }
    
    @Override
    public String toString() {
        String out = String.format("ELStruct{cName=\"%s\"", cName);
        if (namespace != null) {
            out += ", namespace=" + namespace.toString();
        }
        if (parentType != null) {
            out += ", parentType=" + parentType.toString();
        }
        if (parent != null) {
            out += ", parent=" + parent.toString();
        }
        if (!genericsOrder.isEmpty()) {
            out += ", generics=[";
            for (String t : genericsOrder) {
                out += String.format("%s:%s", t, generics.get(t).toString());
            }
        }
        if (abstractClass) {
            out += ", abstractClass";
        }
        return out + "}";
    }
}
