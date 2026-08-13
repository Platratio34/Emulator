package com.peter.emulator.lang.expresion;

public enum OperatorType {
    ADD(0,"+"),
    SUB(0,"-"),
    MUL(1,"*"),
    DIV(1,"/"),

    AND(4,"&&"),
    OR(4,"||"),

    EQUALS(4,"=="),
    LT(4,"<"),
    LEQ(4,"<="),
    GT(4,">"),
    GEQ(4,">="),
    NEQ(4,"!="),

    BIT_AND(5,"&"),
    BIT_OR(5,"|"),
    BIT_NOR(5,"^"),

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