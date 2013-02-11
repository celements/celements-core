package com.celements.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FieldNameTest {

  @Test
  public void test() {
    String className = "myClassName";
    String fieldName = "myFieldName";
    String fieldName2 = "myFieldName2";
    FieldName field1 = new FieldName(className, fieldName);
    FieldName field2 = new FieldName(className, fieldName2);
    FieldName field3 = new FieldName(className, fieldName);
    assertEquals(field1, field1);
    assertTrue(field1.hashCode() == field1.hashCode());
    assertEquals(field2, field2);
    assertTrue(field2.hashCode() == field2.hashCode());
    assertEquals(field3, field3);
    assertTrue(field3.hashCode() == field3.hashCode());
    assertEquals(field1, field3);
    assertTrue(field1.hashCode() == field3.hashCode());
    assertFalse(field1.equals(field2));
    assertFalse(field1.hashCode() == field2.hashCode());
    assertFalse(field3.equals(field2));
    assertFalse(field3.hashCode() == field2.hashCode());
  }

}
