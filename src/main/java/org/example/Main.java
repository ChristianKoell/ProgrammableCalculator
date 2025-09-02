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
        // registerSet.set('a', "(put in some text:) \"");

        // for (Character c : ((String) registerSet.get('a')).toCharArray()) {
        //    commandStream.add(c);
        //}

        // 1 -> anchor
        // 2 -> output string
        // 3 -> temp string
        // 4 -> letter-num
        // 5 -> digit-num
        // 6 -> whitespace-num
        // 7 -> specialChar-num
        // 8 -> word-num
        // Entry point: create counters/strings, move input string to the top of the stack, start the loop
        registerSet.set('S', "(stp) ()() 0 0 0 0 0 m@ 0R@");

        // increment the n-th data-stack entry by 1 and restore the original stack order
        // ((...)@) executed in False case; ((... 2$ 2$ 3! 1- l@)@) removes the options from the data, stack, copies the counter, decrements it and re-starts the loop; "4!" copies the counter to the top of the stack; "_" checks if the counter is 0; "2+!@" executes the corresponding branch
        registerSet.set('i', "n@ 1+ c@");     // increment the n-th data-stack entry by 1

        // moves the first element from the stack to the back
        registerSet.set('m', "#1+!#$");

        // check if letter
        registerSet.set('l', " x@X@|");       // check if the character at position 0 is a letter
        registerSet.set('x', " 2! 96> 3! 123<&");   // check for small letter
        registerSet.set('X', " 3! 64> 4! 91<&");    // check for capital letter
        // if isLetter -> increment stackPos 4 by one, else go to next condition
        registerSet.set('L', "(( 4i@ 2$ 2$ O@)@)(( 2$ 2$ D@)@) 5! 5!% l@ 3+!@");
        // check if integer
        registerSet.set('d', " 4! 47> 5! 58<&");    // check if the character is a digit; char to be checked needs to be on top
        // if isDigit -> increment stackPos 5 by one, else go to next condition
        registerSet.set('D', "(( 5i@ 2$ 1$ O@)@)(( 2$ 1$ W@)@) d@ 2+!@");
        // check if whitespace
        registerSet.set('w', " 4! 32=");
        // if isWhitespace -> increment stackPos 6 by one, else go to next condition
        registerSet.set('W', "(( 6i@ 2$ 1$ F@)@)(( 2$ 1$ P@)@) w@ 2+!@");
        // if !anyOtherCondition -> special character --> increment stackPos 7 by one
        registerSet.set('P', " 7i@ F@");
        // loop through the input string
        registerSet.set('R', "((Q@)@)(( 1$ 1$ L@)@) 5! 5!%_ 2+!@");

        registerSet.set('j', "n@ y@ c@");     // get the n-th dataStack item to the top and execute register y afterward
        registerSet.set('y', " 4! m@*");      // append the current letter/digit to the front of the string

        registerSet.set('k', "n@ v@ c@ 3n@ ()% c@");     // get the n-th dataStack item to the top and execute register v afterward
        registerSet.set('v', " #1+!+ 4!*");   // append the tempString to the front of the string + append the whitespace/Special character to the end of the string

        registerSet.set('u', "(( 1$ 1$ 1$)@)(( 8i@ 1$ 1$ 1$)@) 14! 0%_ 2+!@");


        registerSet.set('n', "(((1$ 1$ 1$)@)@)((m@ 2$ 2$ 3! 3$ 1- n@)@) 4! _ 2+!@");     // get the n-th dataStack item to the top
        registerSet.set('c', "((m@ 2$ 2$)@)((m@ 2$ 2$ c@)@) #! (stp)= 2+!@");            // restore the original stack order

        registerSet.set('t', " 1$ 1$ 1$ #1+!+ 4!* c@");                      // append the tempString to the front of the string + append the whitespace/Special character to the end of the string


        registerSet.set('O', " 2j@ 1$ 1+ R@");
        registerSet.set('F', " 2k@ 1$ 1+ R@");
        // program end
        registerSet.set('Q', " 1$ 1$ 1$");



        while (true) {
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
                                dataStack.push("");
                            } else if (c.equals('/') && Math.abs(d2) < EPSILON) {
                                dataStack.push("");
                            } else {
                                double val = switch (c) {
                                    case '+' -> d1 + d2;
                                    case '-' -> d1 - d2;
                                    case '*' -> d1 * d2;
                                    case '/' -> d1 / d2;
                                    case '%' -> d1 % d2;
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
                                dataStack.push("");
                            }
                        } else {
                            dataStack.push("");
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
                            dataStack.push("");
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
                            dataStack.push("");
                        }
                    } else if (c.equals('?')) {
                        // integer conversion
                        Object x = dataStack.pop();

                        if (x instanceof Double d) {
                            dataStack.push(d.intValue());
                        } else {
                            dataStack.push("");
                        }
                    } else if (c.equals('!')) {
                        // copy
                        Object x = dataStack.peek();

                        if (x instanceof Integer i && i >= 1 && i <= dataStack.size()) {
                            Object item = dataStack.get(dataStack.size() - 1 - (i - 1));
                            dataStack.pop();
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
                    } else if (c.equals(':')) {
                        reset();
                    } else if (c.equals(';')) {
                        System.out.println("power off");
                        return;
                    }
                }

                printState();
            }

            readLine();
        }

    }

    private static void readLine() {
        System.out.print("\t> ");
        String line;
        line = inputStream.nextLine();

        for (int index = 0; index < line.length(); index++) {
            commandStream.addLast(line.charAt(index));
        }
    }

    private static void printState() {
        dataStack.printStack();
        System.out.print("\t\t<\t\t");
        for (char command : commandStream) {
            System.out.print(command);
        }
        System.out.println();
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