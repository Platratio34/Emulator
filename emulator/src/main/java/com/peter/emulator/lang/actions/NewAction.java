package com.peter.emulator.lang.actions;

import com.peter.emulator.lang.ELAnalysisError;
import com.peter.emulator.lang.ELClass;
import com.peter.emulator.lang.ELSymbol;
import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.base.ELPrimitives;
import com.peter.emulator.lang.tokens.IdentifierToken;
import com.peter.emulator.lang.tokens.SetToken;

public class NewAction extends ComplexAction {

    public final ELType retType;

    public NewAction(ActionScope scope, IdentifierToken it, Register targetReg) {
        super(scope);

        ELClass clazz = null;
        if(!it.hasSub()) {
            ELType type = new ELType(it.value);
            if(ELPrimitives.PRIMITIVE_TYPES.containsKey(type)) {
                clazz = ELPrimitives.PRIMITIVE_TYPES.get(type);
            }
        }
        if(clazz == null) {
            clazz = scope.findClass(it);
        }
        if (clazz == null) {
            throw ELAnalysisError.error(String.format("Could not find class or struct `%s`", it.asId()), it.span());
        }
        IdentifierToken it2 = it;
        while (it2.hasSub()) {
            scope.addSymbol(ELSymbol.Type.NAMESPACE_NAME, it2.spanFirst());
            it2 = it2.next();
        }
        scope.addSymbol(ELSymbol.Type.CLASS_NAME, it2.spanFirst());

        int classSize = clazz.getSize();
        if(it.hasParams()) { // constructer
            // needs to be eqivelant to:
            // T* p = malloc(T.sizeof());
            // T(p, args)
            // return p
            addDirect("LOAD %s %d", targetReg, classSize);
            addDirect("STACK INC 4\nSTACK PUSH %s", targetReg);
            addDirect("GOTO PUSH :Memory.malloc_uint32");
            addDirect("STACK INC -4\nSTACK POP %s", targetReg);
            // addDirect("STACK PUSH %s", targetReg);
            // FunctionAction fa = new FunctionAction(scope, null, clazz)
            SetToken st = it.getParamsSub();
            if (st.subSize() > 0) {
                scope.unit.errors.warning("Parameterized constructors not currently supported", st);
            }
        } else if (it.indexed()) { // array
            targetReg.reserve();
            ExpressionAction eA = new ExpressionAction(scope, it.index.subTokens, targetReg);
            if(eA.wasConst) {
                addDirect("LOAD %s %d", targetReg, classSize * eA.constValue);
            } else {
                Register sr = scope.firstFree();
                addDirect("LOAD %s %d\nMUL %s %s %s", sr, classSize, targetReg, targetReg, sr);
                sr.release();
            }
            // needs to be eqivelant to:
            // malloc(T.sizeof() * len); -> malloc(r[sr])
            addDirect("STACK INC 4\nSTACK PUSH %s", targetReg);
            addDirect("GOTO PUSH :Memory.malloc_uint32");
            addDirect("STACK INC -4\nSTACK POP %s", targetReg);
        } else {
            throw ELAnalysisError.error("Unknown new expression. Expected array size or constructor", it);
        }

        retType = clazz.getType().pointerTo();
    }

}
