package com.bifffly.canterbury.parser.stmt;

import com.bifffly.canterbury.parser.expr.Expr;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class WhileStmt implements Stmt {
    private Expr condition;
    private Stmt body;

    @Override
    public <T> Object accept(StmtVisitor<T> stmtVisitor) {
        return stmtVisitor.visitWhileStmt(this);
    }
}
