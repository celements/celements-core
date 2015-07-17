import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.mailsender.CelMailScriptService;


public class CelMailScriptServiceTest {
  
  private CelMailScriptService cmss;
  
  @Before
  public void setUp() throws Exception {
    cmss = new CelMailScriptService();
  }

  @Test
  public void testIsValidEmail_false() {
    assertFalse(cmss.isValidEmail(""));
    assertFalse(cmss.isValidEmail("   "));
    assertFalse(cmss.isValidEmail("  @     "));
    assertFalse(cmss.isValidEmail("a\t@abc.com"));
    assertFalse(cmss.isValidEmail("a@\nabc.com"));
    assertFalse(cmss.isValidEmail("a b@abc.com"));
    assertFalse(cmss.isValidEmail("a@abc.com com"));
    assertFalse(cmss.isValidEmail("abc.com"));
  }

  @Test
  public void testIsValidEmail_true() {
    assertTrue(cmss.isValidEmail("a@abc.om"));
    assertTrue(cmss.isValidEmail("abc.xyz457@abc.newdomain"));
    assertTrue(cmss.isValidEmail("abc+xyz@abc-\u00E7äöü\u0153.com"));
  }

}
