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
package com.celements.navigation.cmd;

import java.util.Set;

import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class RestructureSaveHandlerTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private ReorderSaveHandler restrSaveCmd;
  private XWiki wiki;

  @Before
  public void setUp_RestructureSaveCommandTest() throws Exception {
    context = getContext();
    wiki = createMock(XWiki.class);
    context.setWiki(wiki);
    restrSaveCmd = new ReorderSaveHandler(context);
  }

  @Test
  public void testGetParentFN_null() {
    restrSaveCmd.inject_ParentFN(null);
    assertNotNull("expecting empty string instead of null", restrSaveCmd.getParentFN());
    assertEquals("", restrSaveCmd.getParentFN());
  }

  @Test
  public void testExtractDocFN() {
    assertEquals("MySpace.MyDoc", restrSaveCmd.extractDocFN("CN1:MySpace.MyDoc"));
    assertEquals("", restrSaveCmd.extractDocFN("CN1:"));
  }

  @Test
  public void testGetDirtyParents() {
    Set<String> dirtyParents = restrSaveCmd.getDirtyParents();
    assertNotNull(dirtyParents);
    assertTrue(dirtyParents.isEmpty());
    assertSame(dirtyParents, restrSaveCmd.getDirtyParents());
  }

  @Test
  public void testMarkParentDirty() {
    String parent = "MyStructDoc.MyParentDoc";
    restrSaveCmd.markParentDirty(parent);
    assertEquals(1, restrSaveCmd.getDirtyParents().size());
    assertTrue(restrSaveCmd.getDirtyParents().contains(parent));
  }

  @Test
  public void testReadPropertyKey() {
    restrSaveCmd.inject_current(EReorderLiteral.PARENT_CHILDREN_PROPERTY);
    expect(wiki.exists(eq("MySpace.MyDoc"), same(context))).andReturn(true);
    replay(wiki);
    restrSaveCmd.readPropertyKey("CN1:MySpace.MyDoc");
    assertEquals("MySpace.MyDoc", restrSaveCmd.getParentFN());
    verify(wiki);
  }

  @Test
  public void testReadPropertyKey_notexists() {
    restrSaveCmd.inject_current(EReorderLiteral.PARENT_CHILDREN_PROPERTY);
    expect(wiki.exists(eq("MySpace.MyDoc"), same(context))).andReturn(false);
    replay(wiki);
    restrSaveCmd.readPropertyKey("CN1:MySpace.MyDoc");
    assertEquals("", restrSaveCmd.getParentFN());
    verify(wiki);
  }

  @Test
  public void testStringEvent_onlyPosition() throws Exception {
    restrSaveCmd.inject_current(EReorderLiteral.ELEMENT_ID);
    String parentFN = "MySpace.MyParentDoc";
    restrSaveCmd.inject_ParentFN(parentFN);
    String docFN = "MySpace.MyDoc1";
    expect(wiki.exists(eq(docFN), same(context))).andReturn(true);
    XWikiDocument xdoc = new XWikiDocument();
    xdoc.setParent(parentFN);
    BaseObject menuItemObj = new BaseObject();
    menuItemObj.setIntValue("menu_position", 12);
    xdoc.setObject("Celements2.MenuItem", 0, menuItemObj);
    expect(wiki.getDocument(eq(docFN), same(context))).andReturn(xdoc);
    wiki.saveDocument(same(xdoc), eq("Restructuring"), same(context));
    expectLastCall();
    replay(wiki);
    restrSaveCmd.stringEvent("LIN1:MySpace.MyDoc1");
    assertEquals("expecting increment afterwards.", new Integer(1),
        restrSaveCmd.getCurrentPos());
    assertEquals("expecting position reset.", 0,
        menuItemObj.getIntValue("menu_position"));
    assertTrue("expecting parent in dirtyParents after position changed.",
        restrSaveCmd.getDirtyParents().contains(parentFN));
    assertEquals(1, restrSaveCmd.getDirtyParents().size());
    verify(wiki);
  }

  @Test
  public void testStringEvent_onlyParents() throws Exception {
    restrSaveCmd.inject_current(EReorderLiteral.ELEMENT_ID);
    String parentFN = "MySpace.MyParentDoc";
    restrSaveCmd.inject_ParentFN(parentFN);
    String docFN = "MySpace.MyDoc1";
    expect(wiki.exists(eq(docFN), same(context))).andReturn(true);
    XWikiDocument xdoc = new XWikiDocument();
    String oldParentFN = "MySpace.OldParentDoc";
    xdoc.setParent(oldParentFN);
    BaseObject menuItemObj = new BaseObject();
    menuItemObj.setIntValue("menu_position", 0);
    xdoc.setObject("Celements2.MenuItem", 0, menuItemObj);
    expect(wiki.getDocument(eq(docFN), same(context))).andReturn(xdoc);
    wiki.saveDocument(same(xdoc), eq("Restructuring"), same(context));
    expectLastCall();
    replay(wiki);
    restrSaveCmd.stringEvent("LIN1:MySpace.MyDoc1");
    assertEquals("expecting increment afterwards.", new Integer(1),
        restrSaveCmd.getCurrentPos());
    assertEquals("expecting parent reset.", parentFN, xdoc.getParent());
    assertTrue("expecting old parent in dirtyParents.", restrSaveCmd.getDirtyParents(
      ).contains(oldParentFN));
    assertTrue("expecting new parent in dirtyParents.", restrSaveCmd.getDirtyParents(
      ).contains(parentFN));
    assertEquals(2, restrSaveCmd.getDirtyParents().size());
    verify(wiki);
  }

  @Test
  public void testStringEvent_parentsAndPosition() throws Exception {
    restrSaveCmd.inject_current(EReorderLiteral.ELEMENT_ID);
    String parentFN = "MySpace.MyParentDoc";
    restrSaveCmd.inject_ParentFN(parentFN);
    String docFN = "MySpace.MyDoc1";
    expect(wiki.exists(eq(docFN), same(context))).andReturn(true);
    XWikiDocument xdoc = new XWikiDocument();
    String oldParentFN = "MySpace.OldParentDoc";
    xdoc.setParent(oldParentFN);
    BaseObject menuItemObj = new BaseObject();
    menuItemObj.setIntValue("menu_position", 12);
    xdoc.setObject("Celements2.MenuItem", 0, menuItemObj);
    expect(wiki.getDocument(eq(docFN), same(context))).andReturn(xdoc);
    wiki.saveDocument(same(xdoc), eq("Restructuring"), same(context));
    expectLastCall();
    replay(wiki);
    restrSaveCmd.stringEvent("LIN1:MySpace.MyDoc1");
    assertEquals("expecting increment afterwards.", new Integer(1),
        restrSaveCmd.getCurrentPos());
    assertEquals("expecting parent reset.", parentFN, xdoc.getParent());
    assertEquals("expecting position reset.", 0,
        menuItemObj.getIntValue("menu_position"));
    assertTrue("expecting old parent in dirtyParents.", restrSaveCmd.getDirtyParents(
      ).contains(oldParentFN));
    assertTrue("expecting new parent in dirtyParents.", restrSaveCmd.getDirtyParents(
      ).contains(parentFN));
    assertEquals(2, restrSaveCmd.getDirtyParents().size());
    verify(wiki);
  }

  @Test
  public void testStringEvent_emptyParentForRootElement() throws Exception {
    restrSaveCmd.inject_current(EReorderLiteral.ELEMENT_ID);
    restrSaveCmd.inject_ParentFN("");
    String docFN = "MySpace.MyDoc1";
    expect(wiki.exists(eq(docFN), same(context))).andReturn(true);
    XWikiDocument xdoc = new XWikiDocument();
    String oldParentFN = "MySpace.OldParentDoc";
    xdoc.setParent(oldParentFN);
    BaseObject menuItemObj = new BaseObject();
    menuItemObj.setIntValue("menu_position", 12);
    xdoc.setObject("Celements2.MenuItem", 0, menuItemObj);
    expect(wiki.getDocument(eq(docFN), same(context))).andReturn(xdoc);
    wiki.saveDocument(same(xdoc), eq("Restructuring"), same(context));
    expectLastCall();
    replay(wiki);
    restrSaveCmd.stringEvent("LIN1:MySpace.MyDoc1");
    assertEquals("expecting increment afterwards.", new Integer(1),
        restrSaveCmd.getCurrentPos());
    assertEquals("expecting parent reset.", "", xdoc.getParent());
    assertEquals("expecting position reset.", 0,
        menuItemObj.getIntValue("menu_position"));
    assertEquals(2, restrSaveCmd.getDirtyParents().size());
    verify(wiki);
  }

  @Test
  public void testStringEvent_noChanges() throws Exception {
    restrSaveCmd.inject_current(EReorderLiteral.ELEMENT_ID);
    String parentFN = "MySpace.MyParentDoc";
    restrSaveCmd.inject_ParentFN(parentFN);
    String docFN = "MySpace.MyDoc1";
    expect(wiki.exists(eq(docFN), same(context))).andReturn(true);
    XWikiDocument xdoc = new XWikiDocument();
    xdoc.setParent(parentFN);
    BaseObject menuItemObj = new BaseObject();
    menuItemObj.setIntValue("menu_position", 0);
    xdoc.setObject("Celements2.MenuItem", 0, menuItemObj);
    expect(wiki.getDocument(eq(docFN), same(context))).andReturn(xdoc);
    replay(wiki);
    restrSaveCmd.stringEvent("LIN1:MySpace.MyDoc1");
    assertEquals("expecting increment afterwards.", new Integer(1),
        restrSaveCmd.getCurrentPos());
    assertEquals("expecting parent reset.", parentFN, xdoc.getParent());
    assertEquals("expecting position reset.", 0,
        menuItemObj.getIntValue("menu_position"));
    assertTrue("noChanges --> no parents need to be updated",
        restrSaveCmd.getDirtyParents().isEmpty());
    verify(wiki);
  }

  @Test
  public void testSetFlushCacheNeeded() {
    assertFalse("Expecting default false.", restrSaveCmd.isFlushCacheNeeded());
    restrSaveCmd.setFlushCacheNeeded();
    assertTrue("Expecting true after calling setFlushCacheNeeded.",
        restrSaveCmd.isFlushCacheNeeded());
  }

}
