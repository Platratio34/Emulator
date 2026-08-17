import SysD;

namespace FS {

    protected static uint32 deviceId = 0;

    protected static bool setup() {
        deviceId = 1;
        while((deviceId < 64) && (SysD.Peripheral.TABLE[deviceId] != SysD.Peripheral.TYPE_STORAGE_VIRTUAL)) {
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
        SysD.Peripheral.command(deviceId, 2, &msg);
        status = *SysD.Peripheral.RSP_STATUS;
        handle = SysD.Peripheral.RSP_DATA[1];
    }

    public static void readFile(uint32 handle, void* buffer, uint32 size, uint32 offset, out uint32& read, out uint32& state) {
        if(deviceId == 0) {
            setup();
            if(deviceId == 0) {
                state = 0xff;
            }
        }
        uint32[5] msg = {0x11, handle, buffer, size, offset};
        SysD.Peripheral.command(deviceId, 5, &msg);
        state = *SysD.Peripheral.RSP_DATA;
        read = SysD.Peripheral.RSP_DATA[2];
    }
}