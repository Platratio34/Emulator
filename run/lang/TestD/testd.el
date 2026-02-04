import SysD;

namespace TestD {

    public static uint32 v = 0;
    public static const char* str = "// Test";

    @Entrypoint
    public static void main() {
        uint32 b;
        uint32 a;
        a = SysD.rPgm;
        v = a;
        char c;
        c = b;
        c++;
        b = a + 1 + c;
        c = 32;
        funcb(c);
        asm("LOAD r1 64\nLOAD r2 &TestD.v\nSTORE r1 r2");
        asm(str);
        wait(c);
        // funcC();
    }

    public static void funcb(uint32 a) {
        v += a;
    }
    
    // public static void funcb(uint32 a) {
    //     v += a;
    // }

    /*@InterruptHandler(raw)
    internal static void onInterrupt() {
        char c;
        asm("HALT");
    }*/

    public static void wait(uint32 time) {
        while(time > 0) {
            time--;
        }
    }
}