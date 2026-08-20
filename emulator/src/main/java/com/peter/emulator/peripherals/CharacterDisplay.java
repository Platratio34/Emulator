package com.peter.emulator.peripherals;

import com.peter.emulator.CPU;
import com.peter.emulator.Packer;
import com.peter.emulator.components.ComponentBus;
import com.peter.emulator.gui.CharacterDisplayFrame;
import com.peter.emulator.lang.base.Peripheral;

public class CharacterDisplay implements DMAPeripheral {

    public static final int[] MANUFACTURE = Packer.packChar("Virtual", 16);
    private static int nextSerial = 0;
    public final int[] serial = Packer.packChar((nextSerial++) + "", 16);

    private PeripheralManager manager;
    private CPU cpu;
    private int deviceId;

    public final int width;
    public final int height;

    protected int charBufferStart = 0;
    protected int colorBufferStart = 0;

    private String out = "";
    public CharacterDisplayFrame frame;

    public CharacterDisplay(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void tick() {
        synchronized (out) {
            if (charBufferStart == 0) {
                out = "[NO BUFFER]";
                return;
            }
            out = "";
            String delim = "";
            for (int i = 0; i < width; i++) {
                delim += "-";
            }
            out += delim + "\n";
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    char c = (char) manager.componentBus.readByte(charBufferStart + x + (y * width));
                    if (c == 0) {
                        out += " ";
                    } else if (Character.isISOControl(c)) {
                        out += "@";
                    } else {
                        out += c;
                    }
                }
                out += "\n";
            }
            out += delim;
        }
    }

    public String getOut() {
        synchronized (out) {
            return out;
        }
    }

    @Override
    public void update() {
        
    }

    @Override
    public void message(int[] msg) {
        System.out.println(msg[0] + " "+msg[1]);
        if(msg[0] == 0x0000_0001) {
            int addr = msg[1];
            if(addr == 0) {
                charBufferStart = 0;
            } else {
                charBufferStart = cpu.translateAddress(msg[1]);
            }
            manager.writeRspWords(0x01, deviceId, 0x01);
        } else if(msg[0] == 0x0000_0002) {
            int addr = msg[1];
            if(addr == 0) {
                colorBufferStart = 0;
            } else {
                colorBufferStart = cpu.translateAddress(msg[1]);
            }
            manager.writeRspWords(0x01, deviceId, 0x01);
        } else {
            manager.writeRspWords(0x01, deviceId, 0x0f);
        }

    }

    @Override
    public void link(PeripheralManager manager, CPU cpu, int deviceId) {
        this.manager = manager;
        this.cpu = cpu;
        this.deviceId = deviceId;
    }

    @Override
    public int[] getDescriptor() {
        return new int[] {
            deviceId,
            Peripheral.TYPE_DISPLAY_CHARACTER,
            MANUFACTURE[0],
            MANUFACTURE[1],
            MANUFACTURE[2],
            MANUFACTURE[3],
            serial[0],
            serial[1],
            serial[2],
            serial[3],
            width,
            height,
            0x00,
            0x00,
            0x00,
            0x00    
        };
    }

    @Override
    public int getType() {
        return Peripheral.TYPE_DISPLAY_CHARACTER;
    }

}
