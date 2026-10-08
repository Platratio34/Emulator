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
#var Kernal._pool (128) ProcessFiles[32]
#define Kernal.CMD_STATUS 0x0001_0001 uint8*
#var Kernal.firstFree 0x00 ProcessFiles*
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

#function Kernal.getNew
STACK PUSH r15
COPY rStack r15
#line run/lang/Kernal/fs.el 17:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &Kernal.firstFree
GOTO NEQ r1 :if_end_0 // firstFree == nullptr
// Releasing r1
#line run/lang/Kernal/fs.el 18:14
// Reserving r1
SUB r1 r15 12
STORE 0 r1
// Releasing r1
GOTO :func_exit_Kernal.getNew
//  return nullptr;

#lineend
:if_end_0
//  if(firstFree == nullptr) {return nullptr;}

#line run/lang/Kernal/fs.el 20:10
// Reserving r1
// Releasing r1
// Reserving r1
LOAD MEM r1 &Kernal.firstFree // firstFree
#stackVar ProcessFiles* next
STACK PUSH r1
// Releasing r1
//  ProcessFiles* next = firstFree;

#line run/lang/Kernal/fs.el 21:10
// Reserving r1
// Register r1 already reserved
LOAD r1 &Kernal.firstFree
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 &Kernal.firstFree
LOAD MEM r2 r2 // firstFree.numOpen // force_cast<ProcessFiles*>(firstFree.numOpen)
STORE r2 r1
// Releasing r1
// Releasing r2
//  firstFree = force_cast<ProcessFiles*>(firstFree.numOpen);

#line run/lang/Kernal/fs.el 22:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15 // next
// Reserving r2
SUB r2 r15 12
STORE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_Kernal.getNew
//  return next;

#lineend
:func_exit_Kernal.getNew
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction ProcessFiles*

#function Kernal.read_char*_int32 buffer char*, bufferSize int32
STACK PUSH r15
COPY rStack r15
#stackVar char* buffer -16
#stackVar int32 bufferSize -12
#line run/lang/Kernal/console.el 102:10
#line run/lang/Kernal/console.el 103:14
LOAD r1 Kernal.CONSOLE_IN_COUNT
#line run/lang/Kernal/console.el 104:14
:read_l0
#line run/lang/Kernal/console.el 105:14
LOAD MEM BYTE r2 r1
#line run/lang/Kernal/console.el 106:14
GOTO EQ r2 :read_l0
//  asm{\nLOAD r1 Kernal.CONSOLE_IN_COUNT\n:read_l0\nLOAD MEM BYTE r2 r1\nGOTO EQ r2 :read_l0\n}

#line run/lang/Kernal/console.el 108:10
// Reserving r1
// Releasing r1
LOAD r1 Kernal.CONSOLE_IN_COUNT
// Reserving r1

LOAD MEM BYTE r1 r1 // *CONSOLE_IN_COUNT
#stackVar int32 inCount
STACK PUSH r1
// Releasing r1
//  int32 inCount =* CONSOLE_IN_COUNT;

#line run/lang/Kernal/console.el 109:10
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
// Register r2 already reserved
LOAD MEM r2 r15
SUB r1 r1 r2
GOTO GEQ r1 :if_end_1 // bufferSize < inCount
// Releasing r1
#line run/lang/Kernal/console.el 110:14
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1 // bufferSize
STORE r1 r15
// Releasing r1
//  inCount = bufferSize;

#lineend
:if_end_1
//  if(bufferSize < inCount) {inCount = bufferSize;}

#line run/lang/Kernal/console.el 112:10
// Reserving r1
// Releasing r1
LOAD r1 0 // 0
#stackVar int32 i
STACK PUSH r1
// Releasing r1
//  int32 i = 0;

#line run/lang/Kernal/console.el 113:10
:while_condition_2
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
// Register r2 already reserved
LOAD MEM r2 r15
SUB r1 r1 r2
GOTO GEQ r1 :while_end_2 // i < inCount
// Releasing r1
#line run/lang/Kernal/console.el 114:14
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

#line run/lang/Kernal/console.el 116:14
ADD r2 r15 4
// Register r2 already reserved
LOAD MEM r1 r2
ADD r3 r1 1
STORE r3 r2 // i++
//  i++;

#lineend
GOTO :while_condition_2
:while_end_2
//  while(i < inCount) {buffer[i] =* CONSOLE_IN; i++;}

#line run/lang/Kernal/console.el 118:10
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
SUB r2 r15 12
// Register r2 already reserved
LOAD MEM r2 r2
SUB r1 r1 r2
GOTO GEQ r1 :if_end_3 // i < bufferSize
// Releasing r1
#line run/lang/Kernal/console.el 119:14
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
:if_end_3
//  if(i < bufferSize) {buffer[i] = '\0';}

#lineend
:func_exit_Kernal.read_char*_int32
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#syscall 2 Kernal_kfree
#function syscall::Kernal_kfree num int32
GOTO PUSH :Kernal.kfree_int32
SYSRETURN
#endfunction void

#function Kernal.kfree_int32 num int32
STACK PUSH r15
COPY rStack r15
#stackVar int32 num -12
#line run/lang/Kernal/kalloc.el 45:10
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

#line run/lang/Kernal/kalloc.el 46:10
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

#line run/lang/Kernal/kalloc.el 47:10
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
// Register r2 already reserved
LOAD MEM r2 r15
SUB r1 r1 r2
GOTO LEQ r1 :if_end_4 // num > cPages
// Releasing r1
#line run/lang/Kernal/kalloc.el 48:14
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
:if_end_4
//  if(num > cPages) {num = cPages;}

#line run/lang/Kernal/kalloc.el 50:10
:while_condition_5
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
GOTO LEQ r1 :while_end_5 // num > 0
// Releasing r1
#line run/lang/Kernal/kalloc.el 51:14
// Register r2 already reserved
COPY r15 r2
LOAD MEM r1 r2
SUB r3 r1 1
STORE r3 r2 // cPages--
//  cPages--;

#line run/lang/Kernal/kalloc.el 52:14
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

#line run/lang/Kernal/kalloc.el 53:14
SUB r2 r15 12
// Register r2 already reserved
LOAD MEM r1 r2
SUB r3 r1 1
STORE r3 r2 // num--
//  num--;

#lineend
GOTO :while_condition_5
:while_end_5
//  while(num > 0) {cPages--; pageFreeTable[(cast<int32>(SysD.rMemTbl[cPages]) & 0x7fff_ffff) >> 12] = false; num--;}

#line run/lang/Kernal/kalloc.el 55:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15 // cPages
STORE r1 rMemTbl
// Releasing r1
//  SysD.rMemTbl[0] = cPages;

#line run/lang/Kernal/kalloc.el 56:10
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
GOTO POP
#endfunction void

#function Kernal.release_ProcessFiles* files ProcessFiles*
STACK PUSH r15
COPY rStack r15
#stackVar ProcessFiles* files -12
#line run/lang/Kernal/fs.el 26:10
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 &Kernal.firstFree // firstFree // cast<int32>(firstFree)
STORE r2 r1
// Releasing r1
// Releasing r2
//  files.numOpen = cast<int32>(firstFree);

#line run/lang/Kernal/fs.el 27:10
// Reserving r1
// Register r1 already reserved
LOAD r1 &Kernal.firstFree
// Reserving r2
SUB r2 r15 12
// Register r2 already reserved
LOAD MEM r2 r2 // files
STORE r2 r1
// Releasing r1
// Releasing r2
//  firstFree = files;

#lineend
:func_exit_Kernal.release_ProcessFiles*
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.createProcess
STACK PUSH r15
COPY rStack r15
#line run/lang/Kernal/kernal.el 171:10
// Reserving r1
// Releasing r1
// Reserving r1
LOAD MEM r1 &Kernal.lastPID
INC r1 1 // lastPID + 1
#stackVar int32 nextPID
STACK PUSH r1
// Releasing r1
//  int32 nextPID = lastPID + 1;

#line run/lang/Kernal/kernal.el 172:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 -1024
GOTO NEQ r1 :if_end_6 // nextPID == 1024
// Releasing r1
#line run/lang/Kernal/kernal.el 173:14
STORE 2 r15
//  nextPID = 2;

#lineend
:if_end_6
//  if(nextPID == 1024) {nextPID = 2;}

#line run/lang/Kernal/kernal.el 175:10
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
#line run/lang/Kernal/kernal.el 176:14
// Register r2 already reserved
COPY r15 r2
LOAD MEM r1 r2
ADD r3 r1 1
STORE r3 r2 // nextPID++
//  nextPID++;

#line run/lang/Kernal/kernal.el 177:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 -1024
GOTO NEQ r1 :if_end_8 // nextPID == 1024
// Releasing r1
#line run/lang/Kernal/kernal.el 178:18
STORE 2 r15
//  nextPID = 2;

#lineend
:if_end_8
//  if(nextPID == 1024) {nextPID = 2;}

#line run/lang/Kernal/kernal.el 180:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
// Register r2 already reserved
LOAD MEM r2 &Kernal.lastPID
SUB r1 r1 r2
GOTO NEQ r1 :if_end_9 // nextPID == lastPID
// Releasing r1
#line run/lang/Kernal/kernal.el 181:18
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

#line run/lang/Kernal/kernal.el 184:10
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

#line run/lang/Kernal/kernal.el 185:10
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

#syscall 1 Kernal_kalloc
#function syscall::Kernal_kalloc
GOTO PUSH :Kernal.kalloc
SYSRETURN
#endfunction void*

#function Kernal.kalloc
STACK PUSH r15
COPY rStack r15
#line run/lang/Kernal/kalloc.el 18:10
#alias r1 this // Reserving r1
LOAD this &Kernal.kallocMutex
// INLINE START Mutex.acquire
:Mutex.acquire_loop_3
// Resolving placeholder 1 to r2
TEST AND SET r2 this
GOTO NEQ r2 :Mutex.acquire_loop_3
// INLINE END
STACK POP r0
#alias clear this // Releasing r1 // kallocMutex.acquire()
//  kallocMutex.acquire();

#line run/lang/Kernal/kalloc.el 19:10
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

#line run/lang/Kernal/kalloc.el 20:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 -32
GOTO LT r1 :if_end_10 // cPages >= MAX_BLOCKS
// Releasing r1
#line run/lang/Kernal/kalloc.el 21:14
#alias r1 this // Reserving r1
LOAD this &Kernal.kallocMutex
// INLINE START Mutex.release
STORE BYTE 0x0 this
// INLINE END
STACK POP r0
#alias clear this // Releasing r1 // kallocMutex.release()
//  kallocMutex.release();

#line run/lang/Kernal/kalloc.el 22:14
// Reserving r1
SUB r1 r15 12
STORE 0 r1
// Releasing r1
GOTO :func_exit_Kernal.kalloc
//  return nullptr;

#lineend
:if_end_10
//  if(cPages >= MAX_BLOCKS) {kallocMutex.release(); return nullptr;}

#line run/lang/Kernal/kalloc.el 24:10
// Reserving r1
// Releasing r1
LOAD r1 0 // 0
#stackVar int32 i
STACK PUSH r1
// Releasing r1
//  int32 i = 0;

#line run/lang/Kernal/kalloc.el 25:10
:while_condition_11
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
GOTO EQ r1 :while_end_11
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 -4096
GOTO GEQ r1 :while_end_11 // i < 0x1000 // pageFreeTable[i] && ( i < 0x1000 )
// Releasing r1
#line run/lang/Kernal/kalloc.el 26:14
ADD r2 r15 4
// Register r2 already reserved
LOAD MEM r1 r2
ADD r3 r1 1
STORE r3 r2 // i++
//  i++;

#lineend
GOTO :while_condition_11
:while_end_11
//  while(pageFreeTable[i] && (i < 0x1000)) {i++;}

#line run/lang/Kernal/kalloc.el 28:10
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 -4096
GOTO NEQ r1 :if_end_12 // i == 0x1000
// Releasing r1
#line run/lang/Kernal/kalloc.el 29:14
#alias r1 this // Reserving r1
LOAD this &Kernal.kallocMutex
// INLINE START Mutex.release
STORE BYTE 0x0 this
// INLINE END
STACK POP r0
#alias clear this // Releasing r1 // kallocMutex.release()
//  kallocMutex.release();

#line run/lang/Kernal/kalloc.el 30:14
// Reserving r1
SUB r1 r15 12
STORE 0 r1
// Releasing r1
GOTO :func_exit_Kernal.kalloc
//  return nullptr;

#lineend
:if_end_12
//  if(i == 0x1000) {kallocMutex.release(); return nullptr;}

#line run/lang/Kernal/kalloc.el 32:10
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

#line run/lang/Kernal/kalloc.el 33:10
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

#line run/lang/Kernal/kalloc.el 34:10
// Reserving r1
LOAD MEM BYTE r1 r15
ADD r1 r1 1
STORE r1 r15
// Releasing r1
// Releasing r1
//  cPages += 1;

#line run/lang/Kernal/kalloc.el 35:10
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

#line run/lang/Kernal/kalloc.el 36:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15 // cPages
STORE r1 rMemTbl
// Releasing r1
//  SysD.rMemTbl[0] = cPages;

#line run/lang/Kernal/kalloc.el 38:10
#alias r1 this // Reserving r1
LOAD this &Kernal.kallocMutex
// INLINE START Mutex.release
STORE BYTE 0x0 this
// INLINE END
STACK POP r0
#alias clear this // Releasing r1 // kallocMutex.release()
//  kallocMutex.release();

#line run/lang/Kernal/kalloc.el 39:10
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
GOTO POP
#endfunction void*

:__start
#function Kernal._main
STACK PUSH r15
COPY rStack r15
#line run/lang/Kernal/kernal.el 37:10
#line run/lang/Kernal/kernal.el 38:14
LOAD rIH &:Kernal._interrupt
#line run/lang/Kernal/kernal.el 39:14
LOAD rPID 0
//  asm{\nLOAD rIH &:Kernal._interrupt\nLOAD rPID 0\n}

#line run/lang/Kernal/kernal.el 42:10
// Reserving r1
#define exp_str_inline_11 "Starting \0"
LOAD r1 &exp_str_inline_11 // Starting \0
STACK PUSH r1
// Releasing r1
GOTO PUSH :Kernal.printStr_char*
STACK DEC 4
// Releasing r1 // printStr("Starting \0")
//  printStr("Starting \0");

#line run/lang/Kernal/kernal.el 43:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &Kernal.SYS_NAME // SYS_NAME
STACK PUSH r1
// Releasing r1
GOTO PUSH :Kernal.printStr_char*
STACK DEC 4
// Releasing r1 // printStr(SYS_NAME)
//  printStr(SYS_NAME);

#line run/lang/Kernal/kernal.el 44:10
// Reserving r1
LOAD r1 '\n' // \n
STACK PUSH BYTE r1
// Releasing r1
GOTO PUSH :Kernal.printChar_char
STACK DEC 4
// Releasing r1 // printChar('\n')
//  printChar('\n');

#line run/lang/Kernal/kernal.el 46:10
// Reserving r1
GOTO PUSH :Kernal.setupFS
// Releasing r1 // setupFS()
//  setupFS();

#line run/lang/Kernal/kernal.el 50:10
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

#line run/lang/Kernal/kernal.el 51:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 84
STORE BYTE 3 r1
// Releasing r1
//  kernalProcess.status = ProcessStatus.RUNNING;

#line run/lang/Kernal/kernal.el 52:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 92
STORE 0 r1
// Releasing r1
//  kernalProcess.parent = 0;

#line run/lang/Kernal/kernal.el 53:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
STORE 1 r1
// Releasing r1
//  kernalProcess.pid = 1;

#line run/lang/Kernal/kernal.el 54:10
LOAD rPID 1
// Releasing [un-set register]
//  SysD.rPID = 1;

#line run/lang/Kernal/kernal.el 55:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 88
STORE 0 r1
// Releasing r1
//  kernalProcess.interruptHandler = nullptr;

#line run/lang/Kernal/kernal.el 57:10
// Reserving r1
// Releasing r1
LOAD r1 2 // 2
#stackVar int32 pI
STACK PUSH r1
// Releasing r1
//  int32 pI = 2;

#line run/lang/Kernal/kernal.el 58:10
:while_condition_13
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 -1024
GOTO GEQ r1 :while_end_13 // pI < 1024
// Releasing r1
#line run/lang/Kernal/kernal.el 59:14
// Reserving r1
// Register r1 already reserved
LOAD r1 &Kernal.processStates
// Reserving r2
ADD r2 r15 4
// Register r2 already reserved
LOAD MEM r2 r2 // pI
LSH r2 r2 7
ADD r1 r1 r2
// Releasing r2
INC r1 84
STORE BYTE 0 r1
// Releasing r1
//  processStates[pI].status = ProcessStatus.NONE;

#lineend
GOTO :while_condition_13
:while_end_13
//  while(pI < 1024) {processStates[pI].status = ProcessStatus.NONE;}

#line run/lang/Kernal/kernal.el 65:10
#line run/lang/Kernal/kernal.el 66:14
:kLoop
#line run/lang/Kernal/kernal.el 67:14
LOAD MEM r1 &Kernal.processReadyQueue
#line run/lang/Kernal/kernal.el 68:14
GOTO EQ r1 :kLoop
#line run/lang/Kernal/kernal.el 69:14
INTERRUPT 0x9000_0000
#line run/lang/Kernal/kernal.el 70:14
GOTO :kLoop
//  asm{\n:kLoop\nLOAD MEM r1 &Kernal.processReadyQueue\nGOTO EQ r1 :kLoop\nINTERRUPT 0x9000_0000\nGOTO :kLoop\n}

#lineend
:func_exit_Kernal._main
COPY r15 rStack
STACK POP r15
HALT
#endfunction void

#function Kernal.setupFS
STACK PUSH r15
COPY rStack r15
#line run/lang/Kernal/fs.el 9:10
// Reserving r1
// Register r1 already reserved
LOAD r1 &Kernal.firstFree
// Reserving r2
// Register r2 already reserved
LOAD r2 &Kernal._pool // &_pool
STORE r2 r1
// Releasing r1
// Releasing r2
//  firstFree = & _pool;

#line run/lang/Kernal/fs.el 10:10
// For Loop:
// Initializer
// Reserving r1
// Reserving r2
// Releasing r2
LOAD r2 0 // 0
#stackVar int32 i
STACK PUSH r2
// Releasing r2
:for_condition_14
// Reserving r2
// Register r3 already reserved
COPY r1 r3
INC r3 -32
GOTO GEQ r3 :for_end_14 // i < _pool.length
// Releasing r2
#line run/lang/Kernal/fs.el 11:14
// Still reserved: r1
// Reserving r2
// Register r2 already reserved
LOAD r2 &Kernal._pool
// Reserving r3
// Register r3 already reserved
COPY r1 r3 // i
LSH r3 r3 2
ADD r2 r2 r3
// Releasing r3
// Reserving r3
// Register r3 already reserved
LOAD r3 &Kernal._pool
// Reserving r4
// Register r4 already reserved
COPY r1 r4
INC r4 1 // i + 1
LSH r4 r4 2
ADD r3 r3 r4
// Releasing r4 // &_pool[i + 1] // cast<int32>(& _pool[i + 1])
STORE r3 r2
// Releasing r2
// Releasing r3
//  _pool[i].numOpen = cast<int32>(& _pool[i + 1]);

#lineend
// Reserved: r1
// Iterator
INC r1 1 // i++
GOTO :for_condition_14
:for_end_14
// End of scope
// Releasing r1
//  for(int32 i = 0; i < _pool.length; i++) {_pool[i].numOpen = cast<int32>(& _pool[i + 1]);}

#line run/lang/Kernal/fs.el 13:10
// Reserving r1
// Register r1 already reserved
LOAD r1 &Kernal._pool
// Reserving r2
INC r1 124
// Releasing r2
STORE 0 r1
// Releasing r1
//  _pool[_pool.length - 1].numOpen = 0;

#lineend
:func_exit_Kernal.setupFS
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.intToDec_int32_char* value int32, str char*
STACK PUSH r15
COPY rStack r15
#stackVar char* str -12
#stackVar int32 value -16
#line run/lang/Kernal/console.el 73:10
#line run/lang/Kernal/console.el 74:14
SUB r1 r15 16
#line run/lang/Kernal/console.el 75:14
LOAD MEM r1 r1 // value
#line run/lang/Kernal/console.el 76:14
SUB r2 r15 12
#line run/lang/Kernal/console.el 77:14
LOAD MEM r2 r2 // str
#line run/lang/Kernal/console.el 78:14
COPY rStack r3
#stackVar char[16] tempStr
#line run/lang/Kernal/console.el 80:14
STACK INC 16 // char* str2
#line run/lang/Kernal/console.el 81:14
LOAD r4 10
#line run/lang/Kernal/console.el 82:14
LOAD r6 0x0
#line run/lang/Kernal/console.el 83:14
:intToDec_l1
#line run/lang/Kernal/console.el 84:18
DIV r1 r1 r4
#line run/lang/Kernal/console.el 85:18
COPY rAF r7
#line run/lang/Kernal/console.el 86:18
ADD r7 r7 0x30
#line run/lang/Kernal/console.el 87:18
STORE BYTE r7 r3 INC_RA
#line run/lang/Kernal/console.el 88:18
INC r6
#line run/lang/Kernal/console.el 89:18
GOTO NEQ r1 :intToDec_l1
#line run/lang/Kernal/console.el 90:14
INC r3 -1
#line run/lang/Kernal/console.el 91:14
:intToDec_l2
#line run/lang/Kernal/console.el 92:18
COPY MEM BYTE r3 r2 INC_RD
#line run/lang/Kernal/console.el 93:18
INC r6 -1
#line run/lang/Kernal/console.el 94:18
INC r3 -1
#line run/lang/Kernal/console.el 95:18
GOTO NEQ r6 :intToDec_l2
#line run/lang/Kernal/console.el 96:14
STORE BYTE 0x0 r2
#stackVarClear tempStr
//  asm{\nSUB r1 r15 16\nLOAD MEM r1 r1 // value\nSUB r2 r15 12\nLOAD MEM r2 r2 // str\nCOPY rStack r3\n#stackVar char[16] tempStr\nSTACK INC 16 // char* str2\nLOAD r4 10\nLOAD r6 0x0\n:intToDec_l1\nDIV r1 r1 r4\nCOPY rAF r7\nADD r7 r7 0x30\nSTORE BYTE r7 r3 INC_RA\nINC r6\nGOTO NEQ r1 :intToDec_l1\nINC r3 -1\n:intToDec_l2\nCOPY MEM BYTE r3 r2 INC_RD\nINC r6 -1\nINC r3 -1\nGOTO NEQ r6 :intToDec_l2\nSTORE BYTE 0x0 r2\n#stackVarClear tempStr\n}

#lineend
:func_exit_Kernal.intToDec_int32_char*
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Kernal._interrupt
STACK PUSH r15
COPY rStack r15
#line run/lang/Kernal/kernal.el 76:10
// Reserving r1
// Releasing r1
// Reserving r1
COPY rIC r1 // SysD.rIC
#stackVar int32 code
STACK PUSH r1
// Releasing r1
//  int32 code = SysD.rIC;

#line run/lang/Kernal/kernal.el 77:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
LOAD r2 -2147483648
AND r1 r1 r2 // code & 0x8000_0000
GOTO NEQ r1 :if_end_15 // ( code & 0x8000_0000 ) == 0
// Releasing r1
#line run/lang/Kernal/kernal.el 78:14
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

#line run/lang/Kernal/kernal.el 79:14
LOAD rPM 0
// Releasing [un-set register]
//  SysD.rPM = false;

#line run/lang/Kernal/kernal.el 82:14
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 88
LOAD MEM r1 r1 // cProc.interruptHandler
GOTO EQ r1 :if_end_16
// Releasing r1
#line run/lang/Kernal/kernal.el 83:18
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
:if_end_16
//  if(cProc.interruptHandler) {cProc.interruptHandler(code);}

#line run/lang/Kernal/kernal.el 85:14
INTERRUPT RET // SysD.interruptReturn()
//  SysD.interruptReturn();

#line run/lang/Kernal/kernal.el 86:14
GOTO :func_exit_Kernal._interrupt
//  return;

#lineend
STACK DEC 4
// End of scope
#stackVarClear cProc
:if_end_15
//  if((code & 0x8000_0000) == 0) {ProcessState & cProc = & processStates[SysD.rPID]; SysD.rPM = false; if(cProc.interruptHandler) {cProc.interruptHandler(code);} SysD.interruptReturn(); return;}

#line run/lang/Kernal/kernal.el 89:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
LOAD r2 -2147483647
SUB r1 r1 r2
GOTO NEQ r1 :if_end_17 // code == 0x8000_0001
// Releasing r1
#line run/lang/Kernal/kernal.el 90:14
HALT // SysD.halt()
//  SysD.halt();

#lineend
:if_end_17
//  if(code == 0x8000_0001) {SysD.halt();}

#line run/lang/Kernal/kernal.el 92:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
LOAD r2 -256
AND r1 r1 r2 // code & 0xffff_ff00
LOAD r2 -2147483136
SUB r1 r1 r2
GOTO NEQ r1 :if_end_18 // ( code & 0xffff_ff00 ) == 0x8000_0200
// Releasing r1
#line run/lang/Kernal/kernal.el 93:14
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
:if_end_18
//  if((code & 0xffff_ff00) == 0x8000_0200) {int32 i = code & 0xff;}

#line run/lang/Kernal/kernal.el 95:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
LOAD r2 -16
AND r1 r1 r2
LOAD r2 -1879048192
SUB r1 r1 r2
GOTO NEQ r1 :if_end_19 // code & 0xffff_fff0 == 0x9000_0000
// Releasing r1
#line run/lang/Kernal/kernal.el 96:14
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

#line run/lang/Kernal/kernal.el 97:14
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

#line run/lang/Kernal/kernal.el 98:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
LOAD r2 -1879048191
SUB r1 r1 r2
GOTO NEQ r1 :if_end_20 // code == 0x9000_0001
// Releasing r1
#line run/lang/Kernal/kernal.el 99:18
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 84
LOAD MEM BYTE r1 r1
INC r1 -3
GOTO NEQ r1 :if_end_21 // oldState.status == ProcessStatus.RUNNING
// Releasing r1
#line run/lang/Kernal/kernal.el 100:22
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 84
STORE BYTE 2 r1
// Releasing r1
//  oldState.status = ProcessStatus.READY;

#lineend
:if_end_21
//  if(oldState.status == ProcessStatus.RUNNING) {oldState.status = ProcessStatus.READY;}

#lineend
:if_end_20
//  if(code == 0x9000_0001) {if(oldState.status == ProcessStatus.RUNNING) {oldState.status = ProcessStatus.READY;}}

#line run/lang/Kernal/kernal.el 103:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
LOAD r2 -1879048190
SUB r1 r1 r2
GOTO NEQ r1 :if_end_22 // code == 0x9000_0002
// Releasing r1
#line run/lang/Kernal/kernal.el 104:18
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 84
STORE BYTE 5 r1
// Releasing r1
//  oldState.status = ProcessStatus.DEAD;

#lineend
:if_end_22
//  if(code == 0x9000_0002) {oldState.status = ProcessStatus.DEAD;}

#line run/lang/Kernal/kernal.el 109:14
#alias r1 this // Reserving r1
LOAD this &Kernal.processReadyQueueLock
// INLINE START Mutex.acquire
:Mutex.acquire_loop_4
// Resolving placeholder 1 to r2
TEST AND SET r2 this
GOTO NEQ r2 :Mutex.acquire_loop_4
// INLINE END
STACK POP r0
#alias clear this // Releasing r1 // processReadyQueueLock.acquire()
//  processReadyQueueLock.acquire();

#line run/lang/Kernal/kernal.el 110:14
// Reserving r1
// Register r1 already reserved
LOAD r1 &Kernal.processReadyQueue
// Reserving r2
// Releasing r2
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_23 // processReadyQueue[0] == nullptr
// Releasing r1
#line run/lang/Kernal/kernal.el 111:18
#alias r1 this // Reserving r1
LOAD this &Kernal.processReadyQueueLock
// INLINE START Mutex.release
STORE BYTE 0x0 this
// INLINE END
STACK POP r0
#alias clear this // Releasing r1 // processReadyQueueLock.release()
//  processReadyQueueLock.release();

#line run/lang/Kernal/kernal.el 113:18
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

#line run/lang/Kernal/kernal.el 114:18
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

#line run/lang/Kernal/kernal.el 115:18
// Reserving r1
ADD r1 r15 8
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 84
STORE BYTE 3 r1
// Releasing r1
//  kProc.status = ProcessStatus.RUNNING;

#line run/lang/Kernal/kernal.el 116:18
INTERRUPT RET // SysD.interruptReturn()
//  SysD.interruptReturn();

#line run/lang/Kernal/kernal.el 117:18
GOTO :func_exit_Kernal._interrupt
//  return;

#lineend
STACK DEC 4
// End of scope
#stackVarClear kProc
:if_end_23
//  if(processReadyQueue[0] == nullptr) {processReadyQueueLock.release(); ProcessState & kProc = & processStates[1]; kProc.setInterrupt(); kProc.status = ProcessStatus.RUNNING; SysD.interruptReturn(); return;}

#line run/lang/Kernal/kernal.el 119:14
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

#line run/lang/Kernal/kernal.el 120:14
#line run/lang/Kernal/kernal.el 121:18
LOAD r1 $Kernal.processReadyQueue
#line run/lang/Kernal/kernal.el 122:18
ADD r2 r1 4
#line run/lang/Kernal/kernal.el 123:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 1 -> 0
#line run/lang/Kernal/kernal.el 124:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 2 -> 1
#line run/lang/Kernal/kernal.el 125:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 3 -> 2
#line run/lang/Kernal/kernal.el 126:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 4 -> 3
#line run/lang/Kernal/kernal.el 127:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 5 -> 4
#line run/lang/Kernal/kernal.el 128:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 6 -> 5
#line run/lang/Kernal/kernal.el 129:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 7 -> 6
#line run/lang/Kernal/kernal.el 130:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 8 -> 7
#line run/lang/Kernal/kernal.el 131:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 9 -> 8
#line run/lang/Kernal/kernal.el 132:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 10 -> 9
#line run/lang/Kernal/kernal.el 133:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 11 -> 10
#line run/lang/Kernal/kernal.el 134:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 12 -> 11
#line run/lang/Kernal/kernal.el 135:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 13 -> 12
#line run/lang/Kernal/kernal.el 136:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 14 -> 13
#line run/lang/Kernal/kernal.el 137:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 15 -> 14
#line run/lang/Kernal/kernal.el 138:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 16 -> 15
#line run/lang/Kernal/kernal.el 139:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 17 -> 16
#line run/lang/Kernal/kernal.el 140:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 18 -> 17
#line run/lang/Kernal/kernal.el 141:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 19 -> 18
#line run/lang/Kernal/kernal.el 142:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 20 -> 19
#line run/lang/Kernal/kernal.el 143:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 21 -> 20
#line run/lang/Kernal/kernal.el 144:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 22 -> 21
#line run/lang/Kernal/kernal.el 145:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 23 -> 22
#line run/lang/Kernal/kernal.el 146:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 24 -> 23
#line run/lang/Kernal/kernal.el 147:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 25 -> 24
#line run/lang/Kernal/kernal.el 148:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 26 -> 25
#line run/lang/Kernal/kernal.el 149:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 27 -> 26
#line run/lang/Kernal/kernal.el 150:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 28 -> 27
#line run/lang/Kernal/kernal.el 151:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 29 -> 28
#line run/lang/Kernal/kernal.el 152:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 30 -> 29
#line run/lang/Kernal/kernal.el 153:18
COPY MEM WORD r2 r1 INC_RS INC_RD // 31 -> 30
#line run/lang/Kernal/kernal.el 154:18
STORE WORD r1 0 // 31
//  asm{\nLOAD r1 $Kernal.processReadyQueue\nADD r2 r1 4\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 1 -> 0\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 2 -> 1\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 3 -> 2\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 4 -> 3\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 5 -> 4\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 6 -> 5\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 7 -> 6\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 8 -> 7\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 9 -> 8\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 10 -> 9\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 11 -> 10\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 12 -> 11\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 13 -> 12\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 14 -> 13\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 15 -> 14\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 16 -> 15\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 17 -> 16\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 18 -> 17\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 19 -> 18\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 20 -> 19\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 21 -> 20\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 22 -> 21\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 23 -> 22\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 24 -> 23\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 25 -> 24\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 26 -> 25\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 27 -> 26\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 28 -> 27\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 29 -> 28\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 30 -> 29\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 31 -> 30\nSTORE WORD r1 0 // 31\n}

#line run/lang/Kernal/kernal.el 156:14
#alias r1 this // Reserving r1
LOAD this &Kernal.processReadyQueueLock
// INLINE START Mutex.release
STORE BYTE 0x0 this
// INLINE END
STACK POP r0
#alias clear this // Releasing r1 // processReadyQueueLock.release()
//  processReadyQueueLock.release();

#line run/lang/Kernal/kernal.el 158:14
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

#line run/lang/Kernal/kernal.el 159:14
// Reserving r1
ADD r1 r15 8
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 84
STORE BYTE 3 r1
// Releasing r1
//  newProc.status = ProcessStatus.RUNNING;

#line run/lang/Kernal/kernal.el 161:14
INTERRUPT RET // SysD.interruptReturn()
//  SysD.interruptReturn();

#line run/lang/Kernal/kernal.el 162:14
GOTO :func_exit_Kernal._interrupt
//  return;

#lineend
STACK DEC 8
// End of scope
#stackVarClear oldState
#stackVarClear newProc
:if_end_19
//  if(code & 0xffff_fff0 == 0x9000_0000) {ProcessState & oldState = & processStates[SysD.rPIDI]; oldState.updateInterrupt(); if(code == 0x9000_0001) {if(oldState.status == ProcessStatus.RUNNING) {oldState.status = ProcessStatus.READY;}} if(code == 0x9000_0002) {oldState.status = ProcessStatus.DEAD;} processReadyQueueLock.acquire(); if(processReadyQueue[0] == nullptr) {processReadyQueueLock.release(); ProcessState & kProc = & processStates[1]; kProc.setInterrupt(); kProc.status = ProcessStatus.RUNNING; SysD.interruptReturn(); return;} ProcessState* newProc = processReadyQueue[0]; asm{\nLOAD r1 $Kernal.processReadyQueue\nADD r2 r1 4\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 1 -> 0\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 2 -> 1\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 3 -> 2\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 4 -> 3\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 5 -> 4\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 6 -> 5\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 7 -> 6\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 8 -> 7\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 9 -> 8\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 10 -> 9\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 11 -> 10\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 12 -> 11\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 13 -> 12\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 14 -> 13\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 15 -> 14\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 16 -> 15\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 17 -> 16\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 18 -> 17\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 19 -> 18\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 20 -> 19\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 21 -> 20\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 22 -> 21\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 23 -> 22\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 24 -> 23\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 25 -> 24\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 26 -> 25\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 27 -> 26\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 28 -> 27\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 29 -> 28\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 30 -> 29\nCOPY MEM WORD r2 r1 INC_RS INC_RD // 31 -> 30\nSTORE WORD r1 0 // 31\n} processReadyQueueLock.release(); newProc.setInterrupt(); newProc.status = ProcessStatus.RUNNING; SysD.interruptReturn(); return;}

#line run/lang/Kernal/kernal.el 165:10
INTERRUPT RET // SysD.interruptReturn()
//  SysD.interruptReturn();

#lineend
:func_exit_Kernal._interrupt
COPY r15 rStack
STACK POP r15
INTERRUPT RET
#endfunction void

#function Kernal.printStr_char* str char*
STACK PUSH r15
COPY rStack r15
#stackVar char* str -12
#line run/lang/Kernal/console.el 19:10
#line run/lang/Kernal/console.el 20:14
SUB r1 r15 12
#line run/lang/Kernal/console.el 21:14
LOAD MEM r1 r1 // str
#line run/lang/Kernal/console.el 22:14
:printStr_l1
#line run/lang/Kernal/console.el 23:18
LOAD MEM BYTE r2 r1
#line run/lang/Kernal/console.el 24:18
GOTO EQ r2 :printStr_l1_exit
 
#line run/lang/Kernal/console.el 26:18
STORE BYTE r2 Kernal.CONSOLE_OUT
#line run/lang/Kernal/console.el 27:18
INC r1 1
#line run/lang/Kernal/console.el 28:18
GOTO :printStr_l1
#line run/lang/Kernal/console.el 29:14
:printStr_l1_exit
//  asm{\nSUB r1 r15 12\nLOAD MEM r1 r1 // str\n:printStr_l1\nLOAD MEM BYTE r2 r1\nGOTO EQ r2 :printStr_l1_exit\n\nSTORE BYTE r2 Kernal.CONSOLE_OUT\nINC r1 1\nGOTO :printStr_l1\n:printStr_l1_exit\n}

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
#line run/lang/Kernal/console.el 34:10
#line run/lang/Kernal/console.el 35:14
SUB r14 r15 12
#line run/lang/Kernal/console.el 36:14
LOAD MEM r14 r14 // len
#line run/lang/Kernal/console.el 37:14
LOAD r1 Kernal.CONSOLE_OUT // consolePntr
#line run/lang/Kernal/console.el 38:14
SUB r2 r15 16
#line run/lang/Kernal/console.el 39:14
LOAD MEM r2 r2 // str
#line run/lang/Kernal/console.el 40:14
:printStr_len
#line run/lang/Kernal/console.el 41:18
COPY MEM BYTE r2 r1 INC_RS
#line run/lang/Kernal/console.el 42:18
INC r14 -1
#line run/lang/Kernal/console.el 43:18
GOTO GT r14 :printStr_len
//  asm{\nSUB r14 r15 12\nLOAD MEM r14 r14 // len\nLOAD r1 Kernal.CONSOLE_OUT // consolePntr\nSUB r2 r15 16\nLOAD MEM r2 r2 // str\n:printStr_len\nCOPY MEM BYTE r2 r1 INC_RS\nINC r14 -1\nGOTO GT r14 :printStr_len\n}

#lineend
:func_exit_Kernal.printStr_char*_int32
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.printChar_char c char
STACK PUSH r15
COPY rStack r15
#stackVar char c -9
#line run/lang/Kernal/console.el 10:10
#line run/lang/Kernal/console.el 11:14
SUB r2 r15 12
#line run/lang/Kernal/console.el 12:14
LOAD MEM BYTE r2 r2 // c
 
#line run/lang/Kernal/console.el 14:14
STORE BYTE r2 Kernal.CONSOLE_OUT
//  asm{\nSUB r2 r15 12\nLOAD MEM BYTE r2 r2 // c\n\nSTORE BYTE r2 Kernal.CONSOLE_OUT\n}

#lineend
:func_exit_Kernal.printChar_char
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Kernal.intToHex_int32_char* value int32, str char*
STACK PUSH r15
COPY rStack r15
#stackVar char* str -12
#stackVar int32 value -16
#line run/lang/Kernal/console.el 48:10
#line run/lang/Kernal/console.el 49:14
LOAD r14 7
#line run/lang/Kernal/console.el 50:14
SUB r1 r15 16
#line run/lang/Kernal/console.el 51:14
LOAD MEM r1 r1 // value
#line run/lang/Kernal/console.el 52:14
SUB r2 r15 12
#line run/lang/Kernal/console.el 53:14
LOAD MEM r2 r2 // str
#line run/lang/Kernal/console.el 54:14
LOAD r3 0xf
#line run/lang/Kernal/console.el 55:14
:intToHex_l1
#line run/lang/Kernal/console.el 56:18
LRT r1 r1 4
#line run/lang/Kernal/console.el 57:18
AND r4 r1 r3
#line run/lang/Kernal/console.el 58:18
SUB r5 r4 0xa
#line run/lang/Kernal/console.el 59:18
GOTO GEQ r5 :intToHex_gt
#line run/lang/Kernal/console.el 60:22
INC r4 0x30
#line run/lang/Kernal/console.el 61:22
STORE BYTE r4 r2 INC_RA
#line run/lang/Kernal/console.el 62:22
GOTO :intToHex_l1_end
#line run/lang/Kernal/console.el 63:18
:intToHex_gt
#line run/lang/Kernal/console.el 64:22
INC r4 0x57
#line run/lang/Kernal/console.el 65:22
STORE BYTE r4 r2 INC_RA
#line run/lang/Kernal/console.el 66:18
:intToHex_l1_end
#line run/lang/Kernal/console.el 67:18
INC r14 -1
#line run/lang/Kernal/console.el 68:18
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
#line run/lang/Kernal/kernal.el 268:14
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

#line run/lang/Kernal/kernal.el 269:14
// Reserving r1
SUB r1 r15 20
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 84
STORE BYTE 1 r1
// Releasing r1
//  state.status = ProcessStatus.SETUP;

#line run/lang/Kernal/kernal.el 270:14
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

#line run/lang/Kernal/kernal.el 271:14
// Reserving r1
SUB r1 r15 20
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 88
STORE 0 r1
// Releasing r1
//  state.interruptHandler = nullptr;

#line run/lang/Kernal/kernal.el 272:14
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
#line run/lang/Kernal/kernal.el 240:14
#line run/lang/Kernal/kernal.el 241:18
COPY r0 r1
#line run/lang/Kernal/kernal.el 242:18
LOAD MEM WORD rPIDI r1 INC_RA
#line run/lang/Kernal/kernal.el 243:18
LOAD MEM WORD rPgmI r1 INC_RA
#line run/lang/Kernal/kernal.el 244:18
LOAD MEM WORD rStackI r1 INC_RA
#line run/lang/Kernal/kernal.el 245:18
LOAD MEM WORD rMemTblI r1 INC_RA
#line run/lang/Kernal/kernal.el 246:18
LOAD MEM WORD rPMI r1 INC_RA
 
#line run/lang/Kernal/kernal.el 248:18
LOAD MEM WORD r0I r1 INC_RA
#line run/lang/Kernal/kernal.el 249:18
LOAD MEM WORD r1I r1 INC_RA
#line run/lang/Kernal/kernal.el 250:18
LOAD MEM WORD r2I r1 INC_RA
#line run/lang/Kernal/kernal.el 251:18
LOAD MEM WORD r3I r1 INC_RA
#line run/lang/Kernal/kernal.el 252:18
LOAD MEM WORD r4I r1 INC_RA
#line run/lang/Kernal/kernal.el 253:18
LOAD MEM WORD r5I r1 INC_RA
#line run/lang/Kernal/kernal.el 254:18
LOAD MEM WORD r6I r1 INC_RA
#line run/lang/Kernal/kernal.el 255:18
LOAD MEM WORD r7I r1 INC_RA
#line run/lang/Kernal/kernal.el 256:18
LOAD MEM WORD r8I r1 INC_RA
#line run/lang/Kernal/kernal.el 257:18
LOAD MEM WORD r9I r1 INC_RA
#line run/lang/Kernal/kernal.el 258:18
LOAD MEM WORD r10I r1 INC_RA
#line run/lang/Kernal/kernal.el 259:18
LOAD MEM WORD r11I r1 INC_RA
#line run/lang/Kernal/kernal.el 260:18
LOAD MEM WORD r12I r1 INC_RA
#line run/lang/Kernal/kernal.el 261:18
LOAD MEM WORD r13I r1 INC_RA
#line run/lang/Kernal/kernal.el 262:18
LOAD MEM WORD r14I r1 INC_RA
#line run/lang/Kernal/kernal.el 263:18
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
#line run/lang/Kernal/kernal.el 214:14
#line run/lang/Kernal/kernal.el 215:18
ADD r1 r0 4 // Offset to pgmPtr instead of PID
#line run/lang/Kernal/kernal.el 216:18
STORE WORD rPgmI r1 INC_RA
#line run/lang/Kernal/kernal.el 217:18
STORE WORD rStackI r1 INC_RA
#line run/lang/Kernal/kernal.el 218:18
STORE WORD rMemTblI r1 INC_RA
#line run/lang/Kernal/kernal.el 219:18
STORE WORD rPMI r1 INC_RA
 
#line run/lang/Kernal/kernal.el 221:18
STORE WORD r0I r1 INC_RA
#line run/lang/Kernal/kernal.el 222:18
STORE WORD r1I r1 INC_RA
#line run/lang/Kernal/kernal.el 223:18
STORE WORD r2I r1 INC_RA
#line run/lang/Kernal/kernal.el 224:18
STORE WORD r3I r1 INC_RA
#line run/lang/Kernal/kernal.el 225:18
STORE WORD r4I r1 INC_RA
#line run/lang/Kernal/kernal.el 226:18
STORE WORD r5I r1 INC_RA
#line run/lang/Kernal/kernal.el 227:18
STORE WORD r6I r1 INC_RA
#line run/lang/Kernal/kernal.el 228:18
STORE WORD r7I r1 INC_RA
#line run/lang/Kernal/kernal.el 229:18
STORE WORD r8I r1 INC_RA
#line run/lang/Kernal/kernal.el 230:18
STORE WORD r9I r1 INC_RA
#line run/lang/Kernal/kernal.el 231:18
STORE WORD r10I r1 INC_RA
#line run/lang/Kernal/kernal.el 232:18
STORE WORD r11I r1 INC_RA
#line run/lang/Kernal/kernal.el 233:18
STORE WORD r12I r1 INC_RA
#line run/lang/Kernal/kernal.el 234:18
STORE WORD r13I r1 INC_RA
#line run/lang/Kernal/kernal.el 235:18
STORE WORD r14I r1 INC_RA
#line run/lang/Kernal/kernal.el 236:18
STORE WORD r15I r1 INC_RA
//  asm{\nADD r1 r0 4 // Offset to pgmPtr instead of PID\nSTORE WORD rPgmI r1 INC_RA\nSTORE WORD rStackI r1 INC_RA\nSTORE WORD rMemTblI r1 INC_RA\nSTORE WORD rPMI r1 INC_RA\n\nSTORE WORD r0I r1 INC_RA\nSTORE WORD r1I r1 INC_RA\nSTORE WORD r2I r1 INC_RA\nSTORE WORD r3I r1 INC_RA\nSTORE WORD r4I r1 INC_RA\nSTORE WORD r5I r1 INC_RA\nSTORE WORD r6I r1 INC_RA\nSTORE WORD r7I r1 INC_RA\nSTORE WORD r8I r1 INC_RA\nSTORE WORD r9I r1 INC_RA\nSTORE WORD r10I r1 INC_RA\nSTORE WORD r11I r1 INC_RA\nSTORE WORD r12I r1 INC_RA\nSTORE WORD r13I r1 INC_RA\nSTORE WORD r14I r1 INC_RA\nSTORE WORD r15I r1 INC_RA\n}

#lineend
:func_exit_Kernal.ProcessState.updateInterrupt
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

// Kernal.ProcessStatus

// Kernal.ProcessFiles

// Ref text

// SysD

// SysD.AddressSpace

// Peripheral

#function Peripheral.command_int32_int32_int32* deviceId int32, cmdSize int32, cmd int32*
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