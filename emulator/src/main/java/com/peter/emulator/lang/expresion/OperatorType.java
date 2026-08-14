package com.peter.emulator.lang.expresion;

public enum OperatorType {
    ADD(0,"+"),
    SUB(0,"-"),
    MUL(1,"*"),
    DIV(1,"/"),

    AND(-2,"&&"),
    OR(-2,"||"),

    EQUALS(-1,"=="),
    LT(-1,"<"),
    LEQ(-1,"<="),
    GT(-1,">"),
    GEQ(-1,">="),
    NEQ(-1,"!="),

    BIT_AND(5,"&"),
    BIT_OR(5,"|"),
    BIT_XOR(5,"^"),
    BIT_NOT(5,"~"),

    SHIFT_LEFT(5,"<<"),
    SHIFT_RIGHT(5,">>"),

    NOT(8,"!"),

    DEREF(9,"*"),
    ADDRESS(9,"&")
    ;

    public final int level;
    public final String str;

    private OperatorType(int level, String str) {
        this.level = level;
        this.str = str;
    }
}