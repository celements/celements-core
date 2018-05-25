package com.celements.auth.user;

import static com.celements.auth.user.CelementsUserService.*;
import static com.celements.auth.user.UserTestUtils.*;
import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.ImmutableDocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryManager;

import com.celements.common.test.AbstractComponentTest;
import com.celements.common.test.ExceptionAsserter;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.ModelAccessStrategy;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.access.exception.DocumentSaveException;
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.query.IQueryExecutionServiceRole;
import com.celements.rights.access.EAccessLevel;
import com.celements.web.classes.oldcore.XWikiGroupsClass;
import com.celements.web.classes.oldcore.XWikiRightsClass;
import com.celements.web.classes.oldcore.XWikiUsersClass;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.user.api.XWikiGroupService;
import com.xpn.xwiki.web.Utils;

public class CelementsUserServiceTest extends AbstractComponentTest {

  private CelementsUserService service;

  private final DocumentReference userDocRef = new ImmutableDocumentReference("xwikidb", "XWiki",
      "msladek");

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(ModelAccessStrategy.class, IWebUtilsService.class, QueryManager.class,
        IQueryExecutionServiceRole.class);
    service = (CelementsUserService) Utils.getComponent(UserService.class);
    expect(getMock(IWebUtilsService.class).getAdminMessageTool()).andReturn(
        getMessageToolStub()).anyTimes();
    expect(getWikiMock().getGroupService(getContext())).andReturn(createMockAndAddToDefault(
        XWikiGroupService.class)).anyTimes();
    getMessageToolStub().injectMessage("core.comment.createdUser", "user created");
    getMessageToolStub().injectMessage("core.comment.addedUserToGroup", "user added to group");
  }

  @Test
  public void test_resolveUserDocRef() throws Exception {
    assertEquals(userDocRef, service.resolveUserDocRef("msladek"));
    assertEquals(userDocRef, service.resolveUserDocRef("XWiki.msladek"));
    assertEquals(userDocRef, service.resolveUserDocRef("Space.msladek"));
    assertEquals(userDocRef, service.resolveUserDocRef("xwikidb:Space.msladek"));
    assertEquals(new DocumentReference("otherdb", "XWiki", "msladek"), service.resolveUserDocRef(
        "otherdb:Space.msladek"));
  }

  @Test
  public void test_getUser() throws Exception {
    XWikiDocument userDoc = expectDoc(userDocRef);
    addUserObj(userDoc);

    replayDefault();
    User user = service.getUser(userDocRef);
    verifyDefault();

    assertEquals(userDocRef, user.getDocRef());
  }

  @Test
  public void test_getUser_DocumentNotExistsException() throws Exception {
    expect(getMock(ModelAccessStrategy.class).exists(userDocRef, "")).andReturn(false);

    replayDefault();
    assertSame(DocumentNotExistsException.class, new ExceptionAsserter<UserInstantiationException>(
        UserInstantiationException.class) {

      @Override
      protected void execute() throws UserInstantiationException {
        service.getUser(userDocRef);
      }
    }.evaluate().getCause().getClass());
    verifyDefault();
  }

  @Test
  public void test_getUser_noUserObject() throws Exception {
    expectDoc(userDocRef);

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
  public void test_getPossibleLoginFields() {
    expectPossibleLoginFields("validkey,invalid,loginname,illegal,email");
    replayDefault();
    assertEquals(ImmutableSet.of("validkey", "loginname", "email"),
        service.getPossibleLoginFields());
    verifyDefault();
  }

  @Test
  public void test_getPossibleLoginFields_none() {
    expectPossibleLoginFields(null);
    replayDefault();
    assertEquals(ImmutableSet.of(UserService.DEFAULT_LOGIN_FIELD),
        service.getPossibleLoginFields());
    verifyDefault();
  }

  @Test
  public void test_getPossibleLoginFields_whiteSpaces() {
    expectPossibleLoginFields("   ");
    replayDefault();
    assertEquals(ImmutableSet.of(UserService.DEFAULT_LOGIN_FIELD),
        service.getPossibleLoginFields());
    verifyDefault();
  }

  private void expectPossibleLoginFields(String fields) {
    expect(getWikiMock().getXWikiPreference(eq("cellogin"), eq("celements.login.userfields"), eq(
        UserService.DEFAULT_LOGIN_FIELD), same(getContext()))).andReturn(fields).once();
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
    List<String> possibleLoginFields = Arrays.asList("email");
    expectUserQuery(login, possibleLoginFields, Collections.<DocumentReference>emptyList());

    replayDefault();
    Optional<User> user = service.getUserForLoginField(login, possibleLoginFields);
    verifyDefault();
    assertFalse(user.isPresent());
  }

  @Test
  public void test_getUserForLoginField_name_exists() throws Exception {
    String login = "mSladek";
    XWikiDocument doc = expectDoc(service.resolveUserDocRef(login));
    addUserObj(doc);

    replayDefault();
    Optional<User> user = service.getUserForLoginField(login, null);
    verifyDefault();
    assertTrue(user.isPresent());
    assertEquals(login, user.get().getDocRef().getName());
  }

  @Test
  public void test_getUserForLoginField_name_query() throws Exception {
    String login = "mSladek";
    expect(getMock(ModelAccessStrategy.class).exists(service.resolveUserDocRef(login),
        "")).andReturn(false);
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
    List<String> possibleLoginFields = Arrays.asList("email", "validkey");
    expectUserQuery(login, possibleLoginFields, Arrays.asList(service.resolveUserDocRef(login),
        userDocRef));

    replayDefault();
    Optional<User> user = service.getUserForLoginField(login, possibleLoginFields);
    verifyDefault();
    assertFalse(user.isPresent());
  }

  @Test
  public void test_getUserForLoginField_invalidField() throws Exception {
    String login = "mSladek";
    expect(getMock(ModelAccessStrategy.class).exists(service.resolveUserDocRef(login),
        "")).andReturn(false);
    expectUserQuery(login, Arrays.asList(UserService.DEFAULT_LOGIN_FIELD),
        Collections.<DocumentReference>emptyList());

    replayDefault();
    Optional<User> user = service.getUserForLoginField(login, Arrays.asList("asdf"));
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
      XWikiDocument doc = expectDoc(result.get(0));
      addUserObj(doc);
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

  @Test
  public void test_getOrGenerateUserDocRef() throws Exception {
    String accountName = "msladek";
    expect(getMock(ModelAccessStrategy.class).exists(userDocRef, "")).andReturn(false);

    replayDefault();
    DocumentReference docRef = service.getOrGenerateUserDocRef(accountName);
    verifyDefault();

    assertEquals(userDocRef, docRef);
  }

  @Test
  public void test_getOrGenerateUserDocRef_alreadyExists() throws Exception {
    String accountName = "msladek";
    expect(getMock(ModelAccessStrategy.class).exists(userDocRef, "")).andReturn(true);
    expect(getMock(ModelAccessStrategy.class).exists(anyObject(DocumentReference.class), eq(
        ""))).andReturn(false);

    replayDefault();
    DocumentReference docRef = service.getOrGenerateUserDocRef(accountName);
    verifyDefault();

    assertNotEquals(userDocRef, docRef);
    assertEquals(service.getUserSpaceRef(), docRef.getLastSpaceReference());
    assertEquals(12, docRef.getName().length());
  }

  @Test
  public void test_getOrGenerateUserDocRef_noAccountName() throws Exception {
    expect(getMock(ModelAccessStrategy.class).exists(anyObject(DocumentReference.class), eq(
        ""))).andReturn(false);

    replayDefault();
    DocumentReference docRef = service.getOrGenerateUserDocRef(null);
    verifyDefault();

    assertNotEquals(userDocRef, docRef);
    assertEquals(service.getUserSpaceRef(), docRef.getLastSpaceReference());
    assertEquals(12, docRef.getName().length());
  }

  @Test
  public void test_createUserFromData() throws Exception {
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    Map<String, String> userData = new HashMap<>();
    expectUserClassFromMap(userData);

    replayDefault();
    service.createUserFromData(userDoc, userData);
    verifyDefault();

    assertEquals(getUserClass().getDocRef(), userDoc.getParentReference());
    assertEquals("XWiki.msladek", userDoc.getCreator());
    assertEquals("XWiki.msladek", userDoc.getAuthor());
    assertEquals("#includeForm(\"XWiki.XWikiUserSheet\")", userDoc.getContent());
    assertEquals(1, XWikiObjectFetcher.on(userDoc).filter(getUserClass()).count());
    assertEquals("0", userData.get(XWikiUsersClass.FIELD_ACTIVE.getName()));
    assertEquals(24, userData.get(XWikiUsersClass.FIELD_PASSWORD.getName()).length());
  }

  @Test
  public void test_setRightsOnUser() throws Exception {
    List<EAccessLevel> levels = Arrays.asList(EAccessLevel.VIEW, EAccessLevel.EDIT,
        EAccessLevel.DELETE);
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    expectClassWithNewObj(getRightsClass(), userDocRef.getWikiReference());

    replayDefault();
    service.setRightsOnUser(userDoc, levels);
    verifyDefault();

    List<BaseObject> rightsObs = XWikiObjectFetcher.on(userDoc).filter(getRightsClass()).list();
    assertEquals(2, rightsObs.size());
    assertEquals("XWiki.msladek", getValue(rightsObs.get(0), XWikiRightsClass.FIELD_USERS).get(
        0).getUser());
    assertEquals(levels, getValue(rightsObs.get(0), XWikiRightsClass.FIELD_LEVELS));
    assertTrue(getValue(rightsObs.get(0), XWikiRightsClass.FIELD_ALLOW));
    assertEquals(XWIKI_ADMIN_GROUP_FN, getValue(rightsObs.get(1),
        XWikiRightsClass.FIELD_GROUPS).get(0));
    assertEquals(levels, getValue(rightsObs.get(1), XWikiRightsClass.FIELD_LEVELS));
    assertTrue(getValue(rightsObs.get(1), XWikiRightsClass.FIELD_ALLOW));
  }

  @Test
  public void test_addUserToDefaultGroups() throws Exception {
    List<String> groups = Arrays.asList(XWIKI_ADMIN_GROUP_FN, "XWiki.OtherGroup");
    expectInitialGroups(groups);
    XWikiDocument admGrpDoc = expectGroupAdd(groups.get(0));
    XWikiDocument othGrpDoc = expectGroupAdd(groups.get(1));

    replayDefault();
    service.addUserToDefaultGroups(userDocRef);
    verifyDefault();
    List<BaseObject> admGrpObjs = XWikiObjectFetcher.on(admGrpDoc).filter(getGroupsClass()).list();
    assertEquals(1, admGrpObjs.size());
    assertEquals(userDocRef, getValue(admGrpObjs.get(0), XWikiGroupsClass.FIELD_MEMBER));
    List<BaseObject> othGrpObjs = XWikiObjectFetcher.on(othGrpDoc).filter(getGroupsClass()).list();
    assertEquals(1, othGrpObjs.size());
    assertEquals(userDocRef, getValue(othGrpObjs.get(0), XWikiGroupsClass.FIELD_MEMBER));
  }

  private XWikiDocument expectGroupAdd(String group) throws Exception {
    XWikiDocument grpDoc = expectDoc(DOC_REF_RESOLVER.apply(group));
    expectClassWithNewObj(getGroupsClass(), userDocRef.getWikiReference());
    getMock(ModelAccessStrategy.class).saveDocument(same(grpDoc), anyObject(String.class), eq(
        false));
    getMock(XWikiGroupService.class).addUserToGroup("XWiki.msladek",
        userDocRef.getWikiReference().getName(), group, getContext());
    return grpDoc;
  }

  @Test
  public void test_createNewUser() throws Exception {
    Map<String, String> userData = new HashMap<>();
    userData.put("xwikiname", "msladek");
    XWikiDocument userDoc = new XWikiDocument(userDocRef);

    expect(getMock(ModelAccessStrategy.class).exists(userDocRef, "")).andReturn(false).times(2);
    expect(getMock(ModelAccessStrategy.class).createDocument(userDocRef, "")).andReturn(userDoc);
    expectPossibleLoginFields(null);
    expectUserClassFromMap(userData);
    expectClassWithNewObj(getRightsClass(), userDocRef.getWikiReference());
    getMock(ModelAccessStrategy.class).saveDocument(same(userDoc), anyObject(String.class), eq(
        false));
    expectInitialGroups(Collections.<String>emptyList());

    expect(getMock(ModelAccessStrategy.class).exists(userDocRef, "")).andReturn(true);
    expect(getMock(ModelAccessStrategy.class).getDocument(userDocRef, "")).andReturn(userDoc);

    replayDefault();
    User user = service.createNewUser(userData, false);
    verifyDefault();

    assertSame(userDoc, user.getDocument());
    assertEquals(1, XWikiObjectFetcher.on(userDoc).filter(getUserClass()).count());
    assertEquals(2, XWikiObjectFetcher.on(userDoc).filter(getRightsClass()).count());
  }

  @Test
  public void test_createNewUser_Exception() throws Exception {
    Throwable cause = new DocumentSaveException(userDocRef);
    final Map<String, String> userData = new HashMap<>();
    userData.put("xwikiname", "msladek");
    XWikiDocument userDoc = new XWikiDocument(userDocRef);

    expect(getMock(ModelAccessStrategy.class).exists(userDocRef, "")).andReturn(false).times(2);
    expect(getMock(ModelAccessStrategy.class).createDocument(userDocRef, "")).andReturn(userDoc);
    expectPossibleLoginFields(null);
    expectUserClassFromMap(userData);
    expectClassWithNewObj(getRightsClass(), userDocRef.getWikiReference());
    getMock(ModelAccessStrategy.class).saveDocument(same(userDoc), anyObject(String.class), eq(
        false));
    expectLastCall().andThrow(cause);

    replayDefault();
    assertSame(cause, new ExceptionAsserter<UserCreateException>(UserCreateException.class) {

      @Override
      protected void execute() throws Exception {
        service.createNewUser(userData, false);
      }
    }.evaluate().getCause());
    verifyDefault();
  }

  private void expectInitialGroups(List<String> groups) {
    expect(getWikiMock().getXWikiPreference(eq("initialGroups"), anyObject(String.class), anyObject(
        String.class), same(getContext()))).andReturn(Joiner.on(',').join(groups));
  }

  private BaseClass expectUserClassFromMap(Map<String, String> userData) throws XWikiException {
    BaseClass userXClass = expectClassWithNewObj(getUserClass(), userDocRef.getWikiReference());
    expect(getWikiMock().getUserClass(getContext())).andReturn(userXClass);
    expect(userXClass.fromMap(same(userData), anyObject(BaseObject.class))).andReturn(
        new BaseObject());
    return userXClass;
  }

  private static XWikiDocument expectDoc(DocumentReference docRef) {
    XWikiDocument doc = new XWikiDocument(docRef);
    expect(getMock(ModelAccessStrategy.class).exists(docRef, "")).andReturn(true);
    expect(getMock(ModelAccessStrategy.class).getDocument(docRef, "")).andReturn(doc);
    return doc;
  }

  private static <T> T getValue(BaseObject obj, ClassField<T> field) {
    return Utils.getComponent(IModelAccessFacade.class).getFieldValue(obj, field).get();
  }

  private static ClassDefinition getUserClass() {
    return Utils.getComponent(ClassDefinition.class, XWikiUsersClass.CLASS_DEF_HINT);
  }

  private static ClassDefinition getGroupsClass() {
    return Utils.getComponent(ClassDefinition.class, XWikiGroupsClass.CLASS_DEF_HINT);
  }

  private static ClassDefinition getRightsClass() {
    return Utils.getComponent(ClassDefinition.class, XWikiRightsClass.CLASS_DEF_HINT);
  }

  private static BaseClass expectClassWithNewObj(ClassDefinition classDef, WikiReference wikiRef)
      throws XWikiException {
    BaseClass bClass = expectNewBaseObject(classDef.getDocRef(wikiRef));
    for (ClassField<?> field : classDef.getFields()) {
      expect(bClass.get(field.getName())).andReturn(field.getXField()).anyTimes();
    }
    return bClass;
  }

}
