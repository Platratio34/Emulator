import SysD;

namespace Console {

    public static const uint32* CMD_ADDR = 0x2_0000;
    public static const uint32* CMD_STATUS = 0x2_0001;
    public static const uint32* CMD_DEVICE = 0x2_0002;
    public static const uint32* CMD_SIZE = 0x2_0004;
    public static const uint32* CMD_START = 0x2_0008;
    public static const uint32 CMD_WRITTEN = 0x0001;
    
    public static const char* CONSOLE_OUT = 0x2_0100;
    public static const char* CONSOLE_IN = 0x2_0101;
    public static const uint8* CONSOLE_IN_COUNT = 0x2_0102;

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
            asm("COPY BYTE MEM r2 r1 INC_RG");
            asm("INC r14 -1\nGOTO GT r14 :printStr_len");
        asm(":printStr_exit");
    }

    public static void intToHex(uint32 value, char* str) {
        asm("LOAD r14 7");
        asm("COPY r15 r1\nINC r1 -16\nLOAD MEM r1 r1"); // value
        asm("COPY r15 r2\nINC r2 -12\nLOAD MEM r2 r2\nINC r2 8"); // str
        asm("LOAD r3 0xf\nLOAD r6 0xa");
        asm(":intToHex_l1");
            asm("INC r2 -1\nAND r4 r1 r3\nRSH r1 r1 4");
            asm("SUB r5 r4 r6\nGOTO GEQ r5 :intToHex_gt");
                asm("INC r4 0x30\nSTORE BYTE r4 r2\nGOTO :intToHex_l1_end");
            asm(":intToHex_gt");
                asm("INC r4 0x57\nSTORE BYTE r4 r2");
            asm(":intToHex_l1_end\nINC r14 -1\nGOTO GEQ r14 :intToHex_l1");
    }

    public static void read(char* buffer, uint32 bufferSize) {
        asm("#breakpoint");
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