#include <stdio.h>

#include "chunk.h"
#include "debug.h"

int main() {
    Chunk chunk;

    initChunk(&chunk);
    writeChunk(&chunk, OP_RET);
    disassembleChunk(&chunk, "test chunk");

    freeChunk(&chunk);
    return 0;
}
