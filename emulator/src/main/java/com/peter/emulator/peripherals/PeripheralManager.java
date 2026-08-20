package com.peter.emulator.peripherals;

import static com.peter.emulator.MachineCode.PERIPHERAL_START;

import java.util.ArrayList;
import java.util.HashMap;

import com.peter.emulator.CPU;
import com.peter.emulator.components.BusComponent;
import com.peter.emulator.components.ComponentBus;
import com.peter.emulator.components.MemoryException;
import com.peter.emulator.components.RAM;

public class PeripheralManager implements BusComponent {

    public final ComponentBus componentBus;
    public final CPU cpu;
    private final DMAPeripheral[] peripherals = new DMAPeripheral[64];
    private final ArrayList<MemoryMappedPeripheral> mmps = new ArrayList<>();
    private final HashMap<Integer, MemoryMappedPeripheral> mappedPeripherals = new HashMap<>();

    public static final int PERIPHERAL_START = 0x1_0000;
    public static final int PERIPHERAL_CMD_SIZE = 0x1_0004;
    public static final int PERIPHERAL_CMD_MSG = 0x1_0008;
    
    public static final int PERIPHERAL_RSP_STATUS = 0x1_0080;
    public static final int PERIPHERAL_RSP_DEVICE = 0x1_0083;
    public static final int PERIPHERAL_RSP_DATA = 0x1_0084;

    private final byte[] commandBus = new byte[0x100];

    public static final int PERIPHERAL_TABLE = 0x1_0100;

    public PeripheralManager(ComponentBus componentBus, CPU cpu) {
        this.componentBus = componentBus;
        this.cpu = cpu;
    }

    public void tick() {
        commandBus[0x00] = 0x01;
        
        if (commandBus[0x01] == 0x01) {
            commandBus[0x80] = 0x00;
            try {
                int d = ((commandBus[0x02] & 0xff) << 8) | (commandBus[0x03] & 0xff);
                int size = getCommandWord(1);
                int[] msg = new int[size];
                for (int i = 0; i < size; i++) {
                    msg[i] = getCommandWord(i + 2);
                }
                System.out.println("Message for device"+d+" of size "+size);
                if (d == 0) {
                    onMessage(msg);
                } else {
                    if (peripherals[d] != null) {
                        peripherals[d].message(msg);
                    } else {
                        writeRspWords(0x0f, d, 0xff);
                    }
                }
            } catch (Exception e) {
                System.err.println("Exception in peripheral manager, dumping message memory");
                // System.err.println(ram.debugPrint(0x1_0000, 16));
                // TODO re-add command dump here
                throw e;
            }
            commandBus[0x01] = 0x02;
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
                        dmap.link(this, cpu, i);
                        System.out.println(String.format("Adding DMA peripheral #%d: %s", i, peripheral));
                        return i;
                    }
                }
                return -1;
            }
            case MemoryMappedPeripheral mmp -> {
                mmps.add(mmp);
                for(int address : mmp.getAddresses()) {
                    mappedPeripherals.put(address, mmp);
                }
                return 0;
            }
            default -> {}
        }
        return -1;
    }

    private void onMessage(int[] msg) {
        switch (msg[0]) {
            case 0x0 -> {
            }
            case 0x1 -> { // DESCRIPTOR ID
                int id = msg[1];
                if (peripherals[id] != null) {
                    writeRspWords(0x01, id, peripherals[id].getDescriptor());
                    return;
                }
                writeRspWords(0x0f, id, 0xff);
            }
        }
    }

    protected int getCommandWord(int i) {
        i *= 4;
        return ((commandBus[i] & 0xff) << 24) | ((commandBus[i+1] & 0xff) << 24) | ((commandBus[i+2] & 0xff) << 8)
                | (commandBus[i+3] & 0xff);
    }
    
    public void writeRsp(int status, int deviceId, byte... rsp) {
        System.arraycopy(rsp, 0, commandBus, 0x84, rsp.length);
        commandBus[0x83] = (byte) deviceId;
        commandBus[0x80] = (byte) status;
    }

    public void writeRspWords(int status, int deviceId, int... rsp) {
        int bi = 0x84;
        for (int i = 0; i < rsp.length; i++) {
            commandBus[bi] = (byte) (rsp[i] >> 24);
            commandBus[bi + 1] = (byte) ((rsp[i] >> 16) & 0xff);
            commandBus[bi + 2] = (byte) ((rsp[i] >> 8) & 0xff);
            commandBus[bi + 3] = (byte) (rsp[i] & 0xff);
            bi += 4;
        }
        commandBus[0x83] = (byte) deviceId;
        commandBus[0x80] = (byte) status;
    }

    private static final int[] addresses = new int[64 * 4];
    static {
        for (int i = 0; i < 256; i++) {
            addresses[i] = PERIPHERAL_TABLE + i;
        }
    }

    @Override
    public int getStart() {
        return PERIPHERAL_START;
    }

    @Override
    public int getNumBlocks() {
        return 1;
    }

    @Override
    public void writeByte(int address, byte value) {
        if (address - PERIPHERAL_START < 0x80) {
            commandBus[address - PERIPHERAL_START] = value;
            return;
        }
        if (mappedPeripherals.containsKey(address)) {
            mappedPeripherals.get(address).onUpdate(address, value);
            return;
        }
        throw MemoryException.Write(address);
    }

    @Override
    public byte readByte(int address) {
        if (address - PERIPHERAL_START < 0x100) {
            return commandBus[address - PERIPHERAL_START];
        }
        if (mappedPeripherals.containsKey(address)) {
            return mappedPeripherals.get(address).get(address);
        }
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
