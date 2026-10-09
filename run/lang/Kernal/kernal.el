import SysD;

namespace Kernal {

    static const int32* CMD_ADDR = 0x1_0000;
    static const uint8* CMD_STATUS = 0x1_0001;
    static const uint16* CMD_DEVICE = 0x1_0002;
    static const int32* CMD_SIZE = 0x1_0004;
    static const int32* CMD_START = 0x1_0008;

    static const int32 CMD_WRITTEN = 0x0001;

    static final char* SYS_NAME = "EmulatorOS\0";
    static final ProcessState[1024] processStates;
    static ProcessState*[32] processReadyQueue;
    static final Mutex processReadyQueueLock;

    static const int32* TIMER_UNIT;
    // static Console console;

    // #syscall 0x0001 printChar
    // #syscall 0x0002 printStr
    // #syscall 0x0003 malloc
    // #syscall 0x0fff exit

    @Entrypoint(raw)
    internal static void _main() {
        // SysD.rIH = &Kernal::_interrupt;
        asm{
            LOAD rIH &:Kernal._interrupt
            LOAD rPID 0
        }

        printStr("Starting \0");
        printStr(SYS_NAME);
        printChar('\n');

        // Now setup the kernal process
        // We immediately mark it running because it is, this is really just boilerplate for multi-process
        ProcessState& kernalProcess = &processStates[1];
        kernalProcess.status = ProcessStatus.RUNNING;
        kernalProcess.parent = 0;
        kernalProcess.pid = 1;
        SysD.rPID = 1;
        kernalProcess.interruptHandler = nullptr;

        for(int32 pI = 2; pI < 1024; pI++) {
            processStates[pI].status = ProcessStatus.NONE;
        }

        

        // Wait loop
        asm{
            :kLoop
            LOAD MEM r1 &Kernal.processReadyQueue
            GOTO EQ r1 :kLoop
            INTERRUPT 0x9000_0000
            GOTO :kLoop
        }
    }

    @InterruptHandler(raw)
    internal static void _interrupt() {
        int32 code = SysD.rIC;
        if((code & 0x8000_0000) == 0) { // system interrupt in the active process
            ProcessState& cProc = &processStates[SysD.rPID];
            SysD.rPM = false;
            // System.onInterrupt(code);
            // need to get interrupt handler for the current process here
            if(cProc.interruptHandler) {
                cProc.interruptHandler(code);
            }
            SysD.interruptReturn();
            return; // only including this for clarity, it is technically unreachable
        }
        
        if(code == 0x8000_0001) { // privileged mode failure
            SysD.halt(); // this is a breaking instruct, we just don't know it
        }
        if((code & 0xffff_ff00) == 0x8000_0200) { // Timer interrupt
            int32 i = code & 0xff;
        }
        if(code & 0xffff_fff0 == 0x9000_0000) { // process flow
            ProcessState& oldState = &processStates[SysD.rPIDI];
            oldState.updateInterrupt();
            if(code == 0x9000_0001) { // yield
                if(oldState.status == ProcessStatus.RUNNING) {
                    oldState.status = ProcessStatus.READY;
                }
            }
            if(code == 0x9000_0002) { // exit
                oldState.status = ProcessStatus.DEAD;
                // TODO might do something here w/ exit code, TBD
            }
            // get next process
            // Check if there is a process in the ready queue
            processReadyQueueLock.acquire();
            if(processReadyQueue[0] == nullptr) { // no processes in the ready queue
                processReadyQueueLock.release();
                // Go back to kernal root process to wait so interrupts can happen
                ProcessState& kProc = &processStates[1];
                kProc.setInterrupt();
                kProc.status = ProcessStatus.RUNNING;
                SysD.interruptReturn();
                return; // only including this for clarity, it is technically unreachable
            }
            ProcessState* newProc = processReadyQueue[0];
            asm{
                LOAD r1 $Kernal.processReadyQueue
                ADD r2 r1 4
                COPY MEM WORD r2 r1 INC_RS INC_RD // 1 -> 0
                COPY MEM WORD r2 r1 INC_RS INC_RD // 2 -> 1
                COPY MEM WORD r2 r1 INC_RS INC_RD // 3 -> 2
                COPY MEM WORD r2 r1 INC_RS INC_RD // 4 -> 3
                COPY MEM WORD r2 r1 INC_RS INC_RD // 5 -> 4
                COPY MEM WORD r2 r1 INC_RS INC_RD // 6 -> 5
                COPY MEM WORD r2 r1 INC_RS INC_RD // 7 -> 6
                COPY MEM WORD r2 r1 INC_RS INC_RD // 8 -> 7
                COPY MEM WORD r2 r1 INC_RS INC_RD // 9 -> 8
                COPY MEM WORD r2 r1 INC_RS INC_RD // 10 -> 9
                COPY MEM WORD r2 r1 INC_RS INC_RD // 11 -> 10
                COPY MEM WORD r2 r1 INC_RS INC_RD // 12 -> 11
                COPY MEM WORD r2 r1 INC_RS INC_RD // 13 -> 12
                COPY MEM WORD r2 r1 INC_RS INC_RD // 14 -> 13
                COPY MEM WORD r2 r1 INC_RS INC_RD // 15 -> 14
                COPY MEM WORD r2 r1 INC_RS INC_RD // 16 -> 15
                COPY MEM WORD r2 r1 INC_RS INC_RD // 17 -> 16
                COPY MEM WORD r2 r1 INC_RS INC_RD // 18 -> 17
                COPY MEM WORD r2 r1 INC_RS INC_RD // 19 -> 18
                COPY MEM WORD r2 r1 INC_RS INC_RD // 20 -> 19
                COPY MEM WORD r2 r1 INC_RS INC_RD // 21 -> 20
                COPY MEM WORD r2 r1 INC_RS INC_RD // 22 -> 21
                COPY MEM WORD r2 r1 INC_RS INC_RD // 23 -> 22
                COPY MEM WORD r2 r1 INC_RS INC_RD // 24 -> 23
                COPY MEM WORD r2 r1 INC_RS INC_RD // 25 -> 24
                COPY MEM WORD r2 r1 INC_RS INC_RD // 26 -> 25
                COPY MEM WORD r2 r1 INC_RS INC_RD // 27 -> 26
                COPY MEM WORD r2 r1 INC_RS INC_RD // 28 -> 27
                COPY MEM WORD r2 r1 INC_RS INC_RD // 29 -> 28
                COPY MEM WORD r2 r1 INC_RS INC_RD // 30 -> 29
                COPY MEM WORD r2 r1 INC_RS INC_RD // 31 -> 30
                STORE WORD r1 0 // 31
            }
            processReadyQueueLock.release();

            newProc.setInterrupt();
            newProc.status = ProcessStatus.RUNNING;

            SysD.interruptReturn();
            return; // only including this for clarity, it is technically unreachable
        }
        
        SysD.interruptReturn();
    }

    static int32 lastPID = 1;

    public static ProcessState* createProcess() {
        int32 nextPID = lastPID + 1;
        if(nextPID == 1024) {
            nextPID = 2;
        }
        while(processStates[nextPID].status != ProcessStatus.NONE) {
            nextPID++;
            if(nextPID == 1024) {
                nextPID = 2;
            }
            if(nextPID == lastPID) {
                return nullptr;
            }
        }
        lastPID = nextPID;
        return &processStates[nextPID];
    }

    enum ProcessStatus {
        NONE,
        SETUP,
        READY,
        RUNNING,
        WAITING,
        DEAD;
    }

    struct ProcessState {
        public int32 pid;
        public int32 pgmPtr;
        public void* stackPtr;
        public void* memTablePtr;
        public bool privileged;
        public int32[16] registers;

        public ProcessStatus status;
        public method<int32> interruptHandler;
        public int32 parent;

        public FS.ProcessFiles* files;

        public int32[7] _padding;

        public void updateInterrupt() {
            asm{
                ADD r1 r0 4 // Offset to pgmPtr instead of PID
                STORE WORD rPgmI r1 INC_RA
                STORE WORD rStackI r1 INC_RA
                STORE WORD rMemTblI r1 INC_RA
                STORE WORD rPMI r1 INC_RA

                STORE WORD r0I r1 INC_RA
                STORE WORD r1I r1 INC_RA
                STORE WORD r2I r1 INC_RA
                STORE WORD r3I r1 INC_RA
                STORE WORD r4I r1 INC_RA
                STORE WORD r5I r1 INC_RA
                STORE WORD r6I r1 INC_RA
                STORE WORD r7I r1 INC_RA
                STORE WORD r8I r1 INC_RA
                STORE WORD r9I r1 INC_RA
                STORE WORD r10I r1 INC_RA
                STORE WORD r11I r1 INC_RA
                STORE WORD r12I r1 INC_RA
                STORE WORD r13I r1 INC_RA
                STORE WORD r14I r1 INC_RA
                STORE WORD r15I r1 INC_RA
            }
        }
        public void setInterrupt() {
            asm{
                COPY r0 r1
                LOAD MEM WORD rPIDI r1 INC_RA
                LOAD MEM WORD rPgmI r1 INC_RA
                LOAD MEM WORD rStackI r1 INC_RA
                LOAD MEM WORD rMemTblI r1 INC_RA
                LOAD MEM WORD rPMI r1 INC_RA

                LOAD MEM WORD r0I r1 INC_RA
                LOAD MEM WORD r1I r1 INC_RA
                LOAD MEM WORD r2I r1 INC_RA
                LOAD MEM WORD r3I r1 INC_RA
                LOAD MEM WORD r4I r1 INC_RA
                LOAD MEM WORD r5I r1 INC_RA
                LOAD MEM WORD r6I r1 INC_RA
                LOAD MEM WORD r7I r1 INC_RA
                LOAD MEM WORD r8I r1 INC_RA
                LOAD MEM WORD r9I r1 INC_RA
                LOAD MEM WORD r10I r1 INC_RA
                LOAD MEM WORD r11I r1 INC_RA
                LOAD MEM WORD r12I r1 INC_RA
                LOAD MEM WORD r13I r1 INC_RA
                LOAD MEM WORD r14I r1 INC_RA
                LOAD MEM WORD r15I r1 INC_RA
            }
        }

        public static ProcessState* create(ProcessState& state, int32 pid, int32 parent) {
            state.pid = pid;
            state.status = ProcessStatus.SETUP;
            state.parent = parent;
            state.interruptHandler = nullptr;
            return state;
        }
    }
}