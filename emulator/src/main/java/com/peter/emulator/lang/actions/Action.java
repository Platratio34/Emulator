package com.peter.emulator.lang.actions;

public abstract class Action {

    public abstract String toAssembly();

    public static Action space() {
        return new Action() {

            @Override
            public String toAssembly() {
                return "\n";
            }
            
        };
    }
}
