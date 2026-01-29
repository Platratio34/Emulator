// mem[0x0800-0x0820] console

#define CMD_STATUS 0x8001
#define CMD_DEVICE 0x8002
#define CMD_SIZE 0x8003
#define CMD_0 0x8004
#define CMD_1 0x8005
#define CMD_2 0x8006
#define CMD_3 0x8007
#define CMD_4 0x8008
#define CMD_WRITTEN 0x0001

#define CONSOLE_START 0x0800
#define CONSOLE_END 0x0840
#define CONSOLE_PNTR 0x0841

#define SYS_NAME "EmulatorOS"
#define CONSOLE_SETUP_CMD [0x0001,CONSOLE_START,0x0020]

#syscall 0x0001 printChar
#syscall 0x0002 printStr
#syscall 0x0003 malloc
#syscall 0x0fff exit

LOAD r0 0x0001
LOAD r1 0x0003
LOAD r2 CONSOLE_SETUP_CMD
GOTO PUSH :peripheralCmd

LOAD r0 CONSOLE_PNTR
STORE CONSOLE_START r0

#define HEAP_START 0x9001
#define HEAP_POINTER 0x9000
LOAD r0 HEAP_POINTER
STORE HEAP_START r0

// goto startup program
#define PROGRAM_START 0x20000
LOAD rMBase PROGRAM_START
LOAD rMSize 0x1000
LOAD rPID 0x1
LOAD r0 0
SYSGOTO r0

HALT // just in case ;)

// void peripheralCmd(uint32 deviceID, uint32 cmdSize, uint32* cmd)
#function peripheralCmd r0 uint32 deviceId, r1 uint32 cmdSize, r2 uint32* cmd
// r3 void* memAddr
// r4 uint32* cmdValPntr
// r5 uint32 end
// r6 uint32 offset
STACK PUSH r3
STACK PUSH r4
STACK PUSH r5
STACK PUSH r6

LOAD r3 CMD_DEVICE // Device ID
STORE r0 r3
INC r3 // CMD Size
STORE r1 r3

COPY r2 r4 // cmdValPntr = cmd
ADD r5 r1 r2 // end = cmdSize + cmd

:peripheralCmd-loop // CMD
INC r3 // memAddr++
COPY MEM r4 r3 // mem[memAddr] = mem[cmdValPntr]
INC r4 // cmdValPntr++
SUB r6 r5 r4 // offset = end - cmdValPntr
GOTO GT r6 :peripheralCmd-loop // offset > 0 ? loop : exit

LOAD r3 CMD_STATUS // Mark written
STORE CMD_WRITTEN r3

STACK POP r6
STACK POP r5
STACK POP r4
STACK POP r3
GOTO POP
#endfunction void