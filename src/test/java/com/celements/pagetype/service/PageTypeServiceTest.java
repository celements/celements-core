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
package com.celements.pagetype.service;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.pagetype.IPageTypeClassConfig;
import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.IPageTypeProviderRole;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.xobject.XObjectPageTypeConfig;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.StringClass;
import com.xpn.xwiki.web.Utils;

public class PageTypeServiceTest extends AbstractComponentTest {

  private static final String MOCK_PROVIDER = "MockProvider";

  private PageTypeService ptService;

  private IPageTypeProviderRole providerMock;

  @Before
  public void prepareTest() throws Exception {
    super.setUp();
    providerMock = registerComponentMock(IPageTypeProviderRole.class, MOCK_PROVIDER);
    ptService = (PageTypeService) Utils.getComponent(IPageTypeRole.class);
    ptService.pageTypeProviders.clear();
    ptService.pageTypeProviders.put(MOCK_PROVIDER, providerMock);

  }

  @Test
  public void testComponentRegistration() {
    assertNotNull(Utils.getComponent(IPageTypeRole.class));
  }

  @Test
  public void testGetPageTypeConfig_NPE() {
    replayDefault();
    assertNull(ptService.getPageTypeConfig(null));
    verifyDefault();
  }

  @Test
  public void testGetPageTypeConfig_noPageType_NPE() {
    expect(providerMock.getPageTypes()).andReturn(Collections.<PageTypeReference>emptyList());
    replayDefault();
    IPageTypeConfig ptConfig = ptService.getPageTypeConfig("TestPageType");
    assertNull(ptConfig);
    verifyDefault();
  }

  @Test
  public void testGetPageTypeConfig() {
    PageTypeReference testPageTypeRef = new PageTypeReference("TestPageType", MOCK_PROVIDER,
        Arrays.asList(""));
    expect(providerMock.getPageTypes()).andReturn(Arrays.asList(testPageTypeRef));
    IPageTypeConfig testPTconfig = new XObjectPageTypeConfig("PageTypes.TestPageType");
    expect(providerMock.getPageTypeByReference(eq(testPageTypeRef))).andReturn(testPTconfig);
    replayDefault();
    IPageTypeConfig ptConfig = ptService.getPageTypeConfig("TestPageType");
    assertEquals("TestPageType", ptConfig.getName());
    verifyDefault();
  }

  @Test
  public void testGetPageTypesConfigNamesForCategories() {
    Set<String> catList = new HashSet<>(Arrays.asList("PageType", ""));
    PageTypeReference testPageTypeRef = new PageTypeReference("TestPageType", MOCK_PROVIDER,
        Arrays.asList(""));
    expect(providerMock.getPageTypes()).andReturn(Arrays.asList(testPageTypeRef));
    replayDefault();
    List<String> ptConfigNames = ptService.getPageTypesConfigNamesForCategories(catList, false);
    assertTrue(ptConfigNames.size() == 1);
    assertEquals("TestPageType", ptConfigNames.get(0));
    verifyDefault();
  }

  @Test
  public void testGetPageTypeRefsForCategories_cat_visible() throws Exception {
    Set<String> catList = new HashSet<>(Arrays.asList("PageType", ""));
    PageTypeReference testPageTypeRef = new PageTypeReference("TestPageType", MOCK_PROVIDER,
        Arrays.asList(""));
    expect(providerMock.getPageTypes()).andReturn(Arrays.asList(testPageTypeRef));
    IPageTypeConfig testPTconfig = new XObjectPageTypeConfig("PageTypes.TestPageType");
    expect(providerMock.getPageTypeByReference(eq(testPageTypeRef))).andReturn(testPTconfig);
    expect(getWikiMock().exists(eq("PageTypes.TestPageType"), same(getContext()))).andReturn(true);
    DocumentReference testPageTypeDocRef = new DocumentReference(getContext().getDatabase(),
        "PageTypes", "TestPageType");
    XWikiDocument testPageTypeDoc = new XWikiDocument(testPageTypeDocRef);
    BaseObject testPageTypePropObj = new BaseObject();
    EntityReference pageTypePropClassRef = new DocumentReference(getContext().getDatabase(),
        IPageTypeClassConfig.PAGE_TYPE_PROPERTIES_CLASS_SPACE,
        IPageTypeClassConfig.PAGE_TYPE_PROPERTIES_CLASS_DOC);
    testPageTypePropObj.setXClassReference(pageTypePropClassRef);
    testPageTypeDoc.addXObject(testPageTypePropObj);
    testPageTypePropObj.setIntValue("visible", 1);
    expect(getWikiMock().getDocument(eq("PageTypes.TestPageType"), same(getContext()))).andReturn(
        testPageTypeDoc);
    replayDefault();
    List<PageTypeReference> pageTypeRefs = ptService.getPageTypeRefsForCategories(catList, true);
    assertTrue(pageTypeRefs.size() == 1);
    assertEquals("TestPageType", pageTypeRefs.get(0).getConfigName());
    verifyDefault();
  }

  @Test
  public void testGetPageTypeRefsForCategories_cat_NOTvisible() throws Exception {
    Set<String> catList = new HashSet<>(Arrays.asList("PageType", ""));
    PageTypeReference testPageTypeRef = new PageTypeReference("TestPageType", MOCK_PROVIDER,
        Arrays.asList(""));
    expect(providerMock.getPageTypes()).andReturn(Arrays.asList(testPageTypeRef));
    IPageTypeConfig testPTconfig = new XObjectPageTypeConfig("PageTypes.TestPageType");
    expect(providerMock.getPageTypeByReference(eq(testPageTypeRef))).andReturn(testPTconfig);
    expect(getWikiMock().exists(eq("PageTypes.TestPageType"), same(getContext()))).andReturn(true);
    DocumentReference testPageTypeDocRef = new DocumentReference(getContext().getDatabase(),
        "PageTypes", "TestPageType");
    XWikiDocument testPageTypeDoc = new XWikiDocument(testPageTypeDocRef);
    BaseObject testPageTypePropObj = new BaseObject();
    EntityReference pageTypePropClassRef = new DocumentReference(getContext().getDatabase(),
        IPageTypeClassConfig.PAGE_TYPE_PROPERTIES_CLASS_SPACE,
        IPageTypeClassConfig.PAGE_TYPE_PROPERTIES_CLASS_DOC);
    testPageTypePropObj.setXClassReference(pageTypePropClassRef);
    testPageTypeDoc.addXObject(testPageTypePropObj);
    testPageTypePropObj.setIntValue("visible", 0);
    expect(getWikiMock().getDocument(eq("PageTypes.TestPageType"), same(getContext()))).andReturn(
        testPageTypeDoc);
    replayDefault();
    List<PageTypeReference> pageTypeRefs = ptService.getPageTypeRefsForCategories(catList, true);
    assertTrue(pageTypeRefs.isEmpty());
    verifyDefault();
  }

  @Test
  public void testGetPageTypeRefsByConfigNames() {
    Set<String> catList = new HashSet<>(Arrays.asList("PageType", ""));
    PageTypeReference testPageTypeRef = new PageTypeReference("TestPageType", MOCK_PROVIDER,
        Arrays.asList(""));
    expect(providerMock.getPageTypes()).andReturn(Arrays.asList(testPageTypeRef));
    replayDefault();
    Set<PageTypeReference> pageTypeRefs = ptService.getPageTypeRefsForCategories(catList);
    assertTrue(pageTypeRefs.size() == 1);
    assertTrue(pageTypeRefs.contains(testPageTypeRef));
    verifyDefault();
  }

  @Test
  public void testGetPageTypeRefsForCategories_cat_emptyType() {
    Set<String> catList = new HashSet<>(Arrays.asList("", "PageTypes"));
    PageTypeReference richTextRef = new PageTypeReference("RichText", MOCK_PROVIDER, Arrays.asList(
        ""));
    PageTypeReference testCellTypeRef = new PageTypeReference("testCellPageType", MOCK_PROVIDER,
        Arrays.asList("cellType"));
    expect(providerMock.getPageTypes()).andReturn(Arrays.asList(richTextRef, testCellTypeRef));
    replayDefault();
    Set<PageTypeReference> ptResult = ptService.getPageTypeRefsForCategories(catList);
    Set<PageTypeReference> expectedPageTypes = new HashSet<>(Arrays.asList(richTextRef));
    assertEquals(expectedPageTypes, ptResult);
    verifyDefault();
  }

  @Test
  public void testGetPageTypeRefsForCategories_cat_PageTypes() {
    Set<String> catList = new HashSet<>(Arrays.asList("", "PageTypes"));
    PageTypeReference richTextRef = new PageTypeReference("RichText", MOCK_PROVIDER, Arrays.asList(
        "PageTypes"));
    PageTypeReference testCellTypeRef = new PageTypeReference("testCellPageType", MOCK_PROVIDER,
        Arrays.asList("cellType"));
    expect(providerMock.getPageTypes()).andReturn(Arrays.asList(richTextRef, testCellTypeRef));
    replayDefault();
    Set<PageTypeReference> ptResult = ptService.getPageTypeRefsForCategories(catList);
    Set<PageTypeReference> expectedPageTypes = new HashSet<>(Arrays.asList(richTextRef));
    assertEquals(expectedPageTypes, ptResult);
    verifyDefault();
  }

  @Test
  public void test_setPageType_get() throws Exception {
    XWikiDocument doc = new XWikiDocument(new DocumentReference("xwikidb", "space", "doc"));
    PageTypeReference testPageTypeRef = new PageTypeReference("TestPageType", MOCK_PROVIDER,
        Arrays.asList(""));
    BaseObject obj = new BaseObject();
    obj.setXClassReference(getPageTypeClassRef());
    doc.addXObject(obj);

    BaseClass bClass = createBaseClassMock(getPageTypeClassRef());
    expect(bClass.get(eq(IPageTypeClassConfig.PAGE_TYPE_FIELD))).andReturn(
        new StringClass()).once();

    replayDefault();
    assertTrue(ptService.setPageType(doc, testPageTypeRef));
    verifyDefault();

    assertSame(obj, doc.getXObject(getPageTypeClassRef()));
    assertEquals(testPageTypeRef.getConfigName(), obj.getStringValue(
        IPageTypeClassConfig.PAGE_TYPE_FIELD));
  }

  @Test
  public void test_setPageType_create() throws Exception {
    XWikiDocument doc = new XWikiDocument(new DocumentReference("xwikidb", "space", "doc"));
    PageTypeReference testPageTypeRef = new PageTypeReference("TestPageType", MOCK_PROVIDER,
        Arrays.asList(""));

    BaseClass bClass = expectNewBaseObject(getPageTypeClassRef());
    expect(bClass.get(eq(IPageTypeClassConfig.PAGE_TYPE_FIELD))).andReturn(
        new StringClass()).once();

    replayDefault();
    assertTrue(ptService.setPageType(doc, testPageTypeRef));
    verifyDefault();

    assertNotNull(doc.getXObject(getPageTypeClassRef()));
    assertEquals(testPageTypeRef.getConfigName(), doc.getXObject(
        getPageTypeClassRef()).getStringValue(IPageTypeClassConfig.PAGE_TYPE_FIELD));
  }

  @Test
  public void test_setPageType_alreadySet() throws Exception {
    XWikiDocument doc = new XWikiDocument(new DocumentReference("xwikidb", "space", "doc"));
    PageTypeReference testPageTypeRef = new PageTypeReference("TestPageType", MOCK_PROVIDER,
        Arrays.asList(""));
    BaseObject obj = new BaseObject();
    obj.setXClassReference(getPageTypeClassRef());
    obj.setStringValue(IPageTypeClassConfig.PAGE_TYPE_FIELD, testPageTypeRef.getConfigName());
    doc.addXObject(obj);

    replayDefault();
    assertFalse(ptService.setPageType(doc, testPageTypeRef));
    verifyDefault();

    assertSame(obj, doc.getXObject(getPageTypeClassRef()));
    assertEquals(testPageTypeRef.getConfigName(), obj.getStringValue(
        IPageTypeClassConfig.PAGE_TYPE_FIELD));
  }

  @Test
  public void testGetAvailableTypesForCategory() throws Exception {
    PageTypeReference testPageTypeRef = new PageTypeReference("TestPageType", MOCK_PROVIDER,
        Arrays.asList(""));
    expect(providerMock.getPageTypes()).andReturn(Arrays.asList(testPageTypeRef));
    IPageTypeConfig testPTconfig = createMockAndAddToDefault(XObjectPageTypeConfig.class);
    expect(providerMock.getPageTypeByReference(eq(testPageTypeRef))).andReturn(testPTconfig);
    expect(testPTconfig.isVisible()).andReturn(true);
    replayDefault();
    List<String> pageTypes = ptService.getTypesForCategory("", true);
    verifyDefault();
    assertEquals("Fallback to deprecated names failed", 1, pageTypes.size());
    assertEquals(testPageTypeRef.getConfigName(), pageTypes.get(0));
  }

  private DocumentReference getPageTypeClassRef() {
    return Utils.getComponent(IPageTypeClassConfig.class).getPageTypeClassRef();
  }

}
