package com.peter.emulator.assembly;

import java.util.ArrayList;

public class Define {

    public String name;
    public int value = 0;

    public int[] valueArr = null;

    public final boolean isAddress;
    protected boolean resolved = false;

    public int size = 0;

    protected ArrayList<ResolvableValue> listeners = new ArrayList<>();

    public Define(String name, int value) {
        this.name = name;
        this.value = value;
        isAddress = false;
    }

    public Define(String name, int[] valueArr) {
        this.name = name;
        this.valueArr = valueArr;
        this.value = 0;
        this.size = valueArr.length * 4;
        isAddress = true;
    }

    public Define(String name, String str) {
        this.name = name;
        int len = str.length();
        this.valueArr = new int[Math.ceilDiv(len, 4)];
        for (int i = 0; i < str.length(); i += 4) {
            int v = ((int) str.charAt(i)) << 24;
            if (i + 1 < len)
                v |= ((int) str.charAt(i + 1)) << 16;
            if (i + 2 < len)
                v |= ((int) str.charAt(i + 2)) << 8;
            if (i + 3 < len)
                v |= (int) str.charAt(i + 3);
            valueArr[i / 4] = v;
        }
        this.size = valueArr.length * 4;
        this.value = 0;
        isAddress = true;
    }

    public Define(String name) {
        this.name = name;
        this.value = 0;
        this.size = 4;
        isAddress = true;
    }

    public Define withSize(int size) {
        this.size = size;
        return this;
    }

    public void addListener(ResolvableValue listener) {
        if (!isAddress) {
            return;
        }
        if (resolved) {
            listener.onResolve(value);
        }
        listeners.add(listener);
    }
    
    public void resolveAt(int address) {
        if (!isAddress) {
            return;
        }
        if (resolved) {
            throw new IllegalStateException("Define " + name + " was already resolved");
        } else {
            // System.out.println("Resolving "+name+" at "+Instruction.toHexLead(address));
        }
        resolved = true;
        // System.out.println("Resolving "+name+" at "+address);
        value = address;
        for (ResolvableValue listener : listeners) {
            listener.onResolve(address);
        }
    }
}
