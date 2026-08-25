import Peripheral;

namespace FS {

    protected static uint32 deviceId = 0;

    protected static bool setup() {
        deviceId = 1;
        while((deviceId < 64) && (Peripheral.TABLE[deviceId] != Peripheral.TYPE_STORAGE_VIRTUAL)) {
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
            if(!setup()) {
                status = 0xff;
                return;
            }
        }
        asm("LOAD r1 Peripheral.CMD_SIZE\nSTORE 2 r1 INC_RA");
        asm("LOAD r1 Peripheral.CMD_DATA");
        asm("STORE 0x10 r1 INC_RA");
        asm("COPY r15 r2\nINC r2 -20");
        asm("COPY MEM r2 r1");
        asm("LOAD r1 Peripheral.CMD_ADDR\nLOAD r2 &FS.deviceId\nLOAD MEM r2 r2\nLOAD r3 0x0101_0000\nOR r2 r2 r3\nSTORE r2 r1");

        status = *Peripheral.RSP_STATUS;
        handle = Peripheral.RSP_DATA[1];
    }

    public static void readFile(uint32 handle, void* buffer, uint32 size, uint32 offset, uint32* read, out uint32& state) {
        if(deviceId == 0) {
            if(!setup()) {
                state = 0xff;
                return;
            }
        }
        asm("LOAD r1 Peripheral.CMD_SIZE\nSTORE 6 r1 INC_RA");
        asm("LOAD r1 Peripheral.CMD_DATA");
        asm("STORE 0x11 r1 INC_RA");
        asm("COPY r15 r2\nINC r2 -32");
        asm("COPY MEM r2 r1 INC_RS INC_RD");
        asm("COPY MEM r2 r1 INC_RS INC_RD");
        asm("COPY MEM r2 r1 INC_RS INC_RD");
        asm("COPY MEM r2 r1 INC_RS INC_RD");
        asm("COPY MEM r2 r1 INC_RS INC_RD");
        asm("LOAD r1 Peripheral.CMD_ADDR\nLOAD r2 &FS.deviceId\nLOAD MEM r2 r2\nLOAD r3 0x0101_0000\nOR r2 r2 r3\nSTORE r2 r1");

        state = *Peripheral.RSP_DATA;
    }

    public static void readFileSync(uint32 handle, void* buffer, uint32 size, uint32 offset, out uint32& read, out uint32& state) {
        if(deviceId == 0) {
            if(!setup()) {
                state = 0xff;
                return;
            }
        }
        asm("LOAD r1 Peripheral.CMD_SIZE\nSTORE 6 r1 INC_RA");
        asm("STORE 0x11 r1 INC_RA");
        asm("COPY r15 r2\nINC r2 -32");
        asm("COPY MEM r2 r1 INC_RS INC_RD");
        asm("COPY MEM r2 r1 INC_RS INC_RD");
        asm("COPY MEM r2 r1 INC_RS INC_RD");
        asm("COPY MEM r2 r1 INC_RS INC_RD");
        asm("COPY MEM r2 r1 INC_RS INC_RD");
        asm("LOAD r1 Peripheral.CMD_ADDR\nLOAD r2 &FS.deviceId\nLOAD MEM r2 r2\nLOAD r3 0x0101_0000\nOR r2 r2 r3\nSTORE r2 r1");
        
        state = *Peripheral.RSP_DATA;
        asm("COPY r15 r1\nINC r1 -16\nLOAD MEM r1 r1");
        asm(":FS.readFileSync_wait\nLOAD MEM r2 r1\nGOTO LT r2 :FS.readFileSync_wait");
    }
}