import SysD;

namespace Kernal {

    public static const char* CONSOLE_OUT = 0x1_0000;
    public static const char* CONSOLE_IN = 0x1_0001;
    public static const char* CONSOLE_IN_COUNT = 0x1_0002;

    public static void print(char c) {
        *CONSOLE_OUT = c;
    }

    public static void print(char* str) {
        asm("LOAD MEM r1 Kernal.CONSOLE_OUT");
        asm("COPY r15 r2\nINC r2 -12\nLOAD MEM r2 r2"); // str
        asm(":Kernal.print_char*_l1");
            asm("LOAD MEM BYTE r3 r2\nGOTO EQ r3 :Kernal.print_char*__exit");
            asm("STORE BYTE r3 r1\nINC r2 1\nGOTO :Kernal.print_char*_l1");
        asm(":Kernal.print_char*__exit");
    }

    public static void print(char* str, uint32 len) {
        asm("LOAD MEM r1 Kernal.CONSOLE_OUT");
        asm("COPY r15 r2\nINC r2 -16\nLOAD MEM r2 r2"); // str
        asm("COPY r15 r3\nINC r3 -12\nLOAD MEM r3 r3"); // len
        asm(":Kernal.print_char*_uint32_l1");
            asm("COPY BYTE MEM r2 r1 INC_RG");
            asm("INC r13 -1\nGOTO GT r13 :Kernal.print_char*_uint32_l1");
    }

    public static void read(char* buffer, uint32 bufferSize, out uint32& count) {
        asm("LOAD r1 Console.CONSOLE_IN_COUNT\n:read_l0\nLOAD MEM BYTE r2 r1\nGOTO EQ r2 :read_l0"); // r2 is inCount; then wait for input
        asm("LOAD r1 Console.CONSOLE_IN"); // r1 in console in address
        asm("COPY r15 r3\nINC r3 -16\nLOAD MEM r3 r3"); // r3 is bufferSize
        asm("LOAD r6 1"); // r6 is preset flag to say space remaining
        asm("SUB r4 r3 r1\nGOTO LT r4 :Kernal.read_char*_uint32_if_end_0");
            asm("COPY r3 r2\nLOAD r6 0"); // reduce the count to the buffer size and set the flag for no space remaining
        asm(":Kernal.read_char*_uint32_if_end_0");
        asm("COPY r2 r4\nCOPY r15 r5\nINC r5 -20\n:Kernal.read_char*_uint32_l0"); // r4 is now i; r5 is not &buffer
            asm("COPY BYTE MEM r1 r5 INC_RA\nINC r4 -1"); // copy in character to buffer, then decrement our counter
            asm("GOTO GT r4 :Kernal.read_char*_uint32_l0");
        asm("GOTO EQ r1 :Kernal.read_char*_uint32_end"); // skip if no space remaining
            asm("LOAD r6 0x0\nSTORE BYTE r6 r5"); // set next byte in the buffer to 0
        asm(":Kernal.read_char*_uint32_end");
        asm("COPY r15 r6\nINC r6 -12\nSTORE r2 r6"); // count = inCount;
    }
}