package com.peter.emulator;

import static com.peter.emulator.MachineCode.*;

import java.util.ArrayDeque;

import com.peter.emulator.components.ComponentBus;
import com.peter.emulator.components.MMU;
import com.peter.emulator.components.RAM;
import com.peter.emulator.debug.Debugger;
import com.peter.emulator.machinecode.*;
import com.peter.emulator.machinecode.Goto.Mode;
import com.peter.emulator.machinecode.StoreInstruction.Source;

public class CPU {

    public final int cpuId;

    public final ComponentBus bus;
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
    public int stackPtr = 0x8000;
    // rStackI
    public int stackPtrI = 0x8000;
    // rPgm
    public int arithmeticFlag = 0;
    // rPgmI
    public int arithmeticFlagI = 0;
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

    public Instruction lastInstruction;

    public CPU(int cpuId, ComponentBus bus, MMU mmu) {
        this.cpuId = cpuId;
        this.bus = bus;
        this.mmu = mmu;
    }

    public void setPtr(int ptr) {
        pgmPtr = ptr;
    }

    private boolean isValidReg(int reg) {
        return switch(reg) {
            case REG_PGM_PNTR -> true;
            case REG_STACK_PNTR -> true;
            case REG_ARITHMETIC_FLAG -> true;

            case REG_PID -> true;
            case REG_MEM_TABLE -> true;

            case REG_INTERRUPT -> true;
            case REG_INTR_HANDLER -> true;
            
            case REG_CPU_ID -> true;
            case REG_PRIVILEGED_MODE -> true;
            
            case REG_PGM_PNTR_I -> true;
            case REG_STACK_PNTR_I -> true;
            case REG_ARITHMETIC_FLAG_I -> true;

            case REG_PID_I -> true;
            case REG_MEM_TABLE_I -> true;

            case REG_PRIVILEGED_MODE_I -> true;

            default -> reg >= 0 && reg < 0x30;
        };
    }

    public int getReg(Reg reg) {
        if (reg.code < 0x10) {
            return registers[reg.code];
        } else if (reg.code < 0x20) {
            return registersI[reg.code & 0xf];
        }
        return switch (reg) {
            case PGM -> pgmPtr;
            case STACK -> stackPtr;
            case AF -> arithmeticFlag;

            case PID -> pid;
            case MEM_TABLE -> memTablePtr;

            case INTERRUPT_CODE -> interruptCode;
            case INTERRUPT_HANDLER -> interruptHandler;
            
            case CPU_ID -> cpuId;
            case PRIVILEGE -> privilegeMode ? 1 : 0;
            
            case PGM_I -> pgmPtrI;
            case STACK_I -> stackPtrI;
            case AF_I -> arithmeticFlagI;

            case PID_I -> pidI;
            case MEM_TABLE_I -> memTablePtrI;

            case PRIVILEGE_I -> privilegeModeI ? 1 : 0;
        
            default -> {
                throw new RuntimeException("Invalid special register");
            }
        };
    }

    public void setReg(Reg reg, int val) {
        // System.out.println(String.format("Setting register %x to %x", reg, val));
        if (reg.code < 0x10) {
            registers[reg.code] = val;
            return;
        }
        if (reg.code < 0x20) {
            if (!privilegeMode) {
                interrupt(0x8000_0001);
                return;
            }
            registersI[reg.code] = val;
            return;
        }
        switch (reg) {
            case PGM -> {
                pgmPtr = val;
            }
            case STACK -> {
                stackPtr = val;
            }
            case AF -> {
                arithmeticFlag = val;
            }
            case PID -> {
                if (!privilegeMode) {
                    interrupt(0x8000_0001);
                    return;
                }
                pid = val;
            }
            case MEM_TABLE -> {
                if (!privilegeMode) {
                    interrupt(0x8000_0001);
                    return;
                }
                memTablePtr = val;
            }
            case PRIVILEGE -> {
                if (!privilegeMode) {
                    interrupt(0x8000_0001);
                    return;
                }
                privilegeMode = val != 0;
            }
            case INTERRUPT_CODE -> {
                if (!privilegeMode) {
                    interrupt(0x8000_0001);
                    return;
                }
                interruptCode = val;
            }
            case INTERRUPT_HANDLER -> {
                if (!privilegeMode) {
                    interrupt(0x8000_0001);
                    return;
                }
                interruptHandler = val;
            }

            case CPU_ID -> {
                interrupt(0x8000_0001);
            }
            
            case PGM_I -> {
                if (!privilegeMode) {
                    interrupt(0x8000_0001);
                    return;
                }
                pgmPtrI = val;
            }
            case STACK_I -> {
                if (!privilegeMode) {
                    interrupt(0x8000_0001);
                    return;
                }
                stackPtrI = val;
            }
            case AF_I -> {
                arithmeticFlagI = val;
            }
            case PID_I -> {
                if (!privilegeMode) {
                    interrupt(0x8000_0001);
                    return;
                }
                pidI = val;
            }
            case MEM_TABLE_I -> {
                if (!privilegeMode) {
                    interrupt(0x8000_0001);
                    return;
                }
                memTablePtrI = val;
            }
            case PRIVILEGE_I -> {
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
        bus.writeWord(mmu.translate(this, addr), val);
    }
    public void writeMemShort(int addr, int val) {
        bus.writeShort(mmu.translate(this, addr), val);
    }
    public void writeMemByte(int addr, byte val) {
        bus.writeByte(mmu.translate(this, addr), val);
    }

    public int readMem(int addr) {
        return bus.readWord(mmu.translate(this, addr));
    }
    public int readMemShort(int addr) {
        return bus.readShort(mmu.translate(this, addr));
    }

    public byte readMemByte(int addr) {
        return bus.readByte(mmu.translate(this, addr));
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
    public int instrB;
    public boolean inInterrupt = false;
    public void tick() {
        if (!running)
            return;

        if (interruptCode == 0 && !inInterrupt && !interruptQueue.isEmpty()) {
            interruptCode = interruptQueue.pop();
        }
        
        if (interruptCode != 0 && !inInterrupt) {
            inInterrupt = true;
            // System.err.println("Interrupt: "+interruptCode);
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

        if (debugger != null) {
            debugger.update(this);
        }
        int op = readMem(pgmPtr);
        pgmPtr += 4;
        instr = op;
        int next = readMem(pgmPtr);
        instrB = next;
        lastInstruction = Instruction.fromBytecode(op, next);
        if (printInstr) {
            System.out.println(String.format("CPU Tick: [%04x] %s", mmu.translate(this, pgmPtr - 4), lastInstruction.toString()));
        }
        if (lastInstruction.hasSecond()) {
            pgmPtr += 4;
        }
        switch (lastInstruction.op) {
            case NO_OP, UNKNOWN -> {}
            case HALT -> {
                if (!privilegeMode)
                    return;
                running = false;
            }
            case LOAD -> {
                Load loadInstr = (Load) lastInstruction;
                int val;
                switch (loadInstr.mode) {
                    case LITERAL -> {
                        val = loadInstr.data;
                    }
                    case MEM_WORD -> {val = readMem(getReg(loadInstr.ra));}
                    case MEM_SHORT -> {val = readMemShort(getReg(loadInstr.ra));}
                    case MEM_BYTE -> {val = readMemByte(getReg(loadInstr.ra));}
                    default -> {
                        throw new RuntimeException(String.format("Unknown load mode: %20x", (op & 0xff) >> 8));
                    }
                }
                setReg(loadInstr.rg, val);
            }
            case STORE -> {
                StoreInstruction storeI = (StoreInstruction) lastInstruction;
                int val = switch(storeI.source) {
                    case REG, REG_REG -> getReg(storeI.rg);
                    case MEM -> switch(storeI.size) {
                        case WORD -> readMem(getReg(storeI.rg));
                        case SHORT -> readMemShort(getReg(storeI.rg));
                        case BYTE -> readMemByte(getReg(storeI.rg));
                    };
                    case VAL -> next;
                };
                if(storeI.source == Source.REG_REG) {
                    setReg(storeI.ra, val);
                } else {
                    switch(storeI.size) {
                        case WORD -> writeMem(getReg(storeI.ra), val);
                        case SHORT -> writeMemShort(getReg(storeI.ra), val);
                        case BYTE -> writeMemByte(getReg(storeI.ra), (byte) val);
                    }
                }
                int incSize = switch(storeI.size) {
                    case WORD -> 4;
                    case SHORT -> 2;
                    default -> 1;
                };
                if(storeI.incRG)
                    setReg(storeI.rg, getReg(storeI.rg) + incSize);
                if(storeI.incRA)
                    setReg(storeI.ra, getReg(storeI.ra) + incSize);
            }
            case MATH -> {
                MathInstruction mathI = (MathInstruction) lastInstruction;
                arithmeticFlag = 0;
                switch (mathI.operation) {
                    case ADD -> {
                        int out;
                        int ra = getReg(mathI.ra);
                        int rb = getReg(mathI.rb);
                        try {
                            out = Math.addExact(ra, rb);
                        } catch (ArithmeticException e) {
                            out = ra + rb;
                            arithmeticFlag = 1;
                        }
                        setReg(mathI.rd, out);
                    }
                    case SUB -> {
                        int out;
                        int ra = getReg(mathI.ra);
                        int rb = getReg(mathI.rb);
                        try {
                            out = Math.subtractExact(ra, rb);
                        } catch (ArithmeticException e) {
                            out = ra - rb;
                            arithmeticFlag = 1;
                        }
                        setReg(mathI.rd, out);
                    }
                    case MUL -> {
                        setReg(mathI.rd, getReg(mathI.ra) * getReg(mathI.rb));
                    }
                    case DIV -> {
                        int a = getReg(mathI.ra);
                        int b = getReg(mathI.rb);
                        setReg(mathI.rd, a / b);
                        arithmeticFlag = a % b;
                    }
                    case INC -> {
                        setReg(mathI.rd, getReg(mathI.rd) + mathI.getInc());
                    }
                    case AND -> {
                        setReg(mathI.rd, getReg(mathI.ra) & getReg(mathI.rb));
                    }
                    case OR -> {
                        setReg(mathI.rd, getReg(mathI.ra) | getReg(mathI.rb));
                    }
                    case NAND -> {
                        setReg(mathI.rd, ~(getReg(mathI.ra) & getReg(mathI.rb)));
                    }
                    case NOR -> {
                        setReg(mathI.rd, ~(getReg(mathI.ra) | getReg(mathI.rb)));
                    }
                    case NOT -> {
                        setReg(mathI.rd, ~getReg(mathI.ra));
                    }
                    case XOR -> {
                        setReg(mathI.rd, getReg(mathI.ra) ^ getReg(mathI.rb));
                    }
                    case LSHIFT -> {
                        int ra = getReg(mathI.ra);
                        if(mathI.rotate) {
                            setReg(mathI.rd, Integer.rotateLeft(ra, mathI.data & 0x7f));
                        } else {
                            setReg(mathI.rd, ra << mathI.data);
                        }
                    }
                    case RSHIFT -> {
                        int ra = getReg(mathI.ra);
                        if(mathI.rotate) {
                            setReg(mathI.rd, Integer.rotateRight(ra, mathI.data & 0x7f));
                        } else {
                            setReg(mathI.rd, ra >>> mathI.data);
                        }
                    }
                    case NONE, UNUSED_E, UNUSED_F -> {
                    }
                }
            }
            case GOTO -> {
                Goto gotoInstruction = (Goto) lastInstruction;
                boolean condVal = switch(gotoInstruction.condition) {
                    case UNCONDITIONAL -> true;
                    case EQ_ZERO -> getReg(gotoInstruction.rg) == 0;
                    case LEQ_ZERO -> getReg(gotoInstruction.rg) <= 0;
                    case GT_ZERO -> getReg(gotoInstruction.rg) > 0;
                    case NEQ_ZERO -> getReg(gotoInstruction.rg) != 0;
                    case LT_ZERO -> getReg(gotoInstruction.rg) < 0;
                    case GEQ_ZERO -> getReg(gotoInstruction.rg) >= 0;
                    case UNUSED_7, UNUSED_8, UNUSED_9, UNUSED_A, UNUSED_B, UNUSED_C, UNUSED_D, UNUSED_E, UNUSED_F -> true;
                };
                if (condVal) {
                    if (gotoInstruction.mode == Mode.POP) {
                        pgmPtr = stackPop();
                    } else {
                        if (gotoInstruction.mode == Mode.PUSH)
                            stackPush(pgmPtr);
                        if (gotoInstruction.rel)
                            pgmPtr += next;
                        else
                            pgmPtr = getReg(gotoInstruction.ra);
                    }
                }
            }
            case SET -> {
                SetInstruction setI = (SetInstruction) lastInstruction;
                switch (setI.condition) {
                    case EQ_ZERO -> {
                        if (getReg(setI.rg) == 0) {
                            setReg(setI.rd, 1);
                        } else if (setI.forced) {
                            setReg(setI.rd, 0);
                        }
                    }
                    case LEQ_ZERO -> {
                        if (getReg(setI.rg) <= 0) {
                            setReg(setI.rd, 1);
                        } else if (setI.forced) {
                            setReg(setI.rd, 0);
                        }
                    }
                    case GT_ZERO -> {
                        if (getReg(setI.rg) > 0) {
                            setReg(setI.rd, 1);
                        } else if (setI.forced) {
                            setReg(setI.rd, 0);
                        }
                    }
                    case NEQ_ZERO -> {
                        if (getReg(setI.rg) != 0) {
                            setReg(setI.rd, 1);
                        } else if (setI.forced) {
                            setReg(setI.rd, 0);
                        }
                    }
                    case LT_ZERO -> {
                        if (getReg(setI.rg) < 0) {
                            setReg(setI.rd, 1);
                        } else if (setI.forced) {
                            setReg(setI.rd, 0);
                        }
                    }
                    case GEQ_ZERO -> {
                        if (getReg(setI.rg) >= 0) {
                            setReg(setI.rd, 1);
                        } else if (setI.forced) {
                            setReg(setI.rd, 0);
                        }
                    }
                    case UNCONDITIONAL, UNUSED_7, UNUSED_8, UNUSED_9, UNUSED_A, UNUSED_B, UNUSED_C, UNUSED_D, UNUSED_E, UNUSED_F -> {
                    }
                }
            }
            case STACK -> {
                StackInstruction stackInstr = (StackInstruction) lastInstruction;
                switch(stackInstr.operation) {
                    case PUSH -> {
                        stackPush(getReg(stackInstr.rg));
                    }
                    case POP -> {
                        setReg(stackInstr.rg, stackPop());
                    }
                    case INC -> {
                        stackPtr += stackInstr.getInc();
                    }
                    case DEC -> {
                        stackPtr -= stackInstr.getInc();
                    }
                }
            }
            case SYSCALL -> {
                Syscall syscallI = (Syscall) lastInstruction;
                switch (syscallI.operation) {
                    case RETURN -> {
                        // SYSRETURN
                        if (!privilegeMode) {
                            return;
                        }
                        int ptr = readMem(SYSCALL_TABLE_START);
                        pgmPtr = ptr;
                        privilegeMode = false;
                    }
                    case GOTO -> {
                        // SYSGOTO
                        if (!privilegeMode) {
                            return;
                        }
                        pgmPtr = getReg(syscallI.rg);
                        privilegeMode = false;
                    }
                    case INTERRUPT -> {
                        // int iOp = op & MASK_SYSCALL_INTERRUPT_OP;
                        switch (syscallI.interruptOption) {
                            case RETURN -> {
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
                                // System.out.println("Interrupt ret to " + pgmPtr);
                            }
                            case VALUE -> {
                                interrupt(syscallI.data);
                            }
                            case REGISTER -> {
                                interrupt(getReg(syscallI.rg));
                            }
                        }
                    }
                    case FUNCTION -> {
                        privilegeMode = true;
                        int ptr = readMem((syscallI.data<<2) + SYSCALL_TABLE_START);
                        if (ptr == 0xffff_ffff) {
                            running = false;
                            // TODO: interrupt?
                            throw new RuntimeException(String.format("Unknown syscall: 0x%x", syscallI.data));
                        }
                        writeMem(SYSCALL_TABLE_START, pgmPtr);
                        pgmPtr = ptr;
                    }
                    case TRANSLATE -> {
                        setReg(syscallI.rg, mmu.translate(this, getReg(syscallI.rg)));
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
            vl += toHex(getReg(Reg.from(i))) + "  ";
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
            vl += toHex(getReg(Reg.from(i))) + "  ";
        }
        out += "\n" + vl;
        
        out += "\n";
        vl = "";
        for (int i = 0x10; i <= 0x1f; i++) {
            out += String.format("%-11s", MachineCode.translateReg(i));
            vl += toHex(getReg(Reg.from(i))) + "  ";
        }
        out += "\n" + vl;
        
        out += "\n";
        vl = "";
        for (int i = 0xe0; i <= 0xef; i++) {
            Reg rg = Reg.from(i);
            if (rg == Reg.UNKNOWN) {
                out += "           ";
                vl += "           ";
                continue;
            }
            out += String.format("%-11s", MachineCode.translateReg(i));
            vl += toHex(getReg(Reg.from(i))) + "  ";
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
        instrB = 0;
        for (int i = 0; i < registers.length; i++) {
            registers[i] = 0;
            registersI[i] = 0;
        }
        running = true;
    }
}
