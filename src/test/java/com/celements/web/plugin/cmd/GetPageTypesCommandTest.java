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
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.web.pagetype.PageTypeClasses;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class GetPageTypesCommandTest extends AbstractBridgedComponentTestCase {

  private GetPageTypesCommand gptCmd;
  private XWikiContext context;
  private XWiki xwiki;

  @Before
  public void setUp_GetPageTypesCommandTest() throws Exception {
    gptCmd = new GetPageTypesCommand();
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
  }

  @Test
  public void testGetPThql() {
    String pThql = gptCmd.getPThql(new HashSet<String>(Arrays.asList("myCat",
        "pageTypeCat", "CelCat")), false);
    assertTrue(pThql.startsWith("select doc.fullName "));
    assertTrue(pThql.contains(" from XWikiDocument as doc"));
    assertTrue(pThql.contains(", BaseObject as obj"));
    assertTrue(pThql.contains(", StringProperty as catName"));
    assertFalse(pThql.contains(", IntegerProperty as visible"));
    assertTrue(pThql.contains(" where doc.space='PageTypes' "));
    assertTrue(pThql.contains(" and doc.translation=0 "));
    assertTrue(pThql.contains(" and obj.name=doc.fullName "));
    assertTrue(pThql.contains(" and obj.className='Celements2.PageTypeProperties' "));
    assertTrue(pThql.contains(" and catName.id.id=obj.id "));
    assertTrue(pThql.contains(" and catName.id.name='category' "));
    assertTrue(pThql.contains(" and catName.value in ("));
    assertTrue(pThql.matches(".*?([^)]*'myCat'[^)]*).*"));
    assertTrue(pThql.matches(".*?([^)]*'pageTypeCat'[^)]*).*"));
    assertTrue(pThql.matches(".*?([^)]*'CelCat'[^)]*).*"));
    assertFalse(pThql.contains(" and visible.id.id=obj.id "));
    assertFalse(pThql.contains(" and visible.id.name='visible' "));
    assertFalse(pThql.contains(" and visible.id.value=1 "));
  }

  @Test
  public void testGetPThql_secondList() {
    String pThql = gptCmd.getPThql(new HashSet<String>(Arrays.asList("pageTypeCat")),
        false);
    assertTrue(pThql.contains("('pageTypeCat')"));
  }

  @Test
  public void testGetPThql_emptyList() {
    Set<String> emptyList = Collections.emptySet();
    String pThql = gptCmd.getPThql(emptyList, false) + " ";
    assertFalse(pThql.contains(", StringProperty as catName"));
    assertFalse(pThql.contains(" and catName.id.id=obj.id "));
    assertFalse(pThql.contains(" and catName.id.name='category' "));
    assertFalse(pThql.contains(" and catName.value in ("));
    assertFalse(pThql.contains(", where"));
    assertTrue(pThql.startsWith("select doc.fullName "));
    assertTrue(pThql.contains(" from XWikiDocument as doc"));
    assertTrue(pThql.contains(", BaseObject as obj"));
    assertTrue(pThql.contains(" where doc.space='PageTypes' "));
    assertTrue(pThql.contains(" and doc.translation=0 "));
    assertTrue(pThql.contains(" and obj.name=doc.fullName "));
    assertTrue(pThql.contains(" and obj.className='Celements2.PageTypeProperties' "));
  }


  @Test
  public void testGetPThql_onlyVisible() {
    String pThql = gptCmd.getPThql(new HashSet<String>(Arrays.asList("myCat",
        "pageTypeCat", "CelCat")), true);
    assertTrue(pThql.startsWith("select doc.fullName "));
    assertTrue(pThql.contains(" from XWikiDocument as doc"));
    assertTrue(pThql.contains(", BaseObject as obj"));
    assertTrue(pThql.contains(", StringProperty as catName"));
    assertTrue(pThql.contains(", IntegerProperty as visible"));
    assertTrue(pThql.contains(" where doc.space='PageTypes' "));
    assertTrue(pThql.contains(" and doc.translation=0 "));
    assertTrue(pThql.contains(" and obj.name=doc.fullName "));
    assertTrue(pThql.contains(" and obj.className='Celements2.PageTypeProperties' "));
    assertTrue(pThql.contains(" and catName.id.id=obj.id "));
    assertTrue(pThql.contains(" and catName.id.name='category' "));
    assertTrue(pThql.contains(" and catName.value in ("));
    assertTrue(pThql.matches(".*?([^)]*'myCat'[^)]*).*"));
    assertTrue(pThql.matches(".*?([^)]*'pageTypeCat'[^)]*).*"));
    assertTrue(pThql.matches(".*?([^)]*'CelCat'[^)]*).*"));
    assertTrue(pThql.contains(" and visible.id.id=obj.id "));
    assertTrue(pThql.contains(" and visible.id.name='visible' "));
    assertTrue(pThql.contains(" and visible.id.value=1 "));
  }

  @Test
  public void testGetPThql_emptyList_onlyVisible() {
    Set<String> emptyList = Collections.emptySet();
    String pThql = gptCmd.getPThql(emptyList, true) + " ";
    assertFalse(pThql.contains(", StringProperty as catName"));
    assertFalse(pThql.contains(" and catName.id.id=obj.id "));
    assertFalse(pThql.contains(" and catName.id.name='category' "));
    assertFalse(pThql.contains(" and catName.value in ("));
    assertTrue(pThql.startsWith("select doc.fullName "));
    assertTrue(pThql.contains(" from XWikiDocument as doc"));
    assertTrue(pThql.contains(", BaseObject as obj"));
    assertTrue(pThql.contains(", IntegerProperty as visible"));
    assertTrue(pThql.contains(" where doc.space='PageTypes' "));
    assertTrue(pThql.contains(" and doc.translation=0 "));
    assertTrue(pThql.contains(" and obj.name=doc.fullName "));
    assertTrue(pThql.contains(" and obj.className='Celements2.PageTypeProperties' "));
    assertTrue(pThql.contains(" and visible.id.id=obj.id "));
    assertTrue(pThql.contains(" and visible.id.name='visible' "));
    assertTrue(pThql.contains(" and visible.id.value=1 "));
  }

  @Test
  public void testGetPThql_emptyCategory() {
    String pThql = gptCmd.getPThql(new HashSet<String>(Arrays.asList("", "pageTypeCat")),
        false) + " ";
    assertTrue(pThql.startsWith("select doc.fullName "));
    assertTrue(pThql.contains(" from XWikiDocument as doc"));
    assertTrue(pThql.contains(", BaseObject as obj"));
    assertFalse(pThql.contains(", StringProperty as catName"));
    assertFalse(pThql.contains(", IntegerProperty as visible"));
    assertTrue(pThql.contains(" where doc.space='PageTypes' "));
    assertTrue(pThql.contains(" and doc.translation=0 "));
    assertTrue(pThql.contains(" and obj.name=doc.fullName "));
    assertTrue(pThql.contains(" and obj.className='Celements2.PageTypeProperties' "));
    assertFalse(pThql.contains(" and catName.id.id=obj.id "));
    assertFalse(pThql.contains(" and catName.id.name='category' "));
    assertFalse(pThql.contains(" and catName.value in ("));
    assertFalse(pThql.contains("('', 'pageTypeCat')"));
    assertFalse(pThql.contains(" and visible.id.id=obj.id "));
    assertFalse(pThql.contains(" and visible.id.name='visible' "));
    assertFalse(pThql.contains(" and visible.id.value=1 "));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testGetPageTypesForCategories() throws XWikiException {
    List expectedList = Arrays.asList("PageTypes.RichText", "PageTypes.Code");
    Set<String> catList = new HashSet<String>(Arrays.asList("pageTypeCat"));
    expect(xwiki.search(eq(gptCmd.getPThql(catList, false)), same(context))).andReturn(
        expectedList).times(2);
    replay(xwiki);
    List<String> resultList = gptCmd.getPageTypesForCategories(catList, false, context);
    assertEquals(expectedList, resultList);
    verify(xwiki);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testGetPageTypesForCategories_emptyCategory() throws XWikiException {
    List allPTList = Arrays.asList("PageTypes.RichText", "PageTypes.Code");
    Set<String> catList = new HashSet<String>(Arrays.asList(""));
    expect(xwiki.search(eq(gptCmd.getPThql(catList, false)), same(context))).andReturn(
        allPTList).times(2);
    expect(xwiki.exists(eq("PageTypes.RichText"), same(context))).andReturn(true
      ).anyTimes();
    expect(xwiki.exists(eq("PageTypes.Code"), same(context))).andReturn(true
      ).anyTimes();
    XWikiDocument ptRTE = new XWikiDocument();
    BaseObject ptRTEProp = new BaseObject();
    ptRTEProp.setClassName(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS);
    ptRTEProp.setStringValue("category", "");
    ptRTE.addObject(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS, ptRTEProp);
    expect(xwiki.getDocument(eq("PageTypes.RichText"), same(context))).andReturn(ptRTE 
      ).anyTimes();
    XWikiDocument ptCode = new XWikiDocument();
    BaseObject ptCodeProp = new BaseObject();
    ptCodeProp.setClassName(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS);
    ptCodeProp.setStringValue("category", "cellType");
    ptCode.addObject(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS, ptCodeProp);
    expect(xwiki.getDocument(eq("PageTypes.Code"), same(context))).andReturn(ptCode
      ).anyTimes();
    replay(xwiki);
    List expectedList = Arrays.asList("PageTypes.RichText");
    List<String> resultList = gptCmd.getPageTypesForCategories(catList, false, context);
    assertEquals(expectedList, resultList);
    verify(xwiki);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testGetPageTypesForCategories_emptyCategory_nullValue() throws XWikiException {
    List allPTList = Arrays.asList("PageTypes.RichText", "PageTypes.Code");
    Set<String> catList = new HashSet<String>(Arrays.asList(""));
    expect(xwiki.search(eq(gptCmd.getPThql(catList, false)), same(context))).andReturn(
        allPTList).times(2);
    expect(xwiki.exists(eq("PageTypes.RichText"), same(context))).andReturn(true
      ).anyTimes();
    expect(xwiki.exists(eq("PageTypes.Code"), same(context))).andReturn(true
      ).anyTimes();
    XWikiDocument ptRTE = new XWikiDocument();
    BaseObject ptRTEProp = new BaseObject();
    ptRTEProp.setClassName(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS);
    //ptRTEProp.setStringValue("category", null);
    ptRTE.addObject(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS, ptRTEProp);
    expect(xwiki.getDocument(eq("PageTypes.RichText"), same(context))).andReturn(ptRTE 
      ).anyTimes();
    XWikiDocument ptCode = new XWikiDocument();
    BaseObject ptCodeProp = new BaseObject();
    ptCodeProp.setClassName(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS);
    ptCodeProp.setStringValue("category", "cellType");
    ptCode.addObject(PageTypeClasses.PAGE_TYPE_PROPERTIES_CLASS, ptCodeProp);
    expect(xwiki.getDocument(eq("PageTypes.Code"), same(context))).andReturn(ptCode
      ).anyTimes();
    replay(xwiki);
    List expectedList = Arrays.asList("PageTypes.RichText");
    List<String> resultList = gptCmd.getPageTypesForCategories(catList, false, context);
    assertEquals(expectedList, resultList);
    verify(xwiki);
  }

  @Test
  public void testGetPageTypesForCategories_Exception() throws XWikiException {
    Set<String> catList = new HashSet<String>(Arrays.asList("pageTypeCat"));
    expect(xwiki.search(eq(gptCmd.getPThql(catList, false)), same(context))).andThrow(
        new XWikiException()).atLeastOnce();
    replay(xwiki);
    try {
      List<String> resultList = gptCmd.getPageTypesForCategories(catList, false, context);
      assertTrue("Expecting empty list.", resultList.isEmpty());
    } catch (Exception exp) {
      fail("expecting no (XWiki)Exception being thrown.");
    }
    verify(xwiki);
  }

}
