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

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.store.XWikiStoreInterface;

public class GetMappedMenuItemsForParentCommandTest extends AbstractComponentTest {

  private GetMappedMenuItemsForParentCommand getMenuItemsCmd;
  private XWikiContext context;
  private XWikiStoreInterface mockStore;

  @Before
  public void setUp_GetMappedMenuItemsForParentCommandTest() throws Exception {
    context = getContext();
    getMenuItemsCmd = new GetMappedMenuItemsForParentCommand();
    mockStore = getStoreMock();
  }

  @Test
  public void testGetTreeNodesForParentKey_emptyResult_emptyKey_active() throws XWikiException {
    getMenuItemsCmd.setIsActive(true);
    expect(mockStore.search(isA(String.class), eq(0), eq(0), (List<?>) anyObject(), same(
        context))).andReturn(new ArrayList<Object>()).anyTimes();
    replayDefault();
    assertNotNull(getMenuItemsCmd.getTreeNodesForParentKey(""));
    assertEquals(0, getMenuItemsCmd.getTreeNodesForParentKey("").size());
    verifyDefault();
  }

  @Test
  public void testGetTreeNodesForParentKey_emptyResult_emptyKey_inactive() throws XWikiException {
    getMenuItemsCmd.setIsActive(false);
    expect(mockStore.search(isA(String.class), eq(0), eq(0), (List<?>) anyObject(), same(
        context))).andReturn(new ArrayList<Object>()).anyTimes();
    replayDefault();
    assertNotNull(getMenuItemsCmd.getTreeNodesForParentKey(""));
    assertEquals(0, getMenuItemsCmd.getTreeNodesForParentKey("").size());
    verifyDefault();
  }

  @Test
  public void testGetTreeNodesForParentKey_notActive() throws XWikiException {
    getMenuItemsCmd.setIsActive(false);
    assertEquals(0, getMenuItemsCmd.getTreeNodesForParentKey("Space.Doc").size());
  }

  @Test
  public void testGetHQL() {
    assertTrue(getMenuItemsCmd.getHQL().contains(" and doc.translation='0'"));
    assertTrue(getMenuItemsCmd.getHQL().contains(" and obj.id=menuitem.id"));
    assertTrue(getMenuItemsCmd.getHQL().endsWith("order by menuitem.menu_position"));
  }

  // TODO getTreeNodesForParentKey not yet implemented correctly
  // @Test
  // public void testGetTreeNodesForParentKey_differentParents_miss() throws XWikiException {
  // getMenuItemsCmd.setIsActive(true);
  // context.setDatabase("mydatabase");
  // // doc.fullName, doc.space, doc.parent, menuitem.menu_position, menuitem.part_name
  // List<Object[]> resultList = Arrays.asList(Arrays.<Object>asList("MySpace.MyDoc1", "MySpace",
  // "",
  // 1, "mainPart").toArray(), Arrays.<Object>asList("MySpace.MyDoc2", "MySpace",
  // "MySpace.MyDoc1", 2, "mainPart").toArray());
  // expect(mockStore.<Object[]>search(isA(String.class), eq(0), eq(0), same(context))).andReturn(
  // resultList).atLeastOnce();
  // DocumentReference myDoc1Ref = new DocumentReference(context.getDatabase(), "MySpace",
  // "MyDoc1");
  // DocumentReference myDoc2Ref = new DocumentReference(context.getDatabase(), "MySpace",
  // "MyDoc2");
  // List<TreeNode> expectedList = Arrays.asList(new TreeNode(myDoc2Ref, myDoc1Ref, 2));
  // expect(xwiki.exists(eq(myDoc1Ref), same(context))).andReturn(true).atLeastOnce();
  // expect(xwiki.getDocument(eq(myDoc1Ref), same(context))).andReturn(new XWikiDocument(
  // myDoc1Ref)).atLeastOnce();
  // expect(xwiki.exists(eq(myDoc2Ref), same(context))).andReturn(true).atLeastOnce();
  // expect(xwiki.getDocument(eq(myDoc2Ref), same(context))).andReturn(new XWikiDocument(
  // myDoc2Ref)).atLeastOnce();
  // replayDefault();
  // List<TreeNode> resultTNlist = getMenuItemsCmd.getTreeNodesForParentKey(
  // "mydatabase:MySpace.MyDoc1");
  // assertEquals(expectedList.size(), resultTNlist.size());
  // for (int i = 0; i < expectedList.size(); i++) {
  // assertEquals(expectedList.get(i).getDocumentReference(), resultTNlist.get(
  // i).getDocumentReference());
  // assertEquals(expectedList.get(i).getParentRef(), resultTNlist.get(i).getParentRef());
  // assertEquals(expectedList.get(i).getPosition(), resultTNlist.get(i).getPosition());
  // assertEquals(expectedList.get(i).getPartName(), resultTNlist.get(i).getPartName());
  // }
  // verifyDefault();
  // }

}
