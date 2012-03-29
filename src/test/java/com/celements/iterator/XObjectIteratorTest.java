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
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class XObjectIteratorTest extends AbstractBridgedComponentTestCase {

  private XObjectIterator _iterator;
  private XWikiContext _context;
  private XWiki _xwiki;
  private String _testClassName;
  private XWikiDocument _testDoc;
  private List<String> _docList;

  @Before
  public void setUp_XObjectIteratorTest() throws Exception {
    _context = getContext();
    _xwiki = createMock(XWiki.class);
    _context.setWiki(_xwiki);
    _testClassName = "Celements.TestClass";
    _iterator = new XObjectIterator(_context);
    _iterator.setClassName(_testClassName);
    _testDoc = new XWikiDocument();
    _docList = new ArrayList<String>();
  }  

  @Test
  public void testSetClassName() {
    String className = "myTestClass";
    _iterator.setClassName(className);
    assertEquals(className, _iterator.getClassName());
  }
  
  @Test
  public void testSetFilter() {
    String key = "key";
    Object value = 1;
    _iterator.setFilter(key, value);
    assertEquals(key, _iterator.getFilterKey());
    assertEquals(value, _iterator.getFilterValue());
  }
  
  @Test
  public void testSetDocList() {
    _docList.add("Test.Doc");
    _docList.add("Test.Doc2");
    _iterator.setDocList(_docList);
    assertEquals(_docList, _iterator.getDocList());
  }

  @Test
  public void testSetDocList_resetIterators() throws Exception {
    _docList.add("Test.Doc");
    _iterator.setDocList(_docList);
    _iterator.inject_CurrentDoc(_testDoc);
    Iterator<String> firstDocIt = _iterator.getDocIterator();
    Iterator<BaseObject> firstObjIt = _iterator.getObjectIterator();
    _iterator.setDocList(_docList);
    assertNotSame(firstDocIt, _iterator.getDocIterator());
    assertNotSame(firstObjIt, _iterator.getObjectIterator());
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
  public void testMoveToNextDoc_resetObjectIterator() throws Exception {
    _iterator.inject_CurrentDoc(null); // reset Iterator
    String fullname = "Test.Doc";
    _docList.add(fullname);
    String fullname2 = "Test.Doc2";
    _docList.add(fullname2);
    _iterator.setDocList(_docList);
    expect(_xwiki.getDocument(eq(fullname), same(_context))).andReturn(_testDoc
      ).anyTimes();
    expect(_xwiki.getDocument(eq(fullname2), same(_context))).andReturn(_testDoc
      ).anyTimes();
    replay(_xwiki);
    Iterator<BaseObject> firstObjectIterator = _iterator.getObjectIterator();
    _iterator.moveToNextDoc();
    Iterator<BaseObject> secondObjectIterator = _iterator.getObjectIterator();
    assertNotSame("moveToNextDoc must reset the ObjectIterator.",
        firstObjectIterator, secondObjectIterator);
    verify(_xwiki);
  }

  @Test
  public void testMoveToNextDoc_updateCurrentDoc() throws Exception {
    _iterator.inject_CurrentDoc(null); // reset Iterator
    String fullname = "Test.Doc";
    _docList.add(fullname);
    String fullname2 = "Test.Doc2";
    _docList.add(fullname2);
    _iterator.setDocList(_docList);
    expect(_xwiki.getDocument(eq(fullname), same(_context))).andReturn(new XWikiDocument()
      ).once();
    expect(_xwiki.getDocument(eq(fullname2), same(_context))).andReturn(
      new XWikiDocument()).once();
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
  public void testGetObjectsForCurrentDoc() throws Exception {
    _docList.add("Test.Doc");
    _iterator.setDocList(_docList);
    _iterator.inject_CurrentDoc(_testDoc);
    assertNotNull("getObjects on XWikiDocument returns null if object-list for given"
        + " class is not defined.", _iterator.getObjectsForCurrentDoc());
  }

  @Test
  public void testGetObjectsForCurrentDoc_emptyDocList() throws Exception {
    _iterator.setDocList(new ArrayList<String>());
    _iterator.inject_CurrentDoc(null); // reset currentDoc
    assertNotNull("Empty object List is expected for an empty doc list.",
        _iterator.getObjectsForCurrentDoc());
  }

  @Test
  public void testGetObjectIterator() throws Exception {
    _docList.add("Test.Doc");
    _iterator.setDocList(_docList);
    _iterator.inject_CurrentDoc(_testDoc);
    assertSame(_iterator.getObjectIterator(), _iterator.getObjectIterator());
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
    String fullname = "Test.Doc";
    _docList.add(fullname);
    Vector<BaseObject> testObjs = new Vector<BaseObject>();
    BaseObject firstObj = new BaseObject();
    testObjs.add(firstObj);
    BaseObject secondObj = new BaseObject();
    testObjs.add(secondObj);
    _testDoc.setObjects(_testClassName, testObjs);
    expect(_xwiki.getDocument(eq(fullname), same(_context))).andReturn(_testDoc
        ).anyTimes();
    _iterator.setDocList(_docList);
    replay(_xwiki);
    _iterator.next();
    assertTrue(_iterator.hasNext());
    _iterator.next();
    assertFalse(_iterator.hasNext());
    verify(_xwiki);
  }
  
  @Test
  public void testHasNext_false() throws Exception {
    String fullname = "Test.Doc";
    _docList.add(fullname);
    Vector<BaseObject> testObjs = new Vector<BaseObject>();
    BaseObject firstObj = new BaseObject();
    testObjs.add(firstObj);
    _testDoc.setObjects(_testClassName, testObjs);
    expect(_xwiki.getDocument(eq(fullname), same(_context))).andReturn(_testDoc
        ).anyTimes();
    _iterator.setDocList(_docList);
    replay(_xwiki);
    _iterator.next();
    assertEquals(false, _iterator.hasNext());
    verify(_xwiki); 
  }
  
  @Test
  public void testHasNext_classNameNotDefined() throws Exception {
    _iterator.setClassName(null);
    String fullname = "Test.Doc";
    _docList.add(fullname);
    Vector<BaseObject> testObjs = new Vector<BaseObject>();
    BaseObject firstObj = new BaseObject();
    testObjs.add(firstObj);
    _testDoc.setObjects(_testClassName, testObjs);
    expect(_xwiki.getDocument(eq(fullname), same(_context))).andReturn(_testDoc
        ).anyTimes();
    _iterator.setDocList(_docList);
    replay(_xwiki);
    try {
      _iterator.hasNext();
      fail("Expecting exception.");
    } catch(IllegalStateException ex) {
      //expected behaviour
    }
    verify(_xwiki);
  }

  @Test
  public void testNext_allObjOneDoc() throws Exception {
    String fullname = "Test.Doc";
    _docList.add(fullname);
    Vector<BaseObject> testObjs = new Vector<BaseObject>();
    BaseObject firstObj = new BaseObject();
    testObjs.add(firstObj);
    BaseObject secondObj = new BaseObject();
    testObjs.add(secondObj);
    _testDoc.setObjects(_testClassName, testObjs);
    expect(_xwiki.getDocument(eq(fullname), same(_context))).andReturn(_testDoc
        ).anyTimes();
    _iterator.setDocList(_docList);
    replay(_xwiki);
    assertSame("Expecting first object in list.", firstObj, _iterator.next());
    assertSame("Expecting second object in list.", secondObj, _iterator.next());
    verify(_xwiki);
  }
  
  @Test
  public void testNext_allObjMultipleDoc() throws Exception {
    String fullname = "Test.Doc";
    _docList.add(fullname);
    String fullname2 = "Test.Doc2";
    _docList.add(fullname2);
    Vector<BaseObject> testObjs = new Vector<BaseObject>();
    BaseObject firstObj = new BaseObject();
    testObjs.add(firstObj);
    BaseObject secondObj = new BaseObject();
    testObjs.add(secondObj);
    _testDoc.setObjects(_testClassName, testObjs);
    expect(_xwiki.getDocument(eq(fullname), same(_context))).andReturn(_testDoc
        ).anyTimes();
    Vector<BaseObject> testObjs2 = new Vector<BaseObject>();
    BaseObject firstObj2 = new BaseObject();
    testObjs2.add(firstObj2);
    BaseObject secondObj2 = new BaseObject();
    testObjs2.add(secondObj2);
    XWikiDocument testDoc2 = new XWikiDocument();
    testDoc2.setFullName(fullname2);
    testDoc2.setObjects(_testClassName, testObjs2);
    expect(_xwiki.getDocument(eq(fullname2), same(_context))).andReturn(testDoc2
        ).anyTimes();
    _iterator.setDocList(_docList);
    replay(_xwiki);
    assertSame("Expecting first object in list.", firstObj, _iterator.next());
    assertSame("Expecting second object in list.", secondObj, _iterator.next());
    assertSame("Expecting third object in list.", firstObj2, _iterator.next());
    assertSame("Expecting forth object in list.", secondObj2, _iterator.next());
    assertFalse(_iterator.hasNext());
    verify(_xwiki);
  }
  
  @Test
  public void testNext_emptyDocsInList() throws Exception {
    String fullname0 = "Test.Doc0";
    _docList.add(fullname0);
    String fullname = "Test.Doc";
    _docList.add(fullname);
    String fullname2 = "Test.Doc2";
    _docList.add(fullname2);
    Vector<BaseObject> testObjs = new Vector<BaseObject>();
    BaseObject firstObj = new BaseObject();
    testObjs.add(firstObj);
    BaseObject secondObj = new BaseObject();
    testObjs.add(secondObj);
    _testDoc.setObjects(_testClassName, testObjs);
    expect(_xwiki.getDocument(eq(fullname), same(_context))).andReturn(_testDoc
        ).anyTimes();
    XWikiDocument testDoc0 = new XWikiDocument();
    testDoc0.setFullName(fullname0);
    expect(_xwiki.getDocument(eq(fullname0), same(_context))).andReturn(testDoc0
        ).anyTimes();
    XWikiDocument testDoc2 = new XWikiDocument();
    testDoc2.setFullName(fullname2);
    expect(_xwiki.getDocument(eq(fullname2), same(_context))).andReturn(testDoc2
        ).anyTimes();
    _iterator.setDocList(_docList);
    replay(_xwiki);
    assertSame("Expecting first object in list.", firstObj, _iterator.next());
    assertSame("Expecting second object in list.", secondObj, _iterator.next());
    assertFalse(_iterator.hasNext());
    verify(_xwiki);
  }
  
  @Test
  public void testNext_deletedNullObjectsInList() throws Exception {
    String fullname = "Test.Doc";
    _docList.add(fullname);
    Vector<BaseObject> testObjs = new Vector<BaseObject>();
    BaseObject firstObj = new BaseObject();
    testObjs.add(firstObj);
    testObjs.add(null); // deleted second object
    BaseObject thirdObject = new BaseObject();
    testObjs.add(thirdObject);
    testObjs.add(null); // deleted fourth object
    _testDoc.setObjects(_testClassName, testObjs);
    expect(_xwiki.getDocument(eq(fullname), same(_context))).andReturn(_testDoc
        ).anyTimes();
    _iterator.setDocList(_docList);
    replay(_xwiki);
    assertSame("Expecting first object in list.", firstObj, _iterator.next());
    assertSame("Expecting third object in list (skipping deleted second one).",
        thirdObject, _iterator.next());
    assertFalse(_iterator.hasNext());
    verify(_xwiki);
  }
  
  @Test
  public void testNext_classNameNotDefined() throws Exception {
    _iterator.setClassName(null);
    String fullname = "Test.Doc";
    _docList.add(fullname);
    Vector<BaseObject> testObjs = new Vector<BaseObject>();
    BaseObject firstObj = new BaseObject();
    testObjs.add(firstObj);
    _testDoc.setObjects(_testClassName, testObjs);
    expect(_xwiki.getDocument(eq(fullname), same(_context))).andReturn(_testDoc
        ).anyTimes();
    _iterator.setDocList(_docList);
    replay(_xwiki);
    try {
      _iterator.next();
      fail("Expecting exception.");
    } catch(IllegalStateException ex) {
      //expected behaviour
    }
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
  public void testNext_withFilter() throws Exception{
    String fullname = "Test.Doc";
    _docList.add(fullname);
    String fullname2 = "Test.Doc2";
    _docList.add(fullname2);
    Vector<BaseObject> testObjs = new Vector<BaseObject>();
    BaseObject firstObj = new BaseObject();
    testObjs.add(firstObj);
    BaseObject secondObj = new BaseObject();
    secondObj.setStringValue("key1", "value1");
    testObjs.add(secondObj);
    _testDoc.setObjects(_testClassName, testObjs);
    expect(_xwiki.getDocument(eq(fullname), same(_context))).andReturn(_testDoc
        ).anyTimes();
    Vector<BaseObject> testObjs2 = new Vector<BaseObject>();
    BaseObject firstObj2 = new BaseObject();
    testObjs.add(firstObj2);
    BaseObject secondObj2 = new BaseObject();
    testObjs.add(secondObj2);
    XWikiDocument testDoc2 = new XWikiDocument();
    testDoc2.setFullName(fullname2);
    testDoc2.setObjects(_testClassName, testObjs2);
    expect(_xwiki.getDocument(eq(fullname2), same(_context))).andReturn(testDoc2
        ).anyTimes();
    _iterator.setDocList(_docList);
    replay(_xwiki);
    _iterator.setFilter("key1", "value1");
    assertTrue(_iterator.hasNext());
    assertSame("Expecting secondObj.", secondObj, _iterator.next());
    assertFalse(_iterator.hasNext());
    verify(_xwiki);
  }
  
  @Test
  public void testValidObject(){
    BaseObject firstObj = new BaseObject();
    firstObj.setStringValue("key1", "value1");
    _iterator.inject_NextObject(firstObj);
    _iterator.setFilter("key1", "value1");
    assertTrue(_iterator.isValidObject());
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
    String fullname = "Test.Doc";
    _docList.add(fullname);
    Vector<BaseObject> testObjs = new Vector<BaseObject>();
    BaseObject firstObj = new BaseObject();
    testObjs.add(firstObj);
    BaseObject secondObj = new BaseObject();
    testObjs.add(secondObj);
    BaseObject thirdObj = new BaseObject();
    testObjs.add(thirdObj);
    _testDoc.setObjects(_testClassName, testObjs);
    expect(_xwiki.getDocument(eq(fullname), same(_context))).andReturn(_testDoc
        ).anyTimes();
    _iterator.setDocList(_docList);
    replay(_xwiki);
    int count = 0;
    Vector<BaseObject> resultObjs = new Vector<BaseObject>();
    for (BaseObject obj : _iterator) {
      resultObjs.add(obj);
      count++;
    }
    assertEquals(resultObjs, testObjs);
    assertEquals(3, count);
    verify(_xwiki);
  }

//TODO fix XObjectIterator for this test
//  @Test
//  public void testIterator_foreach_noElements() throws Exception {
//    String fullname = "Test.Doc";
//    _docList.add(fullname);
//    expect(_xwiki.getDocument(eq(fullname), same(_context))).andReturn(_testDoc
//        ).anyTimes();
//    _iterator.setDocList(_docList);
//    replay(_xwiki);
//    int count = 0;
//    for (BaseObject obj : _iterator) {
//      count++;
//    }
//    assertEquals(0, count);
//    verify(_xwiki);
//  }

  //*****************************************************************
  //*                  H E L P E R  - M E T H O D S                 *
  //*****************************************************************/

}
