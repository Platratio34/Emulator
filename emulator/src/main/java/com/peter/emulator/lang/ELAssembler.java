package com.peter.emulator.lang;

import com.peter.emulator.lang.actions.Action;

public class ELAssembler {

    public ProgramModule module;

    public ELAssembler(ProgramModule pm) {
        module = pm;
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
            for(ELFunction f : ns.staticFunctions.values()) {
                if(ent == f)
                    out += "\n:__start";
                out +="\n:"+f.getQualifiedName(true);
                for(Action action : f.actions) {
                    out += "\n"+action.toAssembly();
                }
                if(ent == f)
                    out +="\nHALT";
                else
                    out += "\nGOTO POP";
                // out += String.format("#var %s %s // %s\n", v.getQualifiedName(), (v.startingValue == null)? "0x00" : v.startingValue.valueString(), v.typeString());
            }
        }
        out += "\n\nHALT";
        return out;
    }
}
