package com.celements.web.plugin.cmd;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

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
  private UserService userServiceMock;

  @Before
  public void setUp_PossibleLoginsCommandTest() throws Exception {
    context = getXContext();
    xwiki = getMock(XWiki.class);
    userServiceMock = registerComponentMock(UserService.class);

    possibleLoginsCmd = new PossibleLoginsCommand();
  }

  @Test
  public void testGetPossibleLogins_none() {
    expect(userServiceMock.getPossibleLoginFields())
        .andReturn(new HashSet<>(Arrays.<String>asList(UserService.DEFAULT_LOGIN_FIELD)))
        .atLeastOnce();
    replayDefault();
    assertEquals(UserService.DEFAULT_LOGIN_FIELD, possibleLoginsCmd.getPossibleLogins());
    verifyDefault();
  }

  @Test
  public void testGetPossibleLogins_local() {
    expect(userServiceMock.getPossibleLoginFields())
        .andReturn(new HashSet<>(Arrays.<String>asList("email", "loginname")))
        .atLeastOnce();
    replayDefault();
    List<String> logins = Arrays.<String>asList(possibleLoginsCmd.getPossibleLogins().split(","));
    assertTrue("email", logins.contains("email"));
    assertTrue("email", logins.contains("loginname"));
    assertEquals(2, logins.size());
    verifyDefault();
  }

}
