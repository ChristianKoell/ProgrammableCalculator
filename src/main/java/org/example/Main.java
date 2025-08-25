package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Queue;

public class Main {

    private static Queue<Character> commandStream = new LinkedList<>();
    private static int operationMode = 0;
    private static DataStack dataStack = new DataStack();
    private static RegisterSet registerSet = new RegisterSet();
    private static BufferedReader inputStream; // TODO: not yet sure how to implement

    public static void main(String[] args) {
        inputStream = new BufferedReader(new InputStreamReader(System.in));

        for (Character c : ((String) registerSet.get('a')).toCharArray()) {
            commandStream.add(c);
        }
    }

}