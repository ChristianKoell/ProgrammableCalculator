package org.example;

import java.util.ArrayList;
import java.util.List;

public class RegisterSet {
    private List<Object> data;

    public RegisterSet() {
        clear();
    }

    public void clear() {
        data = new ArrayList<>();
        for (int i = 0; i < 52; i++) {
            data.add(""); // TODO: what predefined values ?
        }
    }

    public void set(char c, Object item) {
        if (item instanceof Integer || item instanceof Double || item instanceof String) {
            data.set(getIndex(c), item);
        } else {
            throw new IllegalArgumentException("unexpected symbol: " + item + " - data item must be an integer, a floating-point number, or a string");
        }
    }

    public Object get(char c) {
        return data.get(getIndex(c));
    }

    private int getIndex(char c) {
        if (c >= 'a' && c <= 'z') {
            return c - 'a';
        } else if (c >= 'A' && c <= 'Z') {
            return c - 'A' + 26;
        } else {
            throw new IllegalArgumentException("Character must be a-z or A-Z");
        }
    }

}
