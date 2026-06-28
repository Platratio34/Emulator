package com.peter.emulator.lang;

import org.eclipse.lsp4j.Position;

import com.peter.emulator.lang.ELFunction.FunctionType;
import com.peter.emulator.languageserver.ELLanguageServer;

public class ELSymbol {

    protected final Type type;
    protected final Span span;
    protected final String text;

    public ELSymbol(Type type, Span span) {
        this.type = type;
        this.span = span;
        this.text = null;
    }
    public ELSymbol(Type type, Span span, String text) {
        this.type = type;
        this.span = span;
        this.text = text;
    }

    public ELSymbol(Type type, Span span, String text, Object... args) {
        this.type = type;
        this.span = span;
        this.text = String.format(text, args);
    }
    
    public boolean contains(Position position, ELLanguageServer lServer) {
        return span.contains(position, lServer);
    }

    public String getText() {
        return text;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        COMMENT_LINE("comment.line"),
        COMMENT_BLOCK("comment.block"),
        COMMENT_DOC("comment.block.doc"),

        KEYWORD("keyword.other.compiler"),
        INSTRUCTION("keyword.other.instruction"),

        NAMESPACE_NAME("entity.name.namespace"),
        CLASS_NAME("entity.name.class"),

        NUMERIC_LITERAL("constant.numeric"),

        VARIABLE_CONSTANT("variable.other.constant"),
        VARIABLE_FINAL("variable.other.constant"),
        VARIABLE_NAME("entity.name.variable"),

        FUNCTION_NAME("entity.name.function"),

        ANNOTATION("support.type");

        public final String name;

        private Type(String name) {
            this.name = name;
        }
    }

    public static class ELTypeSymbol extends ELSymbol {

        public final ELType elType;

        public ELTypeSymbol(ELType type) {
            super(Type.CLASS_NAME, type.span());
            elType = type;
        }

        @Override
        public String getText() {
            return String.format("`%s` (%d bytes)", elType.typeString(), elType.sizeof());
        }
    }

    public static class ELVarSymbol extends ELSymbol {

        public final ELVariable var;

        public ELVarSymbol(ELVariable var, Span span) {
            super((var.finalVal ? Type.VARIABLE_FINAL : Type.VARIABLE_NAME), span);
            this.var = var;
        }

        @Override
        public String getText() {
            String out = "`";
            if(var.finalVal)
                out += "final ";
            else if (var.varType == ELVariable.Type.CONST)
                out += "const ";
            out += String.format("%s %s`", var.typeString(), (var.varType == ELVariable.Type.SCOPE) ? var.name : var.getQualifiedName());
            if ((var.finalVal || var.varType == ELVariable.Type.CONST) && var.hasValue())
                out += "\n\nValue: `" + var.getValueDebug() + "`";
            if (var.type.getELClass() != null) {
                out += "\n\nBase Class: `" + var.type.getELClass().getQualifiedName() + "`";
                out += "\n(`" + var.type.toString() + "`)";
            }
            return out/* + "\n\n\n\n"+span.debugString()*/;
        }

    }

    public static class ELFuncCallSymbol extends ELSymbol {

        public final ELFunction func;

        public ELFuncCallSymbol(ELFunction func, Span span) {
            super(Type.FUNCTION_NAME, span);
            this.func = func;
        }

        @Override
        public String getText() {
            String out = "`";
            // if(func.finalVal)
            //     out += "final ";
            // else if (func.varType == ELVariable.Type.CONST)
            //     out += "const ";
            // out += String.format("%s %s`", func.typeString(), (func.varType == ELVariable.Type.SCOPE) ? func.name : func.getQualifiedName());
            // if ((func.finalVal || func.varType == ELVariable.Type.CONST) && func.hasValue())
            //     out += "\n\nValue: `" + func.getValueDebug() + "`";
            // if (func.type.getELClass() != null) {
            //     out += "\n\nBase Class: `" + func.type.getELClass().getQualifiedName() + "`";
            //     out += "\n(`" + func.type.toString() + "`)";
            // }
            out += func.protection.value + " ";
            if (func.type == FunctionType.STATIC)
                out += "static ";
            if (func.extern)
                out += "extern ";
            if (func.constexpr)
                out += "constexpr ";
            if (func.ret != null)
                out += func.ret.typeString() + " ";
            else
                out += "void ";
            switch (func.type) {
                case CONSTRUCTOR -> {
                    out += func.namespace.getQualifiedName();
                }
                case DESTRUCTOR -> {
                    out += func.namespace.namespace.getQualifiedName() + ".~" + func.namespace.cName;
                }
                case OPERATOR, INSTANCE, STATIC -> {
                    out += func.getQualifiedName();
                }
            }
            out += "(";
            boolean f = true;
            for (String pName : func.paramOrder) {
                if (!f)
                    out += ", ";
                ELType param = func.params.get(pName);
                out += String.format("%s %s", func.params.get(pName).typeString(), pName);
                f = false;
            }
            out += ")`";
            return out/* + "\n\n\n\n"+span.debugString()*/;
        }

    }
}
