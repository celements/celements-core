package com.celements.auth.user;

import static com.celements.auth.user.UserTestUtils.*;
import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.ImmutableDocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryManager;

import com.celements.common.test.AbstractComponentTest;
import com.celements.common.test.ExceptionAsserter;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.query.IQueryExecutionServiceRole;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class CelementsUserServiceTest extends AbstractComponentTest {

  private CelementsUserService service;

  private final DocumentReference userDocRef = new ImmutableDocumentReference("db", "XWiki",
      "msladek");

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(IModelAccessFacade.class, QueryManager.class,
        IQueryExecutionServiceRole.class);
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

  @Test
  public void test_getPossibleLoginFields_none() {
    expect(getWikiMock().getXWikiPreference(eq("cellogin"), eq("celements.login.userfields"), eq(
        UserService.DEFAULT_LOGIN_FIELD), same(getContext()))).andReturn(null).once();
    replayDefault();
    assertEquals(ImmutableSet.of(UserService.DEFAULT_LOGIN_FIELD),
        service.getPossibleLoginFields());
    verifyDefault();
  }

  @Test
  public void test_getPossibleLoginFields_local() {
    expect(getWikiMock().getXWikiPreference(eq("cellogin"), eq("celements.login.userfields"), eq(
        UserService.DEFAULT_LOGIN_FIELD), same(getContext()))).andReturn("a,b").once();
    replayDefault();
    assertEquals(ImmutableSet.of("a", "b"), service.getPossibleLoginFields());
    verifyDefault();
  }

  @Test
  public void test_getPossibleLoginFields_whiteSpaces() {
    expect(getWikiMock().getXWikiPreference(eq("cellogin"), eq("celements.login.userfields"), eq(
        UserService.DEFAULT_LOGIN_FIELD), same(getContext()))).andReturn("   ").once();
    replayDefault();
    assertEquals(ImmutableSet.of(UserService.DEFAULT_LOGIN_FIELD),
        service.getPossibleLoginFields());
    verifyDefault();
  }

  @Test
  public void test_getUserForLoginField_null() throws XWikiException {
    replayDefault();
    new ExceptionAsserter<IllegalArgumentException>(IllegalArgumentException.class) {

      @Override
      protected void execute() throws Exception {
        service.getUserForLoginField(null, Collections.<String>emptyList());
      }
    }.evaluate();
    verifyDefault();
  }

  @Test
  public void test_getUserForLoginField_empty() throws XWikiException {
    replayDefault();
    new ExceptionAsserter<IllegalArgumentException>(IllegalArgumentException.class) {

      @Override
      protected void execute() throws Exception {
        service.getUserForLoginField(" \t", Collections.<String>emptyList());
      }
    }.evaluate();
    verifyDefault();
  }

  @Test
  public void test_getUserForLoginField_notExists() throws Exception {
    String login = "mSladek";
    List<String> possibleLoginFields = Arrays.asList("asdf");
    expectUserQuery(login, possibleLoginFields, Collections.<DocumentReference>emptyList());

    replayDefault();
    Optional<User> user = service.getUserForLoginField(login, possibleLoginFields);
    verifyDefault();
    assertFalse(user.isPresent());
  }

  @Test
  public void test_getUserForLoginField_name_exists() throws Exception {
    String login = "mSladek";
    createAndExpectUserDoc(service.completeUserDocRef(login));

    replayDefault();
    Optional<User> user = service.getUserForLoginField(login, null);
    verifyDefault();
    assertTrue(user.isPresent());
    assertEquals(login, user.get().getDocRef().getName());
  }

  @Test
  public void test_getUserForLoginField_name_query() throws Exception {
    String login = "mSladek";
    expect(getMock(IModelAccessFacade.class).getDocument(service.completeUserDocRef(
        login))).andThrow(new DocumentNotExistsException(service.completeUserDocRef(login)));
    expectUserQuery(login, Arrays.asList(UserService.DEFAULT_LOGIN_FIELD), Arrays.asList(
        userDocRef));

    replayDefault();
    Optional<User> user = service.getUserForLoginField(login, Collections.<String>emptyList());
    verifyDefault();
    assertTrue(user.isPresent());
    assertEquals(userDocRef, user.get().getDocRef());
  }

  @Test
  public void test_getUserForLoginField_email() throws Exception {
    String login = "maRc.sladek@synventis.com";
    List<String> possibleLoginFields = Arrays.asList("email");
    expectUserQuery(login, possibleLoginFields, Arrays.asList(userDocRef));

    replayDefault();
    Optional<User> user = service.getUserForLoginField(login, possibleLoginFields);
    verifyDefault();
    assertTrue(user.isPresent());
    assertEquals(userDocRef, user.get().getDocRef());
  }

  @Test
  public void test_getUserForLoginField_multipleResults() throws Exception {
    String login = "mSladek";
    List<String> possibleLoginFields = Arrays.asList("asdf", "fdsa");
    expectUserQuery(login, possibleLoginFields, Arrays.asList(service.completeUserDocRef(login),
        userDocRef));

    replayDefault();
    Optional<User> user = service.getUserForLoginField(login, possibleLoginFields);
    verifyDefault();
    assertFalse(user.isPresent());
  }

  private void expectUserQuery(String login, List<String> possibleLoginFields,
      List<DocumentReference> result) throws Exception {
    Query queryMock = createMockAndAddToDefault(Query.class);
    expect(getMock(QueryManager.class).createQuery(service.buildPossibleLoginXwql(
        possibleLoginFields.iterator()), Query.XWQL)).andReturn(queryMock);
    expect(queryMock.bindValue("space", "XWiki")).andReturn(queryMock);
    expect(queryMock.bindValue("login", login.toLowerCase())).andReturn(queryMock);
    expect(getMock(IQueryExecutionServiceRole.class).executeAndGetDocRefs(same(
        queryMock))).andReturn(result);
    if (result.size() == 1) {
      createAndExpectUserDoc(result.get(0));
    }
  }

  @Test
  public void test_buildPossibleLoginXwql_name() throws Exception {
    List<String> possibleLoginFields = Arrays.asList(UserService.DEFAULT_LOGIN_FIELD);
    String xwql = service.buildPossibleLoginXwql(possibleLoginFields.iterator());
    assertEquals("from doc.object(XWiki.XWikiUsers) usr where doc.space = :space and "
        + "lower(doc.name) = :login", xwql);
  }

  @Test
  public void test_buildPossibleLoginXwql_email() throws Exception {
    List<String> possibleLoginFields = Arrays.asList("email");
    String xwql = service.buildPossibleLoginXwql(possibleLoginFields.iterator());
    assertEquals("from doc.object(XWiki.XWikiUsers) usr where doc.space = :space and "
        + "lower(usr.email) = :login", xwql);
  }

  @Test
  public void test_buildPossibleLoginXwql_multipleFields() throws Exception {
    List<String> possibleLoginFields = Arrays.asList("email", UserService.DEFAULT_LOGIN_FIELD);
    String xwql = service.buildPossibleLoginXwql(possibleLoginFields.iterator());
    assertEquals("from doc.object(XWiki.XWikiUsers) usr where doc.space = :space and "
        + "lower(usr.email) = :login or lower(doc.name) = :login", xwql);
  }

}
