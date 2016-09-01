package com.celements.model.classes.fields;

import static org.junit.Assert.*;
import static org.mutabilitydetector.unittesting.MutabilityAssert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.TestClassDefinition;
import com.xpn.xwiki.objects.classes.DateClass;

public class DateFieldTest extends AbstractComponentTest {

  private DateField field;

  Integer size = 5;
  Integer emptyIsToday = 3;
  String dateFormat = "dateFormat";

  @Before
  public void prepareTest() throws Exception {
    field = new DateField.Builder(TestClassDefinition.NAME, "name").size(size).emptyIsToday(
        emptyIsToday).dateFormat(dateFormat).build();
  }

  @Test
  public void test_immutability() {
    assertImmutable(DateField.class);
  }

  @Test
  public void test_getters() throws Exception {
    assertEquals(size, field.getSize());
    assertEquals(emptyIsToday, field.getEmptyIsToday());
    assertEquals(dateFormat, field.getDateFormat());
  }

  @Test
  public void test_getXField() throws Exception {
    assertTrue(field.getXField() instanceof DateClass);
    DateClass xField = (DateClass) field.getXField();
    assertEquals(size, (Integer) xField.getSize());
    assertEquals(emptyIsToday, (Integer) xField.getEmptyIsToday());
    assertEquals(dateFormat, xField.getDateFormat());
  }

}
