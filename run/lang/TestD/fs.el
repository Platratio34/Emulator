import Peripheral;

namespace FS {

    protected static int32 deviceId = 0;

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

    public static void openFile(char* path, out int32& status, out int32& handle) {
        if(deviceId == 0) {
            if(!setup()) {
                status = 0xff;
                return;
            }
        }
        asm{
            LOAD r1 Peripheral.CMD_SIZE
            STORE 2 r1 INC_RA

            LOAD r1 Peripheral.CMD_DATA
            STORE 0x10 r1 INC_RA
            COPY r15 r2
            INC r2 -20
            COPY MEM r2 r1

            LOAD r1 Peripheral.CMD_ADDR
            LOAD r2 &FS.deviceId
            LOAD MEM r2 r2
            LOAD r3 0x0101_0000
            OR r2 r2 r3
            STORE r2 r1
        }

        status = *Peripheral.RSP_STATUS;
        handle = Peripheral.RSP_DATA[1];
    }

    public static void readFile(int32 handle, void* buffer, int32 size, int32 offset, int32* read, out int32& state) {
        if(deviceId == 0) {
            if(!setup()) {
                state = 0xff;
                return;
            }
        }
        asm{
            LOAD r1 Peripheral.CMD_SIZE
            STORE 6 r1 INC_RA
       
            LOAD r1 Peripheral.CMD_DATA
            STORE 0x11 r1 INC_RA
            
            COPY r15 r2
            INC r2 -32
            
            COPY MEM r2 r1 INC_RS INC_RD
            COPY MEM r2 r1 INC_RS INC_RD
            COPY MEM r2 r1 INC_RS INC_RD
            COPY MEM r2 r1 INC_RS INC_RD
            COPY MEM r2 r1 INC_RS INC_RD

            LOAD r1 Peripheral.CMD_ADDR
            LOAD r2 &FS.deviceId
            LOAD MEM r2 r2
            LOAD r3 0x0101_0000
            OR r2 r2 r3
            STORE r2 r1
        }

        state = *Peripheral.RSP_DATA;
    }

    public static void readFileSync(int32 handle, void* buffer, int32 size, int32 offset, out int32& read, out int32& state) {
        if(deviceId == 0) {
            if(!setup()) {
                state = 0xff;
                return;
            }
        }
        asm{
            LOAD r1 Peripheral.CMD_SIZE
            STORE 6 r1 INC_RA

            STORE 0x11 r1 INC_RA
            COPY r15 r2
            INC r2 -32

            COPY MEM r2 r1 INC_RS INC_RD
            COPY MEM r2 r1 INC_RS INC_RD
            COPY MEM r2 r1 INC_RS INC_RD
            COPY MEM r2 r1 INC_RS INC_RD
            COPY MEM r2 r1 INC_RS INC_RD

            LOAD r1 Peripheral.CMD_ADDR
            LOAD r2 &FS.deviceId
            LOAD MEM r2 r2
            LOAD r3 0x0101_0000
            OR r2 r2 r3\nSTORE r2 r1
        }
        
        state = *Peripheral.RSP_DATA;

        asm{
            COPY r15 r1
            INC r1 -16
            LOAD MEM r1 r1
            :FS.readFileSync_wait
            LOAD MEM r2 r1
            GOTO LT r2 :FS.readFileSync_wait
        };
    }
}