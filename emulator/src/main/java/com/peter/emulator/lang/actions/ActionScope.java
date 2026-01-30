package com.peter.emulator.lang.actions;

import java.util.ArrayList;
import java.util.HashMap;

import com.peter.emulator.MachineCode;
import com.peter.emulator.lang.ELCompileException;
import com.peter.emulator.lang.ELProtectionLevel;
import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.ELVariable;
import com.peter.emulator.lang.Namespace;
import com.peter.emulator.lang.Token.IdentifierToken;

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
        ELVariable var = new ELVariable(ELProtectionLevel.INTERNAL, false, type, name, false, type.location);
        var.offset = stackOff++;
        stackVars.put(name, var);
    }

    public int getStackOffDif() {
        return stackOff - stackOffStart;
    }
    public DirectAction getStackResetAction() {
        return new DirectAction("INC rStack -%d", stackOff - stackOffStart);
    }
    
    public boolean hasVariable(IdentifierToken idt) {
        if(stackVars.containsKey(idt.value))
            return true;
        if(parent != null)
            return parent.hasVariable(idt);
        return false;
    }

    public ActionScope createChild() {
        return new ActionScope(namespace, parent, stackOff);
    }

    public ELVariable getVar(IdentifierToken idt) {
        String name = idt.value;
        if(!stackVars.containsKey(name)) {
            if(parent != null)
                return parent.getVar(idt);
            return null;
        }
        return stackVars.get(name);
    }

    public void loadVar(IdentifierToken idt, int reg, ArrayList<Action> actions) {
        String name = idt.value;
        if(!stackVars.containsKey(name)) {
            if(parent != null) parent.loadVar(idt, reg, actions);
            return;
        }
        int so = stackVars.get(name).offset;
        actions.add(new DirectAction("COPY r15 %s", MachineCode.translateReg(reg)));
        if(so > 0)
            actions.add(new DirectAction("INC %s %d", MachineCode.translateReg(reg), so));
    }
}
