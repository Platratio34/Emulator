package com.peter.emulator.lang;

import com.peter.emulator.lang.actions.Register;

public class PseudoVariable extends ELVariable {

    public final Register register;

    public PseudoVariable(ELType type, String name, Namespace namespace, ProgramUnit unit, Location location,
            Register register) {
        super(ELProtectionLevel.PUBLIC, Type.STATIC, type, name, false, namespace, unit, location);
        this.register = register;
    }
    public PseudoVariable(Type varType, ELType type, String name, boolean _final, Namespace namespace, ProgramUnit unit, Location location, Register register) {
        super(ELProtectionLevel.PUBLIC, varType, type, name, _final, namespace, unit, location);
        this.register = register;
    }
}
