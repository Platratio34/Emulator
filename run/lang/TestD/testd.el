import SysD;
import Console;
import FS;

namespace TestD {

    public static uint32 v = 0;
    public static const char* str = "// Test";
    public static char[5] testStr = "Test\n";
    public static char[7] testStr2 = "Test2\n\0";
    public static char[9] path = "test.txt\0";
    public static char tc;

    public static const uint32* TIMERS = 0x0001_0200;

    @Entrypoint(raw)
    public static void main() {
        asm("STORE BYTE 'T' r7\nSTORE BYTE 'e' r7\nSTORE BYTE 's' r7\nSTORE BYTE 't' r7\nSTORE BYTE 'D' r7\nSTORE BYTE '\\n' r7");
        asm("LOAD rIH &:TestD.onInterrupt");
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

        char[10] str2;
        str2[8] = '\n';
        str2[9] = '\0';

        // StructA* pntr = new StructA();


        // asm("#breakpoint");
        /*uint32 fh;
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
            FS.readFile(fh, &buffer, 32, 0, &read, &state);
            // asm("#breakpoint");
            Console.intToHex(state, &str2);
            Console.printStr(&str2, 0);
            // Console.printChar('\n');
            // asm("#breakpoint");
            Console.intToHex(read, &str2);
            Console.printStr(&str2, 0);
            
            Console.printChar('\n');
            Console.printStr(&buffer, read);
        }*/

        Console.printStr("\n> \0",0);
        // char[32] buff;
        // Console.read(&buff, 32);
        // Console.printStr(&buff, 0);

        TIMERS[1] = 480 * 5;

        CharacterDisplay.write(0,0,"EmulatorOS\0");

        wait(testRet());
        // funcC();
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
        if(code == 0x8000_0002) { // timer
            uint32 i = 1;
            while(i < 16) {
                if(TIMERS[i] == 0xffff_ffff) {
                    TIMERS[i] = 0x0;
                }
                i++;
            }
            
            Console.printStr("\nTimer\0", 0);
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