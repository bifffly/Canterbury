package com.bifffly.canterbury.parser;

import com.bifffly.canterbury.Canterbury;
import com.bifffly.canterbury.parser.expr.AssignmentExpr;
import com.bifffly.canterbury.parser.expr.BinaryExpr;
import com.bifffly.canterbury.parser.expr.CallExpr;
import com.bifffly.canterbury.parser.expr.ClassExpr;
import com.bifffly.canterbury.parser.expr.Expr;
import com.bifffly.canterbury.parser.expr.FuncExpr;
import com.bifffly.canterbury.parser.expr.GroupingExpr;
import com.bifffly.canterbury.parser.expr.LiteralExpr;
import com.bifffly.canterbury.parser.expr.StructExpr;
import com.bifffly.canterbury.parser.expr.UnaryExpr;
import com.bifffly.canterbury.parser.expr.VariableExpr;
import com.bifffly.canterbury.parser.stmt.BlockStmt;
import com.bifffly.canterbury.parser.stmt.ExpressionStmt;
import com.bifffly.canterbury.parser.stmt.IfStmt;
import com.bifffly.canterbury.parser.stmt.Stmt;
import com.bifffly.canterbury.parser.stmt.WhileStmt;
import com.bifffly.canterbury.tokens.Token;
import com.bifffly.canterbury.tokens.TokenType;

import java.util.ArrayList;
import java.util.List;

import static com.bifffly.canterbury.tokens.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int curr = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> stmts = new ArrayList<>();
        while (hasNext()) {
            stmts.add(statement());
        }
        return stmts;
    }

    private boolean hasNext() {
        return peek().getType() != EOF;
    }

    private Token peek() {
        return tokens.get(curr);
    }

    private Token previous() {
        return tokens.get(curr - 1);
    }

    private ParseError error(Token token, String message) {
        Canterbury.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();
        while (hasNext()) {
            switch (peek().getType()) {
                case FUNC:
                case STRUCT:
                case CLASS:
                case FOR:
                case IF:
                case WHILE:
                case MATCH:
                    return;
            }
            advance();
        }
    }

    private boolean check(TokenType type) {
        return hasNext() && peek().getType() == type;
    }

    private Token advance() {
        if (hasNext()) {
            curr++;
        }
        return previous();
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        throw error(peek(), message);
    }

    private Stmt statement() {
        if (match(IF)) {
            return ifStatement();
        }
        if (match(FOR)) {
            return forStatement();
        }
        if (match(WHILE)) {
            return whileStatement();
        }
        if (match(LEFT_BRACKET)) {
            return blockStatement();
        }
        return expressionStatement();
    }

    private Stmt ifStatement() {
        consume(LEFT_BRACKET, "Expect '[' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_BRACKET, "Expect ']' after condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELIF)) {
            elseBranch = ifStatement();
        } else if (match(ELSE)) {
            elseBranch = statement();
        }

        return new IfStmt(condition, thenBranch, elseBranch);
    }

    private Stmt forStatement() {
        consume(LEFT_BRACKET, "Expect '[' after 'for'.");

        Stmt initializer;
        if (match(COMMA)) {
            initializer = null;
        } else {
            initializer = expressionStatement();
            consume(COMMA, "Expect ',' after loop initializer.");
        }

        Expr condition = null;
        if (!check(COMMA)) {
            condition = expression();
        }
        consume(COMMA, "Expect ',' after loop condition.");

        Expr increment = null;
        if (!check(RIGHT_BRACKET)) {
            increment = expression();
        }
        consume(RIGHT_BRACKET, "Expect ']' after loop declaration.");

        consume(LEFT_BRACKET, "Expect '[' before loop body.");
        Stmt body = blockStatement();
        if (increment != null) {
            body = new BlockStmt(List.of(body, new ExpressionStmt(increment)));
        }

        if (condition == null) {
            condition = new LiteralExpr(true);
        }
        body = new WhileStmt(condition, body);

        if (initializer != null) {
            body = new BlockStmt(List.of(initializer, body));
        }

        return body;
    }

    private Stmt whileStatement() {
        consume(LEFT_BRACKET, "Expect '[' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_BRACKET, "Expect ']' after condition.");
        consume(LEFT_BRACKET, "Expect '[' before loop body.");
        Stmt body = blockStatement();
        return new WhileStmt(condition, body);
    }

    private Stmt blockStatement() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACKET) && hasNext()) {
            statements.add(statement());
        }

        consume(RIGHT_BRACKET, "Expect ']' after block.");
        return new BlockStmt(statements);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        return new ExpressionStmt(expr);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();
        if (match(WALRUS)) {
            Token walrus = previous();
            Expr value = assignmentValue();
            if (expr instanceof VariableExpr) {
                Token identifier = ((VariableExpr) expr).getIdentifier();
                return new AssignmentExpr(identifier, value);
            }
            error(walrus, "Invalid assignment target");
        }
        return expr;
    }

    private AssignmentExpr mandatoryAssignment() {
        Token identifier = consume(IDENTIFIER, "Expect identifier.");
        consume(WALRUS, "Expect walrus.");
        Expr value = assignmentValue();
        return new AssignmentExpr(identifier, value);
    }

    private Expr assignmentValue() {
        if (match(CLASS)) {
            return classValue();
        }
        if (match(STRUCT)) {
            return struct();
        }
        if (match(FUNC)) {
            return func();
        }
        else {
            return or();
        }
    }

    private Expr classValue() {
        Token decl = previous();
        consume(LEFT_BRACKET, "Expect '[' after class declaration.");
        List<Token> params = params();
        consume(LEFT_BRACKET, "Expect '[' before class body.");
        List<AssignmentExpr> body = new ArrayList<>();
        while (!check(RIGHT_BRACKET) && hasNext()) {
            body.add(mandatoryAssignment());
        }
        consume(RIGHT_BRACKET, "Expect ']' after class body.");
        return new ClassExpr(decl, params, body);
    }

    private Expr struct() {
        Token decl = previous();
        consume(LEFT_BRACKET, "Expect '[' after struct declaration.");
        List<Token> params = params();
        return new StructExpr(decl, params);
    }

    private Expr func() {
        Token decl = previous();
        consume(LEFT_BRACKET, "Expect '[' after function declaration.");
        List<Token> params = params();
        consume(LEFT_BRACKET, "Expect function body.");
        BlockStmt body = (BlockStmt) blockStatement();
        return new FuncExpr(decl, params, body);
    }

    private List<Token> params() {
        List<Token> params = new ArrayList<>();
        if (!check(RIGHT_BRACKET)) {
            do {
                if (params.size() >= 128) {
                    error(peek(), "Expected fewer than 128 parameters.");
                }
                params.add(consume(IDENTIFIER, "Expect identifier in params body."));
            } while (match(COMMA));
        }
        consume(RIGHT_BRACKET, "Expect ']' after params.");
        return params;
    }

    private Expr or() {
        return and();
    }

    private Expr and() {
        return equality();
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(EQUAL, UNEQUAL)) {
            Token op = previous();
            Expr right = comparison();
            expr = new BinaryExpr(expr, op, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while (match(LESSER, LESSER_EQUAL, GREATER, GREATER_EQUAL)) {
            Token op = previous();
            Expr right = term();
            expr = new BinaryExpr(expr, op, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(MINUS, PLUS)) {
            Token op = previous();
            Expr right = factor();
            expr = new BinaryExpr(expr, op, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(STAR, SLASH)) {
            Token op = previous();
            Expr right = factor();
            expr = new BinaryExpr(expr, op, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token op = previous();
            Expr right = unary();
            return new UnaryExpr(op, right);
        }
        return call();
    }

    private Expr call() {
        Expr expr = primary();
        while (true) {
            if (match(LEFT_BRACKET)) {
                expr = completeCall(expr);
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr completeCall(Expr callee) {
        List<Expr> args = new ArrayList<>();
        if (!check(RIGHT_BRACKET)) {
            do {
                if (args.size() >= 128) {
                    error(peek(), "Expected fewer than 128 arguments.");
                }
                args.add(expression());
            } while (match(COMMA));
        }
        Token bracket = consume(RIGHT_BRACKET, "Expect ']' after call arguments.");
        return new CallExpr(callee, bracket, args);
    }

    private Expr primary() {
        if (match(TRUE)) {
            return new LiteralExpr(true);
        }
        if (match(FALSE)) {
            return new LiteralExpr(false);
        }
        if (match(NULL)) {
            return new LiteralExpr(null);
        }
        if (match(NUM, STR)) {
            return new LiteralExpr(previous().getValue());
        }
        if (match(IDENTIFIER)) {
            return new VariableExpr(previous());
        }
        if (match(LEFT_BRACKET)) {
            Expr expr = expression();
            consume(RIGHT_BRACKET, "Expect ']' after expression.");
            return new GroupingExpr(expr);
        }

        throw error(peek(), "Expect expression.");
    }
}