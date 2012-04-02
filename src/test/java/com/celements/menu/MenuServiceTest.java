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
package com.celements.menu;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryManager;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

public class MenuServiceTest extends AbstractBridgedComponentTestCase {

  private MenuService menuService;
  private XWikiContext context;
  private XWiki xwiki;
  private XWikiRightService rightsMock;
  private QueryManager queryManagerMock;

  @Before
  public void setUp_DataProviderTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    rightsMock = createMock(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(rightsMock).anyTimes();
    menuService = (MenuService) Utils.getComponent(IMenuService.class);
    queryManagerMock = createMock(QueryManager.class);
    menuService.queryManager = queryManagerMock;
  }

  @Test
  public void testGetHeadersHQL() {
    assertTrue(menuService.getHeadersXWQL().matches(
        "from doc.object\\(Celements.MenuBarHeaderItemClass\\) as mHeader.*?"));
  }

  @Test
  public void testHasview_notLocal_central_hasAccess() throws Exception {
    DocumentReference menuBarDocRef = new DocumentReference(context.getDatabase(),
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBarDocRef), same(context))).andReturn(false).once();

    DocumentReference menuBar2webDocRef = new DocumentReference("celements2web",
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBar2webDocRef), same(context))).andReturn(true).once();
    expect(
        rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
            eq("celements2web:Celements.MenuBar"), same(context))).andReturn(true).once();

    replayAll();
    assertTrue(menuService.hasview(menuBarDocRef));
    verifyAll();
  }

  @Test
  public void testHasview_Local_central_noAccess() throws Exception {
    DocumentReference menuBarDocRef = new DocumentReference(context.getDatabase(),
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBarDocRef), same(context))).andReturn(true).once();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("xwikidb:Celements.MenuBar"), same(context))).andReturn(false).once();

    DocumentReference menuBar2webDocRef = new DocumentReference("celements2web",
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBar2webDocRef), same(context))).andReturn(true).once();
    expect(
        rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
            eq("celements2web:Celements.MenuBar"), same(context))).andReturn(true).once();

    replayAll();
    assertFalse(menuService.hasview(menuBarDocRef));
    verifyAll();
  }

  @Test
  public void testHasview_local_central_hasAccess() throws Exception {
    DocumentReference menuBarDocRef = new DocumentReference(context.getDatabase(),
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBarDocRef), same(context))).andReturn(true).once();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("xwikidb:Celements.MenuBar"), same(context))).andReturn(true).once();

    DocumentReference menuBar2webDocRef = new DocumentReference("celements2web",
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBar2webDocRef), same(context))).andReturn(true).once();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
      eq("celements2web:Celements.MenuBar"), same(context))).andReturn(true).once();

    replayAll();
    assertTrue(menuService.hasview(menuBarDocRef));
    verifyAll();
  }
  
  @Test
  public void testHasview_local_notCentral_hasAccess() throws Exception {
    DocumentReference menuBar2webDocRef = new DocumentReference("celements2web",
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBar2webDocRef), same(context))).andReturn(false).once();

    DocumentReference menuBarDocRef = new DocumentReference(context.getDatabase(),
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBarDocRef), same(context))).andReturn(true).once();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
      eq("xwikidb:Celements.MenuBar"), same(context))).andReturn(true).once();

    replayAll();
    assertTrue(menuService.hasview(menuBarDocRef));
    verifyAll();
  }
  
  @Test
  public void testHasview_notLocal_central_noAccess() throws Exception {
    DocumentReference menuBar2webDocRef = new DocumentReference("celements2web",
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBar2webDocRef), same(context))).andReturn(true).once();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
      eq("celements2web:Celements.MenuBar"), same(context))).andReturn(false).once();

    DocumentReference menuBarDocRef = new DocumentReference(context.getDatabase(),
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBarDocRef), same(context))).andReturn(false).once();

    replayAll();
    assertFalse(menuService.hasview(menuBarDocRef));
    verifyAll();
  }
  
  @Test
  public void testHasview_local_notCentral_noAccess() throws Exception {
    DocumentReference menuBar2webDocRef = new DocumentReference("celements2web",
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBar2webDocRef), same(context))).andReturn(false).once();

    DocumentReference menuBarDocRef = new DocumentReference(context.getDatabase(),
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBarDocRef), same(context))).andReturn(true).once();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("xwikidb:Celements.MenuBar"), same(context))).andReturn(false).once();

    replayAll();
    assertFalse(menuService.hasview(menuBarDocRef));
    verifyAll();
  }
  
  @Test
  public void testAddMenuHeaders() throws Exception {
    DocumentReference menuBar2webDocRef = new DocumentReference("celements2web",
        "Celements", "MenuBar");
    expect(xwiki.exists(eq(menuBar2webDocRef), same(context))).andReturn(false
        ).anyTimes();
    DocumentReference menuBarDocRef = new DocumentReference(context.getDatabase(),
        "Celements", "MenuBar");
    XWikiDocument doc = new XWikiDocument(menuBarDocRef);
    addMenuHeaderObject("mm_menu_name1", 10, 3, doc);
    addMenuHeaderObject("mm_menu_name2", 20, 2, doc);
    expect(xwiki.getDocument(eq(menuBarDocRef), same(context))).andReturn(doc
        ).once();
    List<Object> fullNames = new ArrayList<Object>();
    fullNames.add("Celements.MenuBar");
    Query mockQuery = createMock(Query.class);
    expect(queryManagerMock.createQuery(isA(String.class), eq(Query.XWQL))).andReturn(
        mockQuery).once();
    expect(mockQuery.execute()).andReturn(fullNames).once();
    expect(xwiki.exists(eq(menuBarDocRef), same(context))).andReturn(true
        ).anyTimes();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq("xwikidb:Celements.MenuBar"), same(context))).andReturn(true).anyTimes();
    
    replayAll(mockQuery);
    TreeMap<Integer, BaseObject> menuHeadersMap = new TreeMap<Integer, BaseObject>();
    menuService.addMenuHeaders(menuHeadersMap);
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
    verifyAll(mockQuery);
  }

  @Test
  public void testAddMenuHeaders_multipleDocs() throws Exception {
    DocumentReference menuBar2webDocRef = new DocumentReference("celements2web",
        "Celements", "MenuBar");
    DocumentReference menuBar2web2DocRef = new DocumentReference("celements2web",
        "Celements", "MenuBar2");
    expect(xwiki.exists(eq(menuBar2webDocRef), same(context))).andReturn(false
        ).anyTimes();
    expect(xwiki.exists(eq(menuBar2web2DocRef), same(context))).andReturn(false
        ).anyTimes();
    DocumentReference menuBarDocRef = new DocumentReference(context.getDatabase(),
        "Celements", "MenuBar");
    XWikiDocument doc = new XWikiDocument(menuBarDocRef);
    DocumentReference menuBar2DocRef = new DocumentReference(context.getDatabase(),
        "Celements", "MenuBar2");
    XWikiDocument doc2 = new XWikiDocument(menuBar2DocRef);
    addMenuHeaderObject("mm_menu_name1", 12, 3, doc);
    addMenuHeaderObject("mm_menu_name2", 22, 2, doc);
    addMenuHeaderObject("mm_menu_name1", 42, 1, doc2);
    addMenuHeaderObject("mm_menu_name2", 32, 4, doc2);
    expect(xwiki.getDocument(eq(menuBarDocRef), same(context))).andReturn(doc
        ).once();
    expect(xwiki.getDocument(eq(menuBar2DocRef), same(context))).andReturn(doc2
        ).once();
    List<Object> fullNames = new ArrayList<Object>();
    fullNames.add("Celements.MenuBar");
    fullNames.add("Celements.MenuBar2");
    Query mockQuery = createMock(Query.class);
    expect(queryManagerMock.createQuery(isA(String.class), eq(Query.XWQL))).andReturn(
        mockQuery).once();
    expect(mockQuery.execute()).andReturn(fullNames).once();
    expect(xwiki.exists(eq(menuBarDocRef), same(context))).andReturn(true
        ).anyTimes();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
      eq("xwikidb:Celements.MenuBar"), same(context))).andReturn(true).anyTimes();
    expect(xwiki.exists(eq(menuBar2DocRef), same(context))).andReturn(true
        ).anyTimes();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
      eq("xwikidb:Celements.MenuBar2"), same(context))).andReturn(true).anyTimes();
    
    replayAll(mockQuery);
    TreeMap<Integer, BaseObject> menuHeadersMap = new TreeMap<Integer, BaseObject>();
    menuService.addMenuHeaders(menuHeadersMap);
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
    verifyAll(mockQuery);
  }

  @Test
  public void testAddMenuHeaders_multipleDocs_noAccess() throws Exception {
    DocumentReference menuBar2webDocRef = new DocumentReference("celements2web",
        "Celements", "MenuBar");
    DocumentReference menuBar2web2DocRef = new DocumentReference("celements2web",
        "Celements", "MenuBar2");
    expect(xwiki.exists(eq(menuBar2webDocRef), same(context))).andReturn(false
        ).anyTimes();
    expect(xwiki.exists(eq(menuBar2web2DocRef), same(context))).andReturn(false
        ).anyTimes();
    DocumentReference menuBarDocRef = new DocumentReference(context.getDatabase(),
        "Celements", "MenuBar");
    XWikiDocument doc = new XWikiDocument(menuBarDocRef);
    DocumentReference menuBar2DocRef = new DocumentReference(context.getDatabase(),
        "Celements", "MenuBar2");
    XWikiDocument doc2 = new XWikiDocument(menuBar2DocRef);
    addMenuHeaderObject("mm_menu_name1", 12, 3, doc);
    addMenuHeaderObject("mm_menu_name2", 22, 2, doc);
    addMenuHeaderObject("mm_menu_name1", 42, 1, doc2);
    addMenuHeaderObject("mm_menu_name2", 32, 4, doc2);
    expect(xwiki.getDocument(eq(menuBarDocRef), same(context))).andReturn(doc
        ).anyTimes();
    expect(xwiki.getDocument(eq(menuBar2DocRef), same(context))).andReturn(doc2
        ).anyTimes();
    List<Object> fullNames = new ArrayList<Object>();
    fullNames.add("Celements.MenuBar");
    fullNames.add("Celements.MenuBar2");
    Query mockQuery = createMock(Query.class);
    expect(queryManagerMock.createQuery(isA(String.class), eq(Query.XWQL))).andReturn(
        mockQuery).once();
    expect(mockQuery.execute()).andReturn(fullNames).once();
    expect(xwiki.exists(eq(menuBarDocRef), same(context))).andReturn(true
        ).anyTimes();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
      eq("xwikidb:Celements.MenuBar"), same(context))).andReturn(true).anyTimes();
    expect(xwiki.exists(eq(menuBar2DocRef), same(context))).andReturn(true
        ).anyTimes();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
      eq("xwikidb:Celements.MenuBar2"), same(context))).andReturn(false).anyTimes();
    
    replayAll(mockQuery);
    TreeMap<Integer, BaseObject> menuHeadersMap = new TreeMap<Integer, BaseObject>();
    menuService.addMenuHeaders(menuHeadersMap);
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
    verifyAll(mockQuery);
  }

  @Test
  public void testGetMenuHeaders_multipleDocs_celements2web() throws Exception {
    DocumentReference menuBar2webDocRef = new DocumentReference("celements2web",
        "Celements", "MenuBar");
    DocumentReference menuBar2web2DocRef = new DocumentReference("celements2web",
        "Celements", "MenuBar2");
    expect(xwiki.exists(eq(menuBar2webDocRef), same(context))).andReturn(false
        ).anyTimes();
    expect(xwiki.exists(eq(menuBar2web2DocRef), same(context))).andReturn(true
        ).anyTimes();
    DocumentReference menuBarDocRef = new DocumentReference(context.getDatabase(),
        "Celements", "MenuBar");
    XWikiDocument doc = new XWikiDocument(menuBarDocRef);
    DocumentReference menuBar2DocRef = new DocumentReference(context.getDatabase(),
        "Celements", "MenuBar2");
    XWikiDocument doc2 = new XWikiDocument(menuBar2web2DocRef);
    addMenuHeaderObject("mm_menu_name1", 13, 3, doc);
    addMenuHeaderObject("mm_menu_name2", 23, 5, doc);
    addMenuHeaderObject("mm_menu_name1", 43, 1, doc2);
    addMenuHeaderObject("mm_menu_name2", 33, 4, doc2);
    expect(xwiki.getDocument(eq(menuBarDocRef), same(context))).andReturn(doc
        ).once();
    List<Object> fullNames = new ArrayList<Object>();
    fullNames.add("Celements.MenuBar");
    Query mockQuery = createMock(Query.class);
    expect(queryManagerMock.createQuery(isA(String.class), eq(Query.XWQL))).andReturn(
        mockQuery).once();
    expect(mockQuery.execute()).andReturn(fullNames).once();
    List<Object> fullNamesCentral = new ArrayList<Object>();
    fullNamesCentral.add("Celements.MenuBar2");
    Query mockQuery2 = createMock(Query.class);
    expect(queryManagerMock.createQuery(isA(String.class), eq(Query.XWQL))).andReturn(
        mockQuery2).once();
    expect(mockQuery2.execute()).andReturn(fullNamesCentral).once();
    expect(xwiki.getDocument(eq(menuBar2web2DocRef), same(context))).andReturn(doc2
        ).once();

    expect(xwiki.exists(eq(menuBarDocRef), same(context))).andReturn(true
        ).anyTimes();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
      eq("xwikidb:Celements.MenuBar"), same(context))).andReturn(true).anyTimes();
    expect(xwiki.exists(eq(menuBar2DocRef), same(context))).andReturn(false
        ).anyTimes();
    expect(rightsMock.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
      eq("celements2web:Celements.MenuBar2"), same(context))).andReturn(true).anyTimes();
    
    replayAll(mockQuery, mockQuery2);
    List<BaseObject> menuHeaders = menuService.getMenuHeaders();
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
    verifyAll(mockQuery, mockQuery2);
  }

  
  //*****************************************************************
  //*                  H E L P E R  - M E T H O D S                 *
  //*****************************************************************/

  private void addMenuHeaderObject(String menuName, int headerId, int pos,
      XWikiDocument doc) {
    BaseObject obj = new BaseObject();
    obj.setXClassReference(new DocumentReference(doc.getDocumentReference(
        ).getWikiReference().getName(), "Celements", "MenuBarHeaderItemClass"));
    obj.setStringValue("name", menuName);
    obj.setIntValue("header_id", headerId);
    obj.setIntValue("pos", pos);
    doc.addXObject(obj);
  }

  private void replayAll(Object ... mocks) {
    replay(xwiki, queryManagerMock, rightsMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, queryManagerMock, rightsMock);
    verify(mocks);
  }
}