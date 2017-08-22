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
import org.xwiki.model.reference.ImmutableDocumentReference;
import org.xwiki.model.reference.ObjectReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.common.test.ExceptionAsserter;
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
    SpaceReference ref = docRef.getLastSpaceReference();
    EntityReference clone = cloneRef(ref);
    assertTrue(clone instanceof SpaceReference);
    assertClone(ref, clone);
    assertClone(ref.getParent(), clone.getParent());
    assertClone(ref.getChild(), clone.getChild());
  }

  @Test
  public void test_cloneRef_wikiRef() {
    WikiReference ref = docRef.getWikiReference();
    WikiReference clone = cloneRef(ref, WikiReference.class);
    assertClone(ref, clone);
    assertClone(ref.getParent(), clone.getParent());
    assertClone(ref.getChild(), clone.getChild());
    assertClone(ref.getChild().getChild(), clone.getChild().getChild());
  }

  @Test
  public void test_cloneRef_spaceRef() {
    SpaceReference ref = docRef.getLastSpaceReference();
    SpaceReference clone = cloneRef(ref, SpaceReference.class);
    assertClone(ref, clone);
    assertClone(ref.getParent(), clone.getParent());
    assertClone(ref.getChild(), clone.getChild());
  }

  @Test
  public void test_cloneRef_docRef() {
    DocumentReference ref = attRef.getDocumentReference();
    EntityReference clone = cloneRef(ref, DocumentReference.class);
    assertClone(ref, clone);
    assertClone(ref.getParent(), clone.getParent());
    assertClone(ref.getParent().getParent(), clone.getParent().getParent());
    assertClone(ref.getChild(), clone.getChild());
    assertTrue(clone instanceof ImmutableDocumentReference);
  }

  @Test
  public void test_cloneRef_entityRef() {
    DocumentReference ref = attRef.getDocumentReference();
    EntityReference clone = cloneRef(ref, EntityReference.class);
    assertTrue(clone instanceof DocumentReference);
    assertClone(ref, clone);
    assertClone(ref.getParent(), clone.getParent());
    assertClone(ref.getParent().getParent(), clone.getParent().getParent());
    assertClone(ref.getChild(), clone.getChild());
    // TODO clone should be immutable
    // assertTrue(clone instanceof ImmutableDocumentReference);
  }

  @Test
  public void test_cloneRef_immutable() {
    DocumentReference ref = new ImmutableDocumentReference(attRef.getDocumentReference());
    assertSame(ref, cloneRef(ref));
    assertSame(ref, cloneRef(ref, EntityReference.class));
    assertSame(ref, cloneRef(ref, DocumentReference.class));
    assertSame(ref, cloneRef(ref, ImmutableDocumentReference.class));
  }

  @Test
  public void test_cloneRef_relative() {
    EntityReference ref = new EntityReference("doc", EntityType.DOCUMENT, new EntityReference(
        "space", EntityType.SPACE));
    assertClone(ref, cloneRef(ref));
    assertClone(ref, cloneRef(ref, EntityReference.class));
    assertClone(ref.getParent(), cloneRef(ref).getParent());
    assertClone(ref, cloneRef(ref.getParent()).getChild());
  }

  @Test
  public void test_cloneRef_child_spaceRef() {
    SpaceReference ref = docRef.getLastSpaceReference();
    assertNotNull(ref.getChild());
    assertClone(ref.getChild(), cloneRef(ref).getChild());
    assertClone(ref.getChild(), cloneRef(ref, EntityReference.class).getChild());
    assertClone(ref.getChild(), cloneRef(ref, SpaceReference.class).getChild());
    assertClone(ref.getChild(), cloneRef(ref).getParent().getChild().getChild());
  }

  @Test
  public void test_cloneRef_child_docRef() {
    DocumentReference ref = attRef.getDocumentReference();
    assertNotNull(ref.getChild());
    assertClone(ref.getChild(), cloneRef(ref).getChild());
    assertClone(ref.getChild(), cloneRef(ref, EntityReference.class).getChild());
    assertClone(ref.getChild(), cloneRef(ref, DocumentReference.class).getChild());
    assertClone(ref.getChild(), cloneRef(ref, ImmutableDocumentReference.class).getChild());
    assertClone(ref.getChild(), cloneRef(ref).getParent().getChild().getChild());
  }

  @Test
  public void test_cloneRef_wrongAbsoluteType() {
    IllegalArgumentException iae = new ExceptionAsserter<IllegalArgumentException>(
        IllegalArgumentException.class, "cannot clone space reference as document reference") {

      @Override
      protected void execute() throws Exception {
        cloneRef(spaceRef, DocumentReference.class);
      }
    }.evaluate();
    assertTrue(iae.getMessage(), iae.getMessage().contains("absolute"));
  }

  @Test
  public void test_cloneRef_relativeAsAbsolute() {
    final EntityReference ref = getRelativeRefResolver().resolve(modelUtils.serializeRefLocal(
        docRef), EntityType.DOCUMENT);
    IllegalArgumentException iae = new ExceptionAsserter<IllegalArgumentException>(
        IllegalArgumentException.class, " cannot clone relative reference as absolute") {

      @Override
      protected void execute() throws Exception {
        cloneRef(ref, DocumentReference.class);
      }
    }.evaluate();
    assertTrue(iae.getMessage(), iae.getMessage().contains("relative"));
  }

  @Test
  public void test_cloneRef_relative_asEntityRef() {
    EntityReference ref = getRelativeRefResolver().resolve(modelUtils.serializeRefLocal(docRef),
        EntityType.DOCUMENT);
    EntityReference clone = cloneRef(ref);
    assertFalse(clone instanceof DocumentReference);
    assertNotSame(ref, clone);
    assertEquals(ref, clone);
    ref.getParent().setName("asdf");
    assertFalse(ref.equals(clone));
  }

  @SuppressWarnings("unchecked")
  private EntityReferenceResolver<String> getRelativeRefResolver() {
    return Utils.getComponent(EntityReferenceResolver.class, "relative");
  }

  @Test
  public void test_extractRef() {
    assertEquals(wikiRef, extractRef(docRef, WikiReference.class).get());
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
    assertSame(spaceRef, spaceRef.getParent().getChild());
  }

  @Test
  public void test_create_doc() {
    String name = "doc";
    DocumentReference docRef = create(DocumentReference.class, name, spaceRef);
    assertNotNull(docRef);
    assertTrue(docRef instanceof ImmutableDocumentReference);
    assertEquals(name, docRef.getName());
    assertEquals(spaceRef, docRef.getParent());
    assertSame(docRef, docRef.getParent().getChild());
  }

  @Test
  public void test_create_parent_immutable() {
    String name = "file";
    AttachmentReference attRef = create(AttachmentReference.class, name,
        new ImmutableDocumentReference(docRef));
    assertNotNull(attRef);
    assertEquals(name, attRef.getName());
    assertEquals(docRef, attRef.getParent());
    assertEquals(docRef, attRef.getDocumentReference());
    assertSame(attRef, attRef.getParent().getChild());
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
    assertTrue(ret.get() instanceof DocumentReference);
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
    assertTrue(ret.get() instanceof DocumentReference);
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

  private void assertClone(Object expected, Object actual) {
    if (expected == null) {
      assertNull(actual);
    } else {
      assertNotSame(expected, actual);
      assertEquals(expected, actual);
    }
  }

}
