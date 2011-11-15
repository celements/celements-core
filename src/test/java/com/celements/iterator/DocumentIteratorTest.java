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
package com.celements.iterator;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

public class DocumentIteratorTest extends AbstractBridgedComponentTestCase {

  private DocumentIterator _iterator;
  private XWikiContext _context;
  private XWiki _xwiki;
  private XWikiDocument _testDoc1;
  private XWikiDocument _testDoc2;
  private List<String> _docList;

  @Before
  public void setUp_DocumentIteratorTest() throws Exception {
    _context = getContext();
    _xwiki = createMock(XWiki.class);
    _context.setWiki(_xwiki);
    _iterator = new DocumentIterator(_context);
    _testDoc1 = new XWikiDocument();
    _testDoc2 = new XWikiDocument();
    _docList = new ArrayList<String>();
  }  
  
  @Test
  public void testSetDocList() {
    _docList.add("Test.Doc");
    _docList.add("Test.Doc2");
    _iterator.setDocList(_docList);
    assertEquals(_docList, _iterator.getDocList());
  }

  @Test
  public void testSetDocList_resetIterator() throws Exception {
    _docList.add("Test.Doc");
    _iterator.setDocList(_docList);
    _iterator.inject_CurrentDoc(_testDoc1);
    Iterator<String> firstDocIt = _iterator.getDocIterator();
    _iterator.setDocList(_docList);
    assertNotSame(firstDocIt, _iterator.getDocIterator());
  }

  @Test
  public void testMoveToNextDoc_emptyDocList() {
    _iterator.inject_CurrentDoc(null);
    _iterator.setDocList(new ArrayList<String>());
    replay(_xwiki);
    try {
      _iterator.moveToNextDoc();
      fail("Exception expected.");
    } catch(NoSuchElementException ex) {
      //expected behaviour
    }
    verify(_xwiki);
  }

  @Test
  public void testMoveToNextDoc_updateCurrentDoc() throws Exception {
    _iterator.inject_CurrentDoc(null); // reset Iterator
    String fullname1 = "Test.Doc1";
    String fullname2 = "Test.Doc2";
    _docList.add(fullname1);
    _docList.add(fullname2);
    _iterator.setDocList(_docList);
    expect(_xwiki.getDocument(eq(fullname1), same(_context))).andReturn(
        new XWikiDocument()).once();
    expect(_xwiki.getDocument(eq(fullname2), same(_context))).andReturn(
      new XWikiDocument()).once();
    expect(_xwiki.exists(eq(fullname1), same(_context))).andReturn(
        true).anyTimes();
    expect(_xwiki.exists(eq(fullname2), same(_context))).andReturn(
        true).anyTimes();
    replay(_xwiki);
    XWikiDocument firstDoc = _iterator.getCurrentDoc();
    _iterator.moveToNextDoc();
    XWikiDocument secondDoc = _iterator.getCurrentDoc();
    assertNotSame("moveToNextDoc must update the currentDoc.",
        firstDoc, secondDoc);
    verify(_xwiki);
  }

  @Test
  public void testGetCurrentDoc_emptyDocList() {
    _iterator.inject_CurrentDoc(null);
    _iterator.setDocList(new ArrayList<String>());
    replay(_xwiki);
    assertNull(_iterator.getCurrentDoc());
    verify(_xwiki);
  }

  @Test
  public void testGetDocIterator_noDocList_set() throws Exception {
    try {
      _iterator.getDocIterator();
      fail("Expecting exception.");
    } catch(IllegalStateException ex) {
      //expected behaviour
    }
  }

  @Test
  public void testGetDocIterator() throws Exception {
    _docList.add("Test.Doc");
    _iterator.setDocList(_docList);
    assertSame(_iterator.getDocIterator(), _iterator.getDocIterator());
  }

  @Test
  public void testHasNext_emptyDocList() {
    _iterator.inject_CurrentDoc(null);
    _iterator.setDocList(new ArrayList<String>());
    assertFalse(_iterator.hasNext());
  }

  @Test
  public void testHasNext_true() throws Exception {
    String fullname1 = "Test.Doc1";
    String fullname2 = "Test.Doc2";
    _docList.add(fullname1);
    _docList.add(fullname2);
    _testDoc1.setFullName(fullname1);
    _testDoc2.setFullName(fullname2);
    expect(_xwiki.getDocument(eq(fullname1), same(_context))).andReturn(_testDoc1
        ).anyTimes();
    expect(_xwiki.getDocument(eq(fullname2), same(_context))).andReturn(_testDoc2
        ).anyTimes();
    expect(_xwiki.exists(eq(fullname1), same(_context))).andReturn(
        true).anyTimes();
    expect(_xwiki.exists(eq(fullname2), same(_context))).andReturn(
        true).anyTimes();
    _iterator.setDocList(_docList);
    replay(_xwiki);
    _iterator.next();
    assertTrue(_iterator.hasNext());
    _iterator.next();
    assertFalse(_iterator.hasNext());
    verify(_xwiki);
  }
  
  @Test
  public void testHasNext_skipDoc() throws Exception {
    String fullname1 = "Test.Doc1";
    String fullname2 = "Test.Doc2";
    _docList.add(fullname1);
    _docList.add(fullname2);
    _testDoc2.setFullName(fullname1);
    expect(_xwiki.getDocument(eq(fullname1), same(_context))).andReturn(null
        ).anyTimes();
    expect(_xwiki.getDocument(eq(fullname2), same(_context))).andReturn(_testDoc2
        ).anyTimes();
    expect(_xwiki.exists(eq(fullname1), same(_context))).andReturn(
        false).anyTimes();
    expect(_xwiki.exists(eq(fullname2), same(_context))).andReturn(
        true).anyTimes();
    _iterator.setDocList(_docList);
    replay(_xwiki);
    assertTrue(_iterator.hasNext());
    assertNotNull(_iterator.next());
    assertFalse(_iterator.hasNext());
    verify(_xwiki);
  }
  
  @Test
  public void testHasNext_false() throws Exception {
    String fullname = "Test.Doc";
    _docList.add(fullname);
    expect(_xwiki.getDocument(eq(fullname), same(_context))).andReturn(_testDoc1
        ).anyTimes();
    expect(_xwiki.exists(eq(fullname), same(_context))).andReturn(
        true).anyTimes();
    _iterator.setDocList(_docList);
    replay(_xwiki);
    _iterator.next();
    assertEquals(false, _iterator.hasNext());
    verify(_xwiki); 
  }

  @Test
  public void testNext_noMoreElementsException() throws Exception {
    _iterator.setDocList(_docList);
    replay(_xwiki);
    try {
      _iterator.next();
      fail("Exception expected.");
    } catch(NoSuchElementException ex) {
      //expected behaviour
    }
    verify(_xwiki);
  }
  
  @Test
  public void testRemove() {
    try {
      _iterator.remove();
      fail("Remove not supported.");
    } catch(UnsupportedOperationException ex) {
      //expected behaviour
    }
  }
  
  @Test
  public void testIterator_foreach() throws Exception {
    String fullname1 = "Test.Doc1";
    String fullname2 = "Test.Doc2";
    _docList.add(fullname1);
    _docList.add(fullname2);
    _testDoc1.setFullName(fullname1);
    _testDoc2.setFullName(fullname2);
    expect(_xwiki.getDocument(eq(fullname1), same(_context))).andReturn(_testDoc1
        ).once();
    expect(_xwiki.getDocument(eq(fullname2), same(_context))).andReturn(_testDoc2
        ).once();
    expect(_xwiki.exists(eq(fullname1), same(_context))).andReturn(
        true).anyTimes();
    expect(_xwiki.exists(eq(fullname2), same(_context))).andReturn(
        true).anyTimes();
    _iterator.setDocList(_docList);
    replay(_xwiki);
    int count = 0;
    List<String> resultList = new ArrayList<String>();
    for (XWikiDocument doc : _iterator) {
      resultList.add(doc.getFullName());
      count++;
    }
    assertEquals(resultList, _docList);
    assertEquals(2, count);
    verify(_xwiki);
  }

  //*****************************************************************
  //*                  H E L P E R  - M E T H O D S                 *
  //*****************************************************************/

}
