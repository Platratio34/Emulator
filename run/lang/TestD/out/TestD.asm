// static data
// Kernal
#define Kernal.MAX_BLOCKS 0x0020 int32
#var Kernal.kallocMutex 0x00 Mutex
#define Kernal.pageFreeTable 0x8000 bool*
// Memory
#var Memory.heapStart 0x0002_3000 void*
#var Memory.allocatedBlocks 0x0000 MemoryBlock*
#var Memory.blockFreeList 0x0002_2000 MemoryBlock*
#define Memory.ALLOCATED_BLOCK_LIST 0x0002_2000 int32
// CharacterDisplay
#var CharacterDisplay.width 0x00 int32
#var CharacterDisplay.charBuffer (960) char[960]
#var CharacterDisplay.deviceId 0x0000 int32
#var CharacterDisplay.charColorBuffer (960) char[960]
#var CharacterDisplay.height 0x00 int32
// Console
#define Console.CONSOLE_OUT 0x0001_0300 char*
#define Console.CONSOLE_IN 0x0001_0301 char*
#define Console.CONSOLE_IN_COUNT 0x0001_0302 uint8*
// FS
#var FS.deviceId 0x0000 int32
// TestD
#var TestD.inputBuffer (128) char[128]
#var TestD.tempCmd (64) char[64]
#var TestD.testStr "Test\n" char[5]
#define TestD.KEYBOARD_CONTROL 0x0001_0305 uint8*
#define TestD.KEYBOARD_KEYS 0x0001_0306 char*
#var TestD.tempCmdI 0x00 int32
#define TestD.KEYBOARD_MOD 0x0001_0304 uint8*
#var TestD.tc 0x00 char
#define TestD.str "// Test" char*
#var TestD.path "test.txt\0" char[9]
#define TestD.TIMERS 0x0001_0200 int32*
#var TestD.inputBufferWrite 0x00 int32
#var TestD.v 0x0000 int32
#var TestD.testStr2 "Test2\n\0" char[7]
#var TestD.inputBufferRead 0x00 int32

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

#syscall 2 Kernal_kfree
#function syscall::Kernal_kfree num int32
GOTO PUSH :Kernal.kfree_int32
SYSRETURN
#endfunction void

#function Kernal.kfree_int32 num int32
STACK PUSH r15
COPY rStack r15
#stackVar int32 num -12
#line run\lang\TestD\kalloc.el 45:10
#alias r1 this // Reserving r1
LOAD this &Kernal.kallocMutex
// INLINE START Mutex.acquire
:Mutex.acquire_loop_0
// Resolving placeholder 1 to r2
TEST AND SET r2 this
GOTO NEQ r2 :Mutex.acquire_loop_0
// INLINE END
STACK POP r0
#alias clear this // Releasing r1 // kallocMutex.acquire()
//  kallocMutex.acquire();

#line run\lang\TestD\kalloc.el 46:10
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

#line run\lang\TestD\kalloc.el 47:10
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
// Register r2 already reserved
LOAD MEM r2 r15
SUB r1 r1 r2
GOTO LEQ r1 :if_end_23 // num > cPages
// Releasing r1
#line run\lang\TestD\kalloc.el 48:14
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
:if_end_23
//  if(num > cPages) {num = cPages;}

#line run\lang\TestD\kalloc.el 50:10
:while_condition_24
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
GOTO LEQ r1 :while_end_24 // num > 0
// Releasing r1
#line run\lang\TestD\kalloc.el 51:14
// Register r2 already reserved
COPY r15 r2
LOAD MEM r1 r2
SUB r3 r1 1
STORE r3 r2 // cPages--
//  cPages--;

#line run\lang\TestD\kalloc.el 52:14
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

#line run\lang\TestD\kalloc.el 53:14
SUB r2 r15 12
// Register r2 already reserved
LOAD MEM r1 r2
SUB r3 r1 1
STORE r3 r2 // num--
//  num--;

#lineend
GOTO :while_condition_24
:while_end_24
//  while(num > 0) {cPages--; pageFreeTable[(cast<int32>(SysD.rMemTbl[cPages]) & 0x7fff_ffff) >> 12] = false; num--;}

#line run\lang\TestD\kalloc.el 55:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15 // cPages
STORE r1 rMemTbl
// Releasing r1
//  SysD.rMemTbl[0] = cPages;

#line run\lang\TestD\kalloc.el 56:10
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

#syscall 1 Kernal_kalloc
#function syscall::Kernal_kalloc
GOTO PUSH :Kernal.kalloc
SYSRETURN
#endfunction void*

#function Kernal.kalloc
STACK PUSH r15
COPY rStack r15
#line run\lang\TestD\kalloc.el 18:10
#alias r1 this // Reserving r1
LOAD this &Kernal.kallocMutex
// INLINE START Mutex.acquire
:Mutex.acquire_loop_1
// Resolving placeholder 1 to r2
TEST AND SET r2 this
GOTO NEQ r2 :Mutex.acquire_loop_1
// INLINE END
STACK POP r0
#alias clear this // Releasing r1 // kallocMutex.acquire()
//  kallocMutex.acquire();

#line run\lang\TestD\kalloc.el 19:10
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

#line run\lang\TestD\kalloc.el 20:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 -32
GOTO LT r1 :if_end_25 // cPages >= MAX_BLOCKS
// Releasing r1
#line run\lang\TestD\kalloc.el 21:14
#alias r1 this // Reserving r1
LOAD this &Kernal.kallocMutex
// INLINE START Mutex.release
STORE BYTE 0x0 this
// INLINE END
STACK POP r0
#alias clear this // Releasing r1 // kallocMutex.release()
//  kallocMutex.release();

#line run\lang\TestD\kalloc.el 22:14
// Reserving r1
LOAD r1 0 // nullptr
// Reserving r2
SUB r2 r15 12
STORE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_Kernal.kalloc
//  return nullptr;

#lineend
:if_end_25
//  if(cPages >= MAX_BLOCKS) {kallocMutex.release(); return nullptr;}

#line run\lang\TestD\kalloc.el 24:10
// Reserving r1
// Releasing r1
LOAD r1 0 // 0
#stackVar int32 i
STACK PUSH r1
// Releasing r1
//  int32 i = 0;

#line run\lang\TestD\kalloc.el 25:10
:while_condition_26
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
GOTO EQ r1 :while_end_26
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 -4096
GOTO GEQ r1 :while_end_26 // i < 0x1000 // pageFreeTable[i] && ( i < 0x1000 )
// Releasing r1
#line run\lang\TestD\kalloc.el 26:14
ADD r2 r15 4
// Register r2 already reserved
LOAD MEM r1 r2
ADD r3 r1 1
STORE r3 r2 // i++
//  i++;

#lineend
GOTO :while_condition_26
:while_end_26
//  while(pageFreeTable[i] && (i < 0x1000)) {i++;}

#line run\lang\TestD\kalloc.el 28:10
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 -4096
GOTO NEQ r1 :if_end_27 // i == 0x1000
// Releasing r1
#line run\lang\TestD\kalloc.el 29:14
#alias r1 this // Reserving r1
LOAD this &Kernal.kallocMutex
// INLINE START Mutex.release
STORE BYTE 0x0 this
// INLINE END
STACK POP r0
#alias clear this // Releasing r1 // kallocMutex.release()
//  kallocMutex.release();

#line run\lang\TestD\kalloc.el 30:14
// Reserving r1
LOAD r1 0 // nullptr
// Reserving r2
SUB r2 r15 12
STORE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_Kernal.kalloc
//  return nullptr;

#lineend
:if_end_27
//  if(i == 0x1000) {kallocMutex.release(); return nullptr;}

#line run\lang\TestD\kalloc.el 32:10
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

#line run\lang\TestD\kalloc.el 33:10
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

#line run\lang\TestD\kalloc.el 34:10
// Reserving r1
LOAD MEM BYTE r1 r15
ADD r1 r1 1
STORE r1 r15
// Releasing r1
// Releasing r1
//  cPages += 1;

#line run\lang\TestD\kalloc.el 35:10
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

#line run\lang\TestD\kalloc.el 36:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15 // cPages
STORE r1 rMemTbl
// Releasing r1
//  SysD.rMemTbl[0] = cPages;

#line run\lang\TestD\kalloc.el 38:10
#alias r1 this // Reserving r1
LOAD this &Kernal.kallocMutex
// INLINE START Mutex.release
STORE BYTE 0x0 this
// INLINE END
STACK POP r0
#alias clear this // Releasing r1 // kallocMutex.release()
//  kallocMutex.release();

#line run\lang\TestD\kalloc.el 39:10
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

// Memory

#function Memory.malloc_int32 size int32
STACK PUSH r15
COPY rStack r15
#stackVar int32 size -12
#line run\lang\TestD\memory.el 18:10
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 -4096
GOTO LEQ r1 :if_end_28 // size > 0x1000
// Releasing r1
#line run\lang\TestD\memory.el 19:14
// Reserving r1
LOAD r1 0 // nullptr
// Reserving r2
SUB r2 r15 16
STORE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_Memory.malloc_int32
//  return nullptr;

#lineend
:if_end_28
//  if(size > 0x1000) {return nullptr;}

#line run\lang\TestD\memory.el 21:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &Memory.blockFreeList
GOTO NEQ r1 :if_end_29 // blockFreeList == nullptr
// Releasing r1
#line run\lang\TestD\memory.el 22:14
// Reserving r1
LOAD r1 0 // nullptr
// Reserving r2
SUB r2 r15 16
STORE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_Memory.malloc_int32
//  return nullptr;

#lineend
:if_end_29
//  if(blockFreeList == nullptr) {return nullptr;}

#line run\lang\TestD\memory.el 24:10
// Reserving r1
// Releasing r1
SUB r1 r15 12
// Reserving r1
LOAD MEM r1 r1
RSH r1 r1 2 // size >> 2
#stackVar int32 wordSize
STACK PUSH r1
// Releasing r1
//  int32 wordSize = size >> 2;

#line run\lang\TestD\memory.el 25:10
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
LOAD r2 3
AND r1 r1 r2 // size & 0x3 != 0
GOTO EQ r1 :if_end_30
// Releasing r1
#line run\lang\TestD\memory.el 26:14
// Register r2 already reserved
COPY r15 r2
LOAD MEM r1 r2
ADD r3 r1 1
STORE r3 r2 // wordSize++
//  wordSize++;

#lineend
:if_end_30
//  if(size & 0x3 != 0) {wordSize++;}

#line run\lang\TestD\memory.el 28:10
// Reserving r1
// Releasing r1
// Reserving r1
LOAD MEM r1 &Memory.allocatedBlocks // allocatedBlocks
#stackVar MemoryBlock* block
STACK PUSH r1
// Releasing r1
//  MemoryBlock* block = allocatedBlocks;

#line run\lang\TestD\memory.el 29:10
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_31 // block == nullptr
// Releasing r1
#line run\lang\TestD\memory.el 30:14
// Reserving r1
// Releasing r1
// Reserving r1
LOAD MEM r1 &Memory.blockFreeList // blockFreeList
#stackVar MemoryBlock* next
STACK PUSH r1
// Releasing r1
//  MemoryBlock* next = blockFreeList;

#line run\lang\TestD\memory.el 31:14
// Reserving r1
// Register r1 already reserved
LOAD r1 &Memory.blockFreeList
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 &Memory.blockFreeList
LOAD MEM r2 r2 // blockFreeList.next
STORE r2 r1
// Releasing r1
// Releasing r2
//  blockFreeList = blockFreeList.next;

#line run\lang\TestD\memory.el 32:14
// Reserving r1
ADD r1 r15 8
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 4
// Reserving r2
ADD r2 r15 4
// Register r2 already reserved
LOAD MEM r2 r2
INC r2 8
LOAD MEM r2 r2
INC r2 4 // block.end + 1
STORE r2 r1
// Releasing r1
// Releasing r2
//  next.start = block.end + 1;

#line run\lang\TestD\memory.el 33:14
// Reserving r1
ADD r1 r15 8
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 8
// Reserving r2
ADD r2 r15 8
// Register r2 already reserved
LOAD MEM r2 r2
INC r2 4
LOAD MEM r2 r2 // Reserving r3
// Register r3 already reserved
LOAD MEM r3 r15 // Found r4
LOAD r4 4
MUL r3 r3 r4
ADD r2 r2 r3 // Releasing r3
INC r2 -4 // next.start + wordSize - 1
STORE r2 r1
// Releasing r1
// Releasing r2
//  next.end = next.start + wordSize - 1;

#line run\lang\TestD\memory.el 34:14
// Reserving r1
ADD r1 r15 8
// Register r1 already reserved
LOAD MEM r1 r1
STORE 0 r1
// Releasing r1
//  next.next = nullptr;

#line run\lang\TestD\memory.el 35:14
// Reserving r1
// Register r1 already reserved
LOAD r1 &Memory.allocatedBlocks
// Reserving r2
ADD r2 r15 8
// Register r2 already reserved
LOAD MEM r2 r2 // next
STORE r2 r1
// Releasing r1
// Releasing r2
//  allocatedBlocks = next;

#line run\lang\TestD\memory.el 36:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &Memory.heapStart // heapStart
// Reserving r2
SUB r2 r15 16
STORE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_Memory.malloc_int32
//  return heapStart;

#lineend
STACK DEC 4
// End of scope
#stackVarClear next
:if_end_31
//  if(block == nullptr) {MemoryBlock* next = blockFreeList; blockFreeList = blockFreeList.next; next.start = block.end + 1; next.end = next.start + wordSize - 1; next.next = nullptr; allocatedBlocks = next; return heapStart;}

#line run\lang\TestD\memory.el 38:10
// Reserving r1
// Releasing r1
// Reserving r1
LOAD MEM r1 &Memory.heapStart // heapStart
#stackVar void* lastEnd
STACK PUSH r1
// Releasing r1
//  void* lastEnd = heapStart;

#line run\lang\TestD\memory.el 39:10
:while_condition_32
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
LOAD MEM r1 r1 // block.next != nullptr
GOTO EQ r1 :while_end_32
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 4
LOAD MEM r1 r1
ADD r2 r15 8
// Register r2 already reserved
LOAD MEM r2 r2
SUB r1 r1 r2 // block.start - lastEnd
SUB r2 r15 12
// Register r2 already reserved
LOAD MEM r2 r2
SUB r1 r1 r2
GOTO LT r1 :while_end_32 // ( block.start - lastEnd ) >= size // ( block.next != nullptr ) && ( ( block.start - lastEnd ) >= size )
// Releasing r1
#line run\lang\TestD\memory.el 40:14
// Reserving r1
ADD r1 r15 8
// Register r1 already reserved
// Reserving r2
ADD r2 r15 4
// Register r2 already reserved
LOAD MEM r2 r2
INC r2 8
LOAD MEM r2 r2 // block.end
STORE r2 r1
// Releasing r1
// Releasing r2
//  lastEnd = block.end;

#line run\lang\TestD\memory.el 41:14
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
// Reserving r2
ADD r2 r15 4
// Register r2 already reserved
LOAD MEM r2 r2
LOAD MEM r2 r2 // block.next
STORE r2 r1
// Releasing r1
// Releasing r2
//  block = block.next;

#lineend
GOTO :while_condition_32
:while_end_32
//  while((block.next != nullptr) && ((block.start - lastEnd) >= size)) {lastEnd = block.end; block = block.next;}

#line run\lang\TestD\memory.el 43:10
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
LOAD MEM r1 r1
GOTO NEQ r1 :if_end_33 // block.next == nullptr
// Releasing r1
#line run\lang\TestD\memory.el 44:14
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 8
LOAD MEM r1 r1 // Reserving r2
SUB r2 r15 12
// Register r2 already reserved
LOAD MEM r2 r2 // Found r3
LOAD r3 4
MUL r2 r2 r3
ADD r1 r1 r2 // Releasing r2
INC r1 -4
LOAD r2 196608
SUB r1 r1 r2
GOTO LEQ r1 :if_end_34 // block.end + size - 1 > 0x3_0000
// Releasing r1
#line run\lang\TestD\memory.el 45:18
// Reserving r1
LOAD r1 0 // nullptr
// Reserving r2
SUB r2 r15 16
STORE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_Memory.malloc_int32
//  return nullptr;

#lineend
:if_end_34
//  if(block.end + size - 1 > 0x3_0000) {return nullptr;}

#line run\lang\TestD\memory.el 47:14
// Reserving r1
// Releasing r1
// Reserving r1
LOAD MEM r1 &Memory.blockFreeList // blockFreeList
#stackVar MemoryBlock* next
STACK PUSH r1
// Releasing r1
//  MemoryBlock* next = blockFreeList;

#line run\lang\TestD\memory.el 48:14
// Reserving r1
// Register r1 already reserved
LOAD r1 &Memory.blockFreeList
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 &Memory.blockFreeList
LOAD MEM r2 r2 // blockFreeList.next
STORE r2 r1
// Releasing r1
// Releasing r2
//  blockFreeList = blockFreeList.next;

#line run\lang\TestD\memory.el 49:14
// Reserving r1
ADD r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 4
// Reserving r2
ADD r2 r15 4
// Register r2 already reserved
LOAD MEM r2 r2
INC r2 8
LOAD MEM r2 r2
INC r2 4 // block.end + 1
STORE r2 r1
// Releasing r1
// Releasing r2
//  next.start = block.end + 1;

#line run\lang\TestD\memory.el 50:14
// Reserving r1
ADD r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 8
// Reserving r2
ADD r2 r15 12
// Register r2 already reserved
LOAD MEM r2 r2
INC r2 4
LOAD MEM r2 r2 // Reserving r3
// Register r3 already reserved
LOAD MEM r3 r15 // Found r4
LOAD r4 4
MUL r3 r3 r4
ADD r2 r2 r3 // Releasing r3
INC r2 -4 // next.start + wordSize - 1
STORE r2 r1
// Releasing r1
// Releasing r2
//  next.end = next.start + wordSize - 1;

#line run\lang\TestD\memory.el 51:14
// Reserving r1
ADD r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
STORE 0 r1
// Releasing r1
//  next.next = nullptr;

#line run\lang\TestD\memory.el 52:14
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
// Reserving r2
ADD r2 r15 12
// Register r2 already reserved
LOAD MEM r2 r2 // next
STORE r2 r1
// Releasing r1
// Releasing r2
//  block.next = next;

#line run\lang\TestD\memory.el 53:14
// Reserving r1
ADD r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 4
LOAD MEM r1 r1 // next.start
// Reserving r2
SUB r2 r15 16
STORE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_Memory.malloc_int32
//  return next.start;

#lineend
STACK DEC 4
// End of scope
#stackVarClear next
:if_end_33
//  if(block.next == nullptr) {if(block.end + size - 1 > 0x3_0000) {return nullptr;} MemoryBlock* next = blockFreeList; blockFreeList = blockFreeList.next; next.start = block.end + 1; next.end = next.start + wordSize - 1; next.next = nullptr; block.next = next; return next.start;}

#line run\lang\TestD\memory.el 55:10
// Reserving r1
// Releasing r1
// Reserving r1
LOAD MEM r1 &Memory.blockFreeList // blockFreeList
#stackVar MemoryBlock* next
STACK PUSH r1
// Releasing r1
//  MemoryBlock* next = blockFreeList;

#line run\lang\TestD\memory.el 56:10
// Reserving r1
// Register r1 already reserved
LOAD r1 &Memory.blockFreeList
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 &Memory.blockFreeList
LOAD MEM r2 r2 // blockFreeList.next
STORE r2 r1
// Releasing r1
// Releasing r2
//  blockFreeList = blockFreeList.next;

#line run\lang\TestD\memory.el 57:10
// Reserving r1
ADD r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 4
// Reserving r2
ADD r2 r15 4
// Register r2 already reserved
LOAD MEM r2 r2
INC r2 8
LOAD MEM r2 r2
INC r2 4 // block.end + 1
STORE r2 r1
// Releasing r1
// Releasing r2
//  next.start = block.end + 1;

#line run\lang\TestD\memory.el 58:10
// Reserving r1
ADD r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 8
// Reserving r2
ADD r2 r15 12
// Register r2 already reserved
LOAD MEM r2 r2
INC r2 4
LOAD MEM r2 r2 // Reserving r3
// Register r3 already reserved
LOAD MEM r3 r15 // Found r4
LOAD r4 4
MUL r3 r3 r4
ADD r2 r2 r3 // Releasing r3
INC r2 -4 // next.start + wordSize - 1
STORE r2 r1
// Releasing r1
// Releasing r2
//  next.end = next.start + wordSize - 1;

#line run\lang\TestD\memory.el 59:10
// Reserving r1
ADD r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
// Reserving r2
ADD r2 r15 4
// Register r2 already reserved
LOAD MEM r2 r2
LOAD MEM r2 r2 // block.next
STORE r2 r1
// Releasing r1
// Releasing r2
//  next.next = block.next;

#line run\lang\TestD\memory.el 60:10
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
// Reserving r2
ADD r2 r15 12
// Register r2 already reserved
LOAD MEM r2 r2 // next
STORE r2 r1
// Releasing r1
// Releasing r2
//  block.next = next;

#line run\lang\TestD\memory.el 61:10
// Reserving r1
ADD r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 4
LOAD MEM r1 r1 // next.start
// Reserving r2
SUB r2 r15 16
STORE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_Memory.malloc_int32
//  return next.start;

#lineend
:func_exit_Memory.malloc_int32
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void*

#function Memory.setup
STACK PUSH r15
COPY rStack r15
#line run\lang\TestD\memory.el 9:10
// Reserving r1
// Releasing r1
// Reserving r1
LOAD MEM r1 &Memory.blockFreeList // blockFreeList
#stackVar MemoryBlock* list
STACK PUSH r1
// Releasing r1
//  MemoryBlock* list = blockFreeList;

#line run\lang\TestD\memory.el 10:10
:while_condition_35
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
// Register r2 already reserved
LOAD MEM r2 &Memory.heapStart
INC r2 -12 // heapStart - 3
SUB r1 r1 r2
GOTO GEQ r1 :while_end_35 // list < ( heapStart - 3 )
// Releasing r1
#line run\lang\TestD\memory.el 11:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 r15
INC r2 12 // list + 1
STORE r2 r1
// Releasing r1
// Releasing r2
//  list.next = list + 1;

#line run\lang\TestD\memory.el 12:14
// Register r2 already reserved
COPY r15 r2
LOAD MEM r1 r2
ADD r3 r1 12
STORE r3 r2 // list++
//  list++;

#lineend
GOTO :while_condition_35
:while_end_35
//  while(list < (heapStart - 3)) {list.next = list + 1; list++;}

#line run\lang\TestD\memory.el 14:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
STORE 0 r1
// Releasing r1
//  list.next = nullptr;

#lineend
:func_exit_Memory.setup
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Memory.free_void* ptr void*
STACK PUSH r15
COPY rStack r15
#stackVar void* ptr -12
#line run\lang\TestD\memory.el 65:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &Memory.allocatedBlocks
GOTO NEQ r1 :if_end_36 // allocatedBlocks == nullptr
// Releasing r1
#line run\lang\TestD\memory.el 66:14
GOTO :func_exit_Memory.free_void*
//  return;

#lineend
:if_end_36
//  if(allocatedBlocks == nullptr) {return;}

#line run\lang\TestD\memory.el 68:10
// Reserving r1
// Releasing r1
// Reserving r1
LOAD MEM r1 &Memory.allocatedBlocks // allocatedBlocks
#stackVar MemoryBlock* block
STACK PUSH r1
// Releasing r1
//  MemoryBlock* block = allocatedBlocks;

#line run\lang\TestD\memory.el 69:10
// Reserving r1
// Releasing r1
LOAD r1 0 // nullptr
#stackVar MemoryBlock* last
STACK PUSH r1
// Releasing r1
//  MemoryBlock* last = nullptr;

#line run\lang\TestD\memory.el 70:10
:while_condition_37
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
LOAD MEM r1 r1 // block.next != nullptr
// Register r2 already reserved
LOAD MEM r2 r15
INC r2 4
LOAD MEM r2 r2
SUB r3 r15 12
// Register r3 already reserved
LOAD MEM r3 r3
SUB r2 r2 r3 // block.start != ptr
AND r1 r1 r2 // ( block.next != nullptr ) & ( block.start != ptr )
GOTO EQ r1 :while_end_37
// Releasing r1
#line run\lang\TestD\memory.el 71:14
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 r15 // block
STORE r2 r1
// Releasing r1
// Releasing r2
//  last = block;

#line run\lang\TestD\memory.el 72:14
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
LOAD MEM r1 r1 // block.next
STORE r1 r15
// Releasing r1
//  block = block.next;

#lineend
GOTO :while_condition_37
:while_end_37
//  while((block.next != nullptr) & (block.start != ptr)) {last = block; block = block.next;}

#line run\lang\TestD\memory.el 74:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 4
LOAD MEM r1 r1
SUB r2 r15 12
// Register r2 already reserved
LOAD MEM r2 r2
SUB r1 r1 r2 // block.start != ptr
GOTO EQ r1 :if_end_38
// Releasing r1
#line run\lang\TestD\memory.el 75:14
GOTO :func_exit_Memory.free_void*
//  return;

#lineend
:if_end_38
//  if(block.start != ptr) {return;}

#line run\lang\TestD\memory.el 77:10
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
GOTO NEQ r1 :if_else_39 // last == nullptr
// Releasing r1
#line run\lang\TestD\memory.el 78:14
// Reserving r1
// Register r1 already reserved
LOAD r1 &Memory.allocatedBlocks
STORE 0 r1
// Releasing r1
//  allocatedBlocks = nullptr;

#lineend
GOTO :if_end_39
:if_else_39
#line run\lang\TestD\memory.el 80:14
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 r15
LOAD MEM r2 r2 // block.next
STORE r2 r1
// Releasing r1
// Releasing r2
//  last.next = block.next;

#lineend
:if_end_39
//  if(last == nullptr) {allocatedBlocks = nullptr;} else {last.next = block.next;}

#line run\lang\TestD\memory.el 82:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 &Memory.blockFreeList // blockFreeList
STORE r2 r1
// Releasing r1
// Releasing r2
//  block.next = blockFreeList;

#line run\lang\TestD\memory.el 83:10
// Reserving r1
// Register r1 already reserved
LOAD r1 &Memory.blockFreeList
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 r15 // block
STORE r2 r1
// Releasing r1
// Releasing r2
//  blockFreeList = block;

#lineend
:func_exit_Memory.free_void*
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

// Memory.MemoryBlock

// CharacterDisplay

#function CharacterDisplay.setup
STACK PUSH r15
COPY rStack r15
#line run\lang\TestD\CharacterDisplay.el 14:10
// Reserving r1
// Register r1 already reserved
LOAD r1 &CharacterDisplay.deviceId
STORE 1 r1
// Releasing r1
//  deviceId = 1;

#line run\lang\TestD\CharacterDisplay.el 15:10
:while_condition_40
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &CharacterDisplay.deviceId
INC r1 -64
GOTO GEQ r1 :while_end_40 // deviceId < 64
LOAD r1 Peripheral.TABLE
// Register r1 already reserved
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 &CharacterDisplay.deviceId // deviceId
LSH r2 r2 2
ADD r1 r1 r2
// Releasing r2
LOAD MEM r1 r1
LOAD r2 16777233
SUB r1 r1 r2 // Peripheral.TABLE[deviceId] != Peripheral.TYPE_DISPLAY_CHARACTER
GOTO EQ r1 :while_end_40 // ( deviceId < 64 ) && ( Peripheral.TABLE[deviceId] != Peripheral.TYPE_DISPLAY_CHARACTER )
// Releasing r1
#line run\lang\TestD\CharacterDisplay.el 16:14
// Register r2 already reserved
LOAD r2 &CharacterDisplay.deviceId
LOAD MEM r1 r2
ADD r3 r1 1
STORE r3 r2 // deviceId++
//  deviceId++;

#lineend
GOTO :while_condition_40
:while_end_40
//  while((deviceId < 64) && (Peripheral.TABLE[deviceId] != Peripheral.TYPE_DISPLAY_CHARACTER)) {deviceId++;}

#line run\lang\TestD\CharacterDisplay.el 18:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &CharacterDisplay.deviceId
INC r1 -64
GOTO NEQ r1 :if_end_41 // deviceId == 64
// Releasing r1
#line run\lang\TestD\CharacterDisplay.el 19:14
// Reserving r1
// Register r1 already reserved
LOAD r1 &CharacterDisplay.deviceId
STORE 0 r1
// Releasing r1
//  deviceId = 0;

#line run\lang\TestD\CharacterDisplay.el 20:14
GOTO :func_exit_CharacterDisplay.setup
//  return;

#lineend
:if_end_41
//  if(deviceId == 64) {deviceId = 0; return;}

#line run\lang\TestD\CharacterDisplay.el 23:10
// Reserving r1
#stackVar int32[2] msg2
LOAD r1 1 // 0x01
STACK PUSH r1
// Register r1 already reserved
LOAD MEM r1 &CharacterDisplay.deviceId // deviceId
STACK PUSH r1
// Releasing r1
//  int32[2] msg2 = {0x01, deviceId};

#line run\lang\TestD\CharacterDisplay.el 24:10
// Reserving r1
LOAD r1 0 // 0
STACK PUSH r1
LOAD r1 2 // 2
STACK PUSH r1
// Register r1 already reserved
COPY r15 r1 // &msg2
STACK PUSH r1
// Releasing r1
GOTO PUSH :Peripheral.command_int32_int32_int32*
STACK DEC 12
// Releasing r1 // Peripheral.command(0, 2, & msg2)
//  Peripheral.command(0, 2, & msg2);

#line run\lang\TestD\CharacterDisplay.el 25:10
// Reserving r1
LOAD r1 Peripheral.RSP_STATUS
// Register r1 already reserved

LOAD MEM BYTE r1 r1
INC r1 -1 // *Peripheral.RSP_STATUS != 0x01
GOTO EQ r1 :if_end_42
// Releasing r1
#line run\lang\TestD\CharacterDisplay.el 26:14
GOTO :func_exit_CharacterDisplay.setup
//  return;

#lineend
:if_end_42
//  if(* Peripheral.RSP_STATUS != 0x01) {return;}

#line run\lang\TestD\CharacterDisplay.el 29:10
// Reserving r1
// Register r1 already reserved
LOAD r1 &CharacterDisplay.width
// Reserving r2
LOAD r2 Peripheral.RSP_DATA
// Register r2 already reserved
// Reserving r3
INC r2 40
// Releasing r3
LOAD MEM r2 r2 // Peripheral.RSP_DATA[10]
STORE r2 r1
// Releasing r1
// Releasing r2
//  width = Peripheral.RSP_DATA[10];

#line run\lang\TestD\CharacterDisplay.el 30:10
// Reserving r1
// Register r1 already reserved
LOAD r1 &CharacterDisplay.height
// Reserving r2
LOAD r2 Peripheral.RSP_DATA
// Register r2 already reserved
// Reserving r3
INC r2 44
// Releasing r3
LOAD MEM r2 r2 // Peripheral.RSP_DATA[11]
STORE r2 r1
// Releasing r1
// Releasing r2
//  height = Peripheral.RSP_DATA[11];

#line run\lang\TestD\CharacterDisplay.el 31:10
// Reserving r1
#stackVar int32[2] msg3
LOAD r1 1 // 0x01
STACK PUSH r1
// Register r1 already reserved
LOAD r1 &CharacterDisplay.charBuffer // &charBuffer // cast<int32>(& charBuffer)
STACK PUSH r1
// Releasing r1
//  int32[2] msg3 = {0x01, cast<int32>(& charBuffer)};

#line run\lang\TestD\CharacterDisplay.el 33:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &CharacterDisplay.deviceId // deviceId
STACK PUSH r1
LOAD r1 2 // 2
STACK PUSH r1
ADD r1 r15 8
// Register r1 already reserved // &msg3
STACK PUSH r1
// Releasing r1
GOTO PUSH :Peripheral.command_int32_int32_int32*
STACK DEC 12
// Releasing r1 // Peripheral.command(deviceId, 2, & msg3)
//  Peripheral.command(deviceId, 2, & msg3);

#lineend
:func_exit_CharacterDisplay.setup
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function CharacterDisplay.write_int32_char index int32, data char
STACK PUSH r15
COPY rStack r15
#stackVar char data -9
#stackVar int32 index -16
#line run\lang\TestD\CharacterDisplay.el 37:10
// Reserving r1
// Register r1 already reserved
LOAD r1 &CharacterDisplay.charBuffer
// Reserving r2
SUB r2 r15 16
// Register r2 already reserved
LOAD MEM r2 r2 // index
ADD r1 r1 r2
// Releasing r2
// Reserving r2
SUB r2 r15 9
// Register r2 already reserved
LOAD MEM BYTE r2 r2 // data
STORE BYTE r2 r1
// Releasing r1
// Releasing r2
//  charBuffer[index] = data;

#lineend
:func_exit_CharacterDisplay.write_int32_char
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function CharacterDisplay.write_int32_int32_char x int32, y int32, data char
STACK PUSH r15
COPY rStack r15
#stackVar char data -9
#stackVar int32 x -20
#stackVar int32 y -16
#line run\lang\TestD\CharacterDisplay.el 41:10
// Reserving r1
// Register r1 already reserved
LOAD r1 &CharacterDisplay.charBuffer
// Reserving r2
SUB r2 r15 20
// Register r2 already reserved
LOAD MEM r2 r2
 // Reserving r3
SUB r3 r15 16
// Register r3 already reserved
LOAD MEM r3 r3
// Reserving r4
// Register r4 already reserved
LOAD MEM r4 &CharacterDisplay.width
MUL r3 r3 r4 // Releasing r4 // y * width
ADD r2 r2 r3  // Releasing r3 // x + ( y * width )
ADD r1 r1 r2
// Releasing r2
// Reserving r2
SUB r2 r15 9
// Register r2 already reserved
LOAD MEM BYTE r2 r2 // data
STORE BYTE r2 r1
// Releasing r1
// Releasing r2
//  charBuffer[x + (y* width)] = data;

#lineend
:func_exit_CharacterDisplay.write_int32_int32_char
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function CharacterDisplay.write_int32_int32_char* x int32, y int32, str char*
STACK PUSH r15
COPY rStack r15
#stackVar char* str -12
#stackVar int32 x -20
#stackVar int32 y -16
#line run\lang\TestD\CharacterDisplay.el 44:10
// Reserving r1
// Releasing r1
LOAD r1 0 // 0
#stackVar int32 i
STACK PUSH r1
// Releasing r1
//  int32 i = 0;

#line run\lang\TestD\CharacterDisplay.el 45:10
:while_condition_43
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 r15 // i
ADD r1 r1 r2
// Releasing r2
LOAD MEM BYTE r1 r1
GOTO EQ r1 :while_end_43
SUB r1 r15 20
// Register r1 already reserved
LOAD MEM r1 r1
// Register r2 already reserved
LOAD MEM r2 &CharacterDisplay.width
SUB r1 r1 r2
GOTO GEQ r1 :while_end_43 // str[i] != \0 && x < width
// Releasing r1
#line run\lang\TestD\CharacterDisplay.el 46:14
// Reserving r1
// Register r1 already reserved
LOAD r1 &CharacterDisplay.charBuffer
// Reserving r2
SUB r2 r15 20
// Register r2 already reserved
LOAD MEM r2 r2
 // Reserving r3
SUB r3 r15 16
// Register r3 already reserved
LOAD MEM r3 r3
// Reserving r4
// Register r4 already reserved
LOAD MEM r4 &CharacterDisplay.width
MUL r3 r3 r4 // Releasing r4 // y * width
ADD r2 r2 r3  // Releasing r3 // x + ( y * width )
ADD r1 r1 r2
// Releasing r2
// Reserving r2
SUB r2 r15 12
// Register r2 already reserved
LOAD MEM r2 r2
// Reserving r3
// Register r3 already reserved
LOAD MEM r3 r15 // i
ADD r2 r2 r3
// Releasing r3
LOAD MEM BYTE r2 r2 // str[i]
STORE BYTE r2 r1
// Releasing r1
// Releasing r2
//  charBuffer[x + (y* width)] = str[i];

#line run\lang\TestD\CharacterDisplay.el 47:14
SUB r2 r15 20
// Register r2 already reserved
LOAD MEM r1 r2
ADD r3 r1 1
STORE r3 r2 // x++
//  x++;

#line run\lang\TestD\CharacterDisplay.el 48:14
// Register r2 already reserved
COPY r15 r2
LOAD MEM r1 r2
ADD r3 r1 1
STORE r3 r2 // i++
//  i++;

#lineend
GOTO :while_condition_43
:while_end_43
//  while(str[i] != '\0' && x < width) {charBuffer[x + (y* width)] = str[i]; x++; i++;}

#lineend
:func_exit_CharacterDisplay.write_int32_int32_char*
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

// CharacterDisplay.DeviceDescriptor

// CharacterDisplay.ListEntry

// Console

#function Console.read_char*_int32 buffer char*, bufferSize int32
STACK PUSH r15
COPY rStack r15
#stackVar char* buffer -16
#stackVar int32 bufferSize -12
#line run\lang\TestD\console.el 102:10
LOAD r1 Console.CONSOLE_IN_COUNT
:read_l0
LOAD MEM BYTE r2 r1
GOTO EQ r2 :read_l0
//  asm("LOAD r1 Console.CONSOLE_IN_COUNT\n:read_l0\nLOAD MEM BYTE r2 r1\nGOTO EQ r2 :read_l0");

#line run\lang\TestD\console.el 103:10
// Reserving r1
// Releasing r1
LOAD r1 Console.CONSOLE_IN_COUNT
// Reserving r1

LOAD MEM BYTE r1 r1 // *CONSOLE_IN_COUNT
#stackVar int32 inCount
STACK PUSH r1
// Releasing r1
//  int32 inCount =* CONSOLE_IN_COUNT;

#line run\lang\TestD\console.el 104:10
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
// Register r2 already reserved
LOAD MEM r2 r15
SUB r1 r1 r2
GOTO GEQ r1 :if_end_44 // bufferSize < inCount
// Releasing r1
#line run\lang\TestD\console.el 105:14
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1 // bufferSize
STORE r1 r15
// Releasing r1
//  inCount = bufferSize;

#lineend
:if_end_44
//  if(bufferSize < inCount) {inCount = bufferSize;}

#line run\lang\TestD\console.el 107:10
// Reserving r1
// Releasing r1
LOAD r1 0 // 0
#stackVar int32 i
STACK PUSH r1
// Releasing r1
//  int32 i = 0;

#line run\lang\TestD\console.el 108:10
:while_condition_45
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
// Register r2 already reserved
LOAD MEM r2 r15
SUB r1 r1 r2
GOTO GEQ r1 :while_end_45 // i < inCount
// Releasing r1
#line run\lang\TestD\console.el 109:14
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
LOAD r2 Console.CONSOLE_IN
// Register r2 already reserved

LOAD MEM BYTE r2 r2 // *CONSOLE_IN
STORE BYTE r2 r1
// Releasing r1
// Releasing r2
//  buffer[i] =* CONSOLE_IN;

#line run\lang\TestD\console.el 111:14
ADD r2 r15 4
// Register r2 already reserved
LOAD MEM r1 r2
ADD r3 r1 1
STORE r3 r2 // i++
//  i++;

#lineend
GOTO :while_condition_45
:while_end_45
//  while(i < inCount) {buffer[i] =* CONSOLE_IN; i++;}

#line run\lang\TestD\console.el 113:10
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
SUB r2 r15 12
// Register r2 already reserved
LOAD MEM r2 r2
SUB r1 r1 r2
GOTO GEQ r1 :if_end_46 // i < bufferSize
// Releasing r1
#line run\lang\TestD\console.el 114:14
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
:if_end_46
//  if(i < bufferSize) {buffer[i] = '\0';}

#lineend
:func_exit_Console.read_char*_int32
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Console.intToDec_int32_char* value int32, str char*
STACK PUSH r15
COPY rStack r15
#stackVar char* str -12
#stackVar int32 value -16
#line run\lang\TestD\console.el 73:10
#line run\lang\TestD\console.el 74:14
SUB r1 r15 16
#line run\lang\TestD\console.el 75:14
LOAD MEM r1 r1 // value
#line run\lang\TestD\console.el 76:14
SUB r2 r15 12
#line run\lang\TestD\console.el 77:14
LOAD MEM r2 r2 // str
#line run\lang\TestD\console.el 78:14
COPY rStack r3
#line run\lang\TestD\console.el 79:14
#stackVar char[16] tempStr
#line run\lang\TestD\console.el 80:14
STACK INC 16 // char* str2
#line run\lang\TestD\console.el 81:14
LOAD r4 10
#line run\lang\TestD\console.el 82:14
LOAD r6 0x0
#line run\lang\TestD\console.el 83:14
:intToDec_l1
#line run\lang\TestD\console.el 84:18
DIV r1 r1 r4
#line run\lang\TestD\console.el 85:18
COPY rAF r7
#line run\lang\TestD\console.el 86:18
ADD r7 r7 0x30
#line run\lang\TestD\console.el 87:18
STORE BYTE r7 r3 INC_RA
#line run\lang\TestD\console.el 88:18
INC r6
#line run\lang\TestD\console.el 89:18
GOTO NEQ r1 :intToDec_l1
#line run\lang\TestD\console.el 90:14
INC r3 -1
#line run\lang\TestD\console.el 91:14
:intToDec_l2
#line run\lang\TestD\console.el 92:18
COPY MEM BYTE r3 r2 INC_RD
#line run\lang\TestD\console.el 93:18
INC r6 -1
#line run\lang\TestD\console.el 94:18
INC r3 -1
#line run\lang\TestD\console.el 95:18
GOTO NEQ r6 :intToDec_l2
#line run\lang\TestD\console.el 96:14
STORE BYTE 0x0 r2
#line run\lang\TestD\console.el 97:14
#stackVarClear tempStr
//  asm{\nSUB r1 r15 16\nLOAD MEM r1 r1 // value\nSUB r2 r15 12\nLOAD MEM r2 r2 // str\nCOPY rStack r3\n#stackVar char[16] tempStr\nSTACK INC 16 // char* str2\nLOAD r4 10\nLOAD r6 0x0\n:intToDec_l1\nDIV r1 r1 r4\nCOPY rAF r7\nADD r7 r7 0x30\nSTORE BYTE r7 r3 INC_RA\nINC r6\nGOTO NEQ r1 :intToDec_l1\nINC r3 -1\n:intToDec_l2\nCOPY MEM BYTE r3 r2 INC_RD\nINC r6 -1\nINC r3 -1\nGOTO NEQ r6 :intToDec_l2\nSTORE BYTE 0x0 r2\n#stackVarClear tempStr\n}

#lineend
:func_exit_Console.intToDec_int32_char*
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Console.printStr_char* str char*
STACK PUSH r15
COPY rStack r15
#stackVar char* str -12
#line run\lang\TestD\console.el 19:10
#line run\lang\TestD\console.el 20:14
SUB r1 r15 12
#line run\lang\TestD\console.el 21:14
LOAD MEM r1 r1 // str
#line run\lang\TestD\console.el 22:14
:printStr_l1
#line run\lang\TestD\console.el 23:18
LOAD MEM BYTE r2 r1
#line run\lang\TestD\console.el 24:18
GOTO EQ r2 :printStr_l1_exit
 
#line run\lang\TestD\console.el 26:18
STORE BYTE r2 Console.CONSOLE_OUT
#line run\lang\TestD\console.el 27:18
INC r1 1
#line run\lang\TestD\console.el 28:18
GOTO :printStr_l1
#line run\lang\TestD\console.el 29:14
:printStr_l1_exit
//  asm{\nSUB r1 r15 12\nLOAD MEM r1 r1 // str\n:printStr_l1\nLOAD MEM BYTE r2 r1\nGOTO EQ r2 :printStr_l1_exit\n\nSTORE BYTE r2 Console.CONSOLE_OUT\nINC r1 1\nGOTO :printStr_l1\n:printStr_l1_exit\n}

#lineend
:func_exit_Console.printStr_char*
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Console.printStr_char*_int32 str char*, len int32
STACK PUSH r15
COPY rStack r15
#stackVar char* str -16
#stackVar int32 len -12
#line run\lang\TestD\console.el 34:10
#line run\lang\TestD\console.el 35:14
SUB r14 r15 12
#line run\lang\TestD\console.el 36:14
LOAD MEM r14 r14 // len
#line run\lang\TestD\console.el 37:14
LOAD r1 Console.CONSOLE_OUT // consolePntr
#line run\lang\TestD\console.el 38:14
SUB r2 r15 16
#line run\lang\TestD\console.el 39:14
LOAD MEM r2 r2 // str
#line run\lang\TestD\console.el 40:14
:printStr_len
#line run\lang\TestD\console.el 41:18
COPY MEM BYTE r2 r1 INC_RS
#line run\lang\TestD\console.el 42:18
INC r14 -1
#line run\lang\TestD\console.el 43:18
GOTO GT r14 :printStr_len
//  asm{\nSUB r14 r15 12\nLOAD MEM r14 r14 // len\nLOAD r1 Console.CONSOLE_OUT // consolePntr\nSUB r2 r15 16\nLOAD MEM r2 r2 // str\n:printStr_len\nCOPY MEM BYTE r2 r1 INC_RS\nINC r14 -1\nGOTO GT r14 :printStr_len\n}

#lineend
:func_exit_Console.printStr_char*_int32
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Console.printChar_char c char
STACK PUSH r15
COPY rStack r15
#stackVar char c -9
#line run\lang\TestD\console.el 10:10
#line run\lang\TestD\console.el 11:14
SUB r2 r15 12
#line run\lang\TestD\console.el 12:14
LOAD MEM BYTE r2 r2 // c
 
#line run\lang\TestD\console.el 14:14
STORE BYTE r2 Console.CONSOLE_OUT
//  asm{\nSUB r2 r15 12\nLOAD MEM BYTE r2 r2 // c\n\nSTORE BYTE r2 Console.CONSOLE_OUT\n}

#lineend
:func_exit_Console.printChar_char
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function Console.intToHex_int32_char* value int32, str char*
STACK PUSH r15
COPY rStack r15
#stackVar char* str -12
#stackVar int32 value -16
#line run\lang\TestD\console.el 48:10
#line run\lang\TestD\console.el 49:14
LOAD r14 7
#line run\lang\TestD\console.el 50:14
SUB r1 r15 16
#line run\lang\TestD\console.el 51:14
LOAD MEM r1 r1 // value
#line run\lang\TestD\console.el 52:14
SUB r2 r15 12
#line run\lang\TestD\console.el 53:14
LOAD MEM r2 r2 // str
#line run\lang\TestD\console.el 54:14
LOAD r3 0xf
#line run\lang\TestD\console.el 55:14
:intToHex_l1
#line run\lang\TestD\console.el 56:18
LRT r1 r1 4
#line run\lang\TestD\console.el 57:18
AND r4 r1 r3
#line run\lang\TestD\console.el 58:18
SUB r5 r4 0xa
#line run\lang\TestD\console.el 59:18
GOTO GEQ r5 :intToHex_gt
#line run\lang\TestD\console.el 60:22
INC r4 0x30
#line run\lang\TestD\console.el 61:22
STORE BYTE r4 r2 INC_RA
#line run\lang\TestD\console.el 62:22
GOTO :intToHex_l1_end
#line run\lang\TestD\console.el 63:18
:intToHex_gt
#line run\lang\TestD\console.el 64:22
INC r4 0x57
#line run\lang\TestD\console.el 65:22
STORE BYTE r4 r2 INC_RA
#line run\lang\TestD\console.el 66:18
:intToHex_l1_end
#line run\lang\TestD\console.el 67:18
INC r14 -1
#line run\lang\TestD\console.el 68:18
GOTO GEQ r14 :intToHex_l1
//  asm{\nLOAD r14 7\nSUB r1 r15 16\nLOAD MEM r1 r1 // value\nSUB r2 r15 12\nLOAD MEM r2 r2 // str\nLOAD r3 0xf\n:intToHex_l1\nLRT r1 r1 4\nAND r4 r1 r3\nSUB r5 r4 0xa\nGOTO GEQ r5 :intToHex_gt\nINC r4 0x30\nSTORE BYTE r4 r2 INC_RA\nGOTO :intToHex_l1_end\n:intToHex_gt\nINC r4 0x57\nSTORE BYTE r4 r2 INC_RA\n:intToHex_l1_end\nINC r14 -1\nGOTO GEQ r14 :intToHex_l1\n}

#lineend
:func_exit_Console.intToHex_int32_char*
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

// FS

#function FS.openFile_char*_out_int32&_out_int32& path char*, status out int32&, handle out int32&
STACK PUSH r15
COPY rStack r15
#stackVar char* path -20
#stackVar out int32& handle -12
#stackVar out int32& status -16
#line run\lang\TestD\fs.el 20:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &FS.deviceId
GOTO NEQ r1 :if_end_47 // deviceId == 0
// Releasing r1
#line run\lang\TestD\fs.el 21:14
// Reserving r1
STACK INC 4
// Reserving r2
GOTO PUSH :FS.setup
STACK POP BYTE r1
// Releasing r2
GOTO NEQ r1 :if_end_48 // !setup()
// Releasing r1
#line run\lang\TestD\fs.el 22:18
// Reserving r1
SUB r1 r15 16
// Register r1 already reserved
STORE 255 r1
// Releasing r1
//  status = 0xff;

#line run\lang\TestD\fs.el 23:18
GOTO :func_exit_FS.openFile_char*_out_int32&_out_int32&
//  return;

#lineend
:if_end_48
//  if(! setup()) {status = 0xff; return;}

#lineend
:if_end_47
//  if(deviceId == 0) {if(! setup()) {status = 0xff; return;}}

#line run\lang\TestD\fs.el 26:10
#line run\lang\TestD\fs.el 27:14
LOAD r1 Peripheral.CMD_SIZE
#line run\lang\TestD\fs.el 28:14
STORE 2 r1 INC_RA
 
#line run\lang\TestD\fs.el 30:14
STORE 0x10 r1 INC_RA
#line run\lang\TestD\fs.el 31:14
SUB r2 r15 20
#line run\lang\TestD\fs.el 32:14
COPY MEM r2 r1
 
#line run\lang\TestD\fs.el 34:14
LOAD MEM r1 &FS.deviceId
#line run\lang\TestD\fs.el 35:14
LOAD r2 0x0101_0000
#line run\lang\TestD\fs.el 36:14
OR r1 r1 r2
#line run\lang\TestD\fs.el 37:14
STORE r1 Peripheral.CMD_ADDR
//  asm{\nLOAD r1 Peripheral.CMD_SIZE\nSTORE 2 r1 INC_RA\n\nSTORE 0x10 r1 INC_RA\nSUB r2 r15 20\nCOPY MEM r2 r1\n\nLOAD MEM r1 &FS.deviceId\nLOAD r2 0x0101_0000\nOR r1 r1 r2\nSTORE r1 Peripheral.CMD_ADDR\n}

#line run\lang\TestD\fs.el 40:10
// Reserving r1
SUB r1 r15 16
// Register r1 already reserved
// Reserving r2
LOAD r2 Peripheral.RSP_STATUS
// Register r2 already reserved

LOAD MEM BYTE r2 r2 // *Peripheral.RSP_STATUS
STORE r2 r1
// Releasing r1
// Releasing r2
//  status =* Peripheral.RSP_STATUS;

#line run\lang\TestD\fs.el 41:10
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
// Reserving r2
LOAD r2 Peripheral.RSP_DATA
// Register r2 already reserved
// Reserving r3
INC r2 4
// Releasing r3
LOAD MEM r2 r2 // Peripheral.RSP_DATA[1]
STORE r2 r1
// Releasing r1
// Releasing r2
//  handle = Peripheral.RSP_DATA[1];

#lineend
:func_exit_FS.openFile_char*_out_int32&_out_int32&
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function FS.readFileSync_int32_void*_int32_int32_out_int32&_out_int32& handle int32, buffer void*, size int32, offset int32, read out int32&, state out int32&
STACK PUSH r15
COPY rStack r15
#stackVar out int32& read -16
#stackVar int32 offset -20
#stackVar int32 size -24
#stackVar int32 handle -32
#stackVar out int32& state -12
#stackVar void* buffer -28
#line run\lang\TestD\fs.el 75:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &FS.deviceId
GOTO NEQ r1 :if_end_49 // deviceId == 0
// Releasing r1
#line run\lang\TestD\fs.el 76:14
// Reserving r1
STACK INC 4
// Reserving r2
GOTO PUSH :FS.setup
STACK POP BYTE r1
// Releasing r2
GOTO NEQ r1 :if_end_50 // !setup()
// Releasing r1
#line run\lang\TestD\fs.el 77:18
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
STORE 255 r1
// Releasing r1
//  state = 0xff;

#line run\lang\TestD\fs.el 78:18
GOTO :func_exit_FS.readFileSync_int32_void*_int32_int32_out_int32&_out_int32&
//  return;

#lineend
:if_end_50
//  if(! setup()) {state = 0xff; return;}

#lineend
:if_end_49
//  if(deviceId == 0) {if(! setup()) {state = 0xff; return;}}

#line run\lang\TestD\fs.el 81:10
#line run\lang\TestD\fs.el 82:14
LOAD r1 Peripheral.CMD_SIZE
#line run\lang\TestD\fs.el 83:14
STORE 6 r1 INC_RA
 
#line run\lang\TestD\fs.el 85:14
STORE 0x11 r1 INC_RA
#line run\lang\TestD\fs.el 86:14
SUB r2 r15 32
 
#line run\lang\TestD\fs.el 88:14
COPY MEM r2 r1 INC_RS INC_RD
#line run\lang\TestD\fs.el 89:14
COPY MEM r2 r1 INC_RS INC_RD
#line run\lang\TestD\fs.el 90:14
COPY MEM r2 r1 INC_RS INC_RD
#line run\lang\TestD\fs.el 91:14
COPY MEM r2 r1 INC_RS INC_RD
#line run\lang\TestD\fs.el 92:14
COPY MEM r2 r1 INC_RS INC_RD
 
#line run\lang\TestD\fs.el 94:14
LOAD MEM r1 &FS.deviceId
#line run\lang\TestD\fs.el 95:14
LOAD r2 0x0101_0000
#line run\lang\TestD\fs.el 96:14
OR r1 r1 r2
#line run\lang\TestD\fs.el 97:14
STORE r1 Peripheral.CMD_ADDR
//  asm{\nLOAD r1 Peripheral.CMD_SIZE\nSTORE 6 r1 INC_RA\n\nSTORE 0x11 r1 INC_RA\nSUB r2 r15 32\n\nCOPY MEM r2 r1 INC_RS INC_RD\nCOPY MEM r2 r1 INC_RS INC_RD\nCOPY MEM r2 r1 INC_RS INC_RD\nCOPY MEM r2 r1 INC_RS INC_RD\nCOPY MEM r2 r1 INC_RS INC_RD\n\nLOAD MEM r1 &FS.deviceId\nLOAD r2 0x0101_0000\nOR r1 r1 r2\nSTORE r1 Peripheral.CMD_ADDR\n}

#line run\lang\TestD\fs.el 100:10
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
// Reserving r2
LOAD r2 Peripheral.RSP_DATA
// Register r2 already reserved

LOAD MEM r2 r2 // *Peripheral.RSP_DATA
STORE r2 r1
// Releasing r1
// Releasing r2
//  state =* Peripheral.RSP_DATA;

#line run\lang\TestD\fs.el 102:10
#line run\lang\TestD\fs.el 103:14
SUB r1 r15 16
#line run\lang\TestD\fs.el 104:14
LOAD MEM r1 r1
#line run\lang\TestD\fs.el 105:14
:FS.readFileSync_wait
#line run\lang\TestD\fs.el 106:14
LOAD MEM r2 r1
#line run\lang\TestD\fs.el 107:14
GOTO LT r2 :FS.readFileSync_wait
//  asm{\nSUB r1 r15 16\nLOAD MEM r1 r1\n:FS.readFileSync_wait\nLOAD MEM r2 r1\nGOTO LT r2 :FS.readFileSync_wait\n}

#line run\lang\TestD\fs.el 108:11
// ;

#lineend
:func_exit_FS.readFileSync_int32_void*_int32_int32_out_int32&_out_int32&
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function FS.readFile_int32_void*_int32_int32_int32*_out_int32& handle int32, buffer void*, size int32, offset int32, read int32*, state out int32&
STACK PUSH r15
COPY rStack r15
#stackVar int32* read -16
#stackVar int32 offset -20
#stackVar int32 size -24
#stackVar int32 handle -32
#stackVar out int32& state -12
#stackVar void* buffer -28
#line run\lang\TestD\fs.el 45:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &FS.deviceId
GOTO NEQ r1 :if_end_51 // deviceId == 0
// Releasing r1
#line run\lang\TestD\fs.el 46:14
// Reserving r1
STACK INC 4
// Reserving r2
GOTO PUSH :FS.setup
STACK POP BYTE r1
// Releasing r2
GOTO NEQ r1 :if_end_52 // !setup()
// Releasing r1
#line run\lang\TestD\fs.el 47:18
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
STORE 255 r1
// Releasing r1
//  state = 0xff;

#line run\lang\TestD\fs.el 48:18
GOTO :func_exit_FS.readFile_int32_void*_int32_int32_int32*_out_int32&
//  return;

#lineend
:if_end_52
//  if(! setup()) {state = 0xff; return;}

#lineend
:if_end_51
//  if(deviceId == 0) {if(! setup()) {state = 0xff; return;}}

#line run\lang\TestD\fs.el 51:10
#line run\lang\TestD\fs.el 52:14
LOAD r1 Peripheral.CMD_SIZE
#line run\lang\TestD\fs.el 53:14
STORE 6 r1 INC_RA
 
#line run\lang\TestD\fs.el 55:14
STORE 0x11 r1 INC_RA
 
#line run\lang\TestD\fs.el 57:14
SUB r2 r15 32
 
#line run\lang\TestD\fs.el 59:14
COPY MEM r2 r1 INC_RS INC_RD
#line run\lang\TestD\fs.el 60:14
COPY MEM r2 r1 INC_RS INC_RD
#line run\lang\TestD\fs.el 61:14
COPY MEM r2 r1 INC_RS INC_RD
#line run\lang\TestD\fs.el 62:14
COPY MEM r2 r1 INC_RS INC_RD
#line run\lang\TestD\fs.el 63:14
COPY MEM r2 r1 INC_RS INC_RD
 
#line run\lang\TestD\fs.el 65:14
LOAD MEM r1 &FS.deviceId
#line run\lang\TestD\fs.el 66:14
LOAD r2 0x0101_0000
#line run\lang\TestD\fs.el 67:14
OR r1 r1 r2
#line run\lang\TestD\fs.el 68:14
STORE r1 Peripheral.CMD_ADDR
//  asm{\nLOAD r1 Peripheral.CMD_SIZE\nSTORE 6 r1 INC_RA\n\nSTORE 0x11 r1 INC_RA\n\nSUB r2 r15 32\n\nCOPY MEM r2 r1 INC_RS INC_RD\nCOPY MEM r2 r1 INC_RS INC_RD\nCOPY MEM r2 r1 INC_RS INC_RD\nCOPY MEM r2 r1 INC_RS INC_RD\nCOPY MEM r2 r1 INC_RS INC_RD\n\nLOAD MEM r1 &FS.deviceId\nLOAD r2 0x0101_0000\nOR r1 r1 r2\nSTORE r1 Peripheral.CMD_ADDR\n}

#line run\lang\TestD\fs.el 71:10
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
// Reserving r2
LOAD r2 Peripheral.RSP_DATA
// Register r2 already reserved

LOAD MEM r2 r2 // *Peripheral.RSP_DATA
STORE r2 r1
// Releasing r1
// Releasing r2
//  state =* Peripheral.RSP_DATA;

#lineend
:func_exit_FS.readFile_int32_void*_int32_int32_int32*_out_int32&
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function FS.setup
STACK PUSH r15
COPY rStack r15
#line run\lang\TestD\fs.el 8:10
// Reserving r1
// Register r1 already reserved
LOAD r1 &FS.deviceId
STORE 1 r1
// Releasing r1
//  deviceId = 1;

#line run\lang\TestD\fs.el 9:10
:while_condition_53
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &FS.deviceId
INC r1 -64
GOTO GEQ r1 :while_end_53 // deviceId < 64
LOAD r1 Peripheral.TABLE
// Register r1 already reserved
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 &FS.deviceId // deviceId
LSH r2 r2 2
ADD r1 r1 r2
// Releasing r2
LOAD MEM r1 r1
LOAD r2 16777217
SUB r1 r1 r2 // Peripheral.TABLE[deviceId] != Peripheral.TYPE_STORAGE_VIRTUAL
GOTO EQ r1 :while_end_53 // ( deviceId < 64 ) && ( Peripheral.TABLE[deviceId] != Peripheral.TYPE_STORAGE_VIRTUAL )
// Releasing r1
#line run\lang\TestD\fs.el 10:14
// Register r2 already reserved
LOAD r2 &FS.deviceId
LOAD MEM r1 r2
ADD r3 r1 1
STORE r3 r2 // deviceId++
//  deviceId++;

#lineend
GOTO :while_condition_53
:while_end_53
//  while((deviceId < 64) && (Peripheral.TABLE[deviceId] != Peripheral.TYPE_STORAGE_VIRTUAL)) {deviceId++;}

#line run\lang\TestD\fs.el 12:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &FS.deviceId
INC r1 -64
GOTO NEQ r1 :if_end_54 // deviceId == 64
// Releasing r1
#line run\lang\TestD\fs.el 13:14
// Reserving r1
// Register r1 already reserved
LOAD r1 &FS.deviceId
STORE 0 r1
// Releasing r1
//  deviceId = 0;

#line run\lang\TestD\fs.el 14:14
// Reserving r1
LOAD r1 0 // false
// Reserving r2
SUB r2 r15 12
STORE BYTE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_FS.setup
//  return false;

#lineend
:if_end_54
//  if(deviceId == 64) {deviceId = 0; return false;}

#line run\lang\TestD\fs.el 16:10
// Reserving r1
LOAD r1 1 // true
// Reserving r2
SUB r2 r15 12
STORE BYTE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_FS.setup
//  return true;

#lineend
:func_exit_FS.setup
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction bool

// TestD

#function TestD.onInterrupt
STACK PUSH r15
COPY rStack r15
#line run\lang\TestD\testd.el 199:10
// Reserving r1
// Releasing r1
// Reserving r1
COPY rIC r1 // SysD.rIC
#stackVar int32 code
STACK PUSH r1
// Releasing r1
//  int32 code = SysD.rIC;

#line run\lang\TestD\testd.el 200:10
#line run\lang\TestD\testd.el 200:10
LOAD rIC 0
//  asm{LOAD rIC 0}

#line run\lang\TestD\testd.el 201:10
#stackVar char[9] str
STACK INC 12
//  char[9] str;

#line run\lang\TestD\testd.el 202:10
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
// Reserving r2
INC r1 8
// Releasing r2
STORE BYTE 0 r1
// Releasing r1
//  str[8] = '\0';

#line run\lang\TestD\testd.el 203:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
INC r1 -255
GOTO NEQ r1 :if_end_55 // code == 0xff
// Releasing r1
#line run\lang\TestD\testd.el 204:14
// Reserving r1
#define exp_str_inline_0 "\n\nHalting\0"
LOAD r1 &exp_str_inline_0 // \n\nHalting\0
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*
STACK DEC 4
// Releasing r1 // Console.printStr("\n\nHalting\0")
//  Console.printStr("\n\nHalting\0");

#line run\lang\TestD\testd.el 205:14
#line run\lang\TestD\testd.el 205:14
HALT
//  asm{HALT}

#line run\lang\TestD\testd.el 205:23
// ;

#lineend
:if_end_55
//  if(code == 0xff) {Console.printStr("\n\nHalting\0"); asm{HALT};}

#line run\lang\TestD\testd.el 207:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
LOAD r2 -256
AND r1 r1 r2 // code & 0xffff_ff00
LOAD r2 -2147483136
SUB r1 r1 r2
GOTO NEQ r1 :if_end_56 // ( code & 0xffff_ff00 ) == 0x8000_0200
// Releasing r1
#line run\lang\TestD\testd.el 208:14
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

#line run\lang\TestD\testd.el 209:14
// Reserving r1
ADD r1 r15 16
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 -1
GOTO NEQ r1 :if_end_57 // i == 1
// Releasing r1
#line run\lang\TestD\testd.el 211:18
GOTO :func_exit_TestD.onInterrupt
//  return;

#lineend
:if_end_57
//  if(i == 1) {return;}

#line run\lang\TestD\testd.el 214:14
// Reserving r1
#define exp_str_inline_1 "\nTimer \0"
LOAD r1 &exp_str_inline_1 // \nTimer \0
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*
STACK DEC 4
// Releasing r1 // Console.printStr("\nTimer \0")
//  Console.printStr("\nTimer \0");

#line run\lang\TestD\testd.el 215:14
#stackVar char[3] str
STACK INC 4
//  char[3] str;

#line run\lang\TestD\testd.el 216:14
// Reserving r1
ADD r1 r15 16
// Register r1 already reserved
LOAD MEM r1 r1 // i
STACK PUSH r1
ADD r1 r15 20
// Register r1 already reserved // &str
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.intToDec_int32_char*
STACK DEC 8
// Releasing r1 // Console.intToDec(i, & str)
//  Console.intToDec(i, & str);

#line run\lang\TestD\testd.el 217:14
// Reserving r1
ADD r1 r15 20
// Register r1 already reserved // &str
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*
STACK DEC 4
// Releasing r1 // Console.printStr(& str)
//  Console.printStr(& str);

#line run\lang\TestD\testd.el 218:14
// Reserving r1
LOAD r1 0 // 0
STACK PUSH r1
LOAD r1 23 // 23
STACK PUSH r1
#define exp_str_inline_2 "Timer\0"
LOAD r1 &exp_str_inline_2 // Timer\0
STACK PUSH r1
// Releasing r1
GOTO PUSH :CharacterDisplay.write_int32_int32_char*
STACK DEC 12
// Releasing r1 // CharacterDisplay.write(0, 23, "Timer\0")
//  CharacterDisplay.write(0, 23, "Timer\0");

#line run\lang\TestD\testd.el 219:14
GOTO :func_exit_TestD.onInterrupt
//  return;

#lineend
STACK DEC 8
// End of scope
#stackVarClear str
#stackVarClear i
:if_end_56
//  if((code & 0xffff_ff00) == 0x8000_0200) {int32 i = code & 0xff; if(i == 1) {return;} Console.printStr("\nTimer \0"); char[3] str; Console.intToDec(i, & str); Console.printStr(& str); CharacterDisplay.write(0, 23, "Timer\0"); return;}

#line run\lang\TestD\testd.el 221:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15
LOAD r2 -256
AND r1 r1 r2 // code & 0xffff_ff00
LOAD r2 -2147483392
SUB r1 r1 r2
GOTO NEQ r1 :if_end_58 // ( code & 0xffff_ff00 ) == 0x8000_0100
// Releasing r1
#line run\lang\TestD\testd.el 222:14
// Reserving r1
// Releasing r1
// Reserving r1
LOAD MEM r1 r15
LOAD r2 255
AND r1 r1 r2 // code & 0xff
#stackVar char c
STACK PUSH BYTE r1
// Releasing r1
//  char c = code & 0xff;

#line run\lang\TestD\testd.el 223:14
// Reserving r1
ADD r1 r15 16
// Register r1 already reserved
LOAD MEM BYTE r1 r1
INC r1 -10
GOTO NEQ r1 :if_end_59 // c == \n
// Releasing r1
#line run\lang\TestD\testd.el 224:18
// Reserving r1
// Register r1 already reserved
LOAD r1 &TestD.inputBuffer
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 &TestD.inputBufferWrite // inputBufferWrite
ADD r1 r1 r2
// Releasing r2
STORE BYTE 10 r1
// Releasing r1
//  inputBuffer[inputBufferWrite] = '\n';

#line run\lang\TestD\testd.el 225:18
// Reserving r1
// Register r1 already reserved
LOAD r1 &TestD.inputBufferWrite
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 &TestD.inputBufferWrite
INC r2 1 // inputBufferWrite + 1
LOAD r3 127
AND r2 r2 r3 // ( inputBufferWrite + 1 ) & 0x7f
STORE r2 r1
// Releasing r1
// Releasing r2
//  inputBufferWrite = (inputBufferWrite + 1) & 0x7f;

#lineend
:if_end_59
//  if(c == '\n') {inputBuffer[inputBufferWrite] = '\n'; inputBufferWrite = (inputBufferWrite + 1) & 0x7f;}

#line run\lang\TestD\testd.el 227:14
// Reserving r1
ADD r1 r15 16
// Register r1 already reserved
LOAD MEM BYTE r1 r1
INC r1 -32
GOTO LT r1 :exp_ee_0
ADD r1 r15 16
// Register r1 already reserved
LOAD MEM BYTE r1 r1
INC r1 -126
GOTO LEQ r1 :if_end_60
:exp_ee_0 // c <   || c > ~
// Releasing r1
#line run\lang\TestD\testd.el 228:18
GOTO :func_exit_TestD.onInterrupt
//  return;

#lineend
:if_end_60
//  if(c < ' ' || c > '~') {return;}

#line run\lang\TestD\testd.el 230:14
// Reserving r1
// Register r1 already reserved
LOAD r1 &TestD.inputBuffer
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 &TestD.inputBufferWrite // inputBufferWrite
ADD r1 r1 r2
// Releasing r2
// Reserving r2
ADD r2 r15 16
// Register r2 already reserved
LOAD MEM BYTE r2 r2 // c
STORE BYTE r2 r1
// Releasing r1
// Releasing r2
//  inputBuffer[inputBufferWrite] = c;

#line run\lang\TestD\testd.el 231:14
// Reserving r1
// Register r1 already reserved
LOAD r1 &TestD.inputBufferWrite
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 &TestD.inputBufferWrite
INC r2 1 // inputBufferWrite + 1
LOAD r3 127
AND r2 r2 r3 // ( inputBufferWrite + 1 ) & 0x7f
STORE r2 r1
// Releasing r1
// Releasing r2
//  inputBufferWrite = (inputBufferWrite + 1) & 0x7f;

#line run\lang\TestD\testd.el 232:14
GOTO :func_exit_TestD.onInterrupt
//  return;

#lineend
STACK DEC 4
// End of scope
#stackVarClear c
:if_end_58
//  if((code & 0xffff_ff00) == 0x8000_0100) {char c = code & 0xff; if(c == '\n') {inputBuffer[inputBufferWrite] = '\n'; inputBufferWrite = (inputBufferWrite + 1) & 0x7f;} if(c < ' ' || c > '~') {return;} inputBuffer[inputBufferWrite] = c; inputBufferWrite = (inputBufferWrite + 1) & 0x7f; return;}

#line run\lang\TestD\testd.el 234:10
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 r15 // code
STACK PUSH r1
ADD r1 r15 4
// Register r1 already reserved // &str
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.intToHex_int32_char*
STACK DEC 8
// Releasing r1 // Console.intToHex(code, & str)
//  Console.intToHex(code, & str);

#line run\lang\TestD\testd.el 235:10
// Reserving r1
#define exp_str_inline_3 "\nInterrupt: \0"
LOAD r1 &exp_str_inline_3 // \nInterrupt: \0
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*
STACK DEC 4
// Releasing r1 // Console.printStr("\nInterrupt: \0")
//  Console.printStr("\nInterrupt: \0");

#line run\lang\TestD\testd.el 236:10
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved // &str
STACK PUSH r1
LOAD r1 8 // 8
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*_int32
STACK DEC 8
// Releasing r1 // Console.printStr(& str, 8)
//  Console.printStr(& str, 8);

#line run\lang\TestD\testd.el 237:10
// Reserving r1
LOAD r1 '\n' // \n
STACK PUSH BYTE r1
// Releasing r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
// Releasing r1 // Console.printChar('\n')
//  Console.printChar('\n');

#lineend
:func_exit_TestD.onInterrupt
COPY r15 rStack
STACK POP r15
INTERRUPT RET
#endfunction void

#function TestD.wait_int32 time int32
STACK PUSH r15
COPY rStack r15
#stackVar int32 time -12
#line run\lang\TestD\testd.el 244:10
// Reserving r1
// Releasing r1
LOAD r1 Peripheral.TIMERS
// Reserving r1
// Reserving r2
// Releasing r2
LOAD MEM r1 r1
 // Reserving r2
SUB r2 r15 12
// Register r2 already reserved
LOAD MEM r2 r2
ADD r1 r1 r2  // Releasing r2 // Peripheral.TIMERS[0] + time
#stackVar int32 end
STACK PUSH r1
// Releasing r1
//  int32 end = Peripheral.TIMERS[0] + time;

#line run\lang\TestD\testd.el 245:10
:while_condition_61
// Reserving r1
LOAD r1 Peripheral.TIMERS
// Register r1 already reserved
// Reserving r2
// Releasing r2
LOAD MEM r1 r1
// Register r2 already reserved
LOAD MEM r2 r15
SUB r1 r1 r2
GOTO GEQ r1 :while_end_61 // Peripheral.TIMERS[0] < end
// Releasing r1
// 

#lineend
GOTO :while_condition_61
:while_end_61
//  while(Peripheral.TIMERS[0] < end) {}

#lineend
:func_exit_TestD.wait_int32
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function TestD.stringEquals_char*_char* str1 char*, str2 char*
STACK PUSH r15
COPY rStack r15
#stackVar char* str1 -16
#stackVar char* str2 -12
#line run\lang\TestD\testd.el 160:10
#line run\lang\TestD\testd.el 161:14
#alias r1 str1
#line run\lang\TestD\testd.el 162:14
SUB str1 r15 16
#line run\lang\TestD\testd.el 163:14
LOAD MEM str1 str1
 
#line run\lang\TestD\testd.el 165:14
#alias r2 str2
#line run\lang\TestD\testd.el 166:14
SUB str2 r15 12
#line run\lang\TestD\testd.el 167:14
LOAD MEM str2 str2
 
#line run\lang\TestD\testd.el 169:14
// test comment
 
#line run\lang\TestD\testd.el 171:14
#alias r3 c1
#line run\lang\TestD\testd.el 172:14
#alias r4 c2
#line run\lang\TestD\testd.el 173:14
:string_equals_loop
#line run\lang\TestD\testd.el 174:18
LOAD MEM BYTE c1 str1 INC_RA
#line run\lang\TestD\testd.el 175:18
LOAD MEM BYTE c2 str2 INC_RA
 
#line run\lang\TestD\testd.el 177:18
SUB r4 c1 c2
#line run\lang\TestD\testd.el 178:18
GOTO NEQ r4 :string_equals_fail
 
#line run\lang\TestD\testd.el 180:18
GOTO NEQ c1 :string_equals_loop
#line run\lang\TestD\testd.el 181:14
#alias clear str1
#line run\lang\TestD\testd.el 182:14
#alias clear str2
//  asm{\n#alias r1 str1\nSUB str1 r15 16\nLOAD MEM str1 str1\n\n#alias r2 str2\nSUB str2 r15 12\nLOAD MEM str2 str2\n\n// test comment\n\n#alias r3 c1\n#alias r4 c2\n:string_equals_loop\nLOAD MEM BYTE c1 str1 INC_RA\nLOAD MEM BYTE c2 str2 INC_RA\n\nSUB r4 c1 c2\nGOTO NEQ r4 :string_equals_fail\n\nGOTO NEQ c1 :string_equals_loop\n#alias clear str1\n#alias clear str2\n}

#line run\lang\TestD\testd.el 184:10
// Reserving r1
LOAD r1 1 // true
// Reserving r2
SUB r2 r15 20
STORE BYTE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_TestD.stringEquals_char*_char*
//  return true;

#line run\lang\TestD\testd.el 185:10
#line run\lang\TestD\testd.el 185:10
:string_equals_fail
//  asm{:string_equals_fail};

#line run\lang\TestD\testd.el 186:10
// Reserving r1
LOAD r1 0 // false
// Reserving r2
SUB r2 r15 20
STORE BYTE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_TestD.stringEquals_char*_char*
//  return false;

#lineend
:func_exit_TestD.stringEquals_char*_char*
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction bool

#function TestD.mainLoop
STACK PUSH r15
COPY rStack r15
#line run\lang\TestD\testd.el 129:10
:while_condition_62
// Reserving r1
// Register r1 already reserved
LOAD MEM r1 &TestD.inputBufferRead
// Register r2 already reserved
LOAD MEM r2 &TestD.inputBufferWrite
SUB r1 r1 r2 // inputBufferRead != inputBufferWrite
GOTO EQ r1 :while_end_62
// Releasing r1
#line run\lang\TestD\testd.el 130:14
// Reserving r1
// Releasing r1
// Reserving r1
LOAD r1 &TestD.inputBuffer
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 &TestD.inputBufferRead // inputBufferRead
ADD r1 r1 r2
// Releasing r2
LOAD MEM BYTE r1 r1 // inputBuffer[inputBufferRead]
#stackVar char c
STACK PUSH BYTE r1
// Releasing r1
//  char c = inputBuffer[inputBufferRead];

#line run\lang\TestD\testd.el 131:14
// Reserving r1
// Register r1 already reserved
LOAD MEM BYTE r1 r15 // c
STACK PUSH BYTE r1
// Releasing r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
// Releasing r1 // Console.printChar(c)
//  Console.printChar(c);

#line run\lang\TestD\testd.el 132:14
// Reserving r1
// Register r1 already reserved
LOAD r1 &TestD.inputBufferRead
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 &TestD.inputBufferRead
INC r2 1 // inputBufferRead + 1
LOAD r3 127
AND r2 r2 r3 // ( inputBufferRead + 1 ) & 127
STORE r2 r1
// Releasing r1
// Releasing r2
//  inputBufferRead = (inputBufferRead + 1) & 127;

#line run\lang\TestD\testd.el 133:14
// Reserving r1
// Register r1 already reserved
LOAD MEM BYTE r1 r15
INC r1 -10
GOTO NEQ r1 :if_else_63 // c == \n
// Releasing r1
#line run\lang\TestD\testd.el 134:18
// Reserving r1
// Register r1 already reserved
LOAD r1 &TestD.tempCmd
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 &TestD.tempCmdI // tempCmdI
ADD r1 r1 r2
// Releasing r2
STORE BYTE 0 r1
// Releasing r1
//  tempCmd[tempCmdI] = 0;

#line run\lang\TestD\testd.el 135:18
// Reserving r1
STACK INC 4
// Reserving r2
GOTO PUSH :TestD.processCommand
STACK POP BYTE r1
// Releasing r2
GOTO NEQ r1 :if_end_64 // !processCommand()
// Releasing r1
#line run\lang\TestD\testd.el 136:22
// Reserving r1
LOAD r1 0 // false
// Reserving r2
SUB r2 r15 12
STORE BYTE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_TestD.mainLoop
//  return false;

#lineend
:if_end_64
//  if(! processCommand()) {return false;}

#line run\lang\TestD\testd.el 138:18
// Reserving r1
// Register r1 already reserved
LOAD r1 &TestD.tempCmdI
STORE 0 r1
// Releasing r1
//  tempCmdI = 0;

#lineend
GOTO :if_end_63
:if_else_63
#line run\lang\TestD\testd.el 140:18
// Reserving r1
// Register r1 already reserved
LOAD r1 &TestD.tempCmd
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 &TestD.tempCmdI // tempCmdI
ADD r1 r1 r2
// Releasing r2
// Reserving r2
// Register r2 already reserved
LOAD MEM BYTE r2 r15 // c
STORE BYTE r2 r1
// Releasing r1
// Releasing r2
//  tempCmd[tempCmdI] = c;

#line run\lang\TestD\testd.el 141:18
// Reserving r1
// Register r1 already reserved
LOAD r1 &TestD.tempCmdI
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 &TestD.tempCmdI
INC r2 1 // tempCmdI + 1
LOAD r3 63
AND r2 r2 r3 // ( tempCmdI + 1 ) & 63
STORE r2 r1
// Releasing r1
// Releasing r2
//  tempCmdI = (tempCmdI + 1) & 63;

#lineend
:if_end_63
//  if(c == '\n') {tempCmd[tempCmdI] = 0; if(! processCommand()) {return false;} tempCmdI = 0;} else {tempCmd[tempCmdI] = c; tempCmdI = (tempCmdI + 1) & 63;}

#lineend
STACK DEC 4
// End of scope
#stackVarClear c
GOTO :while_condition_62
:while_end_62
//  while(inputBufferRead != inputBufferWrite) {char c = inputBuffer[inputBufferRead]; Console.printChar(c); inputBufferRead = (inputBufferRead + 1) & 127; if(c == '\n') {tempCmd[tempCmdI] = 0; if(! processCommand()) {return false;} tempCmdI = 0;} else {tempCmd[tempCmdI] = c; tempCmdI = (tempCmdI + 1) & 63;}}

#line run\lang\TestD\testd.el 144:10
// Reserving r1
LOAD r1 1 // true
// Reserving r2
SUB r2 r15 12
STORE BYTE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_TestD.mainLoop
//  return true;

#lineend
:func_exit_TestD.mainLoop
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction bool

#function TestD.testRet
STACK PUSH r15
COPY rStack r15
#line run\lang\TestD\testd.el 256:10
// Reserving r1
LOAD r1 2000 // 2000
// Reserving r2
SUB r2 r15 12
STORE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_TestD.testRet
//  return 2000;

#lineend
:func_exit_TestD.testRet
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction int32

:__start
#function TestD.main
STACK PUSH r15
COPY rStack r15
#line run\lang\TestD\testd.el 28:10
#line run\lang\TestD\testd.el 29:14
STORE BYTE 'T' r7
#line run\lang\TestD\testd.el 30:14
STORE BYTE 'e' r7
#line run\lang\TestD\testd.el 31:14
STORE BYTE 's' r7
#line run\lang\TestD\testd.el 32:14
STORE BYTE 't' r7
#line run\lang\TestD\testd.el 33:14
STORE BYTE 'D' r7
#line run\lang\TestD\testd.el 34:14
STORE BYTE '\n' r7
//  asm{\nSTORE BYTE 'T' r7\nSTORE BYTE 'e' r7\nSTORE BYTE 's' r7\nSTORE BYTE 't' r7\nSTORE BYTE 'D' r7\nSTORE BYTE '\n' r7\n}

#line run\lang\TestD\testd.el 36:10
#line run\lang\TestD\testd.el 36:10
LOAD rIH &:TestD.onInterrupt
//  asm{LOAD rIH &:TestD.onInterrupt};

#line run\lang\TestD\testd.el 37:10
// Reserving r1
LOAD r1 TestD.KEYBOARD_CONTROL
// Register r1 already reserved
STORE BYTE 3 r1
// Releasing r1
// * KEYBOARD_CONTROL = 0x03;

#line run\lang\TestD\testd.el 38:10
// Reserving r1
GOTO PUSH :CharacterDisplay.setup
// Releasing r1 // CharacterDisplay.setup()
//  CharacterDisplay.setup();

#line run\lang\TestD\testd.el 39:10
#stackVar int32 b
STACK INC 4
//  int32 b;

#line run\lang\TestD\testd.el 40:10
// Reserving r1
// Releasing r1
// Reserving r1
COPY rPgm r1 // SysD.rPgm
#stackVar int32 a
STACK PUSH r1
// Releasing r1
//  int32 a = SysD.rPgm;

#line run\lang\TestD\testd.el 41:10
// Reserving r1
// Register r1 already reserved
LOAD r1 &TestD.v
// Reserving r2
ADD r2 r15 4
// Register r2 already reserved
LOAD MEM r2 r2 // a
STORE r2 r1
// Releasing r1
// Releasing r2
//  v = a;

#line run\lang\TestD\testd.el 42:10
#stackVar char c
STACK INC 4
//  char c;

#line run\lang\TestD\testd.el 43:10
// Reserving r1
ADD r1 r15 8
// Register r1 already reserved
// Reserving r2
// Register r2 already reserved
LOAD MEM r2 r15 // b
STORE BYTE r2 r1
// Releasing r1
// Releasing r2
//  c = b;

#line run\lang\TestD\testd.el 45:10
// Reserving r1
ADD r1 r15 4
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 1
 // Reserving r2
ADD r2 r15 8
// Register r2 already reserved
LOAD MEM BYTE r2 r2
ADD r1 r1 r2  // Releasing r2 // a + 1 + c
STORE r1 r15
// Releasing r1
//  b = a + 1 + c;

#line run\lang\TestD\testd.el 46:10
// Reserving r1
ADD r1 r15 8
// Register r1 already reserved
STORE BYTE 32 r1
// Releasing r1
//  c = 32;

#line run\lang\TestD\testd.el 47:10
// Reserving r1
ADD r1 r15 8
// Register r1 already reserved
LOAD MEM BYTE r1 r1 // c
STACK PUSH BYTE r1
// Releasing r1
GOTO PUSH :TestD.funcb_int32
STACK DEC 4
// Releasing r1 // funcb(c)
//  funcb(c);

#line run\lang\TestD\testd.el 48:10
#line run\lang\TestD\testd.el 49:14
LOAD r1 64
#line run\lang\TestD\testd.el 50:14
LOAD r2 &TestD.v
#line run\lang\TestD\testd.el 51:14
STORE r1 r2
//  asm{\nLOAD r1 64\nLOAD r2 &TestD.v\nSTORE r1 r2\n}

#line run\lang\TestD\testd.el 53:10
// Test
//  asm(str);

#line run\lang\TestD\testd.el 55:10
#stackVar StructA sA
STACK INC 8
//  StructA sA;

#line run\lang\TestD\testd.el 56:10
// Reserving r1
ADD r1 r15 12
// Register r1 already reserved // &sA
STACK PUSH r1
// Releasing r1
GOTO PUSH :TestD.testA_StructA&
STACK DEC 4
// Releasing r1 // testA(& sA)
//  testA(& sA);

#line run\lang\TestD\testd.el 59:10
// Reserving r1
#define exp_str_inline_4 "Starting EmulatorOS\n\n\0"
LOAD r1 &exp_str_inline_4 // Starting EmulatorOS\n\n\0
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*
STACK DEC 4
// Releasing r1 // Console.printStr("Starting EmulatorOS\n\n\0")
//  Console.printStr("Starting EmulatorOS\n\n\0");

#line run\lang\TestD\testd.el 61:10
// Reserving r1
// Register r1 already reserved
LOAD r1 &TestD.testStr // &testStr
STACK PUSH r1
LOAD r1 5 // 5
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*_int32
STACK DEC 8
// Releasing r1 // Console.printStr(& testStr, 5)
//  Console.printStr(& testStr, 5);

#line run\lang\TestD\testd.el 62:10
// Reserving r1
// Register r1 already reserved
LOAD r1 &TestD.testStr2 // &testStr2
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*
STACK DEC 4
// Releasing r1 // Console.printStr(& testStr2)
//  Console.printStr(& testStr2);

#line run\lang\TestD\testd.el 63:10
// Reserving r1
LOAD r1 'a' // a
STACK PUSH BYTE r1
// Releasing r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
// Releasing r1 // Console.printChar('a')
//  Console.printChar('a');

#line run\lang\TestD\testd.el 64:10
// Reserving r1
LOAD r1 '\n' // \n
STACK PUSH BYTE r1
// Releasing r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
// Releasing r1 // Console.printChar('\n')
//  Console.printChar('\n');

#line run\lang\TestD\testd.el 66:10
#stackVar char[16] str2
STACK INC 16
//  char[16] str2;

#line run\lang\TestD\testd.el 67:10
// Reserving r1
LOAD r1 4096 // 0x1000
STACK PUSH r1
ADD r1 r15 20
// Register r1 already reserved // &str2
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.intToDec_int32_char*
STACK DEC 8
// Releasing r1 // Console.intToDec(0x1000, & str2)
//  Console.intToDec(0x1000, & str2);

#line run\lang\TestD\testd.el 68:10
// Reserving r1
ADD r1 r15 20
// Register r1 already reserved // &str2
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*
STACK DEC 4
// Releasing r1 // Console.printStr(& str2)
//  Console.printStr(& str2);

#line run\lang\TestD\testd.el 69:10
// Reserving r1
LOAD r1 '\n' // \n
STACK PUSH BYTE r1
// Releasing r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
// Releasing r1 // Console.printChar('\n')
//  Console.printChar('\n');

#line run\lang\TestD\testd.el 70:10
// Reserving r1
LOAD r1 'd' // d
STACK PUSH BYTE r1
// Releasing r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
// Releasing r1 // Console.printChar('d')
//  Console.printChar('d');

#line run\lang\TestD\testd.el 71:10
// Reserving r1
LOAD r1 '\n' // \n
STACK PUSH BYTE r1
// Releasing r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
// Releasing r1 // Console.printChar('\n')
//  Console.printChar('\n');

#line run\lang\TestD\testd.el 77:10
#stackVar int32 fh
STACK INC 4
//  int32 fh;

#line run\lang\TestD\testd.el 78:10
#stackVar int32 rstat
STACK INC 4
//  int32 rstat;

#line run\lang\TestD\testd.el 79:10
// Reserving r1
#define exp_str_inline_5 "test.txt\0"
LOAD r1 &exp_str_inline_5 // test.txt\0
STACK PUSH r1
ADD r1 r15 40
// Register r1 already reserved // &rstat
STACK PUSH r1
ADD r1 r15 36
// Register r1 already reserved // &fh
STACK PUSH r1
// Releasing r1
GOTO PUSH :FS.openFile_char*_out_int32&_out_int32&
STACK DEC 12
// Releasing r1 // FS.openFile("test.txt\0", & rstat, & fh)
//  FS.openFile("test.txt\0", & rstat, & fh);

#line run\lang\TestD\testd.el 80:10
// Reserving r1
ADD r1 r15 36
// Register r1 already reserved
LOAD MEM r1 r1
GOTO NEQ r1 :if_else_65 // fh == 0
// Releasing r1
#line run\lang\TestD\testd.el 81:14
// Reserving r1
#define exp_str_inline_6 "ERROR\n\0"
LOAD r1 &exp_str_inline_6 // ERROR\n\0
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*
STACK DEC 4
// Releasing r1 // Console.printStr("ERROR\n\0")
//  Console.printStr("ERROR\n\0");

#line run\lang\TestD\testd.el 82:14
// Reserving r1
ADD r1 r15 40
// Register r1 already reserved
LOAD MEM r1 r1 // rstat
STACK PUSH r1
ADD r1 r15 20
// Register r1 already reserved // &str2
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.intToHex_int32_char*
STACK DEC 8
// Releasing r1 // Console.intToHex(rstat, & str2)
//  Console.intToHex(rstat, & str2);

#line run\lang\TestD\testd.el 83:14
// Reserving r1
ADD r1 r15 20
// Register r1 already reserved // &str2
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*
STACK DEC 4
// Releasing r1 // Console.printStr(& str2)
//  Console.printStr(& str2);

#lineend
GOTO :if_end_65
:if_else_65
#line run\lang\TestD\testd.el 85:14
// Reserving r1
#define exp_str_inline_7 "Opened\n\0"
LOAD r1 &exp_str_inline_7 // Opened\n\0
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*
STACK DEC 4
// Releasing r1 // Console.printStr("Opened\n\0")
//  Console.printStr("Opened\n\0");

#line run\lang\TestD\testd.el 86:14
#stackVar char[32] buffer
STACK INC 32
//  char[32] buffer;

#line run\lang\TestD\testd.el 87:14
#stackVar int32 read
STACK INC 4
//  int32 read;

#line run\lang\TestD\testd.el 88:14
#stackVar int32 state
STACK INC 4
//  int32 state;

#line run\lang\TestD\testd.el 89:14
// Reserving r1
ADD r1 r15 36
// Register r1 already reserved
LOAD MEM r1 r1 // fh
STACK PUSH r1
ADD r1 r15 44
// Register r1 already reserved // &buffer
STACK PUSH r1
LOAD r1 32 // 32
STACK PUSH r1
LOAD r1 0 // 0
STACK PUSH r1
ADD r1 r15 76
// Register r1 already reserved // &read
STACK PUSH r1
ADD r1 r15 80
// Register r1 already reserved // &state
STACK PUSH r1
// Releasing r1
GOTO PUSH :FS.readFileSync_int32_void*_int32_int32_out_int32&_out_int32&
STACK DEC 24
// Releasing r1 // FS.readFileSync(fh, & buffer, 32, 0, & read, & state)
//  FS.readFileSync(fh, & buffer, 32, 0, & read, & state);

#line run\lang\TestD\testd.el 91:14
// Reserving r1
ADD r1 r15 80
// Register r1 already reserved
LOAD MEM r1 r1 // state
STACK PUSH r1
ADD r1 r15 20
// Register r1 already reserved // &str2
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.intToHex_int32_char*
STACK DEC 8
// Releasing r1 // Console.intToHex(state, & str2)
//  Console.intToHex(state, & str2);

#line run\lang\TestD\testd.el 92:14
// Reserving r1
ADD r1 r15 20
// Register r1 already reserved // &str2
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*
STACK DEC 4
// Releasing r1 // Console.printStr(& str2)
//  Console.printStr(& str2);

#line run\lang\TestD\testd.el 95:14
// Reserving r1
ADD r1 r15 76
// Register r1 already reserved
LOAD MEM r1 r1 // read
STACK PUSH r1
ADD r1 r15 20
// Register r1 already reserved // &str2
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.intToHex_int32_char*
STACK DEC 8
// Releasing r1 // Console.intToHex(read, & str2)
//  Console.intToHex(read, & str2);

#line run\lang\TestD\testd.el 96:14
// Reserving r1
ADD r1 r15 20
// Register r1 already reserved // &str2
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*
STACK DEC 4
// Releasing r1 // Console.printStr(& str2)
//  Console.printStr(& str2);

#line run\lang\TestD\testd.el 98:14
// Reserving r1
LOAD r1 '\n' // \n
STACK PUSH BYTE r1
// Releasing r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
// Releasing r1 // Console.printChar('\n')
//  Console.printChar('\n');

#line run\lang\TestD\testd.el 99:14
// Reserving r1
ADD r1 r15 44
// Register r1 already reserved // &buffer
STACK PUSH r1
ADD r1 r15 76
// Register r1 already reserved
LOAD MEM r1 r1 // read
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*_int32
STACK DEC 8
// Releasing r1 // Console.printStr(& buffer, read)
//  Console.printStr(& buffer, read);

#lineend
STACK DEC 40
// End of scope
#stackVarClear read
#stackVarClear buffer
#stackVarClear state
:if_end_65
//  if(fh == 0) {Console.printStr("ERROR\n\0"); Console.intToHex(rstat, & str2); Console.printStr(& str2);} else {Console.printStr("Opened\n\0"); char[32] buffer; int32 read; int32 state; FS.readFileSync(fh, & buffer, 32, 0, & read, & state); Console.intToHex(state, & str2); Console.printStr(& str2); Console.intToHex(read, & str2); Console.printStr(& str2); Console.printChar('\n'); Console.printStr(& buffer, read);}

#line run\lang\TestD\testd.el 109:10
// Reserving r1
// Reserving r2
LOAD r2 Peripheral.TIMERS
// Register r2 already reserved
// Reserving r3
INC r2 60
// Releasing r3
// Register r1 already reserved
LOAD MEM r1 r2
// Reserving r3
LOAD r3 268435456
OR r1 r1 r3
// Releasing r3
STORE r1 r2
// Releasing r2
// Releasing r1
// Releasing r1
//  Peripheral.TIMERS[15] |= 0b01 << 28;

#line run\lang\TestD\testd.el 110:10
// Reserving r1
LOAD r1 Peripheral.TIMERS
// Register r1 already reserved
// Reserving r2
INC r1 4
// Releasing r2
STORE 1000 r1
// Releasing r1
//  Peripheral.TIMERS[1] = 1000;

#line run\lang\TestD\testd.el 112:10
// Reserving r1
LOAD r1 0 // 0
STACK PUSH r1
LOAD r1 0 // 0
STACK PUSH r1
#define exp_str_inline_8 "EmulatorOS\0"
LOAD r1 &exp_str_inline_8 // EmulatorOS\0
STACK PUSH r1
// Releasing r1
GOTO PUSH :CharacterDisplay.write_int32_int32_char*
STACK DEC 12
// Releasing r1 // CharacterDisplay.write(0, 0, "EmulatorOS\0")
//  CharacterDisplay.write(0, 0, "EmulatorOS\0");

#line run\lang\TestD\testd.el 117:10
// Reserving r1
LOAD r1 '>' // >
STACK PUSH BYTE r1
// Releasing r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
// Releasing r1 // Console.printChar('>')
//  Console.printChar('>');

#line run\lang\TestD\testd.el 118:10
:while_condition_66
// Reserving r1
STACK INC 4
// Reserving r2
GOTO PUSH :TestD.mainLoop
STACK POP BYTE r1
// Releasing r2 // mainLoop()
GOTO EQ r1 :while_end_66
// Releasing r1
// 

#lineend
GOTO :while_condition_66
:while_end_66
//  while(mainLoop()) {}

#line run\lang\TestD\testd.el 121:10
STACK INC 4
// Reserving r1
GOTO PUSH :TestD.mainLoop
STACK DEC 4
// Releasing r1 // mainLoop()
//  mainLoop();

#line run\lang\TestD\testd.el 123:10
// Reserving r1
#define exp_str_inline_9 "Stopping...\0"
LOAD r1 &exp_str_inline_9 // Stopping...\0
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*
STACK DEC 4
// Releasing r1 // Console.printStr("Stopping...\0")
//  Console.printStr("Stopping...\0");

#lineend
:func_exit_TestD.main
COPY r15 rStack
STACK POP r15
HALT
#endfunction void

#function TestD.processCommand
STACK PUSH r15
COPY rStack r15
#line run\lang\TestD\testd.el 148:10
// Reserving r1
LOAD r1 ':' // :
STACK PUSH BYTE r1
// Releasing r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
// Releasing r1 // Console.printChar(':')
//  Console.printChar(':');

#line run\lang\TestD\testd.el 149:10
// Reserving r1
// Register r1 already reserved
LOAD r1 &TestD.tempCmd // &tempCmd
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*
STACK DEC 4
// Releasing r1 // Console.printStr(& tempCmd)
//  Console.printStr(& tempCmd);

#line run\lang\TestD\testd.el 150:10
// Reserving r1
LOAD r1 '\n' // \n
STACK PUSH BYTE r1
// Releasing r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
// Releasing r1 // Console.printChar('\n')
//  Console.printChar('\n');

#line run\lang\TestD\testd.el 151:10
#breakpoint
//  @Breakpoint(())

#line run\lang\TestD\testd.el 152:10
// Reserving r1
STACK INC 4
// Reserving r2
// Register r2 already reserved
LOAD r2 &TestD.tempCmd // &tempCmd
STACK PUSH r2
#define exp_str_inline_10 "STOP\0"
LOAD r2 &exp_str_inline_10 // STOP\0
STACK PUSH r2
// Releasing r2
GOTO PUSH :TestD.stringEquals_char*_char*
STACK DEC 8
STACK POP BYTE r1
// Releasing r2 // stringEquals(& tempCmd, "STOP\0")
GOTO EQ r1 :if_end_67
// Releasing r1
#line run\lang\TestD\testd.el 153:14
// Reserving r1
LOAD r1 0 // false
// Reserving r2
SUB r2 r15 12
STORE BYTE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_TestD.processCommand
//  return false;

#lineend
:if_end_67
//  if(stringEquals(& tempCmd, "STOP\0")) {return false;}

#line run\lang\TestD\testd.el 155:10
// Reserving r1
LOAD r1 '>' // >
STACK PUSH BYTE r1
// Releasing r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
// Releasing r1 // Console.printChar('>')
//  Console.printChar('>');

#line run\lang\TestD\testd.el 156:10
// Reserving r1
LOAD r1 1 // true
// Reserving r2
SUB r2 r15 12
STORE BYTE r1 r2
// Releasing r1
// Releasing r2
GOTO :func_exit_TestD.processCommand
//  return true;

#lineend
:func_exit_TestD.processCommand
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction bool

#function TestD.funcb_int32 a int32
STACK PUSH r15
COPY rStack r15
#stackVar int32 a -12
#line run\lang\TestD\testd.el 190:10
// Reserving r1
// Reserving r2
// Register r2 already reserved
LOAD r2 &TestD.v
// Register r1 already reserved
LOAD MEM r1 r2
// Reserving r3
SUB r3 r15 12
// Register r3 already reserved
LOAD MEM r3 r3 // a
ADD r3 r1 r3
STORE r3 r2
// Releasing r2
// Releasing r1
// Releasing r3
//  v += a;

#lineend
:func_exit_TestD.funcb_int32
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function TestD.funcb_int32_int32* a int32, b int32*
STACK PUSH r15
COPY rStack r15
#stackVar int32 a -16
#stackVar int32* b -12
#line run\lang\TestD\testd.el 194:10
// Reserving r1
// Reserving r2
// Register r2 already reserved
LOAD r2 &TestD.v
// Register r1 already reserved
LOAD MEM r1 r2
// Reserving r3
SUB r3 r15 16
// Register r3 already reserved
LOAD MEM r3 r3 // a
ADD r3 r1 r3
STORE r3 r2
// Releasing r2
// Releasing r1
// Releasing r3
//  v += a;

#lineend
:func_exit_TestD.funcb_int32_int32*
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

#function TestD.testA_StructA& str StructA&
STACK PUSH r15
COPY rStack r15
#stackVar StructA& str -12
#line run\lang\TestD\testd.el 251:10
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
STORE 32 r1
// Releasing r1
//  str.a = 32;

#line run\lang\TestD\testd.el 252:10
// Reserving r1
SUB r1 r15 12
// Register r1 already reserved
LOAD MEM r1 r1
INC r1 4
STORE -1 r1
// Releasing r1
//  str.b = 0xffffffff;

#lineend
:func_exit_TestD.testA_StructA&
COPY r15 rStack
STACK POP r15
GOTO POP
#endfunction void

// TestD.StructA

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