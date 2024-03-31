package com.bifffly.canterbury.function;

import com.bifffly.canterbury.interpreter.Environment;
import com.bifffly.canterbury.interpreter.Interpreter;
import com.bifffly.canterbury.parser.expr.FuncExpr;
import com.bifffly.canterbury.tokens.Token;

import java.util.List;

public class Function implements Callable {
    private final FuncExpr expr;
    private final Environment env;

    public Function(FuncExpr expr, Environment env) {
        this.expr = expr;
        this.env = env;
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
        return interpreter.execBlock(expr.getBody().getStatements(), local);
    }

    @Override
    public String toString() {
        return "<func " + expr.getParams().stream().map(Token::getLexeme).toList() + ">";
    }
}
