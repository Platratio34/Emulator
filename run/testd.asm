// static data
#var TestD.str "// Test" // char*
#var TestD.v 0x0000 // uint32

// text
:TestD.wait_uint32
STACK PUSH r15
COPY rStack r15
// 0 41:10
:while_condition_0
GOTO :while_end_0

:while_body_0
// 0 42:14
COPY r15 r1
INC r1 -3
LOAD MEM r2 r1
INC r2 -1
STORE r2 r1
//  time--;


GOTO :while_condition_0
:while_end_0
//  while time > 0 {time--;}

STACK POP r15

GOTO POP
:__start
:TestD.main
STACK PUSH r15
COPY rStack r15
// 0 10:10
STACK INC
//  uint32 b;

// 1 11:10
STACK INC
//  uint32 a;

// 2 12:10
COPY r15 r1
INC r1 1
STORE rPgm r1
//  a = SysD.rPgm;

// 3 13:10
LOAD r1 &TestD.v
COPY r15 r2
INC r2 1
LOAD MEM r2 r2
STORE r2 r1
//  v = a;

// 4 14:10
STACK INC
//  char c;

// 5 15:10
COPY r15 r1
INC r1 2
COPY r15 r2
LOAD MEM r2 r2
STORE r2 r1
//  c = b;

// 6 16:10
COPY r15 r1
INC r1 2
LOAD MEM r2 r1
INC r2 1
STORE r2 r1
//  c++;

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
//  funcb c;

// 10 20:10
LOAD r1 64
LOAD r2 &TestD.v
STORE r1 r2
//  asm "LOAD r1 64\nLOAD r2 &TestD.v\nSTORE r1 r2";

// 11 21:10
// Test
//  asm str;

// 12 22:10
COPY r15 r2
INC r2 2
LOAD MEM r2 r2
STACK PUSH r2
GOTO PUSH :TestD.wait_uint32
STACK DEC 1
//  wait c;

STACK DEC 3
STACK POP r15

HALT
:TestD.funcb_uint32
STACK PUSH r15
COPY rStack r15
// 0 27:10
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

HALT