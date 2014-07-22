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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public class ExternalJavaScriptFilesCommandTest extends AbstractBridgedComponentTestCase {
  private ExternalJavaScriptFilesCommand command = null;
  private XWikiContext context = null;
  private AttachmentURLCommand attUrlCmd= null;
  private XWiki xwiki;
  
  @Before
  public void setUp_ExternalJavaScriptFilesCommandTest() throws Exception {
    context = getContext();
    command = new ExternalJavaScriptFilesCommand(context);
    attUrlCmd = createMock(AttachmentURLCommand.class);
    command.injectAttUrlCmd(attUrlCmd);
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
  }

  @Test
  public void testAddExtJSfileOnce_beforeGetAll() {
    String file = ":celJS/prototype.js";
    expect(attUrlCmd.getAttachmentURL(eq(file), same(context))).andReturn(file).once();
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(true).anyTimes();
    replay(xwiki, attUrlCmd);
    assertEquals("", command.addExtJSfileOnce(file));
    verify(xwiki, attUrlCmd);
  }
  
  @Test
  public void testAddExtJSfileOnce_beforeGetAll_fileNotFound() {
    String fileNotFound = "Content.WebHome;blabla.js";
    expect(attUrlCmd.getAttachmentURL(eq(fileNotFound), same(context))).andReturn(null
        ).once();
    expect(attUrlCmd.isAttachmentLink(eq(fileNotFound))).andReturn(true).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(fileNotFound))).andReturn(false).anyTimes();
    replay(xwiki, attUrlCmd);
    assertEquals("", command.addExtJSfileOnce(fileNotFound));
    verify(xwiki, attUrlCmd);
  }
  
  @Test
  public void testAddExtJSfileOnce_afterGetAll() throws XWikiException {
    String file = "/skin/resources/celJS/prototype.js";
    expect(attUrlCmd.getAttachmentURL(eq(file), same(context))).andReturn(file).times(2);
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(false).anyTimes();
    replay(xwiki, attUrlCmd);
    command.injectDisplayAll(true);
    assertEquals("<script type=\"text/javascript\" src=\"" + file + "\"></script>", 
        command.addExtJSfileOnce(file));
    assertEquals("", command.addExtJSfileOnce(file));
    verify(xwiki, attUrlCmd);
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll_versioning() throws XWikiException {
    String file = "celJS/prototype.js?version=20110401182200";
    expect(attUrlCmd.getAttachmentURL(eq(file), same(context))).andReturn(file).times(2);
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(false).anyTimes();
    replay(xwiki, attUrlCmd);
    command.injectDisplayAll(true);
    assertEquals("<script type=\"text/javascript\" src=\"" + file + "\"></script>", 
        command.addExtJSfileOnce(file));
    assertEquals("", command.addExtJSfileOnce(file));
    verify(xwiki, attUrlCmd);
  }
  
  @Test
  public void testGetExtStringForJsFile() {
    String url = "http://www.xyz.com?hi=yes&by=no";
    String urlEsc = "http://www.xyz.com?hi=yes&amp;by=no";
    String scriptStart = "<script type=\"text/javascript\" src=\"";
    String scriptEnd = "\"></script>";
    assertEquals(scriptStart + urlEsc + scriptEnd, command.getExtStringForJsFile(url));
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll_fileNotFound_url(
      ) throws XWikiException {
    String fileNotFound = "/download/Content/WebHome/blabla.js";
    expect(attUrlCmd.getAttachmentURL(eq(fileNotFound), same(context))).andReturn(null
        ).times(2);
    expect(attUrlCmd.isAttachmentLink(eq(fileNotFound))).andReturn(false).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(fileNotFound))).andReturn(false).anyTimes();
    replay(xwiki, attUrlCmd);
    command.injectDisplayAll(true);
    assertEquals("<!-- WARNING: js-file not found: " + fileNotFound + "-->", 
        command.addExtJSfileOnce(fileNotFound));
    assertEquals("", command.addExtJSfileOnce(fileNotFound));
    verify(xwiki, attUrlCmd);
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll_fileNotFound_attUrl(
      ) throws XWikiException {
    String fileNotFound = "Content.WebHome;blabla.js";
    expect(attUrlCmd.getAttachmentURL(eq(fileNotFound), same(context))).andReturn(null
        ).once();
    expect(attUrlCmd.isAttachmentLink(eq(fileNotFound))).andReturn(true).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(fileNotFound))).andReturn(false).anyTimes();
    replay(xwiki, attUrlCmd);
    command.injectDisplayAll(true);
    assertEquals("<!-- WARNING: js-file not found: " + fileNotFound + "-->", 
        command.addExtJSfileOnce(fileNotFound));
    assertEquals("", command.addExtJSfileOnce(fileNotFound));
    verify(xwiki, attUrlCmd);
  }

  @Test
  public void testAddExtJSfileOnce_beforeGetAll_double() throws Exception {
    context.setDoc(new XWikiDocument(new DocumentReference(context.getDatabase(), "Main",
        "WebHome")));
    String fileNotFound = "celJS/blabla.js";
    expect(attUrlCmd.getAttachmentURL(eq(fileNotFound), same(context))).andReturn(null
        ).once();
    expect(attUrlCmd.isAttachmentLink(eq(fileNotFound))).andReturn(false).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(fileNotFound))).andReturn(true).anyTimes();
    String file = "/skin/resources/celJS/prototype.js?version=20110401120000";
    expect(attUrlCmd.getAttachmentURL(eq(file), same(context))).andReturn(file).times(2);
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(false).anyTimes();
    DocumentReference xwikiPrefDocRef = new DocumentReference(context.getDatabase(),
        "XWiki", "XWikiPreferences");
    XWikiDocument xwikiPrefDoc = new XWikiDocument(xwikiPrefDocRef);
    expect(xwiki.getDocument(eq(xwikiPrefDocRef), same(context))).andReturn(
        xwikiPrefDoc).anyTimes();
    DocumentReference mainPrefDocRef = new DocumentReference(context.getDatabase(),
        "Main", "WebPreferences");
    XWikiDocument mainPrefDoc = new XWikiDocument(mainPrefDocRef);
    expect(xwiki.getDocument(eq(mainPrefDocRef), same(context))).andReturn(
        mainPrefDoc).anyTimes();
    replay(xwiki, attUrlCmd);
    assertEquals("", command.addExtJSfileOnce(file));
    assertEquals("", command.addExtJSfileOnce(file));
    assertEquals("", command.addExtJSfileOnce(fileNotFound));
    String allStr = command.getAllExternalJavaScriptFiles();
    assertEquals("<script type=\"text/javascript\" src=\"" + file + "\"></script>\n"
        + "<!-- WARNING: js-file not found: " + fileNotFound + "-->\n", allStr);
    verify(xwiki, attUrlCmd);
  }
  
  @Test
  public void testAddExtJSfileOnce_beforeGetAll_explicitAndImplicit_double(
      ) throws Exception {
    context.setDoc(new XWikiDocument(new DocumentReference(context.getDatabase(), "Main",
        "WebHome")));
    String fileNotFound = ":celJS/blabla.js";
    expect(attUrlCmd.getAttachmentURL(eq(fileNotFound), same(context))).andReturn(null
        ).once();
    expect(attUrlCmd.isAttachmentLink(eq(fileNotFound))).andReturn(false).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(fileNotFound))).andReturn(true).anyTimes();
    String attFileURL = ":celJS/prototype.js";
    String file = "/skin/celJS/prototype.js?version=20110401120000";
    expect(attUrlCmd.getAttachmentURL(eq(attFileURL), same(context))).andReturn(file
        ).anyTimes();
    expect(attUrlCmd.isAttachmentLink(eq(attFileURL))).andReturn(false).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(attFileURL))).andReturn(true).anyTimes();
    String file2 = "/file/celJS/prototype.js?version=20110401120000";
    expect(attUrlCmd.getAttachmentURL(eq(attFileURL), eq("file"), same(context))
        ).andReturn(file2).anyTimes();
    DocumentReference xwikiPrefDocRef = new DocumentReference(context.getDatabase(),
        "XWiki", "XWikiPreferences");
    XWikiDocument xwikiPrefDoc = new XWikiDocument(xwikiPrefDocRef);
    expect(xwiki.getDocument(eq(xwikiPrefDocRef), same(context))).andReturn(
        xwikiPrefDoc).anyTimes();
    DocumentReference mainPrefDocRef = new DocumentReference(context.getDatabase(),
        "Main", "WebPreferences");
    XWikiDocument mainPrefDoc = new XWikiDocument(mainPrefDocRef);
    expect(xwiki.getDocument(eq(mainPrefDocRef), same(context))).andReturn(
        mainPrefDoc).anyTimes();
    replay(xwiki, attUrlCmd);
    assertEquals("", command.addExtJSfileOnce(attFileURL, "file"));
    assertEquals("", command.addExtJSfileOnce(attFileURL));
    assertEquals("", command.addExtJSfileOnce(fileNotFound));
    String allStr = command.getAllExternalJavaScriptFiles();
    assertEquals("<script type=\"text/javascript\""
        + " src=\"/file/celJS/prototype.js?version=20110401120000\"></script>\n"
        + "<!-- WARNING: js-file not found: " + fileNotFound + "-->\n", allStr);
    verify(xwiki, attUrlCmd);
  }
  
}
