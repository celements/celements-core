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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.inheritor.InheritorFactory;
import com.celements.navigation.Navigation;
import com.celements.navigation.filter.InternalRightsFilter;
import com.celements.navigation.service.ITreeNodeService;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.render.XWikiRenderingEngine;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.web.XWikiURLFactory;

public class WebUtilsTest extends AbstractBridgedComponentTestCase {

  private WebUtils celUtils;
  private XWikiContext context;
  private XWikiStoreInterface mockStore;
  private XWikiURLFactory mockURLFactory;
  private XWiki wiki;
  private XWikiStoreInterface mockXStore;
  private ITreeNodeService injected_TreeNodeService;
  
  @Before
  public void setUp_WebUtilsTest() {
    context = getContext();
    wiki = createMock(XWiki.class);
    context.setWiki(wiki);
    mockStore = createMock(XWikiStoreInterface.class);
    celUtils = new WebUtils();
    mockURLFactory = createMock(XWikiURLFactory.class);
    mockXStore = createMock(XWikiStoreInterface.class);
    expect(wiki.getStore()).andReturn(mockXStore).anyTimes();
    context.setURLFactory(mockURLFactory);
    injected_TreeNodeService = createMock(ITreeNodeService.class);
    celUtils.injected_TreeNodeService = injected_TreeNodeService;
  }

  @Test
  public void testGetParentForLevel_mainLevel() {
    XWikiDocument activeDoc = new XWikiDocument("Space", "Name");
    activeDoc.setParent("");
    context.setDoc(activeDoc);
    assertNotNull(celUtils.getParentForLevel(1, context));
    assertEquals("", celUtils.getParentForLevel(1, context));
  }

  @Test
  public void testGetMaxConfiguredNavigationLevel_twoParents() throws Exception {
    InheritorFactory inheritorFact = new InheritorFactory();
    PageLayoutCommand mockPageLayoutCmd = createMock(PageLayoutCommand.class);
    inheritorFact.injectPageLayoutCmd(mockPageLayoutCmd);
    celUtils.injectInheritorFactory(inheritorFact);
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    expect(mockPageLayoutCmd.getPageLayoutForDoc(eq(doc.getFullName()), same(context))
        ).andReturn(null).atLeastOnce();
    context.setDoc(doc);
    XWikiDocument webPrefDoc = new XWikiDocument();
    webPrefDoc.setFullName("MySpace.WebPreferences");
    expect(wiki.getDocument(eq("MySpace.WebPreferences"), eq(context))).andReturn(
        webPrefDoc).atLeastOnce();
    Vector<BaseObject> navObjects = new Vector<BaseObject>();
    navObjects.add(createNavObj(5, webPrefDoc));
    navObjects.add(createNavObj(4, webPrefDoc));
    navObjects.add(createNavObj(8, webPrefDoc));
    navObjects.add(createNavObj(3, webPrefDoc));
    expect(wiki.getDocument(eq(new DocumentReference("xwikidb", "MySpace", 
        "WebPreferences")), same(context))).andReturn(webPrefDoc).atLeastOnce();
    webPrefDoc.setObjects(Navigation.NAVIGATION_CONFIG_CLASS, navObjects);
    expect(wiki.getWebPreference(eq("skin"), same(context))).andReturn("Skins.MySkin"
        ).atLeastOnce();
    replay(mockStore, wiki, mockXStore, mockPageLayoutCmd);
    int maxLevel = celUtils.getMaxConfiguredNavigationLevel(context);
    verify(mockStore, wiki, mockXStore, mockPageLayoutCmd);
    assertEquals("Max to Level in navConfigs is 8.", 8, maxLevel);
  }

  @Test
  public void testGetMaxConfiguredNavigationLevel_deletedObject_NPE() throws Exception {
    InheritorFactory inheritorFact = new InheritorFactory();
    PageLayoutCommand mockPageLayoutCmd = createMock(PageLayoutCommand.class);
    inheritorFact.injectPageLayoutCmd(mockPageLayoutCmd);
    celUtils.injectInheritorFactory(inheritorFact);
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    expect(mockPageLayoutCmd.getPageLayoutForDoc(eq(doc.getFullName()), same(context))
        ).andReturn(null).atLeastOnce();
    context.setDoc(doc);
    XWikiDocument webPrefDoc = new XWikiDocument();
    webPrefDoc.setFullName("MySpace.WebPreferences");
    expect(wiki.getDocument(eq("MySpace.WebPreferences"), eq(context))
        ).andReturn(webPrefDoc).atLeastOnce();
    Vector<BaseObject> navObjects = new Vector<BaseObject>();
    navObjects.add(createNavObj(5, webPrefDoc));
    navObjects.add(null); // deleting an object can lead to a null pointer
                          // in the object list
    navObjects.add(createNavObj(8, webPrefDoc));
    navObjects.add(createNavObj(3, webPrefDoc));
    webPrefDoc.setObjects(Navigation.NAVIGATION_CONFIG_CLASS, navObjects);
    expect(wiki.getDocument(eq(new DocumentReference("xwikidb", "MySpace", 
        "WebPreferences")), same(context))).andReturn(webPrefDoc).atLeastOnce();
    expect(wiki.getWebPreference(eq("skin"), same(context))).andReturn("Skins.MySkin"
      ).atLeastOnce();
    replay(mockStore, wiki, mockXStore, mockPageLayoutCmd);
    int maxLevel = celUtils.getMaxConfiguredNavigationLevel(context);
    verify(mockStore, wiki, mockXStore, mockPageLayoutCmd);
    assertEquals("Max to Level in navConfigs is 8.", 8, maxLevel);
  }

  @Test
  public void testGetMaxConfiguredNavigationLevel_noObjectFound_NPE() throws Exception {
    InheritorFactory inheritorFact = new InheritorFactory();
    PageLayoutCommand mockPageLayoutCmd = createMock(PageLayoutCommand.class);
    inheritorFact.injectPageLayoutCmd(mockPageLayoutCmd);
    celUtils.injectInheritorFactory(inheritorFact);
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    expect(mockPageLayoutCmd.getPageLayoutForDoc(eq(doc.getFullName()), same(context))
        ).andReturn(null).atLeastOnce();
    context.setDoc(doc);
    XWikiDocument webPrefDoc = new XWikiDocument();
    webPrefDoc.setFullName("MySpace.WebPreferences");
    expect(wiki.getDocument(eq("MySpace.WebPreferences"), eq(context))
      ).andReturn(webPrefDoc).atLeastOnce();
    XWikiDocument xwikiPrefDoc = new XWikiDocument();
    webPrefDoc.setFullName("XWiki.XWikiPreferences");
    expect(wiki.getDocument(eq("XWiki.XWikiPreferences"), eq(context))
      ).andReturn(xwikiPrefDoc).atLeastOnce();
    XWikiDocument skinDoc = new XWikiDocument();
    skinDoc.setFullName("Skins.MySkin");
    expect(wiki.getDocument(eq("Skins.MySkin"), eq(context))
      ).andReturn(skinDoc).atLeastOnce();
    expect(wiki.getWebPreference(eq("skin"), same(context))).andReturn("Skins.MySkin"
      ).atLeastOnce();
    replay(mockStore, wiki, mockXStore, mockPageLayoutCmd);
    int maxLevel = celUtils.getMaxConfiguredNavigationLevel(context);
    verify(mockStore, wiki, mockXStore, mockPageLayoutCmd);
    assertEquals("Expecting default max level.", Navigation.DEFAULT_MAX_LEVEL, maxLevel);
  }

  @Test
  public void testGetMaxConfiguredNavigationLevel_threeParents() throws Exception {
    InheritorFactory inheritorFact = new InheritorFactory();
    PageLayoutCommand mockPageLayoutCmd = createMock(PageLayoutCommand.class);
    inheritorFact.injectPageLayoutCmd(mockPageLayoutCmd);
    celUtils.injectInheritorFactory(inheritorFact);
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    expect(mockPageLayoutCmd.getPageLayoutForDoc(eq(doc.getFullName()), same(context))
        ).andReturn(null).atLeastOnce();
    context.setDoc(doc);
    XWikiDocument webPrefDoc = new XWikiDocument();
    webPrefDoc.setFullName("MySpace.WebPreferences");
    expect(wiki.getDocument(eq("MySpace.WebPreferences"), eq(context))
        ).andReturn(webPrefDoc).atLeastOnce();
    expect(wiki.getWebPreference(eq("skin"), same(context))).andReturn("Skins.MySkin"
      ).atLeastOnce();
    Vector<BaseObject> navObjects = new Vector<BaseObject>();
    navObjects.add(createNavObj(5, webPrefDoc));
    navObjects.add(createNavObj(4, webPrefDoc));
    navObjects.add(createNavObj(3, webPrefDoc));
    webPrefDoc.setObjects(Navigation.NAVIGATION_CONFIG_CLASS, navObjects);
    expect(wiki.getDocument(eq(new DocumentReference("xwikidb", "MySpace", 
        "WebPreferences")), same(context))).andReturn(webPrefDoc).atLeastOnce();
    replay(mockStore, wiki, mockXStore, mockPageLayoutCmd);
    int maxLevel = celUtils.getMaxConfiguredNavigationLevel(context);
    verify(mockStore, wiki, mockXStore, mockPageLayoutCmd);
    assertEquals("Parents are a.b, b.c and c.d therefor maxlevel must be 5.",
        5, maxLevel);
  }
  
  @Test
  public void testCoveredQuotient() {
    assertEquals(1, ((WebUtils)celUtils).coveredQuotient(5,5));
    assertEquals(3, ((WebUtils)celUtils).coveredQuotient(2,5));
    assertEquals(1, ((WebUtils)celUtils).coveredQuotient(8,4));
    assertEquals(1, ((WebUtils)celUtils).coveredQuotient(8,2));
    assertEquals(1, ((WebUtils)celUtils).coveredQuotient(8,7));
    assertEquals(0, ((WebUtils)celUtils).coveredQuotient(8,0));
  }

  @Test
  public void testPrepareMaxCoverSet() {
    ArrayList<String> threeElems = new ArrayList<String>(
        Arrays.asList(new String[] {"1","lj","lizh"}));
    assertEquals(3, ((WebUtils)celUtils).prepareMaxCoverSet(0, threeElems).size());
    assertEquals(3, ((WebUtils)celUtils).prepareMaxCoverSet(1, threeElems).size());
    assertEquals(3, ((WebUtils)celUtils).prepareMaxCoverSet(2, threeElems).size());
    assertEquals(3, ((WebUtils)celUtils).prepareMaxCoverSet(3, threeElems).size());
    assertEquals(6, ((WebUtils)celUtils).prepareMaxCoverSet(4, threeElems).size());
    assertEquals(6, ((WebUtils)celUtils).prepareMaxCoverSet(5, threeElems).size());
    assertEquals(6, ((WebUtils)celUtils).prepareMaxCoverSet(6, threeElems).size());
    assertEquals(9, ((WebUtils)celUtils).prepareMaxCoverSet(7, threeElems).size());
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
    expect(wiki.getDocument(fullName, context)).andReturn(testGalleryDoc);
    expect(wiki.getDocument(eq(new DocumentReference("xwikidb", "XWiki", 
        "XWikiPreferences")), same(context))).andReturn(null).atLeastOnce();
    replay(mockStore, wiki);
    List<Attachment> randImgs = celUtils.getRandomImages(fullName, 5, context);
    assertNotNull(randImgs);
    assertEquals(5, randImgs.size());
    verify(mockStore, wiki);
  }

  @Test
  public void testIsAdminUser_noAdminRights_noRightsService() {
    XWikiContext context = createMock(XWikiContext.class);
    expect(context.getWiki()).andReturn(wiki).atLeastOnce();
    expect(wiki.getRightService()).andReturn(null).anyTimes();
    TestMockUser xwikiUser = new TestMockUser("XWiki.TestUser");
    expect(context.getXWikiUser()).andReturn(xwikiUser).anyTimes();

    replay(context, mockStore, wiki);
    assertFalse(celUtils.isAdminUser(context));
    verify(context, mockStore, wiki);
  }
  
  @Test
  public void testIsAdminUser_noContextDoc() {
    XWikiContext context = createMock(XWikiContext.class);
    expect(context.getDoc()).andReturn(null).atLeastOnce();
    expect(context.getWiki()).andReturn(wiki).atLeastOnce();
    XWikiRightService mockRightsService = createMock(XWikiRightService.class);
    expect(wiki.getRightService()).andReturn(mockRightsService).anyTimes();
    TestMockUser xwikiUser = new TestMockUser("XWiki.TestUser");
    expect(context.getXWikiUser()).andReturn(xwikiUser).anyTimes();
    //hasAdminRights must not be called it will fail with an NPE

    replay(context, mockStore, wiki, mockRightsService);
    assertFalse(celUtils.isAdminUser(context));
    verify(context, mockStore, wiki, mockRightsService);
  }
  
  @Test
  public void testIsAdminUser_noAdminRights_notInAdminGroup() {
    XWikiContext context = createMock(XWikiContext.class);
    expect(context.getDoc()).andReturn(new XWikiDocument()).atLeastOnce();
    expect(context.getWiki()).andReturn(wiki).atLeastOnce();
    XWikiRightService mockRightsService = createMock(XWikiRightService.class);
    expect(wiki.getRightService()).andReturn(mockRightsService).anyTimes();
    expect(mockRightsService.hasAdminRights(same(context))).andReturn(false).anyTimes();
    TestMockUser xwikiUser = new TestMockUser("XWiki.TestUser");
    expect(context.getXWikiUser()).andReturn(xwikiUser).anyTimes();

    replay(context, mockStore, wiki, mockRightsService);
    assertFalse(celUtils.isAdminUser(context));
    verify(context, mockStore, wiki, mockRightsService);
  }
  
  @Test
  public void testIsAdminUser_noAdminRights_isInAdminGroup() {
    XWikiContext context = createMock(XWikiContext.class);
    expect(context.getDoc()).andReturn(new XWikiDocument()).atLeastOnce();
    expect(context.getWiki()).andReturn(wiki).atLeastOnce();
    XWikiRightService mockRightsService = createMock(XWikiRightService.class);
    expect(wiki.getRightService()).andReturn(mockRightsService).anyTimes();
    expect(mockRightsService.hasAdminRights(same(context))).andReturn(false).anyTimes();
    TestMockUser xwikiUser = new TestMockUser("XWiki.TestUser");
    HashSet<String> groups = new HashSet<String>();
    groups.add("XWiki.XWikiAdminGroup");
    xwikiUser.setTestGroups(groups );
    expect(context.getXWikiUser()).andReturn(xwikiUser ).anyTimes();

    replay(context, mockStore, wiki, mockRightsService);
    assertTrue(celUtils.isAdminUser(context));
    verify(context, mockStore, wiki, mockRightsService);
  }
  
  @Test
  public void testIsAdminUser_adminRights_notInAdminGroup() {
    XWikiContext context = createMock(XWikiContext.class);
    expect(context.getDoc()).andReturn(new XWikiDocument()).atLeastOnce();
    expect(context.getWiki()).andReturn(wiki).atLeastOnce();
    XWikiRightService mockRightsService = createMock(XWikiRightService.class);
    expect(wiki.getRightService()).andReturn(mockRightsService).anyTimes();
    expect(mockRightsService.hasAdminRights(same(context))).andReturn(true).anyTimes();
    TestMockUser xwikiUser = new TestMockUser("XWiki.TestUser");
    expect(context.getXWikiUser()).andReturn(xwikiUser).anyTimes();

    replay(context, mockStore, wiki, mockRightsService);
    assertTrue(celUtils.isAdminUser(context));
    verify(context, mockStore, wiki, mockRightsService);
  }
  
  @Test
  public void testGetMajorVersion_nullDoc() {
    assertEquals("1", ((WebUtils)celUtils).getMajorVersion(null));
  }
  
  @Test
  public void testGetMajorVersion_noVersionSet() {
    XWikiDocument doc = new XWikiDocument();
    assertEquals("1", ((WebUtils)celUtils).getMajorVersion(doc));
  }
  
  @Test
  public void testGetMajorVersion() {
    XWikiDocument doc = new XWikiDocument();
    doc.setVersion("28.82");
    assertEquals("28", ((WebUtils)celUtils).getMajorVersion(doc));
  }
  
  @Test
  public void testReplaceInternalWithExternalLinks_nothingToReplace() {
    String host = "www.bla.com";
    String test = "test";
    assertEquals(test, ((WebUtils)celUtils).replaceInternalWithExternalLinks(test, host));
  }
  
  @Test
  public void testReplaceInternalWithExternalLinks_img_nothingToReplace() {
    String host = "www.bla.com/";
    String test = "<img src=\"http://www.bla.com/download/A/B/test.img\" />";
    assertEquals(test, ((WebUtils)celUtils).replaceInternalWithExternalLinks(test, host));
  }
  
  @Test
  public void testReplaceInternalWithExternalLinks_img_newInternal() {
    String host = "www.bla.com";
    String test = "<img src=\"/download/A/B/test.img\" />";
    String result = "<img src=\"http://" + host + "/download/A/B/test.img\" />";
    assertEquals(result, ((WebUtils)celUtils).replaceInternalWithExternalLinks(test, host));
  }
  
  @Test
  public void testReplaceInternalWithExternalLinks_img_dotInternal() {
    String host = "www.bla.com";
    String test = "<img src=\"../../download/A/B/test.img\" />";
    String result = "<img src=\"http://" + host + "/download/A/B/test.img\" />";
    assertEquals(result, ((WebUtils)celUtils).replaceInternalWithExternalLinks(test, host));
  }
  
  @Test
  public void testReplaceInternalWithExternalLinks_href_nothingToReplace() {
    String host = "www.bla.com";
    String test = "<a href=\"http://www.bla.com/download/A/B/test.pdf\" >bla</a>";
    assertEquals(test, ((WebUtils)celUtils).replaceInternalWithExternalLinks(test, host));
    test = "<a href=\"http://www.bla.com/skin/A/B/test.css\" >bla</a>";
    assertEquals(test, ((WebUtils)celUtils).replaceInternalWithExternalLinks(test, host));
    test = "<a href=\"http://www.bla.com/view/A/B\" >bla</a>";
    assertEquals(test, ((WebUtils)celUtils).replaceInternalWithExternalLinks(test, host));
    test = "<a href=\"http://www.bla.com/edit/A/B\" >bla</a>";
    assertEquals(test, ((WebUtils)celUtils).replaceInternalWithExternalLinks(test, host));
  }
  
  @Test
  public void testReplaceInternalWithExternalLinks_href_newInternal() {
    String host = "www.bla.com";
    String test = "<a href=\"/download/A/B/test.pdf\" >bla</a>";
    String result = "<a href=\"http://" + host + "/download/A/B/test.pdf\" >bla</a>";
    assertEquals(result, ((WebUtils)celUtils).replaceInternalWithExternalLinks(test, host));
    test = "<a href=\"/skin/A/B/test.css\" >bla</a>";
    result = "<a href=\"http://" + host + "/skin/A/B/test.css\" >bla</a>";
    assertEquals(result, ((WebUtils)celUtils).replaceInternalWithExternalLinks(test, host));
    test = "<a href=\"/view/A/B\" >bla</a>";
    result = "<a href=\"http://" + host + "/view/A/B\" >bla</a>";
    assertEquals(result, ((WebUtils)celUtils).replaceInternalWithExternalLinks(test, host));
    test = "<a href=\"/edit/A/B\" >bla</a>";
    result = "<a href=\"http://" + host + "/edit/A/B\" >bla</a>";
    assertEquals(result, ((WebUtils)celUtils).replaceInternalWithExternalLinks(test, host));
  }
  
  @Test
  public void testReplaceInternalWithExternalLinks_href_dotInternal() {
    String host = "www.bla.com";
    String test = "<a href=\"../../download/A/B/test.pdf\" >bla</a>";
    String result = "<a href=\"http://" + host + "/download/A/B/test.pdf\" >bla</a>";
    assertEquals(result, ((WebUtils)celUtils).replaceInternalWithExternalLinks(test, host));
    test = "<a href=\"../../skin/A/B/test.css\" >bla</a>";
    result = "<a href=\"http://" + host + "/skin/A/B/test.css\" >bla</a>";
    assertEquals(result, ((WebUtils)celUtils).replaceInternalWithExternalLinks(test, host));
    test = "<a href=\"../../download/A/B\" >bla</a>";
    result = "<a href=\"http://" + host + "/download/A/B\" >bla</a>";
    assertEquals(result, ((WebUtils)celUtils).replaceInternalWithExternalLinks(test, host));
    test = "<a href=\"../../download/A/B\" >bla</a>";
    result = "<a href=\"http://" + host + "/download/A/B\" >bla</a>";
    assertEquals(result, ((WebUtils)celUtils).replaceInternalWithExternalLinks(test, host));
  }

  @Test
  public void getSiblingMenuItem_previous() throws XWikiException {
    context.setDatabase("siblingPrevious");
    String mItemFullName = "mySpace.myMenuItemDoc";
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName(mItemFullName);
    context.setDoc(doc);
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
    BaseObject menuItem2 = new BaseObject();
    String nextFullName = "mySpace.Doc2";
    menuItem2.setName(nextFullName);
    menuItem2.setClassName("Celements2.MenuItem");
    XWikiDocument nextDoc = new XWikiDocument();
    nextDoc.setFullName(nextFullName);
    nextDoc.setObject("Celements2.MenuItem", 0, menuItem2);
    expect(wiki.getDocument(eq(mItemFullName), same(context))).andReturn(doc).anyTimes();
    expect(wiki.getDocument(eq(mItemFullName), same(context))).andReturn(doc
      ).anyTimes();
    expect(wiki.getDocument(eq(prevFullName), same(context))).andReturn(prevDoc
        ).anyTimes();
    expect(wiki.getDocument(eq(nextFullName), same(context))).andReturn(nextDoc
      ).anyTimes();
    XWikiRightService rightServiceMock = createMock(XWikiRightService.class);
    expect(wiki.getRightService()).andReturn(rightServiceMock).anyTimes();
    expect(rightServiceMock.hasAccessLevel(eq("view"), isA(String.class),
        isA(String.class), same(context))).andReturn(true).anyTimes();
    expect(mockXStore.searchDocumentsNames((String)anyObject(), eq(0), eq(0),
        (List<?>)anyObject(), same(context))).andReturn(new ArrayList<String>()
            ).anyTimes();
    List<BaseObject> menuItemList = new ArrayList<BaseObject>();
    menuItemList.add(menuItem1);
    menuItemList.add(menuItemItemDoc);
    menuItemList.add(menuItem2);
    expect(injected_TreeNodeService.getSubMenuItemsForParent(eq(""), eq("mySpace"),
        isA(InternalRightsFilter.class))).andReturn(menuItemList).atLeastOnce();
    replayAll(rightServiceMock);
    BaseObject prevMenuItem = ((WebUtils) celUtils).getSiblingMenuItem(mItemFullName,
        true, context);
    assertEquals("MySpace.Doc1 MenuItem expected.", menuItem1, prevMenuItem);
    verifyAll(rightServiceMock);
  }

  @Test
  public void getSiblingMenuItem_next() throws XWikiException {
    context.setDatabase("siblingPrevious");
    String mItemFullName = "mySpace.myMenuItemDoc";
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName(mItemFullName);
    context.setDoc(doc);
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
    BaseObject menuItem2 = new BaseObject();
    String nextFullName = "mySpace.Doc2";
    menuItem2.setName(nextFullName);
    menuItem2.setClassName("Celements2.MenuItem");
    XWikiDocument nextDoc = new XWikiDocument();
    nextDoc.setFullName(nextFullName);
    nextDoc.setObject("Celements2.MenuItem", 0, menuItem2);
    expect(wiki.getDocument(eq(mItemFullName), same(context))).andReturn(doc).anyTimes();
    expect(wiki.getDocument(eq(mItemFullName), same(context))).andReturn(doc
      ).anyTimes();
    expect(wiki.getDocument(eq(prevFullName), same(context))).andReturn(prevDoc
        ).anyTimes();
    expect(wiki.getDocument(eq(nextFullName), same(context))).andReturn(nextDoc
      ).anyTimes();
    XWikiRightService rightServiceMock = createMock(XWikiRightService.class);
    expect(wiki.getRightService()).andReturn(rightServiceMock).anyTimes();
    expect(rightServiceMock.hasAccessLevel(eq("view"), isA(String.class),
        isA(String.class), same(context))).andReturn(true).anyTimes();
    expect(mockXStore.searchDocumentsNames((String)anyObject(), eq(0), eq(0),
        (List<?>)anyObject(), same(context))).andReturn(new ArrayList<String>()
            ).anyTimes();
    List<BaseObject> menuItemList = new ArrayList<BaseObject>();
    menuItemList.add(menuItem1);
    menuItemList.add(menuItemItemDoc);
    menuItemList.add(menuItem2);
    expect(injected_TreeNodeService.getSubMenuItemsForParent(eq(""), eq("mySpace"),
        isA(InternalRightsFilter.class))).andReturn(menuItemList).atLeastOnce();
    replayAll(rightServiceMock);
    BaseObject nextMenuItem = ((WebUtils) celUtils).getSiblingMenuItem(mItemFullName,
        false, context);
    assertEquals("MySpace.Doc2 MenuItem expected.", menuItem2, nextMenuItem);
    verifyAll(rightServiceMock);
  }
  
  @Test
  public void getSiblingMenuItem_next_docNotInContextSpace() throws XWikiException {
    context.setDatabase("siblingPrevious");
    String mItemFullName = "mySpace.myMenuItemDoc";
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName(mItemFullName);
    context.setDoc(new XWikiDocument("otherSpace", "otherDoc"));
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
    BaseObject menuItem2 = new BaseObject();
    String nextFullName = "mySpace.Doc2";
    menuItem2.setName(nextFullName);
    menuItem2.setClassName("Celements2.MenuItem");
    XWikiDocument nextDoc = new XWikiDocument();
    nextDoc.setFullName(nextFullName);
    nextDoc.setObject("Celements2.MenuItem", 0, menuItem2);
    expect(wiki.getDocument(eq(mItemFullName), same(context))).andReturn(doc).anyTimes();
    expect(wiki.getDocument(eq(mItemFullName), same(context))).andReturn(doc
      ).anyTimes();
    expect(wiki.getDocument(eq(prevFullName), same(context))).andReturn(prevDoc
        ).anyTimes();
    expect(wiki.getDocument(eq(nextFullName), same(context))).andReturn(nextDoc
      ).anyTimes();
    XWikiRightService rightServiceMock = createMock(XWikiRightService.class);
    expect(wiki.getRightService()).andReturn(rightServiceMock).anyTimes();
    expect(rightServiceMock.hasAccessLevel(eq("view"), isA(String.class),
        isA(String.class), same(context))).andReturn(true).anyTimes();
    expect(mockXStore.searchDocumentsNames((String)anyObject(), eq(0), eq(0),
        (List<?>)anyObject(), same(context))).andReturn(new ArrayList<String>()
            ).anyTimes();
    List<BaseObject> menuItemList = new ArrayList<BaseObject>();
    menuItemList.add(menuItem1);
    menuItemList.add(menuItemItemDoc);
    menuItemList.add(menuItem2);
    expect(injected_TreeNodeService.getSubMenuItemsForParent(eq(""), eq("mySpace"),
        isA(InternalRightsFilter.class))).andReturn(menuItemList).atLeastOnce();
    replayAll(rightServiceMock);
    BaseObject nextMenuItem = ((WebUtils) celUtils).getSiblingMenuItem(mItemFullName,
        false, context);
    assertEquals("MySpace.Doc2 MenuItem expected.", menuItem2, nextMenuItem);
    verifyAll(rightServiceMock);
  }

  @Test
  public void testGetDocSection_empty() throws XWikiException {
    String fullName = "Space.DocName";
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(doc.getFullName()).andReturn(fullName).anyTimes();
    expect(wiki.getDocument(eq(fullName), same(context))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(context))).andReturn(new XWikiDocument()
      ).atLeastOnce();
    replay(wiki, doc);
    assertNull(((WebUtils)WebUtils.getInstance()).getDocSection("(?=<table)", fullName, 1,
        context));
    verify(wiki, doc);
  }
  
  @Test
  public void testGetDocSection_first() throws XWikiException {
    String fullName = "Space.DocName";
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(doc.getFullName()).andReturn(fullName).anyTimes();
    expect(wiki.getDocument(eq(fullName), same(context))).andReturn(doc).atLeastOnce();
    XWikiDocument tdoc = new XWikiDocument();
    tdoc.setContent("abc<table>blabla</table><table>abc</table>");
    expect(doc.getTranslatedDocument(same(context))).andReturn(tdoc).atLeastOnce();
    XWikiRenderingEngine renderer = createMock(XWikiRenderingEngine.class);
    expect(wiki.getRenderingEngine()).andReturn(renderer).atLeastOnce();
    expect(renderer.renderText(eq("{pre}abc{/pre}"), eq(context.getDoc()), same(context))
        ).andReturn("abc").atLeastOnce();
    replay(wiki, doc, renderer);
    assertEquals("abc", ((WebUtils)WebUtils.getInstance()).getDocSection("(?=<table)",
        fullName, 1, context));
    verify(wiki, doc, renderer);
  }
  
  @Test
  public void testGetDocSection_firstEmptyRTE() throws XWikiException {
    String fullName = "Space.DocName";
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(doc.getFullName()).andReturn(fullName).anyTimes();
    XWikiDocument tdoc = new XWikiDocument();
    tdoc.setContent("<p></p>  <br /> \n<table>blabla</table><table>abc</table>");
    expect(wiki.getDocument(eq(fullName), same(context))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(context))).andReturn(tdoc).atLeastOnce();
    XWikiRenderingEngine renderer = createMock(XWikiRenderingEngine.class);
    expect(wiki.getRenderingEngine()).andReturn(renderer).atLeastOnce();
    expect(renderer.renderText(eq("{pre}<table>blabla</table>{/pre}"), eq(context.getDoc()), same(context))).andReturn("<table>blabla</table>").atLeastOnce();
    replay(wiki, doc, renderer);
    assertEquals("<table>blabla</table>", ((WebUtils)WebUtils.getInstance()).getDocSection("(?=<table)", fullName, 1, context));
    verify(wiki, doc, renderer);
  }
  
  @Test
  public void testGetDocSection_middle() throws XWikiException {
    String fullName = "Space.DocName";
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(doc.getFullName()).andReturn(fullName).anyTimes();
    XWikiDocument tdoc = new XWikiDocument();
    tdoc.setContent("abc<table>blabla</table><table>abc</table>");
    expect(wiki.getDocument(eq(fullName), same(context))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(context))).andReturn(tdoc).atLeastOnce();
    XWikiRenderingEngine renderer = createMock(XWikiRenderingEngine.class);
    expect(wiki.getRenderingEngine()).andReturn(renderer).atLeastOnce();
    expect(renderer.renderText(eq("{pre}<table>blabla</table>{/pre}"), eq(context.getDoc()), same(context))).andReturn("<table>blabla</table>").atLeastOnce();
    replay(wiki, doc, renderer);
    assertEquals("<table>blabla</table>", ((WebUtils)WebUtils.getInstance()).getDocSection("(?=<table)", fullName, 2, context));
    verify(wiki, doc, renderer);
  }
  
  @Test
  public void testGetDocSection_last() throws XWikiException {
    String fullName = "Space.DocName";
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(doc.getFullName()).andReturn(fullName).anyTimes();
    XWikiDocument tdoc = new XWikiDocument();
    tdoc.setContent("abc<table>blabla</table><table>abc</table>");
    expect(wiki.getDocument(eq(fullName), same(context))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(context))).andReturn(tdoc).atLeastOnce();
    XWikiRenderingEngine renderer = createMock(XWikiRenderingEngine.class);
    expect(wiki.getRenderingEngine()).andReturn(renderer).atLeastOnce();
    expect(renderer.renderText(eq("{pre}<table>abc</table>{/pre}"), eq(context.getDoc()), same(context))).andReturn("<table>abc</table>").atLeastOnce();
    replay(wiki, doc, renderer);
    assertEquals("<table>abc</table>", ((WebUtils)WebUtils.getInstance()).getDocSection("(?=<table)", fullName, 3, context));
    verify(wiki, doc, renderer);
  }

  @Test
  public void testCountSections_empty() throws XWikiException {
    String fullName = "Space.DocName";
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(doc.getFullName()).andReturn(fullName).anyTimes();
    expect(wiki.getDocument(eq(fullName), same(context))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(context))).andReturn(new XWikiDocument()
      ).atLeastOnce();
    replay(wiki, doc);
    assertEquals(0, WebUtils.getInstance().countSections("", fullName, context));
    verify(wiki, doc);
  }

  @Test
  public void testCountSections_one() throws XWikiException {
    String fullName = "Space.DocName";
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(doc.getFullName()).andReturn(fullName).anyTimes();
    XWikiDocument tdoc = new XWikiDocument();
    tdoc.setContent("<table>blabla</table>");
    expect(wiki.getDocument(eq(fullName), same(context))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(context))).andReturn(tdoc).atLeastOnce();
    replay(wiki, doc);
    assertEquals(1, WebUtils.getInstance().countSections("(?=<table)", fullName, context));
    verify(wiki, doc);
  }
  
  @Test
  public void testCountSections_emptyRTEStart() throws XWikiException {
    String fullName = "Space.DocName";
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(doc.getFullName()).andReturn(fullName).anyTimes();
    XWikiDocument tdoc = new XWikiDocument();
    tdoc.setContent("<p> </p>\n<table>blabla</table>");
    expect(wiki.getDocument(eq(fullName), same(context))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(context))).andReturn(tdoc).atLeastOnce();
    replay(wiki, doc);
    assertEquals(1, WebUtils.getInstance().countSections("(?=<table)", fullName, context));
    verify(wiki, doc);
  }
  
  @Test
  public void testCountSections_several() throws XWikiException {
    String fullName = "Space.DocName";
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(doc.getFullName()).andReturn(fullName).anyTimes();
    XWikiDocument tdoc = new XWikiDocument();
    tdoc.setContent("abc<table>blabla</table><table>abc</table>");
    expect(wiki.getDocument(eq(fullName), same(context))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(context))).andReturn(tdoc).atLeastOnce();
    replay(wiki, doc);
    assertEquals(3, WebUtils.getInstance().countSections("(?=<table)", fullName, context));
    verify(wiki, doc);
  }
  
  @Test
  public void testGetDocSectionAsJSON() throws XWikiException {
    String fullName = "Space.DocName";
    XWikiDocument doc = createMock(XWikiDocument.class);
    expect(doc.getFullName()).andReturn(fullName).anyTimes();
    XWikiDocument tdoc = new XWikiDocument();
    tdoc.setContent("abc<table>blabla</table><table>abc</table>");
    expect(wiki.getDocument(eq(fullName), same(context))).andReturn(doc).atLeastOnce();
    expect(doc.getTranslatedDocument(same(context))).andReturn(tdoc).atLeastOnce();
    XWikiRenderingEngine renderer = createMock(XWikiRenderingEngine.class);
    expect(wiki.getRenderingEngine()).andReturn(renderer).atLeastOnce();
    expect(renderer.renderText(eq("{pre}<table>blabla</table>{/pre}"),
        eq(context.getDoc()), same(context))).andReturn("<table>blabla</table>"
            ).atLeastOnce();
    replay(wiki, doc, renderer);
    String json ="[{\"content\" : \"<table>blabla</table>\", \"section\" : 2," +
    		" \"sectionNr\" : 3}]";
    assertEquals(json, WebUtils.getInstance().getDocSectionAsJSON("(?=<table)", fullName,
        2, context));
    verify(wiki, doc, renderer);
  }

  @Test
  public void testGetSectionNr_negative() {
    assertEquals(1, ((WebUtils)WebUtils.getInstance()).getSectionNr(-3, 5));
  }
  
  @Test
  public void testGetSectionNr_zero() {
    assertEquals(1, ((WebUtils)WebUtils.getInstance()).getSectionNr(0, 5));
  }
  
  @Test
  public void testGetSectionNr_validNr() {
    assertEquals(3, ((WebUtils)WebUtils.getInstance()).getSectionNr(3, 5));
  }

  @Test
  public void testGetSectionNr_toHighNr() {
    assertEquals(5, ((WebUtils)WebUtils.getInstance()).getSectionNr(8, 5));
  }

  @Test
  public void testGetAdminLanguage_defaultToDocLanguage() throws XWikiException {
    context.setLanguage("de");
    String userName = "XWiki.MyUser";
    context.setUser(userName);
    DocumentReference userDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "MyUser");
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    expect(wiki.getDocument(eq(userDocRef), same(context))).andReturn(userDoc);
    expect(wiki.getWebPreference(eq("admin_language"), eq("de"), same(context))
        ).andReturn("de");
    replayAll();
    assertEquals("de", celUtils.getAdminLanguage(context));
    verifyAll();
  }

  @Test
  public void testGetAdminLanguage_contextUser() throws XWikiException {
    context.setLanguage("de");
    String userName = "XWiki.MyUser";
    context.setUser(userName);
    DocumentReference userDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "MyUser");
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    DocumentReference xwikiUserClassRef = new DocumentReference(context.getDatabase(),
        "XWiki", "XWikiUsers");
    BaseObject userObj = new BaseObject();
    userObj.setXClassReference(xwikiUserClassRef);
    userObj.setStringValue("admin_language", "fr");
    userDoc.setXObject(0, userObj);
    expect(wiki.getDocument(eq(userDocRef), same(context))).andReturn(userDoc);
    replayAll();
    assertEquals("fr", celUtils.getAdminLanguage(context));
    verifyAll();
  }

  @Test
  public void testGetAdminLanguage_notContextUser() throws XWikiException {
    context.setLanguage("de");
    String userName = "XWiki.MyUser";
    context.setUser("XWiki.NotMyUser");
    DocumentReference userDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "MyUser");
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    DocumentReference xwikiUserClassRef = new DocumentReference(context.getDatabase(),
        "XWiki", "XWikiUsers");
    BaseObject userObj = new BaseObject();
    userObj.setXClassReference(xwikiUserClassRef);
    userObj.setStringValue("admin_language", "fr");
    userDoc.setXObject(0, userObj);
    expect(wiki.getDocument(eq(userDocRef), same(context))).andReturn(userDoc);
    replayAll();
    assertEquals("fr", celUtils.getAdminLanguage(userName, context));
    verifyAll();
  }

  @Test
  public void testGetAdminLanguage_defaultToWebPreferences() throws XWikiException {
    context.setLanguage("de");
    String userName = "XWiki.MyUser";
    context.setUser("XWiki.NotMyUser");
    expect(wiki.getWebPreference(eq("admin_language"), isA(String.class), same(context))
        ).andReturn("en");
    DocumentReference userDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "MyUser");
    XWikiDocument userDoc = new XWikiDocument(userDocRef);
    DocumentReference xwikiUserClassRef = new DocumentReference(context.getDatabase(),
        "XWiki", "XWikiUsers");
    BaseObject userObj = new BaseObject();
    userObj.setXClassReference(xwikiUserClassRef);
    userObj.setStringValue("admin_language", "");
    userDoc.setXObject(0, userObj);
    expect(wiki.getDocument(eq(userDocRef), same(context))).andReturn(userDoc);
    replayAll();
    assertEquals("en", celUtils.getAdminLanguage(userName, context));
    verifyAll();
  }

  @Test
  public void testGetDocumentParentsList() throws XWikiException {
    String fullName = "mySpace.MyDoc";
    String parent1 = "mySpace.Parent1";
    String parent2 = "mySpace.Parent2";
    expect(wiki.exists(eq(fullName), same(context))).andReturn(true);
    XWikiDocument myDoc = new XWikiDocument();
    myDoc.setFullName(fullName);
    myDoc.setParent(parent1);
    expect(wiki.getDocument(eq(fullName), same(context))).andReturn(myDoc);
    expect(wiki.exists(eq(parent1), same(context))).andReturn(true);
    XWikiDocument myParent1 = new XWikiDocument();
    myParent1.setFullName(parent1);
    myParent1.setParent(parent2);
    expect(wiki.getDocument(eq(parent1), same(context))).andReturn(myParent1);
    expect(wiki.exists(eq(parent2), same(context))).andReturn(true);
    XWikiDocument myParent2 = new XWikiDocument();
    myParent2.setFullName(parent2);
    myParent2.setParent("");
    expect(wiki.getDocument(eq(parent2), same(context))).andReturn(myParent2);
    List<String> docParentsList = Arrays.asList(fullName, parent1, parent2);
    replay(wiki);
    assertEquals(docParentsList, celUtils.getDocumentParentsList(fullName, true,
        context));
    verify(wiki);
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

  //*****************************************************************
  //*                  H E L P E R  - M E T H O D S                 *
  //*****************************************************************/

  private BaseObject createNavObj(int toLevel, XWikiDocument doc) {
    BaseObject navObj = new BaseObject();
    navObj.setClassName(Navigation.NAVIGATION_CONFIG_CLASS);
    navObj.setStringValue("menu_element_name", "mainMenu");
    navObj.setIntValue("to_hierarchy_level", toLevel);
    navObj.setName(doc.getFullName());
    navObj.setDocumentReference(doc.getDocumentReference());
    return navObj;
  }

  final private class TestMockUser extends XWikiUser {
    
    private HashSet<String> groups = new HashSet<String>();

    public TestMockUser(String user) {
      super(user);
    }

    final void setTestGroups(HashSet<String> groups) {
      this.groups = groups;
    }

    @Override
    public boolean isUserInGroup(String groupName, XWikiContext context)
        throws XWikiException {
      return groups.contains(groupName);
    }
    
  }

  private void replayAll(Object ... mocks) {
    replay(mockStore, wiki, mockXStore, injected_TreeNodeService);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(mockStore, wiki, mockXStore, injected_TreeNodeService);
    verify(mocks);
  }

}
