package org.example;

import java.util.ArrayList;
import java.util.List;

public class RegisterSet {
    private List<Object> data; // TODO: Object ?

    public RegisterSet() {
        data = new ArrayList<>();
        for (int i = 0; i < 52; i++) {
            data.add(null); // TODO: what predefined values ?
        }
    }

    public Object get(char c) {
        if (c >= 'a' && c <= 'z') {
            return c - 'a';
        } else if (c >= 'A' && c <= 'Z') {
            return c - 'A' + 26;
        } else {
            throw new IllegalArgumentException("Character must be a-z or A-Z");
        }
    }

}
