package com.celements.cells.cmd;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.navigation.service.ITreeNodeService;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class PageDependentDocumentReferenceCommandOverlayTest extends AbstractComponentTest {

  private XWikiContext context;
  private PageDependentDocumentReferenceCommand pageDepDocRefCmd;
  private DocumentReference cellDocRef;
  private XWikiDocument cellDoc;
  private IWebUtilsService webUtilsMock;
  private IWebUtilsService savedWebUtilsService;
  private ComponentDescriptor<IWebUtilsService> webUtilsServiceDesc;
  private EntityReferenceSerializer<String> refDefaultSerializerMock;
  private PageLayoutCommand pageLayoutCmdMock;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp_PageDependentDocumentReferenceCommandOverlayTest() throws Exception {
    context = getContext();
    registerComponentMock(IModelAccessFacade.class);
    cellDocRef = new DocumentReference(context.getDatabase(), "MyLayout", "Cell2");
    cellDoc = new XWikiDocument(cellDocRef);
    cellDoc.setNew(false);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(eq(cellDocRef)))
        .andReturn(cellDoc).anyTimes();
    pageDepDocRefCmd = new PageDependentDocumentReferenceCommand();
    webUtilsMock = createMockAndAddToDefault(IWebUtilsService.class);
    webUtilsServiceDesc = getComponentManager().getComponentDescriptor(IWebUtilsService.class,
        "default");
    savedWebUtilsService = Utils.getComponent(IWebUtilsService.class);
    getComponentManager().unregisterComponent(ITreeNodeService.class, "default");
    getComponentManager().registerComponent(webUtilsServiceDesc, webUtilsMock);
    refDefaultSerializerMock = createMockAndAddToDefault(EntityReferenceSerializer.class);
    expect(webUtilsMock.getRefDefaultSerializer()).andReturn(refDefaultSerializerMock).anyTimes();
    pageLayoutCmdMock = createMockAndAddToDefault(PageLayoutCommand.class);
    pageDepDocRefCmd.pageLayoutCmd = pageLayoutCmdMock;
  }

  @After
  public void shutdown_EmptyCheckCommandTest() throws Exception {
    getComponentManager().unregisterComponent(IWebUtilsService.class, "default");
    getComponentManager().registerComponent(webUtilsServiceDesc, savedWebUtilsService);
  }

  @Test
  public void testIsInheritable() throws Exception {
    replayDefault();
    assertFalse("default expected false", pageDepDocRefCmd.isInheritable(cellDocRef));
    verifyDefault();
  }

  @Test
  public void testIsInheritable_noValue() throws Exception {
    setDependentDocSpace("leftColumn", null);
    replayDefault();
    assertFalse("default expected false", pageDepDocRefCmd.isInheritable(cellDocRef));
    verifyDefault();
  }

  @Test
  public void testIsInheritable_zero() throws Exception {
    setDependentDocSpace("leftColumn", 0);
    replayDefault();
    assertFalse("default expected false", pageDepDocRefCmd.isInheritable(cellDocRef));
    verifyDefault();
  }

  @Test
  public void testIsInheritable_one() throws Exception {
    setDependentDocSpace("leftColumn", 1);
    replayDefault();
    assertTrue("default expected false", pageDepDocRefCmd.isInheritable(cellDocRef));
    verifyDefault();
  }

  @Test
  public void testGetDependentDocList() {
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "mySpace", "MyDoc");
    List<String> expDepDocList = Arrays.asList("leftColumn_mySpace.MyDoc",
        "leftColumn_mySpace.MyParentDoc");
    List<DocumentReference> docParentList = Arrays.asList(myDocRef, new DocumentReference(
        context.getDatabase(), "mySpace", "MyParentDoc"));
    expect(webUtilsMock.getDocumentParentsList(eq(myDocRef), eq(true))).andReturn(docParentList);
    replayDefault();
    List<String> depDocList = pageDepDocRefCmd.getDependentDocList(myDocRef, "leftColumn_mySpace");
    assertEquals(expDepDocList, depDocList);
    verifyDefault();
  }

  @Test
  public void testGetDependentDocumentReference() throws Exception {
    context.setLanguage("en");
    setDependentDocSpace("leftColumn", 1);
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "mySpace", "MyDoc");
    DocumentReference parentDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "MyParentDoc");
    List<DocumentReference> docParentList = Arrays.asList(myDocRef, parentDocRef);
    expect(webUtilsMock.getDocumentParentsList(eq(myDocRef), eq(true))).andReturn(docParentList);
    DocumentReference expDepDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_leftColumn", "MyParentDoc");
    String myDocFN = "mySpace_leftColumn.MyDoc";
    expect(webUtilsMock.resolveDocumentReference(eq(myDocFN))).andReturn(myDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(myDocRef))).andReturn(false).atLeastOnce();
    String leftParentFullName = "mySpace_leftColumn.MyParentDoc";
    expect(webUtilsMock.resolveDocumentReference(eq(leftParentFullName))).andReturn(
        expDepDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(expDepDocRef))).andReturn(true)
        .atLeastOnce();
    XWikiDocument leftParentDoc = createMockAndAddToDefault(XWikiDocument.class);
    expect(getMock(IModelAccessFacade.class).getDocument(eq(expDepDocRef)))
        .andReturn(leftParentDoc).atLeastOnce();
    expect(leftParentDoc.getContent()).andReturn("parent Content").atLeastOnce();
    expect(leftParentDoc.getDocumentReference()).andReturn(expDepDocRef).atLeastOnce();
    expect(leftParentDoc.getDefaultLanguage()).andReturn("en").anyTimes();
    replayDefault();
    DocumentReference depDocRef = pageDepDocRefCmd.getDependentDocumentReference(myDocRef,
        cellDocRef);
    assertEquals(expDepDocRef, depDocRef);
    verifyDefault();
  }

  @Test
  public void testGetDependentDocumentReference_defaultContent_noDefaults() throws Exception {
    setDependentDocSpace("leftColumn", 1);
    DocumentReference pdcWikiDefaultDocRef = new DocumentReference(context.getDatabase(),
        PageDependentDocumentReferenceCommand.PDC_WIKIDEFAULT_SPACE_NAME + "_leftColumn",
        PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME);
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "mySpace", "MyDoc");
    XWikiDocument myCurrDoc = new XWikiDocument(myDocRef);
    myCurrDoc.setNew(false);
    context.setDoc(myCurrDoc);
    DocumentReference parentDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "MyParentDoc");
    List<DocumentReference> docParentList = Arrays.asList(myDocRef, parentDocRef);
    expect(webUtilsMock.getDocumentParentsList(eq(myDocRef), eq(true))).andReturn(docParentList);
    DocumentReference expDepDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_leftColumn", PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME);
    String myDocFN = "mySpace_leftColumn.MyDoc";
    expect(webUtilsMock.resolveDocumentReference(eq(myDocFN))).andReturn(myDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(myDocRef))).andReturn(false).atLeastOnce();
    String leftParentFullName = "mySpace_leftColumn.MyParentDoc";
    DocumentReference leftParentDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_leftColumn", "MyParentDoc");
    expect(webUtilsMock.resolveDocumentReference(eq(leftParentFullName))).andReturn(
        leftParentDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(leftParentDocRef))).andReturn(false)
        .atLeastOnce();
    String mySpaceLeftColumnDefaultFN = context.getDatabase() + ":mySpace_leftColumn."
        + PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME;
    expect(refDefaultSerializerMock.serialize(eq(expDepDocRef))).andReturn(
        mySpaceLeftColumnDefaultFN);
    expect(webUtilsMock.resolveDocumentReference(eq(mySpaceLeftColumnDefaultFN))).andReturn(
        expDepDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(expDepDocRef))).andReturn(false)
        .atLeastOnce();
    String wikiLeftColumnDefaultFN = context.getDatabase() + ":"
        + PageDependentDocumentReferenceCommand.PDC_WIKIDEFAULT_SPACE_NAME + "_leftColumn" + "."
        + PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME;
    expect(refDefaultSerializerMock.serialize(eq(pdcWikiDefaultDocRef))).andReturn(
        wikiLeftColumnDefaultFN);
    expect(webUtilsMock.resolveDocumentReference(eq(wikiLeftColumnDefaultFN))).andReturn(
        pdcWikiDefaultDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(pdcWikiDefaultDocRef))).andReturn(false)
        .anyTimes();
    expect(pageLayoutCmdMock.getPageLayoutForCurrentDoc()).andReturn(null).atLeastOnce();
    replayDefault();
    DocumentReference depDocRef = pageDepDocRefCmd.getDependentDocumentReference(myDocRef,
        cellDocRef);
    assertEquals(expDepDocRef, depDocRef);
    verifyDefault();
  }

  @Test
  public void testGetDependentDocumentReference_defaultContent_space() throws Exception {
    context.setLanguage("en");
    setDependentDocSpace("leftColumn", 1);
    DocumentReference pdcWikiDefaultDocRef = new DocumentReference(context.getDatabase(),
        PageDependentDocumentReferenceCommand.PDC_WIKIDEFAULT_SPACE_NAME + "_leftColumn",
        PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME);
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "mySpace", "MyDoc");
    XWikiDocument myCurrDoc = new XWikiDocument(myDocRef);
    myCurrDoc.setNew(false);
    context.setDoc(myCurrDoc);
    DocumentReference parentDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "MyParentDoc");
    List<DocumentReference> docParentList = Arrays.asList(myDocRef, parentDocRef);
    expect(webUtilsMock.getDocumentParentsList(eq(myDocRef), eq(true))).andReturn(docParentList);
    DocumentReference expDepDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_leftColumn", PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME);
    String myDocFN = "mySpace_leftColumn.MyDoc";
    expect(webUtilsMock.resolveDocumentReference(eq(myDocFN))).andReturn(myDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(myDocRef))).andReturn(false).atLeastOnce();
    String leftParentFullName = "mySpace_leftColumn.MyParentDoc";
    DocumentReference leftParentDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_leftColumn", "MyParentDoc");
    expect(webUtilsMock.resolveDocumentReference(eq(leftParentFullName))).andReturn(
        leftParentDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(leftParentDocRef))).andReturn(false)
        .atLeastOnce();
    String mySpaceLeftColumnDefaultFN = context.getDatabase() + ":mySpace_leftColumn."
        + PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME;
    expect(refDefaultSerializerMock.serialize(eq(expDepDocRef))).andReturn(
        mySpaceLeftColumnDefaultFN);
    expect(webUtilsMock.resolveDocumentReference(eq(mySpaceLeftColumnDefaultFN))).andReturn(
        expDepDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(expDepDocRef))).andReturn(true)
        .atLeastOnce();
    XWikiDocument spaceDefaultDocument = new XWikiDocument(expDepDocRef);
    spaceDefaultDocument.setNew(false);
    spaceDefaultDocument.setDefaultLanguage("en");
    spaceDefaultDocument.setContent("no empty content");
    expect(getMock(IModelAccessFacade.class).getDocument(eq(expDepDocRef)))
        .andReturn(spaceDefaultDocument);
    String wikiLeftColumnDefaultFN = context.getDatabase() + ":"
        + PageDependentDocumentReferenceCommand.PDC_WIKIDEFAULT_SPACE_NAME + "_leftColumn" + "."
        + PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME;
    expect(refDefaultSerializerMock.serialize(eq(pdcWikiDefaultDocRef))).andReturn(
        wikiLeftColumnDefaultFN);
    expect(pageLayoutCmdMock.getPageLayoutForCurrentDoc()).andReturn(null).atLeastOnce();
    replayDefault();
    DocumentReference depDocRef = pageDepDocRefCmd.getDependentDocumentReference(myDocRef,
        cellDocRef);
    assertEquals(expDepDocRef, depDocRef);
    verifyDefault();
  }

  @Test
  public void testGetDependentDocumentReference_defaultContent_wiki() throws Exception {
    context.setLanguage("en");
    setDependentDocSpace("leftColumn", 1);
    DocumentReference pdcWikiDefaultDocRef = new DocumentReference(context.getDatabase(),
        PageDependentDocumentReferenceCommand.PDC_WIKIDEFAULT_SPACE_NAME + "_leftColumn",
        PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME);
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "mySpace", "MyDoc");
    XWikiDocument myCurrDoc = new XWikiDocument(myDocRef);
    myCurrDoc.setNew(false);
    context.setDoc(myCurrDoc);
    DocumentReference parentDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "MyParentDoc");
    List<DocumentReference> docParentList = Arrays.asList(myDocRef, parentDocRef);
    expect(webUtilsMock.getDocumentParentsList(eq(myDocRef), eq(true))).andReturn(docParentList);
    DocumentReference spaceDepDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_leftColumn", PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME);
    String myDocFN = "mySpace_leftColumn.MyDoc";
    expect(webUtilsMock.resolveDocumentReference(eq(myDocFN))).andReturn(myDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(myDocRef))).andReturn(false).atLeastOnce();
    String leftParentFullName = "mySpace_leftColumn.MyParentDoc";
    DocumentReference leftParentDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_leftColumn", "MyParentDoc");
    expect(webUtilsMock.resolveDocumentReference(eq(leftParentFullName))).andReturn(
        leftParentDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(leftParentDocRef))).andReturn(false)
        .atLeastOnce();
    String mySpaceLeftColumnDefaultFN = context.getDatabase() + ":mySpace_leftColumn."
        + PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME;
    expect(refDefaultSerializerMock.serialize(eq(spaceDepDocRef))).andReturn(
        mySpaceLeftColumnDefaultFN);
    expect(webUtilsMock.resolveDocumentReference(eq(mySpaceLeftColumnDefaultFN))).andReturn(
        spaceDepDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(spaceDepDocRef))).andReturn(false)
        .atLeastOnce();
    String wikiLeftColumnDefaultFN = context.getDatabase() + ":"
        + PageDependentDocumentReferenceCommand.PDC_WIKIDEFAULT_SPACE_NAME + "_leftColumn" + "."
        + PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME;
    expect(refDefaultSerializerMock.serialize(eq(pdcWikiDefaultDocRef))).andReturn(
        wikiLeftColumnDefaultFN);
    expect(webUtilsMock.resolveDocumentReference(eq(wikiLeftColumnDefaultFN))).andReturn(
        pdcWikiDefaultDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(pdcWikiDefaultDocRef))).andReturn(true)
        .atLeastOnce();
    XWikiDocument wikiDefaultDocument = new XWikiDocument(pdcWikiDefaultDocRef);
    wikiDefaultDocument.setNew(false);
    wikiDefaultDocument.setDefaultLanguage("en");
    wikiDefaultDocument.setContent("no empty content");
    expect(getMock(IModelAccessFacade.class).getDocument(eq(pdcWikiDefaultDocRef)))
        .andReturn(wikiDefaultDocument);
    expect(pageLayoutCmdMock.getPageLayoutForCurrentDoc()).andReturn(null).atLeastOnce();
    replayDefault();
    DocumentReference depDocRef = pageDepDocRefCmd.getDependentDocumentReference(myDocRef,
        cellDocRef);
    assertEquals(pdcWikiDefaultDocRef, depDocRef);
    verifyDefault();
  }

  @Test
  public void testGetDependentDocumentReference_defaultContent_layout() throws Exception {
    context.setLanguage("en");
    setDependentDocSpace("leftColumn", 1);
    DocumentReference pdcWikiDefaultDocRef = new DocumentReference(context.getDatabase(),
        PageDependentDocumentReferenceCommand.PDC_WIKIDEFAULT_SPACE_NAME + "_leftColumn",
        PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME);
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "mySpace", "MyDoc");
    XWikiDocument myCurrDoc = new XWikiDocument(myDocRef);
    myCurrDoc.setNew(false);
    context.setDoc(myCurrDoc);
    DocumentReference parentDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "MyParentDoc");
    List<DocumentReference> docParentList = Arrays.asList(myDocRef, parentDocRef);
    expect(webUtilsMock.getDocumentParentsList(eq(myDocRef), eq(true))).andReturn(docParentList);
    DocumentReference spaceDepDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_leftColumn", PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME);
    String myDocFN = "mySpace_leftColumn.MyDoc";
    expect(webUtilsMock.resolveDocumentReference(eq(myDocFN))).andReturn(myDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(myDocRef))).andReturn(false).atLeastOnce();
    String leftParentFullName = "mySpace_leftColumn.MyParentDoc";
    DocumentReference leftParentDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_leftColumn", "MyParentDoc");
    expect(webUtilsMock.resolveDocumentReference(eq(leftParentFullName))).andReturn(
        leftParentDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(leftParentDocRef))).andReturn(false)
        .atLeastOnce();
    String mySpaceLeftColumnDefaultFN = context.getDatabase() + ":mySpace_leftColumn."
        + PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME;
    expect(refDefaultSerializerMock.serialize(eq(spaceDepDocRef))).andReturn(
        mySpaceLeftColumnDefaultFN);
    expect(webUtilsMock.resolveDocumentReference(eq(mySpaceLeftColumnDefaultFN))).andReturn(
        spaceDepDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(spaceDepDocRef))).andReturn(false)
        .atLeastOnce();
    String wikiLeftColumnDefaultFN = context.getDatabase() + ":"
        + PageDependentDocumentReferenceCommand.PDC_WIKIDEFAULT_SPACE_NAME + "_leftColumn" + "."
        + PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME;
    expect(refDefaultSerializerMock.serialize(eq(pdcWikiDefaultDocRef))).andReturn(
        wikiLeftColumnDefaultFN);
    expect(webUtilsMock.resolveDocumentReference(eq(wikiLeftColumnDefaultFN))).andReturn(
        pdcWikiDefaultDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(pdcWikiDefaultDocRef))).andReturn(false)
        .atLeastOnce();
    String layoutSpaceName = "myLayout";
    DocumentReference expectedLayoutDefaultRef = new DocumentReference(context.getDatabase(),
        layoutSpaceName, "leftColumn-"
            + PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME);
    SpaceReference layoutSpace = new SpaceReference(layoutSpaceName, new WikiReference(
        context.getDatabase()));
    expect(pageLayoutCmdMock.getPageLayoutForCurrentDoc()).andReturn(layoutSpace).atLeastOnce();
    String layoutDefaultFN = context.getDatabase() + ":" + layoutSpaceName + "." + "leftColumn-"
        + PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME;
    expect(refDefaultSerializerMock.serialize(eq(expectedLayoutDefaultRef))).andReturn(
        layoutDefaultFN);
    expect(webUtilsMock.resolveDocumentReference(eq(layoutDefaultFN))).andReturn(
        expectedLayoutDefaultRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(expectedLayoutDefaultRef))).andReturn(true)
        .atLeastOnce();
    XWikiDocument layoutDefaultDocument = new XWikiDocument(expectedLayoutDefaultRef);
    layoutDefaultDocument.setNew(false);
    layoutDefaultDocument.setDefaultLanguage("en");
    layoutDefaultDocument.setContent("no empty content");
    expect(getMock(IModelAccessFacade.class).getDocument(eq(expectedLayoutDefaultRef)))
        .andReturn(layoutDefaultDocument);
    replayDefault();
    DocumentReference depDocRef = pageDepDocRefCmd.getDependentDocumentReference(myDocRef,
        cellDocRef);
    assertEquals(expectedLayoutDefaultRef, depDocRef);
    verifyDefault();
  }

  @Test
  public void testGetDependentDocumentReference_defaultContent_overwrite_layout() throws Exception {
    context.setLanguage("en");
    setDependentDocSpace("leftColumn", 1);
    DocumentReference pdcWikiDefaultDocRef = new DocumentReference(context.getDatabase(),
        PageDependentDocumentReferenceCommand.PDC_WIKIDEFAULT_SPACE_NAME + "_leftColumn",
        PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME);
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "mySpace", "MyDoc");
    XWikiDocument myCurrDoc = new XWikiDocument(myDocRef);
    myCurrDoc.setNew(false);
    context.setDoc(myCurrDoc);
    DocumentReference parentDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "MyParentDoc");
    List<DocumentReference> docParentList = Arrays.asList(myDocRef, parentDocRef);
    expect(webUtilsMock.getDocumentParentsList(eq(myDocRef), eq(true))).andReturn(docParentList);
    DocumentReference spaceDepDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_leftColumn", PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME);
    String myDocFN = "mySpace_leftColumn.MyDoc";
    expect(webUtilsMock.resolveDocumentReference(eq(myDocFN))).andReturn(myDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(myDocRef))).andReturn(false).atLeastOnce();
    String leftParentFullName = "mySpace_leftColumn.MyParentDoc";
    DocumentReference leftParentDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_leftColumn", "MyParentDoc");
    expect(webUtilsMock.resolveDocumentReference(eq(leftParentFullName))).andReturn(
        leftParentDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(leftParentDocRef))).andReturn(false)
        .atLeastOnce();
    String mySpaceLeftColumnDefaultFN = context.getDatabase() + ":mySpace_leftColumn."
        + PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME;
    expect(refDefaultSerializerMock.serialize(eq(spaceDepDocRef))).andReturn(
        mySpaceLeftColumnDefaultFN);
    expect(webUtilsMock.resolveDocumentReference(eq(mySpaceLeftColumnDefaultFN))).andReturn(
        spaceDepDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(spaceDepDocRef))).andReturn(false)
        .atLeastOnce();
    String wikiLeftColumnDefaultFN = context.getDatabase() + ":"
        + PageDependentDocumentReferenceCommand.PDC_WIKIDEFAULT_SPACE_NAME + "_leftColumn" + "."
        + PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME;
    expect(refDefaultSerializerMock.serialize(eq(pdcWikiDefaultDocRef))).andReturn(
        wikiLeftColumnDefaultFN);
    expect(webUtilsMock.resolveDocumentReference(eq(wikiLeftColumnDefaultFN))).andReturn(
        pdcWikiDefaultDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(pdcWikiDefaultDocRef))).andReturn(false)
        .atLeastOnce();
    String overwriteLayoutSpaceName = "myOverwriteLayout";
    DocumentReference expectedLayoutDefaultRef = new DocumentReference(context.getDatabase(),
        overwriteLayoutSpaceName, "leftColumn-"
            + PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME);
    SpaceReference overwriteLayoutRef = new SpaceReference(overwriteLayoutSpaceName,
        new WikiReference(context.getDatabase()));
    pageDepDocRefCmd.setCurrentLayoutRef(overwriteLayoutRef);
    String layoutDefaultFN = context.getDatabase() + ":" + overwriteLayoutSpaceName + "."
        + "leftColumn-" + PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME;
    expect(refDefaultSerializerMock.serialize(eq(expectedLayoutDefaultRef))).andReturn(
        layoutDefaultFN);
    expect(webUtilsMock.resolveDocumentReference(eq(layoutDefaultFN))).andReturn(
        expectedLayoutDefaultRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(expectedLayoutDefaultRef))).andReturn(true)
        .atLeastOnce();
    XWikiDocument layoutDefaultDocument = new XWikiDocument(expectedLayoutDefaultRef);
    layoutDefaultDocument.setNew(false);
    layoutDefaultDocument.setDefaultLanguage("en");
    layoutDefaultDocument.setContent("no empty content");
    expect(getMock(IModelAccessFacade.class).getDocument(eq(expectedLayoutDefaultRef)))
        .andReturn(layoutDefaultDocument);
    replayDefault();
    DocumentReference depDocRef = pageDepDocRefCmd.getDependentDocumentReference(myDocRef,
        cellDocRef);
    assertEquals(expectedLayoutDefaultRef, depDocRef);
    verifyDefault();
  }

  @Test
  public void testGetDependentDocumentReference_defaultContent_centrallayout() throws Exception {
    context.setLanguage("en");
    setDependentDocSpace("leftColumn", 1);
    DocumentReference pdcWikiDefaultDocRef = new DocumentReference(context.getDatabase(),
        PageDependentDocumentReferenceCommand.PDC_WIKIDEFAULT_SPACE_NAME + "_leftColumn",
        PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME);
    DocumentReference myDocRef = new DocumentReference(context.getDatabase(), "mySpace", "MyDoc");
    XWikiDocument myCurrDoc = new XWikiDocument(myDocRef);
    myCurrDoc.setNew(false);
    context.setDoc(myCurrDoc);
    DocumentReference parentDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "MyParentDoc");
    List<DocumentReference> docParentList = Arrays.asList(myDocRef, parentDocRef);
    expect(webUtilsMock.getDocumentParentsList(eq(myDocRef), eq(true))).andReturn(docParentList);
    DocumentReference spaceDepDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_leftColumn", PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME);
    String myDocFN = "mySpace_leftColumn.MyDoc";
    expect(webUtilsMock.resolveDocumentReference(eq(myDocFN))).andReturn(myDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(myDocRef))).andReturn(false).atLeastOnce();
    String leftParentFullName = "mySpace_leftColumn.MyParentDoc";
    DocumentReference leftParentDocRef = new DocumentReference(context.getDatabase(),
        "mySpace_leftColumn", "MyParentDoc");
    expect(webUtilsMock.resolveDocumentReference(eq(leftParentFullName))).andReturn(
        leftParentDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(leftParentDocRef))).andReturn(false)
        .atLeastOnce();
    String mySpaceLeftColumnDefaultFN = context.getDatabase() + ":mySpace_leftColumn."
        + PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME;
    expect(refDefaultSerializerMock.serialize(eq(spaceDepDocRef))).andReturn(
        mySpaceLeftColumnDefaultFN);
    expect(webUtilsMock.resolveDocumentReference(eq(mySpaceLeftColumnDefaultFN))).andReturn(
        spaceDepDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(spaceDepDocRef))).andReturn(false)
        .atLeastOnce();
    String wikiLeftColumnDefaultFN = context.getDatabase() + ":"
        + PageDependentDocumentReferenceCommand.PDC_WIKIDEFAULT_SPACE_NAME + "_leftColumn" + "."
        + PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME;
    expect(refDefaultSerializerMock.serialize(eq(pdcWikiDefaultDocRef))).andReturn(
        wikiLeftColumnDefaultFN);
    expect(webUtilsMock.resolveDocumentReference(eq(wikiLeftColumnDefaultFN))).andReturn(
        pdcWikiDefaultDocRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(pdcWikiDefaultDocRef))).andReturn(false)
        .atLeastOnce();
    String layoutSpaceName = "myLayout";
    String layoutDatabase = "layoutDb";
    DocumentReference expectedLayoutDefaultRef = new DocumentReference(layoutDatabase,
        layoutSpaceName, "leftColumn-"
            + PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME);
    SpaceReference layoutSpace = new SpaceReference(layoutSpaceName, new WikiReference(
        layoutDatabase));
    expect(pageLayoutCmdMock.getPageLayoutForCurrentDoc()).andReturn(layoutSpace).atLeastOnce();
    String layoutDefaultFN = layoutDatabase + ":" + layoutSpaceName + "." + "leftColumn-"
        + PageDependentDocumentReferenceCommand.PDC_DEFAULT_CONTENT_NAME;
    expect(refDefaultSerializerMock.serialize(eq(expectedLayoutDefaultRef))).andReturn(
        layoutDefaultFN);
    expect(webUtilsMock.resolveDocumentReference(eq(layoutDefaultFN))).andReturn(
        expectedLayoutDefaultRef).atLeastOnce();
    expect(getMock(IModelAccessFacade.class).exists(eq(expectedLayoutDefaultRef))).andReturn(true)
        .atLeastOnce();
    XWikiDocument layoutDefaultDocument = new XWikiDocument(expectedLayoutDefaultRef);
    layoutDefaultDocument.setNew(false);
    layoutDefaultDocument.setDefaultLanguage("en");
    layoutDefaultDocument.setContent("no empty content");
    expect(getMock(IModelAccessFacade.class).getDocument(eq(expectedLayoutDefaultRef)))
        .andReturn(layoutDefaultDocument);
    replayDefault();
    DocumentReference depDocRef = pageDepDocRefCmd.getDependentDocumentReference(myDocRef,
        cellDocRef);
    assertEquals(expectedLayoutDefaultRef, depDocRef);
    verifyDefault();
  }

  private void setDependentDocSpace(String depDocSpace, Integer isInheritable) {
    BaseObject cellConfig = new BaseObject();
    cellConfig.setStringValue(PageDependentDocumentReferenceCommand.PROPNAME_SPACE_NAME,
        depDocSpace);
    if (isInheritable != null) {
      cellConfig.setIntValue(PageDependentDocumentReferenceCommand.PROPNAME_IS_INHERITABLE,
          isInheritable);
    }
    cellConfig.setDocumentReference(pageDepDocRefCmd.getPageDepCellConfigClassDocRef());
    cellDoc.setXObjects(pageDepDocRefCmd.getPageDepCellConfigClassDocRef(), Arrays.asList(
        cellConfig));
  }

}
