package com.peter.emulator.machinecode;

import java.util.HashMap;

public enum Reg {
    R0(0x00, "r0"),
    R1(0x01, "r1"),
    R2(0x02, "r2"),
    R3(0x03, "r3"),
    R4(0x04, "r4"),
    R5(0x05, "r5"),
    R6(0x06, "r6"),
    R7(0x07, "r7"),
    R8(0x08, "r8"),
    R9(0x09, "r9"),
    R10(0x0a, "r10"),
    R11(0x0b, "r11"),
    R12(0x0c, "r12"),
    R13(0x0d, "r13"),
    R14(0x0e, "r14"),
    R15(0x0f, "r15"),
    
    R0_I(0x10, "r0I"),
    R1_I(0x11, "r1I"),
    R2_I(0x12, "r2I"),
    R3_I(0x13, "r3I"),
    R4_I(0x14, "r4I"),
    R5_I(0x15, "r5I"),
    R6_I(0x16, "r6I"),
    R7_I(0x17, "r7I"),
    R8_I(0x18, "r8I"),
    R9_I(0x19, "r9I"),
    R10_I(0x1a, "r10I"),
    R11_I(0x1b, "r11I"),
    R12_I(0x1c, "r12I"),
    R13_I(0x1d, "r13I"),
    R14_I(0x1e, "r14I"),
    R15_I(0x1f, "r15I"),
            
    PGM_I(0xe0, "rPgmI"),
    STACK_I(0xe1, "rStackI"),
    AF_I(0xe2, "rAFI"),
            
    PID_I(0xe8, "rPIDI"),
    MEM_TABLE_I(0xe9, "rMemTblI"),
            
    PRIVILEGE_I(0xef, "rPMI"),

    PGM(0xf0, "rPgm"),
    STACK(0xf1, "rStack"),
    AF(0xf2, "rAF"),
            
    PID(0xf8, "rPID"),
    MEM_TABLE(0xf9, "rMemTbl"),
    
    INTERRUPT_CODE(0xfa, "rIC"),
    INTERRUPT_HANDLER(0xfb, "rIH"),
            
    CPU_ID(0xfe, "rID"),
    PRIVILEGE(0xff, "rPM"),

    UNKNOWN(0xd0,"UNKNOWN")
    ;

    public final int code;
    public final String string;

    private Reg(int code, String string) {
        this.code = code;
        this.string = string;
        setup();
    }

    private static HashMap<Integer, Reg> byCode;
    private static HashMap<String, Reg> byString;

    private void setup() {
        if (byCode == null)
            byCode = new HashMap<>();
        byCode.put(code, this);
        if (byString == null)
            byString = new HashMap<>();
        byString.put(string, this);
    }
    
    public static Reg from(int code) {
        return byCode.getOrDefault(code & 0xff, UNKNOWN);
    }
    public static Reg from(String code) {
        return byString.getOrDefault(code, UNKNOWN);
    }
}
