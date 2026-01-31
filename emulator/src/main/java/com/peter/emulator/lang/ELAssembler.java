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
        out += "\n";
        ELFunction ent = module.entrypoint;
        if(ent == null)
            throw new ELCompileException("Module had no entry point");
        out += ":__start";
        for(Action action : ent.actions) {
            out += "\n"+action.toAssembly();
        }
        return out;
    }
}
