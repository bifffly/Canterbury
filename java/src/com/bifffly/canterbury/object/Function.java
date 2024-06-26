package com.bifffly.canterbury.object;

import com.bifffly.canterbury.interpreter.Environment;
import com.bifffly.canterbury.interpreter.Interpreter;
import com.bifffly.canterbury.interpreter.Return;
import com.bifffly.canterbury.parser.expr.FuncExpr;
import com.bifffly.canterbury.tokens.Token;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class Function implements Callable {
    private final FuncExpr expr;
    private final Environment env;

    public Function bind(Instance instance) {
        Environment boundEnv = new Environment(env);
        boundEnv.define("self", instance);
        return new Function(expr, boundEnv);
    }

    @Override
    public int arity() {
        return expr.getParams().size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        Environment local = new Environment(env);
        List<Token> params = expr.getParams();
        for (int i = 0; i < arity(); i++) {
            local.define(params.get(i).getLexeme(), args.get(i));
        }
        try {
            interpreter.execBlock(expr.getBody().getStatements(), local);
        } catch (Return value) {
            return value.getValue();
        }
        return null;
    }

    @Override
    public String toString() {
        return "<func " + expr.getParams().stream().map(Token::getLexeme).toList() + ">";
    }
}
