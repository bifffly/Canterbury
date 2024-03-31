package com.bifffly.canterbury.parser.expr;

import com.bifffly.canterbury.parser.stmt.BlockStmt;
import com.bifffly.canterbury.tokens.Token;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class FuncExpr implements Expr {
    private Token decl;
    private List<Token> params;
    private BlockStmt body;

    @Override
    public <T> T accept(ExprVisitor<T> exprVisitor) {
        return exprVisitor.visitFuncExpr(this);
    }
}
