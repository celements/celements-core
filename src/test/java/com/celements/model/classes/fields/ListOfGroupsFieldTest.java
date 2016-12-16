package com.celements.model.classes.fields;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;
import static org.mutabilitydetector.unittesting.MutabilityAssert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.classes.fields.list.ListOfGroupsField;
import com.celements.model.util.ClassFieldValue;
import com.celements.web.classes.oldcore.XWikiRightsClass;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.GroupsClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.web.Utils;

public class ListOfGroupsFieldTest extends AbstractComponentTest {

  private ListOfGroupsField.Builder builder;

  @Before
  public void prepareTest() throws Exception {
    builder = new ListOfGroupsField.Builder(XWikiRightsClass.CLASS_FN,
        XWikiRightsClass.FIELD_GROUPS.getName());
  }

  @Test
  public void test_immutability() {
    assertImmutable(ListOfGroupsField.class);
  }

  @Test
  public void test_getters_null() throws Exception {
    assertNull(builder.build().getUsesList());
  }

  @Test
  public void test_getters() throws Exception {
    assertFalse(builder.usesList(false).build().getUsesList());
  }

  @Test
  public void test_getXField() throws Exception {
    ListOfGroupsField field = builder.usesList(true).build();
    assertTrue(field.getXField() instanceof GroupsClass);
    assertTrue(field.getUsesList());
  }

  @Test
  public void test_resolve_serialize() throws Exception {
    ListOfGroupsField field = builder.usesList(true).build();
    DocumentReference classRef = field.getClassDef().getClassRef();
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    XWikiDocument doc = new XWikiDocument(classRef);
    String value = "XWiki.TestGroup";

    BaseClass bClass = expectNewBaseObject(classRef);
    expectPropertyClass(bClass, field.getName(), (PropertyClass) field.getXField());

    replayDefault();
    modelAccess.setProperty(doc, new ClassFieldValue<>(field, Arrays.asList(value)));
    List<String> ret = modelAccess.getProperty(doc, field);
    verifyDefault();

    assertEquals(Arrays.asList(value), ret);
    assertEquals(value, modelAccess.getXObject(doc, classRef).getStringValue(field.getName()));
  }

  @Test
  public void test_resolve_serialize_multiselect() throws Exception {
    ListOfGroupsField field = builder.usesList(true).multiSelect(true).build();
    DocumentReference classRef = field.getClassDef().getClassRef();
    System.out.println("classRef = " + classRef);
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    XWikiDocument doc = new XWikiDocument(classRef);
    List<String> value = Arrays.asList("XWiki.TestGroup", "XWiki.TestGroup2");

    BaseClass bClass = expectNewBaseObject(classRef);
    expectPropertyClass(bClass, field.getName(), (PropertyClass) field.getXField());
    replayDefault();
    System.out.println("bClass.ref = " + bClass.getXClassReference());
    modelAccess.setProperty(doc, new ClassFieldValue<>(field, value));
    List<String> ret = modelAccess.getProperty(doc, field);
    verifyDefault();

    assertEquals(value, ret);
    BaseObject obj = modelAccess.getXObject(doc, classRef);
    assertEquals(Joiner.on(',').join(value), obj.getStringValue(field.getName()));
    Optional<List<String>> rights = modelAccess.getFieldValue(obj, XWikiRightsClass.FIELD_GROUPS);
    assertTrue(rights.isPresent());
    assertEquals(value.size(), rights.get().size());
    assertEquals(value.get(0), rights.get().get(0));
    assertEquals(value.get(1), rights.get().get(1));
  }

  @Test
  public void test_resolve_serialize_null() throws Exception {
    ListOfGroupsField field = builder.usesList(true).multiSelect(true).build();
    DocumentReference classRef = field.getClassDef().getClassRef();
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    XWikiDocument doc = new XWikiDocument(classRef);

    BaseClass bClass = expectNewBaseObject(classRef);
    expectPropertyClass(bClass, field.getName(), (PropertyClass) field.getXField());

    replayDefault();
    List<String> ret1 = modelAccess.getProperty(doc, field);
    modelAccess.setProperty(doc, new ClassFieldValue<>(field, null));
    List<String> ret2 = modelAccess.getProperty(doc, field);
    verifyDefault();

    assertNotNull(ret1);
    assertTrue(ret1.isEmpty());
    assertNotNull(ret2);
    assertTrue(ret2.isEmpty());
    assertTrue(modelAccess.getXObject(doc, classRef).getStringValue(field.getName()).isEmpty());
  }

}
