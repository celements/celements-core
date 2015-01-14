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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.web.plugin.CelementsWebPlugin;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.User;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiAuthService;
import com.xpn.xwiki.web.XWikiMessageTool;
import com.xpn.xwiki.web.XWikiRequest;

public class RemoteUserValidatorTest extends AbstractBridgedComponentTestCase {
  RemoteUserValidator cmd;
  XWikiContext context;
  
  @Before
  public void setUp_RemoteUserValidatorTest() throws Exception {
    cmd = new RemoteUserValidator();
    context = getContext();
  }

  @Test
  public void testIsValidUserJSON_validationNotAllowed() {
    XWikiRequest request = createMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("  ");
    replay(httpRequest, request);
    assertEquals("{\"access\" : \"false\", \"error\" : \"access_denied\"}", 
        cmd.isValidUserJSON("", "", "", null, context));
    verify(httpRequest, request);
  }

  @Test
  public void testIsValidUserJSON_principalNull() throws XWikiException {
    context.setUser("xwiki:XWiki.superadmin");
    XWikiRequest request = createMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("  ");
    XWiki wiki = new XWiki();
    context.setWiki(wiki);
    CelementsWebPlugin celementsweb = createMock(CelementsWebPlugin.class);
    cmd.injectCelementsWeb(celementsweb);
    expect(celementsweb.getUsernameForUserData(eq("blabla@mail.com"), 
        eq("loginname"), same(context))).andReturn("").once();
    replay(celementsweb, httpRequest, request);
    assertEquals("{\"access\" : \"false\", \"error\" : \"wrong_username_password\"}", 
        cmd.isValidUserJSON("blabla@mail.com", "", "", null, context));
    verify(celementsweb, httpRequest, request);
  }

  @Test
  public void testIsValidUserJSON_validUser_wrongGroup() throws XWikiException {
    context.setUser("xwiki:XWiki.superadmin");
    XWikiRequest request = createMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("  ");
    XWiki xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    XWikiAuthService auth = createMock(XWikiAuthService.class);
    expect(xwiki.getAuthService()).andReturn(auth).once();
    Principal principal = createMock(Principal.class);
    expect(auth.authenticate(eq("XWiki.7sh2lya35"), eq("pwd"), same(context)))
        .andReturn(principal).once();
    expect(principal.getName()).andReturn("XWiki.7sh2lya35").anyTimes();
    User user = createMock(User.class);
    expect(xwiki.getUser(eq("XWiki.7sh2lya35"), same(context))).andReturn(user).once();
    expect(user.isUserInGroup(eq("grp"))).andReturn(false);
    expect(xwiki.getXWikiPreference(eq("cellogin"), eq("loginname"), same(context)))
        .andReturn("email,loginname").once();
    CelementsWebPlugin celementsweb = createMock(CelementsWebPlugin.class);
    cmd.injectCelementsWeb(celementsweb);
    expect(celementsweb.getUsernameForUserData(eq("blabla@mail.com"), 
        eq("email,loginname"), same(context))).andReturn("XWiki.7sh2lya35").once();
    replay(auth, celementsweb, httpRequest, principal, request, user, xwiki);
    assertEquals("{\"access\" : \"false\", \"error\" : \"user_not_in_group\"}", 
        cmd.isValidUserJSON("blabla@mail.com", "pwd", "grp", null, context));
    verify(auth, celementsweb, httpRequest, principal, request, user, xwiki);
  }

  @Test
  public void testIsValidUserJSON_validUser_isInGroup_inactiveUser() throws XWikiException {
    context.setUser("xwiki:XWiki.superadmin");
    XWikiRequest request = createMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("  ");
    XWiki xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    XWikiAuthService auth = createMock(XWikiAuthService.class);
    expect(xwiki.getAuthService()).andReturn(auth).once();
    Principal principal = createMock(Principal.class);
    expect(auth.authenticate(eq("XWiki.7sh2lya35"), eq("pwd"), same(context))).andReturn(principal).once();
    expect(principal.getName()).andReturn("XWiki.7sh2lya35").anyTimes();
    User user = createMock(User.class);
    expect(xwiki.getUser(eq("XWiki.7sh2lya35"), same(context))).andReturn(user).once();
    expect(xwiki.getXWikiPreference(eq("auth_active_check"), same(context)))
        .andReturn("1").atLeastOnce();
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq("XWiki.7sh2lya35"), same(context))).andReturn(doc).once();
    expect(doc.getIntValue(eq("XWiki.XWikiUsers"), eq("active"))).andReturn(0).once();
    expect(user.isUserInGroup(eq("grp"))).andReturn(true);
    expect(xwiki.getXWikiPreference(eq("cellogin"), eq("loginname"), same(context)))
        .andReturn("email,loginname").once();
    CelementsWebPlugin celementsweb = createMock(CelementsWebPlugin.class);
    cmd.injectCelementsWeb(celementsweb);
    expect(celementsweb.getUsernameForUserData(eq("blabla@mail.com"), 
        eq("email,loginname"), same(context))).andReturn("XWiki.7sh2lya35").once();
    replay(auth, celementsweb, doc, httpRequest, principal, request, user, xwiki);
    assertEquals("{\"access\" : \"false\", \"error\" : \"useraccount_inactive\"}", 
        cmd.isValidUserJSON("blabla@mail.com", "pwd", "grp", null, context));
    verify(auth, celementsweb, doc, httpRequest, principal, request, user, xwiki);
  }

  @Test
  public void testIsValidUserJSON_validUser_isInGroup_noRetGroup() throws XWikiException {
    context.setUser("xwiki:XWiki.superadmin");
    XWikiRequest request = createMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("  ");
    XWiki xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    XWikiAuthService auth = createMock(XWikiAuthService.class);
    expect(xwiki.getAuthService()).andReturn(auth).once();
    Principal principal = createMock(Principal.class);
    expect(auth.authenticate(eq("XWiki.7sh2lya35"), eq("pwd"), same(context)))
        .andReturn(principal).once();
    expect(principal.getName()).andReturn("XWiki.7sh2lya35").anyTimes();
    User user = createMock(User.class);
    expect(xwiki.getUser(eq("XWiki.7sh2lya35"), same(context))).andReturn(user).once();
    expect(xwiki.getXWikiPreference(eq("auth_active_check"), same(context)))
    .andReturn("1").atLeastOnce();
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq("XWiki.7sh2lya35"), same(context))).andReturn(doc).once();
    expect(doc.getIntValue(eq("XWiki.XWikiUsers"), eq("active"))).andReturn(1).once();
    expect(user.isUserInGroup(eq("grp"))).andReturn(true);
    expect(xwiki.getXWikiPreference(eq("cellogin"), eq("loginname"), same(context)))
        .andReturn("email,loginname").once();
    CelementsWebPlugin celementsweb = createMock(CelementsWebPlugin.class);
    cmd.injectCelementsWeb(celementsweb);
    expect(celementsweb.getUsernameForUserData(eq("blabla@mail.com"), 
        eq("email,loginname"), same(context))).andReturn("XWiki.7sh2lya35").once();
    replay(auth, celementsweb, doc, httpRequest, principal, request, user, xwiki);
    assertEquals("{\"access\" : \"true\", \"username\" : \"blabla@mail.com\", " +
        "\"group_membership\" : {}}", cmd.isValidUserJSON("blabla@mail.com", "pwd", "grp",
        null, context));
    verify(auth, celementsweb, doc, httpRequest, principal, request, user, xwiki);
  }

  @Test
  public void testIsValidUserJSON_validUser_isInGroup_retGroup() throws XWikiException {
    XWikiContext context = createMock(XWikiContext.class);
    expect(context.getUser()).andReturn("xwiki:XWiki.superadmin").anyTimes();
    XWikiRequest request = createMock(XWikiRequest.class);
    expect(context.getRequest()).andReturn(request).anyTimes();
    HttpServletRequest httpRequest = createMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("  ");
    XWiki xwiki = createMock(XWiki.class);
    expect(context.getWiki()).andReturn(xwiki).anyTimes();
    XWikiAuthService auth = createMock(XWikiAuthService.class);
    expect(xwiki.getAuthService()).andReturn(auth).once();
    Principal principal = createMock(Principal.class);
    expect(auth.authenticate(eq("XWiki.7sh2lya35"), eq("pwd"), same(context)))
        .andReturn(principal).once();
    expect(principal.getName()).andReturn("XWiki.7sh2lya35").anyTimes();
    User user = createMock(User.class);
    expect(xwiki.getUser(eq("XWiki.7sh2lya35"), same(context))).andReturn(user)
        .anyTimes();
    expect(xwiki.getXWikiPreference(eq("auth_active_check"), same(context)))
    .andReturn("1").atLeastOnce();
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq("XWiki.7sh2lya35"), same(context))).andReturn(doc).once();
    expect(doc.getIntValue(eq("XWiki.XWikiUsers"), eq("active"))).andReturn(1).once();
    expect(user.isUserInGroup(eq("XWiki.MemOfGroup"))).andReturn(true).anyTimes();
    expect(user.isUserInGroup(eq("XWiki.TestGroup1"))).andReturn(true).anyTimes();
    expect(user.isUserInGroup(eq("XWiki.TestGroup2"))).andReturn(false).anyTimes();
    expect(user.isUserInGroup(eq("XWiki.TestGroup3"))).andReturn(true).anyTimes();
    expect(xwiki.getXWikiPreference(eq("cellogin"), eq("loginname"), same(context)))
        .andReturn("email,loginname").anyTimes();
    CelementsWebPlugin celementsweb = createMock(CelementsWebPlugin.class);
    cmd.injectCelementsWeb(celementsweb);
    expect(celementsweb.getUsernameForUserData(eq("blabla@mail.com"), 
        eq("email,loginname"), same(context))).andReturn("XWiki.7sh2lya35").anyTimes();
    List<String> retGroup = new ArrayList<String>();
    retGroup.add("XWiki.TestGroup1");
    retGroup.add("XWiki.TestGroup2");
    retGroup.add("XWiki.TestGroup3");
    XWikiMessageTool messageTool = createMock(XWikiMessageTool.class);
    expect(context.getMessageTool()).andReturn(messageTool).anyTimes();
    expect(messageTool.get(eq("cel_groupname_TestGroup1"))).andReturn("grp1").anyTimes();
    expect(messageTool.get(eq("cel_groupname_TestGroup2"))).andReturn("grp2").anyTimes();
    expect(messageTool.get(eq("cel_groupname_TestGroup3"))).andReturn("grp3").anyTimes();
    replay(auth, celementsweb, context, doc, httpRequest, messageTool, principal, request,
        user, xwiki);
    assertEquals("{\"access\" : \"true\", \"username\" : \"blabla@mail.com\", " +
        "\"group_membership\" : {\"grp1\" : \"true\", \"grp2\"" +
        " : \"false\", \"grp3\" : \"true\"}}", cmd.isValidUserJSON(
        "blabla@mail.com", "pwd", "XWiki.MemOfGroup", retGroup, context));
    verify(auth, celementsweb, context, doc, httpRequest, messageTool, principal, request,
        user, xwiki);
  }

  @Test
  public void testIsGroupMember_null() {
    assertEquals("false", cmd.isGroupMember("blabla@mail.com", null, context));
  }

  @Test
  public void testIsGroupMember_blackListOtherDB() {
    assertEquals("false", cmd.isGroupMember("blabla@mail.com", "xwiki:XWiki.Admin", 
        context));
  }

  @Test
  public void testIsGroupMember_blackListAllGroup() {
    assertEquals("false", cmd.isGroupMember("blabla@mail.com", "XWiki.XWikiAllGroup", 
        context));
  }

  @Test
  public void testIsGroupMember_notInGroup() throws XWikiException {
    XWiki xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    CelementsWebPlugin celementsweb = createMock(CelementsWebPlugin.class);
    cmd.injectCelementsWeb(celementsweb);
    expect(xwiki.getXWikiPreference(eq("cellogin"), eq("loginname"), same(context)))
    .andReturn("email,loginname").once();
    expect(celementsweb.getUsernameForUserData(eq("blabla@mail.com"), 
        eq("email,loginname"), same(context))).andReturn("XWiki.7sh2lya35").once();
    User user = createMock(User.class);
    expect(xwiki.getUser(eq("XWiki.7sh2lya35"), same(context))).andReturn(user).once();
    expect(user.isUserInGroup(eq("XWiki.TestGroup"))).andReturn(false).once();
    replay(celementsweb, user, xwiki);
    assertEquals("false", cmd.isGroupMember("blabla@mail.com", "XWiki.TestGroup", 
        context));
    verify(celementsweb, user, xwiki);
  }

  @Test
  public void testIsGroupMember_inGroup() throws XWikiException {
    XWiki xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    CelementsWebPlugin celementsweb = createMock(CelementsWebPlugin.class);
    cmd.injectCelementsWeb(celementsweb);
    expect(xwiki.getXWikiPreference(eq("cellogin"), eq("loginname"), same(context)))
    .andReturn("email,loginname").once();
    expect(celementsweb.getUsernameForUserData(eq("blabla@mail.com"), 
        eq("email,loginname"), same(context))).andReturn("XWiki.7sh2lya35").once();
    User user = createMock(User.class);
    expect(xwiki.getUser(eq("XWiki.7sh2lya35"), same(context))).andReturn(user).once();
    expect(user.isUserInGroup(eq("XWiki.TestGroup"))).andReturn(true).once();
    replay(celementsweb, user, xwiki);
    assertEquals("true", cmd.isGroupMember("blabla@mail.com", "XWiki.TestGroup", 
        context));
    verify(celementsweb, user, xwiki);
  }
  
  @Test
  public void testGetResultJSON() {
    assertEquals("{\"access\" : \"false\", \"error\" : \"access_denied\"}", 
        cmd.getResultJSON(null, false, "access_denied", null, context));
    assertEquals("{\"access\" : \"false\", \"error\" : \"access_denied\"}", 
        cmd.getResultJSON(null, true, "access_denied", null, context));
    assertEquals("{\"access\" : \"false\", \"error\" : \"access_denied\"}", 
        cmd.getResultJSON("", true, "access_denied", null, context));
    assertEquals("{\"access\" : \"false\", \"error\" : \"access_denied\"}", 
        cmd.getResultJSON("haXX0r", true, "access_denied", null, context));
    assertEquals("{\"access\" : \"false\", \"error\" : \"wrong_group\"}", 
        cmd.getResultJSON("user@synventis.com", false, "wrong_group", null, context));
    assertEquals("{\"access\" : \"true\", \"username\" : \"user@synventis.com\", " +
        "\"group_membership\" : {}}", cmd.getResultJSON("user@synventis.com", true, "", 
        null, context));
    assertEquals("{\"access\" : \"true\", \"username\" : \"user@synventis.com\", " +
        "\"group_membership\" : {}}", cmd.getResultJSON("user@synventis.com", true, null, 
        null, context));
  }
  
  @Test
  public void testGetResultJSON_withReturnGroups() throws XWikiException {
    XWikiContext context = createMock(XWikiContext.class);
    XWiki xwiki = createMock(XWiki.class);
    expect(context.getWiki()).andReturn(xwiki).anyTimes();
    CelementsWebPlugin celementsweb = createMock(CelementsWebPlugin.class);
    cmd.injectCelementsWeb(celementsweb);
    expect(xwiki.getXWikiPreference(eq("cellogin"), eq("loginname"), same(context)))
    .andReturn("email,loginname").atLeastOnce();
    expect(celementsweb.getUsernameForUserData(eq("user@synventis.com"), 
        eq("email,loginname"), same(context))).andReturn("XWiki.7sh2lya35").atLeastOnce();
    User user = createMock(User.class);
    expect(xwiki.getUser(eq("XWiki.7sh2lya35"), same(context))).andReturn(user)
        .atLeastOnce();
    expect(user.isUserInGroup(eq("XWiki.TestGroup1"))).andReturn(true).atLeastOnce();
    expect(user.isUserInGroup(eq("XWiki.TestGroup2"))).andReturn(false).atLeastOnce();
    List<String> returnGroups = new ArrayList<String>();
    returnGroups.add("XWiki.TestGroup1");
    XWikiMessageTool messageTool = createMock(XWikiMessageTool.class);
    expect(context.getMessageTool()).andReturn(messageTool).anyTimes();
    expect(messageTool.get(eq("cel_groupname_TestGroup1"))).andReturn("grp1").anyTimes();
    expect(messageTool.get(eq("cel_groupname_TestGroup2"))).andReturn("grp2").anyTimes();
    replay(celementsweb, context, messageTool, user, xwiki);
    assertEquals("{\"access\" : \"true\", \"username\" : \"user@synventis.com\", " +
        "\"group_membership\" : {\"grp1\" : \"true\"}}", 
        cmd.getResultJSON("user@synventis.com", true, null, returnGroups, context));
    returnGroups.add("XWiki.TestGroup2");
    assertEquals("{\"access\" : \"true\", \"username\" : \"user@synventis.com\", " +
        "\"group_membership\" : {\"grp1\" : \"true\", \"grp2\" : \"false\"}}", 
        cmd.getResultJSON("user@synventis.com", true, null, returnGroups, context));
    verify(celementsweb, context, messageTool, user, xwiki);
  }

  @Test
  public void testValidationAllowed_superadmin() {
    context.setUser("xwiki:XWiki.superadmin");
    XWikiRequest request = createMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("  ");
    replay(httpRequest, request);
    assertTrue(cmd.validationAllowed(context));
    verify(httpRequest, request);
  }

  @Test
  public void testValidationAllowed_noHostInRequest_null() {
    XWikiRequest request = createMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("  ");
    replay(httpRequest, request);
    assertFalse(cmd.validationAllowed(context));
    verify(httpRequest, request);
  }

  @Test
  public void testValidationAllowed_noHostInRequest_empty() {
    XWikiRequest request = createMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("  ");
    replay(httpRequest, request);
    assertFalse(cmd.validationAllowed(context));
    verify(httpRequest, request);
  }

  @Test
  public void testValidationAllowed_noConfigFound_noObj() {
    XWikiRequest request = createMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("test.synventis.com:10080");
    XWikiDocument doc = new XWikiDocument();
    context.setDoc(doc);
    replay(httpRequest, request);
    assertFalse(cmd.validationAllowed(context));
    verify(httpRequest, request);
  }

  @Test
  public void testValidationAllowed_noConfigFound_hasObj() {
    XWikiRequest request = createMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("test.synventis.com:10080");
    XWikiDocument doc = new XWikiDocument();
    context.setDoc(doc);
    BaseObject obj = new BaseObject();
    obj.setStringValue("host", "another.host.com");
    doc.addObject("Classes.RemoteUserValidationClass", obj);
    replay(httpRequest, request);
    assertFalse(cmd.validationAllowed(context));
    verify(httpRequest, request);
  }

  @Test
  public void testValidationAllowed_secretEmpty() {
    XWikiRequest request = createMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(httpRequest.getRemoteHost()).andReturn("test.synventis.com:10080");
    XWikiDocument doc = new XWikiDocument();
    context.setDoc(doc);
    BaseObject obj = new BaseObject();
    obj.setStringValue("host", "test.synventis.com:10080");
    obj.setStringValue("serverSecret", "");
    expect(request.get(eq("secret"))).andReturn("").once();
    doc.addObject("Classes.RemoteUserValidationClass", obj);
    replay(httpRequest, request);
    assertFalse(cmd.validationAllowed(context));
    verify(httpRequest, request);
  }

  @Test
  public void testValidationAllowed_secretNoMatch() {
    XWikiRequest request = createMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(request.get(eq("secret"))).andReturn("sn34ky").once();
    expect(httpRequest.getRemoteHost()).andReturn("test.synventis.com:10080");
    XWikiDocument doc = new XWikiDocument();
    context.setDoc(doc);
    BaseObject obj = new BaseObject();
    obj.setStringValue("host", "test.synventis.com:10080");
    obj.setStringValue("serverSecret", "s3cr3tC0d3");
    doc.addObject("Classes.RemoteUserValidationClass", obj);
    replay(httpRequest, request);
    assertFalse(cmd.validationAllowed(context));
    verify(httpRequest, request);
  }

  @Test
  public void testValidationAllowed_allowed() {
    XWikiRequest request = createMock(XWikiRequest.class);
    context.setRequest(request);
    HttpServletRequest httpRequest = createMock(HttpServletRequest.class);
    expect(request.getHttpServletRequest()).andReturn(httpRequest).once();
    expect(request.get(eq("secret"))).andReturn("s3cr3tC0d3").once();
    expect(httpRequest.getRemoteHost()).andReturn("test.synventis.com:10080");
    XWikiDocument doc = new XWikiDocument();
    context.setDoc(doc);
    BaseObject obj = new BaseObject();
    obj.setStringValue("host", "test.synventis.com:10080");
    obj.setStringValue("secret", "s3cr3tC0d3");
    doc.addObject("Classes.RemoteUserValidationClass", obj);
    replay(httpRequest, request);
    assertTrue(cmd.validationAllowed(context));
    verify(httpRequest, request);
  }

  @Test
  public void testHasValue_false() {
    assertFalse(cmd.hasValue(""));
    assertFalse(cmd.hasValue(" "));
    assertFalse(cmd.hasValue("\n"));
    assertFalse(cmd.hasValue("\t"));
    assertFalse(cmd.hasValue("     "));
  }

  @Test
  public void testHasValue_true() {
    assertTrue(cmd.hasValue("a"));
    assertTrue(cmd.hasValue("a  "));
    assertTrue(cmd.hasValue("  A"));
    assertTrue(cmd.hasValue("hi there"));
  }

}
