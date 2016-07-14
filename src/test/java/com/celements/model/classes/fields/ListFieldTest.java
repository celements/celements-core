package com.celements.model.classes.fields;

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
import com.celements.model.classes.fields.list.DBListField;
import com.celements.model.classes.fields.list.ListField;
import com.celements.model.classes.fields.list.StaticListField;
import com.celements.model.classes.fields.list.StringListField;
import com.celements.model.util.ClassFieldValue;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.ListClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.web.Utils;

public class ListFieldTest extends AbstractComponentTest {

  private StaticListField.Builder fieldBuilder;

  private boolean multiSelect = true;
  private Integer size = 5;
  private String displayType = "displayType";
  private Boolean picker = true;
  private String separator = ",";

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    fieldBuilder = new StaticListField.Builder(new DocumentReference("wiki", "class", "any"),
        "name").multiSelect(multiSelect).size(size).displayType(displayType).picker(
            picker).separator(separator);
  }

  @Test
  public void test_immutability() {
    assertInstancesOf(ListField.class, areImmutable(), allowingForSubclassing());
    assertInstancesOf(StringListField.class, areImmutable(), allowingForSubclassing());
    assertImmutable(StaticListField.class);
    assertImmutable(DBListField.class);
  }

  @Test
  public void test_getters() throws Exception {
    StaticListField field = fieldBuilder.build();
    assertEquals(multiSelect, field.getMultiSelect());
    assertEquals(size, field.getSize());
    assertEquals(displayType, field.getDisplayType());
    assertEquals(picker, field.getPicker());
    assertEquals(separator, field.getSeparator());
    assertEquals("|", new StaticListField.Builder(field.getClassRef(),
        field.getName()).build().getSeparator());
  }

  @Test
  public void test_getXField() throws Exception {
    StaticListField field = fieldBuilder.build();
    assertTrue(field.getXField() instanceof ListClass);
    ListClass xField = (ListClass) field.getXField();
    assertEquals(multiSelect, xField.isMultiSelect());
    assertEquals(size, (Integer) xField.getSize());
    assertEquals(displayType, xField.getDisplayType());
    assertEquals(picker, xField.isPicker());
    assertEquals(separator, xField.getSeparators());
    assertEquals(" ", xField.getSeparator()); // this is the view separator
  }

  @Test
  public void test_resolve_serialize() throws Exception {
    StaticListField field = fieldBuilder.values(Arrays.asList("A", "B", "C", "D")).build();
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    XWikiDocument doc = new XWikiDocument(field.getClassRef());
    List<String> value = Arrays.asList("B");

    BaseClass bClass = expectNewBaseObject(field.getClassRef());
    expectPropertyClass(bClass, field.getName(), (PropertyClass) field.getXField());

    replayDefault();
    modelAccess.setProperty(doc, new ClassFieldValue<>(field, value));
    List<String> ret = modelAccess.getProperty(doc, field);
    verifyDefault();

    assertEquals(value, ret);
    assertEquals(value, modelAccess.getXObject(doc, field.getClassRef()).getListValue(
        field.getName()));
  }

  @Test
  public void test_resolve_serialize_multiselect() throws Exception {
    StaticListField field = fieldBuilder.multiSelect(true).values(Arrays.asList("A", "B", "C",
        "D")).build();
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    XWikiDocument doc = new XWikiDocument(field.getClassRef());
    List<String> value = Arrays.asList("B", "D");

    BaseClass bClass = expectNewBaseObject(field.getClassRef());
    expectPropertyClass(bClass, field.getName(), (PropertyClass) field.getXField());

    replayDefault();
    modelAccess.setProperty(doc, new ClassFieldValue<>(field, value));
    List<String> ret = modelAccess.getProperty(doc, field);
    verifyDefault();

    assertEquals(value, ret);
    assertEquals(value, modelAccess.getXObject(doc, field.getClassRef()).getListValue(
        field.getName()));
  }

  @Test
  public void test_resolve_serialize_null() throws Exception {
    StaticListField field = fieldBuilder.multiSelect(true).values(Arrays.asList("A", "B", "C",
        "D")).build();
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    XWikiDocument doc = new XWikiDocument(field.getClassRef());

    BaseClass bClass = expectNewBaseObject(field.getClassRef());
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
    assertTrue(modelAccess.getXObject(doc, field.getClassRef()).getListValue(
        field.getName()).isEmpty());
  }

}
