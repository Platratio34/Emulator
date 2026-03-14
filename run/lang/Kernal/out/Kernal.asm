// static data
// Kernal
#var Kernal.lastPID 0x0000 // uint32
#define Kernal.CONSOLE_END 0x0840 // uint32
#var Kernal.CONSOLE_SETUP_CMD [0x0001,Kernal.CONSOLE_START,0x0020] // uint32[3]
#define Kernal.CONSOLE_START 0x0800 // uint32
#define Kernal.CMD_DEVICE 0x8002 // uint32
#var Kernal.SYS_NAME "EmulatorOS" // char*
#define Kernal.CONSOLE_PNTR 0x0841 // uint32
#define Kernal.CMD_WRITTEN 0x0001 // uint32
#var Kernal.processStates (98304) // ProcessState[1024]
#define Kernal.CMD_1 0x8005 // uint32
#define Kernal.CMD_STATUS 0x8001 // uint32
#define Kernal.CMD_2 0x8006 // uint32
#define Kernal.CMD_3 0x8007 // uint32
#define Kernal.CMD_4 0x8008 // uint32
#define Kernal.CMD_SIZE 0x8003 // uint32
#define Kernal.CMD_0 0x8004 // uint32
// Kernal.Memory
#define Kernal.Memory.MMU_MAX_BLOCKS 0x0800 // uint32
#var Kernal.Memory.mmuId 0x00 // uint32
#define Kernal.Memory.MMU_DEVICE_TYPE 0x0100_0002 // uint32
#define Kernal.Memory.MMU_START 0x0001_0000 // uint32

//--------
// text

// Kernal

#function Kernal.exit
STACK PUSH r15
COPY rStack r15
// 0 79:10
HALT
//  SysD.halt()

// 1 79:21
// ;

:func_exit_Kernal.exit
STACK POP r15
GOTO POP
#endfunction void

:__start
#function Kernal._main
STACK PUSH r15
COPY rStack r15
// 0 31:10
LOAD rIH &:Kernal._interrupt
//  asm("LOAD rIH &:Kernal._interrupt")

// 1 31:45
// ;

// 2 32:10
LOAD r1 1
STACK PUSH r1
LOAD r1 3
STACK PUSH r1
LOAD r1 &Kernal.CONSOLE_SETUP_CMD
STACK PUSH r1
GOTO PUSH :Kernal.peripheralCmd_uint32_uint32_uint32*
STACK DEC 12
//  peripheralCmd(0x0001, 3, & CONSOLE_SETUP_CMD)

// 3 32:54
// ;

// 4 33:10
STACK PUSH r1
LOAD r1 Kernal.CONSOLE_START
LOAD MEM r1 r1
LOAD r2 Kernal.CONSOLE_PNTR
LOAD MEM r2 r2
COPY r2 r2
STORE MEM r1 r2
STACK POP r1
//  SysD.memSet(CONSOLE_START, CONSOLE_PNTR)

// 5 33:50
// ;

// 6 37:10
//  uint32 a = Kernal.CONSOLE_PNTR;

:func_exit_Kernal._main
STACK DEC 4
STACK POP r15
HALT
#endfunction void

#function Kernal.createProcess
STACK PUSH r15
COPY rStack r15
// 0 112:10
//  uint32 nextPID = lastPID + 1;

// 1 113:10
COPY r15 r1
LOAD MEM r1 r1
INC r1 -1024
SET FORCE EQ r1 r1
GOTO EQ r1 :if_end_0
// 0 114:14
COPY r15 r1
LOAD r2 1
STORE r2 r1
//  nextPID = 1;

// 1 115:14
:while_condition_1
//  while(processStates[nextPID].status != 0) {nextPID++; if(nextPID == 1024) {nextPID = 1;} if(nextPID == lastPID) {return nullptr;}}

:if_end_0
//  if(nextPID == 1024) {nextPID = 1; while(processStates[nextPID].status != 0) {nextPID++; if(nextPID == 1024) {nextPID = 1;} if(nextPID == lastPID) {return nullptr;}}}

// 2 125:10
LOAD r1 &Kernal.lastPID
COPY r15 r2
LOAD MEM r2 r2
STORE r2 r1
//  lastPID = nextPID;

// 3 126:10
LOAD r1 &Kernal.processStates
COPY r15 r2
INC r2 -12
STORE r1 r2
GOTO :func_exit_Kernal.createProcess
//  return & processStates[nextPID];

:func_exit_Kernal.createProcess
STACK DEC 4
STACK POP r15
GOTO POP
#endfunction ProcessState*

#function Kernal._interrupt
STACK PUSH r15
COPY rStack r15
// 0 45:10
LOAD r1 &Kernal.processStates
STACK PUSH r1
//  ProcessState* oldState = & processStates[SysD.rPID];

// 1 46:10
COPY r15 r1
LOAD MEM r1 r1
LOAD r2 0
STORE r2 r1
//  oldState.pid = 0;

// 2 47:10
//  oldState.updateInterrupt();

// 3 49:10
//  uint32 code = SysD.rIC;

// 4 50:10
COPY r15 r1
INC r1 4
LOAD MEM r1 r1
LOAD r2 -2147483648
AND r1 r1 r2
SET FORCE EQ r1 r1
GOTO EQ r1 :if_else_2
// 0 51:14
LOAD r1 0
COPY rPM r1
//  SysD.rPM = false;

// 1 54:14
//  oldState.interruptHandler.call(code);

// 2 55:14
INTERRUPT RET
//  SysD.interruptReturn()

// 3 55:36
// ;

// 4 56:14
GOTO :func_exit_Kernal._interrupt
//  return;

GOTO :if_end_2
:if_else_2
// 0 58:14
COPY r15 r1
INC r1 4
LOAD MEM r1 r1
INC r1 2147483647
SET FORCE EQ r1 r1
GOTO EQ r1 :if_end_3
// 0 59:18
HALT
//  SysD.halt()

// 1 59:29
// ;

:if_end_3
//  if(code == 0x8000_0001) {SysD.halt();}

// 1 61:14
COPY r15 r1
INC r1 4
LOAD MEM r1 r1
LOAD r2 65536
AND r1 r1 r2
SET FORCE NEQ r1 r1
GOTO EQ r1 :if_end_4
// 

:if_end_4
//  if((code & 0x0001_0000) != 0) {}

:if_end_2
//  if((code & 0x8000_0000) == 0) {SysD.rPM = false; oldState.interruptHandler.call(code); SysD.interruptReturn(); return;} else {if(code == 0x8000_0001) {SysD.halt();} if((code & 0x0001_0000) != 0) {}}

// 5 69:10
INTERRUPT RET
//  SysD.interruptReturn()

// 6 69:32
// ;

:func_exit_Kernal._interrupt
STACK DEC 8
STACK POP r15
INTERRUPT RET
#endfunction void

#function Kernal.interruptExit
STACK PUSH r15
COPY rStack r15
// 

:func_exit_Kernal.interruptExit
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.peripheralCmd_uint32_uint32_uint32* deviceId uint32, cmdSize uint32, cmd uint32*
STACK PUSH r15
COPY rStack r15
// 0 83:10
//  uint32 addr = CMD_DEVICE;

// 1 84:10
STACK PUSH r1
COPY r15 r1
LOAD MEM r1 r1
COPY r15 r2
INC r2 -20
LOAD MEM r2 r2
COPY r2 r2
STORE MEM r1 r2
STACK POP r1
//  SysD.memSet(addr, deviceId)

// 2 84:37
// ;

// 3 85:10
COPY r15 r3
LOAD MEM r4 r3
INC r4 1
STORE r4 r3
//  addr++;

// 4 86:10
STACK PUSH r1
COPY r15 r1
LOAD MEM r1 r1
COPY r15 r2
INC r2 -16
LOAD MEM r2 r2
COPY r2 r2
STORE MEM r1 r2
STACK POP r1
//  SysD.memSet(addr, cmdSize)

// 5 86:36
// ;

// 6 87:10
COPY r15 r3
LOAD MEM r4 r3
INC r4 1
STORE r4 r3
//  addr++;

// 7 88:10
STACK PUSH r1
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
STACK PUSH r2
LOAD r2 0
COPY r15 r3
INC r3 -16
LOAD MEM r3 r3
COPY r15 r4
LOAD MEM r4 r4
LOAD r5 0
COPY r5 r5
ADD r1 r1 r2
ADD r4 r4 r5
SUB r3 r3 r2
:loop_5
COPY MEM r1 r4
INC r3 -1
GOTO GT r3 :loop_5
STACK POP r2
STACK POP r1
//  SysD.memCopy(cmd, 0, cmdSize, addr, 0)

// 8 88:61
// ;

// 9 89:10
STACK PUSH r1
LOAD r1 Kernal.CMD_STATUS
LOAD MEM r1 r1
LOAD r2 Kernal.CMD_WRITTEN
LOAD MEM r2 r2
COPY r2 r2
STORE MEM r1 r2
STACK POP r1
//  SysD.memSet(CMD_STATUS, CMD_WRITTEN)

// 10 89:46
// ;

:func_exit_Kernal.peripheralCmd_uint32_uint32_uint32*
STACK DEC 4
STACK POP r15
GOTO POP
#endfunction void

// Kernal.ProcessState

#function Kernal.ProcessState.create_ProcessState& state ProcessState&
STACK PUSH r15
COPY rStack r15
// 0 210:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
COPY rPID r2
STORE r2 r1
//  state.pid = SysD.rPID;

// 1 211:14
//  state.update();

// 2 212:14
COPY r0 r1
INC r1 88
LOAD r2 1
STORE r2 r1
//  status = 1;

// 3 213:14
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
COPY r15 r2
INC r2 -16
STORE r1 r2
GOTO :func_exit_Kernal.ProcessState.create_ProcessState&
//  return state;

:func_exit_Kernal.ProcessState.create_ProcessState&
STACK POP r15
GOTO POP
#endfunction ProcessState*

#function Kernal.ProcessState.apply
STACK PUSH r15
COPY rStack r15
// 0 197:14
COPY r0 r1
INC r1 16
LOAD MEM r1 r1
COPY rMemTbl r1
//  SysD.rMemTbl = memTablePtr;

// 1 198:14
COPY r0 r1
INC r1 12
LOAD MEM r1 r1
COPY rStack r1
//  SysD.rStack = stackPtr;

// 2 199:14
COPY r0 r1
INC r1 20
LOAD MEM r1 r1
COPY rPM r1
//  SysD.rPM = privileged;

// 3 200:14
COPY r0 r1
INC r1 8
LOAD MEM r1 r1
COPY rPgm r1
//  SysD.rPgm = pgmPtr;

:func_exit_Kernal.ProcessState.apply
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.ProcessState.applyNoPgm
STACK PUSH r15
COPY rStack r15
// 0 204:14
COPY r0 r1
INC r1 16
LOAD MEM r1 r1
COPY rMemTbl r1
//  SysD.rMemTbl = memTablePtr;

// 1 205:14
COPY r0 r1
INC r1 12
LOAD MEM r1 r1
COPY rStack r1
//  SysD.rStack = stackPtr;

// 2 206:14
COPY r0 r1
INC r1 20
LOAD MEM r1 r1
COPY rPM r1
//  SysD.rPM = privileged;

:func_exit_Kernal.ProcessState.applyNoPgm
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.ProcessState.update
STACK PUSH r15
COPY rStack r15
// 0 145:14
COPY r0 r1
INC r1 8
COPY rPgm r2
STORE r2 r1
//  pgmPtr = SysD.rPgm;

// 1 146:14
COPY r0 r1
INC r1 12
COPY rStack r2
STORE r2 r1
//  stackPtr = SysD.rStack;

// 2 147:14
COPY r0 r1
INC r1 16
COPY rMemTbl r2
STORE r2 r1
//  memTablePtr = SysD.rMemTbl;

// 3 148:14
COPY r0 r1
INC r1 20
COPY rPM r2
STORE r2 r1
//  privileged = SysD.rPM;

:func_exit_Kernal.ProcessState.update
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.ProcessState.setInterrupt
STACK PUSH r15
COPY rStack r15
// 0 174:14
COPY r0 r1
INC r1 12
LOAD MEM r1 r1
COPY rStackI r1
//  SysD.rStackI = stackPtr;

// 1 175:14
COPY r0 r1
INC r1 20
LOAD MEM r1 r1
COPY rPMI r1
//  SysD.rPMI = privileged;

// 2 176:14
COPY r0 r1
INC r1 8
LOAD MEM r1 r1
COPY rPgmI r1
//  SysD.rPgmI = pgmPtr;

// 3 177:14
COPY r0 r1
INC r1 16
LOAD MEM r1 r1
COPY rMemTblI r1
//  SysD.rMemTblI = memTablePtr;

// 4 178:14
COPY r0 r1
INC r1 24
LOAD MEM r1 r1
//  SysD.r0I = registers[0];

// 5 179:14
COPY r0 r1
INC r1 24
LOAD MEM r1 r1
//  SysD.r1I = registers[1];

// 6 180:14
COPY r0 r1
INC r1 24
LOAD MEM r1 r1
//  SysD.r2I = registers[2];

// 7 181:14
COPY r0 r1
INC r1 24
LOAD MEM r1 r1
//  SysD.r3I = registers[3];

// 8 182:14
COPY r0 r1
INC r1 24
LOAD MEM r1 r1
//  SysD.r4I = registers[4];

// 9 183:14
COPY r0 r1
INC r1 24
LOAD MEM r1 r1
//  SysD.r5I = registers[5];

// 10 184:14
COPY r0 r1
INC r1 24
LOAD MEM r1 r1
//  SysD.r6I = registers[6];

// 11 185:14
COPY r0 r1
INC r1 24
LOAD MEM r1 r1
//  SysD.r7I = registers[7];

// 12 186:14
COPY r0 r1
INC r1 24
LOAD MEM r1 r1
//  SysD.r8I = registers[8];

// 13 187:14
COPY r0 r1
INC r1 24
LOAD MEM r1 r1
//  SysD.r9I = registers[9];

// 14 188:14
COPY r0 r1
INC r1 24
LOAD MEM r1 r1
//  SysD.r10I = registers[10];

// 15 189:14
COPY r0 r1
INC r1 24
LOAD MEM r1 r1
//  SysD.r11I = registers[11];

// 16 190:14
COPY r0 r1
INC r1 24
LOAD MEM r1 r1
//  SysD.r12I = registers[12];

// 17 191:14
COPY r0 r1
INC r1 24
LOAD MEM r1 r1
//  SysD.r13I = registers[13];

// 18 192:14
COPY r0 r1
INC r1 24
LOAD MEM r1 r1
//  SysD.r14I = registers[14];

// 19 193:14
COPY r0 r1
INC r1 24
LOAD MEM r1 r1
//  SysD.r15I = registers[15];

:func_exit_Kernal.ProcessState.setInterrupt
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.ProcessState.updateInterrupt
STACK PUSH r15
COPY rStack r15
// 0 152:14
COPY r0 r1
INC r1 12
COPY rStackI r2
STORE r2 r1
//  stackPtr = SysD.rStackI;

// 1 153:14
COPY r0 r1
INC r1 20
COPY rPM r2
STORE r2 r1
//  privileged = SysD.rPMI;

// 2 154:14
COPY r0 r1
INC r1 8
COPY rPgmI r2
STORE r2 r1
//  pgmPtr = SysD.rPgmI;

// 3 155:14
COPY r0 r1
INC r1 16
COPY rMemTblI r2
STORE r2 r1
//  memTablePtr = SysD.rMemTblI;

// 4 156:14
COPY r0 r1
INC r1 24
COPY r0I r2
//  registers[0] = SysD.r0I;

// 5 157:14
COPY r0 r1
INC r1 24
COPY r1I r2
//  registers[1] = SysD.r1I;

// 6 158:14
COPY r0 r1
INC r1 24
COPY r2I r2
//  registers[2] = SysD.r2I;

// 7 159:14
COPY r0 r1
INC r1 24
COPY r3I r2
//  registers[3] = SysD.r3I;

// 8 160:14
COPY r0 r1
INC r1 24
COPY r4I r2
//  registers[4] = SysD.r4I;

// 9 161:14
COPY r0 r1
INC r1 24
COPY r5I r2
//  registers[5] = SysD.r5I;

// 10 162:14
COPY r0 r1
INC r1 24
COPY r6I r2
//  registers[6] = SysD.r6I;

// 11 163:14
COPY r0 r1
INC r1 24
COPY r7I r2
//  registers[7] = SysD.r7I;

// 12 164:14
COPY r0 r1
INC r1 24
COPY r8I r2
//  registers[8] = SysD.r8I;

// 13 165:14
COPY r0 r1
INC r1 24
COPY r9I r2
//  registers[9] = SysD.r9I;

// 14 166:14
COPY r0 r1
INC r1 24
COPY r10I r2
//  registers[10] = SysD.r10I;

// 15 167:14
COPY r0 r1
INC r1 24
COPY r11I r2
//  registers[11] = SysD.r11I;

// 16 168:14
COPY r0 r1
INC r1 24
COPY r12I r2
//  registers[12] = SysD.r12I;

// 17 169:14
COPY r0 r1
INC r1 24
COPY r13I r2
//  registers[13] = SysD.r13I;

// 18 170:14
COPY r0 r1
INC r1 24
COPY r14I r2
//  registers[14] = SysD.r14I;

// 19 171:14
COPY r0 r1
INC r1 24
COPY r15I r2
//  registers[15] = SysD.r15I;

:func_exit_Kernal.ProcessState.updateInterrupt
STACK POP r15
GOTO POP
#endfunction void

// Kernal.Memory

// Kernal.Memory.PageMapTable

// Kernal.Console

#function Kernal.Console.Console_void*_uint32_uint32 address void*, deviceId uint32, consoleSize uint32
STACK PUSH r15
COPY rStack r15
// 0 14:10
COPY r0 r1
INC r1 4
COPY r15 r2
INC r2 -20
LOAD MEM r2 r2
STORE r2 r1
//  this.address = address;

// 1 15:10
COPY r0 r1
INC r1 8
COPY r15 r2
INC r2 -16
LOAD MEM r2 r2
STORE r2 r1
//  this.deviceId = deviceId;

// 2 16:10
COPY r0 r1
INC r1 16
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
STORE r2 r1
//  this.consoleSize = consoleSize;

// 3 17:10
COPY r0 r1
INC r1 12
COPY r15 r2
INC r2 -20
LOAD MEM r2 r2
COPY r15 r3
INC r3 -12
LOAD MEM r3 r3
LOAD r4 2
MUL r3 r3 r4
ADD r2 r2 r3
INC r2 1
STORE r2 r1
//  end = address + (consoleSize* 2) + 1;

// 4 18:10
//  uint32 cmd = {0x0001, address, consoleSize};

// 5 19:10
COPY r15 r1
INC r1 -16
LOAD MEM r1 r1
STACK PUSH r1
LOAD r1 3
STACK PUSH r1
COPY r15 r1
STACK PUSH r1
GOTO PUSH :Kernal.peripheralCmd_uint32_uint32_uint32*
STACK DEC 12
//  Kernal.peripheralCmd(deviceId, 3, & cmd)

// 6 19:49
// ;

:func_exit_Kernal.Console.Console_void*_uint32_uint32
STACK DEC 4
STACK POP r15
GOTO POP
#endfunction Console

#function Kernal.Console.print_char*_uint32 str char*, len uint32
STACK PUSH r15
COPY rStack r15
// 0 33:10
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
SET FORCE EQ r1 r1
GOTO EQ r1 :if_else_6
// 0 34:14
//  uint32 i = 0;

// 1 35:14
:while_condition_7
//  while(str[i] != 0) {printChar(str[i]); i++;}

STACK DEC 4
GOTO :if_end_6
:if_else_6
// 0 40:14
//  for(uint32 i = 0; i < len; i++) {printChar(str[i]);}

:if_end_6
//  if(len == 0) {uint32 i = 0; while(str[i] != 0) {printChar(str[i]); i++;}} else {for(uint32 i = 0; i < len; i++) {printChar(str[i]);}}

:func_exit_Kernal.Console.print_char*_uint32
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.Console.printChar_char c char
STACK PUSH r15
COPY rStack r15
// 0 23:10
COPY r0 r1
INC r1 4
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
//  address[index] = c;

// 1 24:10
COPY r0 r1
INC r1 20
LOAD MEM r2 r1
INC r2 1
STORE r2 r1
//  index++;

// 2 25:10
COPY r0 r1
INC r1 4
LOAD r2 1
STORE r2 r1
//  address[index] = 0x1;

// 3 26:10
COPY r0 r1
INC r1 20
LOAD MEM r2 r1
INC r2 1
STORE r2 r1
//  index++;

// 4 27:10
COPY r0 r1
INC r1 20
LOAD MEM r1 r1
COPY r0 r2
INC r2 16
LOAD MEM r2 r2
SUB r1 r1 r2
SET FORCE GT r1 r1
GOTO EQ r1 :if_end_8
// 0 28:14
COPY r0 r1
INC r1 20
LOAD r2 0
STORE r2 r1
//  index = 0;

:if_end_8
//  if(index > consoleSize) {index = 0;}

:func_exit_Kernal.Console.printChar_char
STACK POP r15
GOTO POP
#endfunction void

// Kernal.Method

HALT