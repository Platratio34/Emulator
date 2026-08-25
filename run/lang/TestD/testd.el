import SysD;
import Peripheral;

namespace TestD {

    public static uint32 v = 0;
    public static const char* str = "// Test";
    public static char[5] testStr = "Test\n";
    public static char[7] testStr2 = "Test2\n\0";
    public static char[9] path = "test.txt\0";
    public static char tc;

    public static const uint32* TIMERS = 0x0001_0200;

    public static const uint8* KEYBOARD_MOD = 0x0001_0304;
    public static const uint8* KEYBOARD_CONTROL = 0x0001_0305;
    public static const char* KEYBOARD_KEYS = 0x0001_0306;

    @Entrypoint(raw)
    public static void main() {
        asm("STORE BYTE 'T' r7\nSTORE BYTE 'e' r7\nSTORE BYTE 's' r7\nSTORE BYTE 't' r7\nSTORE BYTE 'D' r7\nSTORE BYTE '\\n' r7");
        asm("LOAD rIH &:TestD.onInterrupt");
        *KEYBOARD_CONTROL = 0x03; // Enable press interrupts
        CharacterDisplay.setup();
        uint32 b;
        uint32 a = SysD.rPgm;
        v = a;
        char c;
        c = b;
        // c++;
        b = a + 1 + c;
        c = 32;
        funcb(c);
        asm("LOAD r1 64\nLOAD r2 &TestD.v\nSTORE r1 r2");
        asm(str);

        StructA sA;
        testA(&sA);

        // Console.setupConsole();
        Console.printStr("Starting EmulatorOS\n\n\0",0);

        Console.printStr(&testStr, 5);
        Console.printStr(&testStr2, 0);
        Console.printChar('a');
        Console.printChar('\n');

        char[16] str2;
        Console.intToDec(0x1000, &str2);
        Console.printStr(&str2, 0);
        Console.printChar('\n');
        Console.printChar('d');
        Console.printChar('\n');

        // StructA* pntr = new StructA();


        // asm("#breakpoint");
        uint32 fh;
        uint32 rstat;
        FS.openFile("test.txt\0", &rstat, &fh);
        if(fh == 0) {
            Console.printStr("ERROR\n\0", 0);
            Console.intToHex(rstat, &str2);
            Console.printStr(&str2, 0);
        } else {
            Console.printStr("Opened\n\0", 0);
            char[32] buffer;
            uint32 read;
            uint32 state;
            FS.readFileSync(fh, &buffer, 32, 0, &read, &state);
            // asm("#breakpoint");
            Console.intToHex(state, &str2);
            Console.printStr(&str2, 0);
            // Console.printChar('\n');
            // asm("#breakpoint");
            Console.intToHex(read, &str2);
            Console.printStr(&str2, 0);
            
            Console.printChar('\n');
            Console.printStr(&buffer, read);
        }

        // Console.printStr("\n> \0",0);
        // char[32] buff;
        // Console.read(&buff, 32);
        // Console.printStr(&buff, 0);

        Peripheral.TIMERS[15] |= 0b01 << 28;
        Peripheral.TIMERS[1] = 1000;

        CharacterDisplay.write(0,0,"EmulatorOS\0");

        // wait(testRet());
        // funcC();

        asm(":main_loop\nGOTO :main_loop");
    }

    public static void funcb(uint32 a) {
        v += a;
    }
    
    public static void funcb(uint32 a, uint32* b) {
        v += a;
    }

    @InterruptHandler(raw)
    internal static void onInterrupt() {
        uint32 code = SysD.rIC;
        asm("LOAD rIC 0");
        char[9] str;
        str[8] = '\0';
        if(code == 0xff) {
            Console.printStr("\n\nHalting\0",0);
            asm("HALT");
        }
        if((code & 0xffff_ff00) == 0x8000_0200) { // timer
            uint32 i = code & 0xff;
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
            uint32 c = code & 0xff;
            if(c == 10) {
                Console.printChar('\n');
            }
            if(c < 32 || c > 127) {
                return;
            }
            Console.printChar(c);
            return;
        }
        Console.intToHex(code, &str);
        Console.printStr("\nInterrupt: \0", 0);
        Console.printStr(&str, 8);
        Console.printChar('\n');
    }

    public static void wait(uint32 time) {
        while(time > 0) {
            time--;
        }
    }

    public static void testA(StructA& str) {
        str.a = 32;
        str.b = 0xffffffff;
    }

    public static uint32 testRet() {
        return 2000;
    }

    struct StructA {
        public uint32 a;
        public uint32 b;
    }
}