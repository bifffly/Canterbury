#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "common.h"
#include "compiler.h"
#include "scanner.h"
#include "object.h"

#ifdef DEBUG_PRINT_CODE
#include "debug.h"
#endif

Parser parser;
Compiler* current = NULL;
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

static bool check(TokenType type) {
    return parser.current.type == type;
}

static bool match(TokenType type) {
    if (!check(type)) {
        return false;
    }
    advance();
    return true;
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

static void initCompiler(Compiler* compiler) {
    compiler->localCount = 0;
    compiler->scopeDepth = 0;
    current = compiler;
}

static void endCompiler() {
    emitByte(OP_RET);
#ifdef DEBUG_PRINT_CODE
    if (!parser.errorState) {
        disassembleChunk(currentChunk(), "code");
    }
#endif
}

static void beginScope() {
    current->scopeDepth++;
}

static void endScope() {
    current->scopeDepth--;
    while (current->localCount > 0 && current->locals[current->localCount - 1].depth
        > current->scopeDepth) {
        emitByte(OP_POP);
        current->localCount--;
    }
}

static void expressionStatement();
static void expression();
static void block();
static void binary(bool canAssign);
static void grouping(bool canAssign);
static void number(bool canAssign);
static void unary(bool canAssign);
static void literal(bool canAssign);
static void string(bool canAssign);
static void var(bool canAssign);

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
    [T_IDENT]         = {var,      NULL,   PREC_NONE},
    [T_STR]           = {string,   NULL,   PREC_NONE},
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
    bool canAssign = prec <= PREC_ASSIGNMENT;
    prefixRule(canAssign);

    while (prec <= getRule(parser.current.type)->precedence) {
        advance();
        ParseFn infixRule = getRule(parser.previous.type)->infix;
        infixRule(canAssign);
    }

    if (canAssign && match(T_WALRUS)) {
        error("Invalid assignment target.");
    }
}

static uint8_t identConst(Token* token) {
    return makeConst(OBJ_VAL(strcopy(token->start, token->length)));
}

static bool identifiersEqual(Token* a, Token* b) {
    if (a->length != b->length) {
        return false;
    }
    return memcmp(a->start, b->start, a->length) == 0;
}

static void addLocal(Token token) {
    if (current->localCount > UINT8_COUNT) {
        error("Too many local variables in function.");
        return;
    }
    Local* local = &current->locals[current->localCount++];
    local->name = token;
    local->depth = -1;
}

static int resolveLocal(Compiler* compiler, Token* token) {
    if (current->scopeDepth == 0) {
        return -1;
    }

    for (int i = compiler->localCount - 1; i >= 0; i--) {
        Local* local = &compiler->locals[i];
        if (identifiersEqual(token, &local->name)) {
            return i;
        }
    }

    return -1;
}

static void sync() {
    parser.panicState = false;

    while (parser.current.type != T_EOF) {
        if (parser.previous.type == T_SEMICOLON) return;
        switch (parser.current.type) {
            case T_STRUCT:
            case T_FUNC:
            case T_FOR:
            case T_IF:
            case T_WHILE:
            case T_RETURN: return;
            default:;
        }
        advance();
    }
}

static void statement() {
    if (match(T_LEFT_BRACE)) {
        beginScope();
        block();
        endScope();
    } else {
        expressionStatement();
    }
    if (parser.panicState) {
        sync();
    }
}

static void expressionStatement() {
    expression();
    consume(T_SEMICOLON, "Expect ';' after expression.");
    emitByte(OP_POP);
}

static void expression() {
    parsePrecedence(PREC_ASSIGNMENT);
}

static void block() {
    while (!check(T_RIGHT_BRACE) && !check(T_EOF)) {
        statement();
    }

    consume(T_RIGHT_BRACE, "Expect '}' after block.");
}

static void binary(bool canAssign) {
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

static void grouping(bool canAssign) {
    expression();
    consume(T_RIGHT_PAREN, "Expect ')' after expression.");
}

static void literal(bool canAssign) {
    switch (parser.previous.type) {
        case T_FALSE: emitByte(OP_FALSE); break;
        case T_NULL: emitByte(OP_NULL); break;
        case T_TRUE: emitByte(OP_TRUE); break;
        default: return;
    }
}

static void number(bool canAssign) {
    double value = strtod(parser.previous.start, NULL);
    emitBytes(OP_CONST, makeConst(NUM_VAL(value)));
}

static void unary(bool canAssign) {
    TokenType operatorType = parser.previous.type;
    parsePrecedence(PREC_UNARY);
    switch (operatorType) {
        case T_BANG: emitByte(OP_NOT); break;
        case T_MINUS: emitByte(OP_NEG); break;
        default: return;
    }
}

static void string(bool canAssign) {
    emitBytes(OP_CONST, makeConst(OBJ_VAL(strcopy(parser.previous.start + 1, parser.previous.length - 2))));
}

static void markInitialized() {
    current->locals[current->localCount - 1].depth = current->scopeDepth;
}

static void namedVar(Token token, bool canAssign) {
    if (current->scopeDepth > 0) {
        markInitialized();
        addLocal(token);
    }

    uint8_t getOp, setOp;
    int arg = resolveLocal(current, &token);
    if (arg != -1) {
        getOp = OP_GET_LOCAL;
        setOp = OP_SET_LOCAL;
    } else {
        arg = identConst(&token);
        getOp = OP_GET_GLOBAL;
        setOp = OP_SET_GLOBAL;
    }
    if (canAssign && match(T_WALRUS)) {
        expression();
        emitBytes(setOp, arg);
    }
    emitBytes(getOp, arg);
}

static void var(bool canAssign) {
    namedVar(parser.previous, canAssign);
}

bool compile(const char* source, Chunk* chunk) {
    initScanner(source);
    Compiler compiler;
    initCompiler(&compiler);
    compilingChunk = chunk;
    parser.errorState = false;
    parser.panicState = false;

    advance();
    while (!match(T_EOF)) {
        statement();
    }
    consume(T_EOF, "Expect end of expression.");

    endCompiler();
    return !parser.errorState;
}