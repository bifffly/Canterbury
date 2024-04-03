#include <stdio.h>
#include <string.h>

#include "memory.h"
#include "object.h"
#include "value.h"
#include "vm.h"

#define ALLOCATE_OBJ(type, objectType) (type*) objalloc(sizeof(type), objectType)

static Object* objalloc(size_t size, ObjectType type) {
    Object* object = (Object*) reallocate(NULL, 0, size);
    object->type = type;
    return object;
}

static ObjectString* stralloc(char* chars, int length) {
    ObjectString* string = ALLOCATE_OBJ(ObjectString, OBJ_STR);
    string->length = length;
    string->chars = chars;
    return string;
}

ObjectString* strtake(char* chars, int length) {
    return stralloc(chars, length);
}

ObjectString* strcopy(const char* chars, int length) {
    char* heapChars = ALLOCATE(char, length + 1);
    memcpy(heapChars, chars, length);
    heapChars[length] = '\0';
    return stralloc(heapChars, length);
}

void printObject(Value value) {
    switch (OBJ_TYPE(value)) {
        case OBJ_STR: printf("%s", AS_CSTR(value)); break;
    }
}