package com.peter.emulator.machinecode;

public enum MemorySize {
    /** <code>0b00</code> */
    WORD(0b00, 4),
    /** <code>0b01</code> */
    SHORT(0b01, 2),
    /** <code>0b10</code> */
    BYTE(0b10, 1)
    ;

    public final int id;
    public final int size;

    private MemorySize(int id, int size) {
        this.id = id;
        this.size = size;
    }

    public static MemorySize fromBytecode(int bytecode) {
        return switch (bytecode & 0b11) {
            case 0b00 -> WORD;
            case 0b01 -> SHORT;
            case 0b10 -> BYTE;
            default -> WORD;
        };
    }
}
