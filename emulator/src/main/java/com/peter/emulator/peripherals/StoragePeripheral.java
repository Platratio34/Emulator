package com.peter.emulator.peripherals;

import com.peter.emulator.Packer;
import com.peter.emulator.components.RAM;

public class StoragePeripheral implements MemoryMappedPeripheral {

    public static final int DEVICE_TYPE = 0x0100_0001;
    public static final int[] MANUFACTURE = Packer.packChar("Virtual", 16);
    private static int nextSerial = 0;
    public final int[] serial = Packer.packChar((nextSerial++)+"", 16);

    @Override
    public void update() {
        
    }

    @Override
    public void message(int[] msg) {
        
    }

    @Override
    public void tick() {

    }

    @Override
    public void link(RAM ram, int deviceID) {
        
    }

    @Override
    public int[] getDescriptor() {
        return new int[] {
            0x00,
            DEVICE_TYPE,
            MANUFACTURE[0],
            MANUFACTURE[1],
            MANUFACTURE[2],
            MANUFACTURE[3],
            serial[0],
            serial[1],
            serial[2],
            serial[3],
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00    
        };
    }

    @Override
    public int getType() {
        return DEVICE_TYPE;
    }

}
