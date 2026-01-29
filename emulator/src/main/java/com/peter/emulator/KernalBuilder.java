package com.peter.emulator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Map.Entry;

import com.peter.Main;
import com.peter.emulator.assembly.Assembler;
import com.peter.emulator.assembly.AssemblerError;
import com.peter.emulator.assembly.Linker;
import com.peter.emulator.assembly.SymbolFile;

public class KernalBuilder {

    public Linker linker = new Linker();

    protected int syscallStart = 0;

    protected int[] kernal = new int[0x1000];
    protected SymbolFile symbolFile = new SymbolFile();

    public int[] build() throws IOException {
        Assembler kernalAssembler = new Assembler();
        kernalAssembler.setSource(Main.ROOT_PATH.resolve("kernal/kernal.asm"));
        if (!kernalAssembler.assemble()) {
            System.err.println(String.format("%d error(s) building kernal:", kernalAssembler.errors.size()));
            for (AssemblerError err : kernalAssembler.errors) {
                System.err.println(err.getPrintString());
            }
            return new int[0];
        }
        int[] kernalOnly = kernalAssembler.build();
        syscallStart = kernalOnly.length;
        for (Entry<String, Integer> entry : kernalAssembler.getSyscallMapping()) {
            linker.mapSyscall(entry.getKey(), entry.getValue());
        }
        for (String syscall : kernalAssembler.getSyscallMap()) {
            linker.addSyscall(kernalAssembler, syscall, 0);
        }
        symbolFile.combine(kernalAssembler.symbols, 0);
        Files.writeString(Main.ROOT_PATH.resolve("obj/_kernal.obj"), kernalAssembler.symbols.toFile(),
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        Assembler syscallAssembler = new Assembler();
        syscallAssembler.setSource(Main.ROOT_PATH.resolve("kernal/syscall.asm"));
        if (!syscallAssembler.assemble()) {
            System.err.println(String.format("%d error(s) building kernal syscalls:", kernalAssembler.errors.size()));
            for (AssemblerError err : kernalAssembler.errors) {
                System.err.println(err.getPrintString());
            }
            return new int[0];
        }
        int[] syscalls = syscallAssembler.build();
        for (Entry<String, Integer> entry : syscallAssembler.getSyscallMapping()) {
            linker.mapSyscall(entry.getKey(), entry.getValue());
        }
        for (String syscall : syscallAssembler.getSyscallMap()) {
            linker.addSyscall(syscallAssembler, syscall, syscallStart);
        }
        symbolFile.combine(syscallAssembler.symbols, syscallStart);
        Files.writeString(Main.ROOT_PATH.resolve("obj/_syscall.obj"), syscallAssembler.symbols.toFile(),
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        System.arraycopy(kernalOnly, 0, kernal, 0, kernalOnly.length);
        System.arraycopy(syscalls, 0, kernal, syscallStart, syscalls.length);

        return kernal;
    }

    public byte[] binary() {
        byte[] arr = new byte[kernal.length * 4];
        for (int i = 0; i < kernal.length; i++) {
            int w = kernal[i];
            arr[i * 4] = (byte) ((w >> 24) & 0xff);
            arr[i * 4] = (byte) ((w >> 16) & 0xff);
            arr[i * 4] = (byte) ((w >> 8) & 0xff);
            arr[i * 4] = (byte) (w & 0xff);
        }
        return arr;
    }

    public SymbolFile getSymbols() {
        return symbolFile;
    }

    public String[] sysCallNames() {
        return linker.sysCallMap;
    }

    public int[] sysCallTable() {
        int[] table = new int[linker.sysCallMap.length];
        for (int i = 0; i < table.length; i++) {
            String func = linker.sysCallMap[i];
            if (linker.sysCalls.containsKey(func)) {
                table[i] = linker.sysCalls.get(func);
            } else {
                table[i] = 0xffff_ffff;
            }
        }
        return table;
    }
}
