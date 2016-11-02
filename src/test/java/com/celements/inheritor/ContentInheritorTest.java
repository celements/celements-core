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

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.iterator.DocumentIterator;
import com.celements.iterator.IIteratorFactory;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public class ContentInheritorTest extends AbstractComponentTest {

  private ContentInheritor contentInheritor;
  private IIteratorFactory<DocumentIterator> iteratorFactory;
  private XWikiContext context;
  private XWiki xwiki;
  private List<String> docList;

  @Before
  public void setUp_ContentInheritorTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    contentInheritor = new ContentInheritor();
    docList = new ArrayList<>();
    iteratorFactory = getTestIteratorFactory(docList);
  }

  @Test
  public void testSetEmptyDocumentChecker() {
    IEmptyDocumentChecker emptyDocumentChecker = new DefaultEmptyDocumentChecker();
    contentInheritor.setEmptyDocumentChecker(emptyDocumentChecker);
    assertEquals(emptyDocumentChecker, contentInheritor.getEmptyDocumentChecker());
  }

  @Test
  public void testGetEmptyDocumentChecker() {
    assertTrue(contentInheritor.getEmptyDocumentChecker() != null);
    assertTrue(contentInheritor.getEmptyDocumentChecker() instanceof DefaultEmptyDocumentChecker);
  }

  @Test
  public void testGetTitle() throws Exception {
    context.setLanguage("de");
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Test", "Doc1");
    String title1 = "Title1";
    docList.add("Test.Doc1");
    XWikiDocument testDoc1 = new XWikiDocument(docRef);
    testDoc1.setDefaultLanguage("de");
    testDoc1.setTitle(title1);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(testDoc1).anyTimes();
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(true).anyTimes();
    contentInheritor.setIteratorFactory(iteratorFactory);
    replayDefault();
    assertEquals("Expecting title.", title1, contentInheritor.getTitle());
    verifyDefault();
  }

  @Test
  public void testGetTranslatedTitle() throws Exception {
    docList.add("Test.Doc");
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Test", "Doc");
    String title_de = "Deutscher Titel";
    XWikiDocument translatedDoc1 = new XWikiDocument(docRef);
    translatedDoc1.setTitle(title_de);
    XWikiDocument testDoc1 = createMockAndAddToDefault(XWikiDocument.class);
    expect(testDoc1.isFromCache()).andReturn(false).atLeastOnce();
    expect(testDoc1.getTranslatedDocument(eq("de"), same(context))).andReturn(
        translatedDoc1).anyTimes();
    expect(testDoc1.getDefaultLanguage()).andReturn("en").anyTimes();
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(testDoc1).anyTimes();
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(true).anyTimes();
    contentInheritor.setIteratorFactory(iteratorFactory);
    contentInheritor.setLanguage("de");
    replayDefault();
    assertEquals("Expecting german title.", title_de, contentInheritor.getTranslatedTitle(context));
    verifyDefault();
  }

  @Test
  public void testGetContent() throws XWikiException {
    context.setLanguage("de");
    docList.add("Test.Doc1");
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Test", "Doc1");
    String content1 = "Content1";
    XWikiDocument testDoc1 = new XWikiDocument(docRef);
    testDoc1.setDefaultLanguage("de");
    testDoc1.setContent(content1);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(testDoc1).anyTimes();
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(true).anyTimes();
    contentInheritor.setIteratorFactory(iteratorFactory);
    replayDefault();
    assertEquals("Expecting content.", content1, contentInheritor.getContent());
    verifyDefault();
  }

  @Test
  public void testGetTranslatedContent() throws Exception {
    docList.add("Test.Doc");
    String content_de = "Deutscher Inhalt";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Test", "Doc");
    XWikiDocument translatedDoc1 = new XWikiDocument(docRef);
    translatedDoc1.setContent(content_de);
    XWikiDocument testDoc1 = createMockAndAddToDefault(XWikiDocument.class);
    expect(testDoc1.isFromCache()).andReturn(false).atLeastOnce();
    expect(testDoc1.getTranslatedDocument(eq("de"), same(context))).andReturn(
        translatedDoc1).anyTimes();
    expect(testDoc1.getDefaultLanguage()).andReturn("en").anyTimes();
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(testDoc1).anyTimes();
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(true).anyTimes();
    contentInheritor.setIteratorFactory(iteratorFactory);
    contentInheritor.setLanguage("de");
    replayDefault();
    assertEquals("Expecting german content.", content_de, contentInheritor.getTranslatedContent(
        context));
    verifyDefault();
  }

  @Test
  public void testGetTitle_defaultValue() {
    contentInheritor.setIteratorFactory(iteratorFactory);
    assertEquals("", contentInheritor.getTitle());
    assertEquals("", contentInheritor.getTitle(""));
    assertEquals("-", contentInheritor.getTitle("-"));
    assertEquals(null, contentInheritor.getTitle(null));
  }

  @Test
  public void testGetTitle_noSuchObject() {
    contentInheritor.setIteratorFactory(iteratorFactory);
    assertEquals("", contentInheritor.getTitle());
  }

  @Test
  public void testGetTitle_noIteratorFactory() {
    try {
      contentInheritor.getTitle();
      fail("Expecting exception.");
    } catch (IllegalStateException ex) {
      // expected behaviour
    }
  }

  @Test
  public void testGetContent_defaultValue() {
    contentInheritor.setIteratorFactory(iteratorFactory);
    assertEquals("", contentInheritor.getContent());
    assertEquals("", contentInheritor.getContent(""));
    assertEquals("-", contentInheritor.getContent("-"));
    assertEquals(null, contentInheritor.getContent(null));
  }

  @Test
  public void testGetContent_noSuchObject() {
    contentInheritor.setIteratorFactory(iteratorFactory);
    assertEquals("", contentInheritor.getContent());
  }

  @Test
  public void testGetContent_noIteratorFactory() {
    try {
      contentInheritor.getContent();
      fail("Expecting exception.");
    } catch (IllegalStateException ex) {
      // expected behaviour
    }
  }

  // *****************************************************************
  // * H E L P E R - M E T H O D S *
  // *****************************************************************/

  private IIteratorFactory<DocumentIterator> getTestIteratorFactory(final List<String> docList) {
    return new IIteratorFactory<DocumentIterator>() {

      @Override
      public DocumentIterator createIterator() {
        DocumentIterator iterator = new DocumentIterator();
        iterator.setDocList(docList);
        return iterator;
      }
    };
  }

}
