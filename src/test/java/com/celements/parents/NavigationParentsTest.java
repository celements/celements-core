package com.celements.parents;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.cache.IDocumentReferenceCache;
import com.celements.common.test.AbstractComponentTest;
import com.celements.navigation.NavigationCache;
import com.google.common.collect.ImmutableSet;
import com.xpn.xwiki.web.Utils;

public class NavigationParentsTest extends AbstractComponentTest {

  private NavigationParents navParents;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp_NavigationParentsTest() throws Exception {
    navParents = (NavigationParents) Utils.getComponent(IDocParentProviderRole.class,
        NavigationParents.NAME);
    navParents.navCache = createDefaultMock(IDocumentReferenceCache.class);
  }

  @After
  @SuppressWarnings("unchecked")
  public void tearDown_NavigationParentsTest() {
    navParents.navCache = Utils.getComponent(IDocumentReferenceCache.class, NavigationCache.NAME);
  }

  @Test
  public void testGetDocumentParentsList() throws Exception {
    WikiReference wikiRef = new WikiReference("db");
    DocumentReference docRef = new DocumentReference("myDoc", new SpaceReference("space", wikiRef));
    DocumentReference parentLocal = new DocumentReference("parent", new SpaceReference(
        "parentSpace", wikiRef));
    WikiReference centralWikiRef = new WikiReference("celements2web");
    DocumentReference parentCentral = new DocumentReference("parent", new SpaceReference(
        "parentSpace", centralWikiRef));

    expect(navParents.navCache.getCachedDocRefs(eq(wikiRef), eq("space"))).andReturn(
        ImmutableSet.of(parentLocal)).once();
    expect(navParents.navCache.getCachedDocRefs(eq(centralWikiRef), eq("space"))).andReturn(
        ImmutableSet.of(parentCentral)).once();

    List<DocumentReference> docParentsList = Arrays.asList(parentLocal, parentCentral);
    replayDefault();
    assertEquals(docParentsList, navParents.getDocumentParentsList(docRef));
    verifyDefault();
  }

}
