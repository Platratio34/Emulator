// static data
// Kernal
#var Kernal.lastPID 0x0000 uint32
#define Kernal.TIMER_UNIT 0x00 uint32*
#define Kernal.CMD_DEVICE 0x8002 uint32
#var Kernal.SYS_NAME "EmulatorOS\0" char*
#define Kernal.CONSOLE_OUT 0x0001_0000 char*
#define Kernal.CMD_WRITTEN 0x0001 uint32
#var Kernal.processStates (102400) ProcessState[1024]
#define Kernal.CMD_1 0x8005 uint32
#define Kernal.CMD_STATUS 0x8001 uint32
#define Kernal.CMD_2 0x8006 uint32
#define Kernal.CMD_3 0x8007 uint32
#define Kernal.CMD_4 0x8008 uint32
#define Kernal.CONSOLE_IN 0x0001_0001 char*
#define Kernal.CONSOLE_IN_COUNT 0x0001_0002 char*
#define Kernal.CMD_SIZE 0x8003 uint32
#define Kernal.CMD_0 0x8004 uint32
// Kernal.Memory
#define Kernal.Memory.MMU_MAX_BLOCKS 0x0800 uint32
#var Kernal.Memory.mmuId 0x00 uint32
#define Kernal.Memory.MMU_DEVICE_TYPE 0x0100_0002 uint32
#define Kernal.Memory.MMU_START 0x0001_0000 uint32

//--------
// text

// Kernal

#function Kernal.exit
STACK PUSH r15
COPY rStack r15
// 0 86:10
#line run/lang/Kernal/kernal.el 86:10
HALT
//  SysD.halt();

#lineend
:func_exit_Kernal.exit
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.print_char c char
STACK PUSH r15
COPY rStack r15
#stackVar char c -12
// 0 10:10
#line run/lang/Kernal/console.el 10:10
LOAD r1 Kernal.CONSOLE_OUT
COPY r15 r2
INC r2 -12
LOAD MEM BYTE r2 r2
STORE BYTE r2 r1
// * CONSOLE_OUT = c;

#lineend
:func_exit_Kernal.print_char
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.print_char* str char*
STACK PUSH r15
COPY rStack r15
#stackVar char* str -12
// 0 14:10
#line run/lang/Kernal/console.el 14:10
LOAD MEM r1 Kernal.CONSOLE_OUT
//  asm("LOAD MEM r1 Kernal.CONSOLE_OUT");

// 1 15:10
#line run/lang/Kernal/console.el 15:10
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
//  asm("COPY r15 r2\nINC r2 -12\nLOAD MEM r2 r2");

// 2 16:10
#line run/lang/Kernal/console.el 16:10
:Kernal.print_char*_l1
//  asm(":Kernal.print_char*_l1");

// 3 17:14
#line run/lang/Kernal/console.el 17:14
LOAD MEM BYTE r3 r2
GOTO EQ r3 :Kernal.print_char*__exit
//  asm("LOAD MEM BYTE r3 r2\nGOTO EQ r3 :Kernal.print_char*__exit");

// 4 18:14
#line run/lang/Kernal/console.el 18:14
STORE BYTE r3 r1
INC r2 1
GOTO :Kernal.print_char*_l1
//  asm("STORE BYTE r3 r1\nINC r2 1\nGOTO :Kernal.print_char*_l1");

// 5 19:10
#line run/lang/Kernal/console.el 19:10
:Kernal.print_char*__exit
//  asm(":Kernal.print_char*__exit");

#lineend
:func_exit_Kernal.print_char*
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.print_char*_uint32 str char*, len uint32
STACK PUSH r15
COPY rStack r15
#stackVar char* str -16
#stackVar uint32 len -12
// 0 23:10
#line run/lang/Kernal/console.el 23:10
LOAD MEM r1 Kernal.CONSOLE_OUT
//  asm("LOAD MEM r1 Kernal.CONSOLE_OUT");

// 1 24:10
#line run/lang/Kernal/console.el 24:10
COPY r15 r2
INC r2 -16
LOAD MEM r2 r2
//  asm("COPY r15 r2\nINC r2 -16\nLOAD MEM r2 r2");

// 2 25:10
#line run/lang/Kernal/console.el 25:10
COPY r15 r3
INC r3 -12
LOAD MEM r3 r3
//  asm("COPY r15 r3\nINC r3 -12\nLOAD MEM r3 r3");

// 3 26:10
#line run/lang/Kernal/console.el 26:10
:Kernal.print_char*_uint32_l1
//  asm(":Kernal.print_char*_uint32_l1");

// 4 27:14
#line run/lang/Kernal/console.el 27:14
COPY MEM BYTE r2 r1 INC_RS
//  asm("COPY MEM BYTE r2 r1 INC_RS");

// 5 28:14
#line run/lang/Kernal/console.el 28:14
INC r13 -1
GOTO GT r13 :Kernal.print_char*_uint32_l1
//  asm("INC r13 -1\nGOTO GT r13 :Kernal.print_char*_uint32_l1");

#lineend
:func_exit_Kernal.print_char*_uint32
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.read_char*_uint32_out_uint32& buffer char*, bufferSize uint32, count out uint32&
STACK PUSH r15
COPY rStack r15
#stackVar out uint32& count -12
#stackVar char* buffer -20
#stackVar uint32 bufferSize -16
// 0 32:10
#line run/lang/Kernal/console.el 32:10
LOAD r1 Console.CONSOLE_IN_COUNT
:read_l0
LOAD MEM BYTE r2 r1
GOTO EQ r2 :read_l0
//  asm("LOAD r1 Console.CONSOLE_IN_COUNT\n:read_l0\nLOAD MEM BYTE r2 r1\nGOTO EQ r2 :read_l0");

// 1 33:10
#line run/lang/Kernal/console.el 33:10
LOAD r1 Console.CONSOLE_IN
//  asm("LOAD r1 Console.CONSOLE_IN");

// 2 34:10
#line run/lang/Kernal/console.el 34:10
COPY r15 r3
INC r3 -16
LOAD MEM r3 r3
//  asm("COPY r15 r3\nINC r3 -16\nLOAD MEM r3 r3");

// 3 35:10
#line run/lang/Kernal/console.el 35:10
LOAD r6 1
//  asm("LOAD r6 1");

// 4 36:10
#line run/lang/Kernal/console.el 36:10
SUB r4 r3 r1
GOTO LT r4 :Kernal.read_char*_uint32_if_end_0
//  asm("SUB r4 r3 r1\nGOTO LT r4 :Kernal.read_char*_uint32_if_end_0");

// 5 37:14
#line run/lang/Kernal/console.el 37:14
COPY r3 r2
LOAD r6 0
//  asm("COPY r3 r2\nLOAD r6 0");

// 6 38:10
#line run/lang/Kernal/console.el 38:10
:Kernal.read_char*_uint32_if_end_0
//  asm(":Kernal.read_char*_uint32_if_end_0");

// 7 39:10
#line run/lang/Kernal/console.el 39:10
COPY r2 r4
COPY r15 r5
INC r5 -20
:Kernal.read_char*_uint32_l0
//  asm("COPY r2 r4\nCOPY r15 r5\nINC r5 -20\n:Kernal.read_char*_uint32_l0");

// 8 40:14
#line run/lang/Kernal/console.el 40:14
COPY MEM BYTE r1 r5 INC_RD
INC r4 -1
//  asm("COPY MEM BYTE r1 r5 INC_RD\nINC r4 -1");

// 9 41:14
#line run/lang/Kernal/console.el 41:14
GOTO GT r4 :Kernal.read_char*_uint32_l0
//  asm("GOTO GT r4 :Kernal.read_char*_uint32_l0");

// 10 42:10
#line run/lang/Kernal/console.el 42:10
GOTO EQ r1 :Kernal.read_char*_uint32_end
//  asm("GOTO EQ r1 :Kernal.read_char*_uint32_end");

// 11 43:14
#line run/lang/Kernal/console.el 43:14
LOAD r6 0x0
STORE BYTE r6 r5
//  asm("LOAD r6 0x0\nSTORE BYTE r6 r5");

// 12 44:10
#line run/lang/Kernal/console.el 44:10
:Kernal.read_char*_uint32_end
//  asm(":Kernal.read_char*_uint32_end");

// 13 45:10
#line run/lang/Kernal/console.el 45:10
COPY r15 r6
INC r6 -12
STORE r2 r6
//  asm("COPY r15 r6\nINC r6 -12\nSTORE r2 r6");

#lineend
:func_exit_Kernal.read_char*_uint32_out_uint32&
STACK POP r15
GOTO POP
#endfunction void

:__start
#function Kernal._main
STACK PUSH r15
COPY rStack r15
// 0 29:10
#line run/lang/Kernal/kernal.el 29:10
LOAD rIH &:Kernal._interrupt
//  asm("LOAD rIH &:Kernal._interrupt");

// 1 33:10
#line run/lang/Kernal/kernal.el 33:10
#define exp_str_0 "Starting \0"
LOAD r1 exp_str_0
STACK PUSH r1
GOTO PUSH :Kernal.print_char*
STACK DEC 4
//  print("Starting \0");

// 2 34:10
#line run/lang/Kernal/kernal.el 34:10
LOAD r1 &Kernal.SYS_NAME
LOAD MEM r1 r1
STACK PUSH r1
GOTO PUSH :Kernal.print_char*
STACK DEC 4
//  print(SYS_NAME);

// 3 35:10
#line run/lang/Kernal/kernal.el 35:10
LOAD r1 '\n'
STACK PUSH r1
GOTO PUSH :Kernal.print_char
STACK DEC 4
//  print('\n');

#lineend
:func_exit_Kernal._main
STACK POP r15
HALT
#endfunction void

#function Kernal.createProcess
STACK PUSH r15
COPY rStack r15
// 0 119:10
#line run/lang/Kernal/kernal.el 119:10
LOAD r1 &Kernal.lastPID
LOAD MEM r1 r1
INC r1 1
#stackVar uint32 nextPID
STACK PUSH r1
//  uint32 nextPID = lastPID + 1;

// 1 120:10
#line run/lang/Kernal/kernal.el 120:10
COPY r15 r1
LOAD MEM r1 r1
INC r1 -1024
SET FORCE EQ r1 r1
GOTO EQ r1 :if_end_0
// 0 121:14
#line run/lang/Kernal/kernal.el 121:14
COPY r15 r1
LOAD r2 1
STORE r2 r1
//  nextPID = 1;

// 1 122:14
#line run/lang/Kernal/kernal.el 122:14
:while_condition_1
LOAD r1 &Kernal.processStates
COPY r15 r2
LOAD MEM r2 r2
LOAD r3 100
MUL r2 r2 r3
ADD r1 r1 r2
LOAD MEM r1 r1
SET FORCE NEQ r1 r1
GOTO EQ r1 :while_end_1
// 0 123:18
#line run/lang/Kernal/kernal.el 123:18
COPY r15 r1
LOAD MEM r2 r1
INC r2 1
STORE r2 r1
//  nextPID++;

// 1 124:18
#line run/lang/Kernal/kernal.el 124:18
COPY r15 r1
LOAD MEM r1 r1
INC r1 -1024
SET FORCE EQ r1 r1
GOTO EQ r1 :if_end_2
// 0 125:22
#line run/lang/Kernal/kernal.el 125:22
COPY r15 r1
LOAD r2 1
STORE r2 r1
//  nextPID = 1;

#lineend
:if_end_2
//  if(nextPID == 1024) {nextPID = 1;}

// 2 127:18
#line run/lang/Kernal/kernal.el 127:18
COPY r15 r1
LOAD MEM r1 r1
LOAD r2 &Kernal.lastPID
LOAD MEM r2 r2
SUB r1 r1 r2
SET FORCE EQ r1 r1
GOTO EQ r1 :if_end_3
// 0 128:22
#line run/lang/Kernal/kernal.el 128:22
LOAD r1 0
COPY r15 r2
INC r2 -12
STORE r1 r2
GOTO :func_exit_Kernal.createProcess
//  return nullptr;

#lineend
:if_end_3
//  if(nextPID == lastPID) {return nullptr;}

#lineend
GOTO :while_condition_1
:while_end_1
//  while(processStates[nextPID].status != 0) {nextPID++; if(nextPID == 1024) {nextPID = 1;} if(nextPID == lastPID) {return nullptr;}}

#lineend
:if_end_0
//  if(nextPID == 1024) {nextPID = 1; while(processStates[nextPID].status != 0) {nextPID++; if(nextPID == 1024) {nextPID = 1;} if(nextPID == lastPID) {return nullptr;}}}

// 2 132:10
#line run/lang/Kernal/kernal.el 132:10
LOAD r1 &Kernal.lastPID
COPY r15 r2
LOAD MEM r2 r2
STORE r2 r1
//  lastPID = nextPID;

// 3 133:10
#line run/lang/Kernal/kernal.el 133:10
LOAD r1 &Kernal.processStates
COPY r15 r2
LOAD MEM r2 r2
LOAD r3 100
MUL r2 r2 r3
ADD r1 r1 r2
COPY r15 r2
INC r2 -12
STORE r1 r2
GOTO :func_exit_Kernal.createProcess
//  return & processStates[nextPID];

#lineend
:func_exit_Kernal.createProcess
STACK DEC 4
// End of scope
#stackVarClear nextPID
STACK POP r15
GOTO POP
#endfunction ProcessState*

#function Kernal._interrupt
STACK PUSH r15
COPY rStack r15
// 0 45:10
#line run/lang/Kernal/kernal.el 45:10
LOAD r1 &Kernal.processStates
COPY rPID r2
LOAD r3 100
MUL r2 r2 r3
ADD r1 r1 r2
#stackVar ProcessState* oldState
STACK PUSH r1
//  ProcessState* oldState = & processStates[SysD.rPID];

// 1 46:10
#line run/lang/Kernal/kernal.el 46:10
COPY r15 r1
LOAD MEM r1 r1
LOAD r2 0
STORE r2 r1
//  oldState.pid = 0;

// 2 47:10
#line run/lang/Kernal/kernal.el 47:10
//  ProcessState.updateInterrupt(oldState);

// 3 49:10
#line run/lang/Kernal/kernal.el 49:10
COPY rIC r1
#stackVar uint32 code
STACK PUSH r1
//  uint32 code = SysD.rIC;

// 4 50:10
#line run/lang/Kernal/kernal.el 50:10
COPY r15 r1
INC r1 4
LOAD MEM r1 r1
LOAD r2 -2147483648
AND r1 r1 r2
SET FORCE EQ r1 r1
GOTO EQ r1 :if_else_4
// 0 51:14
#line run/lang/Kernal/kernal.el 51:14
LOAD r1 0
COPY rPM r1
//  SysD.rPM = false;

// 1 55:14
#line run/lang/Kernal/kernal.el 55:14
INTERRUPT RET
//  SysD.interruptReturn();

// 2 56:14
#line run/lang/Kernal/kernal.el 56:14
GOTO :func_exit_Kernal._interrupt
//  return;

#lineend
GOTO :if_end_4
:if_else_4
// 0 58:14
#line run/lang/Kernal/kernal.el 58:14
COPY r15 r1
INC r1 4
LOAD MEM r1 r1
INC r1 2147483647
SET FORCE EQ r1 r1
GOTO EQ r1 :if_end_5
// 0 59:18
#line run/lang/Kernal/kernal.el 59:18
HALT
//  SysD.halt();

#lineend
:if_end_5
//  if(code == 0x8000_0001) {SysD.halt();}

// 1 61:14
#line run/lang/Kernal/kernal.el 61:14
COPY r15 r1
INC r1 4
LOAD MEM r1 r1
INC r1 -1
SET FORCE EQ r1 r1
GOTO EQ r1 :if_end_6
// 0 62:18
#line run/lang/Kernal/kernal.el 62:18
LOAD r1 1
#stackVar uint32 timerIndex
STACK PUSH r1
//  uint32 timerIndex = 1;

// 1 63:18
#line run/lang/Kernal/kernal.el 63:18
:while_condition_7
LOAD r1 Kernal.TIMER_UNIT
COPY r15 r2
INC r2 8
LOAD MEM r2 r2
LOAD r3 4
MUL r2 r2 r3
ADD r1 r1 r2
INC r1 1
SET FORCE NEQ r1 r1
GOTO EQ r1 :while_end_7
// 0 64:22
#line run/lang/Kernal/kernal.el 64:22
COPY r15 r1
INC r1 8
LOAD MEM r2 r1
INC r2 1
STORE r2 r1
//  timerIndex++;

#lineend
GOTO :while_condition_7
:while_end_7
//  while(TIMER_UNIT[timerIndex] != 0xffff_ffff) {timerIndex++;}

// 2 66:18
#line run/lang/Kernal/kernal.el 66:18
LOAD r1 Kernal.TIMER_UNIT
COPY r15 r2
INC r2 8
LOAD MEM r2 r2
LOAD r3 4
MUL r2 r2 r3
ADD r1 r1 r2
LOAD r2 0
STORE r2 r1
//  TIMER_UNIT[timerIndex] = 0x0;

#lineend
STACK DEC 4
// End of scope
#stackVarClear timerIndex
:if_end_6
//  if(code == 0x1) {uint32 timerIndex = 1; while(TIMER_UNIT[timerIndex] != 0xffff_ffff) {timerIndex++;} TIMER_UNIT[timerIndex] = 0x0;}

// 2 68:14
#line run/lang/Kernal/kernal.el 68:14
COPY r15 r1
INC r1 4
LOAD MEM r1 r1
LOAD r2 65536
AND r1 r1 r2
SET FORCE NEQ r1 r1
GOTO EQ r1 :if_end_8
// 

#lineend
:if_end_8
//  if((code & 0x0001_0000) != 0) {}

#lineend
:if_end_4
//  if((code & 0x8000_0000) == 0) {SysD.rPM = false; SysD.interruptReturn(); return;} else {if(code == 0x8000_0001) {SysD.halt();} if(code == 0x1) {uint32 timerIndex = 1; while(TIMER_UNIT[timerIndex] != 0xffff_ffff) {timerIndex++;} TIMER_UNIT[timerIndex] = 0x0;} if((code & 0x0001_0000) != 0) {}}

// 5 76:10
#line run/lang/Kernal/kernal.el 76:10
INTERRUPT RET
//  SysD.interruptReturn();

#lineend
:func_exit_Kernal._interrupt
STACK DEC 8
// End of scope
#stackVarClear code
#stackVarClear oldState
STACK POP r15
INTERRUPT RET
#endfunction void

#function Kernal.interruptExit
STACK PUSH r15
COPY rStack r15
// 

#lineend
:func_exit_Kernal.interruptExit
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.peripheralCmd_uint32_uint32_uint32* deviceId uint32, cmdSize uint32, cmd uint32*
STACK PUSH r15
COPY rStack r15
#stackVar uint32 cmdSize -16
#stackVar uint32* cmd -12
#stackVar uint32 deviceId -20
// 0 90:10
#line run/lang/Kernal/kernal.el 90:10
LOAD r1 Kernal.CMD_DEVICE
#stackVar uint32 addr
STACK PUSH r1
//  uint32 addr = CMD_DEVICE;

// 1 91:10
#line run/lang/Kernal/kernal.el 91:10
STACK PUSH r1
COPY r15 r1
LOAD MEM r1 r1
COPY r15 r2
INC r2 -20
LOAD MEM r2 r2
STORE r1 r2
STACK POP r1
//  SysD.memSet(addr, deviceId);

// 2 92:10
#line run/lang/Kernal/kernal.el 92:10
COPY r15 r2
LOAD MEM r3 r2
INC r3 1
STORE r3 r2
//  addr++;

// 3 93:10
#line run/lang/Kernal/kernal.el 93:10
STACK PUSH r1
COPY r15 r1
LOAD MEM r1 r1
COPY r15 r2
INC r2 -16
LOAD MEM r2 r2
STORE r1 r2
STACK POP r1
//  SysD.memSet(addr, cmdSize);

// 4 94:10
#line run/lang/Kernal/kernal.el 94:10
COPY r15 r2
LOAD MEM r3 r2
INC r3 1
STORE r3 r2
//  addr++;

// 5 95:10
#line run/lang/Kernal/kernal.el 95:10
STACK PUSH r1
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD r2 0
COPY r15 r3
INC r3 -16
LOAD MEM r3 r3
COPY r15 r4
LOAD MEM r4 r4
LOAD r5 0
ADD r1 r1 r2
ADD r4 r4 r5
SUB r3 r3 r2
:loop_9
COPY MEM r1 r4
INC r3 -1
GOTO GT r3 :loop_9
STACK POP r1
//  SysD.memCopy(cmd, 0, cmdSize, addr, 0);

// 6 96:10
#line run/lang/Kernal/kernal.el 96:10
STACK PUSH r1
LOAD r1 Kernal.CMD_STATUS
LOAD r2 Kernal.CMD_WRITTEN
STORE r1 r2
STACK POP r1
//  SysD.memSet(CMD_STATUS, CMD_WRITTEN);

#lineend
:func_exit_Kernal.peripheralCmd_uint32_uint32_uint32*
STACK DEC 4
// End of scope
#stackVarClear cmdSize
#stackVarClear cmd
#stackVarClear addr
#stackVarClear deviceId
STACK POP r15
GOTO POP
#endfunction void

// Kernal.ProcessState

#function Kernal.ProcessState.update_ProcessState& state ProcessState&
STACK PUSH r15
COPY rStack r15
#stackVar ProcessState& state -12
// 0 152:14
#line run/lang/Kernal/kernal.el 152:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
COPY rPgm r2
STORE r2 r1
//  state.pgmPtr = SysD.rPgm;

// 1 153:14
#line run/lang/Kernal/kernal.el 153:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
COPY rStack r2
STORE r2 r1
//  state.stackPtr = SysD.rStack;

// 2 154:14
#line run/lang/Kernal/kernal.el 154:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
COPY rMemTbl r2
STORE r2 r1
//  state.memTablePtr = SysD.rMemTbl;

// 3 155:14
#line run/lang/Kernal/kernal.el 155:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
COPY rPM r2
STORE BYTE r2 r1
//  state.privileged = SysD.rPM;

#lineend
:func_exit_Kernal.ProcessState.update_ProcessState&
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.ProcessState.create_ProcessState& state ProcessState&
STACK PUSH r15
COPY rStack r15
#stackVar ProcessState& state -12
// 0 210:14
#line run/lang/Kernal/kernal.el 210:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
COPY rPID r2
STORE r2 r1
//  state.pid = SysD.rPID;

// 1 211:14
#line run/lang/Kernal/kernal.el 211:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
STACK PUSH r1
GOTO PUSH :Kernal.ProcessState.update_ProcessState&
STACK DEC 4
//  ProcessState.update(state);

// 2 212:14
#line run/lang/Kernal/kernal.el 212:14
COPY r0 r1
INC r1 88
LOAD r2 1
STORE r2 r1
//  status = 1;

// 3 213:14
#line run/lang/Kernal/kernal.el 213:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
COPY r15 r2
INC r2 -16
STORE r1 r2
GOTO :func_exit_Kernal.ProcessState.create_ProcessState&
//  return state;

#lineend
:func_exit_Kernal.ProcessState.create_ProcessState&
STACK POP r15
GOTO POP
#endfunction ProcessState*

#function Kernal.ProcessState.updateInterrupt_ProcessState& state ProcessState&
STACK PUSH r15
COPY rStack r15
#stackVar ProcessState& state -12
// 0 159:14
#line run/lang/Kernal/kernal.el 159:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
COPY rStackI r2
STORE r2 r1
//  state.stackPtr = SysD.rStackI;

// 1 160:14
#line run/lang/Kernal/kernal.el 160:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
COPY rPM r2
STORE BYTE r2 r1
//  state.privileged = SysD.rPMI;

// 2 161:14
#line run/lang/Kernal/kernal.el 161:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
COPY rPgmI r2
STORE r2 r1
//  state.pgmPtr = SysD.rPgmI;

// 3 162:14
#line run/lang/Kernal/kernal.el 162:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
COPY rMemTblI r2
STORE r2 r1
//  state.memTablePtr = SysD.rMemTblI;

// 4 163:14
#line run/lang/Kernal/kernal.el 163:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 0
COPY r0I r2
STORE r2 r1
//  state.registers[0] = SysD.r0I;

// 5 164:14
#line run/lang/Kernal/kernal.el 164:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 4
COPY r1I r2
STORE r2 r1
//  state.registers[1] = SysD.r1I;

// 6 165:14
#line run/lang/Kernal/kernal.el 165:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 8
COPY r2I r2
STORE r2 r1
//  state.registers[2] = SysD.r2I;

// 7 166:14
#line run/lang/Kernal/kernal.el 166:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 12
COPY r3I r2
STORE r2 r1
//  state.registers[3] = SysD.r3I;

// 8 167:14
#line run/lang/Kernal/kernal.el 167:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 16
COPY r4I r2
STORE r2 r1
//  state.registers[4] = SysD.r4I;

// 9 168:14
#line run/lang/Kernal/kernal.el 168:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 20
COPY r5I r2
STORE r2 r1
//  state.registers[5] = SysD.r5I;

// 10 169:14
#line run/lang/Kernal/kernal.el 169:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 24
COPY r6I r2
STORE r2 r1
//  state.registers[6] = SysD.r6I;

// 11 170:14
#line run/lang/Kernal/kernal.el 170:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 28
COPY r7I r2
STORE r2 r1
//  state.registers[7] = SysD.r7I;

// 12 171:14
#line run/lang/Kernal/kernal.el 171:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 32
COPY r8I r2
STORE r2 r1
//  state.registers[8] = SysD.r8I;

// 13 172:14
#line run/lang/Kernal/kernal.el 172:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 36
COPY r9I r2
STORE r2 r1
//  state.registers[9] = SysD.r9I;

// 14 173:14
#line run/lang/Kernal/kernal.el 173:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 40
COPY r10I r2
STORE r2 r1
//  state.registers[10] = SysD.r10I;

// 15 174:14
#line run/lang/Kernal/kernal.el 174:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 44
COPY r11I r2
STORE r2 r1
//  state.registers[11] = SysD.r11I;

// 16 175:14
#line run/lang/Kernal/kernal.el 175:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 48
COPY r12I r2
STORE r2 r1
//  state.registers[12] = SysD.r12I;

// 17 176:14
#line run/lang/Kernal/kernal.el 176:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 52
COPY r13I r2
STORE r2 r1
//  state.registers[13] = SysD.r13I;

// 18 177:14
#line run/lang/Kernal/kernal.el 177:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 56
COPY r14I r2
STORE r2 r1
//  state.registers[14] = SysD.r14I;

// 19 178:14
#line run/lang/Kernal/kernal.el 178:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 60
COPY r15I r2
STORE r2 r1
//  state.registers[15] = SysD.r15I;

#lineend
:func_exit_Kernal.ProcessState.updateInterrupt_ProcessState&
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.ProcessState.applyNoPgm_ProcessState& state ProcessState&
STACK PUSH r15
COPY rStack r15
#stackVar ProcessState& state -12
// 0 204:14
#line run/lang/Kernal/kernal.el 204:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD MEM r1 r1
COPY rMemTbl r1
//  SysD.rMemTbl = state.memTablePtr;

// 1 205:14
#line run/lang/Kernal/kernal.el 205:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD MEM r1 r1
COPY rStack r1
//  SysD.rStack = state.stackPtr;

// 2 206:14
#line run/lang/Kernal/kernal.el 206:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD MEM BYTE r1 r1
COPY rPM r1
//  SysD.rPM = state.privileged;

#lineend
:func_exit_Kernal.ProcessState.applyNoPgm_ProcessState&
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.ProcessState.setInterrupt_ProcessState& state ProcessState&
STACK PUSH r15
COPY rStack r15
#stackVar ProcessState& state -12
// 0 181:14
#line run/lang/Kernal/kernal.el 181:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD MEM r1 r1
COPY rStackI r1
//  SysD.rStackI = state.stackPtr;

// 1 182:14
#line run/lang/Kernal/kernal.el 182:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD MEM BYTE r1 r1
COPY rPMI r1
//  SysD.rPMI = state.privileged;

// 2 183:14
#line run/lang/Kernal/kernal.el 183:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD MEM r1 r1
COPY rPgmI r1
//  SysD.rPgmI = state.pgmPtr;

// 3 184:14
#line run/lang/Kernal/kernal.el 184:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD MEM r1 r1
COPY rMemTblI r1
//  SysD.rMemTblI = state.memTablePtr;

// 4 185:14
#line run/lang/Kernal/kernal.el 185:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 0
LOAD MEM r1 r1
COPY r0I r1
//  SysD.r0I = state.registers[0];

// 5 186:14
#line run/lang/Kernal/kernal.el 186:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 4
LOAD MEM r1 r1
COPY r1I r1
//  SysD.r1I = state.registers[1];

// 6 187:14
#line run/lang/Kernal/kernal.el 187:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 8
LOAD MEM r1 r1
COPY r2I r1
//  SysD.r2I = state.registers[2];

// 7 188:14
#line run/lang/Kernal/kernal.el 188:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 12
LOAD MEM r1 r1
COPY r3I r1
//  SysD.r3I = state.registers[3];

// 8 189:14
#line run/lang/Kernal/kernal.el 189:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 16
LOAD MEM r1 r1
COPY r4I r1
//  SysD.r4I = state.registers[4];

// 9 190:14
#line run/lang/Kernal/kernal.el 190:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 20
LOAD MEM r1 r1
COPY r5I r1
//  SysD.r5I = state.registers[5];

// 10 191:14
#line run/lang/Kernal/kernal.el 191:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 24
LOAD MEM r1 r1
COPY r6I r1
//  SysD.r6I = state.registers[6];

// 11 192:14
#line run/lang/Kernal/kernal.el 192:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 28
LOAD MEM r1 r1
COPY r7I r1
//  SysD.r7I = state.registers[7];

// 12 193:14
#line run/lang/Kernal/kernal.el 193:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 32
LOAD MEM r1 r1
COPY r8I r1
//  SysD.r8I = state.registers[8];

// 13 194:14
#line run/lang/Kernal/kernal.el 194:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 36
LOAD MEM r1 r1
COPY r9I r1
//  SysD.r9I = state.registers[9];

// 14 195:14
#line run/lang/Kernal/kernal.el 195:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 40
LOAD MEM r1 r1
COPY r10I r1
//  SysD.r10I = state.registers[10];

// 15 196:14
#line run/lang/Kernal/kernal.el 196:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 44
LOAD MEM r1 r1
COPY r11I r1
//  SysD.r11I = state.registers[11];

// 16 197:14
#line run/lang/Kernal/kernal.el 197:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 48
LOAD MEM r1 r1
COPY r12I r1
//  SysD.r12I = state.registers[12];

// 17 198:14
#line run/lang/Kernal/kernal.el 198:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 52
LOAD MEM r1 r1
COPY r13I r1
//  SysD.r13I = state.registers[13];

// 18 199:14
#line run/lang/Kernal/kernal.el 199:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 56
LOAD MEM r1 r1
COPY r14I r1
//  SysD.r14I = state.registers[14];

// 19 200:14
#line run/lang/Kernal/kernal.el 200:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 60
LOAD MEM r1 r1
COPY r15I r1
//  SysD.r15I = state.registers[15];

#lineend
:func_exit_Kernal.ProcessState.setInterrupt_ProcessState&
STACK POP r15
GOTO POP
#endfunction void

// Kernal.Memory

// Kernal.Memory.PageMapTable

// Kernal.Method

HALT