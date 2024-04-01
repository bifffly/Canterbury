package com.bifffly.canterbury.parser.stmt;

public interface StmtVisitor<T> {
    T visitBlockStmt(BlockStmt stmt);
    T visitExpressionStmt(ExpressionStmt stmt);
    T visitIfStmt(IfStmt stmt);
    T visitImportStmt(ImportStmt stmt);
    T visitMatchStmt(MatchStmt stmt);
    T visitReturnStmt(ReturnStmt stmt);
    T visitWhileStmt(WhileStmt stmt);
}
