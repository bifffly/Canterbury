package com.bifffly.canterbury.parser.expr;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class GroupingExpr implements Expr {
    private final Expr expr;

    @Override
    public <T> T accept(ExprVisitor<T> exprVisitor) {
        return exprVisitor.visitGroupingExpr(this);
    }
}
