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

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.iterator.DocumentIterator;
import com.celements.iterator.IIteratorFactory;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public class ContentInheritorTest extends AbstractBridgedComponentTestCase {

  private ContentInheritor _contentInheritor;
  private IIteratorFactory<DocumentIterator> _iteratorFactory;
  private XWikiContext _context;
  private XWiki _xwiki;
  private List<String> _docList;
  
  @Before
  public void setUp_ContentInheritorTest() throws Exception {
    _context = getContext();
    _xwiki = createMock(XWiki.class);
    _context.setWiki(_xwiki);
    _contentInheritor = new ContentInheritor();
    _docList = new ArrayList<String>();
    _iteratorFactory = getTestIteratorFactory(_docList);
  }
  
  @Test
  public void testSetEmptyDocumentChecker() {
    IEmptyDocumentChecker emptyDocumentChecker = new DefaultEmptyDocumentChecker();
    _contentInheritor.setEmptyDocumentChecker(emptyDocumentChecker);
    assertEquals(emptyDocumentChecker, _contentInheritor.getEmptyDocumentChecker());
  }
  
  @Test
  public void testGetEmptyDocumentChecker() {
    assertTrue(_contentInheritor.getEmptyDocumentChecker() != null);
    assertTrue(_contentInheritor.getEmptyDocumentChecker() instanceof DefaultEmptyDocumentChecker);
  }

  @Test
  public void testGetTitle() throws Exception {
    String fullname1 = "Test.Doc1";
    String title1 = "Title1";
    _docList.add(fullname1);
    XWikiDocument testDoc1 = new XWikiDocument();
    testDoc1.setFullName(fullname1);
    testDoc1.setTitle(title1);
    expect(_xwiki.getDocument(eq(fullname1), same(_context))).andReturn(
        testDoc1).anyTimes();
    expect(_xwiki.exists(eq(fullname1), same(_context))).andReturn(
        true).anyTimes();
    _contentInheritor.setIteratorFactory(_iteratorFactory);
    replay(_xwiki);
    assertSame("Expecting title.", title1, _contentInheritor.getTitle());
    verify(_xwiki);
  }
  
  @Test
  public void testGetTranslatedTitle() throws Exception {
    String fullname = "Test.Doc";
    String title_de = "Deutscher Titel";
    _docList.add(fullname);
    XWikiDocument translatedDoc1 = new XWikiDocument();
    translatedDoc1.setTitle(title_de);
    XWikiDocument testDoc1 = createMock(XWikiDocument.class);
    expect(testDoc1.getTranslatedDocument(eq("de"), same(_context))).andReturn(
        translatedDoc1).anyTimes();
    expect(testDoc1.getDefaultLanguage()).andReturn("en").anyTimes();
    expect(_xwiki.getDocument(eq(fullname), same(_context))).andReturn(
        testDoc1).anyTimes();
    expect(_xwiki.exists(eq(fullname), same(_context))).andReturn(
        true).anyTimes();
    _contentInheritor.setIteratorFactory(_iteratorFactory);
    _contentInheritor.setLanguage("de");
    replay(testDoc1);
    replay(_xwiki);
    assertSame("Expecting german title.", title_de, 
        _contentInheritor.getTranslatedTitle(_context));
    verify(_xwiki);
  }

  @Test
  public void testGetContent() throws XWikiException {
    String fullname1 = "Test.Doc1";
    String content1 = "Content1";
    _docList.add(fullname1);
    XWikiDocument testDoc1 = new XWikiDocument();
    testDoc1.setFullName(fullname1);
    testDoc1.setContent(content1);
    expect(_xwiki.getDocument(eq(fullname1), same(_context))).andReturn(
        testDoc1).anyTimes();
    expect(_xwiki.exists(eq(fullname1), same(_context))).andReturn(
        true).anyTimes();
    _contentInheritor.setIteratorFactory(_iteratorFactory);
    replay(_xwiki);
    assertSame("Expecting content.", content1, _contentInheritor.getContent());
    verify(_xwiki);
  }
  
  @Test
  public void testGetTranslatedContent() throws Exception {
    String fullname = "Test.Doc";
    String content_de = "Deutscher Inhalt";
    _docList.add(fullname);
    XWikiDocument translatedDoc1 = new XWikiDocument();
    translatedDoc1.setContent(content_de);
    XWikiDocument testDoc1 = createMock(XWikiDocument.class);
    expect(testDoc1.getTranslatedDocument(eq("de"), same(_context))).andReturn(
        translatedDoc1).anyTimes();
    expect(testDoc1.getDefaultLanguage()).andReturn("en").anyTimes();
    expect(_xwiki.getDocument(eq(fullname), same(_context))).andReturn(
        testDoc1).anyTimes();
    expect(_xwiki.exists(eq(fullname), same(_context))).andReturn(
        true).anyTimes();
    _contentInheritor.setIteratorFactory(_iteratorFactory);
    _contentInheritor.setLanguage("de");
    replay(testDoc1);
    replay(_xwiki);
    assertSame("Expecting german content.", content_de, 
        _contentInheritor.getTranslatedContent(_context));
    verify(_xwiki);
  }
  
  @Test
  public void testGetTitle_defaultValue(){ 
    _contentInheritor.setIteratorFactory(_iteratorFactory);
    assertEquals("",_contentInheritor.getTitle());
    assertEquals("",_contentInheritor.getTitle(""));
    assertEquals("-",_contentInheritor.getTitle("-"));
    assertEquals(null,_contentInheritor.getTitle(null));
  }
  
  @Test
  public void testGetTitle_noSuchObject(){ 
    _contentInheritor.setIteratorFactory(_iteratorFactory);
    assertEquals("",_contentInheritor.getTitle());
  }
  
  @Test
  public void testGetTitle_noIteratorFactory(){
    try {
      _contentInheritor.getTitle();
      fail("Expecting exception.");
    } catch(IllegalStateException ex) {
      //expected behaviour
    }
  }
  
  @Test
  public void testGetContent_defaultValue(){ 
    _contentInheritor.setIteratorFactory(_iteratorFactory);
    assertEquals("",_contentInheritor.getContent());
    assertEquals("",_contentInheritor.getContent(""));
    assertEquals("-",_contentInheritor.getContent("-"));
    assertEquals(null,_contentInheritor.getContent(null));
  }
  
  @Test
  public void testGetContent_noSuchObject(){ 
    _contentInheritor.setIteratorFactory(_iteratorFactory);
    assertEquals("",_contentInheritor.getContent());
  }
  
  @Test
  public void testGetContent_noIteratorFactory(){
    try {
      _contentInheritor.getContent();
      fail("Expecting exception.");
    } catch(IllegalStateException ex) {
      //expected behaviour
    }
  }
  
  //*****************************************************************
  //*                  H E L P E R  - M E T H O D S                 *
  //*****************************************************************/
  
  private IIteratorFactory<DocumentIterator> getTestIteratorFactory(final List<String> docList) {
    return new IIteratorFactory<DocumentIterator>() {
      public DocumentIterator createIterator() {
        DocumentIterator iterator = new DocumentIterator(getContext());
        iterator.setDocList(docList);
        return iterator;
      }
    };
  }

}
