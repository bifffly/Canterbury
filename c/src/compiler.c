#include <stdio.h>
#include <stdlib.h>

#include "common.h"
#include "compiler.h"
#include "scanner.h"

#ifdef DEBUG_PRINT_CODE
#include "debug.h"
#endif

Parser parser;
Chunk* compilingChunk;

static Chunk* currentChunk() {
    return compilingChunk;
}

static void errorAt(Token* token, const char* msg) {
    if (parser.panicState) {
        return;
    }
    parser.panicState = true;

    fprintf(stderr, "[line %d] ERROR", token->line);
    if (token->type == T_EOF) {
        fprintf(stderr, " at end");
    } else if (token->type != T_ERR) {
        fprintf(stderr, " at '%.*s'", token->length, token->start);
    }
    fprintf(stderr, ": %s\n", msg);

    parser.errorState = true;
}

static void error(const char* msg) {
    errorAt(&parser.previous, msg);
}

static void errorAtCurrent(const char* msg) {
    errorAt(&parser.current, msg);
}

static void advance() {
    parser.previous = parser.current;

    while (true) {
        parser.current = scanToken();
        if (parser.current.type != T_ERR) {
            break;
        }

        errorAtCurrent(parser.current.start);
    }
}

static void consume(TokenType type, const char* message) {
    if (parser.current.type == type) {
        advance();
        return;
    }
    errorAtCurrent(message);
}

static void emitByte(uint8_t byte) {
    writeChunk(currentChunk(), byte, parser.previous.line);
}

static void emitBytes(uint8_t byte1, uint8_t byte2) {
    emitByte(byte1);
    emitByte(byte2);
}

static uint8_t makeConst(Value value) {
    int constant = addConstant(currentChunk(), value);
    if (constant > UINT8_MAX) {
        error("Too many constants in one chunk.");
        return 0;
    }

    return (uint8_t) constant;
}

static void endCompiler() {
    emitByte(OP_RET);
#ifdef DEBUG_PRINT_CODE
    if (!parser.errorState) {
        disassembleChunk(currentChunk(), "code");
    }
#endif
}

static void binary();
static void grouping();
static void number();
static void unary();
static void literal();

ParseRule parseRules[] = {
    [T_LEFT_PAREN]    = {grouping, NULL,   PREC_NONE},
    [T_RIGHT_PAREN]   = {NULL,     NULL,   PREC_NONE},
    [T_LEFT_BRACE]    = {NULL,     NULL,   PREC_NONE},
    [T_RIGHT_BRACE]   = {NULL,     NULL,   PREC_NONE},
    [T_COMMA]         = {NULL,     NULL,   PREC_NONE},
    [T_MINUS]         = {unary,    binary, PREC_TERM},
    [T_PLUS]          = {NULL,     binary, PREC_TERM},
    [T_SEMICOLON]     = {NULL,     NULL,   PREC_NONE},
    [T_SLASH]         = {NULL,     binary, PREC_FACTOR},
    [T_STAR]          = {NULL,     binary, PREC_FACTOR},
    [T_BANG]          = {unary,    NULL,   PREC_NONE},
    [T_UNQEUAL]       = {NULL,     binary, PREC_EQUALITY},
    [T_WALRUS]        = {NULL,     NULL,   PREC_NONE},
    [T_EQUAL]         = {NULL,     binary, PREC_EQUALITY},
    [T_GREATER]       = {NULL,     binary, PREC_COMPARISON},
    [T_GEQ]           = {NULL,     binary, PREC_COMPARISON},
    [T_LESSER]        = {NULL,     binary, PREC_COMPARISON},
    [T_LEQ]           = {NULL,     binary, PREC_COMPARISON},
    [T_IDENT]         = {NULL,     NULL,   PREC_NONE},
    [T_STR]           = {NULL,     NULL,   PREC_NONE},
    [T_NUM]           = {number,   NULL,   PREC_NONE},
    [T_AND]           = {NULL,     NULL,   PREC_NONE},
    [T_STRUCT]        = {NULL,     NULL,   PREC_NONE},
    [T_ELSE]          = {NULL,     NULL,   PREC_NONE},
    [T_FALSE]         = {literal,  NULL,   PREC_NONE},
    [T_FOR]           = {NULL,     NULL,   PREC_NONE},
    [T_FUNC]          = {NULL,     NULL,   PREC_NONE},
    [T_IF]            = {NULL,     NULL,   PREC_NONE},
    [T_NULL]          = {literal,  NULL,   PREC_NONE},
    [T_OR]            = {NULL,     NULL,   PREC_NONE},
    [T_RETURN]        = {NULL,     NULL,   PREC_NONE},
    [T_SELF]          = {NULL,     NULL,   PREC_NONE},
    [T_TRUE]          = {literal,  NULL,   PREC_NONE},
    [T_WHILE]         = {NULL,     NULL,   PREC_NONE},
    [T_ERR]           = {NULL,     NULL,   PREC_NONE},
    [T_EOF]           = {NULL,     NULL,   PREC_NONE},
};

static ParseRule* getRule(TokenType type) {
    return &parseRules[type];
}

static void parsePrecedence(Precedence prec) {
    advance();
    ParseFn prefixRule = getRule(parser.previous.type)->prefix;
    if (prefixRule == NULL) {
        error("Expect expression.");
        return;
    }
    prefixRule();

    while (prec <= getRule(parser.current.type)->precedence) {
        advance();
        ParseFn infixRule = getRule(parser.previous.type)->infix;
        infixRule();
    }
}

static void expression() {
    parsePrecedence(PREC_ASSIGNMENT);
}

static void binary() {
    TokenType operatorType = parser.previous.type;
    ParseRule* rule = getRule(operatorType);
    parsePrecedence((Precedence)(rule->precedence + 1));

    switch (operatorType) {
        case T_PLUS: emitByte(OP_ADD); break;
        case T_MINUS: emitByte(OP_SUB); break;
        case T_STAR: emitByte(OP_MUL); break;
        case T_SLASH: emitByte(OP_DIV); break;
        case T_EQUAL: emitByte(OP_EQ); break;
        case T_UNQEUAL: emitBytes(OP_EQ, OP_NOT); break;
        case T_GREATER: emitByte(OP_GREATER); break;
        case T_GEQ: emitBytes(OP_LESSER, OP_NOT); break;
        case T_LESSER: emitByte(OP_LESSER); break;
        case T_LEQ: emitBytes(OP_GREATER, OP_NOT); break;
        default: return;
    }
}

static void grouping() {
    expression();
    consume(T_RIGHT_PAREN, "Expect ')' after expression.");
}

static void literal() {
    switch (parser.previous.type) {
        case T_FALSE: emitByte(OP_FALSE); break;
        case T_NULL: emitByte(OP_NULL); break;
        case T_TRUE: emitByte(OP_TRUE); break;
        default: return;
    }
}

static void number() {
    double value = strtod(parser.previous.start, NULL);
    emitBytes(OP_CONST, makeConst(NUM_VAL(value)));
}

static void unary() {
    TokenType operatorType = parser.previous.type;
    parsePrecedence(PREC_UNARY);
    switch (operatorType) {
        case T_BANG: emitByte(OP_NOT); break;
        case T_MINUS: emitByte(OP_NEG); break;
        default: return;
    }
}

bool compile(const char* source, Chunk* chunk) {
    initScanner(source);
    compilingChunk = chunk;
    parser.errorState = false;
    parser.panicState = false;

    advance();
    expression();
    consume(T_EOF, "Expect end of expression.");

    endCompiler();
    return !parser.errorState;
}