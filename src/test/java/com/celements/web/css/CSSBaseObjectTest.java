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
package com.celements.web.css;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.web.utils.IWebUtils;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class CSSBaseObjectTest extends AbstractBridgedComponentTestCase {

  BaseObject bo;
  private XWikiContext context;
  private XWiki xwiki;
  private DocumentReference docRef;

  @Before
  public void setUp_CSSBaseObjectTest() throws Exception {
    context = getContext();
    bo = new BaseObject();
    docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    bo.setDocumentReference(docRef);
    bo.setStringValue("media", "print");
    bo.setIntValue("is_rte_content", 0);
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
  }

  @Test
  public void testGetCSS_celements2web() {
    String attLink = "celements2web:Space.Doc;file.txt";
    String url = "http://celements2web.testhost/skin/Space/Doc/file.txt";
    bo.setStringValue("cssname", attLink);
    CSSBaseObject cssFile = new CSSBaseObject(bo, context);
    IWebUtils webutils = createMock(IWebUtils.class);
    cssFile.testInjectUtils(webutils);
    expect(webutils.getAttachmentURL(eq(attLink), same(context))).andReturn(url).once();
    replay(webutils);
    assertEquals(url, cssFile.getCSS(context));
    verify(webutils);
  }
  
  @Test
  public void testGetCSS_emptyObjField() {
    bo.setStringValue("cssname", "");
    CSSBaseObject cssFile = new CSSBaseObject(bo, context);
    IWebUtils webutils = createMock(IWebUtils.class);
    cssFile.testInjectUtils(webutils);
    expect(webutils.getAttachmentURL(eq(""), same(context))).andReturn("").once();
    replay(webutils);
    assertEquals("", cssFile.getCSS(context));
    verify(webutils);
  }

  @Test
  public void testGetCSS() {
    String attLink = "Space.Doc;file.txt";
    String url = "/skin/Space/Doc/file.txt";
    bo.setStringValue("cssname", attLink);
    CSSBaseObject cssFile = new CSSBaseObject(bo, context);
    IWebUtils webutils = createMock(IWebUtils.class);
    cssFile.testInjectUtils(webutils);
    expect(webutils.getAttachmentURL(eq(context.getDatabase() + ":" + attLink),
        same(context))).andReturn(url).once();
    replay(webutils);
    assertEquals(url, cssFile.getCSS(context));
    verify(webutils);
  }

  @Test
  public void testGetMedia() {
    CSSBaseObject cssFile = new CSSBaseObject(bo, context);
    assertEquals("print", cssFile.getMedia());
  }

  @Test
  public void testCSS_String_notContentCSS() {
    bo.setStringValue("cssname", "/test/file.css");
    CSSBaseObject cssFile = new CSSBaseObject(bo, context);
    assertFalse("Filename not ending in -content.css or _content.css",
        cssFile.isContentCSS());
  }

  @Test
  public void testCSS_String_ContentCSS() {
    bo.setStringValue("cssname", "/test/file-content.css");
    CSSBaseObject cssFile = new CSSBaseObject(bo, context);
    assertTrue("Filename ending in -content.css", cssFile.isContentCSS());
    bo = new BaseObject();
    bo.setStringValue("cssname", "/test/file_content.css");
    CSSBaseObject cssFile2 = new CSSBaseObject(bo, context);
    assertTrue("Filename ending in _content.css", cssFile2.isContentCSS());
  }

  @Test
  public void testIsAttachment() {
    bo.setStringValue("cssname", "XWiki.XWikiPreferences;myAttachment.css");
    CSSBaseObject cssFile = new CSSBaseObject(bo, context);
    assertTrue("attachment FullName-Link must be recognized", cssFile.isAttachment());
    bo.setStringValue("cssname", "/XWiki/XWikiPreferences/myAttachment.css");
    CSSBaseObject cssFile2 = new CSSBaseObject(bo, context);
    assertFalse("attachment normal links should not be recogised.", cssFile2.isAttachment());
  }

  @Test
  public void testGetAttachment() throws Exception {
    IWebUtils mockUtils = createMock(IWebUtils.class);
    String link = "XWiki.XWikiPreferences;myAttachment.css";
    DocumentReference xwikiPrefDocRef = new DocumentReference(context.getDatabase(),
        "XWiki", "XWikiPreferences");
    bo.setStringValue("cssname", "XWiki.XWikiPreferences;myAttachment.css");
    CSSBaseObject cssFile = new CSSBaseObject(bo, context);
    expect(mockUtils.isAttachmentLink(eq(link))).andReturn(true).anyTimes();
    cssFile.testInjectUtils(mockUtils);
    XWikiDocument doc = new XWikiDocument(xwikiPrefDocRef);
    List<XWikiAttachment> attList = new ArrayList<XWikiAttachment>();
    XWikiAttachment att = new XWikiAttachment(doc, "myAttachment.css");
    attList.add(att);
    doc.setAttachmentList(attList);
    expect(xwiki.getDocument(eq(xwikiPrefDocRef), same(context))).andReturn(doc);
    replay(xwiki, mockUtils);
    assertNotNull("attachment must not be null", cssFile.getAttachment());
    verify(xwiki, mockUtils);
  }

  @Test
  public void testGetAttachment_Null() throws Exception {
    IWebUtils mockUtils = createMock(IWebUtils.class);
    String link = "XWiki.XWikiPreferences;myAttachment.css";
    String fullName = "XWiki.XWikiPreferences";
    DocumentReference xwikiPrefDocRef = new DocumentReference(context.getDatabase(),
        "XWiki", "XWikiPreferences");
    bo.setStringValue("cssname", "XWiki.XWikiPreferences;myAttachment.css");
    CSSBaseObject cssFile = new CSSBaseObject(bo, context);
    expect(mockUtils.isAttachmentLink(eq(link))).andReturn(true).anyTimes();
    cssFile.testInjectUtils(mockUtils);
    XWikiDocument doc = new XWikiDocument(xwikiPrefDocRef);
    List<XWikiAttachment> attList = new ArrayList<XWikiAttachment>();
    doc.setAttachmentList(attList);
    expect(xwiki.getDocument(eq(xwikiPrefDocRef), same(context))).andReturn(doc);
    replay(xwiki, mockUtils);
    assertNull("attachment must be null, if attachment does not exist",
        cssFile.getAttachment());
    verify(xwiki, mockUtils);
  }

  @Test
  public void testGetAttachment_centralDb() throws Exception {
    IWebUtils mockUtils = createMock(IWebUtils.class);
    String link = "XWiki.XWikiPreferences;myAttachment.css";
    DocumentReference xwikiPrefDocRef = new DocumentReference("celements2web", "XWiki",
        "XWikiPreferences");
    BaseObject centrBo = new BaseObject();
    DocumentReference centrDocRef = new DocumentReference("celements2web", "mySpace",
        "myDoc");
    centrBo.setDocumentReference(centrDocRef);
    centrBo.setStringValue("media", "print");
    centrBo.setIntValue("is_rte_content", 0);
    centrBo.setStringValue("cssname", "XWiki.XWikiPreferences;myAttachment.css");
    CSSBaseObject cssFile = new CSSBaseObject(centrBo, context);
    expect(mockUtils.isAttachmentLink(eq(link))).andReturn(true).anyTimes();
    cssFile.testInjectUtils(mockUtils);
    XWikiDocument doc = new XWikiDocument(xwikiPrefDocRef);
    List<XWikiAttachment> attList = new ArrayList<XWikiAttachment>();
    XWikiAttachment att = new XWikiAttachment(doc, "myAttachment.css");
    attList.add(att);
    doc.setAttachmentList(attList);
    expect(xwiki.getDocument(eq(xwikiPrefDocRef), same(context))).andReturn(doc);
    replay(xwiki, mockUtils);
    assertNotNull("attachment must not be null", cssFile.getAttachment());
    verify(xwiki, mockUtils);
  }

}