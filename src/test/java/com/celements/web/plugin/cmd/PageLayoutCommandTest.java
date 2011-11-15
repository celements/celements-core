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
package com.celements.web.plugin.cmd;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.inheritor.FieldInheritor;
import com.celements.inheritor.InheritorFactory;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.store.XWikiStoreInterface;

public class PageLayoutCommandTest extends AbstractBridgedComponentTestCase{

  private XWikiContext context;
  private XWiki xwiki;
  private PageLayoutCommand plCmd;

  @Before
  public void setUp_PageLayoutCommandTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    plCmd = new PageLayoutCommand();
  }

  @Test
  public void testGetPageLayoutHQL() {
    assertEquals("select doc.space, pl.prettyname"
        + " from XWikiDocument as doc, BaseObject obj,"
        + " Celements.PageLayoutPropertiesClass as pl"
        + " where doc.fullName = obj.name"
        + " and obj.className='Celements.PageLayoutPropertiesClass'"
        + " and pl.id.id=obj.id"
        + " order by pl.prettyname asc",
        plCmd.getPageLayoutHQL(false));
  }

  @Test
  public void testGetPageLayoutHQL_onlyActive() {
    assertEquals("select doc.space, pl.prettyname"
        + " from XWikiDocument as doc, BaseObject obj,"
        + " Celements.PageLayoutPropertiesClass as pl"
        + " where doc.fullName = obj.name"
        + " and obj.className='Celements.PageLayoutPropertiesClass'"
        + " and pl.id.id=obj.id"
        + " and pl.isActive = 1"
        + " order by pl.prettyname asc",
        plCmd.getPageLayoutHQL(true));
  }

  @Test
  public void testExtendToFullName() {
    assertEquals("Test.name", plCmd.extendToFullName("Test", "name"));
    assertEquals("Test.name", plCmd.extendToFullName("Test2", "Test.name"));
  }

  @Test
  public void testGetAllPageLayouts() throws Exception {
    List<Object> resultList = new ArrayList<Object>();
    resultList.add(new Object[] {"layout1Space","Layout 1 pretty name"});
    resultList.add(new Object[]  {"layout2Space","Layout 2 pretty name"});
    Capture<String> capturedHQL = new Capture<String>();
    expect(xwiki.search(capture(capturedHQL), same(context))).andReturn(resultList);
    replay(xwiki);
    Map<String, String> expectedPLmap = new HashMap<String, String>();
    expectedPLmap.put("layout1Space","Layout 1 pretty name");
    expectedPLmap.put("layout2Space","Layout 2 pretty name");
    assertEquals(expectedPLmap , plCmd.getAllPageLayouts(context));
    assertFalse("hql must not contain isActiv constrains.", capturedHQL.getValue(
        ).contains("pl.isActive"));
    verify(xwiki);
  }

  @Test
  public void testGetActivePageLyouts() throws Exception {
    List<Object> resultList = new ArrayList<Object>();
    resultList.add(new Object[] {"layout1Space","Layout 1 pretty name"});
    resultList.add(new Object[]  {"layout2Space","Layout 2 pretty name"});
    Capture<String> capturedHQL = new Capture<String>();
    expect(xwiki.search(capture(capturedHQL), same(context))).andReturn(resultList);
    replay(xwiki);
    Map<String, String> expectedPLmap = new HashMap<String, String>();
    expectedPLmap.put("layout1Space","Layout 1 pretty name");
    expectedPLmap.put("layout2Space","Layout 2 pretty name");
    assertEquals(expectedPLmap , plCmd.getActivePageLyouts(context));
    assertTrue("hql must contain isActiv constrains.", capturedHQL.getValue(
      ).contains("pl.isActive"));
    verify(xwiki);
  }

  @Test
  public void testGetPageLayoutForDoc_noLayoutSpace() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    plCmd.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String fullName = "mySpace.MyDocName";
    String layoutName = "MyPageLayout";
    FieldInheritor inheritor = createMock(FieldInheritor.class);
    expect(injectedInheritorFactory.getPageLayoutInheritor(eq(fullName), same(context))
        ).andReturn(inheritor);
    expect(inheritor.getStringValue(eq("page_layout"), (String)isNull())
      ).andReturn(layoutName);
    XWikiStoreInterface storeMock = createMock(XWikiStoreInterface.class);
    expect(xwiki.getStore()).andReturn(storeMock).anyTimes();
    expect(storeMock.search(isA(String.class), eq(0), eq(0), eq(Arrays.asList("mySpace")),
        same(context))).andReturn(Collections.emptyList()).anyTimes();
    replay(xwiki, storeMock, injectedInheritorFactory, inheritor);
    assertEquals(layoutName, plCmd.getPageLayoutForDoc(fullName, context));
    verify(xwiki, storeMock, injectedInheritorFactory, inheritor);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetPageLayoutForDoc_layoutSpace() throws Exception {
    InheritorFactory injectedInheritorFactory = createMock(InheritorFactory.class);
    plCmd.inject_TEST_InheritorFactory(injectedInheritorFactory);
    String fullName = "MyPageLayout.MyDocName";
    XWikiStoreInterface storeMock = createMock(XWikiStoreInterface.class);
    expect(xwiki.getStore()).andReturn(storeMock).anyTimes();
    List layoutPropDoc = Arrays.asList("MyPageLayout.WebHome");
    expect(storeMock.search(isA(String.class), eq(0), eq(0), eq(Arrays.asList(
        "MyPageLayout")), same(context))).andReturn(layoutPropDoc).anyTimes();
    replay(xwiki, storeMock, injectedInheritorFactory);
    assertEquals("CelLayoutEditor", plCmd.getPageLayoutForDoc(fullName, context));
    verify(xwiki, storeMock, injectedInheritorFactory);
  }

}
