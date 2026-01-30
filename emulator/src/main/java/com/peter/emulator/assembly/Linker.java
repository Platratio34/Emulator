package com.peter.emulator.assembly;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import com.peter.Main;

public class Linker {

    public HashMap<String, Integer> sysCalls = new HashMap<>();
    public String[] sysCallMap = new String[64];
    private final SymbolFile symbols = new SymbolFile();

    public void addSyscall(Assembler assembler, String syscall, int start) {
        int func = assembler.syscallDef.get(syscall) + start;
        sysCalls.put(syscall, func);
        // System.out.println(String.format("Pointing syscall %s to [0x%x]", syscall, func));
    }

    public void mapSyscall(String syscall, int index) {
        if (index >= sysCallMap.length) {
            int newSize = sysCallMap.length + 16;
            while (index >= newSize)
                newSize += 16;
            String[] arr2 = new String[newSize];
            System.arraycopy(sysCallMap, 0, arr2, 0, sysCallMap.length);
            sysCallMap = arr2;
        }
        // System.out.println(String.format("Mapping syscall %s to index 0x%x", syscall, index));
        sysCallMap[index] = syscall;
    }

    public int getSyscall(String name) {
        for (int i = 0; i < sysCallMap.length; i++) {
            if (sysCallMap[i] != null && sysCallMap[i].equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public boolean hasDefinition(String key) {
        return symbols.definitions.containsKey(key);
    }

    public int getDefinition(String key) {
        return symbols.definitions.get(key).getValue();
    }

    public void include(String file) {
        if (file.endsWith(".asm")) {
            
        } else {
            if (!file.endsWith(".obj"))
                file += ".obj";
            try {
                SymbolFile symbolFile = SymbolFile
                        .fromFile(Files.readString(Main.ROOT_PATH.resolve("obj").resolve(file)));
                for (String syscall : symbolFile.syscalls.keySet()) {
                    mapSyscall(syscall, symbolFile.syscalls.get(syscall));
                }
                symbols.combine(symbolFile, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
