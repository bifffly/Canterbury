#ifndef CANTERBURY_SCANNER_H
#define CANTERBURY_SCANNER_H

typedef enum {
    T_LEFT_PAREN, T_RIGHT_PAREN,
    T_lEFT_BRACKET, T_RIGHT_BRACKET,
    T_LEFT_BRACE, T_RIGHT_BRACE,

    T_MINUS, T_PLUS, T_STAR, T_SLASH,
    T_COMMA, T_COLON, T_BANG, T_EQUAL,
    T_BIT_AND, T_BIT_OR, T_BIT_NEG,
    T_UNDERSCORE, T_SEMICOLON,

    T_AND, T_OR, T_UNQEUAL,
    T_GREATER, T_GEQ,
    T_LESSER, T_LEQ,
    T_ARROW, T_WALRUS,

    T_NUM, T_STR, T_IDENT,

    T_FUNC, T_STRUCT, T_SELF,
    T_FOR, T_WHILE, T_IF, T_ELIF, T_ELSE,
    T_MATCH, T_AGAINST, T_IS,
    T_TRUE, T_FALSE, T_NULL,
    T_IMPORT, T_RETURN,

    T_ERR, T_EOF
} TokenType;

typedef struct {
    const char* start;
    const char* current;
    int line;
} Scanner;

typedef struct {
    TokenType type;
    const char* start;
    int length;
    int line;
} Token;

void initScanner(const char* source);

Token scanToken();

#endif