package com.peter.emulator.peripherals;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.peter.emulator.components.MemoryException;

public class KeyboardPeripheral implements MemoryMappedPeripheral {

    public final int address;

    protected byte modifiers = 0;

    protected Queue<Byte> charQueue = new ConcurrentLinkedQueue<>();

    public KeyboardPeripheral(int address) {
        this.address = address;
    }

    public void pressKey(byte key) {
        charQueue.add((byte) (key & 0x7f));
    }

    public void releaseKey(byte key) {
        charQueue.add((byte) (key | 0x80));
    }
    
    public void setModifier(Modifier modifier, boolean state) {
        if (state) {
            modifiers |= modifier.code;
        } else {
            modifiers &= ~modifier.code;
        }
    }

    @Override
    public void tick() {
        
    }

    @Override
    public int[] getAddresses() {
        return new int[] {
            address,
            address + 1
        };
    }

    @Override
    public void onUpdate(int address, byte value) {
        throw MemoryException.Write(address);
    }

    @Override
    public byte get(int address) {
        address -= this.address;
        if (address == 0) {
            Byte b = charQueue.poll();
            return (b != null) ? b : 0;
        } else if (address == 1) {
            return modifiers;
        }
        return 0;
    }

    public static enum Modifier {
        SHIFT(0b1),
        CONTROL(0b10),
        ALT(0b100)        
        ;

        public final byte code;

        private Modifier(int code) {
            this.code = (byte)code;
        }
    }
}
