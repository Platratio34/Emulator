import SysD;

namespace CharacterDisplay {
    
    public static uint32 deviceId = 0;

    protected static char* charBuffer = 0;
    protected static char* charColorBuffer = 0;

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

    public static void setup() {
        uint32[1] msg = {0x01};
        // peripheralCommand(0, 1, &msg);
        // if(*RSP_STATUS != 0x01) {
        //     return;
        // }
        // ListEntry* entry = 0x1_0084;
        // while(entry.id != 0 && entry < 0x1_0100) {
        //     if(entry.type == 0x0100_0011) {
        //         deviceId = entry.id;
        //         break;
        //     }
        //     entry++;
        // }
        deviceId = 1;
        while(deviceId < 64) {
            if(PERIPHERAL_TABLE[deviceId] == 0x0100_0011) {
                break;
            }
            deviceId++;
        }
        if(deviceId == 64) {
            deviceId = 0;
            return;
        }
        uint32[2] msg2 = {0x02, deviceId};
        peripheralCommand(0, 2, &msg2);
        if(*RSP_STATUS != 0x01) {
            return;
        }
        DeviceDescriptor* desc = 0x1_0084;

        uint32 size = desc.width * desc.height;
        charBuffer = Memory.malloc(size);
        if(charBuffer == 0) {
            return;
        }
        // charColorBuffer = Memory.malloc(size);
        // if(charColorBuffer == 0) {
        //     return;
        // }
        uint32[2] msg3 = {0x01, charBuffer};
        peripheralCommand(deviceId, 2, &msg3);
    }

    public static void write(uint32 index, char data) {
        charBuffer[index] = data;
    }

    struct ListEntry {
        public uint32 id;
        public uint32 type;
    }

    struct DeviceDescriptor {
        public uint32 id;
        public uint32 type;
        public char[16] manufacturer;
        public char[16] serial;
        public uint32 width;
        public uint32 height;
        public uint32[4] extra;
    }
}