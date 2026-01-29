package com.peter.emulator.lang;

public class ELAnalysisError extends RuntimeException {

    public final Severity severity;
    public final String reason;
    public final Location location;

    public ELAnalysisError(Severity severity, String reason, Location location) {
        this.severity = severity;
        this.reason = reason;
        this.location = location;
        if(location == null)
            throw new NullPointerException();
    }

    public static ELAnalysisError info(String reason, Location location) {
        return new ELAnalysisError(Severity.INFO, reason, location);
    }
    public static ELAnalysisError info(String reason) {
        return new ELAnalysisError(Severity.INFO, reason, new Location("", 0, 0));
    }

    public static ELAnalysisError warning(String reason, Location location) {
        return new ELAnalysisError(Severity.WARNING, reason, location);
    }
    public static ELAnalysisError warning(String reason) {
        return new ELAnalysisError(Severity.WARNING, reason, new Location("", 0, 0));
    }
    
    public static ELAnalysisError error(String reason, Location location) {
        return new ELAnalysisError(Severity.ERROR, reason, location);
    }
    public static ELAnalysisError error(String reason) {
        return new ELAnalysisError(Severity.ERROR, reason, new Location("", 0, 0));
    }
    
    public static ELAnalysisError fatal(String reason, Location location) {
        return new ELAnalysisError(Severity.FATAL, reason, location);
    }
    public static ELAnalysisError fatal(String reason) {
        return new ELAnalysisError(Severity.FATAL, reason, new Location("", 0, 0));
    }

    @Override
    public String toString() {
        if (location.line() == 0)
            return severity + ": " + reason;
        return severity + ": " + reason +" (@ "+location+")";
    }

    public static enum Severity {
        INFO,
        WARNING,
        ERROR,
        FATAL;
    }
}
