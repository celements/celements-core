package com.celements.model.classes.fields.list;

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
import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.TestClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.util.ClassFieldValue;
import com.celements.web.classes.oldcore.XWikiRightsClass;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.web.Utils;

public class ComponentListFieldTest extends AbstractComponentTest {

  // test static definition
  private static final ClassField<List<ClassDefinition>> STATIC_DEFINITION = new ComponentListField.Builder<>(
      TestClassDefinition.NAME, "name", ClassDefinition.class).build();

  private ComponentListField.Builder<ClassDefinition> fieldBuilder;

  @Before
  public void prepareTest() throws Exception {
    assertNotNull(STATIC_DEFINITION);
    fieldBuilder = new ComponentListField.Builder<>(TestClassDefinition.NAME, "name",
        ClassDefinition.class);
  }

  @Test
  public void test_immutability() {
    assertImmutable(ComponentListField.class);
  }

  @Test
  public void test_getValues() throws Exception {
    ComponentListField<ClassDefinition> field = fieldBuilder.build();
    assertEquals(Utils.getComponentList(ClassDefinition.class), field.getValues());
  }

  @Test
  public void test_resolve_serialize() throws Exception {
    ComponentListField<ClassDefinition> field = fieldBuilder.build();
    DocumentReference classRef = field.getClassDef().getClassRef();
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    XWikiDocument doc = new XWikiDocument(classRef);
    ClassDefinition value = getClassDef(TestClassDefinition.NAME);

    BaseClass bClass = expectNewBaseObject(classRef);
    expectPropertyClass(bClass, field.getName(), (PropertyClass) field.getXField());

    replayDefault();
    modelAccess.setProperty(doc, new ClassFieldValue<>(field, Arrays.asList(value)));
    List<ClassDefinition> ret = modelAccess.getFieldValue(doc, field).orNull();
    verifyDefault();

    assertEquals(Arrays.asList(value), ret);
    assertEquals(value.getName(), modelAccess.getXObject(doc, classRef).getStringValue(
        field.getName()));
  }

  @Test
  public void test_resolve_serialize_multiselect() throws Exception {
    ComponentListField<ClassDefinition> field = fieldBuilder.multiSelect(true).build();
    DocumentReference classRef = field.getClassDef().getClassRef();
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    XWikiDocument doc = new XWikiDocument(classRef);
    List<ClassDefinition> value = Arrays.asList(getClassDef(TestClassDefinition.NAME), getClassDef(
        XWikiRightsClass.CLASS_DEF_HINT));

    BaseClass bClass = expectNewBaseObject(classRef);
    expectPropertyClass(bClass, field.getName(), (PropertyClass) field.getXField());

    replayDefault();
    modelAccess.setProperty(doc, new ClassFieldValue<>(field, value));
    List<ClassDefinition> ret = modelAccess.getFieldValue(doc, field).orNull();
    verifyDefault();

    assertEquals(value, ret);
    assertEquals(TestClassDefinition.NAME, modelAccess.getXObject(doc, classRef).getListValue(
        field.getName()).get(0));
    assertEquals(XWikiRightsClass.CLASS_DEF_HINT, modelAccess.getXObject(doc,
        classRef).getListValue(field.getName()).get(1));
  }

  @Test
  public void test_resolve_serialize_null() throws Exception {
    ComponentListField<ClassDefinition> field = fieldBuilder.multiSelect(true).build();
    DocumentReference classRef = field.getClassDef().getClassRef();
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    XWikiDocument doc = new XWikiDocument(classRef);

    BaseClass bClass = expectNewBaseObject(classRef);
    expectPropertyClass(bClass, field.getName(), (PropertyClass) field.getXField());

    replayDefault();
    List<ClassDefinition> ret1 = modelAccess.getFieldValue(doc, field).orNull();
    modelAccess.setProperty(doc, new ClassFieldValue<>(field, null));
    List<ClassDefinition> ret2 = modelAccess.getFieldValue(doc, field).orNull();
    verifyDefault();

    assertNotNull(ret1);
    assertTrue(ret1.isEmpty());
    assertNotNull(ret2);
    assertTrue(ret2.isEmpty());
    assertTrue(modelAccess.getXObject(doc, classRef).getListValue(field.getName()).isEmpty());
  }

  private ClassDefinition getClassDef(String hint) {
    return Utils.getComponent(ClassDefinition.class, hint);
  }

}
