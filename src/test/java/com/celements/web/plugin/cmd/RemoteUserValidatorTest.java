/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.web.plugin.cmd;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.auth.user.User;
import com.celements.auth.user.UserService;
import com.celements.common.test.AbstractComponentTest;
import com.celements.model.util.ModelUtils;
import com.google.common.base.Optional;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiAuthService;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;

public class RemoteUserValidatorTest extends AbstractComponentTest {

  RemoteUserValidator cmd;
  XWikiContext context;

  private XWikiAuthService xWikiAuthServiceMock;
  private UserService userServiceMock;

  @Before
  public void setUp_RemoteUserValidatorTest() throws Exception {
    cmd = new RemoteUserValidator();
    context = getContext();
    userServiceMock = registerComponentMock(UserService.class);
    xWikiAuthServiceMock = createDefaultMock(XWikiAuthService.class);
    expect(getWikiMock().getAuthService()).andReturn(xWikiAuthServiceMock).anyTimes();
    expect(getWikiMock().isVirtualMode()).andReturn(true).anyTimes();
    expect(getWikiMock().getXWikiPreference(eq("auth_active_check"), anyObject(
        XWikiContext.class))).andReturn("1").anyTimes();
  }

  @Test
  public void test_isValidUserJSON_validationNotAllowed() {
    XWikiRequest request = createDefaultMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createDefaultMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("  ");

    replayDefault();
    assertEquals("{\"access\" : \"false\", \"error\" : \"access_denied\"}", cmd.isValidUserJSON("",
        "", "", null, context));
    verifyDefault();
  }

  @Test
  public void test_isValidUserJSON_noUser() throws XWikiException {
    XWikiRequest request = createDefaultMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createDefaultMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("  ");
    expect(userServiceMock.getUserForLoginField("blabla@mail.com")).andReturn(Optional.<User>absent());

    replayDefault();
    // important only call setUser after replayDefault. In unstable-2.0 branch setUser
    // calls xwiki.isVirtualMode
    context.setUser("xwiki:XWiki.superadmin");
    assertEquals("{\"access\" : \"false\", \"error\" : \"wrong_username_password\"}",
        cmd.isValidUserJSON("blabla@mail.com", "", "", null, context));
    verifyDefault();
  }

  @Test
  public void test_isValidUserJSON_notAuthenticated() throws XWikiException {
    XWikiRequest request = createDefaultMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createDefaultMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("  ");
    User userMock = createUserMock("XWiki.7sh2lya35");
    expect(userServiceMock.getUserForLoginField("blabla@mail.com")).andReturn(Optional.of(userMock));
    expect(xWikiAuthServiceMock.authenticate(eq("XWiki.7sh2lya35"), eq("pwd"), same(
        context))).andReturn(null).once();

    replayDefault();
    // important only call setUser after replayDefault. In unstable-2.0 branch setUser
    // calls xwiki.isVirtualMode
    context.setUser("xwiki:XWiki.superadmin");
    assertEquals("{\"access\" : \"false\", \"error\" : \"wrong_username_password\"}",
        cmd.isValidUserJSON("blabla@mail.com", "pwd", "", null, context));
    verifyDefault();
  }

  @Test
  public void test_isValidUserJSON_validUser_wrongGroup() throws XWikiException {
    XWikiRequest request = createDefaultMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createDefaultMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("  ");
    User userMock = createUserMock("XWiki.7sh2lya35");
    expect(userServiceMock.getUserForLoginField("blabla@mail.com")).andReturn(Optional.of(userMock));
    expectAuth("XWiki.7sh2lya35", "pwd");
    expectInGroup(userMock, "grp", false);

    replayDefault();
    // important only call setUser after replayDefault. In unstable-2.0 branch setUser
    // calls xwiki.isVirtualMode
    context.setUser("xwiki:XWiki.superadmin");
    assertEquals("{\"access\" : \"false\", \"error\" : \"user_not_in_group\"}", cmd.isValidUserJSON(
        "blabla@mail.com", "pwd", "grp", null, context));
    verifyDefault();
  }

  @Test
  public void test_isValidUserJSON_validUser_isInGroup_inactiveUser() throws XWikiException {
    XWikiRequest request = createDefaultMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createDefaultMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("  ");
    User userMock = createUserMock("XWiki.7sh2lya35");
    expect(userServiceMock.getUserForLoginField("blabla@mail.com")).andReturn(Optional.of(userMock));
    expectAuth("XWiki.7sh2lya35", "pwd");
    expectInGroup(userMock, "grp", true);
    expect(userMock.isActive()).andReturn(false);

    replayDefault();
    // important only call setUser after replayDefault. In unstable-2.0 branch setUser
    // calls xwiki.isVirtualMode
    context.setUser("xwiki:XWiki.superadmin");
    assertEquals("{\"access\" : \"false\", \"error\" : \"useraccount_inactive\"}",
        cmd.isValidUserJSON("blabla@mail.com", "pwd", "grp", null, context));
    verifyDefault();
  }

  @Test
  public void testIsValidUserJSON_validUser_isInGroup_noRetGroup() throws XWikiException {
    XWikiRequest request = createDefaultMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createDefaultMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("  ");
    User userMock = createUserMock("XWiki.7sh2lya35");
    expect(userServiceMock.getUserForLoginField("blabla@mail.com")).andReturn(Optional.of(userMock));
    expectAuth("XWiki.7sh2lya35", "pwd");
    expectInGroup(userMock, "grp", true);
    expect(userMock.isActive()).andReturn(true);

    replayDefault();
    // important only call setUser after replayDefault. In unstable-2.0 branch setUser
    // calls xwiki.isVirtualMode
    context.setUser("xwiki:XWiki.superadmin");
    assertEquals("{\"access\" : \"true\", \"username\" : \"blabla@mail.com\", "
        + "\"group_membership\" : {}}", cmd.isValidUserJSON("blabla@mail.com", "pwd", "grp", null,
            context));
    verifyDefault();
  }

  @Test
  public void testIsValidUserJSON_validUser_isInGroup_retGroup() throws XWikiException {
    context = createDefaultMock(XWikiContext.class);
    expect(context.getUser()).andReturn("xwiki:XWiki.superadmin").anyTimes();
    XWikiRequest request = createDefaultMock(XWikiRequest.class);
    expect(context.getRequest()).andReturn(request).anyTimes();
    HttpServletRequest httpRequest = createDefaultMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("  ");
    expect(context.getWiki()).andReturn(getWikiMock()).anyTimes();
    expect(context.getMessageTool()).andReturn(getMessageToolStub()).anyTimes();

    User userMock = createUserMock("XWiki.7sh2lya35");
    expect(userServiceMock.getUserForLoginField("blabla@mail.com")).andReturn(Optional.of(userMock));
    expectAuth("XWiki.7sh2lya35", "pwd");
    expect(userMock.isActive()).andReturn(true);
    List<String> returnGroups = Arrays.asList("XWiki.TestGroup1", "XWiki.TestGroup2",
        "XWiki.TestGroup3");
    XWikiUser xUserMock = createDefaultMock(XWikiUser.class);
    expect(userMock.asXWikiUser()).andReturn(xUserMock).anyTimes();
    expectInGroup(xUserMock, "XWiki.MemOfGroup", true);
    expectInGroup(xUserMock, returnGroups.get(0), true);
    expectInGroup(xUserMock, returnGroups.get(1), false);
    expectInGroup(xUserMock, returnGroups.get(2), true);

    replayDefault();
    String ret = cmd.isValidUserJSON("blabla@mail.com", "pwd", "XWiki.MemOfGroup", returnGroups,
        context);
    verifyDefault();
    assertEquals("{\"access\" : \"true\", \"username\" : \"blabla@mail.com\", "
        + "\"group_membership\" : {\"TestGroup1\" : \"true\", \"TestGroup2\""
        + " : \"false\", \"TestGroup3\" : \"true\"}}", ret);
  }

  @Test
  public void test_isGroupMember_null() {
    User userMock = createUserMock("XWiki.7sh2lya35");

    replayDefault();
    assertEquals("false", cmd.isGroupMember(null, null, context));
    assertEquals("false", cmd.isGroupMember(userMock, null, context));
    assertEquals("false", cmd.isGroupMember(userMock, "", context));
    verifyDefault();
  }

  @Test
  public void test_isGroupMember_blackListOtherDB() {
    User userMock = createUserMock("XWiki.7sh2lya35");

    replayDefault();
    assertEquals("false", cmd.isGroupMember(userMock, "xwiki:XWiki.Admin", context));
    verifyDefault();
  }

  @Test
  public void test_isGroupMember_blackListAllGroup() {
    User userMock = createUserMock("XWiki.7sh2lya35");

    replayDefault();
    assertEquals("false", cmd.isGroupMember(userMock, "XWiki.XWikiAllGroup", context));
    verifyDefault();
  }

  @Test
  public void test_isGroupMember_notInGroup() throws XWikiException {
    User userMock = createUserMock("XWiki.7sh2lya35");
    String group = "XWiki.TestGroup";
    expectInGroup(userMock, group, false);

    replayDefault();
    assertEquals("false", cmd.isGroupMember(userMock, group, context));
    verifyDefault();
  }

  @Test
  public void test_isGroupMember_inGroup() throws XWikiException {
    User userMock = createUserMock("XWiki.7sh2lya35");
    String group = "XWiki.TestGroup";
    expectInGroup(userMock, group, true);

    replayDefault();
    assertEquals("true", cmd.isGroupMember(userMock, group, context));
    verifyDefault();
  }

  @Test
  public void test_getErrorJSON() {
    assertEquals("{\"access\" : \"false\", \"error\" : \"access_denied\"}", cmd.getErrorJSON(
        "access_denied"));
    assertEquals("{\"access\" : \"false\", \"error\" : \"wrong_group\"}", cmd.getErrorJSON(
        "wrong_group"));
  }

  @Test
  public void test_getResultJSON() throws Exception {
    User userMock = createUserMock("XWiki.7sh2lya35");

    replayDefault();
    assertEquals("{\"access\" : \"true\", \"username\" : \"user@synventis.com\", "
        + "\"group_membership\" : {}}", cmd.getResultJSON(userMock, "user@synventis.com", null,
            context));
    verifyDefault();
  }

  @Test
  public void test_getResultJSON_withReturnGroups() throws Exception {
    User userMock = createUserMock("XWiki.7sh2lya35");
    List<String> returnGroups = Arrays.asList("XWiki.TestGroup1", "XWiki.TestGroup2");
    XWikiUser xUserMock = createDefaultMock(XWikiUser.class);
    expect(userMock.asXWikiUser()).andReturn(xUserMock).anyTimes();
    expectInGroup(xUserMock, returnGroups.get(0), true);
    expectInGroup(xUserMock, returnGroups.get(1), false);

    replayDefault();
    assertEquals("{\"access\" : \"true\", \"username\" : \"user@synventis.com\", "
        + "\"group_membership\" : {\"TestGroup1\" : \"true\", \"TestGroup2\" : \"false\"}}",
        cmd.getResultJSON(userMock, "user@synventis.com", returnGroups, context));
    verifyDefault();
  }

  @Test
  public void test_validationAllowed_superadmin() {
    XWikiRequest request = createDefaultMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createDefaultMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("  ");

    replayDefault();
    // important only call setUser after replayDefault. In unstable-2.0 branch setUser
    // calls xwiki.isVirtualMode
    context.setUser("xwiki:XWiki.superadmin");
    assertTrue(cmd.validationAllowed(context));
    verifyDefault();
  }

  @Test
  public void test_validationAllowed_noHostInRequest_null() {
    XWikiRequest request = createDefaultMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createDefaultMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("  ");

    replayDefault();
    assertFalse(cmd.validationAllowed(context));
    verifyDefault();
  }

  @Test
  public void test_validationAllowed_noHostInRequest_empty() {
    XWikiRequest request = createDefaultMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createDefaultMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("  ");

    replayDefault();
    assertFalse(cmd.validationAllowed(context));
    verifyDefault();
  }

  @Test
  public void test_validationAllowed_noConfigFound_noObj() {
    XWikiRequest request = createDefaultMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createDefaultMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("test.synventis.com:10080");
    XWikiDocument doc = new XWikiDocument();
    context.setDoc(doc);

    replayDefault();
    assertFalse(cmd.validationAllowed(context));
    verifyDefault();
  }

  @Test
  public void test_validationAllowed_noConfigFound_hasObj() {
    XWikiRequest request = createDefaultMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createDefaultMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("test.synventis.com:10080");
    XWikiDocument doc = new XWikiDocument();
    context.setDoc(doc);
    BaseObject obj = new BaseObject();
    obj.setStringValue("host", "another.host.com");
    doc.addObject("Classes.RemoteUserValidationClass", obj);

    replayDefault();
    assertFalse(cmd.validationAllowed(context));
    verifyDefault();
  }

  @Test
  public void test_validationAllowed_secretEmpty() {
    XWikiRequest request = createDefaultMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createDefaultMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("test.synventis.com:10080");
    XWikiDocument doc = new XWikiDocument();
    context.setDoc(doc);
    BaseObject obj = new BaseObject();
    obj.setStringValue("host", "test.synventis.com:10080");
    obj.setStringValue("serverSecret", "");
    expect(request.get(eq("secret"))).andReturn("").once();
    doc.addObject("Classes.RemoteUserValidationClass", obj);

    replayDefault();
    assertFalse(cmd.validationAllowed(context));
    verifyDefault();
  }

  @Test
  public void test_validationAllowed_secretNoMatch() {
    XWikiRequest request = createDefaultMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createDefaultMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(request.get(eq("secret"))).andReturn("sn34ky").once();
    expect(httpRequest.getRemoteHost()).andReturn("test.synventis.com:10080");
    XWikiDocument doc = new XWikiDocument();
    context.setDoc(doc);
    BaseObject obj = new BaseObject();
    obj.setStringValue("host", "test.synventis.com:10080");
    obj.setStringValue("serverSecret", "s3cr3tC0d3");
    doc.addObject("Classes.RemoteUserValidationClass", obj);

    replayDefault();
    assertFalse(cmd.validationAllowed(context));
    verifyDefault();
  }

  @Test
  public void test_validationAllowed_allowed() {
    XWikiRequest request = createDefaultMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createDefaultMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(request.get(eq("secret"))).andReturn("s3cr3tC0d3").once();
    expect(httpRequest.getRemoteHost()).andReturn("test.synventis.com:10080");
    XWikiDocument doc = new XWikiDocument();
    context.setDoc(doc);
    BaseObject obj = new BaseObject();
    obj.setStringValue("host", "test.synventis.com:10080");
    obj.setStringValue("secret", "s3cr3tC0d3");
    doc.addObject("Classes.RemoteUserValidationClass", obj);

    replayDefault();
    assertTrue(cmd.validationAllowed(context));
    verifyDefault();
  }

  private Principal expectAuth(String username, String password) throws XWikiException {
    Principal principal = createDefaultMock(Principal.class);
    expect(principal.getName()).andReturn(username).anyTimes();
    expect(xWikiAuthServiceMock.authenticate(eq(username), eq(password), same(context))).andReturn(
        principal).once();
    return principal;
  }

  private User createUserMock(String username) {
    User userMock = createDefaultMock(User.class);
    expect(userMock.getDocRef()).andReturn(Utils.getComponent(ModelUtils.class).resolveRef(username,
        DocumentReference.class)).anyTimes();
    return userMock;
  }

  private XWikiUser expectInGroup(User userMock, String group, boolean isInGrp)
      throws XWikiException {
    XWikiUser xUserMock = createDefaultMock(XWikiUser.class);
    expect(userMock.asXWikiUser()).andReturn(xUserMock).anyTimes();
    expectInGroup(xUserMock, group, isInGrp);
    return xUserMock;
  }

  private void expectInGroup(XWikiUser xUserMock, String group, boolean isInGrp)
      throws XWikiException {
    expect(xUserMock.isUserInGroup(group, context)).andReturn(isInGrp);
    String name = group.substring(group.indexOf(".") + 1);
    getMessageToolStub().injectMessage("cel_groupname_" + name, name);
  }

}
