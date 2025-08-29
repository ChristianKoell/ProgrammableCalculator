package org.example;

import java.util.Stack;

public class DataStack {
    private Stack<Object> data = new Stack<>();

    public void push(Object item) {
        if (item instanceof Integer || item instanceof Double || item instanceof String) {
            //System.out.println("pushed " + item);
            data.push(item);
            System.out.println(data);
        } else {
            throw new IllegalArgumentException("unexpected symbol: " + item + " - data item must be an integer, a floating-point number, or a string");
        }
    }

    public Object pop() {
        //System.out.println("pop:   " + data.peek());
        Object obj = data.pop();
        System.out.println(data);
        return obj;
    }

    public Object peek() {
        //System.out.println("peek:  " + data.peek());
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

    public void clear() {
        data.clear();
    }

}
