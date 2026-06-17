package com.peter.emulator.peripherals;

import com.peter.emulator.Packer;
import com.peter.emulator.components.RAM;

public class ConsolePeripheral implements MemoryMappedPeripheral {

    public static final int DEVICE_TYPE = 0xff00_0001;
    public static final int[] MANUFACTURE = Packer.packChar("Virtual", 16);
    private static int nextSerial = 0;
    public final int[] serial = Packer.packChar((nextSerial++)+"", 16);

    private RAM ram;
    private int deviceId = -1;
    private int start;
    private int end;
    private int pntr;

    private final MessageQueue inputQueue = new MessageQueue();

    @Override
    public void update() {

    }
    
    @Override
    public void message(int[] msg) {
        if (msg[0] == 0x0000_0001) {
            start = msg[1];
            end = msg[2];
            pntr = start;
            
            ram.writeWord(PeripheralManager.PERIPHERAL_RSP_DATA, 0x1);
            ram.writeWord(PeripheralManager.PERIPHERAL_RSP_STATUS, 0x0100_0000 | deviceId);
        }
    }
    
    @Override
    public void tick() {
        int w = ram.readWord(pntr);
        if (w != 0) {
            System.out.print((char) w);
            ram.writeWord(pntr, 0);
            pntr += 4;
            if (pntr >= end) {
                pntr = start;
            }
        }
    }

    @Override
    public void link(RAM ram, int deviceID) {
        this.ram = ram;
        this.deviceId = deviceID;
        inputQueue.setup(ram);
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
