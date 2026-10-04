package com.peter.emulator;

import java.util.HashMap;

public class MachineCode {

    public static final int MASK_INSTRUCTION = 0xff00_0000;

    public static final int HALT = 0xff << 24;

    public static final int NO_OP = 0x00 << 24;

    public static final int LOAD = 0x01 << 24;
    public static final int MASK_LOAD_RG = 0x00ff_0000;
    public static final int MASK_LOAD_RA = 0x0000_00ff;
    public static final int MASK_LOAD_MODE = 0x0000_ff00;
    public static final int LOAD_LITERAL = 0x0000_0000;
    public static final int LOAD_MEM = 0x0000_0100;
    public static final int LOAD_MEM_SHORT = 0x0000_0200;
    public static final int LOAD_MEM_BYTE = 0x0000_0300;

    public static final int STORE = 0x02 << 24;
    public static final int MASK_STORE_RG = 0x00ff_0000;
    public static final int MASK_STORE_SIZE = 0b0000_0011 << 8;
    public static final int MASK_STORE_SOURCE = 0b0000_1100 << 8;
    public static final int MASK_STORE_FLAG_INC_RG = 0b1000_0000 << 8;
    public static final int MASK_STORE_FLAG_INC_RA = 0b0100_0000 << 8;
    public static final int MASK_STORE_RA = 0x0000_00ff;
    public static final int STORE_MEM = 0x0000_0100;
    public static final int STORE_SIZE_WORD = 0b0000_0000 << 8;
    public static final int STORE_SIZE_SHORT = 0b0000_0001 << 8;
    public static final int STORE_SIZE_BYTE = 0b0000_0010 << 8;
    public static final int STORE_SOURCE_REG = 0b0000_0000 << 8;
    public static final int STORE_SOURCE_VAL = 0b0000_0100 << 8;
    public static final int STORE_SOURCE_MEM = 0b0000_1000 << 8;
    public static final int STORE_SOURCE_REG_REG = 0b0000_1100 << 8;
    
    public static final int MATH = 0x04 << 24;
    public static final int MASK_MATH_OP = 0x00f0_0000;
    public static final int MASK_MATH_RD = 0x000f_0000;
    public static final int MASK_MATH_RA = 0x0000_ff00;
    public static final int MASK_MATH_RB = 0x0000_00ff;
    public static final int MASK_MATH_INC = 0x0000_ffff;
    
    public static final int GOTO = 0x05 << 24;
    public static final int MASK_GOTO_OP = 0x00ff_0000;
    public static final int MASK_GOTO_RA = 0x0000_ff00;
    public static final int MASK_GOTO_RG = 0x0000_00ff;
    public static final int MASK_GOTO_COND = 0b0000_1111 << 16;
    public static final int MASK_GOTO_REL = 0b0001_0000 << 16;
    public static final int MASK_GOTO_PUSH = 0b0010_0000 << 16;
    public static final int MASK_GOTO_POP = 0b0100_0000 << 16;
    
    public static final int SET = 0x06 << 24;
    public static final int MASK_SET_OP = 0x000f_0000;
    public static final int MASK_SET_RD = 0x0000_ff00;
    public static final int MASK_SET_RG = 0x0000_00ff;
    public static final int SET_FORCED = 0x0010_0000;

    public static final int STACK = 0x10 << 24;
    public static final int MASK_STACK_RG = 0x0000_00ff;
    public static final int MASK_STACK_OP = 0x00ff_0000;
    public static final int MASK_STACK_VAL = 0x0000_ffff;
    public static final int STACK_PUSH = 0x0000_0000;
    public static final int STACK_POP = 0x0001_0000;
    public static final int STACK_INC = 0x0002_0000;
    public static final int STACK_DEC = 0x0003_0000;

    public static final int SYSCALL = 0x11 << 24;
    public static final int MASK_SYSCALL_FUNCTION = 0x0000_ffff;
    public static final int MASK_SYSCALL_OPTION = 0x00ff_0000;
    public static final int MASK_SYSCALL_RG = 0x0000_00ff;
    public static final int SYSCALL_FUNCTION = 0x0000_0000;
    public static final int SYSCALL_RETURN = 0x0001_0000;
    public static final int SYSCALL_GOTO = 0x0002_0000;
    public static final int SYSCALL_INTERRUPT = 0x0003_0000;
    public static final int MASK_SYSCALL_INTERRUPT_OP = 0x0000_ff00;
    public static final int SYSCALL_INTERRUPT_RG = 0x0000_0000;
    public static final int SYSCALL_INTERRUPT_VAL = 0x0000_0100;
    public static final int SYSCALL_INTERRUPT_RET = 0x0000_ff00;


    public static final int REG_PGM_PNTR = 0xf0;
    public static final int REG_STACK_PNTR = 0xf1;
    public static final int REG_ARITHMETIC_FLAG = 0xf2;
    
    public static final int REG_PID = 0xf8;
    public static final int REG_MEM_TABLE = 0xf9;

    public static final int REG_INTERRUPT = 0xfa;
    public static final int REG_INTR_HANDLER = 0xfb;
    
    public static final int REG_CPU_ID = 0xfe;
    public static final int REG_PRIVILEGED_MODE = 0xff;
    
    public static final int REG_PGM_PNTR_I = 0xe0;
    public static final int REG_STACK_PNTR_I = 0xe1;
    public static final int REG_ARITHMETIC_FLAG_I = 0xe2;
    
    public static final int REG_PID_I = 0xe8;
    public static final int REG_MEM_TABLE_I = 0xe9;

    public static final int REG_PRIVILEGED_MODE_I = 0xef;

    public static final int SYSCALL_TABLE_START = 0xf000;
    
    public static final int KERNAL_OFFSET = 0x1000;
    public static final int PERIPHERAL_START = 0x1_0000;
    public static final int PROCESS_OFFSET = 0x2_0000;
    
    public static int int8(int val) {
        byte v = (byte) val;
        return (int)v;
    }

    public static int int32ToInt8(int val) {
        if (val < -128 || val > 127)
            throw new RuntimeException("Invalid int32 to int8 conversion");
        if (val < 0) {
            byte v = (byte) val;
            return ((int) v) & 0xff;
        } else {
            return val & 0x7f;
        }
    }

    public static String toHex(int num) {
        String str = String.format("%08x", num);
        return str.substring(0, 4) + "_" + str.substring(4);
    }
}
