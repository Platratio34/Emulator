import SysD;

namespace TestD {

    public static uint32 v = 0;

    @Entrypoint
    public static void main() {
        uint32 b;
        uint32 a;
        a = SysD.rPgm;
        v = a;
        uint32 c;
        c = b;
        c++;
        b = a + 1 + c;
        c = 32;
        funcb(c);
        // funcC();
    }

    public static void funcb(uint32 a) {
        v = v + a;
    }
    
    // public static void funcb(uint32 a) {
    //     v = v + a;
    // }
}