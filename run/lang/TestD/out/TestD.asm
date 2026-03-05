// static data
// TestD
#var TestD.str "// Test" // char*
#var TestD.v 0x0000 // uint32
// TestD.StructA

//--------
// text

// TestD

#function TestD.onInterrupt
STACK PUSH r15
COPY rStack r15
// 0 40:10
COPY rIC r1
STACK PUSH r1
//  uint32 code = SysD.rIC;

// 1 41:10
LOAD rIC 0
//  asm("LOAD rIC 0")

// 2 41:27
// ;

// 3 42:10
COPY r15 r1
LOAD MEM r1 r1
INC r1 -255
SET FORCE EQ r1 r1
GOTO EQ r1 :if_end_8
// 0 43:14
HALT
//  asm("HALT")

// 1 43:25
// ;

:if_end_8
//  if(code == 0xff) {asm("HALT");}

:func_exit_TestD.onInterrupt
STACK DEC 1
STACK POP r15
INTERRUPT RET
#endfunction void

#function TestD.wait_uint32 time uint32
STACK PUSH r15
COPY rStack r15
// 0 48:10
:while_condition_9
COPY r15 r1
INC r1 -3
LOAD MEM r1 r1
SET FORCE GT r1 r1
GOTO EQ r1 :while_end_9
// 0 49:14
COPY r15 r1
INC r1 -3
LOAD MEM r2 r1
INC r2 -1
STORE r2 r1
//  time--;

GOTO :while_condition_9
:while_end_9
//  while(time > 0) {time--;}

:func_exit_TestD.wait_uint32
STACK POP r15
GOTO POP
#endfunction void

:__start
#function TestD.main
STACK PUSH r15
COPY rStack r15
// 0 10:10
LOAD rIH &:TestD.onInterrupt
//  asm("LOAD rIH &:TestD.onInterrupt")

// 1 10:45
// ;

// 2 11:10
STACK INC
//  uint32 b;

// 3 12:10
COPY rPgm r1
STACK PUSH r1
//  uint32 a = SysD.rPgm;

// 4 13:10
LOAD r1 &TestD.v
COPY r15 r2
INC r2 1
LOAD MEM r2 r2
STORE r2 r1
//  v = a;

// 5 14:10
STACK INC
//  char c;

// 6 15:10
COPY r15 r1
INC r1 2
COPY r15 r2
LOAD MEM r2 r2
STORE r2 r1
//  c = b;

// 7 17:10
COPY r15 r1
COPY r15 r2
INC r2 1
LOAD MEM r2 r2
INC r2 1
COPY r15 r3
INC r3 2
LOAD MEM r3 r3
ADD r2 r2 r3
STORE r2 r1
//  b = a + 1 + c;

// 8 18:10
COPY r15 r1
INC r1 2
LOAD r2 32
STORE r2 r1
//  c = 32;

// 9 19:10
COPY r15 r1
INC r1 2
LOAD MEM r1 r1
STACK PUSH r1
GOTO PUSH :TestD.funcb_uint32
STACK DEC 1
//  funcb(c)

// 10 19:18
// ;

// 11 20:10
LOAD r1 64
LOAD r2 &TestD.v
STORE r1 r2
//  asm("LOAD r1 64\nLOAD r2 &TestD.v\nSTORE r1 r2")

// 12 20:58
// ;

// 13 21:10
// Test
//  asm(str)

// 14 21:18
// ;

// 15 23:10
STACK INC 2
//  StructA sA;

// 16 24:10
STACK INC 1
COPY r15 r1
INC r1 3
STACK PUSH r1
GOTO PUSH :TestD.testA_StructA&
//  testA(& sA)

// 17 24:20
// ;

// 18 26:10
LOAD r1 1000
STACK PUSH r1
GOTO PUSH :TestD.wait_uint32
STACK DEC 1
//  wait(1000)

// 19 26:20
// ;

:func_exit_TestD.main
STACK DEC 4
STACK POP r15
HALT
#endfunction void

#function TestD.funcb_uint32 a uint32
STACK PUSH r15
COPY rStack r15
// 0 31:10
LOAD r1 &TestD.v
COPY r15 r2
INC r2 -3
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
// 0 35:10
LOAD r1 &TestD.v
COPY r15 r2
INC r2 -4
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
// 0 54:10
COPY r15 r1
INC r1 -3
LOAD MEM r1 r1
LOAD r2 32
STORE r2 r1
//  str.a = 32;

// 1 55:10
COPY r15 r1
INC r1 -3
LOAD MEM r1 r1
LOAD r2 -1
STORE r2 r1
//  str.b = 0xffffffff;

// 2 56:10
COPY r15 r1
INC r1 -3
LOAD MEM r1 r1
COPY r15 r2
INC r2 -4
STORE r1 r2
GOTO :func_exit_TestD.testA_StructA&
//  return str;

:func_exit_TestD.testA_StructA&
STACK POP r15
GOTO POP
#endfunction StructA*

// TestD.StructA

HALT