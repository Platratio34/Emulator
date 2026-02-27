package com.peter.emulator.lang;

import org.eclipse.lsp4j.Position;

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
            return String.format("`%s`", elType.typeString());
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
                out += "\n\nValue: `"+var.getValueDebug()+"`";
            return out/* + "\n\n\n\n"+span.debugString()*/;
        }

    }
}
