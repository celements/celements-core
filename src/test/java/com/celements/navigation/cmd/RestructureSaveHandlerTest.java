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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.navigation.INavigationClassConfig;
import com.celements.navigation.Navigation;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class RestructureSaveHandlerTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private ReorderSaveHandler restrSaveCmd;
  private XWiki wiki;

  @Before
  public void setUp_RestructureSaveCommandTest() throws Exception {
    context = getContext();
    wiki = getWikiMock();
    restrSaveCmd = new ReorderSaveHandler(context);
  }

  @Test
  public void testGetParentFN_null() {
    restrSaveCmd.inject_ParentRef(null);
    assertNotNull("expecting empty string instead of null", restrSaveCmd.getParentFN());
    assertEquals("", restrSaveCmd.getParentFN());
  }

  @Test
  public void testExtractDocFN() {
    assertEquals("MySpace.MyDoc", restrSaveCmd.extractDocFN("CN1:MySpace:MySpace.MyDoc"));
    assertEquals("", restrSaveCmd.extractDocFN("CN1:"));
  }

  @Test
  public void testExtractDocFN_NavigationCreateID() {
    Navigation helpNav = new Navigation("N1");
    helpNav.setNodeSpace(new SpaceReference("MySpace", new WikiReference(
        context.getDatabase())));
    String menuItemName = "MySpace.MyDoc";
    String navUniqLiId = helpNav.getUniqueId(menuItemName);
    assertEquals("getUniqueId in Navigation returns [" + navUniqLiId + "] which cannot be"
        + " parsed correctly in extractDocFN.", menuItemName, restrSaveCmd.extractDocFN(
            navUniqLiId));
  }

  @Test
  public void testGetDirtyParents() {
    Set<EntityReference> dirtyParents = restrSaveCmd.getDirtyParents();
    assertNotNull(dirtyParents);
    assertTrue(dirtyParents.isEmpty());
    assertSame(dirtyParents, restrSaveCmd.getDirtyParents());
  }

  @Test
  public void testMarkParentDirty() {
    DocumentReference parentRef = new DocumentReference(context.getDatabase(),
        "MyStructDoc", "MyParentDoc");
    restrSaveCmd.markParentDirty(parentRef);
    assertEquals(1, restrSaveCmd.getDirtyParents().size());
    assertTrue(restrSaveCmd.getDirtyParents().contains(parentRef));
  }

  @Test
  public void testReadPropertyKey() {
    restrSaveCmd.inject_current(EReorderLiteral.PARENT_CHILDREN_PROPERTY);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyDoc");
    expect(wiki.exists(eq(docRef), same(context))).andReturn(true);
    replayDefault();
    restrSaveCmd.readPropertyKey("CN1:MySpace:MySpace.MyDoc");
    assertEquals("MySpace.MyDoc", restrSaveCmd.getParentFN());
    verifyDefault();
  }

  @Test
  public void testIsDiffParentReferences_oldRef_Null() {
    EntityReference parentRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyParentDoc");
    restrSaveCmd.inject_ParentRef(null);
    replayDefault();
    assertTrue(restrSaveCmd.hasDiffParentReferences(parentRef));
    verifyDefault();
  }

  @Test
  public void testIsDiffParentReferences_newRef_Null() {
    EntityReference parentRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyParentDoc");
    restrSaveCmd.inject_ParentRef(parentRef);
    replayDefault();
    assertTrue(restrSaveCmd.hasDiffParentReferences(null));
    verifyDefault();
  }

  @Test
  public void testIsDiffParentReferences_newRef_changed() {
    EntityReference parentRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyParentDoc");
    EntityReference parentRef2 = new DocumentReference(context.getDatabase(), "MySpace",
        "MyParentDoc2");
    restrSaveCmd.inject_ParentRef(parentRef);
    replayDefault();
    assertTrue(restrSaveCmd.hasDiffParentReferences(parentRef2));
    verifyDefault();
  }

  @Test
  public void testIsDiffParentReferences_both_Null() {
    restrSaveCmd.inject_ParentRef(null);
    replayDefault();
    assertFalse(restrSaveCmd.hasDiffParentReferences(null));
    verifyDefault();
  }

  @Test
  public void testIsDiffParentReferences_newRef_same() {
    EntityReference parentRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyParentDoc");
    EntityReference parentRef2 = new DocumentReference(context.getDatabase(), "MySpace",
        "MyParentDoc");
    restrSaveCmd.inject_ParentRef(parentRef);
    replayDefault();
    assertFalse(restrSaveCmd.hasDiffParentReferences(parentRef2));
    verifyDefault();
  }

  @Test
  public void testReadPropertyKey_notexists() {
    restrSaveCmd.inject_current(EReorderLiteral.PARENT_CHILDREN_PROPERTY);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyDoc");
    expect(wiki.exists(eq(docRef), same(context))).andReturn(false);
    replayDefault();
    restrSaveCmd.readPropertyKey("CN1:MySpace:MySpace.MyDoc");
    assertEquals("", restrSaveCmd.getParentFN());
    verifyDefault();
  }

  @Test
  public void testStringEvent_onlyPosition() throws Exception {
    restrSaveCmd.inject_current(EReorderLiteral.ELEMENT_ID);
    EntityReference parentRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyParentDoc");
    restrSaveCmd.inject_ParentRef(parentRef);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyDoc1");
    expect(wiki.exists(eq(docRef), same(context))).andReturn(true);
    XWikiDocument xdoc = new XWikiDocument(docRef);
    xdoc.setParentReference(parentRef);
    BaseObject menuItemObj = new BaseObject();
    menuItemObj.setXClassReference(getMenuItemClassRef());
    menuItemObj.setIntValue("menu_position", 12);
    xdoc.setXObject(0, menuItemObj);
    expect(wiki.getDocument(eq(docRef), same(context))).andReturn(xdoc);
    wiki.saveDocument(same(xdoc), eq("Restructuring"), same(context));
    expectLastCall();
    replayDefault();
    restrSaveCmd.stringEvent("LIN1:MySpace:MySpace.MyDoc1");
    assertEquals("expecting increment afterwards.", new Integer(1),
        restrSaveCmd.getCurrentPos());
    assertEquals("expecting position reset.", 0,
        menuItemObj.getIntValue("menu_position"));
    assertTrue("expecting parent in dirtyParents after position changed.",
        restrSaveCmd.getDirtyParents().contains(parentRef));
    assertEquals(1, restrSaveCmd.getDirtyParents().size());
    verifyDefault();
  }

  @Test
  public void testStringEvent_onlyParents() throws Exception {
    restrSaveCmd.inject_current(EReorderLiteral.ELEMENT_ID);
    EntityReference parentRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyParentDoc");
    restrSaveCmd.inject_ParentRef(parentRef);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyDoc1");
    expect(wiki.exists(eq(docRef), same(context))).andReturn(true);
    XWikiDocument xdoc = new XWikiDocument(docRef);
    EntityReference oldParentRef = new DocumentReference(context.getDatabase(), "MySpace",
        "OldParentDoc");
    xdoc.setParentReference(oldParentRef);
    BaseObject menuItemObj = new BaseObject();
    menuItemObj.setXClassReference(getMenuItemClassRef());
    menuItemObj.setIntValue("menu_position", 0);
    xdoc.setXObject(0, menuItemObj);
    expect(wiki.getDocument(eq(docRef), same(context))).andReturn(xdoc);
    wiki.saveDocument(same(xdoc), eq("Restructuring"), same(context));
    expectLastCall();
    replayDefault();
    restrSaveCmd.stringEvent("LIN1:MySpace:MySpace.MyDoc1");
    assertEquals("expecting increment afterwards.", new Integer(1),
        restrSaveCmd.getCurrentPos());
    assertEquals("expecting parent reset.", parentRef, xdoc.getParentReference());
    assertTrue("expecting old parent in dirtyParents.", restrSaveCmd.getDirtyParents(
      ).contains(oldParentRef));
    assertTrue("expecting new parent in dirtyParents.", restrSaveCmd.getDirtyParents(
      ).contains(parentRef));
    assertEquals(2, restrSaveCmd.getDirtyParents().size());
    verifyDefault();
  }

  @Test
  public void testStringEvent_parentsAndPosition() throws Exception {
    restrSaveCmd.inject_current(EReorderLiteral.ELEMENT_ID);
    EntityReference parentRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyParentDoc");
    restrSaveCmd.inject_ParentRef(parentRef);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyDoc1");
    expect(wiki.exists(eq(docRef), same(context))).andReturn(true);
    XWikiDocument xdoc = new XWikiDocument(docRef);
    EntityReference oldParentRef = new DocumentReference(context.getDatabase(), "MySpace",
        "OldParentDoc");
    xdoc.setParentReference(oldParentRef);
    BaseObject menuItemObj = new BaseObject();
    menuItemObj.setXClassReference(getMenuItemClassRef());
    menuItemObj.setIntValue("menu_position", 12);
    xdoc.setXObject(0, menuItemObj);
    expect(wiki.getDocument(eq(docRef), same(context))).andReturn(xdoc);
    wiki.saveDocument(same(xdoc), eq("Restructuring"), same(context));
    expectLastCall();
    replayDefault();
    restrSaveCmd.stringEvent("LIN1:MySpace:MySpace.MyDoc1");
    assertEquals("expecting increment afterwards.", new Integer(1),
        restrSaveCmd.getCurrentPos());
    assertEquals("expecting parent reset.", parentRef, xdoc.getParentReference());
    assertEquals("expecting position reset.", 0,
        menuItemObj.getIntValue("menu_position"));
    assertTrue("expecting old parent in dirtyParents.", restrSaveCmd.getDirtyParents(
      ).contains(oldParentRef));
    assertTrue("expecting new parent in dirtyParents.", restrSaveCmd.getDirtyParents(
      ).contains(parentRef));
    assertEquals(2, restrSaveCmd.getDirtyParents().size());
    verifyDefault();
  }

  @Test
  public void testStringEvent_emptyParentForRootElement() throws Exception {
    restrSaveCmd.inject_current(EReorderLiteral.ELEMENT_ID);
    restrSaveCmd.inject_ParentRef(null);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyDoc1");
    expect(wiki.exists(eq(docRef), same(context))).andReturn(true);
    XWikiDocument xdoc = new XWikiDocument(docRef);
    EntityReference oldParentRef = new DocumentReference(context.getDatabase(), "MySpace",
        "OldParentDoc");
    xdoc.setParentReference(oldParentRef);
    BaseObject menuItemObj = new BaseObject();
    menuItemObj.setXClassReference(getMenuItemClassRef());
    menuItemObj.setIntValue("menu_position", 12);
    xdoc.setXObject(0, menuItemObj);
    expect(wiki.getDocument(eq(docRef), same(context))).andReturn(xdoc);
    wiki.saveDocument(same(xdoc), eq("Restructuring"), same(context));
    expectLastCall();
    replayDefault();
    restrSaveCmd.stringEvent("LIN1:MySpace:MySpace.MyDoc1");
    assertEquals("expecting increment afterwards.", new Integer(1),
        restrSaveCmd.getCurrentPos());
    assertNull("expecting parent reset.", xdoc.getParentReference());
    assertEquals("expecting position reset.", 0,
        menuItemObj.getIntValue("menu_position"));
    assertEquals(2, restrSaveCmd.getDirtyParents().size());
    verifyDefault();
  }

  @Test
  public void testStringEvent_noChanges() throws Exception {
    restrSaveCmd.inject_current(EReorderLiteral.ELEMENT_ID);
    EntityReference parentRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyParentDoc");
    restrSaveCmd.inject_ParentRef(parentRef);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace",
        "MyDoc1");
    expect(wiki.exists(eq(docRef), same(context))).andReturn(true);
    XWikiDocument xdoc = new XWikiDocument(docRef);
    xdoc.setParentReference(parentRef);
    BaseObject menuItemObj = new BaseObject();
    menuItemObj.setXClassReference(getMenuItemClassRef());
    menuItemObj.setIntValue("menu_position", 0);
    xdoc.setXObject(0, menuItemObj);
    expect(wiki.getDocument(eq(docRef), same(context))).andReturn(xdoc);
    replayDefault();
    restrSaveCmd.stringEvent("LIN1:MySpace:MySpace.MyDoc1");
    assertEquals("expecting increment afterwards.", new Integer(1),
        restrSaveCmd.getCurrentPos());
    assertEquals("expecting parent reset.", parentRef, xdoc.getParentReference());
    assertEquals("expecting position reset.", 0,
        menuItemObj.getIntValue("menu_position"));
    assertTrue("noChanges --> no parents need to be updated",
        restrSaveCmd.getDirtyParents().isEmpty());
    verifyDefault();
  }

  private INavigationClassConfig getNavClassConfig() {
    return Utils.getComponent(INavigationClassConfig.class);
  }

  private DocumentReference getMenuItemClassRef() {
    return getNavClassConfig().getMenuItemClassRef(context.getDatabase());
  }

}
