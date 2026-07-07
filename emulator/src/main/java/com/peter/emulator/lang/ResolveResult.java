package com.peter.emulator.lang;

public class ResolveResult {

    public final Namespace namespace;
    public final ELVariable variable;
    public final ELFunction function;

    protected ResolveResult(Namespace ns, ELVariable var, ELFunction func) {
        namespace = ns;
        variable = var;
        function = func;
    }

    public static ResolveResult of(Namespace namespace) {
        return new ResolveResult(namespace, null, null);
    }

    public static ResolveResult of(ELVariable variable) {
        return new ResolveResult(null, variable, null);
    }
    
    public static ResolveResult of(ELFunction function) {
        return new ResolveResult(null, null, function);
    }
}
