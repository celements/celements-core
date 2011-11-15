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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.navigation.TreeNode;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.store.XWikiStoreInterface;

public class GetNotMappedMenuItemsForParentCommandTest
  extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki wiki;
  private XWikiStoreInterface mockStore;
  private GetNotMappedMenuItemsForParentCommand notMappedItemsCmd;

  @Before
  public void setUp_GetNotMappedMenuItemsForParentCommandTest() throws Exception {
    context = getContext();
    wiki = createMock(XWiki.class);
    context.setWiki(wiki);
    mockStore = createMock(XWikiStoreInterface.class);
    expect(wiki.getStore()).andReturn(mockStore).anyTimes();
    notMappedItemsCmd = new GetNotMappedMenuItemsForParentCommand();
  }

  @Test
  public void testGetCacheKey_space() {
    context.setDatabase("mydatabase");
    assertEquals("mydatabase", notMappedItemsCmd.getCacheKey(
        "mydatabase:MySpace.", context));
  }

  @Test
  public void testGetCacheKey_fullName() {
    context.setDatabase("mydatabase");
    assertEquals("mydatabase", notMappedItemsCmd.getCacheKey(
        "mydatabase:MySpace2.Doc2", context));
  }

  @Test
  public void testGetHQL() {
    String hql = notMappedItemsCmd.getHQL();
    assertTrue("missing doc name restriction [" + hql  + "].",
        hql.matches(".*[where |and ]obj.name=doc.fullName .*"));
    assertTrue("missing classname restriction [" + hql  + "].",
        hql.matches(".*[where |and ]obj.className='Celements2.MenuItem' .*"));
    assertTrue("missing object-property join restriction [" + hql  + "].",
        hql.matches(".*[where |and ]obj.id = pos.id.id .*"));
    assertTrue("missing property name restriction [" + hql  + "].",
        hql.matches(".*[where |and ]and pos.id.name = 'menu_position' .*"));
    assertTrue("missing translation restriction [" + hql  + "].",
        hql.matches(".*[where |and ]doc.translation = 0 .*"));
    assertTrue("expecting no trash restriction [" + hql  + "].",
        hql.matches(".*[where |and ]and doc.space <> 'Trash' .*"));
    assertTrue("missing order by [" + hql  + "].",
        hql.matches(".* order by doc.space, doc.parent, pos.value"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetTreeNodesForParentKey_miss() throws XWikiException {
    context.setDatabase("mydatabase");
    List resultList = Arrays.asList(Arrays.asList("MySpace.MyDoc1", "MySpace", "", 1
        ).toArray(), Arrays.asList("MySpace.MyDoc2", "MySpace", "", 2).toArray());
    expect(mockStore.search(isA(String.class), eq(0), eq(0), same(context))).andReturn(
        resultList).atLeastOnce();
    List<TreeNode> expectedList = Arrays.asList(new TreeNode(new DocumentReference(
        context.getDatabase(), "MySpace", "MyDoc1"), "", 1),
        new TreeNode(new DocumentReference(context.getDatabase(), "MySpace", "MyDoc2"),
            "", 2));
    replay(wiki, mockStore);
    List<TreeNode> resultTNlist = notMappedItemsCmd.getTreeNodesForParentKey(
        "mydatabase:MySpace.", context);
    assertEquals(expectedList.size(), resultTNlist.size());
    for (int i = 0 ; i < expectedList.size(); i++) {
      assertEquals(expectedList.get(i).getDocumentReference(), resultTNlist.get(i
          ).getDocumentReference());
    }
    verify(wiki, mockStore);
  }

  @Test
  public void testGetTreeNodesForParentKey_hit() throws Exception {
    context.setDatabase("mydatabase");
    String searchParentKey = "mydatabase:MySpace.";
    String cacheKey = notMappedItemsCmd.getCacheKey(searchParentKey, context);
    HashMap<String, List<TreeNode>> mySpaceMap = new HashMap<String, List<TreeNode>>();
    List<TreeNode> expectedList = Arrays.asList(new TreeNode(new DocumentReference(
        context.getDatabase(), "MySpace", "MyDoc1"), "", 1),
        new TreeNode(new DocumentReference(context.getDatabase(), "MySpace", "MyDoc2"),
            "", 2));
    mySpaceMap.put(searchParentKey, expectedList);
    notMappedItemsCmd.getMenuItemsCache().put(cacheKey, mySpaceMap);
    replay(wiki, mockStore);
    List<TreeNode> resultTNlist = notMappedItemsCmd.getTreeNodesForParentKey(
        searchParentKey, context);
    assertEquals(expectedList, resultTNlist);
    verify(wiki, mockStore);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetTreeNodesForParentKey_miss_empty() throws XWikiException {
    context.setDatabase("mydatabase");
    List resultList = Arrays.asList(Arrays.asList("MySpace.MyDoc1", "MySpace", "", 1
        ).toArray(), Arrays.asList("MySpace.MyDoc2", "MySpace", "", 2).toArray());
    expect(mockStore.search(isA(String.class), eq(0), eq(0), same(context))).andReturn(
        resultList).atLeastOnce();
    replay(wiki, mockStore);
    List<TreeNode> resultTNlist = notMappedItemsCmd.getTreeNodesForParentKey(
        "mydatabase:MySpace2.", context);
    assertEquals(Collections.emptyList(), resultTNlist);
    verify(wiki, mockStore);
  }

  @Test
  public void testGetTreeNodesForParentKey_hit_empty() throws Exception {
    context.setDatabase("mydatabase");
    String searchParentKey = "mydatabase:MySpace.MyDoc1";
    String cacheKey = notMappedItemsCmd.getCacheKey(searchParentKey, context);
    HashMap<String, List<TreeNode>> mySpaceMap = new HashMap<String, List<TreeNode>>();
    mySpaceMap.put("mydatabase:MySpace.", Arrays.asList(
        new TreeNode(new DocumentReference(context.getDatabase(), "MySpace", "MyDoc1"),
            "", 1), new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
                "MyDoc2"), "", 2)));
    notMappedItemsCmd.getMenuItemsCache().put(cacheKey, mySpaceMap);
    replay(wiki, mockStore);
    List<TreeNode> resultTNlist = notMappedItemsCmd.getTreeNodesForParentKey(
        searchParentKey, context);
    assertEquals(Collections.emptyList(), resultTNlist);
    verify(wiki, mockStore);
  }

  @Test
  public void testGetParentKey_FullName() {
    context.setDatabase("mydatabase");
    replay(wiki, mockStore);
    assertEquals("mydatabase:Full.Name", notMappedItemsCmd.getParentKey("Full.Name",
        "Space", context));
    verify(wiki, mockStore);
  }

  @Test
  public void testGetParentKey_Name() {
    context.setDatabase("mydatabase");
    replay(wiki, mockStore);
    assertEquals("mydatabase:Space.Name", notMappedItemsCmd.getParentKey("Name", "Space",
        context));
    verify(wiki, mockStore);
  }

  @Test
  public void testFlushMenuItemCache() {
    String dbName = "testDatabase";
    context.setDatabase(dbName);
    notMappedItemsCmd.getMenuItemsCache().put(dbName,
        new HashMap<String, List<TreeNode>>());
    replay(wiki, mockStore);
    notMappedItemsCmd.flushMenuItemCache(context);
    assertFalse(notMappedItemsCmd.getMenuItemsCache().containsKey(dbName));
    verify(wiki, mockStore);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testGetTreeNodesForParentKey_miss_IllegalArgumentExp_emptySpace(
      ) throws XWikiException {
    context.setDatabase("mydatabase");
    List resultList = Arrays.asList(Arrays.asList(".MyDoc1", "", "", 1
        ).toArray(), Arrays.asList("MySpace.MyDoc2", "MySpace", "", 2).toArray());
    expect(mockStore.search(isA(String.class), eq(0), eq(0), same(context))).andReturn(
        resultList).atLeastOnce();
    List<TreeNode> expectedList = Arrays.asList(new TreeNode(new DocumentReference(
        context.getDatabase(), "MySpace", "MyDoc2"), "", 2));
    replay(wiki, mockStore);
    List<TreeNode> resultTNlist = notMappedItemsCmd.getTreeNodesForParentKey(
        "mydatabase:MySpace.", context);
    assertEquals(expectedList.size(), resultTNlist.size());
    for (int i = 0 ; i < expectedList.size(); i++) {
      assertEquals(expectedList.get(i).getDocumentReference(), resultTNlist.get(i
          ).getDocumentReference());
    }
    verify(wiki, mockStore);
  }

}
