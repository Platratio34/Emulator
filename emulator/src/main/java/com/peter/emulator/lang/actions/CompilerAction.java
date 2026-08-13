package com.peter.emulator.lang.actions;

import java.util.function.Function;

public class CompilerAction extends Action {

    public Function<ActionScope, String> onCompile;

    public CompilerAction(ActionScope scope, Function<ActionScope, String> onCompile) {
        super(scope);
        this.onCompile = onCompile;
    }

    @Override
    public String toAssembly() {
        return onCompile.apply(scope);
    }

}
