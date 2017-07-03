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
import com.celements.model.classes.fields.ClassField;
import com.celements.model.util.ClassFieldValue;
import com.celements.rights.access.EAccessLevel;
import com.google.common.base.Joiner;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.LevelsClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.web.Utils;

public class AccessRightLevelsFieldTest extends AbstractComponentTest {

  // test static definition
  private static final ClassField<List<EAccessLevel>> STATIC_DEFINITION = new AccessRightLevelsField.Builder(
      TestClassDefinition.NAME, "name").build();

  private AccessRightLevelsField.Builder fieldBuilder;

  @Before
  public void prepareTest() throws Exception {
    fieldBuilder = new AccessRightLevelsField.Builder(TestClassDefinition.NAME, "name");
  }

  @Test
  public void test_immutability() {
    assertNotNull(STATIC_DEFINITION);
    assertInstancesOf(EnumListField.class, areImmutable(), allowingForSubclassing());
  }

  @Test
  public void test_getXField() throws Exception {
    ClassField<List<EAccessLevel>> field = fieldBuilder.build();
    System.out.println(field.getXField());
    assertTrue(field.getXField() instanceof LevelsClass);
  }

  @Test
  public void test_resolve_serialize() throws Exception {
    ClassField<List<EAccessLevel>> field = fieldBuilder.build();
    DocumentReference classRef = field.getClassDef().getClassRef();
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    XWikiDocument doc = new XWikiDocument(classRef);
    EAccessLevel value = EAccessLevel.VIEW;

    BaseClass bClass = expectNewBaseObject(classRef);
    expectPropertyClass(bClass, field.getName(), (PropertyClass) field.getXField());

    replayDefault();
    modelAccess.setProperty(doc, new ClassFieldValue<>(field, Arrays.asList(value)));
    List<EAccessLevel> ret = modelAccess.getProperty(doc, field);
    verifyDefault();

    assertEquals(Arrays.asList(value), ret);
    assertEquals(value.getIdentifier(), modelAccess.getXObject(doc, classRef).getStringValue(
        field.getName()));
  }

  @Test
  public void test_resolve_serialize_multiselect() throws Exception {
    ClassField<List<EAccessLevel>> field = fieldBuilder.multiSelect(true).separator(",").build();
    DocumentReference classRef = field.getClassDef().getClassRef();
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    XWikiDocument doc = new XWikiDocument(classRef);
    List<EAccessLevel> value = Arrays.asList(EAccessLevel.VIEW, EAccessLevel.EDIT);

    BaseClass bClass = expectNewBaseObject(classRef);
    expectPropertyClass(bClass, field.getName(), (PropertyClass) field.getXField());

    replayDefault();
    modelAccess.setProperty(doc, new ClassFieldValue<>(field, value));
    List<EAccessLevel> ret = modelAccess.getProperty(doc, field);
    verifyDefault();

    assertEquals(value, ret);
    assertEquals(Joiner.on(',').join(EAccessLevel.VIEW.getIdentifier(),
        EAccessLevel.EDIT.getIdentifier()), modelAccess.getXObject(doc, classRef).getStringValue(
            field.getName()));
  }

  @Test
  public void test_resolve_serialize_null() throws Exception {
    ClassField<List<EAccessLevel>> field = fieldBuilder.multiSelect(true).build();
    DocumentReference classRef = field.getClassDef().getClassRef();
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    XWikiDocument doc = new XWikiDocument(classRef);

    BaseClass bClass = expectNewBaseObject(classRef);
    expectPropertyClass(bClass, field.getName(), (PropertyClass) field.getXField());

    replayDefault();
    List<EAccessLevel> ret1 = modelAccess.getProperty(doc, field);
    modelAccess.setProperty(doc, new ClassFieldValue<>(field, null));
    List<EAccessLevel> ret2 = modelAccess.getProperty(doc, field);
    verifyDefault();

    assertNotNull(ret1);
    assertTrue(ret1.isEmpty());
    assertNotNull(ret2);
    assertTrue(ret2.isEmpty());
    assertTrue(modelAccess.getXObject(doc, classRef).getStringValue(field.getName()).isEmpty());
  }

}
