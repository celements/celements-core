package com.celements.model.classes.fields;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.classes.fields.AbstractClassField;
import com.celements.model.classes.fields.StringField;
import com.xpn.xwiki.objects.PropertyInterface;

public class ClassFieldTest extends AbstractComponentTest {

  private DocumentReference classRef;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    classRef = new DocumentReference("wiki", "class", "any");
  }

  @Test
  public void test_constr_null_classRef() throws Exception {
    try {
      new TestClassField(null, "name");
      fail("expecting NullPointerException");
    } catch (NullPointerException npe) {
      // expected
    }
  }

  @Test
  public void test_constr_null_name() throws Exception {
    try {
      new TestClassField(classRef, null);
      fail("expecting NullPointerException");
    } catch (NullPointerException npe) {
      // expected
    }
  }

  @Test
  public void test_getters() throws Exception {
    String name = "name";
    TestClassField field = new TestClassField(classRef, name);
    assertEquals(classRef, field.getClassRef());
    assertNotSame(classRef, field.getClassRef());
    assertEquals(name, field.getName());
    assertEquals(TestClassField.class, field.getType());
    assertSame(field.piMock, field.getXField());
  }

  @Test
  public void test_equals() throws Exception {
    String name = "name";
    TestClassField field = new TestClassField(classRef, name);
    assertTrue(field.equals(new TestClassField(classRef, name)));
    assertTrue(field.equals(new StringField(classRef, name)));
    assertFalse(field.equals(new TestClassField(classRef, "name2")));
    DocumentReference classRefOther = new DocumentReference(classRef);
    classRefOther.setName("other");
    assertFalse(field.equals(new TestClassField(classRefOther, name)));
    assertFalse(field.equals(null));
  }

  @Test
  public void test_hashCode() throws Exception {
    String name = "name";
    TestClassField field = new TestClassField(classRef, name);
    assertTrue(field.hashCode() == new TestClassField(classRef, name).hashCode());
    assertTrue(field.hashCode() == new StringField(classRef, name).hashCode());
    assertFalse(field.hashCode() == new TestClassField(classRef, "name2").hashCode());
    DocumentReference classRefOther = new DocumentReference(classRef);
    classRefOther.setName("other");
    assertFalse(field.hashCode() == new TestClassField(classRefOther, name).hashCode());
  }

  @Test
  public void test_toString() throws Exception {
    String name = "name";
    TestClassField field = new TestClassField(classRef, name);
    assertEquals("class.any.name", field.toString());
  }

  private class TestClassField extends AbstractClassField<TestClassField> {

    private PropertyInterface piMock;

    public TestClassField(DocumentReference classRef, String name) {
      super(classRef, name);
      piMock = createMockAndAddToDefault(PropertyInterface.class);
    }

    @Override
    public Class<TestClassField> getType() {
      return TestClassField.class;
    }

    @Override
    public PropertyInterface getXField() {
      return piMock;
    }

  }

}
