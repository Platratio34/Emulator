package com.peter.emulator.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.peter.emulator.lang.Token.IdentifierToken;

public class Namespace {

    public final String cName;
    public final Namespace namespace;

    public HashMap<String, ELVariable> staticVariables = new HashMap<>();
    public HashMap<String, ELFunction> staticFunctions = new HashMap<>();
    public HashMap<String, Namespace> namespaces = new HashMap<>();

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

    public final ELFunction findFunction(Identifier id, ArrayList<ELType> params) {
        return findFunction(id, params, 0);
    }

    public final ELFunction findFunction(Identifier id, ArrayList<ELType> params, int index) {
        if (id.parts.length == index+1) { // must be in this namespace
            return findFunction(id.last(), params);
        }
        if (id.parts[index].equals(cName)) {
            if(id.parts.length == index+2)
                return findFunction(id.last(), params);
            if (namespaces.containsKey(id.parts[index + 1])) {
                return namespaces.get(id.parts[index + 1]).findFunction(id, params, index + 1);
            }
            return null;
        }
        if(index > 0)
            return null;
        Namespace ns = namespace;
        while (ns != null) {
            if (ns.cName.equals(id.parts[0])) {
                return ns.findFunction(id, params, 0);
            }
            ns = ns.namespace;
        }
        return null;
    }

    protected ELFunction findFunction(String name, ArrayList<ELType> params) {
        if (staticFunctions.containsKey(name)) {
            return staticFunctions.get(name).getFunction(params);
        }
        return null;
    }

    public final ELFunction getFunction(Identifier id) {
        return getFunction(id, 0);
    }

    public final ELFunction getFunction(Identifier id, int index) {
        if (id.parts.length == index+1) { // must be in this namespace
            return getFunction(id.last());
        }
        if (id.parts[index].equals(cName)) {
            if(id.parts.length == index+2)
                return getFunction(id.last());
            if (namespaces.containsKey(id.parts[index + 1])) {
                return namespaces.get(id.parts[index + 1]).getFunction(id, index + 1);
            }
            return null;
        }
        if(index > 0)
            return null;
        Namespace ns = namespace;
        while (ns != null) {
            if (ns.cName.equals(id.parts[0])) {
                return ns.getFunction(id, 0);
            }
            ns = ns.namespace;
        }
        return null;
    }
    
    protected ELFunction getFunction(String name) {
        if (staticFunctions.containsKey(name)) {
            return staticFunctions.get(name);
        }
        return null;
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
    
    public void resolve(ErrorSet errors) {
        for (Namespace namespace : namespaces.values()) {
            namespace.resolve(errors);
        }
    }

    public void analyze(ErrorSet errors) {
        for (ELFunction func : staticFunctions.values()) {
            func.analyze(errors);
        }
        for (ELVariable var : staticVariables.values()) {
            var.analyze(errors, this);
        }
        for (Namespace namespace : namespaces.values()) {
            namespace.analyze(errors);
        }
        errors.info("Analyzed namespace "+getQualifiedName());
    }

    public boolean hasType(ELType base) {
        return hasType(base, 0);
    }

    public boolean hasType(ELType base, int lvl) {
        // System.out.println("- Looking for type "+base.typeString() + " (in NS "+cName+")");
        String n = base.baseClass.last();
        boolean f = base.baseClass.numParts() == lvl+1;
        if (base.baseClass.numParts() > 1 && lvl != 0) {
            // System.err.println("- - had no parents, and was searching for lvl "+lvl);
            return false;
        }
        if (f) {
            // the last step on the chain
        } else if(base.baseClass.numParts() >= lvl) {
            n = base.baseClass.get(lvl);
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

    public ELClass getType(ELType base, Namespace srcNs, ProgramUnit unit) {
        return getType(base, 0, srcNs, unit);
    }

    public ELClass getType(ELType base, int lvl, Namespace srcNs, ProgramUnit unit) {
        // System.out.println("- Looking for type "+base.typeString() + " (in NS "+cName+")");
        String n = base.baseClass.last();
        boolean f = base.baseClass.numParts() == lvl+1;
        if (base.baseClass.numParts() > 1 && lvl != 0) {
            // System.err.println("- - had no parents, and was searching for lvl "+lvl);
            return null;
        }
        if (f) {
            // the last step on the chain
        } else if(base.baseClass.numParts() >= lvl) {
            n = base.baseClass.get(lvl);
        } else {
            // System.out.println("- - Ran out of parents");
            return null;
        }
        if (namespaces.containsKey(n)) {
            Namespace ns = namespaces.get(n);
            if (!f) {
                // System.out.println("- - checking sub ns");
                return ns.getType(base, lvl + 1, srcNs, unit);
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
            Namespace ns = namespace.getType(base, srcNs, unit);
            if(ns != null && ns instanceof ELClass clazz)
                return clazz;
        }
        if (isParentNSOf(srcNs)) {
            // System.out.println("- - was parent, checking imports");
            if (unit.hasInclude(n)) {
                Namespace ns = unit.getNamespaceIncluded(unit.getInclude(n));
                if (ns == null) {
                    // System.out.println("- - - Could not find imported NS: "+imports.get(n));
                    return null;
                }
                // System.out.println("- - - Checking imported NS: "+ns.getQualifiedName());
                if (!f) {
                    // System.out.println("- - checking sub ns");
                    return ns.getType(base, lvl + 1, srcNs, unit);
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

    public ELVariable getVar(Identifier identifier) {
        return getVar(identifier, 0);
    }

    public ELVariable getVar(Identifier identifier, int i) {
        if (staticVariables.containsKey(identifier.parts[i])) {
            return staticVariables.get(identifier.parts[i]);
        }
        if (namespace != null)
            return namespace.getVar(identifier, i);
        return null;
    }
    
    public ArrayList<ELVariable> getVarStack(Identifier identifier, ArrayList<ELVariable> stack) {
        if (staticVariables.containsKey(identifier.parts[stack.size()])) {
            ELVariable v = staticVariables.get(identifier.parts[stack.size()]);
            stack.add(v);
            if (v.type.clazz == null) {
                if (stack.size() == identifier.numParts())
                    return stack;
                throw ELAnalysisError.error("Could not resolve variable " + identifier.fullName, v.span());
            }
            return v.type.clazz.getVarStack(identifier, stack);
        }
        if (namespace != null)
            return namespace.getVarStack(identifier, stack);
        return stack;
    }
    
    public ELVariable getFirstVar(IdentifierToken id, ProgramUnit unit) {
        return getFirstVar(id, 0, unit);
    }

    protected ELVariable getFirstVar(IdentifierToken id, int index, ProgramUnit unit) {
        IdentifierToken it = id;
        if (index == 0) {
            if (id.value.equals(cName))
                index++;
        }
        if (index > 0 && (id.subTokens == null || index > id.subTokens.size()))
            return null;
        if (index > 0) {
            if (id.index != null)
                return null;
            it = id.sub(index - 1);
            if (index > 1)
                if (id.sub(index - 2) != null)
                    return null;
        }
        ELVariable v = getVariable(it.value);
        if (v != null)
            return v;
        if (namespaces.containsKey(it.value))
            return namespaces.get(it.value).getFirstVar(id, index + 1, null);
        if (index == 0 && unit != null) {
            // we only check parent/includes on the first step, and only if we couldn't find it in this namespace or a child namespace, and if we are not already searching an include
            if (unit.hasInclude(it.value)) {
                Namespace ns = unit.getNamespaceIncluded(unit.getInclude(it.value));
                if (ns == null)
                    throw ELAnalysisError.error("Unable to find included namespace " + unit.getInclude(it.value), it);
                return ns.getFirstVar(id, index + 1, null); // and don't check includes within the included namespace 
            }
            if (namespace != null)
                return namespace.getFirstVar(id, unit);
        }
        return null;
    }

    protected boolean hasVariable(String name) {
        return staticVariables.containsKey(name);
    }
    protected ELVariable getVariable(String name) {
        return staticVariables.get(name);
    }
}
