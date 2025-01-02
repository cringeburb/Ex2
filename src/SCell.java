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
    public static boolean isForm(String form) {
        String operators = "+-*/";
        int balance=0;
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
    }

}
