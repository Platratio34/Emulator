package com.peter.emulator.peripherals;

import com.peter.emulator.components.RAM;

public interface MemoryMappedPeripheral extends Peripheral {

    public void update();

    public void message(int[] msg);

    public void link(RAM ram, int deviceID);

    public int[] getDescriptor();

    public int getType();
}
