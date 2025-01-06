import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
// Add your documentation below:

public class Ex2Sheet implements Sheet {
    private Cell[][] table;

    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for (int i = 0; i < x; i = i + 1) {
            for (int j = 0; j < y; j = j + 1) {
                table[i][j] = new SCell("");
            }
        }

    }

    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    @Override
    public String value(int x, int y) {
        String ans = Ex2Utils.EMPTY_CELL;
        // Add your code here

        Cell c = get(x, y);
        if (c != null) {
            ans = c.toString();
        }

        /////////////////////
        return ans;
    }

    @Override
    public Cell get(int x, int y) {
        return table[x][y];
    }

    @Override
    public Cell get(String cords) {
        Cell ans = null;

        return ans;
    }

    @Override
    public int width() {
        return table.length;
    }

    @Override
    public int height() {
        return table[0].length;
    }

    @Override
    public void set(int x, int y, String s) {
        SCell c = new SCell(s);
        table[x][y] = c;


    }

    @Override
    public void eval() {
        int[][] dd = depth();
        // Add your code here

        // ///////////////////
    }

    @Override
    public boolean isIn(int xx, int yy) {
        boolean ans = xx >= 0 && yy >= 0;
        ans = xx <= width() && yy <= height();
        return ans;
    }

    @Override
    public int[][] depth() {
        int depth = 0;
        int count = 0;
        int max = width() * height();
        boolean flagC = true;
        int[][] ans = new int[width()][height()];
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                ans[i][j] = -1;
            }
        }
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (SCell.isNumber(this.table[i][j].getData()) || SCell.isText(this.table[i][j].getData()) || canbecomputednow(i, j))
                    ans[i][j] = 0;
                else {
                    depthCount(i, j, getCellName(i, j));
                }
            }
        }

        return ans;
    }

    @Override
    public void load(String fileName) throws IOException {

    }

    @Override
    public void save(String fileName) throws IOException {
        // Add your code here

        /////////////////////
    }

    @Override
    public String eval(int x, int y) {
        String ans = null;
        ans = String.valueOf(SCell.computeForms(SCell.getData(x,y)));
        return ans;
    }

    public boolean canbecomputednow(int x, int y) {
        String form = this.table[x][y].getData();
        if (!SCell.isForm(form)) {
            return false;
        }
        for (int i = 0; i < form.length(); i++) {
            if (Character.isLetter(form.charAt(i)))
                return false;
        }
        return true;
    }

    // a function where I assume the formula has a cell in it ,so I can calculate the depth
    public int depthCount(int x, int y, String visitedPath) {
        String data = table[x][y].getData();

        // If the cell contains text or a number, depth is 0
        if (SCell.isText(data) || SCell.isNumber(data)) {
            return 0;
        }

        // Check for self-referencing or circular references
        String currentCell = getCellName(x, y);
        if (visitedPath.contains("," + currentCell + ",")) {
            throw new IllegalArgumentException("Circular reference detected at cell: " + currentCell);

        }

        // Add the current cell to the visited path
        visitedPath += "," + currentCell + ",";

        // Regular expression to find cell references in the formula
        Pattern cellPattern = Pattern.compile("[A-Z]+[0-9]+");
        Matcher matcher = cellPattern.matcher(data);

        int maxDepth = 0;
        while (matcher.find()) {
            String cellRef = matcher.group(); // Extract the cell reference

            // Convert cell reference to coordinates (x, y)
            int refX = cellRef.charAt(0) - 'A'; // Column as 0-based index
            int refY = Integer.parseInt(cellRef.substring(1)) - 1; // Row as 0-based index

            // Recursively calculate the depth of the referenced cell
            maxDepth = Math.max(maxDepth, 1 + depthCount(refX, refY, visitedPath));
        }
        return maxDepth;
    }

    // Helper function to get the cell name from coordinates
    private String getCellName(int x, int y) {
        char column = (char) ('A' + x);
        int row = y + 1;
        return column + Integer.toString(row);
    }


}
