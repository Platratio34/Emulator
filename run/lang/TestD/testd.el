import SysD;
import Peripheral;

namespace TestD {

    public static int32 v = 0;
    public static const char* str = "// Test";
    public static char[5] testStr = "Test\n";
    public static char[7] testStr2 = "Test2\n\0";
    public static char[9] path = "test.txt\0";
    public static char tc;

    public static const int32* TIMERS = 0x0001_0200;

    public static const uint8* KEYBOARD_MOD = 0x0001_0304;
    public static const uint8* KEYBOARD_CONTROL = 0x0001_0305;
    public static const char* KEYBOARD_KEYS = 0x0001_0306;

    protected static char[128] inputBuffer;
    protected static int32 inputBufferWrite;
    protected static int32 inputBufferRead;

    @Entrypoint(raw)
    public static void main() {
        asm{
            STORE BYTE 'T' r7
            STORE BYTE 'e' r7
            STORE BYTE 's' r7
            STORE BYTE 't' r7
            STORE BYTE 'D' r7
            STORE BYTE '\n' r7
        }
        asm{LOAD rIH &:TestD.onInterrupt};
        *KEYBOARD_CONTROL = 0x03; // Enable press interrupts
        CharacterDisplay.setup();
        int32 b;
        int32 a = SysD.rPgm;
        v = a;
        char c;
        c = b;
        // c++;
        b = a + 1 + c;
        c = 32;
        funcb(c);
        asm{
            LOAD r1 64
            LOAD r2 &TestD.v
            STORE r1 r2
        }
        asm(str);

        StructA sA;
        testA(&sA);

        // Console.setupConsole();
        Console.printStr("Starting EmulatorOS\n\n\0");

        Console.printStr(&testStr, 5);
        Console.printStr(&testStr2);
        Console.printChar('a');
        Console.printChar('\n');

        char[16] str2;
        Console.intToDec(0x1000, &str2);
        Console.printStr(&str2);
        Console.printChar('\n');
        Console.printChar('d');
        Console.printChar('\n');

        // StructA* pntr = new StructA();


        // asm("#breakpoint");
        int32 fh;
        int32 rstat;
        FS.openFile("test.txt\0", &rstat, &fh);
        if(fh == 0) {
            Console.printStr("ERROR\n\0");
            Console.intToHex(rstat, &str2);
            Console.printStr(&str2);
        } else {
            Console.printStr("Opened\n\0");
            char[32] buffer;
            int32 read;
            int32 state;
            FS.readFileSync(fh, &buffer, 32, 0, &read, &state);
            // asm("#breakpoint");
            Console.intToHex(state, &str2);
            Console.printStr(&str2);
            // Console.printChar('\n');
            // asm("#breakpoint");
            Console.intToHex(read, &str2);
            Console.printStr(&str2, 0);
            
            Console.printChar('\n');
            Console.printStr(&buffer, read);
        }

        // Console.printStr("\n> \0");
        // char[32] buff;
        // Console.read(&buff, 32);
        // Console.printStr(&buff);

        // @Breakpoint(noOp)

        Peripheral.TIMERS[15] |= 0b01 << 28;
        Peripheral.TIMERS[1] = 1000;

        CharacterDisplay.write(0,0,"EmulatorOS\0");

        // wait(testRet());
        // funcC();

        Console.printChar('>');
        while(mainLoop()) {

        }
        mainLoop();

        Console.printStr("Stopping...\0");
    }

    protected static char[64] tempCmd;
    protected static int32 tempCmdI;
    public static bool mainLoop() {
        while(inputBufferRead != inputBufferWrite) {
            char c = inputBuffer[inputBufferRead];
            Console.printChar(c);
            inputBufferRead = (inputBufferRead + 1) & 127;
            if(c == '\n') {
                tempCmd[tempCmdI] = 0;
                if(!processCommand()) {
                    return false;
                }
                tempCmdI = 0;
            } else {
                tempCmd[tempCmdI] = c;
                tempCmdI = (tempCmdI + 1) & 63;
            }
        }
        return true;
    }

    private static bool processCommand() {
        Console.printChar(':');
        Console.printStr(&tempCmd);
        Console.printChar('\n');
        if(stringEquals(&tempCmd, "STOP\0")) {
            return false;
        }
        Console.printChar('>');
        return true;
    }

    public static bool stringEquals(char* str1, char* str2) {
        asm{
            SUB r15 r15 16
            LOAD MEM r1 r1
            // str1
        
            SUB r2 r15 12
            LOAD MEM r2 r2 // str2

            // test comment
            
            :string_equals_loop
                LOAD MEM BYTE r3 r1 INC_RA
                LOAD MEM BYTE r4 r2 INC_RA
                
                SUB r4 r3 r4
                GOTO NEQ r4 :string_equals_fail
                
                GOTO NEQ r3 :string_equals_loop
        }
        return true;
        asm{:string_equals_fail};
        return false;
    }

    public static void funcb(int32 a) {
        v += a;
    }
    
    public static void funcb(int32 a, int32* b) {
        v += a;
    }

    @InterruptHandler(raw)
    internal static void onInterrupt() {
        int32 code = SysD.rIC;
        asm{LOAD rIC 0}
        char[9] str;
        str[8] = '\0';
        if(code == 0xff) {
            Console.printStr("\n\nHalting\0",0);
            asm{HALT};
        }
        if((code & 0xffff_ff00) == 0x8000_0200) { // timer
            int32 i = code & 0xff;
            if(i == 1) {
                // Peripheral.TIMERS[1] = 1000;
                return;
            }
            
            Console.printStr("\nTimer \0", 0);
            char[3] str;
            Console.intToDec(i, &str);
            Console.printStr(&str, 0);
            CharacterDisplay.write(0,23,"Timer\0");
            return;
        }
        if((code & 0xffff_ff00) == 0x8000_0100) { // key pressed
            char c = code & 0xff;
            if(c == '\n') {
                inputBuffer[inputBufferWrite] = '\n';
                inputBufferWrite = (inputBufferWrite + 1) & 0x7f;
            }
            if(c < ' ' || c > '~') {
                return;
            }
            inputBuffer[inputBufferWrite] = c;
            inputBufferWrite = (inputBufferWrite + 1) & 0x7f;
            return;
        }
        Console.intToHex(code, &str);
        Console.printStr("\nInterrupt: \0", 0);
        Console.printStr(&str, 8);
        Console.printChar('\n');
    }

    @/
        Live waits for at least the specified number of clock cycles.
    /@
    public static void wait(int32 time) {
        int32 end = Peripheral.TIMERS[0] + time;
        while(Peripheral.TIMERS[0] < end) {
            
        }
    }

    public static void testA(StructA& str) {
        str.a = 32;
        str.b = 0xffffffff;
    }

    public static int32 testRet() {
        return 2000;
    }

    struct StructA {
        public int32 a;
        public int32 b;
    }
}