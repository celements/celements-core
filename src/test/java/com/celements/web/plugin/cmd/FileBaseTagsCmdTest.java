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
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.navigation.TreeNode;
import com.celements.navigation.filter.INavFilter;
import com.celements.web.utils.IWebUtils;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

public class FileBaseTagsCmdTest extends AbstractBridgedComponentTestCase {

  private FileBaseTagsCmd fileBaseTagCmd;
  private XWikiContext context;
  private XWiki xwiki;
  private IWebUtils mockUtils;

  @Before
  public void setUp_FileBaseTagsCmdTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    fileBaseTagCmd = new FileBaseTagsCmd();
    mockUtils = createMock(IWebUtils.class);
    fileBaseTagCmd.inject_celUtils(mockUtils);
  }

  @Test
  public void testGetTagSpaceName() {
    String celFileBaseName = "Content_attachments.FileBaseDoc";
    expect(xwiki.getSpacePreference(eq("cel_centralfilebase"), eq(""),
        same(context))).andReturn(celFileBaseName);
    replay(xwiki, mockUtils);
    assertEquals("Content_attachments", fileBaseTagCmd.getTagSpaceName(context));
    verify(xwiki, mockUtils);
  }

  @Test
  public void testGetTagSpaceName_onlySpaceName() {
    String celFileBaseName = "Content_attachments";
    expect(xwiki.getSpacePreference(eq("cel_centralfilebase"), eq(""),
        same(context))).andReturn(celFileBaseName);
    replay(xwiki, mockUtils);
    assertEquals("Content_attachments", fileBaseTagCmd.getTagSpaceName(context));
    verify(xwiki, mockUtils);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetTagDocument_docExists_without_MenuItem() throws Exception {
    String celFileBaseName = "Content_attachments";
    expect(xwiki.getSpacePreference(eq("cel_centralfilebase"), eq(""),
        same(context))).andReturn(celFileBaseName).anyTimes();
    String tagDocFN = celFileBaseName + ".tag0";
    expect(xwiki.exists(eq(tagDocFN), same(context))).andReturn(true);
    expect(mockUtils.getSubNodesForParent(eq(""), eq(celFileBaseName),
        isA(INavFilter.class), same(context))).andReturn(Collections.emptyList());
    expect(xwiki.getDocument(eq(tagDocFN), same(context))).andReturn(new XWikiDocument()
      ).once();
    replay(xwiki, mockUtils);
    assertNotNull("docAlready exists: expecting existing doc",
        fileBaseTagCmd.getTagDocument("tag0", false, context));
    verify(xwiki, mockUtils);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetTagDocument_docExists() throws Exception {
    String celFileBaseName = "Content_attachments";
    expect(xwiki.getSpacePreference(eq("cel_centralfilebase"), eq(""),
        same(context))).andReturn(celFileBaseName).anyTimes();
    String tagDocFN = celFileBaseName + ".tag0";
    expect(xwiki.exists(eq(tagDocFN), same(context))).andReturn(true);
    expect(mockUtils.getSubNodesForParent(eq(""), eq(celFileBaseName),
        isA(INavFilter.class), same(context))).andReturn(Arrays.asList(new TreeNode(
            new DocumentReference(context.getDatabase(), celFileBaseName, "tag0"), "",
            0)));
    XWikiDocument existingTagDoc = new XWikiDocument();
    expect(xwiki.getDocument(eq(tagDocFN), same(context))).andReturn(existingTagDoc
      ).once();
    replay(xwiki, mockUtils);
    assertSame("docAlready exists: expecting existing doc", existingTagDoc,
        fileBaseTagCmd.getTagDocument("tag0", false, context));
    verify(xwiki, mockUtils);
  }

}
