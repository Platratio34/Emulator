import SysD;

namespace Console {

    public static const char* CONSOLE_OUT = 0x1_0300;
    public static const char* CONSOLE_IN = 0x1_0301;
    public static const uint8* CONSOLE_IN_COUNT = 0x1_0302;

    public static void printChar(char c) {
        asm("LOAD r1 Console.CONSOLE_OUT");
        asm("COPY r15 r2\nINC r2 -12\nLOAD MEM r2 r2"); // c
        asm("STORE BYTE r2 r1");
    }

    public static void printStr(char* str, uint32 len) {
        asm("COPY r15 r14\nINC r14 -12\nLOAD MEM r14 r14"); // len
        asm("LOAD r1 Console.CONSOLE_OUT"); // consolePntr
        asm("COPY r15 r2\nINC r2 -16\nLOAD MEM r2 r2"); // str
        asm("GOTO GT r14 :printStr_len");
            asm(":printStr_l1");
                asm("LOAD MEM BYTE r3 r2\nGOTO EQ r3 :printStr_l1_exit");
                asm("STORE BYTE r3 r1\nINC r2 1\nGOTO :printStr_l1");
            asm(":printStr_l1_exit\nGOTO :printStr_exit");
        asm(":printStr_len");
            asm("COPY MEM BYTE r2 r1 INC_RS");
            asm("INC r14 -1\nGOTO GT r14 :printStr_len");
        asm(":printStr_exit");
    }

    public static void intToHex(uint32 value, char* str) {
        asm("LOAD r14 7");
        asm("COPY r15 r1\nINC r1 -16\nLOAD MEM r1 r1"); // value
        asm("COPY r15 r2\nINC r2 -12\nLOAD MEM r2 r2"); // str
        asm("LOAD r3 0xf\nLOAD r6 0xa");
        asm(":intToHex_l1");
            asm("LRT r1 r1 4\nAND r4 r1 r3");
            asm("SUB r5 r4 r6\nGOTO GEQ r5 :intToHex_gt");
                asm("INC r4 0x30\nSTORE BYTE r4 r2 INC_RA\nGOTO :intToHex_l1_end");
            asm(":intToHex_gt");
                asm("INC r4 0x57\nSTORE BYTE r4 r2 INC_RA");
            asm(":intToHex_l1_end\nINC r14 -1\nGOTO GEQ r14 :intToHex_l1");
    }
    
    public static void intToDec(uint32 value, char* str) {
        asm("COPY r15 r1\nINC r1 -16\nLOAD MEM r1 r1"); // value
        asm("COPY r15 r2\nINC r2 -12\nLOAD MEM r2 r2"); // str
        asm("COPY rStack r3\n#stackVar char[16] tempStr\nSTACK INC 16"); // char* str2
        asm("LOAD r4 10\nLOAD r5 0x30\nLOAD r6 0x0");
        asm(":intToDec_l1");
            asm("DIV r1 r1 r4\nCOPY rAF r7\nADD r7 r7 r5");
            asm("STORE BYTE r7 r3 INC_RA\nINC r6");
            asm("GOTO NEQ r1 :intToDec_l1");
        asm("INC r3 -1");
        asm(":intToDec_l2");
            asm("COPY MEM BYTE r3 r2 INC_RD\nINC r6 -1\nINC r3 -1");
            asm("GOTO NEQ r6 :intToDec_l2");
        asm("STORE BYTE 0x0 r2\n#stackVarClear tempStr");
        /*
        :loop
        r1 = r1 / r4
        r7 = rAF + r5
        *(r3++) = r7
        r6++
        if(r1 != 0) GOTO :loop
        :loop2
        *(r2++) = *r3
        r6--
        if(r6 != 0) GOTO :loop2
        */
        // asm("LOAD r3 0xf\nLOAD r6 0xa");
        // asm(":intToHex_l1");
        //     asm("LRT r1 r1 4\nAND r4 r1 r3");
        //     asm("SUB r5 r4 r6\nGOTO GEQ r5 :intToHex_gt");
        //         asm("INC r4 0x30\nSTORE BYTE r4 r2 INC_RA\nGOTO :intToHex_l1_end");
        //     asm(":intToHex_gt");
        //         asm("INC r4 0x57\nSTORE BYTE r4 r2 INC_RA");
        //     asm(":intToHex_l1_end\nINC r14 -1\nGOTO GEQ r14 :intToHex_l1");
    }

    public static void read(char* buffer, uint32 bufferSize) {
        asm("LOAD r1 Console.CONSOLE_IN_COUNT\n:read_l0\nLOAD MEM BYTE r2 r1\nGOTO EQ r2 :read_l0");
        uint32 inCount = *CONSOLE_IN_COUNT;
        if(bufferSize < inCount) {
            inCount = bufferSize;
        }
        uint32 i = 0;
        while(i < inCount) {
            buffer[i] = *CONSOLE_IN;
            // inCount--;
            i++;
        }
        if(i < bufferSize) {
            buffer[i] = '\0';
        }
    }
}