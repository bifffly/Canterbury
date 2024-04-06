#ifndef CANTERBURY_VM_H
#define CANTERBURY_VM_H

#include "chunk.h"
#include "table.h"
#include "value.h"
#include "object.h"

#define FRAMES_MAX 64
#define STACK_MAX (FRAMES_MAX * UINT8_COUNT)

typedef struct {
    Chunk* chunk;
    uint8_t* ip;
    Value* slots;
} CallFrame;

typedef struct {
    CallFrame frames[FRAMES_MAX];
    int frameCount;
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