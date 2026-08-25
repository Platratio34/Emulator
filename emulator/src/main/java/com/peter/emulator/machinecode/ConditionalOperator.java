package com.peter.emulator.machinecode;

public enum ConditionalOperator {
    UNCONDITIONAL(0x0),
    EQ_ZERO(0x1),
    LEQ_ZERO(0x2),
    GT_ZERO(0x3),
    NEQ_ZERO(0x4),
    LT_ZERO(0x5),
    GEQ_ZERO(0x6),
            
    UNUSED_7(0x7),
    UNUSED_8(0x8),
    UNUSED_9(0x9),
    UNUSED_A(0xa),
    UNUSED_B(0xb),
    UNUSED_C(0xc),
    UNUSED_D(0xd),
    UNUSED_E(0xe),
    UNUSED_F(0xf),
    ;
    
    public final int id;

    private ConditionalOperator(int id) {
        this.id = id << 16;
        addValue();
    }

    private static ConditionalOperator[] byId;

    private void addValue() {
        if (byId == null) {
            byId = new ConditionalOperator[16];
        }
        byId[id >> 16] = this;
    }

    public static ConditionalOperator fromBytecode(int bytecode) {
        return byId[(bytecode >> 16) & 0xf];
    }
}
