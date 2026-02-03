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

    public void addStackVar(String name, ELType type, ErrorSet errors) {
        if(stackVars.containsKey(name)) {
            errors.warning("Duplicate variable name `"+name+"`");
            return;
        }
        ELVariable var = new ELVariable(ELProtectionLevel.INTERNAL, false, type, name, false, null, type.location);
        var.offset = stackOff++;
        stackVars.put(name, var);
    }

    public void addStackVar(String name, ELType type, int offset, ErrorSet errors) {
        if(stackVars.containsKey(name)) {
            errors.warning("Duplicate variable name `"+name+"`");
            return;
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

    public boolean loadVar(Identifier id, int reg, ArrayList<Action> actions, boolean byValue) {
        if(stackVars.containsKey(id.fullName)) {
            int so = stackVars.get(id.fullName).offset;
            actions.add(new DirectAction("COPY r15 %s", MachineCode.translateReg(reg)));
            if(so != 0)
                actions.add(new DirectAction("INC %s %d", MachineCode.translateReg(reg), so));
            String regStr = MachineCode.translateReg(reg);
            if(byValue)
                actions.add(new DirectAction("LOAD MEM %s %s", regStr, regStr));
            return true;
        }
        if(parent != null && parent.loadVar(id, reg, actions, byValue))
            return true;
        ArrayList<ELVariable> vars = namespace.getVarStack(id, new ArrayList<>());
        if(vars.isEmpty())
            return false;
        actions.add(new ResolveAction(reg, vars).byVal(byValue));
        return true;
    }
}
