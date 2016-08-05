package com.celements.model.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

public class ModelUtilsTest extends AbstractComponentTest {

  DocumentReference docRef = new DocumentReference("doc", new SpaceReference("space",
      new WikiReference("wiki")));

  IModelUtils modelUtils;

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    modelUtils = Utils.getComponent(IModelUtils.class);
  }

  @Test
  public void test_cloneReference() {
    DocumentReference ref = docRef;
    EntityReference newRef = modelUtils.cloneRef(ref);
    assertTrue(newRef instanceof DocumentReference);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.getParent().setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_cloneReference_wikiRef() {
    WikiReference ref = docRef.getWikiReference();
    WikiReference newRef = modelUtils.cloneRef(ref, WikiReference.class);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_cloneReference_spaceRef() {
    SpaceReference ref = docRef.getLastSpaceReference();
    SpaceReference newRef = modelUtils.cloneRef(ref, SpaceReference.class);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.getParent().setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_cloneReference_docRef() {
    DocumentReference ref = docRef;
    DocumentReference newRef = modelUtils.cloneRef(ref, DocumentReference.class);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.getParent().setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_cloneReference_entityRef() {
    DocumentReference ref = docRef;
    EntityReference newRef = modelUtils.cloneRef(ref, EntityReference.class);
    assertTrue(newRef instanceof DocumentReference);
    assertNotSame(ref, newRef);
    assertEquals(ref, newRef);
    ref.getParent().setName("asdf");
    assertFalse(ref.equals(newRef));
  }

  @Test
  public void test_adjustRef_higherLevel_1() {
    EntityReference toRef = new WikiReference("oWiki");
    DocumentReference ret = modelUtils.adjustRef(docRef, DocumentReference.class, toRef);
    assertEquals(toRef, ret.getWikiReference());
    assertEquals(docRef.getName(), ret.getName());
    assertEquals(docRef.getLastSpaceReference().getName(), ret.getLastSpaceReference().getName());
    assertFalse(docRef.getWikiReference().equals(ret.getWikiReference()));
  }

  @Test
  public void test_adjustRef_higherLevel_2() {
    EntityReference toRef = new SpaceReference("oSpace", new WikiReference("oWiki"));
    DocumentReference ret = modelUtils.adjustRef(docRef, DocumentReference.class, toRef);
    assertEquals(toRef, ret.getLastSpaceReference());
    assertEquals(docRef.getName(), ret.getName());
    assertFalse(docRef.getLastSpaceReference().equals(ret.getLastSpaceReference()));
    assertFalse(docRef.getWikiReference().equals(ret.getWikiReference()));
  }

  @Test
  public void test_adjustRef_sameLevel() {
    EntityReference toRef = new DocumentReference("oWiki", "oSpace", "oDoc");
    DocumentReference ret = modelUtils.adjustRef(docRef, DocumentReference.class, toRef);
    assertEquals("expecting toRef if same level entity", toRef, ret);
    assertNotSame(toRef, ret);
    assertFalse(docRef.getName().equals(ret.getName()));
    assertFalse(docRef.getLastSpaceReference().equals(ret.getLastSpaceReference()));
    assertFalse(docRef.getWikiReference().equals(ret.getWikiReference()));
  }

  @Test
  public void test_adjustRef_lowerLevel() {
    EntityReference toRef = new AttachmentReference("att", new DocumentReference("oWiki", "oSpace",
        "oDoc"));
    DocumentReference ret = modelUtils.adjustRef(docRef, DocumentReference.class, toRef);
    assertEquals("expecting docRef if lower level entity", docRef, ret);
    assertNotSame(docRef, ret);
  }

}
