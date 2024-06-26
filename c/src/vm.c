#include <stdarg.h>
#include <stdio.h>
#include <string.h>

#include "common.h"
#include "compiler.h"
#include "debug.h"
#include "memory.h"
#include "object.h"
#include "vm.h"

VM vm;

static void resetStack() {
    vm.top = vm.stack;
    vm.frameCount = 0;
}

static void runtimeError(const char* format, ...) {
    va_list args;
    va_start(args, format);
    vfprintf(stderr, format, args);
    va_end(args);
    fputs("\n", stderr);

    CallFrame* frame = &vm.frames[vm.frameCount - 1];
    size_t instruction = frame->ip - frame->chunk->code - 1;
    int line = frame->chunk->lines[instruction];
    fprintf(stderr, "[line %d] in script\n", line);
#ifdef DEBUG_TRACE_EXEC
    dumpStack();
#endif
    resetStack();
}

void initVM() {
    resetStack();
    vm.objects = NULL;
    initTable(&vm.globals);
    initTable(&vm.strings);
}

void freeVM() {
    freeTable(&vm.globals);
    freeTable(&vm.strings);
    freeObjects();
}

static Value peek(int distance) {
    return vm.top[-1 - distance];
}

static bool isTruthy(Value value) {
    return !IS_NULL(value) && (IS_BOOL(value) && AS_BOOL(value));
}

static void strconcat() {
    ObjectString* bstr = AS_STR(pop());
    ObjectString* astr = AS_STR(pop());

    int length = astr->length + bstr->length;
    char* chars = ALLOCATE(char, length + 1);
    memcpy(chars, astr->chars, astr->length);
    memcpy(chars + astr->length, bstr->chars, bstr->length);
    chars[length] = '\0';

    ObjectString* result = strtake(chars, length);
    push(OBJ_VAL(result));
}

static InterpretResult run() {
    CallFrame* frame = &vm.frames[vm.frameCount - 1];

#define READ_BYTE() (*frame->ip++)
#define READ_CONST() (frame->chunk->consts.values[READ_BYTE()])
#define READ_SHORT() (frame->ip += 2, (uint16_t) ((frame->ip[-2] << 8) | frame->ip[-1]))
#define READ_STR() AS_STR(READ_CONST())

#define BINARY_OP(valueType, op) \
    do { \
      if (!IS_NUM(peek(0)) || !IS_NUM(peek(1))) { \
        runtimeError("Operands must be numbers."); \
        return INTERPRET_RUNTIME_ERR; \
      } \
      double b = AS_NUM(pop()); \
      double a = AS_NUM(pop()); \
      push(valueType(a op b)); \
    } while (false)

    while (true) {
#ifdef DEBUG_TRACE_EXEC
        disassembleInstruction(frame->chunk, (int)(frame->ip - frame->chunk->code));
#endif

        uint8_t instruction;
        switch (instruction = READ_BYTE()) {
            case OP_CONST: {
                Value constant = READ_CONST();
                push(constant);
                break;
            }
            case OP_POP: pop(); break;
            case OP_SET_GLOBAL: {
                ObjectString* name = READ_STR();
                tablePut(&vm.globals, name, peek(0));
                pop();
                break;
            }
            case OP_GET_GLOBAL: {
                ObjectString* name = READ_STR();
                Value value;
                if (!tableGet(&vm.globals, name, &value)) {
                    runtimeError("Undefined variable %s.", name->chars);
                    return INTERPRET_RUNTIME_ERR;
                }
                push(value);
                break;
            }
            case OP_SET_LOCAL: {
                uint8_t slot = READ_BYTE();
                frame->slots[slot] = peek(0);
                break;
            }
            case OP_GET_LOCAL: {
                uint8_t slot = READ_BYTE();
                push(frame->slots[slot]);
                break;
            }
            case OP_JUMP_FALSE: {
                uint16_t offset = READ_SHORT();
                if (!isTruthy(peek(0))) frame->ip += offset;
                break;
            }
            case OP_JUMP: {
                uint16_t offset = READ_SHORT();
                frame->ip += offset;
                break;
            }
            case OP_LOOP: {
                uint16_t offset = READ_SHORT();
                frame->ip -= offset;
                break;
            }
            case OP_NULL: push(NULL_VAL); break;
            case OP_TRUE: push(BOOL_VAL(true)); break;
            case OP_FALSE: push(BOOL_VAL(false)); break;
            case OP_EQ: {
                Value b = pop();
                Value a = pop();
                push(BOOL_VAL(isEqual(a, b)));
                break;
            }
            case OP_GREATER: BINARY_OP(BOOL_VAL, >); break;
            case OP_LESSER: BINARY_OP(BOOL_VAL, <); break;
            case OP_ADD: {
                if (IS_STR(peek(0)) && IS_STR(peek(1))) {
                    strconcat();
                } else if (IS_NUM(peek(0)) && IS_NUM(peek(1))) {
                    double b = AS_NUM(pop());
                    double a = AS_NUM(pop());
                    push(NUM_VAL(a + b));
                } else {
                    runtimeError("Operands must be nums or strs.");
                    return INTERPRET_RUNTIME_ERR;
                }
                break;
            }
            case OP_SUB: BINARY_OP(NUM_VAL, -); break;
            case OP_MUL: BINARY_OP(NUM_VAL, *); break;
            case OP_DIV: BINARY_OP(NUM_VAL, /); break;
            case OP_NOT: push(BOOL_VAL(!isTruthy(pop()))); break;
            case OP_NEG: {
                if (!IS_NUM(peek(0))) {
                    runtimeError("Operand must be a number.");
                    return INTERPRET_RUNTIME_ERR;
                }
                push(NUM_VAL(-AS_NUM(pop())));
                break;
            }
            case OP_RET: {
                return INTERPRET_OK;
            }
        }
    }

#undef READ_BYTE
#undef READ_CONST
#undef READ_SHORT
#undef READ_STR
#undef BINARY_OP
}

InterpretResult interpret(const char* src) {
    Chunk chunk;
    initChunk(&chunk);

    if (!compile(src, &chunk)) {
        freeChunk(&chunk);
        return INTERPRET_COMPILE_ERR;
    }

    CallFrame* frame = &vm.frames[vm.frameCount++];
    frame->chunk = &chunk;
    frame->ip = chunk.code;
    frame->slots = vm.stack;

    InterpretResult result = run();

    freeChunk(&chunk);
    return result;
}

void push(Value value) {
    *vm.top = value;
    vm.top++;
}

Value pop() {
    vm.top--;
    return *vm.top;
}

void dumpStack() {
    while (vm.top != vm.stack) {
        printValue(pop());
        printf("\n");
    }
    printValue(pop());
    printf("\n");
}
