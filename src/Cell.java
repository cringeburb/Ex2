import java.util.Stack;

public class Cell {
    public static boolean isNumber(String text) {
        if (text.indexOf(".") != text.lastIndexOf('.') || text.indexOf("-") != text.lastIndexOf('-'))
            return false;
        if (text.indexOf("-") != -1 && text.indexOf("-") != 0 || text.indexOf(".") == 0)
            return false;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) != '-' && text.charAt(i) != '.' && Character.isDigit(text.charAt(i)) == false)
                return false;
        }
        return true;
    }

    public static boolean isForm(String text) {
        String operators = "+-*/";
        if (text == null || text.trim().isEmpty()) //checks empty strings or just spaces
            return false;
        int balance = 0;
        if (text.indexOf("=") != text.lastIndexOf('=') || text.indexOf("=") != 0)//an equation cannot have 2 '=' symbols, and it has to be in the beginning of the formula
            return false;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) != '=' && text.charAt(i) != '-' && text.charAt(i) != '.'
                    && Character.isDigit(text.charAt(i)) == false
                    && text.charAt(i) != '+' && text.charAt(i) != '('
                    && text.charAt(i) != ')' && text.charAt(i) != '*'
                    && text.charAt(i) != '/'
                    && Character.isLetter(text.charAt(i))) //checks that all given characters are valid
                return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '(') balance++;
            if (c == ')') balance--;
            if (balance < 0) return false; // Closing parenthesis before opening
        }
        if (balance != 0) return false;// Unmatched parentheses

        for (int i = 1; i < text.length(); i++) {
            // Check if both the current and previous characters are operators
            if (operators.indexOf(text.charAt(i)) != -1 && operators.indexOf(text.charAt(i - 1)) != -1) {
                return false; // Found consecutive operators
            }
        }
        return true;
    }
        public static boolean isText(String text) {
        if(text == null || text.trim().isEmpty())
            return false;
        if(!isNumber(text) && !isForm(text))
            return true;
        return false;
        }
}