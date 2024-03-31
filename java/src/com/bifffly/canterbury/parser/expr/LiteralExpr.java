package com.bifffly.canterbury.parser.expr;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class LiteralExpr implements Expr {
    private final Object value;

    @Override
    public <T> T accept(ExprVisitor<T> exprVisitor) {
        return exprVisitor.visitLiteralExpr(this);
    }
}
