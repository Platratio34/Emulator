package com.peter.emulator.assembly;

import com.peter.emulator.machinecode.Reg;
import com.peter.emulator.machinecode.TestAndSet;

public class TempTestAndSet extends TestAndSet implements ResolvableValue {

    public final Define valueDefine;

    public TempTestAndSet(Reg rg, Define addressDefine) {
        super(rg, addressDefine.value);
        this.valueDefine = addressDefine;
        addressDefine.addListener(this);
    }

    @Override
    public void onResolve(int value) {
        data = value;
    }
}
