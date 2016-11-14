package com.celements.model.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.context.ModelContext;
import com.xpn.xwiki.web.Utils;

public class ModelUtilsTest extends AbstractComponentTest {

  WikiReference wikiRef = new WikiReference("wiki");
  SpaceReference spaceRef = new SpaceReference("space", wikiRef);
  DocumentReference docRef = new DocumentReference("doc", spaceRef);
  AttachmentReference attRef = new AttachmentReference("att.jpg", docRef);

  ModelUtils modelUtils;

  @Before
  public void prepareTest() throws Exception {
    Utils.getComponent(ModelContext.class).setWikiRef(wikiRef);
    modelUtils = Utils.getComponent(ModelUtils.class);
  }

  @Test
  public void test_resolveRef() {
    WikiReference oWikiRef = new WikiReference("otherWiki");
    assertEquals(wikiRef, modelUtils.resolveRef("wiki", WikiReference.class));
    // assertEquals(wikiRef, modelUtils.resolveRef("", WikiReference.class)); TODO
    assertEquals(spaceRef, modelUtils.resolveRef("wiki:space", SpaceReference.class));
    assertEquals(spaceRef, modelUtils.resolveRef("space", SpaceReference.class));
    assertEquals(spaceRef, modelUtils.resolveRef("wiki:space", SpaceReference.class, oWikiRef));
    assertEquals(new SpaceReference(spaceRef.getName(), oWikiRef), modelUtils.resolveRef("space",
        SpaceReference.class, oWikiRef));
    assertEquals(docRef, modelUtils.resolveRef("wiki:space.doc", DocumentReference.class));
    assertEquals(docRef, modelUtils.resolveRef("space.doc", DocumentReference.class));
    assertEquals(docRef, modelUtils.resolveRef("doc", DocumentReference.class, spaceRef));
  }

  @Test
  public void test_resolveRef_noParamChange() {
    assertEquals(spaceRef, modelUtils.resolveRef("wiki:space", SpaceReference.class, wikiRef));
    assertNotSame(wikiRef, spaceRef.getParent());
    assertNull(wikiRef.getChild());
  }

  @Test
  public void test_resolveRef_failure() {
    try {
      modelUtils.resolveRef("doc", DocumentReference.class, wikiRef);
      fail("expecting failure, space reference missing");
    } catch (IllegalArgumentException iae) {
    }
    try {
      modelUtils.resolveRef("doc", DocumentReference.class);
      fail("expecting failure, space reference missing");
    } catch (IllegalArgumentException iae) {
    }
    try {
      modelUtils.resolveRef("", DocumentReference.class);
      fail("expecting failure for empty string");
    } catch (IllegalArgumentException iae) {
    }
  }

  @Test
  public void test_serialzeRef() {
    assertEquals("wiki", modelUtils.serializeRef(wikiRef));
    assertEquals("wiki:space", modelUtils.serializeRef(spaceRef));
    assertEquals("wiki:space.doc", modelUtils.serializeRef(docRef));
    assertEquals("wiki:space.doc@att.jpg", modelUtils.serializeRef(attRef));
    try {
      modelUtils.serializeRef(null);
      fail("expecting failure for null value");
    } catch (NullPointerException npe) {
      // expected
    }
  }

  @Test
  public void test_serialzeRefLocal() {
    assertEquals("", modelUtils.serializeRefLocal(wikiRef));
    assertEquals("space", modelUtils.serializeRefLocal(spaceRef));
    assertEquals("space.doc", modelUtils.serializeRefLocal(docRef));
    assertEquals("space.doc@att.jpg", modelUtils.serializeRefLocal(attRef));
    try {
      modelUtils.serializeRefLocal(null);
      fail("expecting failure for null value");
    } catch (NullPointerException npe) {
    }
  }

}
