package com.peter.emulator.peripherals;

import java.util.HashMap;

import com.peter.emulator.components.RAM;

public class PeripheralManager {

    private final RAM ram;
    private final HashMap<Integer, Peripheral> peripherals = new HashMap<>();
    private int nextId = 1;

    public PeripheralManager(RAM ram) {
        this.ram = ram;
    }

    public void tick() {
        ram.write(0x8000, 0x01);
        if (ram.read(0x8001) == 0x01) {
            try {
                int d = ram.read(0x8002);
                if (d == 0) {
                    onMessage();
                } else {
                    int size = ram.read(0x8003);
                    int[] msg = ram.read(0x8004, size);
                    if (peripherals.containsKey(d) && peripherals.get(d) instanceof MemoryMappedPeripheral mmp) {
                        mmp.message(msg);
                    } else {
                        ram.write(0x8081, d);
                        ram.write(0x8082, 0x1);
                        ram.write(0x8080, 0x0f);
                    }
                }
            } catch (Exception e) {
                System.err.println(ram.debugPrint(0x8000, 16));
                throw e;
            }
            ram.write(0x8001, 0x2);
        }
        for (Peripheral peripheral : peripherals.values()) {
            peripheral.tick();
        }
    }

    public int addPeripheral(Peripheral peripheral) {
        int deviceId = nextId++;
        peripherals.put(deviceId, peripheral);
        if (peripheral instanceof MemoryMappedPeripheral mmp) {
            mmp.link(ram, deviceId);
        }
        return deviceId;
    }

    private void onMessage() {
        int size = ram.read(0x8003);
        int[] msg = ram.read(0x8004, size);
        switch (msg[0]) {
            case 0x0 -> {
            }
            case 0x1 -> { // LIST START_ID
                int start = msg[1];
                int arrI = 1;
                int[] out = new int[126];
                int numDevices = 0;
                for (int i = start; i < nextId; i++) {
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
                ram.write(0x8081, 0x00);
                ram.copy(out, 0x8082, arrI);
                ram.write(0x8080, 0x1);
            }
        }
    }
}
