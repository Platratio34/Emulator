package com.peter.emulator.assembly;

import com.peter.emulator.lang.Span;
import com.peter.emulator.lang.ELAnalysisError.Severity;

public class AsmError {

    public final Severity severity;
    public final Span span;

    public final String message;

    public AsmError(Severity severity, Span span, String message) {
        this.severity = severity;
        this.span = span;
        this.message = message;
    }

    public AsmError(Severity severity, Span span, String message, Object... args) {
        this(severity, span, String.format(message, args));
    }

    public AsmError at(Span span) {
        return new AsmError(severity, span, message);
    }

    public static AsmError error(Span span, String message) {
        return new AsmError(Severity.ERROR, span, message);
    }

    public static AsmError error(Span span, String message, Object... args) {
        return new AsmError(Severity.ERROR, span, message, args);
    }
    public static AsmError error(String message) {
        return new AsmError(Severity.ERROR, null, message);
    }

    public static AsmError error(String message, Object... args) {
        return new AsmError(Severity.ERROR, null, message, args);
    }
    
    public static AsmError warning(Span span, String message) {
        return new AsmError(Severity.WARNING, span, message);
    }

    public static AsmError warning(Span span, String message, Object... args) {
        return new AsmError(Severity.WARNING, span, message, args);
    }
    public static AsmError warning(String message) {
        return new AsmError(Severity.WARNING, null, message);
    }
    public static AsmError warning(String message, Object... args) {
        return new AsmError(Severity.WARNING, null, message, args);
    }
    
    public static AsmError info(Span span, String message) {
        return new AsmError(Severity.INFO, span, message);
    }

    public static AsmError info(Span span, String message, Object... args) {
        return new AsmError(Severity.INFO, span, message, args);
    }
    public static AsmError info(String message) {
        return new AsmError(Severity.INFO, null, message);
    }

    public static AsmError info(String message, Object... args) {
        return new AsmError(Severity.INFO, null, message, args);
    }
    
    @Override
    public String toString() {
        return String.format("AsmError: %s: %s (@ %s)", severity, message, span.start());
    }
}
