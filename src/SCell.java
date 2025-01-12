    // Add your documentation below:

    import java.util.ArrayList;
    import java.util.HashSet;
    import java.util.Set;
    import java.util.regex.Matcher;
    import java.util.regex.Pattern;

    public class SCell implements Cell {
        private String originaldata;
        private String ComputedValue;
        private int type;

        public SCell() {
            originaldata = Ex2Utils.EMPTY_CELL;
            ComputedValue = Ex2Utils.EMPTY_CELL;
            type = Ex2Utils.TEXT;

        }

        public SCell(Cell s) {
            this.originaldata = s.getData();
            this.ComputedValue = s.getComputedValue();
            this.type = s.getType();

        }

        public SCell(String s) {
            this();
            this.setData(s);
        }

        public String getComputedValue() {
            if (type == Ex2Utils.FORM) {
                try {
                    // Remove the '=' before computing
                    String formula = originaldata;
                    if (formula.startsWith("=")) {
                        formula = formula.substring(1);
                    }
                    Double result = evaluateform(formula, 0, formula.length() - 1);
                    return result.toString();
                } catch (Exception e) {
                    return Ex2Utils.ERR_FORM;
                }
            }
            return originaldata;
        }

        public void setComputedValue(String value) {
            this.ComputedValue = value;
        }

        public int updatetype() {
            if (originaldata == null || originaldata.trim().isEmpty()) {
                this.type = Ex2Utils.TEXT;
            } else if (originaldata.startsWith("=")) {
                this.type = Ex2Utils.FORM;
            } else if (isNumber(originaldata)) {
                this.type = Ex2Utils.NUMBER;
            } else {
                this.type = Ex2Utils.TEXT;
            }
            return this.type;
        }

        @Override
        public int getOrder() {
            // Add your code here

            return 0;
            // ///////////////////
        }

        @Override
        public String toString() {
            return getData();
        }

        @Override
        public void setData(String s) {
            originaldata = s;
            updatetype();
        }

        @Override
        public String getData() {
            return originaldata;
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
            if (form == null || form.trim().isEmpty()) {
                return false; // Null or empty formulas are invalid
            }

            form = form.trim();

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
                if (balance < 0) return false;
            }
            if (balance != 0) return false;

            String numberPattern = "-?\\d+(\\.\\d+)?";
            String cellReferencePattern = "[A-Z]+[0-9]+"; // Matches cell references
            String validTerm = "(" + numberPattern + "|" + cellReferencePattern + ")";
            String operatorPattern = "[+\\-*/]";

            String formulaPattern = "\\(*" + validTerm + "\\)*(" + operatorPattern + "\\(*" + validTerm + "\\)*)*";

            // Check if the entire formula matches
            return form.matches(formulaPattern);
        }

        public static boolean isNumber(String str) {
            if(str.contains("+"))
                return false;
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
            if (text.equals(Ex2Utils.ERR_CYCLE) || text.equals(Ex2Utils.ERR_FORM) || text.equals(Ex2Utils.ERR_FORM_FORMAT)) {
                return false;
            }

            if (isNumber(text) || isForm(text)) {
                return false;
            }
            return true;
        }

        public static Double computeForms(String form) {
            if (form == null || form.isEmpty()) {
                return (double) Ex2Utils.ERR_FORM_FORMAT;
            }

            try {
                // Remove the '=' and trim whitespace
                String formula = form.startsWith("=") ? form.substring(1).trim() : form.trim();

                // Process cell references
                Pattern cellPattern = Pattern.compile("[A-Z][0-9]+");
                Matcher matcher = cellPattern.matcher(formula);
                StringBuffer processedFormula = new StringBuffer();

                while (matcher.find()) {
                    String cellRef = matcher.group();
                    int col = cellRef.charAt(0) - 'A';
                    int row = Integer.parseInt(cellRef.substring(1));

                    Cell referencedCell = Ex2Sheet.getCell(col, row);
                    if (referencedCell == null) {
                        matcher.appendReplacement(processedFormula, "0");
                        continue;
                    }

                    String cellValue = referencedCell.getData();
                    if (cellValue == null || cellValue.isEmpty()) {
                        matcher.appendReplacement(processedFormula, "0");
                    } else if (cellValue.startsWith("=")) {
                        // Recursively evaluate the referenced cell
                        Double refValue = computeForms(cellValue);
                        matcher.appendReplacement(processedFormula, String.valueOf(refValue));
                    } else if (isNumber(cellValue)) {
                        matcher.appendReplacement(processedFormula, cellValue);
                    } else {
                        matcher.appendReplacement(processedFormula, "0");
                    }
                }
                matcher.appendTail(processedFormula);

                // Evaluate the processed formula
                return evaluateform(processedFormula.toString(), 0, processedFormula.length() - 1);
            } catch (Exception e) {
                return (double) Ex2Utils.ERR_FORM_FORMAT;
            }
        }

        private static Double computeFormSub(String formula, Set<String> visited) {
            if (isNumber(formula)) {
                return Double.parseDouble(formula);
            }

            Pattern cellPattern = Pattern.compile("[A-Z][0-9]+");
            Matcher matcher = cellPattern.matcher(formula);
            StringBuffer processedFormula = new StringBuffer();

            while (matcher.find()) {
                String cellRef = matcher.group();

                // Check for circular dependency
                if (visited.contains(cellRef)) {
                    return (double) Ex2Utils.ERR_CYCLE_FORM;
                }

                visited.add(cellRef);

                int col = cellRef.charAt(0) - 'A';
                int row = Integer.parseInt(cellRef.substring(1));
                Cell cell = Ex2Sheet.getCell(col, row);

                if (cell == null || cell.getData() == null) {
                    matcher.appendReplacement(processedFormula, "0");
                    continue;
                }

                String cellValue = cell.getData();
                if (cellValue.equals(Ex2Utils.ERR_CYCLE)) {
                    return (double) Ex2Utils.ERR_CYCLE_FORM;
                }

                if (cellValue.startsWith("=")) {
                    // Create new visited set with current path for recursive call
                    Set<String> newVisited = new HashSet<>(visited);
                    Double result = computeFormSub(cellValue.substring(1), newVisited);
                    if (result == Ex2Utils.ERR_CYCLE_FORM) {
                        return result;
                    }
                    matcher.appendReplacement(processedFormula, result.toString());
                } else if (isNumber(cellValue)) {
                    matcher.appendReplacement(processedFormula, cellValue);
                } else {
                    matcher.appendReplacement(processedFormula, "0");
                }

                visited.remove(cellRef);
            }
            matcher.appendTail(processedFormula);

            try {
                return evaluateform(processedFormula.toString(), 0, processedFormula.length() - 1);
            } catch (Exception e) {
                return (double) Ex2Utils.ERR_FORM_FORMAT;
            }
        }

        private static boolean hasCircularDependency(String formula, Set<String> visited) {
            Pattern cellPattern = Pattern.compile("[A-Z][0-9]+");
            Matcher matcher = cellPattern.matcher(formula);

            while (matcher.find()) {
                String cellRef = matcher.group();

                // Check for self-reference or circular dependency
                if (visited.contains(cellRef)) {
                    return true;
                }

                visited.add(cellRef);

                int col = cellRef.charAt(0) - 'A';
                int row = Integer.parseInt(cellRef.substring(1));
                Cell cell = Ex2Sheet.getCell(col, row);

                if (cell != null && cell.getData() != null && cell.getData().startsWith("=")) {
                    String cellFormula = cell.getData().substring(1);
                    if (hasCircularDependency(cellFormula, new HashSet<>(visited))) {
                        return true;
                    }
                }

                visited.remove(cellRef);
            }

            return false;
        }



        public static double evaluateform(String text, int start, int end) {
            String expr = text.substring(start, end + 1).trim();

            // Handle empty expression
            if (expr.isEmpty()) {
                return 0;
            }

            // Base case: if it's a number
            if (isNumber(expr)) {
                return Double.parseDouble(expr);
            }

            // Handle parentheses first
            while (expr.startsWith("(") && expr.endsWith(")")) {
                String inner = expr.substring(1, expr.length() - 1).trim();
                if (!inner.isEmpty()) {
                    expr = inner;
                } else {
                    break;
                }
            }

            // Handle unary minus
            if (expr.startsWith("-")) {
                String rest = expr.substring(1).trim();
                if (isNumber(rest)) {
                    return Double.parseDouble(expr);
                }
                return -evaluateform(rest, 0, rest.length() - 1);
            }

            // Find the last operator with lowest precedence
            int mainOpIndex = -1;
            int parenCount = 0;
            int lowestPrecedence = 1;  // Start with multiplication/division precedence

            // Scan for + and - first (lower precedence)
            for (int i = expr.length() - 1; i >= 0; i--) {
                char c = expr.charAt(i);
                if (c == ')') parenCount++;
                else if (c == '(') parenCount--;
                else if (parenCount == 0 && (c == '+' || c == '-')) {
                    mainOpIndex = i;
                    break;
                }
            }

            // If no + or -, scan for * and /
            if (mainOpIndex == -1) {
                for (int i = expr.length() - 1; i >= 0; i--) {
                    char c = expr.charAt(i);
                    if (c == ')') parenCount++;
                    else if (c == '(') parenCount--;
                    else if (parenCount == 0 && (c == '*' || c == '/')) {
                        mainOpIndex = i;
                        break;
                    }
                }
            }

            // If no operator found
            if (mainOpIndex == -1) {
                return Double.parseDouble(expr);
            }

            // Split and evaluate parts
            String leftExpr = expr.substring(0, mainOpIndex).trim();
            String rightExpr = expr.substring(mainOpIndex + 1).trim();

            // Evaluate both sides
            double leftVal = leftExpr.isEmpty() ? 0 : evaluateform(leftExpr, 0, leftExpr.length() - 1);
            double rightVal = evaluateform(rightExpr, 0, rightExpr.length() - 1);

            // Apply operator
            char operator = expr.charAt(mainOpIndex);
            switch (operator) {
                case '+': return leftVal + rightVal;
                case '-': return leftVal - rightVal;
                case '*': return leftVal * rightVal;
                case '/':
                    if (rightVal == 0) throw new ArithmeticException("Division by zero");
                    return leftVal / rightVal;
                default:
                    throw new IllegalArgumentException("Invalid operator");
            }
        }



        private static int getOperatorPrecedence(char op) {
            switch (op) {
                case '+':
                case '-':
                    return 2;  // Lower precedence
                case '*':
                case '/':
                    return 1;  // Higher precedence
                default:
                    return 3;  // Lowest precedence for other characters
            }
        }




        private static void markCellsAsCycle(Set<String> callStack) {
            for (String cellRef : callStack) {
                int cellRefX = cellRef.charAt(0) - 'A';
                int cellRefY = Integer.parseInt(cellRef.substring(1));
                Cell cell = Ex2Sheet.getCell(cellRefX, cellRefY);

                // Mark the cell as ERR_CYCLE_FORM and set its data to "ERROR_CYCLE!"
                cell.setData(Ex2Utils.ERR_CYCLE);
                cell.setComputedValue(Ex2Utils.ERR_CYCLE);
                cell.setType(Ex2Utils.ERR_CYCLE_FORM);

                // Debugging: Print the cell's type and data
                System.out.println("Marked cell " + cellRef + " as ERR_CYCLE_FORM. Type: " + cell.getType() + ", Data: " + cell.getData());
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

        @Override
        public ArrayList<String> getDependencies(String formula) {
            ArrayList<String> dependencies = new ArrayList<>();
            Pattern cellPattern = Pattern.compile("[A-Za-z]+[0-9]+");
            Matcher matcher = cellPattern.matcher(formula);
            while (matcher.find()) {
                dependencies.add(matcher.group());
            }
            return dependencies;
        }

    }
