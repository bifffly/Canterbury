#include <stdio.h>
#include <string.h>

#include "memory.h"
#include "object.h"
#include "table.h"
#include "value.h"
#include "vm.h"

#define ALLOCATE_OBJ(type, objectType) (type*) objalloc(sizeof(type), objectType)

static Object* objalloc(size_t size, ObjectType type) {
    Object* object = (Object*) reallocate(NULL, 0, size);
    object->type = type;
    object->next = vm.objects;
    vm.objects = object;
    return object;
}

ObjectFunction* funcalloc(int handle, int arity) {
    ObjectFunction* func = ALLOCATE_OBJ(ObjectFunction, OBJ_FUNC);
    func->handle = handle;
    func->arity = arity;
    return func;
}

static ObjectString* stralloc(char* chars, int length, uint32_t hash) {
    ObjectString* string = ALLOCATE_OBJ(ObjectString, OBJ_STR);
    string->length = length;
    string->chars = chars;
    string->hash = hash;
    tablePut(&vm.strings, string, NULL_VAL);
    return string;
}

static uint32_t strhash(const char* key, int length) {
    uint32_t hash = 2166136261u;
    for (int i = 0; i < length; i++) {
        hash ^= (uint8_t)key[i];
        hash *= 16777619;
    }
    return hash;
}

ObjectString* strtake(char* chars, int length) {
    uint32_t hash = strhash(chars, length);
    ObjectString* interned = tableFindString(&vm.strings, chars, length, hash);
    if (interned != NULL) {
        FREE_ARRAY(char, chars, length + 1);
        return interned;
    }
    return stralloc(chars, length, hash);
}

ObjectString* strcopy(const char* chars, int length) {
    uint32_t hash = strhash(chars, length);
    ObjectString* interned = tableFindString(&vm.strings, chars, length, hash);
    if (interned != NULL) {
        return interned;
    }
    char* heapChars = ALLOCATE(char, length + 1);
    memcpy(heapChars, chars, length);
    heapChars[length] = '\0';
    return stralloc(heapChars, length, hash);
}

static void printFunc(ObjectFunction* func) {
    printf("<fn %d %d>", func->handle, func->arity);
}

void printObject(Value value) {
    switch (OBJ_TYPE(value)) {
        case OBJ_FUNC: {
            printFunc(AS_FUNC(value));
            break;
        }
        case OBJ_STR: {
            printf("%s", AS_CSTR(value));
            break;
        }
    }
}