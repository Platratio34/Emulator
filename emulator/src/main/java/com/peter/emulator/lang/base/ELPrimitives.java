package com.peter.emulator.lang.base;

import java.util.HashMap;

import com.peter.emulator.lang.ELClass;
import com.peter.emulator.lang.ELType;

public class ELPrimitives {

    public static final ELClass OBJECT_CLASS = new ELClass("Object");
    public static final ELType OBJECT = new ELType("Object", OBJECT_CLASS);

    // bool
    public static final ELClass BOOL_CLASS = new ELClass("bool", OBJECT_CLASS);
    public static final ELType BOOL = new ELType("bool", BOOL_CLASS);
    // uint8
    public static final ELClass UINT8_CLASS = new ELClass("uint8", OBJECT_CLASS) {
        @Override
        public boolean canStaticCast(ELType target) {
            return target.equals(CHAR) || target.equals(UINT16) || target.equals(UINT32);
        }
    };
    public static final ELType UINT8 = new ELType("uint8", UINT8_CLASS);
    // char
    public static final ELClass CHAR_CLASS = new ELClass("char", OBJECT_CLASS) {
        @Override
        public boolean canStaticCast(ELType target) {
            return target.equals(UINT8) || target.equals(UINT16) || target.equals(UINT32);
        }
    };
    public static final ELType CHAR = new ELType("char", CHAR_CLASS);
    // uint16
    public static final ELClass UINT16_CLASS = new ELClass("uint16", OBJECT_CLASS) {
        @Override
        public boolean canStaticCast(ELType target) {
            return target.equals(UINT32);
        }
    };
    public static final ELType UINT16 = new ELType("uint16", UINT16_CLASS);
    // uint32
    public static final ELClass UINT32_CLASS = new ELClass("uint32", OBJECT_CLASS) {
        @Override
        public boolean canStaticCast(ELType target) {
            return target.equals(VOID_PTR);
        }
    };
    public static final ELType UINT32 = new ELType("uint32", UINT32_CLASS);
    // void*
    public static final ELType VOID_PTR = new ELType.Builder("void").pointer().build();

    public static final ELType STRING = new ELType("string");

    /* []
        struct array<T> {
            public final uint32 length;
            public final T* values;
    
            public array<T>(uint32 length) {
                this.length = length;
                values = malloc(length*sizeof(T));
            }
            public array<T>(uint32 length, T* values) {
                this.length = length;
                this.values = values;
            }
            public array<T>(array<T>& arr) {
                this.length = arr.length;
                this.values = arr.getValues();
            }
    
            public ~array<T>() {
                delete values;
            }
    
            @Operator([])
            operator constexpr T* get(uint32 i) {
                return values + i;
            }
    
            public constexpr T* pointer() {
                return values;
            }
    
            public T* getValues() {
                T* v2 = malloc(length*sizeof(T));
                SysD.memCopy(values, 0, length, v2, 0);
                return v2;
            }
            // clone: T[] = new T[](old);
    
            public array<T> clone() {
                array<T> arr = new array<T>(length);
                SysD.memCopy(values, 0, length, arr.values, 0);
                return arr;
            }
        }
        
        new T[len]; -> new array<T>(len);
        new T[len][]; -> new array<array<T>>(len);
        new T[] {val...}; -> new array<T>(len, new T*{val...});
        new T[][] {new T[] {val...}...}; -> new array<array<T>>(len, new T[]*{new array<T> {val...}...});
        new T[](old); -> new array<T>(old);
    */
    /* string
        struct string extends array<char> {
            public string substring(uint32 start) {
                return substring(start, length);
            }
            public string substring(uint32 start, uint32 end) {
                uint32 l = end - start;
                string str = new string[l];
                SysD.memCopy(values, start, end, str2.values, 0, end);
                return str;
            }
    
            @Operator(+)
            operator string append(string str) {
                string str2 = new string(length + str.length);
                SysD.memCopy(values, 0, length, str2.values, 0);
                SysD.memCopy(str, 0, str.length, str2.values, length);
                return str2;
            }
    
            @Operator(+)
            operator string append(char c) {
                string str2 = new string(length + 1);
                SysD.memCopy(values, 0, length, str2.values, 0);
                str2.values[length] = c;
                return str2;
            }
    
            @Operator(==)
            operator boolean equals(string& str2) {
                if(length != str2.length) {
                    return false;
                }
                return SysD.memEquals(values, str2.values, length);
            }
            
            @Operator(cast, src = char)
            operator string fromChar(char c) {
                return new string(1, new char* {c});
            }
        }
        
        string s = ""; -> string s = new string(0);
    */

    public static boolean isNumber(ELType type) {
        return type.equals(UINT8, true) || type.equals(UINT16, true) || type.equals(UINT32, true);
    }

    public static boolean isString(ELType type) {
        return type.equals(CHAR, true) || type.equals(CHAR.pointerTo(), true) || type.equals(STRING, true);
    }

    public static final HashMap<ELType, ELClass> PRIMITIVE_TYPES = new HashMap<>();
    static {
        PRIMITIVE_TYPES.put(BOOL, BOOL_CLASS);
        PRIMITIVE_TYPES.put(UINT8, UINT8_CLASS);
        PRIMITIVE_TYPES.put(CHAR, CHAR_CLASS);
        PRIMITIVE_TYPES.put(UINT16, UINT16_CLASS);
        PRIMITIVE_TYPES.put(UINT32, UINT32_CLASS);
        PRIMITIVE_TYPES.put(STRING, null);
        PRIMITIVE_TYPES.put(OBJECT, OBJECT_CLASS);
    }
}
