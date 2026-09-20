import SysD;

namespace Console {

    public static const char* CONSOLE_OUT = 0x1_0300;
    public static const char* CONSOLE_IN = 0x1_0301;
    public static const uint8* CONSOLE_IN_COUNT = 0x1_0302;

    public static void printChar(char c) {
        asm{
            SUB r2 r15 12
            LOAD MEM BYTE r2 r2 // c

            STORE BYTE r2 Console.CONSOLE_OUT
        }
    }
    
    public static void printStr(char* str) {
        asm{
            SUB r1 r15 12
            LOAD MEM r1 r1 // str
            :printStr_l1
                LOAD MEM BYTE r2 r1
                GOTO EQ r2 :printStr_l1_exit

                STORE BYTE r2 Console.CONSOLE_OUT
                INC r1 1
                GOTO :printStr_l1
            :printStr_l1_exit
        }
    }

    public static void printStr(char* str, int32 len) {
        asm{
            SUB r14 r15 12
            LOAD MEM r14 r14 // len
            LOAD r1 Console.CONSOLE_OUT // consolePntr
            SUB r2 r15 16
            LOAD MEM r2 r2 // str
            :printStr_len
                COPY MEM BYTE r2 r1 INC_RS
                INC r14 -1
                GOTO GT r14 :printStr_len
        }
    }

    public static void intToHex(int32 value, char* str) {
        asm{
            LOAD r14 7
            SUB r1 r15 16
            LOAD MEM r1 r1 // value
            SUB r2 r15 12
            LOAD MEM r2 r2 // str
            LOAD r3 0xf
            :intToHex_l1
                LRT r1 r1 4
                AND r4 r1 r3
                SUB r5 r4 0xa
                GOTO GEQ r5 :intToHex_gt
                    INC r4 0x30
                    STORE BYTE r4 r2 INC_RA
                    GOTO :intToHex_l1_end
                :intToHex_gt
                    INC r4 0x57
                    STORE BYTE r4 r2 INC_RA
                :intToHex_l1_end
                INC r14 -1
                GOTO GEQ r14 :intToHex_l1
        }
    }
    
    public static void intToDec(int32 value, char* str) {
        asm{
            SUB r1 r15 16
            LOAD MEM r1 r1 // value
            SUB r2 r15 12
            LOAD MEM r2 r2 // str
            COPY rStack r3
            #stackVar char[16] tempStr
            STACK INC 16 // char* str2
            LOAD r4 10
            LOAD r6 0x0
            :intToDec_l1
                DIV r1 r1 r4
                COPY rAF r7
                ADD r7 r7 0x30
                STORE BYTE r7 r3 INC_RA
                INC r6
                GOTO NEQ r1 :intToDec_l1
            INC r3 -1
            :intToDec_l2
                COPY MEM BYTE r3 r2 INC_RD
                INC r6 -1
                INC r3 -1
                GOTO NEQ r6 :intToDec_l2
            STORE BYTE 0x0 r2
            #stackVarClear tempStr
        }
    }

    public static void read(char* buffer, int32 bufferSize) {
        asm("LOAD r1 Console.CONSOLE_IN_COUNT\n:read_l0\nLOAD MEM BYTE r2 r1\nGOTO EQ r2 :read_l0");
        int32 inCount = *CONSOLE_IN_COUNT;
        if(bufferSize < inCount) {
            inCount = bufferSize;
        }
        int32 i = 0;
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