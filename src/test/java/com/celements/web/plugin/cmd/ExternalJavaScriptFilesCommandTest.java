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
import static org.springframework.web.util.UriComponentsBuilder.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.util.UriComponentsBuilder;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.javascript.ExtJsFileParameter;
import com.celements.javascript.ExtJsFileParameter.Builder;
import com.celements.javascript.FrontendResourceResolver;
import com.celements.javascript.FrontendResourceResolver.FrontendResource;
import com.celements.javascript.JavaScriptExternalFilesClass;
import com.celements.javascript.JsFileEntry;
import com.celements.javascript.JsIsRteContent;
import com.celements.javascript.JsLoadMode;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.pagelayout.LayoutServiceRole;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class ExternalJavaScriptFilesCommandTest extends AbstractComponentTest {

  private ExternalJavaScriptFilesCommand command;
  private XWikiContext context;
  private AttachmentURLCommand attUrlCmd;
  private IPageTypeResolverRole pageTypeResolverMock;
  private IModelAccessFacade modelAccessMock;
  private LayoutServiceRole pageLayoutCmdMock;
  private FrontendResourceResolver resolver;

  @Before
  public void prepareTest() throws Exception {
    context = getXContext();
    context.put("vcontext", new VelocityContext());
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    pageTypeResolverMock = registerComponentMock(IPageTypeResolverRole.class);
    pageLayoutCmdMock = registerComponentMock(LayoutServiceRole.class);
    attUrlCmd = registerComponentMock(AttachmentURLCommand.class);
    registerComponentMock(CssCommand.class);
    resolver = registerComponentMock(FrontendResourceResolver.class);
    command = getBeanFactory().getBean(ExternalJavaScriptFilesCommand.class);
  }

  @Test
  public void test_addExtJSfileOnce_beforeGetAll() {
    String file = ":celJS/prototype.js";
    String filePath = "/file/celJS/prototype.js?version=12344";
    expect(resolver.isFrontendSource(eq(file))).andReturn(false).anyTimes();
    expect(resolver.get(eq(file))).andReturn(Optional.empty()).anyTimes();
    expect(attUrlCmd.getAttachmentURL(file, null, (String) null))
        .andReturn(Optional.of(UriComponentsBuilder.fromUriString(filePath).build()));
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(true).atLeastOnce();
    replayDefault();
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFile(file)
        .build()));
    verifyDefault();
  }

  @Test
  public void test_addExtJSfileOnce_beforeGetAll_fileNotFound() {
    String fileNotFound = "Content.WebHome;blabla.js";
    expect(resolver.isFrontendSource(eq(fileNotFound))).andReturn(false).anyTimes();
    expect(resolver.get(eq(fileNotFound))).andReturn(Optional.empty()).anyTimes();
    expect(attUrlCmd.getAttachmentURL(fileNotFound, null, (String) null))
        .andReturn(Optional.empty()).atLeastOnce();
    expect(attUrlCmd.isAttachmentLink(eq(fileNotFound))).andReturn(true).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(fileNotFound))).andReturn(false).anyTimes();
    replayDefault();
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFile(fileNotFound)
        .build()));
    verifyDefault();
  }

  @Test
  public void test_streamExtJsFiles() {
    CssCommand cssCommand = getMock(CssCommand.class);
    String frontendFile = ":frontend/progon/vue-poc/main.ts";
    String legacyFile = ":celJS/prototype.js";
    String otherFrontendFile = ":frontend/progon/eventview/main.ts";
    expect(resolver.isFrontendSource(eq(frontendFile))).andReturn(true).anyTimes();
    expect(resolver.isFrontendSource(eq(legacyFile))).andReturn(false).anyTimes();
    expect(resolver.isFrontendSource(eq(otherFrontendFile))).andReturn(true).anyTimes();
    expect(resolver.get(eq(frontendFile))).andReturn(Optional.empty()).anyTimes();
    expect(resolver.get(eq(otherFrontendFile))).andReturn(Optional.empty()).anyTimes();
    expectAddOnDiskFile(frontendFile, "/file/resources/dist/vue-poc.mjs");
    expectAddOnDiskFile(legacyFile, "/file/resources/celJS/prototype.js");
    expectAddOnDiskFile(otherFrontendFile, "/file/resources/dist/eventview.mjs");
    expect(cssCommand.includeCSSPage(eq(frontendFile))).andReturn(List.of());
    expect(cssCommand.includeCSSPage(eq(otherFrontendFile))).andReturn(List.of());

    replayDefault();

    command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFile(frontendFile)
        .build());
    command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFile(legacyFile)
        .build());
    command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFile(otherFrontendFile)
        .build());

    assertEquals(Arrays.asList(frontendFile, legacyFile, otherFrontendFile),
        command.streamExtJsFiles().toList());
    verifyDefault();
  }

  @Test
  public void test_addExtJSfileOnce_afterGetAll_frontendCss() {
    CssCommand cssCommand = getMock(CssCommand.class);
    String frontendFile = ":frontend/progon/vue-poc/main.ts";
    String jsUrl = "/file/resources/dist/vue-poc.mjs";
    String cssPath = "dist/assets/vue-poc.css";
    String cssUrl = "/file/resources/dist/assets/vue-poc.css";
    expect(resolver.isFrontendSource(eq(frontendFile))).andReturn(true).anyTimes();
    expect(resolver.get(eq(frontendFile)))
        .andReturn(Optional.of(new FrontendResource("dist/vue-poc.mjs", List.of(cssPath))));
    expect(attUrlCmd.isAttachmentLink(eq(frontendFile))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(frontendFile))).andReturn(true).atLeastOnce();
    expect(cssCommand.includeCSSPage(eq(frontendFile))).andReturn(List.of());
    expect(attUrlCmd.getAttachmentURL(frontendFile, "file", (String) null))
        .andReturn(Optional.of(fromUriString(jsUrl).build()));
    expect(attUrlCmd.getDiskFileUrl(eq(cssPath))).andReturn(cssUrl);

    replayDefault();

    command.injectDisplayAll(true);
    String includes = command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFile(frontendFile)
        .setAction("file")
        .build());

    assertEquals("<link rel=\"stylesheet\" title=\"\" media=\"all\" type=\"text/css\" href=\""
        + cssUrl + "\" />\n"
        + "<script type=\"module\" src=\"" + jsUrl + "\"></script>", includes);
    verifyDefault();
  }

  @Test
  public void test_addExtJSfileOnce_afterGetAll() {
    String file = "/skin/resources/celJS/prototype.js";
    expect(resolver.isFrontendSource(eq(file))).andReturn(false).anyTimes();
    expect(resolver.get(eq(file))).andReturn(Optional.empty()).anyTimes();
    expect(attUrlCmd.getAttachmentURL(file, null, (String) null))
        .andReturn(Optional.of(fromUriString(file).build())).atLeastOnce();
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(false).atLeastOnce();
    replayDefault();
    command.injectDisplayAll(true);
    final ExtJsFileParameter extJsFileParam = new ExtJsFileParameter.Builder()
        .setJsFile(file)
        .build();
    assertEquals("<script type=\"text/javascript\" src=\"" + file + "\"></script>",
        command.addExtJSfileOnce(extJsFileParam));
    assertEquals("", command.addExtJSfileOnce(extJsFileParam));
    verifyDefault();
  }

  @Test
  public void test_addExtJSfileOnce_afterGetAll_action() {
    String file = "/file/resources/celJS/prototype.js";
    expect(resolver.isFrontendSource(eq(file))).andReturn(false).anyTimes();
    expect(resolver.get(eq(file))).andReturn(Optional.empty()).anyTimes();
    expect(attUrlCmd.getAttachmentURL(file, "file", (String) null))
        .andReturn(Optional.of(fromUriString(file).build())).atLeastOnce();
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(false).atLeastOnce();
    replayDefault();
    command.injectDisplayAll(true);
    final ExtJsFileParameter extJsFile = new ExtJsFileParameter.Builder()
        .setJsFile(file)
        .setAction("file")
        .build();
    assertEquals("<script type=\"text/javascript\" src=\"" + file + "\"></script>",
        command.addExtJSfileOnce(extJsFile));
    assertEquals("", command.addExtJSfileOnce(extJsFile));
    verifyDefault();
  }

  @Test
  public void test_addExtJSfileOnce_afterGetAll_action_params() {
    String file = "/file/resources/celJS/prototype.js";
    expect(resolver.isFrontendSource(eq(file))).andReturn(false).anyTimes();
    expect(resolver.get(eq(file))).andReturn(Optional.empty()).anyTimes();
    expect(attUrlCmd.getAttachmentURL(file, "file", "me=blu"))
        .andReturn(Optional.of(fromUriString(file).queryParam("me", "blu").build())).atLeastOnce();
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(false).atLeastOnce();
    replayDefault();
    command.injectDisplayAll(true);
    assertEquals("<script type=\"text/javascript\" src=\"" + file + "?me=blu\"></script>",
        command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
            .setJsFile(file)
            .setAction("file")
            .setQueryString("me=blu")
            .build()));
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFile(file)
        .setAction("file")
        .setQueryString("me=blu")
        .build()));
    verifyDefault();
  }

  @Test
  public void test_addExtJSfileOnce_afterGetAll_action_params_onDisk() {
    String file = ":celJS/prototype.js";
    String fileURL = "/file/resources/celJS/prototype.js?version=201507061937";
    expect(resolver.isFrontendSource(eq(file))).andReturn(false).anyTimes();
    expect(resolver.get(eq(file))).andReturn(Optional.empty()).anyTimes();
    expect(attUrlCmd.getAttachmentURL(file, "file", "me=blu"))
        .andReturn(Optional.of(fromUriString(fileURL).queryParam("me", "blu").build()));
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(true).atLeastOnce();
    replayDefault();
    command.injectDisplayAll(true);
    final ExtJsFileParameter extJsFileParam = new ExtJsFileParameter.Builder()
        .setJsFile(file)
        .setAction("file")
        .setQueryString("me=blu")
        .build();
    assertEquals("<script type=\"text/javascript\" src=\"" + fileURL + "&amp;me=blu\"></script>",
        command.addExtJSfileOnce(extJsFileParam));
    assertEquals("", command.addExtJSfileOnce(extJsFileParam));
    verifyDefault();
  }

  @Test
  public void test_addExtJSfileOnce_afterGetAll_versioning() {
    String file = "celJS/prototype.js?version=20110401182200";
    expect(resolver.isFrontendSource(eq(file))).andReturn(false).anyTimes();
    expect(resolver.get(eq(file))).andReturn(Optional.empty()).anyTimes();
    expect(attUrlCmd.getAttachmentURL(file, null, (String) null))
        .andReturn(Optional.of(fromUriString(file).build())).atLeastOnce();
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(false).atLeastOnce();
    replayDefault();
    command.injectDisplayAll(true);
    assertEquals("<script type=\"text/javascript\" src=\"" + file + "\"></script>",
        command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
            .setJsFile(file)
            .build()));
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFile(file)
        .build()));
    verifyDefault();
  }

  @Test
  public void testGetExtStringForJsFile() {
    String url = "http://www.xyz.com?hi=yes&by=no";
    String urlEsc = "http://www.xyz.com?hi=yes&amp;by=no";
    String scriptStart = "<script type=\"text/javascript\" src=\"";
    String scriptEnd = "\"></script>";
    JsFileEntry jsFile = new JsFileEntry().addFilepath(url);
    assertEquals(scriptStart + urlEsc + scriptEnd, command.getExtStringForJsFile(jsFile));
  }

  @Test
  public void testGetExtStringForJsFile_defer() {
    String url = "http://www.xyz.com?hi=yes&by=no";
    String urlEsc = "http://www.xyz.com?hi=yes&amp;by=no";
    String scriptStart = "<script defer type=\"text/javascript\" src=\"";
    String scriptEnd = "\"></script>";
    JsFileEntry jsFile = new JsFileEntry().addFilepath(url).addLoadMode(JsLoadMode.DEFER);
    assertEquals(scriptStart + urlEsc + scriptEnd, command.getExtStringForJsFile(jsFile));
  }

  @Test
  public void testGetExtStringForJsFile_async() {
    String url = "http://www.xyz.com?hi=yes&by=no";
    String urlEsc = "http://www.xyz.com?hi=yes&amp;by=no";
    String scriptStart = "<script async type=\"text/javascript\" src=\"";
    String scriptEnd = "\"></script>";
    JsFileEntry jsFile = new JsFileEntry().addFilepath(url).addLoadMode(JsLoadMode.ASYNC);
    assertEquals(scriptStart + urlEsc + scriptEnd, command.getExtStringForJsFile(jsFile));
  }

  @Test
  public void testGetExtStringForJsFile_js() {
    String url = "http://www.xyz.com/file/resources/celJS/myPath/jsfile.js";
    String urlEsc = "http://www.xyz.com/file/resources/celJS/myPath/jsfile.js";
    String scriptStart = "<script type=\"text/javascript\" src=\"";
    String scriptEnd = "\"></script>";
    JsFileEntry jsFile = new JsFileEntry().addFilepath(url);
    assertEquals(scriptStart + urlEsc + scriptEnd, command.getExtStringForJsFile(jsFile));
  }

  @Test
  public void testGetExtStringForJsFile_jsm() {
    String url = "http://www.xyz.com/file/resources/celJS/myPath/jsfile.mjs";
    String urlEsc = "http://www.xyz.com/file/resources/celJS/myPath/jsfile.mjs";
    String scriptStart = "<script type=\"module\" src=\"";
    String scriptEnd = "\"></script>";
    JsFileEntry jsFile = new JsFileEntry().addFilepath(url).addLoadMode(JsLoadMode.DEFER);
    assertEquals(scriptStart + urlEsc + scriptEnd, command.getExtStringForJsFile(jsFile));
  }

  @Test
  public void testGetExtStringForJsFile_jsm_async() {
    String url = "http://www.xyz.com/file/resources/celJS/myPath/jsfile.mjs";
    String urlEsc = "http://www.xyz.com/file/resources/celJS/myPath/jsfile.mjs";
    String scriptStart = "<script async type=\"module\" src=\"";
    String scriptEnd = "\"></script>";
    JsFileEntry jsFile = new JsFileEntry().addFilepath(url).addLoadMode(JsLoadMode.ASYNC);
    assertEquals(scriptStart + urlEsc + scriptEnd, command.getExtStringForJsFile(jsFile));
  }

  @Test
  public void test_getAllRteContentJsFiles_Both() throws Exception {
    String attExtFilePath = "AlumniLayout.WebHome;tailwind.js";
    String attExtFileUrl = "/file/AlumniLayout/WebHome/tailwind.js";
    BaseObject extJsFileObj = new BaseObject();
    extJsFileObj.setXClassReference(JavaScriptExternalFilesClass.CLASS_REF);
    expect(attUrlCmd.isAttachmentLink(eq(attExtFilePath))).andReturn(true).atLeastOnce();
    expect(resolver.isFrontendSource(eq(attExtFilePath))).andReturn(false).anyTimes();
    expect(resolver.get(eq(attExtFilePath))).andReturn(Optional.empty()).anyTimes();
    expect(attUrlCmd.getAttachmentURL(attExtFilePath, null, (String) null))
        .andReturn(Optional.of(fromUriString(attExtFileUrl).build())).atLeastOnce();
    extJsFileObj.setStringValue(JavaScriptExternalFilesClass.FIELD_FILEPATH.getName(),
        attExtFilePath);
    extJsFileObj.setStringValue(JavaScriptExternalFilesClass.FIELD_IS_RTE_CONTENT.getName(),
        JsIsRteContent.BOTH.name());
    DocumentReference simpleLayoutDocRef = new DocumentReference(context.getDatabase(),
        "SimpleLayout", "WebHome");
    XWikiDocument simpleLayoutDoc = new XWikiDocument(simpleLayoutDocRef);
    simpleLayoutDoc.addXObject(extJsFileObj);
    expect(modelAccessMock.getDocument(eq(simpleLayoutDocRef))).andReturn(simpleLayoutDoc)
        .atLeastOnce();
    expect(pageLayoutCmdMock.getLayoutPropDocRefForCurrentDoc()).andReturn(Optional.of(
        simpleLayoutDocRef)).atLeastOnce();
    DocumentReference xwikiPrefDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiPreferences");
    XWikiDocument xwikiPrefDoc = new XWikiDocument(xwikiPrefDocRef);
    expect(modelAccessMock.getDocument(eq(xwikiPrefDocRef))).andReturn(xwikiPrefDoc).atLeastOnce();
    PageTypeReference pageTypeRef = new PageTypeReference("TestPageType", "providerHint",
        Arrays.asList(""));
    expect(pageTypeResolverMock.resolvePageTypeRefForCurrentDoc()).andReturn(pageTypeRef);
    DocumentReference pageTypesDocRef = new DocumentReference(context.getDatabase(), "PageTypes",
        "TestPageType");
    XWikiDocument pageTypesDoc = new XWikiDocument(pageTypesDocRef);
    expect(modelAccessMock.getDocument(eq(pageTypesDocRef))).andReturn(pageTypesDoc).atLeastOnce();
    replayDefault();
    List<JsFileEntry> jsFileList = command.getAllRteContentJsFiles();
    verifyDefault();
    assertEquals(1, jsFileList.size());
    assertEquals(attExtFileUrl, jsFileList.get(0).getFilepath());
  }

  @Test
  public void test_getAllRteContentJsFiles_only() throws Exception {
    String attExtFilePath = "AlumniLayout.WebHome;tailwind.js";
    String attExtFileUrl = "/file/AlumniLayout/WebHome/tailwind.js";
    BaseObject extJsFileObj = new BaseObject();
    extJsFileObj.setXClassReference(JavaScriptExternalFilesClass.CLASS_REF);
    expect(attUrlCmd.isAttachmentLink(eq(attExtFilePath))).andReturn(true).atLeastOnce();
    expect(resolver.isFrontendSource(eq(attExtFilePath))).andReturn(false).anyTimes();
    expect(resolver.get(eq(attExtFilePath))).andReturn(Optional.empty()).anyTimes();
    expect(attUrlCmd.getAttachmentURL(attExtFilePath, null, (String) null))
        .andReturn(Optional.of(fromUriString(attExtFileUrl).build())).atLeastOnce();
    extJsFileObj.setStringValue(JavaScriptExternalFilesClass.FIELD_FILEPATH.getName(),
        attExtFilePath);
    extJsFileObj.setStringValue(JavaScriptExternalFilesClass.FIELD_IS_RTE_CONTENT.getName(),
        JsIsRteContent.ONLY.name());
    DocumentReference simpleLayoutDocRef = new DocumentReference(context.getDatabase(),
        "SimpleLayout", "WebHome");
    XWikiDocument simpleLayoutDoc = new XWikiDocument(simpleLayoutDocRef);
    simpleLayoutDoc.addXObject(extJsFileObj);
    expect(modelAccessMock.getDocument(eq(simpleLayoutDocRef))).andReturn(simpleLayoutDoc)
        .atLeastOnce();
    expect(pageLayoutCmdMock.getLayoutPropDocRefForCurrentDoc()).andReturn(Optional.of(
        simpleLayoutDocRef)).atLeastOnce();
    DocumentReference xwikiPrefDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiPreferences");
    XWikiDocument xwikiPrefDoc = new XWikiDocument(xwikiPrefDocRef);
    expect(modelAccessMock.getDocument(eq(xwikiPrefDocRef))).andReturn(xwikiPrefDoc).atLeastOnce();
    PageTypeReference pageTypeRef = new PageTypeReference("TestPageType", "providerHint",
        Arrays.asList(""));
    expect(pageTypeResolverMock.resolvePageTypeRefForCurrentDoc()).andReturn(pageTypeRef);
    DocumentReference pageTypesDocRef = new DocumentReference(context.getDatabase(), "PageTypes",
        "TestPageType");
    XWikiDocument pageTypesDoc = new XWikiDocument(pageTypesDocRef);
    expect(modelAccessMock.getDocument(eq(pageTypesDocRef))).andReturn(pageTypesDoc).atLeastOnce();
    replayDefault();
    List<JsFileEntry> jsFileList = command.getAllRteContentJsFiles();
    verifyDefault();
    assertEquals(1, jsFileList.size());
    assertEquals(attExtFileUrl, jsFileList.get(0).getFilepath());
  }

  @Test
  public void test_getAllRteContentJsFiles_No() throws Exception {
    String attExtFilePath = "AlumniLayout.WebHome;tailwind.js";
    String attExtFileUrl = "/file/AlumniLayout/WebHome/tailwind.js";
    BaseObject extJsFileObj = new BaseObject();
    extJsFileObj.setXClassReference(JavaScriptExternalFilesClass.CLASS_REF);
    expect(attUrlCmd.isAttachmentLink(eq(attExtFilePath))).andReturn(true).atLeastOnce();
    expect(resolver.isFrontendSource(eq(attExtFilePath))).andReturn(false).anyTimes();
    expect(resolver.get(eq(attExtFilePath))).andReturn(Optional.empty()).anyTimes();
    expect(attUrlCmd.getAttachmentURL(attExtFilePath, null, (String) null))
        .andReturn(Optional.of(fromUriString(attExtFileUrl).build())).atLeastOnce();
    extJsFileObj.setStringValue(JavaScriptExternalFilesClass.FIELD_FILEPATH.getName(),
        attExtFilePath);
    extJsFileObj.setStringValue(JavaScriptExternalFilesClass.FIELD_IS_RTE_CONTENT.getName(),
        JsIsRteContent.NO.name());
    DocumentReference simpleLayoutDocRef = new DocumentReference(context.getDatabase(),
        "SimpleLayout", "WebHome");
    XWikiDocument simpleLayoutDoc = new XWikiDocument(simpleLayoutDocRef);
    simpleLayoutDoc.addXObject(extJsFileObj);
    expect(modelAccessMock.getDocument(eq(simpleLayoutDocRef))).andReturn(simpleLayoutDoc)
        .atLeastOnce();
    expect(pageLayoutCmdMock.getLayoutPropDocRefForCurrentDoc()).andReturn(Optional.of(
        simpleLayoutDocRef)).atLeastOnce();
    DocumentReference xwikiPrefDocRef = new DocumentReference(context.getDatabase(), "XWiki",
        "XWikiPreferences");
    XWikiDocument xwikiPrefDoc = new XWikiDocument(xwikiPrefDocRef);
    expect(modelAccessMock.getDocument(eq(xwikiPrefDocRef))).andReturn(xwikiPrefDoc).atLeastOnce();
    PageTypeReference pageTypeRef = new PageTypeReference("TestPageType", "providerHint",
        Arrays.asList(""));
    expect(pageTypeResolverMock.resolvePageTypeRefForCurrentDoc()).andReturn(pageTypeRef);
    DocumentReference pageTypesDocRef = new DocumentReference(context.getDatabase(), "PageTypes",
        "TestPageType");
    XWikiDocument pageTypesDoc = new XWikiDocument(pageTypesDocRef);
    expect(modelAccessMock.getDocument(eq(pageTypesDocRef))).andReturn(pageTypesDoc).atLeastOnce();
    replayDefault();
    List<JsFileEntry> jsFileList = command.getAllRteContentJsFiles();
    verifyDefault();
    assertTrue(jsFileList.isEmpty());
  }

  @Test
  public void test_addExtJSfileOnce_afterGetAll_fileNotFound_url() {
    String fileNotFound = "/download/Content/WebHome/blabla.js";
    expect(resolver.isFrontendSource(eq(fileNotFound))).andReturn(false).anyTimes();
    expect(resolver.get(eq(fileNotFound))).andReturn(Optional.empty()).anyTimes();
    expect(attUrlCmd.getAttachmentURL(fileNotFound, null, (String) null))
        .andReturn(Optional.empty()).atLeastOnce();
    expect(attUrlCmd.isAttachmentLink(eq(fileNotFound))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(fileNotFound))).andReturn(false).atLeastOnce();
    replayDefault();
    command.injectDisplayAll(true);
    assertEquals("<!-- WARNING: js-file not found: " + fileNotFound + "-->",
        command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
            .setJsFile(fileNotFound)
            .build()));
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFile(fileNotFound)
        .build()));
    verifyDefault();
  }

  @Test
  public void test_addExtJSfileOnce_afterGetAll_fileNotFound_attUrl() {
    String fileNotFound = "Content.WebHome;blabla.js";
    expect(resolver.isFrontendSource(eq(fileNotFound))).andReturn(false).anyTimes();
    expect(resolver.get(eq(fileNotFound))).andReturn(Optional.empty()).anyTimes();
    expect(attUrlCmd.getAttachmentURL(fileNotFound, null, (String) null))
        .andReturn(Optional.empty()).atLeastOnce();
    expect(attUrlCmd.isAttachmentLink(eq(fileNotFound))).andReturn(true).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(fileNotFound))).andReturn(false).anyTimes();
    replayDefault();
    command.injectDisplayAll(true);
    assertEquals("<!-- WARNING: js-file not found: " + fileNotFound + "-->",
        command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
            .setJsFile(fileNotFound)
            .build()));
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFile(fileNotFound)
        .build()));
    verifyDefault();
  }

  @Test
  public void test_addAllExtJSfilesFromDocRef_emptyDoc() throws Exception {
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Content",
        "TestPage");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    expect(modelAccessMock.getDocument(eq(contextDocRef))).andReturn(contextDoc).atLeastOnce();
    context.setDoc(contextDoc);
    replayDefault();
    command.addAllExtJSfilesFromDocRef(contextDocRef);
    verifyDefault();
  }

  @Test
  public void test_addAllExtJSfilesFromDocRef_sync() throws Exception {
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Content",
        "TestPage");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    BaseObject extJsFileObj = new BaseObject();
    extJsFileObj.setXClassReference(JavaScriptExternalFilesClass.CLASS_REF);
    String filePath = "/skin/resources/celJS/prototype.js?version=20220401120000";
    expect(attUrlCmd.isAttachmentLink(eq(filePath))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(filePath))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.getAttachmentURL(filePath, null, (String) null))
        .andReturn(Optional.of(fromUriString(filePath).build())).atLeastOnce();
    extJsFileObj.setStringValue(JavaScriptExternalFilesClass.FIELD_FILEPATH.getName(), filePath);
    contextDoc.addXObject(extJsFileObj);
    expect(modelAccessMock.getDocument(eq(contextDocRef))).andReturn(contextDoc).atLeastOnce();
    context.setDoc(contextDoc);
    replayDefault();
    command.addAllExtJSfilesFromDocRef(contextDocRef);
    assertEquals("must be already added by addAllExtJSfilesFromDocRef", "",
        command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
            .setJsFile(filePath)
            .build()));
    verifyDefault();
  }

  @Test
  public void test_addAllExtJSfilesFromDocRef_defer() throws Exception {
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Content",
        "TestPage");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    BaseObject extJsFileObj = new BaseObject();
    extJsFileObj.setXClassReference(JavaScriptExternalFilesClass.CLASS_REF);
    String filePath = "/skin/resources/celJS/prototype.js?version=20220401120000";
    expect(attUrlCmd.isAttachmentLink(eq(filePath))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(filePath))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.getAttachmentURL(filePath, null, (String) null))
        .andReturn(Optional.of(fromUriString(filePath).build())).atLeastOnce();
    JsLoadMode loadMode = JsLoadMode.DEFER;
    extJsFileObj.setStringValue(JavaScriptExternalFilesClass.FIELD_FILEPATH.getName(), filePath);
    extJsFileObj.setStringValue(JavaScriptExternalFilesClass.FIELD_LOAD_MODE.getName(),
        loadMode.toString());
    contextDoc.addXObject(extJsFileObj);
    expect(modelAccessMock.getDocument(eq(contextDocRef))).andReturn(contextDoc).atLeastOnce();
    context.setDoc(contextDoc);
    replayDefault();
    command.addAllExtJSfilesFromDocRef(contextDocRef);
    assertEquals("must be already added by addAllExtJSfilesFromDocRef", "",
        command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
            .setJsFile(filePath)
            .setLoadMode(loadMode)
            .build()));
    verifyDefault();
  }

  @Test
  public void test_addAllExtJSfilesFromDocRef_async() throws Exception {
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Content",
        "TestPage");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    BaseObject extJsFileObj = new BaseObject();
    extJsFileObj.setXClassReference(JavaScriptExternalFilesClass.CLASS_REF);
    String filePath = "/skin/resources/celJS/prototype.js?version=20220401120000";
    expect(attUrlCmd.isAttachmentLink(eq(filePath))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(filePath))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.getAttachmentURL(filePath, null, (String) null))
        .andReturn(Optional.of(fromUriString(filePath).build())).atLeastOnce();
    JsLoadMode loadMode = JsLoadMode.ASYNC;
    extJsFileObj.setStringValue(JavaScriptExternalFilesClass.FIELD_FILEPATH.getName(), filePath);
    extJsFileObj.setStringValue(JavaScriptExternalFilesClass.FIELD_LOAD_MODE.getName(),
        loadMode.toString());
    contextDoc.addXObject(extJsFileObj);
    expect(modelAccessMock.getDocument(eq(contextDocRef))).andReturn(contextDoc).atLeastOnce();
    context.setDoc(contextDoc);
    replayDefault();
    command.addAllExtJSfilesFromDocRef(contextDocRef);
    assertEquals("must be already added by addAllExtJSfilesFromDocRef", "",
        command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
            .setJsFile(filePath)
            .setLoadMode(loadMode)
            .build()));
    verifyDefault();
  }

  @Test
  public void test_addAllExtJSfilesFromDocRef_notExists() throws Exception {
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Content",
        "TestPage");
    expect(modelAccessMock.getDocument(eq(contextDocRef)))
        .andThrow(new DocumentNotExistsException(contextDocRef)).atLeastOnce();
    replayDefault();
    command.addAllExtJSfilesFromDocRef(contextDocRef);
    verifyDefault();
  }

  @Test
  public void test_addExtJSfileOnce_beforeGetAll_double() throws Exception {
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Main",
        "WebHome");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    String fileNotFound = "celJS/blabla.js";
    expect(resolver.isFrontendSource(eq(fileNotFound))).andReturn(false).anyTimes();
    expect(resolver.get(eq(fileNotFound))).andReturn(Optional.empty()).anyTimes();
    expect(attUrlCmd.getAttachmentURL(fileNotFound, null, (String) null))
        .andReturn(Optional.empty()).atLeastOnce();
    expect(attUrlCmd.isAttachmentLink(eq(fileNotFound))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(fileNotFound))).andReturn(true).atLeastOnce();
    String file = "/skin/resources/celJS/prototype.js?version=20110401120000";
    expect(resolver.isFrontendSource(eq(file))).andReturn(false).anyTimes();
    expect(resolver.get(eq(file))).andReturn(Optional.empty()).anyTimes();
    expect(attUrlCmd.getAttachmentURL(file, null, (String) null))
        .andReturn(Optional.of(fromUriString(file).build())).atLeastOnce();
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
    expect(pageLayoutCmdMock.getLayoutPropDocRefForCurrentDoc()).andReturn(Optional.of(
        simpleLayoutDocRef)).atLeastOnce();
    final ExtJsFileParameter fileParams = new ExtJsFileParameter.Builder()
        .setJsFile(file)
        .build();
    final ExtJsFileParameter fileNotFoundParams = new ExtJsFileParameter.Builder()
        .setJsFile(fileNotFound)
        .build();
    replayDefault();
    assertEquals("", command.addExtJSfileOnce(fileParams));
    assertEquals("", command.addExtJSfileOnce(fileParams));
    assertEquals("", command.addExtJSfileOnce(fileNotFoundParams));
    String allStr = command.getAllExternalJavaScriptFiles();
    assertEquals("<script type=\"text/javascript\" src=\"" + file + "\"></script>\n"
        + "<!-- WARNING: js-file not found: " + fileNotFound + "-->\n", allStr);
    verifyDefault();
  }

  @Test
  public void test_addExtJSfileOnce_beforeGetAll_explicitAndImplicit_double() throws Exception {
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Main",
        "WebHome");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    String fileNotFound = ":celJS/blabla.js";
    expect(resolver.isFrontendSource(eq(fileNotFound))).andReturn(false).anyTimes();
    expect(resolver.get(eq(fileNotFound))).andReturn(Optional.empty()).anyTimes();
    expect(attUrlCmd.getAttachmentURL(fileNotFound, null, (String) null))
        .andReturn(Optional.empty()).atLeastOnce();
    expect(attUrlCmd.isAttachmentLink(eq(fileNotFound))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(fileNotFound))).andReturn(true).atLeastOnce();
    String attFileURL = ":celJS/prototype.js";
    expect(resolver.isFrontendSource(eq(attFileURL))).andReturn(false).anyTimes();
    expect(resolver.get(eq(attFileURL))).andReturn(Optional.empty()).anyTimes();
    expect(attUrlCmd.isAttachmentLink(eq(attFileURL))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(attFileURL))).andReturn(true).atLeastOnce();
    String file2 = "/file/celJS/prototype.js?version=20110401120000";
    expect(attUrlCmd.getAttachmentURL(attFileURL, "file", (String) null))
        .andReturn(Optional.of(fromUriString(file2).build())).atLeastOnce();
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
    expect(pageLayoutCmdMock.getLayoutPropDocRefForCurrentDoc()).andReturn(Optional.of(
        simpleLayoutDocRef)).atLeastOnce();
    replayDefault();
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFile(attFileURL)
        .setAction("file")
        .build()));
    Builder paramBuilder = new ExtJsFileParameter.Builder();
    assertEquals("", command.addExtJSfileOnce(paramBuilder
        .setJsFile(attFileURL)
        .build()));
    assertEquals("", command.addExtJSfileOnce(paramBuilder
        .setJsFile(fileNotFound)
        .build()));
    String allStr = command.getAllExternalJavaScriptFiles();
    assertEquals("<script type=\"text/javascript\""
        + " src=\"/file/celJS/prototype.js?version=20110401120000\"></script>\n"
        + "<!-- WARNING: js-file not found: " + fileNotFound + "-->\n", allStr);
    verifyDefault();
  }

  @Test
  public void test_addLazyExtJSfile() {
    String jsFile = ":celJS/celTabMenu/loadTinyMCE-async.js";
    String jsFileURL = "/file/resources/celJS/celTabMenu/loadTinyMCE-async.js";
    expect(attUrlCmd.getAttachmentURL(jsFile, null, (String) null))
        .andReturn(Optional.of(fromUriString(jsFileURL).build()));
    replayDefault();
    assertEquals(
        "<cel-lazy-load-js src=\"" + jsFileURL + "\" loadMode=\"SYNC\">"
            + "</cel-lazy-load-js>",
        command.getLazyLoadTag(new ExtJsFileParameter.Builder()
            .setJsFile(jsFile)
            .build()));
    verifyDefault();
  }

  @Test
  public void test_addLazyExtJSfile_action() {
    String jsFile = ":celJS/celTabMenu/loadTinyMCE-async.js";
    String jsFileURL = "/file/resources/celJS/celTabMenu/loadTinyMCE-async.js";
    String action = "file";
    expect(attUrlCmd.getAttachmentURL(jsFile, action, (String) null))
        .andReturn(Optional.of(fromUriString(jsFileURL).build()));
    replayDefault();
    assertEquals("<cel-lazy-load-js src=\"" + jsFileURL + "\" loadMode=\"SYNC\">"
        + "</cel-lazy-load-js>",
        command.getLazyLoadTag(new ExtJsFileParameter.Builder()
            .setJsFile(jsFile)
            .setAction(action)
            .build()));
    verifyDefault();
  }

  @Test
  public void test_addLazyExtJSfile_action_params() {
    String jsFile = "mySpace.myDoc;loadTinyMCE-async.js";
    String jsFileURL = "/download/mySpace/myDoc/loadTinyMCE-async.js";
    String action = "file";
    expect(attUrlCmd.getAttachmentURL(jsFile, action, "me=blu"))
        .andReturn(Optional.of(fromUriString(jsFileURL).queryParam("me", "blu").build()));
    replayDefault();
    assertEquals("<cel-lazy-load-js src=\"" + jsFileURL + "?me=blu\" loadMode=\"SYNC\">"
        + "</cel-lazy-load-js>",
        command.getLazyLoadTag(new ExtJsFileParameter.Builder()
            .setJsFile(jsFile)
            .setAction(action)
            .setQueryString("me=blu")
            .build()));
    verifyDefault();
  }

  @Test
  public void test_addLazyExtJSfile_action_params_onDisk() {
    String jsFile = ":celJS/celTabMenu/loadTinyMCE-async.js";
    String jsFileURL = "/file/resources/celJS/celTabMenu/loadTinyMCE-async.js"
        + "?version=201507061937";
    String action = "file";
    expect(attUrlCmd.getAttachmentURL(jsFile, action, "me=blu"))
        .andReturn(Optional.of(fromUriString(jsFileURL).queryParam("me", "blu").build()));
    replayDefault();
    assertEquals("<cel-lazy-load-js src=\"" + jsFileURL
        + "&me=blu\" loadMode=\"SYNC\"></cel-lazy-load-js>",
        command.getLazyLoadTag(new ExtJsFileParameter.Builder()
            .setJsFile(jsFile)
            .setAction(action)
            .setQueryString("me=blu")
            .build()));
    verifyDefault();
  }

  private void expectAddOnDiskFile(String file, String url) {
    expect(attUrlCmd.isAttachmentLink(eq(file))).andReturn(false).atLeastOnce();
    expect(attUrlCmd.isOnDiskLink(eq(file))).andReturn(true).atLeastOnce();
    expect(attUrlCmd.getAttachmentURL(file, null, (String) null))
        .andReturn(Optional.of(fromUriString(url).build()));
  }

}
