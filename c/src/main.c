#include "chunk.h"
#include "vm.h"

int main() {
    initVM();

    Chunk chunk;
    initChunk(&chunk);

    int constant = addConstant(&chunk, 1.2);
    writeChunk(&chunk, OP_CONST, 1);
    writeChunk(&chunk, constant, 1);

    constant = addConstant(&chunk, 3.4);
    writeChunk(&chunk, OP_CONST, 1);
    writeChunk(&chunk, constant, 1);

    writeChunk(&chunk, OP_ADD, 1);

    constant = addConstant(&chunk, 5.6);
    writeChunk(&chunk, OP_CONST, 1);
    writeChunk(&chunk, constant, 1);

    writeChunk(&chunk, OP_DIV, 1);
    writeChunk(&chunk, OP_NEG, 1);

    writeChunk(&chunk, OP_RET, 1);

    interpret(&chunk);

    freeVM();
    freeChunk(&chunk);

    return 0;
}
