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
        assertTrue(SCell.isForm("=(2+5)*7"));
        assertTrue(SCell.isForm("=(2+(5*(5-1)))*2+(3*(1/2))*12"));
        assertFalse(SCell.isForm("==-2.99*100"));
        assertFalse(SCell.isForm("=)2("));
        assertFalse(SCell.isForm("-990="));
        assertFalse(SCell.isForm("=-2.99*+100"));
    }
    @Test
    public void testCompute(){
    assertEquals(49.0,SCell.computeForms("=(2+5)*7"));
    assertEquals(22.5,SCell.computeForms("=(((2+5)*7)-4)/2"));
    assertEquals(-1,SCell.computeForms("(-2)"));
    assertEquals(-16,SCell.computeForms("=(-2)*9-10+12"));
    assertEquals(62,SCell.computeForms("=(2+(5*(5-1)))*2+(3*(1/2))*12"));
    }
    @Test
    public void testDepth(){
         Sheet cells = new Ex2Sheet(3, 3) ;
         cells.set(0,0,"10");
         cells.set(0,1,"=A1+5");
         cells.set(1,0,"=A1*2");
         int[][] result = cells.depth();
        assertEquals(0,result[0][0]);
        assertEquals(1,result[0][1]);
        assertEquals(1,result[1][0]);
    }
    @Test
    public void testEval (){

    }
}
