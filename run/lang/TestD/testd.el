import SysD;

namespace TestD {

    @Entrypoint
    public static void main() {
        uint32 b;
        uint32 a;
        a = SysD.rPgm;
        uint32 c;
        c = b;
        c++;
        b = a + 1 + c;
        c = 32;
    }
}