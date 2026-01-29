package com.peter.emulator.lang;

import java.util.HashMap;

public enum ELProtectionLevel {
    UNKNOWN("unknown"),
    PUBLIC("public"),
    PROTECTED("protected"),
    PRIVATE("private"),
    INTERNAL("internal");

    public final String value;
    private static HashMap<String, ELProtectionLevel> values;

    private ELProtectionLevel(String value) {
        this.value = value;
        add();
    }

    private void add() {
        if (this == UNKNOWN)
            return;
        if (values == null)
            values = new HashMap<>();
        values.put(value, this);
    }

    public static ELProtectionLevel get(String value) {
        if (values.containsKey(value))
            return values.get(value);
        return UNKNOWN;
    }
    public static ELProtectionLevel get(String value, ELProtectionLevel def) {
        return values.getOrDefault(value, def);
    }
    
    public static boolean valid(String value) {
        return values.containsKey(value);
    }
}
