package com.peter.emulator.debug;

import java.util.ArrayList;
import java.util.Set;

import com.peter.emulator.CPU;
import com.peter.emulator.Emulator;
import com.peter.emulator.assembly.SymbolFile;
import com.peter.emulator.assembly.SymbolFile.FunctionSymbol;
import com.peter.emulator.assembly.SymbolFile.VariableSymbol;

public class Debugger {

    private final SymbolFile kernalSymbols;
    private final SymbolFile symbols;
    private final Emulator emulator;

    private final ArrayList<FunctionSymbol> stack = new ArrayList<>();

    public Debugger(SymbolFile kernalSymbols, SymbolFile symbols, Emulator emulator) {
        this.kernalSymbols = kernalSymbols;
        this.symbols = symbols;
        this.emulator = emulator;
    }

    public String getSymbol(CPU cpu) {
        int addr = cpu.pgmPtr;
        if (cpu.privilegeMode) { // in kernal
            for (FunctionSymbol symbol : kernalSymbols.functions.values()) {
                if (symbol.in(addr)) {
                    return String.format("%s", symbol);
                }
            }
            return String.format("Unknown kernal address [0x%x]", addr);
        }
        for (FunctionSymbol symbol : symbols.functions.values()) {
            if (symbol.in(addr)) {
                return String.format("%s", symbol);
            }
        }
        return String.format("Unknown address [0x%x]", addr);
    }

    public void update(CPU cpu) {
        int addr = cpu.pgmPtr;
        if (cpu.privilegeMode) { // in kernal
            for (FunctionSymbol symbol : kernalSymbols.functions.values()) {
                if (symbol.start == addr) {
                    // System.out.println("Entering " + symbol);
                    stack.add(symbol);
                }
                if (symbol.end == addr + 4) {
                    // System.out.println("Exiting " + symbol);
                    stack.remove(stack.size() - 1);
                }
            }
            if (kernalSymbols.breakpoints.contains(cpu.pgmPtr)) {
                emulator.setWait(true);
            }
        } else {
            for (FunctionSymbol symbol : symbols.functions.values()) {
                if (symbol.in(addr)) {
                    if (symbol.start == addr) {
                        // System.out.println("Entering "+ symbol);
                        stack.add(symbol);
                    }
                    if (symbol.end == addr + 4) {
                        // System.out.println("Exiting "+ symbol);
                        stack.remove(stack.size()-1);
                    }
                }
            }
            if (symbols.breakpoints.contains(cpu.pgmPtr)) {
                emulator.setWait(true);
            }
        }
    }

    public String printStack() {
        String str = "Stack trace:";
        for (int i = stack.size() - 1; i >= 0; i--) {
            String s = stack.get(i).toString();
            str += "\n\t" + s;
        }
        return str;
    }

    public String getVar(CPU cpu, String name) {
        if (cpu.privilegeMode) {
            if (!kernalSymbols.variables.containsKey(name)) {
                return "unknown";
            }
            return readVar(cpu, kernalSymbols.variables.get(name));
        } else {
            if (!symbols.variables.containsKey(name)) {
                return "unknown";
            }
            return readVar(cpu, symbols.variables.get(name));
        }
    }

    private String readVar(CPU cpu, VariableSymbol vs) {
        return switch (vs.type) {
            case "char*" -> "\"" + cpu.ram.readString(cpu.translateAddress(vs.start), vs.end - vs.start) + "\"";
            case "char" -> "'" + (char) cpu.readMemByte(vs.address) + "'";
            case "uint8" -> Integer.toString(cpu.readMemByte(vs.address));
            case "boolean" -> cpu.readMemByte(vs.address) != 0 ? "true" : "false";
            case "uint16" -> Integer.toString(cpu.readMemShort(vs.address));
            default -> Integer.toString(cpu.readMem(vs.address));
        };
    }

    public Set<String> getVars() {
        return symbols.variables.keySet();
    }
}
