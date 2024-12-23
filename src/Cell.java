public class Cell {
    public static boolean isNumber(String text){
        if(text.indexOf(".") != text.lastIndexOf('.') || text.indexOf("-") != text.lastIndexOf('-'))
            return false;
        if(text.indexOf("-") != -1 && text.indexOf("-" ) != 0 || text.indexOf(".") == 0)
            return false;
        for (int i = 0; i < text.length(); i++) {
            if(text.charAt(i) != '-' && text.charAt(i) != '.' && Character.isDigit(text.charAt(i)) == false)
                return false;
        }
        return true;
    }

}
