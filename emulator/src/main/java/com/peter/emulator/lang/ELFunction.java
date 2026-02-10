package com.peter.emulator.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.peter.emulator.MachineCode;
import com.peter.emulator.lang.Token.BlockToken;
import com.peter.emulator.lang.Token.IdentifierToken;
import com.peter.emulator.lang.Token.OperatorToken;
import com.peter.emulator.lang.Token.SetToken;
import com.peter.emulator.lang.actions.Action;
import com.peter.emulator.lang.actions.ActionBlock;
import com.peter.emulator.lang.actions.ActionScope;
import com.peter.emulator.lang.actions.DirectAction;
import com.peter.emulator.lang.annotations.ELAnnotation;
import com.peter.emulator.lang.annotations.ELInterruptHandlerAnnotation;

public class ELFunction {

    public final ELProtectionLevel protection;
    public final boolean extern;
    public final Namespace namespace;
    public final String cName;
    public final Location startLocation;
    public final FunctionType type;
    public final boolean constexpr;
    public Location bodyLocation;
    public Location bodyEndLocation;

    public HashMap<String, ELType> params = new HashMap<>();
    public ArrayList<String> paramOrder = new ArrayList<>();
    public ELType ret = null;

    public boolean abstractFunction = false;

    public ArrayList<ELFunction> overloads = new ArrayList<>();
    public ArrayList<Token> body = null;
    public ArrayList<ELAnnotation> annotations = null;
    public ArrayList<Action> actions = new ArrayList<>();

    public ELFunction(ELProtectionLevel protection, boolean extern, Namespace namespace, String name, FunctionType type, boolean constexpr, Location location) {
        if (namespace == null)
            throw new NullPointerException("Namespace must be non-null");
        this.protection = protection;
        this.extern = extern;
        this.namespace = namespace;
        cName = name;
        this.type = type;
        this.constexpr = constexpr;
        this.startLocation = location;
    }

    public void addParameter(ELType type, String name) {
        if (params.containsKey(name)) {
            throw new ELCompileException("Function ");
        }
        params.put(name, type);
        paramOrder.add(name);
    }

    public void addOverload(ELFunction overload) {
        if (!overload.cName.equals(cName) || overload.namespace != namespace)
            throw new ELCompileException("Overload function mismatched "+(overload.namespace != namespace ? "namespace" : "name"));
        if (paramMatch(overload))
            throw new ELCompileException("Overload matches existing function (" + this + ")");
        for (ELFunction o : overloads) {
            if(o == overload)
                continue;
            if (o.paramMatch(overload))
                throw new ELCompileException("Overload matches existing function (" + o + ")");
        }
        overloads.add(overload);
    }

    protected boolean paramMatch(ELFunction other) {
        if (other.paramOrder.size() != paramOrder.size())
            return false;
        for (int i = 0; i < paramOrder.size(); i++) {
            if (!other.params.get(other.paramOrder.get(i)).equals(params.get(paramOrder.get(i))))
                return false;
        }
        return true;
    }

    public ELFunction getFunction(ArrayList<ELType> paramTypes) {
        return getFunction(paramTypes, false);
    }

    public ELFunction getFunction(ArrayList<ELType> paramTypes, boolean cast) {
        boolean self = paramOrder.size() == paramTypes.size();
        if (!cast) {
            if (self)
                for (int i = 0; i < paramOrder.size(); i++) {
                    if (!this.params.get(paramOrder.get(i)).equals(paramTypes.get(i))) {
                        self = false;
                        // System.out.println("Didn't match: " + this.params.get(paramOrder.get(i)) + " != " + paramTypes.get(i));
                        break;
                    }
                }
            if (self)
                return this;
            for (ELFunction ov : overloads) {
                ELFunction o = ov.getFunction(paramTypes);
                if (o != null)
                    return o;
            }
        }
        self = paramOrder.size() == paramTypes.size();
        if (self)
            for (int i = 0; i < paramOrder.size(); i++) {
                if (!paramTypes.get(i).canCastTo(this.params.get(paramOrder.get(i)))) {
                    self = false;
                    // System.out.println("Couldn't cast: "+paramTypes.get(i)+" -> "+this.params.get(paramOrder.get(i)));
                    break;
                }
            }
        if (self)
            return this;
        for (ELFunction ov : overloads) {
            ELFunction o = ov.getFunction(paramTypes, true);
            if (o != null)
                return o;
        }
        return null;
    }
    
    public <T extends ELAnnotation> boolean hasAnnotation(Class<T> clazz) {
        if (annotations == null)
            return false;
        for (ELAnnotation an : annotations) {
            if(clazz.isInstance(an))
                return true;
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends ELAnnotation> T getAnnotation(Class<T> clazz) {
        if (annotations == null)
            return null;
        for (ELAnnotation an : annotations) {
            if(clazz.isInstance(an))
                return (T)an;
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends ELAnnotation> ArrayList<T> getAnnotations(Class<T> clazz) {
        ArrayList<T> list = new ArrayList<>();
        if (annotations == null)
            return list;
        for (ELAnnotation an : annotations) {
            if(clazz.isInstance(clazz))
                list.add((T) an);
        }
        return list;
    }

    @Override
    public String toString() {
        String out = "ELFunction{";
        if (ret != null)
            out += ret.typeString() + " ";
        if (namespace != null)
            out += namespace.cName + ".";
        out += cName + "(";
        boolean fParam = true;
        for (String p : paramOrder) {
            if (!fParam)
                out += ",";
            fParam = false;
            out += params.get(p).typeString() + " " + p;
        }
        out += ")";
        if (overloads != null && !overloads.isEmpty())
            out += " + " + overloads.size() + " overload(s)";
        return out+"}";
    }

    // public String debugString() {
    //     return debugString("", false);
    // }

    public String debugString(String prefix) {
        return debugString(prefix, false);
    }

    public String debugString(String prefix, boolean constructor) {
        String out = "";
        if (annotations != null)
            for (ELAnnotation annotation : annotations) {
                out += annotation.debugString() + prefix;
            }
        if (type == FunctionType.OPERATOR)
            out += "operator ";
        else
            out += protection.value + " ";
        if (constexpr)
            out += "constexpr ";
        switch (type) {
            case CONSTRUCTOR -> out += ret.typeString() + "(";
            case DESTRUCTOR -> out += "~" + ret.typeString() + "(";
            default -> {
                if (extern)
                    out += "extern ";
                if (type == FunctionType.STATIC)
                    out += "static ";
                if (abstractFunction)
                    out += "abstract ";
                if (ret == null) {
                    out += "void ";
                } else {
                    out += ret.typeString() + " ";
                }   out += cName + "(";
            }
        }
        boolean f = true;
        for (String n : paramOrder) {
            if (!f)
                out += ", ";
            f = false;
            out += params.get(n).typeString() + " " + n;
        }
        out += ")";
        if (body != null)
            out += " {}";
        else
            out += ";";
        return out;
    }

    public void ingestParams(SetToken set) {
        ELType.Builder typeBuilder = new ELType.Builder();
        for (Token t : set.subTokens) {
            if (typeBuilder == null && t instanceof OperatorToken ot && ot.type == OperatorToken.Type.COMMA) {
                typeBuilder = new ELType.Builder();
                continue;
            } else if (typeBuilder != null) {
                if (typeBuilder.ingest(t))
                    continue;
                if (t instanceof IdentifierToken it) {
                    addParameter(typeBuilder.build(), it.value);
                    typeBuilder = null;
                    continue;
                }
                throw new ELCompileException("Unexpected token found in parameter (expected type or identifier): " + t);
            }
            throw new ELCompileException("Unexpected token found (function parameters expected): " + t);
        }
    }

    public void ingestBody(BlockToken block) {
        bodyLocation = block.startLocation;
        bodyEndLocation = block.endLocation;
        body = block.subTokens;
    }

    public void analyze(ErrorSet errors, ProgramModule module) {
        for (Entry<String, ELType> entry : params.entrySet()) {
            entry.getValue().analyze(errors, namespace, module);
        }
        if (ret != null)
            ret.analyze(errors, namespace, module);
        if (body == null && !(extern || abstractFunction)) {
            errors.error("Non-abstract or external functions must have a body", startLocation.span());
        } else if (body != null && (extern || abstractFunction)) {
            errors.error((extern ? "External" : "Abstract") + " functions must not have a body", bodySpan());
        }
        for (ELFunction overload : overloads) {
            overload.analyze(errors, module);
        }

        if (body != null) {
            ActionBlock bodyBlock = new ActionBlock(new ActionScope(namespace, null, 0), this);
            int l = paramOrder.size();
            for (int i = 0; i < l; i++) {
                bodyBlock.scope.addParam(paramOrder.get(i), params.get(paramOrder.get(i)), -(l - i), errors);
            }
            bodyBlock.parse(body, errors);
            actions.add(bodyBlock);
        }
    }

    public static enum FunctionType {
        STATIC,
        INSTANCE,
        CONSTRUCTOR,
        DESTRUCTOR,
        OPERATOR;
    }

    public String getQualifiedName() {
        return namespace.getQualifiedName()+"."+cName;
    }
    public String getQualifiedName(boolean incParams) {
        String o = namespace.getQualifiedName()+"."+cName;
        if(incParams) {
            for(String p : paramOrder) {
                o += "_"+params.get(p).typeString();
            }
        }
        return o;
    }

    public Span span() {
        return startLocation.span(bodyLocation);
    }

    public Span bodySpan() {
        if (bodyLocation == null)
            return startLocation.span();
        return bodyLocation.span(bodyEndLocation);
    }
}
