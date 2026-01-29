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
#define STR1 "Test"

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
LOAD r12 0x001
LOAD r13 0x120 // console end

LOAD r11 'T'
GOTO PUSH :print
LOAD r11 'e'
GOTO PUSH :print
LOAD r11 's'
GOTO PUSH :print
LOAD r11 '1'
GOTO PUSH :print

LOAD r11 'T'
GOTO PUSH :print
LOAD r11 'e'
GOTO PUSH :print
LOAD r11 's'
GOTO PUSH :print
LOAD r11 '2'
GOTO PUSH :print

LOAD r11 'T'
GOTO PUSH :print
LOAD r11 'e'
GOTO PUSH :print
LOAD r11 's'
GOTO PUSH :print
LOAD r11 '3'
GOTO PUSH :print

LOAD r11 'T'
GOTO PUSH :print
LOAD r11 'e'
GOTO PUSH :print
LOAD r11 's'
GOTO PUSH :print
LOAD r11 '4'
GOTO PUSH :print

LOAD r11 'T'
GOTO PUSH :print
LOAD r11 'e'
GOTO PUSH :print
LOAD r11 's'
GOTO PUSH :print
LOAD r11 '5'
GOTO PUSH :print

HALT

:print
STORE r11 r10
ADD r10 r10 r12
STORE CMD_WRITTEN r10
ADD r10 r10 r12
SUB r14 r13 r10
GOTO LEQ r14 :printR
GOTO POP

:printR
LOAD r10 CONSOLE_START
GOTO POP