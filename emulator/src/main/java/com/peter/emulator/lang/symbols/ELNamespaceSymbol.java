package com.peter.emulator.lang.symbols;

import com.peter.emulator.lang.ELClass;
import com.peter.emulator.lang.ELSymbol;
import com.peter.emulator.lang.Namespace;
import com.peter.emulator.lang.Span;
import com.peter.emulator.lang.base.ELPrimitives;

public class ELNamespaceSymbol extends ELSymbol {

    public final Namespace namespace;
    public final String nameOverride;

    public ELNamespaceSymbol(Namespace namespace, Span span) {
        super((namespace instanceof ELClass) ? Type.CLASS_NAME : Type.NAMESPACE_NAME, span);
        this.namespace = namespace;
        nameOverride = null;
    }
    public ELNamespaceSymbol(String nameOverride, Span span) {
        super(Type.NAMESPACE_NAME, span);
        this.nameOverride = nameOverride;
        this.namespace = null;
    }

    @Override
    public boolean hasText() {
        return true;
    }

    @Override
    public String getText() {
        String out;
        if (nameOverride != null) {
            out = String.format("Namespace `%s`", nameOverride);
        } else if (namespace instanceof ELClass clazz) {
            String t = clazz.getClassType();
            out = String.format("%s%s `%s`", Character.toUpperCase(t.charAt(0)), t.substring(1),
                    namespace.getQualifiedName());
            if (clazz.parent != null && clazz.parent != ELPrimitives.OBJECT_CLASS) {
                out += String.format(" extends `%s`", clazz.parent.getQualifiedName());
            }
        } else {
            out = String.format("Namespace `%s`", namespace.getQualifiedName());
        }
        if (namespace.docComment != null)
            out += "\n\n" + namespace.docComment.getMD();
        return out;
    }
}