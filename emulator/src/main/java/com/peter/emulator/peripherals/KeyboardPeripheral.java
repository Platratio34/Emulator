package com.peter.emulator.peripherals;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import com.peter.emulator.components.MemoryException;

public class KeyboardPeripheral implements MemoryMappedPeripheral, KeyListener {

    public final int address;
    protected final PeripheralManager manager;

    protected byte modifiers = 0;
    protected final byte[] keys = new byte[6];
    protected boolean interruptsEnabled = false;
    protected boolean pressInterruptEnabled = false;
    protected boolean releaseInterruptEnabled = false;
    protected boolean modifierInterruptEnabled = false;

    private final int[] addresses;

    public KeyboardPeripheral(int address, PeripheralManager manager) {
        this.address = address;
        this.manager = manager;
        addresses = new int[keys.length + 2];
        for (int i = 0; i < addresses.length; i++) {
            addresses[i] = address + i;
        }
    }

    public void pressKey(byte key) {
        synchronized (this) {
            if (interruptsEnabled && pressInterruptEnabled) {
                manager.cpu.interrupt(0x8000_0100 | key);
            }
            for (int i = 0; i < keys.length; i++) {
                if (keys[i] == key) {
                    return;
                }
                if (keys[i] == 0) {
                    keys[i] = key;
                    return;
                }
            }
            for (int i = 1; i < keys.length; i++) {
                keys[i - 1] = keys[i];
            }
            keys[keys.length] = key;
        }
    }

    public void releaseKey(byte key) {
        synchronized(this) {
            if (interruptsEnabled && releaseInterruptEnabled) {
                manager.cpu.interrupt(0x8000_0200 | key);
            }
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
        if(interruptsEnabled)
            manager.cpu.interrupt(0x8000_0120);
        
        if (interruptsEnabled && releaseInterruptEnabled) {
            manager.cpu.interrupt(0x8000_0300);
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
        if (address - this.address == 1) {
            interruptsEnabled = (value & 0b0001) != 0;
            pressInterruptEnabled = (value & 0b0010) != 0;
            releaseInterruptEnabled = (value & 0b0100) != 0;
            modifierInterruptEnabled = (value & 0b1000) != 0;
            return;
        }
        throw MemoryException.Write(address);
    }

    @Override
    public byte get(int address) {
        synchronized (this) {
            address -= this.address;
            if (address == 0) {
                return modifiers;
            } else if (address == 1) {
                return (byte)(interruptsEnabled ? 1 : 0);
            }
            return keys[address - 2];
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

    @Override
    public void keyTyped(KeyEvent e) {

    }
    
    private void updateModifiers(int modifiersEx) {
        int t = modifiers;
        modifiers = 0;
        modifiers |= ((modifiersEx & KeyEvent.SHIFT_DOWN_MASK) != 0) ? Modifier.SHIFT.code : 0;
        modifiers |= ((modifiersEx & KeyEvent.CTRL_DOWN_MASK) != 0) ? Modifier.CONTROL.code : 0;
        modifiers |= ((modifiersEx & KeyEvent.ALT_DOWN_MASK) != 0) ? Modifier.ALT.code : 0;
        modifiers |= ((modifiersEx & KeyEvent.META_DOWN_MASK) != 0) ? Modifier.CMD.code : 0;
        if (t != modifiers) {
            if (interruptsEnabled && modifierInterruptEnabled) {
                manager.cpu.interrupt(0x8000_0300 | modifiers);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        synchronized (this) {
            updateModifiers(e.getModifiersEx());
            pressKey((byte) e.getKeyCode());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        synchronized (this) {
            updateModifiers(e.getModifiersEx());
            releaseKey((byte)e.getKeyCode());
        }
    }
}
