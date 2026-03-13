package com.peter.emulator;

import static com.peter.emulator.MachineCode.*;
import com.peter.emulator.components.MMU;
import com.peter.emulator.components.RAM;
import com.peter.emulator.debug.Debugger;

public class CPU {

    public final int cpuId;

    public final RAM ram;
    public final MMU mmu;
    
    public boolean running = false;

    // r0-r15
    public final int[] registers = new int[0x10];
    // r0i-r15i
    public final int[] registersI = new int[0x10];
    // rPgm
    public int pgmPtr = 0;
    // rPgmI
    public int pgmPtrI = 0;
    // rStack
    public int stackPtr = 0x1000;
    // rStackI
    public int stackPtrI = 0x1000;
    // rPID
    public int pid = 0;
    // rPIDI
    public int pidI = 0;
    // rMemTbl
    public int memTablePtr = 0;
    // rMemTblI
    public int memTablePtrI = 0;

    // rPM
    public boolean privilegeMode = true;
    // rPMI
    public boolean privilegeModeI = true;

    // rIC
    public int interruptCode = 0;
    // rIH
    public int interruptHandler = 0;

    public Debugger debugger = null;

    public CPU(int cpuId, RAM ram, MMU mmu) {
        this.cpuId = cpuId;
        this.ram = ram;
        this.mmu = mmu;
    }

    public void setPtr(int ptr) {
        pgmPtr = ptr;
    }

    public int getReg(int reg) {
        if (reg < 0x10) {
            return registers[reg];
        } else if (reg < 0x20) {
            return registersI[reg];
        }
        return switch (reg) {
            case REG_PGM_PNTR -> pgmPtr;
            case REG_STACK_PNTR -> stackPtr;

            case REG_PID -> pid;
            case REG_MEM_TABLE -> memTablePtr;

            case REG_INTERRUPT -> interruptCode;
            case REG_INTR_HANDLER -> interruptHandler;
            
            case REG_CPU_ID -> cpuId;
            case REG_PRIVILEGED_MODE -> privilegeMode ? 1 : 0;
            
            case REG_PGM_PNTR_I -> pgmPtrI;
            case REG_STACK_PNTR_I -> stackPtrI;

            case REG_PID_I -> pidI;
            case REG_MEM_TABLE_I -> memTablePtrI;

            case REG_PRIVILEGED_MODE_I -> privilegeModeI ? 1 : 0;
        
            default -> {
                throw new RuntimeException("Invalid special register");
            }
        };
    }

    public void setReg(int reg, int val) {
        // System.out.println(String.format("Setting register %x to %x", reg, val));
        if (reg < 0x10) {
            registers[reg] = val;
            return;
        }
        if (reg < 0x20) {
            if (!privilegeMode) {
                interrupt(0x8000_0001);
                return;
            }
            registersI[reg] = val;
            return;
        }
        switch (reg) {
            case REG_PGM_PNTR -> {
                pgmPtr = val;
            }
            case REG_STACK_PNTR -> {
                stackPtr = val;
            }
            case REG_PID -> {
                if (!privilegeMode) {
                    interrupt(0x8000_0001);
                    return;
                }
                pid = val;
            }
            case REG_MEM_TABLE -> {
                if (!privilegeMode) {
                    interrupt(0x8000_0001);
                    return;
                }
                memTablePtr = val;
            }
            case REG_PRIVILEGED_MODE -> {
                if (!privilegeMode) {
                    interrupt(0x8000_0001);
                    return;
                }
                privilegeMode = val != 0;
            }
            case REG_INTERRUPT -> {
                if (!privilegeMode) {
                    interrupt(0x8000_0001);
                    return;
                }
                interruptCode = val;
            }
            case REG_INTR_HANDLER -> {
                if (!privilegeMode) {
                    interrupt(0x8000_0001);
                    return;
                }
                interruptHandler = val;
            }

            case REG_CPU_ID -> {
                interrupt(0x8000_0001);
            }
            
            case REG_PGM_PNTR_I -> {
                if (!privilegeMode) {
                    interrupt(0x8000_0001);
                    return;
                }
                pgmPtrI = val;
            }
            case REG_STACK_PNTR_I -> {
                if (!privilegeMode) {
                    interrupt(0x8000_0001);
                    return;
                }
                stackPtrI = val;
            }
            case REG_PID_I -> {
                if (!privilegeMode) {
                    interrupt(0x8000_0001);
                    return;
                }
                pidI = val;
            }
            case REG_MEM_TABLE_I -> {
                if (!privilegeMode) {
                    interrupt(0x8000_0001);
                    return;
                }
                memTablePtrI = val;
            }
            case REG_PRIVILEGED_MODE_I -> {
                if (!privilegeMode) {
                    interrupt(0x8000_0001);
                    return;
                }
                privilegeModeI = val != 0;
            }

            default -> {
                throw new RuntimeException("Invalid special register");
            }
        }

    }

    public void writeMem(int addr, int val) {
        ram.writeWord(mmu.translate(this, addr), val);
    }
    public int readMem(int addr) {
        return ram.readWord(mmu.translate(this, addr));
    }
    
    public void stackPush(int val) {
        // System.out.println(String.format("\t Stack push: [%x] %x", stackPtr, val));
        writeMem(stackPtr, val);
        stackPtr += 4;
    }

    public int stackPop() {
        stackPtr -= 4;
        int val = readMem(stackPtr);
        // System.out.println(String.format("\t Stack pop: [%x] %x", stackPtr, val));
        return val;
    }

    public void interrupt(int code) {
        interruptCode = code;
    }

    public int instr;
    public int instrb;
    public boolean inInterrupt = false;
    public void tick() {
        if (!running)
            return;

        if (interruptCode != 0 && !inInterrupt) {
            inInterrupt = true;
            System.err.println("Interrupt: "+interruptCode);
            for (int i = 0; i <= 0xf; i++) {
                registersI[i] = registers[i];
                registers[i] = 0;
            }
            stackPtrI = stackPtr;
            memTablePtrI = memTablePtr;
            pidI = pid;
            privilegeModeI = privilegeMode;
            pgmPtrI = pgmPtr;

            privilegeMode = true;
            pgmPtr = interruptHandler;
            return;
        }

        String dbg = "";
        if (debugger != null) {
            debugger.update(this);
            dbg = debugger.getSymbol(this);
        }
        int op = readMem(pgmPtr);
        pgmPtr += 4;
        instr = op;
        int next = readMem(pgmPtr);
        instrb = next;
        int instruction = op & MASK_INSTRUCTION;
        String instrStr = translate(op, next);
        System.out.println(String.format("CPU Tick: [%x] %s", mmu.translate(this, pgmPtr - 4), instrStr));
        switch (instruction) {
            case HALT -> {
                if (!privilegeMode)
                    return;
                running = false;
            }
            case LOAD -> {
                int rg = (op & MASK_LOAD_RG) >> 16;
                boolean mem = (op & MASK_LOAD_MEM) != 0;
                int ra = (op & MASK_LOAD_RA);
                int val;
                if (mem) {
                    val = readMem(getReg(ra));
                } else {
                    val = next;
                    pgmPtr += 4;
                }
                setReg(rg, val);
            }
            case STORE -> {
                int rg = (op & MASK_STORE_RG) >> 16;
                int sOp = op & MASK_STORE_OP;
                boolean mem = sOp == STORE_MEM;
                boolean val = sOp == STORE_VAL;
                boolean memCopy = sOp == STORE_MEM_COPY;
                int ra = (op & MASK_STORE_RA);
                if (mem) {
                    writeMem(getReg(ra), getReg(rg));
                } else if (val) {
                    pgmPtr += 4;
                    writeMem(getReg(ra), next);
                } else if (memCopy) {
                    writeMem(getReg(ra), readMem(getReg(rg)));
                } else {
                    setReg(ra, getReg(rg));
                }
            }
            case MATH -> {
                int mOp = op & MASK_MATH_OP;
                int rd = (op & MASK_MATH_RD) >> 16;
                int ra = (op & MASK_MATH_RA) >> 8;
                int rb = (op & MASK_MATH_RB);
                switch (mOp) {
                    case MATH_ADD -> {
                        setReg(rd, getReg(ra) + getReg(rb));
                    }
                    case MATH_SUB -> {
                        setReg(rd, getReg(ra) - getReg(rb));
                    }
                    case MATH_MUL -> {
                        setReg(rd, getReg(ra) * getReg(rb));
                    }
                    case MATH_INC -> {
                        int inc = op & MASK_MATH_INC;
                        if ((inc & 0x8000) != 0) {
                            setReg(rd, getReg(rd) - (inc & 0x7fff));
                        } else {
                            setReg(rd, getReg(rd) + inc + 1);
                        }
                    }
                    case MATH_AND -> {
                        setReg(rd, getReg(ra) & getReg(rb));
                    }
                    case MATH_OR -> {
                        setReg(rd, getReg(ra) | getReg(rb));
                    }
                    case MATH_NAND -> {
                        setReg(rd, ~(getReg(ra) & getReg(rb)));
                    }
                    case MATH_NOR -> {
                        setReg(rd, ~(getReg(ra) | getReg(rb)));
                    }
                    case MATH_NOT -> {
                        setReg(rd, ~getReg(ra));
                    }
                    case MATH_XOR -> {
                        setReg(rd, getReg(ra) ^ getReg(rb));
                    }
                    case MATH_LSHIFT -> {
                        setReg(rd, getReg(ra) << getReg(rb));
                    }
                    case MATH_RSHIFT -> {
                        setReg(rd, getReg(ra) >> getReg(rb));
                    }
                }
            }
            case GOTO -> {
                int type = op & MASK_GOTO_OP;
                int ra = (op & MASK_GOTO_RA) >> 8;
                int rg = op & MASK_GOTO_RG;
                switch (type) {
                    case GOTO_UNCD -> {
                        pgmPtr = getReg(ra);
                    }
                    case GOTO_EQ_ZERO -> {
                        if (getReg(rg) == 0) {
                            pgmPtr = getReg(ra);
                        }
                    }
                    case GOTO_LEQ_ZERO -> {
                        if (getReg(rg) <= 0) {
                            pgmPtr = getReg(ra);
                        }
                    }
                    case GOTO_GT_ZERO -> {
                        if (getReg(rg) > 0) {
                            pgmPtr = getReg(ra);
                        }
                    }
                    case GOTO_NOT_ZERO -> {
                        if (getReg(rg) != 0) {
                            pgmPtr = getReg(ra);
                        }
                    }

                    case GOTO_REL_UNCD -> {
                        pgmPtr += 4;
                        pgmPtr += next;
                    }
                    case GOTO_REL_EQ_ZERO -> {
                        pgmPtr += 4;
                        if (getReg(rg) == 0) {
                            pgmPtr += next;
                        }
                    }
                    case GOTO_REL_LEQ_ZERO -> {
                        pgmPtr += 4;
                        if (getReg(rg) <= 0) {
                            pgmPtr += next;
                        }
                    }
                    case GOTO_REL_GT_ZERO -> {
                        pgmPtr += 4;
                        if (getReg(rg) > 0) {
                            pgmPtr += next;
                        }
                    }
                    case GOTO_REL_NOT_ZERO -> {
                        pgmPtr += 4;
                        if (registers[rg] != 0) {
                            pgmPtr += next;
                        }
                    }

                    case GOTO_PUSH_UNCD -> {
                        stackPush(pgmPtr);
                        pgmPtr = getReg(ra);
                    }
                    case GOTO_PUSH_EQ_ZERO -> {
                        if (getReg(rg) == 0) {
                            stackPush(pgmPtr);
                            pgmPtr = getReg(ra);
                        }
                    }
                    case GOTO_PUSH_LEQ_ZERO -> {
                        if (getReg(rg) <= 0) {
                            stackPush(pgmPtr);
                            pgmPtr = getReg(ra);
                        }
                    }
                    case GOTO_PUSH_GT_ZERO -> {
                        if (getReg(rg) > 0) {
                            stackPush(pgmPtr);
                            pgmPtr = getReg(ra);
                        }
                    }
                    case GOTO_PUSH_NOT_ZERO -> {
                        if (getReg(rg) != 0) {
                            stackPush(pgmPtr);
                            pgmPtr = getReg(ra);
                        }
                    }

                    case GOTO_PUSH_REL_UNCD -> {
                        pgmPtr += 4;
                        stackPush(pgmPtr);
                            pgmPtr += next;
                    }
                    case GOTO_PUSH_REL_EQ_ZERO -> {
                        pgmPtr += 4;
                        if (getReg(rg) == 0) {
                            stackPush(pgmPtr);
                            pgmPtr += next;
                        }
                    }
                    case GOTO_PUSH_REL_LEQ_ZERO -> {
                        pgmPtr += 4;
                        if (getReg(rg) <= 0) {
                            stackPush(pgmPtr);
                            pgmPtr += next;
                        }
                    }
                    case GOTO_PUSH_REL_GT_ZERO -> {
                        pgmPtr += 4;
                        if (getReg(rg) > 0) {
                            stackPush(pgmPtr);
                            pgmPtr += next;
                        }
                    }
                    case GOTO_PUSH_REL_NOT_ZERO -> {
                        pgmPtr += 4;
                        if (registers[rg] != 0) {
                            stackPush(pgmPtr);
                            pgmPtr += next;
                        }
                    }

                    case GOTO_POP_UNCD -> {
                        pgmPtr = stackPop();
                    }
                    case GOTO_POP_EQ_ZERO -> {
                        if (getReg(rg) == 0) {
                            pgmPtr = stackPop();
                        }
                    }
                    case GOTO_POP_LEQ_ZERO -> {
                        if (getReg(rg) <= 0) {
                            pgmPtr = stackPop();
                        }
                    }
                    case GOTO_POP_GT_ZERO -> {
                        if (getReg(rg) > 0) {
                            pgmPtr = stackPop();
                        }
                    }
                    case GOTO_POP_NOT_ZERO -> {
                        if (registers[rg] != 0) {
                            pgmPtr = stackPop();
                        }
                    }
                }
            }
            case SET -> {
                boolean forced = (op & SET_FORCED) != 0;
                int type = op & MASK_SET_OP;
                int rd = (op & MASK_SET_RD) >> 8;
                int rg = op & MASK_SET_RG;
                switch (type) {
                    case GOTO_EQ_ZERO -> {
                        if (getReg(rg) == 0) {
                            setReg(rd, 1);
                        } else if (forced) {
                            setReg(rd, 0);
                        }
                    }
                    case GOTO_LEQ_ZERO -> {
                        if (getReg(rg) <= 0) {
                            setReg(rd, 1);
                        } else if (forced) {
                            setReg(rd, 0);
                        }
                    }
                    case GOTO_GT_ZERO -> {
                        if (getReg(rg) > 0) {
                            setReg(rd, 1);
                        } else if (forced) {
                            setReg(rd, 0);
                        }
                    }
                    case GOTO_NOT_ZERO -> {
                        if (getReg(rg) != 0) {
                            setReg(rd, 1);
                        } else if (forced) {
                            setReg(rd, 0);
                        }
                    }
                }
            }
            case STACK -> {
                int rg = (op & MASK_STACK_RG) >> 16;
                switch(op & MASK_STACK_OP) {
                    case (STACK_PUSH) -> {
                        stackPush(getReg(rg));
                    }
                    case (STACK_POP) -> {
                        setReg(rg, stackPop());
                    }
                    case (STACK_INC) -> {
                        stackPtr += 1 + op & MASK_STACK_VAL;
                    }
                    case (STACK_DEC) -> {
                        stackPtr -= 1 + op & MASK_STACK_VAL;
                    }
                }
            }
            case SYSCALL -> {
                int option = op & MASK_SYSCALL_OPTION;
                switch (option) {
                    case SYSCALL_RETURN -> {
                        // SYSRETURN
                        if (!privilegeMode) {
                            return;
                        }
                        int ptr = readMem(0x1_0000);
                        pgmPtr = ptr;
                        privilegeMode = false;
                    }
                    case SYSCALL_GOTO -> {
                        // SYSGOTO
                        if (!privilegeMode) {
                            return;
                        }
                        pgmPtr = getReg(op & MASK_SYSCALL_RG);
                        privilegeMode = false;
                    }
                    case SYSCALL_INTERRUPT -> {
                        int iOp = op & MASK_SYSCALL_INTERRUPT_OP;
                        if (iOp == SYSCALL_INTERRUPT_RET) {
                            if (!privilegeMode)
                                return;
                            for (int i = 0; i <= 0xf; i++) {
                                registers[i] = registersI[i];
                            }
                            stackPtr = stackPtrI;
                            memTablePtr = memTablePtrI;
                            pid = pidI;
                            privilegeMode = privilegeModeI;
                            pgmPtr = pgmPtrI;
                            
                            inInterrupt = false;
                            System.out.println("Interrupt ret to "+pgmPtr);
                            return;
                        }
                        interrupt(iOp == SYSCALL_INTERRUPT_VAL ? next : getReg(op & MASK_SYSCALL_RG));
                    }
                    default -> {
                        privilegeMode = true;
                        int function = op & MASK_SYSCALL_FUNCTION;
                        int ptr = readMem((function<<2) + 0x1_0000);
                        if (ptr == 0xffff_ffff) {
                            running = false;
                            // TODO: interrupt?
                            throw new RuntimeException(String.format("Unknown syscall: 0x%x", function));
                        }
                        writeMem(0x1_0000, pgmPtr);
                        pgmPtr = ptr;
                    }
                }
            }
        }
    }

    private String toHex(int num) {
        String str = String.format("%x", num);
        while (str.length() < 8) {
            str = "0" + str;
        }
        return str.substring(0,4)+"_"+str.substring(4);
    }

    public String dump() {
        String out = "r0";
        String vl = "";
        for (int i = 0; i <= 0xf; i++) {
            out += MachineCode.translateReg(i) + "         ";
            vl += toHex(registers[i]) + "  ";
        }
        out += "\n" + vl;
        return out;
    }
    
    public void reset() {
        pgmPtr = 0;
        pgmPtrI = 0;
        inInterrupt = false;
        interruptCode = 0;
        interruptHandler = 0;
        privilegeMode = true;
        privilegeModeI = false;
        memTablePtr = 0;
        memTablePtrI = 1;
        stackPtr = 0x1000;
        stackPtrI = 0x1000;
        pid = 0;
        pidI = 0;
        instr = 0;
        instrb = 0;
        for (int i = 0; i < registers.length; i++) {
            registers[i] = 0;
            registersI[i] = 0;
        }
        running = true;
    }
}
