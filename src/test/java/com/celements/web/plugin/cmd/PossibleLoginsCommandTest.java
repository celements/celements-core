package com.celements.web.plugin.cmd;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;

public class PossibleLoginsCommandTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private PossibleLoginsCommand possibleLoginsCmd;

  @Before
  public void setUp_PossibleLoginsCommandTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    possibleLoginsCmd = new PossibleLoginsCommand();
  }

  @Test
  public void testGetPossibleLogins_none() {
    expect(xwiki.getXWikiPreference(eq("cellogin"), eq("celements.login.userfields"),
        eq("loginname"), same(context))).andReturn(null).anyTimes();
    replayDefault();
    assertEquals("loginname", possibleLoginsCmd.getPossibleLogins());
    verifyDefault();
  }
  
  @Test
  public void testGetPossibleLogins_local() {
    expect(xwiki.getXWikiPreference(eq("cellogin"), eq("celements.login.userfields"),
        eq("loginname"), same(getContext()))).andReturn("a,b").once();
    replayDefault();
    assertEquals("a,b", possibleLoginsCmd.getPossibleLogins());
    verifyDefault();
  }
  
  @Test
  public void testGetPossibleLogins_whiteSpaces() {
    expect(xwiki.getXWikiPreference(eq("cellogin"), eq("celements.login.userfields"),
        eq("loginname"), same(context))).andReturn("   ").once();
    replayDefault();
    assertEquals("loginname", possibleLoginsCmd.getPossibleLogins());
    verifyDefault();
  }

}
