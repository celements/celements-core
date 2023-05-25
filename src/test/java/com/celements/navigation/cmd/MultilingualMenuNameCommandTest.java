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
package com.celements.navigation.cmd;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.reference.RefBuilder;
import com.celements.web.plugin.cmd.AttachmentURLCommand;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class MultilingualMenuNameCommandTest extends AbstractComponentTest {

  private XWikiContext context;
  private XWikiDocument currentDoc;
  private MultilingualMenuNameCommand menuNameCmd;
  private XWiki xwiki;

  @Before
  public void prepareTest() throws Exception {
    xwiki = getWikiMock();
    context = getContext();
    DocumentReference docRef = new RefBuilder().wiki(context.getDatabase()).space("MySpace")
        .doc("MyCurrentDoc").build(DocumentReference.class);
    currentDoc = new XWikiDocument(docRef);
    context.setDoc(currentDoc);
    menuNameCmd = new MultilingualMenuNameCommand();
    menuNameCmd.attCmd = createDefaultMock(AttachmentURLCommand.class);
  }

  @Test
  public void testGetMultilingualMenuName_null() {
    com.xpn.xwiki.api.Object menuItem = null;
    assertEquals("", menuNameCmd.getMultilingualMenuName(menuItem, "de", context));
  }

  @Test
  public void testGetMultilingualMenuName_BaseObject_null() {
    BaseObject menuItem = null;
    assertEquals("", menuNameCmd.getMultilingualMenuName(menuItem, "de", context));
  }

  @Test
  public void testAddNavImageStyle_null() throws Exception {
    expect(xwiki.getDocument(eq(currentDoc.getFullName()), same(context))).andReturn(
        currentDoc).anyTimes();
    expect(xwiki.isMultiLingual(same(context))).andReturn(true).anyTimes();
    replayDefault();
    assertEquals("", menuNameCmd.addNavImageStyle(currentDoc.getFullName(), "de", context));
    verifyDefault();
  }

  @Test
  public void testAddNavImageStyle_emptyNavImage() throws Exception {
    String menuName = "menuName for test";
    BaseObject menuNameObj = new BaseObject();
    menuNameObj.setStringValue("menu_name", menuName);
    menuNameObj.setStringValue("lang", "de");
    menuNameObj.setStringValue("image", "");
    currentDoc.setObject(MultilingualMenuNameCommand.CELEMENTS_MENU_NAME, 0, menuNameObj);
    expect(xwiki.getDocument(eq(currentDoc.getFullName()), same(context))).andReturn(
        currentDoc).anyTimes();
    expect(xwiki.isMultiLingual(same(context))).andReturn(true).anyTimes();
    expect(menuNameCmd.attCmd.getAttachmentURL(eq(""), same(context))).andReturn(null);
    replayDefault();
    assertEquals("", menuNameCmd.addNavImageStyle(currentDoc.getFullName(), "de", context));
    verifyDefault();
  }

  @Test
  public void testAddNavImageStyle() throws Exception {
    String menuName = "menuName for test";
    String attachmentURL = "/download/Content/Home/navImage.jpg";
    BaseObject menuNameObj = new BaseObject();
    menuNameObj.setStringValue("menu_name", menuName);
    menuNameObj.setStringValue("lang", "de");
    menuNameObj.setStringValue("image", "Content.Home;navImage.jpg");
    currentDoc.setObject(MultilingualMenuNameCommand.CELEMENTS_MENU_NAME, 0, menuNameObj);
    expect(xwiki.getDocument(eq(currentDoc.getFullName()), same(context))).andReturn(
        currentDoc).anyTimes();
    expect(xwiki.isMultiLingual(same(context))).andReturn(true).anyTimes();
    expect(menuNameCmd.attCmd.getAttachmentURL(eq("Content.Home;navImage.jpg"), same(context)))
        .andReturn(attachmentURL);
    replayDefault();
    assertEquals("style=\"background-image:url(" + attachmentURL + ")\"",
        menuNameCmd.addNavImageStyle(currentDoc.getFullName(), "de", context));
    verifyDefault();
  }

  @Test
  public void testAddToolTip() throws Exception {
    String menuName = "menuName for test";
    String toolTip = "Tool tip for test";
    BaseObject menuNameObj = new BaseObject();
    menuNameObj.setStringValue("menu_name", menuName);
    menuNameObj.setStringValue("tooltip", toolTip);
    menuNameObj.setStringValue("lang", "de");
    currentDoc.setObject(MultilingualMenuNameCommand.CELEMENTS_MENU_NAME, 0, menuNameObj);
    expect(xwiki.getDocument(eq(currentDoc.getFullName()), same(context))).andReturn(
        currentDoc).anyTimes();
    expect(xwiki.isMultiLingual(same(context))).andReturn(true).anyTimes();
    replayDefault();
    assertEquals("title=\"Tool tip for test\"", menuNameCmd.addToolTip(currentDoc.getFullName(),
        "de", context));
    verifyDefault();
  }

  @Test
  public void testAddToolTip_NPE() throws Exception {
    expect(xwiki.getDocument(eq(currentDoc.getFullName()), same(context))).andReturn(
        currentDoc).anyTimes();
    expect(xwiki.isMultiLingual(same(context))).andReturn(true).anyTimes();
    replayDefault();
    assertEquals("", menuNameCmd.addToolTip(currentDoc.getFullName(), "de", context));
    verifyDefault();
  }

}
