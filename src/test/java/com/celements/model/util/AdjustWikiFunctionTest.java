package com.celements.model.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

public class AdjustWikiFunctionTest {

  @Test
  public void test_apply_noChange() {
    WikiReference wikiRef = new WikiReference("db");
    AdjustWikiFunction<SpaceReference> func = new AdjustWikiFunction<>(SpaceReference.class,
        wikiRef);
    SpaceReference spaceRef = new SpaceReference("space", wikiRef);
    SpaceReference ret = func.apply(spaceRef);
    assertNotSame(spaceRef, ret);
    assertEquals(spaceRef, ret);
  }

  @Test
  public void test_apply_spaceRef() {
    WikiReference wikiRef = new WikiReference("newdb");
    AdjustWikiFunction<SpaceReference> func = new AdjustWikiFunction<>(SpaceReference.class,
        wikiRef);
    SpaceReference spaceRef = new SpaceReference("space", new WikiReference("db"));
    SpaceReference ret = func.apply(spaceRef);
    assertNotSame(spaceRef, ret);
    assertEquals(spaceRef.getName(), ret.getName());
    assertNotSame(wikiRef, ret.getParent());
    assertEquals(wikiRef, ret.getParent());
  }

  @Test
  public void test_apply_docRef() {
    WikiReference wikiRef = new WikiReference("newdb");
    AdjustWikiFunction<DocumentReference> func = new AdjustWikiFunction<>(DocumentReference.class,
        wikiRef);
    DocumentReference docRef = new DocumentReference("db", "space", "doc");
    DocumentReference ret = func.apply(docRef);
    assertEquals(docRef.getName(), ret.getName());
    assertEquals(docRef.getParent().getName(), ret.getParent().getName());
    assertEquals(wikiRef, ret.getWikiReference());
  }

}
