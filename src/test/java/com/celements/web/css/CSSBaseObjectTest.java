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
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

public class CSSBaseObjectTest extends AbstractComponentTest {

  BaseObject bo;
  private XWikiContext context;
  private XWiki xwiki;
  private DocumentReference docRef;

  @Before
  public void setUp_CSSBaseObjectTest() throws Exception {
    registerComponentMocks(UserService.class);
    context = getContext();
    bo = new BaseObject();
    docRef = new DocumentReference(context.getDatabase(), "mySpace", "myDoc");
    bo.setDocumentReference(docRef);
    bo.setStringValue("title", "myTitle");
    bo.setStringValue("media", "print");
    bo.setIntValue("is_rte_content", 0);
    xwiki = getWikiMock();
    DocumentReference userDocRef = new DocumentReference(context.getDatabase(), "XWiki", "user");
    context.setUser(Utils.getComponent(ModelUtils.class).serializeRef(userDocRef));
    User user = createDefaultMock(User.class);
    expect(getMock(UserService.class).getUser(context.getUser())).andReturn(user).anyTimes();
    expect(user.getDocRef()).andReturn(userDocRef).anyTimes();
  }

  @Test
  public void testGetCSS_celements2web() {
    String attLink = "celements2web:Space.Doc;file.txt";
    String url = "http://celements2web.testhost/skin/Space/Doc/file.txt";
    bo.setStringValue("cssname", attLink);
    CSSBaseObject cssFile = new CSSBaseObject(bo, context);
    AttachmentURLCommand attURLcmd = createDefaultMock(AttachmentURLCommand.class);
    cssFile.testInjectAttURLcmd(attURLcmd);
    expect(attURLcmd.getAttachmentURL(eq(attLink), same(context))).andReturn(url).once();
    replayDefault();
    assertEquals(url, cssFile.getCSS(context));
    verifyDefault();
  }

  @Test
  public void testGetCSS_emptyObjField() {
    bo.setStringValue("cssname", "");
    CSSBaseObject cssFile = new CSSBaseObject(bo, context);
    AttachmentURLCommand attURLcmd = createDefaultMock(AttachmentURLCommand.class);
    cssFile.testInjectAttURLcmd(attURLcmd);
    expect(attURLcmd.getAttachmentURL(eq(""), same(context))).andReturn("").once();
    replayDefault();
    assertEquals("", cssFile.getCSS(context));
    verifyDefault();
  }

  @Test
  public void testGetCSS() {
    String attLink = "Space.Doc;file.txt";
    String url = "/skin/Space/Doc/file.txt";
    bo.setStringValue("cssname", attLink);
    CSSBaseObject cssFile = new CSSBaseObject(bo, context);
    AttachmentURLCommand attURLcmd = createDefaultMock(AttachmentURLCommand.class);
    cssFile.testInjectAttURLcmd(attURLcmd);
    expect(attURLcmd.getAttachmentURL(eq(context.getDatabase() + ":" + attLink), same(
        context))).andReturn(url).once();
    replayDefault();
    assertEquals(url, cssFile.getCSS(context));
    verifyDefault();
  }

  @Test
  public void testIsAlternate() {
    CSSBaseObject cssFile = new CSSBaseObject(bo, context);
    assertFalse(cssFile.isAlternate());
    bo.setIntValue("alternate", 0);
    assertFalse(cssFile.isAlternate());
    bo.setIntValue("alternate", 1);
    assertTrue(cssFile.isAlternate());
  }

  @Test
  public void testGetTitle() {
    CSSBaseObject cssFile = new CSSBaseObject(bo, context);
    assertEquals("myTitle", cssFile.getTitle());
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
    assertFalse("Filename not ending in -content.css or _content.css", cssFile.isContentCSS());
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
    AttachmentURLCommand attURLcmd = createDefaultMock(AttachmentURLCommand.class);
    String link = "XWiki.XWikiPreferences;myAttachment.css";
    DocumentReference xwikiPrefDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiPreferences");
    bo.setStringValue("cssname", "XWiki.XWikiPreferences;myAttachment.css");
    CSSBaseObject cssFile = new CSSBaseObject(bo, context);
    expect(attURLcmd.isAttachmentLink(eq(link))).andReturn(true).anyTimes();
    cssFile.testInjectAttURLcmd(attURLcmd);
    XWikiDocument doc = new XWikiDocument(xwikiPrefDocRef);
    List<XWikiAttachment> attList = new ArrayList<>();
    XWikiAttachment att = new XWikiAttachment(doc, "myAttachment.css");
    attList.add(att);
    doc.setAttachmentList(attList);
    expect(getWikiMock().getDocument(eq(xwikiPrefDocRef), same(getContext()))).andReturn(doc);
    XWikiRightService rightSerivce = createDefaultMock(XWikiRightService.class);
    expect(getWikiMock().getRightService()).andReturn(rightSerivce);
    String fullName = getContext().getDatabase() + ":XWiki.XWikiPreferences";
    expect(rightSerivce.hasAccessLevel(eq("view"), eq(getContext().getUser()), eq(fullName), same(
        getContext()))).andReturn(true);
    replayDefault();
    assertNotNull("attachment must not be null", cssFile.getAttachment());
    verifyDefault();
  }

  @Test
  public void testGetAttachment_Null() throws Exception {
    AttachmentURLCommand attURLcmd = createDefaultMock(AttachmentURLCommand.class);
    String link = "XWiki.XWikiPreferences;myAttachment.css";
    DocumentReference xwikiPrefDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiPreferences");
    bo.setStringValue("cssname", "XWiki.XWikiPreferences;myAttachment.css");
    CSSBaseObject cssFile = new CSSBaseObject(bo, context);
    expect(attURLcmd.isAttachmentLink(eq(link))).andReturn(true).anyTimes();
    cssFile.testInjectAttURLcmd(attURLcmd);
    XWikiDocument doc = new XWikiDocument(xwikiPrefDocRef);
    List<XWikiAttachment> attList = new ArrayList<>();
    doc.setAttachmentList(attList);
    expect(xwiki.getDocument(eq(xwikiPrefDocRef), same(context))).andReturn(doc);
    replayDefault();
    assertNull("attachment must be null, if attachment does not exist", cssFile.getAttachment());
    verifyDefault();
  }

  @Test
  public void testGetAttachment_centralDb() throws Exception {
    AttachmentURLCommand attURLcmd = createDefaultMock(AttachmentURLCommand.class);
    String link = "XWiki.XWikiPreferences;myAttachment.css";
    DocumentReference xwikiPrefDocRef = new DocumentReference("celements2web", "XWiki",
        "XWikiPreferences");
    BaseObject centrBo = new BaseObject();
    DocumentReference centrDocRef = new DocumentReference("celements2web", "mySpace", "myDoc");
    centrBo.setDocumentReference(centrDocRef);
    centrBo.setStringValue("media", "print");
    centrBo.setIntValue("is_rte_content", 0);
    centrBo.setStringValue("cssname", "XWiki.XWikiPreferences;myAttachment.css");
    CSSBaseObject cssFile = new CSSBaseObject(centrBo, context);
    expect(attURLcmd.isAttachmentLink(eq(link))).andReturn(true).anyTimes();
    cssFile.testInjectAttURLcmd(attURLcmd);
    XWikiDocument doc = new XWikiDocument(xwikiPrefDocRef);
    List<XWikiAttachment> attList = new ArrayList<>();
    XWikiAttachment att = new XWikiAttachment(doc, "myAttachment.css");
    attList.add(att);
    doc.setAttachmentList(attList);
    expect(getWikiMock().getDocument(eq(xwikiPrefDocRef), same(getContext()))).andReturn(doc);
    XWikiRightService rightSerivce = createDefaultMock(XWikiRightService.class);
    expect(getWikiMock().getRightService()).andReturn(rightSerivce);
    String fullName = "celements2web:XWiki.XWikiPreferences";
    expect(rightSerivce.hasAccessLevel(eq("view"), eq(getContext().getUser()), eq(fullName), same(
        getContext()))).andReturn(true);
    replayDefault();
    assertNotNull("attachment must not be null", cssFile.getAttachment());
    verifyDefault();
  }

  @Test
  public void testGetCSSBasePath_fullName() {
    String cssVal = "Content.WebHome";
    bo.setStringValue("cssname", cssVal);
    CSSBaseObject cssFile = new CSSBaseObject(bo, context);
    assertEquals("xwikidb:" + cssVal, cssFile.getCssBasePath());
  }

  @Test
  public void testGetCSSBasePath_absoluteInternal() {
    String cssVal = "/file/Content/WebHome/test.css";
    bo.setStringValue("cssname", cssVal);
    CSSBaseObject cssFile = new CSSBaseObject(bo, context);
    assertEquals(cssVal, cssFile.getCssBasePath());
  }

  @Test
  public void testGetCSSBasePath_external() {
    String cssVal = "http://www.celements.ch/";
    bo.setStringValue("cssname", cssVal);
    CSSBaseObject cssFile = new CSSBaseObject(bo, context);
    assertEquals(cssVal, cssFile.getCssBasePath());
  }

  @Test
  public void testGetCSSBasePath_externalSecure() {
    String cssVal = "https://www.celements.ch/";
    bo.setStringValue("cssname", cssVal);
    CSSBaseObject cssFile = new CSSBaseObject(bo, context);
    assertEquals(cssVal, cssFile.getCssBasePath());
  }
}
