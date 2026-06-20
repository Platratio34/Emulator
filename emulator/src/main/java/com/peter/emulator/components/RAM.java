package com.peter.emulator.components;

import java.util.HashMap;

import com.peter.emulator.peripherals.MemoryMappedPeripheral;

public class RAM {

    public final int size;
    private final byte[] mem;
    private final HashMap<Integer,MemoryMappedPeripheral> peripherals = new HashMap<>();

    public RAM() {
        this(0x80_0000);
    }

    public RAM(int size) {
        this.size = size;
        mem = new byte[size];
    }

    public void writeWord(int address, int value) {
        if (address < 0 || address >= size)
            throw new ArrayIndexOutOfBoundsException(address);
        writeByte(address, (byte) (value >> 24));
        writeByte(address + 1, (byte) ((value >> 16) & 0xff));
        writeByte(address + 2, (byte) ((value >> 8) & 0xff));
        writeByte(address + 3, (byte) (value & 0xff));
    }
    
    public void writeShort(int address, int value) {
        writeByte(address, (byte)(value >> 8));
        writeByte(address+1, (byte)(value & 0xff));
    }
    
    public void writeByte(int address, byte value) {
        if(peripherals.containsKey(address)) {
            peripherals.get(address).onUpdate(address, value);
            return;
        }
        if (address < 0 || address >= size)
            throw new ArrayIndexOutOfBoundsException(address);
        mem[address] = value;
    }
    
    public int readWord(int address) {
        return ((readByte(address) & 0xff) << 24) | ((readByte(address+1) & 0xff) << 16) | ((readByte(address+2) & 0xff) << 8) | (readByte(address+3) & 0xff);
    }

    public int readShort(int address) {
        return ( ((int)readByte(address)) << 8 ) | ( (int)readByte(address+1) );
    }

    public byte readByte(int address) {
        if(peripherals.containsKey(address))
            return peripherals.get(address).get(address);
        if (address < 0 || address >= size)
            throw new ArrayIndexOutOfBoundsException(address);
        return mem[address];
    }
    
    public byte[] read(int address, int size) {
        byte[] out = new byte[size];
        for (int i = 0; i < size; i++) {
            out[i] = readByte(address + i);
        }
        return out;
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

    public void copy(byte[] data) {
        copy(data, 0, data.length);
    }

    public void copy(byte[] data, int start) {
        copy(data, start, data.length);
    }

    public void copy(byte[] data, int start, int length) {
        if (length > data.length) {
            throw new RuntimeException(
                    "Length argument must be less than or equal to data length; Data length: " + data.length + "; Got "
                            + length);
        }
        System.arraycopy(data, 0, mem, start, length);
    }
    
    
    public void copyWords(int[] data) {
        copyWords(data, 0, data.length);
    }
    public void copyWords(int[] data, int start) {
        copyWords(data, start, data.length);
    }
    public void copyWords(int[] data, int start, int length) {
        if (length > data.length) {
            throw new RuntimeException(
                    "Length argument must be less than or equal to data length; Data length: " + data.length + "; Got "
                            + length);
        }
        for (int i = 0; i < length; i++) {
            writeWord(start + (i * 4), data[i]);
        }
    }

    private String toHex(int num) {
        String str = String.format("%x", num);
        while (str.length() < 8) {
            str = "0" + str;
        }
        return str.substring(0,4)+"_"+str.substring(4);
    }

    public String debugPrint(int start, int rows) {
        String str = "           ";
        for (int i = 0; i < 16; i++) {
            str += String.format("     0x%02x  ", i * 4);
            // str += "     0x" + Integer.toHexString(i*4) + "   ";
        }
        str += "\n";
        str += "          +";
        for (int i = 0; i < 16; i++) {
            str += "-----------";
        }
        str += "\n";
        for (int r = 0; r < rows; r++) {
            int rs = start + (r*16*4);
            int[] words = readWords(rs, 16);
            str += toHex(rs)+" |";
            for (int c = 0; c < 16; c++) {
                str += "  " + toHex(words[c]);
            }
            str += "\n";
        }

        return str;
    }

    public void addMMP(MemoryMappedPeripheral peripheral) {
        for(int addr : peripheral.getAddresses()) {
            peripherals.put(addr, peripheral);
        }
    }
}
