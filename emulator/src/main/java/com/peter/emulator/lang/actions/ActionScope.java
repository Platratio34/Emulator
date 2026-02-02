package com.peter.emulator.lang.actions;

import java.util.ArrayList;
import java.util.HashMap;

import com.peter.emulator.MachineCode;
import com.peter.emulator.lang.*;

public class ActionScope {

    protected int stackOff = 0;
    public final int stackOffStart;
    protected final HashMap<String, ELVariable> stackVars = new HashMap<>();
    public final Namespace namespace;

    public final ActionScope parent;

    public ActionScope(Namespace namespace, ActionScope parent, int stackOffset) {
        this.parent = parent;
        this.namespace = namespace;
        stackOffStart = stackOffset;
        stackOff = stackOffset;
    }

    public void addStackVar(String name, ELType type) {
        if(stackVars.containsKey(name)) {
            throw new ELCompileException("Duplicate variable name `"+name+"`");
        }
        ELVariable var = new ELVariable(ELProtectionLevel.INTERNAL, false, type, name, false, null, type.location);
        var.offset = stackOff++;
        stackVars.put(name, var);
    }

    public void addStackVar(String name, ELType type, int offset) {
        if(stackVars.containsKey(name)) {
            throw new ELCompileException("Duplicate variable name `"+name+"`");
        }
        ELVariable var = new ELVariable(ELProtectionLevel.INTERNAL, false, type, name, false, null, type.location);
        var.offset = offset;
        stackVars.put(name, var);
    }

    public int getStackOffDif() {
        return stackOff - stackOffStart;
    }
    public DirectAction getStackResetAction() {
        return new DirectAction("STACK DEC %d", stackOff - stackOffStart);
    }
    
    public boolean hasVariable(Identifier id) {
        if(stackVars.containsKey(id.fullName))
            return true;
        if(parent != null)
            return parent.hasVariable(id);
        return false;
    }

    public ActionScope createChild() {
        return new ActionScope(namespace, parent, stackOff);
    }

    public ELVariable getVar(Identifier id) {
        if(!stackVars.containsKey(id.fullName)) {
            if(parent != null)
                return parent.getVar(id);
            return null;
        }
        return stackVars.get(id.fullName);
    }

    public void loadVar(Identifier id, int reg, ArrayList<Action> actions) {
        if(!stackVars.containsKey(id.fullName)) {
            if(parent != null) parent.loadVar(id, reg, actions);
            return;
        }
        int so = stackVars.get(id.fullName).offset;
        actions.add(new DirectAction("COPY r15 %s", MachineCode.translateReg(reg)));
        if(so != 0)
            actions.add(new DirectAction("INC %s %d", MachineCode.translateReg(reg), so));
    }
}
