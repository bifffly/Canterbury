#include <stdbool.h>
#include <string.h>

#include "scanner.h"

Scanner scanner;

void initScanner(const char* source) {
    scanner.start = source;
    scanner.current = source;
    scanner.line = 1;
}

static bool isAlpha(char c) {
    return (c >= 'a' && c <= 'z')
        || (c >= 'A' && c <= 'Z')
        || c == '_';
}

static bool isDigit(char c) {
    return c >= '0' && c <= '9';
}

static bool hasNext() {
    return *scanner.current != '\0';
}

static char advance() {
    scanner.current++;
    return scanner.current[-1];
}

static char peek() {
    return *scanner.current;
}

static char peekNext() {
    if (!hasNext()) {
        return '\0';
    }
    return scanner.current[1];
}

static bool match(char expected) {
    if (!hasNext() || *scanner.current != expected) {
        return false;
    }
    scanner.current++;
    return true;
}

static Token makeToken(TokenType type) {
    Token token;
    token.type = type;
    token.start = scanner.start;
    token.length = (int) (scanner.current - scanner.start);
    token.line = scanner.line;
    return token;
}

static Token errToken(const char* msg) {
    Token token;
    token.type = T_ERR;
    token.start = msg;
    token.length = (int) strlen(msg);
    token.line = scanner.line;
    return token;
}

static void skipWhitespace() {
    while (true) {
        char c = peek();
        switch (c) {
            case ' ':
            case '\r':
            case '\t': {
                advance();
                break;
            }
            case '\n': {
                scanner.line++;
                advance();
                break;
            }
            case '#': while (peek() != '\n' && hasNext()) {
                advance();
            } break;
            default: return;
        }
    }
}

static TokenType checkKeyword(int start, int length, const char* rest, TokenType type) {
    if (scanner.current - scanner.start == start + length
        && memcmp(scanner.start + start, rest, length) == 0) {
        return type;
    }
    return T_IDENT;
}

static TokenType identifierType() {
    switch (scanner.start[0]) {
        case 'a': if (scanner.current - scanner.start > 1) {
            switch (scanner.start[0]) {
                case 'n': return checkKeyword(2, 1, "d", T_AND);
                case 'g': return checkKeyword(2, 5, "ainst", T_AGAINST);
            }
        } break;
        case 'e': if (scanner.current - scanner.start > 2) {
            case 'l': switch (scanner.start[2]) {
                case 'i': return checkKeyword(3, 1, "f", T_ELIF);
                case 's': return checkKeyword(3, 1, "e", T_ELSE);
            }
        } break;
        case 'f': if (scanner.current - scanner.start > 1) {
            switch (scanner.start[1]) {
                case 'a': return checkKeyword(2, 3, "lse", T_FALSE);
                case 'o': return checkKeyword(2, 1, "r", T_FOR);
                case 'u': return checkKeyword(2, 2, "nc", T_FUNC);
            }
        } break;
        case 'i': if (scanner.current - scanner.start > 1) {
            switch (scanner.start[1]) {
                case 'f': return checkKeyword(2, 0, "", T_IF);
                case 'm': return checkKeyword(2, 4, "port", T_IMPORT);
                case 's': return checkKeyword(2, 0, "", T_IS);
            }
        } break;
        case 'm': return checkKeyword(1, 4, "atch", T_MATCH);
        case 'n': return checkKeyword(1, 3, "ull", T_NULL);
        case 'o': return checkKeyword(1, 1, "r", T_OR);
        case 'p': return checkKeyword(1, 4, "rint", T_PRINT);
        case 'r': return checkKeyword(1, 5, "eturn", T_RETURN);
        case 's': if (scanner.current - scanner.start > 1) {
            switch (scanner.start[1]) {
                case 'e': return checkKeyword(2, 2, "lf", T_SELF);
                case 't': return checkKeyword(2, 4, "ruct", T_STRUCT);
            }
        } break;
        case 't': return checkKeyword(1, 3, "rue", T_TRUE);
        case 'w': return checkKeyword(1, 4, "hile", T_WHILE);
    }
    return T_IDENT;
}

static Token string(char delim) {
    while (hasNext() && peek() != delim) {
        if (peek() == '\n') {
            scanner.line++;
        }
        advance();
    }

    if (!hasNext()) {
        return errToken("Unterminated string.");
    }

    advance();
    return makeToken(T_STR);
}

static Token number() {
    while (isDigit(peek())) {
        advance();
    }

    if (peek() == '.' && isDigit(peekNext())) {
        advance();
        while (isDigit(peek())) {
            advance();
        }
    }

    return makeToken(T_NUM);
}

static Token identifier() {
    while (isAlpha(peek()) || isDigit(peek())) advance();
    return makeToken(identifierType());
}

Token scanToken() {
    skipWhitespace();
    scanner.start = scanner.current;
    if (!hasNext()) {
        return makeToken(T_EOF);
    }

    char c = advance();
    if (isAlpha(c)) {
        return identifier();
    }
    if (isDigit(c)) {
        return number();
    }

    switch (c) {
        case '(': return makeToken(T_LEFT_PAREN);
        case ')': return makeToken(T_RIGHT_PAREN);
        case '[': return makeToken(T_lEFT_BRACKET);
        case ']': return makeToken(T_RIGHT_BRACKET);
        case '{': return makeToken(T_LEFT_BRACE);
        case '}': return makeToken(T_RIGHT_BRACE);
        case '+': return makeToken(T_PLUS);
        case '*': return makeToken(T_STAR);
        case '/': return makeToken(T_SLASH);
        case ',': return makeToken(T_COMMA);
        case '!': return makeToken(T_BANG);
        case '=': return makeToken(T_EQUAL);
        case '&': return makeToken(T_BIT_AND);
        case '|': return makeToken(T_BIT_OR);
        case '~': return makeToken(T_BIT_NEG);
        case '_': return makeToken(T_UNDERSCORE);
        case ';': return makeToken(T_SEMICOLON);
        case '-': return makeToken(match('>') ? T_ARROW : T_MINUS);
        case ':': return makeToken(match('=') ? T_WALRUS : T_COLON);
        case '>': return makeToken(match('=') ? T_GEQ: T_GREATER);
        case '<': return makeToken(match('=') ? T_LEQ: match('>') ? T_UNQEUAL : T_LESSER);
        case '\'': return string('\'');
        case '"': return string('"');
    }

    return errToken("Unexpected character.");
}