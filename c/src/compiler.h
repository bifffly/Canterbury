#ifndef CANTERBURY_COMPILER_H
#define CANTERBURY_COMPILER_H

#include "scanner.h"
#include "vm.h"

typedef struct {
    Token current;
    Token previous;
    bool errorState;
    bool panicState;
} Parser;

typedef enum {
    PREC_NONE,
    PREC_ASSIGNMENT,  // :=
    PREC_OR,          // or
    PREC_AND,         // and
    PREC_EQUALITY,    // = <>
    PREC_COMPARISON,  // < > <= >=
    PREC_TERM,        // + -
    PREC_FACTOR,      // * /
    PREC_UNARY,       // ! -
    PREC_CALL,        // . ()
    PREC_PRIMARY
} Precedence;

typedef void (*ParseFn)(bool canAssign);

typedef struct {
    ParseFn prefix;
    ParseFn infix;
    Precedence precedence;
} ParseRule;

typedef struct {
    Token name;
    int depth;
} Local;

typedef struct {
    Local locals[UINT8_COUNT];
    int localCount;
    int scopeDepth;
} Compiler;

bool compile(const char* src, Chunk* chunk);

#endif
