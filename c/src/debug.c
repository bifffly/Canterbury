#include <stdio.h>

#include "debug.h"
#include "value.h"

void disassembleChunk(Chunk* chunk, const char* name) {
    printf("== %s ==\n", name);
    for (int offset = 0; offset < chunk->count;) {
        offset = disassembleInstruction(chunk, offset);
    }
}

static int constInstruction(const char* name, Chunk* chunk, int offset) {
    uint8_t constant = chunk->code[offset + 1];
    printf("%s %d ", name, constant);
    printValue(chunk->consts.values[constant]);
    printf("\n");
    return offset + 2;
}

static int simpleInstruction(const char* name, int offset) {
    printf("%s\n", name);
    return offset + 1;
}

static int byteInstruction(const char* name, Chunk* chunk, int offset) {
    uint8_t slot = chunk->code[offset + 1];
    printf("%s %d\n", name, slot);
    return offset + 2;
}

static int jumpInstruction(const char* name, int sign, Chunk* chunk, int offset) {
    uint16_t jump = (uint16_t)(chunk->code[offset + 1] << 8);
    jump |= chunk->code[offset + 2];
    printf("%s %d -> %d\n", name, offset, offset + 3 + sign * jump);
    return offset + 3;
}

int disassembleInstruction(Chunk* chunk, int offset) {
    printf("%d: ", offset);
    uint8_t instruction = chunk->code[offset];
    switch (instruction) {
        case OP_CONST: return constInstruction("con", chunk, offset);
        case OP_POP: return simpleInstruction("pop", offset);
        case OP_SET_GLOBAL: return constInstruction("defg", chunk, offset);
        case OP_GET_GLOBAL: return constInstruction("getg", chunk, offset);
        case OP_SET_LOCAL: return byteInstruction("defl", chunk, offset);
        case OP_GET_LOCAL: return byteInstruction("getl", chunk, offset);
        case OP_JUMP_FALSE: return jumpInstruction("jmpf", 1, chunk, offset);
        case OP_LOOP: return jumpInstruction("jmploop", -1, chunk, offset);
        case OP_JUMP: return jumpInstruction("jmp", 1, chunk, offset);
        case OP_NULL: return simpleInstruction("null", offset);
        case OP_TRUE: return simpleInstruction("true", offset);
        case OP_FALSE: return simpleInstruction("false", offset);
        case OP_EQ: return simpleInstruction("eq", offset);
        case OP_GREATER: return simpleInstruction("cmpg", offset);
        case OP_LESSER: return simpleInstruction("cmpl", offset);
        case OP_ADD: return simpleInstruction("add", offset);
        case OP_SUB: return simpleInstruction("sub", offset);
        case OP_MUL: return simpleInstruction("mul", offset);
        case OP_DIV: return simpleInstruction("div", offset);
        case OP_NEG: return simpleInstruction("neg", offset);
        case OP_NOT: return simpleInstruction("not", offset);
        case OP_RET: return simpleInstruction("ret", offset);
        default: {
            printf("Unknown opcode %d\n", instruction);
            return offset + 1;
        }
    }
}