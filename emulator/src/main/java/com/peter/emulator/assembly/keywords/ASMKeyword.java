package com.peter.emulator.assembly.keywords;

import com.peter.emulator.assembly.ASMParser.ASMLine;
import com.peter.emulator.machinecode.Instruction;

public abstract class ASMKeyword {

    public final String id;

    public ASMKeyword(String id) {
        this.id = id;
    }

    public abstract Instruction add(ASMLine line);

    public abstract String getDef();

    public abstract String getUsage();

    public static class NoOpKeyword extends ASMKeyword {

        public NoOpKeyword() {
            super("NO_OP");
        }

        @Override
        public Instruction add(ASMLine line) {
            return Instruction.NoOp();
        }

        @Override
        public String getDef() {
            return "No Operaiton";
        }

        @Override
        public String getUsage() {
            return "`NO_OP`";
        }

    }

    
    public static class HaltKeyword extends ASMKeyword {

        public HaltKeyword() {
            super("HALT");
        }

        @Override
        public Instruction add(ASMLine line) {
            return Instruction.Halt();
        }

        @Override
        public String getDef() {
            return "Halts the CPU";
        }

        @Override
        public String getUsage() {
            return "`HALT`";
        }

    }
}
