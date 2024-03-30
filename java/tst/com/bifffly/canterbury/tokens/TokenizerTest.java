package com.bifffly.canterbury.tokens;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

public class TokenizerTest {
    @Test
    public void testSimpleTokens() {
        String src = "()[]{}+*/,!=_&|λ";
        Tokenizer tokenizer = new Tokenizer(src);

        List<Token> tokens = tokenizer.tokenize();
        List<Token> expected = List.of(
            new Token(TokenType.LEFT_PAREN, "(", null, 1),
            new Token(TokenType.RIGHT_PAREN, ")", null, 1),
            new Token(TokenType.LEFT_BRACKET, "[", null, 1),
            new Token(TokenType.RIGHT_BRACKET, "]", null, 1),
            new Token(TokenType.LEFT_BRACE, "{", null, 1),
            new Token(TokenType.RIGHT_BRACE, "}", null, 1),
            new Token(TokenType.PLUS, "+", null, 1),
            new Token(TokenType.STAR, "*", null, 1),
            new Token(TokenType.SLASH, "/", null, 1),
            new Token(TokenType.COMMA, ",", null, 1),
            new Token(TokenType.BANG, "!", null, 1),
            new Token(TokenType.EQUAL, "=", null, 1),
            new Token(TokenType.UNDERSCORE, "_", null, 1),
            new Token(TokenType.BIT_AND, "&", null, 1),
            new Token(TokenType.BIT_OR, "|", null, 1),
            new Token(TokenType.FUNC, "λ", null, 1),
            new Token(TokenType.EOF, "", null, 1)
        );
        assertEquals(expected, tokens);
    }

    @Test
    public void testMultiCharacterTokens() {
        String src = "->:=<=<>>=-:><";
        Tokenizer tokenizer = new Tokenizer(src);

        List<Token> tokens = tokenizer.tokenize();
        List<Token> expected = List.of(
            new Token(TokenType.ARROW, "->", null, 1),
            new Token(TokenType.WALRUS, ":=", null, 1),
            new Token(TokenType.LESSER_EQUAL, "<=", null, 1),
            new Token(TokenType.UNEQUAL, "<>", null, 1),
            new Token(TokenType.GREATER_EQUAL, ">=", null, 1),
            new Token(TokenType.MINUS, "-", null, 1),
            new Token(TokenType.COLON, ":", null, 1),
            new Token(TokenType.GREATER, ">", null, 1),
            new Token(TokenType.LESSER, "<", null, 1)
        );
    }

    @Test
    public void testWhitespace() {
        String src = " \r\t\n# asdf\r\t\n";
        Tokenizer tokenizer = new Tokenizer(src);

        List<Token> tokens = tokenizer.tokenize();
        assertEquals(List.of(new Token(TokenType.EOF, "", null, 2)), tokens);
    }

    @Test
    public void testString() {
        String src = "\"double-delimited string\" 'single-delimited string'";
        Tokenizer tokenizer = new Tokenizer(src);

        List<Token> tokens = tokenizer.tokenize();
        List<Token> expected = List.of(
            new Token(TokenType.STR, "\"double-delimited string\"", "double-delimited string", 1),
            new Token(TokenType.STR, "'single-delimited string'", "single-delimited string", 1),
            new Token(TokenType.EOF, "", null, 1)
        );
        assertEquals(expected, tokens);
    }

    @Test
    public void testUnterminatedString_throwsError() {
        String src = "\"trailing off...";
        Tokenizer tokenizer = new Tokenizer(src);

        List<Token> tokens = tokenizer.tokenize();
        assertEquals(List.of(new Token(TokenType.EOF, "", null, 1)), tokens);
    }

    @Test
    public void testNumber_success() {
        String src = "123 123.45 0x123 0b101";
        Tokenizer tokenizer = new Tokenizer(src);

        List<Token> tokens = tokenizer.tokenize();
        List<Token> expected = List.of(
            new Token(TokenType.NUM, "123", 123.0, 1),
            new Token(TokenType.NUM, "123.45", 123.45, 1),
            new Token(TokenType.NUM, "0x123", 291, 1),
            new Token(TokenType.NUM, "0b101", 5, 1),
            new Token(TokenType.EOF, "", null, 1)
        );
        assertEquals(expected, tokens);
    }

    @Test
    public void testNumber_unexpectedCharacter_doesNotParse() {
        String src = "123.";
        Tokenizer tokenizer = new Tokenizer(src);

        List<Token> tokens = tokenizer.tokenize();
        assertEquals(List.of(new Token(TokenType.EOF, "", null, 1)), tokens);
    }

    @Test
    public void testIdentifier() {
        String src = "asdf";
        Tokenizer tokenizer = new Tokenizer(src);

        List<Token> tokens = tokenizer.tokenize();
        List<Token> expected = List.of(
            new Token(TokenType.IDENTIFIER, "asdf", "asdf", 1),
            new Token(TokenType.EOF, "", null, 1)
        );
        assertEquals(expected, tokens);
    }

    @Test
    public void testUnexpectedCharacter() {
        String src = "á";
        Tokenizer tokenizer = new Tokenizer(src);

        List<Token> tokens = tokenizer.tokenize();
        assertEquals(List.of(new Token(TokenType.EOF, "", null, 1)), tokens);
    }
}
