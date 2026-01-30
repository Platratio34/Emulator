package com.peter.emulator.debug;

import java.util.ArrayList;

import com.peter.emulator.CPU;
import com.peter.emulator.assembly.SymbolFile;
import com.peter.emulator.assembly.SymbolFile.FunctionSymbol;

public class Debugger {

    private final SymbolFile kernalSymbols;
    private final SymbolFile symbols;

    private final ArrayList<FunctionSymbol> stack = new ArrayList<>();

    public Debugger(SymbolFile kernalSymbols, SymbolFile symbols) {
        this.kernalSymbols = kernalSymbols;
        this.symbols = symbols;
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
                if (symbol.end == addr + 1) {
                    // System.out.println("Exiting " + symbol);
                    stack.remove(stack.size()-1);
                }
            }
        } else {
            for (FunctionSymbol symbol : symbols.functions.values()) {
                if (symbol.in(addr)) {
                    if (symbol.start == addr) {
                        // System.out.println("Entering "+ symbol);
                        stack.add(symbol);
                    }
                    if (symbol.end == addr) {
                        // System.out.println("Exiting "+ symbol);
                        stack.remove(stack.size()-1);
                    }
                }
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
}
