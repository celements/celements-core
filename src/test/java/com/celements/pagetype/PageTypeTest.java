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
package com.celements.pagetype;

import static junit.framework.Assert.*;
import static org.easymock.EasyMock.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.pagetype.cmd.PageTypeCommand;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.XWikiRequest;

public class PageTypeTest extends AbstractBridgedComponentTestCase {

  private static final String TEST_PAGE_TYPE_DOC = "TestPageType";
  private static final String TEST_PAGE_TYPE_SPACE = "PageTypes";
  private static final String TEST_PAGE_TYPE_FN = TEST_PAGE_TYPE_SPACE + "."
                                                + TEST_PAGE_TYPE_DOC;

  private XWikiContext context;
  private XWiki mockWiki;
  private XWikiRequest request;
  private PageType pageType;

  @Before
  public void setUp_PageTypeTest() throws Exception {
    context = getContext();
    mockWiki = createMock(XWiki.class);
    context.setWiki(mockWiki);
    request = createMock(XWikiRequest.class);
    context.setRequest(request);
    pageType = new PageType(TEST_PAGE_TYPE_FN);
  }

  @Test
  public void testShowFrame_missing_properties() throws XWikiException {
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    //No PageType Object prepared -> Default PageType is RichText
    expect(mockWiki.exists(eq(TEST_PAGE_TYPE_FN), eq(context))
      ).andReturn(true);
    XWikiDocument templateDoc = new XWikiDocument();
    expect(mockWiki.getDocument(eq(TEST_PAGE_TYPE_FN), eq(context))
      ).andReturn(templateDoc).anyTimes();
    // missing page_type_properties object may not lead to NPE
    // this is the tests focus.
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayAll();
    boolean showFrame = pageType.showFrame(context);
    assertTrue("default value of showFrame must be true.", showFrame);
    verifyAll();
  }
  
  @Test
  public void testGetRenderTemplateForRenderMode_missing_properties(
      ) throws XWikiException {
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    //No PageType Object prepared -> Default PageType is RichText
    expect(mockWiki.exists(eq(TEST_PAGE_TYPE_FN), eq(context))
      ).andReturn(true);
    XWikiDocument templateDoc = new XWikiDocument();
    expect(mockWiki.getDocument(eq(TEST_PAGE_TYPE_FN), eq(context))
      ).andReturn(templateDoc).anyTimes();
    // missing page_type_properties object may not lead to NPE
    // this is the tests focus.
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayAll();
    String templName = pageType.getRenderTemplateForRenderMode("view", context);
    assertNull("default value of renderTemplate must be null.", templName);
    verifyAll();
  }
  
  @Test
  public void testGetRenderTemplateForRenderMode_localTemplate() throws XWikiException {
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    //No PageType Object prepared -> Default PageType is RichText
    String ptName = TEST_PAGE_TYPE_FN;
    expect(mockWiki.exists(eq(ptName), eq(context))
      ).andReturn(true).anyTimes();
    XWikiDocument pageTypeDoc = new XWikiDocument();
    BaseObject ptPropertiesObj = new BaseObject();
    ptPropertiesObj.setClassName(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS);
    String ptViewTemplate = "Templates.TestPageView";
    ptPropertiesObj.setStringValue("page_view", ptViewTemplate);
    pageTypeDoc.setObject(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS, 0, ptPropertiesObj);
    expect(mockWiki.getDocument(eq(ptName), eq(context))
      ).andReturn(pageTypeDoc).anyTimes();
    // missing page_type_properties object may not lead to NPE
    // this is the tests focus.
    expect(mockWiki.exists(eq(ptViewTemplate), eq(context))
      ).andReturn(true).anyTimes();
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayAll();
    String templName = pageType.getRenderTemplateForRenderMode("view", context);
    assertNotNull("if local template exists renderTemplate must NOT return null.",
        templName);
    assertEquals(ptViewTemplate, templName);
    verifyAll();
  }
  
  @Test
  public void testGetRenderTemplateForRenderMode_celements2webTemplate(
      ) throws XWikiException {
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    //No PageType Object prepared -> Default PageType is RichText
    String ptName = TEST_PAGE_TYPE_FN;
    expect(mockWiki.exists(eq(ptName), eq(context))
      ).andReturn(true).anyTimes();
    XWikiDocument pageTypeDoc = new XWikiDocument();
    BaseObject ptPropertiesObj = new BaseObject();
    ptPropertiesObj.setClassName(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS);
    String ptViewTemplate = "Templates.TestPageView";
    ptPropertiesObj.setStringValue("page_view", ptViewTemplate);
    pageTypeDoc.setObject(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS, 0, ptPropertiesObj);
    expect(mockWiki.getDocument(eq(ptName), eq(context))
      ).andReturn(pageTypeDoc).anyTimes();
    // missing page_type_properties object may not lead to NPE
    // this is the tests focus.
    expect(mockWiki.exists(eq(ptViewTemplate), eq(context))
      ).andReturn(false).anyTimes();
    expect(mockWiki.exists(eq("celements2web:" + ptViewTemplate), eq(context))
      ).andReturn(true).anyTimes();
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayAll();
    String templName = pageType.getRenderTemplateForRenderMode("view", context);
    assertNotNull("if celements2web template exists renderTemplate must NOT return null.",
        templName);
    assertEquals("celements2web:" + ptViewTemplate, templName);
    verifyAll();
  }
  
  @Test
  public void testGetRenderTemplateForRenderMode_diskTemplate(
      ) throws XWikiException {
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    //No PageType Object prepared -> Default PageType is RichText
    String ptName = TEST_PAGE_TYPE_FN;
    expect(mockWiki.exists(eq(ptName), eq(context))
      ).andReturn(true).anyTimes();
    XWikiDocument pageTypeDoc = new XWikiDocument();
    BaseObject ptPropertiesObj = new BaseObject();
    ptPropertiesObj.setClassName(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS);
    String ptViewTemplate = "Templates.TestPageView";
    ptPropertiesObj.setStringValue("page_view", ptViewTemplate);
    pageTypeDoc.setObject(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS, 0, ptPropertiesObj);
    expect(mockWiki.getDocument(eq(ptName), eq(context))
      ).andReturn(pageTypeDoc).anyTimes();
    // missing page_type_properties object may not lead to NPE
    // this is the tests focus.
    expect(mockWiki.exists(eq(ptViewTemplate), eq(context))
      ).andReturn(false).anyTimes();
    expect(mockWiki.exists(eq("celements2web:" + ptViewTemplate), eq(context))
      ).andReturn(false).anyTimes();
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayAll();
    String templName = pageType.getRenderTemplateForRenderMode("view", context);
    assertNotNull("if disk template exists renderTemplate must NOT return null.",
        templName);
    assertEquals(":" + ptViewTemplate, templName);
    verifyAll();
  }
  
  @Test
  public void testGetRenderTemplateForRenderMode_celements2webTemplate_skipLocal(
      ) throws XWikiException {
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    //No PageType Object prepared -> Default PageType is RichText
    String ptName = TEST_PAGE_TYPE_FN;
    expect(mockWiki.exists(eq(ptName), eq(context))
      ).andReturn(true).anyTimes();
    XWikiDocument pageTypeDoc = new XWikiDocument();
    BaseObject ptPropertiesObj = new BaseObject();
    ptPropertiesObj.setClassName(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS);
    String ptViewTemplate = "celements2web:Templates.TestPageView";
    ptPropertiesObj.setStringValue("page_view", ptViewTemplate);
    pageTypeDoc.setObject(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS, 0, ptPropertiesObj);
    expect(mockWiki.getDocument(eq(ptName), eq(context))
      ).andReturn(pageTypeDoc).anyTimes();
    // missing page_type_properties object may not lead to NPE
    // this is the tests focus.
    expect(mockWiki.exists(eq(ptViewTemplate), eq(context))
      ).andReturn(true).anyTimes();
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayAll();
    String templName = pageType.getRenderTemplateForRenderMode("view", context);
    assertNotNull("if celements2web template exists renderTemplate must NOT return null.",
        templName);
    assertEquals(ptViewTemplate, templName);
    verifyAll();
  }
  
  @Test
  public void testGetRenderTemplateForRenderMode_diskTemplate_skipLocal(
      ) throws XWikiException {
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    //No PageType Object prepared -> Default PageType is RichText
    String ptName = TEST_PAGE_TYPE_FN;
    expect(mockWiki.exists(eq(ptName), eq(context))
      ).andReturn(true).anyTimes();
    XWikiDocument pageTypeDoc = new XWikiDocument();
    BaseObject ptPropertiesObj = new BaseObject();
    ptPropertiesObj.setClassName(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS);
    String ptViewTemplate = "celements2web:Templates.TestPageView";
    ptPropertiesObj.setStringValue("page_view", ptViewTemplate);
    pageTypeDoc.setObject(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS, 0, ptPropertiesObj);
    expect(mockWiki.getDocument(eq(ptName), eq(context))
      ).andReturn(pageTypeDoc).anyTimes();
    // missing page_type_properties object may not lead to NPE
    // this is the tests focus.
    expect(mockWiki.exists(eq(ptViewTemplate), eq(context))
      ).andReturn(false).anyTimes();
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayAll();
    String templName = pageType.getRenderTemplateForRenderMode("view", context);
    assertNotNull("if disk template exists renderTemplate must NOT return null.",
        templName);
    assertEquals(":Templates.TestPageView", templName);
    verifyAll();
  }
  
  @Test
  public void testGetCategoryString_missing_properties(
      ) throws XWikiException {
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    //No PageType Object prepared -> Default PageType is RichText
    expect(mockWiki.exists(eq(TEST_PAGE_TYPE_FN), eq(context))
      ).andReturn(true);
    XWikiDocument templateDoc = new XWikiDocument();
    expect(mockWiki.getDocument(eq(TEST_PAGE_TYPE_FN), eq(context))
      ).andReturn(templateDoc).anyTimes();
    // missing page_type_properties object may not lead to NPE
    // this is the tests focus.
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayAll();
    String category = pageType.getCategoryString(context);
    assertEquals("default value of categoryString must be empty string.", "", category);
    verifyAll();
  }
  
  @Test
  public void testGetCategoryString() throws XWikiException {
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setClassName(PageTypeCommand.PAGE_TYPE_CLASSNAME);
    pageTypeObj.setStringValue("page_type", TEST_PAGE_TYPE_DOC);
    doc.setObject(PageTypeCommand.PAGE_TYPE_CLASSNAME, 0, pageTypeObj);
    expect(mockWiki.exists(eq(TEST_PAGE_TYPE_FN), eq(context))
      ).andReturn(true).anyTimes();
    XWikiDocument testPageTypeDoc = new XWikiDocument();
    expect(mockWiki.getDocument(eq(TEST_PAGE_TYPE_FN), eq(context))
      ).andReturn(testPageTypeDoc).anyTimes();
    BaseObject paeTypePropObj = new BaseObject();
    paeTypePropObj.setStringValue("category", "myCat");
    testPageTypeDoc.setObject(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS, 0, paeTypePropObj);
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayAll();
    assertEquals("myCat", pageType.getCategoryString(context));
    verifyAll();
  }
  
  @Test
  public void testGetCategories_missing_properties() throws XWikiException {
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    //No PageType Object prepared -> Default PageType is RichText
    expect(mockWiki.exists(eq(TEST_PAGE_TYPE_FN), eq(context))
      ).andReturn(true).anyTimes();
    XWikiDocument templateDoc = new XWikiDocument();
    expect(mockWiki.getDocument(eq(TEST_PAGE_TYPE_FN), eq(context))
      ).andReturn(templateDoc).anyTimes();
    // missing page_type_properties object may not lead to NPE
    // this is the tests focus.
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayAll();
    assertEquals(Collections.emptyList(), pageType.getCategories(context));
    verifyAll();
  }
  
  @Test
  public void testGetCategories_oneCat() throws XWikiException {
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setClassName(PageTypeCommand.PAGE_TYPE_CLASSNAME);
    pageTypeObj.setStringValue("page_type", TEST_PAGE_TYPE_DOC);
    doc.setObject(PageTypeCommand.PAGE_TYPE_CLASSNAME, 0, pageTypeObj);
    expect(mockWiki.exists(eq(TEST_PAGE_TYPE_FN), eq(context))
      ).andReturn(true).anyTimes();
    XWikiDocument testPageTypeDoc = new XWikiDocument();
    expect(mockWiki.getDocument(eq(TEST_PAGE_TYPE_FN), eq(context))
      ).andReturn(testPageTypeDoc).anyTimes();
    BaseObject paeTypePropObj = new BaseObject();
    paeTypePropObj.setStringValue("category", "myCat");
    testPageTypeDoc.setObject(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS, 0, paeTypePropObj);
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayAll();
    assertEquals(Arrays.asList("myCat"), pageType.getCategories(context));
    verifyAll();
  }
  
  @Test
  public void testGetCategories_multipleCat() throws XWikiException {
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setClassName(PageTypeCommand.PAGE_TYPE_CLASSNAME);
    pageTypeObj.setStringValue("page_type", TEST_PAGE_TYPE_DOC);
    doc.setObject(PageTypeCommand.PAGE_TYPE_CLASSNAME, 0, pageTypeObj);
    expect(mockWiki.exists(eq(TEST_PAGE_TYPE_FN), eq(context))
      ).andReturn(true).anyTimes();
    XWikiDocument testPageTypeDoc = new XWikiDocument();
    expect(mockWiki.getDocument(eq(TEST_PAGE_TYPE_FN), eq(context))
      ).andReturn(testPageTypeDoc).anyTimes();
    BaseObject paeTypePropObj = new BaseObject();
    paeTypePropObj.setStringValue("category", "myCat,secondCat");
    testPageTypeDoc.setObject(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS, 0, paeTypePropObj);
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayAll();
    assertEquals(Arrays.asList("myCat", "secondCat"), pageType.getCategories(context));
    verifyAll();
  }

  @Test
  public void testGetPrettyName() throws Exception {
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setClassName(PageTypeCommand.PAGE_TYPE_CLASSNAME);
    pageTypeObj.setStringValue("page_type", TEST_PAGE_TYPE_DOC);
    doc.setObject(PageTypeCommand.PAGE_TYPE_CLASSNAME, 0, pageTypeObj);
    expect(mockWiki.exists(eq(TEST_PAGE_TYPE_FN), eq(context))
      ).andReturn(true).anyTimes();
    XWikiDocument testPageTypeDoc = new XWikiDocument();
    expect(mockWiki.getDocument(eq(TEST_PAGE_TYPE_FN), eq(context))
      ).andReturn(testPageTypeDoc).anyTimes();
    BaseObject paeTypePropObj = new BaseObject();
    paeTypePropObj.setStringValue("type_name", "myPrettyTypeName");
    testPageTypeDoc.setObject(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS, 0, paeTypePropObj);
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayAll();
    assertEquals("myPrettyTypeName", pageType.getPrettyName(context));
    verifyAll();
  }

  @Test
  public void testGetPrettyName_noPropertyObj() throws Exception {
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    BaseObject pageTypeObj = new BaseObject();
    pageTypeObj.setClassName(PageTypeCommand.PAGE_TYPE_CLASSNAME);
    pageTypeObj.setStringValue("page_type", TEST_PAGE_TYPE_DOC);
    doc.setObject(PageTypeCommand.PAGE_TYPE_CLASSNAME, 0, pageTypeObj);
    expect(mockWiki.exists(eq(TEST_PAGE_TYPE_FN), eq(context))
      ).andReturn(true).anyTimes();
    XWikiDocument testPageTypeDoc = new XWikiDocument();
    expect(mockWiki.getDocument(eq(TEST_PAGE_TYPE_FN), eq(context))
      ).andReturn(testPageTypeDoc).anyTimes();
    expect(request.get(eq("template"))).andReturn("").anyTimes();
    replayAll();
    assertEquals("", pageType.getPrettyName(context));
    verifyAll();
  }

  @Test
  public void testHasPageTitle_npe() throws Exception {
    expect(mockWiki.exists(eq(TEST_PAGE_TYPE_FN), same(context))).andReturn(false);
    DocumentReference testPageTypeCentralDocRef = new DocumentReference("celements2web",
        TEST_PAGE_TYPE_SPACE, TEST_PAGE_TYPE_DOC);
    expect(mockWiki.getDocument(eq("celements2web:" + TEST_PAGE_TYPE_FN), same(context))
        ).andReturn(new XWikiDocument(testPageTypeCentralDocRef));
    replayAll();
    assertFalse(pageType.hasPageTitle(context));
    verifyAll();
  }

  @Test
  public void testHasPageTitle_true() throws Exception {
    expect(mockWiki.exists(eq(TEST_PAGE_TYPE_FN), same(context))).andReturn(true);
    DocumentReference testPageTypeDocRef = new DocumentReference(context.getDatabase(),
        TEST_PAGE_TYPE_SPACE, TEST_PAGE_TYPE_DOC);
    XWikiDocument testPTdoc = new XWikiDocument(testPageTypeDocRef);
    BaseObject ptPropObj = new BaseObject();
    ptPropObj.setXClassReference(getPTPropClassRef(context.getDatabase()));
    ptPropObj.setIntValue("haspagetitle", 1);
    testPTdoc.addXObject(ptPropObj);
    expect(mockWiki.getDocument(eq(TEST_PAGE_TYPE_FN), same(context))
        ).andReturn(testPTdoc);
    replayAll();
    assertTrue(pageType.hasPageTitle(context));
    verifyAll();
  }

  //*****************************************************************
  //*                  H E L P E R  - M E T H O D S                 *
  //*****************************************************************/

  private DocumentReference getPTPropClassRef(String wikiName) {
    return new PageTypeClasses().getPageTypePropertiesClassRef(wikiName);
  }
  
  private void replayAll(Object ... mocks) {
    replay(mockWiki, request);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(mockWiki, request);
    verify(mocks);
  }

}
