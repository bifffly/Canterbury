package com.bifffly.canterbury.parser.expr;

import com.bifffly.canterbury.tokens.Token;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class SelfExpr implements Expr {
    private Token self;

    @Override
    public <T> T accept(ExprVisitor<T> exprVisitor) {
        return exprVisitor.visitSelfExpr(this);
    }
}
