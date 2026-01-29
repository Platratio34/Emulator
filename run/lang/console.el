import SysD;

namespace Kernal;

class Console {
    private void* address;
    private uint32 deviceId;
    private uint32 end;
    private uint32 consoleSize;
    protected void* consolePtr;

    public Console(void* address, uint32 deviceId, uint32 consoleSize) {
        this.address = address;
        this.deviceId = deviceId;
        this.consoleSize = consoleSize;
        end = address + (consoleSize * 2) + 1;
        Kernal.peripheralCmd(deviceId, 3, new uint32[] {0x0001,address,consoleSize});
    }

    public void printChar(const char c) {
        SysD.memSet(consolePtr++, c);
        SysD.memSet(consolePtr++, 0x1);
        if(consolePtr >= end) {
            consolePtr = address;
        }
    }

    public void print(const char* str, uint32 len) {
        if(len == 0) {
            uint32 i = 0;
            while(str[i] != 0) {
                printChar(str[i++]);
            }
        } else {
            for(uint32 i = 0; i < len; i++) {
                printChar(str[i]);
            }
        }
    }
}