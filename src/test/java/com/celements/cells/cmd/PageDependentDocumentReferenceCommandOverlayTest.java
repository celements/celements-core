package com.celements.cells.cmd;


import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.web.utils.IWebUtils;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class PageDependentDocumentReferenceCommandOverlayTest
    extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private PageDependentDocumentReferenceCommand pageDepDocRefCmd;
  private XWikiDocument document;
  private XWiki xwiki;
  private DocumentReference cellDocRef;
  private XWikiDocument cellDoc;
  private IWebUtils webUtilsMock;

  @Before
  public void setUp_PageDependentDocumentReferenceCommandOverlayTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    document = createMock(XWikiDocument.class);
    cellDocRef = new DocumentReference(context.getDatabase(), "MyLayout", "Cell2");
    cellDoc = new XWikiDocument(cellDocRef);
    expect(xwiki.getDocument(eq(cellDocRef), same(context))).andReturn(cellDoc).anyTimes(
        );
    pageDepDocRefCmd = new PageDependentDocumentReferenceCommand();
    webUtilsMock = createMock(IWebUtils.class);
    pageDepDocRefCmd.webUtils = webUtilsMock;
  }

  @Test
  public void testIsInheritable() throws Exception {
    replayAll();
    assertFalse("default expected false", pageDepDocRefCmd.isInheritable(cellDocRef,
        context));
    verifyAll();
  }

  @Test
  public void testIsInheritable_noValue() throws Exception {
    setDependentDocSpace("leftColumn", null);
    replayAll();
    assertFalse("default expected false", pageDepDocRefCmd.isInheritable(cellDocRef,
        context));
    verifyAll();
  }

  @Test
  public void testIsInheritable_zero() throws Exception {
    setDependentDocSpace("leftColumn", 0);
    replayAll();
    assertFalse("default expected false", pageDepDocRefCmd.isInheritable(cellDocRef,
        context));
    verifyAll();
  }

  @Test
  public void testIsInheritable_one() throws Exception {
    setDependentDocSpace("leftColumn", 1);
    replayAll();
    assertTrue("default expected false", pageDepDocRefCmd.isInheritable(cellDocRef,
        context));
    verifyAll();
  }

  @Test
  public void testGetDependentDocList() {
    String fullName = "mySpace.MyDoc";
    List<String> expDepDocList = Arrays.asList("leftColumn_mySpace.MyDoc",
        "leftColumn_mySpace.MyParentDoc");
    List<String> docParentList = Arrays.asList("mySpace.MyDoc", "mySpace.MyParentDoc");
    expect(webUtilsMock.getDocumentParentsList(eq(fullName), eq(true), same(context))
        ).andReturn(docParentList);
    replayAll();
    List<String> depDocList = pageDepDocRefCmd.getDependentDocList(fullName,
        "leftColumn_mySpace", context);
    assertEquals(expDepDocList, depDocList);
    verifyAll();
  }

  @Test
  public void testGetDependentDocumentReference() throws Exception {
    setDependentDocSpace("leftColumn", 1);
    String fullName = "mySpace.MyDoc";
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myDoc");
    String parentFullName = "mySpace.MyParentDoc";
    DocumentReference parentDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "MyParentDoc");
    List<String> docParentList = Arrays.asList(fullName, parentFullName);
    expect(webUtilsMock.getDocumentParentsList(eq(fullName), eq(true), same(context))
        ).andReturn(docParentList);
    DocumentReference expDepDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_leftColumn", "MyParentDoc");
    expect(document.getDocumentReference()).andReturn(myDocRef).atLeastOnce();
    expect(document.getFullName()).andReturn(fullName).atLeastOnce();
    expect(xwiki.exists(eq("mySpace_leftColumn.MyDoc"), same(context))).andReturn(
        false).atLeastOnce();
    String leftParentFullName = "mySpace_leftColumn.MyParentDoc";
    expect(xwiki.exists(eq(leftParentFullName), same(context))).andReturn(
        true).atLeastOnce();
    XWikiDocument leftParentDoc = createMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq(leftParentFullName), same(context))).andReturn(
        leftParentDoc).atLeastOnce();
    expect(leftParentDoc.getContent()).andReturn("parent Content").atLeastOnce();
    expect(leftParentDoc.getDocumentReference()).andReturn(expDepDocRef).atLeastOnce();
    replayAll(leftParentDoc);
    DocumentReference depDocRef = pageDepDocRefCmd.getDependentDocumentReference(document,
        cellDocRef, context);
    assertEquals(expDepDocRef, depDocRef);
    verifyAll(leftParentDoc);
  }

  @Test
  public void testGetDependentDocumentReference_defaultContent() throws Exception {
    setDependentDocSpace("leftColumn", 1);
    String fullName = "mySpace.MyDoc";
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myDoc");
    String parentFullName = "mySpace.MyParentDoc";
    List<String> docParentList = Arrays.asList(fullName, parentFullName);
    expect(webUtilsMock.getDocumentParentsList(eq(fullName), eq(true), same(context))
        ).andReturn(docParentList);
    DocumentReference expDepDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_leftColumn",
        PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME);
    expect(document.getDocumentReference()).andReturn(myDocRef).atLeastOnce();
    expect(document.getFullName()).andReturn(fullName).atLeastOnce();
    expect(xwiki.exists(eq("mySpace_leftColumn.MyDoc"), same(context))).andReturn(
        false).atLeastOnce();
    String leftParentFullName = "mySpace_leftColumn.MyParentDoc";
    expect(xwiki.exists(eq(leftParentFullName), same(context))).andReturn(
        false).atLeastOnce();
    replayAll();
    DocumentReference depDocRef = pageDepDocRefCmd.getDependentDocumentReference(document,
        cellDocRef, context);
    assertEquals(expDepDocRef, depDocRef);
    verifyAll();
  }


  private void setDependentDocSpace(String depDocSpace, Integer isInheritable) {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setStringValue(PageDependentDocumentReferenceCommand.PROPNAME_SPACE_NAME,
        depDocSpace);
    if (isInheritable != null) {
      cellConfig.setIntValue(
          PageDependentDocumentReferenceCommand.PROPNAME_IS_INHERITABLE, isInheritable);
    }
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(
        context));
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(context),
        Arrays.asList(cellConfig));
  }

  private void replayAll(Object ... mocks) {
    replay(xwiki, document, webUtilsMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, document, webUtilsMock);
    verify(mocks);
  }

}
