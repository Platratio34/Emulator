package com.peter.emulator.lang;

import org.eclipse.lsp4j.Position;

import com.peter.emulator.languageserver.ELLanguageServer;

public class ELSymbol {

    public final Type type;
    public final Span span;
    public final String text;

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
                
        ANNOTATION("support.type")
        ;

        public final String name;

        private Type(String name) {
            this.name = name;
        }
    }
}
