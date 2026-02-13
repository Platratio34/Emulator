package com.peter.emulator.lang;

import java.util.HashMap;
import java.util.Map.Entry;

public class ProgramUnit {

    public final ProgramModule module;
    public final String uri;

    protected final HashMap<String, String> imports = new HashMap<>();

    public ProgramUnit(ProgramModule module, String uri) {
        this.module = module;
        this.uri = uri;
    }

    public void addImport(String importNS, String alias) {
        imports.put(alias, importNS);
    }

    public void addImports(HashMap<String, String> importNSs) {
        for (Entry<String, String> entry : importNSs.entrySet())
            imports.put(entry.getKey(), entry.getValue());
    }

    public boolean hasInclude(String alias) {
        return imports.containsKey(alias);
    }

    public String getInclude(String alias) {
        return imports.get(alias);
    }
    
    public void resolve(ErrorSet errors) {
        for (String ns : imports.values()) {
            if(module.getNamespaceIncluded(ns) == null) {
                String[] p = ns.split("\\.");
                String found = null;
                for(int i = p.length-2; i >= 0; i--) {
                    String n = p[0];
                    for(int j = 1; j <= i; j++) {
                        n += "."+p[j];
                    }
                    if(module.getNamespaceIncluded(n) != null) {
                        found = n;
                        break;
                    }
                }
                if(found != null)
                    errors.warning(String.format("Could not find imported namespace %s (but did find parent %s)", ns, found));
                else
                    errors.warning(String.format("Could not find imported namespace %s", ns));
            }
        }
    }

    public Namespace getNamespaceIncluded(String include) {
        return module.getNamespaceIncluded(include);
    }
}
