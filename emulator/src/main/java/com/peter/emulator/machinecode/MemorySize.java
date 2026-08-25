package com.peter.emulator.machinecode;

public enum MemorySize {
    WORD(0b00),
    SHORT(0b01),
    BYTE(0b10)
    ;

    public final int id;

    private MemorySize(int id) {
        this.id = id;
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
