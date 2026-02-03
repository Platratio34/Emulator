// static data
#var TestD.v 0x0000 // uint32

// text
:__start
:TestD.main
COPY rStack r15
// 0 9:10
STACK INC
//  uint32 b;

// 1 10:10
STACK INC
//  uint32 a;

// 2 11:10
COPY r15 r1
INC r1 1
STORE rPgm r1
//  a = SysD.rPgm;

// 3 12:10
LOAD r1 &TestD.v
COPY r15 r2
INC r2 1
LOAD MEM r2 r2
STORE r2 r1
//  v = a;

// 4 13:10
STACK INC
//  uint32 c;

// 5 14:10
COPY r15 r1
INC r1 2
COPY r15 r2
LOAD MEM r2 r2
STORE r2 r1
//  c = b;

// 6 15:10
COPY r15 r1
INC r1 2
LOAD MEM r2 r1
INC r2 1
STORE r2 r1
//  c++;

// 7 16:10
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

// 8 17:10
COPY r15 r1
INC r1 2
LOAD r2 32
STORE r2 r1
//  c = 32;

// 9 18:10
STACK PUSH r15
COPY r15 r2
INC r2 2
LOAD MEM r2 r2
STACK PUSH r2
GOTO PUSH :TestD.funcb_uint32
STACK DEC 1
STACK POP r15
//  funcb c

// 10 18:18
STACK DEC 3

HALT
:TestD.funcb_uint32
COPY rStack r15
// 0 23:10
LOAD r1 &TestD.v
LOAD r2 &TestD.v
LOAD MEM r2 r2
COPY r15 r3
INC r3 -2
LOAD MEM r3 r3
ADD r2 r2 r3
STORE r2 r1

GOTO POP

HALT