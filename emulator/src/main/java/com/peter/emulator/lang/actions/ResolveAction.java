package com.peter.emulator.lang.actions;

import com.peter.emulator.MachineCode;
import com.peter.emulator.lang.ELAnalysisError;
import com.peter.emulator.lang.ELClass;
import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.ELVariable;
import com.peter.emulator.lang.Token.IdentifierToken;
import com.peter.emulator.lang.base.ELPrimitives;

public class ResolveAction extends ComplexAction {

    public final int reg;
    public final ELType returnType;

    public ResolveAction(ActionScope scope, int reg, ELVariable var, IdentifierToken id, boolean byValue) {
        super(scope);
        this.reg = reg;
        String r = MachineCode.translateReg(reg);

        IdentifierToken it = id;
        int index = 0;
        if (!id.value.equals(var.name)) {
            if (id.subTokens == null)
                throw ELAnalysisError.fatal("Could not find identifier for provided variable", it);
            while (index < id.subTokens.size() - 1) {
                if (it.value.equals(var.name))
                    break;
                index++;
                if (index == id.subTokens.size() - 1)
                    throw ELAnalysisError.fatal("Could not find identifier for provided variable", it);
                it = id.sub(index - 1);
            }
        }

        switch (var.varType) {
            case CONST -> throw ELAnalysisError.error("Can not give constant to Resolve Action", it);
            case STATIC -> actions.add(new DirectAction("LOAD %s &%s", r, var.getQualifiedName()));
            case MEMBER -> actions.add(new DirectAction("COPY r0 %s\nINC %s %d", r, r, var.offset));
            case SCOPE -> {
                actions.add(new DirectAction("COPY r15 %s", r));
                if(var.offset > 0)
                    actions.add(new DirectAction("INC %s %d", r, var.offset));
            }
        }

        ELVariable v = var;
        ELType t = v.type;
        if(id.subTokens != null) {
            while (index < id.subTokens.size() - 1) {
                if (it.index != null) {
                    if (!v.type.isIndexable())
                        throw ELAnalysisError.error(v.type.typeString() + " is not indexable",
                                it.index.getFirst().startLocation.span(it.index.getLast().endLocation));
                    int r2 = scope.firstFree();
                    String rStr = MachineCode.translateReg(r2);
                    ExpressionAction indexExp = new ExpressionAction(scope, id.index, r2);
                    if (!indexExp.outType.equals(ELPrimitives.UINT32))
                        throw ELAnalysisError.error("Index must resolve to a uint32",
                                id.index.getFirst().startLocation.span(id.index.getLast().endLocation));
                    int wds = v.sizeofWords();
                    if (wds > 1) {
                        String r3 = MachineCode.translateReg(scope.firstFree());
                        actions.add(new DirectAction("LOAD %s %d\nMUL %s %s %s", r3, v.sizeofWords(), rStr, rStr, r3));
                    }
                    actions.add(new DirectAction("ADD %s %s %s", r, r, rStr));
                    scope.release(r2);
                    t = t.resolve();
                }

                index++;
                if (index == id.subTokens.size() - 1)
                    break;

                if (v.type.isPointer()) {
                    actions.add(new DirectAction("LOAD MEM %s %s", r, r));
                }
                it = id.sub(index - 1);
                ELClass clazz = t.clazz;
                if (clazz == null)
                    throw ELAnalysisError.fatal("Type was missing class " + t.typeString(), it);
                if (!clazz.memberVariables.containsKey(it.value))
                    throw ELAnalysisError.fatal("Type was missing class " + t.typeString(), it);
                v = clazz.memberVariables.get(it.value);
                t = v.type;
            }
        }
        if (byValue)
            actions.add(new DirectAction("LOAD MEM %s %s", r, r));
        returnType = t;
    }

    // @Override
    // public String toAssembly() {
    //     String out ;
    //     String r = MachineCode.translateReg(reg);
    //     ELVariable var = vars.get(0);
    //     if (var.varType == ELVariable.Type.STATIC) {
    //         out = String.format("LOAD %s &%s",r, var.getQualifiedName());
    //     } else {
    //         out = "COPY r15 "+r;
    //     }
    //     for (int i = 1; i < vars.size(); i++) {
    //         if (var.type.isPointer())
    //             out += String.format("\nLOAD MEM %s %s", r, r);
    //         var = vars.get(i);
    //         out += String.format("\nINC %s &%s", r, var.getQualifiedName());
    //     }
    //     if (resolveValue) {
    //         out += String.format("\nLOAD MEM %s %s", r, r);
    //     }
    //     return out;
    // }
}
