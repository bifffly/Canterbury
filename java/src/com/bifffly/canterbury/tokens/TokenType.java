package com.bifffly.canterbury.tokens;

public enum TokenType {
    // Single-char tokens
    LEFT_PAREN, RIGHT_PAREN,
    LEFT_BRACKET, RIGHT_BRACKET,
    LEFT_BRACE, RIGHT_BRACE,
    MINUS, PLUS, STAR, SLASH,
    COMMA, COLON, BANG, EQUAL,
    BIT_AND, BIT_OR, BIT_NEG,
    UNDERSCORE, SEMICOLON,

    // Multi-char tokens
    AND, OR, UNEQUAL,
    GREATER, GREATER_EQUAL,
    LESSER, LESSER_EQUAL,
    ARROW, WALRUS,

    // Literals
    NUM, STR, IDENTIFIER,

    // Keywords
    FUNC, STRUCT, SELF,
    FOR, WHILE, IF, ELIF, ELSE,
    MATCH, AGAINST, IS,
    TRUE, FALSE, NULL,
    IMPORT, RETURN,

    EOF;
}
