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

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.auth.user.User;
import com.celements.auth.user.UserService;
import com.celements.common.test.AbstractComponentTest;
import com.celements.model.util.ModelUtils;
import com.celements.web.plugin.cmd.AttachmentURLCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

public class CSSStringTest extends AbstractComponentTest {

  private XWikiContext context;

  @Before
  public void prepareTest() throws Exception {
    registerComponentMocks(UserService.class);
    context = getContext();
    DocumentReference userDocRef = new DocumentReference(context.getDatabase(), "XWiki", "user");
    context.setUser(Utils.getComponent(ModelUtils.class).serializeRef(userDocRef));
    User user = createMockAndAddToDefault(User.class);
    expect(getMock(UserService.class).getUser(context.getUser())).andReturn(user).anyTimes();
    expect(user.getDocRef()).andReturn(userDocRef).anyTimes();
  }

  @Test
  public void test_getCSS() {
    String url = "/skin/Space/Page/test.css";
    String attLink = "Space.Page;test.css";
    CSSString cssFile = new CSSString(attLink, context);
    XWikiContext context = new XWikiContext();
    AttachmentURLCommand attURLcmd = createMockAndAddToDefault(AttachmentURLCommand.class);
    cssFile.testInjectAttURLcmd(attURLcmd);
    expect(attURLcmd.getAttachmentURL(eq(attLink), same(context))).andReturn(url).once();
    replayDefault();
    assertEquals(url, cssFile.getCSS(context));
    verifyDefault();
  }

  @Test
  public void test_isAlternate() {
    CSSString cssFile = new CSSString("", false, "", "", false, context);
    assertFalse(cssFile.isAlternate());
    cssFile = new CSSString("", true, "", "", false, context);
    assertTrue(cssFile.isAlternate());
  }

  @Test
  public void test_getTitle() {
    CSSString cssFile = new CSSString("", false, "myTitle", "", false, context);
    assertEquals("myTitle", cssFile.getTitle());
  }

  @Test
  public void test_getMedia() {
    CSSString cssFile = new CSSString("", "print", context);
    assertEquals("print", cssFile.getMedia());
  }

  @Test
  public void test_CSS_String_notContentCSS() {
    CSSString cssFile = new CSSString("/test/file.css", context);
    assertFalse("Filename not ending in -content.css or _content.css", cssFile.isContentCSS());
  }

  @Test
  public void test_CSS_String_ContentCSS() {
    CSSString cssFile = new CSSString("/test/file-content.css", context);
    assertTrue("Filename ending in -content.css", cssFile.isContentCSS());
    CSSString cssFile2 = new CSSString("/test/file_content.css", context);
    assertTrue("Filename ending in _content.css", cssFile2.isContentCSS());
  }

  @Test
  public void test_isAttachment() {
    CSSString cssFile = new CSSString("XWiki.XWikiPreferences;myAttachment.css", context);
    assertTrue("attachment FullName-Link must be recognized", cssFile.isAttachment());
    CSSString cssFile2 = new CSSString("/XWiki/XWikiPreferences/myAttachment.css", context);
    assertFalse("attachment normal links should not be recogised.", cssFile2.isAttachment());
  }

  @Test
  public void test_getAttachment() throws Exception {
    AttachmentURLCommand attURLcmd = createMockAndAddToDefault(AttachmentURLCommand.class);
    String link = "XWiki.XWikiPreferences;myAttachment.css";
    String fullName = "XWiki.XWikiPreferences";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiPreferences");
    CSSString cssFile = new CSSString(link, context);
    expect(attURLcmd.isAttachmentLink(eq(link))).andReturn(true).anyTimes();
    expect(attURLcmd.getPageFullName(eq(link))).andReturn(fullName);
    expect(attURLcmd.getAttachmentName(eq(link))).andReturn("myAttachment.css");
    cssFile.testInjectAttURLcmd(attURLcmd);
    XWikiDocument doc = new XWikiDocument(docRef);
    List<XWikiAttachment> attList = new ArrayList<>();
    XWikiAttachment att = new XWikiAttachment(doc, "myAttachment.css");
    attList.add(att);
    doc.setAttachmentList(attList);
    expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc);
    XWikiRightService rightSerivce = createMockAndAddToDefault(XWikiRightService.class);
    expect(getWikiMock().getRightService()).andReturn(rightSerivce);
    expect(rightSerivce.hasAccessLevel(eq("view"), eq(getContext().getUser()), eq(
        getContext().getDatabase() + ":" + fullName), same(getContext()))).andReturn(true);
    replayDefault();
    assertNotNull("attachment must not be null", cssFile.getAttachment());
    verifyDefault();
  }
}
