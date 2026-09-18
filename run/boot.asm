#define PATH "kernal.bin\0"
#define PERIPHERAL_COMMAND_START 0x1_0004
#define PERIPHERAL_COMMAND_EX 0x1_0000
#define PERIPHERAL_COMMAND_EX_VAL 0x0101_0001
#define PERIPHERAL_COMMAND_STATUS 0x1_0084
#define PERIPHERAL_COMMAND_D0 0x1_0088
#define STORAGE_COMMAND_OPEN 0x10
#define STORAGE_COMMAND_READ 0x11
#define KERNAL_START 0x0_1000
#define CONSOLE_OUT 0x1_0300
#var readCount 0 int32

:start
GOTO NEQ rID :start

LOAD r7 CONSOLE_OUT
// (int32 status, int32 handle) openFile(int32 commandSize, int32 command, char* path)
LOAD r0 PERIPHERAL_COMMAND_START
STORE 0x2 r0 INC_RA // size
STORE STORAGE_COMMAND_OPEN r0 INC_RA // command
STORE PATH r0 INC_RA // path

// execute
LOAD r0 PERIPHERAL_COMMAND_EX
STORE PERIPHERAL_COMMAND_EX_VAL r0

// check status and halt if failed
LOAD MEM r1 PERIPHERAL_COMMAND_STATUS
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
LOAD MEM r2 PERIPHERAL_COMMAND_D0

// readFile(int32 commandSize, int32 command, int32 handle, void* buffer, int32 bufferSize, int32 offset)
LOAD r0 PERIPHERAL_COMMAND_START
STORE 0x6 r0 INC_RA // size
STORE STORAGE_COMMAND_READ r0 INC_RA // command
STORE r2 r0 INC_RA // handle
STORE KERNAL_START r0 INC_RA // buffer
STORE 0x1_0000 r0 INC_RA // bufferSize
STORE 0 r0 INC_RA // offset
STORE &readCount r0 INC_RA // offset

// execute
LOAD r0 PERIPHERAL_COMMAND_EX
STORE PERIPHERAL_COMMAND_EX_VAL r0

// check status and halt if failed
LOAD MEM r1 PERIPHERAL_COMMAND_STATUS
INC r1 -1
GOTO EQ r1 :startKernal

STORE BYTE 'e' r7
STORE BYTE 'r' r7
HALT

:startKernal
STORE BYTE 's' r7
STORE BYTE 'r' r7
STORE BYTE '\n' r7

:wait
LOAD MEM r1 &readCount
GOTO LT r1 :wait

LOAD r0 KERNAL_START
GOTO r0


:halt
HALT