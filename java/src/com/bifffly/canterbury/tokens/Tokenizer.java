package com.bifffly.canterbury.tokens;

import com.bifffly.canterbury.Canterbury;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bifffly.canterbury.tokens.TokenType.*;

public class Tokenizer  {
    private final static Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        KEYWORDS.put("and", AND);
        KEYWORDS.put("or", OR);
        KEYWORDS.put("func", FUNC);
        KEYWORDS.put("lambda", FUNC);
        KEYWORDS.put("struct", STRUCT);
        KEYWORDS.put("for", FOR);
        KEYWORDS.put("while", WHILE);
        KEYWORDS.put("if", IF);
        KEYWORDS.put("elif", ELIF);
        KEYWORDS.put("else", ELSE);
        KEYWORDS.put("match", MATCH);
        KEYWORDS.put("against", AGAINST);
        KEYWORDS.put("is", IS);
        KEYWORDS.put("self", SELF);
        KEYWORDS.put("true", TRUE);
        KEYWORDS.put("false", FALSE);
        KEYWORDS.put("null", NULL);
    }

    private final String src;
    private List<Token> tokens;
    private int start = 0;
    private int curr = 0;
    private int line = 1;

    public Tokenizer(String src) {
        this.src = src;
        this.tokens = new ArrayList<>();
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object value) {
        String text = src.substring(start, curr);
        tokens.add(new Token(type, text, value, line));
    }

    private char advance() {
        return src.charAt(curr++);
    }

    private char peek() {
        return hasNext() ? src.charAt(curr) : '\0';
    }

    private char peekNext() {
        return (curr + 1 < src.length()) ? src.charAt(curr + 1) : '\0';
    }

    private boolean match(char expected) {
        if (!hasNext() || src.charAt(curr) != expected) {
            return false;
        }
        curr++;
        return true;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isBinary(char c) {
        return c == '0' || c == '1';
    }

    private boolean isHex(char c) {
        return (c >= '0' && c <= '9')
            || (c >= 'A' && c <= 'F')
            || (c >= 'a' && c <= 'f');
    }

    private boolean isAlpha(char c) {
        return (c >= 'A' && c <= 'Z')
            || (c >= 'a' && c <= 'z')
            || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void string(char delim) {
        while (hasNext() && peek() != delim) {
            if (peek() == '\n') line++;
            advance();
        }

        if (!hasNext()) {
            Canterbury.error(line, "Unterminated string.");
            return;
        }

        advance();

        String value = src.substring(start + 1, curr - 1);
        addToken(STR, value);
    }

    private void binary() {
        int numStart = ++curr;
        while (isBinary(peek())) {
            advance();
        }
        addToken(NUM, Integer.parseInt(src.substring(numStart, curr), 2));
    }

    private void hex() {
        int numStart = ++curr;
        while (isHex(peek())) {
            advance();
        }
        addToken(NUM, Integer.parseInt(src.substring(numStart, curr), 16));
    }

    private void number() {
        if (src.charAt(curr - 1) == '0') {
            switch (peek()) {
                case 'b': binary(); return;
                case 'x': hex(); return;
                default: break;
            }
        }

        while (isDigit(peek())) {
            advance();
        }

        if (peek() == '.') {
            if (isDigit(peekNext())) {
                advance();
                while (isDigit(peek())) {
                    advance();
                }
            } else {
                Canterbury.error(line, "Unexpected character.");
                return;
            }
        }

        addToken(NUM, Double.parseDouble(src.substring(start, curr)));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }

        String text = src.substring(start, curr);
        TokenType type = KEYWORDS.getOrDefault(text, IDENTIFIER);
        addToken(type, text);
    }

    public boolean hasNext() {
        return curr < src.length();
    }

    public void next() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '[': addToken(LEFT_BRACKET); break;
            case ']': addToken(RIGHT_BRACKET); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case '+': addToken(PLUS); break;
            case '*': addToken(STAR); break;
            case '/': addToken(SLASH); break;
            case ',': addToken(COMMA); break;
            case '!': addToken(BANG); break;
            case '=': addToken(EQUAL); break;
            case '_': addToken(UNDERSCORE); break;
            case '&': addToken(BIT_AND); break;
            case '|': addToken(BIT_OR); break;
            case 'λ': addToken(FUNC); break;
            case '-': addToken(match('>') ? ARROW : MINUS); break;
            case ':': addToken(match('=') ? WALRUS : COLON); break;
            case '<': addToken(match('=') ? LESSER_EQUAL : match('>') ? UNEQUAL : LESSER); break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case '#': {
                while (peek() != '\n' && hasNext()) {
                    advance();
                }
                break;
            }
            case ' ':
            case '\r':
            case '\t': break;
            case '\n': line++; break;
            case '"':
            case '\'': string(c); break;
            default: {
                if (isDigit(c)) {
                    number(); break;
                } else if (isAlpha(c)) {
                    identifier(); break;
                } else {
                    Canterbury.error(line, "Unexpected character.");
                }
                break;
            }
        }
    }

    public List<Token> tokenize() {
        while (hasNext()) {
            // We are at the beginning of the next lexeme.
            start = curr;
            next();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }
}
