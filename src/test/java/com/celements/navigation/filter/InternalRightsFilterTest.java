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
package com.celements.navigation.filter;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.navigation.TreeNode;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiRightService;

public class InternalRightsFilterTest extends AbstractComponentTest {

  private InternalRightsFilter filter;
  private XWikiContext context;
  private XWiki wiki;
  private XWikiRightService rightsService;

  @Before
  public void setUp_InternalRightsFilterTest() throws Exception {
    filter = new InternalRightsFilter();
    context = getContext();
    wiki = getWikiMock();
    rightsService = createDefaultMock(XWikiRightService.class);
    expect(wiki.getRightService()).andReturn(rightsService);
  }

  @Test
  public void testGetMenuPart_NotNull() {
    filter = new InternalRightsFilter();
    assertNotNull("Must not be null (leads to NPE in checkMenuPart!).", filter.getMenuPart());
  }

  @Test
  public void testSetMenuPart() {
    String expectedMenuPart = "mainPart";
    filter.setMenuPart(expectedMenuPart);
    assertNotNull(filter.getMenuPart());
    assertEquals(expectedMenuPart, filter.getMenuPart());
  }

  @Test
  public void testSetMenuPart_null() {
    String expectedMenuPart = "";
    filter.setMenuPart(null);
    assertNotNull(filter.getMenuPart());
    assertEquals(expectedMenuPart, filter.getMenuPart());
  }

  @Test
  public void testConvertObject() {
    BaseObject baseObj = new BaseObject();
    assertSame(baseObj, filter.convertObject(baseObj, context));
  }

  @Test
  @Deprecated
  public void testIncludeMenuItem_noViewRights() throws Exception {
    String docFullName = "MySpace.MyDoc";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace", "MyDoc");
    BaseObject baseObj = new BaseObject();
    baseObj.setDocumentReference(docRef);
    expect(rightsService.hasAccessLevel(eq("view"), eq(context.getUser()), eq(context.getDatabase()
        + ":" + docFullName), same(context))).andReturn(false);
    replayDefault();
    assertFalse(filter.includeMenuItem(baseObj, context));
    verifyDefault();
  }

  @Test
  @Deprecated
  public void testIncludeMenuItem_hasViewRights_noMenuPart() throws Exception {
    String docFullName = "MySpace.MyDoc";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace", "MyDoc");
    BaseObject baseObj = new BaseObject();
    baseObj.setDocumentReference(docRef);
    filter.setMenuPart("");
    expect(rightsService.hasAccessLevel(eq("view"), eq(context.getUser()), eq(context.getDatabase()
        + ":" + docFullName), same(context))).andReturn(true);
    replayDefault();
    assertTrue(filter.includeMenuItem(baseObj, context));
    verifyDefault();
  }

  @Test
  @Deprecated
  public void testIncludeMenuItem_hasViewRights_wrongMenuPart() throws Exception {
    String docFullName = "MySpace.MyDoc";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace", "MyDoc");
    BaseObject baseObj = new BaseObject();
    baseObj.setDocumentReference(docRef);
    baseObj.setStringValue("part_name", "anotherPart");
    filter.setMenuPart("mainPart");
    expect(rightsService.hasAccessLevel(eq("view"), eq(context.getUser()), eq(context.getDatabase()
        + ":" + docFullName), same(context))).andReturn(true);
    replayDefault();
    assertFalse(filter.includeMenuItem(baseObj, context));
    verifyDefault();
  }

  @Test
  @Deprecated
  public void testIncludeMenuItem_hasViewRights_matchingMenuPart() throws Exception {
    String docFullName = "MySpace.MyDoc";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace", "MyDoc");
    BaseObject baseObj = new BaseObject();
    baseObj.setDocumentReference(docRef);
    baseObj.setStringValue("part_name", "mainPart");
    filter.setMenuPart("mainPart");
    expect(rightsService.hasAccessLevel(eq("view"), eq(context.getUser()), eq(context.getDatabase()
        + ":" + docFullName), same(context))).andReturn(true);
    replayDefault();
    assertTrue(filter.includeMenuItem(baseObj, context));
    verifyDefault();
  }

  @Test
  @Deprecated
  public void testIncludeMenuItem_Exception() throws Exception {
    String docFullName = "MySpace.MyDoc";
    DocumentReference docRef = new DocumentReference(context.getDatabase(), "MySpace", "MyDoc");
    BaseObject baseObj = new BaseObject();
    baseObj.setDocumentReference(docRef);
    baseObj.setStringValue("part_name", "mainPart");
    filter.setMenuPart("mainPart");
    expect(rightsService.hasAccessLevel(eq("view"), eq(context.getUser()), eq(context.getDatabase()
        + ":" + docFullName), same(context))).andThrow(new XWikiException());
    replayDefault();
    assertFalse(filter.includeMenuItem(baseObj, context));
    verifyDefault();
  }

  @Test
  public void testIncludeTreeNode_noViewRights() throws Exception {
    String docFullName = "MySpace.MyDoc";
    TreeNode node = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace", "MyDoc"),
        null, 0);
    expect(rightsService.hasAccessLevel(eq("view"), eq(context.getUser()), eq(context.getDatabase()
        + ":" + docFullName), same(context))).andReturn(false);
    replayDefault();
    assertFalse(filter.includeTreeNode(node, context));
    verifyDefault();
  }

  @Test
  public void testIncludeTreeNode_hasViewRights_noMenuPart() throws Exception {
    String docFullName = "MySpace.MyDoc";
    TreeNode node = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace", "MyDoc"),
        null, 0);
    filter.setMenuPart("");
    expect(rightsService.hasAccessLevel(eq("view"), eq(context.getUser()), eq(context.getDatabase()
        + ":" + docFullName), same(context))).andReturn(true);
    replayDefault();
    assertTrue(filter.includeTreeNode(node, context));
    verifyDefault();
  }

  @Test
  public void testIncludeTreeNode_hasViewRights_wrongMenuPart() throws Exception {
    String docFullName = "MySpace.MyDoc";
    TreeNode node = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace", "MyDoc"),
        null, 0, "anotherPart");
    filter.setMenuPart("mainPart");
    expect(rightsService.hasAccessLevel(eq("view"), eq(context.getUser()), eq(context.getDatabase()
        + ":" + docFullName), same(context))).andReturn(true);
    replayDefault();
    assertFalse(filter.includeTreeNode(node, context));
    verifyDefault();
  }

  @Test
  public void testIncludeTreeNode_hasViewRights_matchingMenuPart() throws Exception {
    String docFullName = "MySpace.MyDoc";
    TreeNode node = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace", "MyDoc"),
        null, 0, "mainPart");
    filter.setMenuPart("mainPart");
    expect(rightsService.hasAccessLevel(eq("view"), eq(context.getUser()), eq(context.getDatabase()
        + ":" + docFullName), same(context))).andReturn(true);
    replayDefault();
    assertTrue(filter.includeTreeNode(node, context));
    verifyDefault();
  }

  @Test
  public void testIncludeTreeNode_Exception() throws Exception {
    String docFullName = "MySpace.MyDoc";
    TreeNode node = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace", "MyDoc"),
        null, 0, "mainPart");
    filter.setMenuPart("mainPart");
    expect(rightsService.hasAccessLevel(eq("view"), eq(context.getUser()), eq(context.getDatabase()
        + ":" + docFullName), same(context))).andThrow(new XWikiException());
    replayDefault();
    assertFalse(filter.includeTreeNode(node, context));
    verifyDefault();
  }

}
