#ifndef CANTERBURY_OBJECT_H
#define CANTERBURY_OBJECT_H

#include "common.h"
#include "chunk.h"
#include "value.h"
#include "vm.h"

#define OBJ_TYPE(value) (AS_OBJ(value)->type)

#define IS_FUNC(value) isObjectType(value, OBJ_FUNC)
#define IS_STR(value) isObjectType(value, OBJ_STR)

#define AS_FUNC(value) ((ObjectFunction*) AS_OBJ(value))
#define AS_STR(value) ((ObjectString*) AS_OBJ(value))
#define AS_CSTR(value) (((ObjectString*) AS_OBJ(value))->chars)

typedef enum {
    OBJ_FUNC,
    OBJ_STR,
} ObjectType;

struct Object {
    ObjectType type;
    Object* next;
};

typedef struct {
    Object obj;
    int handle;
    int arity;
} ObjectFunction;

struct ObjectString {
    Object obj;
    int length;
    char* chars;
    uint32_t hash;
};

ObjectFunction* funcalloc(int handle, int arity);

ObjectString* strtake(char* chars, int length);
ObjectString* strcopy(const char* chars, int length);

void printObject(Value value);

static inline bool isObjectType(Value value, ObjectType type) {
    return IS_OBJ(value) && AS_OBJ(value)->type == type;
}

#endif
