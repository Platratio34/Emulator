import System.Collections.List;

namespace Test {
    static const char* SYS_NAME = "EmulatorOS";
    static const int32[] CONSOLE_SETUP_CMD = {0x0001,CONSOLE_START,0x0020};
    public static List<List<string>> strings;
    
    struct AddressSpace {
        public int32 addressOffset;
        public int32 pid;
        public uint8 type;
        public uint8 state;
    }

    @Entrypoint
    static void _main() {
        peripheralCmd(0x0001, 0x0003, *CONSOLE_SETUP_CMD);
        SysD.memSet(CONSOLE_START, CONSOLE_PNTR);
        Kernal.Memory._setup();
        System.console = new Console(0x0800, 0x0001, 0x0020);
    }
}