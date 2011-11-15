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

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.iterator.DocumentIterator;
import com.celements.iterator.XObjectIterator;
import com.celements.web.utils.IWebUtils;
import com.xpn.xwiki.XWikiContext;

public class InheritorFactoryTest extends AbstractBridgedComponentTestCase {

  private InheritorFactory _factory;
  private XWikiContext _context;
  private IWebUtils _mockWebUtils;

  @Before
  public void setUp_InheritorFactoryTest() throws Exception {
    _context = getContext();
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
    replay(_mockWebUtils);
    FieldInheritor inheritor = _factory.getNavigationFieldInheritor(className, fullName,
        _context);
    XObjectIterator iterator = inheritor.getIteratorFactory().createIterator();
    assertEquals(className, iterator.getClassName());
    assertEquals(docList, iterator.getDocListCopy());
    verify(_mockWebUtils);
  }

  @Test
  public void testGetWebPreferencesFullName() {
    String fullName = "mySpace.myDoc";
    assertEquals("mySpace.WebPreferences", _factory.getWebPreferencesFullName(fullName));
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

}
