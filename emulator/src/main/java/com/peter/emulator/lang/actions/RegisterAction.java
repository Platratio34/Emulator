package com.peter.emulator.lang.actions;

import com.peter.emulator.machinecode.Reg;

public class RegisterAction extends Action {

    public Register register;
    public boolean release;
    public boolean findOnly = false;

    public RegisterAction(ActionScope scope, Register register, boolean release) {
        super(scope);
        this.register = register;
        this.release = release;
    }
    public RegisterAction(ActionScope scope, Register register, boolean release, boolean findOnly) {
        super(scope);
        this.register = register;
        this.release = release;
        this.findOnly = findOnly;
    }

    @Override
    public String toAssembly() {
        if (register == null) {
            return "";
        }
        if (release) {
            register.release();
            if (register.alias != null)
                return "#alias clear " + register.alias + " // Releasing " + Reg.from(register.reg);
            return "// Releasing " + register;
        } else {
            if (!register.fistFree()) {
                return "// !!Out of registers!!";
            }
            if (findOnly)
                return "// Found Free register " + register;
            if (register.reserved)
                return "// Register " + register + " already reserved";
            register.reserve();
            if (register.alias != null)
                return "#alias " + Reg.from(register.reg) + " " + register.alias + " // Reserving " + Reg.from(register.reg);
            return "// Reserving " + register;
        }
        // return "";
    }

}
