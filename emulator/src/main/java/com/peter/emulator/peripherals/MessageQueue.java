package com.peter.emulator.peripherals;

import com.peter.emulator.components.RAM;

public class MessageQueue {

    private int start = -1;
    private int end = -1;
    @SuppressWarnings("unused")
    private int size = -1;
    private int pointer = -1;
    private RAM ram;

    public boolean hasMsg() {
        if (start == -1 || ram == null)
            return false;
        return ram.readWord(pointer + 4) == 0x1;
    }

    public int read() {
        int v = ram.readWord(pointer);
        ram.writeWord(pointer + 4, 0x2);
        pointer += 8;
        if (pointer >= end) {
            pointer = start;
        }
        return v;
    }

    public void setup(RAM ram, int start, int size) {
        this.ram = ram;
        this.start = start;
        this.size = size;
        this.pointer = start;
        this.end = start + (size * 8);
    }

    public void setup(int start, int size) {
        this.start = start;
        this.size = size;
        this.pointer = start;
        this.end = start + (size * 8);
    }

    public void setup(RAM ram) {
        this.ram = ram;
    }
}
