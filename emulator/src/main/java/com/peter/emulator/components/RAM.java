package com.peter.emulator.components;

import java.util.HashMap;

import com.peter.emulator.machinecode.Instruction;
import com.peter.emulator.peripherals.MemoryMappedPeripheral;

public class RAM implements BusComponent {

    public final int start;
    public final int numBlocks;
    private final byte[][] blocks;

    public RAM() {
        this(0, 0x80);
    }

    public RAM(int start, int numBlocks) {
        this.start = start;
        this.numBlocks = numBlocks;
        blocks = new byte[numBlocks][];
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getNumBlocks() {
        return numBlocks;
    }

    @Override
    public byte readByte(int address) {
        address -= start;
        int block = address >> 16;
        if (blocks[block] == null) {
            return 0;
        }
        return blocks[block][address & 0xffff];
    }
    
    @Override
    public void writeByte(int address, byte value) {
        address -= start;
        int block = address >> 16;
        if (blocks[block] == null) {
            blocks[block] = new byte[0x1_0000];
        }
        blocks[block][address & 0xffff] = value;
    }
    
    private int readWord(int address) {
        return ((readByte(address) & 0xff) << 24) | ((readByte(address+1) & 0xff) << 16) | ((readByte(address+2) & 0xff) << 8) | (readByte(address+3) & 0xff);
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

    // public void copy(byte[] data) {
    //     copy(data, 0, data.length);
    // }

    // public void copy(byte[] data, int start) {
    //     copy(data, start, data.length);
    // }

    // public void copy(byte[] data, int start, int length) {
    //     if (length > data.length) {
    //         throw new RuntimeException(
    //                 "Length argument must be less than or equal to data length; Data length: " + data.length + "; Got "
    //                         + length);
    //     }
    //     // System.arraycopy(data, 0, mem, start, length);
    //     int bI = start >> 16;
    //     int sourceStart = 0;
    //     while (bI < numBlocks && length > 0) {
    //         if (blocks[bI] == null) {
    //             blocks[bI] = new byte[0x1_0000];
    //         }
    //         int blockStart = start - (bI << 16);
    //         int bLength = Math.min(0x1_0000 - blockStart, length);
    //         System.arraycopy(data, sourceStart, blocks[bI], blockStart, bLength);
    //         length -= bLength;
    //         sourceStart += bLength;
    //         start = (bI + 1) << 16;
    //         bI = start >> 16;
    //     }
    // }

    public void fill(byte[] bytes) {
        int bI = 0;
        int start = 0;
        while (start < bytes.length) {
            if (blocks[bI] == null) {
                blocks[bI] = new byte[0x1_0000];
            }
            int len = Math.min(0x1_0000 - start, 0x1_0000);
            if (len > bytes.length) {
                len = bytes.length;
            }
            System.arraycopy(bytes, start, blocks[bI], 0, len);
            start += 0x1_0000;
            bI++;
        }
    }
    public void fill(int[] words) {
        int bI = 0;
        int start = 0;
        while (start < words.length) {
            if (blocks[bI] == null) {
                blocks[bI] = new byte[0x1_0000];
            }
            for(int i = 0; i < 0x1_0000; i += 4) {
                int wordI = (i / 4) + start;
                if (wordI >= words.length) {
                    break;
                }
                // System.out.println(Instruction.toHexLead(i, 4) + " " + Instruction.toHexLead(words[wordI]));
                blocks[bI][i] = (byte) (words[wordI] >>> 24);
                blocks[bI][i + 1] = (byte) (words[wordI] >>> 16);
                blocks[bI][i + 2] = (byte) (words[wordI] >>> 8);
                blocks[bI][i + 3] = (byte) words[wordI];
            }
            start += 0x4000;
            bI++;
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
}
