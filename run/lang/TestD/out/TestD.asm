// static data
// TestD
#define TestD.CONSOLE_END 0x0002_0200 // uint32
#define TestD.CMD_ADDR 0x0002_0000 // uint32*
#var TestD.CONSOLE_SETUP_CMD [0x0001,0x0002_0100,0x0002_0200] // uint32[3]
#var TestD.testStr "Test\n" // char[5]
#define TestD.CONSOLE_START 0x0002_0100 // uint32
#define TestD.CMD_DEVICE 0x0002_0002 // uint32*
#define TestD.str "// Test" // char*
#define TestD.CMD_WRITTEN 0x0001 // uint32
#var TestD.v 0x0000 // uint32
#var TestD.testStr2 "Test2\n\0" // char[7]
#define TestD.CMD_STATUS 0x0002_0001 // uint32*
#define TestD.CMD_START 0x0002_0008 // uint32*
#define TestD.CMD_SIZE 0x0002_0004 // uint32*
#var TestD.consolePntr TestD.CONSOLE_START // void*

//--------
// text

// TestD

#function TestD.onInterrupt
STACK PUSH r15
COPY rStack r15
// 0 48:10
COPY rIC r1
STACK PUSH r1
//  uint32 code = SysD.rIC;

// 1 49:10
LOAD rIC 0
//  asm("LOAD rIC 0");

// 2 50:10
COPY r15 r1
LOAD MEM r1 r1
INC r1 -255
SET FORCE EQ r1 r1
GOTO EQ r1 :if_end_0
// 0 51:14
HALT
//  asm("HALT")

// 1 51:25
// ;

:if_end_0
//  if(code == 0xff) {asm("HALT");}

:func_exit_TestD.onInterrupt
STACK DEC 4
STACK POP r15
INTERRUPT RET
#endfunction void

#function TestD.setupConsole
STACK PUSH r15
COPY rStack r15
// 0 36:10
LOAD r1 1
STACK PUSH r1
LOAD r1 3
STACK PUSH r1
LOAD r1 &TestD.CONSOLE_SETUP_CMD
STACK PUSH r1
GOTO PUSH :TestD.peripheralCommand_uint32_uint32_uint32*
STACK DEC 12
//  peripheralCommand(0x001, 0x003, & CONSOLE_SETUP_CMD)

// 1 36:61
// ;

:func_exit_TestD.setupConsole
STACK POP r15
GOTO POP
#endfunction void

#function TestD.wait_uint32 time uint32
STACK PUSH r15
COPY rStack r15
// 0 56:10
:while_condition_1
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
SET FORCE GT r1 r1
GOTO EQ r1 :while_end_1
// 0 57:14
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
// 0 12:10
LOAD rIH &:TestD.onInterrupt
//  asm("LOAD rIH &:TestD.onInterrupt");

// 1 13:10
STACK INC 4
//  uint32 b;

// 2 14:10
COPY rPgm r1
STACK PUSH r1
//  uint32 a = SysD.rPgm;

// 3 15:10
LOAD r1 &TestD.v
COPY r15 r2
INC r2 4
LOAD MEM r2 r2
STORE r2 r1
//  v = a;

// 4 16:10
STACK INC 4
//  char c;

// 5 17:10
COPY r15 r1
INC r1 8
COPY r15 r2
LOAD MEM r2 r2
STORE r2 r1
//  c = b;

// 6 19:10
COPY r15 r1
COPY r15 r2
INC r2 4
LOAD MEM r2 r2
INC r2 1
COPY r15 r3
INC r3 8
LOAD MEM r3 r3
ADD r2 r2 r3
STORE r2 r1
//  b = a + 1 + c;

// 7 20:10
COPY r15 r1
INC r1 8
LOAD r2 32
STORE r2 r1
//  c = 32;

// 8 21:10
COPY r15 r1
INC r1 8
LOAD MEM r1 r1
STACK PUSH r1
GOTO PUSH :TestD.funcb_uint32
STACK DEC 4
//  funcb(c);

// 9 22:10
LOAD r1 64
LOAD r2 &TestD.v
STORE r1 r2
//  asm("LOAD r1 64\nLOAD r2 &TestD.v\nSTORE r1 r2");

// 10 23:10
// Test
//  asm(str);

// 11 25:10
STACK INC 12
//  StructA sA;

// 12 26:10
STACK INC 4
COPY r15 r1
INC r1 12
STACK PUSH r1
GOTO PUSH :TestD.testA_StructA&
//  testA(& sA);

// 13 28:10
GOTO PUSH :TestD.setupConsole
//  setupConsole();

// 14 30:10
LOAD r1 &TestD.testStr
STACK PUSH r1
LOAD r1 5
STACK PUSH r1
GOTO PUSH :TestD.printStr_char*_uint32
STACK DEC 8
//  printStr(& testStr, 5);

// 15 31:10
LOAD r1 &TestD.testStr2
STACK PUSH r1
LOAD r1 0
STACK PUSH r1
GOTO PUSH :TestD.printStr_char*_uint32
STACK DEC 8
//  printStr(& testStr2, 0);

// 16 32:10
LOAD r1 'a'
STACK PUSH r1
GOTO PUSH :TestD.printChar_char
STACK DEC 4
//  printChar('a');

// 17 34:10
LOAD r1 1000
STACK PUSH r1
GOTO PUSH :TestD.wait_uint32
STACK DEC 4
//  wait(1000)

// 18 34:20
// ;

:func_exit_TestD.main
STACK DEC 16
STACK POP r15
HALT
#endfunction void

#function TestD.funcb_uint32 a uint32
STACK PUSH r15
COPY rStack r15
// 0 39:10
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
// 0 43:10
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

#function TestD.peripheralCommand_uint32_uint32_uint32* deviceId uint32, cmdSize uint32, cmd uint32*
STACK PUSH r15
COPY rStack r15
// 0 19:10
LOAD r1 TestD.CMD_SIZE
COPY r15 r2
INC r2 -16
LOAD MEM r2 r2
STORE r2 r1
// * CMD_SIZE = cmdSize;

// 1 21:10
LOAD r1 TestD.CMD_START
//  asm("LOAD r1 TestD.CMD_START");

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
LOAD r1 TestD.CMD_ADDR
LOAD r2 16842752
COPY r15 r3
INC r3 -20
LOAD MEM r3 r3
OR r2 r2 r3
STORE r2 r1
// * CMD_ADDR = 0x0101_0000 | deviceId;

:func_exit_TestD.peripheralCommand_uint32_uint32_uint32*
STACK POP r15
GOTO POP
#endfunction void

#function TestD.testA_StructA& str StructA&
STACK PUSH r15
COPY rStack r15
// 0 62:10
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD r2 32
STORE r2 r1
//  str.a = 32;

// 1 63:10
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD r2 -1
STORE r2 r1
//  str.b = 0xffffffff;

// 2 64:10
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

#function TestD.printStr_char*_uint32 str char*, len uint32
STACK PUSH r15
COPY rStack r15
// 0 49:10
COPY r15 r14
INC r14 -12
LOAD MEM r14 r14
//  asm("COPY r15 r14\nINC r14 -12\nLOAD MEM r14 r14");

// 1 50:10
LOAD r1 &TestD.consolePntr
LOAD MEM r1 r1
//  asm("LOAD r1 &TestD.consolePntr\nLOAD MEM r1 r1");

// 2 51:10
COPY r15 r2
INC r2 -16
LOAD MEM r2 r2
//  asm("COPY r15 r2\nINC r2 -16\nLOAD MEM r2 r2");

// 3 52:10
LOAD r4 TestD.CONSOLE_END
LOAD r5 TestD.CONSOLE_START
//  asm("LOAD r4 TestD.CONSOLE_END\nLOAD r5 TestD.CONSOLE_START");

// 4 53:10
GOTO GT r14 :printStr_len
//  asm("GOTO GT r14 :printStr_len");

// 5 54:14
:printStr_l1
//  asm(":printStr_l1");

// 6 55:18
LOAD MEM r3 r2
GOTO EQ r3 :printStr_l1_exit
//  asm("LOAD MEM r3 r2\nGOTO EQ r3 :printStr_l1_exit");

// 7 56:18
STORE r3 r1
INC r1 4
INC r2 4
//  asm("STORE r3 r1\nINC r1 4\nINC r2 4");

// 8 57:18
SUB r3 r4 r1
GOTO GT r3 :printStr_l1
COPY r5 r1
GOTO :printStr_l1
//  asm("SUB r3 r4 r1\nGOTO GT r3 :printStr_l1\nCOPY r5 r1\nGOTO :printStr_l1");

// 9 58:14
:printStr_l1_exit
//  asm(":printStr_l1_exit");

// 10 59:14
GOTO :printStr_exit
//  asm("GOTO :printStr_exit");

// 11 60:10
:printStr_len
//  asm(":printStr_len");

// 12 61:14
:printStr_l2
//  asm(":printStr_l2");

// 13 62:18
COPY MEM r2 r1
INC r1 4
INC r2 4
//  asm("COPY MEM r2 r1\nINC r1 4\nINC r2 4");

// 14 63:18
SUB r3 r4 r1
GOTO GT r3 :printStr_l2_end
COPY r5 r1
//  asm("SUB r3 r4 r1\nGOTO GT r3 :printStr_l2_end\nCOPY r5 r1");

// 15 64:18
:printStr_l2_end
//  asm(":printStr_l2_end");

// 16 65:18
INC r14 -1
GOTO GT r14 :printStr_l2
//  asm("INC r14 -1\nGOTO GT r14 :printStr_l2");

// 17 66:10
:printStr_exit
//  asm(":printStr_exit");

// 18 67:10
LOAD r3 &TestD.consolePntr
STORE r1 r3
//  asm("LOAD r3 &TestD.consolePntr\nSTORE r1 r3")

// 19 67:56
// ;

:func_exit_TestD.printStr_char*_uint32
STACK POP r15
GOTO POP
#endfunction void

#function TestD.printChar_char c char
STACK PUSH r15
COPY rStack r15
// 0 40:10
LOAD r1 &TestD.consolePntr
LOAD MEM r1 r1
//  asm("LOAD r1 &TestD.consolePntr\nLOAD MEM r1 r1");

// 1 41:10
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
//  asm("COPY r15 r2\nINC r2 -12\nLOAD MEM r2 r2");

// 2 42:10
LOAD r4 TestD.CONSOLE_END
LOAD r5 TestD.CONSOLE_START
//  asm("LOAD r4 TestD.CONSOLE_END\nLOAD r5 TestD.CONSOLE_START");

// 3 43:10
STORE r2 r1
INC r1 4
INC r2 4
//  asm("STORE r2 r1\nINC r1 4\nINC r2 4");

// 4 44:10
SUB r3 r4 r1
GOTO GT r3 :printChar_exit
COPY r5 r1
//  asm("SUB r3 r4 r1\nGOTO GT r3 :printChar_exit\nCOPY r5 r1");

// 5 45:10
:printChar_exit
//  asm(":printChar_exit")

// 6 45:32
// ;

:func_exit_TestD.printChar_char
STACK POP r15
GOTO POP
#endfunction void

// TestD.StructA

HALT