// static data
// Console
#define Console.CMD_DEVICE 0x0002_0002 // uint32*
#define Console.CMD_WRITTEN 0x0001 // uint32
#define Console.CONSOLE_END 0x0002_0200 // uint32
#define Console.CMD_ADDR 0x0002_0000 // uint32*
#var Console.CONSOLE_SETUP_CMD [0x0001,0x0002_0100,0x0002_0200] // uint32[3]
#define Console.CMD_STATUS 0x0002_0001 // uint32*
#define Console.CMD_START 0x0002_0008 // uint32*
#define Console.CMD_SIZE 0x0002_0004 // uint32*
#define Console.CONSOLE_START 0x0002_0100 // uint32
#var Console.consolePntr Console.CONSOLE_START // void*
// FS
#define FS.CMD_DEVICE 0x0002_0002 // uint32*
#define FS.CMD_WRITTEN 0x0001 // uint32
#define FS.RSP_DATA_2 0x0002_0088 // uint32*
#define FS.RSP_DATA_3 0x0002_008c // uint32*
#define FS.CMD_ADDR 0x0002_0000 // uint32*
#define FS.RSP_STATUS 0x0002_0080 // uint32*
#define FS.CMD_STATUS 0x0002_0001 // uint32*
#define FS.RSP_DATA 0x0002_0084 // uint32*
#define FS.CMD_START 0x0002_0008 // uint32*
#define FS.CMD_SIZE 0x0002_0004 // uint32*
// TestD
#define TestD.str "// Test" // char*
#var TestD.path "test.txt\0" // char[9]
#var TestD.v 0x0000 // uint32
#var TestD.testStr2 "Test2\n\0" // char[7]
#var TestD.testStr "Test\n" // char[5]
#var TestD.tc 0x00 // char

//--------
// text

// Console

#function Console.setupConsole
STACK PUSH r15
COPY rStack r15
// 0 36:10
LOAD r1 1
STACK PUSH r1
LOAD r1 3
STACK PUSH r1
LOAD r1 &Console.CONSOLE_SETUP_CMD
STACK PUSH r1
GOTO PUSH :Console.peripheralCommand_uint32_uint32_uint32*
STACK DEC 12
//  peripheralCommand(0x001, 0x003, & CONSOLE_SETUP_CMD)

// 1 36:61
// ;

:func_exit_Console.setupConsole
STACK POP r15
GOTO POP
#endfunction void

#function Console.peripheralCommand_uint32_uint32_uint32* deviceId uint32, cmdSize uint32, cmd uint32*
STACK PUSH r15
COPY rStack r15
// 0 19:10
LOAD r1 Console.CMD_SIZE
COPY r15 r2
INC r2 -16
LOAD MEM r2 r2
STORE r2 r1
// * CMD_SIZE = cmdSize;

// 1 21:10
LOAD r1 Console.CMD_START
//  asm("LOAD r1 Console.CMD_START");

// 2 23:10
COPY r15 r3
INC r3 -12
LOAD MEM r3 r3
//  asm("COPY r15 r3\nINC r3 -12\nLOAD MEM r3 r3");

// 3 25:10
:peripheralCommand_l0
//  asm(":peripheralCommand_l0");

// 4 27:10
COPY MEM r3 r1
//  asm("COPY MEM r3 r1");

// 5 28:10
INC r1 4
INC r3 4
INC r2 -1
//  asm("INC r1 4\nINC r3 4\nINC r2 -1");

// 6 30:10
GOTO GT r2 :peripheralCommand_l0
//  asm("GOTO GT r2 :peripheralCommand_l0");

// 7 32:10
LOAD r1 Console.CMD_ADDR
LOAD r2 16842752
COPY r15 r3
INC r3 -20
LOAD MEM r3 r3
OR r2 r2 r3
STORE r2 r1
// * CMD_ADDR = 0x0101_0000 | deviceId;

:func_exit_Console.peripheralCommand_uint32_uint32_uint32*
STACK POP r15
GOTO POP
#endfunction void

#function Console.printStr_char*_uint32 str char*, len uint32
STACK PUSH r15
COPY rStack r15
// 0 46:10
COPY r15 r14
INC r14 -12
LOAD MEM r14 r14
//  asm("COPY r15 r14\nINC r14 -12\nLOAD MEM r14 r14");

// 1 47:10
LOAD r1 Console.CONSOLE_START
//  asm("LOAD r1 Console.CONSOLE_START");

// 2 48:10
COPY r15 r2
INC r2 -16
LOAD MEM r2 r2
//  asm("COPY r15 r2\nINC r2 -16\nLOAD MEM r2 r2");

// 3 49:10
LOAD r4 Console.CONSOLE_END
LOAD r5 Console.CONSOLE_START
//  asm("LOAD r4 Console.CONSOLE_END\nLOAD r5 Console.CONSOLE_START");

// 4 50:10
GOTO GT r14 :printStr_len
//  asm("GOTO GT r14 :printStr_len");

// 5 51:14
:printStr_l1
//  asm(":printStr_l1");

// 6 52:18
LOAD MEM BYTE r3 r2
GOTO EQ r3 :printStr_l1_exit
//  asm("LOAD MEM BYTE r3 r2\nGOTO EQ r3 :printStr_l1_exit");

// 7 53:18
STORE BYTE r3 r1
INC r2 1
GOTO :printStr_l1
//  asm("STORE BYTE r3 r1\nINC r2 1\nGOTO :printStr_l1");

// 8 54:14
:printStr_l1_exit
GOTO :printStr_exit
//  asm(":printStr_l1_exit\nGOTO :printStr_exit");

// 9 55:10
:printStr_len
//  asm(":printStr_len");

// 10 56:14
COPY BYTE MEM r2 r1 INC_RG
//  asm("COPY BYTE MEM r2 r1 INC_RG");

// 11 57:14
INC r14 -1
GOTO GT r14 :printStr_len
//  asm("INC r14 -1\nGOTO GT r14 :printStr_len");

// 12 58:10
:printStr_exit
//  asm(":printStr_exit")

// 13 58:31
// ;

:func_exit_Console.printStr_char*_uint32
STACK POP r15
GOTO POP
#endfunction void

#function Console.printChar_char c char
STACK PUSH r15
COPY rStack r15
// 0 40:10
LOAD r1 Console.CONSOLE_START
//  asm("LOAD r1 Console.CONSOLE_START");

// 1 41:10
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
//  asm("COPY r15 r2\nINC r2 -12\nLOAD MEM r2 r2");

// 2 42:10
STORE BYTE r2 r1
//  asm("STORE BYTE r2 r1")

// 3 42:33
// ;

:func_exit_Console.printChar_char
STACK POP r15
GOTO POP
#endfunction void

#function Console.intToHex_uint32_char* value uint32, str char*
STACK PUSH r15
COPY rStack r15
// 0 62:10
LOAD r14 7
//  asm("LOAD r14 7");

// 1 63:10
COPY r15 r1
INC r1 -16
LOAD MEM r1 r1
//  asm("COPY r15 r1\nINC r1 -16\nLOAD MEM r1 r1");

// 2 64:10
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
INC r2 8
//  asm("COPY r15 r2\nINC r2 -12\nLOAD MEM r2 r2\nINC r2 8");

// 3 65:10
LOAD r3 0xf
LOAD r6 0xa
//  asm("LOAD r3 0xf\nLOAD r6 0xa");

// 4 66:10
:intToHex_l1
//  asm(":intToHex_l1");

// 5 67:14
INC r2 -1
AND r4 r1 r3
RSH r1 r1 4
//  asm("INC r2 -1\nAND r4 r1 r3\nRSH r1 r1 4");

// 6 68:14
SUB r5 r4 r6
GOTO GEQ r5 :intToHex_gt
//  asm("SUB r5 r4 r6\nGOTO GEQ r5 :intToHex_gt");

// 7 69:18
INC r4 0x30
STORE BYTE r4 r2
GOTO :intToHex_l1_end
//  asm("INC r4 0x30\nSTORE BYTE r4 r2\nGOTO :intToHex_l1_end");

// 8 70:14
:intToHex_gt
//  asm(":intToHex_gt");

// 9 71:18
INC r4 0x57
STORE BYTE r4 r2
//  asm("INC r4 0x57\nSTORE BYTE r4 r2");

// 10 72:14
:intToHex_l1_end
INC r14 -1
GOTO GEQ r14 :intToHex_l1
//  asm(":intToHex_l1_end\nINC r14 -1\nGOTO GEQ r14 :intToHex_l1")

// 11 72:76
// ;

:func_exit_Console.intToHex_uint32_char*
STACK POP r15
GOTO POP
#endfunction void

// FS

#function FS.openFile_char*_uint32& path char*, handle uint32&
STACK PUSH r15
COPY rStack r15
// 0 36:10
LOAD r1 16
STACK PUSH r1
COPY r15 r1
INC r1 -16
LOAD MEM r1 r1
STACK PUSH r1
//  uint32[2] msg = {0x10, path};

// 1 37:10
LOAD r1 2
STACK PUSH r1
LOAD r1 2
STACK PUSH r1
COPY r15 r1
STACK PUSH r1
GOTO PUSH :FS.peripheralCommand_uint32_uint32_uint32*
STACK DEC 12
//  peripheralCommand(2, 2, & msg);

// 2 39:10
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD r2 FS.RSP_DATA_2
LOAD MEM r2 r2
STORE r2 r1
//  handle =* RSP_DATA_2;

:func_exit_FS.openFile_char*_uint32&
STACK DEC 8
STACK POP r15
GOTO POP
#endfunction void

#function FS.readFile_uint32_void*_uint32_uint32_uint32&_uint32& handle uint32, buffer void*, size uint32, offset uint32, read uint32&, state uint32&
STACK PUSH r15
COPY rStack r15
// 0 43:10
LOAD r1 17
STACK PUSH r1
COPY r15 r1
INC r1 -32
LOAD MEM r1 r1
STACK PUSH r1
COPY r15 r1
INC r1 -28
LOAD MEM r1 r1
STACK PUSH r1
COPY r15 r1
INC r1 -24
LOAD MEM r1 r1
STACK PUSH r1
COPY r15 r1
INC r1 -20
LOAD MEM r1 r1
STACK PUSH r1
//  uint32[5] msg = {0x11, handle, buffer, size, offset};

// 1 44:10
LOAD r1 2
STACK PUSH r1
LOAD r1 5
STACK PUSH r1
COPY r15 r1
STACK PUSH r1
GOTO PUSH :FS.peripheralCommand_uint32_uint32_uint32*
STACK DEC 12
//  peripheralCommand(2, 5, & msg);

// 2 45:10
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD r2 FS.RSP_DATA
LOAD MEM r2 r2
STORE r2 r1
//  state =* RSP_DATA;

// 3 46:10
COPY r15 r1
INC r1 -16
LOAD MEM r1 r1
LOAD r2 FS.RSP_DATA_3
LOAD MEM r2 r2
STORE r2 r1
//  read =* RSP_DATA_3;

:func_exit_FS.readFile_uint32_void*_uint32_uint32_uint32&_uint32&
STACK DEC 20
STACK POP r15
GOTO POP
#endfunction void

#function FS.peripheralCommand_uint32_uint32_uint32* deviceId uint32, cmdSize uint32, cmd uint32*
STACK PUSH r15
COPY rStack r15
// 0 19:10
LOAD r1 FS.CMD_SIZE
COPY r15 r2
INC r2 -16
LOAD MEM r2 r2
STORE r2 r1
// * CMD_SIZE = cmdSize;

// 1 21:10
LOAD r1 Console.CMD_START
//  asm("LOAD r1 Console.CMD_START");

// 2 23:10
COPY r15 r3
INC r3 -12
LOAD MEM r3 r3
//  asm("COPY r15 r3\nINC r3 -12\nLOAD MEM r3 r3");

// 3 25:10
:peripheralCommand_l0
//  asm(":peripheralCommand_l0");

// 4 27:10
COPY MEM r3 r1
//  asm("COPY MEM r3 r1");

// 5 28:10
INC r1 4
INC r3 4
INC r2 -1
//  asm("INC r1 4\nINC r3 4\nINC r2 -1");

// 6 30:10
GOTO GT r2 :peripheralCommand_l0
//  asm("GOTO GT r2 :peripheralCommand_l0");

// 7 32:10
LOAD r1 FS.CMD_ADDR
LOAD r2 16842752
COPY r15 r3
INC r3 -20
LOAD MEM r3 r3
OR r2 r2 r3
STORE r2 r1
// * CMD_ADDR = 0x0101_0000 | deviceId;

:func_exit_FS.peripheralCommand_uint32_uint32_uint32*
STACK POP r15
GOTO POP
#endfunction void

// TestD

#function TestD.onInterrupt
STACK PUSH r15
COPY rStack r15
// 0 81:10
COPY rIC r1
STACK PUSH r1
//  uint32 code = SysD.rIC;

// 1 82:10
LOAD rIC 0
//  asm("LOAD rIC 0");

// 2 83:10
COPY r15 r1
LOAD MEM r1 r1
INC r1 -255
SET FORCE EQ r1 r1
GOTO EQ r1 :if_end_0
// 0 84:14
HALT
//  asm("HALT")

// 1 84:25
// ;

:if_end_0
//  if(code == 0xff) {asm("HALT");}

:func_exit_TestD.onInterrupt
STACK DEC 4
STACK POP r15
INTERRUPT RET
#endfunction void

#function TestD.wait_uint32 time uint32
STACK PUSH r15
COPY rStack r15
// 0 89:10
:while_condition_1
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
SET FORCE GT r1 r1
GOTO EQ r1 :while_end_1
// 0 90:14
COPY r15 r1
INC r1 -12
LOAD MEM r2 r1
INC r2 -1
STORE r2 r1
//  time--;

GOTO :while_condition_1
:while_end_1
//  while(time > 0) {time--;}

:func_exit_TestD.wait_uint32
STACK POP r15
GOTO POP
#endfunction void

:__start
#function TestD.main
STACK PUSH r15
COPY rStack r15
// 0 16:10
LOAD rIH &:TestD.onInterrupt
//  asm("LOAD rIH &:TestD.onInterrupt");

// 1 17:10
STACK INC 4
//  uint32 b;

// 2 18:10
COPY rPgm r1
STACK PUSH r1
//  uint32 a = SysD.rPgm;

// 3 19:10
LOAD r1 &TestD.v
COPY r15 r2
INC r2 4
LOAD MEM r2 r2
STORE r2 r1
//  v = a;

// 4 20:10
STACK INC 4
//  char c;

// 5 21:10
COPY r15 r1
INC r1 8
COPY r15 r2
LOAD MEM r2 r2
STORE BYTE r2 r1
//  c = b;

// 6 23:10
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

// 7 24:10
COPY r15 r1
INC r1 8
LOAD r2 32
STORE BYTE r2 r1
//  c = 32;

// 8 25:10
COPY r15 r1
INC r1 8
LOAD MEM BYTE r1 r1
STACK PUSH r1
GOTO PUSH :TestD.funcb_uint32
STACK DEC 4
//  funcb(c);

// 9 26:10
LOAD r1 64
LOAD r2 &TestD.v
STORE r1 r2
//  asm("LOAD r1 64\nLOAD r2 &TestD.v\nSTORE r1 r2");

// 10 27:10
// Test
//  asm(str);

// 11 29:10
STACK INC 16
//  StructA sA;

// 12 30:10
STACK INC 4
COPY r15 r1
INC r1 12
STACK PUSH r1
GOTO PUSH :TestD.testA_StructA&
STACK DEC 4
//  testA(& sA);

// 13 34:10
LOAD r1 &TestD.testStr
STACK PUSH r1
LOAD r1 5
STACK PUSH r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
//  Console.printStr(& testStr, 5);

// 14 35:10
LOAD r1 &TestD.testStr2
STACK PUSH r1
LOAD r1 0
STACK PUSH r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
//  Console.printStr(& testStr2, 0);

// 15 36:10
LOAD r1 'a'
STACK PUSH r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
//  Console.printChar('a');

// 16 37:10
LOAD r1 '\n'
STACK PUSH r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
//  Console.printChar('\n');

// 17 40:10
STACK INC 4
//  uint32 fh;

// 18 41:10
#define exp_str_0 "test.txt\0"
LOAD r1 exp_str_0
STACK PUSH r1
COPY r15 r1
INC r1 28
STACK PUSH r1
GOTO PUSH :FS.openFile_char*_uint32&
STACK DEC 8
//  FS.openFile("test.txt\0", & fh);

// 19 42:10
COPY r15 r1
INC r1 28
LOAD MEM r1 r1
SET FORCE EQ r1 r1
GOTO EQ r1 :if_else_2
// 0 43:14
#define exp_str_1 "ERROR\n\0"
LOAD r1 exp_str_1
STACK PUSH r1
LOAD r1 0
STACK PUSH r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
//  Console.printStr("ERROR\n\0", 0)

// 1 43:46
// ;

GOTO :if_end_2
:if_else_2
// 0 45:14
#define exp_str_2 "Opened\n\0"
LOAD r1 exp_str_2
STACK PUSH r1
LOAD r1 0
STACK PUSH r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
//  Console.printStr("Opened\n\0", 0);

// 1 46:14
STACK INC 32
//  char[32] buffer;

// 2 47:14
STACK INC 4
//  uint32 read;

// 3 48:14
STACK INC 4
//  uint32 state;

// 4 49:14
COPY r15 r1
INC r1 28
LOAD MEM r1 r1
STACK PUSH r1
COPY r15 r1
INC r1 32
STACK PUSH r1
LOAD r1 32
STACK PUSH r1
LOAD r1 0
STACK PUSH r1
COPY r15 r1
INC r1 64
STACK PUSH r1
COPY r15 r1
INC r1 68
STACK PUSH r1
GOTO PUSH :FS.readFile_uint32_void*_uint32_uint32_uint32&_uint32&
STACK DEC 24
//  FS.readFile(fh, & buffer, 32, 0, & read, & state);

// 5 50:14
#breakpoint
//  asm("#breakpoint");

// 6 51:14
STACK INC 12
//  char[10] str2;

// 7 52:14
COPY r15 r1
INC r1 72
LOAD r2 8
LOAD MEM r1 r1
ADD r1 r1 r2
LOAD r2 '\n'
STORE BYTE r2 r1
//  str2[8] = '\n';

// 8 53:14
COPY r15 r1
INC r1 72
LOAD r2 9
LOAD MEM r1 r1
ADD r1 r1 r2
LOAD r2 '\0'
STORE BYTE r2 r1
//  str2[9] = '\0';

// 9 54:14
COPY r15 r1
INC r1 68
LOAD MEM r1 r1
STACK PUSH r1
COPY r15 r1
INC r1 72
STACK PUSH r1
GOTO PUSH :Console.intToHex_uint32_char*
STACK DEC 8
//  Console.intToHex(state, & str2);

// 10 55:14
COPY r15 r1
INC r1 72
STACK PUSH r1
LOAD r1 0
STACK PUSH r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
//  Console.printStr(& str2, 0);

// 11 56:14
LOAD r1 '\n'
STACK PUSH r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
//  Console.printChar('\n');

// 12 58:14
COPY r15 r1
INC r1 64
LOAD MEM r1 r1
STACK PUSH r1
COPY r15 r1
INC r1 72
STACK PUSH r1
GOTO PUSH :Console.intToHex_uint32_char*
STACK DEC 8
//  Console.intToHex(read, & str2);

// 13 59:14
COPY r15 r1
INC r1 72
STACK PUSH r1
LOAD r1 0
STACK PUSH r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
//  Console.printStr(& str2, 0);

// 14 61:14
LOAD r1 '\n'
STACK PUSH r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
//  Console.printChar('\n');

// 15 62:14
COPY r15 r1
INC r1 32
STACK PUSH r1
COPY r15 r1
INC r1 64
LOAD MEM r1 r1
STACK PUSH r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
//  Console.printStr(& buffer, read)

// 16 62:45
// ;

STACK DEC 52
:if_end_2
//  if(fh == 0) {Console.printStr("ERROR\n\0", 0);} else {Console.printStr("Opened\n\0", 0); char[32] buffer; uint32 read; uint32 state; FS.readFile(fh, & buffer, 32, 0, & read, & state); asm("#breakpoint"); char[10] str2; str2[8] = '\n'; str2[9] = '\0'; Console.intToHex(state, & str2); Console.printStr(& str2, 0); Console.printChar('\n'); Console.intToHex(read, & str2); Console.printStr(& str2, 0); Console.printChar('\n'); Console.printStr(& buffer, read);}

// 20 65:10
#breakpoint
//  asm("#breakpoint");

// 21 67:10
LOAD r1 1000
STACK PUSH r1
GOTO PUSH :TestD.wait_uint32
STACK DEC 4
//  wait(1000)

// 22 67:20
// ;

:func_exit_TestD.main
STACK DEC 32
STACK POP r15
HALT
#endfunction void

#function TestD.funcb_uint32 a uint32
STACK PUSH r15
COPY rStack r15
// 0 72:10
LOAD r1 &TestD.v
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
LOAD MEM r3 r1
ADD r2 r3 r2
STORE r2 r1
//  v += a;

:func_exit_TestD.funcb_uint32
STACK POP r15
GOTO POP
#endfunction void

#function TestD.funcb_uint32_uint32* a uint32, b uint32*
STACK PUSH r15
COPY rStack r15
// 0 76:10
LOAD r1 &TestD.v
COPY r15 r2
INC r2 -16
LOAD MEM r2 r2
LOAD MEM r3 r1
ADD r2 r3 r2
STORE r2 r1
//  v += a;

:func_exit_TestD.funcb_uint32_uint32*
STACK POP r15
GOTO POP
#endfunction void

#function TestD.testA_StructA& str StructA&
STACK PUSH r15
COPY rStack r15
// 0 95:10
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD r2 32
STORE r2 r1
//  str.a = 32;

// 1 96:10
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD r2 -1
STORE r2 r1
//  str.b = 0xffffffff;

// 2 97:10
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
COPY r15 r2
INC r2 -16
STORE r1 r2
GOTO :func_exit_TestD.testA_StructA&
//  return str;

:func_exit_TestD.testA_StructA&
STACK POP r15
GOTO POP
#endfunction StructA*

// TestD.StructA

HALT