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

import static com.celements.common.test.CelementsTestUtils.*;
import static com.celements.web.token.TokenLDAPAuthServiceImpl.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.classes.ClassDefinition;
import com.celements.web.classes.oldcore.XWikiUsersClass;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;

public class TokenLDAPAuthServiceImplTest extends AbstractComponentTest {

  private TokenLDAPAuthServiceImpl tokenAuthImpl;
  private XWikiStoreInterface store;

  @Before
  public void prepare() throws Exception {
    registerComponentMocks(IModelAccessFacade.class);
    tokenAuthImpl = new TokenLDAPAuthServiceImpl();
    store = createDefaultMock(XWikiStoreInterface.class);
    expect(getWikiMock().getStore()).andReturn(store).anyTimes();
    expect(getWikiMock().isVirtualMode()).andReturn(true).anyTimes();
  }

  @Test
  public void test_getUsernameForToken() throws Exception {
    String userToken = "123456789012345678901234";
    Capture<String> captHQL = newCapture();
    Capture<List<?>> captParams = newCapture();
    expect(store.searchDocumentsNames(capture(captHQL), eq(0), eq(0), capture(captParams), same(
        getContext()))).andReturn(Arrays.asList("Doc.Fullname")).once();

    replayDefault();
    assertEquals("Doc.Fullname", tokenAuthImpl.getUsernameForToken(userToken, getContext()));
    assertTrue(captHQL.getValue().contains("token.tokenvalue=?"));
    assertTrue("There seems to be no database independent 'now' in hql.",
        captHQL.getValue().contains("token.validuntil>=?"));
    assertTrue(captParams.getValue().contains(tokenAuthImpl.encryptString("hash:SHA-512:",
        userToken)));
    verifyDefault();
  }

  @Test
  public void test_getUsernameForToken_userFromMainwiki() throws Exception {
    String userToken = "123456789012345678901234";
    Capture<String> captHQL = newCapture();
    Capture<String> captHQL2 = newCapture();
    Capture<List<?>> captParams = newCapture();
    expect(store.searchDocumentsNames(capture(captHQL), eq(0), eq(0), capture(captParams), same(
        getContext()))).andReturn(new ArrayList<String>()).once();
    expect(store.searchDocumentsNames(capture(captHQL2), eq(0), eq(0), capture(captParams), same(
        getContext()))).andReturn(Arrays.asList("Doc.Fullname")).once();

    replayDefault();
    assertEquals("xwiki:Doc.Fullname", tokenAuthImpl.getUsernameForToken(userToken, getContext()));
    assertTrue(captHQL2.getValue().contains("token.tokenvalue=?"));
    assertTrue("There seems to be no database independent 'now' in hql.",
        captHQL2.getValue().contains("token.validuntil>=?"));
    assertTrue(captParams.getValue().contains(tokenAuthImpl.encryptString("hash:SHA-512:",
        userToken)));
    verifyDefault();
  }

  @Test
  public void test_checkAuthByToken_noUser() throws Exception {
    String userToken = "123456789012345678901234";
    expect(store.searchDocumentsNames(anyString(), eq(0), eq(0), anyObject(List.class),
        same(getContext()))).andReturn(Collections.emptyList()).times(2);

    replayDefault();
    assertNull(tokenAuthImpl.checkAuthByToken("abcd", userToken, getContext()));
    verifyDefault();
  }

  @Test
  public void test_checkAuthByToken_admin() throws Exception {
    String userToken = "123456789012345678901234";
    String loginName = "theUserLoginName";
    String username = "XWiki." + loginName;
    List<String> emptyList = Collections.emptyList();
    expect(store.searchDocumentsNames(anyString(), eq(0), eq(0), anyObject(List.class),
        same(getContext()))).andReturn(emptyList).once();
    expect(store.searchDocumentsNames(anyString(), eq(0), eq(0), anyObject(List.class),
        same(getContext()))).andReturn(Arrays.asList(username)).once();

    replayDefault();
    XWikiUser loggedInUser = tokenAuthImpl.checkAuthByToken(loginName, userToken, getContext());
    verifyDefault();
    assertNotNull(loggedInUser);
    String expectedUserName = "xwiki:" + username;
    assertEquals(expectedUserName, loggedInUser.getUser());
    assertEquals(expectedUserName, getContext().getXWikiUser().getUser());
    assertEquals(expectedUserName, getContext().getUser());
  }

  @Test
  public void test_checkAuthByToken() throws Exception {
    String userToken = "123456789012345678901234";
    String loginName = "theUserLoginName";
    String username = "XWiki." + loginName;
    expect(store.searchDocumentsNames(anyString(), eq(0), eq(0), anyObject(List.class),
        same(getContext()))).andReturn(Arrays.asList(username)).once();

    replayDefault();
    assertEquals(username,
        tokenAuthImpl.checkAuthByToken(loginName, userToken, getContext()).getUser());
    assertEquals(username, getContext().getXWikiUser().getUser());
    assertEquals(username, getContext().getUser());
    verifyDefault();
  }

  @Test
  public void test_checkAuthByToken_wrongUserName() throws Exception {
    String userToken = "123456789012345678901234";
    String loginName = "theUserLoginName";
    String username = "XWiki." + loginName;
    expect(store.searchDocumentsNames(anyString(), eq(0), eq(0), anyObject(List.class),
        same(getContext()))).andReturn(Arrays.asList(username)).once();

    replayDefault();
    assertNull(tokenAuthImpl.checkAuthByToken("abcde", userToken, getContext()));
    assertNull(getContext().getXWikiUser());
    assertEquals("XWiki.XWikiGuest", getContext().getUser());
    verifyDefault();
  }

  @Test
  public void test_checkAuthXWikiContext_noRequest() throws Exception {
    replayDefault();
    assertNull(tokenAuthImpl.checkAuth(getContext()));
    verifyDefault();
  }

  @Test
  public void test_checkAuthXWikiContext() throws Exception {
    String userToken = "123456789012345678901234";
    String loginName = "theUserLoginName";
    String username = "XWiki." + loginName;
    DocumentReference userDocRef = getModelUtils().resolveRef(username, DocumentReference.class);
    BaseObject userObj = new BaseObject();
    userObj.setDocumentReference(userDocRef);
    userObj.setXClassReference(Utils.getComponent(ClassDefinition.class,
        XWikiUsersClass.CLASS_DEF_HINT).getClassReference());
    userObj.setIntValue(XWikiUsersClass.FIELD_SUSPENDED.getName(), 0);
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    userDoc.setNew(false);
    userDoc.addXObject(userObj);
    XWikiRequest request = createDefaultMock(XWikiRequest.class);
    expect(request.getParameter(eq("token"))).andReturn(userToken).atLeastOnce();
    expect(request.getParameter(eq("username"))).andReturn(loginName).atLeastOnce();
    getContext().setRequest(request);
    expect(store.searchDocumentsNames(anyString(), eq(0), eq(0), anyObject(List.class),
        same(getContext()))).andReturn(Arrays.asList(username)).once();
    expect(getMock(IModelAccessFacade.class).getDocument(eq(userDocRef))).andReturn(userDoc);

    replayDefault();
    assertEquals(username, tokenAuthImpl.checkAuth(getContext()).getUser());
    assertEquals(username, getContext().getXWikiUser().getUser());
    assertEquals(username, getContext().getUser());
    verifyDefault();
  }

  @Test
  public void test_checkAuthXWikiContext_noTokenAuth_null() throws Exception {
    XWikiRequest request = createDefaultMock(XWikiRequest.class);
    expect(request.getParameter(eq("token"))).andReturn(null).atLeastOnce();
    expect(request.getParameter(eq("username"))).andReturn(null).atLeastOnce();
    expect(request.getHttpServletRequest()).andReturn(null).anyTimes();
    getContext().setRequest(request);

    replayDefault();
    assertNull(tokenAuthImpl.checkAuth(getContext()));
    assertNull(getContext().getXWikiUser());
    assertEquals("XWiki.XWikiGuest", getContext().getUser());
    verifyDefault();
  }

  @Test
  public void test_checkAuthXWikiContext_noTokenAuth_emptyString() throws Exception {
    XWikiRequest request = createDefaultMock(XWikiRequest.class);
    expect(request.getParameter(eq("token"))).andReturn("").atLeastOnce();
    expect(request.getParameter(eq("username"))).andReturn("").atLeastOnce();
    expect(request.getHttpServletRequest()).andReturn(null).anyTimes();
    getContext().setRequest(request);

    replayDefault();
    assertNull(tokenAuthImpl.checkAuth(getContext()));
    assertNull(getContext().getXWikiUser());
    assertEquals("XWiki.XWikiGuest", getContext().getUser());
    verifyDefault();
  }

  @Test
  public void test_checkAuth_suspended() throws Exception {
    String userToken = "123456789012345678901234";
    String loginName = "theUserLoginName";
    String username = "XWiki." + loginName;
    DocumentReference userDocRef = getModelUtils().resolveRef(username, DocumentReference.class);
    BaseObject userObj = new BaseObject();
    userObj.setDocumentReference(userDocRef);
    userObj.setXClassReference(Utils.getComponent(ClassDefinition.class,
        XWikiUsersClass.CLASS_DEF_HINT).getClassReference());
    userObj.setIntValue(XWikiUsersClass.FIELD_SUSPENDED.getName(), 1);
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    userDoc.setNew(false);
    userDoc.addXObject(userObj);
    XWikiRequest request = createDefaultMock(XWikiRequest.class);
    expect(request.getParameter(eq("token"))).andReturn(userToken).atLeastOnce();
    expect(request.getParameter(eq("username"))).andReturn(loginName).atLeastOnce();
    getContext().setRequest(request);
    expect(store.searchDocumentsNames(anyString(), eq(0), eq(0), anyObject(List.class),
        same(getContext()))).andReturn(Arrays.asList(username)).once();
    expect(getMock(IModelAccessFacade.class).getDocument(eq(userDocRef))).andReturn(userDoc);

    replayDefault();
    assertNull(tokenAuthImpl.checkAuth(getContext()));
    assertEquals(username, getContext().getXWikiUser().getUser());
    assertEquals(username, getContext().getUser());
    verifyDefault();
  }

}
