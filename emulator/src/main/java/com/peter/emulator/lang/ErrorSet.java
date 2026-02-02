package com.peter.emulator.lang;

import java.util.ArrayList;

public class ErrorSet extends ArrayList<ELAnalysisError> {

    private boolean hadError = false;

    @Override
    public boolean add(ELAnalysisError e) {
        if(e.severity == ELAnalysisError.Severity.ERROR || e.severity == ELAnalysisError.Severity.ERROR)
            hadError = true;
        return super.add(e);
    }

    public void combine(ErrorSet other) {
        addAll(other);
        if(other.hadError())
            hadError = true;
    }

    public boolean hadError() {
        return hadError;
    }

    public void info(String reason, Span span) {
        super.add(ELAnalysisError.info(reason, span));
    }
    public void info(String reason, Token token) {
        super.add(ELAnalysisError.info(reason, token));
    }
    public void info(String reason) {
        super.add(ELAnalysisError.info(reason));
    }

    public void warning(String reason, Span span) {
        super.add(ELAnalysisError.warning(reason, span));
    }
    public void warning(String reason, Token token) {
        super.add(ELAnalysisError.warning(reason, token));
    }
    public void warning(String reason) {
        super.add(ELAnalysisError.warning(reason));
    }
    
    public void error(String reason, Span span) {
        hadError = true;
        super.add(ELAnalysisError.error(reason, span));
    }
    public void error(String reason, Token token) {
        hadError = true;
        super.add(ELAnalysisError.error(reason, token));
    }
    public void error(String reason) {
        hadError = true;
        super.add(ELAnalysisError.error(reason));
    }
    
    public void fatal(String reason, Span span) {
        hadError = true;
        super.add(ELAnalysisError.fatal(reason, span));
    }
    public void fatal(String reason, Token token) {
        hadError = true;
        super.add(ELAnalysisError.fatal(reason, token));
    }
    public void fatal(String reason) {
        hadError = true;
        super.add(ELAnalysisError.fatal(reason));
    }
}
