package com.bifffly.canterbury.modules;

import com.bifffly.canterbury.interpreter.RuntimeError;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Function;

import static com.bifffly.canterbury.interpreter.Interpreter.stringify;

@Getter
public class StandardLibrary {
    private List<Module> modules;

    public StandardLibrary() {
        Module math = new Module("Math");
        math.defineNativeFunction("sqrt", 1, (arg) -> Math.sqrt((double) arg));
        math.defineNativeFunction("pow", 2, base -> {
            return (Function<Object, Double>) power -> {
                return Math.pow((double) base, (double) power);
            };
        });

        Module time = new Module("Time");
        time.defineNativeFunction("clock", 0, (arg) -> (double) System.currentTimeMillis() / 1000);

        Module io = new Module("IO");
        io.defineNativeFunction("print", 1, (arg) -> {
            System.out.println(stringify(arg));
            return null;
        });

        modules = List.of(math, time, io);
    }
}
