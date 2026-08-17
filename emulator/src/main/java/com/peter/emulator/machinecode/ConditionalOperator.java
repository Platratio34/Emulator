package com.peter.emulator.machinecode;

import java.util.HashMap;

public enum ConditionalOperator {

    UNCONDITIONAL(0x0),
    EQ_ZERO(0x1 << 16),
    LEQ_ZERO(0x2 << 16),
    GT_ZERO(0x3 << 16),
    NEQ_ZERO(0x4 << 16),
    LT_ZERO(0x5 << 16),
    GEQ_ZERO(0x6 << 16),
    UNKNOWN(0xf << 16);
    
    public final int id;
    private static HashMap<Integer, ConditionalOperator> byId;

    private ConditionalOperator(int id) {
        this.id = id;
        addValue();
    }

    private void addValue() {
        if (byId == null)
            byId = new HashMap<>();
        byId.put(id, this);
    }

    public static ConditionalOperator fromBytecode(int bytecode) {
        return byId.getOrDefault(bytecode &= 0x000f_0000, UNKNOWN);
    }
}
