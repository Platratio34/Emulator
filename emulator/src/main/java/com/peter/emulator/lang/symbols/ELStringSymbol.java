package com.peter.emulator.lang.symbols;

import com.peter.emulator.lang.ELSymbol;
import com.peter.emulator.lang.ELValue.ELStringValue;
import com.peter.emulator.lang.Location;
import com.peter.emulator.lang.Span;
import com.peter.emulator.lang.tokens.StringToken;

public class ELStringSymbol extends ELSymbol {

    public final String value;
    public final boolean block;

    public ELStringSymbol(StringToken token) {
        super(Type.STRING_LITERAL, token.span());
        this.value = token.raw;
        // System.out.println(value);
        block = token.block;
        setup();
    }

    public ELStringSymbol(ELStringValue value) {
        this(value.stringToken);
    }

    public ELStringSymbol(String raw, Span span) {
        super(Type.STRING_LITERAL, span);
        this.value = raw;
        block = false;
        setup();
    }
    
    protected final void setup() {
        final int strLen = value.length();
        Location lastStart = span.start();
        Location current = span.start();
        if (block) {
            current = current.add(2);
            addSymbol(Type.KEYWORD, lastStart.span(current));
            current = current.add(1);
            lastStart = current;
        }
        for (int i = 0; i < strLen; i++) {
            if(block && (i < 3 || i > strLen - 2))
                continue;
            char c = value.charAt(i);
            if (c == '\\') {
                // System.out.println(i);
                if (i + 1 >= strLen) {
                    continue;
                }
                if (block) {
                    switch (value.charAt(i + 1)) {
                        case '0', '\\', 't' -> {
                        }
                        default -> {
                            current = current.add(1);
                            continue;
                        }
                    }
                }
                addSymbol(Type.STRING_LITERAL, lastStart.span(current));
                addSymbol(Type.STRING_LITERAL_ESCAPE,
                        current.add(1).span(current.add(2)));
                i++;
                current = current.add(2);
                lastStart = current.add(1);
            } else if (c == '\n') {
                addSymbol(Type.STRING_LITERAL, lastStart.span(current));
                current = new Location(current.file(), current.line() + 1, 1);
                lastStart = current.add(1);
            } else {
                current = current.add(1);
            }
        }
        if (block) {
            addSymbol(Type.STRING_LITERAL, lastStart.span(span.end().add(-3)));
            addSymbol(Type.KEYWORD, span.end().add(-2).span(span.end()));
        } else {
            addSymbol(Type.STRING_LITERAL, lastStart.span(span.end()));
        }
    }
    
    @Override
    public boolean isWrapper() {
        return true;
    }

}
