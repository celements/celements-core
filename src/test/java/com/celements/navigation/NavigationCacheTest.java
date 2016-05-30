package com.celements.navigation;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.easymock.IExpectationSetters;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.common.cache.CacheLoadingException;
import com.celements.common.cache.IDocumentReferenceCache;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.query.IQueryExecutionServiceRole;
import com.google.common.collect.ImmutableSet;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class NavigationCacheTest extends AbstractBridgedComponentTestCase {

  private NavigationCache cache;
  private QueryManager queryManagerMock;
  private IQueryExecutionServiceRole queryExecServiceMock;

  @Before
  public void setUp_NavigationCacheTest() {
    cache = (NavigationCache) Utils.getComponent(IDocumentReferenceCache.class,
        NavigationCache.NAME);
    queryManagerMock = createMockAndAddToDefault(QueryManager.class);
    cache.injectQueryManager(queryManagerMock);
    queryExecServiceMock = createMockAndAddToDefault(IQueryExecutionServiceRole.class);
    cache.injectQueryExecService(queryExecServiceMock);
  }

  @After
  public void tearDown_NavigationCacheTest() {
    cache.injectQueryManager(Utils.getComponent(QueryManager.class));
    cache.injectQueryExecService(Utils.getComponent(IQueryExecutionServiceRole.class));
  }

  @Test
  public void testGetCachedDocRefs() throws Exception {
    WikiReference wikiRef = new WikiReference("wiki");
    String space1 = "menuSpace1";
    String space2 = "menuSpace2";
    String space3 = "menuSpace3";
    DocumentReference docRef1 = new DocumentReference(wikiRef.getName(), "space", "nav1");
    DocumentReference docRef2 = new DocumentReference(wikiRef.getName(), "space", "nav2");

    expectXWQL(wikiRef, Arrays.asList(docRef1, docRef2));
    expectMenuSpace(docRef1, space1, 0);
    expectMenuSpace(docRef2, space2, 0);

    replayDefault();
    assertEquals(ImmutableSet.of(docRef1), cache.getCachedDocRefs(wikiRef, space1));
    assertEquals(ImmutableSet.of(docRef2), cache.getCachedDocRefs(wikiRef, space2));
    assertEquals(ImmutableSet.of(), cache.getCachedDocRefs(wikiRef, space3));
    assertEquals(ImmutableSet.of(docRef1, docRef2), cache.getCachedDocRefs(wikiRef));
    verifyDefault();
  }

  @Test
  public void testGetCachedDocRefs_multiple() throws Exception {
    WikiReference wikiRef = new WikiReference("wiki");
    String space = "menuSpace";
    DocumentReference docRef1 = new DocumentReference(wikiRef.getName(), "space", "nav1");
    DocumentReference docRef2 = new DocumentReference(wikiRef.getName(), "space", "nav2");

    expectXWQL(wikiRef, Arrays.asList(docRef1, docRef2));
    expectMenuSpace(docRef1, space, 0);
    expectMenuSpace(docRef2, space, 0);

    replayDefault();
    assertEquals(ImmutableSet.of(docRef1, docRef2), cache.getCachedDocRefs(wikiRef, space));
    assertEquals(ImmutableSet.of(docRef1, docRef2), cache.getCachedDocRefs(wikiRef));
    verifyDefault();
  }

  @Test
  public void testGetCachedDocRefs_withNullObj() throws Exception {
    WikiReference wikiRef = new WikiReference("wiki");
    String space = "menuSpace";
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "nav");

    expectXWQL(wikiRef, Arrays.asList(docRef));
    expectMenuSpace(docRef, space, 1);

    replayDefault();
    assertEquals(ImmutableSet.of(docRef), cache.getCachedDocRefs(wikiRef, space));
    verifyDefault();
  }

  @Test
  public void testGetCachedDocRefs_empty() throws Exception {
    WikiReference wikiRef = new WikiReference("wiki");
    String space = "menuSpace";

    expectXWQL(wikiRef, Collections.<DocumentReference>emptyList());

    replayDefault();
    assertEquals(ImmutableSet.of(), cache.getCachedDocRefs(wikiRef, space));
    verifyDefault();
  }

  @Test
  public void testGetCachedDocRefs_multiWiki() throws Exception {
    WikiReference wikiRef1 = new WikiReference("wiki1");
    String space1 = "menuSpace1";
    DocumentReference docRef1 = new DocumentReference(wikiRef1.getName(), "space", "nav1");
    WikiReference wikiRef2 = new WikiReference("wiki2");
    String space2 = "menuSpace2";
    DocumentReference docRef2 = new DocumentReference(wikiRef2.getName(), "space", "nav2");
    String space3 = "menuSpace3";

    expectXWQL(wikiRef1, Arrays.asList(docRef1));
    expectMenuSpace(docRef1, space1, 0);
    expectXWQL(wikiRef2, Arrays.asList(docRef2));
    expectMenuSpace(docRef2, space2, 0);

    replayDefault();
    assertEquals(ImmutableSet.of(docRef1), cache.getCachedDocRefs(wikiRef1, space1));
    assertEquals(ImmutableSet.of(docRef2), cache.getCachedDocRefs(wikiRef2, space2));
    assertEquals(ImmutableSet.of(), cache.getCachedDocRefs(wikiRef1, space3));
    assertEquals(ImmutableSet.of(), cache.getCachedDocRefs(wikiRef2, space3));
    assertEquals(ImmutableSet.of(docRef1), cache.getCachedDocRefs(wikiRef1));
    assertEquals(ImmutableSet.of(docRef2), cache.getCachedDocRefs(wikiRef2));
    verifyDefault();
  }

  @Test
  public void testGetCachedDocRefs_nullKey() throws Exception {
    WikiReference wikiRef = new WikiReference("wiki");
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "nav");

    expectXWQL(wikiRef, Arrays.asList(docRef));
    expectMenuSpace(docRef, "", 0);

    replayDefault();
    assertEquals(ImmutableSet.of(), cache.getCachedDocRefs(wikiRef));
    verifyDefault();
  }

  @Test
  public void testGetCachedDocRefs_QueryException() throws Exception {
    WikiReference wikiRef = new WikiReference("wiki");
    String space = "menuSpace";

    expectXWQL(wikiRef, null);

    replayDefault();
    try {
      cache.getCachedDocRefs(wikiRef, space);
      fail("expecting CacheLoadingException");
    } catch (CacheLoadingException exc) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void testGetCachedDocRefs_XWikiException() throws Exception {
    WikiReference wikiRef = new WikiReference("wiki");
    String space = "menuSpace";
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "nav1");

    expectXWQL(wikiRef, Arrays.asList(docRef));
    expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andThrow(
        new XWikiException()).once();

    replayDefault();
    try {
      cache.getCachedDocRefs(wikiRef, space);
      fail("expecting CacheLoadingException");
    } catch (CacheLoadingException exc) {
      // expected
    }
    verifyDefault();
  }

  @Test
  public void testFlush() throws Exception {
    WikiReference wikiRef = new WikiReference("wiki");
    String space = "menuSpace";
    DocumentReference docRef = new DocumentReference(wikiRef.getName(), "space", "nav");
    WikiReference otherWikiRef = new WikiReference("otherWiki");

    expectXWQL(wikiRef, Arrays.asList(docRef));
    expectMenuSpace(docRef, space, 0);
    expectXWQL(wikiRef, Arrays.asList(docRef));
    expectMenuSpace(docRef, space, 0);

    replayDefault();
    assertEquals(ImmutableSet.of(docRef), cache.getCachedDocRefs(wikiRef, space));
    cache.flush(wikiRef); // this flushes docRef
    assertEquals(ImmutableSet.of(docRef), cache.getCachedDocRefs(wikiRef, space));
    cache.flush(otherWikiRef); // this doesnt flush docRef
    assertEquals(ImmutableSet.of(docRef), cache.getCachedDocRefs(wikiRef, space));
    verifyDefault();
  }

  private void expectXWQL(WikiReference wikiRef, List<DocumentReference> ret) throws Exception {
    Query queryMock = createMockAndAddToDefault(Query.class);
    String xwql = "select distinct doc.fullName from Document doc, doc.object("
        + "Celements2.NavigationConfigClass) as obj";
    expect(queryManagerMock.createQuery(eq(xwql), eq(Query.XWQL))).andReturn(queryMock).once();
    expect(queryMock.setWiki(eq(wikiRef.getName()))).andReturn(queryMock).once();
    IExpectationSetters<List<DocumentReference>> expSetter = expect(
        queryExecServiceMock.executeAndGetDocRefs(same(queryMock)));
    if (ret != null) {
      expSetter.andReturn(ret).once();
    } else {
      expSetter.andThrow(new QueryException("", null, null)).once();
    }
  }

  private void expectMenuSpace(DocumentReference docRef, String space, int nb) throws Exception {
    XWikiDocument doc = new XWikiDocument(docRef);
    BaseObject obj = new BaseObject();
    obj.setXClassReference(Utils.getComponent(
        INavigationClassConfig.class).getNavigationConfigClassRef(docRef.getWikiReference()));
    obj.setStringValue(INavigationClassConfig.MENU_SPACE_FIELD, space);
    doc.setXObject(nb, obj);
    expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc).once();
  }

}
