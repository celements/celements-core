package com.celements.appScript;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.emptycheck.service.IEmptyCheckRole;
import com.celements.model.access.IModelAccessFacade;
import com.celements.web.service.UrlService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;

public class AppScriptServiceTest extends AbstractComponentTest {

  private XWikiContext context;
  private XWiki xwiki;
  private AppScriptService appScriptService;
  private IEmptyCheckRole emptyCheckMock;
  private UrlService urlServiceMock;
  private IModelAccessFacade modelAccessMock;

  @Before
  public void setUp_AppScriptServiceTest() throws Exception {
    context = getXContext();
    xwiki = getMock(XWiki.class);
    urlServiceMock = registerComponentMock(UrlService.class);
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    emptyCheckMock = registerComponentMock(IEmptyCheckRole.class);
    appScriptService = (AppScriptService) Utils.getComponent(IAppScriptService.class);
  }

  @Test
  public void test_getStartIndex_Space_Action() {
    String myAppScriptPath = "/app/myAppScript";
    getConfigurationSource().setProperty(IAppScriptService.APP_SCRIPT_ACTION_NAME_CONF_PROPERTY,
        IAppScriptService.APP_SCRIPT_XPAGE);
    replayDefault();
    int startIndex = appScriptService.getStartIndex(myAppScriptPath);
    assertEquals("myAppScript", myAppScriptPath.substring(startIndex));
    verifyDefault();
  }

  @Test
  public void test_isAppScriptRequest_appXpage() {
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Content",
        "noScript");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    expect(mockRequest.getParameter(eq("xpage"))).andReturn(
        IAppScriptService.APP_SCRIPT_XPAGE).anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("myScript").anyTimes();
    expect(xwiki.getXWikiPreference(eq(IAppScriptService.APP_SCRIPT_XWPREF_OVERW_DOCS), eq(
        IAppScriptService.APP_SCRIPT_CONF_OVERW_DOCS), eq("-"), same(context))).andReturn(
            "Content.login").anyTimes();
    replayDefault(mockRequest);
    assertTrue(appScriptService.isAppScriptRequest());
    verifyDefault(mockRequest);
  }

  @Test
  public void test_isAppScriptRequest_appAction() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    context.put("appAction", true);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    replayDefault(mockRequest);
    assertTrue(appScriptService.isAppScriptRequest());
    verifyDefault(mockRequest);
  }

  @Test
  public void test_isAppScriptRequest_view_appSpace() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(),
        IAppScriptService.APP_SCRIPT_XPAGE, "myScript");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    replayDefault(mockRequest);
    assertTrue(appScriptService.isAppScriptRequest());
    verifyDefault(mockRequest);
  }

  @Test
  public void test_isAppScriptRequest_view_overwriteAppDoc_comma() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Content",
        "login");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq(IAppScriptService.APP_SCRIPT_XWPREF_OVERW_DOCS), eq(
        IAppScriptService.APP_SCRIPT_CONF_OVERW_DOCS), eq("-"), same(context))).andReturn(
            "Content.login,Content.WhatsNew").anyTimes();
    replayDefault(mockRequest);
    assertTrue(appScriptService.isAppScriptRequest());
    verifyDefault(mockRequest);
  }

  @Test
  public void test_isAppScriptRequest_view_overwriteAppDoc_space() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Content",
        "login");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq(IAppScriptService.APP_SCRIPT_XWPREF_OVERW_DOCS), eq(
        IAppScriptService.APP_SCRIPT_CONF_OVERW_DOCS), eq("-"), same(context))).andReturn(
            "Content.WhatsNew Content.login").anyTimes();
    replayDefault(mockRequest);
    assertTrue(appScriptService.isAppScriptRequest());
    verifyDefault(mockRequest);
  }

  @Test
  public void test_isAppScriptRequest_view_overwriteAppDoc_noConfig() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Content",
        "login");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("appScriptOverwriteDocs"), eq(
        "com.celements.appScript.overwriteDocs"), eq("-"), same(context))).andReturn(
            "-").anyTimes();
    replayDefault(mockRequest);
    assertFalse(appScriptService.isAppScriptRequest());
    verifyDefault(mockRequest);
  }

  @Test
  public void test_isAppScriptRequest_view_noAppSpace() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Content",
        "noScript");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("appScriptOverwriteDocs"), eq(
        "com.celements.appScript.overwriteDocs"), eq("-"), same(context))).andReturn(
            "-").anyTimes();
    replayDefault(mockRequest);
    assertFalse(appScriptService.isAppScriptRequest());
    verifyDefault(mockRequest);
  }

  @Test
  public void test_getAppScriptNameFromRequestURL_appXpage() {
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Content",
        "noScript");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    expect(mockRequest.getParameter(eq("xpage"))).andReturn(
        IAppScriptService.APP_SCRIPT_XPAGE).anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("myScript").anyTimes();
    expect(xwiki.getXWikiPreference(eq(IAppScriptService.APP_SCRIPT_XWPREF_OVERW_DOCS), eq(
        IAppScriptService.APP_SCRIPT_CONF_OVERW_DOCS), eq("-"), same(context))).andReturn(
            "Content.login").anyTimes();
    replayDefault(mockRequest);
    assertEquals("myScript", appScriptService.getAppScriptNameFromRequestURL());
    verifyDefault(mockRequest);
  }

  @Test
  public void test_getAppScriptNameFromRequestURL_appAction() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    context.put("appAction", true);
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "pathTo",
        "myScript");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    expect(mockRequest.getPathInfo()).andReturn("/" + IAppScriptService.APP_SCRIPT_XPAGE
        + "/pathTo/myScript").anyTimes();
    getConfigurationSource().setProperty(IAppScriptService.APP_SCRIPT_ACTION_NAME_CONF_PROPERTY,
        IAppScriptService.APP_SCRIPT_XPAGE);
    replayDefault(mockRequest);
    assertEquals("pathTo/myScript", appScriptService.getAppScriptNameFromRequestURL());
    verifyDefault(mockRequest);
  }

  @Test
  public void test_getAppScriptNameFromRequestURL_appAction2() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    context.put("appAction", true);
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "pathTo",
        "pathTo2");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    expect(mockRequest.getPathInfo()).andReturn("/" + IAppScriptService.APP_SCRIPT_XPAGE
        + "/pathTo/pathTo2/myScript").anyTimes();
    getConfigurationSource().setProperty(IAppScriptService.APP_SCRIPT_ACTION_NAME_CONF_PROPERTY,
        IAppScriptService.APP_SCRIPT_XPAGE);
    replayDefault(mockRequest);
    assertEquals("pathTo/pathTo2/myScript", appScriptService.getAppScriptNameFromRequestURL());
    verifyDefault(mockRequest);
  }

  @Test
  public void test_getAppScriptNameFromRequestURL_view_appSpace() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(),
        IAppScriptService.APP_SCRIPT_XPAGE, "myScript");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    expect(mockRequest.getPathInfo()).andReturn("/" + IAppScriptService.APP_SCRIPT_XPAGE
        + "/myScript").anyTimes();
    getConfigurationSource().setProperty(IAppScriptService.APP_SCRIPT_ACTION_NAME_CONF_PROPERTY,
        IAppScriptService.APP_SCRIPT_XPAGE);
    replayDefault(mockRequest);
    assertEquals("myScript", appScriptService.getAppScriptNameFromRequestURL());
    verifyDefault(mockRequest);
  }

  @Test
  public void test_getAppScriptNameFromRequestURL_view_noAppSpace() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Content",
        "noScript");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    getConfigurationSource().setProperty(IAppScriptService.APP_SCRIPT_ACTION_NAME_CONF_PROPERTY,
        IAppScriptService.APP_SCRIPT_XPAGE);
    expect(xwiki.getXWikiPreference(eq(IAppScriptService.APP_SCRIPT_XWPREF_OVERW_DOCS), eq(
        IAppScriptService.APP_SCRIPT_CONF_OVERW_DOCS), eq("-"), same(context))).andReturn(
            "Content.login").anyTimes();
    replayDefault(mockRequest);
    assertEquals("", appScriptService.getAppScriptNameFromRequestURL());
    verifyDefault(mockRequest);
  }

  @Test
  public void test_getAppScriptNameFromRequestURL_view_overwriteAppDoc() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Content",
        "login");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    expect(mockRequest.getPathInfo()).andReturn("/login").anyTimes();
    getConfigurationSource().setProperty(IAppScriptService.APP_SCRIPT_ACTION_NAME_CONF_PROPERTY,
        IAppScriptService.APP_SCRIPT_XPAGE);
    expect(xwiki.getXWikiPreference(eq(IAppScriptService.APP_SCRIPT_XWPREF_OVERW_DOCS), eq(
        IAppScriptService.APP_SCRIPT_CONF_OVERW_DOCS), eq("-"), same(context))).andReturn(
            "Content.WhatsNew Content.login").anyTimes();
    replayDefault(mockRequest);
    assertEquals("login", appScriptService.getAppScriptNameFromRequestURL());
    verifyDefault(mockRequest);
  }

  @Test
  public void test_hasDocAppScript_appAction() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    context.put("appAction", true);
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "sub",
        "testScript");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    getConfigurationSource().setProperty(IAppScriptService.APP_SCRIPT_ACTION_NAME_CONF_PROPERTY,
        IAppScriptService.APP_SCRIPT_XPAGE);
    DocumentReference appScriptDocRef = new DocumentReference(context.getDatabase(),
        IAppScriptService.APP_SCRIPT_SPACE_NAME, "sub/testScript");
    expect(modelAccessMock.exists(eq(appScriptDocRef))).andReturn(false);
    DocumentReference centralAppScriptDocRef = new DocumentReference("celements2web",
        IAppScriptService.APP_SCRIPT_SPACE_NAME, "sub/testScript");
    expect(modelAccessMock.exists(eq(centralAppScriptDocRef))).andReturn(false);
    expect(emptyCheckMock.isEmptyRTEDocument(eq(appScriptDocRef))).andReturn(false).anyTimes();
    expect(emptyCheckMock.isEmptyRTEDocument(eq(centralAppScriptDocRef))).andReturn(
        false).anyTimes();
    replayDefault(mockRequest);
    assertFalse(appScriptService.hasDocAppScript("sub/testScript"));
    verifyDefault(mockRequest);
  }

  @Test
  public void test_hasDocAppScript_appAction_emptyString() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    context.put("appAction", true);
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "sub",
        "testScript");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    replayDefault(mockRequest);
    assertFalse(appScriptService.hasDocAppScript(""));
    verifyDefault(mockRequest);
  }

  @Test
  public void test_getAppScriptDocRef_localOverwritesCentral() throws Exception {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    context.put("appAction", true);
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Content",
        "testScript");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    DocumentReference appScriptDocRef = new DocumentReference(context.getDatabase(),
        IAppScriptService.APP_SCRIPT_SPACE_NAME, "testScript");
    expect(modelAccessMock.exists(eq(appScriptDocRef))).andReturn(true).anyTimes();
    DocumentReference centralAppScriptDocRef = new DocumentReference("celements2web",
        IAppScriptService.APP_SCRIPT_SPACE_NAME, "testScript");
    expect(modelAccessMock.exists(eq(centralAppScriptDocRef))).andReturn(false)
        .anyTimes();
    XWikiDocument appScriptDoc = new XWikiDocument(appScriptDocRef);
    appScriptDoc.setContent("this is no empty script!");
    expect(xwiki.getDocument(eq(appScriptDocRef), same(context))).andReturn(
        appScriptDoc).anyTimes();
    expect(xwiki.getXWikiPreference(eq(IEmptyCheckRole.EMPTYCHECK_MODULS_PREF_NAME), eq(
        "celements.emptycheckModuls"), eq("default"), same(context))).andReturn(
            "default").anyTimes();
    expect(emptyCheckMock.isEmptyRTEDocument(eq(appScriptDocRef))).andReturn(false).anyTimes();
    replayDefault(mockRequest);
    DocumentReference expectedAppDocRef = new DocumentReference(context.getDatabase(),
        IAppScriptService.APP_SCRIPT_SPACE_NAME, "testScript");
    assertEquals(expectedAppDocRef, appScriptService.getAppScriptDocRef("testScript"));
    verifyDefault(mockRequest);
  }

  @Test
  public void test_getAppScriptDocRef_noLocalButCentral() throws Exception {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    context.put("appAction", true);
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Content",
        "testScript");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    DocumentReference appScriptDocRef = new DocumentReference(context.getDatabase(),
        IAppScriptService.APP_SCRIPT_SPACE_NAME, "testScript");
    expect(modelAccessMock.exists(eq(appScriptDocRef))).andReturn(false).anyTimes();
    DocumentReference centralAppScriptDocRef = new DocumentReference("celements2web",
        IAppScriptService.APP_SCRIPT_SPACE_NAME, "testScript");
    expect(modelAccessMock.exists(eq(centralAppScriptDocRef))).andReturn(true)
        .anyTimes();
    XWikiDocument appScriptDoc = new XWikiDocument(appScriptDocRef);
    appScriptDoc.setContent("this is no empty script!");
    expect(xwiki.getDocument(eq(centralAppScriptDocRef), same(context))).andReturn(
        appScriptDoc).anyTimes();
    expect(emptyCheckMock.isEmptyRTEDocument(eq(appScriptDocRef))).andReturn(false).anyTimes();
    expect(emptyCheckMock.isEmptyRTEDocument(eq(centralAppScriptDocRef))).andReturn(
        true).anyTimes();
    replayDefault(mockRequest);
    DocumentReference expectedAppDocRef = new DocumentReference("celements2web",
        IAppScriptService.APP_SCRIPT_SPACE_NAME, "testScript");
    assertEquals(expectedAppDocRef, appScriptService.getAppScriptDocRef("testScript"));
    verifyDefault(mockRequest);
  }

  @Test
  public void test_getScriptNameFromURL_emptyPathInfo() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Main",
        "WebHome");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq(IAppScriptService.APP_SCRIPT_XWPREF_OVERW_DOCS), eq(
        IAppScriptService.APP_SCRIPT_CONF_OVERW_DOCS), eq("-"), same(context))).andReturn(
            "Main.WebHome").anyTimes();
    expect(mockRequest.getPathInfo()).andReturn("").anyTimes();
    replayDefault(mockRequest);
    assertEquals("WebHome", appScriptService.getScriptNameFromURL());
    verifyDefault(mockRequest);
  }

  @Test
  public void test_getScriptNameFromURL_explicit_DefaultSpace() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Main", "Test");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq(IAppScriptService.APP_SCRIPT_XWPREF_OVERW_DOCS), eq(
        IAppScriptService.APP_SCRIPT_CONF_OVERW_DOCS), eq("-"), same(context))).andReturn(
            "Main.Test").anyTimes();
    expect(mockRequest.getPathInfo()).andReturn("/Main/Test").anyTimes();
    replayDefault(mockRequest);
    assertEquals("Test", appScriptService.getScriptNameFromURL());
    verifyDefault(mockRequest);
  }

  @Test
  public void test_getScriptNameFromURL_explicit_DefaultPage() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Test",
        "WebHome");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq(IAppScriptService.APP_SCRIPT_XWPREF_OVERW_DOCS), eq(
        IAppScriptService.APP_SCRIPT_CONF_OVERW_DOCS), eq("-"), same(context))).andReturn(
            "Test.WebHome").anyTimes();
    expect(mockRequest.getPathInfo()).andReturn("/Test/WebHome").anyTimes();
    replayDefault(mockRequest);
    assertEquals("Test/WebHome", appScriptService.getScriptNameFromURL());
    verifyDefault(mockRequest);
  }

  @Test
  public void test_getScriptNameFromURL_implicit_DefaultPage() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Test",
        "WebHome");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq(IAppScriptService.APP_SCRIPT_XWPREF_OVERW_DOCS), eq(
        IAppScriptService.APP_SCRIPT_CONF_OVERW_DOCS), eq("-"), same(context))).andReturn(
            "Test.WebHome").anyTimes();
    expect(mockRequest.getPathInfo()).andReturn("/Test/").anyTimes();
    replayDefault(mockRequest);
    assertEquals("Test/WebHome", appScriptService.getScriptNameFromURL());
    verifyDefault(mockRequest);
  }

  @Test
  public void test_getScriptNameFromURL_explicit_DefaultSpace_DefaultPage() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Main",
        "WebHome");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq(IAppScriptService.APP_SCRIPT_XWPREF_OVERW_DOCS), eq(
        IAppScriptService.APP_SCRIPT_CONF_OVERW_DOCS), eq("-"), same(context))).andReturn(
            "Main.WebHome").anyTimes();
    expect(mockRequest.getPathInfo()).andReturn("/Main/WebHome").anyTimes();
    replayDefault(mockRequest);
    assertEquals("WebHome", appScriptService.getScriptNameFromURL());
    verifyDefault(mockRequest);
  }

  @Test
  public void test_getScriptNameFromURL_export_action() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("export");
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "TestSpace",
        "TestScript");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq(IAppScriptService.APP_SCRIPT_XWPREF_OVERW_DOCS), eq(
        IAppScriptService.APP_SCRIPT_CONF_OVERW_DOCS), eq("-"), same(context))).andReturn(
            "TestSpace.TestScript").anyTimes();
    expect(mockRequest.getPathInfo()).andReturn("/export/TestSpace/TestScript").anyTimes();
    replayDefault(mockRequest);
    assertEquals("TestSpace/TestScript", appScriptService.getScriptNameFromURL());
    verifyDefault(mockRequest);
  }

  @Test
  public void test_getScriptNameFromDocRef_defaultSpace() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Main", "WebHome");
    replayDefault();
    assertEquals("WebHome", appScriptService.getScriptNameFromDocRef(docRef));
    verifyDefault();
  }

  @Test
  public void test_getScriptNameFromDocRef_anySpace() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "ScriptSpace",
        "TestScript");
    replayDefault();
    assertEquals("ScriptSpace/TestScript", appScriptService.getScriptNameFromDocRef(docRef));
    verifyDefault();
  }

  @Test
  public void test_getAppScriptURL_queryString_queryStringEmpty() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "someSpace", "someDoc");
    String scriptName = "someSpace/someDoc";

    expect(urlServiceMock.getURL(eq(docRef), eq("view"), eq("xpage=app&s=someSpace/someDoc")))
        .andReturn("theURL").once();

    replayDefault();
    assertEquals("theURL", appScriptService.getAppScriptURL(scriptName, null));
    verifyDefault();
  }

  @Test
  public void test_getAppScriptURL() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "someSpace", "someDoc");
    String scriptName = "someSpace/someDoc";
    String queryString = "string=a{sd]f&string2=a[sd}f";

    expect(urlServiceMock.getURL(eq(docRef), eq("view"),
        eq("xpage=app&s=someSpace/someDoc&" + queryString))).andReturn("theURL").once();

    replayDefault();
    assertEquals("theURL", appScriptService.getAppScriptURL(scriptName, queryString));
    verifyDefault();
  }

  @Test
  public void test_getAppScriptURL_queryStringWithAmp() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "someSpace", "someDoc");
    String scriptName = "someSpace/someDoc";
    String queryString = "&string=a{sd]f&string2=a[sd}f";

    expect(urlServiceMock.getURL(eq(docRef), eq("view"),
        eq("xpage=app&s=someSpace/someDoc&" + queryString))).andReturn("theURL").once();

    replayDefault();
    assertEquals("theURL", appScriptService.getAppScriptURL(scriptName, queryString));
    verifyDefault();
  }

  @Test
  public void test_getAppScriptURL_longName() {
    String scriptName = "path/to/my/appscript";
    String queryString = "string=a{sd]f&string2=a[sd}f";

    replayDefault();
    String url = appScriptService.getAppScriptURL(scriptName, queryString);
    assertEquals("/app/" + scriptName + "?xpage=app&s=" + scriptName
        + "&string=a%7Bsd%5Df&string2=a%5Bsd%7Df", url);
    verifyDefault();
  }

  @Test
  public void test_isAppScriptAvailable_exists() throws Exception {
    String scriptName = "path/to/my/appscript";
    String scriptNamePath = "/templates/celAppScripts/" + scriptName + ".vm";
    expect(xwiki.getResourceContentAsBytes(eq(scriptNamePath))).andReturn(new byte[0])
        .atLeastOnce();
    replayDefault();
    assertTrue(appScriptService.isAppScriptAvailable(scriptName));
    verifyDefault();
  }

  @Test
  public void test_isAppScriptAvailable_notExists() throws Exception {
    String scriptName = "path/to/my/appscript";
    String scriptNamePath = "/templates/celAppScripts/" + scriptName + ".vm";
    expect(xwiki.getResourceContentAsBytes(eq(scriptNamePath))).andThrow(new IOException())
        .atLeastOnce();
    replayDefault();
    assertFalse(appScriptService.isAppScriptAvailable(scriptName));
    verifyDefault();
  }

  @Test
  public void test_getAppRecursiveScript_exists() throws Exception {
    String scriptName = "path/to/my/appscript";
    String scriptNamePathBase = "/templates/celAppScripts/";
    String expectedScriptName = "path/to++";
    expect(xwiki.getResourceContentAsBytes(eq(scriptNamePathBase + expectedScriptName + ".vm")))
        .andReturn(new byte[0])
        .atLeastOnce();
    expect(xwiki.getResourceContentAsBytes(eq(scriptNamePathBase + "path/to/my++.vm")))
        .andThrow(new IOException())
        .atLeastOnce();
    replayDefault();
    assertEquals(expectedScriptName, appScriptService.getAppRecursiveScript(scriptName));
    verifyDefault();
  }

  @Test
  public void test_getAppRecursiveScript_notExists() throws Exception {
    String scriptName = "path/to/my/appscript";
    String scriptNamePathBase = "/templates/celAppScripts/";
    expect(xwiki.getResourceContentAsBytes(eq(scriptNamePathBase + "path/to/my++.vm")))
        .andThrow(new IOException())
        .atLeastOnce();
    expect(xwiki.getResourceContentAsBytes(eq(scriptNamePathBase + "path/to++.vm")))
        .andThrow(new IOException())
        .atLeastOnce();
    expect(xwiki.getResourceContentAsBytes(eq(scriptNamePathBase + "path++.vm")))
        .andThrow(new IOException())
        .atLeastOnce();
    replayDefault();
    assertNull(appScriptService.getAppRecursiveScript(scriptName));
    verifyDefault();
  }

}
