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
import com.xpn.xwiki.web.Utils;

public class XWikiXWikiRightsTest extends AbstractBridgedComponentTestCase {

  private XWikiXWikiRights mandatoryXWikiRights;

  @Before
  public void setUp_XWikiXWikiPreferencesTest() throws Exception {
    mandatoryXWikiRights = (XWikiXWikiRights) Utils.getComponent(
        IMandatoryDocumentRole.class, "celements.mandatory.wikirights");
  }

  @Test
  public void testDependsOnMandatoryDocuments() throws Exception {
    assertEquals(1, mandatoryXWikiRights.dependsOnMandatoryDocuments().size());
    assertEquals("celements.MandatoryGroups", 
        mandatoryXWikiRights.dependsOnMandatoryDocuments().get(0));
  }

  @Test
  public void testSkip() {
    expect(getWikiMock().ParamAsLong(eq("celements.mandatory.skipWikiRights"), eq(0L))
        ).andReturn(1L).anyTimes();
    replayDefault();
    assertTrue(mandatoryXWikiRights.skip());
    verifyDefault();
  }

  @Test
  public void testSkip_illegalValue() {
    expect(getWikiMock().ParamAsLong(eq("celements.mandatory.skipWikiRights"))).andThrow(
        new NumberFormatException(null)).anyTimes();
    expect(getWikiMock().ParamAsLong(eq("celements.mandatory.skipWikiRights"), eq(0L))
        ).andReturn(0L).anyTimes();
    replayDefault();
    assertFalse(mandatoryXWikiRights.skip());
    verifyDefault();
  }

}
