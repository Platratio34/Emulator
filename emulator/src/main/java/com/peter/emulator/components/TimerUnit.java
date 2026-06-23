package com.peter.emulator.components;

import com.peter.emulator.CPU;
import com.peter.emulator.components.MMU.MemoryException;
import com.peter.emulator.peripherals.MemoryMappedPeripheral;

public class TimerUnit implements MemoryMappedPeripheral {

    public final int startAddress;
    public final CPU cpu;
    protected final int[] addresses;

    protected int time;

    protected int[] timers = new int[15];

    public TimerUnit(int startAddress, CPU cpu) {
        this.startAddress = startAddress;
        this.cpu = cpu;
        addresses = new int[(1 + timers.length)*4];
        for (int i = 0; i < addresses.length; i++) {
            addresses[i] = startAddress + i;
        }
    }

    @Override
    public void tick() {
        time++;
        for (int i = 0; i < timers.length; i++) {
            if (timers[i] == 0xffff_ffff || timers[i] <= 0) {
                continue;
            }
            timers[i]--;
            if (timers[i] == 0) {
                timers[i] = 0xffff_ffff;
                cpu.interrupt(0x01);
            }
        }
    }

    @Override
    public int[] getAddresses() {
        return addresses;

    }

    @Override
    public void onUpdate(int address, byte value) {
        address -= startAddress;
        if (address < 4) { // writing to time field, not allowed
            System.err.println("write to timer unit time");
            return;
        }
        int timerIndex = (address / 4) - 1;
        if (timerIndex >= timers.length || timerIndex < 0) { // invalid timer index
            System.err.println("write to timer unit invalid timer");
            return;
        }
        timers[timerIndex] = switch (address % 4) {
            case 0 -> (timers[timerIndex] & 0x00ff_ffff) | ((value & 0xff) << 24);
            case 1 -> (timers[timerIndex] & 0xff00_ffff) | ((value & 0xff) << 16);
            case 2 -> (timers[timerIndex] & 0xffff_00ff) | ((value & 0xff) << 8);
            case 3 -> (timers[timerIndex] & 0xffff_ff00) | (value & 0xff);
            default -> timers[timerIndex];
        };
    }

    @Override
    public byte get(int address) {
        address -= startAddress;
        if (address < 4) { // reading from time field
            return getByte(address, time);
        }
        int timerIndex = (address / 4) - 1;
        if (timerIndex >= timers.length || timerIndex < 0) { // invalid timer index
            System.err.println("read from timer unit invalid timer");
            return 0x0;
        }
        return getByte(address % 4, timers[timerIndex]);
    }

    private byte getByte(int offset, int value) {
        return switch (offset) {
            case 0 -> (byte) ((value >> 24) & 0xff);
            case 1 -> (byte) ((value >> 16) & 0xff);
            case 2 -> (byte) ((value >> 8) & 0xff);
            case 3 -> (byte) (value & 0xff);
            default -> 0x0;
        };
    }

}
