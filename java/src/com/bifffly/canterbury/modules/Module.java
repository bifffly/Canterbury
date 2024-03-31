package com.bifffly.canterbury.modules;

import com.bifffly.canterbury.interpreter.Environment;
import com.bifffly.canterbury.interpreter.Interpreter;
import com.bifffly.canterbury.object.Callable;
import com.bifffly.canterbury.tokens.Token;

import java.util.List;
import java.util.function.Function;

public class Module {
    private final String name;
    private final Environment env = new Environment();

    public Module(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void define(String identifier, Object value) {
        env.define(identifier, value);
    }

    public Object get(Token identifier) {
        return env.get(identifier);
    }

    void defineNativeFunction(String identifier, int arity, Function<Object, Object> value) {
        this.define(identifier, new Callable() {
            @Override
            public int arity() {
                return arity;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                if (arity == 0) {
                    return value.apply(null);
                }
                int i = 0;
                Function<Object, Object> returnValue = (Function<Object, Object>) value.apply(args.get(i));
                for (i = 1; i < arity - 1; i++) {
                    returnValue = (Function<Object, Object>) returnValue.apply(args.get(i));
                }
                return returnValue.apply(args.get(arity - 1));
            }

            @Override
            public String toString() {
                return "<native func " + identifier + ">";
            }
        });
    }
}
