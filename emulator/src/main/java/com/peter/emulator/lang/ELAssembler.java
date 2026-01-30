package com.peter.emulator.lang;

import com.peter.emulator.lang.actions.Action;

public class ELAssembler {

    public ProgramModule module;

    public ELAssembler(ProgramModule pm) {
        module = pm;
    }

    public String assemble() {
        String out = "";
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
