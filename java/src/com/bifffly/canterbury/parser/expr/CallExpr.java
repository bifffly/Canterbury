package com.bifffly.canterbury.parser.expr;

import com.bifffly.canterbury.tokens.Token;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class CallExpr implements Expr {
    private Expr callee;
    private Token bracket;
    private List<Expr> args;

    @Override
    public <T> T accept(ExprVisitor<T> exprVisitor) {
        return exprVisitor.visitCallExpr(this);
    }
}
