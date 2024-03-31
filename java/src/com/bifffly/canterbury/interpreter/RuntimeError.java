package com.bifffly.canterbury.interpreter;

import com.bifffly.canterbury.tokens.Token;

public class RuntimeError extends RuntimeException {
    private final Token token;

    public RuntimeError(Token token, String msg) {
        super(msg);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}
