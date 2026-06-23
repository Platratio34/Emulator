import SysD;

namespace FS {

    public static const uint32* CMD_ADDR = 0x2_0000;
    public static const uint32* CMD_STATUS = 0x2_0001;
    public static const uint32* CMD_DEVICE = 0x2_0002;
    public static const uint32* CMD_SIZE = 0x2_0004;
    public static const uint32* CMD_START = 0x2_0008;

    public static const uint32 CMD_WRITTEN = 0x0001;
    
    public static const uint32* RSP_STATUS = 0x2_0080;
    public static const uint32* RSP_DATA = 0x2_0084;
    public static const uint32* RSP_DATA_2 = 0x2_0088;
    public static const uint32* RSP_DATA_3 = 0x2_008c;

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

    public static void openFile(char* path, out uint32& status, out uint32& handle) {
        uint32[2] msg = {0x10, path};
        peripheralCommand(3, 2, &msg);
        status = *RSP_STATUS;
        handle = *RSP_DATA_2;
    }

    public static void readFile(uint32 handle, void* buffer, uint32 size, uint32 offset, out uint32& read, out uint32& state) {
        uint32[5] msg = {0x11, handle, buffer, size, offset};
        peripheralCommand(3, 5, &msg);
        state = *RSP_DATA;
        read = *RSP_DATA_3;
    }
}