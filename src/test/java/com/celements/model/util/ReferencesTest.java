package com.celements.model.util;

import static com.celements.model.util.References.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.ObjectReference;
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
  public void test_isAbsoluteRef() {
    assertTrue(isAbsoluteRef(wikiRef));
    assertTrue(isAbsoluteRef(spaceRef));
    assertTrue(isAbsoluteRef(docRef));
    assertTrue(isAbsoluteRef(attRef));
    ObjectReference objRef = new ObjectReference("Class.Obj", docRef);
    assertTrue(isAbsoluteRef(objRef));
    // ObjectPropertyReference is buggy, always contains EntityType.OBJECT, see setType
    // assertTrue(isAbsoluteRef(new ObjectPropertyReference("field", objRef)));
    assertFalse(isAbsoluteRef(getRelativeRefResolver().resolve(modelUtils.serializeRefLocal(docRef),
        EntityType.DOCUMENT)));
    assertTrue(isAbsoluteRef(getRelativeRefResolver().resolve(modelUtils.serializeRef(docRef),
        EntityType.DOCUMENT)));
    assertFalse(isAbsoluteRef(new EntityReference("wiki", EntityType.WIKI, new EntityReference(
        "superwiki", EntityType.WIKI))));
  }

  @Test
  public void test_cloneRef() {
    DocumentReference ref = docRef;
    EntityReference newRef = cloneRef(ref);
    assertTrue(newRef instanceof DocumentReference);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.getParent().setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_cloneRef_wikiRef() {
    WikiReference ref = wikiRef;
    WikiReference newRef = cloneRef(ref, WikiReference.class);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_cloneRef_spaceRef() {
    SpaceReference ref = spaceRef;
    SpaceReference newRef = cloneRef(ref, SpaceReference.class);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.getParent().setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_cloneRef_docRef() {
    DocumentReference ref = docRef;
    DocumentReference newRef = cloneRef(ref, DocumentReference.class);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.getParent().setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_cloneRef_entityRef() {
    DocumentReference ref = docRef;
    EntityReference newRef = cloneRef(ref, EntityReference.class);
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
      cloneRef(ref, DocumentReference.class);
      fail("expecting failure, cannot clone relative reference to specific implementation");
    } catch (IllegalArgumentException iae) {
    }
  }

  @Test
  public void test_cloneRef_relative_asEntityRef() {
    EntityReference ref = getRelativeRefResolver().resolve(modelUtils.serializeRefLocal(docRef),
        EntityType.DOCUMENT);
    EntityReference newRef = cloneRef(ref);
    assertFalse(newRef instanceof DocumentReference);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.getParent().setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_extractRef() {
    assertEquals(wikiRef, extractRef(docRef, WikiReference.class).get());
    assertNull(extractRef(docRef, WikiReference.class).get().getChild());
    assertEquals(spaceRef, extractRef(docRef, SpaceReference.class).get());
    assertEquals(docRef, extractRef(docRef, DocumentReference.class).get());
    assertFalse(extractRef(docRef, AttachmentReference.class).isPresent());
    assertEquals(attRef, extractRef(attRef, AttachmentReference.class).get());
    assertNotSame(docRef, extractRef(docRef, DocumentReference.class).get());
  }

  @Test
  public void test_adjustRef_higherLevel_1() {
    EntityReference toRef = new WikiReference("oWiki");
    DocumentReference ret = adjustRef(docRef, DocumentReference.class, toRef);
    assertEquals(toRef, ret.getWikiReference());
    assertEquals(docRef.getName(), ret.getName());
    assertEquals(spaceRef.getName(), ret.getLastSpaceReference().getName());
    assertEquals("wiki should have changed", toRef, ret.getWikiReference());
  }

  @Test
  public void test_adjustRef_higherLevel_2() {
    EntityReference toRef = new SpaceReference("oSpace", new WikiReference("oWiki"));
    DocumentReference ret = adjustRef(docRef, DocumentReference.class, toRef);
    assertEquals(toRef, ret.getLastSpaceReference());
    assertEquals(docRef.getName(), ret.getName());
    assertEquals("space should have changed", toRef, ret.getLastSpaceReference());
  }

  @Test
  public void test_adjustRef_sameLevel() {
    EntityReference toRef = new DocumentReference("oWiki", "oSpace", "oDoc");
    DocumentReference ret = adjustRef(docRef, DocumentReference.class, toRef);
    assertEquals("expecting toRef if same level entity", toRef, ret);
    assertNotSame(toRef, ret);
    assertEquals("doc should have changed", toRef, ret);
  }

  @Test
  public void test_adjustRef_lowerLevel() {
    EntityReference toRef = new AttachmentReference("oAtt", new DocumentReference("oWiki", "oSpace",
        "oDoc"));
    DocumentReference ret = adjustRef(docRef, DocumentReference.class, toRef);
    assertEquals("expecting docRef of lower level entity", toRef.getParent(), ret);
    assertNotSame(docRef, ret);
  }

  @Test
  public void test_adjustRef_childUnchanged() {
    WikiReference toRef = new WikiReference("oWiki");
    assertNull(toRef.getChild());
    adjustRef(new EntityReference("doc", EntityType.DOCUMENT, new EntityReference("wiki",
        EntityType.WIKI)), EntityReference.class, toRef);
    // EntityReference.setParent overwrites child of param, ensure it is cloned
    assertNull(toRef.getChild());
  }

  @Test
  public void test_create_wiki() {
    String name = "wiki";
    WikiReference wikiRef = create(WikiReference.class, name);
    assertNotNull(wikiRef);
    assertEquals(name, wikiRef.getName());
  }

  @Test
  public void test_create_space() {
    String name = "space";
    SpaceReference spaceRef = create(SpaceReference.class, name, wikiRef);
    assertNotNull(spaceRef);
    assertEquals(name, spaceRef.getName());
    assertEquals(wikiRef, spaceRef.getParent());
  }

  @Test
  public void test_create_doc() {
    String name = "doc";
    DocumentReference docRef = create(DocumentReference.class, name, spaceRef);
    assertNotNull(docRef);
    assertEquals(name, docRef.getName());
    assertEquals(spaceRef, docRef.getParent());
  }

  @Test
  public void test_create_entity() {
    String name = "doc";
    EntityReference docRef = create(EntityType.DOCUMENT, name, spaceRef);
    assertNotNull(docRef);
    assertEquals(name, docRef.getName());
    assertEquals(spaceRef, docRef.getParent());
  }

  @Test
  public void test_create_childparent() {
    WikiReference wikiRef = new WikiReference("wiki");
    SpaceReference spaceRef = create(SpaceReference.class, "space", wikiRef);
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
