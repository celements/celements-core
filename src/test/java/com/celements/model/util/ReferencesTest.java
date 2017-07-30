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
import com.google.common.base.Optional;
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
  public void test_cloneRef_noChild() {
    SpaceReference ref = docRef.getLastSpaceReference();
    assertNotNull(ref.getChild());
    assertNull("child not lost in clone", cloneRef(ref).getChild());
    assertNull("child not lost in clone", cloneRef(ref, SpaceReference.class).getChild());
    assertNull("child not lost in clone", cloneRef(ref, EntityReference.class).getChild());
  }

  @Test
  public void test_cloneRef_wrongAbsoluteType() {
    try {
      cloneRef(spaceRef, DocumentReference.class);
      fail("expecting failure, cannot clone space reference as document reference");
    } catch (IllegalArgumentException iae) {
    }
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

  @SuppressWarnings("unchecked")
  private EntityReferenceResolver<String> getRelativeRefResolver() {
    return Utils.getComponent(EntityReferenceResolver.class, "relative");
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
  public void test_adjustRef_EntityReference_childUnchanged() {
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

  @Test
  public void test_completeRef_identity() {
    Optional<DocumentReference> ret = completeRef(DocumentReference.class, docRef);
    assertTrue(ret.isPresent());
    assertEquals(docRef, ret.get());
    assertNotSame(docRef, ret.get());
    assertEquals(DocumentReference.class, ret.get().getClass());
  }

  @Test
  public void test_completeRef_incomplete() {
    assertFalse(completeRef(WikiReference.class, create(EntityType.SPACE, "space")).isPresent());
    assertFalse(completeRef(DocumentReference.class, wikiRef, spaceRef).isPresent());
    // handle varargs pitfalls
    assertFalse(completeRef(WikiReference.class).isPresent());
    assertFalse(completeRef(WikiReference.class, (EntityReference[]) null).isPresent());
    assertFalse(completeRef(WikiReference.class, null, null).isPresent());
  }

  @Test
  public void test_completeRef_fromRelative() {
    Optional<DocumentReference> ret = completeRef(DocumentReference.class, create(
        EntityType.DOCUMENT, "doc"), create(EntityType.WIKI, "wiki"), spaceRef);
    assertTrue(ret.isPresent());
    assertEquals(docRef, ret.get());
    assertNotSame(docRef, ret.get());
    assertEquals(DocumentReference.class, ret.get().getClass());
  }

  @Test
  public void test_completeRef_EntityReference() {
    try {
      completeRef(EntityReference.class, docRef);
      fail("expecting IAE because EntityReference cannot be absolute");
    } catch (IllegalArgumentException exc) {
      // expected
    }
  }

  @Test
  public void test_completeRef_higher_1() {
    EntityReference toRef = new WikiReference("oWiki");
    assertEquals(docRef, completeRef(DocumentReference.class, docRef, toRef).get());
    DocumentReference ret = completeRef(DocumentReference.class, toRef, docRef).get();
    assertEquals(toRef, ret.getWikiReference());
    assertEquals(docRef.getName(), ret.getName());
    assertEquals(spaceRef.getName(), ret.getLastSpaceReference().getName());
    assertEquals("wiki should have changed", toRef, ret.getWikiReference());
  }

  @Test
  public void test_completeRef_higher_2() {
    EntityReference toRef = new SpaceReference("oSpace", new WikiReference("oWiki"));
    assertEquals(docRef, completeRef(DocumentReference.class, docRef, toRef).get());
    DocumentReference ret = completeRef(DocumentReference.class, toRef, docRef).get();
    assertEquals(toRef, ret.getLastSpaceReference());
    assertEquals(docRef.getName(), ret.getName());
    assertEquals("space should have changed", toRef, ret.getLastSpaceReference());
  }

  @Test
  public void test_completeRef_same() {
    EntityReference toRef = new DocumentReference("oWiki", "oSpace", "oDoc");
    assertEquals(docRef, completeRef(DocumentReference.class, docRef, toRef).get());
    DocumentReference ret = completeRef(DocumentReference.class, toRef, docRef).get();
    assertEquals("expecting toRef if same level entity", toRef, ret);
    assertNotSame(toRef, ret);
    assertEquals("doc should have changed", toRef, ret);
  }

  @Test
  public void test_completeRef_lower() {
    EntityReference toRef = new AttachmentReference("oAtt", new DocumentReference("oWiki", "oSpace",
        "oDoc"));
    assertEquals(docRef, completeRef(DocumentReference.class, docRef, toRef).get());
    DocumentReference ret = completeRef(DocumentReference.class, toRef, docRef).get();
    assertEquals("expecting docRef of lower level entity", toRef.getParent(), ret);
    assertNotSame(docRef, ret);
  }

  @Test
  public void test_combineRef() {
    assertEquals(EntityReference.class, combineRef(docRef).get().getClass());
    SpaceReference spaceRef2 = new SpaceReference("space2", new WikiReference("wiki2"));
    WikiReference wikiRef2 = new WikiReference("wiki3");
    assertEquals(docRef, combineRef(docRef, spaceRef2, wikiRef2).get());
    assertEquals(docRef, combineRef(docRef, wikiRef2, spaceRef2).get());
    assertEquals(new DocumentReference("wiki2", "space2", "doc"), combineRef(spaceRef2, docRef,
        wikiRef).get());
    assertEquals(new DocumentReference("wiki2", "space2", "doc"), combineRef(spaceRef2, wikiRef2,
        docRef).get());
    assertEquals(new DocumentReference("wiki3", "space", "doc"), combineRef(wikiRef2, docRef,
        spaceRef2).get());
    assertEquals(new DocumentReference("wiki3", "space2", "doc"), combineRef(wikiRef2, spaceRef2,
        docRef).get());
  }

  @Test
  public void test_combineRef_type() {
    WikiReference wikiRef2 = new WikiReference("wiki2");
    assertEquals(wikiRef2, combineRef(EntityType.WIKI, wikiRef2, docRef).get());
    assertEquals(new SpaceReference("space", wikiRef2), combineRef(EntityType.SPACE, wikiRef2,
        docRef).get());
    assertEquals(new DocumentReference("wiki2", "space", "doc"), combineRef(EntityType.DOCUMENT,
        wikiRef2, docRef).get());
    assertEquals(new DocumentReference("wiki2", "space", "doc"), combineRef(EntityType.ATTACHMENT,
        wikiRef2, docRef).get());
  }

  @Test
  public void test_combineRef_absent() {
    assertFalse(combineRef(EntityType.SPACE, create(EntityType.DOCUMENT, "space")).isPresent());
    // handle varargs pitfalls
    assertFalse(combineRef().isPresent());
    assertFalse(combineRef((EntityReference[]) null).isPresent());
    assertFalse(combineRef((EntityReference) null, null).isPresent());
  }

}
