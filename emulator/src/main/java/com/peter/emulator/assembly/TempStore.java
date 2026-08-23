package com.peter.emulator.assembly;

import com.peter.emulator.machinecode.Reg;
import com.peter.emulator.machinecode.Store;

public class TempStore extends Store implements ResolvableValue {

    public final Define valueDefine;

    public TempStore(Size size, Define valueDefine, Reg ra) {
        super(size, valueDefine.value, ra);
        this.valueDefine = valueDefine;
        valueDefine.addListener(this);
    }

    @Override
    public void onResolve(int value) {
        data = value;
    }
}
