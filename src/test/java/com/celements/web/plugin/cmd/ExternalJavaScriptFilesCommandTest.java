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

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.javascript.ExtJsFileParameter;
import com.celements.javascript.ExtJsFileParameter.Builder;
import com.celements.javascript.JavaScriptExternalFilesClass;
import com.celements.javascript.JsLoadMode;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.pagelayout.LayoutServiceRole;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.ressource_url.RessourceUrlServiceRole;
import com.celements.ressource_url.UrlRessourceNotExistException;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class ExternalJavaScriptFilesCommandTest extends AbstractComponentTest {

  private ExternalJavaScriptFilesCommand command = null;
  private XWikiContext context = null;
  private IPageTypeResolverRole pageTypeResolverMock;
  private IModelAccessFacade modelAccessMock;
  private LayoutServiceRole pageLayoutCmdMock;
  private RessourceUrlServiceRole resUrlSrv;

  @Before
  public void setUp_ExternalJavaScriptFilesCommandTest() throws Exception {
    context = getContext();
    context.put("vcontext", new VelocityContext());
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    pageTypeResolverMock = registerComponentMock(IPageTypeResolverRole.class);
    pageLayoutCmdMock = registerComponentMock(LayoutServiceRole.class);
    resUrlSrv = registerComponentMock(RessourceUrlServiceRole.class);
    command = new ExternalJavaScriptFilesCommand();
  }

  @Test
  public void testAddExtJSfileOnce_beforeGetAll() throws Exception {
    String file = ":celJS/prototype.js";
    expect(resUrlSrv.createRessourceUrl(eq(file), eq(Optional.empty()), eq(Optional.empty())))
        .andReturn(file).once();
    expect(resUrlSrv.isAttachmentLink(eq(file))).andReturn(false).atLeastOnce();
    expect(resUrlSrv.isOnDiskLink(eq(file))).andReturn(true).atLeastOnce();
    replayDefault();
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFile(file)
        .build()));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_beforeGetAll_fileNotFound() throws Exception {
    String fileNotFound = "Content.WebHome;blabla.js";
    expect(
        resUrlSrv.createRessourceUrl(eq(fileNotFound), eq(Optional.empty()), eq(Optional.empty())))
            .andThrow(new UrlRessourceNotExistException(fileNotFound)).once();
    expect(resUrlSrv.isAttachmentLink(eq(fileNotFound))).andReturn(true).atLeastOnce();
    expect(resUrlSrv.isOnDiskLink(eq(fileNotFound))).andReturn(false).anyTimes();
    replayDefault();
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFile(fileNotFound)
        .build()));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll() throws Exception {
    String file = "/skin/resources/celJS/prototype.js";
    expect(resUrlSrv.createRessourceUrl(eq(file), eq(Optional.empty()), eq(Optional.empty())))
        .andReturn(file).atLeastOnce();
    expect(resUrlSrv.isAttachmentLink(eq(file))).andReturn(false).atLeastOnce();
    expect(resUrlSrv.isOnDiskLink(eq(file))).andReturn(false).atLeastOnce();
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
  public void testAddExtJSfileOnce_afterGetAll_action() throws Exception {
    String file = "/file/resources/celJS/prototype.js";
    expect(resUrlSrv.createRessourceUrl(eq(file), eq(Optional.of("file")), eq(Optional.empty())))
        .andReturn(file).atLeastOnce();
    expect(resUrlSrv.isAttachmentLink(eq(file))).andReturn(false).atLeastOnce();
    expect(resUrlSrv.isOnDiskLink(eq(file))).andReturn(false).atLeastOnce();
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
  public void testAddExtJSfileOnce_afterGetAll_action_params() throws Exception {
    String file = "/file/resources/celJS/prototype.js";
    String params = "me=blu";
    String expectedFilePath = file + "?" + params;
    expect(resUrlSrv.createRessourceUrl(eq(file), eq(Optional.of("file")), eq(Optional.of(params))))
        .andReturn(expectedFilePath).atLeastOnce();
    expect(resUrlSrv.isAttachmentLink(eq(file))).andReturn(false).atLeastOnce();
    expect(resUrlSrv.isOnDiskLink(eq(file))).andReturn(false).atLeastOnce();
    replayDefault();
    command.injectDisplayAll(true);
    assertEquals("<script type=\"text/javascript\" src=\"" + expectedFilePath + "\"></script>",
        command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
            .setJsFile(file)
            .setAction("file")
            .setQueryString(params)
            .build()));
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFile(file)
        .setAction("file")
        .setQueryString("me=blu")
        .build()));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll_action_params_onDisk() throws Exception {
    String file = ":celJS/prototype.js";
    String fileURL = "/file/resources/celJS/prototype.js?version=201507061937";
    String params = "me=blu";
    String expectedFileURL = fileURL + "&" + params;
    expect(resUrlSrv.createRessourceUrl(eq(file), eq(Optional.of("file")), eq(Optional.of(params))))
        .andReturn(expectedFileURL).atLeastOnce();
    expect(resUrlSrv.isAttachmentLink(eq(file))).andReturn(false).atLeastOnce();
    expect(resUrlSrv.isOnDiskLink(eq(file))).andReturn(true).atLeastOnce();
    replayDefault();
    command.injectDisplayAll(true);
    final ExtJsFileParameter extJsFileParam = new ExtJsFileParameter.Builder()
        .setJsFile(file)
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
    String file = "celJS/prototype.js?version=20110401182200";
    expect(resUrlSrv.createRessourceUrl(eq(file), eq(Optional.empty()), eq(Optional.empty())))
        .andReturn(file).atLeastOnce();
    expect(resUrlSrv.isAttachmentLink(eq(file))).andReturn(false).atLeastOnce();
    expect(resUrlSrv.isOnDiskLink(eq(file))).andReturn(false).atLeastOnce();
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
  public void testAddExtJSfileOnce_afterGetAll_fileNotFound_url() throws Exception {
    String fileNotFound = "/download/Content/WebHome/blabla.js";
    expect(
        resUrlSrv.createRessourceUrl(eq(fileNotFound), eq(Optional.empty()), eq(Optional.empty())))
            .andThrow(new UrlRessourceNotExistException(fileNotFound)).atLeastOnce();
    expect(resUrlSrv.isAttachmentLink(eq(fileNotFound))).andReturn(false).atLeastOnce();
    expect(resUrlSrv.isOnDiskLink(eq(fileNotFound))).andReturn(false).atLeastOnce();
    replayDefault();
    command.injectDisplayAll(true);
    assertEquals("<!-- WARNING: js-file not found: " + fileNotFound + " -->",
        command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
            .setJsFile(fileNotFound)
            .build()));
    assertEquals("", command.addExtJSfileOnce(new ExtJsFileParameter.Builder()
        .setJsFile(fileNotFound)
        .build()));
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_afterGetAll_fileNotFound_attUrl() throws Exception {
    String fileNotFound = "Content.WebHome;blabla.js";
    expect(
        resUrlSrv.createRessourceUrl(eq(fileNotFound), eq(Optional.empty()), eq(Optional.empty())))
            .andThrow(new UrlRessourceNotExistException(fileNotFound)).once();
    expect(resUrlSrv.isAttachmentLink(eq(fileNotFound))).andReturn(true).atLeastOnce();
    expect(resUrlSrv.isOnDiskLink(eq(fileNotFound))).andReturn(false).anyTimes();
    replayDefault();
    command.injectDisplayAll(true);
    assertEquals("<!-- WARNING: js-file not found: " + fileNotFound + " -->",
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
    expect(resUrlSrv.isAttachmentLink(eq(filePath))).andReturn(false).atLeastOnce();
    expect(resUrlSrv.isOnDiskLink(eq(filePath))).andReturn(false).atLeastOnce();
    expect(resUrlSrv.createRessourceUrl(eq(filePath), eq(Optional.empty()), eq(Optional.empty())))
        .andReturn(filePath).atLeastOnce();
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
    expect(resUrlSrv.isAttachmentLink(eq(filePath))).andReturn(false).atLeastOnce();
    expect(resUrlSrv.isOnDiskLink(eq(filePath))).andReturn(false).atLeastOnce();
    expect(resUrlSrv.createRessourceUrl(eq(filePath), eq(Optional.empty()), eq(Optional.empty())))
        .andReturn(filePath).atLeastOnce();
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
    expect(resUrlSrv.isAttachmentLink(eq(filePath))).andReturn(false).atLeastOnce();
    expect(resUrlSrv.isOnDiskLink(eq(filePath))).andReturn(false).atLeastOnce();
    expect(resUrlSrv.createRessourceUrl(eq(filePath), eq(Optional.empty()), eq(Optional.empty())))
        .andReturn(filePath).atLeastOnce();
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
  public void testAddExtJSfileOnce_beforeGetAll_double() throws Exception {
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Main",
        "WebHome");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    String fileNotFound = "celJS/blabla.js";
    expect(
        resUrlSrv.createRessourceUrl(eq(fileNotFound), eq(Optional.empty()), eq(Optional.empty())))
            .andThrow(new UrlRessourceNotExistException(fileNotFound)).atLeastOnce();
    expect(resUrlSrv.isAttachmentLink(eq(fileNotFound))).andReturn(false).atLeastOnce();
    expect(resUrlSrv.isOnDiskLink(eq(fileNotFound))).andReturn(true).atLeastOnce();
    String file = "/skin/resources/celJS/prototype.js?version=20110401120000";
    expect(resUrlSrv.createRessourceUrl(eq(file), eq(Optional.empty()), eq(Optional.empty())))
        .andReturn(file).atLeastOnce();
    expect(resUrlSrv.isAttachmentLink(eq(file))).andReturn(false).atLeastOnce();
    expect(resUrlSrv.isOnDiskLink(eq(file))).andReturn(false).atLeastOnce();
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
        + "<!-- WARNING: js-file not found: " + fileNotFound + " -->\n", allStr);
    verifyDefault();
  }

  @Test
  public void testAddExtJSfileOnce_beforeGetAll_explicitAndImplicit_double() throws Exception {
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Main",
        "WebHome");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    String fileNotFound = ":celJS/blabla.js";
    expect(
        resUrlSrv.createRessourceUrl(eq(fileNotFound), eq(Optional.empty()), eq(Optional.empty())))
            .andThrow(new UrlRessourceNotExistException(fileNotFound)).atLeastOnce();
    expect(resUrlSrv.isAttachmentLink(eq(fileNotFound))).andReturn(false).atLeastOnce();
    expect(resUrlSrv.isOnDiskLink(eq(fileNotFound))).andReturn(true).atLeastOnce();
    String attFileURL = ":celJS/prototype.js";
    String file = "/skin/celJS/prototype.js?version=20110401120000";
    expect(resUrlSrv.createRessourceUrl(eq(attFileURL), eq(Optional.empty()), eq(Optional.empty())))
        .andReturn(file).anyTimes();
    expect(resUrlSrv.isAttachmentLink(eq(attFileURL))).andReturn(false).atLeastOnce();
    expect(resUrlSrv.isOnDiskLink(eq(attFileURL))).andReturn(true).atLeastOnce();
    String file2 = "/file/celJS/prototype.js?version=20110401120000";
    expect(resUrlSrv.createRessourceUrl(eq(attFileURL), eq(Optional.of("file")),
        eq(Optional.empty()))).andReturn(file2).atLeastOnce();
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
        + "<!-- WARNING: js-file not found: " + fileNotFound + " -->\n", allStr);
    verifyDefault();
  }

}
