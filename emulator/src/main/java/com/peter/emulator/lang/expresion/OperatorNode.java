package com.peter.emulator.lang.expresion;

import com.peter.emulator.lang.ELType;
import com.peter.emulator.lang.ErrorSet;
import com.peter.emulator.lang.Span;
import com.peter.emulator.lang.actions.ActionScope;
import com.peter.emulator.lang.actions.Register;
import com.peter.emulator.lang.base.ELPrimitives;
import com.peter.emulator.lang.tokens.OperatorToken;

public class OperatorNode extends ExpresionNode {
    public final OperatorType type;
    public final OperatorToken token;

    public OperatorNode(ActionScope scope, OperatorType type, OperatorToken token) {
        super(scope);
        this.type = type;
        this.token = token;
    }
    public OperatorNode(ActionScope scope, OperatorType type, boolean single, OperatorToken token) {
        super(scope);
        this.type = type;
        this.single = single;
        this.token = token;
    }
    public OperatorNode single(OperatorType type) {
        return new OperatorNode(scope, type, true, token);
    }

    @Override
    public String printTree() {
        String out = "( ";
        if(single) {
            out += type.str;
            if(child1 != null) {
                out += child1.printTree();
            } else {
                out += "null";
            }
            return  out + " )";
        }
        if(child1 != null) {
            out += child1.printTree();
        } else {
            out += "null";
        }
        out += " " + type.str + " ";
        if(child2!= null) {
            out += child2.printTree();
        } else {
            out += "null";
        }
        return out + " )";
    }

    @Override
    public String printNode() {
        if(single)
            return type.str;
        return " " + type.str + " ";
    }
    @Override
    public boolean isConstant() {
        switch(type) {
            case ADDRESS -> {return false;}
            case DEREF -> {return false;}
            default -> {}
        }
        if(single) {
            if(child1 == null) {
                return true;
            }
            return child1.isConstant();
        }
        if(child2 == null && child1 == null) {
            return true;
        }
        if(child2 == null) {
            return child1.isConstant();
        }
        return child1.isConstant() && child2.isConstant();
    }

    @Override
    public int getConstant() {
        if(single) {
            if(child1 == null) {
                return type == OperatorType.NOT ? 1 : 0;
            }
            int v = child1.getConstant();
            return switch(type) {
                case ADDRESS -> v; // TODO
                case DEREF -> v; // TODO
                case NOT -> v == 0 ? 1 : 0;
                case SUB -> -v;

                default -> v;
                
            };
        }
        if(child2 == null && child1 == null) {
            return 0;
        }
        if(child2 == null) {
            return child1.getConstant();
        }
        int v1 = child1.getConstant();
        int v2 = child2.getConstant();
        return switch (type) {
            case ADD -> v1 + v2;
            case SUB -> v1 - v2;
            case MUL -> v1 * v2;
            case DIV -> v1 / v2;

            case BIT_AND -> v1 & v2;
            case BIT_OR -> v1 | v2;
            case BIT_NOR -> v1 ^ v2;

            case SHIFT_LEFT -> v1 << v2;
            case SHIFT_RIGHT -> v1 >> v2;
            
            case AND -> (v1 != 0 && v2 != 0) ? 1 : 0;
            case OR -> (v1 != 0 || v2 != 0) ? 1 : 0;

            case EQUALS -> (v1 == v2) ? 1 : 0;
            case NEQ -> (v1 != v2) ? 1 : 0;
            case LT -> (v1 < v2) ? 1 : 0;
            case LEQ -> (v1 <= v2) ? 1 : 0;
            case GT -> (v1 > v2) ? 1 : 0;
            case GEQ -> (v1 >= v2) ? 1 : 0;

            default -> 0;
        };
    }

    @Override
    public boolean validate(ErrorSet errors) {
        if(single) {
            if(child1 == null) {
                errors.error("Missing token after operator", token.endLocation.span());
                return false;
            }
            if(!child1.validate(errors)) {
                return false;
            }
            ELType t = child1.getType();
            switch(type) {
                case ADDRESS -> {
                    if(child1 instanceof VariableNode) {
                        return true;
                    }
                    errors.error("Can not get address of non-variable", span());
                    return false;
                }
                case DEREF -> {
                    if(!t.isResolvable()) {
                        errors.error(String.format("Can not dereference %s", t.typeString()), span());
                        return false;
                    }
                }
                case NOT -> {
                    if(!t.canCastTo(ELPrimitives.BOOL)) {
                        errors.error(String.format("Can not cast %s to boolean", t.typeString()), span());
                        return false;
                    }
                }
                case SUB -> {
                    if(!t.canCastTo(ELPrimitives.UINT32)) {
                        errors.error(String.format("Can not cast %s to uint32", t.typeString()), span());
                        return false;
                    }
                }
                default -> {
                    errors.error(String.format("Unknown single operator: `%s`", type.str), token.span());
                    return false;
                }
            }
            return true;
        }
        if(child1 == null) {
            errors.error("Missing token before operator", token.startLocation.span());
            return false;
        }
        if(child2 == null) {
            errors.error("Missing token after operator", token.endLocation.span());
            return false;
        }
        if(!child1.validate(errors) || !child2.validate(errors)) {
            return false;
        }
        ELType t1 = child1.getType();
        ELType t2 = child2.getType();
        if(type == OperatorType.SHIFT_LEFT || type == OperatorType.SHIFT_RIGHT) {
            boolean bad = false;
            if(!t1.canCastTo(ELPrimitives.UINT32)) {
                errors.error("", token);
                bad = true;
            }
            return !bad;
        }
        if(!t2.canCastTo(t1)) {
            errors.error(String.format("Can not cast %s to %s", t2.typeString(), t1.typeString()), token);
            return false;
        }
        return true;
    }

    @Override
    public ELType getType() {
        return switch(type) {
            case AND, OR, LEQ, GEQ, LT, GT, NEQ -> ELPrimitives.BOOL;
            default -> child1.getType();
        };
    }

    @Override
    public Span span() {
        if(single) {
            return token.startLocation.span(child1.span().end());
        }
        return child1.span().start().span(child2.span().end());
    }
    @Override
    public String toAssembly() {
        if(isConstant()) {
            return String.format("LOAD %s %d", register, getConstant());
        }
        
        if(type == OperatorType.SHIFT_LEFT || type == OperatorType.SHIFT_RIGHT) {
            child1.register = register;
            return child1.toAssembly() + String.format("\nLSH %s %s %d", register, register, child2.getConstant());
        }

        switch(type) {
            case ADD -> {
                if(child1.isConstant()) {
                    int v = child1.getConstant();
                    child2.register = register;
                    if(v < 0x8fff) {
                        return child2.toAssembly() + String.format("\nINC %s %d", register, v);
                    }
                    String str = child2.toAssembly();
                    Register r2 = new Register(scope);
                    r2.fistFree();
                    str += String.format("\nLOAD %s %d\nADD %s %s %s", r2, v, register, register, r2);
                    return str;
                } else if(child2.isConstant()) {
                    int v = child2.getConstant();
                    child1.register = register;
                    if(v < 0x8fff) {
                        return child1.toAssembly() + String.format("\nINC %s %d", register, v);
                    }
                    String str = child1.toAssembly();
                    Register r2 = new Register(scope);
                    r2.fistFree();
                    str += String.format("\nLOAD %s %d\nADD %s %s %s", r2, v, register, register, r2);
                    return str;
                }
                child1.register = register;
                String str = child1.toAssembly() + "\n";
                Register r2 = new Register(scope);
                r2.fistFree();
                r2.reserve();
                child2.register = r2;
                str += child2.toAssembly() + String.format("\nADD %s %s %s", register, register, r2);
                r2.release();
                return str;
            }
            case SUB -> {
                if(child2.isConstant()) {
                    int v = child2.getConstant();
                    child1.register = register;
                    if(v < 0x8fff) {
                        return child1.toAssembly() + String.format("\nINC %s %d", register, -v);
                    }
                    String str = child1.toAssembly();
                    Register r2 = new Register(scope);
                    r2.fistFree();
                    str += String.format("\nLOAD %s %d\nSUB %s %s %s", r2, v, register, register, r2);
                    return str;
                }
                child1.register = register;
                String str = child1.toAssembly() + "\n";
                Register r2 = new Register(scope);
                r2.fistFree();
                r2.reserve();
                child2.register = r2;
                str += child2.toAssembly() + String.format("\nSUB %s %s %s", register, register, r2);
                r2.release();
                return str;
            }
            case MUL -> {
                child1.register = register;
                String str = child1.toAssembly() + "\n";
                Register r2 = new Register(scope);
                r2.fistFree();
                r2.reserve();
                child2.register = r2;
                str += child2.toAssembly() + String.format("\nMUL %s %s %s", register, register, r2);
                r2.release();
                return str;
            }
            case DIV -> {
                child1.register = register;
                String str = child1.toAssembly() + "\n";
                Register r2 = new Register(scope);
                r2.fistFree();
                r2.reserve();
                child2.register = r2;
                str += child2.toAssembly() + String.format("\nDIV %s %s %s", register, register, r2);
                r2.release();
                return str;
            }
        }

        return "// Something went wrong: "+printNode();
    }
}