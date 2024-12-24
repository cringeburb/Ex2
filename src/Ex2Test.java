import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class Ex2Test {
    @Test
    public void testIsNumber() {
        assertTrue(Cell.isNumber("-2.99"));
        assertTrue(Cell.isNumber("2.99"));
        assertTrue(Cell.isNumber("-123213"));
        assertTrue(Cell.isNumber("123.456"));

        assertFalse(Cell.isNumber("-2.99.99"));
        assertFalse(Cell.isNumber("2.99-99"));
        assertFalse(Cell.isNumber("123..456"));
        assertFalse(Cell.isNumber("123.sbc"));
        assertFalse(Cell.isNumber(".123"));
    }
    @Test
    public void testIsForm() {
        assertTrue(Cell.isForm("=-2.99*100"));
        assertTrue(Cell.isForm("=(2)"));
        assertTrue(Cell.isForm("=(2)*9-10+12"));
        assertTrue(Cell.isForm("=((-2))"));

        assertFalse(Cell.isForm("==-2.99*100"));
        assertFalse(Cell.isForm("=)2("));
        assertFalse(Cell.isForm("-990="));
        assertFalse(Cell.isForm("=-2.99*+100"));
    }
}
