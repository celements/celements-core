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
    String name = "name";
    String val = "val";
    XObjectFieldValue<String> field = new XObjectFieldValue<>(new XObjectField<>(classRef, name,
        String.class), val);
    assertTrue(field.equals(new XObjectFieldValue<String>(new XObjectField<String>(classRef, name,
        String.class), val)));
    assertFalse(field.equals(new XObjectFieldValue<String>(new XObjectField<String>(classRef, name,
        String.class), "otherVal")));
    assertFalse(field.equals(new XObjectFieldValue<String>(new XObjectField<String>(classRef,
        "name2", String.class), "otherVal")));
    assertFalse(field.equals(new XObjectField<>(classRef, name, String.class)));
    assertFalse(field.equals(null));
  }

  @Test
  public void test_hashCode() throws Exception {
    String name = "name";
    String val = "val";
    XObjectFieldValue<String> field = new XObjectFieldValue<>(new XObjectField<>(classRef, name,
        String.class), val);
    assertTrue(field.hashCode() == new XObjectFieldValue<String>(new XObjectField<String>(classRef,
        name, String.class), val).hashCode());
    assertFalse(field.hashCode() == new XObjectFieldValue<String>(new XObjectField<String>(classRef,
        name, String.class), "otherVal").hashCode());
    assertFalse(field.hashCode() == new XObjectFieldValue<String>(new XObjectField<String>(classRef,
        "name2", String.class), val).hashCode());
    assertFalse(field.hashCode() == new XObjectField<>(classRef, name, String.class).hashCode());
  }

  @Test
  public void test_serializeToXObjectValue_date() {
    String name = "name";
    Date val = new Date();
    XObjectFieldValue<Date> field = new XObjectFieldValue<>(new XObjectField<>(classRef, name,
        Date.class), val);
    assertEquals(val, field.serializeToXObjectValue());
  }

  @Test
  public void test_serializeToXObjectValue_ref() {
    String name = "name";
    DocumentReference val = classRef;
    XObjectFieldValue<DocumentReference> field = new XObjectFieldValue<>(new XObjectField<>(
        classRef, name, DocumentReference.class), val);
    assertEquals(XObjectField.getWebUtils().serializeRef(val), field.serializeToXObjectValue());
  }

  @Test
  public void test_toString() throws Exception {
    XObjectField<String> field = new XObjectFieldValue<String>(new XObjectField<String>(classRef,
        "name", String.class), "val");
    assertEquals("class.any.name: val", field.toString());
  }

}
