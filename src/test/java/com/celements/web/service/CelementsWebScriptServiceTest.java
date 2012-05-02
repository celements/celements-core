package com.celements.web.service;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.text.DecimalFormat;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;

import com.celements.appScript.IAppScriptService;
import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiRequest;

public class CelementsWebScriptServiceTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private CelementsWebScriptService celWebService;
  private XWikiRightService mockRightService;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp_CelementsWebScriptServiceTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    mockRightService = createMock(XWikiRightService.class);
    expect(xwiki.getRightService()).andReturn(mockRightService).anyTimes();
    celWebService = new CelementsWebScriptService();
    celWebService.execution = Utils.getComponent(Execution.class);
    celWebService.modelSerializer = Utils.getComponent(EntityReferenceSerializer.class,
        "local");
    celWebService.appScriptService = Utils.getComponent(IAppScriptService.class);
  }

  @Test
  public void testDeleteMenuItem() throws Exception {
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myDocument");
    expect(mockRightService.hasAccessLevel(eq("edit"), eq("XWiki.XWikiGuest"),
        eq("mySpace.myDocument"), same(context))).andReturn(false).once();
    replayAll();
    assertFalse("expecting false because of no edit rights", celWebService.deleteMenuItem(
        docRef));
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
    assertEquals("fr", "2,6 MB", celWebService.getHumanReadableSize(2563210, true,
        "fr"));
  }

  @Test
  public void testGetHumanReadableSize_PartSize_de_country_CH() {
    context.setLanguage("de");
    assertEquals("de-ch", "1'006.7 MiB", celWebService.getHumanReadableSize(1055563210,
        false, celWebService.getLocal("de", "ch")));
    assertEquals("fr", "2,6 MB", celWebService.getHumanReadableSize(2563210, true,
        "fr"));
  }

  @Test
  public void testGetLocal() {
    java.text.NumberFormat formater = DecimalFormat.getInstance(celWebService.getLocal(
        "de"));
    assertEquals("12.312.312", formater.format(12312312L));
  }

  @Test
  public void testGetLocal_country() {
    java.text.NumberFormat formater = DecimalFormat.getInstance(celWebService.getLocal(
        "de", "ch"));
    assertEquals("12'312'312", formater.format(12312312L));
  }

  @Test
  public void testGetLocal_country_illegal_combination() {
    java.text.NumberFormat formater = DecimalFormat.getInstance(celWebService.getLocal(
        "en", "ch"));
    assertEquals("12,312,312", formater.format(12312312L));
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
    replayAll(mockRequest);
    assertTrue(celWebService.isAppScriptRequest());
    verifyAll(mockRequest);
  }


  private void replayAll(Object ... mocks) {
    replay(xwiki, mockRightService);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, mockRightService);
    verify(mocks);
  }

}
