package com.peter.emulator.debug;

import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.peter.emulator.CPU;
import com.peter.emulator.Emulator;
import com.peter.emulator.assembly.SymbolFile;
import com.peter.emulator.assembly.SymbolFile.FunctionSymbol;
import com.peter.emulator.assembly.SymbolFile.LineSymbol;
import com.peter.emulator.assembly.SymbolFile.StackVarSymbol;
import com.peter.emulator.assembly.SymbolFile.VariableSymbol;

public class Debugger {

    private final SymbolFile kernalSymbols;
    private final SymbolFile symbols;
    private final Emulator emulator;

    private final ArrayList<FunctionSymbol> stack = new ArrayList<>();

    private static final Pattern CHAR_ARRAY_PATTERN = Pattern.compile("char\\[(\\d+)\\]");

    public final ArrayList<StackVarSymbol> activeStackVars = new ArrayList<>();

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
            // activeStackVars.clear();
            for (StackVarSymbol sv : kernalSymbols.stackVarSymbols) {
                if (sv.start == addr) {
                    activeStackVars.add(sv.activate(cpu.stackPtr));
                }
            }
            ArrayList<StackVarSymbol> toRemove = new ArrayList<>();
            for (StackVarSymbol sv : activeStackVars) {
                if (sv.end == addr) {
                    toRemove.add(sv);
                }
            }
            for (StackVarSymbol sv : toRemove) {
                activeStackVars.remove(sv);
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
            for (StackVarSymbol sv : symbols.stackVarSymbols) {
                if (sv.start == addr) {
                    activeStackVars.add(sv.activate(cpu.stackPtr));
                }
            }
            ArrayList<StackVarSymbol> toRemove = new ArrayList<>();
            for (StackVarSymbol sv : activeStackVars) {
                if (sv.end == addr) {
                    toRemove.add(sv);
                }
            }
            for (StackVarSymbol sv : toRemove) {
                activeStackVars.remove(sv);
            }
        }
    }

    public String getLine(CPU cpu, String defLine) {
        if (cpu.privilegeMode) {
            for (LineSymbol line : kernalSymbols.lineSymbols) {
                if (line.start <= cpu.pgmPtr && line.end >= cpu.pgmPtr)
                    return line.name;
            }
            return defLine;
        } else {
            for (LineSymbol line : symbols.lineSymbols) {
                if (line.start <= cpu.pgmPtr && line.end >= cpu.pgmPtr)
                    return line.name;
            }
            return defLine;
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

    public String readVar(CPU cpu, StackVarSymbol sv) {
        return readVar(cpu, sv.type, sv.address, sv.address, sv.address+3);
    }

    private String readVar(CPU cpu, VariableSymbol vs) {
        return readVar(cpu, vs.type, vs.address, vs.start, vs.end);
    }

    private String readVar(CPU cpu, String type, int address, int start, int end) {
        if (type.startsWith("char[")) {
            // System.out.println(type);
            Matcher m = CHAR_ARRAY_PATTERN.matcher(type);
            if (!m.matches()) {
                return "\"" + cpu.ram.readString(cpu.translateAddress(start), end - start) + "\"";
            }
            int len = Integer.parseInt(m.group(1));
            return "\"" + cpu.ram.readString(cpu.translateAddress(start), len) + "\"";
        }
        return switch (type) {
            case "char*" -> "\"" + cpu.ram.readString(cpu.translateAddress(start), end - start) + "\"";
            case "char" -> "'" + (char) cpu.readMemByte(address) + "'";
            case "uint8" -> Integer.toString(cpu.readMemByte(address));
            case "boolean" -> cpu.readMemByte(address) != 0 ? "true" : "false";
            case "uint16" -> Integer.toString(cpu.readMemShort(address));
            default -> Integer.toString(cpu.readMem(address));
        };
    }

    public Set<String> getVars() {
        return symbols.variables.keySet();
    }
}
