#ifndef CANTERBURY_CHUNK_H
#define CANTERBURY_CHUNK_H

#include "common.h"
#include "value.h"

typedef enum {
    OP_CONST,
    OP_ADD, OP_SUB, OP_MUL, OP_DIV,
    OP_NEG,
    OP_RET
} OpCode;

typedef struct {
    int count;
    int capacity;
    uint8_t* code;
    int* lines;
    ValueArray consts;
} Chunk;

void initChunk(Chunk* chunk);
void writeChunk(Chunk* chunk, uint8_t byte, int line);
void freeChunk(Chunk* chunk);

int addConstant(Chunk* chunk, Value value);

#endif
