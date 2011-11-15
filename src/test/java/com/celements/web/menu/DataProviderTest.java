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
package com.celements.web.menu;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiRightService;

public class DataProviderTest extends AbstractBridgedComponentTestCase {

  private DataProvider pdInstance;
  private XWikiContext context;
  private XWiki xwiki;
  private XWikiRightService rightsMock;

  @Before
  public void setUp_DataProviderTest() throws Exception {
    pdInstance = DataProvider.getInstance();
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    rightsMock = createMock(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(rightsMock).anyTimes();
  }

  @Test
  public void testGetInstance_singleton() {
    assertNotNull(pdInstance);
    assertSame("ensure singleton", pdInstance, DataProvider.getInstance());
  }

  @Test
  public void testGetHeadersHQL() {
    assertTrue(pdInstance.getHeadersHQL().matches("select obj.name"
        + " from BaseObject obj.*?"
        + " where obj.className = 'Celements.MenuBarHeaderItemClass'.*?"));
  }

  @Test
  public void testHasview_notLocal_central_hasAccess() throws Exception {
    expect(xwiki.exists(eq("Celements.MenuBar"), same(context))
      ).andReturn(true).once();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
      eq("Celements.MenuBar"), same(context))).andReturn(true).once();

    expect(xwiki.exists(eq("Celements.MenuBar"), same(context))
      ).andReturn(false).once();

    replay(xwiki, rightsMock);
    assertTrue(pdInstance.hasview("Celements.MenuBar", context));
    verify(xwiki, rightsMock);
  }
  
  @Test
  public void testHasview_local_central_hasAccess() throws Exception {
    expect(xwiki.exists(eq("Celements.MenuBar"), same(context))
      ).andReturn(true).once();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("Celements.MenuBar"), same(context))).andReturn(true).once();

    expect(xwiki.exists(eq("Celements.MenuBar"), same(context))
      ).andReturn(true).once();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
      eq("Celements.MenuBar"), same(context))).andReturn(true).once();

    replay(xwiki, rightsMock);
    assertTrue(pdInstance.hasview("Celements.MenuBar", context));
    verify(xwiki, rightsMock);
  }
  
  @Test
  public void testHasview_local_notCentral_hasAccess() throws Exception {
    expect(xwiki.exists(eq("Celements.MenuBar"), same(context))
      ).andReturn(false).once();

    expect(xwiki.exists(eq("Celements.MenuBar"), same(context))
      ).andReturn(true).once();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
      eq("Celements.MenuBar"), same(context))).andReturn(true).once();

    replay(xwiki, rightsMock);
    assertTrue(pdInstance.hasview("Celements.MenuBar", context));
    verify(xwiki, rightsMock);
  }
  
  @Test
  public void testHasview_notLocal_central_noAccess() throws Exception {
    expect(xwiki.exists(eq("Celements.MenuBar"), same(context))
      ).andReturn(true).once();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
      eq("Celements.MenuBar"), same(context))).andReturn(false).once();

    expect(xwiki.exists(eq("Celements.MenuBar"), same(context))
      ).andReturn(false).once();

    replay(xwiki, rightsMock);
    assertFalse(pdInstance.hasview("Celements.MenuBar", context));
    verify(xwiki, rightsMock);
  }
  
  @Test
  public void testHasview_local_notCentral_noAccess() throws Exception {
    expect(xwiki.exists(eq("Celements.MenuBar"), same(context))
      ).andReturn(false).once();

    expect(xwiki.exists(eq("Celements.MenuBar"), same(context))
      ).andReturn(true).once();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("Celements.MenuBar"), same(context))).andReturn(false).once();

    replay(xwiki, rightsMock);
    assertFalse(pdInstance.hasview("Celements.MenuBar", context));
    verify(xwiki, rightsMock);
  }
  
  @Test
  public void testAddMenuHeaders() throws XWikiException {
    XWikiDocument doc = new XWikiDocument("Celements", "MenuBar");
    addMenuHeaderObject("mm_menu_name1", 10, 3, doc);
    addMenuHeaderObject("mm_menu_name2", 20, 2, doc);
    expect(xwiki.getDocument(eq("Celements.MenuBar"), same(context))
        ).andReturn(doc).once();
    List<Object> fullNames = new ArrayList<Object>();
    fullNames.add("Celements.MenuBar");
    expect(xwiki.search(isA(String.class), same(context))
        ).andReturn(fullNames);
    expect(xwiki.exists(eq("Celements.MenuBar"), same(context))
        ).andReturn(true).anyTimes();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("Celements.MenuBar"), same(context))).andReturn(true).anyTimes();
    
    replay(xwiki, rightsMock);
    TreeMap<Integer, BaseObject> menuHeadersMap = new TreeMap<Integer, BaseObject>();
    pdInstance.addMenuHeaders(menuHeadersMap, context);
    ArrayList<BaseObject> menuHeaders = new ArrayList<BaseObject>();
    menuHeaders.addAll(menuHeadersMap.values());

    assertEquals("Expecting sorted headers", 2,
        menuHeaders.get(0).getIntValue("pos"));
    assertEquals("Expecting sorted headers", 20,
        menuHeaders.get(0).getIntValue("header_id"));
    assertEquals("Expecting sorted headers", 3,
        menuHeaders.get(1).getIntValue("pos"));
    assertEquals("Expecting sorted headers", 10,
        menuHeaders.get(1).getIntValue("header_id"));
    verify(xwiki, rightsMock);
  }

  @Test
  public void testAddMenuHeaders_multipleDocs() throws XWikiException {
    XWikiDocument doc = new XWikiDocument("Celements", "MenuBar");
    XWikiDocument doc2 = new XWikiDocument("Celements", "MenuBar2");
    addMenuHeaderObject("mm_menu_name1", 12, 3, doc);
    addMenuHeaderObject("mm_menu_name2", 22, 2, doc);
    addMenuHeaderObject("mm_menu_name1", 42, 1, doc2);
    addMenuHeaderObject("mm_menu_name2", 32, 4, doc2);
    expect(xwiki.getDocument(eq("Celements.MenuBar"), same(context))
    ).andReturn(doc).once();
    expect(xwiki.getDocument(eq("Celements.MenuBar2"), same(context))
    ).andReturn(doc2).once();
    List<Object> fullNames = new ArrayList<Object>();
    fullNames.add("Celements.MenuBar");
    fullNames.add("Celements.MenuBar2");
    expect(xwiki.search(isA(String.class), same(context))
        ).andReturn(fullNames);
    expect(xwiki.exists(eq("Celements.MenuBar"), same(context))
      ).andReturn(true).anyTimes();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
      eq("Celements.MenuBar"), same(context))).andReturn(true).anyTimes();
    expect(xwiki.exists(eq("Celements.MenuBar2"), same(context))
      ).andReturn(true).anyTimes();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
      eq("Celements.MenuBar2"), same(context))).andReturn(true).anyTimes();
    
    replay(xwiki, rightsMock);
    TreeMap<Integer, BaseObject> menuHeadersMap = new TreeMap<Integer, BaseObject>();
    pdInstance.addMenuHeaders(menuHeadersMap, context);
    ArrayList<BaseObject> menuHeaders = new ArrayList<BaseObject>();

    menuHeaders.addAll(menuHeadersMap.values());
    assertEquals("Expecting sorted headers", 1,
        menuHeaders.get(0).getIntValue("pos"));
    assertEquals("Expecting sorted headers", 42,
        menuHeaders.get(0).getIntValue("header_id"));
    assertEquals("Expecting sorted headers", 2,
        menuHeaders.get(1).getIntValue("pos"));
    assertEquals("Expecting sorted headers", 22,
        menuHeaders.get(1).getIntValue("header_id"));
    assertEquals("Expecting sorted headers", 3,
        menuHeaders.get(2).getIntValue("pos"));
    assertEquals("Expecting sorted headers", 12,
        menuHeaders.get(2).getIntValue("header_id"));
    assertEquals("Expecting sorted headers", 4,
        menuHeaders.get(3).getIntValue("pos"));
    assertEquals("Expecting sorted headers", 32,
        menuHeaders.get(3).getIntValue("header_id"));
    verify(xwiki, rightsMock);
  }

  @Test
  public void testAddMenuHeaders_multipleDocs_noAccess() throws XWikiException {
    XWikiDocument doc = new XWikiDocument("Celements", "MenuBar");
    XWikiDocument doc2 = new XWikiDocument("Celements", "MenuBar2");
    addMenuHeaderObject("mm_menu_name1", 12, 3, doc);
    addMenuHeaderObject("mm_menu_name2", 22, 2, doc);
    addMenuHeaderObject("mm_menu_name1", 42, 1, doc2);
    addMenuHeaderObject("mm_menu_name2", 32, 4, doc2);
    expect(xwiki.getDocument(eq("Celements.MenuBar"), same(context))
    ).andReturn(doc).anyTimes();
    expect(xwiki.getDocument(eq("Celements.MenuBar2"), same(context))
    ).andReturn(doc2).anyTimes();
    List<Object> fullNames = new ArrayList<Object>();
    fullNames.add("Celements.MenuBar");
    fullNames.add("Celements.MenuBar2");
    expect(xwiki.search(isA(String.class), same(context))
        ).andReturn(fullNames);
    expect(xwiki.exists(eq("Celements.MenuBar"), same(context))
      ).andReturn(true).anyTimes();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
      eq("Celements.MenuBar"), same(context))).andReturn(true).anyTimes();
    expect(xwiki.exists(eq("Celements.MenuBar2"), same(context))
      ).andReturn(true).anyTimes();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
      eq("Celements.MenuBar2"), same(context))).andReturn(false).anyTimes();
    
    replay(xwiki, rightsMock);
    TreeMap<Integer, BaseObject> menuHeadersMap = new TreeMap<Integer, BaseObject>();
    pdInstance.addMenuHeaders(menuHeadersMap, context);
    ArrayList<BaseObject> menuHeaders = new ArrayList<BaseObject>();

    menuHeaders.addAll(menuHeadersMap.values());
    assertEquals("Expecting sorted headers", 2,
        menuHeaders.get(0).getIntValue("pos"));
    assertEquals("Expecting sorted headers", 22,
        menuHeaders.get(0).getIntValue("header_id"));
    assertEquals("Expecting sorted headers", 3,
        menuHeaders.get(1).getIntValue("pos"));
    assertEquals("Expecting sorted headers", 12,
        menuHeaders.get(1).getIntValue("header_id"));
    verify(xwiki, rightsMock);
  }

  @Test
  public void testGetMenuHeaders_multipleDocs_celements2web() throws XWikiException {
    XWikiDocument doc = new XWikiDocument("Celements", "MenuBar");
    XWikiDocument doc2 = new XWikiDocument("Celements", "MenuBar2");
    addMenuHeaderObject("mm_menu_name1", 13, 3, doc);
    addMenuHeaderObject("mm_menu_name2", 23, 5, doc);
    addMenuHeaderObject("mm_menu_name1", 43, 1, doc2);
    addMenuHeaderObject("mm_menu_name2", 33, 4, doc2);
    expect(xwiki.getDocument(eq("Celements.MenuBar"), same(context))
      ).andReturn(doc).once();
    List<Object> fullNames = new ArrayList<Object>();
    fullNames.add("Celements.MenuBar");
    expect(xwiki.search(isA(String.class), same(context))
      ).andReturn(fullNames).once();
    List<Object> fullNamesCentral = new ArrayList<Object>();
    fullNamesCentral.add("Celements.MenuBar2");
    expect(xwiki.search(isA(String.class), same(context))
      ).andReturn(fullNamesCentral).once();
    expect(xwiki.getDocument(eq("Celements.MenuBar2"), same(context))
      ).andReturn(doc2).once();

    expect(xwiki.exists(eq("Celements.MenuBar"), same(context))
      ).andReturn(true).anyTimes();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
      eq("Celements.MenuBar"), same(context))).andReturn(true).anyTimes();
    expect(xwiki.exists(eq("Celements.MenuBar2"), same(context))
      ).andReturn(true).anyTimes();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
      eq("Celements.MenuBar2"), same(context))).andReturn(true).anyTimes();
    
    replay(xwiki, rightsMock);
    List<BaseObject> menuHeaders = pdInstance.getMenuHeaders(context);
    assertEquals("Expecting sorted headers", 1,
        menuHeaders.get(0).getIntValue("pos"));
    assertEquals("Expecting sorted headers", 43,
        menuHeaders.get(0).getIntValue("header_id"));
    assertEquals("Expecting sorted headers", 3,
        menuHeaders.get(1).getIntValue("pos"));
    assertEquals("Expecting sorted headers", 13,
        menuHeaders.get(1).getIntValue("header_id"));
    assertEquals("Expecting sorted headers", 4,
        menuHeaders.get(2).getIntValue("pos"));
    assertEquals("Expecting sorted headers", 33,
        menuHeaders.get(2).getIntValue("header_id"));
    assertEquals("Expecting sorted headers", 5,
        menuHeaders.get(3).getIntValue("pos"));
    assertEquals("Expecting sorted headers", 23,
        menuHeaders.get(3).getIntValue("header_id"));
    verify(xwiki, rightsMock);
  }

  
  //*****************************************************************
  //*                  H E L P E R  - M E T H O D S                 *
  //*****************************************************************/

  private void addMenuHeaderObject(String menuName, int headerId, int pos,
      XWikiDocument doc) {
    BaseObject obj = new BaseObject();
    obj.setStringValue("name", menuName);
    obj.setIntValue("header_id", headerId);
    obj.setIntValue("pos", pos);
    doc.addObject("Celements.MenuBarHeaderItemClass", obj);
  }

}