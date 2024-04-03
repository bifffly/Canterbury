#include <stdio.h>

#include "common.h"
#include "compiler.h"
#include "scanner.h"

void compile(const char* source) {
    initScanner(source);

    int line = -1;
    while (true) {
        Token token = scanToken();
        if (token.line != line) {
            line = token.line;
        }
        printf("%d\t%d\t'%.*s'\n", token.line, token.type, token.length, token.start);
        if (token.type == T_EOF) {
            break;
        }
    }
}