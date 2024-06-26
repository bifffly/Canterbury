package com.bifffly.canterbury.object;

import com.bifffly.canterbury.interpreter.Interpreter;

import java.util.List;

public interface Callable {
    public int arity();
    public Object call(Interpreter interpreter, List<Object> args);
}
