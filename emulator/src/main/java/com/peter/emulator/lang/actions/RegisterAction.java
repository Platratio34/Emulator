package com.peter.emulator.lang.actions;

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
        if(release) {
            register.release();
            return "// Releasing "+register;
        } else {
            if(!register.fistFree()) {
                return "// !!Out of registers!!";
            }
            if(findOnly)
                return "// Found Free register "+register;
            register.reserve();
            return "// Reserving "+register;
        }
        // return "";
    }

}
