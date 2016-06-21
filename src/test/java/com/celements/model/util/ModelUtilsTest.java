package com.celements.model.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;

public class ModelUtilsTest extends AbstractComponentTest {

  DocumentReference docRef = new DocumentReference("doc", new SpaceReference("space",
      new WikiReference("wiki")));

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @Test
  public void test_cloneReference() {
    DocumentReference ref = docRef;
    EntityReference newRef = ModelUtils.cloneReference(ref);
    assertTrue(newRef instanceof DocumentReference);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.getParent().setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_cloneReference_wikiRef() {
    WikiReference ref = docRef.getWikiReference();
    WikiReference newRef = ModelUtils.cloneReference(ref, WikiReference.class);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_cloneReference_spaceRef() {
    SpaceReference ref = docRef.getLastSpaceReference();
    SpaceReference newRef = ModelUtils.cloneReference(ref, SpaceReference.class);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.getParent().setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_cloneReference_docRef() {
    DocumentReference ref = docRef;
    DocumentReference newRef = ModelUtils.cloneReference(ref, DocumentReference.class);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.getParent().setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_cloneReference_entityRef() {
    DocumentReference ref = docRef;
    EntityReference newRef = ModelUtils.cloneReference(ref, EntityReference.class);
    assertTrue(newRef instanceof DocumentReference);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.getParent().setName("asdf");
    assertFalse(ref.equals(newRef));
  }
}
