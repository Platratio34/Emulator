package com.peter.emulator.peripherals;

import com.peter.emulator.CPU;

public interface DMAPeripheral extends Peripheral {

    public void update();

    public void message(int[] msg);

    public void link(PeripheralManager manager, CPU cpu, int deviceID);

    public int[] getDescriptor();

    public int getType();
}
