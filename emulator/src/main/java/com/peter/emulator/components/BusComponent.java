package com.peter.emulator.components;

public interface BusComponent {

    public int getStart();

    public int getNumBlocks();

    public byte readByte(int address);

    public void writeByte(int address, byte value);
}
