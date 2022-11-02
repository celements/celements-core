package com.celements.web.service;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.appScript.AppScriptService;
import com.celements.appScript.IAppScriptService;
import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;

public class CelementsWebScriptServiceTest extends AbstractComponentTest {

  private XWikiContext context;
  private XWiki xwiki;
  private CelementsWebScriptService celWebService;
  private XWikiRightService mockRightService;

  @Before
  public void setUp_CelementsWebScriptServiceTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    mockRightService = createMock(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(mockRightService).anyTimes();
    celWebService = new CelementsWebScriptService();
    celWebService.webUtilsService = Utils.getComponent(IWebUtilsService.class);
    celWebService.execution = Utils.getComponent(Execution.class);
    celWebService.appScriptService = Utils.getComponent(IAppScriptService.class);
  }

  @Test
  public void testDeleteMenuItem() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myDocument");
    expect(mockRightService.hasAccessLevel(eq("edit"), eq("XWiki.XWikiGuest"), eq(
        "mySpace.myDocument"), same(context))).andReturn(false).once();
    replayAll();
    assertFalse("expecting false because of no edit rights", celWebService.deleteMenuItem(docRef));
    verifyAll();
  }

  @Test
  public void testGetHumanReadableSize_FullSize() {
    context.setLanguage("de");
    assertEquals("2,0 MB", celWebService.getHumanReadableSize(2000000, true));
  }

  @Test
  public void testGetHumanReadableSize_PartSize_de() {
    context.setLanguage("de");
    assertEquals("25,4 MB", celWebService.getHumanReadableSize(25432100, true));
  }

  @Test
  public void testGetHumanReadableSize_PartSize_dech() {
    context.setLanguage("de-ch");
    assertEquals("de-ch", "2.6 MB", celWebService.getHumanReadableSize(2563210, true));
    assertEquals("fr", "2,6 MB", celWebService.getHumanReadableSize(2563210, true, "fr"));
  }

  @Test
  public void testGetHumanReadableSize_PartSize_de_country_CH() {
    context.setLanguage("de");
    String formatted = celWebService.getHumanReadableSize(1055563210, false,
        celWebService.getLocal("de", "ch"))
        .replace('\'', '’'); // older java versions used different apostrophe
    assertEquals("de-ch", "1’006.7 MiB", formatted);
    assertEquals("fr", "2,6 MB", celWebService.getHumanReadableSize(2563210, true, "fr"));
  }

  @Test
  public void test_getLocal() {
    assertEquals("de", celWebService.getLocal("de").toString());
  }

  @Test
  public void test_getLocal_country() {
    assertEquals("de_CH", celWebService.getLocal("de", "ch").toString());
  }

  @Test
  public void test_getLocal_country_illegal_combination() {
    assertEquals("en_CH", celWebService.getLocal("en", "ch").toString());
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
    assertTrue(celWebService.isAppScriptRequest());
    verifyAll(mockRequest);
  }

  @Test
  public void testGetCurrentPageURL_isAppScriptRequest() {
    IAppScriptService appScriptServiceMock = createMock(AppScriptService.class);
    celWebService.appScriptService = appScriptServiceMock;
    String queryString = "myQueryString";
    String scriptName = "myScript/Name";

    expect(appScriptServiceMock.isAppScriptRequest()).andReturn(true).once();
    expect(appScriptServiceMock.getScriptNameFromURL()).andReturn(scriptName).once();
    expect(appScriptServiceMock.getAppScriptURL(eq(scriptName), eq(queryString))).andReturn(
        "theURL").once();

    replayAll(appScriptServiceMock);
    assertEquals("theURL", celWebService.getCurrentPageURL(queryString));
    verifyAll(appScriptServiceMock);
  }

  @Test
  public void testGetCurrentPageURL_isNotAppScriptRequest() {
    IAppScriptService appScriptServiceMock = createMock(AppScriptService.class);
    celWebService.appScriptService = appScriptServiceMock;
    String queryString = "my[Query}String";

    expect(appScriptServiceMock.isAppScriptRequest()).andReturn(false).once();

    replayAll(appScriptServiceMock);
    assertEquals("?my%5BQuery%7DString", celWebService.getCurrentPageURL(queryString));
    verifyAll(appScriptServiceMock);
  }

  @Test
  public void testIsHighDate() {
    replayAll();
    assertTrue(celWebService.isHighDate(IWebUtilsService.DATE_HIGH));
    verifyAll();
  }

  @Test
  public void testIsHighDate_NPE() {
    replayAll();
    assertFalse(celWebService.isHighDate(null));
    verifyAll();
  }

  private void replayAll(Object... mocks) {
    replay(xwiki, mockRightService);
    replay(mocks);
  }

  private void verifyAll(Object... mocks) {
    verify(xwiki, mockRightService);
    verify(mocks);
  }

}
