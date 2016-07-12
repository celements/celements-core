package com.celements.model.classes.fields;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.fields.number.IntField;
import com.celements.model.classes.fields.number.NumberField;
import com.xpn.xwiki.objects.classes.NumberClass;

public class NumberFieldTest extends AbstractComponentTest {

  private NumberField<Integer> field;

  Integer size = 5;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    field = new IntField.Builder(new DocumentReference("wiki", "class", "any"), "name").size(
        size).build();
  }

  @Test
  public void test_getters() throws Exception {
    assertEquals(size, field.getSize());
  }

  @Test
  public void test_getXField() throws Exception {
    assertTrue(field.getXField() instanceof NumberClass);
    NumberClass xField = (NumberClass) field.getXField();
    assertEquals(size, (Integer) xField.getSize());
  }

}
