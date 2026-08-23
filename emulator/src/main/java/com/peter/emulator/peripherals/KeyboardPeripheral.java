package com.peter.emulator.peripherals;

import com.peter.emulator.components.MemoryException;

public class KeyboardPeripheral implements MemoryMappedPeripheral {

    public final int address;

    protected byte modifiers = 0;
    protected final byte[] keys = new byte[7];

    private final int[] addresses;

    public KeyboardPeripheral(int address) {
        this.address = address;
        addresses = new int[keys.length + 1];
        for (int i = 0; i < addresses.length; i++) {
            addresses[i] = address + i;
        }
    }

    public void pressKey(byte key) {
        synchronized (this) {
            for (int i = 0; i < keys.length; i++) {
                if (keys[i] == key) {
                    return;
                }
                if (keys[i] == 0) {
                    keys[i] = key;
                    return;
                }
            }
        }
        for (int i = 1; i < keys.length; i++) {
            keys[i - 1] = keys[i];
        }
        keys[keys.length] = key;
    }

    public void releaseKey(byte key) {
        synchronized(this) {
            boolean after = false;
            for (int i = 0; i < keys.length; i++) {
                if (keys[i] == key) {
                    after = true;
                }
                if (after) {
                    if (i == keys.length - 1) {
                        keys[i] = 0x00;
                    } else {
                        keys[i] = keys[i + 1];
                    }
                }
            }
        }
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
        return addresses;
    }

    @Override
    public void onUpdate(int address, byte value) {
        throw MemoryException.Write(address);
    }

    @Override
    public byte get(int address) {
        synchronized (this) {
            address -= this.address;
            if (address == 0) {
                return modifiers;
            }
            return keys[address - 1];
        }
    }

    public static enum Modifier {
        SHIFT(0b0001),
        CONTROL(0b0010),
        ALT(0b0100),
        CMD(0b1000)
        ;

        public final byte code;

        private Modifier(int code) {
            this.code = (byte)code;
        }
    }
}
