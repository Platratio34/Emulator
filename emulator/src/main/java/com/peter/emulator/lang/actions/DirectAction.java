package com.peter.emulator.lang.actions;

public class DirectAction extends Action {

    public int reg;
    public String asm;
    public Object[] args = null;

    public DirectAction(String asm) {
        super(null);
        this.asm = asm;
    }

    public DirectAction(String asm, Object... args) {
        super(null);
        this.asm = asm;
        this.args = args;
        this.asm = String.format(asm, args);
    }

    @Override
    public String toAssembly() {
        return args != null ? String.format(asm, args) : asm;
    }

}
