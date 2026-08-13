package com.peter.emulator.lang.actions;

public class RegisterAction extends Action {

    public Register register;
    public boolean release;

    public RegisterAction(ActionScope scope, Register register, boolean release) {
        super(scope);
        this.register = register;
        this.release = release;
    }

    @Override
    public String toAssembly() {
        if(release) {
            register.release();
            return "// Releasing "+register+" ("+register.reg+")";
        } else {
            if(!register.fistFree()) {
                return "// !!Out of registers!!";
            }
            register.reserve();
            return "// Reserving "+register+" ("+register.reg+")";
        }
        // return "";
    }

}
