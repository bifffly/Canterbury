#include <stdio.h>

#include "chunk.h"
#include "debug.h"

int main() {
    Chunk chunk;

    initChunk(&chunk);
    writeChunk(&chunk, OP_RET, 1);

    int constant = addConstant(&chunk, 1.2);
    writeChunk(&chunk, OP_CONST, 1);
    writeChunk(&chunk, constant, 1);

    disassembleChunk(&chunk, "test chunk");

    freeChunk(&chunk);
    return 0;
}
