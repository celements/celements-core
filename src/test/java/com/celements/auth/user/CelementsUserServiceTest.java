package com.celements.auth.user;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.ImmutableDocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.common.test.ExceptionAsserter;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.classes.ClassDefinition;
import com.celements.web.classes.oldcore.XWikiUsersClass;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class CelementsUserServiceTest extends AbstractComponentTest {

  private CelementsUserService service;

  private final ImmutableDocumentReference userDocRef = new ImmutableDocumentReference("db",
      "XWiki", "msladek");

  @Before
  public void prepareTest() throws Exception {
    registerComponentMock(IModelAccessFacade.class);
    service = (CelementsUserService) Utils.getComponent(UserService.class);
  }

  @Test
  public void test_getUser() throws Exception {
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    addUserObj(userDoc);
    expect(getMock(IModelAccessFacade.class).getDocument(userDocRef)).andReturn(userDoc);

    replayDefault();
    User user = service.getUser(userDocRef);
    verifyDefault();

    assertEquals(userDocRef, user.getDocRef());
  }

  @Test
  public void test_getUser_DocumentNotExistsException() throws Exception {
    Throwable cause = new DocumentNotExistsException(userDocRef);
    expect(getMock(IModelAccessFacade.class).getDocument(userDocRef)).andThrow(cause);

    replayDefault();
    assertSame(cause, new ExceptionAsserter<UserInstantiationException>(
        UserInstantiationException.class) {

      @Override
      protected void execute() throws UserInstantiationException {
        service.getUser(userDocRef);
      }
    }.evaluate().getCause());
    verifyDefault();
  }

  @Test
  public void test_getUser_noUserObject() throws Exception {
    expect(getMock(IModelAccessFacade.class).getDocument(userDocRef)).andReturn(new XWikiDocument(
        userDocRef));

    replayDefault();
    new ExceptionAsserter<UserInstantiationException>(UserInstantiationException.class) {

      @Override
      protected void execute() throws UserInstantiationException {
        service.getUser(userDocRef);
      }
    }.evaluate();
    verifyDefault();
  }

  private BaseObject addUserObj(XWikiDocument userDoc) {
    BaseObject userObj = new BaseObject();
    userObj.setDocumentReference(userDocRef);
    userObj.setXClassReference(getUserClass().getClassReference().getDocRef(
        userDoc.getDocumentReference().getWikiReference()));
    userDoc.addXObject(userObj);
    return userObj;
  }

  private ClassDefinition getUserClass() {
    return Utils.getComponent(ClassDefinition.class, XWikiUsersClass.CLASS_DEF_HINT);
  }

}
