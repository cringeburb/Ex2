import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
// Add your documentation below:

public class Ex2Sheet implements Sheet {
    private Cell[][] table;


    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
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

        // Retrieve the cell
        Cell c = get(x, y);

        if (c != null) {
            // Check the cell type
            if (c.getType() == 3) { //if c's data is a formula
                try {
                    double computedValue = SCell.computeForms(c.getData());
                    ans = String.valueOf(computedValue);
                } catch (Exception e) {
                    ans = "#ERROR"; // Handle formula evaluation failure
                }
            } else {
                // For non-formula cells, return the cell's string representation
                ans = c.toString();
            }
        }

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
        if (!isIn(x, y)) return;

        Cell cell = get(x, y);
        if (cell == null) {
            cell = new SCell();
            table[x][y] = cell;
        }

        // Determine the type of the input
        if (table[x][y].getData().startsWith("=")) { // It's a formula
            cell.setType(Ex2Utils.FORM);
            cell.setData(table[x][y].getData()); // Set the raw formula
        } else if (SCell.isNumber(table[x][y].getData())) { // It's a number
            cell.setType(Ex2Utils.NUMBER);
            cell.setData(table[x][y].getData());
        } else { // It's invalid or a plain string
            cell.setType(Ex2Utils.ERR);
            cell.setData(table[x][y].getData());
        }

        eval(); // Re-evaluate after setting a cell


    }

    @Override
    public void eval() {
        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                Cell cell = get(x, y);
                if (cell.getType() == Ex2Utils.FORM) {
                    String formula = cell.getData().substring(1); // Remove '='
                    try {
                        double result = Double.parseDouble(eval(x,y)); // Implement this method
                        cell.setComputedValue(String.valueOf(result));
                    } catch (Exception e) {
                        cell.setComputedValue("#ERR_FORM");
                        cell.setType(Ex2Utils.ERR_FORM_FORMAT);
                    }
                }
                else if (cell.getType() == Ex2Utils.NUMBER) {
                    cell.setComputedValue(cell.getData());
                }
            }
        }
    }

    @Override
    public boolean isIn(int xx, int yy) {
        boolean ans = xx >= 0 && yy >= 0;
        ans = xx <= width() && yy <= height();
        return ans;
    }


    @Override
    public int[][] depth() {
        int[][] ans = new int[width()][height()];

        // Initialize depth matrix with -1
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                ans[i][j] = -1;
            }
        }

        // Compute depths for all cells
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (ans[i][j] == -1) { // Only compute if not already calculated
                    ans[i][j] = depthCount(i, j, "");
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

    // a function where I assume the formula has a cell in it ,so I can calculate the depth
    private int depthCount(int x, int y, String path) {
        // Check bounds
        if (x < 0 || x >= table.length || y < 0 || y >= table[0].length) {
            throw new IllegalArgumentException("Invalid cell coordinates: (" + x + ", " + y + ")");
        }

        // Check for circular dependency
        if (path.contains("[" + x + "," + y + "]")) {
            return -1; // Circular dependency detected
        }

        String cell = table[x][y].getData();
        if (!SCell.isForm(cell)) {
            return 0; // Non-formula cells have depth 0
        }

        // Add current cell to path
        path += "[" + x + "," + y + "]";

        // Parse formula and calculate depth
        ArrayList<String> dependencies = SCell.getDependencies(cell); // Adjust based on how dependencies are extracted
        int maxDepth = 0;
        for (String dependency : dependencies) {
            int refX = dependency.charAt(0) - 'A';
            int refY = Integer.parseInt(dependency.substring(1));
            int dependencyDepth = depthCount(refX, refY, path);
            if (dependencyDepth == -1) {
                return -1; // Propagate circular dependency signal
            }
            maxDepth = Math.max(maxDepth, dependencyDepth);
        }

        return maxDepth + 1; // Depth is 1 + max depth of dependencies
    }
}
