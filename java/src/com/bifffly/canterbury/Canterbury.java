package com.bifffly.canterbury;

import com.bifffly.canterbury.interpreter.Interpreter;
import com.bifffly.canterbury.interpreter.RuntimeError;
import com.bifffly.canterbury.parser.Parser;
import com.bifffly.canterbury.parser.stmt.Stmt;
import com.bifffly.canterbury.tokens.Token;
import com.bifffly.canterbury.tokens.TokenType;
import com.bifffly.canterbury.tokens.Tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Canterbury {
    private static final Interpreter interpreter = new Interpreter();
    private static boolean errorState = false;
    private static boolean runtimeErrorState = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: canterbury [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runREPL();
        }
    }

    public static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (errorState) {
            System.exit(65);
        }
        if (runtimeErrorState) {
            System.exit(70);
        }
    }

    public static void runREPL() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            run(line);
            errorState = false;
        }
    }

    private static void run(String source) {
        Tokenizer tokenizer = new Tokenizer(source);
        List<Token> tokens = tokenizer.tokenize();

        Parser parser = new Parser(tokens);
        List<Stmt> stmts = parser.parse();
        if (errorState) {
            return;
        }
        interpreter.interpret(stmts);
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    public static void error(Token token, String message) {
        if (token.getType() == TokenType.EOF) {
            report(token.getLine(), " at end", message);
        } else {
            report(token.getLine(), " at '" + token.getLexeme()+ "'", message);
        }
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
            "\n[line " + error.getToken().getLine() + "]");
        runtimeErrorState = true;
    }

    private static void report(int line, String loc, String msg) {
        System.err.println("[line " + line + "] ERROR " + loc + ": " + msg);
        errorState = true;
    }
}
