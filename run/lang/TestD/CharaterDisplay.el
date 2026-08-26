import Peripheral;

namespace CharacterDisplay {
    
    public static int32 deviceId = 0;

    protected static char[960] charBuffer;
    protected static char[960] charColorBuffer;

    public static int32 width;
    public static int32 height;

    public static void setup() {
        deviceId = 1;
        while((deviceId < 64) && (Peripheral.TABLE[deviceId] != Peripheral.TYPE_DISPLAY_CHARACTER)) {
            deviceId++;
        }
        if(deviceId == 64) {
            deviceId = 0;
            return;
        }
        // asm("NO_OP\n#breakpoint");
        int32[2] msg2 = {0x01, deviceId};
        Peripheral.command(0, 2, &msg2);
        if(*Peripheral.RSP_STATUS != 0x01) {
            return;
        }
        // asm("NO_OP\n#breakpoint");
        width = Peripheral.RSP_DATA[10];
        height = Peripheral.RSP_DATA[11];
        int32[2] msg3 = {0x01, &charBuffer};
        // asm("NO_OP\n#breakpoint");
        Peripheral.command(deviceId, 2, &msg3);
    }

    public static void write(int32 index, char data) {
        charBuffer[index] = data;
    }

    public static void write(int32 x, int32 y, char data) {
        charBuffer[x + (y * width)] = data;
    }
    public static void write(int32 x, int32 y, char* str) {
        int32 i = 0;
        while(str[i] != '\0' && x < width) {
            charBuffer[x + (y * width)] = str[i];
            x++;
            i++;
        }
    }

    struct ListEntry {
        public int32 id;
        public int32 type;
    }

    struct DeviceDescriptor {
        public int32 id;
        public int32 type;
        public char[16] manufacturer;
        public char[16] serial;
        public int32 width;
        public int32 height;
        public int32[4] extra;
    }
}