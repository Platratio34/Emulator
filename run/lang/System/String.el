import SysD;

namespace System;

class String {
    public final uint32 length;
    public final char* str;

    public String(uint32 len, char* chars) {
        length = len;
        str = malloc(sizeof(char) * length);
        SysD.memCopy(chars, 0, length-1, str, 0);
    }

    public String(array<char> chars) {
        length = chars.length;
        str = malloc(sizeof(char) * length);
        SysD.memCopy(str.values, 0, length-1, str, 0);
    }

    internal String(uint32 len) {
        length = len;
        str = malloc(sizeof(char) * length);
    }

    public ~String() {
        free(str);
    }

    @Operator([])
    operator constexpr char get(uint32 i) {
        return chars[i];
    }

    public String clone() {
        return new String(length, str);
    }

    @Operator(==)
    operator bool equals(String s2) {
        if(s2.length != length) {
            return false;
        }
        for(uint32 i = 0; i < length; i++) {
            if(str[i] != s2.str[i]) {
                return false;
            }
        }
        return true;
    }

    public String substring(uint32 start) {
        return substring(start, length)
    }
    public String substring(uint32 start, uint32 end) {
        String s2 = new String(end-start);
        SysD.memCopy(str, start, end, s2.str, 0);
        return s2;
    }

    @Operator(+)
    public String append(String s2) {
        String s3 = new String(length + s2.length);
        SysD.memCopy(str, 0, length-1, s3.str, 0);
        SysD.memCopy(s2.str, 0, s2.length-1, s3.str, length);
        return s3;
    }
    @Operator(+)
    public String append(char c) {
        String s2 = new String(length + 1);
        SysD.memCopy(str, 0, length-1, s2.str, 0);
        s2.str[length] = c;
        return s2;
    }

    @Operator(cast)
    operator String fromChar(char c) {
        return new String(1, &c);
    }
}