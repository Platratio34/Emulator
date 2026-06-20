package com.peter.emulator.peripherals;

import com.peter.emulator.Packer;

public class ConsolePeripheral implements MemoryMappedPeripheral {

    public static final int DEVICE_TYPE = 0xff00_0001;
    public static final int[] MANUFACTURE = Packer.packChar("Virtual", 16);
    private static int nextSerial = 0;
    public final int[] serial = Packer.packChar((nextSerial++)+"", 16);

    protected final int startAddress;
    protected final int inAddress;
    protected final int inCountAddress;

    protected final int inBufferSize = 128;
    protected final char[] inBuffer = new char[inBufferSize];
    protected int inWritePointer = 0;
    protected int inReadPointer = 0;
    protected int bufferCount = 0;

    public ConsolePeripheral(int startAddress) {
        this.startAddress = startAddress;
        inAddress = startAddress + 1;
        inCountAddress = startAddress + 2;
    }

    @Override
    public int[] getAddresses() {
        return new int[] { startAddress, inAddress, inCountAddress };
    }

    private byte lastChar;
    @Override
    public void onUpdate(int address, byte value) {
        if(address == startAddress) {
            System.out.print((char) value);
            lastChar = value;
        }
    }

    @Override
    public byte get(int address) {
        if (address == inAddress) {
            if(bufferCount == 0)
                return 0;
            char c = inBuffer[inReadPointer];
            inReadPointer = (inReadPointer + 1) % inBufferSize;
            bufferCount--;
            return (byte)c;
        } else if (address == inCountAddress) {
            return (byte) bufferCount;
        } else if (address == startAddress) {
            return lastChar;
        }
        return 0;
    }

    @Override
    public void tick() {

    }
    
    public boolean write(char c) {
        if (bufferCount >= inBufferSize)
            return false;
        inBuffer[inWritePointer] = c;
        inWritePointer = (inWritePointer + 1) % inBufferSize;
        bufferCount++;
        return true;
    }

}
