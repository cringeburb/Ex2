import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Ex2Test {


    @Test
    public void testIsNumber() {
        assertTrue(SCell.isNumber("-2.99"));
        assertTrue(SCell.isNumber("2.99"));
        assertTrue(SCell.isNumber("-123213"));
        assertTrue(SCell.isNumber("123.456"));

        assertFalse(SCell.isNumber("-2.99.99"));
        assertFalse(SCell.isNumber("2.99-99"));
        assertFalse(SCell.isNumber("123..456"));
        assertFalse(SCell.isNumber("123.sbc"));
    }

    @Test
    public void testIsForm() {
        assertTrue(SCell.isForm("=-2.99*100"));
        assertTrue(SCell.isForm("=(((((-2.99)))))"));
        assertTrue(SCell.isForm("=(2)"));
        assertTrue(SCell.isForm("=(2)*9-10+12"));
        assertTrue(SCell.isForm("=((-2))"));
        assertTrue(SCell.isForm("=(2+5)*7"));
        assertTrue(SCell.isForm("=(2+(5*(5-1)))*2+(3*(1/2))*12"));
        assertTrue(SCell.isForm("=A0 + B0 * 2"));

        assertFalse(SCell.isForm("==-2.99*100"));
        assertFalse(SCell.isForm("=)2("));
        assertFalse(SCell.isForm("-990="));
        assertFalse(SCell.isForm("=-2.99*+100"));
    }

    @Test
    public void testCompute() {
        SCell s = new SCell() ;
        s.setFormula("=(2+5)*7");
        assertEquals(49.0, s.computeForms());
        s.setFormula("=(((2+5)*7)-4)/2");
        assertEquals(22.5, s.computeForms());
        s.setFormula("=(2)");
        assertEquals(Ex2Utils.ERR_FORM_FORMAT, s.computeForms());
        s.setFormula("=(2)*9-10+12");
        assertEquals(-16, s.computeForms());
        s.setFormula("=(2+(5*(5-1)))*2+(3*(1/2))*12");
        assertEquals(62, s.computeForms());
    }

    @Test
    public void testDepth() {
        Ex2Sheet cells = new Ex2Sheet(3, 3);
        cells.set(0, 0, "10");        // A0 (0,0) is a constant
        cells.set(1, 0, "=A0*2");     // B0 (1,0) depends on A0
        cells.set(1, 1, "=B0+6");// B1 (1,1) is reassigned to depend on B0
        cells.set(2, 2, "=C2+2");

        int[][] result = cells.depth();

        assertEquals(0, result[0][0]); // A0 has depth 0 (no dependencies)
        assertEquals(1, result[1][0]); // B0 has depth 1 (depends on A0)
        assertEquals(2, result[1][1]); // B1 has depth 2 (depends on B0 -> A0)
        assertEquals(-1, result[2][2]);
    }

    @Test
    public void testEval() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);

        // Test 1: Constant value
        sheet.set(0, 0, "=10"); // A0 = 10
        assertEquals(10.0, Double.valueOf(sheet.eval(0, 0))); // Check constant value

        // Test 2: Simple formula
        sheet.set(1, 1, "=A0 + 5"); // B1 = A0 + 5
        assertEquals(15.0, Double.valueOf(sheet.eval(1, 1))); // Check formula result

        // Test 3: Multiple dependencies
        sheet.set(1, 0, "=A0 * 2"); // B0 = A0 * 2
        sheet.set(1, 1, "=B0 + 5"); // B1 = B0 + 5
        assertEquals(20.0, Double.valueOf(sheet.eval(1, 0))); // B0 = 20
        assertEquals(25.0, Double.valueOf(sheet.eval(1, 1))); // B1 = 25

        // Test 4: Circular dependency
        sheet.set(0, 0, "=B1"); // A0 = B1
        sheet.set(1, 1, "=A0"); // B1 = A0 (circular dependency)
        assertEquals("ERR_CYCLE!", sheet.eval(0, 0)); // A0 circular dependency
        assertEquals("ERR_CYCLE!", sheet.eval(1, 1)); // B1 circular dependency
        // Test 5: Invalid formula
        sheet.set(0, 0, "=//"); // Invalid formula
       assertEquals("ERR_FORM!",sheet.eval(0,0));

        // Test 6: Empty cell
       assertEquals(Ex2Utils.ERR_FORM, sheet.eval(2,2));

        // Test 7: Parentheses in formula
        sheet.set(2, 0, "=10");           // A0 = 10
        sheet.set(1, 0, "=C0*(2+3)"); // B0 = A0 * (2 + 3)
        assertEquals(50.0, Double.valueOf(sheet.eval(1, 0))); // B0 = 10 * 5 = 50

        // Test 8: Mixed references
        sheet.set(0, 0, "=5");            // A0 = 5
        sheet.set(1, 0, "=7");            // B0 = 7
        sheet.set(1, 1, "=A0 + B0 * 2");  // B1 = A0 + B0 * 2
        assertEquals("19.0", sheet.eval(1, 1)); // B1 = 5 + 7 * 2 = 19

        // Test 9: Reassignment
        sheet.set(0, 0, "10");       // A0 = 10
        sheet.set(1, 1, "=A0 + 5");  // B1 = A0 + 5
        sheet.set(0, 0, "20");       // A0 reassigned to 20
        assertEquals(25.0, Double.valueOf(sheet.eval(1, 1))); // B1 should update to A0 + 5 = 25
    }

    @Test
    void testInitialization() {
        // Initialize a 3x3 sheet
        Ex2Sheet sheet = new Ex2Sheet(3, 3);

        // Assert dimensions
        assertEquals(3, sheet.width());
        assertEquals(3, sheet.height());

        // Assert all cells are initialized to empty
        for (int i = 0; i < sheet.width(); i++) {
            for (int j = 0; j < sheet.height(); j++) {
                assertEquals("", sheet.get(i, j).getData());
            }
        }
    }

}
