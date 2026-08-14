// static data
// Memory
#var Memory.heapStart 0x0002_3000 void*
#var Memory.allocatedBlocks 0x0000 MemoryBlock*
#var Memory.blockFreeList 0x0002_2000 MemoryBlock*
#define Memory.ALLOCATED_BLOCK_LIST 0x0002_2000 uint32
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
// TestD
#define TestD.str "// Test" char*
#var TestD.path "test.txt\0" char[9]
#define TestD.TIMERS 0x0001_0200 uint32*
#var TestD.v 0x0000 uint32
#var TestD.testStr2 "Test2\n\0" char[7]
#var TestD.testStr "Test\n" char[5]
#var TestD.tc 0x00 char
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

//--------
// text

// Memory

#function Memory.malloc_uint32 size uint32
STACK PUSH r15
COPY rStack r15
#stackVar uint32 size -12
// 0 18:10
#line run/lang/TestD/memory.el 18:10
// Reserving r1
COPY r15 r1
INC r1 -12
// Reserving r1
LOAD MEM r1 r1
INC r1 -4096
SET FORCE GT r1 r1 // size > 0x1000
GOTO EQ r1 :if_end_0
// Releasing r1
// 0 19:14
#line run/lang/TestD/memory.el 19:14
// Reserving r1
LOAD r1 0 // nullptr
// Reserving r2
COPY r15 r2
INC r2 -16
STORE r1 r2
GOTO :func_exit_Memory.malloc_uint32
// Releasing r1
// Releasing r2
//  return nullptr;

#lineend
:if_end_0
//  if(size > 0x1000) {return nullptr;}

// 1 21:10
#line run/lang/TestD/memory.el 21:10
// Reserving r1
LOAD r1 &Memory.blockFreeList
// Reserving r1
LOAD MEM r1 r1
SET FORCE EQ r1 r1 // blockFreeList == nullptr
GOTO EQ r1 :if_end_1
// Releasing r1
// 0 22:14
#line run/lang/TestD/memory.el 22:14
// Reserving r1
LOAD r1 0 // nullptr
// Reserving r2
COPY r15 r2
INC r2 -16
STORE r1 r2
GOTO :func_exit_Memory.malloc_uint32
// Releasing r1
// Releasing r2
//  return nullptr;

#lineend
:if_end_1
//  if(blockFreeList == nullptr) {return nullptr;}

// 2 24:10
#line run/lang/TestD/memory.el 24:10
// Reserving r1
COPY r15 r1
INC r1 -12
// Reserving r1
LOAD MEM r1 r1
RSH r1 r1 2 // size >> 2
#stackVar uint32 wordSize
STACK PUSH r1
// Releasing r1
//  uint32 wordSize = size >> 2;

// 3 25:10
#line run/lang/TestD/memory.el 25:10
// Reserving r1
COPY r15 r1
INC r1 -12
// Reserving r1
LOAD MEM r1 r1
LOAD r2 3
AND r1 r1 r2
SET FORCE NEQ r1 r1 // size & 0x3 != 0
GOTO EQ r1 :if_end_2
// Releasing r1
// 0 26:14
#line run/lang/TestD/memory.el 26:14
// Reserving r1
// Reserving r2
COPY r15 r2
// Reserving r2
// Releasing r1
LOAD MEM r1 r2
INC r1 1
STORE r1 r2
// Releasing r2
//  wordSize++;

#lineend
:if_end_2
//  if(size & 0x3 != 0) {wordSize++;}

// 4 28:10
#line run/lang/TestD/memory.el 28:10
// Reserving r1
LOAD r1 &Memory.allocatedBlocks
// Reserving r1
LOAD MEM r1 r1 // allocatedBlocks
#stackVar MemoryBlock* block
STACK PUSH r1
// Releasing r1
//  MemoryBlock* block = allocatedBlocks;

// 5 29:10
#line run/lang/TestD/memory.el 29:10
// Reserving r1
COPY r15 r1
INC r1 4
// Reserving r1
LOAD MEM r1 r1
SET FORCE EQ r1 r1 // block == nullptr
GOTO EQ r1 :if_end_3
// Releasing r1
// 0 30:14
#line run/lang/TestD/memory.el 30:14
// Reserving r1
LOAD r1 &Memory.blockFreeList
// Reserving r1
LOAD MEM r1 r1 // blockFreeList
#stackVar MemoryBlock* next
STACK PUSH r1
// Releasing r1
//  MemoryBlock* next = blockFreeList;

// 1 31:14
#line run/lang/TestD/memory.el 31:14
// Reserving r1
// Reserving r2
LOAD r2 &Memory.blockFreeList
// Reserving r2
// Releasing r1
LOAD r1 &Memory.blockFreeList
// Reserving r1
LOAD MEM r1 r1
LOAD MEM r1 r1 // blockFreeList.next
STORE r1 r2
// Releasing r2
// Releasing r1
//  blockFreeList = blockFreeList.next;

// 2 32:14
#line run/lang/TestD/memory.el 32:14
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 8
// Reserving r2
LOAD MEM r2 r2
// Releasing r1
COPY r15 r1
INC r1 4
// Reserving r1
LOAD MEM r1 r1
LOAD MEM r1 r1
INC r1 4 // block.end + 1
STORE r1 r2
// Releasing r2
// Releasing r1
//  next.start = block.end + 1;

// 3 33:14
#line run/lang/TestD/memory.el 33:14
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 8
// Reserving r2
LOAD MEM r2 r2
// Releasing r1
COPY r15 r1
INC r1 8
// Reserving r1
LOAD MEM r1 r1
LOAD MEM r1 r1 // Reserving r3
COPY r15 r3
// Reserving r3
LOAD MEM r3 r3 // Found r4
LOAD r4 4
MUL r3 r3 r4
ADD r1 r1 r3 // Releasing r3
INC r1 -4 // next.start + wordSize - 1
STORE r1 r2
// Releasing r2
// Releasing r1
//  next.end = next.start + wordSize - 1;

// 4 34:14
#line run/lang/TestD/memory.el 34:14
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 8
// Reserving r2
LOAD MEM r2 r2
// Releasing r1
LOAD r1 0 // nullptr
STORE r1 r2
// Releasing r2
// Releasing r1
//  next.next = nullptr;

// 5 35:14
#line run/lang/TestD/memory.el 35:14
// Reserving r1
// Reserving r2
LOAD r2 &Memory.allocatedBlocks
// Reserving r2
// Releasing r1
COPY r15 r1
INC r1 8
// Reserving r1
LOAD MEM r1 r1 // next
STORE r1 r2
// Releasing r2
// Releasing r1
//  allocatedBlocks = next;

// 6 36:14
#line run/lang/TestD/memory.el 36:14
// Reserving r1
LOAD r1 &Memory.heapStart
// Reserving r1
LOAD MEM r1 r1 // heapStart
// Reserving r2
COPY r15 r2
INC r2 -16
STORE r1 r2
GOTO :func_exit_Memory.malloc_uint32
// Releasing r1
// Releasing r2
//  return heapStart;

#lineend
STACK DEC 4
// End of scope
#stackVarClear next
:if_end_3
//  if(block == nullptr) {MemoryBlock* next = blockFreeList; blockFreeList = blockFreeList.next; next.start = block.end + 1; next.end = next.start + wordSize - 1; next.next = nullptr; allocatedBlocks = next; return heapStart;}

// 6 38:10
#line run/lang/TestD/memory.el 38:10
// Reserving r1
LOAD r1 &Memory.heapStart
// Reserving r1
LOAD MEM r1 r1 // heapStart
#stackVar void* lastEnd
STACK PUSH r1
// Releasing r1
//  void* lastEnd = heapStart;

// 7 39:10
#line run/lang/TestD/memory.el 39:10
:while_condition_4
// Reserving r1
COPY r15 r1
INC r1 4
// Reserving r1
LOAD MEM r1 r1
LOAD MEM r1 r1
SET FORCE NEQ r1 r1 // block.next != nullptr
GOTO EQ r1 :exp_ee_0
COPY r15 r1
INC r1 4
// Reserving r1
LOAD MEM r1 r1
LOAD MEM r1 r1
COPY r15 r2
INC r2 8
// Reserving r2
LOAD MEM r2 r2
SUB r1 r1 r2 // block.start - lastEnd
COPY r15 r2
INC r2 -12
// Reserving r2
LOAD MEM r2 r2
SUB r1 r1 r2
SET FORCE GEQ r1 r1 // ( block.start - lastEnd ) >= size
:exp_ee_0 // ( block.next != nullptr ) && ( ( block.start - lastEnd ) >= size )
GOTO EQ r1 :while_end_4
// Releasing r1
// 0 40:14
#line run/lang/TestD/memory.el 40:14
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 8
// Reserving r2
// Releasing r1
COPY r15 r1
INC r1 4
// Reserving r1
LOAD MEM r1 r1
LOAD MEM r1 r1 // block.end
STORE r1 r2
// Releasing r2
// Releasing r1
//  lastEnd = block.end;

// 1 41:14
#line run/lang/TestD/memory.el 41:14
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 4
// Reserving r2
// Releasing r1
COPY r15 r1
INC r1 4
// Reserving r1
LOAD MEM r1 r1
LOAD MEM r1 r1 // block.next
STORE r1 r2
// Releasing r2
// Releasing r1
//  block = block.next;

#lineend
GOTO :while_condition_4
:while_end_4
//  while((block.next != nullptr) && ((block.start - lastEnd) >= size)) {lastEnd = block.end; block = block.next;}

// 8 43:10
#line run/lang/TestD/memory.el 43:10
// Reserving r1
COPY r15 r1
INC r1 4
// Reserving r1
LOAD MEM r1 r1
LOAD MEM r1 r1
SET FORCE EQ r1 r1 // block.next == nullptr
GOTO EQ r1 :if_end_5
// Releasing r1
// 0 44:14
#line run/lang/TestD/memory.el 44:14
// Reserving r1
COPY r15 r1
INC r1 4
// Reserving r1
LOAD MEM r1 r1
LOAD MEM r1 r1 // Reserving r2
COPY r15 r2
INC r2 -12
// Reserving r2
LOAD MEM r2 r2 // Found r3
LOAD r3 4
MUL r2 r2 r3
ADD r1 r1 r2 // Releasing r2
INC r1 -4
LOAD r2 196608
SUB r1 r1 r2
SET FORCE GT r1 r1 // block.end + size - 1 > 0x3_0000
GOTO EQ r1 :if_end_6
// Releasing r1
// 0 45:18
#line run/lang/TestD/memory.el 45:18
// Reserving r1
LOAD r1 0 // nullptr
// Reserving r2
COPY r15 r2
INC r2 -16
STORE r1 r2
GOTO :func_exit_Memory.malloc_uint32
// Releasing r1
// Releasing r2
//  return nullptr;

#lineend
:if_end_6
//  if(block.end + size - 1 > 0x3_0000) {return nullptr;}

// 1 47:14
#line run/lang/TestD/memory.el 47:14
// Reserving r1
LOAD r1 &Memory.blockFreeList
// Reserving r1
LOAD MEM r1 r1 // blockFreeList
#stackVar MemoryBlock* next
STACK PUSH r1
// Releasing r1
//  MemoryBlock* next = blockFreeList;

// 2 48:14
#line run/lang/TestD/memory.el 48:14
// Reserving r1
// Reserving r2
LOAD r2 &Memory.blockFreeList
// Reserving r2
// Releasing r1
LOAD r1 &Memory.blockFreeList
// Reserving r1
LOAD MEM r1 r1
LOAD MEM r1 r1 // blockFreeList.next
STORE r1 r2
// Releasing r2
// Releasing r1
//  blockFreeList = blockFreeList.next;

// 3 49:14
#line run/lang/TestD/memory.el 49:14
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 12
// Reserving r2
LOAD MEM r2 r2
// Releasing r1
COPY r15 r1
INC r1 4
// Reserving r1
LOAD MEM r1 r1
LOAD MEM r1 r1
INC r1 4 // block.end + 1
STORE r1 r2
// Releasing r2
// Releasing r1
//  next.start = block.end + 1;

// 4 50:14
#line run/lang/TestD/memory.el 50:14
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 12
// Reserving r2
LOAD MEM r2 r2
// Releasing r1
COPY r15 r1
INC r1 12
// Reserving r1
LOAD MEM r1 r1
LOAD MEM r1 r1 // Reserving r3
COPY r15 r3
// Reserving r3
LOAD MEM r3 r3 // Found r4
LOAD r4 4
MUL r3 r3 r4
ADD r1 r1 r3 // Releasing r3
INC r1 -4 // next.start + wordSize - 1
STORE r1 r2
// Releasing r2
// Releasing r1
//  next.end = next.start + wordSize - 1;

// 5 51:14
#line run/lang/TestD/memory.el 51:14
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 12
// Reserving r2
LOAD MEM r2 r2
// Releasing r1
LOAD r1 0 // nullptr
STORE r1 r2
// Releasing r2
// Releasing r1
//  next.next = nullptr;

// 6 52:14
#line run/lang/TestD/memory.el 52:14
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 4
// Reserving r2
LOAD MEM r2 r2
// Releasing r1
COPY r15 r1
INC r1 12
// Reserving r1
LOAD MEM r1 r1 // next
STORE r1 r2
// Releasing r2
// Releasing r1
//  block.next = next;

// 7 53:14
#line run/lang/TestD/memory.el 53:14
// Reserving r1
COPY r15 r1
INC r1 12
// Reserving r1
LOAD MEM r1 r1
LOAD MEM r1 r1 // next.start
// Reserving r2
COPY r15 r2
INC r2 -16
STORE r1 r2
GOTO :func_exit_Memory.malloc_uint32
// Releasing r1
// Releasing r2
//  return next.start;

#lineend
STACK DEC 4
// End of scope
#stackVarClear next
:if_end_5
//  if(block.next == nullptr) {if(block.end + size - 1 > 0x3_0000) {return nullptr;} MemoryBlock* next = blockFreeList; blockFreeList = blockFreeList.next; next.start = block.end + 1; next.end = next.start + wordSize - 1; next.next = nullptr; block.next = next; return next.start;}

// 9 55:10
#line run/lang/TestD/memory.el 55:10
// Reserving r1
LOAD r1 &Memory.blockFreeList
// Reserving r1
LOAD MEM r1 r1 // blockFreeList
#stackVar MemoryBlock* next
STACK PUSH r1
// Releasing r1
//  MemoryBlock* next = blockFreeList;

// 10 56:10
#line run/lang/TestD/memory.el 56:10
// Reserving r1
// Reserving r2
LOAD r2 &Memory.blockFreeList
// Reserving r2
// Releasing r1
LOAD r1 &Memory.blockFreeList
// Reserving r1
LOAD MEM r1 r1
LOAD MEM r1 r1 // blockFreeList.next
STORE r1 r2
// Releasing r2
// Releasing r1
//  blockFreeList = blockFreeList.next;

// 11 57:10
#line run/lang/TestD/memory.el 57:10
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 12
// Reserving r2
LOAD MEM r2 r2
// Releasing r1
COPY r15 r1
INC r1 4
// Reserving r1
LOAD MEM r1 r1
LOAD MEM r1 r1
INC r1 4 // block.end + 1
STORE r1 r2
// Releasing r2
// Releasing r1
//  next.start = block.end + 1;

// 12 58:10
#line run/lang/TestD/memory.el 58:10
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 12
// Reserving r2
LOAD MEM r2 r2
// Releasing r1
COPY r15 r1
INC r1 12
// Reserving r1
LOAD MEM r1 r1
LOAD MEM r1 r1 // Reserving r3
COPY r15 r3
// Reserving r3
LOAD MEM r3 r3 // Found r4
LOAD r4 4
MUL r3 r3 r4
ADD r1 r1 r3 // Releasing r3
INC r1 -4 // next.start + wordSize - 1
STORE r1 r2
// Releasing r2
// Releasing r1
//  next.end = next.start + wordSize - 1;

// 13 59:10
#line run/lang/TestD/memory.el 59:10
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 12
// Reserving r2
LOAD MEM r2 r2
// Releasing r1
COPY r15 r1
INC r1 4
// Reserving r1
LOAD MEM r1 r1
LOAD MEM r1 r1 // block.next
STORE r1 r2
// Releasing r2
// Releasing r1
//  next.next = block.next;

// 14 60:10
#line run/lang/TestD/memory.el 60:10
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 4
// Reserving r2
LOAD MEM r2 r2
// Releasing r1
COPY r15 r1
INC r1 12
// Reserving r1
LOAD MEM r1 r1 // next
STORE r1 r2
// Releasing r2
// Releasing r1
//  block.next = next;

// 15 61:10
#line run/lang/TestD/memory.el 61:10
// Reserving r1
COPY r15 r1
INC r1 12
// Reserving r1
LOAD MEM r1 r1
LOAD MEM r1 r1 // next.start
// Reserving r2
COPY r15 r2
INC r2 -16
STORE r1 r2
GOTO :func_exit_Memory.malloc_uint32
// Releasing r1
// Releasing r2
//  return next.start;

#lineend
:func_exit_Memory.malloc_uint32
STACK DEC 16
// End of scope
#stackVarClear next
#stackVarClear size
#stackVarClear lastEnd
#stackVarClear block
#stackVarClear wordSize
STACK POP r15
GOTO POP
#endfunction void*

#function Memory.setup
STACK PUSH r15
COPY rStack r15
// 0 9:10
#line run/lang/TestD/memory.el 9:10
// Reserving r1
LOAD r1 &Memory.blockFreeList
// Reserving r1
LOAD MEM r1 r1 // blockFreeList
#stackVar MemoryBlock* list
STACK PUSH r1
// Releasing r1
//  MemoryBlock* list = blockFreeList;

// 1 10:10
#line run/lang/TestD/memory.el 10:10
:while_condition_7
// Reserving r1
COPY r15 r1
// Reserving r1
LOAD MEM r1 r1
LOAD r2 &Memory.heapStart
// Reserving r2
LOAD MEM r2 r2
INC r2 -12 // heapStart - 3
SUB r1 r1 r2
SET FORCE LT r1 r1 // list < ( heapStart - 3 )
GOTO EQ r1 :while_end_7
// Releasing r1
// 0 11:14
#line run/lang/TestD/memory.el 11:14
// Reserving r1
// Reserving r2
COPY r15 r2
// Reserving r2
LOAD MEM r2 r2
// Releasing r1
COPY r15 r1
// Reserving r1
LOAD MEM r1 r1
INC r1 12 // list + 1
STORE r1 r2
// Releasing r2
// Releasing r1
//  list.next = list + 1;

// 1 12:14
#line run/lang/TestD/memory.el 12:14
// Reserving r1
// Reserving r2
COPY r15 r2
// Reserving r2
// Releasing r1
LOAD MEM r1 r2
INC r1 12
STORE r1 r2
// Releasing r2
//  list++;

#lineend
GOTO :while_condition_7
:while_end_7
//  while(list < (heapStart - 3)) {list.next = list + 1; list++;}

// 2 14:10
#line run/lang/TestD/memory.el 14:10
// Reserving r1
// Reserving r2
COPY r15 r2
// Reserving r2
LOAD MEM r2 r2
// Releasing r1
LOAD r1 0 // nullptr
STORE r1 r2
// Releasing r2
// Releasing r1
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
// 0 65:10
#line run/lang/TestD/memory.el 65:10
// Reserving r1
LOAD r1 &Memory.allocatedBlocks
// Reserving r1
LOAD MEM r1 r1
SET FORCE EQ r1 r1 // allocatedBlocks == nullptr
GOTO EQ r1 :if_end_8
// Releasing r1
// 0 66:14
#line run/lang/TestD/memory.el 66:14
GOTO :func_exit_Memory.free_void*
//  return;

#lineend
:if_end_8
//  if(allocatedBlocks == nullptr) {return;}

// 1 68:10
#line run/lang/TestD/memory.el 68:10
// Reserving r1
LOAD r1 &Memory.allocatedBlocks
// Reserving r1
LOAD MEM r1 r1 // allocatedBlocks
#stackVar MemoryBlock* block
STACK PUSH r1
// Releasing r1
//  MemoryBlock* block = allocatedBlocks;

// 2 69:10
#line run/lang/TestD/memory.el 69:10
// Reserving r1
LOAD r1 0 // nullptr
#stackVar MemoryBlock* last
STACK PUSH r1
// Releasing r1
//  MemoryBlock* last = nullptr;

// 3 70:10
#line run/lang/TestD/memory.el 70:10
:while_condition_9
// Reserving r1
COPY r15 r1
// Reserving r1
LOAD MEM r1 r1
LOAD MEM r1 r1
SET FORCE NEQ r1 r1 // block.next != nullptr
COPY r15 r2
// Reserving r2
LOAD MEM r2 r2
LOAD MEM r2 r2
COPY r15 r3
INC r3 -12
// Reserving r3
LOAD MEM r3 r3
SUB r2 r2 r3
SET FORCE NEQ r2 r2 // block.start != ptr
AND r1 r1 r2 // ( block.next != nullptr ) & ( block.start != ptr )
GOTO EQ r1 :while_end_9
// Releasing r1
// 0 71:14
#line run/lang/TestD/memory.el 71:14
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 4
// Reserving r2
// Releasing r1
COPY r15 r1
// Reserving r1
LOAD MEM r1 r1 // block
STORE r1 r2
// Releasing r2
// Releasing r1
//  last = block;

// 1 72:14
#line run/lang/TestD/memory.el 72:14
// Reserving r1
// Reserving r2
COPY r15 r2
// Reserving r2
// Releasing r1
COPY r15 r1
// Reserving r1
LOAD MEM r1 r1
LOAD MEM r1 r1 // block.next
STORE r1 r2
// Releasing r2
// Releasing r1
//  block = block.next;

#lineend
GOTO :while_condition_9
:while_end_9
//  while((block.next != nullptr) & (block.start != ptr)) {last = block; block = block.next;}

// 4 74:10
#line run/lang/TestD/memory.el 74:10
// Reserving r1
COPY r15 r1
// Reserving r1
LOAD MEM r1 r1
LOAD MEM r1 r1
COPY r15 r2
INC r2 -12
// Reserving r2
LOAD MEM r2 r2
SUB r1 r1 r2
SET FORCE NEQ r1 r1 // block.start != ptr
GOTO EQ r1 :if_end_10
// Releasing r1
// 0 75:14
#line run/lang/TestD/memory.el 75:14
GOTO :func_exit_Memory.free_void*
//  return;

#lineend
:if_end_10
//  if(block.start != ptr) {return;}

// 5 77:10
#line run/lang/TestD/memory.el 77:10
// Reserving r1
COPY r15 r1
INC r1 4
// Reserving r1
LOAD MEM r1 r1
SET FORCE EQ r1 r1 // last == nullptr
GOTO EQ r1 :if_else_11
// Releasing r1
// 0 78:14
#line run/lang/TestD/memory.el 78:14
// Reserving r1
// Reserving r2
LOAD r2 &Memory.allocatedBlocks
// Reserving r2
// Releasing r1
LOAD r1 0 // nullptr
STORE r1 r2
// Releasing r2
// Releasing r1
//  allocatedBlocks = nullptr;

#lineend
GOTO :if_end_11
:if_else_11
// 0 80:14
#line run/lang/TestD/memory.el 80:14
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 4
// Reserving r2
LOAD MEM r2 r2
// Releasing r1
COPY r15 r1
// Reserving r1
LOAD MEM r1 r1
LOAD MEM r1 r1 // block.next
STORE r1 r2
// Releasing r2
// Releasing r1
//  last.next = block.next;

#lineend
:if_end_11
//  if(last == nullptr) {allocatedBlocks = nullptr;} else {last.next = block.next;}

// 6 82:10
#line run/lang/TestD/memory.el 82:10
// Reserving r1
// Reserving r2
COPY r15 r2
// Reserving r2
LOAD MEM r2 r2
// Releasing r1
LOAD r1 &Memory.blockFreeList
// Reserving r1
LOAD MEM r1 r1 // blockFreeList
STORE r1 r2
// Releasing r2
// Releasing r1
//  block.next = blockFreeList;

// 7 83:10
#line run/lang/TestD/memory.el 83:10
// Reserving r1
// Reserving r2
LOAD r2 &Memory.blockFreeList
// Reserving r2
// Releasing r1
COPY r15 r1
// Reserving r1
LOAD MEM r1 r1 // block
STORE r1 r2
// Releasing r2
// Releasing r1
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
#line run/lang/TestD/console.el 52:10
LOAD r1 Console.CONSOLE_IN_COUNT
:read_l0
LOAD MEM BYTE r2 r1
GOTO EQ r2 :read_l0
//  asm("LOAD r1 Console.CONSOLE_IN_COUNT\n:read_l0\nLOAD MEM BYTE r2 r1\nGOTO EQ r2 :read_l0");

// 1 53:10
#line run/lang/TestD/console.el 53:10
// Reserving r1
LOAD r1 Console.CONSOLE_IN_COUNT
// Reserving r1

LOAD MEM BYTE r1 r1 // *CONSOLE_IN_COUNT
#stackVar uint32 inCount
STACK PUSH r1
// Releasing r1
//  uint32 inCount =* CONSOLE_IN_COUNT;

// 2 54:10
#line run/lang/TestD/console.el 54:10
// Reserving r1
COPY r15 r1
INC r1 -12
// Reserving r1
LOAD MEM r1 r1
COPY r15 r2
// Reserving r2
LOAD MEM r2 r2
SUB r1 r1 r2
SET FORCE LT r1 r1 // bufferSize < inCount
GOTO EQ r1 :if_end_12
// Releasing r1
// 0 55:14
#line run/lang/TestD/console.el 55:14
// Reserving r1
// Reserving r2
COPY r15 r2
// Reserving r2
// Releasing r1
COPY r15 r1
INC r1 -12
// Reserving r1
LOAD MEM r1 r1 // bufferSize
STORE r1 r2
// Releasing r2
// Releasing r1
//  inCount = bufferSize;

#lineend
:if_end_12
//  if(bufferSize < inCount) {inCount = bufferSize;}

// 3 57:10
#line run/lang/TestD/console.el 57:10
// Reserving r1
LOAD r1 0 // 0
#stackVar uint32 i
STACK PUSH r1
// Releasing r1
//  uint32 i = 0;

// 4 58:10
#line run/lang/TestD/console.el 58:10
:while_condition_13
// Reserving r1
COPY r15 r1
INC r1 4
// Reserving r1
LOAD MEM r1 r1
COPY r15 r2
// Reserving r2
LOAD MEM r2 r2
SUB r1 r1 r2
SET FORCE LT r1 r1 // i < inCount
GOTO EQ r1 :while_end_13
// Releasing r1
// 0 59:14
#line run/lang/TestD/console.el 59:14
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 -16
// Reserving r2
LOAD MEM r2 r2
// Reserving r3
COPY r15 r3
INC r3 4
// Reserving r3
LOAD MEM r3 r3 // i
ADD r2 r2 r3
// Releasing r3
// Releasing r1
LOAD r1 Console.CONSOLE_IN
// Reserving r1

LOAD MEM BYTE r1 r1 // *CONSOLE_IN
STORE BYTE r1 r2
// Releasing r2
// Releasing r1
//  buffer[i] =* CONSOLE_IN;

// 1 61:14
#line run/lang/TestD/console.el 61:14
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 4
// Reserving r2
// Releasing r1
LOAD MEM r1 r2
INC r1 1
STORE r1 r2
// Releasing r2
//  i++;

#lineend
GOTO :while_condition_13
:while_end_13
//  while(i < inCount) {buffer[i] =* CONSOLE_IN; i++;}

// 5 63:10
#line run/lang/TestD/console.el 63:10
// Reserving r1
COPY r15 r1
INC r1 4
// Reserving r1
LOAD MEM r1 r1
COPY r15 r2
INC r2 -12
// Reserving r2
LOAD MEM r2 r2
SUB r1 r1 r2
SET FORCE LT r1 r1 // i < bufferSize
GOTO EQ r1 :if_end_14
// Releasing r1
// 0 64:14
#line run/lang/TestD/console.el 64:14
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 -16
// Reserving r2
LOAD MEM r2 r2
// Reserving r3
COPY r15 r3
INC r3 4
// Reserving r3
LOAD MEM r3 r3 // i
ADD r2 r2 r3
// Releasing r3
// Releasing r1
LOAD r1 '\0' // \0
STORE BYTE r1 r2
// Releasing r2
// Releasing r1
//  buffer[i] = '\0';

#lineend
:if_end_14
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
#line run/lang/TestD/console.el 23:10
COPY r15 r14
INC r14 -12
LOAD MEM r14 r14
//  asm("COPY r15 r14\nINC r14 -12\nLOAD MEM r14 r14");

// 1 24:10
#line run/lang/TestD/console.el 24:10
LOAD r1 Console.CONSOLE_OUT
//  asm("LOAD r1 Console.CONSOLE_OUT");

// 2 25:10
#line run/lang/TestD/console.el 25:10
COPY r15 r2
INC r2 -16
LOAD MEM r2 r2
//  asm("COPY r15 r2\nINC r2 -16\nLOAD MEM r2 r2");

// 3 26:10
#line run/lang/TestD/console.el 26:10
GOTO GT r14 :printStr_len
//  asm("GOTO GT r14 :printStr_len");

// 4 27:14
#line run/lang/TestD/console.el 27:14
:printStr_l1
//  asm(":printStr_l1");

// 5 28:18
#line run/lang/TestD/console.el 28:18
LOAD MEM BYTE r3 r2
GOTO EQ r3 :printStr_l1_exit
//  asm("LOAD MEM BYTE r3 r2\nGOTO EQ r3 :printStr_l1_exit");

// 6 29:18
#line run/lang/TestD/console.el 29:18
STORE BYTE r3 r1
INC r2 1
GOTO :printStr_l1
//  asm("STORE BYTE r3 r1\nINC r2 1\nGOTO :printStr_l1");

// 7 30:14
#line run/lang/TestD/console.el 30:14
:printStr_l1_exit
GOTO :printStr_exit
//  asm(":printStr_l1_exit\nGOTO :printStr_exit");

// 8 31:10
#line run/lang/TestD/console.el 31:10
:printStr_len
//  asm(":printStr_len");

// 9 32:14
#line run/lang/TestD/console.el 32:14
COPY MEM BYTE r2 r1 INC_RS
//  asm("COPY MEM BYTE r2 r1 INC_RS");

// 10 33:14
#line run/lang/TestD/console.el 33:14
INC r14 -1
GOTO GT r14 :printStr_len
//  asm("INC r14 -1\nGOTO GT r14 :printStr_len");

// 11 34:10
#line run/lang/TestD/console.el 34:10
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
#line run/lang/TestD/console.el 17:10
LOAD r1 Console.CONSOLE_OUT
//  asm("LOAD r1 Console.CONSOLE_OUT");

// 1 18:10
#line run/lang/TestD/console.el 18:10
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
//  asm("COPY r15 r2\nINC r2 -12\nLOAD MEM r2 r2");

// 2 19:10
#line run/lang/TestD/console.el 19:10
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
#line run/lang/TestD/console.el 38:10
LOAD r14 7
//  asm("LOAD r14 7");

// 1 39:10
#line run/lang/TestD/console.el 39:10
COPY r15 r1
INC r1 -16
LOAD MEM r1 r1
//  asm("COPY r15 r1\nINC r1 -16\nLOAD MEM r1 r1");

// 2 40:10
#line run/lang/TestD/console.el 40:10
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
INC r2 8
//  asm("COPY r15 r2\nINC r2 -12\nLOAD MEM r2 r2\nINC r2 8");

// 3 41:10
#line run/lang/TestD/console.el 41:10
LOAD r3 0xf
LOAD r6 0xa
//  asm("LOAD r3 0xf\nLOAD r6 0xa");

// 4 42:10
#line run/lang/TestD/console.el 42:10
:intToHex_l1
//  asm(":intToHex_l1");

// 5 43:14
#line run/lang/TestD/console.el 43:14
INC r2 -1
AND r4 r1 r3
RSH r1 r1 4
//  asm("INC r2 -1\nAND r4 r1 r3\nRSH r1 r1 4");

// 6 44:14
#line run/lang/TestD/console.el 44:14
SUB r5 r4 r6
GOTO GEQ r5 :intToHex_gt
//  asm("SUB r5 r4 r6\nGOTO GEQ r5 :intToHex_gt");

// 7 45:18
#line run/lang/TestD/console.el 45:18
INC r4 0x30
STORE BYTE r4 r2
GOTO :intToHex_l1_end
//  asm("INC r4 0x30\nSTORE BYTE r4 r2\nGOTO :intToHex_l1_end");

// 8 46:14
#line run/lang/TestD/console.el 46:14
:intToHex_gt
//  asm(":intToHex_gt");

// 9 47:18
#line run/lang/TestD/console.el 47:18
INC r4 0x57
STORE BYTE r4 r2
//  asm("INC r4 0x57\nSTORE BYTE r4 r2");

// 10 48:14
#line run/lang/TestD/console.el 48:14
:intToHex_l1_end
INC r14 -1
GOTO GEQ r14 :intToHex_l1
//  asm(":intToHex_l1_end\nINC r14 -1\nGOTO GEQ r14 :intToHex_l1");

#lineend
:func_exit_Console.intToHex_uint32_char*
STACK POP r15
GOTO POP
#endfunction void

// TestD

#function TestD.onInterrupt
STACK PUSH r15
COPY rStack r15
// 0 96:10
#line run/lang/TestD/testd.el 96:10
// Reserving r1
COPY rIC r1 // SysD.rIC
#stackVar uint32 code
STACK PUSH r1
// Releasing r1
//  uint32 code = SysD.rIC;

// 1 97:10
#line run/lang/TestD/testd.el 97:10
LOAD rIC 0
//  asm("LOAD rIC 0");

// 2 98:10
#line run/lang/TestD/testd.el 98:10
#stackVar char[9] str
STACK INC 12
//  char[9] str;

// 3 99:10
#line run/lang/TestD/testd.el 99:10
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 4
// Reserving r2
// Reserving r3
INC r2 8
// Releasing r3
// Releasing r1
LOAD r1 '\0' // \0
STORE BYTE r1 r2
// Releasing r2
// Releasing r1
//  str[8] = '\0';

// 4 100:10
#line run/lang/TestD/testd.el 100:10
// Reserving r1
COPY r15 r1
// Reserving r1
LOAD MEM r1 r1
INC r1 -255
SET FORCE EQ r1 r1 // code == 0xff
GOTO EQ r1 :if_end_15
// Releasing r1
// 0 101:14
#line run/lang/TestD/testd.el 101:14
// Reserving r1
#define exp_str_inline_0 "\n\nHalting\0"
LOAD r1 exp_str_inline_0 // \n\nHalting\0
STACK PUSH r1
LOAD r1 0 // 0
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
// Releasing r1
//  Console.printStr("\n\nHalting\0", 0);

// 1 102:14
#line run/lang/TestD/testd.el 102:14
HALT
//  asm("HALT");

#lineend
:if_end_15
//  if(code == 0xff) {Console.printStr("\n\nHalting\0", 0); asm("HALT");}

// 5 104:10
#line run/lang/TestD/testd.el 104:10
// Reserving r1
COPY r15 r1
// Reserving r1
LOAD MEM r1 r1
LOAD r2 -2147483646
SUB r1 r1 r2
SET FORCE EQ r1 r1 // code == 0x8000_0002
GOTO EQ r1 :if_end_16
// Releasing r1
// 0 105:14
#line run/lang/TestD/testd.el 105:14
// Reserving r1
LOAD r1 1 // 1
#stackVar uint32 i
STACK PUSH r1
// Releasing r1
//  uint32 i = 1;

// 1 106:14
#line run/lang/TestD/testd.el 106:14
:while_condition_17
// Reserving r1
COPY r15 r1
INC r1 16
// Reserving r1
LOAD MEM r1 r1
INC r1 -16
SET FORCE LT r1 r1 // i < 16
GOTO EQ r1 :while_end_17
// Releasing r1
// 0 107:18
#line run/lang/TestD/testd.el 107:18
// Reserving r1
LOAD r1 0 // TIMERS[i] == 0xffff_ffff
GOTO EQ r1 :if_end_18
// Releasing r1
// 0 108:22
#line run/lang/TestD/testd.el 108:22
// Reserving r1
// Reserving r2
LOAD r2 TestD.TIMERS
// Reserving r2
// Reserving r3
COPY r15 r3
INC r3 16
// Reserving r3
LOAD MEM r3 r3 // i
// Found Free register r4
LOAD r4 4
MUL r3 r3 r4
ADD r2 r2 r3
// Releasing r3
// Releasing r1
LOAD r1 0 // 0x0
STORE r1 r2
// Releasing r2
// Releasing r1
//  TIMERS[i] = 0x0;

#lineend
:if_end_18
//  if(TIMERS[i] == 0xffff_ffff) {TIMERS[i] = 0x0;}

// 1 110:18
#line run/lang/TestD/testd.el 110:18
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 16
// Reserving r2
// Releasing r1
LOAD MEM r1 r2
INC r1 1
STORE r1 r2
// Releasing r2
//  i++;

#lineend
GOTO :while_condition_17
:while_end_17
//  while(i < 16) {if(TIMERS[i] == 0xffff_ffff) {TIMERS[i] = 0x0;} i++;}

// 2 113:14
#line run/lang/TestD/testd.el 113:14
// Reserving r1
#define exp_str_inline_1 "\nTimer\0"
LOAD r1 exp_str_inline_1 // \nTimer\0
STACK PUSH r1
LOAD r1 0 // 0
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
// Releasing r1
//  Console.printStr("\nTimer\0", 0);

// 3 114:14
#line run/lang/TestD/testd.el 114:14
GOTO :func_exit_TestD.onInterrupt
//  return;

#lineend
STACK DEC 4
// End of scope
#stackVarClear i
:if_end_16
//  if(code == 0x8000_0002) {uint32 i = 1; while(i < 16) {if(TIMERS[i] == 0xffff_ffff) {TIMERS[i] = 0x0;} i++;} Console.printStr("\nTimer\0", 0); return;}

// 6 116:10
#line run/lang/TestD/testd.el 116:10
// Reserving r1
COPY r15 r1
// Reserving r1
LOAD MEM r1 r1 // code
STACK PUSH r1
COPY r15 r1
INC r1 4
// Reserving r1 // &str
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.intToHex_uint32_char*
STACK DEC 8
// Releasing r1
//  Console.intToHex(code, & str);

// 7 117:10
#line run/lang/TestD/testd.el 117:10
// Reserving r1
#define exp_str_inline_2 "\nInterrupt: \0"
LOAD r1 exp_str_inline_2 // \nInterrupt: \0
STACK PUSH r1
LOAD r1 0 // 0
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
// Releasing r1
//  Console.printStr("\nInterrupt: \0", 0);

// 8 118:10
#line run/lang/TestD/testd.el 118:10
// Reserving r1
COPY r15 r1
INC r1 4
// Reserving r1 // &str
STACK PUSH r1
LOAD r1 8 // 8
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
// Releasing r1
//  Console.printStr(& str, 8);

// 9 119:10
#line run/lang/TestD/testd.el 119:10
// Reserving r1
LOAD r1 '\n' // \n
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
// Releasing r1
//  Console.printChar('\n');

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
// 0 123:10
#line run/lang/TestD/testd.el 123:10
:while_condition_19
// Reserving r1
COPY r15 r1
INC r1 -12
// Reserving r1
LOAD MEM r1 r1
SET FORCE GT r1 r1 // time > 0
GOTO EQ r1 :while_end_19
// Releasing r1
// 0 124:14
#line run/lang/TestD/testd.el 124:14
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 -12
// Reserving r2
// Releasing r1
// Found Free register r1
LOAD MEM r1 r2
INC r1 -1
STORE r1 r2
// Releasing r2
//  time--;

#lineend
GOTO :while_condition_19
:while_end_19
//  while(time > 0) {time--;}

#lineend
:func_exit_TestD.wait_uint32
STACK POP r15
GOTO POP
#endfunction void

#function TestD.testRet
STACK PUSH r15
COPY rStack r15
// 0 134:10
#line run/lang/TestD/testd.el 134:10
// Reserving r1
LOAD r1 2000 // 2000
// Reserving r2
COPY r15 r2
INC r2 -12
STORE r1 r2
GOTO :func_exit_TestD.testRet
// Releasing r1
// Releasing r2
//  return 2000;

#lineend
:func_exit_TestD.testRet
STACK POP r15
GOTO POP
#endfunction uint32

:__start
#function TestD.main
STACK PUSH r15
COPY rStack r15
// 0 18:10
#line run/lang/TestD/testd.el 18:10
LOAD rIH &:TestD.onInterrupt
//  asm("LOAD rIH &:TestD.onInterrupt");

// 1 19:10
#line run/lang/TestD/testd.el 19:10
#stackVar uint32 b
STACK INC 4
//  uint32 b;

// 2 20:10
#line run/lang/TestD/testd.el 20:10
// Reserving r1
COPY rPgm r1 // SysD.rPgm
#stackVar uint32 a
STACK PUSH r1
// Releasing r1
//  uint32 a = SysD.rPgm;

// 3 21:10
#line run/lang/TestD/testd.el 21:10
// Reserving r1
// Reserving r2
LOAD r2 &TestD.v
// Reserving r2
// Releasing r1
COPY r15 r1
INC r1 4
// Reserving r1
LOAD MEM r1 r1 // a
STORE r1 r2
// Releasing r2
// Releasing r1
//  v = a;

// 4 22:10
#line run/lang/TestD/testd.el 22:10
#stackVar char c
STACK INC 4
//  char c;

// 5 23:10
#line run/lang/TestD/testd.el 23:10
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 8
// Reserving r2
// Releasing r1
COPY r15 r1
// Reserving r1
LOAD MEM r1 r1 // b
STORE BYTE r1 r2
// Releasing r2
// Releasing r1
//  c = b;

// 6 25:10
#line run/lang/TestD/testd.el 25:10
// Reserving r1
// Reserving r2
COPY r15 r2
// Reserving r2
// Releasing r1
COPY r15 r1
INC r1 4
// Reserving r1
LOAD MEM r1 r1
INC r1 1
 // Reserving r3
COPY r15 r3
INC r3 8
// Reserving r3
LOAD MEM BYTE r3 r3
ADD r1 r1 r3 // Releasing r3 // a + 1 + c
STORE r1 r2
// Releasing r2
// Releasing r1
//  b = a + 1 + c;

// 7 26:10
#line run/lang/TestD/testd.el 26:10
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 8
// Reserving r2
// Releasing r1
LOAD r1 32 // 32
STORE BYTE r1 r2
// Releasing r2
// Releasing r1
//  c = 32;

// 8 27:10
#line run/lang/TestD/testd.el 27:10
// Reserving r1
COPY r15 r1
INC r1 8
// Reserving r1
LOAD MEM BYTE r1 r1 // c
STACK PUSH r1
// Releasing r1
GOTO PUSH :TestD.funcb_uint32
STACK DEC 4
// Releasing r1
//  funcb(c);

// 9 28:10
#line run/lang/TestD/testd.el 28:10
LOAD r1 64
LOAD r2 &TestD.v
STORE r1 r2
//  asm("LOAD r1 64\nLOAD r2 &TestD.v\nSTORE r1 r2");

// 10 29:10
#line run/lang/TestD/testd.el 29:10
// Test
//  asm(str);

// 11 31:10
#line run/lang/TestD/testd.el 31:10
#stackVar StructA sA
STACK INC 8
//  StructA sA;

// 12 32:10
#line run/lang/TestD/testd.el 32:10
// Reserving r1
COPY r15 r1
INC r1 12
// Reserving r1 // &sA
STACK PUSH r1
// Releasing r1
GOTO PUSH :TestD.testA_StructA&
STACK DEC 4
// Releasing r1
//  testA(& sA);

// 13 35:10
#line run/lang/TestD/testd.el 35:10
// Reserving r1
#define exp_str_inline_3 "Starting EmulatorOS\n\n\0"
LOAD r1 exp_str_inline_3 // Starting EmulatorOS\n\n\0
STACK PUSH r1
LOAD r1 0 // 0
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
// Releasing r1
//  Console.printStr("Starting EmulatorOS\n\n\0", 0);

// 14 37:10
#line run/lang/TestD/testd.el 37:10
// Reserving r1
LOAD r1 &TestD.testStr
// Reserving r1 // &testStr
STACK PUSH r1
LOAD r1 5 // 5
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
// Releasing r1
//  Console.printStr(& testStr, 5);

// 15 38:10
#line run/lang/TestD/testd.el 38:10
// Reserving r1
LOAD r1 &TestD.testStr2
// Reserving r1 // &testStr2
STACK PUSH r1
LOAD r1 0 // 0
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
// Releasing r1
//  Console.printStr(& testStr2, 0);

// 16 39:10
#line run/lang/TestD/testd.el 39:10
// Reserving r1
LOAD r1 'a' // a
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
// Releasing r1
//  Console.printChar('a');

// 17 40:10
#line run/lang/TestD/testd.el 40:10
// Reserving r1
LOAD r1 '\n' // \n
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
// Releasing r1
//  Console.printChar('\n');

// 18 42:10
#line run/lang/TestD/testd.el 42:10
#stackVar char[10] str2
STACK INC 12
//  char[10] str2;

// 19 43:10
#line run/lang/TestD/testd.el 43:10
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 20
// Reserving r2
// Reserving r3
INC r2 8
// Releasing r3
// Releasing r1
LOAD r1 '\n' // \n
STORE BYTE r1 r2
// Releasing r2
// Releasing r1
//  str2[8] = '\n';

// 20 44:10
#line run/lang/TestD/testd.el 44:10
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 20
// Reserving r2
// Reserving r3
INC r2 9
// Releasing r3
// Releasing r1
LOAD r1 '\0' // \0
STORE BYTE r1 r2
// Releasing r2
// Releasing r1
//  str2[9] = '\0';

// 21 75:10
#line run/lang/TestD/testd.el 75:10
// Reserving r1
#define exp_str_inline_4 "\n> \0"
LOAD r1 exp_str_inline_4 // \n> \0
STACK PUSH r1
LOAD r1 0 // 0
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
// Releasing r1
//  Console.printStr("\n> \0", 0);

// 22 76:10
#line run/lang/TestD/testd.el 76:10
#stackVar char[32] buff
STACK INC 32
//  char[32] buff;

// 23 77:10
#line run/lang/TestD/testd.el 77:10
// Reserving r1
COPY r15 r1
INC r1 32
// Reserving r1 // &buff
STACK PUSH r1
LOAD r1 32 // 32
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.read_char*_uint32
STACK DEC 8
// Releasing r1
//  Console.read(& buff, 32);

// 24 78:10
#line run/lang/TestD/testd.el 78:10
// Reserving r1
COPY r15 r1
INC r1 32
// Reserving r1 // &buff
STACK PUSH r1
LOAD r1 0 // 0
STACK PUSH r1
// Releasing r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
// Releasing r1
//  Console.printStr(& buff, 0);

// 25 80:10
#line run/lang/TestD/testd.el 80:10
// Reserving r1
// Reserving r2
LOAD r2 TestD.TIMERS
// Reserving r2
// Reserving r3
INC r2 4
// Releasing r3
// Releasing r1
LOAD r1 2400 // 480 * 5
STORE r1 r2
// Releasing r2
// Releasing r1
//  TIMERS[1] = 480* 5;

// 26 82:10
#line run/lang/TestD/testd.el 82:10
// Reserving r1
// Reserving r2
STACK INC 4
GOTO PUSH :TestD.testRet
STACK POP r1
// Releasing r2 // testRet()
STACK PUSH r1
// Releasing r1
GOTO PUSH :TestD.wait_uint32
STACK DEC 4
// Releasing r1
//  wait(testRet());

#lineend
:func_exit_TestD.main
STACK DEC 64
// End of scope
#stackVarClear a
#stackVarClear b
#stackVarClear c
#stackVarClear str2
#stackVarClear buff
#stackVarClear sA
STACK POP r15
HALT
#endfunction void

#function TestD.funcb_uint32 a uint32
STACK PUSH r15
COPY rStack r15
#stackVar uint32 a -12
// 0 87:10
#line run/lang/TestD/testd.el 87:10
// Reserving r1
// Reserving r2
LOAD r2 &TestD.v
// Reserving r2
// Releasing r1
COPY r15 r1
INC r1 -12
// Reserving r1
LOAD MEM r1 r1 // a
// Found Free register r3
LOAD MEM r3 r2
ADD r1 r3 r1
STORE r1 r2
// Releasing r2
// Releasing r1
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
// 0 91:10
#line run/lang/TestD/testd.el 91:10
// Reserving r1
// Reserving r2
LOAD r2 &TestD.v
// Reserving r2
// Releasing r1
COPY r15 r1
INC r1 -16
// Reserving r1
LOAD MEM r1 r1 // a
// Found Free register r3
LOAD MEM r3 r2
ADD r1 r3 r1
STORE r1 r2
// Releasing r2
// Releasing r1
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
// 0 129:10
#line run/lang/TestD/testd.el 129:10
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 -12
// Reserving r2
LOAD MEM r2 r2
// Releasing r1
LOAD r1 32 // 32
STORE r1 r2
// Releasing r2
// Releasing r1
//  str.a = 32;

// 1 130:10
#line run/lang/TestD/testd.el 130:10
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 -12
// Reserving r2
LOAD MEM r2 r2
// Releasing r1
LOAD r1 -1 // 0xffffffff
STORE r1 r2
// Releasing r2
// Releasing r1
//  str.b = 0xffffffff;

#lineend
:func_exit_TestD.testA_StructA&
STACK POP r15
GOTO POP
#endfunction void

// TestD.StructA

// FS

#function FS.openFile_char*_out_uint32&_out_uint32& path char*, status out uint32&, handle out uint32&
STACK PUSH r15
COPY rStack r15
#stackVar char* path -20
#stackVar out uint32& handle -12
#stackVar out uint32& status -16
// 0 36:10
#line run/lang/TestD/fs.el 36:10
// Reserving r1
LOAD r1 16 // 0x10
#stackVar uint32[2] msg
STACK PUSH r1
// Releasing r1
// Reserving r1
COPY r15 r1
INC r1 -20
// Reserving r1
LOAD MEM r1 r1 // path
#stackVar uint32[2] msg
STACK PUSH r1
// Releasing r1
//  uint32[2] msg = {0x10, path};

// 1 37:10
#line run/lang/TestD/fs.el 37:10
// Reserving r1
LOAD r1 3 // 3
STACK PUSH r1
LOAD r1 2 // 2
STACK PUSH r1
COPY r15 r1
// Reserving r1 // &msg
STACK PUSH r1
// Releasing r1
GOTO PUSH :FS.peripheralCommand_uint32_uint32_uint32*
STACK DEC 12
// Releasing r1
//  peripheralCommand(3, 2, & msg);

// 2 38:10
#line run/lang/TestD/fs.el 38:10
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 -16
// Reserving r2
// Releasing r1
LOAD MEM r2 r2
LOAD r1 FS.RSP_STATUS
// Reserving r1

LOAD MEM r1 r1 // *RSP_STATUS
STORE r1 r2
// Releasing r2
// Releasing r1
//  status =* RSP_STATUS;

// 3 39:10
#line run/lang/TestD/fs.el 39:10
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 -12
// Reserving r2
// Releasing r1
LOAD MEM r2 r2
LOAD r1 FS.RSP_DATA_2
// Reserving r1

LOAD MEM r1 r1 // *RSP_DATA_2
STORE r1 r2
// Releasing r2
// Releasing r1
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
#line run/lang/TestD/fs.el 43:10
// Reserving r1
LOAD r1 17 // 0x11
#stackVar uint32[5] msg
STACK PUSH r1
// Releasing r1
// Reserving r1
COPY r15 r1
INC r1 -32
// Reserving r1
LOAD MEM r1 r1 // handle
#stackVar uint32[5] msg
STACK PUSH r1
// Releasing r1
// Reserving r1
COPY r15 r1
INC r1 -28
// Reserving r1
LOAD MEM r1 r1 // buffer
#stackVar uint32[5] msg
STACK PUSH r1
// Releasing r1
// Reserving r1
COPY r15 r1
INC r1 -24
// Reserving r1
LOAD MEM r1 r1 // size
#stackVar uint32[5] msg
STACK PUSH r1
// Releasing r1
// Reserving r1
COPY r15 r1
INC r1 -20
// Reserving r1
LOAD MEM r1 r1 // offset
#stackVar uint32[5] msg
STACK PUSH r1
// Releasing r1
//  uint32[5] msg = {0x11, handle, buffer, size, offset};

// 1 44:10
#line run/lang/TestD/fs.el 44:10
// Reserving r1
LOAD r1 3 // 3
STACK PUSH r1
LOAD r1 5 // 5
STACK PUSH r1
COPY r15 r1
// Reserving r1 // &msg
STACK PUSH r1
// Releasing r1
GOTO PUSH :FS.peripheralCommand_uint32_uint32_uint32*
STACK DEC 12
// Releasing r1
//  peripheralCommand(3, 5, & msg);

// 2 45:10
#line run/lang/TestD/fs.el 45:10
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 -12
// Reserving r2
// Releasing r1
LOAD MEM r2 r2
LOAD r1 FS.RSP_DATA
// Reserving r1

LOAD MEM r1 r1 // *RSP_DATA
STORE r1 r2
// Releasing r2
// Releasing r1
//  state =* RSP_DATA;

// 3 46:10
#line run/lang/TestD/fs.el 46:10
// Reserving r1
// Reserving r2
COPY r15 r2
INC r2 -16
// Reserving r2
// Releasing r1
LOAD MEM r2 r2
LOAD r1 FS.RSP_DATA_3
// Reserving r1

LOAD MEM r1 r1 // *RSP_DATA_3
STORE r1 r2
// Releasing r2
// Releasing r1
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
#line run/lang/TestD/fs.el 19:10
// Reserving r1
// Reserving r2
LOAD r2 FS.CMD_SIZE
// Reserving r2
// Releasing r1
COPY r15 r1
INC r1 -16
// Reserving r1
LOAD MEM r1 r1 // cmdSize
STORE r1 r2
// Releasing r2
// Releasing r1
// * CMD_SIZE = cmdSize;

// 1 21:10
#line run/lang/TestD/fs.el 21:10
LOAD r1 Console.CMD_START
//  asm("LOAD r1 Console.CMD_START");

// 2 23:10
#line run/lang/TestD/fs.el 23:10
COPY r15 r3
INC r3 -12
LOAD MEM r3 r3
//  asm("COPY r15 r3\nINC r3 -12\nLOAD MEM r3 r3");

// 3 25:10
#line run/lang/TestD/fs.el 25:10
:peripheralCommand_l0
//  asm(":peripheralCommand_l0");

// 4 27:10
#line run/lang/TestD/fs.el 27:10
COPY MEM r3 r1
//  asm("COPY MEM r3 r1");

// 5 28:10
#line run/lang/TestD/fs.el 28:10
INC r1 4
INC r3 4
INC r2 -1
//  asm("INC r1 4\nINC r3 4\nINC r2 -1");

// 6 30:10
#line run/lang/TestD/fs.el 30:10
GOTO GT r2 :peripheralCommand_l0
//  asm("GOTO GT r2 :peripheralCommand_l0");

// 7 32:10
#line run/lang/TestD/fs.el 32:10
// Reserving r1
// Reserving r2
LOAD r2 FS.CMD_ADDR
// Reserving r2
// Releasing r1
LOAD r1 16842752
COPY r15 r1
INC r1 -20
// Reserving r1
LOAD MEM r1 r1
OR r1 r1 r1 // 0x0101_0000 | deviceId
STORE r1 r2
// Releasing r2
// Releasing r1
// * CMD_ADDR = 0x0101_0000 | deviceId;

#lineend
:func_exit_FS.peripheralCommand_uint32_uint32_uint32*
STACK POP r15
GOTO POP
#endfunction void

HALT