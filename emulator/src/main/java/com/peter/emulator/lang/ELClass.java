package com.peter.emulator.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import com.peter.emulator.lang.Token.OperatorToken;
import com.peter.emulator.lang.annotations.ELAnnotation;
import com.peter.emulator.lang.annotations.ELOperatorAnnotation;
import com.peter.emulator.lang.annotations.ELOverrideAnnotation;
import com.peter.emulator.lang.base.ELPrimitives;

public class ELClass extends Namespace {

    public final ProgramUnit unit;

    public ELClass parent = ELPrimitives.OBJECT_CLASS;
    public ELType parentType = ELPrimitives.OBJECT;

    public HashMap<String, ELType> generics = new HashMap<>();
    public ArrayList<String> genericsOrder = new ArrayList<>();

    public HashMap<String, ELFunction> memberFunctions = new HashMap<>();
    public HashMap<String, ELVariable> memberVariables = new HashMap<>();
    protected ArrayList<String> order = new ArrayList<>();
    public ArrayList<ELAnnotation> annotations = null;

    public HashMap<OperatorToken.Type, ELFunction> operators = new HashMap<>();
    public ELFunction casters = null;

    public ELFunction constructor = null;
    public ELFunction destructor = null;

    public boolean abstractClass = false;

    public ELClass(String name, Namespace namespace, ProgramUnit unit) {
        super(name, namespace);
        this.unit = unit;
        unit.classes.add(this);
    }

    public void addMember(ELVariable var) {
        if (memberVariables.containsKey(var.name) || staticVariables.containsKey(var.name) || staticFunctions.containsKey(var.name) || memberFunctions.containsKey(var.name))
            throw new ELCompileException("Duplicate member name: `"+var.name+"` in class "+cName);
        int size = 0;
        memberVariables.put(var.name, var);
        for (int i = 0; i < order.size(); i++) {
            int vSize = memberVariables.get(order.get(i)).sizeof();
            if (vSize == 1) {
                size++;
            } else if (vSize == 2) {
                int o = size % 2;
                size += o + vSize;
            } else if (vSize >= 3) {
                int o = size % 4;
                size += o + vSize;
            }
        }
        int vSize = var.sizeof();
        if (vSize == 2) {
            int o = size % 2;
            size += o;
        } else if (vSize >= 3) {
            int o = size % 4;
            size += o;
        }
        var.offset = size;
        order.add(var.name);
    }

    public int getSize() {
        int size = 0;
        for (int i = 0; i < order.size(); i++) {
            int vSize = memberVariables.get(order.get(i)).sizeof();
            if (vSize == 1) {
                size++;
            } else if (vSize == 2) {
                int o = size % 2;
                size += o + vSize;
            } else if (vSize >= 3) {
                int o = size % 4;
                size += o + vSize;
            }
        }
        size += size % 4;
        return size;
    }

    public int getOffset(String member) {
        if (!memberVariables.containsKey(member)) {
            throw new NoSuchElementException("Struct " + cName + " does not contain member variable " + member);
        }
        int size = 0;
        for (int i = 0; i < order.size(); i++) {
            String n = order.get(i);
            int vSize = memberVariables.get(n).sizeof();
            if (n.equals(member)) {
                if (vSize == 2) {
                    size += size % 2;
                } else if (vSize >= 3) {
                    size += size % 4;
                }
                return size;
            } else {
                if (vSize == 1) {
                    size++;
                } else if (vSize == 2) {
                    int o = size % 2;
                    size += o + vSize;
                } else if (vSize >= 3) {
                    int o = size % 4;
                    size += o + vSize;
                }
            }
        }
        return -1;
    }

    public ELFunction addFunction(ELFunction function) {
        if (function.namespace != this) {
            throw new ELCompileException("Function " + function.cName + " must be marked as in class " + cName
                    + " (was marked as " + (function.namespace != null ? function.namespace.getQualifiedName() : "none")
                    + ")");
        }
        if (function.type == ELFunction.FunctionType.OPERATOR) {
            if (function.annotations == null)
                throw new ELCompileException("Operator function must have an @Operator annotation");
            ELOperatorAnnotation opAnnotation = function.getAnnotation(ELOperatorAnnotation.class);
            if (opAnnotation == null)
                throw new ELCompileException("Operator function must have an @Operator annotation");
            if (opAnnotation.cast) {
                if(casters != null)
                    casters.addOverload(function);
                else
                    casters = function;
            } else {
                if (operators.containsKey(opAnnotation.type))
                    operators.get(opAnnotation.type).addOverload(function);
                else
                    operators.put(opAnnotation.type, function);
            }
            if(opAnnotation.cast) {
                return addStaticFunction(function);
            }
        }
        if (memberFunctions.containsKey(function.cName)) {
            memberFunctions.get(function.cName).addOverload(function);
        } else {
            if (memberVariables.containsKey(function.cName) || staticVariables.containsKey(function.cName)
                    || staticFunctions.containsKey(function.cName))
                throw new ELCompileException("Duplicate member name: `" + function.cName + "` in class " + cName);
            memberFunctions.put(function.cName, function);
        }
        return function;
    }
    
    public void setParentType(ELType type) {
        parent = null;
        parentType = type;
    }

    public ELType getType() {
        ELType t = new ELType(cName);
        for(String g : genericsOrder)
            t.genericTypes.add(new ELType(g));
        return t;
    }

    public String getClassType() {
        if (abstractClass)
            return "abstract class";
        return "class";
    }

    @Override
    public String debugString(String prefix) {
        String out = "";
        if (annotations != null)
            for (ELAnnotation annotation : annotations) {
                out += annotation.debugString() + prefix;
            }
        out += getClassType() + " " + cName;
        if (!genericsOrder.isEmpty()) {
            out += "<";
            boolean f = true;
            for (String t : genericsOrder) {
                if (!f)
                    out += ", ";
                f = false;
                out += t;
                ELType et = generics.get(t);
                if (et != null) {
                    out += " extends " + et.toString();
                }
            }
            out += ">";
        }
        if (!parentType.equals(ELPrimitives.OBJECT)) {
            out += " extends " + parentType.typeString();
        }
        out += " {";
        boolean had = false;
        String innerPrefix = prefix + "\t";
        for (Entry<String, ELVariable> entry : staticVariables.entrySet()) {
            ELVariable var = entry.getValue();
            out += innerPrefix + var.debugString() + ";";
            had = true;
        }
        if (!staticFunctions.isEmpty()) {
            if (had)
                out += innerPrefix;
            had = false;
            for (Entry<String, ELFunction> entry : staticFunctions.entrySet()) {
                out += innerPrefix + entry.getValue().debugString(innerPrefix);
                if (entry.getValue().overloads != null)
                    for (ELFunction overload : entry.getValue().overloads) {
                        out += innerPrefix + overload.debugString(innerPrefix);
                    }
                had = true;
            }
        }
        if (constructor != null) {
            if (had)
                out += innerPrefix;
            had = true;
            out += innerPrefix + constructor.debugString(innerPrefix, true);
            if (constructor.overloads != null)
                for (ELFunction overload : constructor.overloads) {
                    out += innerPrefix + overload.debugString(innerPrefix, true);
                }
        }
        if (destructor != null) {
            if (had)
                out += innerPrefix;
            had = true;
            out += innerPrefix + destructor.debugString(innerPrefix, true);
        }
        if (!memberVariables.isEmpty()) {
            if (had)
                out += innerPrefix;
            had = false;
            for (Entry<String, ELVariable> entry : memberVariables.entrySet()) {
                ELVariable var = entry.getValue();
                out += innerPrefix + var.debugString() + ";";
                had = true;
            }
        }
        if (!memberFunctions.isEmpty()) {
            if (had)
                out += innerPrefix;
            had = false;
            for (Entry<String, ELFunction> entry : memberFunctions.entrySet()) {
                out += innerPrefix + entry.getValue().debugString(innerPrefix);
                if (entry.getValue().overloads != null)
                    for (ELFunction overload : entry.getValue().overloads) {
                        out += innerPrefix + overload.debugString(innerPrefix);
                    }
                had = true;
            }
        }
        if (had && !namespaces.isEmpty())
            out += innerPrefix;
        for (Entry<String, Namespace> entry : namespaces.entrySet()) {
            out += innerPrefix + entry.getValue().debugString(innerPrefix);
        }
        return out + prefix + "}";
    }
    
    @Override
    public void append(Namespace ns) {
        throw new ELCompileException("Can not append classes");
    }

    public boolean isParentClassOf(Namespace ns) {
        if (this == ns)
            return true;
        if (ns instanceof ELClass clazz) {
            clazz = clazz.parent;
            while (clazz != null) {
                if(this == clazz)
                    return true;
                clazz = clazz.parent;
            }
        }
        return false;
    }

    @Override
    public ELClass getType(ELType base, Namespace srcNs, ProgramUnit unit) {
        if (srcNs == this) {

            String n = base.qualifiedBaseClass();
            if (generics.containsKey(n)) {
                ELType t = generics.get(n);
                if (t == null)
                    return ELPrimitives.OBJECT_CLASS;
                return getType(t.base(), 0, this, unit);
            }
        }
        return super.getType(base, srcNs, unit);
    }

    @Override
    public void analyze(ErrorSet errors) {
        for (ELFunction func : memberFunctions.values()) {
            func.analyze(errors);
            if(func.abstractFunction && !abstractClass)
                errors.error("Abstract functions must exist within an abstract class",
                        func.startLocation.span());
            ELOverrideAnnotation oa = func.getAnnotation(ELOverrideAnnotation.class);
            if (oa != null) {
                ArrayList<ELType> p = new ArrayList<>();
                for(String s : func.paramOrder)
                    p.add(func.params.get(s));
                boolean found = false;
                if (parent != null) {
                    ELClass c = parent;
                    while (c != null) {
                        if (!c.memberFunctions.containsKey(func.cName)) {
                            c = c.parent;
                            continue;
                        }
                        ELFunction f2 = c.memberFunctions.get(func.cName);
                        if (f2.getFunction(p) != null) {
                            found = true;
                            break;
                        }
                        c = c.parent;
                    }
                }
                if(!found)
                    errors.error("Function was marked override, but no matching parent function could be found", oa.span());
            }
        }
        for (ELVariable var : memberVariables.values()) {
            var.analyze(errors, this);
        }
        super.analyze(errors);
    }
    
    @Override
    public void resolve(ErrorSet errors) {
        if (parent == null) {
            ELType base = parentType.base();
            parent = getType(base, this, unit);
            if (parent == null) {
                errors.error(String.format("Could not resolve parent class %s for %s", base.typeString(), getQualifiedName()), parentType.span());
                parent = ELPrimitives.OBJECT_CLASS;
            }
            // errors.info("Found parent class for " + getQualifiedName()+": " + parent.getQualifiedName()));
        } else {
            // errors.info("Parent class for " + getQualifiedName() +" was "+parent.getQualifiedName()));
        }
        super.resolve(errors);
    }

    @Override
    public ELVariable getVar(Identifier identifier, int i) {
        if (memberVariables.containsKey(identifier.parts[i])) {
            return memberVariables.get(identifier.parts[i]);
        }
        return super.getVar(identifier, i);
    }
    
    @Override
    public ArrayList<ELVariable> getVarStack(Identifier identifier, ArrayList<ELVariable> stack) {
        if (memberVariables.containsKey(identifier.parts[stack.size()])) {
            ELVariable v = memberVariables.get(identifier.parts[stack.size()]);
            stack.add(v);
            if (v.type.clazz == null) {
                if (stack.size() == identifier.numParts())
                    return stack;
                throw ELAnalysisError.error("Could not resolve variable " + identifier.fullName, v.span());
            }
            return v.type.clazz.getVarStack(identifier, stack);
        }
        return super.getVarStack(identifier, stack);
    }

    public boolean canStaticCast(ELType target) {
        return false;
    }

    @Override
    protected ELFunction getFunction(String name) {
        if (memberFunctions.containsKey(name))
            return memberFunctions.get(name);
        return super.getFunction(name);
    }
    
    @Override
    protected ELFunction findFunction(String name, ArrayList<ELType> params) {
        if (memberFunctions.containsKey(name)) {
            return memberFunctions.get(name).getFunction(params);
        }
        return super.findFunction(name, params);
    }
}
