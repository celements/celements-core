package com.celements.appScript;


import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;

public class AppScriptServiceTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private AppScriptService appScriptService;

  @Before
  public void setUp_AppScriptServiceTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    appScriptService = (AppScriptService)Utils.getComponent(IAppScriptService.class);
  }

  @Test
  public void testIsAppScriptRequest_appXpage() {
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(),
        "Content", "noScript");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    expect(mockRequest.getParameter(eq("xpage"))).andReturn(
        IAppScriptService.APP_SCRIPT_XPAGE).anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("myScript").anyTimes();
    expect(xwiki.getXWikiPreference(eq(IAppScriptService.APP_SCRIPT_XWPREF_OVERW_DOCS),
        eq(IAppScriptService.APP_SCRIPT_CONF_OVERW_DOCS), eq("-"), same(context))
        ).andReturn("Content.login").anyTimes();
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
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(),
        "Content", "login");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq(IAppScriptService.APP_SCRIPT_XWPREF_OVERW_DOCS),
        eq(IAppScriptService.APP_SCRIPT_CONF_OVERW_DOCS), eq("-"), same(context))
        ).andReturn("Content.login,Content.WhatsNew").anyTimes();
    replayAll(mockRequest);
    assertTrue(appScriptService.isAppScriptRequest());
    verifyAll(mockRequest);
  }

  @Test
  public void testIsAppScriptRequest_view_overwriteAppDoc_space() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(),
        "Content", "login");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq(IAppScriptService.APP_SCRIPT_XWPREF_OVERW_DOCS),
        eq(IAppScriptService.APP_SCRIPT_CONF_OVERW_DOCS), eq("-"), same(context))
        ).andReturn("Content.WhatsNew Content.login").anyTimes();
    replayAll(mockRequest);
    assertTrue(appScriptService.isAppScriptRequest());
    verifyAll(mockRequest);
  }

  @Test
  public void testIsAppScriptRequest_view_overwriteAppDoc_noConfig() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(),
        "Content", "login");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("appScriptOverwriteDocs"),
        eq("com.celements.appScript.overwriteDocs"), eq("-"), same(context))).andReturn(
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
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(),
        "Content", "noScript");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(xwiki.getXWikiPreference(eq("appScriptOverwriteDocs"),
        eq("com.celements.appScript.overwriteDocs"), eq("-"), same(context))).andReturn(
            "-").anyTimes();
    replayAll(mockRequest);
    assertFalse(appScriptService.isAppScriptRequest());
    verifyAll(mockRequest);
  }

  @Test
  public void testGetAppScriptNameFromRequestURL_appXpage() {
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(),
        "Content", "noScript");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    expect(mockRequest.getParameter(eq("xpage"))).andReturn(
        IAppScriptService.APP_SCRIPT_XPAGE).anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("myScript").anyTimes();
    expect(xwiki.getXWikiPreference(eq(IAppScriptService.APP_SCRIPT_XWPREF_OVERW_DOCS),
        eq(IAppScriptService.APP_SCRIPT_CONF_OVERW_DOCS), eq("-"), same(context))
        ).andReturn("Content.login").anyTimes();
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
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(),
        "pathTo", "myScript");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    expect(mockRequest.getPathInfo()).andReturn("/" +
        IAppScriptService.APP_SCRIPT_XPAGE + "/pathTo/myScript").anyTimes();
    expect(xwiki.Param(eq(IAppScriptService.APP_SCRIPT_ACTION_NAME_CONF_PROPERTY),
        eq("app"))).andReturn("app").anyTimes();
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
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(),
        "pathTo", "pathTo2");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    expect(mockRequest.getPathInfo()).andReturn("/" +
        IAppScriptService.APP_SCRIPT_XPAGE + "/pathTo/pathTo2/myScript"
        ).anyTimes();
    expect(xwiki.Param(eq(IAppScriptService.APP_SCRIPT_ACTION_NAME_CONF_PROPERTY),
        eq("app"))).andReturn("app").anyTimes();
    replayAll(mockRequest);
    assertEquals("pathTo/pathTo2/myScript",
        appScriptService.getAppScriptNameFromRequestURL());
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
    expect(mockRequest.getPathInfo()).andReturn("/" +
        IAppScriptService.APP_SCRIPT_XPAGE + "/myScript").anyTimes();
    expect(xwiki.Param(eq(IAppScriptService.APP_SCRIPT_ACTION_NAME_CONF_PROPERTY),
        eq("app"))).andReturn("app").anyTimes();
    replayAll(mockRequest);
    assertEquals("myScript", appScriptService.getAppScriptNameFromRequestURL());
    verifyAll(mockRequest);
  }

  @Test
  public void testGetAppScriptNameFromRequestURL_view_noAppSpace() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(),
        "Content", "noScript");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    expect(xwiki.Param(eq(IAppScriptService.APP_SCRIPT_ACTION_NAME_CONF_PROPERTY),
        eq("app"))).andReturn("app").anyTimes();
    expect(xwiki.getXWikiPreference(eq(IAppScriptService.APP_SCRIPT_XWPREF_OVERW_DOCS),
        eq(IAppScriptService.APP_SCRIPT_CONF_OVERW_DOCS), eq("-"), same(context))
        ).andReturn("Content.login").anyTimes();
    replayAll(mockRequest);
    assertEquals("", appScriptService.getAppScriptNameFromRequestURL());
    verifyAll(mockRequest);
  }

  @Test
  public void testGetAppScriptNameFromRequestURL_view_overwriteAppDoc() {
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(),
        "Content", "login");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    expect(mockRequest.getPathInfo()).andReturn("/login").anyTimes();
    expect(xwiki.Param(eq(IAppScriptService.APP_SCRIPT_ACTION_NAME_CONF_PROPERTY),
        eq("app"))).andReturn("app").anyTimes();
    expect(xwiki.getXWikiPreference(eq(IAppScriptService.APP_SCRIPT_XWPREF_OVERW_DOCS),
        eq(IAppScriptService.APP_SCRIPT_CONF_OVERW_DOCS), eq("-"), same(context))
        ).andReturn("Content.WhatsNew Content.login").anyTimes();
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
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(),
        "sub", "testScript");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    expect(xwiki.Param(eq(IAppScriptService.APP_SCRIPT_ACTION_NAME_CONF_PROPERTY),
        eq("app"))).andReturn("app").anyTimes();
    DocumentReference appScriptDocRef = new DocumentReference(context.getDatabase(),
        IAppScriptService.APP_SCRIPT_SPACE_NAME, "sub/testScript");
    expect(xwiki.exists(eq(appScriptDocRef), same(context))).andReturn(false);
    DocumentReference centralAppScriptDocRef = new DocumentReference("celements2web",
        IAppScriptService.APP_SCRIPT_SPACE_NAME, "sub/testScript");
    expect(xwiki.exists(eq(centralAppScriptDocRef), same(context))).andReturn(false);
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
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(),
        "sub", "testScript");
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
    XWikiRequest mockRequest = createMock(XWikiRequest.class);
    context.setRequest(mockRequest);
    context.setAction("view");
    context.put("appAction", true);
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(),
        "Content", "testScript");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    DocumentReference appScriptDocRef = new DocumentReference(context.getDatabase(),
        IAppScriptService.APP_SCRIPT_SPACE_NAME, "testScript");
    expect(xwiki.exists(eq(appScriptDocRef), same(context))).andReturn(true).anyTimes();
    DocumentReference centralAppScriptDocRef = new DocumentReference("celements2web",
        IAppScriptService.APP_SCRIPT_SPACE_NAME, "testScript");
    expect(xwiki.exists(eq(centralAppScriptDocRef), same(context))).andReturn(false
        ).anyTimes();
    XWikiDocument appScriptDoc = new XWikiDocument(appScriptDocRef);
    appScriptDoc.setContent("this is no empty script!");
    expect(xwiki.getDocument(eq(appScriptDocRef), same(context))).andReturn(appScriptDoc
        ).anyTimes();
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
    DocumentReference contextDocRef = new DocumentReference(context.getDatabase(),
        "Content", "testScript");
    XWikiDocument contextDoc = new XWikiDocument(contextDocRef);
    context.setDoc(contextDoc);
    expect(mockRequest.getParameter(eq("xpage"))).andReturn("").anyTimes();
    expect(mockRequest.getParameter(eq("s"))).andReturn("").anyTimes();
    DocumentReference appScriptDocRef = new DocumentReference(context.getDatabase(),
        IAppScriptService.APP_SCRIPT_SPACE_NAME, "testScript");
    expect(xwiki.exists(eq(appScriptDocRef), same(context))).andReturn(false).anyTimes();
    DocumentReference centralAppScriptDocRef = new DocumentReference("celements2web",
        IAppScriptService.APP_SCRIPT_SPACE_NAME, "testScript");
    expect(xwiki.exists(eq(centralAppScriptDocRef), same(context))).andReturn(true
        ).anyTimes();
    XWikiDocument appScriptDoc = new XWikiDocument(appScriptDocRef);
    appScriptDoc.setContent("this is no empty script!");
    expect(xwiki.getDocument(eq(centralAppScriptDocRef), same(context))).andReturn(
        appScriptDoc).anyTimes();
    replayAll(mockRequest);
    DocumentReference expectedAppDocRef = new DocumentReference("celements2web",
        IAppScriptService.APP_SCRIPT_SPACE_NAME, "testScript");
    assertEquals(expectedAppDocRef, appScriptService.getAppScriptDocRef("testScript"));
    verifyAll(mockRequest);
  }

  private void replayAll(Object ... mocks) {
    replay(xwiki);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki);
    verify(mocks);
  }

}
