package com.peter.emulator.peripherals;

import com.peter.emulator.CPU;
import com.peter.emulator.Packer;
import com.peter.emulator.components.RAM;
import com.peter.emulator.gui.CharacterDisplayFrame;

public class CharacterDisplay implements DMAPeripheral {

    public static final int DEVICE_TYPE = 0x0100_0011;
    public static final int[] MANUFACTURE = Packer.packChar("Virtual", 16);
    private static int nextSerial = 0;
    public final int[] serial = Packer.packChar((nextSerial++) + "", 16);

    private RAM ram;
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
        if(charBufferStart == 0) {
            out = "";
            return;
        }
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                char c = (char)ram.readByte(charBufferStart + x + (y*width));
                if(Character.isISOControl(c)) {
                    out += " ";
                } else {
                    out += c;
                }
            }
            out += "\n";
        }
        if(frame != null) {
            frame.updateDisplay();
        }
    }

    public String getOut() {
        return out;
    }

    @Override
    public void update() {
        
    }

    @Override
    public void message(int[] msg) {
        if(msg[0] == 0x0000_0001) {
            int addr = msg[1];
            if(addr == 0) {
                charBufferStart = 0;
            } else {
                charBufferStart = cpu.translateAddress(msg[1]);
            }
        } else if(msg[0] == 0x0000_0002) {
            int addr = msg[1];
            if(addr == 0) {
                colorBufferStart = 0;
            } else {
                colorBufferStart = cpu.translateAddress(msg[1]);
            }
        }

    }

    @Override
    public void link(RAM ram, CPU cpu, int deviceId) {
        this.ram = ram;
        this.cpu = cpu;
        this.deviceId = deviceId;
    }

    @Override
    public int[] getDescriptor() {
        return new int[] {
            deviceId,
            DEVICE_TYPE,
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
        return DEVICE_TYPE;
    }

}
