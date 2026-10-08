package com.peter.emulator.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.lsp4j.Position;

import com.peter.emulator.languageserver.ELLanguageServer;

public class ELSymbol {

    public final Type type;
    public final Span span;
    public final String text;
    protected int modifier = 0;

    public ELSymbol definition = null;

    public ELSymbol(Type type, Span span) {
        this.type = type;
        this.span = span;
        if(span == null) {
            throw new IllegalArgumentException("Span must be non-null");
        }
        this.text = null;
    }
    public ELSymbol(Type type, Span span, String text) {
        this.type = type;
        this.span = span;
        if(span == null) {
            throw new IllegalArgumentException("Span must be non-null");
        }
        this.text = text;
    }

    public ELSymbol(Type type, Span span, String text, Object... args) {
        this.type = type;
        this.span = span;
        if(span == null) {
            throw new IllegalArgumentException("Span must be non-null");
        }
        this.text = String.format(text, args);
    }

    public ELSymbol withModifiers(Modifier... modifiers) {
        for (Modifier m : modifiers) {
            modifier |= m.value;
        }
        return this;
    }
    
    public boolean contains(Position position, ELLanguageServer lServer) {
        return span.contains(position, lServer);
    }

    public String getText() {
        return text;
    }

    public boolean hasText() {
        return text != null;
    }

    public Type getType() {
        return type;
    }
    
    public int getModifier() {
        return modifier | type.modifier;
    }

    public boolean onAdd(ProgramUnit unit) {
        return false;
    }

    public enum Modifier {
        NONE("", 0x0),
        DECLARATION("declaration", 0b1),
        DEFINITION("definition", 0b10),
        READ_ONLY("readonly", 0b100),
        STATIC("static", 0b1000),
        DOCUMENTATION("documentation", 0b10000),
        LANGUAGE("defaultLibrary", 0b100000)
        ;

        public final String name;
        public final int value;

        public static List<String> MODIFIERS;

        private Modifier(String name, int value) {
            this.name = name;
            this.value = value;

            setup();
        }
        
        private void setup() {
            if(MODIFIERS == null)
                MODIFIERS = new ArrayList<>();
            if (value > 0) {
                MODIFIERS.add(name);
            }
        }
    }

    public enum Type {
        KEYWORD("keyword.other.compiler", "keyword"),
        INSTRUCTION("keyword.other.instruction","keyword"),
        SEMICOLON("keyword.other.compiler", "keyword"),
                
        COMMENT_LINE("comment.line", "comment"),
        COMMENT_BLOCK("comment.block", "comment"),
        COMMENT_DOC("comment.block.doc", "comment"),

        OPERATOR("keyword.other.compiler","operator"),

        NAMESPACE_NAME("entity.name.namespace", "namespace"),
        CLASS_NAME("entity.name.class", "class"),

        NUMERIC_LITERAL("constant.numeric", "number"),
        STRING_LITERAL("constant.numeric", "string"),
        STRING_LITERAL_ESCAPE("constant.character.escape", "escape"),

        VARIABLE_CONSTANT("variable.other.constant", "variable", Modifier.READ_ONLY),
        VARIABLE_FINAL("variable.other.constant", "variable", Modifier.READ_ONLY),
        VARIABLE_NAME("entity.name.variable", "variable"),
        PARAMETER("entity.name.variable", "parameter"),
        PROPERTY("entity.name.variable", "property"),

        FUNCTION_NAME("entity.name.function", "function"),

        ANNOTATION("support.type", "decorator");

        public final String name;
        public final String semanticType;
        public final int modifier;

        private static int nextI = 0;
        public static HashMap<String, Integer> NAME_TO_INDEX;
        public static ArrayList<String> TYPE_NAMES;

        private Type(String name, String semanticType, Modifier... modifiers) {
            this.name = name;
            this.semanticType = semanticType;
            int mod = 0;
            for (Modifier m : modifiers) {
                mod |= m.value;
            }
            this.modifier = mod;

            setup();
        }
        
        private void setup() {
            if (NAME_TO_INDEX == null)
                NAME_TO_INDEX = new HashMap<>();
            if (!NAME_TO_INDEX.containsKey(semanticType)) {
                // System.out.println("Added "+semanticType);
                NAME_TO_INDEX.put(semanticType, nextI++);
                if(TYPE_NAMES == null)
                    TYPE_NAMES = new ArrayList<>();
                TYPE_NAMES.add(semanticType);
            }
        }

        public int semanticTypeIndex() {
            return NAME_TO_INDEX.get(semanticType);
        }

        public static void init() {
            
        }
    }

    public static class ELVarSymbol extends ELSymbol {

        public final ELVariable var;

        public ELVarSymbol(ELVariable var, Span span) {
            super((var.finalVal ? Type.VARIABLE_FINAL : Type.VARIABLE_NAME), span);
            this.var = var;
            this.definition = var.defSymbol;
        }

        @Override
        public boolean hasText() {
            return true;
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
            // if (var.type.getELClass() != null) {
            //     out += "\n\nBase Class: `" + var.type.getELClass().getQualifiedName() + "`";
            //     out += "\n(`" + var.type.toString() + "`)";
            // }
            if (var.varType == ELVariable.Type.MEMBER) {
                out += "\n\nOffset: " + var.offset;
            }
            if (var.doc != null) {
                out += "\n\n" + var.doc.desc;
            }
            return out/* + "\n\n\n\n"+span.debugString()*/;
        }

    }

    private final ArrayList<ELSymbol> symbols = new ArrayList<>();

    public boolean isWrapper() {
        return false;
    }

    public ArrayList<ELSymbol> getSub() {
        return symbols;
    }

    protected ELSymbol addSymbol(Type type, Span span) {
        ELSymbol symbol = new ELSymbol(type, span);
        symbols.add(symbol);
        return symbol;
    }
    protected ELSymbol addSymbol(ELSymbol symbol) {
        symbols.add(symbol);
        return symbol;
    }
}
