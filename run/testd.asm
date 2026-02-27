// static data
#var TestD.str "// Test" // char*
#var TestD.v 0x0000 // uint32

//--------
// text

#function TestD.onInterrupt
STACK PUSH r15
COPY rStack r15
// 0 36:10
COPY rIC r1
STACK PUSH r1
//  uint32 code = SysD.rIC;

// 1 37:10
LOAD rIC 0
//  asm("LOAD rIC 0")

// 2 37:27
// ;

// 3 38:10
COPY r15 r1
LOAD MEM r1 r1
INC r1 -255
SET FORCE EQ r1 r1
GOTO EQ r1 :if_end_0
// 0 39:14
HALT
//  asm("HALT")

// 1 39:25
// ;

:if_end_0
//  if(code == 0xff) {asm("HALT");}

STACK DEC 1
STACK POP r15
INTERRUPT RET
#endfunction void

#function TestD.wait_uint32 time uint32
STACK PUSH r15
COPY rStack r15
// 0 44:10
:while_condition_1
COPY r15 r1
INC r1 -3
LOAD MEM r1 r1
SET FORCE GT r1 r1
GOTO EQ r1 :while_end_1
// 0 45:14
COPY r15 r1
INC r1 -3
LOAD MEM r2 r1
INC r2 -1
STORE r2 r1
//  time--;

GOTO :while_condition_1
:while_end_1
//  while(time > 0) {time--;}

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
COPY r15 r2
INC r2 2
LOAD MEM r2 r2
STACK PUSH r2
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

// 15 22:10
LOAD r2 1000
STACK PUSH r2
GOTO PUSH :TestD.wait_uint32
STACK DEC 1
//  wait(1000)

// 16 22:20
// ;

STACK DEC 3
STACK POP r15
HALT
#endfunction void

#function TestD.funcb_uint32 a uint32
STACK PUSH r15
COPY rStack r15
// 0 27:10
LOAD r1 &TestD.v
COPY r15 r2
INC r2 -3
LOAD MEM r2 r2
LOAD MEM r3 r1
ADD r1 r3 r1
STORE r2 r1
//  v += a;

STACK POP r15
GOTO POP
#endfunction void

#function TestD.funcb_uint32_uint32* a uint32, b uint32*
STACK PUSH r15
COPY rStack r15
// 0 31:10
LOAD r1 &TestD.v
COPY r15 r2
INC r2 -4
LOAD MEM r2 r2
LOAD MEM r3 r1
ADD r1 r3 r1
STORE r2 r1
//  v += a;

STACK POP r15
GOTO POP
#endfunction void

HALT