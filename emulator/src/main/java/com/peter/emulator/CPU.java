package com.peter.emulator;

import static com.peter.emulator.MachineCode.*;

import java.util.ArrayDeque;

import com.peter.emulator.MachineCode.ConditionalOperator;
import com.peter.emulator.MachineCode.MathOperator;
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
    protected ArrayDeque<Integer> interruptQueue = new ArrayDeque<>();

    public Debugger debugger = null;
    public boolean printInstr = false;

    public CPU(int cpuId, RAM ram, MMU mmu) {
        this.cpuId = cpuId;
        this.ram = ram;
        this.mmu = mmu;
    }

    public void setPtr(int ptr) {
        pgmPtr = ptr;
    }

    private boolean isValidReg(int reg) {
        return switch(reg) {
            case REG_PGM_PNTR -> true;
            case REG_STACK_PNTR -> true;

            case REG_PID -> true;
            case REG_MEM_TABLE -> true;

            case REG_INTERRUPT -> true;
            case REG_INTR_HANDLER -> true;
            
            case REG_CPU_ID -> true;
            case REG_PRIVILEGED_MODE -> true;
            
            case REG_PGM_PNTR_I -> true;
            case REG_STACK_PNTR_I -> true;

            case REG_PID_I -> true;
            case REG_MEM_TABLE_I -> true;

            case REG_PRIVILEGED_MODE_I -> true;

            default -> reg >= 0 && reg < 0x30;
        };
    }

    public int getReg(int reg) {
        if (reg < 0x10) {
            return registers[reg];
        } else if (reg < 0x20) {
            return registersI[reg & 0xf];
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
    public void writeMemShort(int addr, int val) {
        ram.writeShort(mmu.translate(this, addr), val);
    }
    public void writeMemByte(int addr, byte val) {
        ram.writeByte(mmu.translate(this, addr), val);
    }

    public int readMem(int addr) {
        return ram.readWord(mmu.translate(this, addr));
    }
    public int readMemShort(int addr) {
        return ram.readShort(mmu.translate(this, addr));
    }

    public byte readMemByte(int addr) {
        return ram.readByte(mmu.translate(this, addr));
    }
    
    public int translateAddress(int addr) {
        return mmu.translate(this, addr);
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
        if (interruptCode != 0) {
            interruptQueue.add(code);
            return;
        }
        interruptCode = code;
    }

    public int instr;
    public int instrb;
    public boolean inInterrupt = false;
    public void tick() {
        if (!running)
            return;

        if (interruptCode == 0 && !inInterrupt && !interruptQueue.isEmpty()) {
            interruptCode = interruptQueue.pop();
        }
        
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
        if (printInstr) {
            String instrStr = translate(op, next);
            System.out.println(String.format("CPU Tick: [%x] %s", mmu.translate(this, pgmPtr - 4), instrStr));
        }
        switch (instruction) {
            case HALT -> {
                if (!privilegeMode)
                    return;
                running = false;
            }
            case LOAD -> {
                int rg = (op & MASK_LOAD_RG) >> 16;
                int mode = op & MASK_LOAD_MODE;
                int ra = (op & MASK_LOAD_RA);
                int val;
                if (mode != 0) {
                    switch(mode) {
                        case LOAD_MEM -> {val = readMem(getReg(ra));}
                        case LOAD_MEM_SHORT -> {val = readMemShort(getReg(ra));}
                        case LOAD_MEM_BYTE -> {val = readMemByte(getReg(ra));}
                        default -> {
                            throw new RuntimeException(String.format("Unknown load mode: %20x", mode >> 8));
                        }
                    }
                } else {
                    val = next;
                    pgmPtr += 4;
                }
                setReg(rg, val);
            }
            case STORE -> {
                int rg = (op & MASK_STORE_RG) >> 16;
                int size = op & MASK_STORE_SIZE;
                int source = op & MASK_STORE_SOURCE;
                boolean incRG = (op & MASK_STORE_FLAG_INC_RG) != 0;
                boolean incRA = (op & MASK_STORE_FLAG_INC_RA) != 0;
                int ra = op & MASK_STORE_RA;

                int val = switch(source) {
                    case STORE_SOURCE_REG, STORE_SOURCE_REG_REG -> getReg(rg);
                    case STORE_SOURCE_MEM -> switch(size) {
                        case STORE_SIZE_WORD -> readMem(getReg(rg));
                        case STORE_SIZE_SHORT -> readMemShort(getReg(rg));
                        case STORE_SIZE_BYTE -> readMemByte(getReg(rg));
                        default -> 0;
                    };
                    case STORE_SOURCE_VAL -> next;
                    default -> 0;
                };
                if(source == STORE_SOURCE_VAL)
                    pgmPtr += 4;
                if(source == STORE_SOURCE_REG_REG) {
                    setReg(ra, val);
                } else {
                    switch(size) {
                        case STORE_SIZE_WORD -> writeMem(getReg(ra), val);
                        case STORE_SIZE_SHORT -> writeMemShort(getReg(ra), val);
                        case STORE_SIZE_BYTE -> writeMemByte(getReg(ra), (byte) val);
                    }
                }
                int incSize = switch(size) {
                    case STORE_SIZE_WORD -> 4;
                    case STORE_SIZE_SHORT -> 2;
                    default -> 1;
                };
                if(incRG)
                    setReg(rg, getReg(rg) + incSize);
                if(incRA)
                    setReg(ra, getReg(ra) + incSize);

                // int rg = (op & MASK_STORE_RG) >> 16;
                // int sOp = op & MASK_STORE_OP;
                // int ra = (op & MASK_STORE_RA);
                // switch (sOp) {
                //     case STORE_MEM -> writeMem(getReg(ra), getReg(rg));
                //     case STORE_MEM_SHORT -> writeMemShort(getReg(ra), getReg(rg) & 0xffff);
                //     case STORE_MEM_BYTE -> writeMemByte(getReg(ra), (byte)(getReg(rg) & 0xff));
                //     case STORE_MEM_COPY -> writeMem(getReg(ra), readMem(getReg(rg)));
                //     case STORE_VAL-> {
                //         pgmPtr += 4;
                //         writeMem(getReg(ra), next);
                //     }
                //     default -> setReg(ra, getReg(rg));
                // }
            }
            case MATH -> {
                int rd = (op & MASK_MATH_RD) >> 16;
                int ra = (op & MASK_MATH_RA) >> 8;
                int rb = (op & MASK_MATH_RB);
                switch (MathOperator.fromMachineCode(op)) {
                    case ADD -> {
                        setReg(rd, getReg(ra) + getReg(rb));
                    }
                    case SUB -> {
                        setReg(rd, getReg(ra) - getReg(rb));
                    }
                    case MUL -> {
                        setReg(rd, getReg(ra) * getReg(rb));
                    }
                    case INC -> {
                        int inc = op & MASK_MATH_INC;
                        if ((inc & 0x8000) != 0) {
                            setReg(rd, getReg(rd) - (inc & 0x7fff));
                        } else {
                            setReg(rd, getReg(rd) + inc + 1);
                        }
                    }
                    case AND -> {
                        setReg(rd, getReg(ra) & getReg(rb));
                    }
                    case OR -> {
                        setReg(rd, getReg(ra) | getReg(rb));
                    }
                    case NAND -> {
                        setReg(rd, ~(getReg(ra) & getReg(rb)));
                    }
                    case NOR -> {
                        setReg(rd, ~(getReg(ra) | getReg(rb)));
                    }
                    case NOT -> {
                        setReg(rd, ~getReg(ra));
                    }
                    case XOR -> {
                        setReg(rd, getReg(ra) ^ getReg(rb));
                    }
                    case LSHIFT -> {
                        setReg(rd, getReg(ra) << rb);
                    }
                    case RSHIFT -> {
                        setReg(rd, getReg(ra) >> rb);
                    }
                    case UNKNOWN -> {
                    }
                }
            }
            case GOTO -> {
                boolean rel = (op & MASK_GOTO_REL) != 0;
                boolean push = (op & MASK_GOTO_PUSH) != 0;
                boolean pop = (op & MASK_GOTO_POP) != 0;
                int ra = (op & MASK_GOTO_RA) >> 8;
                int rg = op & MASK_GOTO_RG;
                boolean condVal = switch(ConditionalOperator.fromMachineCode(op)) {
                    case UNCONDITIONAL -> true;
                    case EQ_ZERO -> getReg(rg) == 0;
                    case LEQ_ZERO -> getReg(rg) <= 0;
                    case GT_ZERO -> getReg(rg) > 0;
                    case NEQ_ZERO -> getReg(rg) != 0;
                    case LT_ZERO -> getReg(rg) < 0;
                    case GEQ_ZERO -> getReg(rg) >= 0;
                    case UNKNOWN -> true;
                };
                if (rel)
                    pgmPtr += 4;
                if (condVal) {
                    if (pop) {
                        pgmPtr = stackPop();
                    } else {
                        if (push)
                            stackPush(pgmPtr);
                        if (rel)
                            pgmPtr += next;
                        else
                            pgmPtr = getReg(ra);
                    }
                }
            }
            case SET -> {
                boolean forced = (op & SET_FORCED) != 0;
                int rd = (op & MASK_SET_RD) >> 8;
                int rg = op & MASK_SET_RG;
                switch (ConditionalOperator.fromMachineCode(op)) {
                    case EQ_ZERO -> {
                        if (getReg(rg) == 0) {
                            setReg(rd, 1);
                        } else if (forced) {
                            setReg(rd, 0);
                        }
                    }
                    case LEQ_ZERO -> {
                        if (getReg(rg) <= 0) {
                            setReg(rd, 1);
                        } else if (forced) {
                            setReg(rd, 0);
                        }
                    }
                    case GT_ZERO -> {
                        if (getReg(rg) > 0) {
                            setReg(rd, 1);
                        } else if (forced) {
                            setReg(rd, 0);
                        }
                    }
                    case NEQ_ZERO -> {
                        if (getReg(rg) != 0) {
                            setReg(rd, 1);
                        } else if (forced) {
                            setReg(rd, 0);
                        }
                    }
                    case LT_ZERO -> {
                        if (getReg(rg) < 0) {
                            setReg(rd, 1);
                        } else if (forced) {
                            setReg(rd, 0);
                        }
                    }
                    case GEQ_ZERO -> {
                        if (getReg(rg) >= 0) {
                            setReg(rd, 1);
                        } else if (forced) {
                            setReg(rd, 0);
                        }
                    }
                    case UNCONDITIONAL, UNKNOWN -> {
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

    public static String toHex(int num) {
        String str = String.format("%08x", num);
        return str.substring(0,4)+"_"+str.substring(4);
    }

    public String dump() {
        String out = "";
        String vl = "";
        for (int i = 0; i <= 0xf; i++) {
            out += String.format("%-11s", MachineCode.translateReg(i));
            vl += toHex(getReg(i)) + "  ";
        }
        out += "\n" + vl;
        
        out += "\n";
        vl = "";
        for (int i = 0xf0; i <= 0xff; i++) {
            if (!isValidReg(i)) {
                out += "           ";
                vl += "           ";
                continue;
            }
            out += String.format("%-11s", MachineCode.translateReg(i));
            vl += toHex(getReg(i)) + "  ";
        }
        out += "\n" + vl;
        
        out += "\n";
        vl = "";
        for (int i = 0x10; i <= 0x1f; i++) {
            out += String.format("%-11s", MachineCode.translateReg(i));
            vl += toHex(getReg(i)) + "  ";
        }
        out += "\n" + vl;
        
        out += "\n";
        vl = "";
        for (int i = 0xe0; i <= 0xef; i++) {
            if (!isValidReg(i)) {
                out += "           ";
                vl += "           ";
                continue;
            }
            out += String.format("%-11s", MachineCode.translateReg(i));
            vl += toHex(getReg(i)) + "  ";
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
