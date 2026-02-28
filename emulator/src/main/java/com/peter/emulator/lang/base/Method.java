package com.peter.emulator.lang.base;

import com.peter.emulator.lang.ELClass;
import com.peter.emulator.lang.ELFunction;
import com.peter.emulator.lang.ELProtectionLevel;
import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.Namespace;
import com.peter.emulator.lang.ProgramUnit;
import com.peter.emulator.lang.ELFunction.FunctionType;

public class Method extends ELClass {

    public Method(String name, Namespace namespace, ProgramUnit unit, String... params) {
        super(name, namespace, unit);
        for (String p : params) {
            genericsOrder.add(p);
            generics.put(p, ELPrimitives.OBJECT);
        }

        memberFunctions.put("call", new ELFunction(ELProtectionLevel.PUBLIC, false, this, "call", FunctionType.INSTANCE, false, unit, ELPrimitives.INTERNAL_LOCATION));
    }

    @Override
    public boolean canStaticCast(ELType target) {
        return target.equals(ELPrimitives.VOID_PTR);
    }

    @Override
    public int getSize() {
        return 4;
    };

}
