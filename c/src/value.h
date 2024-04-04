#ifndef CANTERBURY_VALUE_H
#define CANTERBURY_VALUE_H

#include <stdlib.h>

#include "common.h"

typedef struct Object Object;
typedef struct ObjectString ObjectString;

typedef enum {
    VAL_BOOL,
    VAL_NULL,
    VAL_NUM,
    VAL_OBJ
} ValueType;

typedef struct {
    ValueType type;
    union {
        bool boolean;
        double num;
        Object* obj;
    } as;
} Value;

#define IS_BOOL(value) ((value).type == VAL_BOOL)
#define IS_NULL(value) ((value).type == VAL_NULL)
#define IS_NUM(value) ((value).type == VAL_NUM)
#define IS_OBJ(value) ((value).type == VAL_OBJ)

#define AS_BOOL(value) ((value).as.boolean)
#define AS_NUM(value) ((value).as.num)
#define AS_OBJ(value) ((value).as.obj)

#define BOOL_VAL(value) ((Value) {VAL_BOOL, {.boolean = value}})
#define NULL_VAL ((Value) {VAL_NULL, {.num = 0}})
#define NUM_VAL(value) ((Value) {VAL_NUM, {.num = value}})
#define OBJ_VAL(value) ((Value) {VAL_OBJ, {.obj = (Object*) value}})

typedef struct {
    int capacity;
    int count;
    Value* values;
} ValueArray;

void initValueArray(ValueArray* valueArray);
void writeValueArray(ValueArray* valueArray, Value value);
void freeValueArray(ValueArray* valueArray);

bool isEqual(Value a, Value b);
void printValue(Value value);

#endif
