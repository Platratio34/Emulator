package com.peter.emulator.lang.actions;

import java.util.ArrayList;

import com.peter.emulator.lang.ELAnalysisError;
import com.peter.emulator.lang.ELClass;
import com.peter.emulator.lang.ELFunction;
import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.Location;
import com.peter.emulator.lang.base.ELPrimitives;
import com.peter.emulator.lang.expresion.Expression;
import com.peter.emulator.lang.symbols.SymbolType;
import com.peter.emulator.lang.tokens.IdentifierToken;
import com.peter.emulator.lang.tokens.OperatorToken;
import com.peter.emulator.lang.tokens.SetToken;
import com.peter.emulator.lang.tokens.Token;

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
            scope.addSymbol(SymbolType.NAMESPACE_NAME, it2.spanFirst());
            it2 = it2.next();
        }
        scope.addSymbol(SymbolType.CLASS_NAME, it2.spanFirst());

        int classSize = clazz.getSize();

        addDirect("STACK PUSH r0");

        retType = clazz.getType().pointerTo();
        if(it.hasParams()) { // constructer
            // needs to be eqivelant to:
            // T* p = malloc(T.sizeof());
            // T(p, args)
            // return p
            addDirect("LOAD %s %d", targetReg, classSize);
            addDirect("STACK INC 4\nSTACK PUSH %s", targetReg);
            addDirect("GOTO PUSH :Memory.malloc_int32");
            addDirect("STACK INC -4\nSTACK POP %s", targetReg);

            String endLbl = String.format(":end_constructor_%d", ActionBlock.subIndex++);
            if(it.getParamsSub().subSize() > 0 || clazz.constructor != null)
                addDirect("GOTO EQ %s %s", targetReg, endLbl);
            // addDirect("STACK PUSH %s", targetReg);
            // FunctionAction fa = new FunctionAction(scope, null, clazz)
            SetToken st = it.getParamsSub();
            ArrayList<ELType> paramTypes = new ArrayList<>();
            int stackSize = 0;
            if (st.subSize() > 0) {
                ArrayList<Token> expList = new ArrayList<>();
                Register rg = newRegister();
                addReserve(rg);
                for (Token t : st.subTokens) {
                    if (t instanceof OperatorToken ot && ot.type == OperatorToken.Type.COMMA) {
                        if (expList.isEmpty()) {
                            scope.unit.errors.errorF(ot, "Empty expression");
                            continue;
                        }
                        Expression exp = new Expression(scope, expList, rg);
                        expList.clear();
                        add(exp);
                        ELType pType = exp.getType();
                        paramTypes.add(pType);
                        switch (pType.sizeof()) {
                            case 1 -> addDirect("STACK PUSH BYTE %s", rg);
                            case 2 -> addDirect("STACK PUSH SHORT %s", rg);
                            default -> addDirect("STACK PUSH %s", rg);
                        }
                        stackSize += 4;
                    }
                }
                addRelease(rg);
            }
            
            if (clazz.constructor == null) {
                if (paramTypes.isEmpty()) {
                    // No constructor needed?
                    // TODO Or this should probably be fill for starting values...
                    return;
                }
                scope.unit.errors.errorF(it.span(), "No parameterized constructor for class %s", clazz.getQualifiedName());
                return;
            }
            ELFunction con = clazz.constructor.getFunction(paramTypes);
            if (con == null) {
                String tStr = "(";
                for (int i = 0; i < paramTypes.size(); i++) {
                    if (i > 0)
                        tStr += ",";
                    tStr += paramTypes.get(i).typeString();
                }
                tStr += ")";
                scope.unit.errors.errorF(it.params,
                        "Found no overload of %s matching %s; Found %s", clazz.cName, tStr,
                        clazz.constructor.debugString(""));
                return;
            }
            
            addDirect("COPY %s r0", targetReg);
            addDirect("GOTO :%s", con.getQualifiedName(true));

            if (stackSize > 0) {
                addDirect("STACK DEC %d", stackSize);
            }
            addDirect("STACK POP r0");
            addDirect(endLbl);
            
        } else if (it.indexed()) { // array
            addReserve(targetReg);
            Expression eA = new Expression(scope, it.index.subTokens, targetReg);
            if(eA.isConstant()) {
                addDirect("LOAD %s %d", targetReg, classSize * eA.getConstant());
            } else {
                Register sr = newRegister();
                addReserve(sr);
                addDirect("LOAD %s %d\nMUL %s %s %s", sr, classSize, targetReg, targetReg, sr);
                addRelease(sr);
            }
            // needs to be eqivelant to:
            // malloc(T.sizeof() * len); -> malloc(r[sr])
            addDirect("STACK INC 4\nSTACK PUSH %s", targetReg);
            addDirect("GOTO PUSH :Memory.malloc_int32");
            addDirect("STACK INC -4\nSTACK POP %s", targetReg);
        } else {
            throw ELAnalysisError.error("Unknown new expression. Expected array size or constructor", it);
        }
    }

}
