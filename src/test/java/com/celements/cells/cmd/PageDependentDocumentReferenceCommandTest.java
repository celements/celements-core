package com.celements.cells.cmd;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class PageDependentDocumentReferenceCommandTest
  extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private PageDependentDocumentReferenceCommand pageDepDocRefCmd;
  private XWikiDocument document;
  private XWiki xwiki;
  private DocumentReference cellDocRef;
  private XWikiDocument cellDoc;

  @Before
  public void setUp_PageDependentDocumentReferenceCommandTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    document = createMock(XWikiDocument.class);
    cellDocRef = new DocumentReference(context.getDatabase(), "MyLayout", "Cell2");
    cellDoc = new XWikiDocument(cellDocRef);
    expect(xwiki.getDocument(eq(cellDocRef), same(context))).andReturn(cellDoc).anyTimes(
        );
    pageDepDocRefCmd = new PageDependentDocumentReferenceCommand();
  }

  @Test
  public void testGetPageDepCellConfigClassDocRef() {
    replayAll();
    assertEquals(new DocumentReference(context.getDatabase(),
        PageDependentDocumentReferenceCommand.PAGE_DEP_CELL_CONFIG_CLASS_SPACE,
        PageDependentDocumentReferenceCommand.PAGE_DEP_CELL_CONFIG_CLASS_DOC),
    pageDepDocRefCmd.getPageDepCellConfigClassDocRef(context));
    verifyAll();
  }

  @Test
  public void testGetCurrentDocumentSpaceName_ZeroSpaces() {
    DocumentReference currentDocRef = createMock(DocumentReference.class);
    expect(document.getDocumentReference()).andReturn(currentDocRef).atLeastOnce();
    List<SpaceReference> emptySpaceRefList = Collections.emptyList();
    expect(currentDocRef.getSpaceReferences()).andReturn(emptySpaceRefList);
    expect(xwiki.getDefaultSpace(same(context))).andReturn("myDefaultSpace").once();
    replayAll(currentDocRef);
    assertEquals("myDefaultSpace", pageDepDocRefCmd.getCurrentDocumentSpaceName(document,
        context));
    verifyAll(currentDocRef);
  }

  @Test
  public void testGetCurrentDocumentSpaceName_greaterZeroSpaces() {
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "Content", "myDocument");
    expect(document.getDocumentReference()).andReturn(currentDocRef).atLeastOnce();
    replayAll();
    assertEquals("Content", pageDepDocRefCmd.getCurrentDocumentSpaceName(document,
        context));
    verifyAll();
  }

  @Test
  public void testGetDepCellSpace_cellDocWithoutObject() throws Exception {
    replayAll();
    assertEquals("", pageDepDocRefCmd.getDepCellSpace(cellDocRef, context));
    verifyAll();
  }

  @Test
  public void testGetDepCellSpace_cellDocWithEmptyObject() throws Exception {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(
        context));
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(context),
        Arrays.asList(cellConfig));
    replayAll();
    assertEquals("", pageDepDocRefCmd.getDepCellSpace(cellDocRef, context));
    verifyAll();
  }

  @Test
  public void testGetDepCellSpace_cellDocWithNonEmptyObject() throws Exception {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(
        context));
    cellConfig.setStringValue(PageDependentDocumentReferenceCommand.PROPNAME_SPACE_NAME,
      "myDepSpace");
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(context),
        Arrays.asList(cellConfig));
    replayAll();
    assertEquals("myDepSpace", pageDepDocRefCmd.getDepCellSpace(cellDocRef, context));
    verifyAll();
  }

  @Test
  public void testIsCurrentDocument_cellDocWithoutObject() throws Exception {
    replayAll();
    assertTrue(pageDepDocRefCmd.isCurrentDocument(document, cellDocRef, context));
    verifyAll();
  }

  @Test
  public void testIsCurrentDocument_cellDocWithEmptyObject() throws Exception {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(
        context));
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(context),
        Arrays.asList(cellConfig));
    replayAll();
    assertTrue(pageDepDocRefCmd.isCurrentDocument(document, cellDocRef, context));
    verifyAll();
  }

  @Test
  public void testIsCurrentDocument_cellDocWithNonEmptyObject() throws Exception {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(
        context));
    cellConfig.setStringValue(PageDependentDocumentReferenceCommand.PROPNAME_SPACE_NAME,
      "myDepSpace");
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(context),
        Arrays.asList(cellConfig));
    replayAll();
    assertFalse(pageDepDocRefCmd.isCurrentDocument(document, cellDocRef, context));
    verifyAll();
  }

  @Test
  public void testIsCurrentDocument_Exception() {
    try {
      reset(xwiki);
      expect(xwiki.getDocument(eq(cellDocRef), same(context))).andThrow(
          new XWikiException()).atLeastOnce();
      replayAll();
      assertTrue("expecting fallback to currentDoc.", pageDepDocRefCmd.isCurrentDocument(
          document, cellDocRef, context));
      verifyAll();
    } catch (XWikiException exp) {
      fail("Expecting isCurrentDocument to catch XWikiException and returning True.");
    }
  }

  @Test
  public void testGetDependentDocumentSpace_cellDocWithoutObject_Content() {
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "Content", "myDocument");
    expect(document.getDocumentReference()).andReturn(currentDocRef).atLeastOnce();
    replayAll();
    assertEquals("Content", pageDepDocRefCmd.getDependentDocumentSpace(document,
        cellDocRef, context));
    verifyAll();
  }

  @Test
  public void testGetDependentDocumentSpace_cellDocWithoutObject_anySpace() {
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myDocument");
    expect(document.getDocumentReference()).andReturn(currentDocRef).atLeastOnce();
    replayAll();
    assertEquals("mySpace", pageDepDocRefCmd.getDependentDocumentSpace(document,
        cellDocRef, context));
    verifyAll();
  }

  @Test
  public void testGetDependentDocumentSpace_cellDocWithEmptyObject_Content() {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(
        context));
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(context),
        Arrays.asList(cellConfig));
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "Content", "myDocument");
    expect(document.getDocumentReference()).andReturn(currentDocRef).atLeastOnce();
    replayAll();
    assertEquals("Content", pageDepDocRefCmd.getDependentDocumentSpace(document,
        cellDocRef, context));
    verifyAll();
  }

  @Test
  public void testGetDependentDocumentSpace_cellDocWithEmptyObject_anySpace() {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(
        context));
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(context),
        Arrays.asList(cellConfig));
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "MySpace", "myDocument");
    expect(document.getDocumentReference()).andReturn(currentDocRef).atLeastOnce();
    replayAll();
    assertEquals("MySpace", pageDepDocRefCmd.getDependentDocumentSpace(document,
        cellDocRef, context));
    verifyAll();
  }

  @Test
  public void testGetDependentDocumentSpace_cellDocWithNonEmptyObject_Content() {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setStringValue(PageDependentDocumentReferenceCommand.PROPNAME_SPACE_NAME,
        "myDepSpace");
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(
        context));
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(context),
        Arrays.asList(cellConfig));
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "Content", "myDocument");
    expect(document.getDocumentReference()).andReturn(currentDocRef).atLeastOnce();
    replayAll();
    assertEquals("Content_myDepSpace", pageDepDocRefCmd.getDependentDocumentSpace(
        document, cellDocRef, context));
    verifyAll();
  }

  @Test
  public void testGetDependentDocumentSpace_cellDocWithNonEmptyObject_anySpace() {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setStringValue(PageDependentDocumentReferenceCommand.PROPNAME_SPACE_NAME,
        "myDepSpace");
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(
        context));
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(context),
        Arrays.asList(cellConfig));
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myDocument");
    expect(document.getDocumentReference()).andReturn(currentDocRef).atLeastOnce();
    replayAll();
    assertEquals("mySpace_myDepSpace", pageDepDocRefCmd.getDependentDocumentSpace(
        document, cellDocRef, context));
    verifyAll();
  }

  @Test
  public void testGetDependentDocumentSpace_Exception() {
    try {
      reset(xwiki);
      expect(xwiki.getDocument(eq(cellDocRef), same(context))).andThrow(
          new XWikiException()).atLeastOnce();
      BaseObject cellConfig = new BaseObject();
      cellConfig.setStringValue(PageDependentDocumentReferenceCommand.PROPNAME_SPACE_NAME,
          "myDepSpace");
      cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(
          context));
      cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(context),
          Arrays.asList(cellConfig));
      DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
          "mySpace", "myDocument");
      expect(document.getDocumentReference()).andReturn(currentDocRef).atLeastOnce();
      replayAll();
      assertEquals("mySpace", pageDepDocRefCmd.getDependentDocumentSpace(document,
          cellDocRef, context));
      verifyAll();
    } catch (XWikiException exp) {
      fail("expecting to catch exception and fallback to current space");
    }
  }

  @Test
  public void testGetDocumentReference_isCurrent() {
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myDocument");
    expect(document.getDocumentReference()).andReturn(expectedDocRef).once();
    replayAll();
    assertEquals(expectedDocRef, pageDepDocRefCmd.getDocumentReference(document,
        cellDocRef, context));
    verifyAll();
  }

  @Test
  public void testGetDocumentReference_isNotCurrent() {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setStringValue(PageDependentDocumentReferenceCommand.PROPNAME_SPACE_NAME,
        "myDepSpace");
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(
        context));
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(context),
        Arrays.asList(cellConfig));
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myDocument");
    expect(document.getDocumentReference()).andReturn(currentDocRef).atLeastOnce();
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_myDepSpace", "myDocument");
    replayAll();
    assertEquals(expectedDocRef, pageDepDocRefCmd.getDocumentReference(document,
        cellDocRef, context));
    verifyAll();
  }

  @Test
  public void testGetDocument_isCurrent() throws XWikiException {
    replayAll();
    assertSame(document, pageDepDocRefCmd.getDocument(document, cellDocRef, context));
    verifyAll();
  }

  @Test
  public void testGetDocument_isNotCurrent() throws XWikiException {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setStringValue(PageDependentDocumentReferenceCommand.PROPNAME_SPACE_NAME,
        "myDepSpace");
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(
        context));
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(context),
        Arrays.asList(cellConfig));
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myDocument");
    expect(document.getDocumentReference()).andReturn(currentDocRef).atLeastOnce();
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_myDepSpace", "myDocument");
    XWikiDocument expectedDoc = new XWikiDocument(expectedDocRef);
    expect(xwiki.getDocument(eq(expectedDocRef), same(context))).andReturn(expectedDoc
        ).once();
    replayAll();
    assertEquals(expectedDoc, pageDepDocRefCmd.getDocument(document, cellDocRef, context)
        );
    verifyAll();
  }

  @Test
  public void testGetTranslatedDocument_isCurrent() throws XWikiException {
    replayAll();
    assertSame(document, pageDepDocRefCmd.getTranslatedDocument(document, cellDocRef,
        context));
    verifyAll();
  }

  @Test
  public void testGetTranslatedDocument_isNotCurrent() throws XWikiException {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setStringValue(PageDependentDocumentReferenceCommand.PROPNAME_SPACE_NAME,
        "myDepSpace");
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(
        context));
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(context),
        Arrays.asList(cellConfig));
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myDocument");
    expect(document.getDocumentReference()).andReturn(currentDocRef).atLeastOnce();
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_myDepSpace", "myDocument");
    XWikiDocument expectedDoc = createMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq(expectedDocRef), same(context))).andReturn(expectedDoc
        ).once();
    XWikiDocument expectedTransDoc = new XWikiDocument(expectedDocRef);
    expect(expectedDoc.getTranslatedDocument(same(context))).andReturn(expectedTransDoc
        ).once();
    replayAll(expectedDoc);
    assertEquals(expectedTransDoc, pageDepDocRefCmd.getTranslatedDocument(document,
        cellDocRef, context));
    verifyAll(expectedDoc);
  }


  private void replayAll(Object ... mocks) {
    replay(xwiki, document);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, document);
    verify(mocks);
  }

}
