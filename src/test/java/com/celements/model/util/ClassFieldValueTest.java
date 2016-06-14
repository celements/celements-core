package com.celements.model.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.fields.StringField;

public class ClassFieldValueTest extends AbstractComponentTest {

  private StringField field;
  private String val;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    field = new StringField(new DocumentReference("wiki", "class", "any"), "name");
    val = "val";
  }

  @Test
  public void test_constr_null_field() throws Exception {
    try {
      new ClassFieldValue<String>(null, val);
      fail("expecting NullPointerException");
    } catch (NullPointerException npe) {
      // expected
    }
  }

  @Test
  public void test_constr_null_val() throws Exception {
    new ClassFieldValue<>(field, null);
    // null value allowed, not expecting NPE
  }

  @Test
  public void test_getters() throws Exception {
    ClassFieldValue<String> fieldValue = new ClassFieldValue<>(field, val);
    assertEquals(field, fieldValue.getField());
    assertEquals(val, fieldValue.getValue());
  }

  @Test
  public void test_equals() throws Exception {
    ClassFieldValue<String> fieldValue = new ClassFieldValue<>(field, val);
    assertTrue(fieldValue.equals(new ClassFieldValue<String>(field, val)));
    assertFalse(fieldValue.equals(new ClassFieldValue<String>(field, "otherVal")));
    assertFalse(fieldValue.equals(new ClassFieldValue<String>(new StringField(field.getClassRef(),
        "name2"), val)));
    assertFalse(fieldValue.equals(field));
    assertFalse(fieldValue.equals(null));
  }

  @Test
  public void test_hashCode() throws Exception {
    ClassFieldValue<String> fieldValue = new ClassFieldValue<>(field, val);
    assertTrue(fieldValue.hashCode() == new ClassFieldValue<String>(field, val).hashCode());
    assertFalse(fieldValue.hashCode() == new ClassFieldValue<String>(field, "otherVal").hashCode());
    assertFalse(fieldValue.hashCode() == new ClassFieldValue<String>(new StringField(
        field.getClassRef(), "name2"), val).hashCode());
    assertFalse(fieldValue.hashCode() == field.hashCode());
  }

  @Test
  public void test_toString() throws Exception {
    ClassFieldValue<String> fieldValue = new ClassFieldValue<String>(field, val);
    assertEquals("class.any.name: val", fieldValue.toString());
  }
}
