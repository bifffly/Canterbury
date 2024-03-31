package com.bifffly.canterbury.parser.expr;

public interface Expr {
    <T> T accept(ExprVisitor<T> exprVisitor);
}
