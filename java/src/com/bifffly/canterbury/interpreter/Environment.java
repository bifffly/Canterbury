package com.bifffly.canterbury.interpreter;

import com.bifffly.canterbury.tokens.Token;
import lombok.EqualsAndHashCode;

import java.util.HashMap;

@EqualsAndHashCode
public class Environment {
    private final Environment parent;
    private final HashMap<String, Object> env;

    public Environment() {
        this.parent = null;
        this.env = new HashMap<>();
    }

    public Environment(Environment parent) {
        this.parent = parent;
        this.env = new HashMap<>();
    }

    public Environment(Environment parent, HashMap<String, Object> env) {
        this.parent = parent;
        this.env = env;
    }

    public HashMap<String, Object> getEnv() {
        return env;
    }

    public void define(String identifier, Object value) {
        env.put(identifier, value);
    }

    public void undefine(String identfier) {
        env.remove(identfier);
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

    @Override
    public Environment clone() {
        return new Environment(parent, (HashMap<String, Object>) env.clone());
    }
}
