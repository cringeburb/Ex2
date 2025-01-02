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
        assertFalse(SCell.isNumber(".123"));
    }
    @Test
    public void testIsForm() {
        assertTrue(SCell.isForm("=-2.99*100"));
        assertTrue(SCell.isForm("=(((((-2.99)))))"));
        assertTrue(SCell.isForm("=(2)"));
        assertTrue(SCell.isForm("=(2)*9-10+12"));
        assertTrue(SCell.isForm("=((-2))"));

        assertFalse(SCell.isForm("==-2.99*100"));
        assertFalse(SCell.isForm("=)2("));
        assertFalse(SCell.isForm("-990="));
        assertFalse(SCell.isForm("=-2.99*+100"));
    }
}
