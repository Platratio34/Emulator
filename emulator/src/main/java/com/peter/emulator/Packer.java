package com.peter.emulator;

public class Packer {

    public static final int[] packChar(String str, int length) {
        int[] arr = new int[Math.ceilDiv(length, 4)];
        int aI = 0;
        int aS = 24;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int v = (int) c << aS;
            arr[aI] = v;
            aS -= 8;
            if (aS < 0) {
                aI++;
                aS = 24;
            }
        }
        return arr;
    }
}
