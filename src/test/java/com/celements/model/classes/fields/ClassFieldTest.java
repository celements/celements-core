package com.celements.model.classes.fields;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.objects.classes.StringClass;

public class ClassFieldTest extends AbstractComponentTest {

  private TestClassField field;

  DocumentReference classRef = new DocumentReference("wiki", "class", "any");
  String name = "name";
  String prettyName = "prettyName";
  String validationRegExp = "validationRegExp";
  String validationMessage = "validationMessage";

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    field = new TestClassField(classRef, name);
    field.setPrettyName(prettyName);
    field.setValidationRegExp(validationRegExp);
    field.setValidationMessage(validationMessage);
  }

  @Test
  public void test_constr_null_classRef() throws Exception {
    try {
      new TestClassField(null, field.getName());
      fail("expecting NullPointerException");
    } catch (NullPointerException npe) {
      // expected
    }
  }

  @Test
  public void test_constr_null_name() throws Exception {
    try {
      new TestClassField(field.getClassRef(), null);
      fail("expecting NullPointerException");
    } catch (NullPointerException npe) {
      // expected
    }
  }

  @Test
  public void test_getters() throws Exception {
    assertEquals(classRef, field.getClassRef());
    assertNotSame(classRef, field.getClassRef());
    assertEquals(name, field.getName());
    assertEquals(TestClassField.class, field.getType());
    assertEquals(prettyName, field.getPrettyName());
    assertEquals(validationRegExp, field.getValidationRegExp());
    assertEquals(validationMessage, field.getValidationMessage());
  }

  @Test
  public void test_getXField() throws Exception {
    assertTrue(field.getXField() instanceof StringClass);
    StringClass xField = (StringClass) field.getXField();
    assertEquals(field.getName(), xField.getName());
    assertEquals(prettyName, xField.getPrettyName());
    assertEquals(validationRegExp, xField.getValidationRegExp());
    assertEquals(validationMessage, xField.getValidationMessage());
  }

  @Test
  public void test_equals() throws Exception {
    assertTrue(field.equals(new TestClassField(field.getClassRef(), field.getName())));
    assertTrue(field.equals(new StringField(field.getClassRef(), field.getName())));
    assertTrue(field.equals(new TestClassField(field.getClassRef(), field.getName()).setPrettyName(
        "asdf").setValidationRegExp("asdf").setValidationMessage("asdf")));
    assertFalse(field.equals(new TestClassField(field.getClassRef(), "name2")));
    DocumentReference classRefOther = new DocumentReference(field.getClassRef());
    classRefOther.setName("other");
    assertFalse(field.equals(new TestClassField(classRefOther, field.getName())));
    assertFalse(field.equals(null));
  }

  @Test
  public void test_hashCode() throws Exception {
    assertTrue(field.hashCode() == new TestClassField(field.getClassRef(), field.getName()).hashCode());
    assertTrue(field.hashCode() == new StringField(field.getClassRef(), field.getName()).hashCode());
    assertTrue(field.hashCode() == new TestClassField(field.getClassRef(), field.getName()).setPrettyName(
        "asdf").setValidationRegExp("asdf").setValidationMessage("asdf").hashCode());
    assertFalse(field.hashCode() == new TestClassField(field.getClassRef(), "name2").hashCode());
    DocumentReference classRefOther = new DocumentReference(field.getClassRef());
    classRefOther.setName("other");
    assertFalse(field.hashCode() == new TestClassField(classRefOther, field.getName()).hashCode());
  }

  @Test
  public void test_toString() throws Exception {
    assertEquals("class.any.name", field.toString());
  }

  private class TestClassField extends AbstractClassField<TestClassField> {

    public TestClassField(DocumentReference classRef, String name) {
      super(classRef, name);
    }

    @Override
    public Class<TestClassField> getType() {
      return TestClassField.class;
    }

    @Override
    protected PropertyClass getPropertyClass() {
      return new StringClass();
    }

  }

}
