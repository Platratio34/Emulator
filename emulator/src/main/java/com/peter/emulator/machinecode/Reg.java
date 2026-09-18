package com.peter.emulator.machinecode;

import java.util.HashMap;

public enum Reg {
    UNKNOWN(0xd0,"UNKNOWN", "Unknown register"),

    R0(0x00, "r0", "User register 0"),
    R1(0x01, "r1", "User register 1"),
    R2(0x02, "r2", "User register 2"),
    R3(0x03, "r3", "User register 3"),
    R4(0x04, "r4", "User register 4"),
    R5(0x05, "r5", "User register 5"),
    R6(0x06, "r6", "User register 6"),
    R7(0x07, "r7", "User register 7"),
    R8(0x08, "r8", "User register 8"),
    R9(0x09, "r9", "User register 9"),
    R10(0x0a, "r10", "User Register 10"),
    R11(0x0b, "r11", "User Register 11"),
    R12(0x0c, "r12", "User Register 12"),
    R13(0x0d, "r13", "User Register 13"),
    R14(0x0e, "r14", "User Register 14"),
    R15(0x0f, "r15", "User Register 15"),
    
    R0_I(0x10, "r0I", "Interrupt stored User register 0"),
    R1_I(0x11, "r1I", "Interrupt stored User register 1"),
    R2_I(0x12, "r2I", "Interrupt stored User register 2"),
    R3_I(0x13, "r3I", "Interrupt stored User register 3"),
    R4_I(0x14, "r4I", "Interrupt stored User register 4"),
    R5_I(0x15, "r5I", "Interrupt stored User register 5"),
    R6_I(0x16, "r6I", "Interrupt stored User register 6"),
    R7_I(0x17, "r7I", "Interrupt stored User register 7"),
    R8_I(0x18, "r8I", "Interrupt stored User register 8"),
    R9_I(0x19, "r9I", "Interrupt stored User register 9"),
    R10_I(0x1a, "r10I", "Interrupt stored User register 10"),
    R11_I(0x1b, "r11I", "Interrupt stored User register 11"),
    R12_I(0x1c, "r12I", "Interrupt stored User register 12"),
    R13_I(0x1d, "r13I", "Interrupt stored User register 13"),
    R14_I(0x1e, "r14I", "Interrupt stored User register 14"),
    R15_I(0x1f, "r15I", "Interrupt stored User register 15"),
            
    PGM_I(0xe0, "rPgmI", "Interrupt stored Program pointer (next address)"),
    STACK_I(0xe1, "rStackI", "Interrupt stored Stack pointer"),
    AF_I(0xe2, "rAFI", "Interrupt stored Arithmetic Flag"),
            
    PID_I(0xe8, "rPIDI", "Interrupt stored Process ID (Kernal set) **(PM Required)**"),
    MEM_TABLE_I(0xe9, "rMemTblI", "Interrupt stored Memory Map table pointer **(PM Required)**"),
            
    PRIVILEGE_I(0xef, "rPMI", "Interrupt stored Interrupt code **(PM Required)**"),

    PGM(0xf0, "rPgm", "Program pointer (next address)"),
    STACK(0xf1, "rStack", "Stack pointer"),
    AF(0xf2, "rAF", "Arithmetic Flag"),
            
    SYS_TABLE(0xf7, "rSysTbl", "Syscall Table Pointer **(PM Required)**"),
    PID(0xf8, "rPID", "Process ID (Kernal set) **(PM Required)**"),
    MEM_TABLE(0xf9, "rMemTbl", "Memory Map table pointer **(PM Required)**"),
    
    INTERRUPT_CODE(0xfa, "rIC", "Interrupt code **(PM Required)**"),
    INTERRUPT_HANDLER(0xfb, "rIH", "Interrupt Handler pointer **(PM Required)**"),
            
    CPU_ID(0xfe, "rID", "Core ID *(Read Only)*"),
    PRIVILEGE(0xff, "rPM", "Privileged Mode **(PM Required)**"),
    ;

    public final int code;
    public final String string;
    public final String description;

    private Reg(int code, String string, String description) {
        this.code = code;
        this.string = string;
        this.description = description;
        setup();
    }

    private static Reg[] byCode;
    private static HashMap<String, Reg> byString;

    @Override
    public String toString() {
        return string;
    }

    private void setup() {
        if (byCode == null) {
            byCode = new Reg[256];
        }
        byCode[code] = this;
        if (byString == null)
            byString = new HashMap<>();
        byString.put(string, this);
    }
    
    public static Reg from(int code) {
        Reg r = byCode[code & 0xff];
        return r != null ? r : UNKNOWN;
    }
    public static Reg from(String code) {
        return byString.getOrDefault(code, UNKNOWN);
    }
}
