#ifndef CANTERBURY_VM_H
#define CANTERBURY_VM_H

#include "chunk.h"
#include "table.h"
#include "value.h"

#define STACK_MAX 256

typedef struct {
    Chunk* chunk;
    uint8_t* ip;
    Value stack[STACK_MAX];
    Value* top;
    Table globals;
    Table strings;
    Object* objects;
} VM;

typedef enum {
    INTERPRET_OK,
    INTERPRET_COMPILE_ERR,
    INTERPRET_RUNTIME_ERR
} InterpretResult;

extern VM vm;

void initVM();
void freeVM();

InterpretResult interpret(const char* src);

void push(Value value);
Value pop();
void dumpStack();

#endif