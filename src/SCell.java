// Add your documentation below:

public class SCell implements Cell {
    private String line;
    private int type;
    // Add your code here

    public SCell(String s) {
        // Add your code here
        setData(s);
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

    public static boolean isNumber(String text) {
        if(!Character.isDigit(text.charAt(0))&& text.charAt(0)!= '-') {
            return false;
        }
        try { Double.parseDouble(text);
            return true; }
        catch (NumberFormatException e)
        { return false; }
    }

    public static boolean isForm(String form) {
        String operators = "+-*/";
        /*int balance=0;
        if (form == null || form.isEmpty()) {
            return false;
        }
        if(form.length()==1 && Character.isDigit(form.charAt(0))){
            return true;
        }
        // Check if the string starts with '='
        if (form.charAt(0) != '=')
            return false;
        form = form.substring(1);
        if (form.charAt(0) == '(' && form.charAt(form.length() - 1) == ')')
            return isForm('=' + form.substring(1, form.length() -1));
        if (isNumber(form))
            return true;
        for (int i = 0; i < form.length(); i++) {
            if(operators.indexOf(form.charAt(i)) != -1 && operators.indexOf(form.charAt(i+1)) != -1)
                return false;
        }

        int lastop = -1;
        balance = 0;
        for (int i = form.length() - 1; i >= 0; i--) {
            char c = form.charAt(i);
            if (c == ')') {
                balance++;
            } else if (c == '(') {
                balance--;
            } else if (operators.indexOf(c) != -1 && balance == 0) {
                lastop = i;
                break;
            }
        }

        if (lastop == -1) {
            return false;
        }
        // Split the expression and recursively validate both sides
        String left = form.substring(0, lastop);
        String right = form.substring(lastop + 1);
        return isForm('=' + left) && isForm('=' + right);

         */
        int balance = 0;

        if (form == null || form.isEmpty()) {
            return false;
        }

        // Check if the string starts with '='
        if (form.charAt(0) != '=') {
            return false;
        }

        // Remove the '=' at the beginning
        form = form.substring(1);

        // Base case: if the form is a single digit, return true
        if (isNumber(form)) {
            return true;
        }

        // Handle parentheses at the start and end
        if (form.charAt(0) == '(' && form.charAt(form.length() - 1) == ')') {
            return isForm('=' + form.substring(1, form.length() - 1));
        }

        // Check for consecutive operators
        for (int i = 0; i < form.length() - 1; i++) {
            if (operators.indexOf(form.charAt(i)) != -1 && operators.indexOf(form.charAt(i + 1)) != -1) {
                return false;
            }
        }

        int lastop = -1;
        balance = 0;

        // Traverse the expression from right to left to find the main operator
        for (int i = form.length() - 1; i >= 0; i--) {
            char c = form.charAt(i);
            if (c == ')') {
                balance++;
            } else if (c == '(') {
                balance--;
            } else if (operators.indexOf(c) != -1 && balance <= 0) {
                lastop = i;
                break;
            }
        }

        if (lastop == -1) {
            return false;
        }

        // Split the expression and recursively validate both sides
        String left = form.substring(0, lastop);
        String right = form.substring(lastop + 1);
        return isForm('=' + left) && isForm('=' + right);



    }
    public static boolean isText (String text) {
        if(!isForm(text) && !isNumber(text))
            return true;
        return false;
    }
    public static Double computeForms (String form){
        if(isForm(form))
            return computeFormsub(form, 1,form.length()-1);
        else
        return -1.00;
    }
    public static  double computeFormsub(String text ,int start , int end) {
        while(start<=end && text.charAt(start)=='(' && text.charAt(end)==')'){
            start++;
            end--;
        }
       if (isNumber(text.substring(start,end+1 )))
           return Double.parseDouble(text.substring(start,end+1));
       String RHS,LHS;
       int mainopindex = findMainOperator(text , start , end);
       double LHSVAL = computeFormsub(text,start,mainopindex -1);
       double RHSVAL = computeFormsub(text,mainopindex+1,end);
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

}
