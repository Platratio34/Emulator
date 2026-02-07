package com.peter.emulator.lang;

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
        if(module.entrypoint == f)
            out += "\n:__start";
        out += "\n#function " + f.getQualifiedName(true);
        boolean first = true;
        for (String p : f.paramOrder) {
            if (!first)
                out += ",";
            first = false;
            out += String.format(" %s %s", p, f.params.get(p).typeString());
        }
        for(Action action : f.actions) {
            out += "\n"+action.toAssembly();
        }
        if(module.entrypoint == f)
            out +="\nHALT";
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

    public String assemble() {
        String out = "";
        out += "// static data\n";
        for(Namespace ns : module.namespaces.values()) {
            for(ELVariable v : ns.staticVariables.values()) {
                out += String.format("#var %s %s // %s\n", v.getQualifiedName(), (v.startingValue == null)? "0x00" : v.startingValue.valueString(), v.typeString());
            }
        }
        ELFunction ent = module.entrypoint;
        if(ent == null)
            throw new ELCompileException("Module had no entry point");
        out+="\n// text";
        for(Namespace ns : module.namespaces.values()) {
            for (ELFunction f : ns.staticFunctions.values()) {
                out += assembleFunction(f);
            }
        }
        out += "\n\nHALT";
        return out;
    }
}
