// Add your documentation below:

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SCell implements Cell {
    private String line;
    private String datacalc;
    private int type;

    public SCell(String s) {
        // Add your code here
        setData(s);
        datacalc = line;
        updatetype();
    }

    public void updatetype() {
        if (isForm(line))
            type = 3;
        else if (isNumber(line)) {
            type = 2;

        }
        if (isText(line))
            type = 1;
        else {
            type = -1;
        }
    }

    @Override
    public int getOrder() {
        // Add your code here

        return 0;
        // ///////////////////
    }

    //@Override
    @Override
    public String toString() {
        return getData();
    }

    @Override
    public void setData(String s) {
        // Add your code here
        line = s;

        /////////////////////
    }

    @Override
    public String getData() {
        return line;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int t) {
        type = t;
    }

    @Override
    public void setOrder(int t) {
        // Add your code here

    }

    public static boolean isForm(String form) {
        String operators = "+-*/";
        int balance = 0;

        if (form == null || form.isEmpty() || form.trim().isEmpty()) {
            return false;
        }
        form = form.trim();
        // Check if the string starts with '='
        if (form.charAt(0) != '=') {
            return false;
        }
        form = form.substring(1);
        //checks for instances where the formula calls another cell
        if (Character.isLetter(form.charAt(0)) && Character.isDigit(form.charAt(1)) && form.length() == 2) {
            return true;
        }
        if (Character.isLetter(form.charAt(0)) && Character.isDigit(form.charAt(1)) && Character.isDigit(form.charAt(2)) && form.length() == 3) {
            return true;
        }
        // Base case: Single digit or valid number
        if (isNumber(form)) {
            return true;
        }

        // Avoid stripping parentheses unless they enclose the whole expression
        if (form.charAt(0) == '(' && form.charAt(form.length() - 1) == ')') {
            int balanceCheck = 0;
            boolean fullyEnclosed = true;
            for (int i = 0; i < form.length() - 1; i++) {
                char c = form.charAt(i);
                if (c == '(') balanceCheck++;
                if (c == ')') balanceCheck--;
                if (balanceCheck == 0 && i != form.length() - 2) {
                    fullyEnclosed = false;
                    break;
                }
            }
            if (fullyEnclosed) {
                return isForm('=' + form.substring(1, form.length() - 1));
            }
        }

        int lastOperator = -1;

        // Traverse the string while maintaining balance for parentheses
        for (int i = 0; i < form.length(); i++) {
            char c = form.charAt(i);

            if (c == '(') {
                balance++;
            } else if (c == ')') {
                balance--;
            } else if (operators.indexOf(c) != -1 && balance == 0) {
                lastOperator = i;
            }

            // If parentheses become unbalanced, return false
            if (balance < 0) {
                return false;
            }
        }
        for (int i = 0; i < form.length(); i++) {
            if (operators.indexOf(form.charAt(i)) != -1 && operators.indexOf(form.charAt(i + 1)) != -1) {
                return false;
            }
        }

        // If parentheses are not balanced at the end, return false
        if (balance != 0) {
            return false;
        }

        // If no operator found and not a valid number, return false
        if (lastOperator == -1) {
            return false;
        }

        // Split the expression and recursively validate both sides
        String left = form.substring(0, lastOperator);
        String right = form.substring(lastOperator + 1);

        return isForm('=' + left) && isForm('=' + right);
    }

    public static boolean isNumber(String str) {
        if (str.charAt(0) == '.')
            return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    public static boolean isText(String text) {
        if (!isForm(text) && !isNumber(text))
            return true;
        return false;
    }

    public static Double computeForms(String form) {
        String processedForm = form;

        // Regular expression to match cell references like A1, B12, or AB123
        Pattern cellPattern = Pattern.compile("[A-Z]+[0-9]+");
        Matcher matcher = cellPattern.matcher(form);

        while (matcher.find()) {
            String cellRef = matcher.group(); // Extract the cell reference, e.g., "A11"

            // Get the value of the referenced cell
            String cellValue =getData(cellRef);// Default to "0" if undefined


            // Check for nested formulas and compute them recursively
            if (isForm(cellValue)) {
                cellValue = computeForms(cellValue).toString();
            }

            // Replace the cell reference with its computed value in the formula
            processedForm = processedForm.replace(cellRef, cellValue);
        }

        // Compute the processed formula
        if (isForm(processedForm)) {
            return computeFormsub(processedForm, 1, processedForm.length() - 1);
        } else if (isNumber(processedForm)) {
            return Double.parseDouble(processedForm);
        } else {
            return -1.00; // Invalid formula
        }
    }
    //Help method to find the value of a given cell in a formula
    public static String getData (String s){
        int x = s.charAt(0)- 'A';
        int y = Integer.parseInt(s.substring(1,s.length()-1));
        return Ex2GUI.getTable().value(x,y);
    }
    //Same method just for the coordinates instead of the strings
    public static String getData (int x , int y){
        return Ex2GUI.getTable().value(x,y);
    }
    public static double computeFormsub(String text, int start, int end) {
        while (start <= end && text.charAt(start) == '(' && text.charAt(end) == ')') {
            start++;
            end--;
        }
        if (isNumber(text.substring(start, end + 1)))
            return Double.parseDouble(text.substring(start, end + 1));
        String RHS, LHS;
        int mainopindex = findMainOperator(text, start, end);
        double LHSVAL = computeFormsub(text, start, mainopindex - 1);
        double RHSVAL = computeFormsub(text, mainopindex + 1, end);
        char mainop = text.charAt(mainopindex);
        switch (mainop) {
            case '+':
                return LHSVAL + RHSVAL;
            case '-':
                return LHSVAL - RHSVAL;
            case '*':
                return LHSVAL * RHSVAL;
            case '/':
                return LHSVAL / RHSVAL;
            default:
                throw new ArithmeticException("unknown operator :" + mainop);
        }
    }

    public static int findMainOperator(String formula) {
        return findMainOperator(formula, 0, formula.length() - 1);
    }

    private static int findMainOperator(String formula, int start, int end) {
        //if the formula segment is a single character, return it
        if (start == end) {
            return start;
        }

        int mainOperatorPosition = -1;
        int lowestPrecedence = Integer.MAX_VALUE;
        int parenthesesCount = 0;

        // Traverse the formula segment
        for (int i = start; i <= end; i++) {
            char c = formula.charAt(i);

            if (c == '(') {
                parenthesesCount++;
            } else if (c == ')') {
                parenthesesCount--;
            } else if (parenthesesCount == 0 && isOperator(c)) {
                int precedence = getOperatorPrecedence(c);
                if (precedence <= lowestPrecedence) {
                    lowestPrecedence = precedence;
                    mainOperatorPosition = i;
                }
            }
        }

        // If no operator is found in the current segment, return the character at the start position
        if (mainOperatorPosition == -1) {
            return start;
        }

        // Recursively process the left and right segments of the formula
        int leftOperator = findMainOperator(formula, start, mainOperatorPosition - 1);
        int rightOperator = findMainOperator(formula, mainOperatorPosition + 1, end);

        // Return the main operator for the current segment
        return mainOperatorPosition;
    }

    // Helper method to check if a character is an operator
    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    // Helper method to get the precedence of an operator
    private static int getOperatorPrecedence(char operator) {
        switch (operator) {
            case '+':
            case '-':
                return 1; // Addition and subtraction have the lowest precedence
            case '*':
            case '/':
                return 2; // Multiplication and division have higher precedence than addition and subtraction
            default:
                return Integer.MAX_VALUE; // Unknown operator has the highest precedence
        }
    }

    // Helper method which finds a cell reference in another cell's formula
    public static boolean hasCell (String s){
        for(int i = 0 ; i<s.length();i++){
            if( s.charAt(i)=='e' && !Character.isDigit(s.charAt(i-1))||Character.isLetter(s.charAt(i))
            && Character.isDigit(s.charAt(i+1)))
                return true;
            }
        return false;
    }


}
