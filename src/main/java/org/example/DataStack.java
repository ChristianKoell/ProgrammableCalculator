package org.example;

import java.util.Stack;

public class DataStack {
    private Stack<Object> data = new Stack<>();

    public void push(Object item) {
        if (item instanceof Integer || item instanceof Double || item instanceof String) {
            data.push(item);
        } else {
            throw new IllegalArgumentException("data item must be an integer, a floating-point number, or a string");
        }
    }

    public Object pop() {
        return data.pop();
    }

    public Object peek() {
        return data.peek();
    }

    public int size() {
        return data.size();
    }

    public Object get(int index) {
        return data.get(index);
    }

    public void remove(int index) {
        data.remove(index);
    }

}
