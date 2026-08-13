package com.peter.emulator.lang.expresion;

import java.util.ArrayList;

import com.peter.emulator.lang.*;
import com.peter.emulator.lang.actions.Action;
import com.peter.emulator.lang.actions.ActionScope;
import com.peter.emulator.lang.actions.Register;
import com.peter.emulator.lang.base.ELPrimitives;
import com.peter.emulator.lang.tokens.*;

public class Expression extends Action {

    protected ExpresionNode head = null;
    protected ArrayList<ExpresionNode> nodes = new ArrayList<>();
    protected boolean lastWasOperator = true;

    public Expression(ActionScope scope) {
        super(scope);
    }
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public Expression(ActionScope scope, ArrayList<Token> tokens) {
        super(scope);
        for(Token token : tokens) {
            switch (token) {
                case OperatorToken ot -> {
                    switch(ot.type) {
                        case ADD -> { add(new OperatorNode(scope, OperatorType.ADD, ot)); }
                        case SUB -> { add(new OperatorNode(scope, OperatorType.SUB, ot)); }
                        case POINTER -> { add(new OperatorNode(scope, OperatorType.MUL, ot)); }
                        case DIV -> { add(new OperatorNode(scope, OperatorType.DIV, ot)); }
                        
                        case BITWISE_AND -> { add(new OperatorNode(scope, OperatorType.BIT_AND, ot)); }
                        case BITWISE_OR -> { add(new OperatorNode(scope, OperatorType.BIT_OR, ot)); }
                        case BITWISE_NOR -> { add(new OperatorNode(scope, OperatorType.BIT_NOR, ot)); }
                        
                        case LEFT_SHIFT -> { add(new OperatorNode(scope, OperatorType.SHIFT_LEFT, ot)); }
                        case RIGHT_SHIFT -> { add(new OperatorNode(scope, OperatorType.SHIFT_RIGHT, ot)); }
                        
                        case AND -> { add(new OperatorNode(scope, OperatorType.AND, ot)); }
                        case OR -> { add(new OperatorNode(scope, OperatorType.OR, ot)); }
                        
                        case EQ2 -> { add(new OperatorNode(scope, OperatorType.EQUALS, ot)); }
                        case ANGLE_LEFT -> { add(new OperatorNode(scope, OperatorType.LT, ot)); }
                        case LEQ -> { add(new OperatorNode(scope, OperatorType.LEQ, ot)); }
                        case ANGLE_RIGHT -> { add(new OperatorNode(scope, OperatorType.GT, ot)); }
                        case GEQ -> { add(new OperatorNode(scope, OperatorType.GEQ, ot)); }
                        case NEQ -> { add(new OperatorNode(scope, OperatorType.NEQ, ot)); }
                        
                        case NOT -> { add(new OperatorNode(scope, OperatorType.NOT, ot)); }
                        
                        // case INC -> {
                        // }
                        // case DEC -> {
                        // }
                        
                        // case TERNARY -> {
                        // }
                        // case COLON -> {
                        // }
                        
                        // case ASSIGN -> {
                        // }
                        
                        
                        // case ARRAY -> {
                        // }
                        // case COMMA -> {
                        // }
                        // case DESTRUCTOR -> {
                        // }
                        // case DOT -> {
                        // }
                        // case INDEX -> {
                        // }
                        
                        // case COMMENT -> {
                        // }
                        // case COMMENT_MULTILINE -> {
                        // }
                        
                        // case SUB_ASSIGN -> {
                        // }
                        // case ADD_ASSIGN -> {
                        // }
                        
                        default -> {
                            throw ELAnalysisError.errorF(ot, "Unexpected operator in expression: %s", ot.type.value);
                        }
                        
                    }
                }
                case NumberToken nt -> add(new LiteralNode(scope, nt.numValue, nt));
                case IdentifierToken it -> {
                    switch (it.value) {
                        case "true" -> add(new LiteralNode(scope, true, it));
                        case "false" -> add(new LiteralNode(scope, false, it));
                        case "nullptr" -> add(new LiteralNode(scope, 0, ELPrimitives.VOID_PTR, it));
                        default -> {
                            if(it.hasParamsSub()) {
                                
                            } else {
                                add(new VariableNode(scope, it));
                            }
                        }
                    }
                }
                case SetToken st -> add(new SubNode(new Expression(scope, st.subTokens)));
                default -> {
                }
            }
        }
    }

    protected void add(ExpresionNode node) {
        if(node instanceof OperatorNode opNode) {
            if(!opNode.single) {
                if(lastWasOperator) {
                    switch(opNode.type) {
                        case OperatorType.MUL -> node = opNode.single(OperatorType.DEREF);
                        case OperatorType.BIT_AND -> node = opNode.single(OperatorType.ADDRESS);
                        case OperatorType.NOT -> node = opNode.single(OperatorType.NOT);
                        case OperatorType.SUB -> node = opNode.single(OperatorType.SUB);
                        default -> throw new RuntimeException("Found invalid node type for singe operator: `"+opNode.type.str+"`");
                    }
                } else {
                    if(opNode.type == OperatorType.NOT) {
                        throw new RuntimeException("Found `!` but was not single operator");
                    }
                }
            }
            lastWasOperator = true;
        } else {
            lastWasOperator = false;
        }
        nodes.add(node);
        // System.out.println("- "+node.printTree());
        if(head == null) {
            head = node;
            System.out.println("\t"+printTree());
            return;
        }
        if(!(head instanceof OperatorNode)) {
            node.child1 = head;
            head = node;
            System.out.println("\t"+printTree());
            return;
        }
        OperatorNode headOp = (OperatorNode)head;
        if(node instanceof OperatorNode opNode) {
            int lvlOff = headOp.type.level - opNode.type.level;
            if(lvlOff >= 0) { // if we have a lower presidence than the current head, so swap the new node into the head
                opNode.child1 = head;
                head = opNode;
                System.out.println("\t"+printTree());
                return;
            }
            if(!opNode.single) {
                ExpresionNode cNode = head;
                ExpresionNode parent = head;
                while(cNode instanceof OperatorNode opNode2 && opNode.single == opNode2.single && (opNode2.type.level - opNode.type.level) < 0) {
                    parent = cNode;
                    if(cNode.child2 != null)
                        cNode = cNode.child2;
                    else if(cNode.child1 != null)
                        cNode = cNode.child1;
                    else
                        break;
                    if(cNode == null) {

                    }
                }
                node.child1 = cNode;
                if(parent.child1 == cNode) {
                    parent.child1 = node;
                } else {
                    parent.child2 = node;
                }
                System.out.println("\t"+printTree());
                return;
            }
        }
        ExpresionNode cNode = head;
        while(cNode.child1 != null && (cNode.single || cNode.child2 != null)) {
            cNode = cNode.child2 != null ? cNode.child2 : cNode.child1;
        }
        if(cNode.child1 == null) {
            cNode.child1 = node;
        } else if(cNode.child2 == null) {
            cNode.child2 = node;
        }
        System.out.println("\t"+printTree());
    }

    public String printNodes() {
        String out = "";
        for(ExpresionNode node : nodes) {
            out += node.printNode();
        }
        return out;
    }

    public String printTree() {
        if(head == null)
            return "NO TREE";
        return head.printTree();
    }

    public boolean isConstant() {
        if(head == null) {
            return true;
        }
        return head.isConstant();
    }
    
    public int getConstant() {
        if(head == null) {
            return 0;
        }
        return head.getConstant();
    }

    public boolean validate(ErrorSet errors) {
        return head.validate(errors);
    }

    public ELType getType() {
        return head.getType();
    }

    public Span span() {
        return head.span();
    }

    @Override
    public String toAssembly() {
        if(head == null) {
            return "";
        }
        return head.toAssembly();
    }

    public void setRegister(Register register) {
        if(head == null)
            return;
        head.register = register;
    }

    public static void test() {
        String exp1Str = "1 + 2 / SysD.rID + 4";
        ProgramUnit unit = new ProgramUnit(null, "");
        Tokenizer tokenizer = new Tokenizer(exp1Str, new Location("temp",0,0), unit);
        tokenizer.tokenize();
        ActionScope scope = new ActionScope(null, unit, null);
        Expression exp1 = new Expression(scope, tokenizer.tokens);
        System.out.println(exp1.printNodes());
        ErrorSet errorSet = new ErrorSet();
        if(!exp1.validate(errorSet)) {
            for(ELAnalysisError error : errorSet) {
                System.out.println(error.toString());
            }
        }
        Register reg = new Register(scope);
        reg.fistFree();
        reg.reserve();
        exp1.setRegister(reg);
        System.out.println(exp1.toAssembly());
    }

    /*
    1 + 1

      +
     / \
    1   1

    1 + 1 + 2

      +
     / \
    1   1
    
    1 + 1 * 2

      +
     / \
    1   *
       / \
      1   2 
    
    1 * 2 + 1

        +
       / \
      *   1
     / \
    1   2 

    a * b + c * d

    a

      *
     /
    a

      *
     / \
    a   b

        +
       /
      *
     / \
    a   b

        +
       / \
      *   c
     / \
    a   b

        +
       / \
      *   *
     / \ /
    a  b c

        +
       / \
      *   *
     / \ / \
    a  b c  d

    */
}
