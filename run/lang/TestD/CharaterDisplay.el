import SysD;

namespace CharacterDisplay {
    
    public static uint32 deviceId = 0;

    protected static char[960] charBuffer;
    protected static char[960] charColorBuffer;

    public static uint32 width;
    public static uint32 height;

    public static void setup() {
        asm("#breakpoint");
        deviceId = 1;
        while((deviceId < 64) && (SysD.Peripheral.TABLE[deviceId] != SysD.Peripheral.TYPE_DISPLAY_CHARACTER)) {
            deviceId++;
        }
        if(deviceId == 64) {
            deviceId = 0;
            return;
        }
        uint32[2] msg2 = {0x01, deviceId};
        SysD.Peripheral.command(0, 2, &msg2);
        if(*SysD.Peripheral.RSP_STATUS != 0x01) {
            return;
        }
        width = SysD.Peripheral.RSP_DATA[10];
        height = SysD.Peripheral.RSP_DATA[11];
        uint32[2] msg3 = {0x01, &charBuffer};
        SysD.Peripheral.command(deviceId, 2, &msg3);
    }

    public static void write(uint32 index, char data) {
        charBuffer[index] = data;
    }

    public static void write(uint32 x, uint32 y, char data) {
        charBuffer[x + (y * width)] = data;
    }
    public static void write(uint32 x, uint32 y, char* str) {
        uint32 i = 0;
        while(str[i] != '\0' && x < width) {
            charBuffer[x + (y * width)] = str[i];
            x++;
            i++;
        }
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