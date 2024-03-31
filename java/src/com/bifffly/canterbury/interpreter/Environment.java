package com.bifffly.canterbury.interpreter;

import com.bifffly.canterbury.tokens.Token;

import java.util.HashMap;

public class Environment {
    private final Environment parent;
    private final HashMap<String, Object> env = new HashMap<>();

    public Environment() {
        this.parent = null;
    }

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public void define(String identifier, Object value) {
        env.put(identifier, value);
        /*
        if (parent != null) {
            parent.define(identifier, value);
        }
        */
    }

    public Object get(Token identifier) {
        if (env.containsKey(identifier.getLexeme())) {
            return env.get(identifier.getLexeme());
        }
        if (parent != null) {
            return parent.get(identifier);
        }
        throw new RuntimeError(identifier, "Undefined variable " + identifier.getLexeme() + ".");
    }
}
