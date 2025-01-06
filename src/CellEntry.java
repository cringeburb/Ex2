// Add your documentation below:

public class CellEntry  implements Index2D {
    private String data;
    private String CellIndex;
    @Override
    public boolean isValid()
    {if(data.length()>3) //a cells name length can be 3 characters long utmost.
        return false;
        if(!Character.isDigit(data.charAt(0))&& !Character.isDigit(data.charAt(1))&& !Character.isDigit(data.charAt(2)) )
            return false;
        return true;
    }
    public CellEntry (int x , int y , String data){
        this.data = data;
        this.CellIndex = convertX(x) + convertY(y);
    }
    public CellEntry (int x , int y ){
        this.data = null;
        this.CellIndex = convertX(x) + convertY(y);
    }
    @Override
    public int getX() {
        if(isValid())
            return Integer.parseInt(data.charAt(0) + "");
        return Ex2Utils.ERR;}

    @Override
    public int getY() {
        if(isValid())
            return Integer.parseInt(data.substring(1,data.length()-1));
        return Ex2Utils.ERR;}
    public void setData(String data){
        this.data = data;
    }
    //return the string of the y value of a given cell
    public static String convertX (int x){
        if(x>=0 && x<=26)
            return Character.toString((char)'A' + x);//if x is larger than 10 then the returned String will be 'A' and B=11 and so on.
        return null;
    }
    //return the string of the y value of a given cell
    public static String convertY (int y){
        return String.valueOf(Character.toChars(y));
    }
}
