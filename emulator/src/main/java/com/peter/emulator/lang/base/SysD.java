package com.peter.emulator.lang.base;

import com.peter.emulator.lang.Namespace;

public class SysD extends Namespace {

    public static final SysD INSTANCE = new SysD();

    private SysD() {
        super("SysD");
        // void memSet(uint32 address, uint32 value)
        // void memSet(uint32 address, char value)
        // uint32 memGet(uint32 address)
        // void memCopy(void* src, uint32 start, uint32 end, void* dest, uint32 start);
        // void <T> memCopy(T* src, uint32 start, uint32 end, T* dest, uint32 start);
        // boolean <T> memEquals(T* a, T* b, uint32 length);

        // void* sysCall(uint32 call)

        // uint32 getPID()

        // const uint32 MEMORY_DEVICE_START = 0x1_0000;
        // const uint32 MEMORY_PROCESS_START = 0x2_0000;
        // const uint32 MEMORY_BLOCK_SIZE = 0x8000;

        // const uint32 REG_PGM_PNTR = 0xf0;
        // const uint32 REG_STACK_PNTR = 0xf1;
        // const uint32 REG_PID = 0xf8;
        // const uint32 REG_MEM_TABLE = 0xf9;
        // const uint32 REG_PRIVILEGED_MODE = 0xff;

        // void copyFromReg(uint32 reg, uint32* target)
        // void copyToReg(uint32* src, uint32 reg)
    }

}
