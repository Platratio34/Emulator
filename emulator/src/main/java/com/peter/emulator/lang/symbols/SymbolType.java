package com.peter.emulator.lang.symbols;

import java.util.ArrayList;
import java.util.HashMap;

import com.peter.emulator.lang.symbols.ELSymbol.Modifier;

public enum SymbolType {
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

    private SymbolType(String name, String semanticType, Modifier... modifiers) {
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