package com.peter.emulator.lang.base;

import java.util.HashSet;

import com.peter.emulator.lang.ELClass;
import com.peter.emulator.lang.ELType;

public class ELPrimitives {

    // bool
    public static final ELType BOOL = new ELType("bool");
    // uint8
    public static final ELType UINT8 = new ELType("uint8");
    // char
    public static final ELType CHAR = new ELType("char");
    // uint16
    public static final ELType UINT16 = new ELType("uint16");
    // uint32
    public static final ELType UINT32 = new ELType("uint32");
    // void*
    public static final ELType VOID_PTR = new ELType.Builder("void").pointer().build();

    public static final ELType STRING = new ELType("string");

    public static final ELType OBJECT = new ELType("Object");
    public static final ELClass OBJECT_CLASS = new ELClass("Object");

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

    public static final HashSet<ELType> PRIMITIVE_TYPES = new HashSet<>();
    static {
        PRIMITIVE_TYPES.add(BOOL);
        PRIMITIVE_TYPES.add(UINT8);
        PRIMITIVE_TYPES.add(CHAR);
        PRIMITIVE_TYPES.add(UINT16);
        PRIMITIVE_TYPES.add(UINT32);
        PRIMITIVE_TYPES.add(STRING);
        PRIMITIVE_TYPES.add(OBJECT);
    }
}
