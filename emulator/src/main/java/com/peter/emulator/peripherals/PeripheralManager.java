package com.peter.emulator.peripherals;

import java.util.ArrayList;

import com.peter.emulator.CPU;
import com.peter.emulator.components.MemoryException;
import com.peter.emulator.components.RAM;

public class PeripheralManager implements MemoryMappedPeripheral {

    private final RAM ram;
    private final CPU cpu;
    private final DMAPeripheral[] peripherals = new DMAPeripheral[64];
    private final ArrayList<MemoryMappedPeripheral> mmps = new ArrayList<>();

    public static final int PERIPHERAL_START = 0x1_0000;
    public static final int PERIPHERAL_CMD_SIZE = 0x1_0004;
    public static final int PERIPHERAL_CMD_MSG = 0x1_0008;
    
    public static final int PERIPHERAL_RSP_STATUS = 0x1_0080;
    public static final int PERIPHERAL_RSP_DEVICE = 0x1_0083;
    public static final int PERIPHERAL_RSP_DATA = 0x1_0084;

    public static final int PERIPHERAL_TABLE = 0x1_0100;

    public PeripheralManager(RAM ram, CPU cpu) {
        this.ram = ram;
        this.cpu = cpu;
        ram.addMMP(this);
    }

    @Override
    public void tick() {
        ram.writeByte(PERIPHERAL_START, (byte)0x01);
        int w = ram.readWord(PERIPHERAL_START);
        if ((w & 0x00ff_0000) == 0x0001_0000) {
            ram.writeWord(PeripheralManager.PERIPHERAL_RSP_STATUS, 0x0);
            try {
                int d = w & 0xffff;
                if (d == 0) {
                    onMessage();
                } else {
                    int size = ram.readWord(PERIPHERAL_CMD_SIZE);
                    int[] msg = ram.readWords(PERIPHERAL_CMD_MSG, size);
                    if (peripherals[d] != null) {
                        peripherals[d].message(msg);
                    } else {
                        ram.writeWord(PERIPHERAL_RSP_DATA, 0xff);
                        ram.writeWord(PERIPHERAL_RSP_STATUS, 0x0f00_0000 | d);
                    }
                }
            } catch (Exception e) {
                System.err.println("Exception in peripheral manager, dumping message memory");
                System.err.println(ram.debugPrint(0x1_0000, 16));
                throw e;
            }
            ram.writeByte(PERIPHERAL_START + 1, (byte)0x2);
        }
        for (Peripheral peripheral : peripherals) {
            if (peripheral != null) {
                peripheral.tick();
            }
        }
        for(MemoryMappedPeripheral mmp : mmps) {
            mmp.tick();
        }
    }

    public int addPeripheral(Peripheral peripheral) {
        switch (peripheral) {
            case DMAPeripheral dmap -> {
                for(int i = 1; i < peripherals.length; i++) {
                    if(peripherals[i] == null) {
                        peripherals[i] = dmap;
                        dmap.link(ram, cpu, i);
                        System.out.println(String.format("Adding DMA peripheral #%d: %s", i, peripheral));
                        return i;
                    }
                }
                return -1;
            }
            case MemoryMappedPeripheral mmp -> {
                ram.addMMP(mmp);
                mmps.add(mmp);
                return 0;
            }
            default -> {}
        }
        return -1;
    }

    private void onMessage() {
        int size = ram.readWord(PERIPHERAL_CMD_SIZE);
        int[] msg = ram.readWords(PERIPHERAL_CMD_MSG, size);
        switch (msg[0]) {
            case 0x0 -> {
            }
            case 0x1 -> { // DESCRIPTOR ID
                int id = msg[1];
                if(peripherals[id] != null) {
                    ram.copyWords(peripherals[id].getDescriptor(), PERIPHERAL_RSP_DATA);
                    ram.writeWord(PERIPHERAL_RSP_STATUS, 0x0100_0000 | id);
                    return;
                }
                ram.writeWord(PERIPHERAL_RSP_DATA, 0xff);
                ram.writeWord(PERIPHERAL_RSP_STATUS, 0x0f00_0000 | id);
            }
        }
    }

    private static final int[] addresses = new int[64 * 4];
    static {
        for(int i = 0; i < 256; i++) {
            addresses[i] = PERIPHERAL_TABLE + i;
        }
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
        int i = (address - PERIPHERAL_TABLE) / 4;
        int o = address % 4;
        if(i == 0) {
            return (byte)((o == 3) ? 1 : 0);
        } else if(peripherals[i] == null) {
            return 0;
        }
        int type = peripherals[i].getType();
        return switch(o) {
            case 0 -> (byte)(type >> 24);
            case 1 -> (byte)(type >> 16);
            case 2 -> (byte)(type >> 8);
            case 3 -> (byte)type;
            default -> 0;
        };
    }
}
