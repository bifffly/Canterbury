package com.bifffly.canterbury.parser.stmt;

public interface StmtVisitor<T> {
    T visitBlockStmt(BlockStmt stmt);
    T visitExpressionStmt(ExpressionStmt stmt);
    T visitIfStmt(IfStmt stmt);
    T visitWhileStmt(WhileStmt stmt);
}
