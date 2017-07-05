package org.xwiki.model.reference;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.EntityType;

import com.celements.common.test.AbstractComponentTest;
import com.celements.common.test.ExceptionAsserter;

public class ClassReferenceTest extends AbstractComponentTest {

  private ClassReference classRef;

  @Before
  public void prepareTest() {
    classRef = new ClassReference("space", "class");
  }

  @Test
  public void test_equals() {
    assertEquals(classRef, new ClassReference(classRef));
    assertEquals(classRef, new ClassReference(classRef.getParent().getName(), classRef.getName()));
  }

  @Test
  public void test_clone() {
    assertNotSame(classRef, classRef.clone());
    assertEquals(classRef, classRef.clone());
    assertTrue(classRef.clone() instanceof ClassReference);
  }

  @Test
  public void test_getName() {
    assertEquals("class", classRef.getName());
  }

  @Test
  public void test_setName() {
    new ExceptionAsserter<IllegalStateException>(IllegalStateException.class) {

      @Override
      protected void execute() throws IllegalStateException {
        classRef.setName("x");
      }

    }.evaluate();
  }

  @Test
  public void test_getParent() {
    EntityReference parent = classRef.getParent();
    assertNotNull(parent);
    assertEquals("space", parent.getName());
    assertEquals(EntityType.SPACE, parent.getType());
    assertSame(classRef, parent.getChild());
    assertNull(parent.getParent());
    parent.setName("x");
  }

  @Test
  public void test_getParent_modify() {
    EntityReference clone = classRef.clone();
    clone.getParent().setName("x");
    clone.getParent().setParent(null);
    clone.getParent().setChild(null);
    clone.getParent().setType(EntityType.WIKI);
    assertEquals(classRef, clone);
  }

  @Test
  public void test_setParent() {
    new ExceptionAsserter<IllegalStateException>(IllegalStateException.class) {

      @Override
      protected void execute() throws IllegalStateException {
        classRef.setParent(classRef);
      }

    }.evaluate();
  }

  @Test
  public void test_getChild() {
    assertNull(classRef.getChild());
  }

  @Test
  public void test_setChild() {
    new ExceptionAsserter<IllegalStateException>(IllegalStateException.class) {

      @Override
      protected void execute() throws IllegalStateException {
        classRef.setChild(classRef);
      }

    }.evaluate();
  }

  @Test
  public void test_getType() {
    assertEquals(EntityType.DOCUMENT, classRef.getType());
  }

  @Test
  public void test_setType() {
    new ExceptionAsserter<IllegalStateException>(IllegalStateException.class) {

      @Override
      protected void execute() throws IllegalStateException {
        classRef.setType(EntityType.DOCUMENT);
      }

    }.evaluate();
  }

  @Test
  public void test_extractReference() {
    assertSame(classRef, classRef.extractReference(EntityType.DOCUMENT));
    assertEquals(classRef.getParent(), classRef.extractReference(EntityType.SPACE));
    assertNotSame(classRef.getParent(), classRef.extractReference(EntityType.SPACE));
    assertNull(classRef.extractReference(EntityType.WIKI));
  }

  @Test
  public void test_getDocumentReference() {
    assertEquals(new DocumentReference(getContext().getDatabase(), classRef.getParent().getName(),
        classRef.getName()), classRef.getDocumentReference());
    WikiReference wikiRef = new WikiReference("wiki");
    assertEquals(new DocumentReference(wikiRef.getName(), classRef.getParent().getName(),
        classRef.getName()), classRef.getDocumentReference(wikiRef));
  }

}
