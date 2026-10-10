import SysD;

namespace System;

class String {
    public final int32 length;
    public final char* str;

    public String(int32 len, char* chars) {
        length = len;
        str = malloc(sizeof<char>() * length);
        SysD.memCopy(chars, str, length);
    }

    // public String(array<char> chars) {
    //     length = chars.length;
    //     str = malloc(sizeof<char>() * length);
    //     SysD.memCopy(str.values, 0, length-1, str, 0);
    // }

    internal String(int32 len) {
        length = len;
        str = malloc(sizeof<char>() * length);
    }

    public ~String() {
        free(str);
    }

    @Operator([])
    operator inline char get(int32 i) {
        return str[i];
    }

    public String* clone() {
        return new String(length, str);
    }

    @Operator(==)
    operator bool equals(String* s2) {
        if(s2.length != length) {
            return false;
        }
        for(int32 i = 0; i < length; i++) {
            if(str[i] != s2.str[i]) {
                return false;
            }
        }
        return true;
    }

    public String* substring(int32 start) {
        return substring(start, length);
    }
    public String* substring(int32 start, int32 end) {
        String* s2 = new String(end-start);
        SysD.memCopy(str + start, s2.str, s2.length);
        return s2;
    }

    @Operator(+)
    public String* append(String s2) {
        String* s3 = new String(length + s2.length);
        SysD.memCopy(str, s3.str, length);
        SysD.memCopy(s2.str, s3.str + length, s2.length);
        return s3;
    }
    @Operator(+)
    public String* append(char c) {
        String* s2 = new String(length + 1);
        SysD.memCopy(str, s2.str, length);
        SysD.memCopy(&c, s2.str + length, 1);
        return s2;
    }

    // @Operator(cast)
    // operator String* _cast(char c) {
    //     return new String(1, &c);
    // }

    // @Operator(cast)
    // operator String* _cast(char* c) {
    //     int32 len = 0;
    //     while(c[len] != '\0') {
    //         len++;
    //     }
    //     return new String(len, c);
    // }
}