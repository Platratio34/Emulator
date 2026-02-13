package com.peter.emulator.lang.actions;

public abstract class Action {

    public final ActionScope scope;
    public Action(ActionScope scope) {
        this.scope = scope;
    }
    public abstract String toAssembly();

    public static Action space() {
        return new Action(null) {

            @Override
            public String toAssembly() {
                return "\n";
            }
            
        };
    }
}
