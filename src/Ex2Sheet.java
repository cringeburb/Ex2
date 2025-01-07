import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;


public class Ex2Sheet implements Sheet {
    public static Cell[][] table;


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
        SCell a = new SCell(c);
        if (c != null) {
            // Check the cell type
            if (c.getType() == 3) { //if c's data is a formula
                try {
                    double computedValue = a.computeForms();
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

        // Update the cell's data and type based on the input string
        cell.setData(s);
        SCell a = new SCell(cell);
        if (s.startsWith("=")) { // It's a formula
            cell.setType(Ex2Utils.FORM);
            // Update dependencies for the formula
            updateDependencies(x, y, s);
        } else if (SCell.isNumber(s)) { // It's a number
            cell.setType(Ex2Utils.NUMBER);
            clearDependencies(x, y); // Clear dependencies since it's not a formula
        } else { // It's an invalid string
            cell.setType(Ex2Utils.ERR);
            clearDependencies(x, y); // Clear dependencies for invalid input
        }
        if( a.computeForms() == Ex2Utils.ERR_CYCLE_FORM)
            cell.setType(Ex2Utils.ERR_CYCLE_FORM);


        eval(); // Re-evaluate all cells to ensure correctness


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
                        cell.setComputedValue(Ex2Utils.ERR_FORM);
                        cell.setType(Ex2Utils.ERR_FORM_FORMAT);
                    }
                }
                else if (cell.getType() == Ex2Utils.NUMBER) {
                    cell.setComputedValue(cell.getData());
                }
                else if (cell.getType() == Ex2Utils.ERR_CYCLE_FORM) {
                    cell.setComputedValue(Ex2Utils.ERR_CYCLE);
                    cell.setType(Ex2Utils.ERR_CYCLE_FORM);
                }
                CellEntry cellEntry = new CellEntry(x,y,cell.getData());
            }
        }
    }

    @Override
    public boolean isIn(int xx, int yy) {
        return xx >= 0 && xx < width() && yy >= 0 && yy < height();
    }


    @Override
    public int[][] depth() {
        int[][] ans = new int[width()][height()];

        // Initialize depth matrix with -1 (uncalculated)
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                ans[i][j] = -1;
            }
        }

        // Compute depths for all cells
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (ans[i][j] == -1) { // Compute depth only if not calculated
                    ans[i][j] = depthCount(i, j, "");
                }
            }
        }

        return ans;
    }


    @Override
    public void load(String fileName) throws IOException {
        // clean table
        for(int i=0; i<width(); i++) {
            for(int j=0; j<height(); j++) {
                // reset the original formula + the current data
                table[i][j].setData("");
                table[i][j].getData();
                table[i][j].setType(0);
            }
        }
        //load file
        File file = new File(fileName);
        Scanner scanner = new Scanner(file);
        ArrayList<String> lines = new ArrayList<String>();
        String[] splitLine;
        String form;
        int x,y;
        while(scanner.hasNextLine()) {
            lines.add(scanner.nextLine());
        }
        scanner.close();
        for(int i = 1; i<lines.size(); i++) {
            splitLine = lines.get(i).split(",");
            if(splitLine.length>=3) {
                try {
                    x = Integer.parseInt(splitLine[0]);
                    y = Integer.parseInt(splitLine[1]);
                    SCell currentcell = (SCell) table[x][y];
                    form = splitLine[2];
                    CellEntry cellEntry = new CellEntry(x,y);
                    if(cellEntry.isValid() && isIn(x,y)) {
                        table[x][y].setData(form);
                        table[x][y].getData();
                        int type = currentcell.updatetype();
                        currentcell.setType(type);
                    }
                } catch (NumberFormatException e) {

                }
            }
        }
        eval();

    }

    @Override
    public void save(String fileName) throws IOException {

    }

    @Override
    public String eval(int x, int y) {
        SCell s = new SCell(table[x][y]);
        // Use a Set to track visited cells in the current evaluation chain
        Set<Cell> visitedCells = new HashSet<>();

        // Assuming you get the cell from some data structure (like a 2D array or Map)
        Cell currentCell = getCell(x, y);

        // If the cell is already in the visited set, it means a circular reference exists
        if (visitedCells.contains(currentCell)) {
            // Set the cell's computed value to indicate a circular reference
            currentCell.setComputedValue(Ex2Utils.ERR_CYCLE);
            return Ex2Utils.ERR_CYCLE;  // Return the error message for circular reference
        }

        // Add the current cell to the visited set to track it during the current evaluation chain
        visitedCells.add(currentCell);

        // The normal formula evaluation logic goes here
        try {
            String formula = currentCell.getFormula();

            // Now call computeForm() or similar methods for evaluation, passing visitedCells to prevent circular references
            double result = s.computeForms();  // Your existing formula computation logic

            // If no errors, set the computed value of the cell
            currentCell.setComputedValue(String.valueOf(result));

            // Return the computed value as a string
            return String.valueOf(result);
        } catch (Exception e) {
            // If an error occurs (like invalid formula or division by zero), set the cell's computed value to error
            currentCell.setComputedValue(Ex2Utils.ERR_FORM);  // Invalid formula error
            return Ex2Utils.ERR_FORM;  // Return the error message
        } finally {
            // Remove the cell from the visited set once the evaluation is done
            visitedCells.remove(currentCell);
        }
    }

    // a function where I assume the formula has a cell in it ,so I can calculate the depth
    private int depthCount(int x, int y, String path) {
        // Detect circular dependencies
        String currentCell = "[" + x + "," + y + "]";
        if (path.contains(currentCell)) {
            System.out.println("Circular dependency detected at: " + currentCell);
            return Ex2Utils.ERR_CYCLE_FORM ;//// Circular dependency detected
        }
        // Validate coordinates
        if (x < 0 || x >= this.table.length || y < 0 || y >= this.table[0].length) {
            throw new IllegalArgumentException("Invalid cell coordinates: (" + x + ", " + y + ")");
        }


        // Get the cell
        SCell cell = (SCell) this.table[x][y];
        if (cell == null || !SCell.isForm(cell.getData())) {
            return 0; // Non-formula cells have depth 0
        }
        currentCell = "[" + x + "," + y + "]";
        if (path.contains(currentCell)) {
            System.out.println("Circular dependency detected at: " + currentCell);
            return Ex2Utils.ERR_CYCLE_FORM;//// Circular dependency detected
        }
        // Add current cell to path
        path += "->" + currentCell;

        // Parse dependencies
        ArrayList<String> dependencies = cell.getDependencies(cell.getData());
        int maxDepth = 0;
        for (String dependency : dependencies) {
            // Convert dependency to coordinates
            int refX = dependency.charAt(0) - 'A'; // Column index
            int refY = Integer.parseInt(dependency.substring(1)); // Row index

            // Recursively calculate depth
            int dependencyDepth = depthCount(refX, refY, path);
            if (dependencyDepth == -1) {
                return -1; // Propagate circular dependency
            }
            maxDepth = Math.max(maxDepth, dependencyDepth);
        }

        return maxDepth + 1; // Depth is 1 + max depth of dependencies
    }

    public  String getData (String s){
        int x = s.charAt(0)- 'A';
        int y = Integer.parseInt(s.substring(1,s.length()-1));
        return table[x][y].getData();
    }
    //Same method just for the coordinates instead of the strings
    public  String getData (int x , int y){
        return table[x][y].getData();
    }
    public static Cell getCell (int x , int y){
        return table[x][y];
    }
    private void updateDependencies(int x, int y, String formula) {
        SCell cell = new SCell(table[x][y]);
        // Parse dependencies from the formula
        ArrayList<String> dependencies = cell.getDependencies(formula);

        // Add logic to update dependency graph here
        // For example, map (x, y) to its dependencies in a data structure
    }
    private void clearDependencies(int x, int y) {
        // Remove (x, y) from the dependency graph
    }
    public String getCellName(int x, int y) {
        return x - 'A' + String.valueOf(y);
    }

}
