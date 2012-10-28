package com.celements.pagetype.service;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.IPageTypeProviderRole;
import com.celements.pagetype.PageTypeClasses;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.xobject.XObjectPageTypeConfig;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class PageTypeServiceTest extends AbstractBridgedComponentTestCase {

  private static final String X_OBJECT_PROVIDER = "XObjectProvider";

  private XWikiContext context;
  private XWiki xwiki;
  private PageTypeService ptService;

  private IPageTypeProviderRole xObjectProviderMock;

  @Before
  public void setUp_PageTypeServiceTest() throws Exception {
    ptService = new PageTypeService();
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    ptService.pageTypeProviders = new HashMap<String, IPageTypeProviderRole>();
    xObjectProviderMock = createMock(IPageTypeProviderRole.class);
    ptService.pageTypeProviders.put(X_OBJECT_PROVIDER, xObjectProviderMock);
  }

  @Test
  public void testComponentRegistration() {
    assertNotNull(Utils.getComponent(IPageTypeRole.class));
  }

  @Test
  public void testGetPageTypeConfig() {
    PageTypeReference testPageTypeRef = new PageTypeReference("TestPageType",
        X_OBJECT_PROVIDER, Arrays.asList(""));
    expect(xObjectProviderMock.getPageTypes()).andReturn(Arrays.asList(testPageTypeRef));
    IPageTypeConfig testPTconfig = new XObjectPageTypeConfig("PageTypes.TestPageType");
    expect(xObjectProviderMock.getPageTypeByReference(eq(testPageTypeRef))).andReturn(
        testPTconfig);
    replayAll();
    IPageTypeConfig ptConfig = ptService.getPageTypeConfig("TestPageType");
    assertEquals("TestPageType", ptConfig.getName());
    verifyAll();
  }

  @Test
  public void testGetPageTypesConfigNamesForCategories() {
    Set<String> catList = new HashSet<String>(Arrays.asList("PageType", ""));
    PageTypeReference testPageTypeRef = new PageTypeReference("TestPageType",
        X_OBJECT_PROVIDER, Arrays.asList(""));
    expect(xObjectProviderMock.getPageTypes()).andReturn(Arrays.asList(testPageTypeRef));
    replayAll();
    List<String> ptConfigNames = ptService.getPageTypesConfigNamesForCategories(catList,
        false);
    assertTrue(ptConfigNames.size() == 1);
    assertEquals("TestPageType", ptConfigNames.get(0));
    verifyAll();
  }

  @Test
  public void testGetPageTypeRefsForCategories_cat_visible() throws Exception {
    Set<String> catList = new HashSet<String>(Arrays.asList("PageType", ""));
    PageTypeReference testPageTypeRef = new PageTypeReference("TestPageType",
        X_OBJECT_PROVIDER, Arrays.asList(""));
    expect(xObjectProviderMock.getPageTypes()).andReturn(Arrays.asList(testPageTypeRef));
    IPageTypeConfig testPTconfig = new XObjectPageTypeConfig("PageTypes.TestPageType");
    expect(xObjectProviderMock.getPageTypeByReference(eq(testPageTypeRef))).andReturn(
        testPTconfig);
    expect(xwiki.exists(eq("PageTypes.TestPageType"), same(context))).andReturn(true);
    DocumentReference testPageTypeDocRef = new DocumentReference(context.getDatabase(),
        "PageTypes", "TestPageType");
    XWikiDocument testPageTypeDoc = new XWikiDocument(testPageTypeDocRef);
    BaseObject testPageTypePropObj = new BaseObject();
    EntityReference pageTypePropClassRef = new DocumentReference(context.getDatabase(),
        PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS_SPACE,
        PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS_DOC);
    testPageTypePropObj.setXClassReference(pageTypePropClassRef);
    testPageTypeDoc.addXObject(testPageTypePropObj);
    testPageTypePropObj.setIntValue("visible", 1);
    expect(xwiki.getDocument(eq("PageTypes.TestPageType"), same(context))).andReturn(
        testPageTypeDoc);
    replayAll();
    List<PageTypeReference> pageTypeRefs = ptService.getPageTypeRefsForCategories(catList,
        true);
    assertTrue(pageTypeRefs.size() == 1);
    assertEquals("TestPageType", pageTypeRefs.get(0).getConfigName());
    verifyAll();
  }

  @Test
  public void testGetPageTypeRefsForCategories_cat_NOTvisible() throws Exception {
    Set<String> catList = new HashSet<String>(Arrays.asList("PageType", ""));
    PageTypeReference testPageTypeRef = new PageTypeReference("TestPageType",
        X_OBJECT_PROVIDER, Arrays.asList(""));
    expect(xObjectProviderMock.getPageTypes()).andReturn(Arrays.asList(testPageTypeRef));
    IPageTypeConfig testPTconfig = new XObjectPageTypeConfig("PageTypes.TestPageType");
    expect(xObjectProviderMock.getPageTypeByReference(eq(testPageTypeRef))).andReturn(
        testPTconfig);
    expect(xwiki.exists(eq("PageTypes.TestPageType"), same(context))).andReturn(true);
    DocumentReference testPageTypeDocRef = new DocumentReference(context.getDatabase(),
        "PageTypes", "TestPageType");
    XWikiDocument testPageTypeDoc = new XWikiDocument(testPageTypeDocRef);
    BaseObject testPageTypePropObj = new BaseObject();
    EntityReference pageTypePropClassRef = new DocumentReference(context.getDatabase(),
        PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS_SPACE,
        PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS_DOC);
    testPageTypePropObj.setXClassReference(pageTypePropClassRef);
    testPageTypeDoc.addXObject(testPageTypePropObj);
    testPageTypePropObj.setIntValue("visible", 0);
    expect(xwiki.getDocument(eq("PageTypes.TestPageType"), same(context))).andReturn(
        testPageTypeDoc);
    replayAll();
    List<PageTypeReference> pageTypeRefs = ptService.getPageTypeRefsForCategories(catList,
        true);
    assertTrue(pageTypeRefs.isEmpty());
    verifyAll();
  }

  @Test
  public void testGetPageTypeRefsByConfigNames() {
    Set<String> catList = new HashSet<String>(Arrays.asList("PageType", ""));
    PageTypeReference testPageTypeRef = new PageTypeReference("TestPageType",
        X_OBJECT_PROVIDER, Arrays.asList(""));
    expect(xObjectProviderMock.getPageTypes()).andReturn(Arrays.asList(testPageTypeRef));
    replayAll();
    Set<PageTypeReference> pageTypeRefs = ptService.getPageTypeRefsForCategories(catList);
    assertTrue(pageTypeRefs.size() == 1);
    assertTrue(pageTypeRefs.contains(testPageTypeRef));
    verifyAll();
  }

  @Test
  public void testGetPageTypeRefsForCategories_cat_emptyType() {
    Set<String> catList = new HashSet<String>(Arrays.asList("", "PageTypes"));
    PageTypeReference richTextRef = new PageTypeReference("RichText", X_OBJECT_PROVIDER,
        Arrays.asList(""));
    PageTypeReference testCellTypeRef = new PageTypeReference("testCellPageType",
        X_OBJECT_PROVIDER, Arrays.asList("cellType"));
    expect(xObjectProviderMock.getPageTypes()).andReturn(Arrays.asList(richTextRef,
        testCellTypeRef));
    replayAll();
    Set<PageTypeReference> ptResult = ptService.getPageTypeRefsForCategories(catList);
    Set<PageTypeReference> expectedPageTypes = new HashSet<PageTypeReference>(
        Arrays.asList(richTextRef));
    assertEquals(expectedPageTypes, ptResult);
    verifyAll();
  }

  @Test
  public void testGetPageTypeRefsForCategories_cat_PageTypes() {
    Set<String> catList = new HashSet<String>(Arrays.asList("", "PageTypes"));
    PageTypeReference richTextRef = new PageTypeReference("RichText", X_OBJECT_PROVIDER,
        Arrays.asList("PageTypes"));
    PageTypeReference testCellTypeRef = new PageTypeReference("testCellPageType",
        X_OBJECT_PROVIDER, Arrays.asList("cellType"));
    expect(xObjectProviderMock.getPageTypes()).andReturn(Arrays.asList(richTextRef,
        testCellTypeRef));
    replayAll();
    Set<PageTypeReference> ptResult = ptService.getPageTypeRefsForCategories(catList);
    Set<PageTypeReference> expectedPageTypes = new HashSet<PageTypeReference>(
        Arrays.asList(richTextRef));
    assertEquals(expectedPageTypes, ptResult);
    verifyAll();
  }

  private void replayAll(Object ... mocks) {
    replay(xwiki, xObjectProviderMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, xObjectProviderMock);
    verify(mocks);
  }

}
