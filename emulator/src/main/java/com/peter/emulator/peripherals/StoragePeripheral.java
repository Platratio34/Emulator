package com.peter.emulator.peripherals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import com.peter.emulator.CPU;
import com.peter.emulator.Packer;
import com.peter.emulator.components.ComponentBus;
import com.peter.emulator.components.RAM;
import com.peter.emulator.lang.base.Peripheral;

public class StoragePeripheral implements DMAPeripheral {

    public static final int[] MANUFACTURE = Packer.packChar("Virtual", 16);
    private static int nextSerial = 0;
    public final int[] serial = Packer.packChar((nextSerial++) + "", 16);

    protected PeripheralManager manager;
    protected ComponentBus bus;
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
                String startPath = bus.readStringNT(startPathPntr);

                bus.writeWord(rplyAddr, 0x0);

                String[] names = rootPath.resolve(startPath).toFile().list();

                int wPntr = rplyAddr+1;
                int numWritten = 0;
                for (int i = offset; i < names.length; i++) {
                    String fName = names[i];
                    if (rplyEnd > wPntr + (fName.length() + 1) * 4) // only add the name if there is space left in the buffer
                        break;
                    for (int j = 0; j < fName.length(); j++) {
                        bus.writeWord(wPntr++, fName.charAt(j));
                    }
                    bus.writeWord(wPntr++, 0x0); // null terminate each name
                    numWritten++;
                }
                for (int i = wPntr; i < rplyEnd; i += 4) {
                    bus.writeWord(i, 0x0); // fill the rest of the buffer with null
                }
                
                bus.writeWord(rplyAddr, 0x1);

                manager.writeRspWords(0x01, deviceId, 0x01, names.length, numWritten);
            }
            case 0x02 -> { // get file descriptor
                int pathPntr = cpu.translateAddress(msg[1]); // pointer to null terminated path string
                // int rplyPntr = cpu.translateAddress(msg[2]); // start address of reply buffer
                // int rplyEnd = rplyPntr + msg[3]; // length of reply buffer (then added to start for simpler logic)

                String path = bus.readStringNT(pathPntr);
                File f = rootPath.resolve(path).toFile();
                if(!f.exists()) {
                    manager.writeRspWords(0x01, deviceId, 0x01, 0x0, 0x0);
                    return;
                }
                int len = 0;
                boolean isDir = f.isDirectory();
                if (!isDir) {
                    len = (int) f.length();
                }

                manager.writeRspWords(0x01, deviceId, 0x01, isDir ? 0x2 : 0x1, len);
            }
            case 0x10 -> { // open handle
                int pathPntr = cpu.translateAddress(msg[1]); // pointer to null terminated path string
                String path = bus.readStringNT(pathPntr);
                File f = rootPath.resolve(path).toFile();
                if (!f.exists()) {
                    System.err.println("File did not exist "+f.getAbsolutePath());
                    manager.writeRspWords(0x01, deviceId, 0x02, 0x0);
                    return;
                }
                int handle = nextHandle++;
                openFiles.put(handle, f);
                manager.writeRspWords(0x01, deviceId, 0x01, handle);
                return;
            }
            case 0x11 -> { // read from handle
                int handle = msg[1];
                int buffStart = cpu.translateAddress(msg[2]);
                int buffSize = msg[3];
                int offset = msg[4];
                // System.out.println(String.format("- %x %x %x %x", handle, buffStart, buffSize, offset));
                if (!openFiles.containsKey(handle)) {
                    manager.writeRspWords(0x01, deviceId, 0x02, handle, 0x0);
                    return;
                }
                File f = openFiles.get(handle);
                int written = 0;
                byte[] bytes;
                try {
                    bytes = Files.readAllBytes(f.toPath());
                    // System.out.println("- "+bytes.length);
                } catch (IOException e) {
                    manager.writeRspWords(0x01, deviceId, 0x0f, handle, 0x0);
                    System.err.println(e);
                    return;
                }
                for (int i = 0; i < buffSize; i++) {
                    int j = i + offset;
                    if (j >= bytes.length) {
                        break;
                    }
                    written++;
                    bus.writeByte(buffStart++, bytes[j]);
                }
                
                // System.out.println("- "+written);
                manager.writeRspWords(0x01, deviceId, 0x01, handle, written);
            }

            default -> {
                manager.writeRspWords(0x01, deviceId, 0xff, 0x01);
            }
        }
    }

    @Override
    public void tick() {

    }

    @Override
    public void link(PeripheralManager manager, CPU cpu, int deviceId) {
        this.bus = manager.componentBus;
        this.manager = manager;
        this.cpu = cpu;
        this.deviceId = deviceId;
    }

    @Override
    public int[] getDescriptor() {
        return new int[] {
            deviceId,
            Peripheral.TYPE_STORAGE_VIRTUAL,
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
        return Peripheral.TYPE_STORAGE_VIRTUAL;
    }
}
