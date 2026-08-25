package com.peter.emulator.debug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.peter.emulator.CPU;
import com.peter.emulator.Emulator;
import com.peter.emulator.assembly.SymbolFile;
import com.peter.emulator.assembly.SymbolFile.FunctionSymbol;
import com.peter.emulator.assembly.SymbolFile.LineSymbol;
import com.peter.emulator.assembly.SymbolFile.StackVarSymbol;
import com.peter.emulator.assembly.SymbolFile.VariableSymbol;
import com.peter.emulator.machinecode.Instruction;

public class Debugger {

    private final SymbolFile kernalSymbols;
    private final SymbolFile symbols;
    private final Emulator emulator;

    private final ArrayList<FunctionSymbol> stack = new ArrayList<>();

    private static final Pattern TYPE_PATTERN = Pattern.compile("(\\w+)([\\*\\&]*)(\\[\\d+\\])?");

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
        synchronized (this) {
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
                    if (cpu.stackPtr <= sv.address && sv.start != addr) {
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
                    if (cpu.stackPtr <= sv.address && sv.start != addr) {
                        toRemove.add(sv);
                    }
                }
                for (StackVarSymbol sv : toRemove) {
                    activeStackVars.remove(sv);
                }
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

    private int getSizeFromIndex(String index) {
        return Integer.parseInt(index.substring(1, index.length() - 1));
    }

    private String readVar(CPU cpu, String type, int address, int start, int end) {
        Matcher m = TYPE_PATTERN.matcher(type);
        if (!m.matches()) {
            return "??";
        }
        String baseType = m.group(1);
        boolean pointer = m.group(2).length() > 0;
        int arrSize = -1;
        if (m.group(3) != null) {
            arrSize = getSizeFromIndex(m.group(3));
        }
        if (baseType.equals("char") && !pointer && arrSize > -1) {
            return "\"" + cpu.bus.readString(cpu.translateAddress(start), arrSize) + "\"";
        }
        if (arrSize > -1) {
            String out = "{";
            if (pointer) { // pointer
                for (int i = 0; i < arrSize; i++) {
                    if (out.length() > 1)
                        out += ",";
                    out += "@0x" + Instruction.toHex(cpu.readMem(address + (i * 4)));
                }
                return out += "}";
            }
            switch (baseType) {
                case "uint16" -> {
                    for (int i = 0; i < arrSize; i++) {
                        if (out.length() > 1)
                            out += ",";
                        out += Integer.toString(cpu.readMemShort(address + (i * 2)));
                    }
                }
                case "uint8" -> {
                    for (int i = 0; i < arrSize; i++) {
                        if (out.length() > 1)
                            out += ",";
                        out += Integer.toString(cpu.readMemByte(address + i));
                    }
                }
                case "bool" -> {
                    for (int i = 0; i < arrSize; i++) {
                        if (out.length() > 1)
                            out += ",";
                        out += cpu.readMemByte(address + i) != 0;
                    }
                }
                default -> {
                    for (int i = 0; i < arrSize; i++) {
                        if (out.length() > 1)
                            out += ",";
                        out += Integer.toString(cpu.readMem(address + (i * 4)));
                    }
                }
            }
            return out += "}";
        } else if (pointer) {
            if (baseType.equals("char") && m.group(2).equals("*")) {
                return "\"" + cpu.bus.readStringNT(cpu.translateAddress(cpu.readMem(address))) + "\""/* @0x" + Instruction.toHex(cpu.readMem(address))*/;
            }
            return "@0x" + Instruction.toHex(cpu.readMem(address));
        }
        return switch (baseType) {
            case "char" -> "'" + (char) cpu.readMemByte(address) + "'";
            case "uint8" -> Integer.toString(cpu.readMemByte(address));
            case "bool" -> cpu.readMemByte(address) != 0 ? "true" : "false";
            case "uint16" -> Integer.toString(cpu.readMemShort(address));
            case "uint32" -> Integer.toString(cpu.readMem(address));
            default -> Instruction.toHexLead(cpu.readMem(address));
        };
    }

    public Collection<VariableSymbol> getVars() {
        return symbols.variables.values();
    }
}
