package com.bifffly.canterbury.function;

import com.bifffly.canterbury.interpreter.Interpreter;
import com.bifffly.canterbury.parser.expr.StructExpr;
import com.bifffly.canterbury.tokens.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
public class Struct implements Callable {
    private final StructExpr expr;
    private final Map<String, Object> attributes;

    @Override
    public int arity() {
        return expr.getParams().size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        return new Instance(this, interpreter.getEnv(), args);
    }

    @Override
    public String toString() {
        return "<struct " + expr.getParams().stream().map(Token::getLexeme).toList() + ">";
    }
}
