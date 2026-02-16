import SysD;

namespace Kernal;

class Console {
    private void* address;
    private uint32 deviceId;
    private uint32 end;
    private uint32 consoleSize;
    private uint32 index;
    protected void* consolePtr;

    public Console(void* address, uint32 deviceId, uint32 consoleSize) {
        this.address = address;
        this.deviceId = deviceId;
        this.consoleSize = consoleSize;
        end = address + (consoleSize * 2) + 1;
        Kernal.peripheralCmd(deviceId, 3, new uint32[] {0x0001,address,consoleSize});
    }

    public void printChar(char c) {
        address[index] = c;
        index++;
        address[index] = 0x1;
        index++;
        if(index > consoleSize) {
            index = 0;
        }
    }

    public void print(char* str, uint32 len) {
        if(len == 0) {
            uint32 i = 0;
            while(str[i] != 0) {
                printChar(str[i]);
                i++;
            }
        } else {
            for(uint32 i = 0; i < len; i++) {
                printChar(str[i]);
            }
        }
    }
}