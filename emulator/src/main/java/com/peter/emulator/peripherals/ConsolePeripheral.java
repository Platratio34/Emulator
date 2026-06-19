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

    protected final int startAddress;

    public ConsolePeripheral(int startAddress) {
        this.startAddress = startAddress;
    }

    @Override
    public int[] getAddresses() {
        return new int[] {startAddress};
    }

    @Override
    public void onUpdate(int address, byte value) {
        if(address == startAddress) {
            System.out.print((char) value);
        }
    }

    @Override
    public byte get(int address) {
        return 0;
    }

    @Override
    public void tick() {
        
    }

}
