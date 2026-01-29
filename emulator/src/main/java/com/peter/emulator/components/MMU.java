package com.peter.emulator.components;

import java.util.ArrayList;

import com.peter.emulator.CPU;
import com.peter.emulator.peripherals.MemoryMappedPeripheral;

public class MMU {

    public static final int DEVICE_TYPE = 0x0100_0002;
    public static final int BLOCK_SIZE = 0x0800;

    private ArrayList<AddressSpace> addressSpaces = new ArrayList<>();

    public void addAddressSpace(int start, int size, int offset, int pid) {
        addressSpaces.add(new AddressSpace(start, size, offset, pid));
    }

    public int translate(CPU cpu, int addr) {
        if (cpu.memTablePtr == 0) {
            if(!cpu.privilegeMode)
                throw new MemoryException(addr);
            return addr;
        }
        if (addr < 0x1_0000) { // kernal ram
            if(!cpu.privilegeMode)
                throw new MemoryException(addr);
        } else if (addr < 0x2_0000) { // device ram
            if(!cpu.privilegeMode)
                throw new MemoryException(addr);
        } else { // process ram
            int localAddr = addr - 0x2_0000;
            int numPages = cpu.ram.read(cpu.memTablePtr);
            int pageIndex = (localAddr & 0xffff_f000) >> 12;
            int addrOffset = localAddr & 0xfff;
            if (pageIndex > numPages)
                throw new MemoryException(addr);
            int pageAddr = cpu.ram.read(cpu.memTablePtr + pageIndex + 1);
            return pageAddr | addrOffset;
            // for (int i = 0; i < maxNumBlocks; i++) {
            //     if (cpu.pid != ram.read(blockAddr + 2))
            //         continue;
            //     int offset = ram.read(blockAddr + 3);
            //     if (localAddr >= offset && localAddr < offset + BLOCK_SIZE) {
            //         addr = (addr - offset) + (BLOCK_SIZE * i);
            //         if (addr > cpu.memSize) {
            //             throw new MemoryException(addr);
            //         }
            //         return addr;
            //     }
            // }
            // throw new MemoryException(addr);
        }
        if (addr >= cpu.ram.size) {
            throw new MemoryException(addr);
        }
        return addr;
    }
    
    public static class AddressSpace {

        public boolean remapped = false;
        public int start;
        public int size;
        public int offset = 0;

        public int pid = -1;

        public ProtectionLevel protectionLevel;

        public AddressSpace(int start, int size, int offset, int pid) {
            this.start = start;
            this.size = size;
            this.offset = offset;
            this.pid = pid;
            remapped = true;
            protectionLevel = ProtectionLevel.ANY;
        }

        public AddressSpace(int start, int size, int offset, ProtectionLevel protectionLevel) {
            this.start = start;
            this.size = size;
            this.offset = offset;
            pid = 0;
            remapped = false;
            this.protectionLevel = protectionLevel;
        }
    }

    public static enum ProtectionLevel {
        ANY,
        PRIVILEGED,
        NEVER_PRIVILEGED,
        DEVICE;
    }

    public static class MemoryException extends RuntimeException {
        public int address = 0;

        public MemoryException(int address) {
            this.address = address;
        }

        @Override
        public String toString() {
            return String.format("Emulator Memory Exception: 0x%x", address);
        }
    }
}
