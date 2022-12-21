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

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

public class DocumentIteratorTest extends AbstractComponentTest {

  private DocumentIterator _iterator;
  private XWikiContext _context;
  private XWiki _xwiki;
  private XWikiDocument _testDoc1;
  private XWikiDocument _testDoc2;
  private List<String> _docList;
  private IWebUtilsService webUtilsMock;
  private String fullname1;
  private String fullname2;
  private DocumentReference docRef1;
  private DocumentReference docRef2;

  @Before
  public void setUp_DocumentIteratorTest() throws Exception {
    registerComponentMocks(IModelAccessFacade.class);
    webUtilsMock = registerComponentMock(IWebUtilsService.class);
    _context = getContext();
    _xwiki = getWikiMock();
    fullname1 = "Test.Doc1";
    fullname2 = "Test.Doc2";
    docRef1 = new DocumentReference(_context.getDatabase(), "Test", "Doc1");
    docRef2 = new DocumentReference(_context.getDatabase(), "Test", "Doc2");
    _iterator = new DocumentIterator();
    _testDoc1 = new XWikiDocument(docRef1);
    _testDoc1.setNew(false);
    _testDoc2 = new XWikiDocument(docRef2);
    _testDoc2.setNew(false);
    _docList = new ArrayList<>();
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
    replayDefault();
    try {
      _iterator.moveToNextDoc();
      fail("Exception expected.");
    } catch (NoSuchElementException ex) {
      // expected behaviour
    }
    verifyDefault();
  }

  @Test
  public void testMoveToNextDoc_updateCurrentDoc() throws Exception {
    _iterator.inject_CurrentDoc(null); // reset Iterator
    _docList.add(fullname1);
    _docList.add(fullname2);
    _iterator.setDocList(_docList);
    expect(getMock(IModelAccessFacade.class).getDocument(eq(docRef1))).andReturn(_testDoc1);
    expect(getMock(IModelAccessFacade.class).getDocument(eq(docRef2))).andReturn(_testDoc2);
    expect(getMock(IModelAccessFacade.class).exists(eq(docRef1))).andReturn(true).anyTimes();
    expect(getMock(IModelAccessFacade.class).exists(eq(docRef2))).andReturn(true).anyTimes();
    expect(webUtilsMock.resolveDocumentReference(eq(fullname1))).andReturn(docRef1);
    expect(webUtilsMock.resolveDocumentReference(eq(fullname2))).andReturn(docRef2);
    replayDefault();
    XWikiDocument firstDoc = _iterator.getCurrentDoc();
    _iterator.moveToNextDoc();
    XWikiDocument secondDoc = _iterator.getCurrentDoc();
    assertNotSame("moveToNextDoc must update the currentDoc.", firstDoc, secondDoc);
    verifyDefault();
  }

  @Test
  public void testGetCurrentDoc_emptyDocList() {
    _iterator.inject_CurrentDoc(null);
    _iterator.setDocList(new ArrayList<String>());
    replayDefault();
    assertNull(_iterator.getCurrentDoc());
    verifyDefault();
  }

  @Test
  public void testGetDocIterator_noDocList_set() throws Exception {
    try {
      _iterator.getDocIterator();
      fail("Expecting exception.");
    } catch (IllegalStateException ex) {
      // expected behaviour
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
    _docList.add(fullname1);
    _docList.add(fullname2);
    expect(getMock(IModelAccessFacade.class).getDocument(eq(docRef1))).andReturn(_testDoc1)
        .anyTimes();
    expect(getMock(IModelAccessFacade.class).getDocument(eq(docRef2))).andReturn(_testDoc2)
        .anyTimes();
    expect(getMock(IModelAccessFacade.class).exists(eq(docRef1))).andReturn(true).anyTimes();
    expect(getMock(IModelAccessFacade.class).exists(eq(docRef2))).andReturn(true).anyTimes();
    _iterator.setDocList(_docList);
    expect(webUtilsMock.resolveDocumentReference(eq(fullname1))).andReturn(docRef1);
    expect(webUtilsMock.resolveDocumentReference(eq(fullname2))).andReturn(docRef2);
    replayDefault();
    _iterator.next();
    assertTrue(_iterator.hasNext());
    _iterator.next();
    assertFalse(_iterator.hasNext());
    verifyDefault();
  }

  @Test
  public void testHasNext_skipDoc() throws Exception {
    _docList.add(fullname1);
    _docList.add(fullname2);
    expect(getMock(IModelAccessFacade.class).getDocument(eq(docRef1))).andReturn(null).anyTimes();
    expect(getMock(IModelAccessFacade.class).getDocument(eq(docRef2))).andReturn(_testDoc2)
        .anyTimes();
    expect(getMock(IModelAccessFacade.class).exists(eq(docRef1))).andReturn(false).anyTimes();
    expect(getMock(IModelAccessFacade.class).exists(eq(docRef2))).andReturn(true).anyTimes();
    _iterator.setDocList(_docList);
    expect(webUtilsMock.resolveDocumentReference(eq(fullname1))).andReturn(docRef1);
    expect(webUtilsMock.resolveDocumentReference(eq(fullname2))).andReturn(docRef2);
    replayDefault();
    assertTrue(_iterator.hasNext());
    assertNotNull(_iterator.next());
    assertFalse(_iterator.hasNext());
    verifyDefault();
  }

  @Test
  public void testHasNext_false() throws Exception {
    String fullname = "Test.Doc";
    DocumentReference docRef = new DocumentReference(_context.getDatabase(), "Test", "Doc");
    _docList.add(fullname);
    expect(getMock(IModelAccessFacade.class).getDocument(eq(docRef))).andReturn(_testDoc1)
        .anyTimes();
    expect(getMock(IModelAccessFacade.class).exists(eq(docRef))).andReturn(true).anyTimes();
    _iterator.setDocList(_docList);
    expect(webUtilsMock.resolveDocumentReference(eq(fullname))).andReturn(docRef);
    replayDefault();
    _iterator.next();
    assertEquals(false, _iterator.hasNext());
    verifyDefault();
  }

  @Test
  public void testNext_noMoreElementsException() throws Exception {
    _iterator.setDocList(_docList);
    replayDefault();
    try {
      _iterator.next();
      fail("Exception expected.");
    } catch (NoSuchElementException ex) {
      // expected behaviour
    }
    verifyDefault();
  }

  @Test
  public void testRemove() {
    try {
      _iterator.remove();
      fail("Remove not supported.");
    } catch (UnsupportedOperationException ex) {
      // expected behaviour
    }
  }

  @Test
  public void testIterator_foreach() throws Exception {
    _docList.add(fullname1);
    _docList.add(fullname2);
    expect(getMock(IModelAccessFacade.class).getDocument(eq(docRef1))).andReturn(_testDoc1);
    expect(getMock(IModelAccessFacade.class).getDocument(eq(docRef2))).andReturn(_testDoc2);
    expect(getMock(IModelAccessFacade.class).exists(eq(docRef1))).andReturn(true).anyTimes();
    expect(getMock(IModelAccessFacade.class).exists(eq(docRef2))).andReturn(true).anyTimes();
    _iterator.setDocList(_docList);
    expect(webUtilsMock.resolveDocumentReference(eq(fullname1))).andReturn(docRef1);
    expect(webUtilsMock.resolveDocumentReference(eq(fullname2))).andReturn(docRef2);
    replayDefault();
    int count = 0;
    List<String> resultList = new ArrayList<>();
    for (XWikiDocument doc : _iterator) {
      resultList.add(doc.getFullName());
      count++;
    }
    assertEquals(resultList, _docList);
    assertEquals(2, count);
    verifyDefault();
  }

  // *****************************************************************
  // * H E L P E R - M E T H O D S *
  // *****************************************************************/

}
