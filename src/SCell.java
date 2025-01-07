// Add your documentation below:

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SCell implements Cell {
    private String line,CellName;
    private String ComputedValue;
    private int type;
    public SCell() {
        line = "";
        ComputedValue = "";
        type = Ex2Utils.TEXT;
    }
    public SCell (Cell s){
        this.line=s.getFormula();
        this.ComputedValue=s.getComputedValue();
        this.type=s.getType();
    }
    public SCell(String s) {
        setData(s);
        ComputedValue = line;
        updatetype();
    }

    public String getComputedValue() {
        return ComputedValue;
    }

    public void setComputedValue(String value) {
        this.ComputedValue = value;
    }

    public int updatetype() {
        if (isForm(line))
            type = Ex2Utils.FORM;
        else if (isNumber(line)) {
            type = Ex2Utils.NUMBER;

        }
        else if (isText(line))
            type = Ex2Utils.TEXT;
        else {
            double computedvalue = this.computeForms();
            type = (int) computedvalue;
        }
        return type;


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
        this.line = s.trim();
        if (s.startsWith("=")) {
            this.type = Ex2Utils.FORM;

            // Create a Set to track the current chain of visited cells
            Set<Cell> visitedCells = new HashSet<>();

            // Compute the formula value, passing in the visitedCells set to detect cycles
            double computedValue = this.computeForms();

            // If there's a cycle, set the computed value to "ERROR_CYCLE!"
            if (computedValue == Ex2Utils.ERR_CYCLE_FORM) {
                this.ComputedValue = Ex2Utils.ERR_CYCLE;  // Mark as circular reference
            } else {
                this.ComputedValue = String.valueOf(computedValue);  // Store the computed value as string
            }

        } else if (isNumber(s)) {
            this.type = Ex2Utils.NUMBER;
            this.ComputedValue = s;  // Store the number as a string
        } else {
            this.type = Ex2Utils.TEXT;
            this.ComputedValue = String.valueOf(Ex2Utils.ERR_FORM_FORMAT);  // Invalid formula stored as -2 string
        }
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

    @Override
    public String getFormula() {
        return this.line;
    }

    @Override
    public void setFormula(String formula) {
        this.line = formula;
    }

    public static boolean isForm(String form) {
        if (form == null || form.trim().isEmpty()) {
            return false; // Null or empty formulas are invalid
        }

        form = form.trim();

        // Check if the formula starts with '='
        if (form.charAt(0) != '=') {
            return false;
        }

        form = form.substring(1); // Remove the leading '='
        form = form.replaceAll("\\s+", ""); // Remove spaces

        // Validate parentheses balance and placement
        int balance = 0;
        for (int i = 0; i < form.length(); i++) {
            char c = form.charAt(i);
            if (c == '(') balance++;
            if (c == ')') balance--;
            if (balance < 0) return false; // Unbalanced parentheses
        }
        if (balance != 0) return false; // Unbalanced parentheses at the end

        // Regex patterns for validation
        String numberPattern = "-?\\d+(\\.\\d+)?"; // Matches numbers (e.g., 2, -2.99)
        String cellReferencePattern = "[A-Z]+[0-9]+"; // Matches cell references (e.g., A0, B10)
        String validTerm = "(" + numberPattern + "|" + cellReferencePattern + ")"; // A valid term is a number or cell reference
        String operatorPattern = "[+\\-*/]"; // Supported operators

        // Regex for matching entire formula structure
        String formulaPattern = "\\(*" + validTerm + "\\)*(" + operatorPattern + "\\(*" + validTerm + "\\)*)*";

        // Check if the entire formula matches
        return form.matches(formulaPattern);
    }

    //Helper method that checks for cell references in a formula
    private static boolean isValidCellReference(String ref) {
        if (ref == null || ref.isEmpty()) {
            return false;
        }
        // Match cell references like A0, B12, Z123 (letters followed by digits)
        return ref.matches("[A-Za-z]+[0-9]+");
    }

    public static boolean isNumber(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    public static boolean isText(String text) {
        return !isNumber(text) && !isForm(text);
    }

    public  Double computeForms() {
        if (this.ComputedValue == null || ComputedValue.isEmpty())
            return (double) Ex2Utils.ERR_FORM_FORMAT; // Treat empty or null formula as invalid

        if (!isForm(ComputedValue) && !isNumber(ComputedValue)) {
            return (double) Ex2Utils.ERR_FORM_FORMAT; // Invalid formula syntax
        }

        ComputedValue = ComputedValue.trim();
        return computeForms(new HashSet<>());
    }

    public double computeForms(Set<Cell> visitedCells) {
        // If this cell is already in the visited set, it's a circular reference
        if (visitedCells.contains(this)) {
            this.setComputedValue(Ex2Utils.ERR_CYCLE);  // Mark as circular reference
            return Ex2Utils.ERR_CYCLE_FORM;  // Return the circular reference error
        }

        // Add this cell to the visited cells set to track the current evaluation chain
        visitedCells.add(this);

        double result = 0;
        try {
            String formula = this.getData().substring(1); // Remove '=' from the formula
            // Resolve cell references (if any)
            String resolvedFormula = resolveCellReferences(formula, visitedCells);

            // Now evaluate the resolved formula
            result = computeFormsub(resolvedFormula, 0, resolvedFormula.length() - 1);
            this.setComputedValue(String.valueOf(result));  // Store computed value

        } catch (Exception e) {
            // Handle exceptions, like invalid formulas
            this.setComputedValue(Ex2Utils.ERR_FORM);
            result = Ex2Utils.ERR_FORM_FORMAT;
        } finally {
            // Remove the cell from the visited set after evaluation
            visitedCells.remove(this);
        }

        return result;
    }


    //Helper method to find the value of a given cell in a formula
    public double computeFormsub(String text, int start, int end) {
        // Remove surrounding parentheses
        while (start <= end && text.charAt(start) == '(' && text.charAt(end) == ')') {
            start++;
            end--;
        }

        // Handle unary negative numbers
        if (start <= end && text.charAt(start) == '-') {
            if (start == end) { // Single negative number like "-2"
                return Double.parseDouble(text.substring(start, end + 1));
            }
            // Negate the rest of the subexpression
            return -computeFormsub(text, start + 1, end);
        }

        int mainOpIndex = -1;
        int parenCount = 0;
        int lowestPrecedence = Integer.MAX_VALUE;

        // Identify the main operator considering precedence
        for (int i = start; i <= end; i++) {
            char c = text.charAt(i);

            if (c == '(') {
                parenCount++; // Entering a parenthesis
            } else if (c == ')') {
                parenCount--; // Exiting a parenthesis
            } else if (parenCount == 0) { // Outside parentheses
                int precedence = getOperatorPrecedence(c);
                if (precedence > 0 && precedence <= lowestPrecedence) {
                    mainOpIndex = i;
                    lowestPrecedence = precedence;
                }
            }
        }

        // If no operator is found, the text should be a number
        if (mainOpIndex == -1) {
            if (isNumber(text.substring(start, end + 1))) {
                return Double.parseDouble(text.substring(start, end + 1));
            }
            throw new IllegalArgumentException("Invalid subexpression: " + text.substring(start, end + 1));
        }

        // Split the expression into LHS and RHS
        double lhs = computeFormsub(text, start, mainOpIndex - 1);
        double rhs = computeFormsub(text, mainOpIndex + 1, end);
        char mainOp = text.charAt(mainOpIndex);

        // Perform the operation
        switch (mainOp) {
            case '+':
                return lhs + rhs;
            case '-':
                return lhs - rhs;
            case '*':
                return lhs * rhs;
            case '/':
                if (rhs == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return lhs / rhs;
            default:
                throw new ArithmeticException("Unknown operator: " + mainOp);
        }
    }

    // Helper to determine operator precedence
    private static int getOperatorPrecedence(char op) {
        switch (op) {
            case '+':
            case '-':
                return 1; // Lowest precedence
            case '*':
            case '/':
                return 2; // Higher precedence
            default:
                return -1; // Not an operator
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

    public  ArrayList<String> getDependencies(String formula) {
        ArrayList<String> dependencies = new ArrayList<>();

        // Validate the input formula
        if (formula == null || formula.isEmpty() || !isForm(formula)) {
            return dependencies;
        }

        // Normalize the formula
        formula = formula.substring(1).trim().toUpperCase();

        // Regex to match cell references (e.g., A0, B12)
        Pattern cellPattern = Pattern.compile("[A-Z]+[0-9]+");
        Matcher matcher = cellPattern.matcher(formula);

        while (matcher.find()) {
            dependencies.add(matcher.group());
        }

        return dependencies;
    }
    public String resolveCellReferences(String formula, Set<Cell> visitedCells) {
        // Extract all cell references (e.g., A1, B2) from the formula
        ArrayList<String> dependencies = this.getDependencies(formula);

        // Iterate through each dependency to resolve its value
        for (String dep : dependencies) {
            int refX = dep.charAt(0) - 'A'; // Convert letter to column index (e.g., 'A' -> 0)
            int refY = Integer.parseInt(dep.substring(1)) - 1; // Convert row number (e.g., "1" -> 0)

            // Get the referenced cell's data
            Cell refCell = getCell(refX, refY);  // Use helper method to get a specific cell

            // If the referenced cell is a formula, compute its value recursively
            if (refCell.getType() == Ex2Utils.FORM) {
                // Recursively evaluate the referenced cell
                double refValue = ((SCell) refCell).computeForms(visitedCells);
                formula = formula.replace(dep, String.valueOf(refValue));  // Replace reference with computed value
            } else {
                // Replace reference with the actual data (if it's not a formula)
                formula = formula.replace(dep, refCell.getData());
            }
        }

        return formula;  // Return the formula with all resolved references
    }
    // Helper method to access a specific cell from the table
    private Cell getCell(int x, int y) {
        return Ex2Sheet.table[x][y];  // Assuming Ex2Sheet is a static reference to the table
    }

}
