package com.celements.model.classes.fields;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mutabilitydetector.unittesting.AllowedReason.*;
import static org.mutabilitydetector.unittesting.MutabilityAssert.*;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.objects.classes.StringClass;

public class ClassFieldTest extends AbstractComponentTest {

  private TestClassField field;

  DocumentReference classRef;
  String name = "name";
  String prettyName = "prettyName";
  String validationRegExp = "validationRegExp";
  String validationMessage = "validationMessage";

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    classRef = new DocumentReference("wiki", "class", "any");
    field = new TestClassField.Builder(classRef, name).size(5).prettyName(
        prettyName).validationRegExp(validationRegExp).validationMessage(validationMessage).build();
  }

  @Test
  public void test_immutability() {
    assertInstancesOf(AbstractClassField.class, areImmutable(), allowingForSubclassing(),
        assumingFields("classRef").areNotModifiedAndDoNotEscape());
    assertImmutable(TestClassField.class);
  }

  @Test
  public void test_immutability_classRef_isNotModifiedAndDoesNotEscape() throws Exception {
    DocumentReference intClassRef = field.getClassRef();
    assertThat(field.getClassRef(), not(sameInstance(intClassRef)));
    assertThat(field.getClassRef(), is(intClassRef));
    intClassRef.setName("asdf");
    assertThat(field.getClassRef(), not(equalTo(intClassRef)));
    classRef.setName("asdf");
    assertThat(field.getClassRef(), not(equalTo(classRef)));
  }

  @Test
  public void test_constr_null_classRef() throws Exception {
    try {
      new TestClassField.Builder(null, field.getName()).build();
      fail("expecting NullPointerException");
    } catch (NullPointerException npe) {
      // expected
    }
  }

  @Test
  public void test_constr_null_name() throws Exception {
    try {
      new TestClassField.Builder(field.getClassRef(), null).build();
      fail("expecting NullPointerException");
    } catch (NullPointerException npe) {
      // expected
    }
  }

  @Test
  public void test_getters() throws Exception {
    assertEquals(classRef, field.getClassRef());
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
    assertTrue(field.equals(new TestClassField.Builder(field.getClassRef(),
        field.getName()).build()));
    assertTrue(field.equals(new StringField.Builder(field.getClassRef(), field.getName()).build()));
    assertTrue(field.equals(new TestClassField.Builder(field.getClassRef(),
        field.getName()).prettyName("asdf").validationRegExp("asdf").validationMessage(
            "asdf").build()));
    assertFalse(field.equals(new TestClassField.Builder(field.getClassRef(), "name2").build()));
    DocumentReference classRefOther = new DocumentReference(field.getClassRef());
    classRefOther.setName("other");
    assertFalse(field.equals(new TestClassField.Builder(classRefOther, field.getName()).build()));
    assertFalse(field.equals(null));
  }

  @Test
  public void test_hashCode() throws Exception {
    assertTrue(field.hashCode() == new TestClassField.Builder(field.getClassRef(),
        field.getName()).build().hashCode());
    assertTrue(field.hashCode() == new StringField.Builder(field.getClassRef(),
        field.getName()).build().hashCode());
    assertTrue(field.hashCode() == new TestClassField.Builder(field.getClassRef(),
        field.getName()).prettyName("asdf").validationRegExp("asdf").validationMessage(
            "asdf").build().hashCode());
    assertFalse(field.hashCode() == new TestClassField.Builder(field.getClassRef(),
        "name2").build().hashCode());
    DocumentReference classRefOther = new DocumentReference(field.getClassRef());
    classRefOther.setName("other");
    assertFalse(field.hashCode() == new TestClassField.Builder(classRefOther,
        field.getName()).build().hashCode());
  }

  @Test
  public void test_toString() throws Exception {
    assertEquals("class.any.name", field.toString());
  }

}
