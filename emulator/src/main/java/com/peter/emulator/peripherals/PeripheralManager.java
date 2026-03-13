package com.peter.emulator.peripherals;

import java.util.HashMap;

import com.peter.emulator.components.RAM;

public class PeripheralManager {

    private final RAM ram;
    private final HashMap<Byte, Peripheral> peripherals = new HashMap<>();
    private byte nextId = 1;

    public static final int PERIPHERAL_START = 0x8000;
    public static final int PERIPHERAL_CMD_STATUS = 0x8001;
    public static final int PERIPHERAL_CMD_DEVICE_ID = 0x8002;
    public static final int PERIPHERAL_CMD_SIZE = 0x8004;
    public static final int PERIPHERAL_CMD_MSG = 0x8008;
    
    public static final int PERIPHERAL_RSP_STATUS = 0x8080;
    public static final int PERIPHERAL_RSP_DEVICE_ID = 0x8081;
    public static final int PERIPHERAL_RSP_DATA = 0x8084;

    public PeripheralManager(RAM ram) {
        this.ram = ram;
    }

    public void tick() {
        ram.writeByte(PERIPHERAL_START, (byte)0x01);
        if (ram.readByte(PERIPHERAL_CMD_STATUS) == 0x01) {
            try {
                byte d = ram.readByte(PERIPHERAL_CMD_DEVICE_ID);
                if (d == 0) {
                    onMessage();
                } else {
                    int size = ram.readWord(PERIPHERAL_CMD_SIZE);
                    int[] msg = ram.readWords(PERIPHERAL_CMD_MSG, size);
                    if (peripherals.containsKey(d) && peripherals.get(d) instanceof MemoryMappedPeripheral mmp) {
                        mmp.message(msg);
                    } else {
                        ram.writeByte(PERIPHERAL_RSP_DEVICE_ID, d);
                        ram.writeByte(PERIPHERAL_RSP_DATA, (byte)0x1);
                        ram.writeByte(PERIPHERAL_RSP_STATUS, (byte)0x0f);
                    }
                }
            } catch (Exception e) {
                System.err.println(ram.debugPrint(0x8000, 16));
                throw e;
            }
            ram.writeWord(PERIPHERAL_CMD_STATUS, 0x2);
        }
        for (Peripheral peripheral : peripherals.values()) {
            peripheral.tick();
        }
    }

    public byte addPeripheral(Peripheral peripheral) {
        byte deviceId = nextId++;
        peripherals.put(deviceId, peripheral);
        if (peripheral instanceof MemoryMappedPeripheral mmp) {
            mmp.link(ram, deviceId);
        }
        return deviceId;
    }

    private void onMessage() {
        int size = ram.readWord(PERIPHERAL_CMD_SIZE);
        int[] msg = ram.readWords(PERIPHERAL_CMD_MSG, size);
        switch (msg[0]) {
            case 0x0 -> {
            }
            case 0x1 -> { // LIST START_ID
                byte start = (byte)msg[1];
                int arrI = 1;
                int[] out = new int[126];
                int numDevices = 0;
                for (byte i = start; i < nextId; i++) {
                    if (peripherals.containsKey(i) && peripherals.get(i) instanceof MemoryMappedPeripheral mmp) {
                        out[arrI++] = i;
                        out[arrI++] = mmp.getType();
                        numDevices++;
                        if (arrI >= 124) {
                            out[arrI++] = (i != nextId - 1) ? 0x1 : 0x0;
                            break;
                        }
                    }
                }
                out[0] = numDevices;
                ram.writeByte(PERIPHERAL_RSP_DEVICE_ID, (byte)0x00);
                ram.copyWords(out, PERIPHERAL_RSP_DATA, arrI);
                ram.writeByte(PERIPHERAL_RSP_STATUS, (byte)0x1);
            }
        }
    }
}
