package com.bifffly.canterbury.parser.stmt;

import com.bifffly.canterbury.parser.expr.Expr;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class IfStmt implements Stmt {
    private Expr condition;
    private Stmt thenBranch;
    private Stmt elseBranch;

    @Override
    public <T> Object accept(StmtVisitor<T> stmtVisitor) {
        return stmtVisitor.visitIfStmt(this);
    }
}
