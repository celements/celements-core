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
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.QueryException;

import com.celements.auth.IAuthenticationServiceRole;
import com.celements.auth.user.UserInstantiationException;
import com.celements.common.test.AbstractComponentTest;
import com.celements.common.test.ExceptionAsserter;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.web.classes.oldcore.XWikiUsersClass;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PasswordClass;
import com.xpn.xwiki.web.Utils;

public class NewCelementsTokenForUserCommandTest extends AbstractComponentTest {

  private NewCelementsTokenForUserCommand cmd;

  private IAuthenticationServiceRole authServiceMock;
  private IModelAccessFacade modelAccessMock;

  @Before
  public void setUp_NewCelementsTokenForUserCommandTest() throws Exception {
    authServiceMock = registerComponentMock(IAuthenticationServiceRole.class);
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    cmd = new NewCelementsTokenForUserCommand();
  }

  @Test
  public void test_getNewCelementsTokenForUser() throws Exception {
    String token = "asdf";
    DocumentReference userDocRef = new DocumentReference("db", "XWiki", "User");
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    addUserObj(userDoc);
    expect(modelAccessMock.getDocument(userDocRef)).andReturn(userDoc);
    expect(authServiceMock.getUniqueValidationKey()).andReturn(token);
    BaseClass bClass = expectNewBaseObject(cmd.getTokenClassRef().getDocRef(
        userDocRef.getWikiReference()));
    PasswordClass pwClass = new PasswordClass();
    expect(bClass.get("tokenvalue")).andReturn(pwClass);
    modelAccessMock.saveDocument(userDoc);
    expectLastCall();

    replayDefault();
    assertEquals(token, cmd.getNewCelementsTokenForUser(userDocRef, false));
    verifyDefault();
    assertEquals(1, getTokenObjects(userDoc).count());
    assertEquals(pwClass.getEquivalentPassword("hash:SHA-512:", token), getTokenObjects(
        userDoc).first().get().getStringValue("tokenvalue"));
    assertNotNull(getTokenObjects(userDoc).first().get().getDateValue("validuntil"));
  }

  @Test
  public void test_getNewCelementsTokenForUser_XWikiGuestPlus() throws Exception {
    final DocumentReference guestDocRef = new DocumentReference("db", "XWiki", "XWikiGuest");
    DocumentReference guestPlusDocRef = new DocumentReference("db", "XWiki", "XWikiGuestPlus");
    expect(modelAccessMock.getDocument(guestPlusDocRef)).andThrow(new DocumentNotExistsException(
        guestPlusDocRef)).once();
    expectLastCall();
    replayDefault();
    new ExceptionAsserter<UserInstantiationException>(UserInstantiationException.class) {

      @Override
      protected void execute() throws Exception {
        cmd.getNewCelementsTokenForUser(guestDocRef, true);
      }
    }.evaluate();
    verifyDefault();
  }

  @Test
  public void test_removeOutdatedTokens_noTokens() throws QueryException {
    DocumentReference userDocRef = new DocumentReference("db", "XWiki", "User");
    XWikiDocument userDoc = new XWikiDocument(userDocRef);

    replayDefault();
    assertFalse(cmd.removeOutdatedTokens(userDoc));
    verifyDefault();
    assertFalse(getTokenObjects(userDoc).exists());
  }

  @Test
  public void test_removeOutdatedTokens_1new() throws QueryException {
    DocumentReference userDocRef = new DocumentReference("db", "XWiki", "User");
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    Date afterNow = new Date();
    afterNow.setTime(afterNow.getTime() + 1000000l);
    createTokenObject(userDoc, afterNow);

    replayDefault();
    assertFalse(cmd.removeOutdatedTokens(userDoc));
    verifyDefault();
    assertEquals(1, getTokenObjects(userDoc).count());
  }

  @Test
  public void test_removeOutdatedTokens_1outdated() throws QueryException {
    DocumentReference userDocRef = new DocumentReference("db", "XWiki", "User");
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    Date beforeNow = new Date();
    beforeNow.setTime(beforeNow.getTime() - 1000000l);
    createTokenObject(userDoc, beforeNow);

    replayDefault();
    assertTrue(cmd.removeOutdatedTokens(userDoc));
    verifyDefault();
    assertFalse(getTokenObjects(userDoc).exists());
  }

  @Test
  public void test_removeOutdatedTokens_multiple() throws QueryException {
    DocumentReference userDocRef = new DocumentReference("db", "XWiki", "User");
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    Date afterNow = new Date();
    afterNow.setTime(afterNow.getTime() + 1000000l);
    createTokenObject(userDoc, afterNow);
    Date beforeNow = new Date();
    beforeNow.setTime(beforeNow.getTime() - 1000000l);
    createTokenObject(userDoc, beforeNow);
    beforeNow.setTime(beforeNow.getTime() - 1000000l);
    createTokenObject(userDoc, beforeNow);

    replayDefault();
    assertTrue(cmd.removeOutdatedTokens(userDoc));
    verifyDefault();
    assertEquals(1, getTokenObjects(userDoc).count());
    assertEquals(afterNow, getTokenObjects(userDoc).first().get().getDateValue("validuntil"));
  }

  private BaseObject createTokenObject(XWikiDocument doc, Date validUntil) {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(cmd.getTokenClassRef().getDocRef(
        doc.getDocumentReference().getWikiReference()));
    obj.setDateValue("validuntil", validUntil);
    doc.addXObject(obj);
    return obj;
  }

  private XWikiObjectFetcher getTokenObjects(XWikiDocument doc) {
    return XWikiObjectFetcher.on(doc).filter(cmd.getTokenClassRef());
  }

  private BaseObject addUserObj(XWikiDocument userDoc) {
    BaseObject userObj = new BaseObject();
    userObj.setDocumentReference(userDoc.getDocumentReference());
    userObj.setXClassReference(getUserClass().getClassReference().getDocRef(
        userDoc.getDocumentReference().getWikiReference()));
    userObj.setIntValue(XWikiUsersClass.FIELD_SUSPENDED.getName(), 0);
    userDoc.addXObject(userObj);
    return userObj;
  }

  private ClassDefinition getUserClass() {
    return Utils.getComponent(ClassDefinition.class, XWikiUsersClass.CLASS_DEF_HINT);
  }

}
