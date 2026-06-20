import SysD;

namespace Console {

    public static const uint32* CMD_ADDR = 0x2_0000;
    public static const uint32* CMD_STATUS = 0x2_0001;
    public static const uint32* CMD_DEVICE = 0x2_0002;
    public static const uint32* CMD_SIZE = 0x2_0004;
    public static const uint32* CMD_START = 0x2_0008;
    public static const uint32 CMD_WRITTEN = 0x0001;
    
    public static const uint32 CONSOLE_START = 0x2_0100;
    public static const uint32 CONSOLE_END = 0x2_0200;
    public static void* consolePntr = CONSOLE_START;

    static final uint32[3] CONSOLE_SETUP_CMD = {0x0001,0x2_0100,0x2_0200};

    protected static void peripheralCommand(uint32 deviceId, uint32 cmdSize, uint32* cmd) {
        *CMD_SIZE = cmdSize;
        
        asm("LOAD r1 Console.CMD_START");

        asm("COPY r15 r3\nINC r3 -12\nLOAD MEM r3 r3");

        asm(":peripheralCommand_l0");

        asm("COPY MEM r3 r1");
        asm("INC r1 4\nINC r3 4\nINC r2 -1");

        asm("GOTO GT r2 :peripheralCommand_l0");

        *CMD_ADDR = 0x0101_0000 | deviceId;
    }

    public static void setupConsole() {
        peripheralCommand(0x001, 0x003, &CONSOLE_SETUP_CMD);
    }

    public static void printChar(char c) {
        asm("LOAD r1 Console.CONSOLE_START");
        asm("COPY r15 r2\nINC r2 -12\nLOAD MEM r2 r2"); // c
        asm("STORE BYTE r2 r1");
    }

    public static void printStr(char* str, uint32 len) {
        asm("COPY r15 r14\nINC r14 -12\nLOAD MEM r14 r14"); // len
        asm("LOAD r1 Console.CONSOLE_START"); // consolePntr
        asm("COPY r15 r2\nINC r2 -16\nLOAD MEM r2 r2"); // str
        asm("LOAD r4 Console.CONSOLE_END\nLOAD r5 Console.CONSOLE_START");
        asm("GOTO GT r14 :printStr_len");
            asm(":printStr_l1");
                asm("LOAD MEM BYTE r3 r2\nGOTO EQ r3 :printStr_l1_exit");
                asm("STORE BYTE r3 r1\nINC r2 1\nGOTO :printStr_l1");
            asm(":printStr_l1_exit");
            asm("GOTO :printStr_exit");
        asm(":printStr_len");
            asm(":printStr_l2");
                asm("COPY BYTE MEM r2 r1 INC_RG");
                asm("INC r14 -1\nGOTO GT r14 :printStr_l2");
        asm(":printStr_exit");
        asm("LOAD r3 &Console.consolePntr\nSTORE r1 r3");
    }

    public static void intToHex(uint32 value, char* str) {
        uint32 i = 7;
        // asm("#breakpoint");
        while(i >= 0) {
            uint32 part = value & 0xf;
            if(part < 0xa) {
                str[i] = part + 0x30;
            } else {
                str[i] = part + 0x57;
            }
            value = value >> 4;
            i--;
        }
    }
}