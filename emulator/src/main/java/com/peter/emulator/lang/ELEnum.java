package com.peter.emulator.lang;

import java.util.HashMap;

import com.peter.emulator.lang.ELValue.ELNumberValue;
import com.peter.emulator.lang.actions.ActionScope;
import com.peter.emulator.lang.base.ELPrimitives;
import com.peter.emulator.lang.expresion.Expression;
import com.peter.emulator.lang.tokens.BlockToken;
import com.peter.emulator.lang.tokens.IdentifierToken;
import com.peter.emulator.lang.tokens.OperatorToken;
import com.peter.emulator.lang.tokens.Token;

public class ELEnum extends ELClass {

    protected int nextI = 0;
    public HashMap<String, Integer> values = new HashMap<>();

    public ELEnum(String name, Span nameSpan, Namespace namespace, ProgramUnit unit) {
        super(name, nameSpan, namespace, unit);
    }

    public void add(String name, Location location) {
        if (values.containsKey(name)) {
            throw ELAnalysisError.errorF("Duplicate name in enum: %s", name);
        }
        int i = nextI++;
        while (values.containsValue(i)) {
            i = nextI++;
        }
        if (i > 0xff) {
            throw ELAnalysisError.errorF("Index out of range in enum (On name '%s')", name);
        }
        values.put(name, i);
        ELVariable var = addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.CONST, getType(),
                name, true, this, unit, location));
        var.startingValue = ELNumberValue.number(getType(), i, location.span());
    }

    public void add(String name, int i, Location location) {
        if (values.containsKey(name)) {
            throw ELAnalysisError.errorF("Duplicate name in enum: %s", name);
        }
        if (values.containsValue(i)) {
            throw ELAnalysisError.errorF("Duplicate index in enum: %d (On name '%s')", i, name);
        }
        if (i > 0xff || i < 0) {
            throw ELAnalysisError.errorF("Index out of range in enum: %d (On name '%s')", i, name);
        }
        values.put(name, i);
        ELVariable var = addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.CONST, getType(),
                name, true, this, unit, location));
        var.startingValue = ELNumberValue.number(getType(), i, location.span());
    }

    public void add(IdentifierToken it) {
        if (values.containsKey(it.value)) {
            throw ELAnalysisError.errorF("Duplicate name in enum: %s", it.value);
        }
        int i;
        Span indexSpan;
        if (it.hasParams() && !it.params.subTokens.isEmpty()) {
            Expression exp = new Expression(new ActionScope(this, unit, null), it.params.subTokens);
            if (!exp.validate(unit.errors)) {
                return;
            }
            if (!exp.isConstant()) {
                throw ELAnalysisError.errorF("Enum index must be constant (On name '%s')", it.value);
            }
            i = exp.getConstant();
            indexSpan = it.params.span();
        } else {
            i = nextI++;
            while (values.containsValue(i)) {
                i = nextI++;
            }
            indexSpan = it.endLocation.span();
        }
        if (values.containsValue(i)) {
            throw ELAnalysisError.errorF("Duplicate index in enum: %d (On name '%s')", i, it.value);
        }
        if (i > 0xff || i < 0) {
            throw ELAnalysisError.errorF("Index out of range in enum: %d (On name '%s')", i, it.value);
        }
        values.put(it.value, i);
        ELVariable var = addStaticVariable(new ELVariable(ELProtectionLevel.PUBLIC, ELVariable.Type.CONST, getType(),
                it.value, true, this, unit, it.startLocation));
        var.startingValue = ELNumberValue.number(getType(), i, indexSpan);
    }

    @Override
    public String getClassType() {
        return "enum";
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public boolean canStaticCast(ELType target) {
        return target.canCastTo(ELPrimitives.UINT8);
    }

    @Override
    public ELType getType() {
        return new ELType(cName, this, nameSpan.start());
    }

    public void parse(BlockToken body) {
        if (body.subTokens.isEmpty()) {
            unit.errors.warning("Enum has no values", nameSpan);
            return;
        }
        int tI = 0;
        int numTokens = body.subTokens.size();
        while (tI < numTokens) {
            Token token = body.subTokens.get(tI++);
            if (token instanceof IdentifierToken it) {
                try {
                    add(it);
                    unit.addSymbol(new ELSymbol.ELVarSymbol(staticVariables.get(it.value), it.nameSpan()));
                } catch (ELAnalysisError err) {
                    unit.errors.add(err);
                }
                if (tI >= numTokens) {
                    unit.errors.warning("Unterminated enum, may cause problems in the future",
                            token.endLocation.span());
                }
                token = body.subTokens.get(tI++);
                if (token instanceof OperatorToken ot) {
                    if (ot.type == OperatorToken.Type.COMMA) {
                        continue;
                    } else if (ot.type == OperatorToken.Type.SEMICOLON) {
                        break;
                    }
                }
                unit.errors.errorF(token, "Unexpected token in enum, expected `,` or `;`");
            }
        }
    }
}
