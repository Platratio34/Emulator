#define CMD_STATUS 0x8001
#define CMD_DEVICE 0x8002
#define CMD_SIZE 0x8003
#define CMD_0 0x8004
#define CMD_1 0x8005
#define CMD_2 0x8006
#define CMD_3 0x8007
#define CMD_4 0x8008
#define CMD_WRITTEN 0x0001

#define CONSOLE_START 0x0100
// char*
#define STR1 "Test\t"
#define STR2 "Tes2\\"

// Setup console
LOAD r1 CMD_DEVICE // Device ID
STORE 0x0001 r1
LOAD r1 CMD_SIZE // CMD Size
STORE 0x0003 r1
LOAD r1 CMD_0 // SETUP
STORE 0x0001 r1
LOAD r1 CMD_1 // Queue start
STORE 0x0100 r1
LOAD r1 CMD_2 // Queue size
STORE 0x0010 r1
LOAD r1 CMD_STATUS // Mark written
STORE CMD_WRITTEN r1

LOAD r10 CONSOLE_START // console start
LOAD r13 0x120 // console end

LOAD r16 5
LOAD r1 0x00
LOAD r20 0x10
:loop
LOAD r15 STR1
GOTO PUSH :printS
LOAD r15 STR2
GOTO PUSH :printS
LOAD r11 '\n'
GOTO PUSH :print
INC r1
SUB r2 r20 r1
GOTO GT r2 :loop

HALT

:printS
// r15 const char* str
// r16 const uint32 size
// r1 char* c
// r2 uint32 end
// r14 uint32 off
STACK PUSH r1
STACK PUSH r2
COPY r15 r1
ADD r2 r15 r16
:printS-loop
LOAD MEM r11 r1
GOTO PUSH :print
INC r1
SUB r14 r2 r1
GOTO LEQ r14 :printS-exit
GOTO :printS-loop
:printS-exit
STACK POP r2
STACK POP r1
GOTO POP

:print
// r10 uint32* consolePntr
// r11 const char* c
// r13 const uint32 CONSOLE_END
// r14 uint32 off
STORE r11 r10
INC r10
STORE CMD_WRITTEN r10
INC r10
SUB r14 r13 r10
GOTO LEQ r14 :printR
GOTO POP

:printR
LOAD r10 CONSOLE_START
GOTO POP