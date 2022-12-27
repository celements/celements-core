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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.navigation.INavigationClassConfig;
import com.celements.navigation.TreeNode;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.web.Utils;

public class GetNotMappedMenuItemsForParentCommandTest extends AbstractComponentTest {

  private XWikiContext context;
  private XWiki xwiki;
  private XWikiStoreInterface mockStore;
  private GetNotMappedMenuItemsForParentCommand notMappedItemsCmd;

  @Before
  public void setUp_GetNotMappedMenuItemsForParentCommandTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    registerComponentMock(IModelAccessFacade.class);
    mockStore = createMockAndAddToDefault(XWikiStoreInterface.class);
    expect(xwiki.getStore()).andReturn(mockStore).anyTimes();
    notMappedItemsCmd = new GetNotMappedMenuItemsForParentCommand();
  }

  @Test
  public void testGetCacheKey_space() {
    context.setDatabase("mydatabase");
    assertEquals("mydatabase", notMappedItemsCmd.getWikiCacheKey("mydatabase:MySpace."));
  }

  @Test
  public void testGetCacheKey_fullName() {
    context.setDatabase("mydatabase");
    assertEquals("mydatabase", notMappedItemsCmd.getWikiCacheKey("mydatabase:MySpace2.Doc2"));
  }

  @Test
  public void testGetCacheKey_space_different_db() {
    context.setDatabase("theWiki");
    assertEquals("mydatabase", notMappedItemsCmd.getWikiCacheKey("mydatabase:MySpace."));
  }

  @Test
  public void testGetHQL() {
    String hql = notMappedItemsCmd.getHQL();
    assertTrue("missing doc name restriction [" + hql + "].", hql.matches(
        ".*[where |and ]obj.name=doc.fullName .*"));
    assertTrue("missing classname restriction [" + hql + "].", hql.matches(
        ".*[where |and ]obj.className='Celements2.MenuItem' .*"));
    assertTrue("missing object-property join restriction [" + hql + "].", hql.matches(
        ".*[where |and ]obj.id = pos.id.id .*"));
    assertTrue("missing property name restriction [" + hql + "].", hql.matches(
        ".*[where |and ]and pos.id.name = 'menu_position' .*"));
    assertTrue("missing translation restriction [" + hql + "].", hql.matches(
        ".*[where |and ]doc.translation = 0 .*"));
    assertTrue("expecting no trash restriction [" + hql + "].", hql.matches(
        ".*[where |and ]and doc.space <> 'Trash' .*"));
    assertTrue("missing order by [" + hql + "].", hql.matches(".* order by doc.parent, pos.value"));
  }

  @Test
  public void testGetTreeNodesForParentKey_miss() throws XWikiException {
    context.setDatabase("mydatabase");
    List<Object[]> resultList = Arrays.asList(
        Arrays.<Object>asList("MySpace.MyDoc1", "MySpace", "", 1).toArray(),
        Arrays.<Object>asList("MySpace.MyDoc2", "MySpace", "", 2).toArray());
    expect(mockStore.<Object[]>search(isA(String.class), eq(0), eq(0), same(context)))
        .andReturn(resultList).atLeastOnce();
    DocumentReference myDoc1Ref = new DocumentReference(context.getDatabase(), "MySpace", "MyDoc1");
    DocumentReference myDoc2Ref = new DocumentReference(context.getDatabase(), "MySpace", "MyDoc2");
    List<TreeNode> expectedList = Arrays.asList(new TreeNode(myDoc1Ref, null, 1), new TreeNode(
        myDoc2Ref, null, 2));
    XWikiDocument doc1 = new XWikiDocument(myDoc2Ref);
    doc1.setNew(false);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(eq(myDoc1Ref))).andReturn(doc1)
        .atLeastOnce();
    XWikiDocument doc2 = new XWikiDocument(myDoc2Ref);
    doc2.setNew(false);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(eq(myDoc2Ref))).andReturn(doc2)
        .atLeastOnce();
    replayDefault();
    List<TreeNode> resultTNlist = notMappedItemsCmd.getTreeNodesForParentKey("mydatabase:MySpace.");
    assertEquals(expectedList.size(), resultTNlist.size());
    for (int i = 0; i < expectedList.size(); i++) {
      assertEquals(expectedList.get(i).getDocumentReference(), resultTNlist.get(
          i).getDocumentReference());
      assertEquals(expectedList.get(i).getParentRef(), resultTNlist.get(i).getParentRef());
      assertEquals(expectedList.get(i).getPosition(), resultTNlist.get(i).getPosition());
      assertEquals(expectedList.get(i).getPartName(), resultTNlist.get(i).getPartName());
    }
    verifyDefault();
  }

  @Test
  public void testGetTreeNodesForParentKey_differentParents_miss() throws XWikiException {
    context.setDatabase("mydatabase");
    List<Object[]> resultList = Arrays.asList(
        Arrays.<Object>asList("MySpace.MyDoc1", "MySpace", "", 1).toArray(),
        Arrays.<Object>asList("MySpace.MyDoc2", "MySpace", "MySpace.MyDoc1", 2).toArray());
    expect(mockStore.<Object[]>search(isA(String.class), eq(0), eq(0), same(context)))
        .andReturn(resultList).atLeastOnce();
    DocumentReference myDoc1Ref = new DocumentReference(context.getDatabase(), "MySpace", "MyDoc1");
    DocumentReference myDoc2Ref = new DocumentReference(context.getDatabase(), "MySpace", "MyDoc2");
    List<TreeNode> expectedList = Arrays.asList(new TreeNode(myDoc2Ref, myDoc1Ref, 2));
    XWikiDocument doc2 = new XWikiDocument(myDoc2Ref);
    doc2.setNew(false);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(eq(myDoc2Ref))).andReturn(doc2)
        .atLeastOnce();
    replayDefault();
    List<TreeNode> resultTNlist = notMappedItemsCmd.getTreeNodesForParentKey(
        "mydatabase:MySpace.MyDoc1");
    assertEquals(expectedList.size(), resultTNlist.size());
    for (int i = 0; i < expectedList.size(); i++) {
      assertEquals(expectedList.get(i).getDocumentReference(), resultTNlist.get(
          i).getDocumentReference());
      assertEquals(expectedList.get(i).getParentRef(), resultTNlist.get(i).getParentRef());
      assertEquals(expectedList.get(i).getPosition(), resultTNlist.get(i).getPosition());
      assertEquals(expectedList.get(i).getPartName(), resultTNlist.get(i).getPartName());
    }
    verifyDefault();
  }

  @Test
  public void testGetTreeNodesForParentKey_hit() throws Exception {
    context.setDatabase("mydatabase");
    String searchParentKey = "mydatabase:MySpace.";
    String cacheKey = notMappedItemsCmd.getWikiCacheKey(searchParentKey);
    HashMap<String, List<TreeNode>> mySpaceMap = new HashMap<>();
    List<TreeNode> expectedList = Arrays.asList(new TreeNode(new DocumentReference(
        context.getDatabase(), "MySpace", "MyDoc1"), null, 1), new TreeNode(
            new DocumentReference(context.getDatabase(), "MySpace", "MyDoc2"),
            null, 2));
    mySpaceMap.put(searchParentKey, expectedList);
    notMappedItemsCmd.injectMapForTests(cacheKey, mySpaceMap);
    replayDefault();
    List<TreeNode> resultTNlist = notMappedItemsCmd.getTreeNodesForParentKey(searchParentKey);
    assertEquals(expectedList, resultTNlist);
    verifyDefault();
  }

  @Test
  public void testGetTreeNodesForParentKey_miss_empty() throws XWikiException {
    context.setDatabase("mydatabase");
    List<Object[]> resultList = Arrays.asList(
        Arrays.<Object>asList("MySpace.MyDoc1", "MySpace", "", 1).toArray(),
        Arrays.<Object>asList("MySpace.MyDoc2", "MySpace", "", 2).toArray());
    expect(mockStore.<Object[]>search(isA(String.class), eq(0), eq(0), same(context))).andReturn(
        resultList).atLeastOnce();
    replayDefault();
    List<TreeNode> resultTNlist = notMappedItemsCmd.getTreeNodesForParentKey(
        "mydatabase:MySpace2.");
    assertEquals(Collections.emptyList(), resultTNlist);
    verifyDefault();
  }

  @Test
  public void testGetTreeNodesForParentKey_hit_empty() throws Exception {
    context.setDatabase("mydatabase");
    String searchParentKey = "mydatabase:MySpace.MyDoc1";
    String cacheKey = notMappedItemsCmd.getWikiCacheKey(searchParentKey);
    HashMap<String, List<TreeNode>> mySpaceMap = new HashMap<>();
    mySpaceMap.put("mydatabase:MySpace.", Arrays.asList(new TreeNode(new DocumentReference(
        context.getDatabase(), "MySpace", "MyDoc1"), null, 1), new TreeNode(
            new DocumentReference(
                context.getDatabase(), "MySpace", "MyDoc2"),
            null, 2)));
    notMappedItemsCmd.injectMapForTests(cacheKey, mySpaceMap);
    replayDefault();
    List<TreeNode> resultTNlist = notMappedItemsCmd.getTreeNodesForParentKey(searchParentKey);
    assertNotNull(resultTNlist);
    assertEquals(Collections.emptyList(), resultTNlist);
    verifyDefault();
  }

  @Test
  public void testGetTreeNodesForParentKey_differentDbs() throws Exception {
    context.setDatabase("myTestWiki");
    String searchParentKey = "mydatabase:MySpace.";
    String expectedPartName = "mainPart";
    List<TreeNode> expectedList = Arrays.asList(new TreeNode(new DocumentReference("mydatabase",
        "MySpace", "MyDoc1"), null, 1, expectedPartName), new TreeNode(
            new DocumentReference("mydatabase", "MySpace", "MyDoc2"),
            null, 2, expectedPartName));
    List<Object[]> resultList = Arrays.asList(
        Arrays.<Object>asList("MySpace.MyDoc1", "MySpace", "", 1).toArray(),
        Arrays.<Object>asList("MySpace.MyDoc2", "MySpace", "", 2).toArray());
    expect(mockStore.<Object[]>search(isA(String.class), eq(0), eq(0), same(context)))
        .andReturn(resultList).atLeastOnce();
    DocumentReference myDoc1Ref = new DocumentReference("mydatabase", "MySpace", "MyDoc1");
    XWikiDocument doc1 = new XWikiDocument(myDoc1Ref);
    doc1.setNew(false);
    BaseObject menuItem1 = new BaseObject();
    menuItem1.setXClassReference(getNavClassConfig().getMenuItemClassRef("mydatabase"));
    menuItem1.setStringValue(INavigationClassConfig.PART_NAME_FIELD, expectedPartName);
    doc1.addXObject(menuItem1);
    DocumentReference myDoc2Ref = new DocumentReference("mydatabase", "MySpace", "MyDoc2");
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(eq(myDoc1Ref))).andReturn(doc1)
        .atLeastOnce();
    XWikiDocument doc2 = new XWikiDocument(myDoc2Ref);
    doc2.setNew(false);
    BaseObject menuItem2 = new BaseObject();
    menuItem2.setXClassReference(getNavClassConfig().getMenuItemClassRef("mydatabase"));
    menuItem2.setStringValue(INavigationClassConfig.PART_NAME_FIELD, expectedPartName);
    doc2.addXObject(menuItem2);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(eq(myDoc2Ref))).andReturn(doc2)
        .atLeastOnce();
    replayDefault();
    List<TreeNode> resultTNlist = notMappedItemsCmd.getTreeNodesForParentKey(searchParentKey);
    for (int index = 0; index < resultTNlist.size(); index++) {
      TreeNode testNode = resultTNlist.get(index);
      TreeNode expectedNode = expectedList.get(index);
      assertTreeNoteEquals(expectedNode, testNode);
    }
    assertEquals(expectedList, resultTNlist);
    verifyDefault();
  }

  @Test
  @Deprecated
  public void testGetTreeNodesForParentKey_differentDbs_deprecated() throws Exception {
    context.setDatabase("myTestWiki");
    String searchParentKey = "mydatabase:MySpace.";
    DocumentReference myDoc1Ref = new DocumentReference("mydatabase", "MySpace", "MyDoc1");
    DocumentReference myDoc2Ref = new DocumentReference("mydatabase", "MySpace", "MyDoc2");
    XWikiDocument doc1 = new XWikiDocument(myDoc1Ref);
    doc1.setNew(false);
    String expectedPartName = "mainPart";
    BaseObject menuItem1 = new BaseObject();
    menuItem1.setXClassReference(getNavClassConfig().getMenuItemClassRef("mydatabase"));
    menuItem1.setStringValue(INavigationClassConfig.PART_NAME_FIELD, expectedPartName);
    doc1.addXObject(menuItem1);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(eq(myDoc1Ref))).andReturn(doc1)
        .atLeastOnce();
    XWikiDocument doc2 = new XWikiDocument(myDoc2Ref);
    doc2.setNew(false);
    BaseObject menuItem2 = new BaseObject();
    menuItem2.setXClassReference(getNavClassConfig().getMenuItemClassRef("mydatabase"));
    menuItem2.setStringValue(INavigationClassConfig.PART_NAME_FIELD, expectedPartName);
    doc2.addXObject(menuItem2);
    expect(getMock(IModelAccessFacade.class).getOrCreateDocument(eq(myDoc2Ref))).andReturn(doc2)
        .atLeastOnce();
    List<TreeNode> expectedList = Arrays.asList(new TreeNode(myDoc1Ref, null, 1, expectedPartName),
        new TreeNode(myDoc2Ref, null, 2, expectedPartName));
    List<Object[]> resultList = Arrays.asList(
        Arrays.<Object>asList("MySpace.MyDoc1", "MySpace", "", 1).toArray(),
        Arrays.<Object>asList("MySpace.MyDoc2", "MySpace", "", 2).toArray());
    expect(mockStore.<Object[]>search(isA(String.class), eq(0), eq(0), same(context)))
        .andReturn(resultList).atLeastOnce();
    replayDefault();
    List<TreeNode> resultTNlist = notMappedItemsCmd.getTreeNodesForParentKey(searchParentKey,
        context);
    for (int index = 0; index < resultTNlist.size(); index++) {
      TreeNode testNode = resultTNlist.get(index);
      TreeNode expectedNode = expectedList.get(index);
      assertTreeNoteEquals(expectedNode, testNode);
    }
    assertEquals(expectedList, resultTNlist);
    verifyDefault();
  }

  @Test
  public void testGetParentKey_FullName() {
    context.setDatabase("mydatabase");
    replayDefault();
    assertEquals("mydatabase:Full.Name", notMappedItemsCmd.getParentKey(context.getDatabase(),
        "Full.Name", "Space"));
    verifyDefault();
  }

  @Test
  public void testGetParentKey_Name() {
    context.setDatabase("mydatabase");
    replayDefault();
    assertEquals("mydatabase:Space.Name", notMappedItemsCmd.getParentKey(context.getDatabase(),
        "Name", "Space"));
    verifyDefault();
  }

  @Test
  public void testGetParentKey_Name_differentdb() {
    context.setDatabase("myTestWiki");
    replayDefault();
    assertEquals("mydatabase:Space.Name", notMappedItemsCmd.getParentKey("mydatabase", "Name",
        "Space"));
    verifyDefault();
  }

  @Test
  public void testFlushMenuItemCache() {
    String dbName = "testDatabase";
    context.setDatabase(dbName);
    notMappedItemsCmd.injectMapForTests(dbName, new HashMap<String, List<TreeNode>>());
    replayDefault();
    notMappedItemsCmd.flushMenuItemCache(context);
    assertNull(notMappedItemsCmd.internalGetMenuItemsForWiki(dbName));
    verifyDefault();
  }

  @Test
  @Deprecated
  public void testGetTreeNodesForParentKey_miss_IllegalArgumentExp_emptySpace_deprecated()
      throws XWikiException {
    context.setDatabase("mydatabase");
    List<Object[]> resultList = Arrays.asList(
        Arrays.<Object>asList(".MyDoc1", "", "", 1).toArray(),
        Arrays.<Object>asList("MySpace.MyDoc2", "MySpace", "", 2).toArray());
    expect(mockStore.<Object[]>search(isA(String.class), eq(0), eq(0), same(context))).andReturn(
        resultList).atLeastOnce();
    List<TreeNode> expectedList = Arrays.asList(new TreeNode(new DocumentReference(
        context.getDatabase(), "MySpace", "MyDoc2"), null, 2));
    replayDefault();
    List<TreeNode> resultTNlist = notMappedItemsCmd.getTreeNodesForParentKey("mydatabase:MySpace.",
        context);
    assertEquals(expectedList.size(), resultTNlist.size());
    for (int i = 0; i < expectedList.size(); i++) {
      assertEquals(expectedList.get(i).getDocumentReference(), resultTNlist.get(
          i).getDocumentReference());
    }
    verifyDefault();
  }

  @Test
  public void testGetTreeNodesForParentKey_miss_IllegalArgumentExp_emptySpace()
      throws XWikiException {
    context.setDatabase("mydatabase");
    List<Object[]> resultList = Arrays.asList(
        Arrays.<Object>asList(".MyDoc1", "", "", 1).toArray(),
        Arrays.<Object>asList("MySpace.MyDoc2", "MySpace", "", 2).toArray());
    expect(mockStore.<Object[]>search(isA(String.class), eq(0), eq(0), same(context))).andReturn(
        resultList).atLeastOnce();
    List<TreeNode> expectedList = Arrays.asList(new TreeNode(new DocumentReference(
        context.getDatabase(), "MySpace", "MyDoc2"), null, 2));
    replayDefault();
    List<TreeNode> resultTNlist = notMappedItemsCmd.getTreeNodesForParentKey("mydatabase:MySpace.");
    assertEquals(expectedList.size(), resultTNlist.size());
    for (int i = 0; i < expectedList.size(); i++) {
      assertEquals(expectedList.get(i).getDocumentReference(), resultTNlist.get(
          i).getDocumentReference());
    }
    verifyDefault();
  }

  @Test
  public void testExecuteSearch_fixDbForSearch() throws Exception {
    context.setDatabase("myTestWiki");
    List<Object[]> resultList = Arrays.asList(
        Arrays.<Object>asList(".MyDoc1", "", "", 1).toArray(),
        Arrays.<Object>asList("MySpace.MyDoc2", "MySpace", "", 2).toArray());
    expect(mockStore.<Object[]>search(isA(String.class), eq(0), eq(0), same(context))).andReturn(
        resultList).atLeastOnce();
    replayDefault();
    List<Object[]> result = notMappedItemsCmd.executeSearch("mydatabase");
    assertEquals("expect database being adjusted.", "mydatabase", context.getDatabase());
    assertEquals(resultList, result);
    verifyDefault();
  }

  @Test
  public void testGetFromDBForParentKey_preserve_db() throws Exception {
    context.setDatabase("myTestWiki");
    List<Object[]> resultList = Arrays.asList(
        Arrays.<Object>asList("MySpace.MyDoc1", "MySpace", "", 1).toArray(),
        Arrays.<Object>asList("MySpace.MyDoc2", "MySpace", "", 2).toArray());
    expect(mockStore.<Object[]>search(isA(String.class), eq(0), eq(0), same(context))).andReturn(
        resultList).atLeastOnce();
    replayDefault();
    List<Object[]> result = notMappedItemsCmd.getFromDBForParentKey("mydatabase:MySpace.");
    assertEquals("expect database being preserved.", "myTestWiki", context.getDatabase());
    assertEquals(resultList, result);
    verifyDefault();
  }

  private INavigationClassConfig getNavClassConfig() {
    return Utils.getComponent(INavigationClassConfig.class);
  }

  private void assertTreeNoteEquals(TreeNode expectedNode, TreeNode testNode) {
    assertEquals("wrong parentRef for [" + expectedNode.getDocumentReference().getName() + "]",
        expectedNode.getParentRef(), testNode.getParentRef());
    assertEquals("wrong documentReference for [" + expectedNode.getDocumentReference().getName()
        + "]", expectedNode.getDocumentReference(), testNode.getDocumentReference());
    assertEquals("wrong position for [" + expectedNode.getDocumentReference().getName() + "]",
        expectedNode.getPosition(), testNode.getPosition());
    assertEquals("wrong partName for [" + expectedNode.getDocumentReference().getName() + "]",
        expectedNode.getPartName(), testNode.getPartName());
    assertEquals("expected [" + expectedNode.getDocumentReference() + "] but found ["
        + testNode.getDocumentReference() + "].", expectedNode, testNode);
  }

}
