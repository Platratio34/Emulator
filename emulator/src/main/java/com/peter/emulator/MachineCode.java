package com.peter.emulator;

public class MachineCode {

    public static final int MASK_INSTRUCTION = 0xff00_0000;

    public static final int HALT = 0xff << 24;

    public static final int NO_OP = 0x00 << 24;

    public static final int LOAD = 0x01 << 24;
    public static final int MASK_LOAD_RG = 0x00ff_0000;
    public static final int MASK_LOAD_MEM = 0x0000_0100;
    public static final int MASK_LOAD_RA = 0x0000_00ff;

    public static final int STORE = 0x02 << 24;
    public static final int MASK_STORE_RG = 0x00ff_0000;
    public static final int MASK_STORE_OP = 0x0000_ff00;
    public static final int MASK_STORE_RA = 0x0000_00ff;
    public static final int STORE_MEM = 0x0000_0100;
    public static final int STORE_VAL = 0x0000_0200;
    public static final int STORE_MEM_COPY = 0x0000_0300;
    
    public static final int MATH = 0x04 << 24;
    public static final int MASK_MATH_OP = 0x00f0_0000;
    public static final int MASK_MATH_RD = 0x000f_0000;
    public static final int MASK_MATH_RA = 0x0000_ff00;
    public static final int MASK_MATH_RB = 0x0000_00ff;
    public static final int MASK_MATH_INC = 0x0000_ffff;
    public static final int MATH_ADD = 0x10 << 16;
    public static final int MATH_SUB = 0x20 << 16;
    public static final int MATH_INC = 0x30 << 16;
    public static final int MATH_AND = 0x40 << 16;
    public static final int MATH_OR = 0x50 << 16;
    public static final int MATH_NAND = 0x60 << 16;
    public static final int MATH_NOR = 0x70 << 16;
    public static final int MATH_NOT = 0x80 << 16;
    public static final int MATH_XOR = 0x90 << 16;
    public static final int MATH_LSHIFT = 0xa0 << 16;
    public static final int MATH_RSHIFT = 0xb0 << 16;
    public static final int MATH_MUL = 0xc0 << 16;
    
    public static final int GOTO = 0x05 << 24;
    public static final int MASK_GOTO_OP = 0x00ff_0000;
    public static final int MASK_GOTO_RA = 0x0000_ff00;
    public static final int MASK_GOTO_RG = 0x0000_00ff;
    public static final int GOTO_UNCD = 0x00 << 16;
    public static final int GOTO_EQ_ZERO = 0x01 << 16;
    public static final int GOTO_LEQ_ZERO = 0x02 << 16;
    public static final int GOTO_GT_ZERO = 0x03 << 16;
    public static final int GOTO_NOT_ZERO = 0x04 << 16;
    public static final int GOTO_REL_UNCD = 0x08 << 16;
    public static final int GOTO_REL_EQ_ZERO = 0x09 << 16;
    public static final int GOTO_REL_LEQ_ZERO = 0x0a << 16;
    public static final int GOTO_REL_GT_ZERO = 0x0b << 16;
    public static final int GOTO_REL_NOT_ZERO = 0x0c << 16;
    public static final int GOTO_PUSH_UNCD = 0x10 << 16;
    public static final int GOTO_PUSH_EQ_ZERO = 0x11 << 16;
    public static final int GOTO_PUSH_LEQ_ZERO = 0x12 << 16;
    public static final int GOTO_PUSH_GT_ZERO = 0x13 << 16;
    public static final int GOTO_PUSH_NOT_ZERO = 0x14 << 16;
    public static final int GOTO_PUSH_REL_UNCD = 0x18 << 16;
    public static final int GOTO_PUSH_REL_EQ_ZERO = 0x19 << 16;
    public static final int GOTO_PUSH_REL_LEQ_ZERO = 0x1a << 16;
    public static final int GOTO_PUSH_REL_GT_ZERO = 0x1b << 16;
    public static final int GOTO_PUSH_REL_NOT_ZERO = 0x1c << 16;
    public static final int GOTO_POP_UNCD = 0x20 << 16;
    public static final int GOTO_POP_EQ_ZERO = 0x21 << 16;
    public static final int GOTO_POP_LEQ_ZERO = 0x22 << 16;
    public static final int GOTO_POP_GT_ZERO = 0x23 << 16;
    public static final int GOTO_POP_NOT_ZERO = 0x24 << 16;
    
    public static final int SET = 0x06 << 24;
    public static final int MASK_SET_OP = 0x000f_0000;
    public static final int MASK_SET_RD = 0x0000_ff00;
    public static final int MASK_SET_RG = 0x0000_00ff;
    public static final int SET_FORCED = 0x0010_0000;

    public static final int STACK = 0x10 << 24;
    public static final int MASK_STACK_RG = 0x00ff_0000;
    public static final int MASK_STACK_OP = 0x0000_ff00;
    public static final int MASK_STACK_VAL = 0x0000_00ff;
    public static final int STACK_PUSH = 0x0000_0000;
    public static final int STACK_POP = 0x0000_0100;
    public static final int STACK_INC = 0x0000_0200;
    public static final int STACK_DEC = 0x0000_0300;

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
    
    public static final int REG_PID = 0xf8;
    public static final int REG_MEM_TABLE = 0xf9;

    public static final int REG_INTERRUPT = 0xfa;
    public static final int REG_INTR_HANDLER = 0xfb;
    
    public static final int REG_CPU_ID = 0xfe;
    public static final int REG_PRIVILEGED_MODE = 0xff;
    
    public static final int REG_PGM_PNTR_I = 0xe0;
    public static final int REG_STACK_PNTR_I = 0xe1;
    
    public static final int REG_PID_I = 0xe8;
    public static final int REG_MEM_TABLE_I = 0xe9;

    public static final int REG_PRIVILEGED_MODE_I = 0xef;

    public static String translateReg(int reg) {
        return switch (reg) {
            case REG_PGM_PNTR -> "rPgm";
            case REG_STACK_PNTR -> "rStack";
            
            case REG_PID -> "rPID";
            case REG_MEM_TABLE -> "rMTbl";
            
            case REG_PRIVILEGED_MODE -> "rPM";
            
            case REG_INTERRUPT -> "rIC";
            case REG_INTR_HANDLER -> "rIH";

            case REG_CPU_ID -> "rID";
            
            case REG_PGM_PNTR_I -> "rPgmI";
            case REG_STACK_PNTR_I -> "rStackI";
            
            case REG_PID_I -> "rPIDI";
            case REG_MEM_TABLE_I -> "rMTblI";
            
            case REG_PRIVILEGED_MODE_I -> "rPMI";
        
            default -> reg < 0x10 ? String.format("r%d", reg) : String.format("r%dI", reg-0x10);
        };
    }

    public static String translate(int instruction, int next) {
        return translate(instruction, String.format("0x%x", next));
    }

    public static String translate(int instruction) {
        return translate(instruction, "next");
    }

    public static String translate(int instruction, String next) {
        int instr = instruction & MASK_INSTRUCTION;
        switch (instr) {
            case HALT -> {
                return "HALT";
            }
            case NO_OP -> {
                return "NO OP";
            }
            case LOAD -> {
                if ((instruction & MASK_LOAD_MEM) != 0) {
                    return String.format("LOAD mem[%s] -> %s", translateReg(instruction & MASK_LOAD_RA),
                            translateReg((instruction & MASK_LOAD_RG) >> 16));
                } else {
                    return String.format("LOAD (%s) -> %s", next, translateReg((instruction & MASK_LOAD_RG) >> 16));
                }
            }
            case STORE -> {
                int op = instruction & MASK_STORE_OP;
                return switch (op) {
                    case STORE_MEM -> String.format("STORE %s -> mem[%s]", translateReg((instruction & MASK_STORE_RG) >> 16),
                            translateReg(instruction & MASK_STORE_RA));
                    case STORE_VAL -> String.format("STORE (%s) -> mem[%s]", next, translateReg(instruction & MASK_STORE_RA));
                    case STORE_MEM_COPY -> String.format("COPY mem[%s] -> mem[%s]", translateReg((instruction & MASK_STORE_RG) >> 16),
                            translateReg(instruction & MASK_STORE_RA));
                    default -> String.format("COPY %s -> %s", translateReg((instruction & MASK_STORE_RG) >> 16),
                            translateReg(instruction & MASK_STORE_RA));
                };
            }
            case MATH -> {
                int op = instruction & MASK_MATH_OP;
                String rd = translateReg((instruction & MASK_MATH_RD) >> 16);
                String ra = translateReg((instruction & MASK_MATH_RA) >> 8);
                String rb = translateReg(instruction & MASK_MATH_RB);
                int inc = instruction & MASK_MATH_INC;
                if ((inc & 0x8000) != 0) {
                    inc &= 0x7fff;
                    inc *= -1;
                } else {
                    inc += 1;
                }
                return switch (op) {
                    case MATH_ADD -> String.format("ADD %s = %s + %s", rd, ra, rb);
                    case MATH_SUB -> String.format("SUB %s = %s - %s", rd, ra, rb);
                    case MATH_INC -> String.format("INC %s = %s + %d", rd, rd, inc);
                    case MATH_AND -> String.format("AND %s = %s & %s", rd, ra, rb);
                    case MATH_OR -> String.format("OR %s = %s | %s", rd, ra, rb);
                    case MATH_NAND -> String.format("NAND %s = %s !& %s", rd, ra, rb);
                    case MATH_NOR -> String.format("NOR %s = %s !| %s", rd, ra, rb);
                    case MATH_NOT -> String.format("NOT %s = ~%s", rd, ra);
                    case MATH_XOR -> String.format("XOR %s = %s ^ %s", rd, ra, rb);
                    case MATH_LSHIFT -> String.format("SHIFT %s = %s << %d", rd, ra, instruction & MASK_MATH_RB);
                    case MATH_RSHIFT -> String.format("SHIFT %s = %s >> %d", rd, ra, instruction & MASK_MATH_RB);
                    case MATH_MUL -> String.format("MUL %s = %s * %d", rd, ra, instruction & MASK_MATH_RB);
                    
                    default -> String.format("MATH (%x)", instruction);
                }
                ;
            }
            case GOTO -> {
                int op = instruction & MASK_GOTO_OP;
                int raV = (instruction & MASK_GOTO_RA) >> 8;
                String ra = translateReg(raV);
                String rg = translateReg(instruction & MASK_GOTO_RG);
                return switch (op) {
                    case GOTO_UNCD -> String.format("GOTO %s", ra);
                    case GOTO_REL_UNCD -> String.format("GOTO pPtr + %d", int8(raV));
                    case GOTO_PUSH_UNCD -> String.format("GOTO PUSH %s", ra);
                    case GOTO_PUSH_REL_UNCD -> String.format("GOTO PUSH pPtr + %d", int8(raV));
                    case GOTO_POP_UNCD -> String.format("GOTO POP");

                    case GOTO_EQ_ZERO -> String.format("GOTO %s ? %s == 0", ra, rg);
                    case GOTO_REL_EQ_ZERO -> String.format("GOTO pPtr + %d ? %s == 0", int8(raV), rg);
                    case GOTO_PUSH_EQ_ZERO -> String.format("GOTO PUSH %s ? %s == 0", ra, rg);
                    case GOTO_PUSH_REL_EQ_ZERO -> String.format("GOTO PUSH pPtr + %d ? %s == 0", int8(raV), rg);
                    case GOTO_POP_EQ_ZERO -> String.format("GOTO POP ? %s == 0", rg);

                    case GOTO_LEQ_ZERO -> String.format("GOTO %s ? %s <= 0", ra, rg);
                    case GOTO_REL_LEQ_ZERO -> String.format("GOTO pPtr + %d ? %s <= 0", int8(raV), rg);
                    case GOTO_PUSH_LEQ_ZERO -> String.format("GOTO PUSH %s ? %s <= 0", ra, rg);
                    case GOTO_PUSH_REL_LEQ_ZERO -> String.format("GOTO PUSH pPtr + %d ? %s <= 0", int8(raV), rg);
                    case GOTO_POP_LEQ_ZERO -> String.format("GOTO POP ? %s <= 0", rg);

                    case GOTO_GT_ZERO -> String.format("GOTO %s ? %s > 0", ra, rg);
                    case GOTO_REL_GT_ZERO -> String.format("GOTO pPtr + %d ? %s > 0", int8(raV), rg);
                    case GOTO_PUSH_GT_ZERO -> String.format("GOTO PUSH %s ? %s > 0", ra, rg);
                    case GOTO_PUSH_REL_GT_ZERO -> String.format("GOTO PUSH pPtr + %d ? %s > 0", int8(raV), rg);
                    case GOTO_POP_GT_ZERO -> String.format("GOTO POP ? %s > 0", rg);

                    case GOTO_NOT_ZERO -> String.format("GOTO %s ? %s != 0", ra, rg);
                    case GOTO_REL_NOT_ZERO -> String.format("GOTO pPtr + %d ? %s != 0", int8(raV), rg);
                    case GOTO_PUSH_NOT_ZERO -> String.format("GOTO PUSH %s ? %s != 0", ra, rg);
                    case GOTO_PUSH_REL_NOT_ZERO -> String.format("GOTO PUSH pPtr + %d ? %s != 0", int8(raV), rg);
                    case GOTO_POP_NOT_ZERO -> String.format("GOTO POP ? %s != 0", rg);

                    default -> String.format("GOTO (%x)", instruction);
                };
            }
            case SET -> {
                int op = instruction & MASK_SET_OP;
                int rdV = (instruction & MASK_SET_RD) >> 8;
                String rd = translateReg(rdV);
                String rg = translateReg(instruction & MASK_SET_RG);
                String forced = ((instruction & SET_FORCED) != 0) ? "FORCED " : "";
                return switch (op) {
                    case GOTO_EQ_ZERO -> String.format("SET %s%s ? %s == 0", forced, rd, rg);
                    case GOTO_LEQ_ZERO -> String.format("SET %s%s ? %s <= 0", forced, rd, rg);
                    case GOTO_GT_ZERO -> String.format("SET %s%s ? %s > 0", forced, rd, rg);
                    case GOTO_NOT_ZERO -> String.format("SET %s%s ? %s != 0", forced, rd, rg);

                    default -> String.format("SET (%x)", instruction);
                };
            }
            case STACK -> {
                return switch(instruction & MASK_STACK_OP) {
                    case (STACK_PUSH) -> String.format("STACK PUSH <- %s", translateReg((instruction & MASK_STACK_RG) >> 16));
                    case (STACK_POP) -> String.format("STACK POP -> %s", translateReg((instruction & MASK_STACK_RG) >> 16));
                    
                    case (STACK_INC) -> String.format("STACK INC %d", (instruction & MASK_STACK_VAL) + 1);
                    case (STACK_DEC) -> String.format("STACK DEC %d", (instruction & MASK_STACK_VAL) + 1);

                    default -> String.format("STACK 0x%s", Integer.toHexString((instruction & MASK_STACK_OP) >> 16));
                };
            }
            case SYSCALL -> {
                int option = instruction & MASK_SYSCALL_OPTION;
                switch (option) {
                    case SYSCALL_RETURN -> {
                        return "SYSRETURN";
                    }
                    case SYSCALL_GOTO -> {
                        return String.format("SYSGOTO %s", translateReg(instruction & MASK_SYSCALL_RG));
                    }
                    case SYSCALL_INTERRUPT -> {
                        int iOp = instruction & MASK_SYSCALL_INTERRUPT_OP;
                        return switch (iOp) {
                            case SYSCALL_INTERRUPT_RG -> String.format("INTERRUPT %s", translateReg(instruction & MASK_SYSCALL_RG));
                            case SYSCALL_INTERRUPT_VAL -> String.format("INTERRUPT %s", next);
                            case SYSCALL_INTERRUPT_RET -> "INTERRUPT RET";
                            default -> String.format("INTERRUPT %s", Integer.toHexString(iOp));
                        };
                    }
                    default -> {
                    }
                }
                return String.format("SYSCALL %x", instruction & MASK_SYSCALL_FUNCTION);
            }

            default -> {
                return String.format("Unknown: %x", instruction);
            }
        }
    }
    
    public static int int8(int val) {
        byte v = (byte) val;
        return (int)v;
    }

    public static int uint32ToInt8(int val) {
        if (val < 0) {
            byte v = (byte) val;
            return ((int) v) & 0xff;
        } else {
            return val & 0x7f;
        }
    }

    public static String regDesc(String vN) {
        return switch (vN) {
            case "r0" -> "User register 0";
            case "r1" -> "User register 1";
            case "r2" -> "User register 2";
            case "r3" -> "User register 3";
            case "r4" -> "User register 4";
            case "r5" -> "User register 5";
            case "r6" -> "User register 6";
            case "r7" -> "User register 7";
            case "r8" -> "User register 8";
            case "r9" -> "User register 9";
            case "r10" -> "User register 10";
            case "r11" -> "User register 11";
            case "r12" -> "User register 12";
            case "r13" -> "User register 13";
            case "r14" -> "User register 14";
            case "r15" -> "User register 15";
            
            case "rPgm" -> "Program pointer (next address)";
            case "rStack" -> "Stack pointer";

            case "rPID" -> "Process ID (Kernal set) (PM required)";
            case "rMemTbl" -> "Memory Map Table pointer  (PM required)";
            case "rID" -> "Core ID (Read only)";
            
            case "rIC" -> "Interrupt code (PM required)";
            case "rIH" -> "Interrupt handler pointer (PM required)";

            case "rPM" -> "Privileged Mode (PM required)";
            
            case "r0I" -> "Interrupt stored User register 0";
            case "r1I" -> "Interrupt stored User register 1";
            case "r2I" -> "Interrupt stored User register 2";
            case "r3I" -> "Interrupt stored User register 3";
            case "r4I" -> "Interrupt stored User register 4";
            case "r5I" -> "Interrupt stored User register 5";
            case "r6I" -> "Interrupt stored User register 6";
            case "r7I" -> "Interrupt stored User register 7";
            case "r8I" -> "Interrupt stored User register 8";
            case "r9I" -> "Interrupt stored User register 9";
            case "r10I" -> "Interrupt stored User register 10";
            case "r11I" -> "Interrupt stored User register 11";
            case "r12I" -> "Interrupt stored User register 12";
            case "r13I" -> "Interrupt stored User register 13";
            case "r14I" -> "Interrupt stored User register 14";
            case "r15I" -> "Interrupt stored User register 15";
            
            case "rPgmI" -> "Interrupt stored Program pointer (next address)";
            case "rStackI" -> "Interrupt stored Stack pointer";

            case "rPIDI" -> "Interrupt stored Process ID (Kernal set) (PM required)";
            case "rMemTblI" -> "Interrupt stored Memory Map Table pointer  (PM required)";

            case "rPMI" -> "Interrupt stored Privileged Mode (PM required)";
        
            default -> "Unknown register";
        };
    }
}
