package com.bifffly.canterbury.object;

import com.bifffly.canterbury.interpreter.Environment;
import com.bifffly.canterbury.tokens.Token;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode
public class Instance {
    private final Struct struct;
    private final Environment env;

    public Instance(Struct struct, Environment env, List<Object> args) {
        this.struct = struct;
        this.env = new Environment(env);
        struct.getAttributes().forEach(this.env::define);

        // Adding param values to attributes
        for (int i = 0; i < struct.arity(); i++) {
            String paramName = struct.getExpr().getParams().get(i).getLexeme();
            env.define(paramName, args.get(i));

            // Need to add param values to environment of function attributes
            for (String name : struct.getAttributes().keySet()) {
                Object attribute = struct.getAttributes().get(name);
                if (attribute instanceof Function func) {
                    func.getEnv().define(paramName, args.get(i));
                }
            }
        }
    }

    public void define(Token identifier, Object value) {
        env.define(identifier.getLexeme(), value);
    }

    public Object get(Token identifier) {
        Object value = env.get(identifier);
        if (value instanceof Function func) {
            return func.bind(this);
        }
        return env.get(identifier);
    }

    @Override
    public String toString() {
        return "instance of " + struct.toString();
    }
}
