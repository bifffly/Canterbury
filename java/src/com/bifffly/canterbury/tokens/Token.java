package com.bifffly.canterbury.tokens;

public class Token {
    private final TokenType type;
    private final String lexeme;
    private final Object value;
    private final int line;

    public Token(TokenType type, String lexeme, Object value, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.value = value;
        this.line = line;
    }

    @Override
    public String toString() {
        return "[" + type + " " + lexeme + " " + value + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Token)) {
            return false;
        }
        Token token = (Token) o;
        return type == token.type
            && lexeme.equals(token.lexeme)
            && value == null ? token.value == null : value.equals(token.value)
            && line == token.line;
    }
}
