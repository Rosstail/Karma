package com.rosstail.karma.apis;

public class ExpressionCalculator {

    public static double eval(final String expression) {

        try {
            return new Object() { //To avoid async problems ?
                int pos = -1;
                int ch;

                void nextChar() { //place the position on the expression
                    if (++pos < expression.length()) { //if position inside expression
                        ch = expression.charAt(pos); //keep going
                    } else { //else
                        ch = -1; //back from start
                    }
                }

                boolean eat(int charToEat) { //eat = ignore or pass by
                    while (ch == ' ') { //ignore spaces in expression
                        nextChar();
                    }
                    if (ch == charToEat) { //ignore the char to eat
                        nextChar();
                        return true;
                    }
                    return false;
                }

                double parse() {
                    nextChar();
                    double x = parseExpression();
                    if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                    return x;
                }

                // Grammar:
                // expression = term | expression `+` term | expression `-` term
                // term = factor | term `*` factor | term `/` factor
                // factor = `+` factor | `-` factor | `(` expression `)`
                //        | number | functionName factor | factor `^` factor

                double parseExpression() {
                    double x = parseTerm(); //priority to terms
                    for (;;) { //Infinite
                        if (eat('+')) {
                            x += parseTerm(); // addition
                        } else if (eat('-')) {
                            x -= parseTerm(); // subtraction
                        } else {
                            return x;
                        }
                    }
                }

                double parseTerm() { //Mult or div are a priority
                    double x = parseFactor();
                    for (;;) { //Infinite
                        if (eat('*')) {
                            x *= parseFactor(); // multiplication
                        } else if (eat('/')) {
                            x /= parseFactor(); // division
                        } else {
                            return x;
                        }
                    }
                }

                double parseFactor() {
                    if (eat('+')) return parseFactor(); // unary plus
                    if (eat('-')) return -parseFactor(); // unary minus

                    double x;
                    int startPos = this.pos;
                    if (eat('(')) { // parentheses
                        x = parseExpression();
                        eat(')');
                    } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                        while ((ch >= '0' && ch <= '9') || ch == '.') {
                            nextChar();
                        }
                        x = Double.parseDouble(expression.substring(startPos, this.pos));
                    } else if (ch >= 'a' && ch <= 'z') { // functions
                        while (ch >= 'a' && ch <= 'z') nextChar();
                        String func = expression.substring(startPos, this.pos);
                        x = parseFactor();
                        switch (func) { //Add custom function here
                            case "sqrt":
                                x = Math.sqrt(x);
                                break;
                            case "sin":
                                x = Math.sin(Math.toRadians(x));
                                break;
                            case "cos":
                                x = Math.cos(Math.toRadians(x));
                                break;
                            case "tan":
                                x = Math.tan(Math.toRadians(x));
                                break;
                            case "rand":
                                x = Math.random() * x;
                                break;
                            default:
                                throw new RuntimeException("Unknown function: " + func);
                        }
                    } else {
                        throw new RuntimeException("Unexpected: " + (char)ch);
                    }

                    if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                    return x;
                }
            }.parse(); //parse Object to double before returning it

        }
        catch (Exception e) {
            System.out.println("[Karma] An exception as occured during the calculation : " + expression);
        }
        return 0D;
    }
}
