package com.celements.web.plugin.cmd;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.celements.auth.user.User;
import com.celements.auth.user.UserService;
import com.celements.common.test.AbstractComponentTest;
import com.google.common.base.Optional;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.user.api.XWikiUser;

public class UserNameForUserDataCommandTest extends AbstractComponentTest {

  UserNameForUserDataCommand cmd;

  XWiki xwiki;
  UserService userServiceMock;

  @Before
  public void prepareTest() throws Exception {
    cmd = new UserNameForUserDataCommand();
    xwiki = getWikiMock();
    userServiceMock = registerComponentMock(UserService.class);
  }

  @Test
  public void testGetUsernameForUserData_null() throws XWikiException {
    replayDefault();
    assertEquals("", cmd.getUsernameForUserData(null, "loginname,email", getContext()));
    verifyDefault();
  }

  @Test
  public void testGetUsernameForUserData_empty() throws XWikiException {
    replayDefault();
    assertEquals("", cmd.getUsernameForUserData(" \t", "loginname,email", getContext()));
    verifyDefault();
  }

  @Test
  public void testGetUsernameForUserData_loginname_notExists() throws XWikiException {
    String login = "testLogin";
    expect(userServiceMock.getUserForLoginField(login, Arrays.asList(
        UserService.DEFAULT_LOGIN_FIELD))).andReturn(Optional.<User>absent());

    replayDefault();
    assertEquals("", cmd.getUsernameForUserData(login, "loginname,,", getContext()));
    verifyDefault();
  }

  @Test
  public void testGetUsernameForUserData_loginname_exists() throws XWikiException {
    String login = "testLogin";
    User user = createMockAndAddToDefault(User.class);
    expect(userServiceMock.getUserForLoginField(login, Arrays.asList(
        UserService.DEFAULT_LOGIN_FIELD))).andReturn(Optional.of(user));
    expect(user.asXWikiUser()).andReturn(new XWikiUser("XWiki." + login));

    replayDefault();
    assertEquals("XWiki." + login, cmd.getUsernameForUserData(login, ",loginname,", getContext()));
    verifyDefault();
  }

  @Test
  public void testGetUsernameForUserData_multiplePossibleFields() throws XWikiException {
    String login = "testLogin";
    User user = createMockAndAddToDefault(User.class);
    expect(userServiceMock.getUserForLoginField(login, Arrays.asList("email",
        UserService.DEFAULT_LOGIN_FIELD))).andReturn(Optional.of(user));
    expect(user.asXWikiUser()).andReturn(new XWikiUser("XWiki." + login));

    replayDefault();
    assertEquals("XWiki." + login, cmd.getUsernameForUserData(login, "email,loginname",
        getContext()));
    verifyDefault();
  }

}
