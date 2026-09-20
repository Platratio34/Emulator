#function Peripheral.command_int32_int32_int32* deviceId int32, cmdSize int32, cmd int32*
#line <Peripheral> 1:1
STACK PUSH r15
COPY rStack r15
#stackVar int32 deviceId -20
#stackVar int32 cmdSize -16
#stackVar int32* cmd -12
LOAD r1 Peripheral.CMD_SIZE
SUB r2 r15 16
LOAD MEM r2 r2
STORE r2 r1 INC_RA
SUB r3 r15 12
LOAD MEM r3 r3
:Peripheral.command_loop
COPY MEM r3 r1 INC_RS INC_RD
INC r2 -1
GOTO GT r2 :Peripheral.command_loop
SUB r1 r15 20
LOAD MEM r1 r1
LOAD r2 0x0101_0000
OR r1 r1 r2
STORE r1 Peripheral.CMD_ADDR
#stackVarClear deviceId
#stackVarClear cmdSize
#stackVarClear cmd
STACK POP r15
#lineend
GOTO POP
#endfunction void