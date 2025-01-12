import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Ex2Sheet implements Sheet {
    public static Cell[][] table;

    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                table[i][j] = new SCell();
            }
        }
    }

    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    @Override
    public String value(int x, int y) {
        String ans = Ex2Utils.EMPTY_CELL;

        Cell c = get(x, y);

        if (c != null) {
            if (c.getType() == Ex2Utils.FORM) {
                try {
                    double computedValue = SCell.computeForms(c.getData());
                    ans = String.valueOf(computedValue);
                } catch (Exception e) {
                    ans = Ex2Utils.ERR_FORM;
                }
            } else {

                ans = c.toString();
            }
        }

        return ans;
    }

    @Override
    public Cell get(int x, int y) {
        if(isIn(x, y))
        {return table[x][y];}
        return null;
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
        if (!isIn(x, y)) {
            return;
        }

        // Get or create cell
        Cell cell = get(x, y);
        if (cell == null) {
            cell = new SCell();
            table[x][y] = cell;
        }

        // Set the data first
        cell.setData(s);

        // Update the cell type
        if (s == null || s.trim().isEmpty()) {
            cell.setType(Ex2Utils.TEXT);
        } else if (s.startsWith("=")) {
            cell.setType(Ex2Utils.FORM);
        } else if (SCell.isNumber(s)) {
            cell.setType(Ex2Utils.NUMBER);
        } else {
            cell.setType(Ex2Utils.TEXT);
        }
    }


    @Override
    public void eval() {
        // First, check for circular dependencies
        int[][] depths = depth();

        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                Cell cell = get(x, y);
                String data = cell.getData();

                // Handle empty cells
                if (data == null || data.isEmpty()) {
                    cell.setComputedValue(Ex2Utils.EMPTY_CELL);
                    continue;
                }

                // Update cells with circular dependencies
                if (depths[x][y] == -1) {
                    cell.setData(Ex2Utils.ERR_CYCLE);
                    cell.setType(Ex2Utils.ERR_CYCLE_FORM);
                    cell.setComputedValue(Ex2Utils.ERR_CYCLE);
                    continue;
                }

                // Handle FORM cells
                if (data.startsWith("=")) {
                    // Check if it's a valid formula format
                    if (!SCell.isForm(data)) {
                        cell.setData(Ex2Utils.ERR_FORM);
                        cell.setType(Ex2Utils.ERR_FORM_FORMAT);
                        cell.setComputedValue(Ex2Utils.ERR_FORM);
                        continue;
                    }

                    try {
                        double result = SCell.computeForms(data);
                        if (result == Ex2Utils.ERR_CYCLE_FORM) {
                            cell.setData(Ex2Utils.ERR_CYCLE);
                            cell.setType(Ex2Utils.ERR_CYCLE_FORM);
                            cell.setComputedValue(Ex2Utils.ERR_CYCLE);
                        } else if (result == Ex2Utils.ERR_FORM_FORMAT) {
                            cell.setData(Ex2Utils.ERR_FORM);
                            cell.setType(Ex2Utils.ERR_FORM_FORMAT);
                            cell.setComputedValue(Ex2Utils.ERR_FORM);
                        } else {
                            cell.setComputedValue(String.valueOf(result));
                        }
                    } catch (Exception e) {
                        cell.setData(Ex2Utils.ERR_FORM);
                        cell.setType(Ex2Utils.ERR_FORM_FORMAT);
                        cell.setComputedValue(Ex2Utils.ERR_FORM);
                    }
                    continue;
                }

                // Handle NUMBER and TEXT cells
                cell.setComputedValue(cell.getData());
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
        // Initialize depth matrix with -2 (unprocessed)
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                ans[i][j] = -2;
            }
        }

        // Compute depths for all cells
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (ans[i][j] == -2) {
                    depthCount(i, j, ans, new HashSet<>());
                }
            }
        }
        return ans;
    }


    private int depthCount(int x, int y, int[][] ans, Set<String> visited) {
        // If already calculated, return the cached value
        if (ans[x][y] != -2) {
            return ans[x][y];
        }

        Cell cell = get(x, y);
        String data = cell.getData();

        // Handle empty cells or null data
        if (cell == null || data == null || data.isEmpty()) {
            ans[x][y] = 0;
            return 0;
        }

        // If it's not a formula, depth is 0
        if (!data.startsWith("=")) {
            ans[x][y] = 0;
            return 0;
        }

        // Create cell identifier
        String currentCell = x + "," + y;

        // Check for circular dependency
        if (visited.contains(currentCell)) {
            ans[x][y] = -1; // Circular dependency
            return -1;
        }

        // Add current cell to visited set
        visited.add(currentCell);

        // Extract cell references
        String formula = data.substring(1).trim();
        Pattern cellPattern = Pattern.compile("[A-Z][0-9]+");
        Matcher matcher = cellPattern.matcher(formula);

        // If no cell references found, it's a constant formula (like "=10+2")
        if (!matcher.find()) {
            ans[x][y] = 0;
            visited.remove(currentCell);
            return 0;
        }

        // Reset matcher to start
        matcher.reset();

        int maxDepth = -1;
        // Process each dependency
        while (matcher.find()) {
            String cellRef = matcher.group();
            int refX = cellRef.charAt(0) - 'A';
            int refY = Integer.parseInt(cellRef.substring(1));

            // Skip invalid references
            if (!isIn(refX, refY)) {
                continue;
            }

            int depthOfDependency = depthCount(refX, refY, ans, visited);

            // Propagate circular dependency
            if (depthOfDependency == -1) {
                ans[x][y] = -1;
                visited.remove(currentCell);
                return -1;
            }

            maxDepth = Math.max(maxDepth, depthOfDependency);
        }

        // Remove current cell from visited set
        visited.remove(currentCell);

        // Set depth as max depth of dependencies + 1
        ans[x][y] = maxDepth + 1;
        return ans[x][y];
    }
    private int calculateDepth(int x, int y, int[][] depths, boolean[][] visited) {
        // If already calculated, return the depth
        if (depths[x][y] >= -1) {
            return depths[x][y];
        }

        // Check for circular dependency
        if (visited[x][y]) {
            depths[x][y] = -1;
            return -1;
        }

        Cell cell = table[x][y];
        String data = cell.getData();

        // If empty or not a formula, depth is 0
        if (data == null || data.isEmpty() || !data.startsWith("=")) {
            depths[x][y] = 0;
            return 0;
        }

        visited[x][y] = true;
        int maxDepth = 0;

        // Find all cell references in the formula
        Pattern pattern = Pattern.compile("[A-Z][0-9]+");
        Matcher matcher = pattern.matcher(data);

        while (matcher.find()) {
            String ref = matcher.group();
            int col = ref.charAt(0) - 'A';
            int row = Integer.parseInt(ref.substring(1));

            if (isIn(col, row)) {
                int depthOfDependency = calculateDepth(col, row, depths, visited);
                if (depthOfDependency == -1) {
                    depths[x][y] = -1;
                    visited[x][y] = false;
                    return -1;
                }
                maxDepth = Math.max(maxDepth, depthOfDependency);
            }
        }

        visited[x][y] = false;
        depths[x][y] = maxDepth + 1;
        return depths[x][y];
    }

    @Override
    public void load(String fileName) throws IOException {
        // Clear table
        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                set(x, y, ""); // Reset all cells to empty
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Skip the header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }


                String[] parts = line.split(",");
                if (parts.length < 3) {
                    continue;
                }

                try {

                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);

                    // Parse the cell data
                    String data = parts[2];

                    // Set the cell data if the coordinates are valid
                    if (isIn(x, y)) {
                        set(x, y, data);
                    }
                } catch (NumberFormatException e) {
                    // Skip lines with invalid coordinates
                    continue;
                }
            }
        }
        eval();

    }

    @Override
    public void save(String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // Write the header line
            writer.write("I2CS ArielU: SpreadSheet (Ex2) assignment");
            writer.newLine();
            for (int x = 0; x < width(); x++) {
                for (int y = 0; y < height(); y++) {
                    Cell cell = get(x, y);
                    String data = cell.getData();
                    // Skip empty cells
                    if (data == null || data.isEmpty()) {
                        continue;
                    }
                    writer.write(x + "," + y + "," + data);
                    writer.newLine();
                }
            }
        }
    }

    @Override
    public String eval(int x, int y) {
        Cell cell = get(x, y);
        if (cell == null) {
            return Ex2Utils.EMPTY_CELL;
        }

        String data = cell.getData();
        if (data == null || data.isEmpty()) {
            return Ex2Utils.EMPTY_CELL;
        }

        // For formula cells
        if (data.startsWith("=")) {
            // First check if it's a valid formula format
            if (!SCell.isForm(data)) {
                cell.setType(Ex2Utils.ERR_FORM_FORMAT);
                return Ex2Utils.ERR_FORM;
            }

            try {
                // Check for circular dependencies
                int[][] depths = depth();
                if (depths[x][y] == -1) {
                    cell.setType(Ex2Utils.ERR_CYCLE_FORM);
                    return Ex2Utils.ERR_CYCLE;
                }

                Double result = SCell.computeForms(data);
                if (result == Ex2Utils.ERR_FORM_FORMAT) {
                    cell.setType(Ex2Utils.ERR_FORM_FORMAT);
                    return Ex2Utils.ERR_FORM;
                }
                return String.valueOf(result);
            } catch (Exception e) {
                cell.setType(Ex2Utils.ERR_FORM_FORMAT);
                return Ex2Utils.ERR_FORM;
            }
        }

        // For non-formula cells
        return data;
    }

    public  String getData (String s){
        int x = s.charAt(0)- 'A';
        int y = Integer.parseInt(s.substring(1,s.length()-1));
        return table[x][y].getData();
    }
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
    }
    private void clearDependencies(int x, int y) {
        // Remove (x, y) from the dependency graph
    }
    public String getCellName(int x, int y) {
        return x - 'A' + String.valueOf(y);
    }
}
