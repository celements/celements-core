package com.celements.model.classes.fields;

import static org.junit.Assert.*;
import static org.mutabilitydetector.unittesting.MutabilityAssert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.TestClassDefinition;
import com.xpn.xwiki.objects.classes.StringClass;
import com.xpn.xwiki.objects.classes.TextAreaClass;

public class LargeStringFieldTest extends AbstractComponentTest {

  // test static definition
  private static final ClassField<String> STATIC_DEFINITION = new LargeStringField.Builder(
      TestClassDefinition.NAME, "name").build();

  private LargeStringField field;

  Integer rows = 10;

  @Before
  public void prepareTest() throws Exception {
    assertNotNull(STATIC_DEFINITION);
    field = new LargeStringField.Builder(TestClassDefinition.NAME, "name").rows(rows).build();
  }

  @Test
  public void test_immutability() {
    assertImmutable(LargeStringField.class);
  }

  @Test
  public void test_getters() throws Exception {
    assertEquals(rows, field.getRows());
  }

  @Test
  public void test_getXField() throws Exception {
    assertTrue(field.getXField() instanceof StringClass);
    TextAreaClass xField = (TextAreaClass) field.getXField();
    assertEquals(rows, (Integer) xField.getRows());
  }

}
