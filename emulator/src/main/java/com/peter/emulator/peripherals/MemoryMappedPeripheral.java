package com.peter.emulator.peripherals;

public interface MemoryMappedPeripheral extends Peripheral {

    public int[] getAddresses();

    public void onUpdate(int address, byte value);

    public byte get(int address);
}
