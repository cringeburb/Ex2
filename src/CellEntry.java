/**
 * This class represents a cell entry in a spreadsheet, handling the conversion and validation
 * of cell coordinates (like "A0", "B1", etc.).
 * Last modified: 2025-01-12
 * @author cringeburb
 */
public class CellEntry implements Index2D {
    private String data;
    private int x;
    private int y;

    /**
     * Constructs a CellEntry with coordinates and data
     * @param x column index (0 for A, 1 for B, etc.)
     * @param y row index
     * @param data cell content
     */
    public CellEntry(int x, int y, String data) {
        this.x = x;
        this.y = y;
        this.data = data;
    }

    /**
     * Constructs an empty CellEntry with coordinates
     * @param x column index (0 for A, 1 for B, etc.)
     * @param y row index
     */
    public CellEntry(int x, int y) {
        this(x, y, null);
    }

    /**
     * Validates if the cell reference is in correct format
     * Valid format examples: "A0", "B1", "Z9"
     * @return true if the cell reference is valid
     */
    @Override
    public boolean isValid() {
        if (data == null) return false;

        // Check length (should be 2 or 3 characters)
        if (data.length() < 2 || data.length() > 3) return false;

        // First character should be a letter A-Z
        if (!Character.isUpperCase(data.charAt(0))) return false;

        // Remaining characters should be digits
        for (int i = 1; i < data.length(); i++) {
            if (!Character.isDigit(data.charAt(i))) return false;
        }

        return true;
    }

    /**
     * Gets the column index of the cell
     * @return column index (0 for A, 1 for B, etc.) or ERR if invalid
     */
    @Override
    public int getX() {
        if (isValid()) {
            return data.charAt(0) - 'A';
        }
        return Ex2Utils.ERR;
    }

    /**
     * Gets the row index of the cell
     * @return row index or ERR if invalid
     */
    @Override
    public int getY() {
        if (isValid()) {
            return Integer.parseInt(data.substring(1));
        }
        return Ex2Utils.ERR;
    }

    /**
     * Sets the cell's data content
     * @param data the content to set
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Converts a column index to its letter representation
     * @param x column index (0 for A, 1 for B, etc.)
     * @return the column letter or null if invalid
     */
    public static String convertX(int x) {
        if (x >= 0 && x < 26) {
            return String.valueOf((char)('A' + x));
        }
        return null;
    }

    /**
     * Converts a row index to its string representation
     * @param y row index
     * @return the row number as a string
     */
    public static String convertY(int y) {
        if (y >= 0) {
            return String.valueOf(y);
        }
        return null;
    }

    /**
     * Gets the cell's reference in spreadsheet notation (e.g., "A0", "B1")
     * @return the cell reference string
     */
    public String getCellReference() {
        return convertX(x) + convertY(y);
    }

    /**
     * Gets the raw data stored in the cell
     * @return the cell's data
     */
    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return getCellReference() + (data != null ? data : "");
    }
}