// static data
// TestD
#define TestD.CONSOLE_END 0x0002_0140 // uint32
#define TestD.CMD_ADDR 0x0002_0000 // uint32*
#var TestD.CONSOLE_SETUP_CMD [0x0001,0x0002_0100,0x0020] // uint32[3]
#var TestD.testStr "Test" // char[4]
#define TestD.CONSOLE_START 0x0002_0100 // uint32
#define TestD.CMD_DEVICE 0x0002_0002 // uint32*
#define TestD.str "// Test" // char*
#define TestD.CMD_WRITTEN 0x0001 // uint32
#var TestD.v 0x0000 // uint32
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
// 0 45:10
COPY rIC r1
STACK PUSH r1
//  uint32 code = SysD.rIC;

// 1 46:10
LOAD rIC 0
//  asm("LOAD rIC 0");

// 2 47:10
COPY r15 r1
LOAD MEM r1 r1
INC r1 -255
SET FORCE EQ r1 r1
GOTO EQ r1 :if_end_0
// 0 48:14
HALT
//  asm("HALT")

// 1 48:25
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
// 0 40:10
LOAD r1 1
STACK PUSH r1
LOAD r1 3
STACK PUSH r1
LOAD r1 &TestD.CONSOLE_SETUP_CMD
STACK PUSH r1
GOTO PUSH :TestD.peripheralCommand_uint32_uint32_uint32*
STACK DEC 12
//  peripheralCommand(0x001, 0x003, & CONSOLE_SETUP_CMD)

// 1 40:61
// ;

:func_exit_TestD.setupConsole
STACK POP r15
GOTO POP
#endfunction void

#function TestD.wait_uint32 time uint32
STACK PUSH r15
COPY rStack r15
// 0 53:10
:while_condition_1
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
SET FORCE GT r1 r1
GOTO EQ r1 :while_end_1
// 0 54:14
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
// 0 11:10
LOAD rIH &:TestD.onInterrupt
//  asm("LOAD rIH &:TestD.onInterrupt");

// 1 12:10
STACK INC 4
//  uint32 b;

// 2 13:10
COPY rPgm r1
STACK PUSH r1
//  uint32 a = SysD.rPgm;

// 3 14:10
LOAD r1 &TestD.v
COPY r15 r2
INC r2 4
LOAD MEM r2 r2
STORE r2 r1
//  v = a;

// 4 15:10
STACK INC 4
//  char c;

// 5 16:10
COPY r15 r1
INC r1 8
COPY r15 r2
LOAD MEM r2 r2
STORE r2 r1
//  c = b;

// 6 18:10
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

// 7 19:10
COPY r15 r1
INC r1 8
LOAD r2 32
STORE r2 r1
//  c = 32;

// 8 20:10
COPY r15 r1
INC r1 8
LOAD MEM r1 r1
STACK PUSH r1
GOTO PUSH :TestD.funcb_uint32
STACK DEC 4
//  funcb(c);

// 9 21:10
LOAD r1 64
LOAD r2 &TestD.v
STORE r1 r2
//  asm("LOAD r1 64\nLOAD r2 &TestD.v\nSTORE r1 r2");

// 10 22:10
// Test
//  asm(str);

// 11 24:10
STACK INC 12
//  StructA sA;

// 12 25:10
STACK INC 4
COPY r15 r1
INC r1 12
STACK PUSH r1
GOTO PUSH :TestD.testA_StructA&
//  testA(& sA);

// 13 27:10
GOTO PUSH :TestD.setupConsole
//  setupConsole();

// 14 29:10
LOAD r1 &TestD.testStr
STACK PUSH r1
LOAD r1 4
STACK PUSH r1
GOTO PUSH :TestD.printStr_char*_uint32
STACK DEC 8
//  printStr(& testStr, 4);

// 15 31:10
LOAD r1 1000
STACK PUSH r1
GOTO PUSH :TestD.wait_uint32
STACK DEC 4
//  wait(1000)

// 16 31:20
// ;

:func_exit_TestD.main
STACK DEC 16
STACK POP r15
HALT
#endfunction void

#function TestD.funcb_uint32 a uint32
STACK PUSH r15
COPY rStack r15
// 0 36:10
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
// 0 40:10
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
// 0 23:10
LOAD r1 TestD.CMD_SIZE
COPY r15 r2
INC r2 -16
LOAD MEM r2 r2
STORE r2 r1
// * CMD_SIZE = cmdSize;

// 1 25:10
LOAD r1 TestD.CMD_START
//  asm("LOAD r1 TestD.CMD_START");

// 2 27:10
COPY r15 r3
INC r3 -12
LOAD MEM r3 r3
//  asm("COPY r15 r3\nINC r3 -12\nLOAD MEM r3 r3");

// 3 29:10
:peripheralCommand_l0
//  asm(":peripheralCommand_l0");

// 4 31:10
COPY MEM r3 r1
//  asm("COPY MEM r3 r1");

// 5 32:10
INC r1 4
INC r3 4
INC r2 -1
//  asm("INC r1 4\nINC r3 4\nINC r2 -1");

// 6 34:10
GOTO GT r2 :peripheralCommand_l0
//  asm("GOTO GT r2 :peripheralCommand_l0");

// 7 36:10
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
// 0 59:10
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD r2 32
STORE r2 r1
//  str.a = 32;

// 1 60:10
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
LOAD r2 -1
STORE r2 r1
//  str.b = 0xffffffff;

// 2 61:10
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
// 0 54:10
LOAD r1 0
STACK PUSH r1
//  uint32 i = 0;

// 1 55:10
COPY r15 r1
INC r1 -12
LOAD MEM r1 r1
SET FORCE EQ r1 r1
GOTO EQ r1 :if_else_2
// 0 56:14
:while_condition_3
COPY r15 r1
INC r1 -16
COPY r15 r2
LOAD MEM r2 r2
LOAD r3 4
MUL r2 r2 r3
LOAD MEM r1 r1
ADD r1 r1 r2
LOAD MEM r1 r1
SET FORCE NEQ r1 r1
GOTO EQ r1 :while_end_3
// 0 57:18
LOAD r1 &TestD.consolePntr
LOAD MEM r1 r1
COPY r15 r2
INC r2 -16
COPY r15 r3
LOAD MEM r3 r3
LOAD r4 4
MUL r3 r3 r4
LOAD MEM r2 r2
ADD r2 r2 r3
LOAD MEM r2 r2
STORE r2 r1
// * consolePntr = str[i];

// 1 58:18
LOAD r1 &TestD.consolePntr
LOAD r2 4
LOAD MEM r3 r1
ADD r2 r3 r2
STORE r2 r1
//  consolePntr += 4;

// 2 59:18
LOAD r1 &TestD.consolePntr
LOAD MEM r1 r1
LOAD r2 1
STORE r2 r1
// * consolePntr = 0x1;

// 3 60:18
LOAD r1 &TestD.consolePntr
LOAD r2 4
LOAD MEM r3 r1
ADD r2 r3 r2
STORE r2 r1
//  consolePntr += 4;

// 4 61:18
LOAD r1 &TestD.consolePntr
LOAD MEM r1 r1
LOAD r2 TestD.CONSOLE_END
SUB r1 r1 r2
SET FORCE GT r1 r1
GOTO EQ r1 :if_end_4
// 0 62:22
LOAD r1 &TestD.consolePntr
LOAD r2 TestD.CONSOLE_START
STORE r2 r1
//  consolePntr = CONSOLE_START;

:if_end_4
//  if(consolePntr > CONSOLE_END) {consolePntr = CONSOLE_START;}

// 5 64:18
COPY r15 r1
LOAD MEM r2 r1
INC r2 1
STORE r2 r1
//  i++;

GOTO :while_condition_3
:while_end_3
//  while(str[i] != 0) {* consolePntr = str[i]; consolePntr += 4;* consolePntr = 0x1; consolePntr += 4; if(consolePntr > CONSOLE_END) {consolePntr = CONSOLE_START;} i++;}

GOTO :if_end_2
:if_else_2
// 0 67:14
:while_condition_5
COPY r15 r1
LOAD MEM r1 r1
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
SUB r1 r2 r1
SET FORCE GT r1 r1
GOTO EQ r1 :while_end_5
// 0 68:18
LOAD r1 &TestD.consolePntr
LOAD MEM r1 r1
COPY r15 r2
INC r2 -16
COPY r15 r3
LOAD MEM r3 r3
LOAD r4 4
MUL r3 r3 r4
LOAD MEM r2 r2
ADD r2 r2 r3
LOAD MEM r2 r2
STORE r2 r1
// * consolePntr = str[i];

// 1 69:18
LOAD r1 &TestD.consolePntr
LOAD r2 4
LOAD MEM r3 r1
ADD r2 r3 r2
STORE r2 r1
//  consolePntr += 4;

// 2 70:18
LOAD r1 &TestD.consolePntr
LOAD MEM r1 r1
LOAD r2 1
STORE r2 r1
// * consolePntr = 0x1;

// 3 71:18
LOAD r1 &TestD.consolePntr
LOAD r2 4
LOAD MEM r3 r1
ADD r2 r3 r2
STORE r2 r1
//  consolePntr += 4;

// 4 72:18
LOAD r1 &TestD.consolePntr
LOAD MEM r1 r1
LOAD r2 TestD.CONSOLE_END
SUB r1 r1 r2
SET FORCE GT r1 r1
GOTO EQ r1 :if_end_6
// 0 73:22
LOAD r1 &TestD.consolePntr
LOAD r2 TestD.CONSOLE_START
STORE r2 r1
//  consolePntr = CONSOLE_START;

:if_end_6
//  if(consolePntr > CONSOLE_END) {consolePntr = CONSOLE_START;}

// 5 75:18
COPY r15 r1
LOAD MEM r2 r1
INC r2 1
STORE r2 r1
//  i++;

GOTO :while_condition_5
:while_end_5
//  while(i < len) {* consolePntr = str[i]; consolePntr += 4;* consolePntr = 0x1; consolePntr += 4; if(consolePntr > CONSOLE_END) {consolePntr = CONSOLE_START;} i++;}

:if_end_2
//  if(len == 0) {while(str[i] != 0) {* consolePntr = str[i]; consolePntr += 4;* consolePntr = 0x1; consolePntr += 4; if(consolePntr > CONSOLE_END) {consolePntr = CONSOLE_START;} i++;}} else {while(i < len) {* consolePntr = str[i]; consolePntr += 4;* consolePntr = 0x1; consolePntr += 4; if(consolePntr > CONSOLE_END) {consolePntr = CONSOLE_START;} i++;}}

:func_exit_TestD.printStr_char*_uint32
STACK DEC 4
STACK POP r15
GOTO POP
#endfunction void

#function TestD.printChar_char c char
STACK PUSH r15
COPY rStack r15
// 0 44:10
LOAD r1 &TestD.consolePntr
LOAD MEM r1 r1
COPY r15 r2
INC r2 -12
LOAD MEM r2 r2
STORE r2 r1
// * consolePntr = c;

// 1 45:10
LOAD r1 &TestD.consolePntr
LOAD r2 4
LOAD MEM r3 r1
ADD r2 r3 r2
STORE r2 r1
//  consolePntr += 4;

// 2 46:10
LOAD r1 &TestD.consolePntr
LOAD MEM r1 r1
LOAD r2 1
STORE r2 r1
// * consolePntr = 0x1;

// 3 47:10
LOAD r1 &TestD.consolePntr
LOAD r2 4
LOAD MEM r3 r1
ADD r2 r3 r2
STORE r2 r1
//  consolePntr += 4;

// 4 48:10
LOAD r1 &TestD.consolePntr
LOAD MEM r1 r1
LOAD r2 TestD.CONSOLE_END
SUB r1 r1 r2
SET FORCE GT r1 r1
GOTO EQ r1 :if_end_7
// 0 49:14
LOAD r1 &TestD.consolePntr
LOAD r2 TestD.CONSOLE_START
STORE r2 r1
//  consolePntr = CONSOLE_START;

:if_end_7
//  if(consolePntr > CONSOLE_END) {consolePntr = CONSOLE_START;}

:func_exit_TestD.printChar_char
STACK POP r15
GOTO POP
#endfunction void

// TestD.StructA

HALT