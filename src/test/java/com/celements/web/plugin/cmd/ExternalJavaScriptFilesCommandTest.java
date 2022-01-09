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

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.javascript.JsLoadMode;
import com.celements.model.access.IModelAccessFacade;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.web.plugin.cmd.ExternalJavaScriptFilesCommand.ExtJsFileParameter;
import com.celements.web.plugin.cmd.ExternalJavaScriptFilesCommand.JsFileEntry;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public class ExternalJavaScriptFilesCommandTest extends AbstractComponentTest {

  private ExternalJavaScriptFilesCommand command = null;
  private XWikiContext context = null;
  private AttachmentURLCommand attUrlCmd = null;
  private IPageTypeResolverRole pageTypeResolverMock;
  private IModelAccessFacade modelAccessMock;

  @Before
  public void setUp_ExternalJavaScriptFilesCommandTest() throws Exception {
    context = getContext();
    context.put("vcontext", new VelocityContext());
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    pageTypeResolverMock = registerComponentMock(IPageTypeResolverRole.class);
    attUrlCmd = createMockAndAddToDefault(AttachmentURLCommand.class);
    command = new ExternalJavaScriptFilesCommand();
  }

  @Test
  public void testAddExtJSfileOnce_beforeGetAll() {
    String file = ":celJS/prototype.js";
    expect(attUrlCmd.getAttachmentURL(eq(file), same(context))).andReturn(file).once();
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(true).atLeastOnce();
    replayDefault();
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter()
        .setJsFile(file)
        .setAttUrlCmd(attUrlCmd)));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_beforeGetAll_fileNotFound() {
    String fileNotFound = "Content.WebHome;blabla.js";
    expect(attUrlCmd.getAttachmentURL(eq(fileNotFound), same(context))).andReturn(null).once();
    expect(attUrlCmd.isAttachmentLink(eq(fileNotFound))).andReturn(true).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(fileNotFound))).andReturn(false).anyTimes();
    replayDefault();
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter()
        .setJsFile(fileNotFound)
        .setAttUrlCmd(attUrlCmd)));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll() throws XWikiException {
    String file = "/skin/resources/celJS/prototype.js";
    expect(attUrlCmd.getAttachmentURL(eq(file), same(context))).andReturn(file).times(2);
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(false).atLeastOnce();
    replayDefault();
    command.injectDisplayAll(true);
    final ExtJsFileParameter extJsFileParam = new ExtJsFileParameter()
        .setJsFile(file)
        .setAttUrlCmd(attUrlCmd);
    assertEquals("<script type=\"text/javascript\" src=\"" + file + "\"></script>",
        command.addExtJSfileOnce(extJsFileParam));
    assertEquals("", command.addExtJSfileOnce(extJsFileParam));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll_action() throws XWikiException {
    String file = "/file/resources/celJS/prototype.js";
    expect(attUrlCmd.getAttachmentURL(eq(file), eq("file"), same(context))).andReturn(
        file).atLeastOnce();
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(false).atLeastOnce();
    replayDefault();
    command.injectDisplayAll(true);
    final ExtJsFileParameter extJsFile = new ExtJsFileParameter()
        .setJsFile(file)
        .setAction("file")
        .setAttUrlCmd(attUrlCmd);
    assertEquals("<script type=\"text/javascript\" src=\"" + file + "\"></script>",
        command.addExtJSfileOnce(extJsFile));
    assertEquals("", command.addExtJSfileOnce(extJsFile));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll_action_params() throws XWikiException {
    String file = "/file/resources/celJS/prototype.js";
    expect(attUrlCmd.getAttachmentURL(eq(file), eq("file"), same(context))).andReturn(
        file).atLeastOnce();
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(false).atLeastOnce();
    replayDefault();
    command.injectDisplayAll(true);
    assertEquals("<script type=\"text/javascript\" src=\"" + file + "?me=blu\"></script>",
        command.addExtJSfileOnce(new ExtJsFileParameter()
            .setJsFile(file)
            .setAction("file")
            .setParams("me=blu")
            .setAttUrlCmd(attUrlCmd)));
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter()
        .setJsFile(file)
        .setAction("file")
        .setParams("me=blu")
        .setAttUrlCmd(attUrlCmd)));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll_action_params_onDisk() throws XWikiException {
    String file = ":celJS/prototype.js";
    String fileURL = "/file/resources/celJS/prototype.js?version=201507061937";
    expect(attUrlCmd.getAttachmentURL(eq(file), eq("file"), same(context))).andReturn(
        fileURL).atLeastOnce();
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(true).atLeastOnce();
    replayDefault();
    command.injectDisplayAll(true);
    final ExtJsFileParameter extJsFileParam = new ExtJsFileParameter()
        .setJsFile(file)
        .setAction("file")
        .setParams("me=blu")
        .setAttUrlCmd(attUrlCmd);
    assertEquals("<script type=\"text/javascript\" src=\"" + fileURL + "&amp;me=blu\"></script>",
        command.addExtJSfileOnce(extJsFileParam));
    assertEquals("", command.addExtJSfileOnce(extJsFileParam));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll_versioning() throws XWikiException {
    String file = "celJS/prototype.js?version=20110401182200";
    expect(attUrlCmd.getAttachmentURL(eq(file), same(context))).andReturn(file).times(2);
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(false).atLeastOnce();
    replayDefault();
    command.injectDisplayAll(true);
    assertEquals("<script type=\"text/javascript\" src=\"" + file + "\"></script>",
        command.addExtJSfileOnce(new ExtJsFileParameter()
            .setJsFile(file)
            .setAttUrlCmd(attUrlCmd)));
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter()
        .setJsFile(file)
        .setAttUrlCmd(attUrlCmd)));
    verifyDefault();
  }

  @Test
  public void testGetExtStringForJsFile() {
    String url = "http://www.xyz.com?hi=yes&by=no";
    String urlEsc = "http://www.xyz.com?hi=yes&amp;by=no";
    String scriptStart = "<script type=\"text/javascript\" src=\"";
    String scriptEnd = "\"></script>";
    JsFileEntry jsFile = new JsFileEntry(url);
    assertEquals(scriptStart + urlEsc + scriptEnd, command.getExtStringForJsFile(jsFile));
  }

  @Test
  public void testGetExtStringForJsFile_defer() {
    String url = "http://www.xyz.com?hi=yes&by=no";
    String urlEsc = "http://www.xyz.com?hi=yes&amp;by=no";
    String scriptStart = "<script defer type=\"text/javascript\" src=\"";
    String scriptEnd = "\"></script>";
    JsFileEntry jsFile = new JsFileEntry(url, JsLoadMode.DEFER);
    assertEquals(scriptStart + urlEsc + scriptEnd, command.getExtStringForJsFile(jsFile));
  }

  @Test
  public void testGetExtStringForJsFile_async() {
    String url = "http://www.xyz.com?hi=yes&by=no";
    String urlEsc = "http://www.xyz.com?hi=yes&amp;by=no";
    String scriptStart = "<script async type=\"text/javascript\" src=\"";
    String scriptEnd = "\"></script>";
    JsFileEntry jsFile = new JsFileEntry(url, JsLoadMode.ASYNC);
    assertEquals(scriptStart + urlEsc + scriptEnd, command.getExtStringForJsFile(jsFile));
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll_fileNotFound_url() throws XWikiException {
    String fileNotFound = "/download/Content/WebHome/blabla.js";
    expect(attUrlCmd.getAttachmentURL(eq(fileNotFound), same(context))).andReturn(null).times(2);
    expect(attUrlCmd.isAttachmentLink(eq(fileNotFound))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(fileNotFound))).andReturn(false).atLeastOnce();
    replayDefault();
    command.injectDisplayAll(true);
    assertEquals("<!-- WARNING: js-file not found: " + fileNotFound + "-->",
        command.addExtJSfileOnce(new ExtJsFileParameter()
            .setJsFile(fileNotFound)
            .setAttUrlCmd(attUrlCmd)));
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter()
        .setJsFile(fileNotFound)
        .setAttUrlCmd(attUrlCmd)));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll_fileNotFound_attUrl() throws XWikiException {
    String fileNotFound = "Content.WebHome;blabla.js";
    expect(attUrlCmd.getAttachmentURL(eq(fileNotFound), same(context))).andReturn(null).once();
    expect(attUrlCmd.isAttachmentLink(eq(fileNotFound))).andReturn(true).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(fileNotFound))).andReturn(false).anyTimes();
    replayDefault();
    command.injectDisplayAll(true);
    assertEquals("<!-- WARNING: js-file not found: " + fileNotFound + "-->",
        command.addExtJSfileOnce(new ExtJsFileParameter()
            .setJsFile(fileNotFound)
            .setAttUrlCmd(attUrlCmd)));
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter()
        .setJsFile(fileNotFound)
        .setAttUrlCmd(attUrlCmd)));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_beforeGetAll_double() throws Exception {
    PageLayoutCommand pageLayoutCmdMock = createMockAndAddToDefault(PageLayoutCommand.class);
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Main",
        "WebHome");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    String fileNotFound = "celJS/blabla.js";
    expect(attUrlCmd.getAttachmentURL(eq(fileNotFound), same(context))).andReturn(null).once();
    expect(attUrlCmd.isAttachmentLink(eq(fileNotFound))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(fileNotFound))).andReturn(true).atLeastOnce();
    String file = "/skin/resources/celJS/prototype.js?version=20110401120000";
    expect(attUrlCmd.getAttachmentURL(eq(file), same(context))).andReturn(file).times(2);
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(false).atLeastOnce();
    DocumentReference xwikiPrefDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiPreferences");
    XWikiDocument xwikiPrefDoc = new XWikiDocument(xwikiPrefDocRef);
    expect(modelAccessMock.getDocument(eq(xwikiPrefDocRef))).andReturn(xwikiPrefDoc).atLeastOnce();
    DocumentReference mainPrefDocRef = new DocumentReference(context.getDatabase(), "Main",
        "WebPreferences");
    XWikiDocument mainPrefDoc = new XWikiDocument(mainPrefDocRef);
    expect(modelAccessMock.getDocument(eq(mainPrefDocRef))).andReturn(mainPrefDoc).atLeastOnce();
    DocumentReference webHomeDocRef = contextDocRef;
    XWikiDocument webHomeDoc = new XWikiDocument(webHomeDocRef);
    expect(modelAccessMock.getDocument(eq(webHomeDocRef))).andReturn(webHomeDoc).atLeastOnce();
    PageTypeReference pageTypeRef = new PageTypeReference("TestPageType", "providerHint",
        Arrays.asList(""));
    expect(pageTypeResolverMock.resolvePageTypeRefForCurrentDoc()).andReturn(pageTypeRef);
    DocumentReference pageTypesDocRef = new DocumentReference(context.getDatabase(), "PageTypes",
        "TestPageType");
    XWikiDocument pageTypesDoc = new XWikiDocument(pageTypesDocRef);
    expect(modelAccessMock.getDocument(eq(pageTypesDocRef))).andReturn(pageTypesDoc).atLeastOnce();
    DocumentReference simpleLayoutDocRef = new DocumentReference(context.getDatabase(),
        "SimpleLayout", "WebHome");
    XWikiDocument simpleLayoutDoc = new XWikiDocument(simpleLayoutDocRef);
    expect(modelAccessMock.getDocument(eq(simpleLayoutDocRef))).andReturn(simpleLayoutDoc)
        .atLeastOnce();
    expect(pageLayoutCmdMock.getLayoutPropDoc()).andReturn(simpleLayoutDoc).atLeastOnce();
    final ExtJsFileParameter fileParams = new ExtJsFileParameter()
        .setJsFile(file)
        .setAttUrlCmd(attUrlCmd);
    final ExtJsFileParameter fileNotFoundParams = new ExtJsFileParameter()
        .setJsFile(fileNotFound)
        .setAttUrlCmd(attUrlCmd);
    replayDefault();
    assertEquals("", command.addExtJSfileOnce(fileParams));
    assertEquals("", command.addExtJSfileOnce(fileParams));
    assertEquals("", command.addExtJSfileOnce(fileNotFoundParams));
    String allStr = command.getAllExternalJavaScriptFiles(pageLayoutCmdMock, attUrlCmd);
    assertEquals("<script type=\"text/javascript\" src=\"" + file + "\"></script>\n"
        + "<!-- WARNING: js-file not found: " + fileNotFound + "-->\n", allStr);
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_beforeGetAll_explicitAndImplicit_double() throws Exception {
    PageLayoutCommand pageLayoutCmdMock = createMockAndAddToDefault(PageLayoutCommand.class);
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Main",
        "WebHome");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    String fileNotFound = ":celJS/blabla.js";
    expect(attUrlCmd.getAttachmentURL(eq(fileNotFound), same(context))).andReturn(null).once();
    expect(attUrlCmd.isAttachmentLink(eq(fileNotFound))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(fileNotFound))).andReturn(true).atLeastOnce();
    String attFileURL = ":celJS/prototype.js";
    String file = "/skin/celJS/prototype.js?version=20110401120000";
    expect(attUrlCmd.getAttachmentURL(eq(attFileURL), same(context))).andReturn(file).anyTimes();
    expect(attUrlCmd.isAttachmentLink(eq(attFileURL))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(attFileURL))).andReturn(true).atLeastOnce();
    String file2 = "/file/celJS/prototype.js?version=20110401120000";
    expect(attUrlCmd.getAttachmentURL(eq(attFileURL), eq("file"), same(context))).andReturn(
        file2).atLeastOnce();
    DocumentReference xwikiPrefDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiPreferences");
    XWikiDocument xwikiPrefDoc = new XWikiDocument(xwikiPrefDocRef);
    expect(modelAccessMock.getDocument(eq(xwikiPrefDocRef))).andReturn(xwikiPrefDoc).atLeastOnce();
    DocumentReference mainPrefDocRef = new DocumentReference(context.getDatabase(), "Main",
        "WebPreferences");
    XWikiDocument mainPrefDoc = new XWikiDocument(mainPrefDocRef);
    expect(modelAccessMock.getDocument(eq(mainPrefDocRef))).andReturn(mainPrefDoc).atLeastOnce();

    PageTypeReference pageTypeRef = new PageTypeReference("TestPageType", "providerHint",
        Arrays.asList(""));
    expect(pageTypeResolverMock.resolvePageTypeRefForCurrentDoc()).andReturn(pageTypeRef);
    DocumentReference pageTypesDocRef = new DocumentReference(context.getDatabase(), "PageTypes",
        "TestPageType");
    XWikiDocument pageTypesDoc = new XWikiDocument(pageTypesDocRef);
    pageTypesDoc.setNew(false);
    expect(modelAccessMock.getDocument(eq(pageTypesDocRef))).andReturn(pageTypesDoc).atLeastOnce();
    DocumentReference mainWebHomeDocRef = new DocumentReference(context.getDatabase(), "Main",
        "WebHome");
    XWikiDocument mainWebHomeDoc = new XWikiDocument(pageTypesDocRef);
    expect(modelAccessMock.getDocument(eq(mainWebHomeDocRef))).andReturn(mainWebHomeDoc)
        .atLeastOnce();
    DocumentReference simpleLayoutDocRef = new DocumentReference(context.getDatabase(),
        "SimpleLayout", "WebHome");
    XWikiDocument simpleLayoutDoc = new XWikiDocument(simpleLayoutDocRef);
    expect(modelAccessMock.getDocument(eq(simpleLayoutDocRef))).andReturn(simpleLayoutDoc)
        .atLeastOnce();
    expect(pageLayoutCmdMock.getLayoutPropDoc()).andReturn(simpleLayoutDoc).atLeastOnce();
    replayDefault();
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter()
        .setJsFile(attFileURL)
        .setAction("file")
        .setAttUrlCmd(attUrlCmd)));
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter()
        .setJsFile(attFileURL)
        .setAttUrlCmd(attUrlCmd)));
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter()
        .setJsFile(fileNotFound)
        .setAttUrlCmd(attUrlCmd)));
    String allStr = command.getAllExternalJavaScriptFiles(pageLayoutCmdMock, attUrlCmd);
    assertEquals("<script type=\"text/javascript\""
        + " src=\"/file/celJS/prototype.js?version=20110401120000\"></script>\n"
        + "<!-- WARNING: js-file not found: " + fileNotFound + "-->\n", allStr);
    verifyDefault();
  }

  @Test
  public void testAddLazyExtJSfile() {
    String jsFile = ":celJS/celTabMenu/loadTinyMCE-async.js";
    String jsFileURL = "/file/resources/celJS/celTabMenu/loadTinyMCE-async.js";
    String expJSON = "{\"fullURL\" : " + "\"" + jsFileURL + "\", \"initLoad\" : true}";
    expect(attUrlCmd.getAttachmentURL(eq(jsFile), same(context))).andReturn(jsFileURL).once();
    replayDefault();
    assertEquals("<span class='cel_lazyloadJS' style='display: none;'>" + expJSON + "</span>",
        command.addLazyExtJSfile(new ExtJsFileParameter()
            .setJsFile(jsFile)
            .setAttUrlCmd(attUrlCmd)));
    verifyDefault();
  }

  @Test
  public void testAddLazyExtJSfile_action() {
    String jsFile = ":celJS/celTabMenu/loadTinyMCE-async.js";
    String jsFileURL = "/file/resources/celJS/celTabMenu/loadTinyMCE-async.js";
    String action = "file";
    String expJSON = "{\"fullURL\" : " + "\"" + jsFileURL + "\", \"initLoad\" : true}";
    expect(attUrlCmd.getAttachmentURL(eq(jsFile), eq(action), same(context))).andReturn(
        jsFileURL).once();
    replayDefault();
    assertEquals("<span class='cel_lazyloadJS' style='display: none;'>" + expJSON + "</span>",
        command.addLazyExtJSfile(new ExtJsFileParameter()
            .setJsFile(jsFile)
            .setAction(action)
            .setAttUrlCmd(attUrlCmd)));
    verifyDefault();
  }

  @Test
  public void testAddLazyExtJSfile_action_params() {
    String jsFile = "mySpace.myDoc;loadTinyMCE-async.js";
    String jsFileURL = "/download/mySpace/myDoc/loadTinyMCE-async.js";
    String action = "file";
    String expJSON = "{\"fullURL\" : " + "\"" + jsFileURL + "?me=blu\", \"initLoad\" : true}";
    expect(attUrlCmd.getAttachmentURL(eq(jsFile), eq(action), same(context))).andReturn(
        jsFileURL).once();
    replayDefault();
    assertEquals("<span class='cel_lazyloadJS' style='display: none;'>" + expJSON + "</span>",
        command.addLazyExtJSfile(new ExtJsFileParameter()
            .setJsFile(jsFile)
            .setAction(action)
            .setParams("me=blu")
            .setAttUrlCmd(attUrlCmd)));
    verifyDefault();
  }

  @Test
  public void testAddLazyExtJSfile_action_params_onDisk() {
    String jsFile = ":celJS/celTabMenu/loadTinyMCE-async.js";
    String jsFileURL = "/file/resources/celJS/celTabMenu/loadTinyMCE-async.js"
        + "?version=201507061937";
    String action = "file";
    String expJSON = "{\"fullURL\" : " + "\"" + jsFileURL + "&me=blu\", \"initLoad\" : true}";
    expect(attUrlCmd.getAttachmentURL(eq(jsFile), eq(action), same(context))).andReturn(
        jsFileURL).once();
    replayDefault();
    assertEquals("<span class='cel_lazyloadJS' style='display: none;'>" + expJSON + "</span>",
        command.addLazyExtJSfile(new ExtJsFileParameter()
            .setJsFile(jsFile)
            .setAction(action)
            .setParams("me=blu")
            .setAttUrlCmd(attUrlCmd)));
    verifyDefault();
  }
}
