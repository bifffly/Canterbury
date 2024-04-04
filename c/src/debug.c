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
    printf("%s\t%d\t", name, constant);
    printValue(chunk->consts.values[constant]);
    printf("\n");
    return offset + 2;
}

static int simpleInstruction(const char* name, int offset) {
    printf("%s\n", name);
    return offset + 1;
}

int disassembleInstruction(Chunk* chunk, int offset) {
    printf("%d: ", offset);
    printf("\t%d\t", chunk->lines[offset]);
    uint8_t instruction = chunk->code[offset];
    switch (instruction) {
        case OP_CONST: return constInstruction("con", chunk, offset);
        case OP_POP: return simpleInstruction("pop", offset);
        case OP_DEF_GLOBAL: return constInstruction("defg", chunk, offset);
        case OP_GET_GLOBAL: return constInstruction("getg", chunk, offset);
        case OP_NULL: return simpleInstruction("null", offset);
        case OP_TRUE: return simpleInstruction("true", offset);
        case OP_FALSE: return simpleInstruction("false", offset);
        case OP_EQ: return simpleInstruction("eq", offset);
        case OP_GREATER: return simpleInstruction("gcmp", offset);
        case OP_LESSER: return simpleInstruction("lcmp", offset);
        case OP_ADD: return simpleInstruction("dadd", offset);
        case OP_SUB: return simpleInstruction("dsub", offset);
        case OP_MUL: return simpleInstruction("dmul", offset);
        case OP_DIV: return simpleInstruction("ddiv", offset);
        case OP_NEG: return simpleInstruction("dneg", offset);
        case OP_NOT: return simpleInstruction("bnot", offset);
        case OP_RET: return simpleInstruction("ret", offset);
        default: {
            printf("Unknown opcode %d\n", instruction);
            return offset + 1;
        }
    }
}