#include kernal.obj

#define STACK_PNTR (256)
LOAD rStack STACK_PNTR

#define STR "This is a test string"
LOAD r0 STR

// this is only to fix the absolute offset
// COPY rPgm r1
// INC r1 -3
// ADD r0 r0 r1

LOAD r1 21
SYSCALL printStr
LOAD r0 '\n'
SYSCALL printChar
LOAD r0 '2'
SYSCALL printChar
INC r0 -2
SYSCALL printChar

LOAD r0 0x1
SYSCALL malloc

HALT
GOTO PUSH :f3

LOAD r0 0x0
SYSCALL exit

#function f1
LOAD r0 0xffffff
LOAD MEM r0 r0
GOTO POP
#endfunction void

#function f2
GOTO PUSH :f1
GOTO POP
#endfunction void

#function f3
GOTO PUSH :f2
GOTO POP
#endfunction void