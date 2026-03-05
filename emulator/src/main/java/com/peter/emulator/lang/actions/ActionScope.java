package com.peter.emulator.lang.actions;

import java.util.ArrayList;
import java.util.HashMap;

import com.peter.emulator.lang.*;
import com.peter.emulator.lang.ELFunction.FunctionType;
import com.peter.emulator.lang.tokens.IdentifierToken;

public class ActionScope {

    protected int stackOff = 0;
    protected int returnOffset = 0;
    public final int stackOffStart;
    protected final HashMap<String, ELVariable> stackVars = new HashMap<>();
    public final Namespace namespace;
    public final ProgramUnit unit;

    public final ActionScope parent;
    public final ELFunction function;
    public final boolean[] reservedRegisters = new boolean[16];
    private final ArrayList<Register> registerHandles = new ArrayList<>();

    public ActionScope(Namespace namespace, ProgramUnit unit, ELFunction function) {
        this.parent = null;
        this.unit = unit;
        this.namespace = namespace;
        stackOffStart = 0;
        stackOff = 0;
        this.function = function;
        if (function != null) {
            int o = - 2;
            for (int i = function.paramOrder.size() - 1; i >= 0; i--) {
                String name = function.paramOrder.get(i);
                ELType type = function.params.get(name);
                ELVariable var = new ELVariable(ELProtectionLevel.INTERNAL, ELVariable.Type.SCOPE, type, name, false,
                        namespace, unit, type.location);
                if (stackVars.containsKey(name)) {
                    unit.errors.warning("Duplicate variable name `" + name + "`");
                    continue;
                }
                o -= type.sizeofWords();
                var.offset = o;
                stackVars.put(name, var);
            }
            if (function.ret != null) {
                returnOffset = o - function.ret.sizeofWords();
            }
        }
    }
    public ActionScope(Namespace namespace, ProgramUnit unit, ActionScope parent, int stackOffset, int returnOffset) {
        this.parent = parent;
        this.unit = unit;
        this.namespace = namespace;
        stackOffStart = stackOffset;
        stackOff = stackOffset;
        this.returnOffset = returnOffset;
        function = null;
    }

    public ELVariable addStackVar(String name, ELType type, Location endLocation, ErrorSet errors) {
        ELVariable var = new ELVariable(ELProtectionLevel.INTERNAL, ELVariable.Type.SCOPE, type, name, false, namespace,
                unit, type.location, endLocation);
        if (stackVars.containsKey(name)) {
            errors.warning("Duplicate variable name `" + name + "`");
            return var;
        }
        var.offset = stackOff;
        stackOff += type.sizeofWords();
        stackVars.put(name, var);
        return var;
    }
    
    // public void addParams(ArrayList<String> names, ArrayList<ELType> types, ErrorSet errors, ELType retType) {
    //     int o = - 2;
    //     for (int i = names.size() - 1; i >= 0; i--) {
    //         String name = names.get(i);
    //         ELType type = types.get(i);
    //         ELVariable var = new ELVariable(ELProtectionLevel.INTERNAL, ELVariable.Type.SCOPE, type, name, false,
    //                 namespace, unit, type.location);
    //         if (stackVars.containsKey(name)) {
    //             errors.warning("Duplicate variable name `" + name + "`");
    //             continue;
    //         }
    //         o -= type.sizeofWords();
    //         var.offset = o;
    //         returnOffset = o;
    //         stackVars.put(name, var);
    //     }
    //     if (retType != null) {
            
    //     }
    // }

    public int getStackOffDif() {
        return stackOff - stackOffStart;
    }

    public DirectAction getStackResetAction() {
        return new DirectAction("STACK DEC %d", stackOff - stackOffStart);
    }
    
    public Namespace getNamespace() {
        return (parent != null) ? parent.getNamespace() : namespace;
    }
    
    public boolean hasVariable(Identifier id) {
        if (id.first().equals("this"))
            return true;
        if(stackVars.containsKey(id.first()))
            return true;
        if(parent != null)
            return parent.hasVariable(id);
        return !namespace.getVarStack(id, new ArrayList<>()).isEmpty();
    }

    public ActionScope createChild() {
        return new ActionScope(namespace, unit, this, stackOff, returnOffset);
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
            ELClass clazz = t.getELClass();
            if(clazz == null)
                throw ELAnalysisError.fatal("Type was missing class (type was `" + t.typeString()+"`)");
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
    public ResolveAction loadVar(IdentifierToken id, Register reg, boolean byValue) {
        if (id.value.equals("this")) {
            Namespace ns = getNamespace();
            ELFunction func = getFunction();
            if (ns != null && func != null) {
                if (ns instanceof ELClass c && func.type != FunctionType.STATIC) {
                    ELVariable v = c.memberVariables.get(id.sub(0).value);
                    if(v == null)
                        return null;
                    return new ResolveAction(this, reg, v, id, byValue);
                } else {
                    throw ELAnalysisError.error("Can not use this outside of class instance function", id);
                }
            }
        }
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

    // public void reserve(Register reg) {
    //     reservedRegisters[reg.reg] = true;
    // }
    // public void release(Register reg) {
    //     reservedRegisters[reg.reg] = false;
    // }

    // public boolean isReserved(Register reg) {
    //     return reservedRegisters[reg.reg];
    // }
    
    public void reserve(int reg) {
        reservedRegisters[reg] = true;
    }
    public void release(int reg) {
        reservedRegisters[reg] = false;
    }

    public boolean isReserved(int reg) {
        return reservedRegisters[reg];
    }
    
    public Register makeHandle(int reg) {
        Register r = new Register(this, reg);
        registerHandles.add(r);
        return r;
    }

    public Register firstFree() {
        for (int i = 1; i < 15; i++)
            if (!reservedRegisters[i])
                return makeHandle(i);
        throw ELAnalysisError.error("No free registers");
        // return null;
    }
    public int firstFreeR() {
        for(int i = 1; i < 15; i++)
            if(!reservedRegisters[i])
                return i;
        return -1;
    }

    public ELSymbol addSymbol(ELSymbol symbol) {
        unit.symbols.add(symbol);
        return symbol;
    }

    public ELSymbol addSymbol(ELSymbol.Type type, Span span, String text) {
        ELSymbol symbol = new ELSymbol(type, span, text);
        unit.symbols.add(symbol);
        return symbol;
    }

    public ELFunction getFunction() {
        return (parent != null) ? parent.getFunction() : function;
    }
    public void freeScopeHandles(ErrorSet errors, Span span) {
        for (Register r : registerHandles) {
            if (r.reserved) {
                errors.warning("Register " + r + " was not freed before the end of the scope", span);
                r.release();
            }
        }
    }
}
