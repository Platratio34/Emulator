// static data
// Memory
#var Memory.heapStart 0x0002_3000 void*
#var Memory.allocatedBlocks 0x0000 MemoryBlock*
#var Memory.blockFreeList 0x0002_2000 MemoryBlock*
#define Memory.ALLOCATED_BLOCK_LIST 0x0002_2000 void*
// Console
#define Console.CMD_DEVICE 0x0001_0002 uint32*
#define Console.CONSOLE_OUT 0x0001_0100 char*
#define Console.CMD_WRITTEN 0x0001 uint32
#define Console.CMD_ADDR 0x2710 uint32*
#define Console.CMD_STATUS 0x0001_0001 uint32*
#define Console.CONSOLE_IN 0x0001_0101 char*
#define Console.CONSOLE_IN_COUNT 0x0001_0102 uint8*
#define Console.CMD_START 0x0001_0008 uint32*
#define Console.CMD_SIZE 0x0001_0004 uint32*
// FS
#define FS.CMD_DEVICE 0x0001_0002 uint32*
#define FS.CMD_WRITTEN 0x0001 uint32
#define FS.RSP_DATA_2 0x0001_0088 uint32*
#define FS.RSP_DATA_3 0x0001_008c uint32*
#define FS.CMD_ADDR 0x0001_0000 uint32*
#define FS.RSP_STATUS 0x0001_0080 uint32*
#define FS.CMD_STATUS 0x0001_0001 uint32*
#define FS.RSP_DATA 0x0001_0084 uint32*
#define FS.CMD_START 0x0001_0008 uint32*
#define FS.CMD_SIZE 0x0001_0004 uint32*
// TestD
#define TestD.str "// Test" char*
#var TestD.path "test.txt\0" char[9]
#define TestD.TIMERS 0x0002_0200 uint32*
#var TestD.v 0x0000 uint32
#var TestD.testStr2 "Test2\n\0" char[7]
#var TestD.testStr "Test\n" char[5]
#var TestD.tc 0x00 char

//--------
// text

// Memory

#function Memory.malloc_uint32 size uint32
STACK PUSH r15
COPY rStack r15
#stackVar uint32 size -12
// 0 18:10
#line run\lang\TestD\memory.el 18:10
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
INC r1 -4096
SET FORCE GT r1 r1
GOTO EQ r1 :if_end_0
// 0 19:14
#line run\lang\TestD\memory.el 19:14
LOAD r1 0
COPY r15 r2
INC r2 -16
STORE r1 r2
GOTO :func_exit_Memory.malloc_uint32
//  return nullptr;

#lineend
:if_end_0
//  if(size > 0x1000) {return nullptr;}

// 1 21:10
#line run\lang\TestD\memory.el 21:10
LOAD r1 &Memory.blockFreeList
LOAD MEM r1 r1
LOAD r2 0
SUB r1 r1 r2
SET FORCE EQ r1 r1
GOTO EQ r1 :if_end_1
// 0 22:14
#line run\lang\TestD\memory.el 22:14
LOAD r1 0
COPY r15 r2
INC r2 -16
STORE r1 r2
GOTO :func_exit_Memory.malloc_uint32
//  return nullptr;

#lineend
:if_end_1
//  if(blockFreeList == nullptr) {return nullptr;}

// 2 24:10
#line run\lang\TestD\memory.el 24:10
LOAD r1 &Memory.allocatedBlocks
LOAD MEM r1 r1
#stackVar MemoryBlock* block
STACK PUSH r1
//  MemoryBlock* block = allocatedBlocks;

// 3 25:10
#line run\lang\TestD\memory.el 25:10
COPY r15 r1
LOAD MEM r1 r1
LOAD r2 0
SUB r1 r1 r2
SET FORCE EQ r1 r1
GOTO EQ r1 :if_end_2
// 0 26:14
#line run\lang\TestD\memory.el 26:14
LOAD r1 &Memory.blockFreeList
LOAD MEM r1 r1
#stackVar MemoryBlock* next
STACK PUSH r1
//  MemoryBlock* next = blockFreeList;

// 1 27:14
#line run\lang\TestD\memory.el 27:14
LOAD r1 &Memory.blockFreeList
LOAD r2 &Memory.blockFreeList
LOAD MEM r2 r2
LOAD MEM r2 r2
STORE r2 r1
//  blockFreeList = blockFreeList.next;

// 2 28:14
#line run\lang\TestD\memory.el 28:14
COPY r15 r1
INC r1 4
LOAD MEM r1 r1
COPY r15 r2
LOAD MEM r2 r2
LOAD MEM r2 r2
INC r2 4
STORE r2 r1
//  next.start = block.end + 1;

// 3 29:14
#line run\lang\TestD\memory.el 29:14
COPY r15 r1
INC r1 4
LOAD MEM r1 r1
COPY r15 r2
INC r2 4
LOAD MEM r2 r2
LOAD MEM r2 r2
COPY r15 r3
INC r3 -12
LOAD MEM r3 r3
ADD r2 r2 r3
INC r2 -4
STORE r2 r1
//  next.end = next.start + size - 1;

// 4 30:14
#line run\lang\TestD\memory.el 30:14
COPY r15 r1
INC r1 4
LOAD MEM r1 r1
LOAD r2 0
STORE r2 r1
//  next.next = nullptr;

// 5 31:14
#line run\lang\TestD\memory.el 31:14
LOAD r1 &Memory.allocatedBlocks
COPY r15 r2
INC r2 4
LOAD MEM r2 r2
STORE r2 r1
//  allocatedBlocks = next;

// 6 32:14
#line run\lang\TestD\memory.el 32:14
LOAD r1 &Memory.heapStart
LOAD MEM r1 r1
COPY r15 r2
INC r2 -16
STORE r1 r2
GOTO :func_exit_Memory.malloc_uint32
//  return heapStart;

#lineend
STACK DEC 4
// End of scope
#stackVarClear next
:if_end_2
//  if(block == nullptr) {MemoryBlock* next = blockFreeList; blockFreeList = blockFreeList.next; next.start = block.end + 1; next.end = next.start + size - 1; next.next = nullptr; allocatedBlocks = next; return heapStart;}

// 4 34:10
#line run\lang\TestD\memory.el 34:10
LOAD r1 &Memory.heapStart
LOAD MEM r1 r1
#stackVar void* lastEnd
STACK PUSH r1
//  void* lastEnd = heapStart;

// 5 35:10
#line run\lang\TestD\memory.el 35:10
:while_condition_3
COPY r15 r1
LOAD MEM r1 r1
LOAD MEM r1 r1
LOAD r2 0
SUB r1 r1 r2
SET FORCE NEQ r1 r1
COPY r15 r2
LOAD MEM r2 r2
LOAD MEM r2 r2
COPY r15 r3
INC r3 4
LOAD MEM r3 r3
SUB r2 r2 r3
COPY r15 r3
INC r3 -12
LOAD MEM r3 r3
SUB r2 r2 r3
SET FORCE GEQ r2 r2
AND r1 r1 r2
GOTO EQ r1 :while_end_3
// 0 36:14
#line run\lang\TestD\memory.el 36:14
COPY r15 r1
INC r1 4
COPY r15 r2
LOAD MEM r2 r2
LOAD MEM r2 r2
STORE r2 r1
//  lastEnd = block.end;

// 1 37:14
#line run\lang\TestD\memory.el 37:14
COPY r15 r1
COPY r15 r2
LOAD MEM r2 r2
LOAD MEM r2 r2
STORE r2 r1
//  block = block.next;

#lineend
GOTO :while_condition_3
:while_end_3
//  while((block.next != nullptr) & ((block.start - lastEnd) >= size)) {lastEnd = block.end; block = block.next;}

// 6 39:10
#line run\lang\TestD\memory.el 39:10
COPY r15 r1
LOAD MEM r1 r1
LOAD MEM r1 r1
LOAD r2 0
SUB r1 r1 r2
SET FORCE EQ r1 r1
GOTO EQ r1 :if_end_4
// 0 40:14
#line run\lang\TestD\memory.el 40:14
COPY r15 r1
LOAD MEM r1 r1
LOAD MEM r1 r1
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
ADD r1 r1 r2
INC r1 -4
INC r1 -786432
SET FORCE GT r1 r1
GOTO EQ r1 :if_end_5
// 0 41:18
#line run\lang\TestD\memory.el 41:18
LOAD r1 0
COPY r15 r2
INC r2 -16
STORE r1 r2
GOTO :func_exit_Memory.malloc_uint32
//  return nullptr;

#lineend
:if_end_5
//  if(block.end + size - 1 > 0x3_0000) {return nullptr;}

// 1 43:14
#line run\lang\TestD\memory.el 43:14
LOAD r1 &Memory.blockFreeList
LOAD MEM r1 r1
#stackVar MemoryBlock* next
STACK PUSH r1
//  MemoryBlock* next = blockFreeList;

// 2 44:14
#line run\lang\TestD\memory.el 44:14
LOAD r1 &Memory.blockFreeList
LOAD r2 &Memory.blockFreeList
LOAD MEM r2 r2
LOAD MEM r2 r2
STORE r2 r1
//  blockFreeList = blockFreeList.next;

// 3 45:14
#line run\lang\TestD\memory.el 45:14
COPY r15 r1
INC r1 8
LOAD MEM r1 r1
COPY r15 r2
LOAD MEM r2 r2
LOAD MEM r2 r2
INC r2 4
STORE r2 r1
//  next.start = block.end + 1;

// 4 46:14
#line run\lang\TestD\memory.el 46:14
COPY r15 r1
INC r1 8
LOAD MEM r1 r1
COPY r15 r2
INC r2 8
LOAD MEM r2 r2
LOAD MEM r2 r2
COPY r15 r3
INC r3 -12
LOAD MEM r3 r3
ADD r2 r2 r3
INC r2 -4
STORE r2 r1
//  next.end = next.start + size - 1;

// 5 47:14
#line run\lang\TestD\memory.el 47:14
COPY r15 r1
INC r1 8
LOAD MEM r1 r1
LOAD r2 0
STORE r2 r1
//  next.next = nullptr;

// 6 48:14
#line run\lang\TestD\memory.el 48:14
COPY r15 r1
LOAD MEM r1 r1
COPY r15 r2
INC r2 8
LOAD MEM r2 r2
STORE r2 r1
//  block.next = next;

// 7 49:14
#line run\lang\TestD\memory.el 49:14
COPY r15 r1
INC r1 8
LOAD MEM r1 r1
LOAD MEM r1 r1
COPY r15 r2
INC r2 -16
STORE r1 r2
GOTO :func_exit_Memory.malloc_uint32
//  return next.start;

#lineend
STACK DEC 4
// End of scope
#stackVarClear next
:if_end_4
//  if(block.next == nullptr) {if(block.end + size - 1 > 0x3_0000) {return nullptr;} MemoryBlock* next = blockFreeList; blockFreeList = blockFreeList.next; next.start = block.end + 1; next.end = next.start + size - 1; next.next = nullptr; block.next = next; return next.start;}

// 7 51:10
#line run\lang\TestD\memory.el 51:10
LOAD r1 &Memory.blockFreeList
LOAD MEM r1 r1
#stackVar MemoryBlock* next
STACK PUSH r1
//  MemoryBlock* next = blockFreeList;

// 8 52:10
#line run\lang\TestD\memory.el 52:10
LOAD r1 &Memory.blockFreeList
LOAD r2 &Memory.blockFreeList
LOAD MEM r2 r2
LOAD MEM r2 r2
STORE r2 r1
//  blockFreeList = blockFreeList.next;

// 9 53:10
#line run\lang\TestD\memory.el 53:10
COPY r15 r1
INC r1 8
LOAD MEM r1 r1
COPY r15 r2
LOAD MEM r2 r2
LOAD MEM r2 r2
INC r2 4
STORE r2 r1
//  next.start = block.end + 1;

// 10 54:10
#line run\lang\TestD\memory.el 54:10
COPY r15 r1
INC r1 8
LOAD MEM r1 r1
COPY r15 r2
INC r2 8
LOAD MEM r2 r2
LOAD MEM r2 r2
COPY r15 r3
INC r3 -12
LOAD MEM r3 r3
ADD r2 r2 r3
INC r2 -4
STORE r2 r1
//  next.end = next.start + size - 1;

// 11 55:10
#line run\lang\TestD\memory.el 55:10
COPY r15 r1
INC r1 8
LOAD MEM r1 r1
COPY r15 r2
LOAD MEM r2 r2
LOAD MEM r2 r2
STORE r2 r1
//  next.next = block.next;

// 12 56:10
#line run\lang\TestD\memory.el 56:10
COPY r15 r1
LOAD MEM r1 r1
COPY r15 r2
INC r2 8
LOAD MEM r2 r2
STORE r2 r1
//  block.next = next;

// 13 57:10
#line run\lang\TestD\memory.el 57:10
COPY r15 r1
INC r1 8
LOAD MEM r1 r1
LOAD MEM r1 r1
COPY r15 r2
INC r2 -16
STORE r1 r2
GOTO :func_exit_Memory.malloc_uint32
//  return next.start;

#lineend
:func_exit_Memory.malloc_uint32
STACK DEC 12
// End of scope
#stackVarClear next
#stackVarClear size
#stackVarClear lastEnd
#stackVarClear block
STACK POP r15
GOTO POP
#endfunction void*

#function Memory.setup
STACK PUSH r15
COPY rStack r15
// 0 9:10
#line run\lang\TestD\memory.el 9:10
LOAD r1 &Memory.blockFreeList
LOAD MEM r1 r1
#stackVar MemoryBlock* list
STACK PUSH r1
//  MemoryBlock* list = blockFreeList;

// 1 10:10
#line run\lang\TestD\memory.el 10:10
:while_condition_6
COPY r15 r1
LOAD MEM r1 r1
LOAD r2 &Memory.heapStart
LOAD MEM r2 r2
INC r2 -12
SUB r1 r1 r2
SET FORCE LT r1 r1
GOTO EQ r1 :while_end_6
// 0 11:14
#line run\lang\TestD\memory.el 11:14
COPY r15 r1
LOAD MEM r1 r1
COPY r15 r2
LOAD MEM r2 r2
INC r2 12
STORE r2 r1
//  list.next = list + 1;

// 1 12:14
#line run\lang\TestD\memory.el 12:14
COPY r15 r1
LOAD MEM r2 r1
INC r2 12
STORE r2 r1
//  list++;

#lineend
GOTO :while_condition_6
:while_end_6
//  while(list < (heapStart - 3)) {list.next = list + 1; list++;}

// 2 14:10
#line run\lang\TestD\memory.el 14:10
COPY r15 r1
LOAD MEM r1 r1
LOAD r2 0
STORE r2 r1
//  list.next = nullptr;

#lineend
:func_exit_Memory.setup
STACK DEC 4
// End of scope
#stackVarClear list
STACK POP r15
GOTO POP
#endfunction void

#function Memory.free_void* ptr void*
STACK PUSH r15
COPY rStack r15
#stackVar void* ptr -12
// 0 61:10
#line run\lang\TestD\memory.el 61:10
LOAD r1 &Memory.allocatedBlocks
LOAD MEM r1 r1
LOAD r2 0
SUB r1 r1 r2
SET FORCE EQ r1 r1
GOTO EQ r1 :if_end_7
// 0 62:14
#line run\lang\TestD\memory.el 62:14
GOTO :func_exit_Memory.free_void*
//  return;

#lineend
:if_end_7
//  if(allocatedBlocks == nullptr) {return;}

// 1 64:10
#line run\lang\TestD\memory.el 64:10
LOAD r1 &Memory.allocatedBlocks
LOAD MEM r1 r1
#stackVar MemoryBlock* block
STACK PUSH r1
//  MemoryBlock* block = allocatedBlocks;

// 2 65:10
#line run\lang\TestD\memory.el 65:10
LOAD r1 0
#stackVar MemoryBlock* last
STACK PUSH r1
//  MemoryBlock* last = nullptr;

// 3 66:10
#line run\lang\TestD\memory.el 66:10
:while_condition_8
COPY r15 r1
LOAD MEM r1 r1
LOAD MEM r1 r1
LOAD r2 0
SUB r1 r1 r2
SET FORCE NEQ r1 r1
COPY r15 r2
LOAD MEM r2 r2
LOAD MEM r2 r2
COPY r15 r3
INC r3 -12
LOAD MEM r3 r3
SUB r2 r2 r3
SET FORCE NEQ r2 r2
AND r1 r1 r2
GOTO EQ r1 :while_end_8
// 0 67:14
#line run\lang\TestD\memory.el 67:14
COPY r15 r1
INC r1 4
COPY r15 r2
LOAD MEM r2 r2
STORE r2 r1
//  last = block;

// 1 68:14
#line run\lang\TestD\memory.el 68:14
COPY r15 r1
COPY r15 r2
LOAD MEM r2 r2
LOAD MEM r2 r2
STORE r2 r1
//  block = block.next;

#lineend
GOTO :while_condition_8
:while_end_8
//  while((block.next != nullptr) & (block.start != ptr)) {last = block; block = block.next;}

// 4 70:10
#line run\lang\TestD\memory.el 70:10
COPY r15 r1
LOAD MEM r1 r1
LOAD MEM r1 r1
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
SUB r1 r1 r2
SET FORCE NEQ r1 r1
GOTO EQ r1 :if_end_9
// 0 71:14
#line run\lang\TestD\memory.el 71:14
GOTO :func_exit_Memory.free_void*
//  return;

#lineend
:if_end_9
//  if(block.start != ptr) {return;}

// 5 73:10
#line run\lang\TestD\memory.el 73:10
COPY r15 r1
INC r1 4
LOAD MEM r1 r1
LOAD r2 0
SUB r1 r1 r2
SET FORCE EQ r1 r1
GOTO EQ r1 :if_else_10
// 0 74:14
#line run\lang\TestD\memory.el 74:14
LOAD r1 &Memory.allocatedBlocks
LOAD r2 0
STORE r2 r1
//  allocatedBlocks = nullptr;

#lineend
GOTO :if_end_10
:if_else_10
// 0 76:14
#line run\lang\TestD\memory.el 76:14
COPY r15 r1
INC r1 4
LOAD MEM r1 r1
COPY r15 r2
LOAD MEM r2 r2
LOAD MEM r2 r2
STORE r2 r1
//  last.next = block.next;

#lineend
:if_end_10
//  if(last == nullptr) {allocatedBlocks = nullptr;} else {last.next = block.next;}

// 6 78:10
#line run\lang\TestD\memory.el 78:10
COPY r15 r1
LOAD MEM r1 r1
LOAD r2 &Memory.blockFreeList
LOAD MEM r2 r2
STORE r2 r1
//  block.next = blockFreeList;

// 7 79:10
#line run\lang\TestD\memory.el 79:10
LOAD r1 &Memory.blockFreeList
COPY r15 r2
LOAD MEM r2 r2
STORE r2 r1
//  blockFreeList = block;

#lineend
:func_exit_Memory.free_void*
STACK DEC 8
// End of scope
#stackVarClear last
#stackVarClear block
#stackVarClear ptr
STACK POP r15
GOTO POP
#endfunction void

// Memory.MemoryBlock

// Console

#function Console.read_char*_uint32 buffer char*, bufferSize uint32
STACK PUSH r15
COPY rStack r15
#stackVar char* buffer -16
#stackVar uint32 bufferSize -12
// 0 52:10
#line run\lang\TestD\console.el 52:10
#breakpoint
//  asm("#breakpoint");

// 1 53:10
#line run\lang\TestD\console.el 53:10
LOAD r1 Console.CONSOLE_IN_COUNT
:read_l0
LOAD MEM BYTE r2 r1
GOTO EQ r2 :read_l0
//  asm("LOAD r1 Console.CONSOLE_IN_COUNT\n:read_l0\nLOAD MEM BYTE r2 r1\nGOTO EQ r2 :read_l0");

// 2 54:10
#line run\lang\TestD\console.el 54:10
LOAD r1 Console.CONSOLE_IN_COUNT
LOAD MEM BYTE r1 r1
#stackVar uint32 inCount
STACK PUSH r1
//  uint32 inCount =* CONSOLE_IN_COUNT;

// 3 55:10
#line run\lang\TestD\console.el 55:10
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
COPY r15 r2
LOAD MEM r2 r2
SUB r1 r1 r2
SET FORCE LT r1 r1
GOTO EQ r1 :if_end_11
// 0 56:14
#line run\lang\TestD\console.el 56:14
COPY r15 r1
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
STORE r2 r1
//  inCount = bufferSize;

#lineend
:if_end_11
//  if(bufferSize < inCount) {inCount = bufferSize;}

// 4 58:10
#line run\lang\TestD\console.el 58:10
LOAD r1 0
#stackVar uint32 i
STACK PUSH r1
//  uint32 i = 0;

// 5 59:10
#line run\lang\TestD\console.el 59:10
:while_condition_12
COPY r15 r1
INC r1 4
LOAD MEM r1 r1
COPY r15 r2
LOAD MEM r2 r2
SUB r1 r1 r2
SET FORCE LT r1 r1
GOTO EQ r1 :while_end_12
// 0 60:14
#line run\lang\TestD\console.el 60:14
COPY r15 r1
INC r1 -16
COPY r15 r2
INC r2 4
LOAD MEM r2 r2
ADD r1 r1 r2
LOAD r2 Console.CONSOLE_IN
LOAD MEM BYTE r2 r2
STORE BYTE r2 r1
//  buffer[i] =* CONSOLE_IN;

// 1 62:14
#line run\lang\TestD\console.el 62:14
COPY r15 r1
INC r1 4
LOAD MEM r2 r1
INC r2 1
STORE r2 r1
//  i++;

#lineend
GOTO :while_condition_12
:while_end_12
//  while(i < inCount) {buffer[i] =* CONSOLE_IN; i++;}

// 6 64:10
#line run\lang\TestD\console.el 64:10
COPY r15 r1
INC r1 4
LOAD MEM r1 r1
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
SUB r1 r1 r2
SET FORCE LT r1 r1
GOTO EQ r1 :if_end_13
// 0 65:14
#line run\lang\TestD\console.el 65:14
COPY r15 r1
INC r1 -16
COPY r15 r2
INC r2 4
LOAD MEM r2 r2
ADD r1 r1 r2
LOAD r2 '\0'
STORE BYTE r2 r1
//  buffer[i] = '\0';

#lineend
:if_end_13
//  if(i < bufferSize) {buffer[i] = '\0';}

#lineend
:func_exit_Console.read_char*_uint32
STACK DEC 8
// End of scope
#stackVarClear i
#stackVarClear buffer
#stackVarClear inCount
#stackVarClear bufferSize
STACK POP r15
GOTO POP
#endfunction void

#function Console.printStr_char*_uint32 str char*, len uint32
STACK PUSH r15
COPY rStack r15
#stackVar char* str -16
#stackVar uint32 len -12
// 0 23:10
#line run\lang\TestD\console.el 23:10
COPY r15 r14
INC r14 -12
LOAD MEM r14 r14
//  asm("COPY r15 r14\nINC r14 -12\nLOAD MEM r14 r14");

// 1 24:10
#line run\lang\TestD\console.el 24:10
LOAD r1 Console.CONSOLE_OUT
//  asm("LOAD r1 Console.CONSOLE_OUT");

// 2 25:10
#line run\lang\TestD\console.el 25:10
COPY r15 r2
INC r2 -16
LOAD MEM r2 r2
//  asm("COPY r15 r2\nINC r2 -16\nLOAD MEM r2 r2");

// 3 26:10
#line run\lang\TestD\console.el 26:10
GOTO GT r14 :printStr_len
//  asm("GOTO GT r14 :printStr_len");

// 4 27:14
#line run\lang\TestD\console.el 27:14
:printStr_l1
//  asm(":printStr_l1");

// 5 28:18
#line run\lang\TestD\console.el 28:18
LOAD MEM BYTE r3 r2
GOTO EQ r3 :printStr_l1_exit
//  asm("LOAD MEM BYTE r3 r2\nGOTO EQ r3 :printStr_l1_exit");

// 6 29:18
#line run\lang\TestD\console.el 29:18
STORE BYTE r3 r1
INC r2 1
GOTO :printStr_l1
//  asm("STORE BYTE r3 r1\nINC r2 1\nGOTO :printStr_l1");

// 7 30:14
#line run\lang\TestD\console.el 30:14
:printStr_l1_exit
GOTO :printStr_exit
//  asm(":printStr_l1_exit\nGOTO :printStr_exit");

// 8 31:10
#line run\lang\TestD\console.el 31:10
:printStr_len
//  asm(":printStr_len");

// 9 32:14
#line run\lang\TestD\console.el 32:14
COPY MEM BYTE r2 r1 INC_RS
//  asm("COPY MEM BYTE r2 r1 INC_RS");

// 10 33:14
#line run\lang\TestD\console.el 33:14
INC r14 -1
GOTO GT r14 :printStr_len
//  asm("INC r14 -1\nGOTO GT r14 :printStr_len");

// 11 34:10
#line run\lang\TestD\console.el 34:10
:printStr_exit
//  asm(":printStr_exit");

#lineend
:func_exit_Console.printStr_char*_uint32
STACK POP r15
GOTO POP
#endfunction void

#function Console.printChar_char c char
STACK PUSH r15
COPY rStack r15
#stackVar char c -12
// 0 17:10
#line run\lang\TestD\console.el 17:10
LOAD r1 Console.CONSOLE_OUT
//  asm("LOAD r1 Console.CONSOLE_OUT");

// 1 18:10
#line run\lang\TestD\console.el 18:10
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
//  asm("COPY r15 r2\nINC r2 -12\nLOAD MEM r2 r2");

// 2 19:10
#line run\lang\TestD\console.el 19:10
STORE BYTE r2 r1
//  asm("STORE BYTE r2 r1");

#lineend
:func_exit_Console.printChar_char
STACK POP r15
GOTO POP
#endfunction void

#function Console.intToHex_uint32_char* value uint32, str char*
STACK PUSH r15
COPY rStack r15
#stackVar char* str -12
#stackVar uint32 value -16
// 0 38:10
#line run\lang\TestD\console.el 38:10
LOAD r14 7
//  asm("LOAD r14 7");

// 1 39:10
#line run\lang\TestD\console.el 39:10
COPY r15 r1
INC r1 -16
LOAD MEM r1 r1
//  asm("COPY r15 r1\nINC r1 -16\nLOAD MEM r1 r1");

// 2 40:10
#line run\lang\TestD\console.el 40:10
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
INC r2 8
//  asm("COPY r15 r2\nINC r2 -12\nLOAD MEM r2 r2\nINC r2 8");

// 3 41:10
#line run\lang\TestD\console.el 41:10
LOAD r3 0xf
LOAD r6 0xa
//  asm("LOAD r3 0xf\nLOAD r6 0xa");

// 4 42:10
#line run\lang\TestD\console.el 42:10
:intToHex_l1
//  asm(":intToHex_l1");

// 5 43:14
#line run\lang\TestD\console.el 43:14
INC r2 -1
AND r4 r1 r3
RSH r1 r1 4
//  asm("INC r2 -1\nAND r4 r1 r3\nRSH r1 r1 4");

// 6 44:14
#line run\lang\TestD\console.el 44:14
SUB r5 r4 r6
GOTO GEQ r5 :intToHex_gt
//  asm("SUB r5 r4 r6\nGOTO GEQ r5 :intToHex_gt");

// 7 45:18
#line run\lang\TestD\console.el 45:18
INC r4 0x30
STORE BYTE r4 r2
GOTO :intToHex_l1_end
//  asm("INC r4 0x30\nSTORE BYTE r4 r2\nGOTO :intToHex_l1_end");

// 8 46:14
#line run\lang\TestD\console.el 46:14
:intToHex_gt
//  asm(":intToHex_gt");

// 9 47:18
#line run\lang\TestD\console.el 47:18
INC r4 0x57
STORE BYTE r4 r2
//  asm("INC r4 0x57\nSTORE BYTE r4 r2");

// 10 48:14
#line run\lang\TestD\console.el 48:14
:intToHex_l1_end
INC r14 -1
GOTO GEQ r14 :intToHex_l1
//  asm(":intToHex_l1_end\nINC r14 -1\nGOTO GEQ r14 :intToHex_l1");

#lineend
:func_exit_Console.intToHex_uint32_char*
STACK POP r15
GOTO POP
#endfunction void

// FS

#function FS.openFile_char*_out_uint32&_out_uint32& path char*, status out uint32&, handle out uint32&
STACK PUSH r15
COPY rStack r15
#stackVar char* path -20
#stackVar out uint32& handle -12
#stackVar out uint32& status -16
// 0 36:10
#line run\lang\TestD\fs.el 36:10
LOAD r1 16
#stackVar uint32[2] msg
STACK PUSH r1
COPY r15 r1
INC r1 -20
LOAD MEM r1 r1
#stackVar uint32[2] msg
STACK PUSH r1
//  uint32[2] msg = {0x10, path};

// 1 37:10
#line run\lang\TestD\fs.el 37:10
LOAD r1 3
STACK PUSH r1
LOAD r1 2
STACK PUSH r1
COPY r15 r1
STACK PUSH r1
GOTO PUSH :FS.peripheralCommand_uint32_uint32_uint32*
STACK DEC 12
//  peripheralCommand(3, 2, & msg);

// 2 38:10
#line run\lang\TestD\fs.el 38:10
COPY r15 r1
INC r1 -16
LOAD MEM r1 r1
LOAD r2 FS.RSP_STATUS
LOAD MEM r2 r2
STORE r2 r1
//  status =* RSP_STATUS;

// 3 39:10
#line run\lang\TestD\fs.el 39:10
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD r2 FS.RSP_DATA_2
LOAD MEM r2 r2
STORE r2 r1
//  handle =* RSP_DATA_2;

#lineend
:func_exit_FS.openFile_char*_out_uint32&_out_uint32&
STACK DEC 8
// End of scope
#stackVarClear msg
#stackVarClear path
#stackVarClear handle
#stackVarClear status
STACK POP r15
GOTO POP
#endfunction void

#function FS.readFile_uint32_void*_uint32_uint32_out_uint32&_out_uint32& handle uint32, buffer void*, size uint32, offset uint32, read out uint32&, state out uint32&
STACK PUSH r15
COPY rStack r15
#stackVar out uint32& read -16
#stackVar uint32 offset -20
#stackVar uint32 size -24
#stackVar uint32 handle -32
#stackVar out uint32& state -12
#stackVar void* buffer -28
// 0 43:10
#line run\lang\TestD\fs.el 43:10
LOAD r1 17
#stackVar uint32[5] msg
STACK PUSH r1
COPY r15 r1
INC r1 -32
LOAD MEM r1 r1
#stackVar uint32[5] msg
STACK PUSH r1
COPY r15 r1
INC r1 -28
LOAD MEM r1 r1
#stackVar uint32[5] msg
STACK PUSH r1
COPY r15 r1
INC r1 -24
LOAD MEM r1 r1
#stackVar uint32[5] msg
STACK PUSH r1
COPY r15 r1
INC r1 -20
LOAD MEM r1 r1
#stackVar uint32[5] msg
STACK PUSH r1
//  uint32[5] msg = {0x11, handle, buffer, size, offset};

// 1 44:10
#line run\lang\TestD\fs.el 44:10
LOAD r1 3
STACK PUSH r1
LOAD r1 5
STACK PUSH r1
COPY r15 r1
STACK PUSH r1
GOTO PUSH :FS.peripheralCommand_uint32_uint32_uint32*
STACK DEC 12
//  peripheralCommand(3, 5, & msg);

// 2 45:10
#line run\lang\TestD\fs.el 45:10
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD r2 FS.RSP_DATA
LOAD MEM r2 r2
STORE r2 r1
//  state =* RSP_DATA;

// 3 46:10
#line run\lang\TestD\fs.el 46:10
COPY r15 r1
INC r1 -16
LOAD MEM r1 r1
LOAD r2 FS.RSP_DATA_3
LOAD MEM r2 r2
STORE r2 r1
//  read =* RSP_DATA_3;

#lineend
:func_exit_FS.readFile_uint32_void*_uint32_uint32_out_uint32&_out_uint32&
STACK DEC 20
// End of scope
#stackVarClear msg
#stackVarClear read
#stackVarClear offset
#stackVarClear size
#stackVarClear handle
#stackVarClear state
#stackVarClear buffer
STACK POP r15
GOTO POP
#endfunction void

#function FS.peripheralCommand_uint32_uint32_uint32* deviceId uint32, cmdSize uint32, cmd uint32*
STACK PUSH r15
COPY rStack r15
#stackVar uint32 cmdSize -16
#stackVar uint32* cmd -12
#stackVar uint32 deviceId -20
// 0 19:10
#line run\lang\TestD\fs.el 19:10
LOAD r1 FS.CMD_SIZE
COPY r15 r2
INC r2 -16
LOAD MEM r2 r2
STORE r2 r1
// * CMD_SIZE = cmdSize;

// 1 21:10
#line run\lang\TestD\fs.el 21:10
LOAD r1 Console.CMD_START
//  asm("LOAD r1 Console.CMD_START");

// 2 23:10
#line run\lang\TestD\fs.el 23:10
COPY r15 r3
INC r3 -12
LOAD MEM r3 r3
//  asm("COPY r15 r3\nINC r3 -12\nLOAD MEM r3 r3");

// 3 25:10
#line run\lang\TestD\fs.el 25:10
:peripheralCommand_l0
//  asm(":peripheralCommand_l0");

// 4 27:10
#line run\lang\TestD\fs.el 27:10
COPY MEM r3 r1
//  asm("COPY MEM r3 r1");

// 5 28:10
#line run\lang\TestD\fs.el 28:10
INC r1 4
INC r3 4
INC r2 -1
//  asm("INC r1 4\nINC r3 4\nINC r2 -1");

// 6 30:10
#line run\lang\TestD\fs.el 30:10
GOTO GT r2 :peripheralCommand_l0
//  asm("GOTO GT r2 :peripheralCommand_l0");

// 7 32:10
#line run\lang\TestD\fs.el 32:10
LOAD r1 FS.CMD_ADDR
LOAD r2 16842752
COPY r15 r3
INC r3 -20
LOAD MEM r3 r3
OR r2 r2 r3
STORE r2 r1
// * CMD_ADDR = 0x0101_0000 | deviceId;

#lineend
:func_exit_FS.peripheralCommand_uint32_uint32_uint32*
STACK POP r15
GOTO POP
#endfunction void

// TestD

#function TestD.onInterrupt
STACK PUSH r15
COPY rStack r15
// 0 97:10
#line run\lang\TestD\testd.el 97:10
COPY rIC r1
#stackVar uint32 code
STACK PUSH r1
//  uint32 code = SysD.rIC;

// 1 98:10
#line run\lang\TestD\testd.el 98:10
LOAD rIC 0
//  asm("LOAD rIC 0");

// 2 99:10
#line run\lang\TestD\testd.el 99:10
#stackVar char[9] str
STACK INC 12
//  char[9] str;

// 3 100:10
#line run\lang\TestD\testd.el 100:10
COPY r15 r1
INC r1 4
INC r1 8
LOAD r2 '\0'
STORE BYTE r2 r1
//  str[8] = '\0';

// 4 105:10
#line run\lang\TestD\testd.el 105:10
COPY r15 r1
LOAD MEM r1 r1
INC r1 -255
SET FORCE EQ r1 r1
GOTO EQ r1 :if_end_14
// 0 106:14
#line run\lang\TestD\testd.el 106:14
HALT
//  asm("HALT");

#lineend
:if_end_14
//  if(code == 0xff) {asm("HALT");}

// 5 108:10
#line run\lang\TestD\testd.el 108:10
COPY r15 r1
LOAD MEM r1 r1
INC r1 -1
SET FORCE EQ r1 r1
GOTO EQ r1 :if_end_15
// 0 109:14
#line run\lang\TestD\testd.el 109:14
LOAD r1 1
#stackVar uint32 i
STACK PUSH r1
//  uint32 i = 1;

// 1 110:14
#line run\lang\TestD\testd.el 110:14
:while_condition_16
COPY r15 r1
INC r1 16
LOAD MEM r1 r1
INC r1 -16
SET FORCE LT r1 r1
GOTO EQ r1 :while_end_16
// 0 111:18
#line run\lang\TestD\testd.el 111:18
LOAD r1 TestD.TIMERS
COPY r15 r2
INC r2 16
LOAD MEM r2 r2
LOAD r3 4
MUL r2 r2 r3
ADD r1 r1 r2
INC r1 1
SET FORCE EQ r1 r1
GOTO EQ r1 :if_end_17
// 0 112:22
#line run\lang\TestD\testd.el 112:22
LOAD r1 TestD.TIMERS
COPY r15 r2
INC r2 16
LOAD MEM r2 r2
LOAD r3 4
MUL r2 r2 r3
ADD r1 r1 r2
LOAD r2 0
STORE r2 r1
//  TIMERS[i] = 0x0;

#lineend
:if_end_17
//  if(TIMERS[i] == 0xffff_ffff) {TIMERS[i] = 0x0;}

// 1 114:18
#line run\lang\TestD\testd.el 114:18
COPY r15 r1
INC r1 16
LOAD MEM r2 r1
INC r2 1
STORE r2 r1
//  i++;

#lineend
GOTO :while_condition_16
:while_end_16
//  while(i < 16) {if(TIMERS[i] == 0xffff_ffff) {TIMERS[i] = 0x0;} i++;}

#lineend
STACK DEC 4
// End of scope
#stackVarClear i
:if_end_15
//  if(code == 0x01) {uint32 i = 1; while(i < 16) {if(TIMERS[i] == 0xffff_ffff) {TIMERS[i] = 0x0;} i++;}}

#lineend
:func_exit_TestD.onInterrupt
STACK DEC 16
// End of scope
#stackVarClear str
#stackVarClear code
STACK POP r15
INTERRUPT RET
#endfunction void

#function TestD.wait_uint32 time uint32
STACK PUSH r15
COPY rStack r15
#stackVar uint32 time -12
// 0 120:10
#line run\lang\TestD\testd.el 120:10
:while_condition_18
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
SET FORCE GT r1 r1
GOTO EQ r1 :while_end_18
// 0 121:14
#line run\lang\TestD\testd.el 121:14
COPY r15 r1
INC r1 -12
LOAD MEM r2 r1
INC r2 -1
STORE r2 r1
//  time--;

#lineend
GOTO :while_condition_18
:while_end_18
//  while(time > 0) {time--;}

#lineend
:func_exit_TestD.wait_uint32
STACK POP r15
GOTO POP
#endfunction void

:__start
#function TestD.main
STACK PUSH r15
COPY rStack r15
// 0 18:10
#line run\lang\TestD\testd.el 18:10
LOAD rIH &:TestD.onInterrupt
//  asm("LOAD rIH &:TestD.onInterrupt");

// 1 19:10
#line run\lang\TestD\testd.el 19:10
#stackVar uint32 b
STACK INC 4
//  uint32 b;

// 2 20:10
#line run\lang\TestD\testd.el 20:10
COPY rPgm r1
#stackVar uint32 a
STACK PUSH r1
//  uint32 a = SysD.rPgm;

// 3 21:10
#line run\lang\TestD\testd.el 21:10
LOAD r1 &TestD.v
COPY r15 r2
INC r2 4
LOAD MEM r2 r2
STORE r2 r1
//  v = a;

// 4 22:10
#line run\lang\TestD\testd.el 22:10
#stackVar char c
STACK INC 4
//  char c;

// 5 23:10
#line run\lang\TestD\testd.el 23:10
COPY r15 r1
INC r1 8
COPY r15 r2
LOAD MEM r2 r2
STORE BYTE r2 r1
//  c = b;

// 6 25:10
#line run\lang\TestD\testd.el 25:10
COPY r15 r1
COPY r15 r2
INC r2 4
LOAD MEM r2 r2
INC r2 1
COPY r15 r3
INC r3 8
LOAD MEM BYTE r3 r3
ADD r2 r2 r3
STORE r2 r1
//  b = a + 1 + c;

// 7 26:10
#line run\lang\TestD\testd.el 26:10
COPY r15 r1
INC r1 8
LOAD r2 32
STORE BYTE r2 r1
//  c = 32;

// 8 27:10
#line run\lang\TestD\testd.el 27:10
COPY r15 r1
INC r1 8
LOAD MEM BYTE r1 r1
STACK PUSH r1
GOTO PUSH :TestD.funcb_uint32
STACK DEC 4
//  funcb(c);

// 9 28:10
#line run\lang\TestD\testd.el 28:10
LOAD r1 64
LOAD r2 &TestD.v
STORE r1 r2
//  asm("LOAD r1 64\nLOAD r2 &TestD.v\nSTORE r1 r2");

// 10 29:10
#line run\lang\TestD\testd.el 29:10
// Test
//  asm(str);

// 11 31:10
#line run\lang\TestD\testd.el 31:10
#stackVar StructA sA
STACK INC 8
//  StructA sA;

// 12 32:10
#line run\lang\TestD\testd.el 32:10
COPY r15 r1
INC r1 12
STACK PUSH r1
GOTO PUSH :TestD.testA_StructA&
STACK DEC 4
//  testA(& sA);

// 13 36:10
#line run\lang\TestD\testd.el 36:10
LOAD r1 &TestD.testStr
STACK PUSH r1
LOAD r1 5
STACK PUSH r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
//  Console.printStr(& testStr, 5);

// 14 37:10
#line run\lang\TestD\testd.el 37:10
LOAD r1 &TestD.testStr2
STACK PUSH r1
LOAD r1 0
STACK PUSH r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
//  Console.printStr(& testStr2, 0);

// 15 38:10
#line run\lang\TestD\testd.el 38:10
LOAD r1 'a'
STACK PUSH r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
//  Console.printChar('a');

// 16 39:10
#line run\lang\TestD\testd.el 39:10
LOAD r1 '\n'
STACK PUSH r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
//  Console.printChar('\n');

// 17 41:10
#line run\lang\TestD\testd.el 41:10
#stackVar char[10] str2
STACK INC 12
//  char[10] str2;

// 18 42:10
#line run\lang\TestD\testd.el 42:10
COPY r15 r1
INC r1 20
INC r1 8
LOAD r2 '\n'
STORE BYTE r2 r1
//  str2[8] = '\n';

// 19 43:10
#line run\lang\TestD\testd.el 43:10
COPY r15 r1
INC r1 20
INC r1 9
LOAD r2 '\0'
STORE BYTE r2 r1
//  str2[9] = '\0';

// 20 45:10
#line run\lang\TestD\testd.el 45:10
LOAD r1 8
STACK INC 4
STACK PUSH r1
GOTO PUSH :Memory.malloc_uint32
STACK INC -4
STACK POP r1
#stackVar StructA* pntr
STACK PUSH r1
//  StructA* pntr = new StructA();

// 21 75:10
#line run\lang\TestD\testd.el 75:10
#define exp_str_0 "\n> \0"
LOAD r1 exp_str_0
STACK PUSH r1
LOAD r1 0
STACK PUSH r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
//  Console.printStr("\n> \0", 0);

// 22 76:10
#line run\lang\TestD\testd.el 76:10
#breakpoint
//  asm("#breakpoint");

// 23 77:10
#line run\lang\TestD\testd.el 77:10
#stackVar char[32] buff
STACK INC 32
//  char[32] buff;

// 24 78:10
#line run\lang\TestD\testd.el 78:10
COPY r15 r1
INC r1 36
STACK PUSH r1
LOAD r1 32
STACK PUSH r1
GOTO PUSH :Console.read_char*_uint32
STACK DEC 8
//  Console.read(& buff, 32);

// 25 79:10
#line run\lang\TestD\testd.el 79:10
COPY r15 r1
INC r1 36
STACK PUSH r1
LOAD r1 0
STACK PUSH r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
//  Console.printStr(& buff, 0);

// 26 81:10
#line run\lang\TestD\testd.el 81:10
LOAD r1 TestD.TIMERS
INC r1 4
LOAD r2 600
STORE r2 r1
//  TIMERS[1] = 120* 5;

// 27 83:10
#line run\lang\TestD\testd.el 83:10
LOAD r1 2000
STACK PUSH r1
GOTO PUSH :TestD.wait_uint32
STACK DEC 4
//  wait(2000);

#lineend
:func_exit_TestD.main
STACK DEC 68
// End of scope
#stackVarClear a
#stackVarClear b
#stackVarClear c
#stackVarClear str2
#stackVarClear pntr
#stackVarClear buff
#stackVarClear sA
STACK POP r15
HALT
#endfunction void

#function TestD.funcb_uint32 a uint32
STACK PUSH r15
COPY rStack r15
#stackVar uint32 a -12
// 0 88:10
#line run\lang\TestD\testd.el 88:10
LOAD r1 &TestD.v
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
LOAD MEM r3 r1
ADD r2 r3 r2
STORE r2 r1
//  v += a;

#lineend
:func_exit_TestD.funcb_uint32
STACK POP r15
GOTO POP
#endfunction void

#function TestD.funcb_uint32_uint32* a uint32, b uint32*
STACK PUSH r15
COPY rStack r15
#stackVar uint32 a -16
#stackVar uint32* b -12
// 0 92:10
#line run\lang\TestD\testd.el 92:10
LOAD r1 &TestD.v
COPY r15 r2
INC r2 -16
LOAD MEM r2 r2
LOAD MEM r3 r1
ADD r2 r3 r2
STORE r2 r1
//  v += a;

#lineend
:func_exit_TestD.funcb_uint32_uint32*
STACK POP r15
GOTO POP
#endfunction void

#function TestD.testA_StructA& str StructA&
STACK PUSH r15
COPY rStack r15
#stackVar StructA& str -12
// 0 126:10
#line run\lang\TestD\testd.el 126:10
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD r2 32
STORE r2 r1
//  str.a = 32;

// 1 127:10
#line run\lang\TestD\testd.el 127:10
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD r2 -1
STORE r2 r1
//  str.b = 0xffffffff;

#lineend
:func_exit_TestD.testA_StructA&
STACK POP r15
GOTO POP
#endfunction void

// TestD.StructA

HALT