package com.celements.cells.cmd;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

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

import com.celements.cells.classes.PageDepCellConfigClass;
import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.navigation.service.ITreeNodeService;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class PageDependentDocumentReferenceCommandTest extends AbstractComponentTest {

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
  public void prepare() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    registerComponentMock(IModelAccessFacade.class);
    document = createDefaultMock(XWikiDocument.class);
    cellDocRef = new DocumentReference(context.getDatabase(), "MyLayout", "Cell2");
    cellDoc = new XWikiDocument(cellDocRef);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(eq(cellDocRef)))
        .andReturn(cellDoc).anyTimes();
    pageDepDocRefCmd = new PageDependentDocumentReferenceCommand();
    defaultValueProviderDesc = getComponentManager().getComponentDescriptor(
        EntityReferenceValueProvider.class, "default");
    savedDefaultValueProviderService = Utils.getComponent(EntityReferenceValueProvider.class);
    getComponentManager().unregisterComponent(ITreeNodeService.class, "default");
    defValueProviderMock = createDefaultMock(EntityReferenceValueProvider.class);
    getComponentManager().registerComponent(defaultValueProviderDesc, defValueProviderMock);
  }

  @After
  public void shutdown_EmptyCheckCommandTest() throws Exception {
    getComponentManager().unregisterComponent(EntityReferenceValueProvider.class, "default");
    getComponentManager().registerComponent(defaultValueProviderDesc,
        savedDefaultValueProviderService);
  }

  @Test
  public void test_getPageLayoutCmd() {
    pageDepDocRefCmd.pageLayoutCmd = null;
    replayDefault();
    assertNotNull(pageDepDocRefCmd.getPageLayoutCmd());
    verifyDefault();
  }

  @Test
  public void test_inject_pageLayoutCmdMock() {
    PageLayoutCommand pageLayoutCmdMock = createDefaultMock(PageLayoutCommand.class);
    pageDepDocRefCmd.pageLayoutCmd = pageLayoutCmdMock;
    replayDefault();
    assertSame(pageLayoutCmdMock, pageDepDocRefCmd.getPageLayoutCmd());
    verifyDefault();
  }

  @Test
  public void test_getPageDepCellConfigClassDocRef() {
    replayDefault();
    assertEquals(new DocumentReference(context.getDatabase(),
        PageDependentDocumentReferenceCommand.PAGE_DEP_CELL_CONFIG_CLASS_SPACE,
        PageDependentDocumentReferenceCommand.PAGE_DEP_CELL_CONFIG_CLASS_DOC),
        pageDepDocRefCmd.getPageDepCellConfigClassDocRef());
    verifyDefault();
  }

  @Test
  public void test_getCurrentLayoutRef() {
    PageLayoutCommand pageLayoutCmdMock = createDefaultMock(PageLayoutCommand.class);
    pageDepDocRefCmd.pageLayoutCmd = pageLayoutCmdMock;
    SpaceReference expectedLayoutRef = new SpaceReference("MyLayout", new WikiReference(
        context.getDatabase()));
    expect(pageLayoutCmdMock.getPageLayoutForCurrentDoc()).andReturn(expectedLayoutRef).once();
    replayDefault();
    assertEquals(expectedLayoutRef, pageDepDocRefCmd.getCurrentLayoutRef());
    verifyDefault();
  }

  @Test
  public void test_getCurrentLayoutRef_injectLayout() {
    PageLayoutCommand pageLayoutCmdMock = createDefaultMock(PageLayoutCommand.class);
    pageDepDocRefCmd.pageLayoutCmd = pageLayoutCmdMock;
    SpaceReference expectedLayoutRef = new SpaceReference("MyLayout", new WikiReference(
        context.getDatabase()));
    pageDepDocRefCmd.setCurrentLayoutRef(expectedLayoutRef);
    replayDefault();
    assertEquals(expectedLayoutRef, pageDepDocRefCmd.getCurrentLayoutRef());
    verifyDefault();
  }

  @Test
  public void test_getCurrentDocumentSpaceName_ZeroSpaces() {
    DocumentReference currentDocRef = createDefaultMock(DocumentReference.class);
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
  public void test_getCurrentDocumentSpaceName_greaterZeroSpaces() {
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(), "Content",
        "myDocument");
    replayDefault();
    assertEquals("Content", pageDepDocRefCmd.getCurrentDocumentSpaceRef(currentDocRef).getName());
    verifyDefault();
  }

  @Test
  public void test_getDepCellSpace_cellDocWithoutObject() throws Exception {
    replayDefault();
    assertEquals("", pageDepDocRefCmd.getDepCellSpace(cellDocRef));
    verifyDefault();
  }

  @Test
  public void test_getDepCellSpace_cellDocWithEmptyObject() throws Exception {
    setDependentDocSpace(null, null);
    replayDefault();
    assertEquals("", pageDepDocRefCmd.getDepCellSpace(cellDocRef));
    verifyDefault();
  }

  @Test
  public void test_getDepCellSpace_cellDocWithNonEmptyObject() throws Exception {
    setDependentDocSpace("myDepSpace", null);
    replayDefault();
    assertEquals("myDepSpace", pageDepDocRefCmd.getDepCellSpace(cellDocRef));
    verifyDefault();
  }

  @Test
  public void test_isCurrentDocument_cellDocWithoutObject() throws Exception {
    replayDefault();
    assertTrue(pageDepDocRefCmd.isCurrentDocument(cellDocRef));
    verifyDefault();
  }

  @Test
  public void test_isCurrentDocument_cellDocWithEmptyObject() throws Exception {
    setDependentDocSpace(null, null);
    replayDefault();
    assertTrue(pageDepDocRefCmd.isCurrentDocument(cellDocRef));
    verifyDefault();
  }

  @Test
  public void test_isCurrentDocument_cellDocWithNonEmptyObject() throws Exception {
    setDependentDocSpace("myDepSpace", null);
    replayDefault();
    assertFalse(pageDepDocRefCmd.isCurrentDocument(cellDocRef));
    verifyDefault();
  }

  @Test
  public void test_getDependentDocumentSpace_cellDocWithoutObject_Content() {
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(), "Content",
        "myDocument");
    replayDefault();
    assertEquals("Content", pageDepDocRefCmd.getDependentDocumentSpaceRef(currentDocRef,
        cellDocRef).getName());
    verifyDefault();
  }

  @Test
  public void test_getDependentDocumentSpace_cellDocWithoutObject_anySpace() {
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myDocument");
    replayDefault();
    assertEquals("mySpace", pageDepDocRefCmd.getDependentDocumentSpaceRef(currentDocRef,
        cellDocRef).getName());
    verifyDefault();
  }

  @Test
  public void test_getDependentDocumentSpace_cellDocWithEmptyObject_Content() {
    setDependentDocSpace(null, null);
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(), "Content",
        "myDocument");
    replayDefault();
    assertEquals("Content", pageDepDocRefCmd.getDependentDocumentSpaceRef(currentDocRef,
        cellDocRef).getName());
    verifyDefault();
  }

  @Test
  public void test_getDependentDocumentSpace_cellDocWithEmptyObject_anySpace() {
    setDependentDocSpace(null, null);
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(), "MySpace",
        "myDocument");
    replayDefault();
    assertEquals("MySpace", pageDepDocRefCmd.getDependentDocumentSpaceRef(currentDocRef,
        cellDocRef).getName());
    verifyDefault();
  }

  @Test
  public void test_getDependentDocumentSpace_cellDocWithNonEmptyObject_Content() {
    setDependentDocSpace("myDepSpace", null);
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(), "Content",
        "myDocument");
    replayDefault();
    assertEquals("Content_myDepSpace", pageDepDocRefCmd.getDependentDocumentSpaceRef(currentDocRef,
        cellDocRef).getName());
    verifyDefault();
  }

  @Test
  public void test_getDependentDocumentSpace_cellDocWithNonEmptyObject_anySpace() {
    setDependentDocSpace("myDepSpace", null);
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myDocument");
    replayDefault();
    assertEquals("mySpace_myDepSpace", pageDepDocRefCmd.getDependentDocumentSpaceRef(currentDocRef,
        cellDocRef).getName());
    verifyDefault();
  }

  @Test
  public void test_getDocumentReference_isCurrent() {
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myDocument");
    replayDefault();
    assertEquals(expectedDocRef, pageDepDocRefCmd.getDocumentReference(expectedDocRef, cellDocRef));
    verifyDefault();
  }

  @Test
  public void test_getDocumentReference_isNotCurrent() {
    setDependentDocSpace("myDepSpace", null);
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myDocument");
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_myDepSpace", "myDocument");
    replayDefault();
    assertEquals(expectedDocRef, pageDepDocRefCmd.getDocumentReference(currentDocRef, cellDocRef));
    verifyDefault();
  }

  @Test
  public void test_getDocumentReference_isCurrent_inheritable() {
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myDocument");
    replayDefault();
    assertEquals(expectedDocRef, pageDepDocRefCmd.getDocumentReference(expectedDocRef, cellDocRef,
        false));
    verifyDefault();
  }

  @Test
  public void test_getDocumentReference_isNotCurrent_inheritable() {
    setDependentDocSpace("myDepSpace", null);
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myDocument");
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_myDepSpace", "myDocument");
    replayDefault();
    assertEquals(expectedDocRef, pageDepDocRefCmd.getDocumentReference(currentDocRef, cellDocRef,
        false));
    verifyDefault();
  }

  @Test
  public void test_getDocument_isCurrent() throws XWikiException {
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myDocument");
    expect(document.getDocumentReference()).andReturn(expectedDocRef).atLeastOnce();
    replayDefault();
    assertSame(document, pageDepDocRefCmd.getDocument(document, cellDocRef));
    verifyDefault();
  }

  @Test
  public void test_getDocument_isNotCurrent() throws XWikiException {
    setDependentDocSpace("myDepSpace", null);
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myDocument");
    expect(document.getDocumentReference()).andReturn(currentDocRef).atLeastOnce();
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_myDepSpace", "myDocument");
    XWikiDocument expectedDoc = new XWikiDocument(expectedDocRef);
    expect(xwiki.getDocument(eq(expectedDocRef), same(context))).andReturn(expectedDoc).once();
    replayDefault();
    assertEquals(expectedDoc, pageDepDocRefCmd.getDocument(document, cellDocRef));
    verifyDefault();
  }

  @Test
  public void test_getTranslatedDocument_isCurrent() throws XWikiException {
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myDocument");
    expect(document.getDocumentReference()).andReturn(expectedDocRef).atLeastOnce();
    replayDefault();
    assertSame(document, pageDepDocRefCmd.getTranslatedDocument(document, cellDocRef));
    verifyDefault();
  }

  @Test
  public void test_getTranslatedDocument_isNotCurrent() throws XWikiException {
    String contextLang = "fr";
    context.setLanguage(contextLang);
    setDependentDocSpace("myDepSpace", null);
    DocumentReference currentDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myDocument");
    expect(document.getDocumentReference()).andReturn(currentDocRef).atLeastOnce();
    DocumentReference expectedDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_myDepSpace", "myDocument");
    XWikiDocument expectedDoc = createDefaultMock(XWikiDocument.class);
    expect(xwiki.getDocument(eq(expectedDocRef), same(context))).andReturn(expectedDoc).once();
    XWikiDocument expectedTransDoc = new XWikiDocument(expectedDocRef);
    expect(expectedDoc.getTranslatedDocument(eq(contextLang), same(context))).andReturn(
        expectedTransDoc).once();
    replayDefault();
    assertEquals(expectedTransDoc, pageDepDocRefCmd.getTranslatedDocument(document, cellDocRef));
    verifyDefault();
  }

  private void setDependentDocSpace(String depDocSpace, Integer isInheritable) {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setXClassReference(PageDepCellConfigClass.CLASS_REF);
    cellConfig.setStringValue(PageDepCellConfigClass.FIELD_SPACE_NAME.getName(), depDocSpace);
    if (isInheritable != null) {
      cellConfig.setIntValue(PageDepCellConfigClass.FIELD_IS_ACTIVE.getName(), isInheritable);
    }
    cellConfig.setDocumentReference(PageDepCellConfigClass.CLASS_REF.getDocRef());
    cellDoc.addXObject(cellConfig);
  }
}
