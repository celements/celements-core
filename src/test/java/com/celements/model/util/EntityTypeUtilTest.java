package com.celements.model.util;

import static com.celements.model.util.EntityTypeUtil.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.WikiReference;

public class EntityTypeUtilTest {

  @Test
  public void test_rootRefClass() {
    assertSame("WikiReference has to be the root reference", WikiReference.class, getRootClass());
  }

  @Test
  public void test_identifyEntityTypeFromName() {
    assertEquals(EntityType.WIKI, identifyEntityTypeFromName("wiki").get());
    assertEquals(EntityType.SPACE, identifyEntityTypeFromName("wiki:space").get());
    assertEquals(EntityType.DOCUMENT, identifyEntityTypeFromName("wiki:space.doc").get());
    assertEquals(EntityType.DOCUMENT, identifyEntityTypeFromName("space.doc").get());
    assertEquals(EntityType.ATTACHMENT, identifyEntityTypeFromName("wiki:space.doc@att.jpg").get());
    assertEquals(EntityType.ATTACHMENT, identifyEntityTypeFromName("space.doc@att.jpg").get());
    assertFalse("expecting failure because of relative ref", identifyEntityTypeFromName(
        "doc@att").isPresent());
  }

  @Test
  public void test_identifyEntityTypeFromName_specialChars() {
    assertEquals(EntityType.SPACE, identifyEntityTypeFromName("wiki:space_test").get());
    assertEquals(EntityType.SPACE, identifyEntityTypeFromName("wiki:space-test").get());
    assertEquals(EntityType.DOCUMENT, identifyEntityTypeFromName("wiki:space.doc_test").get());
    assertEquals(EntityType.DOCUMENT, identifyEntityTypeFromName("wiki:space.doc-test").get());
    assertEquals(EntityType.DOCUMENT, identifyEntityTypeFromName("space.doc_test").get());
    assertEquals(EntityType.DOCUMENT, identifyEntityTypeFromName("space.doc-test").get());
    assertEquals(EntityType.ATTACHMENT, identifyEntityTypeFromName(
        "wiki:space.doc@att-test.jpg").get());
    assertEquals(EntityType.ATTACHMENT, identifyEntityTypeFromName(
        "wiki:space.doc@att_test.jpg").get());
  }

}
