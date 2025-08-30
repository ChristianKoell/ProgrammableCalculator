package org.example;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Scanner;

public class Main {

    public static final double EPSILON = 0.001;

    private static int operationMode = 0;
    private static Deque<Character> commandStream = new LinkedList<>();
    private static DataStack dataStack = new DataStack();
    private static RegisterSet registerSet = new RegisterSet();
    private static Scanner inputStream = new Scanner(System.in);

    public static void main(String[] args) {
        /*for (Character c : ((String) registerSet.get('a')).toCharArray()) {
            commandStream.add(c);
        }*/

        while (true) {
            reset();

            System.out.print("put in text: ");
            String line;
            line = inputStream.nextLine();

            for (int index = 0; index < line.length(); index++) {
                commandStream.addLast(line.charAt(index));
            }

            while (!commandStream.isEmpty()) {
                Character c = commandStream.pop();
                if (operationMode == -1) {
                    if (!(dataStack.peek() instanceof Integer)) {
                        throw new IllegalStateException("operationMode = -1 -> top element of data stack must be an integer");
                    }

                    if (Character.isDigit(c)) {
                        dataStack.push((((int) dataStack.pop()) * 10) + c - '0');
                    } else if (c.equals('.')) {
                        dataStack.push(((Integer) dataStack.pop()).doubleValue());
                        operationMode = -2;
                    } else {
                        operationMode = 0;
                        commandStream.addFirst(c);
                    }
                } else if (operationMode < -1) {
                    if (!(dataStack.peek() instanceof Double)) {
                        throw new IllegalStateException("operationMode < -1 -> top element of data stack must be a floating-point number");
                    }

                    if (Character.isDigit(c)) {
                        double d = (c - '0') * Math.pow(10, operationMode + 1);
                        dataStack.push(((double) dataStack.pop()) + d);
                        operationMode--;
                    } else if (c.equals('.')) {
                        dataStack.push(0.0);
                        operationMode = -2;
                    } else {
                        operationMode = 0;
                        commandStream.addFirst(c);
                    }
                } else if (operationMode > 0) {
                    if (!(dataStack.peek() instanceof String)) {
                        throw new IllegalStateException("operationMode > 0 -> top element of data stack must be a string");
                    }

                    if (c.equals('(')) {
                        dataStack.push(((String) dataStack.pop()) + '(');
                        operationMode++;
                    } else if (c.equals(')')) {
                        operationMode--;
                        if (operationMode > 0) {
                            dataStack.push(((String) dataStack.pop()) + ')');
                        }
                    } else {
                        // add command character to the string on top of the data stack
                        dataStack.push(((String) dataStack.pop()) + c);
                    }
                } else {
                    // execution mode
                    if (Character.isDigit(c)) {
                        dataStack.push(c - '0');
                        operationMode = -1;
                    } else if (c.equals('.')) {
                        dataStack.push(0.0);
                        operationMode = -2;
                    } else if (c.equals('(')) {
                        dataStack.push("");
                        operationMode = 1;
                    } else if (Character.isLetter(c)) {
                        // load value from register c
                        dataStack.push(registerSet.get(c));
                    } else if (Arrays.asList('=', '<', '>').contains(c)) {
                        Object snd = dataStack.pop();
                        Object fst = dataStack.pop();

                        int val = getCompareValue(c, fst, snd);
                        dataStack.push(val);
                    } else if (Arrays.asList('+', '-', '*', '/', '%').contains(c)) {
                        Object snd = dataStack.pop();
                        Object fst = dataStack.pop();

                        boolean bothInt = fst instanceof Integer && snd instanceof Integer;

                        if (fst instanceof Number n1 && snd instanceof Number n2) {
                            double d1 = n1.doubleValue();
                            double d2 = n2.doubleValue();

                            if (c.equals('%') && !bothInt) {
                                dataStack.push("()");
                            } else if (c.equals('/') && Math.abs(d2) < EPSILON) {
                                dataStack.push("()");
                            } else {
                                double val = switch (c) {
                                    case '+' -> d1 + d2;
                                    case '-' -> d1 - d2;
                                    case '*' -> d1 * d2;
                                    case '/' -> d1 / d2;
                                    case '%' -> d1 % d2; // TODO: check for double
                                    default -> throw new IllegalStateException("Unexpected value: " + c);
                                };

                                if (bothInt) {
                                    dataStack.push(((int) val));
                                } else {
                                    dataStack.push(val);
                                }
                            }

                        } else if (fst instanceof String s1 && snd instanceof String s2 && c.equals('/')) {
                            // position of second string in first string
                            dataStack.push(s1.indexOf(s2));
                        } else if (c.equals('+')) { // && (fst instanceof String || snd instanceof String)
                            // string concatenation
                            dataStack.push(fst.toString() + snd);
                        } else if (fst instanceof Integer || snd instanceof Integer) {
                            boolean fstInt = fst instanceof Integer;
                            int i;
                            String str;
                            if (fstInt) {
                                i = ((int) fst);
                                str = ((String) snd);
                            } else {
                                str = ((String) fst);
                                i = ((int) snd);
                            }

                            if (c.equals('*') && i >= 0 && i <= 128) {
                                char ascii = (char) i;
                                // add ascii symbol corresponding to ascii code i
                                if (fstInt) {
                                    dataStack.push(ascii + str);
                                } else {
                                    dataStack.push(str + ascii);
                                }
                            } else if (c.equals('-') && i >= 0 && i <= str.length()) {
                                if (fstInt) {
                                    // remove first i characters
                                    dataStack.push(str.substring(i));
                                } else {
                                    // remove last i characters
                                    dataStack.push(str.substring(0, str.length() - i));
                                }
                            } else if (c.equals('%') && i >= 0 && i < str.length()) {
                                // ascii code of character at index i
                                char ch = str.charAt(i);
                                dataStack.push((int) ch);
                            } else {
                                dataStack.push("()");
                            }
                        } else {
                            dataStack.push("()");
                        }
                    } else if (Arrays.asList('&', '|').contains(c)) {
                        Object snd = dataStack.pop();
                        Object fst = dataStack.pop();

                        if (fst instanceof Integer && snd instanceof Integer) {
                            if (c.equals('&')) {
                                // logical and
                                dataStack.push((((int) fst) != 0 && ((int) snd) != 0) ? 1 : 0);
                            } else {
                                // logical or
                                dataStack.push((((int) fst) != 0 || ((int) snd) != 0) ? 1 : 0);
                            }
                        } else {
                            dataStack.push("()");
                        }
                    } else if (c.equals('_')) {
                        // null-check (negate boolean)
                        Object x = dataStack.pop();

                        if (x instanceof String s) {
                            dataStack.push(s.isEmpty() ? 1 : 0);
                        } else if (x instanceof Integer i) {
                            dataStack.push(i == 0 ? 1 : 0);
                        } else {
                            dataStack.push(Math.abs((double) x) < EPSILON ? 1 : 0);
                        }
                    } else if (c.equals('~')) {
                        // negate
                        Object x = dataStack.pop();

                        if (x instanceof Integer i) {
                            dataStack.push(-i);
                        } else if (x instanceof Double d) {
                            dataStack.push(-d);
                        } else {
                            dataStack.push("()");
                        }
                    } else if (c.equals('?')) {
                        // integer conversion
                        Object x = dataStack.pop();

                        if (x instanceof Double d) {
                            dataStack.push(d.intValue());
                        } else {
                            dataStack.push("()");
                        }
                    } else if (c.equals('!')) {
                        // copy
                        Object x = dataStack.peek();

                        if (x instanceof Integer i && i >= 1 && i <= dataStack.size()) {
                            Object item = dataStack.get(dataStack.size() - 1 - (i - 1));
                            dataStack.pop(); // TODO: remove after? -> otherwise adapt range
                            dataStack.push(item);
                        }
                    } else if (c.equals('$')) {
                        // delete
                        Object x = dataStack.pop();

                        if (x instanceof Integer i && i >= 1 && i <= dataStack.size()) {
                            dataStack.remove((dataStack.size() - 1) - (i - 1));
                        }
                    } else if (c.equals('@')) {
                        // apply immediately
                        Object x = dataStack.peek();

                        if (x instanceof String s) {
                            dataStack.pop();

                            for (int i = s.length() - 1; i >= 0; i--) {
                                commandStream.addFirst(s.charAt(i));
                            }
                        }
                    } else if (c.equals('\\')) {
                        // apply later
                        Object x = dataStack.peek();

                        if (x instanceof String s) {
                            dataStack.pop();

                            for (char c2 : s.toCharArray()) {
                                commandStream.addLast(c2);
                            }
                        }
                    } else if (c.equals('#')) {
                        // stack size
                        dataStack.push(dataStack.size());
                    } else if (c.equals('\'')) {
                        // read input
                        String line2 = inputStream.nextLine();
                        Object value;

                        // 1. try integer
                        try {
                            value = Integer.parseInt(line2);
                        } catch (NumberFormatException e1) {
                            // 2. try double
                            try {
                                value = Double.parseDouble(line2);
                            } catch (NumberFormatException e2) {
                                // 3. otherwise string - filter only ASCII
                                StringBuilder asciiOnly = new StringBuilder();
                                for (char ch : line2.toCharArray()) {
                                    if (ch <= 127) {
                                        asciiOnly.append(ch);
                                    }
                                }
                                value = asciiOnly.toString();
                            }
                        }

                        // push onto stack
                        dataStack.push(value);
                    } else if (c.equals('"')) {
                        // write output
                        Object value = dataStack.pop();

                        switch (value) {
                            case String s -> System.out.println(s);
                            case Integer i -> System.out.println(i);
                            case Double d -> {
                                // format double without unnecessary ".0"
                                if (d == Math.rint(d)) {
                                    System.out.println(d.intValue());
                                } else {
                                    System.out.println(d);
                                }
                            }
                            case null, default -> throw new IllegalStateException("unexpected type");
                        }

                    }
                }


            }
        }

    }

    private static void reset() {
        operationMode = 0;
        commandStream.clear();
        dataStack.clear();
        registerSet.clear();
    }

    private static int getCompareValue(Character c, Object fst, Object snd) {
        int val;
        if (fst instanceof Integer && snd instanceof Integer) {
            if (c.equals('<')) {
                val = (((Integer) fst) < ((Integer) snd)) ? 1 : 0;
            } else if (c.equals('>')) {
                val = (((Integer) fst) > ((Integer) snd)) ? 1 : 0;
            } else {
                val = fst.equals(snd) ? 1 : 0;
            }
        } else if (fst instanceof Number n1 && snd instanceof Number n2) {
            double d1 = n1.doubleValue();
            double d2 = n2.doubleValue();

            if (c.equals('=')) {
                if (d1 <= 1.0 && d1 >= -1.0 && d2 <= 1.0 && d2 >= -1.0) {
                    val = (d1 - d2) < EPSILON ? 1 : 0;
                } else {
                    double maxAbs = Math.max(Math.abs(d1), Math.abs(d2));
                    val = (d1 - d2) < maxAbs * EPSILON ? 1 : 0;
                }
            } else if (c.equals('>')) {
                val = (d1 > d2) ? 1 : 0;
            } else {
                val = (d1 < d2) ? 1 : 0;
            }
        } else if (fst instanceof String && snd instanceof String) {
            int result = ((String) fst).compareTo(((String) snd));

            if (c.equals('<')) {
                val = (result < 0) ? 1 : 0;
            } else if (c.equals('>')) {
                val = (result > 0) ? 1 : 0;
            } else {
                val = (result == 0) ? 1 : 0;
            }
        } else {
            // compare number with string -> number is smaller
            if (fst instanceof String) {
                if (c.equals('<')) {
                    val = 0;
                } else if (c.equals('>')) {
                    val = 1;
                } else {
                    val = 0;
                }
            } else if (snd instanceof String) {
                if (c.equals('<')) {
                    val = 1;
                } else if (c.equals('>')) {
                    val = 0;
                } else {
                    val = 0;
                }
            } else {
                throw new RuntimeException("CASE NOT COVERED");
            }
        }
        return val;
    }

}