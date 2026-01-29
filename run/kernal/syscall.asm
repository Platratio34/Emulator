#include _kernal.obj

// void syscall::printChar(const char r0 c)
#function syscall::printChar r0 const char c
GOTO PUSH :printChar
SYSRETURN
#endfunction void

#function printChar r0 const char c
// r1 const Queue** queueIndexPntr
// r2 Queue* queue
// r3 uint32 queueEndOffset
STACK PUSH r1
STACK PUSH r2
STACK PUSH r3

LOAD r1 CONSOLE_PNTR

// write to queue
LOAD MEM r2 r1
STORE r0 r2
INC r2
STORE 0x1 r2
INC r2

// check wrap
LOAD r3 CONSOLE_END
SUB r3 r3 r2
GOTO GT r3 :printChar-return
LOAD r2 CONSOLE_START
:printChar-return
STORE r2 r1

STACK POP r3
STACK POP r2
STACK POP r1

GOTO POP
#endfunction void

// void syscall::printStr(const char* r0 str, const uint32 r1 length)
#function syscall::printStr r0 const char* str, r1 const uint32 length
STACK PUSH r0
ADD r0 r0 rMBase
GOTO PUSH :printStr
STACK POP r0
SYSRETURN
#endfunction void

#function printStr r0 const char* str, r1 const uint32 length
// r1 uint32 end
// r3 uint32 offset
// r4 char* c
STACK PUSH r0
STACK PUSH r1
STACK PUSH r2
STACK PUSH r3

COPY r0 r4
ADD r1 r1 r4

:printStr-loop
LOAD MEM r0 r4
GOTO PUSH :printChar
INC r4
SUB r3 r1 r4
GOTO GT r3 :printStr-loop

STACK POP r3
STACK POP r2
STACK POP r1
STACK POP r0

GOTO POP
#endfunction void

// void syscall::malloc(uint32 size, void* pntr)
#function syscall::malloc r0 uint32 size, r1 void* pntr
// r2 uint3 nextAddr
// r3 void* heapPntr
STACK PUSH r2
STACK PUSH r3

LOAD r3 HEAP_POINTER
LOAD MEM r1 r3 // pntr = mem[heapPntr]
ADD r2 r1 r0 // nextAddr = pntr + size
STORE r2 r3 // mem[heapPntr] = nextAddr

SUB r1 r1 rMBase

STACK POP r3
STACK POP r2
SYSRETURN
#endfunction void

#define EXIT_MSG "\nProcess exit with code "
#function syscall::exit r0 uint32 exitCode
COPY r0 r2
INC r2 '0'
LOAD r0 EXIT_MSG
LOAD r1 24
GOTO PUSH :printStr
COPY r2 r0
GOTO PUSH :printChar
HALT
#endfunction void