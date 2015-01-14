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
package com.celements.web.plugin.cmd;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.pagetype.PageTypeClasses;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;

public class CreateDocumentCommandTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private CreateDocumentCommand createDocumentCmd;
  private IWebUtilsService webServiceMock;

  @Before
  public void setUp_CreateDocumentCommandTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    expect(xwiki.isVirtualMode()).andReturn(true).anyTimes();
    createDocumentCmd = new CreateDocumentCommand();
    webServiceMock = createMock(IWebUtilsService.class);
    createDocumentCmd.injected_webService = webServiceMock;
    expect(webServiceMock.getDefaultLanguage()).andReturn("de").anyTimes();
  }

  @Test
  public void testInitNewXWikiDocument() {
    String authorUser = "XWiki.MyAuthor";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myNewDocument");
    XWikiDocument theNewDoc = new XWikiDocument(docRef);
    replayAll();
    //context.setUser calls xwiki.isVirtualMode in xwiki version 4.5 thus why it must be
    //set after calling replay
    context.setUser(authorUser);
    Date beforeDate = new Date();
    createDocumentCmd.initNewXWikiDocument(theNewDoc);
    Date afterDate = new Date();
    assertEquals("de", theNewDoc.getDefaultLanguage());
    assertEquals("", theNewDoc.getLanguage());
    Date creationDate = theNewDoc.getCreationDate();
    assertTrue("XWiki floors the date given. Thus we compare on the difference."
        + " beforeDate: " + beforeDate.getTime() + " creationDate: "
        + creationDate.getTime(), Math.abs(creationDate.getTime() - beforeDate.getTime()
            ) < 1000);
    assertTrue(afterDate.compareTo(creationDate) + "afterDate: " + afterDate
        + " creationDate: " + creationDate, afterDate.after(creationDate));
    assertEquals(creationDate, theNewDoc.getContentUpdateDate());
    assertEquals(creationDate, theNewDoc.getDate());
    assertEquals(authorUser, theNewDoc.getAuthor());
    assertEquals(authorUser, theNewDoc.getCreator());
    assertEquals(0, theNewDoc.getTranslation());
    assertEquals("", theNewDoc.getContent());
    assertTrue(theNewDoc.isMetaDataDirty());
    verifyAll();
  }

  @Test
  public void testCreateDocument_exists() throws Exception {
    String pageType = "RichText";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myNewDocument");
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(true).once();
    replayAll();
    assertNull("only create document if NOT exists.",
        createDocumentCmd.createDocument(docRef, pageType));
    verifyAll();
  }

  @Test
  public void testCreateDocument_noPageType() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myNewDocument");
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(false).once();
    XWikiDocument theNewDoc = new XWikiDocument(docRef);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(theNewDoc).once();
    xwiki.saveDocument(same(theNewDoc), eq("init document"), eq(false), same(context));
    expectLastCall().once();
    replayAll();
    XWikiDocument theDoc = createDocumentCmd.createDocument(docRef, null);
    assertNotNull(theDoc);
    assertSame(theNewDoc, theDoc);
    verifyAll();
  }

  @Test
  public void testCreateDocument() throws Exception {
    String pageType = "RichText";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myNewDocument");
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(false).once();
    XWikiDocument theNewDoc = new XWikiDocument(docRef);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(theNewDoc).once();
    DocumentReference pageTypeClassRef = new DocumentReference(
        getContext().getDatabase(), PageTypeClasses.PAGE_TYPE_CLASS_SPACE,
        PageTypeClasses.PAGE_TYPE_CLASS_DOC);
    BaseClass pageTypeClassMock = createMock(BaseClass.class);
    expect(xwiki.getXClass(pageTypeClassRef, context)).andReturn(pageTypeClassMock
        ).once();
    xwiki.saveDocument(same(theNewDoc), eq("init RichText-document"), eq(false),
        same(context));
    expectLastCall().once();
    BaseObject newPageTypeObj = new BaseObject();
    newPageTypeObj.setXClassReference(pageTypeClassRef);
    expect(pageTypeClassMock.newCustomClassInstance(same(context))).andReturn(
        newPageTypeObj);
    replayAll(pageTypeClassMock);
    XWikiDocument theDoc = createDocumentCmd.createDocument(docRef, pageType);
    assertNotNull(theDoc);
    BaseObject ptObj = theDoc.getXObject(pageTypeClassRef);
    assertNotNull(ptObj);
    assertEquals("RichText", ptObj.getStringValue("page_type"));
    verifyAll(pageTypeClassMock);
  }

  @Test
  public void testCreateDocument_differentDB() throws Exception {
    String pageType = "RichText";
    String database = "otherDB";
    DocumentReference docRef = new DocumentReference(database, "mySpace", "myNewDocument");
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(false).once();
    XWikiDocument theNewDoc = new XWikiDocument(docRef);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(theNewDoc).once();
    DocumentReference pageTypeClassRef = new DocumentReference(database, 
        PageTypeClasses.PAGE_TYPE_CLASS_SPACE, PageTypeClasses.PAGE_TYPE_CLASS_DOC);
    BaseClass pageTypeClassMock = createMock(BaseClass.class);
    expect(xwiki.getXClass(pageTypeClassRef, context)).andReturn(pageTypeClassMock
        ).once();
    xwiki.saveDocument(same(theNewDoc), eq("init RichText-document"), eq(false),
        same(context));
    expectLastCall().once();
    BaseObject newPageTypeObj = new BaseObject();
    newPageTypeObj.setXClassReference(pageTypeClassRef);
    expect(pageTypeClassMock.newCustomClassInstance(same(context))).andReturn(
        newPageTypeObj);
    replayAll(pageTypeClassMock);
    XWikiDocument theDoc = createDocumentCmd.createDocument(docRef, pageType);
    assertNotNull(theDoc);
    BaseObject ptObj = theDoc.getXObject(pageTypeClassRef);
    assertNotNull(ptObj);
    assertEquals("RichText", ptObj.getStringValue("page_type"));
    verifyAll(pageTypeClassMock);
  }

  @Test
  public void testCreateDocument_noSave() throws Exception {
    String pageType = "RichText";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myNewDocument");
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(false).once();
    XWikiDocument theNewDoc = new XWikiDocument(docRef);
    expect(xwiki.getDocument(eq(docRef), same(context))).andReturn(theNewDoc).once();
    DocumentReference pageTypeClassRef = new DocumentReference(
        getContext().getDatabase(), PageTypeClasses.PAGE_TYPE_CLASS_SPACE,
        PageTypeClasses.PAGE_TYPE_CLASS_DOC);
    BaseClass pageTypeClassMock = createMock(BaseClass.class);
    expect(xwiki.getXClass(pageTypeClassRef, context)).andReturn(pageTypeClassMock
        ).once();
    BaseObject newPageTypeObj = new BaseObject();
    newPageTypeObj.setXClassReference(pageTypeClassRef);
    expect(pageTypeClassMock.newCustomClassInstance(same(context))).andReturn(
        newPageTypeObj);
    replayAll(pageTypeClassMock);
    XWikiDocument theDoc = createDocumentCmd.createDocument(docRef, pageType, false);
    assertNotNull(theDoc);
    BaseObject ptObj = theDoc.getXObject(pageTypeClassRef);
    assertNotNull(ptObj);
    assertEquals("RichText", ptObj.getStringValue("page_type"));
    verifyAll(pageTypeClassMock);
  }

  @Test
  public void testCreateDocument_XWE() throws Exception {
    String pageType = "RichText";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myNewDocument");
    expect(xwiki.exists(eq(docRef), same(context))).andReturn(false).once();
    expect(xwiki.getDocument(eq(docRef), same(context))).andThrow(new XWikiException()
    ).once();
    replayAll();
    try {
      createDocumentCmd.createDocument(docRef, pageType, false);
      fail("expecting XWE");
    } catch (XWikiException xwe) {
      // expected
    }
    verifyAll();
  }

  private void replayAll(Object ... mocks) {
    replay(xwiki, webServiceMock);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, webServiceMock);
    verify(mocks);
  }

}
