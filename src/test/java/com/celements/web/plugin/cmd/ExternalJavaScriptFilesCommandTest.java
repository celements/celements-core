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

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public class ExternalJavaScriptFilesCommandTest extends AbstractBridgedComponentTestCase {

  private ExternalJavaScriptFilesCommand command = null;
  private XWikiContext context = null;
  private AttachmentURLCommand attUrlCmd = null;
  private XWiki xwiki;
  IPageTypeResolverRole pageTypeResolverMock;

  @Before
  public void setUp_ExternalJavaScriptFilesCommandTest() throws Exception {
    context = getContext();
    command = new ExternalJavaScriptFilesCommand(context);
    attUrlCmd = createMockAndAddToDefault(AttachmentURLCommand.class);
    command.injectAttUrlCmd(attUrlCmd);
    pageTypeResolverMock = createMockAndAddToDefault(IPageTypeResolverRole.class);
    command.injectPageTypeResolver(pageTypeResolverMock);
    xwiki = getWikiMock();
  }

  @Test
  public void testAddExtJSfileOnce_beforeGetAll() {
    String file = ":celJS/prototype.js";
    expect(attUrlCmd.getAttachmentURL(eq(file), same(context))).andReturn(file).once();
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(true).anyTimes();
    replayDefault();
    assertEquals("", command.addExtJSfileOnce(file));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_beforeGetAll_fileNotFound() {
    String fileNotFound = "Content.WebHome;blabla.js";
    expect(attUrlCmd.getAttachmentURL(eq(fileNotFound), same(context))).andReturn(null).once();
    expect(attUrlCmd.isAttachmentLink(eq(fileNotFound))).andReturn(true).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(fileNotFound))).andReturn(false).anyTimes();
    replayDefault();
    assertEquals("", command.addExtJSfileOnce(fileNotFound));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll() throws XWikiException {
    String file = "/skin/resources/celJS/prototype.js";
    expect(attUrlCmd.getAttachmentURL(eq(file), same(context))).andReturn(file).times(2);
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(false).anyTimes();
    replayDefault();
    command.injectDisplayAll(true);
    assertEquals("<script type=\"text/javascript\" src=\"" + file + "\"></script>",
        command.addExtJSfileOnce(file));
    assertEquals("", command.addExtJSfileOnce(file));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll_action() throws XWikiException {
    String file = "/file/resources/celJS/prototype.js";
    expect(attUrlCmd.getAttachmentURL(eq(file), eq("file"), same(context))).andReturn(
        file).atLeastOnce();
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(false).anyTimes();
    replayDefault();
    command.injectDisplayAll(true);
    assertEquals("<script type=\"text/javascript\" src=\"" + file + "\"></script>",
        command.addExtJSfileOnce(file, "file"));
    assertEquals("", command.addExtJSfileOnce(file, "file"));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll_action_params() throws XWikiException {
    String file = "/file/resources/celJS/prototype.js";
    expect(attUrlCmd.getAttachmentURL(eq(file), eq("file"), same(context))).andReturn(
        file).atLeastOnce();
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(false).anyTimes();
    replayDefault();
    command.injectDisplayAll(true);
    assertEquals("<script type=\"text/javascript\" src=\"" + file + "?me=blu\"></script>",
        command.addExtJSfileOnce(file, "file", "me=blu"));
    assertEquals("", command.addExtJSfileOnce(file, "file", "me=blu"));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll_action_params_onDisk() throws XWikiException {
    String file = ":celJS/prototype.js";
    String fileURL = "/file/resources/celJS/prototype.js?version=201507061937";
    expect(attUrlCmd.getAttachmentURL(eq(file), eq("file"), same(context))).andReturn(
        fileURL).atLeastOnce();
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(true).anyTimes();
    replayDefault();
    command.injectDisplayAll(true);
    assertEquals("<script type=\"text/javascript\" src=\"" + fileURL + "&amp;me=blu\"></script>",
        command.addExtJSfileOnce(file, "file", "me=blu"));
    assertEquals("", command.addExtJSfileOnce(file, "file", "me=blu"));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll_versioning() throws XWikiException {
    String file = "celJS/prototype.js?version=20110401182200";
    expect(attUrlCmd.getAttachmentURL(eq(file), same(context))).andReturn(file).times(2);
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(false).anyTimes();
    replayDefault();
    command.injectDisplayAll(true);
    assertEquals("<script type=\"text/javascript\" src=\"" + file + "\"></script>",
        command.addExtJSfileOnce(file));
    assertEquals("", command.addExtJSfileOnce(file));
    verifyDefault();
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
  public void testAddExtJSfileOnce_afterGetAll_fileNotFound_url() throws XWikiException {
    String fileNotFound = "/download/Content/WebHome/blabla.js";
    expect(attUrlCmd.getAttachmentURL(eq(fileNotFound), same(context))).andReturn(null).times(2);
    expect(attUrlCmd.isAttachmentLink(eq(fileNotFound))).andReturn(false).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(fileNotFound))).andReturn(false).anyTimes();
    replayDefault();
    command.injectDisplayAll(true);
    assertEquals("<!-- WARNING: js-file not found: " + fileNotFound + "-->",
        command.addExtJSfileOnce(fileNotFound));
    assertEquals("", command.addExtJSfileOnce(fileNotFound));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll_fileNotFound_attUrl() throws XWikiException {
    String fileNotFound = "Content.WebHome;blabla.js";
    expect(attUrlCmd.getAttachmentURL(eq(fileNotFound), same(context))).andReturn(null).once();
    expect(attUrlCmd.isAttachmentLink(eq(fileNotFound))).andReturn(true).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(fileNotFound))).andReturn(false).anyTimes();
    replayDefault();
    command.injectDisplayAll(true);
    assertEquals("<!-- WARNING: js-file not found: " + fileNotFound + "-->",
        command.addExtJSfileOnce(fileNotFound));
    assertEquals("", command.addExtJSfileOnce(fileNotFound));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_beforeGetAll_double() throws Exception {
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Main",
        "WebHome");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    String fileNotFound = "celJS/blabla.js";
    expect(attUrlCmd.getAttachmentURL(eq(fileNotFound), same(context))).andReturn(null).once();
    expect(attUrlCmd.isAttachmentLink(eq(fileNotFound))).andReturn(false).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(fileNotFound))).andReturn(true).anyTimes();
    String file = "/skin/resources/celJS/prototype.js?version=20110401120000";
    expect(attUrlCmd.getAttachmentURL(eq(file), same(context))).andReturn(file).times(2);
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(false).anyTimes();
    DocumentReference xwikiPrefDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiPreferences");
    XWikiDocument xwikiPrefDoc = new XWikiDocument(xwikiPrefDocRef);
    expect(xwiki.getDocument(eq(xwikiPrefDocRef), same(context))).andReturn(
        xwikiPrefDoc).anyTimes();
    DocumentReference mainPrefDocRef = new DocumentReference(context.getDatabase(), "Main",
        "WebPreferences");
    XWikiDocument mainPrefDoc = new XWikiDocument(mainPrefDocRef);
    expect(xwiki.getDocument(eq(mainPrefDocRef), same(context))).andReturn(mainPrefDoc).anyTimes();
    DocumentReference webHomeDocRef = contextDocRef;
    XWikiDocument webHomeDoc = new XWikiDocument(webHomeDocRef);
    expect(xwiki.exists(eq(webHomeDocRef), same(context))).andReturn(true).anyTimes();
    expect(xwiki.getDocument(eq(webHomeDocRef), same(context))).andReturn(webHomeDoc).anyTimes();
    String pageTypeDocName1 = "PageTypes.Name1";
    String pageTypeDocName2 = "PageTypes.Name2";
    List<String> resultList = Arrays.asList(pageTypeDocName1, pageTypeDocName2);
    expect(xwiki.<String>search(eq(
        "select doc.fullName from XWikiDocument as doc, BaseObject as obj where "
            + "doc.space='PageTypes' and doc.translation=0 and obj.name=doc.fullName  and "
            + "obj.className='Celements2.PageTypeProperties'"),
        same(context))).andReturn(
            resultList).anyTimes();
    DocumentReference pageTypesName2DocRef = new DocumentReference(context.getDatabase(),
        "PageTypes", "Name2");
    XWikiDocument pageTypesName2Doc = new XWikiDocument(pageTypesName2DocRef);
    expect(xwiki.exists(eq(pageTypeDocName2), same(context))).andReturn(true).anyTimes();
    expect(xwiki.getDocument(eq(pageTypeDocName2), same(context))).andReturn(
        pageTypesName2Doc).anyTimes();
    DocumentReference pageTypesName1DocRef = new DocumentReference(context.getDatabase(),
        "PageTypes", "Name1");
    XWikiDocument pageTypesName1Doc = new XWikiDocument(pageTypesName1DocRef);
    expect(xwiki.exists(eq(pageTypeDocName1), same(context))).andReturn(true).anyTimes();
    expect(xwiki.getDocument(eq(pageTypeDocName1), same(context))).andReturn(
        pageTypesName1Doc).anyTimes();
    PageTypeReference pageTypeRef = new PageTypeReference("TestPageType", "providerHint",
        Arrays.asList(""));
    expect(pageTypeResolverMock.getPageTypeRefForCurrentDoc()).andReturn(pageTypeRef);
    DocumentReference pageTypesDocRef = new DocumentReference(context.getDatabase(), "PageTypes",
        "TestPageType");
    XWikiDocument pageTypesDoc = new XWikiDocument(pageTypesDocRef);
    expect(xwiki.exists(eq(pageTypesDocRef), same(context))).andReturn(true).anyTimes();
    expect(xwiki.getDocument(eq(pageTypesDocRef), same(context))).andReturn(
        pageTypesDoc).anyTimes();
    expect(xwiki.Param("celements.layout.default", "SimpleLayout")).andReturn(
        "SimpleLayout").once();
    DocumentReference simpleLayoutDocRef = new DocumentReference(context.getDatabase(),
        "SimpleLayout", "WebHome");
    XWikiDocument simpleLayoutDoc = new XWikiDocument(simpleLayoutDocRef);
    expect(xwiki.exists(eq(simpleLayoutDocRef), same(context))).andReturn(true).anyTimes();
    expect(xwiki.getDocument(eq(simpleLayoutDocRef), same(context))).andReturn(
        simpleLayoutDoc).anyTimes();
    DocumentReference simpleLayoutCentralDocRef = new DocumentReference("celements2web",
        "SimpleLayout", "WebHome");
    XWikiDocument simpleLayoutCentralDoc = new XWikiDocument(simpleLayoutDocRef);
    expect(xwiki.exists(eq(simpleLayoutCentralDocRef), same(context))).andReturn(true).anyTimes();
    expect(xwiki.getDocument(eq(simpleLayoutCentralDocRef), same(context))).andReturn(
        simpleLayoutCentralDoc).anyTimes();

    replayDefault();
    assertEquals("", command.addExtJSfileOnce(file));
    assertEquals("", command.addExtJSfileOnce(file));
    assertEquals("", command.addExtJSfileOnce(fileNotFound));
    String allStr = command.getAllExternalJavaScriptFiles();
    assertEquals("<script type=\"text/javascript\" src=\"" + file + "\"></script>\n"
        + "<!-- WARNING: js-file not found: " + fileNotFound + "-->\n", allStr);
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_beforeGetAll_explicitAndImplicit_double() throws Exception {
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Main",
        "WebHome");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    String fileNotFound = ":celJS/blabla.js";
    expect(attUrlCmd.getAttachmentURL(eq(fileNotFound), same(context))).andReturn(null).once();
    expect(attUrlCmd.isAttachmentLink(eq(fileNotFound))).andReturn(false).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(fileNotFound))).andReturn(true).anyTimes();
    String attFileURL = ":celJS/prototype.js";
    String file = "/skin/celJS/prototype.js?version=20110401120000";
    expect(attUrlCmd.getAttachmentURL(eq(attFileURL), same(context))).andReturn(file).anyTimes();
    expect(attUrlCmd.isAttachmentLink(eq(attFileURL))).andReturn(false).anyTimes();
    expect(attUrlCmd.isOnDiskLink(eq(attFileURL))).andReturn(true).anyTimes();
    String file2 = "/file/celJS/prototype.js?version=20110401120000";
    expect(attUrlCmd.getAttachmentURL(eq(attFileURL), eq("file"), same(context))).andReturn(
        file2).anyTimes();
    DocumentReference xwikiPrefDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiPreferences");
    XWikiDocument xwikiPrefDoc = new XWikiDocument(xwikiPrefDocRef);
    expect(xwiki.getDocument(eq(xwikiPrefDocRef), same(context))).andReturn(
        xwikiPrefDoc).anyTimes();
    DocumentReference mainPrefDocRef = new DocumentReference(context.getDatabase(), "Main",
        "WebPreferences");
    XWikiDocument mainPrefDoc = new XWikiDocument(mainPrefDocRef);
    expect(xwiki.getDocument(eq(mainPrefDocRef), same(context))).andReturn(mainPrefDoc).anyTimes();

    PageTypeReference pageTypeRef = new PageTypeReference("TestPageType", "providerHint",
        Arrays.asList(""));
    expect(pageTypeResolverMock.getPageTypeRefForCurrentDoc()).andReturn(pageTypeRef);
    DocumentReference pageTypesDocRef = new DocumentReference(context.getDatabase(), "PageTypes",
        "TestPageType");
    XWikiDocument pageTypesDoc = new XWikiDocument(pageTypesDocRef);
    pageTypesDoc.setNew(false);
    expect(xwiki.getDocument(eq(pageTypesDocRef), same(context))).andReturn(
        pageTypesDoc).atLeastOnce();
    DocumentReference mainWebHomeDocRef = new DocumentReference(context.getDatabase(), "Main",
        "WebHome");
    XWikiDocument mainWebHomeDoc = new XWikiDocument(pageTypesDocRef);
    expect(xwiki.getDocument(eq(mainWebHomeDocRef), same(context))).andReturn(
        mainWebHomeDoc).atLeastOnce();
    expect(xwiki.Param("celements.layout.default", "SimpleLayout")).andReturn(
        "SimpleLayout").once();
    DocumentReference simpleLayoutDocRef = new DocumentReference(context.getDatabase(),
        "SimpleLayout", "WebHome");
    XWikiDocument simpleLayoutDoc = new XWikiDocument(simpleLayoutDocRef);
    expect(xwiki.exists(eq(simpleLayoutDocRef), same(context))).andReturn(true).anyTimes();
    expect(xwiki.getDocument(eq(simpleLayoutDocRef), same(context))).andReturn(
        simpleLayoutDoc).anyTimes();
    DocumentReference simpleLayoutCentralDocRef = new DocumentReference("celements2web",
        "SimpleLayout", "WebHome");
    XWikiDocument simpleLayoutCentralDoc = new XWikiDocument(simpleLayoutDocRef);
    expect(xwiki.getDocument(eq(simpleLayoutCentralDocRef), same(context))).andReturn(
        simpleLayoutCentralDoc).anyTimes();

    replayDefault();
    assertEquals("", command.addExtJSfileOnce(attFileURL, "file"));
    assertEquals("", command.addExtJSfileOnce(attFileURL));
    assertEquals("", command.addExtJSfileOnce(fileNotFound));
    String allStr = command.getAllExternalJavaScriptFiles();
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
        command.addLazyExtJSfile(jsFile));
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
        command.addLazyExtJSfile(jsFile, action));
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
        command.addLazyExtJSfile(jsFile, action, "me=blu"));
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
        command.addLazyExtJSfile(jsFile, action, "me=blu"));
    verifyDefault();
  }
}
