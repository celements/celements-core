package com.celements.store;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.web.Utils;

public class DocumentCacheStoreTest extends AbstractComponentTest {

  private static final String MOCK_STORE_HINT = "mockStoreHint";

  private DocumentCacheStore docCacheStore;
  private XWikiStoreInterface mockStore;

  @Before
  public void setUp_DocumentCacheStoreTest() throws Exception {
    mockStore = registerComponentMock(XWikiStoreInterface.class, MOCK_STORE_HINT);
    getConfigurationSource().setProperty(DocumentCacheStore.BACKING_STORE_STRATEGY,
        MOCK_STORE_HINT);
    expect(getWikiMock().Param(eq(DocumentCacheStore.PARAM_PAGEEXIST_CAPACITY))).andReturn(
        "100").anyTimes();
    expect(getWikiMock().Param(eq(DocumentCacheStore.PARAM_CACHE_CAPACITY))).andReturn(
        "100").anyTimes();
    docCacheStore = (DocumentCacheStore) Utils.getComponent(XWikiStoreInterface.class,
        DocumentCacheStore.COMPONENT_NAME);
  }

  @Test
  public void testSaveXWikiDoc() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "page");
    XWikiDocument savedDoc = new XWikiDocument(docRef);
    savedDoc.setNew(false);
    Capture<XWikiDocument> querySaveDocCapture = new Capture<>();
    expect(mockStore.loadXWikiDoc(capture(querySaveDocCapture), same(getContext()))).andReturn(
        savedDoc).once();
    Capture<XWikiDocument> savingDocCapture = new Capture<>();
    mockStore.saveXWikiDoc(capture(savingDocCapture), same(getContext()), eq(true));
    expectLastCall().once();
    replayDefault();
    docCacheStore.initalizeCache();
    XWikiDocument inputParamDoc = new XWikiDocument(docRef);
    XWikiDocument existingDocument = docCacheStore.loadXWikiDoc(inputParamDoc, getContext());
    XWikiDocument querySaveDoc = querySaveDocCapture.getValue();
    assertNotSame(inputParamDoc, querySaveDoc);
    assertEquals(inputParamDoc.getDocumentReference(), querySaveDoc.getDocumentReference());
    assertEquals(inputParamDoc.getLanguage(), querySaveDoc.getLanguage());
    // Save a document
    docCacheStore.saveXWikiDoc(existingDocument, getContext());
    XWikiDocument existingDocSaved = savingDocCapture.getValue();
    assertSame(existingDocument, existingDocSaved);
    String key = docCacheStore.getKeyWithLang(existingDocument);
    assertNull("on saving doc must be removed from Cache", docCacheStore.getDocFromCache(key));
    verifyDefault();
  }

  @Test
  public void testSaveXWikiDoc_noTransaction() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "page");
    XWikiDocument savedDoc = new XWikiDocument(docRef);
    savedDoc.setNew(false);
    Capture<XWikiDocument> querySaveDocCapture = new Capture<>();
    boolean bTransaction = false;
    expect(mockStore.loadXWikiDoc(capture(querySaveDocCapture), same(getContext()))).andReturn(
        savedDoc).once();
    Capture<XWikiDocument> savingDocCapture = new Capture<>();
    mockStore.saveXWikiDoc(capture(savingDocCapture), same(getContext()), eq(bTransaction));
    expectLastCall().once();
    replayDefault();
    docCacheStore.initalizeCache();
    XWikiDocument inputParamDoc = new XWikiDocument(docRef);
    XWikiDocument existingDocument = docCacheStore.loadXWikiDoc(inputParamDoc, getContext());
    XWikiDocument querySaveDoc = querySaveDocCapture.getValue();
    assertNotSame(inputParamDoc, querySaveDoc);
    assertEquals(inputParamDoc.getDocumentReference(), querySaveDoc.getDocumentReference());
    assertEquals(inputParamDoc.getLanguage(), querySaveDoc.getLanguage());
    // Save a document
    docCacheStore.saveXWikiDoc(existingDocument, getContext(), bTransaction);
    XWikiDocument existingDocSaved = savingDocCapture.getValue();
    assertSame(existingDocument, existingDocSaved);
    String key = docCacheStore.getKeyWithLang(existingDocument);
    assertNull("on saving doc must be removed from Cache", docCacheStore.getDocFromCache(key));
    verifyDefault();
  }

  @Test
  public void testGetKey() {
    DocumentReference docRef = new DocumentReference("wiki", "space", "page");
    XWikiDocument testDoc = new XWikiDocument(docRef);
    assertEquals("wiki:space.page", docCacheStore.getKeyWithLang(testDoc));
  }

  @Test
  public void testGetKey_lang() {
    DocumentReference docRef = new DocumentReference("wiki", "space", "page");
    XWikiDocument testDoc = new XWikiDocument(docRef);
    testDoc.setLanguage("en");
    assertEquals("wiki:space.page:en", docCacheStore.getKeyWithLang(testDoc));
  }

  @Test
  public void testGetKey_docRef_deflang() {
    DocumentReference docRef = new DocumentReference("wiki", "space", "page");
    assertEquals("wiki:space.page", docCacheStore.getKeyWithLang(docRef, ""));
  }

  @Test
  public void testGetKey_docRef_lang() {
    DocumentReference docRef = new DocumentReference("wiki", "space", "page");
    assertEquals("wiki:space.page:fr", docCacheStore.getKeyWithLang(docRef, "fr"));
  }

  @Test
  public void testInvalidateCacheFromClusterEventXWikiDocument() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "page");
    XWikiDocument savedDoc = new XWikiDocument(docRef);
    savedDoc.setNew(false);
    expect(mockStore.loadXWikiDoc(isA(XWikiDocument.class), same(getContext()))).andReturn(
        savedDoc).once();
    replayDefault();
    docCacheStore.initalizeCache();
    XWikiDocument inputParamDoc = new XWikiDocument(docRef);
    XWikiDocument existingDocument = docCacheStore.loadXWikiDoc(inputParamDoc, getContext());
    assertTrue(existingDocument.isFromCache());
    String key = docCacheStore.getKeyWithLang(existingDocument);
    assertNotNull("doc expected in cache", docCacheStore.getDocFromCache(key));
    assertTrue("doc expected in exists cache", docCacheStore.exists(existingDocument,
        getContext()));
    docCacheStore.invalidateCacheFromClusterEvent(existingDocument);
    assertNull("doc not in cache anymore", docCacheStore.getDocFromCache(key));
    verifyDefault();
  }

  @Test
  public void testInvalidateCacheFromClusterEventString() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "page");
    XWikiDocument savedDoc = new XWikiDocument(docRef);
    savedDoc.setNew(false);
    expect(mockStore.loadXWikiDoc(isA(XWikiDocument.class), same(getContext()))).andReturn(
        savedDoc).once();
    replayDefault();
    docCacheStore.initalizeCache();
    XWikiDocument inputParamDoc = new XWikiDocument(docRef);
    XWikiDocument existingDocument = docCacheStore.loadXWikiDoc(inputParamDoc, getContext());
    assertTrue(existingDocument.isFromCache());
    String key = docCacheStore.getKeyWithLang(existingDocument);
    assertNotNull("doc expected in cache", docCacheStore.getDocFromCache(key));
    assertTrue("doc expected in exists cache", docCacheStore.exists(existingDocument,
        getContext()));
    docCacheStore.invalidateCacheFromClusterEvent(key);
    assertNull("doc not in cache anymore", docCacheStore.getDocFromCache(key));
    verifyDefault();
  }

  @Test
  public void testLoadXWikiDoc() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "page");
    XWikiDocument savedDoc = new XWikiDocument(docRef);
    savedDoc.setNew(false);
    Capture<XWikiDocument> querySaveDocCapture = new Capture<>();
    expect(mockStore.loadXWikiDoc(capture(querySaveDocCapture), same(getContext()))).andReturn(
        savedDoc).once();
    replayDefault();
    docCacheStore.initalizeCache();
    XWikiDocument inputParamDoc = new XWikiDocument(docRef);
    XWikiDocument existingDocument = docCacheStore.loadXWikiDoc(inputParamDoc, getContext());
    XWikiDocument querySaveDoc = querySaveDocCapture.getValue();
    assertNotSame(inputParamDoc, querySaveDoc);
    assertEquals(inputParamDoc.getDocumentReference(), querySaveDoc.getDocumentReference());
    assertEquals(inputParamDoc.getLanguage(), querySaveDoc.getLanguage());
    assertFalse(existingDocument.isNew());
    assertTrue(existingDocument.isFromCache());
    String key = docCacheStore.getKeyWithLang(existingDocument);
    assertNotNull("doc expected in cache", docCacheStore.getDocFromCache(key));
    assertTrue(docCacheStore.exists(existingDocument, getContext()));

    assertSame(existingDocument, docCacheStore.loadXWikiDoc(inputParamDoc, getContext()));
    verifyDefault();
  }

  @Test
  public void testDeleteXWikiDoc() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "page");
    XWikiDocument savedDoc = new XWikiDocument(docRef);
    savedDoc.setNew(false);
    Capture<XWikiDocument> querySavedDocCapture = new Capture<>();
    expect(mockStore.loadXWikiDoc(capture(querySavedDocCapture), same(getContext()))).andReturn(
        savedDoc).once();
    Capture<XWikiDocument> deletingDocCapture = new Capture<>();
    mockStore.deleteXWikiDoc(capture(deletingDocCapture), same(getContext()));
    expectLastCall().once();
    replayDefault();
    docCacheStore.initalizeCache();
    XWikiDocument inputParamDoc = new XWikiDocument(docRef);
    XWikiDocument existingDocument = docCacheStore.loadXWikiDoc(inputParamDoc, getContext());
    XWikiDocument querySavedDoc = querySavedDocCapture.getValue();
    assertNotSame(inputParamDoc, querySavedDoc);
    assertEquals(inputParamDoc.getDocumentReference(), querySavedDoc.getDocumentReference());
    assertEquals(inputParamDoc.getLanguage(), querySavedDoc.getLanguage());
    // Save a document
    docCacheStore.deleteXWikiDoc(existingDocument, getContext());
    XWikiDocument existingDocDeleted = deletingDocCapture.getValue();
    assertSame(existingDocument, existingDocDeleted);
    String key = docCacheStore.getKeyWithLang(existingDocument);
    assertNull("on deleting doc must be removed from Cache", docCacheStore.getDocFromCache(key));
    verifyDefault();
  }

  @Test
  public void testExists_true() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "page");
    Capture<XWikiDocument> querySaveDocCapture = new Capture<>();
    boolean docExists = true;
    expect(mockStore.exists(capture(querySaveDocCapture), same(getContext()))).andReturn(
        docExists).once();
    replayDefault();
    docCacheStore.initalizeCache();
    XWikiDocument inputParamDoc = new XWikiDocument(docRef);
    boolean existsDoc = docCacheStore.exists(inputParamDoc, getContext());
    assertEquals(docExists, existsDoc);
    XWikiDocument querySaveDoc = querySaveDocCapture.getValue();
    assertSame(inputParamDoc, querySaveDoc);
    assertEquals(inputParamDoc.getDocumentReference(), querySaveDoc.getDocumentReference());
    assertEquals(inputParamDoc.getLanguage(), querySaveDoc.getLanguage());
    assertEquals("result must be in exists cache", docExists, docCacheStore.exists(inputParamDoc,
        getContext()));
    verifyDefault();
  }

  @Test
  public void testExists_false() throws Exception {
    DocumentReference docRef = new DocumentReference("wiki", "space", "page");
    Capture<XWikiDocument> querySaveDocCapture = new Capture<>();
    boolean docExists = false;
    expect(mockStore.exists(capture(querySaveDocCapture), same(getContext()))).andReturn(
        docExists).once();
    replayDefault();
    docCacheStore.initalizeCache();
    XWikiDocument inputParamDoc = new XWikiDocument(docRef);
    boolean existsDoc = docCacheStore.exists(inputParamDoc, getContext());
    assertEquals(docExists, existsDoc);
    XWikiDocument querySaveDoc = querySaveDocCapture.getValue();
    assertSame(inputParamDoc, querySaveDoc);
    assertEquals(inputParamDoc.getDocumentReference(), querySaveDoc.getDocumentReference());
    assertEquals(inputParamDoc.getLanguage(), querySaveDoc.getLanguage());
    assertEquals("result must be in exists cache", docExists, docCacheStore.exists(inputParamDoc,
        getContext()));
    verifyDefault();
  }

}
