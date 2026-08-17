#define PATH "kernal.bin\0"
#define PERIPHERAL_COMMAND_START 0x1_0004
#define PERIPHERAL_COMMAND_EX 0x1_0000
#define PERIPHERAL_COMMAND_EX_VAL 0x0101_0001
#define PERIPHERAL_COMMAND_STATUS 0x1_0084
#define STORAGE_COMMAND_OPEN 0x10
#define STORAGE_COMMAND_READ 0x11
#define KERNAL_START 0x0_1000
#define CONSOLE_OUT 0x1_0300

LOAD r7 CONSOLE_OUT
// (uint32 status, uint32 handle) openFile(uint32 commandSize, uint32 command, char* path)
LOAD r0 PERIPHERAL_COMMAND_START
STORE 0x2 r0 INC_RA // size
STORE STORAGE_COMMAND_OPEN r0 INC_RA // command
STORE PATH r0 INC_RA // path

// execute
LOAD r0 PERIPHERAL_COMMAND_EX
STORE PERIPHERAL_COMMAND_EX_VAL r0

// check status and halt if failed
LOAD r0 PERIPHERAL_COMMAND_STATUS
LOAD MEM r1 r0
INC r1 -1
GOTO EQ r1 :openSuccess

STORE BYTE 'e' r7
STORE BYTE 'o' r7
HALT

:openSuccess
STORE BYTE 's' r7
STORE BYTE 'o' r7
STORE BYTE '\n' r7

// load the handle into r2
INC r0 4
LOAD MEM r2 r0

// readFile(uint32 commandSize, uint32 command, uint32 handle, void* buffer, uint32 bufferSize, uint32 offset)
LOAD r0 PERIPHERAL_COMMAND_START
STORE 0x5 r0 INC_RA // size
STORE STORAGE_COMMAND_READ r0 INC_RA // command
STORE r2 r0 INC_RA // handle
STORE KERNAL_START r0 INC_RA // buffer
STORE 0x1_0000 r0 INC_RA // bufferSize
STORE 0 r0 INC_RA // offset

// execute
LOAD r0 PERIPHERAL_COMMAND_EX
STORE PERIPHERAL_COMMAND_EX_VAL r0

// check status and halt if failed
LOAD r0 PERIPHERAL_COMMAND_STATUS
LOAD MEM r1 r0
INC r1 -1
GOTO EQ r1 :startKernal

STORE BYTE 'e' r7
STORE BYTE 'o' r7
HALT

:startKernal
STORE BYTE 's' r7
STORE BYTE 'c' r7
STORE BYTE '\n' r7

LOAD r0 KERNAL_START
GOTO r0


:halt
HALT