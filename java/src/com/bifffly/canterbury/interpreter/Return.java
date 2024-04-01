package com.bifffly.canterbury.interpreter;

import lombok.Getter;

@Getter
public class Return extends RuntimeException {
    private Object value;

    public Return(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
