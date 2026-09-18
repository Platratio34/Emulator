package com.peter.emulator.assembly;

import com.peter.emulator.machinecode.MemorySize;
import com.peter.emulator.machinecode.Reg;
import com.peter.emulator.machinecode.StoreInstruction;

public class TempStore extends StoreInstruction implements ResolvableValue {

    public final Define valueDefine;

    public TempStore(MemorySize size, Define valueDefine, Reg ra) {
        super(size, valueDefine.value, ra);
        this.valueDefine = valueDefine;
        valueDefine.addListener(this);
    }

    public TempStore(MemorySize size, Reg rg, Define addressDefine) {
        super(size, rg, addressDefine.value);
        this.valueDefine = addressDefine;
        valueDefine.addListener(this);
    }

    @Override
    public void onResolve(int value) {
        data = value;
    }
}
