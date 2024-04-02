package com.bifffly.canterbury.parser.expr;

import com.bifffly.canterbury.parser.stmt.Stmt;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CaseExpr implements Expr {
    private Expr condition;
    private Expr then;

    @Override
    public <T> T accept(ExprVisitor<T> exprVisitor) {
        return exprVisitor.visitCaseExpr(this);
    }
}
