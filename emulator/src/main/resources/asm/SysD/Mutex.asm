#function Mutex.release
#line <Mutex>:release 1:1
STORE BYTE 0x0 r0
#lineend
GOTO POP
#endfunction void

#function Mutex.try
#line <Mutex>:try 1:1
TEST AND SET r1 r0
SUB r2 rStack 8
SET FORCE EQ r1 r1
STORE r1 r2
#lineend
GOTO POP
#endfunction bool

#function Mutex.acquire
#line <Mutex>:acquire 1:1
:Mutex.acquire_loop
TEST AND SET r1 r0
GOTO NEQ r1 :Mutex.acquire_loop
#lineend
GOTO POP
#endfunction void