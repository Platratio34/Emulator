package com.peter.emulator.peripherals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import com.peter.emulator.CPU;
import com.peter.emulator.Packer;
import com.peter.emulator.components.RAM;

public class StoragePeripheral implements DMAPeripheral {

    public static final int DEVICE_TYPE = 0x0100_0001;
    public static final int[] MANUFACTURE = Packer.packChar("Virtual", 16);
    private static int nextSerial = 0;
    public final int[] serial = Packer.packChar((nextSerial++) + "", 16);

    protected RAM ram;
    protected CPU cpu;
    protected int deviceId;

    protected final Path rootPath;

    protected final HashMap<Integer, File> openFiles = new HashMap<>();
    protected int nextHandle = 1;

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
                int startPathPntr = cpu.translateAddress(msg[1]); // null terminated char buffer
                int rplyAddr = cpu.translateAddress(msg[2]); // start address of reply buffer
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
                int pathPntr = cpu.translateAddress(msg[1]); // pointer to null terminated path string
                // int rplyPntr = cpu.translateAddress(msg[2]); // start address of reply buffer
                // int rplyEnd = rplyPntr + msg[3]; // length of reply buffer (then added to start for simpler logic)

                String path = ram.readStringNT(pathPntr);
                File f = rootPath.resolve(path).toFile();
                if(!f.exists()) {
                    ram.copyWords(new int[] { 0x01, 0x0, 0x0 }, PeripheralManager.PERIPHERAL_RSP_DATA);
                    ram.writeWord(PeripheralManager.PERIPHERAL_RSP_STATUS, 0x0100_0000 | deviceId);
                    return;
                }
                int len = 0;
                boolean isDir = f.isDirectory();
                if (!isDir) {
                    len = (int) f.length();
                }

                ram.copyWords(new int[] { 0x01, isDir ? 0x2 : 0x1, len }, PeripheralManager.PERIPHERAL_RSP_DATA);
                ram.writeWord(PeripheralManager.PERIPHERAL_RSP_STATUS, 0x0100_0000 | deviceId);
            }
            case 0x10 -> { // open handle
                int pathPntr = cpu.translateAddress(msg[1]); // pointer to null terminated path string
                String path = ram.readStringNT(pathPntr);
                File f = rootPath.resolve(path).toFile();
                if (!f.exists()) {
                    System.err.println("File did not exist "+f.getAbsolutePath());
                    ram.copyWords(new int[] { 0x02, 0x0 }, PeripheralManager.PERIPHERAL_RSP_DATA);
                    ram.writeWord(PeripheralManager.PERIPHERAL_RSP_STATUS, 0x0100_0000 | deviceId);
                    return;
                }
                int handle = nextHandle++;
                openFiles.put(handle, f);
                ram.copyWords(new int[] { 0x01, handle }, PeripheralManager.PERIPHERAL_RSP_DATA);
                ram.writeWord(PeripheralManager.PERIPHERAL_RSP_STATUS, 0x0100_0000 | deviceId);
                return;
            }
            case 0x11 -> { // read from handle
                int handle = msg[1];
                int buffStart = cpu.translateAddress(msg[2]);
                int buffSize = msg[3];
                int offset = msg[4];
                // System.out.println(String.format("- %x %x %x %x", handle, buffStart, buffSize, offset));
                if (!openFiles.containsKey(handle)) {
                    ram.copyWords(new int[] { 0x02, handle, 0x0 }, PeripheralManager.PERIPHERAL_RSP_DATA);
                    ram.writeWord(PeripheralManager.PERIPHERAL_RSP_STATUS, 0x0100_0000 | deviceId);
                    return;
                }
                File f = openFiles.get(handle);
                int written = 0;
                byte[] bytes;
                try {
                    bytes = Files.readAllBytes(f.toPath());
                    // System.out.println("- "+bytes.length);
                } catch (IOException e) {
                    ram.copyWords(new int[] { 0x0f, handle, 0x0 }, PeripheralManager.PERIPHERAL_RSP_DATA);
                    ram.writeWord(PeripheralManager.PERIPHERAL_RSP_STATUS, 0x0100_0000 | deviceId);
                    System.err.println(e);
                    return;
                }
                for (int i = 0; i < buffSize; i++) {
                    int j = i + offset;
                    if (j >= bytes.length) {
                        break;
                    }
                    written++;
                    ram.writeByte(buffStart++, bytes[j]);
                }
                
                // System.out.println("- "+written);
                ram.copyWords(new int[] { 0x01, handle, written }, PeripheralManager.PERIPHERAL_RSP_DATA);
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
    public void link(RAM ram, CPU cpu, int deviceId) {
        this.ram = ram;
        this.cpu = cpu;
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
