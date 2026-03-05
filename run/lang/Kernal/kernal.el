import SysD;

namespace Kernal {

    static const uint32 CMD_STATUS = 0x8001;
    static const uint32 CMD_DEVICE = 0x8002;
    static const uint32 CMD_SIZE = 0x8003;
    static const uint32 CMD_0 = 0x8004;
    static const uint32 CMD_1 = 0x8005;
    static const uint32 CMD_2 = 0x8006;
    static const uint32 CMD_3 = 0x8007;
    static const uint32 CMD_4 = 0x8008;
    static const uint32 CMD_WRITTEN = 0x0001;

    static const uint32 CONSOLE_START = 0x0800;
    static const uint32 CONSOLE_END = 0x0840;
    static const uint32 CONSOLE_PNTR = 0x0841;

    static final char* SYS_NAME = "EmulatorOS";
    static final uint32[3] CONSOLE_SETUP_CMD = {0x0001,CONSOLE_START,0x0020};
    static final ProcessState[1024] processStates;

    // #syscall 0x0001 printChar
    // #syscall 0x0002 printStr
    // #syscall 0x0003 malloc
    // #syscall 0x0fff exit

    static const void* HEAP_START = (void*) 0x9001;
    static void* heepPtr = (void*) HEAP_START;

    @Entrypoint(raw)
    internal static void _main() {
        // SysD.rIH = &Kernal::_interrupt;
        peripheralCmd(0x0001, 3, &CONSOLE_SETUP_CMD);
        SysD.memSet(CONSOLE_START, CONSOLE_PNTR);
        // Memory._setup();
        // System.console = new Console(0x0800, 0x0001, 0x0020);
        // Probably should't be referencing System module in the kernal
        uint32 a = Kernal.CONSOLE_PNTR;
    }

    @InterruptHandler(raw)
    internal static void _interrupt() {
        // stack: [...pgmPtr,rPM,r0...r15 [HEAD]]
        void* stack = SysD.rStack; // stack: [...pgmPtr,rPM,r0...r15,var(stack) [HEAD], [stack*]]
        stack -= 2; // now points to r15; stack: [...pgmPtr,rPM,r0...r15 [stack*],var(stack,+17) [HEAD]]
        ProcessState* oldState = &processStates[SysD.rPID];
        oldState.updateInterrupt();

        uint32 code = SysD.rIC;
        if((code & 0x8000_0000) == 0) { // system interrupt in the active process
            SysD.rPM = false;
            // System.onInterrupt(code);
            // need to get interrupt handler for the current process here
            oldState.interruptHandler.call(code);
            SysD.interruptReturn();
            return; // only including this for clarity, it is tactically unreachable
        } else {
            if(code == 0x8000_0001) { // privileged mode failure
                SysD.halt(); // this is a breaking instruct, we just don't know it
            }
            if((code & 0x0001_0000) != 0) { // peripheral interrupt

            }
        }
        // SysD.rMemTblI = oldState.memTablePtr;
        // stack: [...pgmPtr,rPM,r0...r15 [oldState.stackPtr*],var(stack,+1)...]
        // SysD.rStackI = oldState.stackPtr;
        // stack: [...pgmPtr [stack*],rPM,r0...r15 [HEAD,oldState.stackPtr*] ,var(stack,+0)...]
        SysD.interruptReturn();
    }

    @Syscall
    public static void interruptExit() {

    }

    @Syscall
    public static void exit() {
        SysD.halt(); // this is a breaking instruct, we just don't know it
    }

    public static void peripheralCmd(uint32 deviceId, uint32 cmdSize, uint32* cmd) {
        uint32 addr = CMD_DEVICE;
        SysD.memSet(addr, deviceId);
        addr++;
        SysD.memSet(addr, cmdSize);
        addr++;
        SysD.memCopy(cmd, 0, cmdSize, (uint32*)addr, 0);
        SysD.memSet(CMD_STATUS, CMD_WRITTEN);
    }

    // public static uint32 getPeripheral(uint32 type) {
    //     uint32[1] cmd = {0x1};
    //     peripheralCmd(0, 0x1, &cmd);
    //     while(SysD.memGet(0x8080) != 0x1) {
    //         asm("NO OP");
    //     }
    //     uint32 numDevices = SysD.memGet(0x8082);
    //     for(int i = 0; i < numDevices; i++) {
    //         uint32 addr = 0x8083 + ( i * 2 );
    //         uint32 deviceType = SysD.memGet(addr+1);
    //         if(deviceType == type) {
    //             return SysD.memGet(addr);
    //         }
    //     }
    //     return 0;
    // }

    static uint32 lastPID = 0;

    public static ProcessState* createProcess() {
        uint32 nextPID = lastPID + 1;
        if(nextPID == 1024) {
            nextPID = 1;
            while(processStates[nextPID].status != 0) {
                nextPID ++;
                if(nextPID == 1024) {
                    return nullptr;
                }
            }
        }
        return &processStates[nextPID];
    }

    class Method<P1> {

    }

    struct ProcessState {
        public uint32 pid;
        public uint32 pgmPtr;
        public void* stackPtr;
        public void* memTablePtr;
        public bool privileged;
        public uint32[16] registers;

        public uint32 status;
        public Method<uint32>* interruptHandler;

        public void update() {
            pgmPtr = SysD.rPgm;
            stackPtr = SysD.rStack;
            memTablePtr = SysD.rMemTbl;
            privileged = SysD.rPM;
        }

        public void updateInterrupt() {
            stackPtr = SysD.rStackI;
            privileged = SysD.rPMI;
            pgmPtr = SysD.rPgmI;
            memTablePtr = SysD.rMemTblI;
            registers[0] = SysD.r0I;
            registers[1] = SysD.r1I;
            registers[2] = SysD.r2I;
            registers[3] = SysD.r3I;
            registers[4] = SysD.r4I;
            registers[5] = SysD.r5I;
            registers[6] = SysD.r6I;
            registers[7] = SysD.r7I;
            registers[8] = SysD.r8I;
            registers[9] = SysD.r9I;
            registers[10] = SysD.r10I;
            registers[11] = SysD.r11I;
            registers[12] = SysD.r12I;
            registers[13] = SysD.r13I;
            registers[14] = SysD.r14I;
            registers[15] = SysD.r15I;
        }
        public void setInterrupt() {
            SysD.rStackI = stackPtr;
            SysD.rPMI = privileged;
            SysD.rPgmI = pgmPtr;
            SysD.rMemTblI = memTablePtr;
            SysD.r0I = registers[0];
            SysD.r1I = registers[1];
            SysD.r2I = registers[2];
            SysD.r3I = registers[3];
            SysD.r4I = registers[4];
            SysD.r5I = registers[5];
            SysD.r6I = registers[6];
            SysD.r7I = registers[7];
            SysD.r8I = registers[8];
            SysD.r9I = registers[9];
            SysD.r10I = registers[10];
            SysD.r11I = registers[11];
            SysD.r12I = registers[12];
            SysD.r13I = registers[13];
            SysD.r14I = registers[14];
            SysD.r15I = registers[15];
        }

        public void apply() {
            SysD.rMemTbl = memTablePtr;
            SysD.rStack = stackPtr;
            SysD.rPM = privileged;
            SysD.rPgm = pgmPtr;
        }

        public void applyNoPgm() {
            SysD.rMemTbl = memTablePtr;
            SysD.rStack = stackPtr;
            SysD.rPM = privileged;
        }

        public static ProcessState* create(ProcessState& state) {
            state.pid = SysD.rPID;
            state.update();
            status = 1;
            return state;
        }
    }
}