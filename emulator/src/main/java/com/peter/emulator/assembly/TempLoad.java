package com.peter.emulator.assembly;

import com.peter.emulator.machinecode.Load;
import com.peter.emulator.machinecode.Reg;

public class TempLoad extends Load implements ResolvableValue {

    public final Define valueDefine;

    public TempLoad(Reg rg, Define valueDefine) {
        super(rg, valueDefine.value);
        this.valueDefine = valueDefine;
        valueDefine.addListener(this);
    }

    @Override
    public void onResolve(int value) {
        data = value;
    }
}
