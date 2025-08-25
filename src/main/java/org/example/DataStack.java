package org.example;

import java.util.Stack;

public class DataStack {
    private Stack<Object> data = new Stack<>();

    public void add(Object item) {
        if (item instanceof Integer || item instanceof Double || item instanceof String) {
            data.add(item);
        } else {
            throw new IllegalArgumentException("data item must be an integer, a floating-point number, or a string");
        }
    }

}
