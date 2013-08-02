package com.celements.mandatory;

import static org.easymock.EasyMock.*;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

public class XWikiXWikiPreferencesTest extends AbstractBridgedComponentTestCase {

  private XWikiXWikiPreferences mandatoryXWikiPref;
  private XWikiContext context;
  private XWiki xwiki;

  @Before
  public void setUp_XWikiXWikiPreferencesTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    mandatoryXWikiPref = (XWikiXWikiPreferences) Utils.getComponent(
        IMandatoryDocumentRole.class, "celements.mandatory.wikipreferences");
  }

  @Test
  public void testNoMainWiki_main() {
    context.setDatabase("mainWiki");
    context.setMainXWiki("mainWiki");
    replayDefault();
    assertFalse(mandatoryXWikiPref.noMainWiki());
    verifyDefault();
  }

  @Test
  public void testNoMainWiki_notMain() {
    context.setDatabase("myWiki");
    context.setMainXWiki("mainWiki");
    replayDefault();
    assertTrue(mandatoryXWikiPref.noMainWiki());
    verifyDefault();
  }

  @Test
  public void testSkipCelementsWikiPreferences_illegalValue() {
    expect(xwiki.ParamAsLong(eq("celements.mandatory.skipWikiPreferences"))).andThrow(
        new NumberFormatException(null)).anyTimes();
    expect(xwiki.ParamAsLong(eq("celements.mandatory.skipWikiPreferences"), eq(0L))
        ).andReturn(0L).anyTimes();
    replayDefault();
    mandatoryXWikiPref.isSkipCelementsWikiPreferences();
    verifyDefault();
  }

}
