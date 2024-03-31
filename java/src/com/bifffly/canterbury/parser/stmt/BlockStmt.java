package com.bifffly.canterbury.parser.stmt;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class BlockStmt implements Stmt {
    private List<Stmt> statements;

    @Override
    public <T> Object accept(StmtVisitor<T> stmtVisitor) {
        return stmtVisitor.visitBlockStmt(this);
    }
}
