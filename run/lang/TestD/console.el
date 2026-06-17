import SysD;

namespace TestD {

    public static const uint32* CMD_ADDR = 0x2_0000;
    public static const uint32* CMD_STATUS = 0x2_0001;
    public static const uint32* CMD_DEVICE = 0x2_0002;
    public static const uint32* CMD_SIZE = 0x2_0004;
    public static const uint32* CMD_START = 0x2_0008;
    public static const uint32 CMD_WRITTEN = 0x0001;
    
    public static const uint32 CONSOLE_START = 0x2_0100;
    public static const uint32 CONSOLE_END = 0x2_0140;
    public static void* consolePntr = CONSOLE_START;

    static final uint32[3] CONSOLE_SETUP_CMD = {0x0001,0x2_0100,0x0020};

    protected static void peripheralCommand(uint32 deviceId, uint32 cmdSize, uint32* cmd) {
        // asm("LOAD r1 TestD.CMD_DEVICE");
        // asm("COPY r15 r2\nINC r2 -20\nLOAD MEM r2 r2");
        // asm("STORE r1 r2");

        *CMD_SIZE = cmdSize;
        
        asm("LOAD r1 TestD.CMD_START");

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
        *consolePntr = c;
        consolePntr += 4;
        *consolePntr = 0x1;
        consolePntr += 4;
        if(consolePntr > CONSOLE_END) {
            consolePntr = CONSOLE_START;
        }
    }

    public static void printStr(char* str, uint32 len) {
        uint32 i = 0;
        if(len == 0) {
            while(str[i] != 0) {
                *consolePntr = str[i];
                consolePntr += 4;
                *consolePntr = 0x1;
                consolePntr += 4;
                if(consolePntr > CONSOLE_END) {
                    consolePntr = CONSOLE_START;
                }
                i++;
            }
        } else {
            while(i < len) {
                *consolePntr = str[i];
                consolePntr += 4;
                *consolePntr = 0x1;
                consolePntr += 4;
                if(consolePntr > CONSOLE_END) {
                    consolePntr = CONSOLE_START;
                }
                i++;
            }
        }
    }
}