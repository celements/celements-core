package com.celements.parents;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class DocumentParentsListerTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private DocumentParentsLister docParentsLister;
  private Map<String, IDocParentProviderRole> docParentProviderMapBackup;

  @Before
  public void setUp_WebUtilsServiceTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    docParentsLister = (DocumentParentsLister) Utils.getComponent(
        IDocumentParentsListerRole.class);
    docParentProviderMapBackup = docParentsLister.docParentProviderMap;
    docParentsLister.docParentProviderMap = new HashMap<String, IDocParentProviderRole>(
        docParentProviderMapBackup);
  }

  @After
  public void tearDown_DocumentParentsListerTest() {
    docParentsLister.docParentProviderMap = docParentProviderMapBackup;
  }

  @Test
  public void testGetDocumentParentsList_not_include() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myDoc");
    DocumentReference parentRef1 = new DocumentReference(context.getDatabase(), "mySpace",
        "parent1");
    DocumentReference parentRef2 = new DocumentReference(context.getDatabase(), "mySpace",
        "parent2");
    XWikiDocument doc = new XWikiDocument(docRef);
    XWikiDocument docP1 = new XWikiDocument(parentRef1);
    XWikiDocument docP2 = new XWikiDocument(parentRef2);
    docP1.setParentReference(parentRef2.extractReference(EntityType.DOCUMENT));
    doc.setParentReference(parentRef1.extractReference(EntityType.DOCUMENT));
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
    expect(xwiki.getDocument(eq(parentRef1), same(context))).andReturn(docP1).once();
    expect(xwiki.exists(eq(parentRef1), same(context))).andReturn(true).anyTimes();
    expect(xwiki.getDocument(eq(parentRef2), same(context))).andReturn(docP2).once();
    expect(xwiki.exists(eq(parentRef2), same(context))).andReturn(true).anyTimes();
    List<DocumentReference> docParentsList = Arrays.asList(parentRef1, parentRef2);
    replayDefault();
    assertEquals(docParentsList, docParentsLister.getDocumentParentsList(docRef, false));
    verifyDefault();
  }

  @Test
  public void testGetDocumentParentsList_not_include_testProvider_empty(
      ) throws Exception {
    IDocParentProviderRole testProviderMock = createMockAndAddToDefault(
        IDocParentProviderRole.class);
    docParentsLister.docParentProviderMap.put("TestProvider", testProviderMock);

    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myDoc");
    XWikiDocument doc = new XWikiDocument(docRef);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
    expect(testProviderMock.getDocumentParentsList(eq(docRef))).andReturn(
        Collections.<DocumentReference>emptyList()).once();
    List<DocumentReference> docParentsList = Collections.emptyList();
    replayDefault();
    assertEquals(docParentsList, docParentsLister.getDocumentParentsList(docRef, false));
    verifyDefault();
  }

  @Test
  public void testGetDocumentParentsList_not_include_testProvider_hasParent(
      ) throws Exception {
    IDocParentProviderRole testProviderMock = createMockAndAddToDefault(
        IDocParentProviderRole.class);
    docParentsLister.docParentProviderMap.put("TestProvider", testProviderMock);

    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myDoc");
    XWikiDocument doc = new XWikiDocument(docRef);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();

    DocumentReference testProviderParentRef = new DocumentReference(context.getDatabase(),
        "MySpaceTest", "TestProviderDoc");
    XWikiDocument testProviderParentDoc = new XWikiDocument(testProviderParentRef);
    expect(xwiki.getDocument(eq(testProviderParentRef), same(context))).andReturn(
        testProviderParentDoc).once();
    expect(xwiki.exists(eq(testProviderParentRef), same(context))).andReturn(true
        ).anyTimes();

    expect(testProviderMock.getDocumentParentsList(eq(docRef))).andReturn(Arrays.asList(
        testProviderParentRef)).once();
    List<DocumentReference> docParentsList = Arrays.asList(testProviderParentRef);
    replayDefault();
    assertEquals(docParentsList, docParentsLister.getDocumentParentsList(docRef, false));
    verifyDefault();
  }

  @Test
  public void testGetDocumentParentsList_includeDoc() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myDoc");
    DocumentReference parentRef1 = new DocumentReference(context.getDatabase(), "mySpace",
        "parent1");
    DocumentReference parentRef2 = new DocumentReference(context.getDatabase(), "mySpace",
        "parent2");

    XWikiDocument doc = new XWikiDocument(docRef);
    XWikiDocument docP1 = new XWikiDocument(parentRef1);
    XWikiDocument docP2 = new XWikiDocument(parentRef2);
    docP1.setParentReference(parentRef2.extractReference(EntityType.DOCUMENT));
    doc.setParentReference(parentRef1.extractReference(EntityType.DOCUMENT));

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(true).anyTimes();
    expect(xwiki.getDocument(eq(parentRef1), same(context))).andReturn(docP1).once();
    expect(xwiki.exists(eq(parentRef1), same(context))).andReturn(true).anyTimes();
    expect(xwiki.getDocument(eq(parentRef2), same(context))).andReturn(docP2).once();
    expect(xwiki.exists(eq(parentRef2), same(context))).andReturn(true).anyTimes();

    List<DocumentReference> docParentsList = Arrays.asList(docRef, parentRef1,
        parentRef2);
    replayDefault();
    assertEquals(docParentsList, docParentsLister.getDocumentParentsList(docRef, true));
    verifyDefault();
  }

  @Test
  public void testGetDocumentParentsList_includeDoc_testProvider() throws Exception {
    IDocParentProviderRole testProviderMock = createMockAndAddToDefault(
        IDocParentProviderRole.class);
    docParentsLister.docParentProviderMap.put("TestProvider", testProviderMock);

    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myDoc");
    DocumentReference parentRef1 = new DocumentReference(context.getDatabase(), "mySpace",
        "parent1");
    DocumentReference parentRef2 = new DocumentReference(context.getDatabase(), "mySpace",
        "parent2");

    XWikiDocument doc = new XWikiDocument(docRef);
    XWikiDocument docP1 = new XWikiDocument(parentRef1);
    XWikiDocument docP2 = new XWikiDocument(parentRef2);
    docP1.setParentReference(parentRef2.extractReference(EntityType.DOCUMENT));
    doc.setParentReference(parentRef1.extractReference(EntityType.DOCUMENT));

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(true).anyTimes();
    expect(xwiki.getDocument(eq(parentRef1), same(context))).andReturn(docP1).once();
    expect(xwiki.exists(eq(parentRef1), same(context))).andReturn(true).anyTimes();
    expect(xwiki.getDocument(eq(parentRef2), same(context))).andReturn(docP2).once();
    expect(xwiki.exists(eq(parentRef2), same(context))).andReturn(true).anyTimes();

    DocumentReference testProviderParentRef = new DocumentReference(context.getDatabase(),
        "MySpaceTest", "TestProviderDoc");
    DocumentReference testProviderParentRef2 = new DocumentReference(context.getDatabase(),
        "MySpaceTest", "TestProviderDoc2");
    XWikiDocument testProviderParentDoc2 = new XWikiDocument(testProviderParentRef2);
    expect(xwiki.getDocument(eq(testProviderParentRef2), same(context))).andReturn(
        testProviderParentDoc2).once();
    expect(xwiki.exists(eq(testProviderParentRef2), same(context))).andReturn(false
        ).anyTimes();

    expect(testProviderMock.getDocumentParentsList(eq(parentRef2))).andReturn(
        Arrays.asList(testProviderParentRef, testProviderParentRef2)).once();

    List<DocumentReference> docParentsList = Arrays.asList(docRef, parentRef1,
        parentRef2, testProviderParentRef, testProviderParentRef2);
    replayDefault();
    assertEquals(docParentsList, docParentsLister.getDocumentParentsList(docRef, true));
    verifyDefault();
  }

  @Test
  public void testGetDocumentParentsList_includeDoc_notexist() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myDoc");

    XWikiDocument doc = new XWikiDocument(docRef);

    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(false).anyTimes();

    List<DocumentReference> docParentsList = Arrays.asList(docRef);
    replayDefault();
    assertEquals(docParentsList, docParentsLister.getDocumentParentsList(docRef, true));
    verifyDefault();
  }

  @Test
  public void testGetDocumentParentsList_includeDoc_notexist_testProvider(
      ) throws Exception {
    IDocParentProviderRole testProviderMock = createMockAndAddToDefault(
        IDocParentProviderRole.class);
    docParentsLister.docParentProviderMap.put("TestProvider", testProviderMock);

    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myDoc");
    XWikiDocument doc = new XWikiDocument(docRef);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(doc).once();
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(false).anyTimes();

    DocumentReference testProviderParentRef = new DocumentReference(context.getDatabase(),
        "MySpaceTest", "TestProviderDoc");
    XWikiDocument testProviderParentDoc = new XWikiDocument(testProviderParentRef);
    expect(xwiki.getDocument(eq(testProviderParentRef), same(context))).andReturn(
        testProviderParentDoc).once();
    expect(xwiki.exists(eq(testProviderParentRef), same(context))).andReturn(false
        ).anyTimes();

    expect(testProviderMock.getDocumentParentsList(eq(docRef))).andReturn(Arrays.asList(
        testProviderParentRef)).once();

    List<DocumentReference> docParentsList = Arrays.asList(docRef, testProviderParentRef);
    replayDefault();
    assertEquals(docParentsList, docParentsLister.getDocumentParentsList(docRef, true));
    verifyDefault();
  }

}
