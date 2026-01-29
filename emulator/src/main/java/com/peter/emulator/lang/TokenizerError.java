package com.peter.emulator.lang;

public class TokenizerError extends RuntimeException {
    public String reason;

    public TokenizerError(String reason) {
        this.reason = reason;
    }
}
