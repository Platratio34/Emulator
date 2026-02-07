package com.peter.emulator;

import static com.peter.emulator.MachineCode.*;
import com.peter.emulator.components.MMU;
import com.peter.emulator.components.RAM;
import com.peter.emulator.debug.Debugger;

public class CPU {

    public final int[] registers = new int[0x20];

    public final RAM ram;
    public final MMU mmu;

    public int pgmPtr = 0;
    public int stackPtr = 0x1000;
    public int pid = 0;
    public int memTablePtr = 0;
    
    public boolean running = false;

    public boolean privilegeMode = true;

    public int interruptCode = 0;
    public int interruptRsp = 0;

    public Debugger debugger = null;

    public CPU(RAM ram, MMU mmu) {
        this.ram = ram;
        this.mmu = mmu;
    }

    public void setPtr(int ptr) {
        pgmPtr = ptr;
    }

    public int getReg(int reg) {
        if (reg < 0xf0) {
            return registers[reg];
        }
        return switch (reg) {
            case REG_PGM_PNTR -> pgmPtr;
            case REG_STACK_PNTR -> stackPtr;

            case REG_PID -> pid;
            case REG_MEM_TABLE -> memTablePtr;

            case REG_PRIVILEGED_MODE -> privilegeMode ? 1 : 0;
            case REG_INTERRUPT -> interruptCode;
            case REG_INTR_RSP -> interruptRsp;
        
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
            case REG_INTR_RSP -> {
                if (!privilegeMode) {
                    interrupt(0x8000_0001);
                    return;
                }
                interruptRsp = val;
            }

            default -> {
                throw new RuntimeException("Invalid special register");
            }
        }

    }

    public void writeMem(int addr, int val) {
        ram.write(mmu.translate(this, addr), val);
    }
    public int readMem(int addr) {
        return ram.read(mmu.translate(this, addr));
    }
    
    public void stackPush(int val) {
        // System.out.println(String.format("\t Stack push: [%x] %x", stackPtr, val));
        writeMem(stackPtr++, val);
    }

    public int stackPop() {
        stackPtr--;
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
            stackPush(pgmPtr);
            stackPush(getReg(REG_PRIVILEGED_MODE));
            for (int i = 0; i <= 0xf; i++)
                stackPush(registers[i]);
            privilegeMode = true;
            pgmPtr = interruptRsp;
            return;
        }

        String dbg = "";
        if (debugger != null) {
            debugger.update(this);
            dbg = debugger.getSymbol(this);
        }
        int op = readMem(pgmPtr++);
        instr = op;
        int next = readMem(pgmPtr);
        instrb = next;
        int instruction = op & MASK_INSTRUCTION;
        String instrStr = translate(op, next);
        System.out.println(String.format("CPU Tick: [%x] %s", mmu.translate(this, pgmPtr - 1), instrStr));
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
                    pgmPtr++;
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
                    int v = next;
                    pgmPtr++;
                    writeMem(getReg(ra), v);
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
                            inc &= 0x7fff;
                            inc *= -1;
                            inc -= 1;
                        } else {
                            inc += 1;
                        }
                        setReg(rd, getReg(rd) + inc);
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
                int ro = op & MASK_GOTO_RO;
                switch (type) {
                    case GOTO_UNCD -> {
                        pgmPtr = getReg(ra);
                    }
                    case GOTO_EQ_ZERO -> {
                        if (getReg(ro) == 0) {
                            pgmPtr = getReg(ra);
                        }
                    }
                    case GOTO_LEQ_ZERO -> {
                        if (getReg(ro) <= 0) {
                            pgmPtr = getReg(ra);
                        }
                    }
                    case GOTO_GT_ZERO -> {
                        if (getReg(ro) > 0) {
                            pgmPtr = getReg(ra);
                        }
                    }
                    case GOTO_NOT_ZERO -> {
                        if (getReg(ro) != 0) {
                            pgmPtr = getReg(ra);
                        }
                    }

                    case GOTO_REL_UNCD -> {
                        pgmPtr += int8(ra);
                    }
                    case GOTO_REL_EQ_ZERO -> {
                        if (getReg(ro) == 0) {
                            pgmPtr += int8(ra);
                        }
                    }
                    case GOTO_REL_LEQ_ZERO -> {
                        if (getReg(ro) <= 0) {
                            pgmPtr += int8(ra);
                        }
                    }
                    case GOTO_REL_GT_ZERO -> {
                        if (getReg(ro) > 0) {
                            pgmPtr += int8(ra);
                        }
                    }
                    case GOTO_REL_NOT_ZERO -> {
                        if (registers[ro] != 0) {
                            pgmPtr += int8(ra);
                        }
                    }

                    case GOTO_PUSH_UNCD -> {
                        stackPush(pgmPtr);
                        pgmPtr = getReg(ra);
                    }
                    case GOTO_PUSH_EQ_ZERO -> {
                        if (getReg(ro) == 0) {
                            stackPush(pgmPtr);
                            pgmPtr = getReg(ra);
                        }
                    }
                    case GOTO_PUSH_LEQ_ZERO -> {
                        if (getReg(ro) <= 0) {
                            stackPush(pgmPtr);
                            pgmPtr = getReg(ra);
                        }
                    }
                    case GOTO_PUSH_GT_ZERO -> {
                        if (getReg(ro) > 0) {
                            stackPush(pgmPtr);
                            pgmPtr = getReg(ra);
                        }
                    }
                    case GOTO_PUSH_NOT_ZERO -> {
                        if (getReg(ro) != 0) {
                            stackPush(pgmPtr);
                            pgmPtr = getReg(ra);
                        }
                    }

                    case GOTO_PUSH_REL_UNCD -> {
                        stackPush(pgmPtr);
                        pgmPtr += int8(ra);
                    }
                    case GOTO_PUSH_REL_EQ_ZERO -> {
                        if (getReg(ro) == 0) {
                            stackPush(pgmPtr);
                            pgmPtr += int8(ra);
                        }
                    }
                    case GOTO_PUSH_REL_LEQ_ZERO -> {
                        if (getReg(ro) <= 0) {
                            stackPush(pgmPtr);
                            pgmPtr += int8(ra);
                        }
                    }
                    case GOTO_PUSH_REL_GT_ZERO -> {
                        if (getReg(ro) > 0) {
                            stackPush(pgmPtr);
                            pgmPtr += int8(ra);
                        }
                    }
                    case GOTO_PUSH_REL_NOT_ZERO -> {
                        if (registers[ro] != 0) {
                            stackPush(pgmPtr);
                            pgmPtr += int8(ra);
                        }
                    }

                    case GOTO_POP_UNCD -> {
                        pgmPtr = stackPop();
                    }
                    case GOTO_POP_EQ_ZERO -> {
                        if (getReg(ro) == 0) {
                            pgmPtr = stackPop();
                        }
                    }
                    case GOTO_POP_LEQ_ZERO -> {
                        if (getReg(ro) <= 0) {
                            pgmPtr = stackPop();
                        }
                    }
                    case GOTO_POP_GT_ZERO -> {
                        if (getReg(ro) > 0) {
                            pgmPtr = stackPop();
                        }
                    }
                    case GOTO_POP_NOT_ZERO -> {
                        if (registers[ro] != 0) {
                            pgmPtr = stackPop();
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
                            for (int i = 0xf; i >= 0x00; i--)
                                registers[i] = stackPop();
                            setReg(REG_PRIVILEGED_MODE, stackPop());
                            pgmPtr = stackPop();
                            inInterrupt = false;
                            System.out.println("Interrupt ret to "+pgmPtr);
                            return;
                        }
                        interrupt(iOp == SYSCALL_INTERRUPT_VAL ? next : getReg(op & MASK_SYSCALL_RG));
                    }
                    default -> {
                        privilegeMode = true;
                        int function = op & MASK_SYSCALL_FUNCTION;
                        int ptr = readMem(function + 0x1_0000);
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
        for(int i = 0; i <= 0xf; i++) {
            out += MachineCode.translateReg(i) + "         ";
            vl += toHex(registers[i]) + "  ";
        }
        out += "\n" + vl;
        return out;
    }
}
