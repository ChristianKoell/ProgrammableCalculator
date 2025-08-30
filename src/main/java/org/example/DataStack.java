package org.example;

import java.util.Stack;

public class DataStack {
    private Stack<Object> data = new Stack<>();

    public void push(Object item) {
        if (item instanceof Integer || item instanceof Double || item instanceof String) {
            data.push(item);
            printStack();
        } else {
            throw new IllegalArgumentException("unexpected symbol: " + item + " - data item must be an integer, a floating-point number, or a string");
        }
    }

    public Object pop() {
        Object obj = data.pop();
        printStack();
        return obj;
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
        printStack();
    }

    public void clear() {
        data.clear();
    }

    private void printStack() {
        System.out.print("[");
        for (int i = 0; i < data.size(); i++) {
            Object o = data.get(i);
            if (o instanceof String) {
                System.out.print("(" + o + ")");
            } else {
                System.out.print(o);
            }

            if (i < data.size() - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("]");
    }

}
