package com.celements.model.util;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;

public class XObjectFieldValueTest extends AbstractComponentTest {

  private DocumentReference classRef;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    classRef = new DocumentReference("wiki", "class", "any");
  }

  @Test
  public void test_constructor_getters() throws Exception {
    String val = "val";
    XObjectFieldValue<String> field = new XObjectFieldValue<>(new XObjectField<>(classRef, "name",
        String.class), val);
    assertEquals(val, field.getValue());
  }

  @Test
  public void test_equals() throws Exception {
    String val = "val";
    String name = "name";
    XObjectFieldValue<String> field = new XObjectFieldValue<>(new XObjectField<>(classRef, name,
        String.class), val);
    assertTrue(field.equals(new XObjectFieldValue<String>(new XObjectField<String>(classRef, name,
        String.class), "otherVal")));
    assertTrue(field.equals(new XObjectField<>(classRef, name, String.class)));
    assertTrue(field.equals(new XObjectField<Date>(classRef, name, Date.class)));
    assertFalse(field.equals(new XObjectField<>(classRef, "name2", String.class)));
    DocumentReference classRefOther = new DocumentReference(classRef);
    classRefOther.setName("other");
    assertFalse(field.equals(new XObjectField<>(classRefOther, name, String.class)));
    assertFalse(field.equals(null));
  }

  @Test
  public void test_hashCode() throws Exception {
    String name = "name";
    XObjectField<String> field = new XObjectField<>(classRef, name, String.class);
    assertTrue(field.hashCode() == new XObjectFieldValue<String>(new XObjectField<String>(classRef,
        name, String.class), "otherVal").hashCode());
    assertTrue(field.hashCode() == new XObjectField<>(classRef, name, String.class).hashCode());
    assertTrue(field.hashCode() == new XObjectField<Date>(classRef, name, Date.class).hashCode());
    assertFalse(field.hashCode() == new XObjectField<>(classRef, "name2", String.class).hashCode());
    DocumentReference classRefOther = new DocumentReference(classRef);
    classRefOther.setName("other");
    assertFalse(field.hashCode() == new XObjectField<>(classRefOther, name, String.class)
        .hashCode());
  }

  @Test
  public void test_toString() throws Exception {
    XObjectField<String> field = new XObjectFieldValue<String>(new XObjectField<String>(classRef,
        "name", String.class), "val");
    assertEquals("class.any.name", field.toString());
  }

}
