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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;

public class ImageMapCommandTest extends AbstractBridgedComponentTestCase {

  private ImageMapCommand imgMapCmd;
  private XWikiContext context;
  private XWiki xwiki;

  @Before
  public void setUp_ImageMapCommandTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    imgMapCmd = new ImageMapCommand(context);
  }

  @Test
  public void testGetImageUseMaps_simple() {
    String rteContent = "<img "
      + " src=\"/download/Content_attachments/FileBaseDoc/grundriss.png\""
      + " usemap=\"#objektwahlImg\">";
    replayAll();
    List<String> useMaps = imgMapCmd.getImageUseMaps(rteContent);
    assertNotNull(useMaps);
    assertEquals(Arrays.asList("objektwahlImg"), useMaps);
    assertEquals(1, useMaps.size());
    verifyAll();
  }

  @Test
  public void testGetImageUseMaps() {
    String rteContent = "<p>\n<img style=\"border-style: initial; border-color: initial;"
      + " border-image: initial; border-width: 0px;\""
      + " src=\"/download/Content_attachments/FileBaseDoc/grundriss.png\""
      + " border=\"0\" usemap=\"#objektwahlImg\">\n</p>";
    replayAll();
    List<String> useMaps = imgMapCmd.getImageUseMaps(rteContent);
    assertNotNull(useMaps);
    assertEquals(Arrays.asList("objektwahlImg"), useMaps);
    assertEquals(1, useMaps.size());
    verifyAll();
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
