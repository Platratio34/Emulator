package com.peter.emulator.lang.actions;

import java.util.ArrayList;

public class ExpressionAction extends Action {

    public ArrayList<Action> actions = new ArrayList<>();

    @Override
    public String toAssembly() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toAssembly'");
    }

    /*
    var1 = var2 + 1;
    
    ->
    EXPRESSION r1 var2 + 1
        RESOLVE r1 var2 BY VALUE
        INC r1 1
    RESOLVE r2 var1
    MEM STORE r1 r2
    
    ----
    
    var1 = var2 + (var3 * 3);
    
    ->
    EXPRESSION r1 var2 + (var3 * 3)
        RESOLVE r1 var2 BY VALUE
        EXPRESSION r2 var3 * 3
            RESOLVE r2 var3 BY VALUE
            LOAD r3 [3]
            MUL r2 r2 r3
        INC r1 r2
    RESOLVE r2 var1
    MEM STORE r1 r2
    
    ----
    
    var1 = (var2 + 3) * (var3 * 3);
    
    ->
    EXPRESSION r1 (var2 + 3) * (var3 * 3)
        EXPRESSION r1 var2 + 3
            RESOLVE r1 var2 BY VALUE
            INC r1 [3]
        EXPRESSION r2 var3 * 3
            RESOLVE r2 var3 BY VALUE
            LOAD r3 [3]
            MUL r2 r2 r3
        MUL r1 r1 r2
    RESOLVE r2 var1
    MEM STORE r1 r2
    
     */
}
