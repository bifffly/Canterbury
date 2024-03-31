package com.bifffly.canterbury.parser.expr;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GroupingExpr implements Expr {
    private final Expr expr;

    @Override
    public <T> T accept(ExprVisitor<T> exprVisitor) {
        return exprVisitor.visitGroupingExpr(this);
    }
}
