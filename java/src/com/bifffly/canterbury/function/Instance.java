package com.bifffly.canterbury.function;

import com.bifffly.canterbury.interpreter.RuntimeError;
import com.bifffly.canterbury.tokens.Token;

import java.util.List;
import java.util.Map;

public class Instance {
    private Struct struct;
    private final Map<String, Object> attributes;

    public Instance(Struct struct, List<Object> args) {
        this.struct = struct;
        this.attributes = struct.getAttributes();

        // Adding param values to attributes
        for (int i = 0; i < struct.arity(); i++) {
            String paramName = struct.getExpr().getParams().get(i).getLexeme();
            attributes.put(paramName, args.get(i));

            // Need to add param values to environment of function attributes
            for (String name : struct.getAttributes().keySet()) {
                Object attribute = struct.getAttributes().get(name);
                if (attribute instanceof Function func) {
                    func.getEnv().define(paramName, args.get(i));
                }
            }
        }
    }

    public Object get(Token identifier) {
        if (attributes.containsKey(identifier.getLexeme())) {
            return attributes.get(identifier.getLexeme());
        }
        throw new RuntimeError(identifier, "Undefined attribute '" + identifier.getLexeme() + "'.");
    }

    @Override
    public String toString() {
        return "instance of " + struct.toString();
    }
}
