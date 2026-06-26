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

    static final char* SYS_NAME = "EmulatorOS\0";
    static final ProcessState[1024] processStates;

    static const uint32* TIMER_UNIT;
    // static Console console;

    // #syscall 0x0001 printChar
    // #syscall 0x0002 printStr
    // #syscall 0x0003 malloc
    // #syscall 0x0fff exit

    @Entrypoint(raw)
    internal static void _main() {
        // SysD.rIH = &Kernal::_interrupt;
        asm("LOAD rIH &:Kernal._interrupt");
        // Memory._setup();
        // console.address = 0x1_0000;
        // console.
        print("Starting \0");
        print(SYS_NAME);
        print('\n');
        // System.console = new Console(0x1_0000);
        // Probably should't be referencing System module in the kernal
    }

    @InterruptHandler(raw)
    internal static void _interrupt() {
        // stack: [...pgmPtr,rPM,r0...r15 [HEAD]]
        // void* stack = SysD.rStack; // stack: [...pgmPtr,rPM,r0...r15,var(stack) [HEAD], [stack*]]
        // stack -= 2; // now points to r15; stack: [...pgmPtr,rPM,r0...r15 [stack*],var(stack,+17) [HEAD]]
        ProcessState* oldState = &processStates[SysD.rPID];
        oldState.pid = 0;
        ProcessState.updateInterrupt(oldState);

        uint32 code = SysD.rIC;
        if((code & 0x8000_0000) == 0) { // system interrupt in the active process
            SysD.rPM = false;
            // System.onInterrupt(code);
            // need to get interrupt handler for the current process here
            // oldState.interruptHandler.call(code);
            SysD.interruptReturn();
            return; // only including this for clarity, it is technically unreachable
        } else {
            if(code == 0x8000_0001) { // privileged mode failure
                SysD.halt(); // this is a breaking instruct, we just don't know it
            }
            if(code == 0x1) { // Timer interrupt
                uint32 timerIndex = 1;
                while(TIMER_UNIT[timerIndex] != 0xffff_ffff) {
                    timerIndex++;
                }
                TIMER_UNIT[timerIndex] = 0x0;
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
        SysD.memCopy(cmd, 0, cmdSize, /*(uint32*)*/addr, 0);
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
                nextPID++;
                if(nextPID == 1024) {
                    nextPID = 1;
                }
                if(nextPID == lastPID) {
                    return nullptr;
                }
            }
        }
        lastPID = nextPID;
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

        public static void update(ProcessState& state) {
            state.pgmPtr = SysD.rPgm;
            state.stackPtr = SysD.rStack;
            state.memTablePtr = SysD.rMemTbl;
            state.privileged = SysD.rPM;
        }

        public static void updateInterrupt(ProcessState& state) {
            state.stackPtr = SysD.rStackI;
            state.privileged = SysD.rPMI;
            state.pgmPtr = SysD.rPgmI;
            state.memTablePtr = SysD.rMemTblI;
            state.registers[0] = SysD.r0I;
            state.registers[1] = SysD.r1I;
            state.registers[2] = SysD.r2I;
            state.registers[3] = SysD.r3I;
            state.registers[4] = SysD.r4I;
            state.registers[5] = SysD.r5I;
            state.registers[6] = SysD.r6I;
            state.registers[7] = SysD.r7I;
            state.registers[8] = SysD.r8I;
            state.registers[9] = SysD.r9I;
            state.registers[10] = SysD.r10I;
            state.registers[11] = SysD.r11I;
            state.registers[12] = SysD.r12I;
            state.registers[13] = SysD.r13I;
            state.registers[14] = SysD.r14I;
            state.registers[15] = SysD.r15I;
        }
        public void setInterrupt(ProcessState& state) {
            SysD.rStackI = state.stackPtr;
            SysD.rPMI = state.privileged;
            SysD.rPgmI = state.pgmPtr;
            SysD.rMemTblI = state.memTablePtr;
            SysD.r0I = state.registers[0];
            SysD.r1I = state.registers[1];
            SysD.r2I = state.registers[2];
            SysD.r3I = state.registers[3];
            SysD.r4I = state.registers[4];
            SysD.r5I = state.registers[5];
            SysD.r6I = state.registers[6];
            SysD.r7I = state.registers[7];
            SysD.r8I = state.registers[8];
            SysD.r9I = state.registers[9];
            SysD.r10I = state.registers[10];
            SysD.r11I = state.registers[11];
            SysD.r12I = state.registers[12];
            SysD.r13I = state.registers[13];
            SysD.r14I = state.registers[14];
            SysD.r15I = state.registers[15];
        }

        public void applyNoPgm(ProcessState& state) {
            SysD.rMemTbl = state.memTablePtr;
            SysD.rStack = state.stackPtr;
            SysD.rPM = state.privileged;
        }

        public static ProcessState* create(ProcessState& state) {
            state.pid = SysD.rPID;
            ProcessState.update(state);
            status = 1;
            return state;
        }
    }
}