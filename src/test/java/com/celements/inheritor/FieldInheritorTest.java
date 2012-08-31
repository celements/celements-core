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
import com.celements.iterator.IIteratorFactory;
import com.celements.iterator.XObjectIterator;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class FieldInheritorTest extends AbstractBridgedComponentTestCase {
  
  private FieldInheritor _fieldInheritor;
  private IIteratorFactory<XObjectIterator> _iteratorFactory;
  private XWikiContext _context;
  private XWiki _xwiki;
  private DocumentReference _testClassRef;
  private XWikiDocument _testDoc;
  private List<String> _docList;
  private String _fullname;
  private DocumentReference _docRef;
  
  @Before
  public void setUp_FieldInheritorTest() throws Exception {
    _context = getContext();
    _xwiki = createMock(XWiki.class);
    _context.setWiki(_xwiki);
    _testClassRef = new DocumentReference(_context.getDatabase(), "Celements",
        "TestClass");
    _fieldInheritor = new FieldInheritor();
    _fullname = "Test.Doc";
    _docRef = new DocumentReference(_context.getDatabase(), "Test", "Doc");
    _testDoc = new XWikiDocument(_docRef);
    _docList = new ArrayList<String>();
    _iteratorFactory = getTestIteratorFactory(_docList);
  }
  
  @Test
  public void testSetEmptyFieldChecker() {
    IEmptyFieldChecker emptyFieldChecker = new DefaultEmptyFieldChecker();
    _fieldInheritor.setEmptyFieldChecker(emptyFieldChecker);
    assertEquals(emptyFieldChecker, _fieldInheritor.getEmptyFieldChecker());
  }
  
  @Test
  public void testGetEmptyFieldChecker() {
    assertTrue(_fieldInheritor.getEmptyFieldChecker() != null);
    assertTrue(_fieldInheritor.getEmptyFieldChecker() instanceof DefaultEmptyFieldChecker);
  }

  @Test
  public void testGetObject() throws Exception{
    _docList.add(_fullname);
    BaseObject firstObj = new BaseObject();
    firstObj.setXClassReference(_testClassRef);
    firstObj.setStringValue("field1", "value1");
    _testDoc.addXObject(firstObj);
    BaseObject secondObj = new BaseObject();
    secondObj.setXClassReference(_testClassRef);
    secondObj.setStringValue("field2", "value2");
    _testDoc.addXObject(secondObj);
    expect(_xwiki.getDocument(eq(_fullname), same(_context))).andReturn(_testDoc
        ).anyTimes();
    _fieldInheritor.setIteratorFactory(_iteratorFactory);
    replay(_xwiki);      
    assertSame("Expecting second object.", secondObj,
        _fieldInheritor.getObject("field2"));
    assertSame("Expecting first object.", firstObj,
        _fieldInheritor.getObject("field1"));
    verify(_xwiki);
  }
  
  @Test
  public void testGetObject_noSuchObject(){ 
    _fieldInheritor.setIteratorFactory(_iteratorFactory);
    assertEquals(null,_fieldInheritor.getObject("myField"));
  }
  
  @Test
  public void testGetObject_noIteratorFactory(){
    try {
      _fieldInheritor.getObject("myField");
      fail("Expecting exception.");
    } catch(IllegalStateException ex) {
      //expected behaviour
    }
  }
  
  //*****************************************************************
  //*                  H E L P E R  - M E T H O D S                 *
  //*****************************************************************/
  
  private IIteratorFactory<XObjectIterator> getTestIteratorFactory(
      final List<String> docList) {
    return new IIteratorFactory<XObjectIterator>() {
      public XObjectIterator createIterator() {
        XObjectIterator iterator = new XObjectIterator(_context);
        iterator.setClassName(_testClassRef.getLastSpaceReference().getName() + "."
            + _testClassRef.getName());
        iterator.setDocList(docList);
        return iterator;
      }
    };
  }
}
