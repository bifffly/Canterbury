package com.bifffly.canterbury.parser.stmt;

public interface Stmt {
    <T> Object accept(StmtVisitor<T> stmtVisitor);
}
