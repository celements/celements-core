package com.celements.cells.cmd;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceValueProvider;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.navigation.service.ITreeNodeService;
import com.celements.web.plugin.cmd.PageLayoutCommand;
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
  private ComponentDescriptor<EntityReferenceValueProvider> defaultValueProviderDesc;
  private EntityReferenceValueProvider savedDefaultValueProviderService;
  private EntityReferenceValueProvider defValueProviderMock;

  @Before
  public void setUp_PageDependentDocumentReferenceCommandTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    document = createMockAndAddToDefault(XWikiDocument.class);
    cellDocRef = new DocumentReference(context.getDatabase(), "MyLayout", "Cell2");
    cellDoc = new XWikiDocument(cellDocRef);
    expect(xwiki.getDocument(eq(cellDocRef), same(context))).andReturn(cellDoc).anyTimes(
        );
    pageDepDocRefCmd = new PageDependentDocumentReferenceCommand();
    defaultValueProviderDesc = getComponentManager().getComponentDescriptor(
        EntityReferenceValueProvider.class, "default");
    savedDefaultValueProviderService = getComponentManager().lookup(
        EntityReferenceValueProvider.class);
    getComponentManager().unregisterComponent(ITreeNodeService.class, "default");
    defValueProviderMock = createMockAndAddToDefault(EntityReferenceValueProvider.class);
    getComponentManager().registerComponent(defaultValueProviderDesc,
        defValueProviderMock);
  }

  @After
  public void shutdown_EmptyCheckCommandTest() throws Exception {
    getComponentManager().unregisterComponent(EntityReferenceValueProvider.class,
        "default");
    getComponentManager().registerComponent(defaultValueProviderDesc,
        savedDefaultValueProviderService);
  }

  @Test
  public void testGetPageLayoutCmd() {
    pageDepDocRefCmd.pageLayoutCmd = null;
    replayDefault();
    assertNotNull(pageDepDocRefCmd.getPageLayoutCmd());
    verifyDefault();
  }

  @Test
  public void testInject_pageLayoutCmdMock() {
    PageLayoutCommand pageLayoutCmdMock = createMockAndAddToDefault(
        PageLayoutCommand.class);
    pageDepDocRefCmd.pageLayoutCmd = pageLayoutCmdMock;
    replayDefault();
    assertSame(pageLayoutCmdMock, pageDepDocRefCmd.getPageLayoutCmd());
    verifyDefault();
  }

  @Test
  public void testGetPageDepCellConfigClassDocRef() {
    replayDefault();
    assertEquals(new DocumentReference(context.getDatabase(),
        PageDependentDocumentReferenceCommand.PAGE_DEP_CELL_CONFIG_CLASS_SPACE,
        PageDependentDocumentReferenceCommand.PAGE_DEP_CELL_CONFIG_CLASS_DOC),
    pageDepDocRefCmd.getPageDepCellConfigClassDocRef());
    verifyDefault();
  }

  @Test
  public void testGetCurrentLayoutRef() {
    PageLayoutCommand pageLayoutCmdMock = createMockAndAddToDefault(
        PageLayoutCommand.class);
    pageDepDocRefCmd.pageLayoutCmd = pageLayoutCmdMock;
    SpaceReference expectedLayoutRef = new SpaceReference("MyLayout", new WikiReference(
        context.getDatabase()));
    expect(pageLayoutCmdMock.getPageLayoutForCurrentDoc()).andReturn(expectedLayoutRef
        ).once();
    replayDefault();
    assertEquals(expectedLayoutRef, pageDepDocRefCmd.getCurrentLayoutRef());
    verifyDefault();
  }

  @Test
  public void testGetCurrentLayoutRef_injectLayout() {
    PageLayoutCommand pageLayoutCmdMock = createMockAndAddToDefault(
        PageLayoutCommand.class);
    pageDepDocRefCmd.pageLayoutCmd = pageLayoutCmdMock;
    SpaceReference expectedLayoutRef = new SpaceReference("MyLayout", new WikiReference(
        context.getDatabase()));
    pageDepDocRefCmd.setCurrentLayoutRef(expectedLayoutRef);
    replayDefault();
    assertEquals(expectedLayoutRef, pageDepDocRefCmd.getCurrentLayoutRef());
    verifyDefault();
  }

  @Test
  public void testGetCurrentDocumentSpaceName_ZeroSpaces() {
    DocumentReference currentDocRef = createMockAndAddToDefault(DocumentReference.class);
    List<SpaceReference> emptySpaceRefList = Collections.emptyList();
    expect(currentDocRef.getSpaceReferences()).andReturn(emptySpaceRefList);
    expect(defValueProviderMock.getDefaultValue(eq(EntityType.SPACE))).andReturn(
        "myDefaultSpace").once();
    replayDefault();
    assertEquals("myDefaultSpace", pageDepDocRefCmd.getCurrentDocumentSpaceRef(
        currentDocRef).getName());
    verifyDefault();
  }

  @Test
  public void testGetCurrentDocumentSpaceName_greaterZeroSpaces() {
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "Content", "myDocument");
    replayDefault();
    assertEquals("Content", pageDepDocRefCmd.getCurrentDocumentSpaceRef(currentDocRef
        ).getName());
    verifyDefault();
  }

  @Test
  public void testGetDepCellSpace_cellDocWithoutObject() throws Exception {
    replayDefault();
    assertEquals("", pageDepDocRefCmd.getDepCellSpace(cellDocRef));
    verifyDefault();
  }

  @Test
  public void testGetDepCellSpace_cellDocWithEmptyObject() throws Exception {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef());
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(),
        Arrays.asList(cellConfig));
    replayDefault();
    assertEquals("", pageDepDocRefCmd.getDepCellSpace(cellDocRef));
    verifyDefault();
  }

  @Test
  public void testGetDepCellSpace_cellDocWithNonEmptyObject() throws Exception {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef());
    cellConfig.setStringValue(PageDependentDocumentReferenceCommand.PROPNAME_SPACE_NAME,
      "myDepSpace");
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(),
        Arrays.asList(cellConfig));
    replayDefault();
    assertEquals("myDepSpace", pageDepDocRefCmd.getDepCellSpace(cellDocRef));
    verifyDefault();
  }

  @Test
  public void testIsCurrentDocument_cellDocWithoutObject() throws Exception {
    replayDefault();
    assertTrue(pageDepDocRefCmd.isCurrentDocument(cellDocRef));
    verifyDefault();
  }

  @Test
  public void testIsCurrentDocument_cellDocWithEmptyObject() throws Exception {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef());
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(),
        Arrays.asList(cellConfig));
    replayDefault();
    assertTrue(pageDepDocRefCmd.isCurrentDocument(cellDocRef));
    verifyDefault();
  }

  @Test
  public void testIsCurrentDocument_cellDocWithNonEmptyObject() throws Exception {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef());
    cellConfig.setStringValue(PageDependentDocumentReferenceCommand.PROPNAME_SPACE_NAME,
      "myDepSpace");
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(),
        Arrays.asList(cellConfig));
    replayDefault();
    assertFalse(pageDepDocRefCmd.isCurrentDocument(cellDocRef));
    verifyDefault();
  }

  @Test
  public void testIsCurrentDocument_Exception() {
    try {
      reset(xwiki);
      expect(xwiki.getDocument(eq(cellDocRef), same(context))).andThrow(
          new XWikiException()).atLeastOnce();
      replayDefault();
      assertTrue("expecting fallback to currentDoc.", pageDepDocRefCmd.isCurrentDocument(
          cellDocRef));
      verifyDefault();
    } catch (XWikiException exp) {
      fail("Expecting isCurrentDocument to catch XWikiException and returning True.");
    }
  }

  @Test
  public void testGetDependentDocumentSpace_cellDocWithoutObject_Content() {
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "Content", "myDocument");
    replayDefault();
    assertEquals("Content", pageDepDocRefCmd.getDependentDocumentSpaceRef(currentDocRef,
        cellDocRef).getName());
    verifyDefault();
  }

  @Test
  public void testGetDependentDocumentSpace_cellDocWithoutObject_anySpace() {
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myDocument");
    replayDefault();
    assertEquals("mySpace", pageDepDocRefCmd.getDependentDocumentSpaceRef(currentDocRef,
        cellDocRef).getName());
    verifyDefault();
  }

  @Test
  public void testGetDependentDocumentSpace_cellDocWithEmptyObject_Content() {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef());
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(),
        Arrays.asList(cellConfig));
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "Content", "myDocument");
    replayDefault();
    assertEquals("Content", pageDepDocRefCmd.getDependentDocumentSpaceRef(currentDocRef,
        cellDocRef).getName());
    verifyDefault();
  }

  @Test
  public void testGetDependentDocumentSpace_cellDocWithEmptyObject_anySpace() {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef());
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(),
        Arrays.asList(cellConfig));
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "MySpace", "myDocument");
    replayDefault();
    assertEquals("MySpace", pageDepDocRefCmd.getDependentDocumentSpaceRef(currentDocRef,
        cellDocRef).getName());
    verifyDefault();
  }

  @Test
  public void testGetDependentDocumentSpace_cellDocWithNonEmptyObject_Content() {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setStringValue(PageDependentDocumentReferenceCommand.PROPNAME_SPACE_NAME,
        "myDepSpace");
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef());
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(),
        Arrays.asList(cellConfig));
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "Content", "myDocument");
    replayDefault();
    assertEquals("Content_myDepSpace", pageDepDocRefCmd.getDependentDocumentSpaceRef(
        currentDocRef, cellDocRef).getName());
    verifyDefault();
  }

  @Test
  public void testGetDependentDocumentSpace_cellDocWithNonEmptyObject_anySpace() {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setStringValue(PageDependentDocumentReferenceCommand.PROPNAME_SPACE_NAME,
        "myDepSpace");
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef());
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(),
        Arrays.asList(cellConfig));
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myDocument");
    replayDefault();
    assertEquals("mySpace_myDepSpace", pageDepDocRefCmd.getDependentDocumentSpaceRef(
        currentDocRef, cellDocRef).getName());
    verifyDefault();
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
      cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef());
      cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(),
          Arrays.asList(cellConfig));
      DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
          "mySpace", "myDocument");
      replayDefault();
      assertEquals("mySpace", pageDepDocRefCmd.getDependentDocumentSpaceRef(currentDocRef,
          cellDocRef).getName());
      verifyDefault();
    } catch (XWikiException exp) {
      fail("expecting to catch exception and fallback to current space");
    }
  }

  @Test
  public void testGetDocumentReference_isCurrent() {
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myDocument");
    replayDefault();
    assertEquals(expectedDocRef, pageDepDocRefCmd.getDocumentReference(expectedDocRef,
        cellDocRef));
    verifyDefault();
  }

  @Test
  public void testGetDocumentReference_isNotCurrent() {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setStringValue(PageDependentDocumentReferenceCommand.PROPNAME_SPACE_NAME,
        "myDepSpace");
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef());
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(),
        Arrays.asList(cellConfig));
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myDocument");
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_myDepSpace", "myDocument");
    replayDefault();
    assertEquals(expectedDocRef, pageDepDocRefCmd.getDocumentReference(currentDocRef,
        cellDocRef));
    verifyDefault();
  }

  @Test
  public void testGetDocumentReference_isCurrent_inheritable() {
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myDocument");
    replayDefault();
    assertEquals(expectedDocRef, pageDepDocRefCmd.getDocumentReference(expectedDocRef,
        cellDocRef, false));
    verifyDefault();
  }

  @Test
  public void testGetDocumentReference_isNotCurrent_inheritable() {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setStringValue(PageDependentDocumentReferenceCommand.PROPNAME_SPACE_NAME,
        "myDepSpace");
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef());
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(),
        Arrays.asList(cellConfig));
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myDocument");
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_myDepSpace", "myDocument");
    replayDefault();
    assertEquals(expectedDocRef, pageDepDocRefCmd.getDocumentReference(currentDocRef,
        cellDocRef, false));
    verifyDefault();
  }

  @Test
  public void testGetDocument_isCurrent() throws XWikiException {
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myDocument");
    expect(document.getDocumentReference()).andReturn(expectedDocRef).atLeastOnce();
    replayDefault();
    assertSame(document, pageDepDocRefCmd.getDocument(document, cellDocRef));
    verifyDefault();
  }

  @Test
  public void testGetDocument_isNotCurrent() throws XWikiException {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setStringValue(PageDependentDocumentReferenceCommand.PROPNAME_SPACE_NAME,
        "myDepSpace");
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef());
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(),
        Arrays.asList(cellConfig));
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myDocument");
    expect(document.getDocumentReference()).andReturn(currentDocRef).atLeastOnce();
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_myDepSpace", "myDocument");
    XWikiDocument expectedDoc = new XWikiDocument(expectedDocRef);
    expect(xwiki.getDocument(eq(expectedDocRef), same(context))).andReturn(expectedDoc
        ).once();
    replayDefault();
    assertEquals(expectedDoc, pageDepDocRefCmd.getDocument(document, cellDocRef));
    verifyDefault();
  }

  @Test
  public void testGetTranslatedDocument_isCurrent() throws XWikiException {
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myDocument");
    expect(document.getDocumentReference()).andReturn(expectedDocRef).atLeastOnce();
    replayDefault();
    assertSame(document, pageDepDocRefCmd.getTranslatedDocument(document, cellDocRef));
    verifyDefault();
  }

  @Test
  public void testGetTranslatedDocument_isNotCurrent() throws XWikiException {
    String contextLang = "fr";
    context.setLanguage(contextLang);
    BaseObject cellConfig = new BaseObject();
    cellConfig.setStringValue(PageDependentDocumentReferenceCommand.PROPNAME_SPACE_NAME,
        "myDepSpace");
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef());
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(),
        Arrays.asList(cellConfig));
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myDocument");
    expect(document.getDocumentReference()).andReturn(currentDocRef).atLeastOnce();
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_myDepSpace", "myDocument");
    XWikiDocument expectedDoc = createMockAndAddToDefault(XWikiDocument.class);
    expect(xwiki.getDocument(eq(expectedDocRef), same(context))).andReturn(expectedDoc
        ).once();
    XWikiDocument expectedTransDoc = new XWikiDocument(expectedDocRef);
    expect(expectedDoc.getTranslatedDocument(eq(contextLang), same(context))).andReturn(
        expectedTransDoc).once();
    replayDefault();
    assertEquals(expectedTransDoc, pageDepDocRefCmd.getTranslatedDocument(document,
        cellDocRef));
    verifyDefault();
  }

}
