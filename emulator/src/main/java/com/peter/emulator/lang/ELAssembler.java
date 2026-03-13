package com.peter.emulator.lang;

import com.peter.emulator.lang.ELVariable.Type;
import com.peter.emulator.lang.actions.Action;
import com.peter.emulator.lang.annotations.ELInterruptHandlerAnnotation;

public class ELAssembler {

    public ProgramModule module;

    public ELAssembler(ProgramModule pm) {
        module = pm;
    }

    private String assembleFunction(ELFunction f) {
        String out = "";
        out += "\n";
        if (module.entrypoint == f)
            out += "\n:__start";
        out += "\n#function " + f.getQualifiedName(true);
        boolean first = true;
        for (String p : f.paramOrder) {
            if (!first)
                out += ",";
            first = false;
            out += String.format(" %s %s", p, f.params.get(p).typeString());
        }
        for (Action action : f.actions) {
            out += "\n" + action.toAssembly();
        }
        if (module.entrypoint == f)
            out += "\nHALT";
        else if (f.hasAnnotation(ELInterruptHandlerAnnotation.class))
            out += "\nINTERRUPT RET";
        else
            out += "\nGOTO POP";
        out += "\n#endfunction " + (f.ret == null ? "void" : f.ret.typeString());
        for (ELFunction f2 : f.overloads) {
            out += assembleFunction(f2);
        }
        return out;
    }

    private String assembleStatics(Namespace ns) {
        if(ns.staticVariables.isEmpty() && ns.namespaces.isEmpty())
            return "";
        String out = "// " + ns.getQualifiedName() + "\n";
        for (ELVariable v : ns.staticVariables.values()) {
            out += (v.varType == Type.CONST) ? "#define" : "#var";
            if (v.type.isArray()) {
                out += String.format(" %s %s // %s\n", v.getQualifiedName(), (v.startingValue == null) ? String.format("(%d)", v.sizeof()) : v.startingValue.valueString(),
                        v.typeString());
            } else if (v.sizeof() > 4) {
                out += String.format(" %s %s // %s\n", v.getQualifiedName(), String.format("(%d)", v.sizeof()),
                        v.typeString());
            } else {
                out += String.format(" %s %s // %s\n", v.getQualifiedName(),
                        (v.startingValue == null) ? "0x00" : v.startingValue.valueString(), v.typeString());
            }
        }
        for (Namespace n : ns.namespaces.values()) {
            out += assembleStatics(n);
        }
        return out;
    }
    
    private String assembleFunctions(Namespace ns) {
        String out = "\n\n// " + ns.getQualifiedName();
        for (ELFunction f : ns.staticFunctions.values()) {
            out += assembleFunction(f);
        }
        if (ns instanceof ELClass c) {
            if (c.constructor != null)
                out += assembleFunction(c.constructor);
            if (c.destructor != null)
                out += assembleFunction(c.destructor);
            for (ELFunction f : c.memberFunctions.values()) {
                out += assembleFunction(f);
            }
        }
        for (Namespace n : ns.namespaces.values()) {
            out += assembleFunctions(n);
        }
        return out;
    }
    
    public String assemble() {
        String out = "";
        out += "// static data\n";
        for(Namespace ns : module.namespaces.values()) {
            out += assembleStatics(ns);
        }
        ELFunction ent = module.entrypoint;
        if(ent == null)
            throw new ELCompileException("Module had no entry point");
        out += "\n//--------\n// text";
        // out += "\n\n#function <init>\\n:__start";
        // out += "\nGOTO :" + ent.getQualifiedName(true);
        // out += "\n#endfunction void";
        for(Namespace ns : module.namespaces.values()) {
            out += assembleFunctions(ns);
        }
        out += "\n\nHALT";
        return out;
    }
}
