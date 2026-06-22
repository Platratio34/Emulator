// static data
// Console
#define Console.CMD_DEVICE 0x0002_0002 uint32*
#define Console.CONSOLE_OUT 0x0002_0100 char*
#define Console.CMD_WRITTEN 0x0001 uint32
#define Console.CMD_ADDR 0x0002_0000 uint32*
#define Console.CMD_STATUS 0x0002_0001 uint32*
#define Console.CONSOLE_IN 0x0002_0101 char*
#define Console.CONSOLE_IN_COUNT 0x0002_0102 uint8*
#define Console.CMD_START 0x0002_0008 uint32*
#define Console.CMD_SIZE 0x0002_0004 uint32*
// FS
#define FS.CMD_DEVICE 0x0002_0002 uint32*
#define FS.CMD_WRITTEN 0x0001 uint32
#define FS.RSP_DATA_2 0x0002_0088 uint32*
#define FS.RSP_DATA_3 0x0002_008c uint32*
#define FS.CMD_ADDR 0x0002_0000 uint32*
#define FS.RSP_STATUS 0x0002_0080 uint32*
#define FS.CMD_STATUS 0x0002_0001 uint32*
#define FS.RSP_DATA 0x0002_0084 uint32*
#define FS.CMD_START 0x0002_0008 uint32*
#define FS.CMD_SIZE 0x0002_0004 uint32*
// TestD
#define TestD.str "// Test" char*
#var TestD.path "test.txt\0" char[9]
#var TestD.v 0x0000 uint32
#var TestD.testStr2 "Test2\n\0" char[7]
#var TestD.testStr "Test\n" char[5]
#var TestD.tc 0x00 char

//--------
// text

// Console

#function Console.read_char*_uint32 buffer char*, bufferSize uint32
STACK PUSH r15
COPY rStack r15
// 0 53:10
#line run\lang\TestD\console.el 53:10
:while_condition_0
LOAD r1 Console.CONSOLE_IN_COUNT
LOAD MEM BYTE r1 r1
SET FORCE EQ r1 r1
GOTO EQ r1 :while_end_0
// 

#lineend
GOTO :while_condition_0
:while_end_0
//  while(* CONSOLE_IN_COUNT == 0) {}

// 1 54:10
#line run\lang\TestD\console.el 54:10
LOAD r1 Console.CONSOLE_IN_COUNT
LOAD MEM BYTE r1 r1
STACK PUSH r1
//  uint32 inCount =* CONSOLE_IN_COUNT;

// 2 55:10
#line run\lang\TestD\console.el 55:10
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
COPY r15 r2
LOAD MEM r2 r2
SUB r1 r1 r2
SET FORCE LT r1 r1
GOTO EQ r1 :if_end_1
// 0 56:14
#line run\lang\TestD\console.el 56:14
COPY r15 r1
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
STORE r2 r1
//  inCount = bufferSize;

#lineend
:if_end_1
//  if(bufferSize < inCount) {inCount = bufferSize;}

// 3 58:10
#line run\lang\TestD\console.el 58:10
LOAD r1 0
STACK PUSH r1
//  uint32 i = 0;

// 4 59:10
#line run\lang\TestD\console.el 59:10
:while_condition_2
COPY r15 r1
LOAD MEM r1 r1
SET FORCE GT r1 r1
GOTO EQ r1 :while_end_2
// 0 60:14
#line run\lang\TestD\console.el 60:14
COPY r15 r1
INC r1 -16
COPY r15 r2
INC r2 4
LOAD MEM r2 r2
LOAD MEM r1 r1
ADD r1 r1 r2
LOAD r2 Console.CONSOLE_IN
LOAD MEM BYTE r2 r2
STORE BYTE r2 r1
//  buffer[i] =* CONSOLE_IN;

// 1 61:14
#line run\lang\TestD\console.el 61:14
COPY r15 r1
LOAD MEM r2 r1
INC r2 -1
STORE r2 r1
//  inCount--;

// 2 62:14
#line run\lang\TestD\console.el 62:14
COPY r15 r1
INC r1 4
LOAD MEM r2 r1
INC r2 1
STORE r2 r1
//  i++;

#lineend
GOTO :while_condition_2
:while_end_2
//  while(inCount > 0) {buffer[i] =* CONSOLE_IN; inCount--; i++;}

// 5 64:10
#line run\lang\TestD\console.el 64:10
COPY r15 r1
INC r1 4
LOAD MEM r1 r1
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
SUB r1 r1 r2
SET FORCE LT r1 r1
GOTO EQ r1 :if_end_3
// 0 65:14
#line run\lang\TestD\console.el 65:14
COPY r15 r1
INC r1 -16
COPY r15 r2
INC r2 4
LOAD MEM r2 r2
LOAD MEM r1 r1
ADD r1 r1 r2
LOAD r2 '\0'
STORE BYTE r2 r1
//  buffer[i] = '\0';

#lineend
:if_end_3
//  if(i < bufferSize) {buffer[i] = '\0';}

#lineend
:func_exit_Console.read_char*_uint32
STACK DEC 8
STACK POP r15
GOTO POP
#endfunction void

#function Console.printStr_char*_uint32 str char*, len uint32
STACK PUSH r15
COPY rStack r15
// 0 23:10
#line run\lang\TestD\console.el 23:10
COPY r15 r14
INC r14 -12
LOAD MEM r14 r14
//  asm("COPY r15 r14\nINC r14 -12\nLOAD MEM r14 r14");

// 1 24:10
#line run\lang\TestD\console.el 24:10
LOAD r1 Console.CONSOLE_OUT
//  asm("LOAD r1 Console.CONSOLE_OUT");

// 2 25:10
#line run\lang\TestD\console.el 25:10
COPY r15 r2
INC r2 -16
LOAD MEM r2 r2
//  asm("COPY r15 r2\nINC r2 -16\nLOAD MEM r2 r2");

// 3 26:10
#line run\lang\TestD\console.el 26:10
LOAD r5 Console.CONSOLE_OUT
//  asm("LOAD r5 Console.CONSOLE_OUT");

// 4 27:10
#line run\lang\TestD\console.el 27:10
GOTO GT r14 :printStr_len
//  asm("GOTO GT r14 :printStr_len");

// 5 28:14
#line run\lang\TestD\console.el 28:14
:printStr_l1
//  asm(":printStr_l1");

// 6 29:18
#line run\lang\TestD\console.el 29:18
LOAD MEM BYTE r3 r2
GOTO EQ r3 :printStr_l1_exit
//  asm("LOAD MEM BYTE r3 r2\nGOTO EQ r3 :printStr_l1_exit");

// 7 30:18
#line run\lang\TestD\console.el 30:18
STORE BYTE r3 r1
INC r2 1
GOTO :printStr_l1
//  asm("STORE BYTE r3 r1\nINC r2 1\nGOTO :printStr_l1");

// 8 31:14
#line run\lang\TestD\console.el 31:14
:printStr_l1_exit
GOTO :printStr_exit
//  asm(":printStr_l1_exit\nGOTO :printStr_exit");

// 9 32:10
#line run\lang\TestD\console.el 32:10
:printStr_len
//  asm(":printStr_len");

// 10 33:14
#line run\lang\TestD\console.el 33:14
COPY BYTE MEM r2 r1 INC_RG
//  asm("COPY BYTE MEM r2 r1 INC_RG");

// 11 34:14
#line run\lang\TestD\console.el 34:14
INC r14 -1
GOTO GT r14 :printStr_len
//  asm("INC r14 -1\nGOTO GT r14 :printStr_len");

// 12 35:10
#line run\lang\TestD\console.el 35:10
:printStr_exit
//  asm(":printStr_exit")

// 13 35:31
#line run\lang\TestD\console.el 35:31
// ;

#lineend
:func_exit_Console.printStr_char*_uint32
STACK POP r15
GOTO POP
#endfunction void

#function Console.printChar_char c char
STACK PUSH r15
COPY rStack r15
// 0 17:10
#line run\lang\TestD\console.el 17:10
LOAD r1 Console.CONSOLE_OUT
//  asm("LOAD r1 Console.CONSOLE_OUT");

// 1 18:10
#line run\lang\TestD\console.el 18:10
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
//  asm("COPY r15 r2\nINC r2 -12\nLOAD MEM r2 r2");

// 2 19:10
#line run\lang\TestD\console.el 19:10
STORE BYTE r2 r1
//  asm("STORE BYTE r2 r1")

// 3 19:33
#line run\lang\TestD\console.el 19:33
// ;

#lineend
:func_exit_Console.printChar_char
STACK POP r15
GOTO POP
#endfunction void

#function Console.intToHex_uint32_char* value uint32, str char*
STACK PUSH r15
COPY rStack r15
// 0 39:10
#line run\lang\TestD\console.el 39:10
LOAD r14 7
//  asm("LOAD r14 7");

// 1 40:10
#line run\lang\TestD\console.el 40:10
COPY r15 r1
INC r1 -16
LOAD MEM r1 r1
//  asm("COPY r15 r1\nINC r1 -16\nLOAD MEM r1 r1");

// 2 41:10
#line run\lang\TestD\console.el 41:10
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
INC r2 8
//  asm("COPY r15 r2\nINC r2 -12\nLOAD MEM r2 r2\nINC r2 8");

// 3 42:10
#line run\lang\TestD\console.el 42:10
LOAD r3 0xf
LOAD r6 0xa
//  asm("LOAD r3 0xf\nLOAD r6 0xa");

// 4 43:10
#line run\lang\TestD\console.el 43:10
:intToHex_l1
//  asm(":intToHex_l1");

// 5 44:14
#line run\lang\TestD\console.el 44:14
INC r2 -1
AND r4 r1 r3
RSH r1 r1 4
//  asm("INC r2 -1\nAND r4 r1 r3\nRSH r1 r1 4");

// 6 45:14
#line run\lang\TestD\console.el 45:14
SUB r5 r4 r6
GOTO GEQ r5 :intToHex_gt
//  asm("SUB r5 r4 r6\nGOTO GEQ r5 :intToHex_gt");

// 7 46:18
#line run\lang\TestD\console.el 46:18
INC r4 0x30
STORE BYTE r4 r2
GOTO :intToHex_l1_end
//  asm("INC r4 0x30\nSTORE BYTE r4 r2\nGOTO :intToHex_l1_end");

// 8 47:14
#line run\lang\TestD\console.el 47:14
:intToHex_gt
//  asm(":intToHex_gt");

// 9 48:18
#line run\lang\TestD\console.el 48:18
INC r4 0x57
STORE BYTE r4 r2
//  asm("INC r4 0x57\nSTORE BYTE r4 r2");

// 10 49:14
#line run\lang\TestD\console.el 49:14
:intToHex_l1_end
INC r14 -1
GOTO GEQ r14 :intToHex_l1
//  asm(":intToHex_l1_end\nINC r14 -1\nGOTO GEQ r14 :intToHex_l1")

// 11 49:76
#line run\lang\TestD\console.el 49:76
// ;

#lineend
:func_exit_Console.intToHex_uint32_char*
STACK POP r15
GOTO POP
#endfunction void

// FS

#function FS.openFile_char*_out uint32& path char*, handle out uint32&
STACK PUSH r15
COPY rStack r15
// 0 36:10
#line run\lang\TestD\fs.el 36:10
LOAD r1 16
STACK PUSH r1
COPY r15 r1
INC r1 -16
LOAD MEM r1 r1
STACK PUSH r1
//  uint32[2] msg = {0x10, path};

// 1 37:10
#line run\lang\TestD\fs.el 37:10
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
#line run\lang\TestD\fs.el 39:10
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD r2 FS.RSP_DATA_2
LOAD MEM r2 r2
STORE r2 r1
//  handle =* RSP_DATA_2;

#lineend
:func_exit_FS.openFile_char*_out uint32&
STACK DEC 8
STACK POP r15
GOTO POP
#endfunction void

#function FS.readFile_uint32_void*_uint32_uint32_out uint32&_out uint32& handle uint32, buffer void*, size uint32, offset uint32, read out uint32&, state out uint32&
STACK PUSH r15
COPY rStack r15
// 0 43:10
#line run\lang\TestD\fs.el 43:10
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
#line run\lang\TestD\fs.el 44:10
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
#line run\lang\TestD\fs.el 45:10
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD r2 FS.RSP_DATA
LOAD MEM r2 r2
STORE r2 r1
//  state =* RSP_DATA;

// 3 46:10
#line run\lang\TestD\fs.el 46:10
COPY r15 r1
INC r1 -16
LOAD MEM r1 r1
LOAD r2 FS.RSP_DATA_3
LOAD MEM r2 r2
STORE r2 r1
//  read =* RSP_DATA_3;

#lineend
:func_exit_FS.readFile_uint32_void*_uint32_uint32_out uint32&_out uint32&
STACK DEC 20
STACK POP r15
GOTO POP
#endfunction void

#function FS.peripheralCommand_uint32_uint32_uint32* deviceId uint32, cmdSize uint32, cmd uint32*
STACK PUSH r15
COPY rStack r15
// 0 19:10
#line run\lang\TestD\fs.el 19:10
LOAD r1 FS.CMD_SIZE
COPY r15 r2
INC r2 -16
LOAD MEM r2 r2
STORE r2 r1
// * CMD_SIZE = cmdSize;

// 1 21:10
#line run\lang\TestD\fs.el 21:10
LOAD r1 Console.CMD_START
//  asm("LOAD r1 Console.CMD_START");

// 2 23:10
#line run\lang\TestD\fs.el 23:10
COPY r15 r3
INC r3 -12
LOAD MEM r3 r3
//  asm("COPY r15 r3\nINC r3 -12\nLOAD MEM r3 r3");

// 3 25:10
#line run\lang\TestD\fs.el 25:10
:peripheralCommand_l0
//  asm(":peripheralCommand_l0");

// 4 27:10
#line run\lang\TestD\fs.el 27:10
COPY MEM r3 r1
//  asm("COPY MEM r3 r1");

// 5 28:10
#line run\lang\TestD\fs.el 28:10
INC r1 4
INC r3 4
INC r2 -1
//  asm("INC r1 4\nINC r3 4\nINC r2 -1");

// 6 30:10
#line run\lang\TestD\fs.el 30:10
GOTO GT r2 :peripheralCommand_l0
//  asm("GOTO GT r2 :peripheralCommand_l0");

// 7 32:10
#line run\lang\TestD\fs.el 32:10
LOAD r1 FS.CMD_ADDR
LOAD r2 16842752
COPY r15 r3
INC r3 -20
LOAD MEM r3 r3
OR r2 r2 r3
STORE r2 r1
// * CMD_ADDR = 0x0101_0000 | deviceId;

#lineend
:func_exit_FS.peripheralCommand_uint32_uint32_uint32*
STACK POP r15
GOTO POP
#endfunction void

// TestD

#function TestD.onInterrupt
STACK PUSH r15
COPY rStack r15
// 0 86:10
#line run\lang\TestD\testd.el 86:10
COPY rIC r1
STACK PUSH r1
//  uint32 code = SysD.rIC;

// 1 87:10
#line run\lang\TestD\testd.el 87:10
LOAD rIC 0
//  asm("LOAD rIC 0");

// 2 88:10
#line run\lang\TestD\testd.el 88:10
STACK INC 12
//  char[9] str;

// 3 89:10
#line run\lang\TestD\testd.el 89:10
COPY r15 r1
INC r1 4
LOAD r2 8
ADD r1 r1 r2
LOAD r2 '\0'
STORE BYTE r2 r1
//  str[8] = '\0';

// 4 90:10
#line run\lang\TestD\testd.el 90:10
COPY r15 r1
LOAD MEM r1 r1
STACK PUSH r1
COPY r15 r1
INC r1 4
STACK PUSH r1
GOTO PUSH :Console.intToHex_uint32_char*
STACK DEC 8
//  Console.intToHex(code, & str);

// 5 91:10
#line run\lang\TestD\testd.el 91:10
#define exp_str_0 "\nInterrupt: \0"
LOAD r1 exp_str_0
STACK PUSH r1
LOAD r1 0
STACK PUSH r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
//  Console.printStr("\nInterrupt: \0", 0);

// 6 92:10
#line run\lang\TestD\testd.el 92:10
COPY r15 r1
INC r1 4
STACK PUSH r1
LOAD r1 8
STACK PUSH r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
//  Console.printStr(& str, 8);

// 7 93:10
#line run\lang\TestD\testd.el 93:10
LOAD r1 '\n'
STACK PUSH r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
//  Console.printChar('\n');

// 8 94:10
#line run\lang\TestD\testd.el 94:10
COPY r15 r1
LOAD MEM r1 r1
INC r1 -255
SET FORCE EQ r1 r1
GOTO EQ r1 :if_end_4
// 0 95:14
#line run\lang\TestD\testd.el 95:14
HALT
//  asm("HALT")

// 1 95:25
#line run\lang\TestD\testd.el 95:25
// ;

#lineend
:if_end_4
//  if(code == 0xff) {asm("HALT");}

#lineend
:func_exit_TestD.onInterrupt
STACK DEC 16
STACK POP r15
INTERRUPT RET
#endfunction void

#function TestD.wait_uint32 time uint32
STACK PUSH r15
COPY rStack r15
// 0 100:10
#line run\lang\TestD\testd.el 100:10
:while_condition_5
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
SET FORCE GT r1 r1
GOTO EQ r1 :while_end_5
// 0 101:14
#line run\lang\TestD\testd.el 101:14
COPY r15 r1
INC r1 -12
LOAD MEM r2 r1
INC r2 -1
STORE r2 r1
//  time--;

#lineend
GOTO :while_condition_5
:while_end_5
//  while(time > 0) {time--;}

#lineend
:func_exit_TestD.wait_uint32
STACK POP r15
GOTO POP
#endfunction void

:__start
#function TestD.main
STACK PUSH r15
COPY rStack r15
// 0 16:10
#line run\lang\TestD\testd.el 16:10
LOAD rIH &:TestD.onInterrupt
//  asm("LOAD rIH &:TestD.onInterrupt");

// 1 17:10
#line run\lang\TestD\testd.el 17:10
STACK INC 4
//  uint32 b;

// 2 18:10
#line run\lang\TestD\testd.el 18:10
COPY rPgm r1
STACK PUSH r1
//  uint32 a = SysD.rPgm;

// 3 19:10
#line run\lang\TestD\testd.el 19:10
LOAD r1 &TestD.v
COPY r15 r2
INC r2 4
LOAD MEM r2 r2
STORE r2 r1
//  v = a;

// 4 20:10
#line run\lang\TestD\testd.el 20:10
STACK INC 4
//  char c;

// 5 21:10
#line run\lang\TestD\testd.el 21:10
COPY r15 r1
INC r1 8
COPY r15 r2
LOAD MEM r2 r2
STORE BYTE r2 r1
//  c = b;

// 6 23:10
#line run\lang\TestD\testd.el 23:10
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
#line run\lang\TestD\testd.el 24:10
COPY r15 r1
INC r1 8
LOAD r2 32
STORE BYTE r2 r1
//  c = 32;

// 8 25:10
#line run\lang\TestD\testd.el 25:10
COPY r15 r1
INC r1 8
LOAD MEM BYTE r1 r1
STACK PUSH r1
GOTO PUSH :TestD.funcb_uint32
STACK DEC 4
//  funcb(c);

// 9 26:10
#line run\lang\TestD\testd.el 26:10
LOAD r1 64
LOAD r2 &TestD.v
STORE r1 r2
//  asm("LOAD r1 64\nLOAD r2 &TestD.v\nSTORE r1 r2");

// 10 27:10
#line run\lang\TestD\testd.el 27:10
// Test
//  asm(str);

// 11 29:10
#line run\lang\TestD\testd.el 29:10
STACK INC 16
//  StructA sA;

// 12 30:10
#line run\lang\TestD\testd.el 30:10
COPY r15 r1
INC r1 12
STACK PUSH r1
GOTO PUSH :TestD.testA_StructA&
STACK DEC 4
//  testA(& sA);

// 13 34:10
#line run\lang\TestD\testd.el 34:10
LOAD r1 &TestD.testStr
STACK PUSH r1
LOAD r1 5
STACK PUSH r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
//  Console.printStr(& testStr, 5);

// 14 35:10
#line run\lang\TestD\testd.el 35:10
LOAD r1 &TestD.testStr2
STACK PUSH r1
LOAD r1 0
STACK PUSH r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
//  Console.printStr(& testStr2, 0);

// 15 36:10
#line run\lang\TestD\testd.el 36:10
LOAD r1 'a'
STACK PUSH r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
//  Console.printChar('a');

// 16 37:10
#line run\lang\TestD\testd.el 37:10
LOAD r1 '\n'
STACK PUSH r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
//  Console.printChar('\n');

// 17 40:10
#line run\lang\TestD\testd.el 40:10
STACK INC 4
//  uint32 fh;

// 18 41:10
#line run\lang\TestD\testd.el 41:10
#define exp_str_1 "test.txt\0"
LOAD r1 exp_str_1
STACK PUSH r1
COPY r15 r1
INC r1 28
STACK PUSH r1
GOTO PUSH :FS.openFile_char*_out uint32&
STACK DEC 8
//  FS.openFile("test.txt\0", & fh);

// 19 42:10
#line run\lang\TestD\testd.el 42:10
COPY r15 r1
INC r1 28
LOAD MEM r1 r1
SET FORCE EQ r1 r1
GOTO EQ r1 :if_else_6
// 0 43:14
#line run\lang\TestD\testd.el 43:14
#define exp_str_2 "ERROR\n\0"
LOAD r1 exp_str_2
STACK PUSH r1
LOAD r1 0
STACK PUSH r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
//  Console.printStr("ERROR\n\0", 0)

// 1 43:46
#line run\lang\TestD\testd.el 43:46
// ;

#lineend
GOTO :if_end_6
:if_else_6
// 0 45:14
#line run\lang\TestD\testd.el 45:14
#define exp_str_3 "Opened\n\0"
LOAD r1 exp_str_3
STACK PUSH r1
LOAD r1 0
STACK PUSH r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
//  Console.printStr("Opened\n\0", 0);

// 1 46:14
#line run\lang\TestD\testd.el 46:14
STACK INC 32
//  char[32] buffer;

// 2 47:14
#line run\lang\TestD\testd.el 47:14
STACK INC 4
//  uint32 read;

// 3 48:14
#line run\lang\TestD\testd.el 48:14
STACK INC 4
//  uint32 state;

// 4 49:14
#line run\lang\TestD\testd.el 49:14
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
GOTO PUSH :FS.readFile_uint32_void*_uint32_uint32_out uint32&_out uint32&
STACK DEC 24
//  FS.readFile(fh, & buffer, 32, 0, & read, & state);

// 5 51:14
#line run\lang\TestD\testd.el 51:14
STACK INC 12
//  char[10] str2;

// 6 52:14
#line run\lang\TestD\testd.el 52:14
COPY r15 r1
INC r1 72
LOAD r2 8
ADD r1 r1 r2
LOAD r2 '\n'
STORE BYTE r2 r1
//  str2[8] = '\n';

// 7 53:14
#line run\lang\TestD\testd.el 53:14
COPY r15 r1
INC r1 72
LOAD r2 9
ADD r1 r1 r2
LOAD r2 '\0'
STORE BYTE r2 r1
//  str2[9] = '\0';

// 8 54:14
#line run\lang\TestD\testd.el 54:14
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

// 9 55:14
#line run\lang\TestD\testd.el 55:14
COPY r15 r1
INC r1 72
STACK PUSH r1
LOAD r1 0
STACK PUSH r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
//  Console.printStr(& str2, 0);

// 10 58:14
#line run\lang\TestD\testd.el 58:14
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

// 11 59:14
#line run\lang\TestD\testd.el 59:14
COPY r15 r1
INC r1 72
STACK PUSH r1
LOAD r1 0
STACK PUSH r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
//  Console.printStr(& str2, 0);

// 12 61:14
#line run\lang\TestD\testd.el 61:14
LOAD r1 '\n'
STACK PUSH r1
GOTO PUSH :Console.printChar_char
STACK DEC 4
//  Console.printChar('\n');

// 13 62:14
#line run\lang\TestD\testd.el 62:14
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

// 14 62:45
#line run\lang\TestD\testd.el 62:45
// ;

#lineend
STACK DEC 52
:if_end_6
//  if(fh == 0) {Console.printStr("ERROR\n\0", 0);} else {Console.printStr("Opened\n\0", 0); char[32] buffer; uint32 read; uint32 state; FS.readFile(fh, & buffer, 32, 0, & read, & state); char[10] str2; str2[8] = '\n'; str2[9] = '\0'; Console.intToHex(state, & str2); Console.printStr(& str2, 0); Console.intToHex(read, & str2); Console.printStr(& str2, 0); Console.printChar('\n'); Console.printStr(& buffer, read);}

// 20 65:10
#line run\lang\TestD\testd.el 65:10
#define exp_str_4 "\n> \0"
LOAD r1 exp_str_4
STACK PUSH r1
LOAD r1 0
STACK PUSH r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
//  Console.printStr("\n> \0", 0);

// 21 66:10
#line run\lang\TestD\testd.el 66:10
STACK INC 32
//  char[32] buff;

// 22 67:10
#line run\lang\TestD\testd.el 67:10
COPY r15 r1
INC r1 32
STACK PUSH r1
LOAD r1 32
STACK PUSH r1
GOTO PUSH :Console.read_char*_uint32
STACK DEC 8
//  Console.read(& buff, 32);

// 23 68:10
#line run\lang\TestD\testd.el 68:10
COPY r15 r1
INC r1 32
STACK PUSH r1
LOAD r1 0
STACK PUSH r1
GOTO PUSH :Console.printStr_char*_uint32
STACK DEC 8
//  Console.printStr(& buff, 0);

// 24 72:10
#line run\lang\TestD\testd.el 72:10
LOAD r1 1000
STACK PUSH r1
GOTO PUSH :TestD.wait_uint32
STACK DEC 4
//  wait(1000)

// 25 72:20
#line run\lang\TestD\testd.el 72:20
// ;

#lineend
:func_exit_TestD.main
STACK DEC 64
STACK POP r15
HALT
#endfunction void

#function TestD.funcb_uint32 a uint32
STACK PUSH r15
COPY rStack r15
// 0 77:10
#line run\lang\TestD\testd.el 77:10
LOAD r1 &TestD.v
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
LOAD MEM r3 r1
ADD r2 r3 r2
STORE r2 r1
//  v += a;

#lineend
:func_exit_TestD.funcb_uint32
STACK POP r15
GOTO POP
#endfunction void

#function TestD.funcb_uint32_uint32* a uint32, b uint32*
STACK PUSH r15
COPY rStack r15
// 0 81:10
#line run\lang\TestD\testd.el 81:10
LOAD r1 &TestD.v
COPY r15 r2
INC r2 -16
LOAD MEM r2 r2
LOAD MEM r3 r1
ADD r2 r3 r2
STORE r2 r1
//  v += a;

#lineend
:func_exit_TestD.funcb_uint32_uint32*
STACK POP r15
GOTO POP
#endfunction void

#function TestD.testA_StructA& str StructA&
STACK PUSH r15
COPY rStack r15
// 0 106:10
#line run\lang\TestD\testd.el 106:10
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD r2 32
STORE r2 r1
//  str.a = 32;

// 1 107:10
#line run\lang\TestD\testd.el 107:10
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD r2 -1
STORE r2 r1
//  str.b = 0xffffffff;

#lineend
:func_exit_TestD.testA_StructA&
STACK POP r15
GOTO POP
#endfunction void

// TestD.StructA

HALT