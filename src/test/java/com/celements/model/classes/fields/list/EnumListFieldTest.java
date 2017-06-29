package com.celements.model.classes.fields.list;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;
import static org.mutabilitydetector.unittesting.AllowedReason.*;
import static org.mutabilitydetector.unittesting.MutabilityAssert.*;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.classes.TestClassDefinition;
import com.celements.model.util.ClassFieldValue;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.objects.classes.StaticListClass;
import com.xpn.xwiki.web.Utils;

public class EnumListFieldTest extends AbstractComponentTest {

  private enum TestEnum {
    A, B, C, D;
  }

  private EnumListField.Builder<TestEnum> fieldBuilder;

  @Before
  public void prepareTest() throws Exception {
    fieldBuilder = new EnumListField.Builder<>(TestClassDefinition.NAME, "name", TestEnum.class);
  }

  @Test
  public void test_immutability() {
    assertInstancesOf(EnumListField.class, areImmutable(), allowingForSubclassing());
    assertImmutable(AccessRightLevelsField.class);
  }

  @Test
  public void test_getXField() throws Exception {
    EnumListField<TestEnum> field = fieldBuilder.build();
    assertTrue(field.getXField() instanceof StaticListClass);
    StaticListClass xField = (StaticListClass) field.getXField();
    assertEquals("A|B|C|D", xField.getValues());
  }

  @Test
  public void test_resolve_serialize() throws Exception {
    EnumListField<TestEnum> field = fieldBuilder.build();
    DocumentReference classRef = field.getClassDef().getClassRef();
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    XWikiDocument doc = new XWikiDocument(classRef);
    TestEnum value = TestEnum.B;

    BaseClass bClass = expectNewBaseObject(classRef);
    expectPropertyClass(bClass, field.getName(), (PropertyClass) field.getXField());

    replayDefault();
    modelAccess.setProperty(doc, new ClassFieldValue<>(field, Arrays.asList(value)));
    List<TestEnum> ret = modelAccess.getFieldValue(doc, field).orNull();
    verifyDefault();

    assertEquals(Arrays.asList(value), ret);
    assertEquals(value.name(), modelAccess.getXObject(doc, classRef).getStringValue(
        field.getName()));
  }

  @Test
  public void test_resolve_serialize_multiselect() throws Exception {
    EnumListField<TestEnum> field = fieldBuilder.multiSelect(true).build();
    DocumentReference classRef = field.getClassDef().getClassRef();
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    XWikiDocument doc = new XWikiDocument(classRef);
    List<TestEnum> value = Arrays.asList(TestEnum.B, TestEnum.D);

    BaseClass bClass = expectNewBaseObject(classRef);
    expectPropertyClass(bClass, field.getName(), (PropertyClass) field.getXField());

    replayDefault();
    modelAccess.setProperty(doc, new ClassFieldValue<>(field, value));
    List<TestEnum> ret = modelAccess.getFieldValue(doc, field).orNull();
    verifyDefault();

    assertEquals(value, ret);
    assertEquals(Arrays.asList("B", "D"), modelAccess.getXObject(doc, classRef).getListValue(
        field.getName()));
  }

  @Test
  public void test_resolve_serialize_null() throws Exception {
    EnumListField<TestEnum> field = fieldBuilder.multiSelect(true).build();
    DocumentReference classRef = field.getClassDef().getClassRef();
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    XWikiDocument doc = new XWikiDocument(classRef);

    BaseClass bClass = expectNewBaseObject(classRef);
    expectPropertyClass(bClass, field.getName(), (PropertyClass) field.getXField());

    replayDefault();
    List<TestEnum> ret1 = modelAccess.getFieldValue(doc, field).orNull();
    modelAccess.setProperty(doc, new ClassFieldValue<>(field, null));
    List<TestEnum> ret2 = modelAccess.getFieldValue(doc, field).orNull();
    verifyDefault();

    assertNotNull(ret1);
    assertTrue(ret1.isEmpty());
    assertNotNull(ret2);
    assertTrue(ret2.isEmpty());
    assertTrue(modelAccess.getXObject(doc, classRef).getListValue(field.getName()).isEmpty());
  }

}
