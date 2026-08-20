package com.peter.emulator.components;

import java.util.HashMap;

public class ComponentBus {

    public HashMap<Integer, BusComponent> components = new HashMap<>();

    public void addComponent(BusComponent component) {
        int start = component.getStart() >> 16;
        int numBlocks = component.getNumBlocks();
        for (int i = 0; i < numBlocks; i++) {
            // System.out.println(String.format("Component add: 0x%04x %s", start + i, component));
            components.put(start + i, component);
        }
    }

    public byte readByte(int address) {
        int bI = address >> 16;
        if (components.containsKey(bI)) {
            return components.get(bI).readByte(address);
        }
        throw MemoryException.Read(address);
    }

    public int readShort(int address) {
        return ((readByte(address) & 0xff) << 8) | (readByte(address+1) & 0xff);
    }
    
    public int readWord(int address) {
        return ((readByte(address) & 0xff) << 24) | ((readByte(address+1) & 0xff) << 16) | ((readByte(address+2) & 0xff) << 8) | (readByte(address+3) & 0xff);
    }

    public void writeByte(int address, byte value) {
        int bI = address >> 16;
        if (components.containsKey(bI)) {
            components.get(bI).writeByte(address, value);
            return;
        }
        throw MemoryException.Write(address);
    }
    
    public void writeShort(int address, int value) {
        writeByte(address, (byte) (value >> 8));
        writeByte(address + 1, (byte) (value & 0xff));
    }
    
    public void writeWord(int address, int value) {
        writeByte(address, (byte) (value >> 24));
        writeByte(address + 1, (byte) ((value >> 16) & 0xff));
        writeByte(address + 2, (byte) ((value >> 8) & 0xff));
        writeByte(address + 3, (byte) (value & 0xff));
    }
    
    public int[] readWords(int address, int size) {
        int[] out = new int[size];
        for (int i = 0; i < size; i++) {
            out[i] = readWord(address + (i * 4));
        }
        return out;
    }
    
    public String readString(int startAddress, int length) {
        String str = "";
        for (int i = 0; i < length; i++) {
            str += (char) readByte(startAddress++);
        }
        return str;
    }

    public String readStringNT(int startAddress) {
        char c = (char) readByte(startAddress++);
        String str = "";
        while (c != 0x0) {
            str += c;
            c = (char) readByte(startAddress++);
        }
        return str;
    }
    
    public void writeString(int startAddress, String str) {
        for (int i = 0; i < str.length(); i++) {
            writeByte(startAddress++, (byte)str.charAt(i));
        }
    }
}
