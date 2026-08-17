import SysD as SysD;

namespace FS {

    public static const uint32* CMD_ADDR = 0x1_0000;
    public static const uint32* CMD_STATUS = 0x1_0001;
    public static const uint32* CMD_DEVICE = 0x1_0002;
    public static const uint32* CMD_SIZE = 0x1_0004;
    public static const uint32* CMD_START = 0x1_0008;

    public static const uint32 CMD_WRITTEN = 0x0001;
    
    public static const uint32* RSP_STATUS = 0x1_0080;
    public static const uint32* RSP_DATA = 0x1_0084;
    public static const uint32* RSP_DATA_2 = 0x1_0088;
    public static const uint32* RSP_DATA_3 = 0x1_008c;

    public static const uint32* PERIPHERAL_TABLE = 0x1_0100;
    
    public static const uint32 STORAGE_DEVICE_TYPE = 0x0100_0001;

    protected static uint32 deviceId = 0;

    protected static void peripheralCommand(uint32 deviceId, uint32 cmdSize, uint32* cmd) {
        asm("LOAD r1 Console.CMD_SIZE\nCOPY r15 r2\nINC r2 -16\nLOAD MEM r2 r2");
        asm("STORE r2 r1");
        
        asm("LOAD r1 Console.CMD_START");

        asm("COPY r15 r3\nINC r3 -12\nLOAD MEM r3 r3");

        asm(":peripheralCommand_l0");

        asm("COPY MEM r3 r1 INC_RS INC_RD\nINC r2 -1");

        asm("GOTO GT r2 :peripheralCommand_l0");

        *CMD_ADDR = 0x0101_0000 | deviceId;
    }

    protected static bool setup() {
        deviceId = 1;
        while((deviceId < 64) && (PERIPHERAL_TABLE[deviceId] != STORAGE_DEVICE_TYPE)) {
            deviceId++;
        }
        if(deviceId == 64) {
            deviceId = 0;
            return false;
        }
        return true;
    }

    public static void openFile(char* path, out uint32& status, out uint32& handle) {
        if(deviceId == 0) {
            setup();
            if(deviceId == 0) {
                status = 0xff;
            }
        }
        uint32[2] msg = {0x10, path};
        peripheralCommand(deviceId, 2, &msg);
        status = *RSP_STATUS;
        handle = *RSP_DATA_2;
    }

    public static void readFile(uint32 handle, void* buffer, uint32 size, uint32 offset, out uint32& read, out uint32& state) {
        if(deviceId == 0) {
            setup();
            if(deviceId == 0) {
                state = 0xff;
            }
        }
        uint32[5] msg = {0x11, handle, buffer, size, offset};
        peripheralCommand(deviceId, 5, &msg);
        state = *RSP_DATA;
        read = *RSP_DATA_3;
    }
}