package com.peter.emulator.assembly;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class SymbolFile {

    public String source = null;

    public final HashMap<String, FunctionSymbol> functions = new HashMap<>();
    public final HashMap<String, ValueSymbol> definitions = new HashMap<>();
    public final HashMap<String, Integer> syscalls = new HashMap<>();

    public SymbolFile() {
    }

    public SymbolFile(String source) {
        this.source = source;
    }

    public FunctionSymbol addFunction(FunctionSymbol function) {
        if(source != null) function.source = source;
        functions.put(function.name, function);
        return function;
    }

    public void addDefinition(ValueSymbol value) {
        if(source != null) value.source = source;
        definitions.put(value.name, value);
    }

    public FunctionSymbol addFunction(FunctionSymbol function, int lineN) {
        if(source != null) function.source = source;
        function.sourceLine = lineN;
        functions.put(function.name, function);
        return function;
    }

    public void addDefinition(ValueSymbol value, int lineN) {
        if(source != null) value.source = source;
        value.sourceLine = lineN;
        definitions.put(value.name, value);
    }

    public void updateDefinition(String name, int start, int end) {
        ValueSymbol def = definitions.get(name);
        def.start = start;
        def.end = end;
    }

    public void mapSyscall(String name, int index) {
        syscalls.put(name, index);
    }

    public SymbolFile combine(SymbolFile other, int otherOffset) {
        for (FunctionSymbol functionSymbol : other.functions.values()) {
            FunctionSymbol f2 = functionSymbol.copy();
            if (f2.end >= f2.start) {
                f2.end += otherOffset;
            }
            f2.start += otherOffset;
            addFunction(f2);
        }
        for (ValueSymbol valueSymbol : other.definitions.values()) {
            ValueSymbol v2 = valueSymbol.copy();
            if (v2.start >= 0) {
                if (v2.end >= v2.start) {
                    v2.end += otherOffset;
                }
                v2.start += otherOffset;
            }
            addDefinition(v2);
        }
        for (String name : other.syscalls.keySet()) {
            mapSyscall(name, other.syscalls.get(name));
        }
        return this;
    }
    
    public String toFile() {
        JSONObject json = new JSONObject();
        json.put("version", 1);
        JSONObject jsonFunctions = new JSONObject();
        json.put("functions", jsonFunctions);
        for (FunctionSymbol symbol : functions.values()) {
            jsonFunctions.put(symbol.name, symbol.toJSON());
        }
        JSONObject jsonDefinitions = new JSONObject();
        json.put("definitions", jsonDefinitions);
        for (ValueSymbol symbol : definitions.values()) {
            jsonDefinitions.put(symbol.name, symbol.toJSON());
        }
        JSONObject jsonSyscalls = new JSONObject();
        json.put("syscalls", jsonSyscalls);
        for (String name : syscalls.keySet()) {
            jsonSyscalls.put(name, syscalls.get(name));
        }
        return json.toString(4);
    }
    public String toFileKernal() {
        JSONObject json = new JSONObject();
        json.put("version", 1);
        JSONObject jsonFunctions = new JSONObject();
        json.put("functions", jsonFunctions); // not included in kernal.obj
        
        JSONObject jsonDefinitions = new JSONObject();
        json.put("definitions", jsonDefinitions);
        for (ValueSymbol symbol : definitions.values()) {
            if(symbol.name.startsWith("KERNAL_"))
                jsonDefinitions.put(symbol.name, symbol.toJSON());
        }
        JSONObject jsonSyscalls = new JSONObject();
        json.put("syscalls", jsonSyscalls);
        for (String name : syscalls.keySet()) {
            jsonSyscalls.put(name, syscalls.get(name));
        }
        return json.toString(4);
    }

    public static SymbolFile fromFile(String file) {
        JSONObject json = new JSONObject(file);
        SymbolFile symbols = new SymbolFile();
        switch (json.getInt("version")) {
            case 1 -> {
                JSONObject jsonFunctions = json.getJSONObject("functions");
                for (String key : jsonFunctions.keySet()) {
                    symbols.functions.put(key, FunctionSymbol.fromJSON(key, jsonFunctions.getJSONObject(key)));
                }
                JSONObject jsonDefinitions = json.getJSONObject("definitions");
                for (String key : jsonDefinitions.keySet()) {
                    symbols.definitions.put(key, ValueSymbol.fromJSON(key, jsonDefinitions.getJSONObject(key)));
                }
                JSONObject jsonSyscalls = json.getJSONObject("syscalls");
                for (String key : jsonSyscalls.keySet()) {
                    symbols.syscalls.put(key, jsonSyscalls.getInt(key));
                }
            }
            default -> {
                throw new RuntimeException("Unsupported symbol file version: found "+json.getInt("version")+" but only version(s) [1] were supported");
            }
        }
        return symbols;
    }

    public static class Symbol {
        public String source = null;
        public int sourceLine = -1;
        public String name;
        public int start;
        public int end;

        public Symbol(String name, int start, int end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }

        public JSONObject toJSON() {
            JSONObject json = new JSONObject();
            json.put("start", start);
            json.put("end", end);
            return json;
        }

        public static Symbol fromJSON(String name, JSONObject json) {
            int start = json.getInt("start");
            int end = json.getInt("start");
            return new Symbol(name, start, end);
        }

        public boolean in(int addr) {
            if (end == -1) {
                return addr == start;
            }
            return addr >= start && addr < end;
        }

        @Override
        public String toString() {
            String line = "";
            if (sourceLine >= 0) {
                line = String.format(" (line %d)", sourceLine);
            }
            if(source != null)
                return String.format("%s:%s%s", source, name, line);
            return String.format("%s%s", name, line);
        }
    }

    public static class ValueSymbol extends Symbol {

        public String type;
        public String value;

        public ValueSymbol(String name, int start, int end, String type, String value) {
            super(name, start, end);
            this.type = type;
            this.value = value;
        }

        public ValueSymbol copy() {
            ValueSymbol s = new ValueSymbol(name, start, end, type, value);
            s.source = source;
            return s;
        }

        @Override
        public JSONObject toJSON() {
            JSONObject json = super.toJSON();
            json.put("type", type);
            json.put("value", value);
            return json;
        }

        public static ValueSymbol fromJSON(String name, JSONObject json) {
            int start = json.getInt("start");
            int end = json.getInt("start");
            String type = json.getString("type");
            String value = json.getString("value");
            return new ValueSymbol(name, start, end, type, value);
        }

        public int getValue() {
            if (type.equals("const uint32")) {
                return Integer.parseInt(value);
            }
            throw new RuntimeException("Can't get value of non `const uint32`");
        }

        @Override
        public String toString() {
            if(source != null)
                return String.format("%s.%s = %s", source, name, value);
            return String.format("%s = %s", name, value);
        }

    }

    public static class FunctionSymbol extends Symbol {

        public String[] args;
        public String rt;
        public boolean isSyscall;
        public boolean hasSyscall;
        public int endLine = -1;

        public FunctionSymbol(String name, int start, int end, String[] args, String rt) {
            super(name, start, end);
            this.args = args;
            this.rt = rt;
        }
        
        public FunctionSymbol(String name, int start, int end, String[] args, String rt, boolean isSyscall, boolean hasSyscall) {
            super(name, start, end);
            this.args = args;
            this.rt = rt;
            this.isSyscall = isSyscall;
            this.hasSyscall = hasSyscall;
        }

        public FunctionSymbol copy() {
            FunctionSymbol s = new FunctionSymbol(name, start, end, args.clone(), rt, isSyscall, hasSyscall);
            s.source = source;
            return s;
        }

        @Override
        public JSONObject toJSON() {
            JSONObject json = super.toJSON();
            json.put("args", args);
            json.put("rt", rt);
            if(isSyscall)
                json.put("isSyscall", true);
            if(hasSyscall)
                json.put("hasSyscall", true);
            return json;
        }

        public static FunctionSymbol fromJSON(String name, JSONObject json) {
            int start = json.getInt("start");
            int end = json.getInt("start");
            JSONArray jsonArgs = json.getJSONArray("args");
            String[] args = new String[jsonArgs.length()];
            for (int i = 0; i < args.length; i++) {
                args[i] = jsonArgs.getString(i);
            }
            String rt = json.getString("rt");
            FunctionSymbol func = new FunctionSymbol(name, start, end, args, rt);
            if (json.has("isSyscall"))
                func.isSyscall = json.getBoolean("isSyscall");
            if (json.has("hasSyscall"))
                func.hasSyscall = json.getBoolean("hasSyscall");
            return func;
        }

        @Override
        public String toString() {
            String argStr = "";
            for (String string : args) {
                if (argStr.length() > 0)
                    argStr += ", ";
                argStr += string;
            }
            String line = "";
            if (sourceLine >= 0) {
                line = String.format(" (line %d)", sourceLine);
            }
            if(source != null)
                return String.format("%s:%s(%s) %s%s", source, name, argStr, rt, line);
            return String.format("%s(%s) %s%s", name, argStr, rt, line);
        }

    }
}
