package com.bifffly.canterbury.parser.stmt;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class BlockStmt implements Stmt {
    private List<Stmt> statements;

    @Override
    public <T> Object accept(StmtVisitor<T> stmtVisitor) {
        return stmtVisitor.visitBlockStmt(this);
    }
}
