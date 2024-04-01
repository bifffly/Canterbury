package com.bifffly.canterbury.parser.stmt;

import com.bifffly.canterbury.parser.expr.CaseExpr;
import com.bifffly.canterbury.parser.expr.Expr;
import com.bifffly.canterbury.tokens.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class MatchStmt implements Stmt {
    private Token token;
    private Expr expr;
    private List<CaseExpr> cases;

    @Override
    public <T> T accept(StmtVisitor<T> stmtVisitor) {
        return stmtVisitor.visitMatchStmt(this);
    }
}
