package com.celements.model.classes.fields;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.classes.fields.list.StaticListField;
import com.celements.model.util.ClassFieldValue;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.ListClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.web.Utils;

public class StringListFieldTest extends AbstractComponentTest {

  private StaticListField field;

  private boolean multiSelect = true;
  private Integer size = 5;
  private String displayType = "displayType";
  private Boolean picker = true;
  private String separator = ",";

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    field = new StaticListField(new DocumentReference("wiki", "class", "any"), "name");
    field.setMultiSelect(multiSelect);
    field.setSize(size);
    field.setDisplayType(displayType);
    field.setPicker(picker);
    field.setSeparator(separator);
  }

  @Test
  public void test_getters_setters() throws Exception {
    assertEquals(multiSelect, field.getMultiSelect());
    assertEquals(size, field.getSize());
    assertEquals(displayType, field.getDisplayType());
    assertEquals(picker, field.getPicker());
    assertEquals(separator, field.getSeparator());
    assertEquals("|", new StaticListField(field.getClassRef(), field.getName()).getSeparator());
  }

  @Test
  public void test_getXField() throws Exception {
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
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    XWikiDocument doc = new XWikiDocument(field.getClassRef());
    String value = "B";
    field.setMultiSelect(false);
    field.setValues(Arrays.asList("A", "B", "C", "D"));

    BaseClass bClass = expectNewBaseObject(field.getClassRef());
    expectPropertyClass(bClass, field.getName(), (PropertyClass) field.getXField());

    replayDefault();
    modelAccess.setProperty(doc, new ClassFieldValue<>(field, Arrays.asList(value)));
    List<String> ret = modelAccess.getProperty(doc, field);
    verifyDefault();

    assertEquals(Arrays.asList(value), ret);
    assertEquals(value, modelAccess.getXObject(doc, field.getClassRef()).getStringValue(
        field.getName()));
  }

  @Test
  public void test_resolve_serialize_multiselect() throws Exception {
    IModelAccessFacade modelAccess = Utils.getComponent(IModelAccessFacade.class);
    XWikiDocument doc = new XWikiDocument(field.getClassRef());
    List<String> value = Arrays.asList("B", "D");
    field.setMultiSelect(true);
    field.setValues(Arrays.asList("A", "B", "C", "D"));

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

}
