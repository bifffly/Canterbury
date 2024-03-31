package com.bifffly.canterbury.parser.expr;

import com.bifffly.canterbury.tokens.Token;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class AssignmentExpr implements Expr {
    private final Expr target;
    private final Expr value;

    @Override
    public <T> T accept(ExprVisitor<T> exprVisitor) {
        return exprVisitor.visitAssignmentExpr(this);
    }
}
