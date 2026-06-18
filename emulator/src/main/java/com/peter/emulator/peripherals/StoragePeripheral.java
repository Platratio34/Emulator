package com.peter.emulator.peripherals;

import java.io.File;
import java.nio.file.Path;

import com.peter.emulator.Packer;
import com.peter.emulator.components.RAM;

public class StoragePeripheral implements MemoryMappedPeripheral {

    public static final int DEVICE_TYPE = 0x0100_0001;
    public static final int[] MANUFACTURE = Packer.packChar("Virtual", 16);
    private static int nextSerial = 0;
    public final int[] serial = Packer.packChar((nextSerial++) + "", 16);

    protected RAM ram;
    protected int deviceId;

    protected final Path rootPath;

    public StoragePeripheral(Path rootPath) {
        this.rootPath = rootPath;
        // for (String fName : rootPath.toFile().list()) {
        //     System.out.println(fName);
        // }
    }

    @Override
    public void update() {
        
    }

    @Override
    public void message(int[] msg) {
        switch (msg[0]) {
            case 0x01 -> { // list files
                int startPathPntr = msg[1]; // null terminated char buffer
                int rplyAddr = msg[2]; // start address of reply buffer
                int rplyEnd = rplyAddr + msg[3]; // length of reply buffer (then added to start for simpler logic)
                int offset = msg[4]; // offset within the name list to read from
                String startPath = ram.readStringNT(startPathPntr);

                ram.writeWord(rplyAddr, 0x0);

                String[] names = rootPath.resolve(startPath).toFile().list();

                int wPntr = rplyAddr+1;
                int numWritten = 0;
                for (int i = offset; i < names.length; i++) {
                    String fName = names[i];
                    if (rplyEnd > wPntr + (fName.length() + 1) * 4) // only add the name if there is space left in the buffer
                        break;
                    for (int j = 0; j < fName.length(); j++) {
                        ram.writeWord(wPntr++, fName.charAt(j));
                    }
                    ram.writeWord(wPntr++, 0x0); // null terminate each name
                    numWritten++;
                }
                for (int i = wPntr; i < rplyEnd; i += 4) {
                    ram.writeWord(i, 0x0); // fill the rest of the buffer with null
                }
                
                ram.writeWord(rplyAddr, 0x1);

                ram.copyWords(new int[] { 0x01, names.length, numWritten }, PeripheralManager.PERIPHERAL_RSP_DATA);
                ram.writeWord(PeripheralManager.PERIPHERAL_RSP_STATUS, 0x0100_0000 | deviceId);
            }
            case 0x02 -> { // get file descriptor
                int pathPntr = msg[1]; // pointer to null terminated path string
                int rplyPntr = msg[2]; // start address of reply buffer
                int rplyEnd = rplyPntr + msg[3]; // length of reply buffer (then added to start for simpler logic)

                String path = ram.readStringNT(pathPntr);
                File f = rootPath.resolve(path).toFile();
                
                ram.copyWords(new int[] { 0x01 }, PeripheralManager.PERIPHERAL_RSP_DATA);
                ram.writeWord(PeripheralManager.PERIPHERAL_RSP_STATUS, 0x0100_0000 | deviceId);
            }

            default -> {
                ram.copyWords(new int[] { 0xff, 0x01 }, PeripheralManager.PERIPHERAL_RSP_DATA);
                ram.writeWord(PeripheralManager.PERIPHERAL_RSP_STATUS, 0x0100_0000 | deviceId);
            }
        }
    }

    @Override
    public void tick() {

    }

    @Override
    public void link(RAM ram, int deviceId) {
        this.ram = ram;
        this.deviceId = deviceId;
    }

    @Override
    public int[] getDescriptor() {
        return new int[] {
            0x00,
            DEVICE_TYPE,
            MANUFACTURE[0],
            MANUFACTURE[1],
            MANUFACTURE[2],
            MANUFACTURE[3],
            serial[0],
            serial[1],
            serial[2],
            serial[3],
            0x00,
            0x00,
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
