package calculator;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    static Map<String, String> vars = new HashMap<>();

    static final Pattern signNumberRegex = Pattern.compile("\\s*[-|+]?[0-9]+\\s*");
    static final Pattern numberRegex = Pattern.compile("\\s*[0-9]+\\s*");
    static final Pattern varNameRegex = Pattern.compile("\\s*[a-zA-Z]+\\s*");
    static final Pattern lowSignRegex = Pattern.compile("\\s*[+|-]+\\s*");
    static final Pattern highSignRegex = Pattern.compile("\\s*[*|/]+\\s*");

    static final Pattern varValueRegex = Pattern.compile("(" + signNumberRegex + "|[+|-]?" + varNameRegex + ")");
    static final Pattern operator = Pattern.compile("(" + lowSignRegex + "|" + highSignRegex + ")");

    static int getPrecedence(String token) {
        if (token.matches(lowSignRegex.pattern())) {
            return 0;
        } else if (token.matches(highSignRegex.pattern())) {
            return 1;
        }
        return -1;
    }


    static void processOperator(String token, Deque<String> postfixExp, Deque<String> operators) {
        if (operators.isEmpty() || operators.peekLast().equals("(") ||
                (token.matches(String.valueOf(highSignRegex)) &&
                        operators.peekLast().matches(String.valueOf(lowSignRegex)))){
            operators.offerLast(token);
        } else {
            if (getPrecedence(token) <= getPrecedence(operators.getLast())) {
                while (operators.peekLast() != null &&
                        getPrecedence(token) <= getPrecedence(operators.getLast()) &&
                        !operators.getLast().equals("(")) {
                    postfixExp.offerLast(operators.removeLast());
                }
                operators.offerLast(token);
            }
        }
    }

    static void processParenthesis(String token, Deque<String> postfixExp, Deque<String> operators) {
        if (token.matches("\\s*\\(\\s*")) {
            operators.offerLast(token);
        } else if (token.matches("\\s*\\)\\s*")) {
            while (!operators.isEmpty() &&
                    !operators.peekLast().matches("\\s*\\(\\s*")) {
                postfixExp.offerLast(operators.removeLast());
            }
            if (operators.isEmpty() || !operators.getLast().matches("\\s*\\(\\s*")){
                throw new RuntimeException("Invalid expression");
            }
            operators.removeLast();
        }
    }

    static String useOperator(String sa, String sb, String operator) {
        BigInteger a = new BigInteger(sa);
        BigInteger b = new BigInteger(sb);

        switch (operator) {
            case "+":
                return String.valueOf(a.add(b));
            case "*":
                return String.valueOf(a.multiply(b));
            case "-":
                return String.valueOf(b.subtract(a));
            case "/":
                return String.valueOf(b.divide(a));
            default:
                throw new RuntimeException("Invalid expression");
        }
    }

    static BigInteger calculateResult(Deque<String> postfixExp) {
        Deque<String> temp = new LinkedList<>();
        while (!postfixExp.isEmpty()) {
            String token = postfixExp.removeFirst();
            if (token.matches(signNumberRegex.pattern())) {
                temp.offerLast(token);
            } else if (token.matches(varNameRegex.pattern())) {
                if (!vars.containsKey(token)) {
                    throw new RuntimeException("Unknown variable");
                }
                temp.offerLast(vars.get(token));
            } else if (token.matches(operator.pattern())) {
                temp.offerLast(useOperator(temp.removeLast(), temp.removeLast(), token));
            } else {
                throw new RuntimeException("Invalid expression");
            }
        }
        return new BigInteger(temp.removeLast());
    }

    static BigInteger calculate(String[] splits) {
        Deque<String> postfixExp = new LinkedList<>();
        Deque<String> operators = new LinkedList<>();

        for (String token : splits) {
            Matcher matcherValue = varValueRegex.matcher(token);
            Matcher matcherOperator = operator.matcher(token);
            if (matcherValue.matches()) {
                postfixExp.offerLast(token);
            } else if (matcherOperator.matches()) {
                processOperator(token, postfixExp, operators);
            } else if (token.matches("\\s*[(|)]\\s*")) {
                processParenthesis(token, postfixExp, operators);
            } else {
                throw new RuntimeException("Invalid expression");
            }
        }
        while (!operators.isEmpty()) {
            postfixExp.offerLast(operators.removeLast());
        }
        return calculateResult(postfixExp);
    }

    static boolean executeCommand(String str) {
        if ("/exit".equals(str)) {
            System.out.println("Bye!");
            return false;
        } else if ("/help".equals(str)) {
            System.out.println("The program calculates the sum of numbers");
        } else {
            throw new RuntimeException("Unknown command");
        }
        return true;
    }
    static void processVar(String str) {
        str = str.replaceAll("\\s", "");
        String[] strs = str.split("=");
        if (strs.length != 2 ||
                !strs[0].matches(varNameRegex.pattern()) ||
                !strs[1].matches(varValueRegex.pattern()))
            throw new RuntimeException("Invalid assignment");
        String num;
        if (strs[1].matches(signNumberRegex.pattern()))
            num = strs[1];
        else
            num = vars.get(strs[1]);
        if (num == null)
            throw new RuntimeException("Unknown variable");
        vars.put(strs[0], num);
    }

    static String[] processInput(String input) {
        input = input.replaceAll("\\(", " ( ");
        input = input.replaceAll("\\)", " ) ");
        input = input.replaceAll("\\b", " ");
        input = input.trim();
        input = input.replaceAll("\\s+", " ");

        String[] splits = input.split(" ", 0);
        for (int i = 0; i < splits.length; i++) {
            if (splits[i].matches("\\++")) {
                splits[i] = "+";
            } else if (splits[i].matches("-+")) {
                if (splits[i].length() % 2 == 0) {
                    splits[i] = "+";
                } else {
                    splits[i] = "-";
                }
            }
        }

        return splits;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                String str = scanner.nextLine();

                if (str.matches("/.*")) {
                    if (!executeCommand(str))
                        break;
                } else if (str.equals("")) {
                } else if (str.matches(".*=.*")) {
                    processVar(str);
                } else {
                    System.out.println(calculate(processInput(str)));
                }
            } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                }
        }
    }
}
