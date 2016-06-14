package com.celements.model.classes.fields;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.fields.number.IntField;
import com.celements.model.classes.fields.number.NumberField;
import com.xpn.xwiki.objects.classes.StringClass;

public class NumberFieldTest extends AbstractComponentTest {

  private NumberField<Integer> field;

  Integer size = 5;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    field = new IntField(new DocumentReference("wiki", "class", "any"), "name");
    field.setSize(size);
  }

  @Test
  public void test_getters_setters() throws Exception {
    assertEquals(size, field.getSize());
  }

  @Test
  public void test_getXField() throws Exception {
    assertTrue(field.getXField() instanceof StringClass);
    StringClass xField = (StringClass) field.getXField();
    assertEquals(size, (Integer) xField.getSize());
  }

}
