#include <stdio.h>

#include "memory.h"
#include "value.h"

void initValueArray(ValueArray* valueArray) {
    valueArray->values = NULL;
    valueArray->capacity = 0;
    valueArray->count = 0;
}

void writeValueArray(ValueArray* valueArray, Value value) {
    if (valueArray->capacity < valueArray->count + 1) {
        int oldCapacity = valueArray->capacity;
        valueArray->capacity = GROW_CAPACITY(oldCapacity);
        valueArray->values = GROW_ARRAY(Value, valueArray->values,oldCapacity, valueArray->capacity);
    }
    valueArray->values[valueArray->count] = value;
    valueArray->count++;
}

void freeValueArray(ValueArray* valueArray) {
    FREE_ARRAY(Value, valueArray->values, valueArray->capacity);
    initValueArray(valueArray);
}

bool isEqual(Value a, Value b) {
    if (a.type != b.type) {
        return false;
    }
    switch (a.type) {
        case VAL_BOOL: return AS_BOOL(a) == AS_BOOL(b);
        case VAL_NUM: return AS_NUM(a) == AS_NUM(b);
        case VAL_NULL:
        default: return false;
    }
}

void printValue(Value value) {
    switch (value.type) {
        case VAL_BOOL: printf(AS_BOOL(value) ? "true" : "false"); break;
        case VAL_NULL: printf("null"); break;
        case VAL_NUM: printf("%g", AS_NUM(value)); break;
    }
}
