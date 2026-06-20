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

    @Entrypoint(raw)
    public static void main() {
        asm("LOAD rIH &:TestD.onInterrupt");
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

        Console.printStr(&testStr, 5);
        Console.printStr(&testStr2, 0);
        Console.printChar('a');
        Console.printChar('\n');

        asm("#breakpoint");
        uint32 fh;
        FS.openFile("test.txt\0", &fh);
        if(fh == 0) {
            Console.printStr("ERROR\n\0", 0);
        } else {
            Console.printStr("Opened\n\0", 0);
            char[32] buffer;
            uint32 read;
            FS.readFile(fh, &buffer, 32, 0, &read);
            Console.printStr(&buffer, read);
        }
        
        asm("#breakpoint");

        wait(1000);
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
        if(code == 0xff) {
            asm("HALT");
        }
    }

    public static void wait(uint32 time) {
        while(time > 0) {
            time--;
        }
    }

    public static StructA* testA(StructA& str) {
        str.a = 32;
        str.b = 0xffffffff;
        return str;
    }

    struct StructA {
        public uint32 a;
        public uint32 b;
    }
}