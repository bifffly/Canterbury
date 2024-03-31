package com.bifffly.canterbury.parser.expr;

public interface ExprVisitor<T> {
    T visitAssignmentExpr(AssignmentExpr expr);
    T visitBinaryExpr(BinaryExpr expr);
    T visitCallExpr(CallExpr expr);
    T visitClassExpr(ClassExpr expr);
    T visitFuncExpr(FuncExpr expr);
    T visitGroupingExpr(GroupingExpr expr);
    T visitLiteralExpr(LiteralExpr expr);
    T visitStructExpr(StructExpr expr);
    T visitUnaryExpr(UnaryExpr expr);
    T visitVariableExpr(VariableExpr expr);
}
