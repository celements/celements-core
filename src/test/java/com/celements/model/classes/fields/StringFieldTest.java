package com.celements.model.classes.fields;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.TestClassDefinition;
import com.xpn.xwiki.objects.classes.StringClass;

public class StringFieldTest extends AbstractComponentTest {

  // test static definition
  private static final ClassField<String> STATIC_DEFINITION = new StringField.Builder(
      TestClassDefinition.NAME, "name").build();

  private StringField field;

  Integer size = 5;

  @Before
  public void prepareTest() throws Exception {
    assertNotNull(STATIC_DEFINITION);
    field = new StringField.Builder(TestClassDefinition.NAME, "name").size(size).build();
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
