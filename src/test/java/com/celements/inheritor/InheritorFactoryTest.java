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
package com.celements.inheritor;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.iterator.DocumentIterator;
import com.celements.iterator.XObjectIterator;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.celements.web.utils.IWebUtils;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;

public class InheritorFactoryTest extends AbstractBridgedComponentTestCase {

  private InheritorFactory _factory;
  private XWikiContext _context;
  private IWebUtils _mockWebUtils;
  private XWiki _xwiki;

  @Before
  public void setUp_InheritorFactoryTest() throws Exception {
    _context = getContext();
    _xwiki = createMock(XWiki.class);
    _context.setWiki(_xwiki);
    _factory = new InheritorFactory();
    _mockWebUtils = createMock(IWebUtils.class);
    _factory.inject_TEST_WebUtils(_mockWebUtils);
  }

  @Test
  public void testGetFieldInheritor() {
    String className = "TestClassName";
    List<String> docList = new ArrayList<String>();
    docList.add("my.Doc");
    docList.add("my.Doc2");
    FieldInheritor inheritor = _factory.getFieldInheritor(className, docList, _context);
    XObjectIterator iterator = inheritor.getIteratorFactory().createIterator();
    assertEquals(className, iterator.getClassName());
    assertEquals(docList, iterator.getDocListCopy());
  }
  
  @Test
  public void testGetContentInheritor() {
    List<String> docList = new ArrayList<String>();
    docList.add("my.Doc");
    docList.add("my.Doc2");
    ContentInheritor inheritor = _factory.getContentInheritor(docList, _context);
    DocumentIterator iterator = inheritor.getIteratorFactory().createIterator();
    assertEquals(docList, iterator.getDocListCopy());
  }

  @Test
  public void testGetNavigationFieldInheritor() {
    String className = "Tools.Banner";
    String fullName = "mySpace.myDoc";
    List<String> docList = new ArrayList<String>();
    docList.add(fullName);
    docList.add("myparent.Doc");
    docList.add("myparent.Doc2");
    expect(_mockWebUtils.getDocumentParentsList(eq(fullName), eq(true), same(_context))
        ).andReturn(docList);
    replayAll();
    FieldInheritor inheritor = _factory.getNavigationFieldInheritor(className, fullName,
        _context);
    XObjectIterator iterator = inheritor.getIteratorFactory().createIterator();
    assertEquals(className, iterator.getClassName());
    assertEquals(docList, iterator.getDocListCopy());
    verifyAll();
  }

  @Test
  public void testgetSpacePreferencesFullName() {
    String fullName = "mySpace.myDoc";
    assertEquals("mySpace.WebPreferences", _factory.getSpacePreferencesFullName(fullName));
  }

  @Test
  public void testGetPageLayoutInheritor() {
    String className = "Celements2.PageType";
    String fullName = "mySpace.myDoc";
    List<String> docList = new ArrayList<String>();
    docList.add(fullName);
    docList.add("mySpace.WebPreferences");
    docList.add("XWiki.XWikiPreferences");
    FieldInheritor inheritor = _factory.getPageLayoutInheritor(fullName, _context);
    XObjectIterator iterator = inheritor.getIteratorFactory().createIterator();
    assertEquals(className, iterator.getClassName());
    assertEquals(docList, iterator.getDocListCopy());
  }

  @Test
  public void testGetConfigDocFieldInheritor_fullnames() throws Exception {
    PageLayoutCommand mockPageLayoutCmd = createMock(PageLayoutCommand.class);
    _factory.injectPageLayoutCmd(mockPageLayoutCmd);
    String className = "mySpace.myClassName";
    String fullName = "mySpace.myDocName";
    List<String> docList = new ArrayList<String>();
    docList.add("mySpace.WebPreferences");
    docList.add("XWiki.XWikiPreferences");
    DocumentReference webHomeDocRef = new DocumentReference(_context.getDatabase(),
        "mySpace", "WebHome");
    expect(_xwiki.exists(eq(webHomeDocRef), same(_context))).andReturn(false).anyTimes();
    expect(mockPageLayoutCmd.getPageLayoutForDoc(eq(fullName), same(_context))).andReturn(
        null);
    expect(_xwiki.getSpacePreference(eq("skin"), same(_context))).andReturn(null);
    replayAll(mockPageLayoutCmd);
    FieldInheritor fieldInheritor = _factory.getConfigDocFieldInheritor(className,
        fullName, _context);
    XObjectIterator iterator = fieldInheritor.getIteratorFactory().createIterator();
    verifyAll(mockPageLayoutCmd);
    assertEquals(className, iterator.getClassName());
    assertEquals(docList, iterator.getDocListCopy());
  }

  @Test
  public void testGetConfigFieldInheritor_docRef() throws Exception {
    PageLayoutCommand mockPageLayoutCmd = createMock(PageLayoutCommand.class);
    _factory.injectPageLayoutCmd(mockPageLayoutCmd);
    String className = "mySpace.myClassName";
    String fullName = "mySpace.myDocName";
    DocumentReference docRef = new DocumentReference(_context.getDatabase(), "mySpace",
        "myDocName");
    DocumentReference classDocRef = new DocumentReference(_context.getDatabase(),
        "mySpace", "myClassName");
    List<String> docList = new ArrayList<String>();
    docList.add("xwikidb:" + fullName);
    docList.add("xwikidb:mySpace.WebPreferences");
    docList.add("xwikidb:XWiki.XWikiPreferences");
    replayAll(mockPageLayoutCmd);
    FieldInheritor fieldInheritor = _factory.getConfigFieldInheritor(classDocRef, docRef);
    XObjectIterator iterator = fieldInheritor.getIteratorFactory().createIterator();
    verifyAll(mockPageLayoutCmd);
    assertEquals("xwikidb:" + className, iterator.getClassName());
    assertEquals(docList, iterator.getDocListCopy());
  }

  //*****************************************************************
  //*                  H E L P E R  - M E T H O D S                 *
  //*****************************************************************/

  private void replayAll(Object ... mocks) {
    replay(_xwiki, _mockWebUtils);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(_xwiki, _mockWebUtils);
    verify(mocks);
  }

}
