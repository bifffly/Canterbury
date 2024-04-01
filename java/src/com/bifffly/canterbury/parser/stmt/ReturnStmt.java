package com.bifffly.canterbury.parser.stmt;

import com.bifffly.canterbury.parser.expr.Expr;
import com.bifffly.canterbury.tokens.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ReturnStmt implements Stmt {
    private Token token;
    private Expr value;

    @Override
    public <T> Object accept(StmtVisitor<T> stmtVisitor) {
        return stmtVisitor.visitReturnStmt(this);
    }
}
