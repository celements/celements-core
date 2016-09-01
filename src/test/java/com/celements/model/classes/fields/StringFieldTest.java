package com.celements.model.classes.fields;

import static org.junit.Assert.*;
import static org.mutabilitydetector.unittesting.MutabilityAssert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.TestClassDefinition;
import com.xpn.xwiki.objects.classes.StringClass;

public class StringFieldTest extends AbstractComponentTest {

  private StringField field;

  Integer size = 5;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    field = new StringField.Builder(TestClassDefinition.NAME, "name").size(size).build();
  }

  @Test
  public void test_immutability() {
    assertImmutable(StringField.class);
  }

  @Test
  public void test_getters() throws Exception {
    assertEquals(size, field.getSize());
  }

  @Test
  public void test_getXField() throws Exception {
    assertTrue(field.getXField() instanceof StringClass);
    StringClass xField = (StringClass) field.getXField();
    assertEquals(size, (Integer) xField.getSize());
  }

}
