package com.peter.emulator.lang.actions;

import java.util.ArrayList;
import java.util.HashMap;

import com.peter.emulator.MachineCode;
import com.peter.emulator.lang.*;
import com.peter.emulator.lang.Token.IdentifierToken;
import com.peter.emulator.lang.base.ELPrimitives;

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
     * @return If loading succeded
     */
    public boolean loadVar(Identifier id, int reg, ArrayList<Action> actions, boolean byValue) {
        if (stackVars.containsKey(id.first())) {
            ELVariable v = stackVars.get(id.first());
            int so = v.offset;
            actions.add(new DirectAction("COPY r15 %s", MachineCode.translateReg(reg)));
            if (so != 0)
                actions.add(new DirectAction("INC %s %d", MachineCode.translateReg(reg), so));
            String regStr = MachineCode.translateReg(reg);
            if (id.parts.length > 1) {
                ELType t = v.type;
                for (int i = 1; i < id.parts.length; i++) {
                    ELClass clazz = t.clazz;
                    if (clazz == null)
                        throw ELAnalysisError.fatal("Type was missing class " + t.typeString());
                    if (!clazz.memberVariables.containsKey(id.parts[i]))
                        return false;
                    ELVariable v2 = clazz.memberVariables.get(id.parts[i]);
                    actions.add(new DirectAction("INC %s %d", regStr, v2.offset));
                    t = v2.type;
                }
            }
            if (byValue)
                actions.add(new DirectAction("LOAD MEM %s %s", regStr, regStr));
            return true;
        }
        if (parent != null && parent.loadVar(id, reg, actions, byValue))
            return true;
        ArrayList<ELVariable> vars = namespace.getVarStack(id, new ArrayList<>());
        if (vars.isEmpty())
            return false;
        actions.add(new ResolveAction(this, reg, vars).byVal(byValue));
        return true;
    }

    /**
     * @return If loading succeded
     */
    public boolean loadVar(IdentifierToken id, int reg, ArrayList<Action> actions, boolean byValue) {
        if(stackVars.containsKey(id.value)) {
            ELVariable v = stackVars.get(id.value);
            int so = v.offset;
            String regStr = MachineCode.translateReg(reg);
            actions.add(new DirectAction("COPY r15 %s", regStr));
            if(so != 0)
                actions.add(new DirectAction("INC %s %d", regStr, so));
            if (id.index != null) {
                if(!v.type.isIndexable())
                    throw ELAnalysisError.error("Can not index type "+v.type.typeString(), id.index.getFirst().startLocation.span(id.index.getLast().endLocation));
                int r = firstFree();
                String rStr = MachineCode.translateReg(r);
                ExpressionAction indexExp = new ExpressionAction(this, id.index, r);
                if(!indexExp.outType.equals(ELPrimitives.UINT32))
                    throw ELAnalysisError.error("Index must resolve to a uint32",
                            id.index.getFirst().startLocation.span(id.index.getLast().endLocation));
                if (v.sizeof() > 4) {
                    String r2 = MachineCode.translateReg(firstFree());
                    actions.add(new DirectAction("LOAD %s %d\nMUL %s %s %s", r2, v.sizeofWords(), rStr, rStr, r2));
                }
                actions.add(new DirectAction("ADD %s %s %s", regStr, regStr, rStr));
                release(r);
            }
            if (id.subTokens.size() > 1) {
                ELType t = v.type;
                for (int i = 0; i < id.subTokens.size(); i++) {
                    IdentifierToken it2 = id.sub(i);
                    ELClass clazz = t.clazz;
                    if(clazz == null)
                        throw ELAnalysisError.fatal("Type was missing class " + t.typeString());
                    if(!clazz.memberVariables.containsKey(it2.value))
                        return false;
                    ELVariable v2 = clazz.memberVariables.get(it2.value);
                    actions.add(new DirectAction("INC %s %d", regStr, v2.offset));

                    if (it2.index != null) {
                        if(!v2.type.isIndexable())
                            throw ELAnalysisError.error("Can not index type "+v2.type.typeString(), id.index.getFirst().startLocation.span(id.index.getLast().endLocation));
                        int r = firstFree();
                        String rStr = MachineCode.translateReg(r);
                        ExpressionAction indexExp = new ExpressionAction(this, it2.index, r);
                        if(!indexExp.outType.equals(ELPrimitives.UINT32))
                            throw ELAnalysisError.error("Index must resolve to a uint32",
                                    it2.index.getFirst().startLocation.span(it2.index.getLast().endLocation));
                        if (v2.sizeof() > 4) {
                            String r2 = MachineCode.translateReg(firstFree());
                            actions.add(new DirectAction("LOAD %s %d\nMUL %s %s %s", r2, v2.sizeofWords(), rStr, rStr, r2));
                        }
                        actions.add(new DirectAction("ADD %s %s %s", regStr, regStr, rStr));
                        release(r);
                    }

                    t = v2.type;
                }
            }
            if(byValue)
                actions.add(new DirectAction("LOAD MEM %s %s", regStr, regStr));
            return true;
        }
        if(parent != null && parent.loadVar(id, reg, actions, byValue))
            return true;
        ArrayList<ELVariable> vars = namespace.getVarStack(id, new ArrayList<>());
        if(vars.isEmpty())
            return false;
        actions.add(new ResolveAction(this, reg, vars).byVal(byValue));
        return true;
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
}
