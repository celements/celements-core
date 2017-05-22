package com.celements.model.classes.fields.list;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;
import static org.mutabilitydetector.unittesting.MutabilityAssert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.classes.TestClassDefinition;
import com.celements.model.util.ClassFieldValue;
import com.google.common.base.Joiner;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.objects.classes.UsersClass;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.web.Utils;

public class ListOfUsersFieldTest extends AbstractComponentTest {

  private ListOfUsersField.Builder fieldBuilder;

  @Before
  public void prepareTest() throws Exception {
    fieldBuilder = new ListOfUsersField.Builder(TestClassDefinition.NAME, "name");
  }

  @Test
  public void test_immutability() {
    assertImmutable(ListOfUsersField.class);
  }

  @Test
  public void test_getXField() throws Exception {
    ListOfUsersField field = fieldBuilder.build();
    assertTrue(field.getXField() instanceof UsersClass);
    assertNull(field.getUsesList());
  }

  @Test
  public void test_resolve_serialize() throws Exception {
    ListOfUsersField field = fieldBuilder.build();
    DocumentReference classRef = field.getClassDef().getClassRef();
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    XWikiDocument doc = new XWikiDocument(classRef);
    XWikiUser user1 = new XWikiUser("XWiki.User1");
    List<XWikiUser> userList = Collections.unmodifiableList(Arrays.asList(user1));

    BaseClass bClass = expectNewBaseObject(classRef);
    expectPropertyClass(bClass, field.getName(), (PropertyClass) field.getXField());

    replayDefault();
    modelAccess.setProperty(doc, new ClassFieldValue<>(field, userList));
    List<XWikiUser> ret = modelAccess.getFieldValue(doc, field).orNull();
    verifyDefault();

    assertEquals(userList.size(), ret.size());
    assertEquals(userList.get(0).getUser(), ret.get(0).getUser());
    assertEquals(user1.getUser(), modelAccess.getXObject(doc, classRef).getStringValue(
        field.getName()));
  }

  @Test
  public void test_resolve_serialize_multiselect() throws Exception {
    ListOfUsersField field = fieldBuilder.multiSelect(true).build();
    DocumentReference classRef = field.getClassDef().getClassRef();
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    XWikiDocument doc = new XWikiDocument(classRef);
    XWikiUser user1 = new XWikiUser("XWiki.User1");
    XWikiUser user2 = new XWikiUser("XWiki.User2");
    List<XWikiUser> userList = Collections.unmodifiableList(Arrays.asList(user1, user2));

    BaseClass bClass = expectNewBaseObject(classRef);
    expectPropertyClass(bClass, field.getName(), (PropertyClass) field.getXField());

    replayDefault();
    modelAccess.setProperty(doc, new ClassFieldValue<>(field, userList));
    List<XWikiUser> ret = modelAccess.getFieldValue(doc, field).orNull();
    verifyDefault();

    assertEquals(userList.size(), ret.size());
    assertEquals(userList.get(0).getUser(), ret.get(0).getUser());
    assertEquals(userList.get(1).getUser(), ret.get(1).getUser());
    assertEquals(Joiner.on(field.getSeparator()).join(Arrays.asList(user1.getUser(),
        user2.getUser())), modelAccess.getXObject(doc, classRef).getStringValue(field.getName()));
  }

  @Test
  public void test_resolve_serialize_null() throws Exception {
    ListOfUsersField field = fieldBuilder.multiSelect(true).build();
    DocumentReference classRef = field.getClassDef().getClassRef();
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    XWikiDocument doc = new XWikiDocument(classRef);

    BaseClass bClass = expectNewBaseObject(classRef);
    expectPropertyClass(bClass, field.getName(), (PropertyClass) field.getXField());

    replayDefault();
    List<XWikiUser> ret1 = modelAccess.getFieldValue(doc, field).orNull();
    modelAccess.setProperty(doc, new ClassFieldValue<>(field, null));
    List<XWikiUser> ret2 = modelAccess.getFieldValue(doc, field).orNull();
    verifyDefault();

    assertNotNull(ret1);
    assertTrue(ret1.isEmpty());
    assertNotNull(ret2);
    assertTrue(ret2.isEmpty());
    assertTrue(modelAccess.getXObject(doc, classRef).getListValue(field.getName()).isEmpty());
  }

}
