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
package com.celements.pagetype.xobject;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.pagetype.IPageTypeClassConfig;
import com.celements.pagetype.PageType;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

public class XObjectPageTypeConfigTest extends AbstractComponentTest {

  private XObjectPageTypeConfig xObjPTconfig;
  private PageType pageTypeMock;
  private XWikiContext context;

  @Before
  public void setUp_XObjectPageTypeConfigTest() throws Exception {
    context = getContext();
    DocumentReference testPageTypeDocRef = new DocumentReference(context.getDatabase(), "PageTypes",
        "TestPageType");
    xObjPTconfig = new XObjectPageTypeConfig(testPageTypeDocRef);
    pageTypeMock = createMockAndAddToDefault(PageType.class);
    xObjPTconfig.pageType = pageTypeMock;
    expect(pageTypeMock.getConfigName(same(context))).andReturn("TestPageType").anyTimes();
  }

  @Test
  public void testGetCategories_noEmptyCategories() {
    expect(pageTypeMock.getCategories(same(context))).andReturn(Collections.<String>emptyList());
    replayDefault();
    assertEquals(Arrays.asList(""), xObjPTconfig.getCategories());
    verifyDefault();
  }

  @Test
  public void testGetCategories() {
    expect(pageTypeMock.getCategories(same(context))).andReturn(Arrays.asList("cellType"));
    replayDefault();
    assertEquals(Arrays.asList("cellType"), xObjPTconfig.getCategories());
    verifyDefault();
  }

  @Test
  public void testDisplayInFrameLayout_yes() {
    expect(pageTypeMock.showFrame(same(context))).andReturn(true);
    replayDefault();
    assertTrue(xObjPTconfig.displayInFrameLayout());
    verifyDefault();
  }

  @Test
  public void testDisplayInFrameLayout_no() {
    expect(pageTypeMock.showFrame(same(context))).andReturn(false);
    replayDefault();
    assertFalse(xObjPTconfig.displayInFrameLayout());
    verifyDefault();
  }

  @Test
  public void testGetName() {
    replayDefault();
    assertEquals("TestPageType", xObjPTconfig.getName());
    verifyDefault();
  }

  @Test
  public void testGetPrettyName() {
    String expectedPrettyName = "Test Page Type Pretty Name";
    expect(pageTypeMock.getPrettyName(same(context))).andReturn(expectedPrettyName);
    replayDefault();
    assertEquals(expectedPrettyName, xObjPTconfig.getPrettyName());
    verifyDefault();
  }

  @Test
  public void testHasPageTitle_yes() {
    expect(pageTypeMock.hasPageTitle(same(context))).andReturn(true);
    replayDefault();
    assertTrue(xObjPTconfig.hasPageTitle());
    verifyDefault();
  }

  @Test
  public void testHasPageTitle_no() {
    expect(pageTypeMock.hasPageTitle(same(context))).andReturn(false);
    replayDefault();
    assertFalse(xObjPTconfig.hasPageTitle());
    verifyDefault();
  }

  @Test
  public void testGetRenderTemplateForRenderMode_view() throws Exception {
    String expectedRenderTemplate = "Templates.TestPageTypeView";
    expect(pageTypeMock.getRenderTemplate(eq("view"), same(context))).andReturn(
        expectedRenderTemplate);
    replayDefault();
    assertEquals(expectedRenderTemplate, xObjPTconfig.getRenderTemplateForRenderMode("view"));
    verifyDefault();
  }

  @Test
  public void testGetRenderTemplateForRenderMode_edit() throws Exception {
    String expectedRenderTemplate = "Templates.TestPageTypeEdit";
    expect(pageTypeMock.getRenderTemplate(eq("edit"), same(context))).andReturn(
        expectedRenderTemplate);
    replayDefault();
    assertEquals(expectedRenderTemplate, xObjPTconfig.getRenderTemplateForRenderMode("edit"));
    verifyDefault();
  }

  @Test
  public void testIsVisible_yes() throws Exception {
    BaseObject testPageTypePropObj = new BaseObject();
    EntityReference pageTypePropClassRef = new DocumentReference(context.getDatabase(),
        IPageTypeClassConfig.PAGE_TYPE_PROPERTIES_CLASS_SPACE,
        IPageTypeClassConfig.PAGE_TYPE_PROPERTIES_CLASS_DOC);
    testPageTypePropObj.setXClassReference(pageTypePropClassRef);
    testPageTypePropObj.setIntValue("visible", 1);
    expect(pageTypeMock.getPageTypeProperties(same(context))).andReturn(testPageTypePropObj);
    replayDefault();
    assertTrue(xObjPTconfig.isVisible());
    verifyDefault();
  }

  @Test
  public void testIsVisible_no() throws Exception {
    BaseObject testPageTypePropObj = new BaseObject();
    EntityReference pageTypePropClassRef = new DocumentReference(context.getDatabase(),
        IPageTypeClassConfig.PAGE_TYPE_PROPERTIES_CLASS_SPACE,
        IPageTypeClassConfig.PAGE_TYPE_PROPERTIES_CLASS_DOC);
    testPageTypePropObj.setXClassReference(pageTypePropClassRef);
    testPageTypePropObj.setIntValue("visible", 0);
    expect(pageTypeMock.getPageTypeProperties(same(context))).andReturn(testPageTypePropObj);
    replayDefault();
    assertFalse(xObjPTconfig.isVisible());
    verifyDefault();
  }

  @Test
  public void test_getPrettyName_absent() {
    BaseObject testPageTypePropObj = new BaseObject();
    EntityReference pageTypePropClassRef = new DocumentReference(context.getDatabase(),
        IPageTypeClassConfig.PAGE_TYPE_PROPERTIES_CLASS_SPACE,
        IPageTypeClassConfig.PAGE_TYPE_PROPERTIES_CLASS_DOC);
    testPageTypePropObj.setXClassReference(pageTypePropClassRef);
    expect(pageTypeMock.getPageTypeProperties(same(context))).andReturn(testPageTypePropObj);
    replayDefault();
    assertFalse(xObjPTconfig.defaultTagName().isPresent());
    verifyDefault();
  }

  @Test
  public void test_getPrettyName_present() {
    String tagName = "abstract";
    BaseObject testPageTypePropObj = new BaseObject();
    EntityReference pageTypePropClassRef = new DocumentReference(context.getDatabase(),
        IPageTypeClassConfig.PAGE_TYPE_PROPERTIES_CLASS_SPACE,
        IPageTypeClassConfig.PAGE_TYPE_PROPERTIES_CLASS_DOC);
    testPageTypePropObj.setXClassReference(pageTypePropClassRef);
    testPageTypePropObj.setStringValue(IPageTypeClassConfig.PAGETYPE_PROP_TAG_NAME, tagName);
    expect(pageTypeMock.getPageTypeProperties(same(context))).andReturn(
        testPageTypePropObj).atLeastOnce();
    replayDefault();
    assertTrue(xObjPTconfig.defaultTagName().isPresent());
    assertEquals(tagName, xObjPTconfig.defaultTagName().get());
    verifyDefault();
  }

  @Test
  public void testIsVisible_NPE_no_object() {
    expect(pageTypeMock.getPageTypeProperties(same(context))).andReturn(null);
    replayDefault();
    assertFalse(xObjPTconfig.isVisible());
    verifyDefault();
  }

}
