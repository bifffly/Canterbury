package com.bifffly.canterbury.parser.expr;

import com.bifffly.canterbury.tokens.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class MatchExpr implements Expr {
    private Token token;
    private Expr expr;
    private List<CaseExpr> cases;

    @Override
    public <T> T accept(ExprVisitor<T> exprVisitor) {
        return exprVisitor.visitMatchExpr(this);
    }
}
