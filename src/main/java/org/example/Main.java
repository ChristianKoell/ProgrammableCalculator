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

    // TODO: not yet sure how to implement
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


            for (Character c : line.toCharArray()) {
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
                        // TODO: execute character in execution mode 0
                    }
                } else if (operationMode < -1) {
                    if (!(dataStack.peek() instanceof Double)) {
                        throw new IllegalStateException("operationMode < -1 -> top element of data stack must be a floating-point number");
                    }

                    if (Character.isDigit(c)) {
                        double d = (c - '0') * Math.pow(10, operationMode + 1);
                        dataStack.push(((double) dataStack.pop()) + d);
                    } else if (c.equals('.')) {
                        dataStack.push(0.0);
                        operationMode = -2;
                    } else {
                        operationMode = 0;
                        // TODO: execute character in execution mode 0
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
                        Object fst = dataStack.pop();
                        Object snd = dataStack.pop();

                        int val = getCompareValue(c, fst, snd);
                        dataStack.push(val);
                    } else if (Arrays.asList('+', '-', '*', '/', '%').contains(c)) {
                        Object fst = dataStack.pop();
                        Object snd = dataStack.pop();

                        boolean bothInt = fst instanceof Integer && snd instanceof Integer;

                        if (fst instanceof Number && snd instanceof Number) {
                            double d1 = ((Number) fst).doubleValue();
                            double d2 = ((Number) snd).doubleValue();

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

                        } else if (fst instanceof String && snd instanceof String && c.equals('/')) {
                            dataStack.push(((String) fst).indexOf(((String) snd)));
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
                        Object fst = dataStack.pop();
                        Object snd = dataStack.pop();

                        if (fst instanceof Integer && snd instanceof Integer) {
                            if (c.equals('&')) {
                                dataStack.push(((int) fst) != 0 && ((int) snd) != 0);
                            } else {
                                dataStack.push(((int) fst) != 0 || ((int) snd) != 0);
                            }
                        } else {
                            dataStack.push("()");
                        }
                    } else if (c.equals('_')) {
                        // null-check (negate boolean)
                        Object x = dataStack.pop();

                        if (x instanceof String s) {
                            dataStack.push(s.equals("()") ? 1 : 0);
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
                            dataStack.push(dataStack.get(i));
                            dataStack.pop(); // TODO: remove after? -> otherwise adapt range
                        }
                    } else if (c.equals('$')) {
                        // delete
                        Object x = dataStack.pop();

                        if (x instanceof Integer i && i >= 1 && i <= dataStack.size()) {
                            dataStack.remove(i);
                        }
                    } else if (c.equals('@')) {
                        // apply immediately
                        Object x = dataStack.peek();

                        if (x instanceof String s) {
                            dataStack.pop();

                            // TODO: correct order ?
                            for (char c2 : s.toCharArray()) {
                                commandStream.addFirst(c2);
                            }
                        }
                    } else if (c.equals('\\')) {
                        // apply later
                        Object x = dataStack.peek();

                        if (x instanceof String s) {
                            dataStack.pop();

                            // TODO: correct order ?
                            for (char c2 : s.toCharArray()) {
                                commandStream.add(c2);
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
                            value = Integer.parseInt(line);
                        } catch (NumberFormatException e1) {
                            // 2. try double
                            try {
                                value = Double.parseDouble(line);
                            } catch (NumberFormatException e2) {
                                // 3. otherwise string - filter only ASCII
                                StringBuilder asciiOnly = new StringBuilder();
                                for (char ch : line.toCharArray()) {
                                    if (ch >= 0 && ch <= 127) {
                                        asciiOnly.append(ch);
                                    }
                                }
                                value = asciiOnly.toString();
                            }
                        }

                        // Push onto stack
                        dataStack.push(value);
                    } else if (c.equals('"')) {
                        // write output
                        System.out.println(dataStack.pop());
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
        } else if ((fst instanceof Double || snd instanceof Double)
                && ((fst instanceof Double && snd instanceof Double)
                || (fst instanceof Integer || snd instanceof Integer)
        )) {
            double d1 = ((double) fst);
            double d2 = ((double) snd);

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