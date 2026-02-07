// static data
#var TestD.str "// Test" // char*
#var TestD.v 0x0000 // uint32

// text

#function TestD.onInterrupt
STACK PUSH r15
COPY rStack r15
// 0 37:10
STACK INC
//  uint32 code;

// 1 38:10
COPY r15 r1
STORE rIC r1
//  code = SysD.rIC;

// 2 39:10
LOAD rIC 0
//  asm "LOAD rIC 0";

// 3 40:10
COPY r15 r1
LOAD MEM r1 r1
LOAD r2 255
SUB r1 r1 r2
GOTO EQ r1 :if_true_0
GOTO :if_end_0

:if_true_0
// 0 41:14
HALT
//  asm "HALT";


:if_end_0
//  if code == 0xff {asm "HALT";}

STACK DEC 1
STACK POP r15

INTERRUPT RET
#endfunction void

#function TestD.wait_uint32 time uint32
STACK PUSH r15
COPY rStack r15
// 0 46:10
:while_condition_1
COPY r15 r1
INC r1 -3
LOAD MEM r1 r1
LOAD r2 0
SUB r1 r1 r2
GOTO GT r1 :while_body_1
GOTO :while_end_1

:while_body_1
// 0 47:14
COPY r15 r1
INC r1 -3
LOAD MEM r2 r1
INC r2 -1
STORE r2 r1
//  time--;


GOTO :while_condition_1
:while_end_1
//  while time > 0 {time--;}

STACK POP r15

GOTO POP
#endfunction void

:__start
#function TestD.main
STACK PUSH r15
COPY rStack r15
// 0 10:10
LOAD rIR &:TestD.onInterrupt
//  asm "LOAD rIR &:TestD.onInterrupt";

// 1 11:10
STACK INC
//  uint32 b;

// 2 12:10
STACK INC
//  uint32 a;

// 3 13:10
COPY r15 r1
INC r1 1
STORE rPgm r1
//  a = SysD.rPgm;

// 4 14:10
LOAD r1 &TestD.v
COPY r15 r2
INC r2 1
LOAD MEM r2 r2
STORE r2 r1
//  v = a;

// 5 15:10
STACK INC
//  char c;

// 6 16:10
COPY r15 r1
INC r1 2
COPY r15 r2
LOAD MEM r2 r2
STORE r2 r1
//  c = b;

// 7 17:10
COPY r15 r1
INC r1 2
LOAD MEM r2 r1
INC r2 1
STORE r2 r1
//  c++;

// 8 18:10
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

// 9 19:10
COPY r15 r1
INC r1 2
LOAD r2 32
STORE r2 r1
//  c = 32;

// 10 20:10
COPY r15 r2
INC r2 2
LOAD MEM r2 r2
STACK PUSH r2
GOTO PUSH :TestD.funcb_uint32
STACK DEC 1
//  funcb c;

// 11 21:10
LOAD r1 64
LOAD r2 &TestD.v
STORE r1 r2
//  asm "LOAD r1 64\nLOAD r2 &TestD.v\nSTORE r1 r2";

// 12 22:10
// Test
//  asm str;

// 13 23:10
LOAD r2 1000
STACK PUSH r2
GOTO PUSH :TestD.wait_uint32
STACK DEC 1
//  wait 1000;

STACK DEC 3
STACK POP r15

HALT
#endfunction void

#function TestD.funcb_uint32 a uint32
STACK PUSH r15
COPY rStack r15
// 0 28:10
LOAD r1 &TestD.v
LOAD MEM r2 r1
COPY r15 r3
INC r3 -3
LOAD MEM r3 r3
ADD r3 r2 r3
STORE r3 r1
//  v += a;

STACK POP r15

GOTO POP
#endfunction void

#function TestD.funcb_uint32_void* a uint32, b void*
STACK PUSH r15
COPY rStack r15
// 0 32:10
LOAD r1 &TestD.v
LOAD MEM r2 r1
COPY r15 r3
INC r3 -4
LOAD MEM r3 r3
ADD r3 r2 r3
STORE r3 r1
//  v += a;

STACK POP r15

GOTO POP
#endfunction void

HALT