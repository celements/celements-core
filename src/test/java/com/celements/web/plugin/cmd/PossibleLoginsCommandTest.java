package com.celements.web.plugin.cmd;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.auth.user.UserService;
import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;

public class PossibleLoginsCommandTest extends AbstractComponentTest {

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
    expect(xwiki.getXWikiPreference(eq("cellogin"), eq("celements.login.userfields"), eq(
        UserService.DEFAULT_LOGIN_FIELD), same(context))).andReturn(null).anyTimes();
    replayDefault();
    assertEquals(UserService.DEFAULT_LOGIN_FIELD, possibleLoginsCmd.getPossibleLogins());
    verifyDefault();
  }

  @Test
  public void testGetPossibleLogins_local() {
    expect(xwiki.getXWikiPreference(eq("cellogin"), eq("celements.login.userfields"), eq(
        UserService.DEFAULT_LOGIN_FIELD), same(getContext()))).andReturn("email,loginname").once();
    replayDefault();
    assertEquals("email,loginname", possibleLoginsCmd.getPossibleLogins());
    verifyDefault();
  }

  @Test
  public void testGetPossibleLogins_whiteSpaces() {
    expect(xwiki.getXWikiPreference(eq("cellogin"), eq("celements.login.userfields"), eq(
        UserService.DEFAULT_LOGIN_FIELD), same(context))).andReturn("   ").once();
    replayDefault();
    assertEquals(UserService.DEFAULT_LOGIN_FIELD, possibleLoginsCmd.getPossibleLogins());
    verifyDefault();
  }

}
