package com.bifffly.canterbury.parser.stmt;

import com.bifffly.canterbury.parser.expr.Expr;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ExpressionStmt implements Stmt {
    private final Expr expr;

    @Override
    public <T> Object accept(StmtVisitor<T> stmtVisitor) {
        return stmtVisitor.visitExpressionStmt(this);
    }
}
