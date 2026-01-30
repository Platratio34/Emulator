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

    private final MessageQueue inputQueue = new MessageQueue();

    @Override
    public void update() {

    }
    
    @Override
    public void message(int[] msg) {
        if (msg[0] == 0x0000_0001) {
            inputQueue.setup(msg[1], msg[2]);
            ram.write(0x8081, deviceId);
            ram.write(0x8082, 0x1);
            ram.write(0x8080, 0x1);
        }
    }
    
    @Override
    public void tick() {
        if (!inputQueue.hasMsg())
            return;
        int i2 = inputQueue.read();
        System.out.print((char) i2);
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
