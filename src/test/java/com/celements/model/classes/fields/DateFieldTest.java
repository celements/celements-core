package com.celements.model.classes.fields;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.objects.classes.DateClass;

public class DateFieldTest extends AbstractComponentTest {

  private DateField field;

  Integer size = 5;
  Integer emptyIsToday = 3;
  String dateFormat = "dateFormat";

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    field = new DateField.Builder(new DocumentReference("wiki", "class", "any"), "name").size(
        size).emptyIsToday(emptyIsToday).dateFormat(dateFormat).build();
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
