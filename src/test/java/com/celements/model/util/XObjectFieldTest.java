package com.celements.model.util;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;

public class XObjectFieldTest extends AbstractComponentTest {

  private DocumentReference classRef;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    classRef = new DocumentReference("wiki", "class", "any");
  }

  @Test
  public void test_constructor_getters_1() throws Exception {
    String name = "name";
    XObjectField<String> field = new XObjectField<>(classRef, name, String.class);
    assertEquals(classRef, field.getClassRef());
    assertEquals(name, field.getName());
    assertEquals(String.class, field.getToken());
  }

  @Test
  public void test_constructor_getters_2() throws Exception {
    String name = "name";
    XObjectField<String> field = new XObjectField<>("wiki", "class", "any", name, String.class);
    assertEquals(classRef, field.getClassRef());
    assertEquals(name, field.getName());
    assertEquals(String.class, field.getToken());
  }

  @Test
  public void test_constructor_getters_2_nullWiki() throws Exception {
    String name = "name";
    XObjectField<String> field = new XObjectField<>("class", "any", name, String.class);
    classRef.setWikiReference(new WikiReference(getContext().getDatabase()));
    assertEquals(classRef, field.getClassRef());
    assertEquals(name, field.getName());
    assertEquals(String.class, field.getToken());
  }

  @Test
  public void test_equals() throws Exception {
    String name = "name";
    XObjectField<String> field = new XObjectField<>(classRef, name, String.class);
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
    String name = "name";
    XObjectField<String> field = new XObjectField<>(classRef, name, String.class);
    assertEquals("class.any.name", field.toString());
  }

}
