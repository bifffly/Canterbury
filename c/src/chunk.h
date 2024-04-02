#ifndef CANTERBURY_CHUNK_H
#define CANTERBURY_CHUNK_H

#include "common.h"

typedef enum {
    OP_RET
} OpCode;

typedef struct {
    int count;
    int capacity;
    uint8_t* code;
} Chunk;

void initChunk(Chunk* chunk);
void writeChunk(Chunk* chunk, uint8_t byte);
void freeChunk(Chunk* chunk);

#endif
