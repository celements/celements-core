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

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.web.utils.IWebUtils;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;

public class CSSStringTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;

  @Before
  public void setUp_CSSStringTest() {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
  }

  @Test
  public void testGetCSS() {
    String url = "/skin/Space/Page/test.css";
    String attLink = "Space.Page;test.css";
    CSSString cssFile = new CSSString(attLink, context);
    XWikiContext context = new XWikiContext();
    IWebUtils webutils = createMock(IWebUtils.class);
    cssFile.testInjectUtils(webutils);
    expect(webutils.getAttachmentURL(eq(attLink), same(context))).andReturn(url).once();
    replay(webutils);
    assertEquals(url, cssFile.getCSS(context));
    verify(webutils);
  }

  @Test
  public void testGetMedia() {
    CSSString cssFile = new CSSString("", "print", context);
    assertEquals("print", cssFile.getMedia());
  }
  
  @Test
  public void testCSS_String_notContentCSS() {
    CSSString cssFile = new CSSString("/test/file.css", context);
    assertFalse("Filename not ending in -content.css or _content.css",
        cssFile.isContentCSS());
  }

  @Test
  public void testCSS_String_ContentCSS() {
    CSSString cssFile = new CSSString("/test/file-content.css", context);
    assertTrue("Filename ending in -content.css", cssFile.isContentCSS());
    CSSString cssFile2 = new CSSString("/test/file_content.css", context);
    assertTrue("Filename ending in _content.css", cssFile2.isContentCSS());
  }

  @Test
  public void testIsAttachment() {
    CSSString cssFile = new CSSString("XWiki.XWikiPreferences;myAttachment.css", context);
    assertTrue("attachment FullName-Link must be recognized", cssFile.isAttachment());
    CSSString cssFile2 = new CSSString("/XWiki/XWikiPreferences/myAttachment.css", context);
    assertFalse("attachment normal links should not be recogised.", cssFile2.isAttachment());
  }

  @Test
  public void testGetAttachment() throws Exception {
    IWebUtils mockUtils = createMock(IWebUtils.class);
    String link = "XWiki.XWikiPreferences;myAttachment.css";
    String fullName = "XWiki.XWikiPreferences";
    CSSString cssFile = new CSSString(link, context);
    expect(mockUtils.isAttachmentLink(eq(link))).andReturn(true).anyTimes();
    expect(mockUtils.getPageFullName(eq(link))).andReturn(fullName);
    expect(mockUtils.getAttachmentName(eq(link))).andReturn("myAttachment.css");
    cssFile.testInjectUtils(mockUtils);
    XWikiDocument doc = new XWikiDocument();
    List<XWikiAttachment> attList = new ArrayList<XWikiAttachment>();
    XWikiAttachment att = new XWikiAttachment(doc, "myAttachment.css");
    attList.add(att);
    doc.setAttachmentList(attList);
    expect(xwiki.getDocument(eq(fullName), same(context))).andReturn(doc);
    replay(xwiki, mockUtils);
    assertNotNull("attachment must not be null", cssFile.getAttachment());
    verify(xwiki, mockUtils);
  }
}