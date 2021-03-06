package com.celements.appScript;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.emptycheck.service.IEmptyCheckRole;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;

public class AppScriptServiceTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private AppScriptService appScriptService;
  private IEmptyCheckRole preseveEmptyCheck;
  private IEmptyCheckRole emptyCheckMock;

  @Before
  public void setUp_AppScriptServiceTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    appScriptService = (AppScriptService) Utils.getComponent(IAppScriptService.class);
    preseveEmptyCheck = appScriptService.emptyCheck;
    emptyCheckMock = createMockAndAddToDefault(IEmptyCheckRole.class);
    appScriptService.emptyCheck = emptyCheckMock;
  }

  @After
  public void tearDown_AppScriptServiceTest() {
    appScriptService.emptyCheck = preseveEmptyCheck;
  }

  @Test
  public void testGetStartIndex_Space_Action() {
    String myAppScriptPath = "/app/myAppScript";
    expect(xwiki.Param(eq(IAppScriptService.APP_SCRIPT_ACTION_NAME_CONF_PROPERTY), eq(
        IAppScriptService.APP_SCRIPT_XPAGE))).andReturn(
            IAppScriptService.APP_SCRIPT_XPAGE).atLeastOnce();
    replayDefault();
    int startIndex = appScriptService.getStartIndex(myAppScriptPath);
    assertEquals("myAppScript", myAppScriptPath.substring(startIndex));
    verifyDefault();
  }

  @Test
  public void testIsAppScriptRequest_appXpage() {
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
    replayAll(mockRequest);
    assertTrue(appScriptService.isAppScriptRequest());
    verifyAll(mockRequest);
  }

  @Test
  public void testIsAppScriptRequest_appAction() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    context.put("appAction", true);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    replayAll(mockRequest);
    assertTrue(appScriptService.isAppScriptRequest());
    verifyAll(mockRequest);
  }

  @Test
  public void testIsAppScriptRequest_view_appSpace() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(),
        IAppScriptService.APP_SCRIPT_XPAGE, "myScript");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    replayAll(mockRequest);
    assertTrue(appScriptService.isAppScriptRequest());
    verifyAll(mockRequest);
  }

  @Test
  public void testIsAppScriptRequest_view_overwriteAppDoc_comma() {
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
    replayAll(mockRequest);
    assertTrue(appScriptService.isAppScriptRequest());
    verifyAll(mockRequest);
  }

  @Test
  public void testIsAppScriptRequest_view_overwriteAppDoc_space() {
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
    replayAll(mockRequest);
    assertTrue(appScriptService.isAppScriptRequest());
    verifyAll(mockRequest);
  }

  @Test
  public void testIsAppScriptRequest_view_overwriteAppDoc_noConfig() {
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
    replayAll(mockRequest);
    assertFalse(appScriptService.isAppScriptRequest());
    verifyAll(mockRequest);
  }

  @Test
  public void testIsAppScriptRequest_view_noAppSpace() {
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
    replayAll(mockRequest);
    assertFalse(appScriptService.isAppScriptRequest());
    verifyAll(mockRequest);
  }

  @Test
  public void testGetAppScriptNameFromRequestURL_appXpage() {
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
    replayAll(mockRequest);
    assertEquals("myScript", appScriptService.getAppScriptNameFromRequestURL());
    verifyAll(mockRequest);
  }

  @Test
  public void testGetAppScriptNameFromRequestURL_appAction() {
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
    expect(xwiki.Param(eq(IAppScriptService.APP_SCRIPT_ACTION_NAME_CONF_PROPERTY), eq(
        "app"))).andReturn("app").anyTimes();
    replayAll(mockRequest);
    assertEquals("pathTo/myScript", appScriptService.getAppScriptNameFromRequestURL());
    verifyAll(mockRequest);
  }

  @Test
  public void testGetAppScriptNameFromRequestURL_appAction2() {
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
    expect(xwiki.Param(eq(IAppScriptService.APP_SCRIPT_ACTION_NAME_CONF_PROPERTY), eq(
        "app"))).andReturn("app").anyTimes();
    replayAll(mockRequest);
    assertEquals("pathTo/pathTo2/myScript", appScriptService.getAppScriptNameFromRequestURL());
    verifyAll(mockRequest);
  }

  @Test
  public void testGetAppScriptNameFromRequestURL_view_appSpace() {
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
    expect(xwiki.Param(eq(IAppScriptService.APP_SCRIPT_ACTION_NAME_CONF_PROPERTY), eq(
        "app"))).andReturn("app").anyTimes();
    replayAll(mockRequest);
    assertEquals("myScript", appScriptService.getAppScriptNameFromRequestURL());
    verifyAll(mockRequest);
  }

  @Test
  public void testGetAppScriptNameFromRequestURL_view_noAppSpace() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(), "Content",
        "noScript");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    expect(xwiki.Param(eq(IAppScriptService.APP_SCRIPT_ACTION_NAME_CONF_PROPERTY), eq(
        "app"))).andReturn("app").anyTimes();
    expect(xwiki.getXWikiPreference(eq(IAppScriptService.APP_SCRIPT_XWPREF_OVERW_DOCS), eq(
        IAppScriptService.APP_SCRIPT_CONF_OVERW_DOCS), eq("-"), same(context))).andReturn(
            "Content.login").anyTimes();
    replayAll(mockRequest);
    assertEquals("", appScriptService.getAppScriptNameFromRequestURL());
    verifyAll(mockRequest);
  }

  @Test
  public void testGetAppScriptNameFromRequestURL_view_overwriteAppDoc() {
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
    expect(xwiki.Param(eq(IAppScriptService.APP_SCRIPT_ACTION_NAME_CONF_PROPERTY), eq(
        "app"))).andReturn("app").anyTimes();
    expect(xwiki.getXWikiPreference(eq(IAppScriptService.APP_SCRIPT_XWPREF_OVERW_DOCS), eq(
        IAppScriptService.APP_SCRIPT_CONF_OVERW_DOCS), eq("-"), same(context))).andReturn(
            "Content.WhatsNew Content.login").anyTimes();
    replayAll(mockRequest);
    assertEquals("login", appScriptService.getAppScriptNameFromRequestURL());
    verifyAll(mockRequest);
  }

  @Test
  public void testHasDocAppScript_appAction() {
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
    expect(xwiki.Param(eq(IAppScriptService.APP_SCRIPT_ACTION_NAME_CONF_PROPERTY), eq(
        "app"))).andReturn("app").anyTimes();
    DocumentReference appScriptDocRef = new DocumentReference(context.getDatabase(),
        IAppScriptService.APP_SCRIPT_SPACE_NAME, "sub/testScript");
    expect(xwiki.exists(eq(appScriptDocRef), same(context))).andReturn(false);
    DocumentReference centralAppScriptDocRef = new DocumentReference("celements2web",
        IAppScriptService.APP_SCRIPT_SPACE_NAME, "sub/testScript");
    expect(xwiki.exists(eq(centralAppScriptDocRef), same(context))).andReturn(false);
    expect(emptyCheckMock.isEmptyRTEDocument(eq(appScriptDocRef))).andReturn(false).anyTimes();
    expect(emptyCheckMock.isEmptyRTEDocument(eq(centralAppScriptDocRef))).andReturn(
        false).anyTimes();
    replayAll(mockRequest);
    assertFalse(appScriptService.hasDocAppScript("sub/testScript"));
    verifyAll(mockRequest);
  }

  @Test
  public void testHasDocAppScript_appAction_emptyString() {
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
    replayAll(mockRequest);
    assertFalse(appScriptService.hasDocAppScript(""));
    verifyAll(mockRequest);
  }

  @Test
  public void testGetAppScriptDocRef_localOverwritesCentral() throws Exception {
    appScriptService.emptyCheck = preseveEmptyCheck;
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
    expect(xwiki.exists(eq(appScriptDocRef), same(context))).andReturn(true).anyTimes();
    DocumentReference centralAppScriptDocRef = new DocumentReference("celements2web",
        IAppScriptService.APP_SCRIPT_SPACE_NAME, "testScript");
    expect(xwiki.exists(eq(centralAppScriptDocRef), same(context))).andReturn(false).anyTimes();
    XWikiDocument appScriptDoc = new XWikiDocument(appScriptDocRef);
    appScriptDoc.setContent("this is no empty script!");
    expect(xwiki.getDocument(eq(appScriptDocRef), same(context))).andReturn(
        appScriptDoc).anyTimes();
    expect(xwiki.getXWikiPreference(eq(IEmptyCheckRole.EMPTYCHECK_MODULS_PREF_NAME), eq(
        "celements.emptycheckModuls"), eq("default"), same(context))).andReturn(
            "default").anyTimes();
    replayAll(mockRequest);
    DocumentReference expectedAppDocRef = new DocumentReference(context.getDatabase(),
        IAppScriptService.APP_SCRIPT_SPACE_NAME, "testScript");
    assertEquals(expectedAppDocRef, appScriptService.getAppScriptDocRef("testScript"));
    verifyAll(mockRequest);
  }

  @Test
  public void testGetAppScriptDocRef_noLocalButCentral() throws Exception {
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
    expect(xwiki.exists(eq(appScriptDocRef), same(context))).andReturn(false).anyTimes();
    DocumentReference centralAppScriptDocRef = new DocumentReference("celements2web",
        IAppScriptService.APP_SCRIPT_SPACE_NAME, "testScript");
    expect(xwiki.exists(eq(centralAppScriptDocRef), same(context))).andReturn(true).anyTimes();
    XWikiDocument appScriptDoc = new XWikiDocument(appScriptDocRef);
    appScriptDoc.setContent("this is no empty script!");
    expect(xwiki.getDocument(eq(centralAppScriptDocRef), same(context))).andReturn(
        appScriptDoc).anyTimes();
    expect(emptyCheckMock.isEmptyRTEDocument(eq(appScriptDocRef))).andReturn(false).anyTimes();
    expect(emptyCheckMock.isEmptyRTEDocument(eq(centralAppScriptDocRef))).andReturn(
        true).anyTimes();
    replayAll(mockRequest);
    DocumentReference expectedAppDocRef = new DocumentReference("celements2web",
        IAppScriptService.APP_SCRIPT_SPACE_NAME, "testScript");
    assertEquals(expectedAppDocRef, appScriptService.getAppScriptDocRef("testScript"));
    verifyAll(mockRequest);
  }

  @Test
  public void testGetScriptNameFromURL_emptyPathInfo() {
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
    replayAll(mockRequest);
    assertEquals("WebHome", appScriptService.getScriptNameFromURL());
    verifyAll(mockRequest);
  }

  @Test
  public void testGetScriptNameFromURL_explicit_DefaultSpace() {
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
    replayAll(mockRequest);
    assertEquals("Test", appScriptService.getScriptNameFromURL());
    verifyAll(mockRequest);
  }

  @Test
  public void testGetScriptNameFromURL_explicit_DefaultPage() {
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
    replayAll(mockRequest);
    assertEquals("Test/WebHome", appScriptService.getScriptNameFromURL());
    verifyAll(mockRequest);
  }

  @Test
  public void testGetScriptNameFromURL_implicit_DefaultPage() {
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
    replayAll(mockRequest);
    assertEquals("Test/WebHome", appScriptService.getScriptNameFromURL());
    verifyAll(mockRequest);
  }

  @Test
  public void testGetScriptNameFromURL_explicit_DefaultSpace_DefaultPage() {
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
    replayAll(mockRequest);
    assertEquals("WebHome", appScriptService.getScriptNameFromURL());
    verifyAll(mockRequest);
  }

  @Test
  public void testGetScriptNameFromURL_export_action() {
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
    replayAll(mockRequest);
    assertEquals("TestSpace/TestScript", appScriptService.getScriptNameFromURL());
    verifyAll(mockRequest);
  }

  @Test
  public void testGetScriptNameFromDocRef_defaultSpace() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "Main", "WebHome");
    replayAll();
    assertEquals("WebHome", appScriptService.getScriptNameFromDocRef(docRef));
    verifyAll();
  }

  @Test
  public void testGetScriptNameFromDocRef_anySpace() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "ScriptSpace",
        "TestScript");
    replayAll();
    assertEquals("ScriptSpace/TestScript", appScriptService.getScriptNameFromDocRef(docRef));
    verifyAll();
  }

  @Test
  public void testGetAppScriptURL_queryString_queryStringEmpty() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "someSpace", "someDoc");
    String scriptName = "someSpace/someDoc";

    expect(xwiki.getURL(eq(docRef), eq("view"), eq("xpage=app&s=someSpace/someDoc"),
        (String) isNull(), same(context))).andReturn("theURL").once();

    replayAll();
    assertEquals("theURL", appScriptService.getAppScriptURL(scriptName, null));
    verifyAll();
  }

  @Test
  public void testGetAppScriptURL() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "someSpace", "someDoc");
    String scriptName = "someSpace/someDoc";
    String queryString = "string=a{sd]f&string2=a[sd}f";

    expect(xwiki.getURL(eq(docRef), eq("view"), eq("xpage=app&s=someSpace/someDoc&" + queryString),
        (String) isNull(), same(context))).andReturn("theURL").once();

    replayAll();
    assertEquals("theURL", appScriptService.getAppScriptURL(scriptName, queryString));
    verifyAll();
  }

  @Test
  public void testGetAppScriptURL_queryStringWithAmp() {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "someSpace", "someDoc");
    String scriptName = "someSpace/someDoc";
    String queryString = "&string=a{sd]f&string2=a[sd}f";

    expect(xwiki.getURL(eq(docRef), eq("view"), eq("xpage=app&s=someSpace/someDoc&" + queryString),
        (String) isNull(), same(context))).andReturn("theURL").once();

    replayAll();
    assertEquals("theURL", appScriptService.getAppScriptURL(scriptName, queryString));
    verifyAll();
  }

  @Test
  public void testGetAppScriptURL_longName() {
    String scriptName = "path/to/my/appscript";
    String queryString = "string=a{sd]f&string2=a[sd}f";

    replayAll();
    String url = appScriptService.getAppScriptURL(scriptName, queryString);
    assertEquals("/app/" + scriptName + "?xpage=app&s=" + scriptName
        + "&string=a%7Bsd%5Df&string2=a%5Bsd%7Df", url);
    verifyAll();
  }

  private void replayAll(Object... mocks) {
    replayDefault(mocks);
  }

  private void verifyAll(Object... mocks) {
    verifyDefault(mocks);
  }

}
