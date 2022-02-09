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
import java.util.Optional;

import javax.ws.rs.core.UriBuilder;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.filebase.references.FileReference;
import com.celements.filebase.uri.FileNotExistException;
import com.celements.filebase.uri.FileUriServiceRole;
import com.celements.javascript.ExtJsFileParameter;
import com.celements.javascript.ExtJsFileParameter.Builder;
import com.celements.javascript.JavaScriptExternalFilesClass;
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

  private ExternalJavaScriptFilesCommand command = null;
  private XWikiContext context = null;
  private IPageTypeResolverRole pageTypeResolverMock;
  private IModelAccessFacade modelAccessMock;
  private LayoutServiceRole pageLayoutCmdMock;
  private FileUriServiceRole resUrlSrv;

  @Before
  public void setUp_ExternalJavaScriptFilesCommandTest() throws Exception {
    context = getContext();
    context.put("vcontext", new VelocityContext());
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    pageTypeResolverMock = registerComponentMock(IPageTypeResolverRole.class);
    pageLayoutCmdMock = registerComponentMock(LayoutServiceRole.class);
    resUrlSrv = registerComponentMock(FileUriServiceRole.class);
    command = new ExternalJavaScriptFilesCommand();
  }

  @Test
  public void testAddExtJSfileOnce_beforeGetAll() throws Exception {
    FileReference fileRef = FileReference.of(":celJS/prototype.js").build();
    replayDefault();
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFileRef(fileRef)
        .build()));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_beforeGetAll_fileNotFound() throws Exception {
    FileReference fileNotFoundRef = FileReference.of("Content.WebHome;blabla.js").build();
    expect(
        resUrlSrv.createFileUri(eq(fileNotFoundRef), eq(Optional.empty()), eq(Optional.empty())))
            .andThrow(new FileNotExistException(fileNotFoundRef)).once();
    replayDefault();
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFileRef(fileNotFoundRef)
        .build()));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll() throws Exception {
    FileReference fileRef = FileReference.of("/skin/resources/celJS/prototype.js").build();
    UriBuilder fileUri = UriBuilder.fromPath(fileRef.getFullPath());
    expect(resUrlSrv.createFileUri(eq(fileRef), eq(Optional.empty()), eq(Optional.empty())))
        .andReturn(fileUri).atLeastOnce();
    replayDefault();
    command.injectDisplayAll(true);
    final ExtJsFileParameter extJsFileParam = new ExtJsFileParameter.Builder()
        .setJsFileRef(fileRef)
        .build();
    assertEquals("<script type=\"text/javascript\" src=\"" + fileRef.getFullPath() + "\"></script>",
        command.addExtJSfileOnce(extJsFileParam));
    assertEquals("", command.addExtJSfileOnce(extJsFileParam));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll_action() throws Exception {
    FileReference fileRef = FileReference.of("/file/resources/celJS/prototype.js").build();
    UriBuilder fileUri = UriBuilder.fromPath(fileRef.getFullPath());
    expect(resUrlSrv.createFileUri(eq(fileRef), eq(Optional.of("file")), eq(Optional.empty())))
        .andReturn(fileUri).atLeastOnce();
    replayDefault();
    command.injectDisplayAll(true);
    final ExtJsFileParameter extJsFile = new ExtJsFileParameter.Builder()
        .setJsFileRef(fileRef)
        .setAction("file")
        .build();
    assertEquals("<script type=\"text/javascript\" src=\"" + fileRef.getFullPath() + "\"></script>",
        command.addExtJSfileOnce(extJsFile));
    assertEquals("", command.addExtJSfileOnce(extJsFile));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll_action_params() throws Exception {
    FileReference fileRef = FileReference.of("/file/resources/celJS/prototype.js").build();
    String params = "me=blu";
    String expectedFilePath = fileRef.getFullPath() + "?" + params;
    UriBuilder expectedFileUri = UriBuilder.fromPath(fileRef.getFullPath()).replaceQuery(params);
    expect(resUrlSrv.createFileUri(eq(fileRef), eq(Optional.of("file")), eq(Optional.of(params))))
        .andReturn(expectedFileUri).atLeastOnce();
    replayDefault();
    command.injectDisplayAll(true);
    assertEquals("<script type=\"text/javascript\" src=\"" + expectedFilePath + "\"></script>",
        command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
            .setJsFileRef(fileRef)
            .setAction("file")
            .setQueryString(params)
            .build()));
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFileRef(fileRef)
        .setAction("file")
        .setQueryString(params)
        .build()));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll_action_params_onDisk() throws Exception {
    FileReference fileRef = FileReference.of(":celJS/prototype.js").build();
    String fileURL = "/file/resources/celJS/prototype.js?version=201507061937";
    UriBuilder expectedFileUri = UriBuilder.fromPath("/file/resources/celJS/prototype.js")
        .queryParam("version", "201507061937")
        .queryParam("me", "blu");
    String params = "me=blu";
    expect(resUrlSrv.createFileUri(eq(fileRef), eq(Optional.of("file")), eq(Optional.of(params))))
        .andReturn(expectedFileUri).atLeastOnce();
    replayDefault();
    command.injectDisplayAll(true);
    final ExtJsFileParameter extJsFileParam = new ExtJsFileParameter.Builder()
        .setJsFileRef(fileRef)
        .setAction("file")
        .setQueryString("me=blu")
        .build();
    assertEquals("<script type=\"text/javascript\" src=\"" + fileURL + "&amp;" + params
        + "\"></script>", command.addExtJSfileOnce(extJsFileParam));
    assertEquals("", command.addExtJSfileOnce(extJsFileParam));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll_versioning() throws Exception {
    String versionStr = "version=20110401182200";
    FileReference fileRef = FileReference.of("celJS/prototype.js?" + versionStr).build();
    UriBuilder expectedFileUri = UriBuilder.fromPath("celJS/prototype.js")
        .replaceQuery(versionStr);
    expect(resUrlSrv.createFileUri(eq(fileRef), eq(Optional.empty()), eq(Optional.of(versionStr))))
        .andReturn(expectedFileUri).atLeastOnce();
    replayDefault();
    command.injectDisplayAll(true);
    assertEquals("<script type=\"text/javascript\" src=\"" + expectedFileUri + "\"></script>",
        command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
            .setJsFileRef(fileRef)
            .build()));
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFileRef(fileRef)
        .build()));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll_fileNotFound_url() throws Exception {
    FileReference fileNotFoundRef = FileReference.of("/download/Content/WebHome/blabla.js").build();
    expect(
        resUrlSrv.createFileUri(eq(fileNotFoundRef), eq(Optional.empty()), eq(Optional.empty())))
            .andThrow(new FileNotExistException(fileNotFoundRef)).atLeastOnce();
    replayDefault();
    command.injectDisplayAll(true);
    assertEquals("<!-- WARNING: js-file not found: " + fileNotFoundRef.getFullPath() + " -->",
        command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
            .setJsFileRef(fileNotFoundRef)
            .build()));
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFileRef(fileNotFoundRef)
        .build()));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll_fileNotFound_attUrl() throws Exception {
    FileReference fileNotFoundRef = FileReference.of("Content.WebHome;blabla.js").build();
    expect(
        resUrlSrv.createFileUri(eq(fileNotFoundRef), eq(Optional.empty()), eq(Optional.empty())))
            .andThrow(new FileNotExistException(fileNotFoundRef)).once();
    replayDefault();
    command.injectDisplayAll(true);
    assertEquals("<!-- WARNING: js-file not found: " + fileNotFoundRef + " -->",
        command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
            .setJsFileRef(fileNotFoundRef)
            .build()));
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFileRef(fileNotFoundRef)
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
    String versionStr = "version=20220401120000";
    String fileUrlStr = "/skin/resources/celJS/prototype.js?" + versionStr;
    FileReference fileRef = FileReference.of(fileUrlStr).build();
    UriBuilder expectedFileUri = UriBuilder.fromPath("/skin/resources/celJS/prototype.js")
        .replaceQuery(versionStr);
    expect(resUrlSrv.createFileUri(eq(fileRef), eq(Optional.empty()), eq(Optional.of(versionStr))))
        .andReturn(expectedFileUri).atLeastOnce();
    extJsFileObj.setStringValue(JavaScriptExternalFilesClass.FIELD_FILEPATH.getName(), fileUrlStr);
    contextDoc.addXObject(extJsFileObj);
    expect(modelAccessMock.getDocument(eq(contextDocRef))).andReturn(contextDoc).atLeastOnce();
    context.setDoc(contextDoc);
    replayDefault();
    command.addAllExtJSfilesFromDocRef(contextDocRef);
    assertEquals("must be already added by addAllExtJSfilesFromDocRef", "",
        command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
            .setJsFileRef(fileRef)
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
    String versionStr = "version=20220401120000";
    String fileUrlStr = "/skin/resources/celJS/prototype.js?" + versionStr;
    FileReference fileRef = FileReference.of(fileUrlStr).build();
    UriBuilder expectedFileUri = UriBuilder.fromPath("/skin/resources/celJS/prototype.js")
        .replaceQuery(versionStr);
    expect(resUrlSrv.createFileUri(eq(fileRef), eq(Optional.empty()), eq(Optional.of(versionStr))))
        .andReturn(expectedFileUri).atLeastOnce();
    JsLoadMode loadMode = JsLoadMode.DEFER;
    extJsFileObj.setStringValue(JavaScriptExternalFilesClass.FIELD_FILEPATH.getName(), fileUrlStr);
    extJsFileObj.setStringValue(JavaScriptExternalFilesClass.FIELD_LOAD_MODE.getName(),
        loadMode.toString());
    contextDoc.addXObject(extJsFileObj);
    expect(modelAccessMock.getDocument(eq(contextDocRef))).andReturn(contextDoc).atLeastOnce();
    context.setDoc(contextDoc);
    replayDefault();
    command.addAllExtJSfilesFromDocRef(contextDocRef);
    assertEquals("must be already added by addAllExtJSfilesFromDocRef", "",
        command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
            .setJsFileRef(fileRef)
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
    String versionStr = "version=20220401120000";
    String fileUrlStr = "/skin/resources/celJS/prototype.js?" + versionStr;
    FileReference fileRef = FileReference.of(fileUrlStr).build();
    UriBuilder expectedFileUri = UriBuilder.fromPath("/skin/resources/celJS/prototype.js")
        .replaceQuery(versionStr);
    expect(resUrlSrv.createFileUri(eq(fileRef), eq(Optional.empty()), eq(Optional.of(versionStr))))
        .andReturn(expectedFileUri).atLeastOnce();
    JsLoadMode loadMode = JsLoadMode.ASYNC;
    extJsFileObj.setStringValue(JavaScriptExternalFilesClass.FIELD_FILEPATH.getName(), fileUrlStr);
    extJsFileObj.setStringValue(JavaScriptExternalFilesClass.FIELD_LOAD_MODE.getName(),
        loadMode.toString());
    contextDoc.addXObject(extJsFileObj);
    expect(modelAccessMock.getDocument(eq(contextDocRef))).andReturn(contextDoc).atLeastOnce();
    context.setDoc(contextDoc);
    replayDefault();
    command.addAllExtJSfilesFromDocRef(contextDocRef);
    assertEquals("must be already added by addAllExtJSfilesFromDocRef", "",
        command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
            .setJsFileRef(fileRef)
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
  public void testAddExtJSfileOnce_beforeGetAll_double() throws Exception {
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Main",
        "WebHome");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    FileReference fileNotFoundRef = FileReference.of("celJS/blabla.js").build();
    expect(resUrlSrv.createFileUri(eq(fileNotFoundRef), eq(Optional.empty()), eq(Optional.empty())))
        .andThrow(new FileNotExistException(fileNotFoundRef)).atLeastOnce();
    String versionStr = "version=20110401120000";
    FileReference fileRef = FileReference
        .of("/skin/resources/celJS/prototype.js?" + versionStr).build();
    UriBuilder expectedFileUri = UriBuilder.fromPath("/skin/resources/celJS/prototype.js")
        .replaceQuery(versionStr);
    expect(resUrlSrv.createFileUri(eq(fileRef), eq(Optional.empty()),
        eq(Optional.of(versionStr))))
            .andReturn(expectedFileUri).atLeastOnce();
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
        .setJsFileRef(fileRef)
        .build();
    final ExtJsFileParameter fileNotFoundParams = new ExtJsFileParameter.Builder()
        .setJsFileRef(fileNotFoundRef)
        .build();
    replayDefault();
    assertEquals("", command.addExtJSfileOnce(fileParams));
    assertEquals("", command.addExtJSfileOnce(fileParams));
    assertEquals("", command.addExtJSfileOnce(fileNotFoundParams));
    String allStr = command.getAllExternalJavaScriptFiles();
    assertEquals(
        "<script type=\"text/javascript\" src=\"" + expectedFileUri + "\"></script>\n"
            + "<!-- WARNING: js-file not found: " + fileNotFoundRef.getFullPath() + " -->\n",
        allStr);
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_beforeGetAll_explicitAndImplicit_double() throws Exception {
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Main",
        "WebHome");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    FileReference fileNotFoundRef = FileReference.of("celJS/blabla.js").build();
    expect(
        resUrlSrv.createFileUri(eq(fileNotFoundRef), eq(Optional.empty()), eq(Optional.empty())))
            .andThrow(new FileNotExistException(fileNotFoundRef)).atLeastOnce();
    FileReference fileRef = FileReference.of(":celJS/prototype.js").build();
    UriBuilder expectedFileUri = UriBuilder.fromPath("/skin/resources/celJS/prototype.js")
        .queryParam("version", "20110401120000");
    expect(resUrlSrv.createFileUri(eq(fileRef), eq(Optional.empty()), eq(Optional.empty())))
        .andReturn(expectedFileUri).anyTimes();
    String versionStr = "version=20110401120000";
    FileReference file2Ref = FileReference
        .of("/skin/resources/celJS/prototype.js?" + versionStr).build();
    UriBuilder expectedFile2Uri = UriBuilder.fromPath("/file/resources/celJS/prototype.js")
        .replaceQuery(versionStr);
    expect(resUrlSrv.createFileUri(eq(file2Ref), eq(Optional.of("file")),
        eq(Optional.of(versionStr)))).andReturn(expectedFile2Uri).atLeastOnce();
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
        .setJsFileRef(fileRef)
        .setAction("file")
        .build()));
    Builder paramBuilder = new ExtJsFileParameter.Builder();
    assertEquals("", command.addExtJSfileOnce(paramBuilder
        .setJsFileRef(fileRef)
        .build()));
    assertEquals("", command.addExtJSfileOnce(paramBuilder
        .setJsFileRef(fileNotFoundRef)
        .build()));
    String allStr = command.getAllExternalJavaScriptFiles();
    assertEquals("<script type=\"text/javascript\""
        + " src=\"/file/celJS/prototype.js?version=20110401120000\"></script>\n"
        + "<!-- WARNING: js-file not found: " + fileNotFoundRef.getFullPath() + " -->\n", allStr);
    verifyDefault();
  }

}
