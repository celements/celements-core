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
package com.celements.web.token;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.util.ModelUtils;
import com.celements.web.classes.oldcore.XWikiUsersClass;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;

public class TokenLDAPAuthServiceImplTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private TokenLDAPAuthServiceImpl tokenAuthImpl;
  private XWikiStoreInterface store;

  @Before
  public void setUp_TokenLDAPAuthServiceImplTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    tokenAuthImpl = new TokenLDAPAuthServiceImpl();
    store = createMock(XWikiStoreInterface.class);
    expect(xwiki.getStore()).andReturn(store).anyTimes();
    // context.setUser calls xwiki.isVirtualMode in xwiki version 4.5
    expect(xwiki.isVirtualMode()).andReturn(true).anyTimes();
  }

  @Test
  public void testGetUsernameForToken() throws XWikiException {
    String userToken = "123456789012345678901234";
    List<String> userDocs = new Vector<>();
    userDocs.add("Doc.Fullname");
    Capture<String> captHQL = new Capture<>();
    Capture<List<?>> captParams = new Capture<>();
    expect(store.searchDocumentsNames(capture(captHQL), eq(0), eq(0), capture(captParams), same(
        context))).andReturn(userDocs).once();
    replay(xwiki, store);
    assertEquals("Doc.Fullname", tokenAuthImpl.getUsernameForToken(userToken, context));
    assertTrue(captHQL.getValue().contains("token.tokenvalue=?"));
    assertTrue("There seems to be no database independent 'now' in hql.",
        captHQL.getValue().contains("token.validuntil>=?"));
    assertTrue(captParams.getValue().contains(tokenAuthImpl.encryptString("hash:SHA-512:",
        userToken)));
    verify(xwiki, store);
  }

  @Test
  public void testGetUsernameForToken_userFromMainwiki() throws XWikiException {
    XWiki xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    XWikiStoreInterface store = createMock(XWikiStoreInterface.class);
    String userToken = "123456789012345678901234";
    List<String> userDocs = new Vector<>();
    userDocs.add("Doc.Fullname");
    expect(xwiki.getStore()).andReturn(store).once();
    Capture<String> captHQL = new Capture<>();
    Capture<String> captHQL2 = new Capture<>();
    Capture<List<?>> captParams = new Capture<>();
    expect(store.searchDocumentsNames(capture(captHQL), eq(0), eq(0), capture(captParams), same(
        context))).andReturn(new ArrayList<String>()).once();
    expect(store.searchDocumentsNames(capture(captHQL2), eq(0), eq(0), capture(captParams), same(
        context))).andReturn(userDocs).once();
    replay(xwiki, store);
    assertEquals("xwiki:Doc.Fullname", tokenAuthImpl.getUsernameForToken(userToken, context));
    assertTrue(captHQL2.getValue().contains("token.tokenvalue=?"));
    assertTrue("There seems to be no database independent 'now' in hql.",
        captHQL2.getValue().contains("token.validuntil>=?"));
    assertTrue(captParams.getValue().contains(tokenAuthImpl.encryptString("hash:SHA-512:",
        userToken)));
    verify(xwiki, store);
  }

  @Test
  public void testCheckAuthByToken_noUser() throws XWikiException {
    String userToken = "123456789012345678901234";
    List<String> userDocs = new Vector<>();
    Capture<String> captHQL = new Capture<>();
    Capture<List<?>> captParams = new Capture<>();
    expect(store.searchDocumentsNames(capture(captHQL), eq(0), eq(0), capture(captParams), same(
        context))).andReturn(userDocs).times(2);
    replay(xwiki, store);
    assertNull(tokenAuthImpl.checkAuthByToken("abcd", userToken, context));
  }

  @Test
  public void testCheckAuthByToken_admin() throws XWikiException {
    String userToken = "123456789012345678901234";
    List<String> userDocs = new Vector<>();
    String loginName = "theUserLoginName";
    String username = "XWiki." + loginName;
    userDocs.add(username);
    Capture<String> captHQL = new Capture<>();
    Capture<List<?>> captParams = new Capture<>();
    List<String> emptyList = Collections.emptyList();
    expect(store.searchDocumentsNames(capture(captHQL), eq(0), eq(0), capture(captParams), same(
        context))).andReturn(emptyList).once();
    expect(store.searchDocumentsNames(capture(captHQL), eq(0), eq(0), capture(captParams), same(
        context))).andReturn(userDocs).once();
    replay(xwiki, store);
    XWikiUser loggedInUser = tokenAuthImpl.checkAuthByToken(loginName, userToken, context);
    verify(xwiki, store);
    assertNotNull(loggedInUser);
    String expectedUserName = "xwiki:" + username;
    assertEquals(expectedUserName, loggedInUser.getUser());
    assertEquals(expectedUserName, context.getXWikiUser().getUser());
    assertEquals(expectedUserName, context.getUser());
  }

  @Test
  public void testCheckAuthByToken() throws XWikiException {
    String userToken = "123456789012345678901234";
    List<String> userDocs = new Vector<>();
    String loginName = "theUserLoginName";
    String username = "XWiki." + loginName;
    userDocs.add(username);
    Capture<String> captHQL = new Capture<>();
    Capture<List<?>> captParams = new Capture<>();
    expect(store.searchDocumentsNames(capture(captHQL), eq(0), eq(0), capture(captParams), same(
        context))).andReturn(userDocs).once();
    replay(xwiki, store);
    assertEquals(username, tokenAuthImpl.checkAuthByToken(loginName, userToken, context).getUser());
    assertEquals(username, context.getXWikiUser().getUser());
    assertEquals(username, context.getUser());
    verify(xwiki, store);
  }

  @Test
  public void testCheckAuthByToken_wrongUserName() throws XWikiException {
    String userToken = "123456789012345678901234";
    List<String> userDocs = new Vector<>();
    String loginName = "theUserLoginName";
    String username = "XWiki." + loginName;
    userDocs.add(username);
    Capture<String> captHQL = new Capture<>();
    Capture<List<?>> captParams = new Capture<>();
    expect(store.searchDocumentsNames(capture(captHQL), eq(0), eq(0), capture(captParams), same(
        context))).andReturn(userDocs).once();
    replay(xwiki, store);
    assertNull(tokenAuthImpl.checkAuthByToken("abcde", userToken, context));
    assertNull(context.getXWikiUser());
    assertEquals("XWiki.XWikiGuest", context.getUser());
    verify(xwiki, store);
  }

  @Test
  public void testCheckAuthXWikiContext_noRequest() throws XWikiException {
    replay(xwiki, store);
    assertNull(tokenAuthImpl.checkAuth(context));
    verify(xwiki, store);
  }

  @Test
  public void testCheckAuthXWikiContext() throws XWikiException {
    String userToken = "123456789012345678901234";
    String loginName = "theUserLoginName";
    String username = "XWiki." + loginName;
    DocumentReference userDocRef = Utils.getComponent(ModelUtils.class).resolveRef(username,
        DocumentReference.class);
    BaseObject userObj = new BaseObject();
    userObj.setDocumentReference(userDocRef);
    userObj.setXClassReference(Utils.getComponent(ClassDefinition.class,
        XWikiUsersClass.CLASS_DEF_HINT).getClassReference());
    userObj.setIntValue(XWikiUsersClass.FIELD_SUSPENDED.getName(), 0);
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    userDoc.addXObject(userObj);
    XWikiRequest request = createMock(XWikiRequest.class);
    expect(request.getParameter(eq("token"))).andReturn(userToken).atLeastOnce();
    expect(request.getParameter(eq("username"))).andReturn(loginName).atLeastOnce();
    context.setRequest(request);
    List<String> userDocs = new Vector<>();
    userDocs.add(username);
    Capture<String> captHQL = new Capture<>();
    Capture<List<?>> captParams = new Capture<>();
    expect(store.searchDocumentsNames(capture(captHQL), eq(0), eq(0), capture(captParams), same(
        context))).andReturn(userDocs).once();
    expect(xwiki.exists(eq(userDocRef), same(context))).andReturn(true);
    expect(xwiki.getDocument(eq(userDocRef), same(context))).andReturn(userDoc);
    replay(xwiki, store, request);
    assertEquals(username, tokenAuthImpl.checkAuth(context).getUser());
    assertEquals(username, context.getXWikiUser().getUser());
    assertEquals(username, context.getUser());
    verify(xwiki, store, request);
  }

  @Test
  public void testCheckAuthXWikiContext_noTokenAuth_null() throws XWikiException {
    XWikiRequest request = createMock(XWikiRequest.class);
    expect(request.getParameter(eq("token"))).andReturn(null).atLeastOnce();
    expect(request.getParameter(eq("username"))).andReturn(null).atLeastOnce();
    expect(request.getHttpServletRequest()).andReturn(null).anyTimes();
    context.setRequest(request);
    replay(xwiki, store, request);
    assertNull(tokenAuthImpl.checkAuth(context));
    assertNull(context.getXWikiUser());
    assertEquals("XWiki.XWikiGuest", context.getUser());
    verify(xwiki, store, request);
  }

  @Test
  public void testCheckAuthXWikiContext_noTokenAuth_emptyString() throws XWikiException {
    XWikiRequest request = createMock(XWikiRequest.class);
    expect(request.getParameter(eq("token"))).andReturn("").atLeastOnce();
    expect(request.getParameter(eq("username"))).andReturn("").atLeastOnce();
    expect(request.getHttpServletRequest()).andReturn(null).anyTimes();
    context.setRequest(request);
    replay(xwiki, store, request);
    assertNull(tokenAuthImpl.checkAuth(context));
    assertNull(context.getXWikiUser());
    assertEquals("XWiki.XWikiGuest", context.getUser());
    verify(xwiki, store, request);
  }

}
