package com.celements.mailsender;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.script.service.ScriptService;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

public class CelMailScriptServiceTest extends AbstractComponentTest {

  private CelMailScriptService cmss;

  @Before
  public void setUp_CelMailScriptServiceTest() throws Exception {
    cmss = (CelMailScriptService) Utils.getComponent(ScriptService.class, "celmail");
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
    assertFalse(cmss.isValidEmail("a@abc.com b@abc.com"));
    assertFalse(cmss.isValidEmail("a@abc.com,b@abc.com"));
    assertFalse(cmss.isValidEmail("a@abc.com, b@abc.com"));
  }

  @Test
  public void testIsValidEmail_true() {
    assertTrue(cmss.isValidEmail("a@abc.om"));
    assertTrue(cmss.isValidEmail("abc.xyz457@abc.newdomain"));
    assertTrue(cmss.isValidEmail("abc+xyz@abc-\u00E7äöü\u0153.com"));
  }

}
