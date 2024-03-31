package com.bifffly.canterbury.parser.expr;

public interface ExprVisitor<T> {
    T visitAssignmentExpr(AssignmentExpr expr);
    T visitBinaryExpr(BinaryExpr expr);
    T visitCallExpr(CallExpr expr);
    T visitFuncExpr(FuncExpr expr);
    T visitGetExpr(GetExpr expr);
    T visitGroupingExpr(GroupingExpr expr);
    T visitLiteralExpr(LiteralExpr expr);
    T visitLogicalExpr(LogicalExpr expr);
    T visitSelfExpr(SelfExpr expr);
    T visitStructExpr(StructExpr expr);
    T visitUnaryExpr(UnaryExpr expr);
    T visitVariableExpr(VariableExpr expr);
}
