package com.peter.emulator.peripherals;

import java.util.HashMap;

import com.peter.emulator.components.RAM;

public class PeripheralManager {

    private final RAM ram;
    private final HashMap<Integer, Peripheral> peripherals = new HashMap<>();
    private int nextId = 1;

    public static final int PERIPHERAL_START = 0x2_0000;
    public static final int PERIPHERAL_CMD_SIZE = 0x2_0004;
    public static final int PERIPHERAL_CMD_MSG = 0x2_0008;
    
    public static final int PERIPHERAL_RSP_STATUS = 0x2_0080;
    public static final int PERIPHERAL_RSP_DATA = 0x2_0084;

    public PeripheralManager(RAM ram) {
        this.ram = ram;
    }

    public void tick() {
        ram.writeByte(PERIPHERAL_START, (byte)0x01);
        int w = ram.readWord(PERIPHERAL_START);
        if ((w & 0x00ff_0000) == 0x0001_0000) {
            try {
                int d = w & 0xffff;
                if (d == 0) {
                    onMessage();
                } else {
                    int size = ram.readWord(PERIPHERAL_CMD_SIZE);
                    int[] msg = ram.readWords(PERIPHERAL_CMD_MSG, size);
                    if (peripherals.containsKey(d) && peripherals.get(d) instanceof DMAPeripheral mmp) {
                        mmp.message(msg);
                    } else {
                        ram.writeWord(PERIPHERAL_RSP_DATA, 0xff);
                        ram.writeWord(PERIPHERAL_RSP_STATUS, 0x0f00_0000 | d);
                    }
                }
            } catch (Exception e) {
                System.err.println("Exception in peripheral manager, dumping message memory");
                System.err.println(ram.debugPrint(0x2_0000, 16));
                throw e;
            }
            ram.writeByte(PERIPHERAL_START + 1, (byte)0x2);
        }
        for (Peripheral peripheral : peripherals.values()) {
            peripheral.tick();
        }
    }

    public int addPeripheral(Peripheral peripheral) {
        int deviceId = nextId++;
        peripherals.put(deviceId, peripheral);
        switch (peripheral) {
            case DMAPeripheral dmap -> dmap.link(ram, deviceId);
            case MemoryMappedPeripheral mmp -> ram.addMMP(mmp);
            default -> {}
        }
        return deviceId;
    }

    private void onMessage() {
        int size = ram.readWord(PERIPHERAL_CMD_SIZE);
        int[] msg = ram.readWords(PERIPHERAL_CMD_MSG, Math.ceilDiv(size, 4));
        switch (msg[0]) {
            case 0x0 -> {
            }
            case 0x1 -> { // LIST START_ID
                byte start = (byte)msg[1];
                int arrI = 1;
                int[] out = new int[126];
                int numDevices = 0;
                for (byte i = start; i < nextId; i++) {
                    if (peripherals.containsKey(i) && peripherals.get(i) instanceof DMAPeripheral mmp) {
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
                ram.copyWords(out, PERIPHERAL_RSP_DATA, arrI);
                ram.writeWord(PERIPHERAL_RSP_STATUS, 0x0100_0000);
            }
        }
    }
}
