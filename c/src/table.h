#ifndef CANTERBURY_TABLE_H
#define CANTERBURY_TABLE_H

#include "common.h"
#include "value.h"

typedef struct {
    ObjectString* key;
    Value value;
} Entry;

typedef struct {
    int count;
    int capacity;
    Entry* entries;
} Table;

void initTable(Table* table);
void freeTable(Table* table);
bool tableGet(Table* table, ObjectString* key, Value* value);
bool tablePut(Table* table, ObjectString* key, Value value);
bool tableDel(Table* table, ObjectString* key);
void tableCopy(Table* src, Table* dest);
ObjectString* tableFindString(Table* table, const char* chars, int length, uint32_t hash);

#endif
