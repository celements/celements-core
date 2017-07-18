package org.xwiki.model.reference;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.EntityType;

import com.celements.common.test.AbstractComponentTest;
import com.celements.common.test.ExceptionAsserter;

public class ImmutableDocumentReferenceTest extends AbstractComponentTest {

  private DocumentReference docRef;

  @Before
  public void prepareTest() {
    docRef = new ImmutableDocumentReference("wiki", "space", "doc");
  }

  @Test
  public void test_equals() {
    assertEquals(docRef, new DocumentReference(docRef));
    assertEquals(docRef, new ImmutableDocumentReference(docRef));
    assertEquals(docRef, new ImmutableDocumentReference(docRef.getWikiReference().getName(),
        docRef.getLastSpaceReference().getName(), docRef.getName()));
    assertEquals(docRef, new ImmutableDocumentReference(docRef.getName(),
        docRef.getLastSpaceReference()));
  }

  @Test
  public void test_clone() {
    assertNotSame(docRef, docRef.clone());
    assertEquals(docRef, docRef.clone());
    assertTrue(docRef.clone() instanceof ImmutableDocumentReference);
  }

  @Test
  public void test_getName() {
    assertEquals("doc", docRef.getName());
  }

  @Test
  public void test_setName() {
    new ExceptionAsserter<IllegalStateException>(IllegalStateException.class) {

      @Override
      protected void execute() throws IllegalStateException {
        docRef.setName("x");
      }

    }.evaluate();
  }

  @Test
  public void test_getParent_modify() {
    EntityReference clone = docRef.clone();
    clone.getParent().setName("x");
    clone.getParent().setParent(new WikiReference("asdf"));
    clone.getParent().setChild(null);
    assertEquals(docRef, clone);
    assertSame(docRef, docRef.getParent().getChild());
  }

  @Test
  public void test_setParent() {
    new ExceptionAsserter<IllegalStateException>(IllegalStateException.class) {

      @Override
      protected void execute() throws IllegalStateException {
        docRef.setParent(docRef);
      }

    }.evaluate();
  }

  @Test
  public void test_setChild() {
    new ExceptionAsserter<IllegalStateException>(IllegalStateException.class) {

      @Override
      protected void execute() throws IllegalStateException {
        docRef.setChild(docRef);
      }

    }.evaluate();
  }

  @Test
  public void test_setType() {
    new ExceptionAsserter<IllegalStateException>(IllegalStateException.class) {

      @Override
      protected void execute() throws IllegalStateException {
        docRef.setType(EntityType.DOCUMENT);
      }

    }.evaluate();
  }

  @Test
  public void test_setWikiReference() {
    new ExceptionAsserter<IllegalStateException>(IllegalStateException.class) {

      @Override
      protected void execute() throws IllegalStateException {
        docRef.setWikiReference(new WikiReference("asdf"));
      }

    }.evaluate();
  }

  @Test
  public void test_extractReference() {
    ImmutableDocumentReference clone = new ImmutableDocumentReference(docRef);
    assertSame(docRef, docRef.extractReference(EntityType.DOCUMENT));
    clone.extractReference(EntityType.SPACE).setName("asdf");
    clone.extractReference(EntityType.WIKI).setName("asdf");
    assertEquals(docRef, clone);
  }

  @Test
  public void test_getWikiReference() {
    ImmutableDocumentReference clone = new ImmutableDocumentReference(docRef);
    clone.getWikiReference().setName("asdf");
    assertEquals(docRef, clone);
  }

  @Test
  public void test_getLastSpaceReference() {
    ImmutableDocumentReference clone = new ImmutableDocumentReference(docRef);
    clone.getLastSpaceReference().setName("asdf");
    assertEquals(docRef, clone);
  }

  @Test
  public void test_getSpaceReferences() {
    ImmutableDocumentReference clone = new ImmutableDocumentReference(docRef);
    clone.getSpaceReferences().get(0).setName("asdf");
    assertEquals(docRef, clone);
  }

}
