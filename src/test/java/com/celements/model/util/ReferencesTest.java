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
import com.xpn.xwiki.web.Utils;

public class ReferencesTest extends AbstractComponentTest {

  ModelUtils modelUtils;
  WikiReference wikiRef;
  SpaceReference spaceRef;
  DocumentReference docRef;
  AttachmentReference attRef;

  @Before
  public void prepareTest() throws Exception {
    modelUtils = Utils.getComponent(ModelUtils.class);
    wikiRef = new WikiReference("wiki");
    spaceRef = new SpaceReference("space", wikiRef);
    docRef = new DocumentReference("doc", spaceRef);
    attRef = new AttachmentReference("att.jpg", docRef);
  }

  @Test
  public void test_rootRefClass() {
    assertSame("WikiReference has to be the root reference", WikiReference.class,
        References.getRootClass());
  }

  @Test
  public void test_isAbsoluteRef() {
    assertTrue(References.isAbsoluteRef(docRef));
    assertTrue(References.isAbsoluteRef(spaceRef));
    assertTrue(References.isAbsoluteRef(wikiRef));
    assertFalse(References.isAbsoluteRef(getRelativeRefResolver().resolve(
        modelUtils.serializeRefLocal(docRef), EntityType.DOCUMENT)));
    assertTrue(References.isAbsoluteRef(getRelativeRefResolver().resolve(modelUtils.serializeRef(
        docRef), EntityType.DOCUMENT)));
  }

  @Test
  public void test_cloneRef() {
    DocumentReference ref = docRef;
    EntityReference newRef = References.cloneRef(ref);
    assertTrue(newRef instanceof DocumentReference);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.getParent().setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_cloneRef_wikiRef() {
    WikiReference ref = wikiRef;
    WikiReference newRef = References.cloneRef(ref, WikiReference.class);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_cloneRef_spaceRef() {
    SpaceReference ref = spaceRef;
    SpaceReference newRef = References.cloneRef(ref, SpaceReference.class);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.getParent().setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_cloneRef_docRef() {
    DocumentReference ref = docRef;
    DocumentReference newRef = References.cloneRef(ref, DocumentReference.class);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.getParent().setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_cloneRef_entityRef() {
    DocumentReference ref = docRef;
    EntityReference newRef = References.cloneRef(ref, EntityReference.class);
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
      References.cloneRef(ref, DocumentReference.class);
      fail("expecting failure, cannot clone relative reference to specific implementation");
    } catch (IllegalArgumentException iae) {
    }
  }

  @Test
  public void test_cloneRef_relative_asEntityRef() {
    EntityReference ref = getRelativeRefResolver().resolve(modelUtils.serializeRefLocal(docRef),
        EntityType.DOCUMENT);
    EntityReference newRef = References.cloneRef(ref);
    assertFalse(newRef instanceof DocumentReference);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.getParent().setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_identifyClassFromName() {
    assertEquals(WikiReference.class, References.identifyClassFromName("wiki"));
    assertEquals(SpaceReference.class, References.identifyClassFromName("wiki:space"));
    assertEquals(DocumentReference.class, References.identifyClassFromName("wiki:space.doc"));
    assertEquals(DocumentReference.class, References.identifyClassFromName("space.doc"));
    assertEquals(AttachmentReference.class, References.identifyClassFromName(
        "wiki:space.doc@att.jpg"));
    assertEquals(AttachmentReference.class, References.identifyClassFromName("space.doc@att.jpg"));
    try {
      References.identifyClassFromName("doc@att");
      fail("expecting failure because of relative ref");
    } catch (IllegalArgumentException iae) {
    }
  }

  @Test
  public void test_identifyClassFromName_specialChars() {
    assertEquals(SpaceReference.class, References.identifyClassFromName("wiki:space_test"));
    assertEquals(SpaceReference.class, References.identifyClassFromName("wiki:space-test"));
    assertEquals(DocumentReference.class, References.identifyClassFromName("wiki:space.doc_test"));
    assertEquals(DocumentReference.class, References.identifyClassFromName("wiki:space.doc-test"));
    assertEquals(DocumentReference.class, References.identifyClassFromName("space.doc_test"));
    assertEquals(DocumentReference.class, References.identifyClassFromName("space.doc-test"));
    assertEquals(AttachmentReference.class, References.identifyClassFromName(
        "wiki:space.doc@att-test.jpg"));
    assertEquals(AttachmentReference.class, References.identifyClassFromName(
        "wiki:space.doc@att_test.jpg"));
  }

  @Test
  public void test_extractRef() {
    assertEquals(wikiRef, References.extractRef(docRef, WikiReference.class).get());
    assertEquals(spaceRef, References.extractRef(docRef, SpaceReference.class).get());
    assertEquals(docRef, References.extractRef(docRef, DocumentReference.class).get());
    assertFalse(References.extractRef(docRef, AttachmentReference.class).isPresent());
    assertEquals(attRef, References.extractRef(attRef, AttachmentReference.class).get());
    assertNotSame(docRef, References.extractRef(docRef, DocumentReference.class).get());
  }

  @Test
  public void test_adjustRef_higherLevel_1() {
    EntityReference toRef = new WikiReference("oWiki");
    DocumentReference ret = References.adjustRef(docRef, DocumentReference.class, toRef);
    assertEquals(toRef, ret.getWikiReference());
    assertEquals(docRef.getName(), ret.getName());
    assertEquals(spaceRef.getName(), ret.getLastSpaceReference().getName());
    assertEquals("wiki should have changed", toRef, ret.getWikiReference());
  }

  @Test
  public void test_adjustRef_higherLevel_2() {
    EntityReference toRef = new SpaceReference("oSpace", new WikiReference("oWiki"));
    DocumentReference ret = References.adjustRef(docRef, DocumentReference.class, toRef);
    assertEquals(toRef, ret.getLastSpaceReference());
    assertEquals(docRef.getName(), ret.getName());
    assertEquals("space should have changed", toRef, ret.getLastSpaceReference());
  }

  @Test
  public void test_adjustRef_sameLevel() {
    EntityReference toRef = new DocumentReference("oWiki", "oSpace", "oDoc");
    DocumentReference ret = References.adjustRef(docRef, DocumentReference.class, toRef);
    assertEquals("expecting toRef if same level entity", toRef, ret);
    assertNotSame(toRef, ret);
    assertEquals("doc should have changed", toRef, ret);
  }

  @Test
  public void test_adjustRef_lowerLevel() {
    EntityReference toRef = new AttachmentReference("oAtt", new DocumentReference("oWiki", "oSpace",
        "oDoc"));
    DocumentReference ret = References.adjustRef(docRef, DocumentReference.class, toRef);
    assertEquals("expecting docRef if lower level entity", docRef, ret);
    assertNotSame(docRef, ret);
  }

  @Test
  public void test_adjustRef_childUnchanged() {
    WikiReference toRef = new WikiReference("oWiki");
    assertNull(toRef.getChild());
    References.adjustRef(new EntityReference("doc", EntityType.DOCUMENT, new EntityReference("wiki",
        EntityType.WIKI)), EntityReference.class, toRef);
    // EntityReference.setParent overwrites child of param, ensure it is cloned
    assertNull(toRef.getChild());
  }

  @Test
  public void test_create_wiki() {
    String name = "wiki";
    WikiReference wikiRef = References.create(WikiReference.class, name);
    assertNotNull(wikiRef);
    assertEquals(name, wikiRef.getName());
  }

  @Test
  public void test_create_space() {
    String name = "space";
    SpaceReference spaceRef = References.create(SpaceReference.class, name, wikiRef);
    assertNotNull(spaceRef);
    assertEquals(name, spaceRef.getName());
    assertEquals(wikiRef, spaceRef.getParent());
  }

  @Test
  public void test_create_doc() {
    String name = "doc";
    DocumentReference docRef = References.create(DocumentReference.class, name, spaceRef);
    assertNotNull(docRef);
    assertEquals(name, docRef.getName());
    assertEquals(spaceRef, docRef.getParent());
  }

  @Test
  public void test_create_entity() {
    String name = "doc";
    EntityReference docRef = References.create(EntityType.DOCUMENT, name, spaceRef);
    assertNotNull(docRef);
    assertEquals(name, docRef.getName());
    assertEquals(spaceRef, docRef.getParent());
  }

  @Test
  public void test_create_childparent() {
    WikiReference wikiRef = new WikiReference("wiki");
    SpaceReference spaceRef = References.create(SpaceReference.class, "space", wikiRef);
    assertNotSame(wikiRef, spaceRef.getParent());
    assertEquals(wikiRef, spaceRef.getParent());
    assertSame(spaceRef, spaceRef.getParent().getChild());
    assertNull(wikiRef.getChild());
  }

  @SuppressWarnings("unchecked")
  private EntityReferenceResolver<String> getRelativeRefResolver() {
    return Utils.getComponent(EntityReferenceResolver.class, "relative");
  }

}
