package com.celements.model.classes.fields;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.fields.list.ListField;
import com.celements.model.classes.fields.list.StaticListField;
import com.xpn.xwiki.objects.classes.ListClass;

public class ListFieldTest extends AbstractComponentTest {

  private ListField field;

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

}
