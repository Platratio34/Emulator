// static data
// Kernal
#define Kernal.MAX_BLOCKS 0x0020 int32
#var Kernal.lastPID 0x0001 int32
#define Kernal.CMD_ADDR 0x0001_0000 int32*
#var Kernal.kallocMutex 0x00 Mutex
#define Kernal.pageFreeTable 0x8000 bool*
#var Kernal.processReadyQueue (128) ProcessState*[32]
#define Kernal.TIMER_UNIT 0x00 int32*
#define Kernal.CMD_DEVICE 0x0001_0002 uint16*
#var Kernal.SYS_NAME "EmulatorOS\0" char*
#define Kernal.CONSOLE_OUT 0x0001_0300 char*
#define Kernal.CMD_WRITTEN 0x0001 int32
#var Kernal.processStates (131072) ProcessState[1024]
#var Kernal.processReadyQueueLock 0x00 Mutex
#define Kernal.CMD_STATUS 0x0001_0001 uint8*
#define Kernal.CONSOLE_IN 0x0001_0301 char*
#define Kernal.CONSOLE_IN_COUNT 0x0001_0302 uint8*
#define Kernal.CMD_START 0x0001_0008 int32*
#define Kernal.CMD_SIZE 0x0001_0004 int32*
// Kernal.ProcessStatus
#define Kernal.ProcessStatus.READY 0x0002 ProcessStatus
#define Kernal.ProcessStatus.RUNNING 0x0003 ProcessStatus
#define Kernal.ProcessStatus.WAITING 0x0004 ProcessStatus
#define Kernal.ProcessStatus.DEAD 0x0005 ProcessStatus
#define Kernal.ProcessStatus.NONE 0x0000 ProcessStatus
#define Kernal.ProcessStatus.SETUP 0x0001 ProcessStatus
// Kernal.FS
#var Kernal.FS.fsDeviceId 0x0000 int32
// Kernal.FS.FileHandle
#var Kernal.FS.FileHandle.pool (3072) FileHandle[128]
#var Kernal.FS.FileHandle.nextFree 0xffffffff FileHandle*
// Kernal.FS.OpenMode
#define Kernal.FS.OpenMode.READ 0x0000 OpenMode
#define Kernal.FS.OpenMode.STREAM_READ 0x0008 OpenMode
#define Kernal.FS.OpenMode.STREAM_WRITE 0x0018 OpenMode
#define Kernal.FS.OpenMode.APPEND 0x0011 OpenMode
#define Kernal.FS.OpenMode.WRITE 0x0010 OpenMode
// Kernal.FS.ProcessFiles
#var Kernal.FS.ProcessFiles.pool (8704) ProcessFiles[128]
#var Kernal.FS.ProcessFiles.nextFree 0xffffffff ProcessFiles*

// Ref static data
// SysD
#define SysD.MEMORY_DEVICE_START 0x0001_0000 int32
#define SysD.MEMORY_PROCESS_START 0x0002_0000 int32
#define SysD.MEMORY_BLOCK_SIZE 0x8000 int32
// Peripheral
#define Peripheral.TABLE 0x0001_0100 int32*
#define Peripheral.TIMERS 0x0001_0200 int32*
#define Peripheral.TYPE_STORAGE_BLOCK 0x0100_0002 int32*
#define Peripheral.RSP_DEVICE 0x0001_0083 uint8*
#define Peripheral.TYPE_DISPLAY_CHARACTER 0x0100_0011 int32*
#define Peripheral.CMD_ADDR 0x0001_0000 int32*
#define Peripheral.RSP_STATUS 0x0001_0080 uint8*
#define Peripheral.RSP_DATA 0x0001_0084 int32*
#define Peripheral.CMD_DATA 0x0001_0008 int32*
#define Peripheral.TYPE_STORAGE_VIRTUAL 0x0100_0001 int32*
#define Peripheral.CMD_SIZE 0x0001_0004 int32*

//--------
// text

// Kernal

#function Kernal.read_char*_int32 buffer char*, bufferSize int32
STACK PUSH r15
COPY rStack r15
#stackVar char* buffer -16
#stackVar int32 bufferSize -12
#line run\lang\Kernal\console.el 138:10
#line run\lang\Kernal\console.el 139:14
LOAD r1 Kernal.CONSOLE_IN_COUNT
#line run\lang\Kernal\console.el 140:14
:read_l0
#line run\lang\Kernal\console.el 141:14
LOAD MEM BYTE r2 r1
#line run\lang\Kernal\console.el 142:14
GOTO EQ r2 :read_l0
//  asm{\nLOAD r1 Kernal.CONSOLE_IN_COUNT\n:read_l0\nLOAD MEM BYTE r2 r1\nGOTO EQ r2 :read_l0\n}

#line run\lang\Kernal\console.el 144:10
// Reserving r1
// Releasing r1
LOAD r1 Kernal.CONSOLE_IN_COUNT
// Reserving r1

LOAD MEM BYTE r1 r1 // *CONSOLE_IN_COUNT
#stackVar int32 inCount
STACK PUSH r1
// Releasing r1
//  int32 inCount =* CONSOLE_IN_COUNT;

#line run\lang\Kernal\console.el 145:10
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
// Register r2 already reserved
LOAD MEM r2 r15
SUB r1 r1 r2
GOTO GEQ r1 :if_end_0 // bufferSize < inCount
// Releasing r1
#line run\lang\Kernal\console.el 146:14
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1 // bufferSize
STORE r1 r15
// Releasing r1
//  inCount = bufferSize;

#lineend
:if_end_0
//  if(bufferSize < inCount) {inCount = bufferSize;}

#line run\lang\Kernal\console.el 148:10
// Reserving r1
// Releasing r1
LOAD r1 0 // 0
#stackVar int32 i
STACK PUSH r1
// Releasing r1
//  int32 i = 0;

#line run\lang\Kernal\console.el 149:10
:while_condition_1
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
// Register r2 already reserved
LOAD MEM r2 r15
SUB r1 r1 r2
GOTO GEQ r1 :while_end_1 // i < inCount
// Releasing r1
#line run\lang\Kernal\console.el 150:14
// Reserving r1
SUB r1 r15 16
// Register r1 already reserved
LOAD MEM r1 r1
// Reserving r2
ADD r2 r15 4
// Register r2 already reserved
LOAD MEM r2 r2 // i
ADD r1 r1 r2
// Releasing r2
// Reserving r2
LOAD r2 Kernal.CONSOLE_IN
// Register r2 already reserved

LOAD MEM BYTE r2 r2 // *CONSOLE_IN
STORE BYTE r2 r1
// Releasing r1
// Releasing r2
//  buffer[i] =* CONSOLE_IN;

#line run\lang\Kernal\console.el 152:14
ADD r2 r15 4
// Register r2 already reserved
LOAD MEM r1 r2
ADD r3 r1 1
STORE r3 r2 // i++
//  i++;

#lineend
GOTO :while_condition_1
:while_end_1
//  while(i < inCount) {buffer[i] =* CONSOLE_IN; i++;}

#line run\lang\Kernal\console.el 154:10
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
SUB r2 r15 12
// Register r2 already reserved
LOAD MEM r2 r2
SUB r1 r1 r2
GOTO GEQ r1 :if_end_2 // i < bufferSize
// Releasing r1
#line run\lang\Kernal\console.el 155:14
// Reserving r1
SUB r1 r15 16
// Register r1 already reserved
LOAD MEM r1 r1
// Reserving r2
ADD r2 r15 4
// Register r2 already reserved
LOAD MEM r2 r2 // i
ADD r1 r1 r2
// Releasing r2
STORE BYTE 0 r1
// Releasing r1
//  buffer[i] = '\0';

#lineend
:if_end_2
//  if(i < bufferSize) {buffer[i] = '\0';}

#lineend
:func_exit_Kernal.read_char*_int32
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#syscall 2 Kernal.kfree_int32
#function syscall::Kernal.kfree_int32 num int32
STACK PUSH r15
COPY rStack r15
#stackVar int32 num -12
#line run\lang\Kernal\kalloc.el 45:10
#alias r1 this // Reserving r1
LOAD this &Kernal.kallocMutex
// INLINE START Mutex.acquire
:Mutex.acquire_loop_2
// Resolving placeholder 1 to r2
TEST AND SET r2 this
GOTO NEQ r2 :Mutex.acquire_loop_2
// INLINE END
STACK POP r0
#alias clear this // Releasing r1 // kallocMutex.acquire()
//  kallocMutex.acquire();

#line run\lang\Kernal\kalloc.el 46:10
// Reserving r1
// Releasing r1
// Reserving r1
// Reserving r2
// Releasing r2
LOAD MEM r1 rMemTbl // SysD.rMemTbl[0]
#stackVar int32 cPages
STACK PUSH r1
// Releasing r1
//  int32 cPages = SysD.rMemTbl[0];

#line run\lang\Kernal\kalloc.el 47:10
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
// Register r2 already reserved
LOAD MEM r2 r15
SUB r1 r1 r2
GOTO LEQ r1 :if_end_3 // num > cPages
// Releasing r1
#line run\lang\Kernal\kalloc.el 48:14
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 r15 // cPages
STORE r2 r1
// Releasing r1
// Releasing r2
//  num = cPages;

#lineend
:if_end_3
//  if(num > cPages) {num = cPages;}

#line run\lang\Kernal\kalloc.el 50:10
:while_condition_4
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
GOTO LEQ r1 :while_end_4 // num > 0
// Releasing r1
#line run\lang\Kernal\kalloc.el 51:14
// Register r2 already reserved
COPY r15 r2
LOAD MEM r1 r2
SUB r3 r1 1
STORE r3 r2 // cPages--
//  cPages--;

#line run\lang\Kernal\kalloc.el 52:14
// Reserving r1
LOAD r1 Kernal.pageFreeTable
// Register r1 already reserved
// Reserving r2
// Register r2 already reserved
// Reserving r3
// Register r3 already reserved
LOAD MEM r3 r15 // cPages
LSH r3 r3 2
ADD r2 rMemTbl r3
// Releasing r3
LOAD MEM r2 r2 // SysD.rMemTbl[cPages]
LOAD r3 2147483647
AND r2 r2 r3 // cast<int32>(SysD.rMemTbl[cPages]) & 0x7fff_ffff
RSH r2 r2 12 // ( cast<int32>(SysD.rMemTbl[cPages]) & 0x7fff_ffff ) >> 12
ADD r1 r1 r2
// Releasing r2
STORE BYTE 0 r1
// Releasing r1
//  pageFreeTable[(cast<int32>(SysD.rMemTbl[cPages]) & 0x7fff_ffff) >> 12] = false;

#line run\lang\Kernal\kalloc.el 53:14
SUB r2 r15 12
// Register r2 already reserved
LOAD MEM r1 r2
SUB r3 r1 1
STORE r3 r2 // num--
//  num--;

#lineend
GOTO :while_condition_4
:while_end_4
//  while(num > 0) {cPages--; pageFreeTable[(cast<int32>(SysD.rMemTbl[cPages]) & 0x7fff_ffff) >> 12] = false; num--;}

#line run\lang\Kernal\kalloc.el 55:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15 // cPages
STORE r1 rMemTbl
// Releasing r1
//  SysD.rMemTbl[0] = cPages;

#line run\lang\Kernal\kalloc.el 56:10
#alias r1 this // Reserving r1
LOAD this &Kernal.kallocMutex
// INLINE START Mutex.release
STORE BYTE 0x0 this
// INLINE END
STACK POP r0
#alias clear this // Releasing r1 // kallocMutex.release()
//  kallocMutex.release();

#lineend
:func_exit_Kernal.kfree_int32
COPY r15 rStack
STACK POP r15
SYSRETURN
#endfunction void

:__start
#function Kernal._main
STACK PUSH r15
COPY rStack r15
#line run\lang\Kernal\kernal.el 29:10
#line run\lang\Kernal\kernal.el 30:14
LOAD rIH &:Kernal._interrupt
#line run\lang\Kernal\kernal.el 31:14
LOAD rPID 0
//  asm{\nLOAD rIH &:Kernal._interrupt\nLOAD rPID 0\n}

#line run\lang\Kernal\kernal.el 34:10
// Reserving r1
#define exp_str_inline_11 "Starting \0"
LOAD r1 &exp_str_inline_11 // Starting \0
STACK PUSH r1
// Releasing r1
GOTO PUSH :Kernal.printStr_char*
STACK DEC 4
// Releasing r1 // printStr("Starting \0")
//  printStr("Starting \0");

#line run\lang\Kernal\kernal.el 35:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &Kernal.SYS_NAME // SYS_NAME
STACK PUSH r1
// Releasing r1
GOTO PUSH :Kernal.printStr_char*
STACK DEC 4
// Releasing r1 // printStr(SYS_NAME)
//  printStr(SYS_NAME);

#line run\lang\Kernal\kernal.el 36:10
#alias r1 c // Reserving r1
LOAD c '\n' // \n
// INLINE START Kernal.printChar
#line run\lang\Kernal\console.el 15:10
#line run\lang\Kernal\console.el 16:14
STORE BYTE c Kernal.CONSOLE_OUT
//  asm{\nSTORE BYTE c Kernal.CONSOLE_OUT\n}

#lineend
:func_exit_Kernal.printChar_char
// INLINE END
#alias clear c // Releasing r1 // printChar('\n')
//  printChar('\n');

#line run\lang\Kernal\kernal.el 40:10
// Reserving r1
// Releasing r1
// Reserving r1
LOAD r1 &Kernal.processStates
// Reserving r2
INC r1 128
// Releasing r2 // &processStates[1]
#stackVar ProcessState& kernalProcess
STACK PUSH r1
// Releasing r1
//  ProcessState & kernalProcess = & processStates[1];

#line run\lang\Kernal\kernal.el 41:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 84
STORE BYTE 3 r1
// Releasing r1
//  kernalProcess.status = ProcessStatus.RUNNING;

#line run\lang\Kernal\kernal.el 42:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 92
STORE 0 r1
// Releasing r1
//  kernalProcess.parent = 0;

#line run\lang\Kernal\kernal.el 43:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
STORE 1 r1
// Releasing r1
//  kernalProcess.pid = 1;

#line run\lang\Kernal\kernal.el 44:10
LOAD rPID 1
// Releasing [un-set register]
//  SysD.rPID = 1;

#line run\lang\Kernal\kernal.el 45:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 88
STORE 0 r1
// Releasing r1
//  kernalProcess.interruptHandler = nullptr;

#line run\lang\Kernal\kernal.el 47:10
// For Loop:
// Initializer
// Reserving r1
// Reserving r2
// Releasing r2
LOAD r2 2 // 2
#stackVar int32 pI
STACK PUSH r2
// Releasing r2
:for_condition_5
// Reserving r2
// Register r3 already reserved
COPY r1 r3
INC r3 -1024
GOTO GEQ r3 :for_end_5 // pI < 1024
// Releasing r2
#line run\lang\Kernal\kernal.el 48:14
// Still reserved: r1
// Reserving r2
// Register r2 already reserved
LOAD r2 &Kernal.processStates
// Reserving r3
// Register r3 already reserved
COPY r1 r3 // pI
LSH r3 r3 7
ADD r2 r2 r3
// Releasing r3
INC r2 84
STORE BYTE 0 r2
// Releasing r2
//  processStates[pI].status = ProcessStatus.NONE;

#lineend
// Reserved: r1
// Iterator
INC r1 1 // pI++
GOTO :for_condition_5
:for_end_5
// End of scope
// Releasing r1
//  for(int32 pI = 2; pI < 1024; pI++) {processStates[pI].status = ProcessStatus.NONE;}

#line run\lang\Kernal\kernal.el 54:10
#line run\lang\Kernal\kernal.el 55:14
:kLoop
#line run\lang\Kernal\kernal.el 56:14
LOAD MEM r1 &Kernal.processReadyQueue
#line run\lang\Kernal\kernal.el 57:14
GOTO EQ r1 :kLoop
#line run\lang\Kernal\kernal.el 58:14
INTERRUPT 0x9000_0000
#line run\lang\Kernal\kernal.el 59:14
GOTO :kLoop
//  asm{\n:kLoop\nLOAD MEM r1 &Kernal.processReadyQueue\nGOTO EQ r1 :kLoop\nINTERRUPT 0x9000_0000\nGOTO :kLoop\n}

#lineend
:func_exit_Kernal._main
COPY r15 rStack
STACK POP r15
HALT
#endfunction void

#function Kernal.intToDec_int32_char* value int32, str char*
STACK PUSH r15
COPY rStack r15
#stackVar char* str -12
#stackVar int32 value -16
#line run\lang\Kernal\console.el 102:10
#line run\lang\Kernal\console.el 103:14
SUB r1 r15 16
#line run\lang\Kernal\console.el 104:14
LOAD MEM r1 r1 // value
#line run\lang\Kernal\console.el 105:14
SUB r2 r15 12
#line run\lang\Kernal\console.el 106:14
LOAD MEM r2 r2 // str
#line run\lang\Kernal\console.el 107:14
COPY rStack r3
#stackVar char[16] tempStr
#line run\lang\Kernal\console.el 109:14
STACK INC 16 // char* str2
#line run\lang\Kernal\console.el 110:14
LOAD r4 10
#line run\lang\Kernal\console.el 111:14
LOAD r6 0x0
#line run\lang\Kernal\console.el 112:14
:intToDec_l1
#line run\lang\Kernal\console.el 113:18
DIV r1 r1 r4
#line run\lang\Kernal\console.el 114:18
COPY rAF r7
#line run\lang\Kernal\console.el 115:18
ADD r7 r7 0x30
#line run\lang\Kernal\console.el 116:18
STORE BYTE r7 r3 INC_RA
#line run\lang\Kernal\console.el 117:18
INC r6
#line run\lang\Kernal\console.el 118:18
GOTO NEQ r1 :intToDec_l1
#line run\lang\Kernal\console.el 119:14
INC r3 -1
#line run\lang\Kernal\console.el 120:14
:intToDec_l2
#line run\lang\Kernal\console.el 121:18
COPY MEM BYTE r3 r2 INC_RD
#line run\lang\Kernal\console.el 122:18
INC r6 -1
#line run\lang\Kernal\console.el 123:18
INC r3 -1
#line run\lang\Kernal\console.el 124:18
GOTO NEQ r6 :intToDec_l2
#line run\lang\Kernal\console.el 125:14
STORE BYTE 0x0 r2
#stackVarClear tempStr
//  asm{\nSUB r1 r15 16\nLOAD MEM r1 r1 // value\nSUB r2 r15 12\nLOAD MEM r2 r2 // str\nCOPY rStack r3\n#stackVar char[16] tempStr\nSTACK INC 16 // char* str2\nLOAD r4 10\nLOAD r6 0x0\n:intToDec_l1\nDIV r1 r1 r4\nCOPY rAF r7\nADD r7 r7 0x30\nSTORE BYTE r7 r3 INC_RA\nINC r6\nGOTO NEQ r1 :intToDec_l1\nINC r3 -1\n:intToDec_l2\nCOPY MEM BYTE r3 r2 INC_RD\nINC r6 -1\nINC r3 -1\nGOTO NEQ r6 :intToDec_l2\nSTORE BYTE 0x0 r2\n#stackVarClear tempStr\n}

#lineend
:func_exit_Kernal.intToDec_int32_char*
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.createProcess
STACK PUSH r15
COPY rStack r15
#line run\lang\Kernal\kernal.el 160:10
// Reserving r1
// Releasing r1
// Reserving r1
LOAD MEM r1 &Kernal.lastPID
INC r1 1 // lastPID + 1
#stackVar int32 nextPID
STACK PUSH r1
// Releasing r1
//  int32 nextPID = lastPID + 1;

#line run\lang\Kernal\kernal.el 161:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 -1024
GOTO NEQ r1 :if_end_6 // nextPID == 1024
// Releasing r1
#line run\lang\Kernal\kernal.el 162:14
STORE 2 r15
//  nextPID = 2;

#lineend
:if_end_6
//  if(nextPID == 1024) {nextPID = 2;}

#line run\lang\Kernal\kernal.el 164:10
:while_condition_7
// Reserving r1
// Register r1 already reserved
LOAD r1 &Kernal.processStates
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 r15 // nextPID
LSH r2 r2 7
ADD r1 r1 r2
// Releasing r2
INC r1 84
LOAD MEM BYTE r1 r1 // processStates[nextPID].status != ProcessStatus.NONE
GOTO EQ r1 :while_end_7
// Releasing r1
#line run\lang\Kernal\kernal.el 165:14
// Register r2 already reserved
COPY r15 r2
LOAD MEM r1 r2
ADD r3 r1 1
STORE r3 r2 // nextPID++
//  nextPID++;

#line run\lang\Kernal\kernal.el 166:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 -1024
GOTO NEQ r1 :if_end_8 // nextPID == 1024
// Releasing r1
#line run\lang\Kernal\kernal.el 167:18
STORE 2 r15
//  nextPID = 2;

#lineend
:if_end_8
//  if(nextPID == 1024) {nextPID = 2;}

#line run\lang\Kernal\kernal.el 169:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
// Register r2 already reserved
LOAD MEM r2 &Kernal.lastPID
SUB r1 r1 r2
GOTO NEQ r1 :if_end_9 // nextPID == lastPID
// Releasing r1
#line run\lang\Kernal\kernal.el 170:18
// Reserving r1
SUB r1 r15 12
STORE 0 r1
// Releasing r1
GOTO :func_exit_Kernal.createProcess
//  return nullptr;

#lineend
:if_end_9
//  if(nextPID == lastPID) {return nullptr;}

#lineend
GOTO :while_condition_7
:while_end_7
//  while(processStates[nextPID].status != ProcessStatus.NONE) {nextPID++; if(nextPID == 1024) {nextPID = 2;} if(nextPID == lastPID) {return nullptr;}}

#line run\lang\Kernal\kernal.el 173:10
// Reserving r1
// Register r1 already reserved
LOAD r1 &Kernal.lastPID
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 r15 // nextPID
STORE r2 r1
// Releasing r1
// Releasing r2
//  lastPID = nextPID;

#line run\lang\Kernal\kernal.el 174:10
// Reserving r1
// Register r1 already reserved
LOAD r1 &Kernal.processStates
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 r15 // nextPID
LSH r2 r2 7
ADD r1 r1 r2
// Releasing r2 // &processStates[nextPID]
// Reserving r2
SUB r2 r15 12
STORE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_Kernal.createProcess
//  return & processStates[nextPID];

#lineend
:func_exit_Kernal.createProcess
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction ProcessState*

#function Kernal._interrupt
STACK PUSH r15
COPY rStack r15
#line run\lang\Kernal\kernal.el 65:10
// Reserving r1
// Releasing r1
// Reserving r1
COPY rIC r1 // SysD.rIC
#stackVar int32 code
STACK PUSH r1
// Releasing r1
//  int32 code = SysD.rIC;

#line run\lang\Kernal\kernal.el 66:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
LOAD r2 -2147483648
AND r1 r1 r2 // code & 0x8000_0000
GOTO NEQ r1 :if_end_10 // ( code & 0x8000_0000 ) == 0
// Releasing r1
#line run\lang\Kernal\kernal.el 67:14
// Reserving r1
// Releasing r1
// Reserving r1
LOAD r1 &Kernal.processStates
// Reserving r2
// Register r2 already reserved
COPY rPID r2 // SysD.rPID
LSH r2 r2 7
ADD r1 r1 r2
// Releasing r2 // &processStates[SysD.rPID]
#stackVar ProcessState& cProc
STACK PUSH r1
// Releasing r1
//  ProcessState & cProc = & processStates[SysD.rPID];

#line run\lang\Kernal\kernal.el 68:14
LOAD rPM 0
// Releasing [un-set register]
//  SysD.rPM = false;

#line run\lang\Kernal\kernal.el 71:14
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 88
LOAD MEM r1 r1 // cProc.interruptHandler
GOTO EQ r1 :if_end_11
// Releasing r1
#line run\lang\Kernal\kernal.el 72:18
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15 // code
STACK PUSH r1
// Releasing r1
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 88
GOTO PUSH r1
// Releasing r1
STACK DEC 4
// Releasing r1 // cProc.interruptHandler(code)
//  cProc.interruptHandler(code);

#lineend
:if_end_11
//  if(cProc.interruptHandler) {cProc.interruptHandler(code);}

#line run\lang\Kernal\kernal.el 74:14
INTERRUPT RET // SysD.interruptReturn()
//  SysD.interruptReturn();

#line run\lang\Kernal\kernal.el 75:14
GOTO :func_exit_Kernal._interrupt
//  return;

#lineend
STACK DEC 4
// End of scope
#stackVarClear cProc
:if_end_10
//  if((code & 0x8000_0000) == 0) {ProcessState & cProc = & processStates[SysD.rPID]; SysD.rPM = false; if(cProc.interruptHandler) {cProc.interruptHandler(code);} SysD.interruptReturn(); return;}

#line run\lang\Kernal\kernal.el 78:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
LOAD r2 -2147483647
SUB r1 r1 r2
GOTO NEQ r1 :if_end_12 // code == 0x8000_0001
// Releasing r1
#line run\lang\Kernal\kernal.el 79:14
HALT // SysD.halt()
//  SysD.halt();

#lineend
:if_end_12
//  if(code == 0x8000_0001) {SysD.halt();}

#line run\lang\Kernal\kernal.el 81:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
LOAD r2 -256
AND r1 r1 r2 // code & 0xffff_ff00
LOAD r2 -2147483136
SUB r1 r1 r2
GOTO NEQ r1 :if_end_13 // ( code & 0xffff_ff00 ) == 0x8000_0200
// Releasing r1
#line run\lang\Kernal\kernal.el 82:14
// Reserving r1
// Releasing r1
// Reserving r1
LOAD MEM r1 r15
LOAD r2 255
AND r1 r1 r2 // code & 0xff
#stackVar int32 i
STACK PUSH r1
// Releasing r1
//  int32 i = code & 0xff;

#lineend
STACK DEC 4
// End of scope
#stackVarClear i
:if_end_13
//  if((code & 0xffff_ff00) == 0x8000_0200) {int32 i = code & 0xff;}

#line run\lang\Kernal\kernal.el 84:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
LOAD r2 -16
AND r1 r1 r2
LOAD r2 -1879048192
SUB r1 r1 r2
GOTO NEQ r1 :if_end_14 // code & 0xffff_fff0 == 0x9000_0000
// Releasing r1
#line run\lang\Kernal\kernal.el 85:14
// Reserving r1
// Releasing r1
// Reserving r1
LOAD r1 &Kernal.processStates
// Reserving r2
// Register r2 already reserved
COPY rPIDI r2 // SysD.rPIDI
LSH r2 r2 7
ADD r1 r1 r2
// Releasing r2 // &processStates[SysD.rPIDI]
#stackVar ProcessState& oldState
STACK PUSH r1
// Releasing r1
//  ProcessState & oldState = & processStates[SysD.rPIDI];

#line run\lang\Kernal\kernal.el 86:14
STACK PUSH r0
// Reserving r1
// Reserving r2
ADD r2 r15 4
// Register r2 already reserved
COPY r0 r2
// Releasing r2
GOTO PUSH :Kernal.ProcessState.updateInterrupt
STACK POP r0
// Releasing r1 // oldState.updateInterrupt()
//  oldState.updateInterrupt();

#line run\lang\Kernal\kernal.el 87:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
LOAD r2 -1879048191
SUB r1 r1 r2
GOTO NEQ r1 :if_end_15 // code == 0x9000_0001
// Releasing r1
#line run\lang\Kernal\kernal.el 88:18
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 84
LOAD MEM BYTE r1 r1
INC r1 -3
GOTO NEQ r1 :if_end_16 // oldState.status == ProcessStatus.RUNNING
// Releasing r1
#line run\lang\Kernal\kernal.el 89:22
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 84
STORE BYTE 2 r1
// Releasing r1
//  oldState.status = ProcessStatus.READY;

#lineend
:if_end_16
//  if(oldState.status == ProcessStatus.RUNNING) {oldState.status = ProcessStatus.READY;}

#lineend
:if_end_15
//  if(code == 0x9000_0001) {if(oldState.status == ProcessStatus.RUNNING) {oldState.status = ProcessStatus.READY;}}

#line run\lang\Kernal\kernal.el 92:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
LOAD r2 -1879048190
SUB r1 r1 r2
GOTO NEQ r1 :if_end_17 // code == 0x9000_0002
// Releasing r1
#line run\lang\Kernal\kernal.el 93:18
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 84
STORE BYTE 5 r1
// Releasing r1
//  oldState.status = ProcessStatus.DEAD;

#lineend
:if_end_17
//  if(code == 0x9000_0002) {oldState.status = ProcessStatus.DEAD;}

#line run\lang\Kernal\kernal.el 98:14
#alias r1 this // Reserving r1
LOAD this &Kernal.processReadyQueueLock
// INLINE START Mutex.acquire
:Mutex.acquire_loop_3
// Resolving placeholder 1 to r2
TEST AND SET r2 this
GOTO NEQ r2 :Mutex.acquire_loop_3
// INLINE END
STACK POP r0
#alias clear this // Releasing r1 // processReadyQueueLock.acquire()
//  processReadyQueueLock.acquire();

#line run\lang\Kernal\kernal.el 99:14
// Reserving r1
// Register r1 already reserved
LOAD r1 &Kernal.processReadyQueue
// Reserving r2
// Releasing r2
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_18 // processReadyQueue[0] == nullptr
// Releasing r1
#line run\lang\Kernal\kernal.el 100:18
#alias r1 this // Reserving r1
LOAD this &Kernal.processReadyQueueLock
// INLINE START Mutex.release
STORE BYTE 0x0 this
// INLINE END
STACK POP r0
#alias clear this // Releasing r1 // processReadyQueueLock.release()
//  processReadyQueueLock.release();

#line run\lang\Kernal\kernal.el 102:18
// Reserving r1
// Releasing r1
// Reserving r1
LOAD r1 &Kernal.processStates
// Reserving r2
INC r1 128
// Releasing r2 // &processStates[1]
#stackVar ProcessState& kProc
STACK PUSH r1
// Releasing r1
//  ProcessState & kProc = & processStates[1];

#line run\lang\Kernal\kernal.el 103:18
STACK PUSH r0
// Reserving r1
// Reserving r2
ADD r2 r15 8
// Register r2 already reserved
COPY r0 r2
// Releasing r2
GOTO PUSH :Kernal.ProcessState.setInterrupt
STACK POP r0
// Releasing r1 // kProc.setInterrupt()
//  kProc.setInterrupt();

#line run\lang\Kernal\kernal.el 104:18
// Reserving r1
ADD r1 r15 8
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 84
STORE BYTE 3 r1
// Releasing r1
//  kProc.status = ProcessStatus.RUNNING;

#line run\lang\Kernal\kernal.el 105:18
INTERRUPT RET // SysD.interruptReturn()
//  SysD.interruptReturn();

#line run\lang\Kernal\kernal.el 106:18
GOTO :func_exit_Kernal._interrupt
//  return;

#lineend
STACK DEC 4
// End of scope
#stackVarClear kProc
:if_end_18
//  if(processReadyQueue[0] == nullptr) {processReadyQueueLock.release(); ProcessState & kProc = & processStates[1]; kProc.setInterrupt(); kProc.status = ProcessStatus.RUNNING; SysD.interruptReturn(); return;}

#line run\lang\Kernal\kernal.el 108:14
// Reserving r1
// Releasing r1
// Reserving r1
LOAD r1 &Kernal.processReadyQueue
// Reserving r2
// Releasing r2
LOAD MEM r1 r1 // processReadyQueue[0]
#stackVar ProcessState* newProc
STACK PUSH r1
// Releasing r1
//  ProcessState* newProc = processReadyQueue[0];

#line run\lang\Kernal\kernal.el 109:14
#line run\lang\Kernal\kernal.el 110:18
LOAD r1 $Kernal.processReadyQueue
#line run\lang\Kernal\kernal.el 111:18
ADD r2 r1 4
#line run\lang\Kernal\kernal.el 112:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 1 -> 0
#line run\lang\Kernal\kernal.el 113:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 2 -> 1
#line run\lang\Kernal\kernal.el 114:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 3 -> 2
#line run\lang\Kernal\kernal.el 115:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 4 -> 3
#line run\lang\Kernal\kernal.el 116:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 5 -> 4
#line run\lang\Kernal\kernal.el 117:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 6 -> 5
#line run\lang\Kernal\kernal.el 118:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 7 -> 6
#line run\lang\Kernal\kernal.el 119:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 8 -> 7
#line run\lang\Kernal\kernal.el 120:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 9 -> 8
#line run\lang\Kernal\kernal.el 121:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 10 -> 9
#line run\lang\Kernal\kernal.el 122:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 11 -> 10
#line run\lang\Kernal\kernal.el 123:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 12 -> 11
#line run\lang\Kernal\kernal.el 124:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 13 -> 12
#line run\lang\Kernal\kernal.el 125:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 14 -> 13
#line run\lang\Kernal\kernal.el 126:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 15 -> 14
#line run\lang\Kernal\kernal.el 127:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 16 -> 15
#line run\lang\Kernal\kernal.el 128:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 17 -> 16
#line run\lang\Kernal\kernal.el 129:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 18 -> 17
#line run\lang\Kernal\kernal.el 130:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 19 -> 18
#line run\lang\Kernal\kernal.el 131:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 20 -> 19
#line run\lang\Kernal\kernal.el 132:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 21 -> 20
#line run\lang\Kernal\kernal.el 133:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 22 -> 21
#line run\lang\Kernal\kernal.el 134:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 23 -> 22
#line run\lang\Kernal\kernal.el 135:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 24 -> 23
#line run\lang\Kernal\kernal.el 136:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 25 -> 24
#line run\lang\Kernal\kernal.el 137:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 26 -> 25
#line run\lang\Kernal\kernal.el 138:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 27 -> 26
#line run\lang\Kernal\kernal.el 139:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 28 -> 27
#line run\lang\Kernal\kernal.el 140:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 29 -> 28
#line run\lang\Kernal\kernal.el 141:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 30 -> 29
#line run\lang\Kernal\kernal.el 142:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 31 -> 30
#line run\lang\Kernal\kernal.el 143:18
STORE WORD r1 0 // 31
//  asm{\nLOAD r1 $Kernal.processReadyQueue\nADD r2 r1 4\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 1 -> 0\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 2 -> 1\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 3 -> 2\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 4 -> 3\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 5 -> 4\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 6 -> 5\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 7 -> 6\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 8 -> 7\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 9 -> 8\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 10 -> 9\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 11 -> 10\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 12 -> 11\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 13 -> 12\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 14 -> 13\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 15 -> 14\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 16 -> 15\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 17 -> 16\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 18 -> 17\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 19 -> 18\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 20 -> 19\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 21 -> 20\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 22 -> 21\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 23 -> 22\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 24 -> 23\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 25 -> 24\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 26 -> 25\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 27 -> 26\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 28 -> 27\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 29 -> 28\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 30 -> 29\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 31 -> 30\nSTORE WORD r1 0 // 31\n}

#line run\lang\Kernal\kernal.el 145:14
#alias r1 this // Reserving r1
LOAD this &Kernal.processReadyQueueLock
// INLINE START Mutex.release
STORE BYTE 0x0 this
// INLINE END
STACK POP r0
#alias clear this // Releasing r1 // processReadyQueueLock.release()
//  processReadyQueueLock.release();

#line run\lang\Kernal\kernal.el 147:14
STACK PUSH r0
// Reserving r1
// Reserving r2
ADD r2 r15 8
// Register r2 already reserved
COPY r0 r2
// Releasing r2
GOTO PUSH :Kernal.ProcessState.setInterrupt
STACK POP r0
// Releasing r1 // newProc.setInterrupt()
//  newProc.setInterrupt();

#line run\lang\Kernal\kernal.el 148:14
// Reserving r1
ADD r1 r15 8
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 84
STORE BYTE 3 r1
// Releasing r1
//  newProc.status = ProcessStatus.RUNNING;

#line run\lang\Kernal\kernal.el 150:14
INTERRUPT RET // SysD.interruptReturn()
//  SysD.interruptReturn();

#line run\lang\Kernal\kernal.el 151:14
GOTO :func_exit_Kernal._interrupt
//  return;

#lineend
STACK DEC 8
// End of scope
#stackVarClear oldState
#stackVarClear newProc
:if_end_14
//  if(code & 0xffff_fff0 == 0x9000_0000) {ProcessState & oldState = & processStates[SysD.rPIDI]; oldState.updateInterrupt(); if(code == 0x9000_0001) {if(oldState.status == ProcessStatus.RUNNING) {oldState.status = ProcessStatus.READY;}} if(code == 0x9000_0002) {oldState.status = ProcessStatus.DEAD;} processReadyQueueLock.acquire(); if(processReadyQueue[0] == nullptr) {processReadyQueueLock.release(); ProcessState & kProc = & processStates[1]; kProc.setInterrupt(); kProc.status = ProcessStatus.RUNNING; SysD.interruptReturn(); return;} ProcessState* newProc = processReadyQueue[0]; asm{\nLOAD r1 $Kernal.processReadyQueue\nADD r2 r1 4\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 1 -> 0\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 2 -> 1\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 3 -> 2\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 4 -> 3\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 5 -> 4\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 6 -> 5\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 7 -> 6\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 8 -> 7\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 9 -> 8\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 10 -> 9\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 11 -> 10\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 12 -> 11\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 13 -> 12\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 14 -> 13\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 15 -> 14\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 16 -> 15\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 17 -> 16\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 18 -> 17\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 19 -> 18\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 20 -> 19\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 21 -> 20\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 22 -> 21\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 23 -> 22\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 24 -> 23\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 25 -> 24\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 26 -> 25\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 27 -> 26\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 28 -> 27\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 29 -> 28\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 30 -> 29\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 31 -> 30\nSTORE WORD r1 0 // 31\n} processReadyQueueLock.release(); newProc.setInterrupt(); newProc.status = ProcessStatus.RUNNING; SysD.interruptReturn(); return;}

#line run\lang\Kernal\kernal.el 154:10
INTERRUPT RET // SysD.interruptReturn()
//  SysD.interruptReturn();

#lineend
:func_exit_Kernal._interrupt
COPY r15 rStack
STACK POP r15
INTERRUPT RET
#endfunction void

#syscall 1 Kernal.kalloc
#function syscall::Kernal.kalloc
STACK PUSH r15
COPY rStack r15
#line run\lang\Kernal\kalloc.el 18:10
#alias r1 this // Reserving r1
LOAD this &Kernal.kallocMutex
// INLINE START Mutex.acquire
:Mutex.acquire_loop_4
// Resolving placeholder 1 to r2
TEST AND SET r2 this
GOTO NEQ r2 :Mutex.acquire_loop_4
// INLINE END
STACK POP r0
#alias clear this // Releasing r1 // kallocMutex.acquire()
//  kallocMutex.acquire();

#line run\lang\Kernal\kalloc.el 19:10
// Reserving r1
// Releasing r1
// Reserving r1
// Reserving r2
// Releasing r2
LOAD MEM r1 rMemTbl // SysD.rMemTbl[0]
#stackVar int32 cPages
STACK PUSH r1
// Releasing r1
//  int32 cPages = SysD.rMemTbl[0];

#line run\lang\Kernal\kalloc.el 20:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 -32
GOTO LT r1 :if_end_19 // cPages >= MAX_BLOCKS
// Releasing r1
#line run\lang\Kernal\kalloc.el 21:14
#alias r1 this // Reserving r1
LOAD this &Kernal.kallocMutex
// INLINE START Mutex.release
STORE BYTE 0x0 this
// INLINE END
STACK POP r0
#alias clear this // Releasing r1 // kallocMutex.release()
//  kallocMutex.release();

#line run\lang\Kernal\kalloc.el 22:14
// Reserving r1
SUB r1 r15 12
STORE 0 r1
// Releasing r1
GOTO :func_exit_Kernal.kalloc
//  return nullptr;

#lineend
:if_end_19
//  if(cPages >= MAX_BLOCKS) {kallocMutex.release(); return nullptr;}

#line run\lang\Kernal\kalloc.el 24:10
// Reserving r1
// Releasing r1
LOAD r1 0 // 0
#stackVar int32 i
STACK PUSH r1
// Releasing r1
//  int32 i = 0;

#line run\lang\Kernal\kalloc.el 25:10
:while_condition_20
// Reserving r1
LOAD r1 Kernal.pageFreeTable
// Register r1 already reserved
// Reserving r2
ADD r2 r15 4
// Register r2 already reserved
LOAD MEM r2 r2 // i
ADD r1 r1 r2
// Releasing r2
LOAD MEM BYTE r1 r1
GOTO EQ r1 :while_end_20
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 -4096
GOTO GEQ r1 :while_end_20 // i < 0x1000 // pageFreeTable[i] && ( i < 0x1000 )
// Releasing r1
#line run\lang\Kernal\kalloc.el 26:14
ADD r2 r15 4
// Register r2 already reserved
LOAD MEM r1 r2
ADD r3 r1 1
STORE r3 r2 // i++
//  i++;

#lineend
GOTO :while_condition_20
:while_end_20
//  while(pageFreeTable[i] && (i < 0x1000)) {i++;}

#line run\lang\Kernal\kalloc.el 28:10
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 -4096
GOTO NEQ r1 :if_end_21 // i == 0x1000
// Releasing r1
#line run\lang\Kernal\kalloc.el 29:14
#alias r1 this // Reserving r1
LOAD this &Kernal.kallocMutex
// INLINE START Mutex.release
STORE BYTE 0x0 this
// INLINE END
STACK POP r0
#alias clear this // Releasing r1 // kallocMutex.release()
//  kallocMutex.release();

#line run\lang\Kernal\kalloc.el 30:14
// Reserving r1
SUB r1 r15 12
STORE 0 r1
// Releasing r1
GOTO :func_exit_Kernal.kalloc
//  return nullptr;

#lineend
:if_end_21
//  if(i == 0x1000) {kallocMutex.release(); return nullptr;}

#line run\lang\Kernal\kalloc.el 32:10
// Reserving r1
LOAD r1 Kernal.pageFreeTable
// Register r1 already reserved
// Reserving r2
ADD r2 r15 4
// Register r2 already reserved
LOAD MEM r2 r2 // i
ADD r1 r1 r2
// Releasing r2
STORE BYTE 1 r1
// Releasing r1
//  pageFreeTable[i] = true;

#line run\lang\Kernal\kalloc.el 33:10
// Reserving r1
// Releasing r1
// Reserving r1
LOAD MEM r1 r15
LSH r1 r1 12 // cPages << 12
LOAD r2 -2147483648
ADD r1 r1 r2 // 0x8000_0000 + ( cPages << 12 )
#stackVar void* addr
STACK PUSH r1
// Releasing r1
//  void* addr = 0x8000_0000 + (cPages << 12);

#line run\lang\Kernal\kalloc.el 34:10
// Reserving r1
LOAD MEM BYTE r1 r15
ADD r1 r1 1
STORE r1 r15
// Releasing r1
// Releasing r1
//  cPages += 1;

#line run\lang\Kernal\kalloc.el 35:10
// Reserving r1
// Register r1 already reserved
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 r15 // cPages
LSH r2 r2 2
ADD r1 rMemTbl r2
// Releasing r2
// Reserving r2
ADD r2 r15 4
// Register r2 already reserved
LOAD MEM r2 r2
LSH r2 r2 12 // i << 12
LOAD r3 -2147483648
ADD r2 r2 r3 // 0x8000_0000 + ( i << 12 )
STORE r2 r1
// Releasing r1
// Releasing r2
//  SysD.rMemTbl[cPages] = 0x8000_0000 + (i << 12);

#line run\lang\Kernal\kalloc.el 36:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15 // cPages
STORE r1 rMemTbl
// Releasing r1
//  SysD.rMemTbl[0] = cPages;

#line run\lang\Kernal\kalloc.el 38:10
#alias r1 this // Reserving r1
LOAD this &Kernal.kallocMutex
// INLINE START Mutex.release
STORE BYTE 0x0 this
// INLINE END
STACK POP r0
#alias clear this // Releasing r1 // kallocMutex.release()
//  kallocMutex.release();

#line run\lang\Kernal\kalloc.el 39:10
// Reserving r1
ADD r1 r15 8
// Register r1 already reserved
LOAD MEM r1 r1 // addr
// Reserving r2
SUB r2 r15 12
STORE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_Kernal.kalloc
//  return addr;

#lineend
:func_exit_Kernal.kalloc
COPY r15 rStack
STACK POP r15
SYSRETURN
#endfunction void*

#function Kernal.printStr_char* str char*
STACK PUSH r15
COPY rStack r15
#stackVar char* str -12
#line run\lang\Kernal\console.el 26:10
#alias r1 str
#line run\lang\Kernal\console.el 28:14
SUB str r15 12
#line run\lang\Kernal\console.el 29:14
LOAD MEM str str // str
#alias r2 c
#line run\lang\Kernal\console.el 31:14
:printStr_l1
#line run\lang\Kernal\console.el 32:18
LOAD MEM BYTE c str INC_RA
#line run\lang\Kernal\console.el 33:18
GOTO EQ c :printStr_l1_exit
 
#line run\lang\Kernal\console.el 35:18
STORE BYTE c Kernal.CONSOLE_OUT
#line run\lang\Kernal\console.el 36:18
GOTO :printStr_l1
#line run\lang\Kernal\console.el 37:14
:printStr_l1_exit
//  asm{\n#alias r1 str\nSUB str r15 12\nLOAD MEM str str // str\n#alias r2 c\n:printStr_l1\nLOAD MEM BYTE c str INC_RA\nGOTO EQ c :printStr_l1_exit\n\nSTORE BYTE c Kernal.CONSOLE_OUT\nGOTO :printStr_l1\n:printStr_l1_exit\n}

#lineend
:func_exit_Kernal.printStr_char*
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.printStr_char*_int32 str char*, len int32
STACK PUSH r15
COPY rStack r15
#stackVar char* str -16
#stackVar int32 len -12
#line run\lang\Kernal\console.el 48:10
#alias r14 len
#line run\lang\Kernal\console.el 50:14
SUB len r15 12
#line run\lang\Kernal\console.el 51:14
LOAD MEM len len
#alias r1 consolePntr
#line run\lang\Kernal\console.el 53:14
LOAD consolePntr Kernal.CONSOLE_OUT
#alias r2 str
#line run\lang\Kernal\console.el 55:14
SUB str r15 16
#line run\lang\Kernal\console.el 56:14
LOAD MEM str str
#line run\lang\Kernal\console.el 57:14
:printStr_len
#line run\lang\Kernal\console.el 58:18
COPY MEM BYTE str consolePntr INC_RS
#line run\lang\Kernal\console.el 59:18
INC len -1
#line run\lang\Kernal\console.el 60:18
GOTO GT len :printStr_len
//  asm{\n#alias r14 len\nSUB len r15 12\nLOAD MEM len len\n#alias r1 consolePntr\nLOAD consolePntr Kernal.CONSOLE_OUT\n#alias r2 str\nSUB str r15 16\nLOAD MEM str str\n:printStr_len\nCOPY MEM BYTE str consolePntr INC_RS\nINC len -1\nGOTO GT len :printStr_len\n}

#lineend
:func_exit_Kernal.printStr_char*_int32
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.intToHex_int32_char* value int32, str char*
STACK PUSH r15
COPY rStack r15
#stackVar char* str -12
#stackVar int32 value -16
#line run\lang\Kernal\console.el 71:10
#line run\lang\Kernal\console.el 72:14
LOAD r14 7
#line run\lang\Kernal\console.el 73:14
SUB r1 r15 16
#line run\lang\Kernal\console.el 74:14
LOAD MEM r1 r1 // value
#line run\lang\Kernal\console.el 75:14
SUB r2 r15 12
#line run\lang\Kernal\console.el 76:14
LOAD MEM r2 r2 // str
#line run\lang\Kernal\console.el 77:14
LOAD r3 0xf
#line run\lang\Kernal\console.el 78:14
:intToHex_l1
#line run\lang\Kernal\console.el 79:18
LRT r1 r1 4
#line run\lang\Kernal\console.el 80:18
AND r4 r1 r3
#line run\lang\Kernal\console.el 81:18
SUB r5 r4 0xa
#line run\lang\Kernal\console.el 82:18
GOTO GEQ r5 :intToHex_gt
#line run\lang\Kernal\console.el 83:22
INC r4 0x30
#line run\lang\Kernal\console.el 84:22
STORE BYTE r4 r2 INC_RA
#line run\lang\Kernal\console.el 85:22
GOTO :intToHex_l1_end
#line run\lang\Kernal\console.el 86:18
:intToHex_gt
#line run\lang\Kernal\console.el 87:22
INC r4 0x57
#line run\lang\Kernal\console.el 88:22
STORE BYTE r4 r2 INC_RA
#line run\lang\Kernal\console.el 89:18
:intToHex_l1_end
#line run\lang\Kernal\console.el 90:18
INC r14 -1
#line run\lang\Kernal\console.el 91:18
GOTO GEQ r14 :intToHex_l1
//  asm{\nLOAD r14 7\nSUB r1 r15 16\nLOAD MEM r1 r1 // value\nSUB r2 r15 12\nLOAD MEM r2 r2 // str\nLOAD r3 0xf\n:intToHex_l1\nLRT r1 r1 4\nAND r4 r1 r3\nSUB r5 r4 0xa\nGOTO GEQ r5 :intToHex_gt\nINC r4 0x30\nSTORE BYTE r4 r2 INC_RA\nGOTO :intToHex_l1_end\n:intToHex_gt\nINC r4 0x57\nSTORE BYTE r4 r2 INC_RA\n:intToHex_l1_end\nINC r14 -1\nGOTO GEQ r14 :intToHex_l1\n}

#lineend
:func_exit_Kernal.intToHex_int32_char*
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

// Kernal.ProcessState

#function Kernal.ProcessState.create_ProcessState&_int32_int32 state ProcessState&, pid int32, parent int32
STACK PUSH r15
COPY rStack r15
#stackVar int32 parent -12
#stackVar int32 pid -16
#stackVar ProcessState& state -20
#line run\lang\Kernal\kernal.el 257:14
// Reserving r1
SUB r1 r15 20
// Register r1 already reserved
LOAD MEM r1 r1
// Reserving r2
SUB r2 r15 16
// Register r2 already reserved
LOAD MEM r2 r2 // pid
STORE r2 r1
// Releasing r1
// Releasing r2
//  state.pid = pid;

#line run\lang\Kernal\kernal.el 258:14
// Reserving r1
SUB r1 r15 20
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 84
STORE BYTE 1 r1
// Releasing r1
//  state.status = ProcessStatus.SETUP;

#line run\lang\Kernal\kernal.el 259:14
// Reserving r1
SUB r1 r15 20
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 92
// Reserving r2
SUB r2 r15 12
// Register r2 already reserved
LOAD MEM r2 r2 // parent
STORE r2 r1
// Releasing r1
// Releasing r2
//  state.parent = parent;

#line run\lang\Kernal\kernal.el 260:14
// Reserving r1
SUB r1 r15 20
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 88
STORE 0 r1
// Releasing r1
//  state.interruptHandler = nullptr;

#line run\lang\Kernal\kernal.el 261:14
// Reserving r1
SUB r1 r15 20
// Register r1 already reserved
LOAD MEM r1 r1 // state
// Reserving r2
SUB r2 r15 24
STORE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_Kernal.ProcessState.create_ProcessState&_int32_int32
//  return state;

#lineend
:func_exit_Kernal.ProcessState.create_ProcessState&_int32_int32
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction ProcessState*

#function Kernal.ProcessState.setInterrupt
STACK PUSH r15
COPY rStack r15
#line run\lang\Kernal\kernal.el 229:14
#line run\lang\Kernal\kernal.el 230:18
COPY r0 r1
#line run\lang\Kernal\kernal.el 231:18
LOAD MEM WORD rPIDI r1 INC_RA
#line run\lang\Kernal\kernal.el 232:18
LOAD MEM WORD rPgmI r1 INC_RA
#line run\lang\Kernal\kernal.el 233:18
LOAD MEM WORD rStackI r1 INC_RA
#line run\lang\Kernal\kernal.el 234:18
LOAD MEM WORD rMemTblI r1 INC_RA
#line run\lang\Kernal\kernal.el 235:18
LOAD MEM WORD rPMI r1 INC_RA
 
#line run\lang\Kernal\kernal.el 237:18
LOAD MEM WORD r0I r1 INC_RA
#line run\lang\Kernal\kernal.el 238:18
LOAD MEM WORD r1I r1 INC_RA
#line run\lang\Kernal\kernal.el 239:18
LOAD MEM WORD r2I r1 INC_RA
#line run\lang\Kernal\kernal.el 240:18
LOAD MEM WORD r3I r1 INC_RA
#line run\lang\Kernal\kernal.el 241:18
LOAD MEM WORD r4I r1 INC_RA
#line run\lang\Kernal\kernal.el 242:18
LOAD MEM WORD r5I r1 INC_RA
#line run\lang\Kernal\kernal.el 243:18
LOAD MEM WORD r6I r1 INC_RA
#line run\lang\Kernal\kernal.el 244:18
LOAD MEM WORD r7I r1 INC_RA
#line run\lang\Kernal\kernal.el 245:18
LOAD MEM WORD r8I r1 INC_RA
#line run\lang\Kernal\kernal.el 246:18
LOAD MEM WORD r9I r1 INC_RA
#line run\lang\Kernal\kernal.el 247:18
LOAD MEM WORD r10I r1 INC_RA
#line run\lang\Kernal\kernal.el 248:18
LOAD MEM WORD r11I r1 INC_RA
#line run\lang\Kernal\kernal.el 249:18
LOAD MEM WORD r12I r1 INC_RA
#line run\lang\Kernal\kernal.el 250:18
LOAD MEM WORD r13I r1 INC_RA
#line run\lang\Kernal\kernal.el 251:18
LOAD MEM WORD r14I r1 INC_RA
#line run\lang\Kernal\kernal.el 252:18
LOAD MEM WORD r15I r1 INC_RA
//  asm{\nCOPY r0 r1\nLOAD MEM WORD rPIDI r1 INC_RA\nLOAD MEM WORD rPgmI r1 INC_RA\nLOAD MEM WORD rStackI r1 INC_RA\nLOAD MEM WORD rMemTblI r1 INC_RA\nLOAD MEM WORD rPMI r1 INC_RA\n\nLOAD MEM WORD r0I r1 INC_RA\nLOAD MEM WORD r1I r1 INC_RA\nLOAD MEM WORD r2I r1 INC_RA\nLOAD MEM WORD r3I r1 INC_RA\nLOAD MEM WORD r4I r1 INC_RA\nLOAD MEM WORD r5I r1 INC_RA\nLOAD MEM WORD r6I r1 INC_RA\nLOAD MEM WORD r7I r1 INC_RA\nLOAD MEM WORD r8I r1 INC_RA\nLOAD MEM WORD r9I r1 INC_RA\nLOAD MEM WORD r10I r1 INC_RA\nLOAD MEM WORD r11I r1 INC_RA\nLOAD MEM WORD r12I r1 INC_RA\nLOAD MEM WORD r13I r1 INC_RA\nLOAD MEM WORD r14I r1 INC_RA\nLOAD MEM WORD r15I r1 INC_RA\n}

#lineend
:func_exit_Kernal.ProcessState.setInterrupt
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.ProcessState.updateInterrupt
STACK PUSH r15
COPY rStack r15
#line run\lang\Kernal\kernal.el 203:14
#line run\lang\Kernal\kernal.el 204:18
ADD r1 r0 4 // Offset to pgmPtr instead of PID
#line run\lang\Kernal\kernal.el 205:18
STORE WORD rPgmI r1 INC_RA
#line run\lang\Kernal\kernal.el 206:18
STORE WORD rStackI r1 INC_RA
#line run\lang\Kernal\kernal.el 207:18
STORE WORD rMemTblI r1 INC_RA
#line run\lang\Kernal\kernal.el 208:18
STORE WORD rPMI r1 INC_RA
 
#line run\lang\Kernal\kernal.el 210:18
STORE WORD r0I r1 INC_RA
#line run\lang\Kernal\kernal.el 211:18
STORE WORD r1I r1 INC_RA
#line run\lang\Kernal\kernal.el 212:18
STORE WORD r2I r1 INC_RA
#line run\lang\Kernal\kernal.el 213:18
STORE WORD r3I r1 INC_RA
#line run\lang\Kernal\kernal.el 214:18
STORE WORD r4I r1 INC_RA
#line run\lang\Kernal\kernal.el 215:18
STORE WORD r5I r1 INC_RA
#line run\lang\Kernal\kernal.el 216:18
STORE WORD r6I r1 INC_RA
#line run\lang\Kernal\kernal.el 217:18
STORE WORD r7I r1 INC_RA
#line run\lang\Kernal\kernal.el 218:18
STORE WORD r8I r1 INC_RA
#line run\lang\Kernal\kernal.el 219:18
STORE WORD r9I r1 INC_RA
#line run\lang\Kernal\kernal.el 220:18
STORE WORD r10I r1 INC_RA
#line run\lang\Kernal\kernal.el 221:18
STORE WORD r11I r1 INC_RA
#line run\lang\Kernal\kernal.el 222:18
STORE WORD r12I r1 INC_RA
#line run\lang\Kernal\kernal.el 223:18
STORE WORD r13I r1 INC_RA
#line run\lang\Kernal\kernal.el 224:18
STORE WORD r14I r1 INC_RA
#line run\lang\Kernal\kernal.el 225:18
STORE WORD r15I r1 INC_RA
//  asm{\nADD r1 r0 4 // Offset to pgmPtr instead of PID\nSTORE WORD rPgmI r1 INC_RA\nSTORE WORD rStackI r1 INC_RA\nSTORE WORD rMemTblI r1 INC_RA\nSTORE WORD rPMI r1 INC_RA\n\nSTORE WORD r0I r1 INC_RA\nSTORE WORD r1I r1 INC_RA\nSTORE WORD r2I r1 INC_RA\nSTORE WORD r3I r1 INC_RA\nSTORE WORD r4I r1 INC_RA\nSTORE WORD r5I r1 INC_RA\nSTORE WORD r6I r1 INC_RA\nSTORE WORD r7I r1 INC_RA\nSTORE WORD r8I r1 INC_RA\nSTORE WORD r9I r1 INC_RA\nSTORE WORD r10I r1 INC_RA\nSTORE WORD r11I r1 INC_RA\nSTORE WORD r12I r1 INC_RA\nSTORE WORD r13I r1 INC_RA\nSTORE WORD r14I r1 INC_RA\nSTORE WORD r15I r1 INC_RA\n}

#lineend
:func_exit_Kernal.ProcessState.updateInterrupt
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

// Kernal.ProcessStatus

// Kernal.FS

#syscall 19 Kernal.FS.read_int32_void*_int32
#function syscall::Kernal.FS.read_int32_void*_int32 handle int32, buffer void*, capacity int32
STACK PUSH r15
COPY rStack r15
#stackVar int32 handle -20
#stackVar void* buffer -16
#stackVar int32 capacity -12
#line run\lang\Kernal\fs.el 136:10
// Reserving r1
// Releasing r1
// Reserving r1
LOAD r1 &Kernal.processStates
// Reserving r2
// Register r2 already reserved
COPY rPID r2 // SysD.rPID
LSH r2 r2 7
ADD r1 r1 r2
// Releasing r2 // &processStates[SysD.rPID]
#stackVar ProcessState& proc
STACK PUSH r1
// Releasing r1
//  ProcessState & proc = & processStates[SysD.rPID];

#line run\lang\Kernal\fs.el 137:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_22 // proc.files == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 138:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
// Reserving r2
STACK INC 4
STACK PUSH r1
// Reserving r3
GOTO PUSH :Kernal.FS.ProcessFiles.new
STACK POP r2
STACK POP r1
// Releasing r3 // ProcessFiles.new()
STORE r2 r1
// Releasing r1
// Releasing r2
//  proc.files = ProcessFiles.new();

#line run\lang\Kernal\fs.el 139:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_23 // proc.files == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 140:18
// Reserving r1
SUB r1 r15 24
STORE BYTE 0 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.read_int32_void*_int32
//  return false;

#lineend
:if_end_23
//  if(proc.files == nullptr) {return false;}

#lineend
:if_end_22
//  if(proc.files == nullptr) {proc.files = ProcessFiles.new(); if(proc.files == nullptr) {return false;}}

#line run\lang\Kernal\fs.el 143:10
// Reserving r1
SUB r1 r15 20
// Register r1 already reserved
LOAD MEM r1 r1
GOTO LT r1 :exp_ee_1
SUB r1 r15 20
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 -16
GOTO LEQ r1 :if_end_24
:exp_ee_1 // handle < 0 || handle > proc.files.handles.length
// Releasing r1
#line run\lang\Kernal\fs.el 144:14
// Reserving r1
SUB r1 r15 24
STORE BYTE 0 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.read_int32_void*_int32
//  return false;

#lineend
:if_end_24
//  if(handle < 0 || handle > proc.files.handles.length) {return false;}

#line run\lang\Kernal\fs.el 146:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
LOAD MEM r1 r1
INC r1 4
// Reserving r2
SUB r2 r15 20
// Register r2 already reserved
LOAD MEM r2 r2 // handle
LSH r2 r2 2
ADD r1 r1 r2
// Releasing r2
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_25 // proc.files.handles[handle] == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 147:14
// Reserving r1
SUB r1 r15 24
STORE BYTE 0 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.read_int32_void*_int32
//  return false;

#lineend
:if_end_25
//  if(proc.files.handles[handle] == nullptr) {return false;}

#line run\lang\Kernal\fs.el 149:10
STACK PUSH r0
// Reserving r1
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 r15
INC r2 96
LOAD MEM r2 r2
INC r2 4
// Reserving r3
SUB r3 r15 20
// Register r3 already reserved
LOAD MEM r3 r3 // handle
LSH r3 r3 2
ADD r2 r2 r3
// Releasing r3
COPY r0 r2
// Releasing r2
GOTO PUSH :Kernal.FS.FileHandle.flush
STACK POP r0
// Releasing r1 // proc.files.handles[handle].flush()
//  proc.files.handles[handle].flush();

#line run\lang\Kernal\fs.el 150:10
// Reserving r1
SUB r1 r15 24
STORE BYTE 1 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.read_int32_void*_int32
//  return true;

#lineend
:func_exit_Kernal.FS.read_int32_void*_int32
COPY r15 rStack
STACK POP r15
SYSRETURN
#endfunction bool

#syscall 19 Kernal.FS.flush_int32
#function syscall::Kernal.FS.flush_int32 handle int32
STACK PUSH r15
COPY rStack r15
#stackVar int32 handle -12
#line run\lang\Kernal\fs.el 110:10
// Reserving r1
// Releasing r1
// Reserving r1
LOAD r1 &Kernal.processStates
// Reserving r2
// Register r2 already reserved
COPY rPID r2 // SysD.rPID
LSH r2 r2 7
ADD r1 r1 r2
// Releasing r2 // &processStates[SysD.rPID]
#stackVar ProcessState& proc
STACK PUSH r1
// Releasing r1
//  ProcessState & proc = & processStates[SysD.rPID];

#line run\lang\Kernal\fs.el 111:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_26 // proc.files == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 112:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
// Reserving r2
STACK INC 4
STACK PUSH r1
// Reserving r3
GOTO PUSH :Kernal.FS.ProcessFiles.new
STACK POP r2
STACK POP r1
// Releasing r3 // ProcessFiles.new()
STORE r2 r1
// Releasing r1
// Releasing r2
//  proc.files = ProcessFiles.new();

#line run\lang\Kernal\fs.el 113:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_27 // proc.files == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 114:18
// Reserving r1
SUB r1 r15 16
STORE BYTE 0 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.flush_int32
//  return false;

#lineend
:if_end_27
//  if(proc.files == nullptr) {return false;}

#lineend
:if_end_26
//  if(proc.files == nullptr) {proc.files = ProcessFiles.new(); if(proc.files == nullptr) {return false;}}

#line run\lang\Kernal\fs.el 117:10
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
GOTO LT r1 :exp_ee_2
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 -16
GOTO LEQ r1 :if_end_28
:exp_ee_2 // handle < 0 || handle > proc.files.handles.length
// Releasing r1
#line run\lang\Kernal\fs.el 118:14
// Reserving r1
SUB r1 r15 16
STORE BYTE 0 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.flush_int32
//  return false;

#lineend
:if_end_28
//  if(handle < 0 || handle > proc.files.handles.length) {return false;}

#line run\lang\Kernal\fs.el 120:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
LOAD MEM r1 r1
INC r1 4
// Reserving r2
SUB r2 r15 12
// Register r2 already reserved
LOAD MEM r2 r2 // handle
LSH r2 r2 2
ADD r1 r1 r2
// Releasing r2
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_29 // proc.files.handles[handle] == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 121:14
// Reserving r1
SUB r1 r15 16
STORE BYTE 0 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.flush_int32
//  return false;

#lineend
:if_end_29
//  if(proc.files.handles[handle] == nullptr) {return false;}

#line run\lang\Kernal\fs.el 123:10
STACK PUSH r0
// Reserving r1
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 r15
INC r2 96
LOAD MEM r2 r2
INC r2 4
// Reserving r3
SUB r3 r15 12
// Register r3 already reserved
LOAD MEM r3 r3 // handle
LSH r3 r3 2
ADD r2 r2 r3
// Releasing r3
COPY r0 r2
// Releasing r2
GOTO PUSH :Kernal.FS.FileHandle.flush
STACK POP r0
// Releasing r1 // proc.files.handles[handle].flush()
//  proc.files.handles[handle].flush();

#line run\lang\Kernal\fs.el 124:10
// Reserving r1
SUB r1 r15 16
STORE BYTE 1 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.flush_int32
//  return true;

#lineend
:func_exit_Kernal.FS.flush_int32
COPY r15 rStack
STACK POP r15
SYSRETURN
#endfunction bool

#function Kernal.FS.setupFS
STACK PUSH r15
COPY rStack r15
#line run\lang\Kernal\fs.el 270:10
// For Loop:
// Initializer
// Reserving r1
// Reserving r2
// Releasing r2
LOAD r2 1 // 1
#stackVar int32 i
STACK PUSH r2
// Releasing r2
:for_condition_30
// Reserving r2
// Register r3 already reserved
COPY r1 r3
INC r3 -64
GOTO GEQ r3 :for_end_30 // i < 64
// Releasing r2
#line run\lang\Kernal\fs.el 271:14
// Still reserved: r1
// Reserving r2
LOAD r2 Peripheral.TABLE
// Register r2 already reserved
// Reserving r3
// Register r3 already reserved
COPY r1 r3 // i
LSH r3 r3 2
ADD r2 r2 r3
// Releasing r3
LOAD MEM r2 r2
LOAD r3 16777217
SUB r2 r2 r3
GOTO NEQ r2 :if_end_31 // Peripheral.TABLE[i] == Peripheral.TYPE_STORAGE_VIRTUAL
// Releasing r2
#line run\lang\Kernal\fs.el 272:18
// Reserving r2
// Register r2 already reserved
LOAD r2 &Kernal.FS.fsDeviceId
// Reserving r3
// Register r3 already reserved
COPY r1 r3 // i
STORE r3 r2
// Releasing r2
// Releasing r3
//  fsDeviceId = i;

#line run\lang\Kernal\fs.el 273:18
GOTO :for_end_30
//  break;

#lineend
:if_end_31
//  if(Peripheral.TABLE[i] == Peripheral.TYPE_STORAGE_VIRTUAL) {fsDeviceId = i; break;}

#lineend
// Reserved: r1
// Iterator
INC r1 1 // i++
GOTO :for_condition_30
:for_end_30
// End of scope
// Releasing r1
//  for(int32 i = 1; i < 64; i++) {if(Peripheral.TABLE[i] == Peripheral.TYPE_STORAGE_VIRTUAL) {fsDeviceId = i; break;}}

#line run\lang\Kernal\fs.el 276:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &Kernal.FS.fsDeviceId // fsDeviceId != 0
// Reserving r2
SUB r2 r15 12
STORE BYTE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_Kernal.FS.setupFS
//  return fsDeviceId != 0;

#lineend
:func_exit_Kernal.FS.setupFS
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction bool

#syscall 17 Kernal.FS.write_int32_void*_int32
#function syscall::Kernal.FS.write_int32_void*_int32 handle int32, buffer void*, len int32
STACK PUSH r15
COPY rStack r15
#stackVar int32 len -12
#stackVar int32 handle -20
#stackVar void* buffer -16
#line run\lang\Kernal\fs.el 56:10
// Reserving r1
// Releasing r1
// Reserving r1
LOAD r1 &Kernal.processStates
// Reserving r2
// Register r2 already reserved
COPY rPID r2 // SysD.rPID
LSH r2 r2 7
ADD r1 r1 r2
// Releasing r2 // &processStates[SysD.rPID]
#stackVar ProcessState& proc
STACK PUSH r1
// Releasing r1
//  ProcessState & proc = & processStates[SysD.rPID];

#line run\lang\Kernal\fs.el 57:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_32 // proc.files == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 58:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
// Reserving r2
STACK INC 4
STACK PUSH r1
// Reserving r3
GOTO PUSH :Kernal.FS.ProcessFiles.new
STACK POP r2
STACK POP r1
// Releasing r3 // ProcessFiles.new()
STORE r2 r1
// Releasing r1
// Releasing r2
//  proc.files = ProcessFiles.new();

#line run\lang\Kernal\fs.el 59:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_33 // proc.files == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 60:18
// Reserving r1
SUB r1 r15 24
STORE BYTE 0 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.write_int32_void*_int32
//  return false;

#lineend
:if_end_33
//  if(proc.files == nullptr) {return false;}

#lineend
:if_end_32
//  if(proc.files == nullptr) {proc.files = ProcessFiles.new(); if(proc.files == nullptr) {return false;}}

#line run\lang\Kernal\fs.el 63:10
// Reserving r1
SUB r1 r15 20
// Register r1 already reserved
LOAD MEM r1 r1
GOTO LT r1 :exp_ee_3
SUB r1 r15 20
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 -16
GOTO LEQ r1 :if_end_34
:exp_ee_3 // handle < 0 || handle > proc.files.handles.length
// Releasing r1
#line run\lang\Kernal\fs.el 64:14
// Reserving r1
SUB r1 r15 24
STORE BYTE 0 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.write_int32_void*_int32
//  return false;

#lineend
:if_end_34
//  if(handle < 0 || handle > proc.files.handles.length) {return false;}

#line run\lang\Kernal\fs.el 66:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
LOAD MEM r1 r1
INC r1 4
// Reserving r2
SUB r2 r15 20
// Register r2 already reserved
LOAD MEM r2 r2 // handle
LSH r2 r2 2
ADD r1 r1 r2
// Releasing r2
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_35 // proc.files.handles[handle] == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 67:14
// Reserving r1
SUB r1 r15 24
STORE BYTE 0 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.write_int32_void*_int32
//  return false;

#lineend
:if_end_35
//  if(proc.files.handles[handle] == nullptr) {return false;}

#line run\lang\Kernal\fs.el 69:10
// Reserving r1
STACK PUSH r0
STACK INC 4
// Reserving r2
SUB r2 r15 16
// Register r2 already reserved
LOAD MEM r2 r2 // buffer
STACK PUSH r2
SUB r2 r15 12
// Register r2 already reserved
LOAD MEM r2 r2 // len
STACK PUSH r2
// Releasing r2
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 r15
INC r2 96
LOAD MEM r2 r2
INC r2 4
// Reserving r3
SUB r3 r15 20
// Register r3 already reserved
LOAD MEM r3 r3 // handle
LSH r3 r3 2
ADD r2 r2 r3
// Releasing r3
COPY r0 r2
// Releasing r2
GOTO PUSH :Kernal.FS.FileHandle.write_void*_int32
STACK DEC 8
STACK POP BYTE r1
STACK POP r0
// Releasing r2 // proc.files.handles[handle].write(buffer, len)
// Reserving r2
SUB r2 r15 24
STORE BYTE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_Kernal.FS.write_int32_void*_int32
//  return proc.files.handles[handle].write(buffer, len);

#lineend
:func_exit_Kernal.FS.write_int32_void*_int32
COPY r15 rStack
STACK POP r15
SYSRETURN
#endfunction bool

#syscall 18 Kernal.FS.writeD_int32_void*_int32
#function syscall::Kernal.FS.writeD_int32_void*_int32 handle int32, buffer void*, len int32
STACK PUSH r15
COPY rStack r15
#stackVar int32 len -12
#stackVar int32 handle -20
#stackVar void* buffer -16
#line run\lang\Kernal\fs.el 85:10
// Reserving r1
// Releasing r1
// Reserving r1
LOAD r1 &Kernal.processStates
// Reserving r2
// Register r2 already reserved
COPY rPID r2 // SysD.rPID
LSH r2 r2 7
ADD r1 r1 r2
// Releasing r2 // &processStates[SysD.rPID]
#stackVar ProcessState& proc
STACK PUSH r1
// Releasing r1
//  ProcessState & proc = & processStates[SysD.rPID];

#line run\lang\Kernal\fs.el 86:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_36 // proc.files == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 87:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
// Reserving r2
STACK INC 4
STACK PUSH r1
// Reserving r3
GOTO PUSH :Kernal.FS.ProcessFiles.new
STACK POP r2
STACK POP r1
// Releasing r3 // ProcessFiles.new()
STORE r2 r1
// Releasing r1
// Releasing r2
//  proc.files = ProcessFiles.new();

#line run\lang\Kernal\fs.el 88:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_37 // proc.files == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 89:18
// Reserving r1
SUB r1 r15 24
STORE BYTE 0 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.writeD_int32_void*_int32
//  return false;

#lineend
:if_end_37
//  if(proc.files == nullptr) {return false;}

#lineend
:if_end_36
//  if(proc.files == nullptr) {proc.files = ProcessFiles.new(); if(proc.files == nullptr) {return false;}}

#line run\lang\Kernal\fs.el 92:10
// Reserving r1
SUB r1 r15 20
// Register r1 already reserved
LOAD MEM r1 r1
GOTO LT r1 :exp_ee_4
SUB r1 r15 20
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 -16
GOTO LEQ r1 :if_end_38
:exp_ee_4 // handle < 0 || handle > proc.files.handles.length
// Releasing r1
#line run\lang\Kernal\fs.el 93:14
// Reserving r1
SUB r1 r15 24
STORE BYTE 0 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.writeD_int32_void*_int32
//  return false;

#lineend
:if_end_38
//  if(handle < 0 || handle > proc.files.handles.length) {return false;}

#line run\lang\Kernal\fs.el 95:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
LOAD MEM r1 r1
INC r1 4
// Reserving r2
SUB r2 r15 20
// Register r2 already reserved
LOAD MEM r2 r2 // handle
LSH r2 r2 2
ADD r1 r1 r2
// Releasing r2
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_39 // proc.files.handles[handle] == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 96:14
// Reserving r1
SUB r1 r15 24
STORE BYTE 0 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.writeD_int32_void*_int32
//  return false;

#lineend
:if_end_39
//  if(proc.files.handles[handle] == nullptr) {return false;}

#line run\lang\Kernal\fs.el 98:10
// Reserving r1
STACK PUSH r0
STACK INC 4
// Reserving r2
SUB r2 r15 16
// Register r2 already reserved
LOAD MEM r2 r2 // buffer
STACK PUSH r2
SUB r2 r15 12
// Register r2 already reserved
LOAD MEM r2 r2 // len
STACK PUSH r2
// Releasing r2
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 r15
INC r2 96
LOAD MEM r2 r2
INC r2 4
// Reserving r3
SUB r3 r15 20
// Register r3 already reserved
LOAD MEM r3 r3 // handle
LSH r3 r3 2
ADD r2 r2 r3
// Releasing r3
COPY r0 r2
// Releasing r2
GOTO PUSH :Kernal.FS.FileHandle.write_void*_int32
STACK DEC 8
STACK POP BYTE r1
STACK POP r0
// Releasing r2 // proc.files.handles[handle].write(buffer, len)
// Reserving r2
SUB r2 r15 24
STORE BYTE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_Kernal.FS.writeD_int32_void*_int32
//  return proc.files.handles[handle].write(buffer, len);

#lineend
:func_exit_Kernal.FS.writeD_int32_void*_int32
COPY r15 rStack
STACK POP r15
SYSRETURN
#endfunction bool

#syscall 31 Kernal.FS.close_int32
#function syscall::Kernal.FS.close_int32 handle int32
STACK PUSH r15
COPY rStack r15
#stackVar int32 handle -12
#line run\lang\Kernal\fs.el 161:10
// Reserving r1
// Releasing r1
// Reserving r1
LOAD r1 &Kernal.processStates
// Reserving r2
// Register r2 already reserved
COPY rPID r2 // SysD.rPID
LSH r2 r2 7
ADD r1 r1 r2
// Releasing r2 // &processStates[SysD.rPID]
#stackVar ProcessState& proc
STACK PUSH r1
// Releasing r1
//  ProcessState & proc = & processStates[SysD.rPID];

#line run\lang\Kernal\fs.el 162:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_40 // proc.files == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 163:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
// Reserving r2
STACK INC 4
STACK PUSH r1
// Reserving r3
GOTO PUSH :Kernal.FS.ProcessFiles.new
STACK POP r2
STACK POP r1
// Releasing r3 // ProcessFiles.new()
STORE r2 r1
// Releasing r1
// Releasing r2
//  proc.files = ProcessFiles.new();

#line run\lang\Kernal\fs.el 164:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_41 // proc.files == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 165:18
GOTO :func_exit_Kernal.FS.close_int32
//  return;

#lineend
:if_end_41
//  if(proc.files == nullptr) {return;}

#lineend
:if_end_40
//  if(proc.files == nullptr) {proc.files = ProcessFiles.new(); if(proc.files == nullptr) {return;}}

#line run\lang\Kernal\fs.el 168:10
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
GOTO LT r1 :exp_ee_5
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 -16
GOTO LEQ r1 :if_end_42
:exp_ee_5 // handle < 0 || handle > proc.files.handles.length
// Releasing r1
#line run\lang\Kernal\fs.el 169:14
GOTO :func_exit_Kernal.FS.close_int32
//  return;

#lineend
:if_end_42
//  if(handle < 0 || handle > proc.files.handles.length) {return;}

#line run\lang\Kernal\fs.el 171:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
LOAD MEM r1 r1
INC r1 4
// Reserving r2
SUB r2 r15 12
// Register r2 already reserved
LOAD MEM r2 r2 // handle
LSH r2 r2 2
ADD r1 r1 r2
// Releasing r2
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_43 // proc.files.handles[handle] == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 172:14
GOTO :func_exit_Kernal.FS.close_int32
//  return;

#lineend
:if_end_43
//  if(proc.files.handles[handle] == nullptr) {return;}

#line run\lang\Kernal\fs.el 174:10
STACK PUSH r0
// Reserving r1
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 r15
INC r2 96
LOAD MEM r2 r2
INC r2 4
// Reserving r3
SUB r3 r15 12
// Register r3 already reserved
LOAD MEM r3 r3 // handle
LSH r3 r3 2
ADD r2 r2 r3
// Releasing r3
COPY r0 r2
// Releasing r2
GOTO PUSH :Kernal.FS.FileHandle.close
STACK POP r0
// Releasing r1 // proc.files.handles[handle].close()
//  proc.files.handles[handle].close();

#line run\lang\Kernal\fs.el 175:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
LOAD MEM r1 r1
INC r1 4
// Reserving r2
SUB r2 r15 12
// Register r2 already reserved
LOAD MEM r2 r2 // handle
LSH r2 r2 2
ADD r1 r1 r2
// Releasing r2
STORE 0 r1
// Releasing r1
//  proc.files.handles[handle] = nullptr;

#line run\lang\Kernal\fs.el 176:10
GOTO :func_exit_Kernal.FS.close_int32
//  return;

#lineend
:func_exit_Kernal.FS.close_int32
COPY r15 rStack
STACK POP r15
SYSRETURN
#endfunction void

#syscall 16 Kernal.FS.open_char*_OpenMode
#function syscall::Kernal.FS.open_char*_OpenMode path char*, mode OpenMode
STACK PUSH r15
COPY rStack r15
#stackVar OpenMode mode -9
#stackVar char* path -16
#line run\lang\Kernal\fs.el 30:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &Kernal.FS.fsDeviceId
GOTO NEQ r1 :if_end_44 // fsDeviceId == 0
// Releasing r1
#line run\lang\Kernal\fs.el 31:14
// Reserving r1
STACK INC 4
// Reserving r2
GOTO PUSH :Kernal.FS.setupFS
STACK POP BYTE r1
// Releasing r2
GOTO NEQ r1 :if_end_45 // !setupFS()
// Releasing r1
#line run\lang\Kernal\fs.el 32:18
// Reserving r1
SUB r1 r15 20
STORE -1 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.open_char*_OpenMode
//  return - 1;

#lineend
:if_end_45
//  if(! setupFS()) {return - 1;}

#lineend
:if_end_44
//  if(fsDeviceId == 0) {if(! setupFS()) {return - 1;}}

#line run\lang\Kernal\fs.el 35:10
// Reserving r1
// Releasing r1
// Reserving r1
LOAD r1 &Kernal.processStates
// Reserving r2
// Register r2 already reserved
COPY rPID r2 // SysD.rPID
LSH r2 r2 7
ADD r1 r1 r2
// Releasing r2 // &processStates[SysD.rPID]
#stackVar ProcessState& proc
STACK PUSH r1
// Releasing r1
//  ProcessState & proc = & processStates[SysD.rPID];

#line run\lang\Kernal\fs.el 36:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_46 // proc.files == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 37:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
// Reserving r2
STACK INC 4
STACK PUSH r1
// Reserving r3
GOTO PUSH :Kernal.FS.ProcessFiles.new
STACK POP r2
STACK POP r1
// Releasing r3 // ProcessFiles.new()
STORE r2 r1
// Releasing r1
// Releasing r2
//  proc.files = ProcessFiles.new();

#line run\lang\Kernal\fs.el 38:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 96
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_47 // proc.files == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 39:18
// Reserving r1
SUB r1 r15 20
STORE -1 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.open_char*_OpenMode
//  return - 1;

#lineend
:if_end_47
//  if(proc.files == nullptr) {return - 1;}

#lineend
:if_end_46
//  if(proc.files == nullptr) {proc.files = ProcessFiles.new(); if(proc.files == nullptr) {return - 1;}}

#line run\lang\Kernal\fs.el 42:10
// Reserving r1
STACK PUSH r0
STACK INC 4
// Reserving r2
SUB r2 r15 16
// Register r2 already reserved
LOAD MEM r2 r2 // path
STACK PUSH r2
SUB r2 r15 9
// Register r2 already reserved
LOAD MEM BYTE r2 r2 // mode
STACK PUSH BYTE r2
// Releasing r2
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 r15
INC r2 96
COPY r0 r2
// Releasing r2
GOTO PUSH :Kernal.FS.ProcessFiles.open_char*_OpenMode
STACK DEC 8
STACK POP r1
STACK POP r0
// Releasing r2 // proc.files.open(path, mode)
// Reserving r2
SUB r2 r15 20
STORE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_Kernal.FS.open_char*_OpenMode
//  return proc.files.open(path, mode);

#lineend
:func_exit_Kernal.FS.open_char*_OpenMode
COPY r15 rStack
STACK POP r15
SYSRETURN
#endfunction int32

// Kernal.FS.FileOpenCommand

// Kernal.FS.FileHandle

#function Kernal.FS.FileHandle.new
STACK PUSH r15
COPY rStack r15
#line run\lang\Kernal\fs.el 413:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &Kernal.FS.FileHandle.nextFree
INC r1 1
GOTO NEQ r1 :if_end_48 // nextFree == 0xffff_ffff
// Releasing r1
#line run\lang\Kernal\fs.el 414:18
// Reserving r1
// Register r1 already reserved
LOAD r1 &Kernal.FS.FileHandle.nextFree
// Reserving r2
// Register r2 already reserved
LOAD r2 &Kernal.FS.FileHandle.pool // &pool
STORE r2 r1
// Releasing r1
// Releasing r2
//  nextFree = & pool;

#line run\lang\Kernal\fs.el 415:18
// For Loop:
// Initializer
// Reserving r1
// Reserving r2
// Releasing r2
LOAD r2 0 // 0
#stackVar int32 i
STACK PUSH r2
// Releasing r2
:for_condition_49
// Reserving r2
// Register r3 already reserved
COPY r1 r3
INC r3 -127
GOTO GEQ r3 :for_end_49 // i < pool.length - 1
// Releasing r2
#line run\lang\Kernal\fs.el 416:22
// Still reserved: r1
// Reserving r2
// Register r2 already reserved
LOAD r2 &Kernal.FS.FileHandle.pool
// Reserving r3
// Register r3 already reserved
COPY r1 r3 // i
// Found Free register r4
LOAD r4 24
MUL r3 r3 r4
ADD r2 r2 r3
// Releasing r3
// Reserving r3
// Register r3 already reserved
LOAD r3 &Kernal.FS.FileHandle.pool
// Reserving r4
// Register r4 already reserved
COPY r1 r4
INC r4 1 // i + 1
// Found Free register r5
LOAD r5 24
MUL r4 r4 r5
ADD r3 r3 r4
// Releasing r4 // &pool[i + 1] // cast<int32>(& pool[i + 1])
STORE r3 r2
// Releasing r2
// Releasing r3
//  pool[i].rawHandle = cast<int32>(& pool[i + 1]);

#lineend
// Reserved: r1
// Iterator
INC r1 1 // i++
GOTO :for_condition_49
:for_end_49
// End of scope
// Releasing r1
//  for(int32 i = 0; i < pool.length - 1; i++) {pool[i].rawHandle = cast<int32>(& pool[i + 1]);}

#line run\lang\Kernal\fs.el 418:18
// Reserving r1
// Register r1 already reserved
LOAD r1 &Kernal.FS.FileHandle.pool
// Reserving r2
INC r1 3048
// Releasing r2
STORE 0 r1
// Releasing r1
//  pool[pool.length - 1].rawHandle = 0;

#lineend
:if_end_48
//  if(nextFree == 0xffff_ffff) {nextFree = & pool; for(int32 i = 0; i < pool.length - 1; i++) {pool[i].rawHandle = cast<int32>(& pool[i + 1]);} pool[pool.length - 1].rawHandle = 0;}

#line run\lang\Kernal\fs.el 420:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &Kernal.FS.FileHandle.nextFree
GOTO NEQ r1 :if_end_50 // nextFree == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 421:18
// Reserving r1
SUB r1 r15 12
STORE 0 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.FileHandle.new
//  return nullptr;

#lineend
:if_end_50
//  if(nextFree == nullptr) {return nullptr;}

#line run\lang\Kernal\fs.el 423:14
// Reserving r1
// Releasing r1
// Reserving r1
LOAD MEM r1 &Kernal.FS.FileHandle.nextFree // nextFree
#stackVar FileHandle* next
STACK PUSH r1
// Releasing r1
//  FileHandle* next = nextFree;

#line run\lang\Kernal\fs.el 424:14
// Reserving r1
// Register r1 already reserved
LOAD r1 &Kernal.FS.FileHandle.nextFree
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 r15
LOAD MEM r2 r2 // next.rawHandle // force_cast<FileHandle*>(next.rawHandle)
STORE r2 r1
// Releasing r1
// Releasing r2
//  nextFree = force_cast<FileHandle*>(next.rawHandle);

#line run\lang\Kernal\fs.el 425:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15 // next
// Reserving r2
SUB r2 r15 12
STORE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_Kernal.FS.FileHandle.new
//  return next;

#lineend
:func_exit_Kernal.FS.FileHandle.new
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction FileHandle*

#function Kernal.FS.FileHandle.writeDirect_void*_int32 buffer void*, len int32
STACK PUSH r15
COPY rStack r15
#stackVar int32 len -12
#stackVar void* buffer -16
#line run\lang\Kernal\fs.el 359:14
// Reserving r1
ADD r1 r0 4
// Register r1 already reserved
LOAD MEM BYTE r1 r1
INC r1 -24
GOTO NEQ r1 :if_end_51 // mode == OpenMode.STREAM_WRITE
// Releasing r1
#line run\lang\Kernal\fs.el 360:18
// Reserving r1
ADD r1 r0 8
// Register r1 already reserved
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_52 // path == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 361:22
// Reserving r1
SUB r1 r15 16
// Register r1 already reserved
LOAD MEM r1 r1 // buffer
STACK PUSH r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1 // len
STACK PUSH r1
// Releasing r1
GOTO PUSH :Kernal.printStr_char*_int32
STACK DEC 8
// Releasing r1 // Kernal.printStr(buffer, len)
//  Kernal.printStr(buffer, len);

#lineend
:if_end_52
//  if(path == nullptr) {Kernal.printStr(buffer, len);}

#line run\lang\Kernal\fs.el 363:18
GOTO :func_exit_Kernal.FS.FileHandle.writeDirect_void*_int32
//  return;

#lineend
:if_end_51
//  if(mode == OpenMode.STREAM_WRITE) {if(path == nullptr) {Kernal.printStr(buffer, len);} return;}

#line run\lang\Kernal\fs.el 365:14
// Reserving r1
ADD r1 r0 4
// Register r1 already reserved
LOAD MEM BYTE r1 r1
LOAD r2 Kernal.FS.OpenMode.WRITE
// Register r2 already reserved
AND r1 r1 r2
GOTO EQ r1 :exp_ee_6 // mode & OpenMode.WRITE == 0
SUB r1 r15 16
// Register r1 already reserved
LOAD MEM r1 r1
GOTO EQ r1 :exp_ee_6
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_53
:exp_ee_6 // ( mode & OpenMode.WRITE == 0 ) || buffer == nullptr || len == 0
// Releasing r1
#line run\lang\Kernal\fs.el 366:18
GOTO :func_exit_Kernal.FS.FileHandle.writeDirect_void*_int32
//  return;

#lineend
:if_end_53
//  if((mode & OpenMode.WRITE == 0) || buffer == nullptr || len == 0) {return;}

#line run\lang\Kernal\fs.el 368:14
// Reserving r1
// Reserving r2
ADD r2 r0 20
// Register r2 already reserved
// Register r1 already reserved
LOAD MEM r1 r2
// Reserving r3
SUB r3 r15 12
// Register r3 already reserved
LOAD MEM r3 r3 // len
ADD r3 r1 r3
STORE r3 r2
// Releasing r2
// Releasing r1
// Releasing r3
//  offset += len;

#lineend
:func_exit_Kernal.FS.FileHandle.writeDirect_void*_int32
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.FS.FileHandle.read_void*_int32_int32& buffer void*, len int32, count int32&
STACK PUSH r15
COPY rStack r15
#stackVar int32 len -16
#stackVar int32& count -12
#stackVar void* buffer -20
#line run\lang\Kernal\fs.el 381:14
// Reserving r1
ADD r1 r0 4
// Register r1 already reserved
LOAD MEM BYTE r1 r1
INC r1 -8
GOTO NEQ r1 :if_end_54 // mode == OpenMode.STREAM_READ
// Releasing r1
#line run\lang\Kernal\fs.el 382:18
// Reserving r1
ADD r1 r0 8
// Register r1 already reserved
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_55 // path == nullptr
// Releasing r1
// 

#lineend
:if_end_55
//  if(path == nullptr) {}

#line run\lang\Kernal\fs.el 385:18
GOTO :func_exit_Kernal.FS.FileHandle.read_void*_int32_int32&
//  return;

#lineend
:if_end_54
//  if(mode == OpenMode.STREAM_READ) {if(path == nullptr) {} return;}

#line run\lang\Kernal\fs.el 387:14
// Reserving r1
ADD r1 r0 4
// Register r1 already reserved
LOAD MEM BYTE r1 r1 // mode != OpenMode.READ
GOTO EQ r1 :if_end_56
// Releasing r1
#line run\lang\Kernal\fs.el 388:18
GOTO :func_exit_Kernal.FS.FileHandle.read_void*_int32_int32&
//  return;

#lineend
:if_end_56
//  if(mode != OpenMode.READ) {return;}

#line run\lang\Kernal\fs.el 390:14
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
STORE 0 r1
// Releasing r1
//  count = 0;

#line run\lang\Kernal\fs.el 391:14
// Reserving r1
#stackVar FileReadCommand cmd
STORE 0x0011 rStack INC_RA
// Register r1 already reserved
LOAD MEM r1 r0 // rawHandle
STORE r1 rStack INC_RA
SUB r1 r15 20
// Register r1 already reserved
LOAD MEM r1 r1 // buffer
STORE r1 rStack INC_RA
SUB r1 r15 16
// Register r1 already reserved
LOAD MEM r1 r1 // len
STORE r1 rStack INC_RA
ADD r1 r0 20
// Register r1 already reserved
LOAD MEM r1 r1 // offset
STORE r1 rStack INC_RA
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1 // count
STORE r1 rStack INC_RA
// Releasing r1
//  FileReadCommand cmd = {, rawHandle, buffer, len, offset, count};

#line run\lang\Kernal\fs.el 392:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &Kernal.FS.fsDeviceId // fsDeviceId
STACK PUSH r1
LOAD null 24
LOAD r2 4
DIV r1 r1 r2 // sizeof(cmd) / 4
STACK PUSH r1
// Register r1 already reserved
COPY r15 r1 // &cmd
STACK PUSH r1
// Releasing r1
GOTO PUSH :Peripheral.command_int32_int32_void*
STACK DEC 12
// Releasing r1 // Peripheral.command(fsDeviceId, sizeof(cmd) / 4, & cmd)
//  Peripheral.command(fsDeviceId, sizeof(cmd) / 4, & cmd);

#line run\lang\Kernal\fs.el 393:14
// Reserving r1
// Reserving r2
ADD r2 r0 20
// Register r2 already reserved
// Register r1 already reserved
LOAD MEM r1 r2
// Reserving r3
SUB r3 r15 16
// Register r3 already reserved
LOAD MEM r3 r3 // len
ADD r3 r1 r3
STORE r3 r2
// Releasing r2
// Releasing r1
// Releasing r3
//  offset += len;

#lineend
:func_exit_Kernal.FS.FileHandle.read_void*_int32_int32&
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.FS.FileHandle.flush
STACK PUSH r15
COPY rStack r15
#line run\lang\Kernal\fs.el 376:14
STACK PUSH r0
// Reserving r1
ADD r1 r0 12
// Register r1 already reserved
LOAD MEM r1 r1 // intBuffer
STACK PUSH r1
ADD r1 r0 18
// Register r1 already reserved
LOAD MEM SHORT r1 r1 // bufferSize
STACK PUSH SHORT r1
// Releasing r1
GOTO PUSH :Kernal.FS.FileHandle.writeDirect_void*_int32
STACK DEC 8
STACK POP r0
// Releasing r1 // writeDirect(intBuffer, bufferSize)
//  writeDirect(intBuffer, bufferSize);

#line run\lang\Kernal\fs.el 377:14
// Reserving r1
ADD r1 r0 18
// Register r1 already reserved
STORE SHORT 0 r1
// Releasing r1
//  bufferSize = 0;

#lineend
:func_exit_Kernal.FS.FileHandle.flush
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.FS.FileHandle.release
STACK PUSH r15
COPY rStack r15
#line run\lang\Kernal\fs.el 434:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &Kernal.FS.FileHandle.nextFree // nextFree // cast<int32>(nextFree)
STORE r1 r0
// Releasing r1
//  rawHandle = cast<int32>(nextFree);

#line run\lang\Kernal\fs.el 435:14
// Reserving r1
// Register r1 already reserved
LOAD r1 &Kernal.FS.FileHandle.nextFree
// Reserving r2
// Register r2 already reserved
COPY r0 r2 // this
STORE r2 r1
// Releasing r1
// Releasing r2
//  nextFree = this;

#lineend
:func_exit_Kernal.FS.FileHandle.release
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.FS.FileHandle.close
STACK PUSH r15
COPY rStack r15
#line run\lang\Kernal\fs.el 318:14
// Reserving r1
ADD r1 r0 4
// Register r1 already reserved
LOAD MEM BYTE r1 r1
LOAD r2 Kernal.FS.OpenMode.WRITE
// Register r2 already reserved
AND r1 r1 r2 // mode & OpenMode.WRITE != 0
GOTO EQ r1 :if_end_57
// Releasing r1
#line run\lang\Kernal\fs.el 320:18
STACK PUSH r0
// Reserving r1
GOTO PUSH :Kernal.FS.FileHandle.flush
STACK POP r0
// Releasing r1 // flush()
//  flush();

#lineend
:if_end_57
//  if(mode & OpenMode.WRITE != 0) {flush();}

#line run\lang\Kernal\fs.el 322:14
STACK PUSH r0
// Reserving r1
GOTO PUSH :Kernal.FS.FileHandle.release
STACK POP r0
// Releasing r1 // release()
//  release();

#lineend
:func_exit_Kernal.FS.FileHandle.close
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.FS.FileHandle.write_void*_int32 buffer void*, len int32
STACK PUSH r15
COPY rStack r15
#stackVar int32 len -12
#stackVar void* buffer -16
#line run\lang\Kernal\fs.el 333:14
// Reserving r1
ADD r1 r0 4
// Register r1 already reserved
LOAD MEM BYTE r1 r1
LOAD r2 Kernal.FS.OpenMode.WRITE
// Register r2 already reserved
AND r1 r1 r2
GOTO EQ r1 :exp_ee_7
ADD r1 r0 12
// Register r1 already reserved
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_58
:exp_ee_7 // mode & OpenMode.WRITE == 0 || intBuffer == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 334:18
// Reserving r1
SUB r1 r15 20
STORE BYTE 0 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.FileHandle.write_void*_int32
//  return false;

#lineend
:if_end_58
//  if(mode & OpenMode.WRITE == 0 || intBuffer == nullptr) {return false;}

#line run\lang\Kernal\fs.el 336:14
// Reserving r1
ADD r1 r0 18
// Register r1 already reserved
LOAD MEM SHORT r1 r1
 // Reserving r2
SUB r2 r15 12
// Register r2 already reserved
LOAD MEM r2 r2
ADD r1 r1 r2  // Releasing r2
ADD r2 r0 16
// Register r2 already reserved
LOAD MEM SHORT r2 r2
SUB r1 r1 r2
GOTO LEQ r1 :if_end_59 // bufferSize + len > bufferCapacity
// Releasing r1
#line run\lang\Kernal\fs.el 337:18
STACK PUSH r0
// Reserving r1
GOTO PUSH :Kernal.FS.FileHandle.flush
STACK POP r0
// Releasing r1 // flush()
//  flush();

#lineend
:if_end_59
//  if(bufferSize + len > bufferCapacity) {flush();}

#line run\lang\Kernal\fs.el 339:14
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
ADD r2 r0 16
// Register r2 already reserved
LOAD MEM SHORT r2 r2
SUB r1 r1 r2
GOTO LEQ r1 :if_end_60 // len > bufferCapacity
// Releasing r1
#line run\lang\Kernal\fs.el 340:18
STACK PUSH r0
// Reserving r1
SUB r1 r15 16
// Register r1 already reserved
LOAD MEM r1 r1 // buffer
STACK PUSH r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1 // len
STACK PUSH r1
// Releasing r1
GOTO PUSH :Kernal.FS.FileHandle.writeDirect_void*_int32
STACK DEC 8
STACK POP r0
// Releasing r1 // writeDirect(buffer, len)
//  writeDirect(buffer, len);

#line run\lang\Kernal\fs.el 341:18
// Reserving r1
SUB r1 r15 20
STORE BYTE 1 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.FileHandle.write_void*_int32
//  return true;

#lineend
:if_end_60
//  if(len > bufferCapacity) {writeDirect(buffer, len); return true;}

#line run\lang\Kernal\fs.el 344:14
// Reserving r1
// Reserving r2
ADD r2 r0 18
// Register r2 already reserved
// Register r1 already reserved
LOAD MEM SHORT r1 r2
// Reserving r3
SUB r3 r15 12
// Register r3 already reserved
LOAD MEM r3 r3 // len
ADD r3 r1 r3
STORE SHORT r3 r2
// Releasing r2
// Releasing r1
// Releasing r3
//  bufferSize += len;

#line run\lang\Kernal\fs.el 346:14
// Reserving r1
SUB r1 r15 20
STORE BYTE 1 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.FileHandle.write_void*_int32
//  return true;

#lineend
:func_exit_Kernal.FS.FileHandle.write_void*_int32
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction bool

#function Kernal.FS.FileHandle.seek_int32 newOffset int32
STACK PUSH r15
COPY rStack r15
#stackVar int32 newOffset -12
#line run\lang\Kernal\fs.el 397:14
// Reserving r1
ADD r1 r0 20
// Register r1 already reserved
// Reserving r2
SUB r2 r15 12
// Register r2 already reserved
LOAD MEM r2 r2 // newOffset
STORE r2 r1
// Releasing r1
// Releasing r2
//  offset = newOffset;

#lineend
:func_exit_Kernal.FS.FileHandle.seek_int32
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.FS.FileHandle.open_char*_OpenMode path char*, mode OpenMode
STACK PUSH r15
COPY rStack r15
#stackVar OpenMode mode -9
#stackVar char* path -16
#line run\lang\Kernal\fs.el 301:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r0
INC r1 8
// Reserving r2
SUB r2 r15 16
// Register r2 already reserved
LOAD MEM r2 r2 // path
STORE r2 r1
// Releasing r1
// Releasing r2
//  this.path = path;

#line run\lang\Kernal\fs.el 302:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r0
INC r1 4
// Reserving r2
SUB r2 r15 9
// Register r2 already reserved
LOAD MEM BYTE r2 r2 // mode
STORE BYTE r2 r1
// Releasing r1
// Releasing r2
//  this.mode = mode;

#line run\lang\Kernal\fs.el 304:14
// Reserving r1
SUB r1 r15 9
// Register r1 already reserved
LOAD MEM BYTE r1 r1
LOAD r2 Kernal.FS.OpenMode.STREAM_READ
// Register r2 already reserved
AND r1 r1 r2 // mode & OpenMode.STREAM_READ != 0
GOTO EQ r1 :if_end_61
// Releasing r1
#line run\lang\Kernal\fs.el 306:18
// Reserving r1
SUB r1 r15 20
STORE BYTE 1 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.FileHandle.open_char*_OpenMode
//  return true;

#lineend
:if_end_61
//  if(mode & OpenMode.STREAM_READ != 0) {return true;}

#line run\lang\Kernal\fs.el 308:14
// Reserving r1
#stackVar FileOpenCommand cmd
STORE 0x0010 rStack INC_RA
SUB r1 r15 16
// Register r1 already reserved
LOAD MEM r1 r1 // path
STORE r1 rStack INC_RA
// Releasing r1
//  FileOpenCommand cmd = {, path};

#line run\lang\Kernal\fs.el 309:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &Kernal.FS.fsDeviceId // fsDeviceId
STACK PUSH r1
LOAD null 8
LOAD r2 4
DIV r1 r1 r2 // sizeof(cmd) / 4
STACK PUSH r1
// Register r1 already reserved
COPY r15 r1 // &cmd
STACK PUSH r1
// Releasing r1
GOTO PUSH :Peripheral.command_int32_int32_void*
STACK DEC 12
// Releasing r1 // Peripheral.command(fsDeviceId, sizeof(cmd) / 4, & cmd)
//  Peripheral.command(fsDeviceId, sizeof(cmd) / 4, & cmd);

#line run\lang\Kernal\fs.el 310:14
// Reserving r1
LOAD r1 Peripheral.RSP_DATA
// Register r1 already reserved
// Reserving r2
INC r1 4
// Releasing r2
LOAD MEM r1 r1 // Peripheral.RSP_DATA[1]
STORE r1 r0
// Releasing r1
//  rawHandle = Peripheral.RSP_DATA[1];

#line run\lang\Kernal\fs.el 311:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r0
SET FORCE EQ r1 r1 // rawHandle == 0
// Reserving r2
SUB r2 r15 20
STORE BYTE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_Kernal.FS.FileHandle.open_char*_OpenMode
//  return rawHandle == 0;

#lineend
:func_exit_Kernal.FS.FileHandle.open_char*_OpenMode
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction bool

// Kernal.FS.FileReadCommand

// Kernal.FS.OpenMode

// Kernal.FS.ProcessFiles

#function Kernal.FS.ProcessFiles.new
STACK PUSH r15
COPY rStack r15
#line run\lang\Kernal\fs.el 238:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &Kernal.FS.ProcessFiles.nextFree
INC r1 1
GOTO NEQ r1 :if_end_62 // nextFree == 0xffff_ffff
// Releasing r1
#line run\lang\Kernal\fs.el 239:18
// Reserving r1
// Register r1 already reserved
LOAD r1 &Kernal.FS.ProcessFiles.nextFree
// Reserving r2
// Register r2 already reserved
LOAD r2 &Kernal.FS.ProcessFiles.pool // &pool
STORE r2 r1
// Releasing r1
// Releasing r2
//  nextFree = & pool;

#line run\lang\Kernal\fs.el 240:18
// For Loop:
// Initializer
// Reserving r1
// Reserving r2
// Releasing r2
LOAD r2 0 // 0
#stackVar int32 i
STACK PUSH r2
// Releasing r2
:for_condition_63
// Reserving r2
// Register r3 already reserved
COPY r1 r3
INC r3 -127
GOTO GEQ r3 :for_end_63 // i < pool.length - 1
// Releasing r2
#line run\lang\Kernal\fs.el 241:22
// Still reserved: r1
// Reserving r2
// Register r2 already reserved
LOAD r2 &Kernal.FS.ProcessFiles.pool
// Reserving r3
// Register r3 already reserved
COPY r1 r3 // i
// Found Free register r4
LOAD r4 68
MUL r3 r3 r4
ADD r2 r2 r3
// Releasing r3
// Reserving r3
// Register r3 already reserved
LOAD r3 &Kernal.FS.ProcessFiles.pool
// Reserving r4
// Register r4 already reserved
COPY r1 r4
INC r4 1 // i + 1
// Found Free register r5
LOAD r5 68
MUL r4 r4 r5
ADD r3 r3 r4
// Releasing r4 // &pool[i + 1] // cast<int32>(& pool[i + 1])
STORE r3 r2
// Releasing r2
// Releasing r3
//  pool[i].numOpen = cast<int32>(& pool[i + 1]);

#lineend
// Reserved: r1
// Iterator
INC r1 1 // i++
GOTO :for_condition_63
:for_end_63
// End of scope
// Releasing r1
//  for(int32 i = 0; i < pool.length - 1; i++) {pool[i].numOpen = cast<int32>(& pool[i + 1]);}

#line run\lang\Kernal\fs.el 243:18
// Reserving r1
// Register r1 already reserved
LOAD r1 &Kernal.FS.ProcessFiles.pool
// Reserving r2
INC r1 8636
// Releasing r2
STORE 0 r1
// Releasing r1
//  pool[pool.length - 1].numOpen = 0;

#lineend
:if_end_62
//  if(nextFree == 0xffff_ffff) {nextFree = & pool; for(int32 i = 0; i < pool.length - 1; i++) {pool[i].numOpen = cast<int32>(& pool[i + 1]);} pool[pool.length - 1].numOpen = 0;}

#line run\lang\Kernal\fs.el 245:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &Kernal.FS.ProcessFiles.nextFree
GOTO NEQ r1 :if_end_64 // nextFree == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 246:18
// Reserving r1
SUB r1 r15 12
STORE 0 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.ProcessFiles.new
//  return nullptr;

#lineend
:if_end_64
//  if(nextFree == nullptr) {return nullptr;}

#line run\lang\Kernal\fs.el 248:14
// Reserving r1
// Releasing r1
// Reserving r1
LOAD MEM r1 &Kernal.FS.ProcessFiles.nextFree // nextFree
#stackVar ProcessFiles* next
STACK PUSH r1
// Releasing r1
//  ProcessFiles* next = nextFree;

#line run\lang\Kernal\fs.el 249:14
// Reserving r1
// Register r1 already reserved
LOAD r1 &Kernal.FS.ProcessFiles.nextFree
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 r15
LOAD MEM r2 r2 // next.numOpen // force_cast<ProcessFiles*>(next.numOpen)
STORE r2 r1
// Releasing r1
// Releasing r2
//  nextFree = force_cast<ProcessFiles*>(next.numOpen);

#line run\lang\Kernal\fs.el 250:14
STACK PUSH r0
// Reserving r1
// Reserving r2
// Register r2 already reserved
COPY r15 r2
COPY r0 r2
// Releasing r2
GOTO PUSH :Kernal.FS.ProcessFiles.reset
STACK POP r0
// Releasing r1 // next.reset()
//  next.reset();

#line run\lang\Kernal\fs.el 251:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15 // next
// Reserving r2
SUB r2 r15 12
STORE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_Kernal.FS.ProcessFiles.new
//  return next;

#lineend
:func_exit_Kernal.FS.ProcessFiles.new
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction FileHandle*

#function Kernal.FS.ProcessFiles.release
STACK PUSH r15
COPY rStack r15
#line run\lang\Kernal\fs.el 255:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r0
GOTO LEQ r1 :if_end_65 // numOpen > 0
// Releasing r1
#line run\lang\Kernal\fs.el 256:18
// For Loop:
// Initializer
// Reserving r1
// Reserving r2
// Releasing r2
LOAD r2 0 // 0
#stackVar int32 i
STACK PUSH r2
// Releasing r2
:for_condition_66
// Reserving r2
// Register r3 already reserved
COPY r1 r3
INC r3 -16
GOTO GEQ r3 :for_end_66 // i < handles.length
// Releasing r2
#line run\lang\Kernal\fs.el 257:22
// Still reserved: r1
// Reserving r2
ADD r2 r0 4
// Register r2 already reserved
// Reserving r3
// Register r3 already reserved
COPY r1 r3 // i
LSH r3 r3 2
ADD r2 r2 r3
// Releasing r3
LOAD MEM r2 r2 // handles[i] != nullptr
GOTO EQ r2 :if_end_67
// Releasing r2
#line run\lang\Kernal\fs.el 258:26
STACK PUSH r0
STACK PUSH r1
// Reserving r2
// Reserving r3
ADD r3 r0 4
// Register r3 already reserved
// Reserving r4
// Register r4 already reserved
COPY r1 r4 // i
LSH r4 r4 2
ADD r3 r3 r4
// Releasing r4
COPY r0 r3
// Releasing r3
GOTO PUSH :Kernal.FS.FileHandle.close
STACK POP r0
STACK POP r1
// Releasing r2 // handles[i].close()
//  handles[i].close();

#line run\lang\Kernal\fs.el 259:26
// Reserving r2
ADD r2 r0 4
// Register r2 already reserved
// Reserving r3
// Register r3 already reserved
COPY r1 r3 // i
LSH r3 r3 2
ADD r2 r2 r3
// Releasing r3
STORE 0 r2
// Releasing r2
//  handles[i] = nullptr;

#lineend
:if_end_67
//  if(handles[i] != nullptr) {handles[i].close(); handles[i] = nullptr;}

#lineend
// Reserved: r1
// Iterator
INC r1 1 // i++
GOTO :for_condition_66
:for_end_66
// End of scope
// Releasing r1
//  for(int32 i = 0; i < handles.length; i++) {if(handles[i] != nullptr) {handles[i].close(); handles[i] = nullptr;}}

#lineend
:if_end_65
//  if(numOpen > 0) {for(int32 i = 0; i < handles.length; i++) {if(handles[i] != nullptr) {handles[i].close(); handles[i] = nullptr;}}}

#line run\lang\Kernal\fs.el 263:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &Kernal.FS.ProcessFiles.nextFree // nextFree // cast<int32>(nextFree)
STORE r1 r0
// Releasing r1
//  numOpen = cast<int32>(nextFree);

#line run\lang\Kernal\fs.el 264:14
// Reserving r1
// Register r1 already reserved
LOAD r1 &Kernal.FS.ProcessFiles.nextFree
// Reserving r2
// Register r2 already reserved
COPY r0 r2 // this
STORE r2 r1
// Releasing r1
// Releasing r2
//  nextFree = this;

#lineend
:func_exit_Kernal.FS.ProcessFiles.release
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.FS.ProcessFiles.reset
STACK PUSH r15
COPY rStack r15
#line run\lang\Kernal\fs.el 231:14
// For Loop:
// Initializer
// Reserving r1
// Reserving r2
// Releasing r2
LOAD r2 0 // 0
#stackVar int32 i
STACK PUSH r2
// Releasing r2
:for_condition_68
// Reserving r2
// Register r3 already reserved
COPY r1 r3
INC r3 -16
GOTO GEQ r3 :for_end_68 // i < handles.length
// Releasing r2
#line run\lang\Kernal\fs.el 232:18
// Still reserved: r1
// Reserving r2
ADD r2 r0 4
// Register r2 already reserved
// Reserving r3
// Register r3 already reserved
COPY r1 r3 // i
LSH r3 r3 2
ADD r2 r2 r3
// Releasing r3
STORE 0 r2
// Releasing r2
//  handles[i] = nullptr;

#lineend
// Reserved: r1
// Iterator
INC r1 1 // i++
GOTO :for_condition_68
:for_end_68
// End of scope
// Releasing r1
//  for(int32 i = 0; i < handles.length; i++) {handles[i] = nullptr;}

#line run\lang\Kernal\fs.el 234:14
STORE 0 r0
//  numOpen = 0;

#lineend
:func_exit_Kernal.FS.ProcessFiles.reset
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.FS.ProcessFiles.close
STACK PUSH r15
COPY rStack r15
#line run\lang\Kernal\fs.el 215:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r0
GOTO NEQ r1 :if_end_69 // numOpen == 0
// Releasing r1
#line run\lang\Kernal\fs.el 216:18
GOTO :func_exit_Kernal.FS.ProcessFiles.close
//  return;

#lineend
:if_end_69
//  if(numOpen == 0) {return;}

#line run\lang\Kernal\fs.el 218:14
// For Loop:
// Initializer
// Reserving r1
// Reserving r2
// Releasing r2
LOAD r2 0 // 0
#stackVar int32 i
STACK PUSH r2
// Releasing r2
:for_condition_70
// Reserving r2
// Register r3 already reserved
COPY r1 r3
INC r3 -16
GOTO GEQ r3 :for_end_70 // i < handles.length
// Releasing r2
#line run\lang\Kernal\fs.el 219:18
// Still reserved: r1
// Reserving r2
ADD r2 r0 4
// Register r2 already reserved
// Reserving r3
// Register r3 already reserved
COPY r1 r3 // i
LSH r3 r3 2
ADD r2 r2 r3
// Releasing r3
LOAD MEM r2 r2 // handles[i] != nullptr
GOTO EQ r2 :if_end_71
// Releasing r2
#line run\lang\Kernal\fs.el 220:22
STACK PUSH r0
STACK PUSH r1
// Reserving r2
// Reserving r3
ADD r3 r0 4
// Register r3 already reserved
// Reserving r4
// Register r4 already reserved
COPY r1 r4 // i
LSH r4 r4 2
ADD r3 r3 r4
// Releasing r4
COPY r0 r3
// Releasing r3
GOTO PUSH :Kernal.FS.FileHandle.close
STACK POP r0
STACK POP r1
// Releasing r2 // handles[i].close()
//  handles[i].close();

#line run\lang\Kernal\fs.el 221:22
// Reserving r2
ADD r2 r0 4
// Register r2 already reserved
// Reserving r3
// Register r3 already reserved
COPY r1 r3 // i
LSH r3 r3 2
ADD r2 r2 r3
// Releasing r3
STORE 0 r2
// Releasing r2
//  handles[i] = nullptr;

#lineend
:if_end_71
//  if(handles[i] != nullptr) {handles[i].close(); handles[i] = nullptr;}

#lineend
// Reserved: r1
// Iterator
INC r1 1 // i++
GOTO :for_condition_70
:for_end_70
// End of scope
// Releasing r1
//  for(int32 i = 0; i < handles.length; i++) {if(handles[i] != nullptr) {handles[i].close(); handles[i] = nullptr;}}

#line run\lang\Kernal\fs.el 224:14
STORE 0 r0
//  numOpen = 0;

#lineend
:func_exit_Kernal.FS.ProcessFiles.close
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.FS.ProcessFiles.open_char*_OpenMode path char*, mode OpenMode
STACK PUSH r15
COPY rStack r15
#stackVar OpenMode mode -9
#stackVar char* path -16
#line run\lang\Kernal\fs.el 192:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r0
INC r1 -16
GOTO NEQ r1 :if_end_72 // numOpen == handles.length
// Releasing r1
#line run\lang\Kernal\fs.el 193:18
// Reserving r1
SUB r1 r15 20
STORE -1 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.ProcessFiles.open_char*_OpenMode
//  return - 1;

#lineend
:if_end_72
//  if(numOpen == handles.length) {return - 1;}

#line run\lang\Kernal\fs.el 195:14
// Reserving r1
// Releasing r1
LOAD r1 0 // 0
#stackVar int32 outHandle
STACK PUSH r1
// Releasing r1
//  int32 outHandle = 0;

#line run\lang\Kernal\fs.el 196:14
// For Loop:
// Initializer
:for_condition_73
// Reserving r1
// Register r2 already reserved
LOAD MEM r2 r15
INC r2 -16
GOTO GEQ r2 :for_end_73 // outHandle < handles.length
// Releasing r1
#line run\lang\Kernal\fs.el 197:18
// Reserving r1
ADD r1 r0 4
// Register r1 already reserved
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 r15 // outHandle
LSH r2 r2 2
ADD r1 r1 r2
// Releasing r2
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_74 // handles[outHandle] == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 198:22
GOTO :for_end_73
//  break;

#lineend
:if_end_74
//  if(handles[outHandle] == nullptr) {break;}

#lineend
// Iterator
// Register r2 already reserved
COPY r15 r2
LOAD MEM r1 r2
ADD r3 r1 1
STORE r3 r2 // outHandle++
GOTO :for_condition_73
:for_end_73
// End of scope
//  for(; outHandle < handles.length; outHandle++) {if(handles[outHandle] == nullptr) {break;}}

#line run\lang\Kernal\fs.el 201:14
// Reserving r1
// Releasing r1
STACK INC 4
// Reserving r1
GOTO PUSH :Kernal.FS.FileHandle.new
STACK POP r1
// Releasing r1 // FileHandle.new()
#stackVar FileHandle* ptr
STACK PUSH r1
// Releasing r1
//  FileHandle* ptr = FileHandle.new();

#line run\lang\Kernal\fs.el 202:14
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_75 // ptr == nullptr
// Releasing r1
#line run\lang\Kernal\fs.el 203:18
// Reserving r1
SUB r1 r15 20
STORE -1 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.ProcessFiles.open_char*_OpenMode
//  return - 1;

#lineend
:if_end_75
//  if(ptr == nullptr) {return - 1;}

#line run\lang\Kernal\fs.el 205:14
// Reserving r1
STACK PUSH r0
STACK INC 4
// Reserving r2
SUB r2 r15 16
// Register r2 already reserved
LOAD MEM r2 r2 // path
STACK PUSH r2
SUB r2 r15 9
// Register r2 already reserved
LOAD MEM BYTE r2 r2 // mode
STACK PUSH BYTE r2
// Releasing r2
// Reserving r2
ADD r2 r15 4
// Register r2 already reserved
COPY r0 r2
// Releasing r2
GOTO PUSH :Kernal.FS.FileHandle.open_char*_OpenMode
STACK DEC 8
STACK POP BYTE r1
STACK POP r0
// Releasing r2
GOTO NEQ r1 :if_end_76 // !ptr.open(path, mode)
// Releasing r1
#line run\lang\Kernal\fs.el 206:18
STACK PUSH r0
// Reserving r1
// Reserving r2
ADD r2 r15 4
// Register r2 already reserved
COPY r0 r2
// Releasing r2
GOTO PUSH :Kernal.FS.FileHandle.release
STACK POP r0
// Releasing r1 // ptr.release()
//  ptr.release();

#line run\lang\Kernal\fs.el 207:18
// Reserving r1
SUB r1 r15 20
STORE -1 r1
// Releasing r1
GOTO :func_exit_Kernal.FS.ProcessFiles.open_char*_OpenMode
//  return - 1;

#lineend
:if_end_76
//  if(! ptr.open(path, mode)) {ptr.release(); return - 1;}

#line run\lang\Kernal\fs.el 209:14
// Reserving r1
ADD r1 r0 4
// Register r1 already reserved
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 r15 // outHandle
LSH r2 r2 2
ADD r1 r1 r2
// Releasing r2
// Reserving r2
ADD r2 r15 4
// Register r2 already reserved
LOAD MEM r2 r2 // ptr
STORE r2 r1
// Releasing r1
// Releasing r2
//  handles[outHandle] = ptr;

#line run\lang\Kernal\fs.el 210:14
// Register r2 already reserved
COPY r0 r2
LOAD MEM r1 r2
ADD r3 r1 1
STORE r3 r2 // numOpen++
//  numOpen++;

#line run\lang\Kernal\fs.el 211:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15 // outHandle
// Reserving r2
SUB r2 r15 20
STORE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_Kernal.FS.ProcessFiles.open_char*_OpenMode
//  return outHandle;

#lineend
:func_exit_Kernal.FS.ProcessFiles.open_char*_OpenMode
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction int32

// Ref text

// SysD

// SysD.AddressSpace

// Peripheral

#function Peripheral.command_int32_int32_void* deviceId int32, cmdSize int32, cmd void*
#line <Peripheral> 1:1
STACK PUSH r15
COPY rStack r15
#stackVar int32 deviceId -20
#stackVar int32 cmdSize -16
#stackVar int32* cmd -12
LOAD r1 Peripheral.CMD_SIZE
SUB r2 r15 16
LOAD MEM r2 r2
STORE r2 r1 INC_RA
SUB r3 r15 12
LOAD MEM r3 r3
:Peripheral.command_loop
COPY MEM r3 r1 INC_RS INC_RD
INC r2 -1
GOTO GT r2 :Peripheral.command_loop
SUB r1 r15 20
LOAD MEM r1 r1
LOAD r2 0x0101_0000
OR r1 r1 r2
STORE r1 Peripheral.CMD_ADDR
#stackVarClear deviceId
#stackVarClear cmdSize
#stackVarClear cmd
STACK POP r15
#lineend
GOTO POP
#endfunction void

// Peripheral.PeripheralDescriptor

// Mutex

HALT