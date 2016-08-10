package com.celements.model.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.context.IModelContext;
import com.xpn.xwiki.web.Utils;

public class ModelUtilsTest extends AbstractComponentTest {

  WikiReference wikiRef = new WikiReference("wiki");
  SpaceReference spaceRef = new SpaceReference("space", wikiRef);
  DocumentReference docRef = new DocumentReference("doc", spaceRef);
  AttachmentReference attRef = new AttachmentReference("att.jpg", docRef);

  DefaultModelUtils modelUtils;

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    modelUtils = (DefaultModelUtils) Utils.getComponent(IModelUtils.class);
  }

  @Test
  public void test_rootRefClass() {
    assertSame("WikiReference has to be the root reference", WikiReference.class,
        modelUtils.getRootRefClass());
  }

  @Test
  public void test_isAbsoluteRef() {
    assertTrue(modelUtils.isAbsoluteRef(docRef));
    assertTrue(modelUtils.isAbsoluteRef(spaceRef));
    assertTrue(modelUtils.isAbsoluteRef(wikiRef));
    assertFalse(modelUtils.isAbsoluteRef(getRelativeRefResolver().resolve(
        modelUtils.serializeRefLocal(docRef), EntityType.DOCUMENT)));
    assertTrue(modelUtils.isAbsoluteRef(getRelativeRefResolver().resolve(modelUtils.serializeRef(
        docRef), EntityType.DOCUMENT)));
  }

  @Test
  public void test_cloneRef() {
    DocumentReference ref = docRef;
    EntityReference newRef = modelUtils.cloneRef(ref);
    assertTrue(newRef instanceof DocumentReference);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.getParent().setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_cloneRef_wikiRef() {
    WikiReference ref = wikiRef;
    WikiReference newRef = modelUtils.cloneRef(ref, WikiReference.class);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_cloneRef_spaceRef() {
    SpaceReference ref = spaceRef;
    SpaceReference newRef = modelUtils.cloneRef(ref, SpaceReference.class);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.getParent().setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_cloneRef_docRef() {
    DocumentReference ref = docRef;
    DocumentReference newRef = modelUtils.cloneRef(ref, DocumentReference.class);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.getParent().setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_cloneRef_entityRef() {
    DocumentReference ref = docRef;
    EntityReference newRef = modelUtils.cloneRef(ref, EntityReference.class);
    assertTrue(newRef instanceof DocumentReference);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.getParent().setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_cloneRef_relative() {
    EntityReference ref = getRelativeRefResolver().resolve(modelUtils.serializeRefLocal(docRef),
        EntityType.DOCUMENT);
    try {
      modelUtils.cloneRef(ref, DocumentReference.class);
      fail("expecting failure, cannot clone relative reference to specific implementation");
    } catch (IllegalArgumentException iae) {
    }
  }

  @Test
  public void test_cloneRef_relative_asEntityRef() {
    EntityReference ref = getRelativeRefResolver().resolve(modelUtils.serializeRefLocal(docRef),
        EntityType.DOCUMENT);
    EntityReference newRef = modelUtils.cloneRef(ref);
    assertFalse(newRef instanceof DocumentReference);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.getParent().setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_resolveRefClass() {
    assertEquals(WikiReference.class, modelUtils.resolveRefClass("wiki"));
    assertEquals(SpaceReference.class, modelUtils.resolveRefClass("wiki:space"));
    assertEquals(DocumentReference.class, modelUtils.resolveRefClass("wiki:space.doc"));
    assertEquals(DocumentReference.class, modelUtils.resolveRefClass("space.doc"));
    assertEquals(AttachmentReference.class, modelUtils.resolveRefClass("wiki:space.doc@att.jpg"));
    assertEquals(AttachmentReference.class, modelUtils.resolveRefClass("space.doc@att.jpg"));
    try {
      modelUtils.resolveRefClass("doc@att");
      fail("expecting failure because of relative ref");
    } catch (IllegalArgumentException iae) {
    }
  }

  @Test
  public void test_resolveRef() {
    WikiReference oWikiRef = new WikiReference("otherWiki");
    Utils.getComponent(IModelContext.class).setWiki(wikiRef);
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

  @Test
  public void test_extractRef() {
    assertEquals(wikiRef, modelUtils.extractRef(docRef, WikiReference.class));
    assertEquals(spaceRef, modelUtils.extractRef(docRef, SpaceReference.class));
    assertEquals(docRef, modelUtils.extractRef(docRef, DocumentReference.class));
    assertEquals(null, modelUtils.extractRef(docRef, AttachmentReference.class));
    assertEquals(attRef, modelUtils.extractRef(attRef, AttachmentReference.class));
    assertNotSame(docRef, modelUtils.extractRef(docRef, DocumentReference.class));
  }

  @Test
  public void test_extractRef_default() {
    assertEquals(docRef, modelUtils.extractRef(docRef, new DocumentReference("other", spaceRef),
        DocumentReference.class));
    assertEquals(attRef, modelUtils.extractRef(docRef, attRef, AttachmentReference.class));
  }

  @Test
  public void test_adjustRef_higherLevel_1() {
    EntityReference toRef = new WikiReference("oWiki");
    DocumentReference ret = modelUtils.adjustRef(docRef, DocumentReference.class, toRef);
    assertEquals(toRef, ret.getWikiReference());
    assertEquals(docRef.getName(), ret.getName());
    assertEquals(spaceRef.getName(), ret.getLastSpaceReference().getName());
    assertEquals("wiki should have changed", toRef, ret.getWikiReference());
  }

  @Test
  public void test_adjustRef_higherLevel_2() {
    EntityReference toRef = new SpaceReference("oSpace", new WikiReference("oWiki"));
    DocumentReference ret = modelUtils.adjustRef(docRef, DocumentReference.class, toRef);
    assertEquals(toRef, ret.getLastSpaceReference());
    assertEquals(docRef.getName(), ret.getName());
    assertEquals("space should have changed", toRef, ret.getLastSpaceReference());
  }

  @Test
  public void test_adjustRef_sameLevel() {
    EntityReference toRef = new DocumentReference("oWiki", "oSpace", "oDoc");
    DocumentReference ret = modelUtils.adjustRef(docRef, DocumentReference.class, toRef);
    assertEquals("expecting toRef if same level entity", toRef, ret);
    assertNotSame(toRef, ret);
    assertEquals("doc should have changed", toRef, ret);
  }

  @Test
  public void test_adjustRef_lowerLevel() {
    EntityReference toRef = new AttachmentReference("oAtt", new DocumentReference("oWiki", "oSpace",
        "oDoc"));
    DocumentReference ret = modelUtils.adjustRef(docRef, DocumentReference.class, toRef);
    assertEquals("expecting docRef if lower level entity", docRef, ret);
    assertNotSame(docRef, ret);
  }

  @SuppressWarnings("unchecked")
  private EntityReferenceResolver<String> getRelativeRefResolver() {
    return Utils.getComponent(EntityReferenceResolver.class, "relative");
  }

}
