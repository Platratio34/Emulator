package com.peter.emulator.lang;

import com.peter.emulator.machinecode.Reg;

public class PseudoVariable extends ELVariable {

    public final Reg register;

    public PseudoVariable(ELType type, String name, Namespace namespace, ProgramUnit unit, Location location,
            Reg register) {
        super(ELProtectionLevel.PUBLIC, Type.STATIC, type, name, false, namespace, unit, location);
        this.register = register;
    }
    public PseudoVariable(Type varType, ELType type, String name, boolean _final, Namespace namespace, ProgramUnit unit, Location location, Reg register) {
        super(ELProtectionLevel.PUBLIC, varType, type, name, _final, namespace, unit, location);
        this.register = register;
    }
}
