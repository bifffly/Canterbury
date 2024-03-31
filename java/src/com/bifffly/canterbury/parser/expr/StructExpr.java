package com.bifffly.canterbury.parser.expr;

import com.bifffly.canterbury.tokens.Token;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class StructExpr implements Expr {
    private Token decl;
    private List<Token> params;
    private List<AssignmentExpr> body;

    @Override
    public <T> T accept(ExprVisitor<T> exprVisitor) {
        return exprVisitor.visitStructExpr(this);
    }
}
