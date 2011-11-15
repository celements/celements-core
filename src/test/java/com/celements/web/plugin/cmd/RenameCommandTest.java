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
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.web.utils.IWebUtils;
import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public class RenameCommandTest  extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private RenameCommand renameCmd;
  private IWebUtils mockWebUtils;
  
  @Before
  public void setUp_CelementsWebPluginTest() throws Exception {
    renameCmd = new RenameCommand();
    mockWebUtils = createMock(IWebUtils.class);
    renameCmd.inject_webUtils(mockWebUtils);
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
  }

  @Test
  public void testInject_WebUtils() {
    renameCmd.inject_webUtils(mockWebUtils);
    assertSame(mockWebUtils, renameCmd.getWebUtils());
  }

  @Test
  public void testGetWebUtils() {
    renameCmd.inject_webUtils(null);
    assertSame(WebUtils.getInstance(), renameCmd.getWebUtils());
  }

  @Test
  public void testRenameDoc_externalAccessWithFlushMenuCache() throws Exception {
    String docName = "mySpace.myDoc1";
    String newDocName = "myNewSpace.myDoc2";
    XWikiDocument xDoc = createMock(XWikiDocument.class);
    expect(xwiki.exists(eq(docName), same(context))).andReturn(true).atLeastOnce();
    expect(xwiki.getDocument(eq(docName), same(context))).andReturn(xDoc).once();
    expect(xwiki.exists(eq(newDocName), same(context))).andReturn(false).atLeastOnce();
    xDoc.rename(eq(newDocName), same(context));
    expectLastCall().once();
    mockWebUtils.flushMenuItemCache(same(context));
    expectLastCall().once();
    replay(xwiki, mockWebUtils, xDoc);
    assertTrue(renameCmd.renameDoc(docName, newDocName, context));
    verify(xwiki, mockWebUtils, xDoc);
  }

  @Test
  public void testRenameDoc_internalAccessWithFlushMenuCache() throws Exception {
    String docName = "mySpace.myDoc1";
    String newDocName = "myNewSpace.myDoc2";
    XWikiDocument xDoc = createMock(XWikiDocument.class);
    expect(xwiki.exists(eq(docName), same(context))).andReturn(true).atLeastOnce();
    expect(xwiki.getDocument(eq(docName), same(context))).andReturn(xDoc).once();
    expect(xwiki.exists(eq(newDocName), same(context))).andReturn(false).atLeastOnce();
    xDoc.rename(eq(newDocName), same(context));
    expectLastCall().once();
    mockWebUtils.flushMenuItemCache(same(context));
    expectLastCall().once();
    replay(xwiki, mockWebUtils, xDoc);
    assertTrue(renameCmd.renameDoc(docName, newDocName, false, context));
    verify(xwiki, mockWebUtils, xDoc);
  }

  @Test
  public void testRenameDoc_internalAccessWithOUTflushMenuCache() throws Exception {
    String docName = "mySpace.myDoc1";
    String newDocName = "myNewSpace.myDoc2";
    XWikiDocument xDoc = createMock(XWikiDocument.class);
    expect(xwiki.exists(eq(docName), same(context))).andReturn(true
        ).atLeastOnce();
    expect(xwiki.getDocument(eq(docName), same(context))).andReturn(xDoc).once();
    expect(xwiki.exists(eq(newDocName), same(context))).andReturn(false).atLeastOnce();
    xDoc.rename(eq(newDocName), same(context));
    expectLastCall().once();
    replay(xwiki, mockWebUtils, xDoc);
    assertTrue(renameCmd.renameDoc(docName, newDocName, true, context));
    verify(xwiki, mockWebUtils, xDoc);
  }

  @Test
  public void testRenameDoc_internalAccess_newDocAlreadyExists() throws Exception {
    String docName = "mySpace.myDoc1";
    String newDocName = "myNewSpace.myDoc2";
    expect(xwiki.exists(eq(docName), same(context))).andReturn(true
        ).atLeastOnce();
    expect(xwiki.exists(eq(newDocName), same(context))).andReturn(true).atLeastOnce();
    replay(xwiki, mockWebUtils);
    assertFalse(renameCmd.renameDoc(docName, newDocName, true, context));
    verify(xwiki, mockWebUtils);
  }

  @Test
  public void testRenameDoc_internalAccess_docDoesNOTexists() throws Exception {
    String docName = "mySpace.myDoc1";
    String newDocName = "myNewSpace.myDoc2";
    expect(xwiki.exists(eq(docName), same(context))).andReturn(false
        ).atLeastOnce();
    expect(xwiki.exists(eq(newDocName), same(context))).andReturn(false).atLeastOnce();
    replay(xwiki, mockWebUtils);
    assertFalse(renameCmd.renameDoc(docName, newDocName, true, context));
    verify(xwiki, mockWebUtils);
  }

  @Test
  public void testRenameDoc_Exception_on_getDocument() throws Exception {
    String docName = "mySpace.myDoc1";
    String newDocName = "myNewSpace.myDoc2";
    XWikiDocument xDoc = createMock(XWikiDocument.class);
    expect(xwiki.exists(eq(docName), same(context))).andReturn(true
        ).atLeastOnce();
    expect(xwiki.exists(eq(newDocName), same(context))).andReturn(false).atLeastOnce();
    expect(xwiki.getDocument(eq(docName), same(context))).andThrow(new XWikiException());
    replay(xwiki, mockWebUtils, xDoc);
    assertFalse(renameCmd.renameDoc(docName, newDocName, true, context));
    verify(xwiki, mockWebUtils, xDoc);
  }

  @Test
  public void testRenameDoc_Exception_on_rename_withFlushMenuCache() throws Exception {
    String docName = "mySpace.myDoc1";
    String newDocName = "myNewSpace.myDoc2";
    XWikiDocument xDoc = createMock(XWikiDocument.class);
    expect(xwiki.exists(eq(docName), same(context))).andReturn(true
        ).atLeastOnce();
    expect(xwiki.getDocument(eq(docName), same(context))).andReturn(xDoc).once();
    expect(xwiki.exists(eq(newDocName), same(context))).andReturn(false).atLeastOnce();
    xDoc.rename(eq(newDocName), same(context));
    expectLastCall().andThrow(new XWikiException());
    replay(xwiki, mockWebUtils, xDoc);
    assertFalse(renameCmd.renameDoc(docName, newDocName, false, context));
    verify(xwiki, mockWebUtils, xDoc);
  }

  @Test
  public void testRenameSpace() throws Exception {
    String spaceName = "mySpace";
    String newSpaceName = "myNewSpace";
    String firstDocName = "myDoc1";
    XWikiDocument firstDoc = createMock(XWikiDocument.class);
    expect(xwiki.exists(eq(spaceName + "." + firstDocName), same(context))).andReturn(true
        ).atLeastOnce();
    expect(xwiki.getDocument(eq(spaceName + "." + firstDocName), same(context))
        ).andReturn(firstDoc).once();
    expect(xwiki.exists(eq(newSpaceName + "." + firstDocName), same(context))
        ).andReturn(false).atLeastOnce();
    firstDoc.rename(eq("myNewSpace.myDoc1"), same(context));
    expectLastCall().once();
    String firstDocName2 = "myDoc2";
    XWikiDocument secondDoc = createMock(XWikiDocument.class);
    expect(xwiki.exists(eq(spaceName + "." + firstDocName2), same(context))
        ).andReturn(true).atLeastOnce();
    expect(xwiki.exists(eq(newSpaceName + "." + firstDocName2), same(context))
        ).andReturn(false).atLeastOnce();
    expect(xwiki.getDocument(eq(spaceName + "." + firstDocName2), same(context))
        ).andReturn(secondDoc).once();
    secondDoc.rename(eq("myNewSpace.myDoc2"), same(context));
    expectLastCall().once();
    List<String> docNames = Arrays.asList(firstDocName, firstDocName2);
    expect(xwiki.getSpaceDocsName(eq(spaceName), same(context))).andReturn(docNames);
    mockWebUtils.flushMenuItemCache(same(context));
    expectLastCall().once();
    replay(xwiki, mockWebUtils, firstDoc, secondDoc);
    List<String> renamedPages = renameCmd.renameSpace(spaceName, newSpaceName, context);
    assertEquals(docNames, renamedPages);
    verify(xwiki, mockWebUtils, firstDoc, secondDoc);
  }
  
  @Test
  public void testRenameSpace_firstDocFails() throws Exception {
    String spaceName = "mySpace";
    String newSpaceName = "myNewSpace";
    String firstDocName = "myDoc1";
    expect(xwiki.exists(eq(spaceName + "." + firstDocName), same(context))).andReturn(true
        ).atLeastOnce();
    expect(xwiki.exists(eq(newSpaceName + "." + firstDocName), same(context))
        ).andReturn(false).atLeastOnce();
    expect(xwiki.getDocument(eq(spaceName + "." + firstDocName), same(context))
      ).andThrow(new XWikiException());
    String firstDocName2 = "myDoc2";
    XWikiDocument secondDoc = createMock(XWikiDocument.class);
    expect(xwiki.exists(eq(spaceName + "." + firstDocName2), same(context))
        ).andReturn(true).atLeastOnce();
    expect(xwiki.exists(eq(newSpaceName + "." + firstDocName2), same(context))
        ).andReturn(false).atLeastOnce();
    expect(xwiki.getDocument(eq(spaceName + "." + firstDocName2), same(context))
        ).andReturn(secondDoc).once();
    secondDoc.rename(eq("myNewSpace.myDoc2"), same(context));
    expectLastCall().once();
    List<String> docNames = Arrays.asList(firstDocName, firstDocName2);
    expect(xwiki.getSpaceDocsName(eq(spaceName), same(context))).andReturn(docNames);
    mockWebUtils.flushMenuItemCache(same(context));
    expectLastCall().once();
    replay(xwiki, mockWebUtils, secondDoc);
    List<String> renamedPages = renameCmd.renameSpace(spaceName, newSpaceName, context);
    assertEquals(Arrays.asList(firstDocName2), renamedPages);
    verify(xwiki, mockWebUtils, secondDoc);
  }
  
  @Test
  public void testRenameSpace_spaceSearchFails_Exception() throws Exception {
    String spaceName = "mySpace";
    String newSpaceName = "myNewSpace";
    expect(xwiki.getSpaceDocsName(eq(spaceName), same(context))).andThrow(
        new XWikiException());
    replay(xwiki, mockWebUtils);
    List<String> renamedPages = renameCmd.renameSpace(spaceName, newSpaceName, context);
    assertTrue(renamedPages.isEmpty());
    verify(xwiki, mockWebUtils);
  }
  
  @Test
  public void testRenameSpace_allDocFail() throws Exception {
    String spaceName = "mySpace";
    String newSpaceName = "myNewSpace";
    String firstDocName = "myDoc1";
    expect(xwiki.exists(eq(spaceName + "." + firstDocName), same(context))).andReturn(true
        ).atLeastOnce();
    expect(xwiki.exists(eq(newSpaceName + "." + firstDocName), same(context))
        ).andReturn(true).atLeastOnce();
    String firstDocName2 = "myDoc2";
    expect(xwiki.exists(eq(spaceName + "." + firstDocName2), same(context))
        ).andReturn(true).atLeastOnce();
    expect(xwiki.exists(eq(newSpaceName + "." + firstDocName2), same(context))
        ).andReturn(true).atLeastOnce();
    List<String> docNames = Arrays.asList(firstDocName, firstDocName2);
    expect(xwiki.getSpaceDocsName(eq(spaceName), same(context))).andReturn(docNames);
    replay(xwiki, mockWebUtils);
    List<String> renamedPages = renameCmd.renameSpace(spaceName, newSpaceName, context);
    assertTrue(renamedPages.isEmpty());
    verify(xwiki, mockWebUtils);
  }
  
}
