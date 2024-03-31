package com.bifffly.canterbury.parser.expr;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LiteralExpr implements Expr {
    private final Object value;

    @Override
    public <T> T accept(ExprVisitor<T> exprVisitor) {
        return exprVisitor.visitLiteralExpr(this);
    }
}
