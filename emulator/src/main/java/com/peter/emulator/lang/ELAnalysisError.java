package com.peter.emulator.lang;

public class ELAnalysisError extends RuntimeException {

    public final Severity severity;
    public final String reason;
    public final Span span;

    public ELAnalysisError(Severity severity, String reason, Span span) {
        this.severity = severity;
        this.reason = reason;
        this.span = span;
    }

    public static ELAnalysisError info(String reason, Span span) {
        return new ELAnalysisError(Severity.INFO, reason, span);
    }
    public static ELAnalysisError info(String reason, Token token) {
        return new ELAnalysisError(Severity.INFO, reason, token.span());
    }
    public static ELAnalysisError info(String reason) {
        return new ELAnalysisError(Severity.INFO, reason, null);
    }

    public static ELAnalysisError warning(String reason, Span span) {
        return new ELAnalysisError(Severity.WARNING, reason, span);
    }
    public static ELAnalysisError warning(String reason, Token token) {
        return new ELAnalysisError(Severity.WARNING, reason, token.span());
    }
    public static ELAnalysisError warning(String reason) {
        return new ELAnalysisError(Severity.WARNING, reason, null);
    }
    
    public static ELAnalysisError error(String reason, Span span) {
        return new ELAnalysisError(Severity.ERROR, reason, span);
    }
    public static ELAnalysisError error(String reason, Token token) {
        return new ELAnalysisError(Severity.ERROR, reason, token.span());
    }
    public static ELAnalysisError error(String reason) {
        return new ELAnalysisError(Severity.ERROR, reason, null);
    }
    
    public static ELAnalysisError fatal(String reason, Span span) {
        return new ELAnalysisError(Severity.FATAL, reason, span);
    }
    public static ELAnalysisError fatal(String reason, Token token) {
        return new ELAnalysisError(Severity.FATAL, reason, token.span());
    }
    public static ELAnalysisError fatal(String reason) {
        return new ELAnalysisError(Severity.FATAL, reason, null);
    }

    @Override
    public String toString() {
        if (span == null)
            return severity + ": " + reason;
        return severity + ": " + reason + " (@ "+span.start()+")";
    }

    public static enum Severity {
        INFO(3),
        WARNING(2),
        ERROR(1),
        FATAL(0);

        public final int level;
        private Severity(int lvl) {
            level = lvl;
        }

        public boolean atLeast(Severity other) {
            return level <= other.level;
        }
    }
}
