package com.peter.emulator.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class Namespace {

    public final String cName;
    public final Namespace namespace;

    public HashMap<String, ELVariable> staticVariables = new HashMap<>();
    public HashMap<String, ELFunction> staticFunctions = new HashMap<>();
    public HashMap<String, Namespace> namespaces = new HashMap<>();

    protected HashMap<String, String> imports = new HashMap<>();

    public Namespace(String name) {
        cName = name;
        namespace = null;
    }
    public Namespace(String name, Namespace namespace) {
        cName = name;
        this.namespace = namespace;
        if(namespace != null)
            namespace.addNamespace(this);
    }

    public <T extends Namespace> T addNamespace(T ns) {
        if (namespaces.containsKey(ns.cName)) {
            throw new ELCompileException("Namespace " + cName + " already has has a Class named `" + ns.cName + "`");
        }
        if (staticVariables.containsKey(ns.cName) || staticFunctions.containsKey(ns.cName))
            throw new ELCompileException("Duplicate member name: `" + ns.cName + "` in namespace " + cName);
        if (ns.namespace != this) {
            throw new ELCompileException("Class " + ns.cName + " must be marked as in namespace " + cName + " (was marked as "+(ns.namespace != null ? ns.namespace.cName : "none")+")");
        }
        namespaces.put(ns.cName, ns);
        return ns;
    }
    
    public <T extends ELFunction> T addStaticFunction(T function) {
        if (function.namespace != this) {
            throw new ELCompileException("Function " + function.cName + " must be marked as in namespace " + cName + " (was marked as "+(function.namespace != null ? function.namespace.cName : "none")+")");
        }
        if (staticFunctions.containsKey(function.cName)) {
            staticFunctions.get(function.cName).addOverload(function);
        } else {
            if (staticVariables.containsKey(function.cName) || namespaces.containsKey(function.cName))
                throw new ELCompileException("Duplicate member name: `" + function.cName + "` in namespace " + cName);
            staticFunctions.put(function.cName, function);
        }
        return function;
    }

    public void addStaticVariable(ELVariable var) {
        if (staticVariables.containsKey(var.name) || namespaces.containsKey(var.name))
            throw new ELCompileException("Duplicate member name: `" + var.name + "` in namespace " + cName);
        staticVariables.put(var.name, var);
    }

    public String debugString() {
        return debugString("\n");
    }

    public String debugString(String prefix) {
        String out = "namespace " + getQualifiedName() + " {";
        boolean had = false;
        String internalPrefix = prefix + "\t";
        for (Entry<String, ELVariable> entry : staticVariables.entrySet()) {
            ELVariable var = entry.getValue();
            out += internalPrefix + var.debugString() + ";";
            had = true;
        }
        if (!staticFunctions.isEmpty()) {
            if (had)
                out += internalPrefix;
            had = false;
            for (Entry<String, ELFunction> entry : staticFunctions.entrySet()) {
                out += internalPrefix + entry.getValue().debugString(internalPrefix);
                if (entry.getValue().overloads != null)
                    for (ELFunction overload : entry.getValue().overloads) {
                        out += internalPrefix + overload.debugString(internalPrefix);
                    }
                had = true;
            }
        }
        if (had && !namespaces.isEmpty())
            out += internalPrefix;
        for (Entry<String, Namespace> entry : namespaces.entrySet()) {
            out += internalPrefix + entry.getValue().debugString(internalPrefix);
        }
        return out + prefix + "}";
    }

    public void addImport(String importNS, String alias) {
        imports.put(alias, importNS);
    }

    public void addImports(HashMap<String, String> importNSs) {
        for(Entry<String, String> entry : importNSs.entrySet())
            imports.put(entry.getKey(), entry.getValue());
    }
    
    public String getQualifiedName() {
        String name = cName;
        Namespace ns = namespace;
        while (ns != null) {
            name = ns.cName + "." + name;
            ns = ns.namespace;
        }
        return name;
    }

    public void append(Namespace ns) {
        for (Entry<String, ELFunction> entry : ns.staticFunctions.entrySet()) {
            addStaticFunction(entry.getValue());
        }
        for (Entry<String, ELVariable> entry : ns.staticVariables.entrySet()) {
            addStaticVariable(entry.getValue());
        }
        for (Entry<String, Namespace> entry : ns.namespaces.entrySet()) {
            addNamespace(entry.getValue());
        }
        addImports(ns.imports);
    }

    public boolean isParentNSOf(Namespace ns) {
        if (ns == this)
            return true;
        ns = ns.namespace;
        while (ns != null) {
            if (ns == this)
                return true;
            ns = ns.namespace;
        }
        return false;
    }
    
    public void resolve(ArrayList<ELAnalysisError> errors, ProgramModule module) {
        for (String ns : imports.values()) {
            if(module.getNamespaceIncluded(ns) == null)
                errors.add(ELAnalysisError.warning(String.format("Could not find imported namespace %s", ns)));
        }
        for (Namespace namespace : namespaces.values()) {
            namespace.resolve(errors, module);
        }
    }

    public void analyze(ArrayList<ELAnalysisError> errors, ProgramModule module) {
        for (ELFunction func : staticFunctions.values()) {
            func.analyze(errors, module);
        }
        for (ELVariable var : staticVariables.values()) {
            var.analyze(errors, this, module);
        }
        for (Namespace namespace : namespaces.values()) {
            namespace.analyze(errors, module);
        }
        errors.add(ELAnalysisError.info("Analyzed namespace "+getQualifiedName()));
    }

    public boolean hasType(ELType base) {
        return hasType(base, 0);
    }

    public boolean hasType(ELType base, int lvl) {
        // System.out.println("- Looking for type "+base.typeString() + " (in NS "+cName+")");
        String n = base.baseClass;
        boolean f = base.baseClassParents.size() == lvl;
        if (base.baseClassParents.isEmpty() && lvl != 0) {
            // System.err.println("- - had no parents, and was searching for lvl "+lvl);
            return false;
        }
        if (f) {
            // the last step on the chain
        } else if(base.baseClassParents.size() > lvl) {
            n = base.baseClassParents.get(lvl);
        } else {
            // System.out.println("- - Ran out of parents");
            return false;
        }
        if (namespaces.containsKey(n)) {
            Namespace ns = namespaces.get(n);
            if (!f) {
                // System.out.println("- - checking sub ns");
                return ns.hasType(base, lvl + 1);
            }
            if (!(ns instanceof ELClass)) {
                // System.out.println("- - Was not class");
                return false;
            }
            return true;
        }
        return false;
    }

    public ELClass getType(ELType base, Namespace srcNs, ProgramModule module) {
        return getType(base, 0, srcNs, module);
    }

    public ELClass getType(ELType base, int lvl, Namespace srcNs, ProgramModule module) {
        // System.out.println("- Looking for type "+base.typeString() + " (in NS "+cName+")");
        String n = base.baseClass;
        boolean f = base.baseClassParents.size() == lvl;
        if (base.baseClassParents.isEmpty() && lvl != 0) {
            // System.err.println("- - had no parents, and was searching for lvl "+lvl);
            return null;
        }
        if (f) {
            // the last step on the chain
        } else if(base.baseClassParents.size() > lvl) {
            n = base.baseClassParents.get(lvl);
        } else {
            // System.out.println("- - Ran out of parents");
            return null;
        }
        if (namespaces.containsKey(n)) {
            Namespace ns = namespaces.get(n);
            if (!f) {
                // System.out.println("- - checking sub ns");
                return ns.getType(base, lvl + 1, srcNs, module);
            }
            if (ns instanceof ELClass clazz) {
                return clazz;
            }
            // System.out.println("- - Was not class");
            return null;
        } else {
            // System.out.println("- - Did find in current");
        }
        if (namespace != null) {
            Namespace ns = namespace.getType(base, srcNs, module);
            if(ns != null && ns instanceof ELClass clazz)
                return clazz;
        }
        if (isParentNSOf(srcNs)) {
            // System.out.println("- - was parent, checking imports");
            if (imports.containsKey(n)) {
                Namespace ns = module.getNamespaceIncluded(imports.get(n));
                if (ns == null) {
                    // System.out.println("- - - Could not find imported NS: "+imports.get(n));
                    return null;
                }
                // System.out.println("- - - Checking imported NS: "+ns.getQualifiedName());
                if (!f) {
                    // System.out.println("- - checking sub ns");
                    return ns.getType(base, lvl + 1, srcNs, module);
                }
                if (ns instanceof ELClass clazz) {
                    return clazz;
                }
                // System.out.println("- - Was not class");
                return null;
            }
        }
        return null;
    }
}
