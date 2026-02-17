package com.peter.emulator.lang.actions;

import java.util.ArrayList;
import java.util.HashMap;

import com.peter.emulator.lang.*;
import com.peter.emulator.lang.Token.IdentifierToken;

public class ActionScope {

    protected int stackOff = 0;
    public final int stackOffStart;
    protected final HashMap<String, ELVariable> stackVars = new HashMap<>();
    public final Namespace namespace;
    public final ProgramUnit unit;

    public final ActionScope parent;
    public final boolean[] reservedRegisters = new boolean[16];

    public ActionScope(Namespace namespace, ProgramUnit unit, ActionScope parent, int stackOffset) {
        this.parent = parent;
        this.unit = unit;
        this.namespace = namespace;
        stackOffStart = stackOffset;
        stackOff = stackOffset;
    }

    public ELVariable addStackVar(String name, ELType type, Location endLocation, ErrorSet errors) {
        ELVariable var = new ELVariable(ELProtectionLevel.INTERNAL, ELVariable.Type.SCOPE, type, name, false, namespace, unit, type.location, endLocation);
        if(stackVars.containsKey(name)) {
            errors.warning("Duplicate variable name `"+name+"`");
            return var;
        }
        var.offset = stackOff++;
        stackVars.put(name, var);
        return var;
    }

    public ELVariable addParam(String name, ELType type, int offset, ErrorSet errors) {
        ELVariable var = new ELVariable(ELProtectionLevel.INTERNAL, ELVariable.Type.SCOPE, type, name, false, namespace, unit, type.location);
        if(stackVars.containsKey(name)) {
            errors.warning("Duplicate variable name `"+name+"`");
            return var;
        }
        var.offset = offset - 2; // offseting by 2 to account for return address and r15
        stackVars.put(name, var);
        return var;
    }

    public int getStackOffDif() {
        return stackOff - stackOffStart;
    }
    public DirectAction getStackResetAction() {
        return new DirectAction("STACK DEC %d", stackOff - stackOffStart);
    }
    
    public boolean hasVariable(Identifier id) {
        if(stackVars.containsKey(id.first()))
            return true;
        if(parent != null)
            return parent.hasVariable(id);
        return !namespace.getVarStack(id, new ArrayList<>()).isEmpty();
    }

    public ActionScope createChild() {
        return new ActionScope(namespace, unit, this, stackOff);
    }

    public ArrayList<ELVariable> getVarStack(Identifier id) {
        if (!stackVars.containsKey(id.first())) {
            if (parent != null)
                return parent.getVarStack(id);
            if (namespace != null)
                return namespace.getVarStack(id, new ArrayList<>());
            return null;
        }
        ArrayList<ELVariable> vars = new ArrayList<>();
        ELVariable v = stackVars.get(id.first());
        vars.add(v);
        ELType t = v.type;
        for (int i = 1; i < id.parts.length; i++) {
            ELClass clazz = t.clazz;
            if(clazz == null)
                throw ELAnalysisError.fatal("Type was missing class " + t.typeString());
            if(!clazz.memberVariables.containsKey(id.parts[i]))
                throw ELAnalysisError.fatal("Class " + clazz.getQualifiedName() + " does not contain member variable "+id.parts[i]);
            ELVariable v2 = clazz.memberVariables.get(id.parts[i]);
            vars.add(v2);
            t = v2.type;
        }
        return vars;
    }

    /**
     * @return the resolve action
     */
    public ResolveAction loadVar(IdentifierToken id, int reg, boolean byValue) {
        if(stackVars.containsKey(id.value)) {
            ELVariable v = stackVars.get(id.value);
            return new ResolveAction(this, reg, v, id, byValue);
        }
        if(parent != null)
            return parent.loadVar(id, reg, byValue);
        ELVariable v = namespace.getFirstVar(id, unit);
        if(v == null)
            return null;
        return new ResolveAction(this, reg, v, id, byValue);
    }

    public void reserve(int reg) {
        reservedRegisters[reg] = true;
    }
    public void release(int reg) {
        reservedRegisters[reg] = false;
    }
    public boolean isReserved(int reg) {
        return reservedRegisters[reg];
    }
    public int firstFree() {
        for(int i = 1; i < 15; i++)
            if(!reservedRegisters[i])
                return i;
        return -1;
    }

    public ELSymbol addSymbol(ELSymbol symbol) {
        unit.symbols.add(symbol);
        return symbol;
    }
}
