/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.web.utils;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.syntax.Syntax;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.inheritor.InheritorFactory;
import com.celements.navigation.Navigation;
import com.celements.navigation.TreeNode;
import com.celements.navigation.service.ITreeNodeService;
import com.celements.navigation.service.TreeNodeService;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.xpn.xwiki.CoreConfiguration;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.render.XWikiRenderingEngine;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiEngineContext;
import com.xpn.xwiki.web.XWikiURLFactory;

public class WebUtilsTest extends AbstractBridgedComponentTestCase {

  private WebUtils celUtils;
  private XWikiURLFactory mockURLFactory;
  private XWikiStoreInterface mockXStore;

  @Before
  public void setUp_WebUtilsTest() throws Exception {
    celUtils = new WebUtils();
    mockURLFactory = createMockAndAddToDefault(XWikiURLFactory.class);
    mockXStore = createMockAndAddToDefault(XWikiStoreInterface.class);
    expect(getWikiMock().getStore()).andReturn(mockXStore).anyTimes();
    getContext().setURLFactory(mockURLFactory);
    expect(getWikiMock().isVirtualMode()).andReturn(true).anyTimes();
    expect(registerComponentMock(CoreConfiguration.class).getDefaultDocumentSyntax()).andReturn(
        Syntax.XWIKI_2_0).anyTimes();

  }

  @Test
  public void testCoveredQuotient() {
    assertEquals(1, celUtils.coveredQuotient(5, 5));
    assertEquals(3, celUtils.coveredQuotient(2, 5));
    assertEquals(1, celUtils.coveredQuotient(8, 4));
    assertEquals(1, celUtils.coveredQuotient(8, 2));
    assertEquals(1, celUtils.coveredQuotient(8, 7));
    assertEquals(0, celUtils.coveredQuotient(8, 0));
  }

  @Test
  public void testPrepareMaxCoverSet() {
    ArrayList<String> threeElems = new ArrayList<String>(Arrays.asList(new String[] { "1", "lj",
        "lizh" }));
    assertEquals(3, celUtils.prepareMaxCoverSet(0, threeElems).size());
    assertEquals(3, celUtils.prepareMaxCoverSet(1, threeElems).size());
    assertEquals(3, celUtils.prepareMaxCoverSet(2, threeElems).size());
    assertEquals(3, celUtils.prepareMaxCoverSet(3, threeElems).size());
    assertEquals(6, celUtils.prepareMaxCoverSet(4, threeElems).size());
    assertEquals(6, celUtils.prepareMaxCoverSet(5, threeElems).size());
    assertEquals(6, celUtils.prepareMaxCoverSet(6, threeElems).size());
    assertEquals(9, celUtils.prepareMaxCoverSet(7, threeElems).size());
  }

  @Test
  public void testGetRandomImages() throws XWikiException {
    String fullName = "Test.Gallery";
    XWikiDocument testGalleryDoc = new XWikiDocument();
    testGalleryDoc.setFullName(fullName);
    List<XWikiAttachment> attList = new ArrayList<XWikiAttachment>();
    TestImgAttachment imgAtt = new TestImgAttachment();
    imgAtt.setFilename("TestImg.jpg");
    attList.add(imgAtt);
    testGalleryDoc.setAttachmentList(attList);
    expect(getWikiMock().getDocument(eq(fullName), same(getContext()))).andReturn(testGalleryDoc);
    replayDefault();
    List<Attachment> randImgs = celUtils.getRandomImages(fullName, 5, getContext());
    assertNotNull(randImgs);
    assertEquals(5, randImgs.size());
    verifyDefault();
  }

  @Test
  public void testGetParentForLevel_mainLevel() {
    XWikiDocument activeDoc = new XWikiDocument("Space", "Name");
    activeDoc.setParent("");
    getContext().setDoc(activeDoc);
    assertNotNull(celUtils.getParentForLevel(1, getContext()));
    assertEquals("", celUtils.getParentForLevel(1, getContext()));
  }

  @Test
  public void testGetMaxConfiguredNavigationLevel_twoParents() throws Exception {
    DocumentReference navigationConfigClassReference = Navigation.getNavigationConfigClassReference(
        getContext().getDatabase());
    InheritorFactory inheritorFact = new InheritorFactory();
    PageLayoutCommand mockPageLayoutCmd = createMock(PageLayoutCommand.class);
    inheritorFact.injectPageLayoutCmd(mockPageLayoutCmd);
    celUtils.injectInheritorFactory(inheritorFact);
    ((TreeNodeService) Utils.getComponent(
        ITreeNodeService.class)).pageLayoutCmd = mockPageLayoutCmd;
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "MySpace",
        "MyDocument");
    XWikiDocument doc = new XWikiDocument(docRef);
    getContext().setDoc(doc);
    expect(mockPageLayoutCmd.getPageLayoutForDoc(eq(doc.getFullName()), same(
        getContext()))).andReturn(null).atLeastOnce();
    expect(mockPageLayoutCmd.getPageLayoutForCurrentDoc()).andReturn(null).atLeastOnce();
    DocumentReference webPrefDocRef = new DocumentReference(getContext().getDatabase(), "MySpace",
        "WebPreferences");
    XWikiDocument webPrefDoc = new XWikiDocument(webPrefDocRef);
    expect(getWikiMock().getDocument(eq(webPrefDocRef), eq(getContext()))).andReturn(
        webPrefDoc).atLeastOnce();
    Vector<BaseObject> navObjects = new Vector<BaseObject>();
    navObjects.add(createNavObj(5, webPrefDoc));
    navObjects.add(createNavObj(4, webPrefDoc));
    navObjects.add(createNavObj(8, webPrefDoc));
    navObjects.add(createNavObj(3, webPrefDoc));
    webPrefDoc.setXObjects(navigationConfigClassReference, navObjects);
    expect(getWikiMock().getSpacePreference(eq("skin"), same(getContext()))).andReturn(
        "Skins.MySkin").atLeastOnce();
    replayDefault(mockPageLayoutCmd);
    int maxLevel = celUtils.getMaxConfiguredNavigationLevel(getContext());
    verifyDefault(mockPageLayoutCmd);
    assertEquals("Max to Level in navConfigs is 8.", 8, maxLevel);
  }

  @Test
  public void testGetMaxConfiguredNavigationLevel_deletedObject_NPE() throws Exception {
    DocumentReference navigationConfigClassReference = Navigation.getNavigationConfigClassReference(
        getContext().getDatabase());
    InheritorFactory inheritorFact = new InheritorFactory();
    PageLayoutCommand mockPageLayoutCmd = createMock(PageLayoutCommand.class);
    inheritorFact.injectPageLayoutCmd(mockPageLayoutCmd);
    celUtils.injectInheritorFactory(inheritorFact);
    ((TreeNodeService) Utils.getComponent(
        ITreeNodeService.class)).pageLayoutCmd = mockPageLayoutCmd;
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "MySpace",
        "MyDocument");
    XWikiDocument doc = new XWikiDocument(docRef);
    getContext().setDoc(doc);
    expect(mockPageLayoutCmd.getPageLayoutForDoc(eq(doc.getFullName()), same(
        getContext()))).andReturn(null).atLeastOnce();
    expect(mockPageLayoutCmd.getPageLayoutForCurrentDoc()).andReturn(null).atLeastOnce();
    DocumentReference webPrefDocRef = new DocumentReference(getContext().getDatabase(), "MySpace",
        "WebPreferences");
    XWikiDocument webPrefDoc = new XWikiDocument(webPrefDocRef);
    expect(getWikiMock().getDocument(eq(webPrefDocRef), eq(getContext()))).andReturn(
        webPrefDoc).atLeastOnce();
    webPrefDoc.setXObject(0, createNavObj(5, webPrefDoc));
    // skipping 1 --> webPrefDoc.setXObject(1, null); // deleting an object can lead to
    // a null pointer in the object list
    webPrefDoc.setXObject(2, createNavObj(8, webPrefDoc));
    webPrefDoc.setXObject(3, createNavObj(3, webPrefDoc));
    webPrefDoc.setXObject(4, createNavObj(8, webPrefDoc));
    expect(getWikiMock().getSpacePreference(eq("skin"), same(getContext()))).andReturn(
        "Skins.MySkin").atLeastOnce();
    replayDefault(mockPageLayoutCmd);
    int maxLevel = celUtils.getMaxConfiguredNavigationLevel(getContext());
    verifyDefault(mockPageLayoutCmd);
    assertEquals("Max to Level in navConfigs is 8.", 8, maxLevel);
  }

  @Test
  public void testGetMaxConfiguredNavigationLevel_noObjectFound_NPE() throws Exception {
    InheritorFactory inheritorFact = new InheritorFactory();
    PageLayoutCommand mockPageLayoutCmd = createMock(PageLayoutCommand.class);
    inheritorFact.injectPageLayoutCmd(mockPageLayoutCmd);
    celUtils.injectInheritorFactory(inheritorFact);
    ((TreeNodeService) Utils.getComponent(
        ITreeNodeService.class)).pageLayoutCmd = mockPageLayoutCmd;
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "MySpace",
        "MyDocument");
    XWikiDocument doc = new XWikiDocument(docRef);
    getContext().setDoc(doc);
    expect(mockPageLayoutCmd.getPageLayoutForDoc(eq(doc.getFullName()), same(
        getContext()))).andReturn(null).atLeastOnce();
    expect(mockPageLayoutCmd.getPageLayoutForCurrentDoc()).andReturn(null).atLeastOnce();
    DocumentReference webPrefDocRef = new DocumentReference(getContext().getDatabase(), "MySpace",
        "WebPreferences");
    XWikiDocument webPrefDoc = new XWikiDocument(webPrefDocRef);
    expect(getWikiMock().getDocument(eq(webPrefDocRef), eq(getContext()))).andReturn(
        webPrefDoc).atLeastOnce();
    DocumentReference xwikiPrefDocRef = new DocumentReference(getContext().getDatabase(), "XWiki",
        "XWikiPreferences");
    XWikiDocument xwikiPrefDoc = new XWikiDocument(xwikiPrefDocRef);
    expect(getWikiMock().getDocument(eq(xwikiPrefDocRef), eq(getContext()))).andReturn(
        xwikiPrefDoc).atLeastOnce();
    DocumentReference skinDocRef = new DocumentReference(getContext().getDatabase(), "Skins",
        "MySkin");
    XWikiDocument skinDoc = new XWikiDocument(skinDocRef);
    expect(getWikiMock().getDocument(eq(skinDocRef), eq(getContext()))).andReturn(
        skinDoc).atLeastOnce();
    expect(getWikiMock().getSpacePreference(eq("skin"), same(getContext()))).andReturn(
        "Skins.MySkin").atLeastOnce();
    replayDefault(mockPageLayoutCmd);
    int maxLevel = celUtils.getMaxConfiguredNavigationLevel(getContext());
    verifyDefault(mockPageLayoutCmd);
    assertEquals("Expecting default max level.", Navigation.DEFAULT_MAX_LEVEL, maxLevel);
  }

  @Test
  public void testGetMaxConfiguredNavigationLevel_threeParents() throws Exception {
    DocumentReference navigationConfigClassReference = Navigation.getNavigationConfigClassReference(
        getContext().getDatabase());
    InheritorFactory inheritorFact = new InheritorFactory();
    PageLayoutCommand mockPageLayoutCmd = createMock(PageLayoutCommand.class);
    inheritorFact.injectPageLayoutCmd(mockPageLayoutCmd);
    celUtils.injectInheritorFactory(inheritorFact);
    ((TreeNodeService) Utils.getComponent(
        ITreeNodeService.class)).pageLayoutCmd = mockPageLayoutCmd;
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "MySpace",
        "MyDocument");
    XWikiDocument doc = new XWikiDocument(docRef);
    getContext().setDoc(doc);
    expect(mockPageLayoutCmd.getPageLayoutForDoc(eq(doc.getFullName()), same(
        getContext()))).andReturn(null).atLeastOnce();
    expect(mockPageLayoutCmd.getPageLayoutForCurrentDoc()).andReturn(null).atLeastOnce();
    DocumentReference webPrefDocRef = new DocumentReference(getContext().getDatabase(), "MySpace",
        "WebPreferences");
    XWikiDocument webPrefDoc = new XWikiDocument(webPrefDocRef);
    expect(getWikiMock().getDocument(eq(webPrefDocRef), eq(getContext()))).andReturn(
        webPrefDoc).atLeastOnce();
    expect(getWikiMock().getSpacePreference(eq("skin"), same(getContext()))).andReturn(
        "Skins.MySkin").atLeastOnce();
    Vector<BaseObject> navObjects = new Vector<BaseObject>();
    navObjects.add(createNavObj(5, webPrefDoc));
    navObjects.add(createNavObj(4, webPrefDoc));
    navObjects.add(createNavObj(3, webPrefDoc));
    webPrefDoc.setXObjects(navigationConfigClassReference, navObjects);
    replayDefault(mockPageLayoutCmd);
    int maxLevel = celUtils.getMaxConfiguredNavigationLevel(getContext());
    verifyDefault(mockPageLayoutCmd);
    assertEquals("Parents are a.b, b.c and c.d therefor maxlevel must be 5.", 5, maxLevel);
  }

  @Test
  public void testGetMajorVersion_nullDoc() {
    assertEquals("1", celUtils.getMajorVersion(null));
  }

  @Test
  public void testGetMajorVersion_noVersionSet() {
    XWikiDocument doc = new XWikiDocument();
    assertEquals("1", celUtils.getMajorVersion(doc));
  }

  @Test
  public void testGetMajorVersion() {
    XWikiDocument doc = new XWikiDocument();
    doc.setVersion("28.82");
    assertEquals("28", celUtils.getMajorVersion(doc));
  }

  @Test
  public void testPrevMenuItem() throws XWikiException {
    String mItemFullName = "mySpace.myMenuItemDoc";
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName(mItemFullName);
    getContext().setDoc(doc);
    BaseObject menuItem1 = new BaseObject();
    String prevFullName = "mySpace.Doc1";
    menuItem1.setName(prevFullName);
    menuItem1.setClassName("Celements2.MenuItem");
    XWikiDocument prevDoc = new XWikiDocument();
    prevDoc.setFullName(prevFullName);
    prevDoc.setObject("Celements2.MenuItem", 0, menuItem1);
    BaseObject menuItemItemDoc = new BaseObject();
    menuItemItemDoc.setName(mItemFullName);
    menuItemItemDoc.setClassName("Celements2.MenuItem");
    doc.setObject("Celements2.MenuItem", 0, menuItemItemDoc);
    expect(getWikiMock().getDocument(eq(prevDoc.getDocumentReference()), same(
        getContext()))).andReturn(prevDoc).once();
    TreeNode tnPrev = new TreeNode(prevDoc.getDocumentReference(), null, 0);
    ITreeNodeService mockTreeNodeService = createMock(ITreeNodeService.class);
    celUtils.injectTreeNodeService(mockTreeNodeService);
    expect(mockTreeNodeService.getPrevMenuItem(doc.getDocumentReference())).andReturn(
        tnPrev).once();
    replayDefault(mockTreeNodeService);
    BaseObject prevMenuItem = celUtils.getPrevMenuItem(mItemFullName, getContext());
    assertEquals("MySpace.Doc1 MenuItem expected.", menuItem1, prevMenuItem);
    verifyDefault(mockTreeNodeService);
  }

  @Test
  public void testPrevMenuItem_noPrev() throws XWikiException {
    String mItemFullName = "mySpace.myMenuItemDoc";
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName(mItemFullName);
    getContext().setDoc(doc);
    BaseObject menuItem1 = new BaseObject();
    String prevFullName = "mySpace.Doc1";
    menuItem1.setName(prevFullName);
    menuItem1.setClassName("Celements2.MenuItem");
    XWikiDocument prevDoc = new XWikiDocument();
    prevDoc.setFullName(prevFullName);
    prevDoc.setObject("Celements2.MenuItem", 0, menuItem1);
    BaseObject menuItemItemDoc = new BaseObject();
    menuItemItemDoc.setName(mItemFullName);
    menuItemItemDoc.setClassName("Celements2.MenuItem");
    doc.setObject("Celements2.MenuItem", 0, menuItemItemDoc);
    ITreeNodeService mockTreeNodeService = createMock(ITreeNodeService.class);
    celUtils.injectTreeNodeService(mockTreeNodeService);
    expect(mockTreeNodeService.getPrevMenuItem(eq(prevDoc.getDocumentReference()))).andReturn(
        null).once();
    replayDefault(mockTreeNodeService);
    BaseObject prevMenuItem = celUtils.getPrevMenuItem(prevFullName, getContext());
    assertNull(prevMenuItem);
    verifyDefault(mockTreeNodeService);
  }

  @Test
  public void testNextMenuItem() throws XWikiException {
    String mItemFullName = "mySpace.myMenuItemDoc";
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName(mItemFullName);
    getContext().setDoc(doc);
    BaseObject menuItem2 = new BaseObject();
    String nextFullName = "mySpace.Doc2";
    menuItem2.setName(nextFullName);
    menuItem2.setClassName("Celements2.MenuItem");
    XWikiDocument nextDoc = new XWikiDocument();
    nextDoc.setFullName(nextFullName);
    nextDoc.setObject("Celements2.MenuItem", 0, menuItem2);
    BaseObject menuItemItemDoc = new BaseObject();
    menuItemItemDoc.setName(mItemFullName);
    menuItemItemDoc.setClassName("Celements2.MenuItem");
    doc.setObject("Celements2.MenuItem", 0, menuItemItemDoc);
    expect(getWikiMock().getDocument(eq(nextDoc.getDocumentReference()), same(
        getContext()))).andReturn(nextDoc).once();
    TreeNode tnPrev = new TreeNode(nextDoc.getDocumentReference(), null, 0);
    ITreeNodeService mockTreeNodeService = createMock(ITreeNodeService.class);
    celUtils.injectTreeNodeService(mockTreeNodeService);
    expect(mockTreeNodeService.getNextMenuItem(eq(doc.getDocumentReference()))).andReturn(
        tnPrev).once();
    replayDefault(mockTreeNodeService);
    BaseObject prevMenuItem = celUtils.getNextMenuItem(mItemFullName, getContext());
    assertEquals("MySpace.Doc1 MenuItem expected.", menuItem2, prevMenuItem);
    verifyDefault(mockTreeNodeService);
  }

  @Test
  public void getNextMenuItem_next_docNotInContextSpace() throws XWikiException {
    getContext().setDatabase("siblingPrevious");
    String mItemFullName = "mySpace.myMenuItemDoc";
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName(mItemFullName);
    getContext().setDoc(new XWikiDocument("otherSpace", "otherDoc"));
    BaseObject menuItemItemDoc = new BaseObject();
    menuItemItemDoc.setName(mItemFullName);
    menuItemItemDoc.setClassName("Celements2.MenuItem");
    doc.setObject("Celements2.MenuItem", 0, menuItemItemDoc);
    BaseObject menuItem2 = new BaseObject();
    String nextFullName = "mySpace.Doc2";
    menuItem2.setName(nextFullName);
    menuItem2.setClassName("Celements2.MenuItem");
    XWikiDocument nextDoc = new XWikiDocument();
    nextDoc.setFullName(nextFullName);
    nextDoc.setObject("Celements2.MenuItem", 0, menuItem2);
    expect(getWikiMock().getDocument(eq(nextDoc.getDocumentReference()), same(
        getContext()))).andReturn(nextDoc).once();
    TreeNode tnPrev = new TreeNode(nextDoc.getDocumentReference(), null, 0);
    ITreeNodeService mockTreeNodeService = createMock(ITreeNodeService.class);
    celUtils.injectTreeNodeService(mockTreeNodeService);
    expect(mockTreeNodeService.getNextMenuItem(eq(doc.getDocumentReference()))).andReturn(
        tnPrev).once();
    replayDefault(mockTreeNodeService);
    BaseObject nextMenuItem = celUtils.getNextMenuItem(mItemFullName, getContext());
    assertEquals("MySpace.Doc2 MenuItem expected.", menuItem2, nextMenuItem);
    verifyDefault(mockTreeNodeService);
  }

  @Test
  public void testNextMenuItem_noNext() throws XWikiException {
    String mItemFullName = "mySpace.myMenuItemDoc";
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName(mItemFullName);
    getContext().setDoc(doc);
    BaseObject menuItem2 = new BaseObject();
    String nextFullName = "mySpace.Doc2";
    menuItem2.setName(nextFullName);
    menuItem2.setClassName("Celements2.MenuItem");
    XWikiDocument nextDoc = new XWikiDocument();
    nextDoc.setFullName(nextFullName);
    nextDoc.setObject("Celements2.MenuItem", 0, menuItem2);
    BaseObject menuItemItemDoc = new BaseObject();
    menuItemItemDoc.setName(mItemFullName);
    menuItemItemDoc.setClassName("Celements2.MenuItem");
    doc.setObject("Celements2.MenuItem", 0, menuItemItemDoc);
    ITreeNodeService mockTreeNodeService = createMock(ITreeNodeService.class);
    celUtils.injectTreeNodeService(mockTreeNodeService);
    expect(mockTreeNodeService.getNextMenuItem(eq(nextDoc.getDocumentReference()))).andReturn(
        null).once();
    replayDefault(mockTreeNodeService);
    BaseObject prevMenuItem = celUtils.getNextMenuItem(nextFullName, getContext());
    assertNull(prevMenuItem);
    verifyDefault(mockTreeNodeService);
  }

  @Test
  public void testGetDocSection_empty() throws XWikiException {
    String fullName = "Space.DocName";
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Space",
        "DocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(doc.getFullName()).andReturn(fullName).anyTimes();
    expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(getContext()))).andReturn(
        new XWikiDocument()).atLeastOnce();
    replayDefault(doc);
    assertNull(((WebUtils) WebUtils.getInstance()).getDocSection("(?=<table)", fullName, 1,
        getContext()));
    verifyDefault(doc);
  }

  @Test
  public void testGetDocSection_first() throws XWikiException {
    String fullName = "Space.DocName";
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Space",
        "DocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(doc.getFullName()).andReturn(fullName).anyTimes();
    expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc).atLeastOnce();
    XWikiDocument tdoc = new XWikiDocument();
    tdoc.setContent("abc<table>blabla</table><table>abc</table>");
    expect(doc.getTranslatedDocument(same(getContext()))).andReturn(tdoc).atLeastOnce();
    XWikiRenderingEngine renderer = createMock(XWikiRenderingEngine.class);
    expect(getWikiMock().getRenderingEngine()).andReturn(renderer).atLeastOnce();
    expect(renderer.renderText(eq("{pre}abc{/pre}"), eq(getContext().getDoc()), same(
        getContext()))).andReturn("abc").atLeastOnce();
    replayDefault(doc, renderer);
    System.out.println();
    assertEquals("abc", ((WebUtils) WebUtils.getInstance()).getDocSection("(?=<table)", fullName, 1,
        getContext()));
    verifyDefault(doc, renderer);
  }

  @Test
  public void testGetDocSection_firstEmptyRTE() throws XWikiException {
    String fullName = "Space.DocName";
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Space",
        "DocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(doc.getFullName()).andReturn(fullName).anyTimes();
    XWikiDocument tdoc = new XWikiDocument();
    tdoc.setContent("<p></p>  <br /> \n<table>blabla</table><table>abc</table>");
    expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(getContext()))).andReturn(tdoc).atLeastOnce();
    XWikiRenderingEngine renderer = createMock(XWikiRenderingEngine.class);
    expect(getWikiMock().getRenderingEngine()).andReturn(renderer).atLeastOnce();
    expect(renderer.renderText(eq("{pre}<table>blabla</table>{/pre}"), eq(getContext().getDoc()),
        same(getContext()))).andReturn("<table>blabla</table>").atLeastOnce();
    replayDefault(doc, renderer);
    assertEquals("<table>blabla</table>", ((WebUtils) WebUtils.getInstance()).getDocSection(
        "(?=<table)", fullName, 1, getContext()));
    verifyDefault(doc, renderer);
  }

  @Test
  public void testGetDocSection_middle() throws XWikiException {
    String fullName = "Space.DocName";
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Space",
        "DocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(doc.getFullName()).andReturn(fullName).anyTimes();
    XWikiDocument tdoc = new XWikiDocument();
    tdoc.setContent("abc<table>blabla</table><table>abc</table>");
    expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(getContext()))).andReturn(tdoc).atLeastOnce();
    XWikiRenderingEngine renderer = createMock(XWikiRenderingEngine.class);
    expect(getWikiMock().getRenderingEngine()).andReturn(renderer).atLeastOnce();
    expect(renderer.renderText(eq("{pre}<table>blabla</table>{/pre}"), eq(getContext().getDoc()),
        same(getContext()))).andReturn("<table>blabla</table>").atLeastOnce();
    replayDefault(doc, renderer);
    assertEquals("<table>blabla</table>", ((WebUtils) WebUtils.getInstance()).getDocSection(
        "(?=<table)", fullName, 2, getContext()));
    verifyDefault(doc, renderer);
  }

  @Test
  public void testGetDocSection_last() throws XWikiException {
    String fullName = "Space.DocName";
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Space",
        "DocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(doc.getFullName()).andReturn(fullName).anyTimes();
    XWikiDocument tdoc = new XWikiDocument();
    tdoc.setContent("abc<table>blabla</table><table>abc</table>");
    expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(getContext()))).andReturn(tdoc).atLeastOnce();
    XWikiRenderingEngine renderer = createMock(XWikiRenderingEngine.class);
    expect(getWikiMock().getRenderingEngine()).andReturn(renderer).atLeastOnce();
    expect(renderer.renderText(eq("{pre}<table>abc</table>{/pre}"), eq(getContext().getDoc()), same(
        getContext()))).andReturn("<table>abc</table>").atLeastOnce();
    replayDefault(doc, renderer);
    assertEquals("<table>abc</table>", ((WebUtils) WebUtils.getInstance()).getDocSection(
        "(?=<table)", fullName, 3, getContext()));
    verifyDefault(doc, renderer);
  }

  @Test
  public void testCountSections_empty() throws XWikiException {
    String fullName = "Space.DocName";
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Space",
        "DocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(doc.getFullName()).andReturn(fullName).anyTimes();
    expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(getContext()))).andReturn(
        new XWikiDocument()).atLeastOnce();
    replayDefault(doc);
    assertEquals(0, WebUtils.getInstance().countSections("", fullName, getContext()));
    verifyDefault(doc);
  }

  @Test
  public void testCountSections_one() throws XWikiException {
    String fullName = "Space.DocName";
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Space",
        "DocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(doc.getFullName()).andReturn(fullName).anyTimes();
    XWikiDocument tdoc = new XWikiDocument();
    tdoc.setContent("<table>blabla</table>");
    expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(getContext()))).andReturn(tdoc).atLeastOnce();
    replayDefault(doc);
    assertEquals(1, WebUtils.getInstance().countSections("(?=<table)", fullName, getContext()));
    verifyDefault(doc);
  }

  @Test
  public void testCountSections_emptyRTEStart() throws XWikiException {
    String fullName = "Space.DocName";
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Space",
        "DocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(doc.getFullName()).andReturn(fullName).anyTimes();
    XWikiDocument tdoc = new XWikiDocument();
    tdoc.setContent("<p> </p>\n<table>blabla</table>");
    expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(getContext()))).andReturn(tdoc).atLeastOnce();
    replayDefault(doc);
    assertEquals(1, WebUtils.getInstance().countSections("(?=<table)", fullName, getContext()));
    verifyDefault(doc);
  }

  @Test
  public void testCountSections_several() throws XWikiException {
    String fullName = "Space.DocName";
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Space",
        "DocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(doc.getFullName()).andReturn(fullName).anyTimes();
    XWikiDocument tdoc = new XWikiDocument();
    tdoc.setContent("abc<table>blabla</table><table>abc</table>");
    expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(getContext()))).andReturn(tdoc).atLeastOnce();
    replayDefault(doc);
    assertEquals(3, WebUtils.getInstance().countSections("(?=<table)", fullName, getContext()));
    verifyDefault(doc);
  }

  @Test
  public void testGetDocSectionAsJSON() throws XWikiException {
    String fullName = "Space.DocName";
    DocumentReference docRef = new DocumentReference(getContext().getDatabase(), "Space",
        "DocName");
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(doc.getFullName()).andReturn(fullName).anyTimes();
    XWikiDocument tdoc = new XWikiDocument();
    tdoc.setContent("abc<table>blabla</table><table>abc</table>");
    expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(getContext()))).andReturn(tdoc).atLeastOnce();
    XWikiRenderingEngine renderer = createMock(XWikiRenderingEngine.class);
    expect(getWikiMock().getRenderingEngine()).andReturn(renderer).atLeastOnce();
    expect(renderer.renderText(eq("{pre}<table>blabla</table>{/pre}"), eq(getContext().getDoc()),
        same(getContext()))).andReturn("<table>blabla</table>").atLeastOnce();
    replayDefault(doc, renderer);
    String json = "[{\"content\" : \"<table>blabla</table>\", \"section\" : 2,"
        + " \"sectionNr\" : 3}]";
    assertEquals(json, WebUtils.getInstance().getDocSectionAsJSON("(?=<table)", fullName, 2,
        getContext()));
    verifyDefault(doc, renderer);
  }

  @Test
  public void testGetAdminLanguage_defaultToDocLanguage() throws XWikiException {
    getContext().setLanguage("de");
    String userName = "XWiki.MyUser";
    DocumentReference userDocRef = new DocumentReference(getContext().getDatabase(), "XWiki",
        "MyUser");
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    expect(getWikiMock().getDocument(eq(userDocRef), same(getContext()))).andReturn(userDoc);
    expect(getWikiMock().getSpacePreference(eq("admin_language"), eq("de"), same(
        getContext()))).andReturn("de");
    replayDefault();
    // getContext().setUser calls xgetWikiMock().isVirtualMode in xgetWikiMock() version
    // 4.5 thus why it must be
    // set after calling replay
    getContext().setUser(userName);
    assertEquals("de", celUtils.getAdminLanguage(getContext()));
    verifyDefault();
  }

  @Test
  public void testGetAdminLanguage_contextUser() throws XWikiException {
    getContext().setLanguage("de");
    String userName = "XWiki.MyUser";
    DocumentReference userDocRef = new DocumentReference(getContext().getDatabase(), "XWiki",
        "MyUser");
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    DocumentReference xwikiUserClassRef = new DocumentReference(getContext().getDatabase(), "XWiki",
        "XWikiUsers");
    BaseObject userObj = new BaseObject();
    userObj.setXClassReference(xwikiUserClassRef);
    userObj.setStringValue("admin_language", "fr");
    userDoc.setXObject(0, userObj);
    expect(getWikiMock().getDocument(eq(userDocRef), same(getContext()))).andReturn(userDoc);
    replayDefault();
    // getContext().setUser calls xgetWikiMock().isVirtualMode in xgetWikiMock() version
    // 4.5 thus why it must be
    // set after calling replay
    getContext().setUser(userName);
    assertEquals("fr", celUtils.getAdminLanguage(getContext()));
    verifyDefault();
  }

  @Test
  public void testGetAdminLanguage_notContextUser() throws XWikiException {
    getContext().setLanguage("de");
    String userName = "XWiki.MyUser";
    DocumentReference userDocRef = new DocumentReference(getContext().getDatabase(), "XWiki",
        "MyUser");
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    DocumentReference xwikiUserClassRef = new DocumentReference(getContext().getDatabase(), "XWiki",
        "XWikiUsers");
    BaseObject userObj = new BaseObject();
    userObj.setXClassReference(xwikiUserClassRef);
    userObj.setStringValue("admin_language", "fr");
    userDoc.setXObject(0, userObj);
    expect(getWikiMock().getDocument(eq(userDocRef), same(getContext()))).andReturn(userDoc);
    replayDefault();
    // getContext().setUser calls xgetWikiMock().isVirtualMode in xgetWikiMock() version
    // 4.5 thus why it must be
    // set after calling replay
    getContext().setUser("XWiki.NotMyUser");
    assertEquals("fr", celUtils.getAdminLanguage(userName, getContext()));
    verifyDefault();
  }

  @Test
  public void testGetAdminLanguage_defaultToWebPreferences() throws XWikiException {
    getContext().setLanguage("de");
    String userName = "XWiki.MyUser";
    expect(getWikiMock().getSpacePreference(eq("admin_language"), isA(String.class), same(
        getContext()))).andReturn("en");
    DocumentReference userDocRef = new DocumentReference(getContext().getDatabase(), "XWiki",
        "MyUser");
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    DocumentReference xwikiUserClassRef = new DocumentReference(getContext().getDatabase(), "XWiki",
        "XWikiUsers");
    BaseObject userObj = new BaseObject();
    userObj.setXClassReference(xwikiUserClassRef);
    userObj.setStringValue("admin_language", "");
    userDoc.setXObject(0, userObj);
    expect(getWikiMock().getDocument(eq(userDocRef), same(getContext()))).andReturn(userDoc);
    replayDefault();
    // getContext().setUser calls xgetWikiMock().isVirtualMode in xgetWikiMock() version
    // 4.5 thus why it must be
    // set after calling replay
    getContext().setUser("XWiki.NotMyUser");
    assertEquals("en", celUtils.getAdminLanguage(userName, getContext()));
    verifyDefault();
  }

  @Test
  public void testGetDocumentParentsList() throws XWikiException {
    String fullName = "mySpace.MyDoc";
    String parent1 = "mySpace.Parent1";
    String parent2 = "mySpace.Parent2";
    expect(getWikiMock().exists(eq(fullName), same(getContext()))).andReturn(true);
    XWikiDocument myDoc = new XWikiDocument();
    myDoc.setFullName(fullName);
    myDoc.setParent(parent1);
    expect(getWikiMock().getDocument(eq(fullName), same(getContext()))).andReturn(myDoc);
    expect(getWikiMock().exists(eq(parent1), same(getContext()))).andReturn(true);
    XWikiDocument myParent1 = new XWikiDocument();
    myParent1.setFullName(parent1);
    myParent1.setParent(parent2);
    expect(getWikiMock().getDocument(eq(parent1), same(getContext()))).andReturn(myParent1);
    expect(getWikiMock().exists(eq(parent2), same(getContext()))).andReturn(true);
    XWikiDocument myParent2 = new XWikiDocument();
    myParent2.setFullName(parent2);
    myParent2.setParent("");
    expect(getWikiMock().getDocument(eq(parent2), same(getContext()))).andReturn(myParent2);
    List<String> docParentsList = Arrays.asList(fullName, parent1, parent2);
    replayDefault();
    assertEquals(docParentsList, celUtils.getDocumentParentsList(fullName, true, getContext()));
    verifyDefault();
  }

  @Test
  public void isAttachmentLink_null() {
    assertFalse(celUtils.isAttachmentLink(null));
  }

  @Test
  public void isAttachmentLink_empty() {
    assertFalse(celUtils.isAttachmentLink(""));
  }

  @Test
  public void isAttachmentLink_url() {
    assertFalse(celUtils.isAttachmentLink("/download/Space/Page/attachment.jpg"));
  }

  @Test
  public void isAttachmentLink_is() {
    assertTrue(celUtils.isAttachmentLink("Space.Page;attachment.jpg"));
  }

  @Test
  public void isAttachmentLink_isWithDb() {
    assertTrue(celUtils.isAttachmentLink("db:Space.Page;attachment.jpg"));
  }

  @Test
  public void testGetAttachmentListSorted_getAll() throws ClassNotFoundException, XWikiException,
      IOException {
    XWikiRightService rightsService = createMock(XWikiRightService.class);
    expect(getWikiMock().getRightService()).andReturn(rightsService).anyTimes();
    expect(getWikiMock().getDocument(eq(new DocumentReference(getContext().getDatabase(), "XWiki",
        "XWikiPreferences")), same(getContext()))).andReturn(null).anyTimes();
    expect(getWikiMock().clearName(eq("a.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "a.jpg").once();
    expect(getWikiMock().clearName(eq("b.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "b.jpg").once();
    expect(getWikiMock().clearName(eq("c.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "c.jpg").once();
    expect(getWikiMock().clearName(eq("d.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "d.jpg").once();
    expect(getWikiMock().clearName(eq("e.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "e.jpg").once();
    expect(getWikiMock().clearName(eq("f.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "f.jpg").once();
    expect(getWikiMock().clearName(eq("g.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "g.jpg").once();
    expect(getWikiMock().clearName(eq("h.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "h.jpg").once();
    XWikiEngineContext engContext = createMock(XWikiEngineContext.class);
    getContext().setEngineContext(engContext);
    expect(engContext.getMimeType((String) anyObject())).andReturn("image/jpg").anyTimes();
    replayDefault(engContext, rightsService);
    DocumentReference docref = new DocumentReference("a", "b", "c");
    Document doc = new Document(new XWikiDocument(docref), getContext());
    InputStream in = getClass().getClassLoader().getResourceAsStream("test.jpg");
    doc.addAttachment("c.jpg", in);
    doc.addAttachment("d.jpg", in);
    doc.addAttachment("h.jpg", in);
    doc.addAttachment("a.jpg", in);
    doc.addAttachment("e.jpg", in);
    doc.addAttachment("b.jpg", in);
    doc.addAttachment("g.jpg", in);
    doc.addAttachment("f.jpg", in);
    List<Attachment> result = celUtils.getAttachmentListSorted(doc,
        "AttachmentAscendingNameComparator", true, 0, 0);
    verifyDefault(engContext, rightsService);
    assertEquals(8, result.size());
    assertEquals("a.jpg", result.get(0).getFilename());
    assertEquals("e.jpg", result.get(4).getFilename());
    assertEquals("h.jpg", result.get(7).getFilename());
  }

  @Test
  public void testGetAttachmentListSorted_getFirstPart() throws XWikiException,
      ClassNotFoundException, IOException {
    XWikiRightService rightsService = createMock(XWikiRightService.class);
    expect(getWikiMock().getRightService()).andReturn(rightsService).anyTimes();
    expect(getWikiMock().getDocument(eq(new DocumentReference(getContext().getDatabase(), "XWiki",
        "XWikiPreferences")), same(getContext()))).andReturn(null).anyTimes();
    expect(getWikiMock().clearName(eq("a.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "a.jpg").once();
    expect(getWikiMock().clearName(eq("b.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "b.jpg").once();
    expect(getWikiMock().clearName(eq("c.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "c.jpg").once();
    expect(getWikiMock().clearName(eq("d.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "d.jpg").once();
    expect(getWikiMock().clearName(eq("e.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "e.jpg").once();
    expect(getWikiMock().clearName(eq("f.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "f.jpg").once();
    expect(getWikiMock().clearName(eq("g.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "g.jpg").once();
    expect(getWikiMock().clearName(eq("h.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "h.jpg").once();
    XWikiEngineContext engContext = createMock(XWikiEngineContext.class);
    getContext().setEngineContext(engContext);
    expect(engContext.getMimeType((String) anyObject())).andReturn("image/jpg").anyTimes();
    replayDefault(engContext, rightsService);
    DocumentReference docref = new DocumentReference("a", "b", "c");
    Document doc = new Document(new XWikiDocument(docref), getContext());
    InputStream in = getClass().getClassLoader().getResourceAsStream("test.jpg");
    doc.addAttachment("c.jpg", in);
    doc.addAttachment("d.jpg", in);
    doc.addAttachment("h.jpg", in);
    doc.addAttachment("a.jpg", in);
    doc.addAttachment("e.jpg", in);
    doc.addAttachment("b.jpg", in);
    doc.addAttachment("g.jpg", in);
    doc.addAttachment("f.jpg", in);
    List<Attachment> result = celUtils.getAttachmentListSorted(doc,
        "AttachmentAscendingNameComparator", true, -1, 3);
    verifyDefault(engContext, rightsService);
    assertEquals(3, result.size());
    assertEquals("a.jpg", result.get(0).getFilename());
    assertEquals("b.jpg", result.get(1).getFilename());
    assertEquals("c.jpg", result.get(2).getFilename());
  }

  @Test
  public void testGetAttachmentListSorted_getMiddlePart() throws XWikiException,
      ClassNotFoundException, IOException {
    XWikiRightService rightsService = createMock(XWikiRightService.class);
    expect(getWikiMock().getRightService()).andReturn(rightsService).anyTimes();
    expect(getWikiMock().getDocument(eq(new DocumentReference(getContext().getDatabase(), "XWiki",
        "XWikiPreferences")), same(getContext()))).andReturn(null).anyTimes();
    expect(getWikiMock().clearName(eq("a.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "a.jpg").once();
    expect(getWikiMock().clearName(eq("b.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "b.jpg").once();
    expect(getWikiMock().clearName(eq("c.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "c.jpg").once();
    expect(getWikiMock().clearName(eq("d.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "d.jpg").once();
    expect(getWikiMock().clearName(eq("e.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "e.jpg").once();
    expect(getWikiMock().clearName(eq("f.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "f.jpg").once();
    expect(getWikiMock().clearName(eq("g.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "g.jpg").once();
    expect(getWikiMock().clearName(eq("h.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "h.jpg").once();
    XWikiEngineContext engContext = createMock(XWikiEngineContext.class);
    getContext().setEngineContext(engContext);
    expect(engContext.getMimeType((String) anyObject())).andReturn("image/jpg").anyTimes();
    replayDefault(engContext, rightsService);
    DocumentReference docref = new DocumentReference("a", "b", "c");
    Document doc = new Document(new XWikiDocument(docref), getContext());
    InputStream in = getClass().getClassLoader().getResourceAsStream("test.jpg");
    doc.addAttachment("c.jpg", in);
    doc.addAttachment("d.jpg", in);
    doc.addAttachment("h.jpg", in);
    doc.addAttachment("a.jpg", in);
    doc.addAttachment("e.jpg", in);
    doc.addAttachment("b.jpg", in);
    doc.addAttachment("g.jpg", in);
    doc.addAttachment("f.jpg", in);
    List<Attachment> result = celUtils.getAttachmentListSorted(doc,
        "AttachmentAscendingNameComparator", true, 3, 3);
    verifyDefault(engContext, rightsService);
    assertEquals(3, result.size());
    assertEquals("d.jpg", result.get(0).getFilename());
    assertEquals("e.jpg", result.get(1).getFilename());
    assertEquals("f.jpg", result.get(2).getFilename());
  }

  @Test
  public void testGetAttachmentListSorted_getLastPart() throws XWikiException,
      ClassNotFoundException, IOException {
    XWikiRightService rightsService = createMock(XWikiRightService.class);
    expect(getWikiMock().getRightService()).andReturn(rightsService).anyTimes();
    expect(getWikiMock().getDocument(eq(new DocumentReference(getContext().getDatabase(), "XWiki",
        "XWikiPreferences")), same(getContext()))).andReturn(null).anyTimes();
    expect(getWikiMock().clearName(eq("a.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "a.jpg").once();
    expect(getWikiMock().clearName(eq("b.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "b.jpg").once();
    expect(getWikiMock().clearName(eq("c.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "c.jpg").once();
    expect(getWikiMock().clearName(eq("d.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "d.jpg").once();
    expect(getWikiMock().clearName(eq("e.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "e.jpg").once();
    expect(getWikiMock().clearName(eq("f.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "f.jpg").once();
    expect(getWikiMock().clearName(eq("g.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "g.jpg").once();
    expect(getWikiMock().clearName(eq("h.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "h.jpg").once();
    XWikiEngineContext engContext = createMock(XWikiEngineContext.class);
    getContext().setEngineContext(engContext);
    expect(engContext.getMimeType((String) anyObject())).andReturn("image/jpg").anyTimes();
    replayDefault(engContext, rightsService);
    DocumentReference docref = new DocumentReference("a", "b", "c");
    Document doc = new Document(new XWikiDocument(docref), getContext());
    InputStream in = getClass().getClassLoader().getResourceAsStream("test.jpg");
    doc.addAttachment("c.jpg", in);
    doc.addAttachment("d.jpg", in);
    doc.addAttachment("h.jpg", in);
    doc.addAttachment("a.jpg", in);
    doc.addAttachment("e.jpg", in);
    doc.addAttachment("b.jpg", in);
    doc.addAttachment("g.jpg", in);
    doc.addAttachment("f.jpg", in);
    List<Attachment> result = celUtils.getAttachmentListSorted(doc,
        "AttachmentAscendingNameComparator", true, 6, 3);
    verifyDefault(engContext, rightsService);
    assertEquals(2, result.size());
    assertEquals("g.jpg", result.get(0).getFilename());
    assertEquals("h.jpg", result.get(1).getFilename());
  }

  @Test
  public void testGetAttachmentListSorted_getEmpty() throws XWikiException, ClassNotFoundException,
      IOException {
    XWikiRightService rightsService = createMock(XWikiRightService.class);
    expect(getWikiMock().getRightService()).andReturn(rightsService).anyTimes();
    expect(getWikiMock().getDocument(eq(new DocumentReference(getContext().getDatabase(), "XWiki",
        "XWikiPreferences")), same(getContext()))).andReturn(null).anyTimes();
    expect(getWikiMock().clearName(eq("a.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "a.jpg").once();
    expect(getWikiMock().clearName(eq("b.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "b.jpg").once();
    expect(getWikiMock().clearName(eq("c.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "c.jpg").once();
    expect(getWikiMock().clearName(eq("d.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "d.jpg").once();
    expect(getWikiMock().clearName(eq("e.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "e.jpg").once();
    expect(getWikiMock().clearName(eq("f.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "f.jpg").once();
    expect(getWikiMock().clearName(eq("g.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "g.jpg").once();
    expect(getWikiMock().clearName(eq("h.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "h.jpg").once();
    XWikiEngineContext engContext = createMock(XWikiEngineContext.class);
    getContext().setEngineContext(engContext);
    expect(engContext.getMimeType((String) anyObject())).andReturn("image/jpg").anyTimes();
    replayDefault(engContext, rightsService);
    DocumentReference docref = new DocumentReference("a", "b", "c");
    Document doc = new Document(new XWikiDocument(docref), getContext());
    InputStream in = getClass().getClassLoader().getResourceAsStream("test.jpg");
    doc.addAttachment("c.jpg", in);
    doc.addAttachment("d.jpg", in);
    doc.addAttachment("h.jpg", in);
    doc.addAttachment("a.jpg", in);
    doc.addAttachment("e.jpg", in);
    doc.addAttachment("b.jpg", in);
    doc.addAttachment("g.jpg", in);
    doc.addAttachment("f.jpg", in);
    List<Attachment> result = celUtils.getAttachmentListSorted(doc,
        "AttachmentAscendingNameComparator", true, 7, 0);
    verifyDefault(engContext, rightsService);
    assertEquals(0, result.size());
  }

  @Test
  public void testGetAttachmentListSorted_getWithNonImages() throws XWikiException,
      ClassNotFoundException, IOException {
    XWikiRightService rightsService = createMock(XWikiRightService.class);
    expect(getWikiMock().getRightService()).andReturn(rightsService).anyTimes();
    expect(getWikiMock().getDocument(eq(new DocumentReference(getContext().getDatabase(), "XWiki",
        "XWikiPreferences")), same(getContext()))).andReturn(null).anyTimes();
    expect(getWikiMock().clearName(eq("a.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "a.jpg").once();
    expect(getWikiMock().clearName(eq("b.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "b.jpg").once();
    expect(getWikiMock().clearName(eq("c.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "c.jpg").once();
    expect(getWikiMock().clearName(eq("d.txt"), eq(false), eq(true), same(getContext()))).andReturn(
        "d.txt").once();
    expect(getWikiMock().clearName(eq("e.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "e.jpg").once();
    expect(getWikiMock().clearName(eq("f.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "f.jpg").once();
    expect(getWikiMock().clearName(eq("g.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "g.jpg").once();
    expect(getWikiMock().clearName(eq("h.jpg"), eq(false), eq(true), same(getContext()))).andReturn(
        "h.jpg").once();
    XWikiEngineContext engContext = createMock(XWikiEngineContext.class);
    getContext().setEngineContext(engContext);
    expect(engContext.getMimeType("d.txt")).andReturn("txt").once();
    expect(engContext.getMimeType((String) anyObject())).andReturn("image/jpg").anyTimes();
    replayDefault(engContext, rightsService);
    DocumentReference docref = new DocumentReference("a", "b", "c");
    Document doc = new Document(new XWikiDocument(docref), getContext());
    InputStream in = getClass().getClassLoader().getResourceAsStream("test.jpg");
    InputStream intxt = getClass().getClassLoader().getResourceAsStream("test.txt");
    doc.addAttachment("c.jpg", in);
    doc.addAttachment("d.txt", intxt);
    doc.addAttachment("h.jpg", in);
    doc.addAttachment("a.jpg", in);
    doc.addAttachment("e.jpg", in);
    doc.addAttachment("b.jpg", in);
    doc.addAttachment("g.jpg", in);
    doc.addAttachment("f.jpg", in);
    List<Attachment> result = celUtils.getAttachmentListSorted(doc,
        "AttachmentAscendingNameComparator", true, 2, 3);
    verifyDefault(engContext, rightsService);
    assertEquals(3, result.size());
    assertEquals("c.jpg", result.get(0).getFilename());
    assertEquals("e.jpg", result.get(1).getFilename());
    assertEquals("f.jpg", result.get(2).getFilename());
  }

  // *****************************************************************
  // * H E L P E R - M E T H O D S *
  // *****************************************************************/

  private BaseObject createNavObj(int toLevel, XWikiDocument doc) {
    DocumentReference navigationConfigClassReference = Navigation.getNavigationConfigClassReference(
        getContext().getDatabase());
    BaseObject navObj = new BaseObject();
    navObj.setXClassReference(navigationConfigClassReference);
    navObj.setStringValue("menu_element_name", "mainMenu");
    navObj.setIntValue("to_hierarchy_level", toLevel);
    navObj.setDocumentReference(doc.getDocumentReference());
    return navObj;
  }

}
