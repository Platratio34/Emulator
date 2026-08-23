package com.peter.emulator.assembly;

import com.peter.emulator.machinecode.ConditionalOperator;
import com.peter.emulator.machinecode.Goto;
import com.peter.emulator.machinecode.Reg;

public class TempGoto extends Goto implements ResolvableValue {

    public final Define target;
    protected int address = 0;

	protected TempGoto(ConditionalOperator condition, Mode mode, Reg rg, Define target) {
        super(condition, mode, 0, rg);
        this.target = target;
        target.addListener(this);
	}

    @Override
    public void onResolve(int value) {
        System.out.println("Resolving goto from "+toHexLead(address) + " to " + target.name + " @"+toHexLead(value));
        data = value - address;
    }
    
    public void setAddress(int address) {
        this.address = address;
        if (target.resolved) {
            data = target.value - address;
        }
    }

    public int getAddress() {
        return address;
    }

}
