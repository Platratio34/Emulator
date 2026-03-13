package com.peter.emulator.components;

public class RAM {

    public final int size;
    private final byte[] mem;

    public RAM() {
        this(0x8_0000);
    }

    public RAM(int size) {
        this.size = size;
        mem = new byte[size];
    }

    public void writeWord(int address, int value) {
        if (address < 0 || address >= size)
            throw new ArrayIndexOutOfBoundsException(address);
        mem[address] = (byte) (value >> 24);
        mem[address + 1] = (byte) ((value >> 16) & 0xff);
        mem[address + 2] = (byte) ((value >> 8) & 0xff);
        mem[address + 3] = (byte) (value & 0xff);
    }
    
    public void writeByte(int address, byte value) {
        if (address < 0 || address >= size)
            throw new ArrayIndexOutOfBoundsException(address);
        mem[address] = value;
    }
    
    public int readWord(int address) {
        if (address < 0 || address >= size)
            throw new ArrayIndexOutOfBoundsException(address);
        return ((mem[address] & 0xff) << 24) | ((mem[address+1] & 0xff) << 16) | ((mem[address+2] & 0xff) << 8) | (mem[address+3] & 0xff);
    }

    public byte readByte(int address) {
        if (address < 0 || address >= size)
            throw new ArrayIndexOutOfBoundsException(address);
        return mem[address];
    }
    
    public byte[] read(int address, int size) {
        if (address < 0 || address + size > this.size)
            throw new ArrayIndexOutOfBoundsException(address);
        byte[] out = new byte[size];
        for (int i = 0; i < size; i++) {
            out[i] = mem[address + i];
        }
        return out;
    }
    
    public int[] readWords(int address, int size) {
        if (address < 0 || address+size > this.size)
            throw new ArrayIndexOutOfBoundsException(address);
        int[] out = new int[size];
        for (int i = 0; i < size; i++) {
            out[i] = readWord(address + (i*4));
        }
        return out;
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
            str += "     0x" + Integer.toHexString(i) + "   ";
        }
        str += "\n";
        str += "          +";
        for (int i = 0; i < 16; i++) {
            str += "-----------";
        }
        str += "\n";
        for (int r = 0; r < rows; r++) {
            int rs = start + (r*16);
            str += toHex(rs)+" |";
            for (int c = 0; c < 16; c++) {
                str += "  " + toHex(mem[rs + c]);
            }
            str += "\n";
        }

        return str;
    }
}
