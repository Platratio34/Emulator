#define PATH "kernal.bin\0"
#define PERIPHERAL_COMMAND_START 0x1_0004
#define PERIPHERAL_COMMAND_EX 0x1_0000
#define PERIPHERAL_COMMAND_EX_VAL 0x0101_0003
#define PERIPHERAL_COMMAND_STATUS 0x1_0084
#define STORAGE_COMMAND_OPEN 0x10
#define STORAGE_COMMAND_READ 0x11
#define KERNAL_START 0x0_1000

// (uint32 status, uint32 handle) openFile(uint32 commandSize, uint32 command, char* path)
// size
LOAD r0 PERIPHERAL_COMMAND_START
LOAD r1 0x2
STORE r1 r0 INC_RA

// command
LOAD r1 STORAGE_COMMAND_OPEN
STORE r1 r0 INC_RA

// path
LOAD r1 PATH
STORE r1 r0 INC_RA

// execute
LOAD r0 PERIPHERAL_COMMAND_EX
LOAD r1 PERIPHERAL_COMMAND_EX_VAL
STORE r1 r0

// check status and halt if failed
LOAD r0 PERIPHERAL_COMMAND_STATUS
LOAD MEM r1 r0
INC r1 -1
GOTO NEQ r1 :halt

// load the handle into r2
INC r0 4
LOAD MEM r2 r0

// readFile(uint32 commandSize, uint32 command, uint32 handle, void* buffer, uint32 bufferSize, uint32 offset)
// size
LOAD r0 PERIPHERAL_COMMAND_START
LOAD r1 0x5
STORE r1 r0 INC_RA

// command
LOAD r1 STORAGE_COMMAND_READ
STORE r1 r0 INC_RA

// handle
STORE r2 r0 INC_RA

// buffer
LOAD r1 KERNAL_START
STORE r1 r0 INC_RA

// bufferSize
LOAD r1 0x1_0000
STORE r1 r0 INC_RA

// offset
LOAD r1 0
STORE r1 r0 INC_RA

// execute
LOAD r0 PERIPHERAL_COMMAND_EX
LOAD r1 PERIPHERAL_COMMAND_EX_VAL
STORE r1 r0

// check status and halt if failed
LOAD r0 PERIPHERAL_COMMAND_STATUS
LOAD MEM r1 r0
INC r1 -1
GOTO NEQ r1 :halt

LOAD r0 KERNAL_START
GOTO r0

:halt
HALT