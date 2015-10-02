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
  public void testDependsOnMandatoryDocuments() throws Exception {
    assertEquals(0, mandatoryXWikiPref.dependsOnMandatoryDocuments().size());
  }

  @Test
  public void testNotMainWiki_main() {
    context.setDatabase("mainWiki");
    context.setMainXWiki("mainWiki");
    replayDefault();
    assertFalse(mandatoryXWikiPref.notMainWiki());
    verifyDefault();
  }

  @Test
  public void testNotMainWiki_notMain() {
    context.setDatabase("myWiki");
    context.setMainXWiki("mainWiki");
    replayDefault();
    assertTrue(mandatoryXWikiPref.notMainWiki());
    verifyDefault();
  }

  @Test
  public void testSkip_illegalValue() {
    expect(xwiki.ParamAsLong(eq("celements.mandatory.skipWikiPreferences"))).andThrow(
        new NumberFormatException(null)).anyTimes();
    expect(xwiki.ParamAsLong(eq("celements.mandatory.skipWikiPreferences"), eq(0L))
        ).andReturn(0L).anyTimes();
    replayDefault();
    mandatoryXWikiPref.skip();
    verifyDefault();
  }

}
