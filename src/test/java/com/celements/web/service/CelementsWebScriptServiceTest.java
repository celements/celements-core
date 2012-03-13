package com.celements.web.service;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

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
    assertEquals("2 MB", celWebService.getHumanReadableSize(2000000L, true));
  }

  @Test
  public void testGetHumanReadableSize_PartSize_chde() {
    context.setLanguage("de-ch");
    assertEquals("de-ch", "2.6 MB", celWebService.getHumanReadableSize(2563210L, true));
    assertEquals("fr", "2,6 MB", celWebService.getHumanReadableSize(2563210L, true,
        "fr"));
  }

  @Test
  public void testGetHumanReadableSize_PartSize_de() {
    context.setLanguage("de");
    assertEquals("25,4 MB", celWebService.getHumanReadableSize(25432100L, true));
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
