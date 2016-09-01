package com.celements.model.classes.fields;

import static org.junit.Assert.*;
import static org.mutabilitydetector.unittesting.MutabilityAssert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.TestClassDefinition;
import com.xpn.xwiki.objects.classes.BooleanClass;

public class BooleanFieldTest extends AbstractComponentTest {

  private BooleanField field;

  String displayType = "displayType";
  Integer defaultValue = 5;

  @Before
  public void prepareTest() throws Exception {
    field = new BooleanField.Builder(TestClassDefinition.NAME, "name").displayType(
        displayType).defaultValue(defaultValue).build();
  }

  @Test
  public void test_immutability() {
    assertImmutable(BooleanField.class);
  }

  @Test
  public void test_getters() throws Exception {
    assertEquals(displayType, field.getDisplayType());
    assertEquals(defaultValue, field.getDefaultValue());
  }

  @Test
  public void test_getXField() throws Exception {
    assertTrue(field.getXField() instanceof BooleanClass);
    BooleanClass xField = (BooleanClass) field.getXField();
    assertEquals(field.getName(), xField.getName());
    assertEquals(displayType, xField.getDisplayType());
    assertEquals(defaultValue, (Integer) xField.getDefaultValue());
  }
}
