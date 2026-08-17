package com.peter.emulator.components;

import com.peter.emulator.machinecode.Instruction;

public class MemoryException extends RuntimeException {
    public final int address;
    public final boolean write;
    public final boolean read;

    public MemoryException(int address) {
        this.address = address;
        write = false;
        read = false;
    }
    protected MemoryException(int address, boolean write, boolean read) {
        this.address = address;
        this.write = write;
        this.read = read;
    }

    public static MemoryException Write(int address) {
        return new MemoryException(address, true, false);
    }
    public static MemoryException Read(int address) {
        return new MemoryException(address, false, true);
    }

    @Override
    public String toString() {
        if (write) {
            return String.format("Emulator Memory Exception: Invalid write address 0x%s", Instruction.toHex(address));
        } else if (read) {
            return String.format("Emulator Memory Exception: Invalid read address 0x%s", Instruction.toHex(address));
        }
        return String.format("Emulator Memory Exception: 0x%s", Instruction.toHex(address));
    }
}