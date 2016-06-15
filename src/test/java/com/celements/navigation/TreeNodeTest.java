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
package com.celements.navigation;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.XWikiContext;

public class TreeNodeTest extends AbstractComponentTest {

  private DocumentReference docRef;
  private XWikiContext context;
  private TreeNode treeNode;
  private String parent;

  @Before
  public void setUp_TreeNodeTest() throws Exception {
    context = getContext();
    docRef = new DocumentReference(context.getDatabase(), "MySpace", "myPage");
    parent = "";
    treeNode = new TreeNode(docRef, parent, 1);
  }

  @Test
  public void test_ConstructorParentSpaceRef_partName_null() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), new SpaceReference("MySpace", new WikiReference(context.getDatabase())), null,
        1);
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
    assertEquals("", treeNodeTest.getPartName(getContext()));
    verifyDefault();
  }

  @Test
  public void test_ConstructorParentSpaceRef_partName_empty() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), new SpaceReference("MySpace", new WikiReference(context.getDatabase())), "", 1);
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
    assertEquals("", treeNodeTest.getPartName(getContext()));
    verifyDefault();
  }

  @Test
  public void test_ConstructorParentSpaceRef_partName() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), new SpaceReference("MySpace", new WikiReference(context.getDatabase())),
        "mainNav", 1);
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
    assertEquals("mainNav", treeNodeTest.getPartName(getContext()));
    verifyDefault();
  }

  @Test
  public void testEquals() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), "", 1);
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
    verifyDefault();
  }

  @Test
  public void testEquals_not_getClass() {
    TreeNode treeNodeTest = new TestTreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), "", 1);
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
    verifyDefault();
  }

  @Test
  public void testEquals_position_notSameInteger() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), "", new Integer(1));
    TreeNode treeNodeTest2 = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), "", new Integer(1));
    replayDefault();
    assertTrue(treeNodeTest2.equals(treeNodeTest));
    verifyDefault();
  }

  @Test
  public void testEquals_null() {
    replayDefault();
    assertFalse(treeNode.equals(null));
    verifyDefault();
  }

  @Test
  public void testHash() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), "", 1);
    replayDefault();
    assertEquals(treeNodeTest.hashCode(), treeNode.hashCode());
    verifyDefault();
  }

  @Test
  public void testHash_null_position() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), "", null);
    TreeNode treeNodeTest2 = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), "", null);
    replayDefault();
    assertEquals(treeNodeTest.hashCode(), treeNodeTest2.hashCode());
    verifyDefault();
  }

  @Test
  public void testEquals_parentSpaceRefConstructor() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), new SpaceReference("MySpace", new WikiReference(context.getDatabase())), "", 1);
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
    verifyDefault();
  }

  @Test
  public void testHash_parentSpaceRefConstructor() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), new SpaceReference("MySpace", new WikiReference(context.getDatabase())), "", 1);
    replayDefault();
    assertEquals(treeNodeTest.hashCode(), treeNode.hashCode());
    verifyDefault();
  }

  @Test
  public void testGetParentRef_spaceRef() {
    SpaceReference parentSpaceRef = new SpaceReference("MySpace", new WikiReference(
        context.getDatabase()));
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), parentSpaceRef, "", 1);
    replayDefault();
    assertEquals(parentSpaceRef, treeNodeTest.getParentRef());
    verifyDefault();
  }

  @Test
  public void testGetParentRef_docRef() {
    DocumentReference parentDocRef = new DocumentReference(context.getDatabase(), "myParentPage",
        "MySpace");
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), parentDocRef, 1);
    replayDefault();
    assertEquals(parentDocRef, treeNodeTest.getParentRef());
    verifyDefault();
  }

  @Test
  public void testEquals_parentSpaceRefConstructorWithPartName() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), new SpaceReference("MySpace", new WikiReference(context.getDatabase())),
        "mainNav", 1);
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
    verifyDefault();
  }

  @Test
  public void testHash_parentSpaceRefConstructorWithPartName() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), new SpaceReference("MySpace", new WikiReference(context.getDatabase())),
        "mainNav", 1);
    replayDefault();
    assertEquals(treeNodeTest.hashCode(), treeNode.hashCode());
    verifyDefault();
  }

  @Test
  public void testEquals_parentDocRefConstructor() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), new DocumentReference(context.getDatabase(), "myParentPage", "MySpace"), 1);
    TreeNode treeNode2 = new TreeNode(docRef, "MySpace.myParentPage", 1);
    replayDefault();
    assertTrue(treeNode2.equals(treeNodeTest));
    verifyDefault();
  }

  @Test
  public void testHash_parentDocRefConstructor() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), new DocumentReference(context.getDatabase(), "myParentPage", "MySpace"), 1);
    TreeNode treeNode2 = new TreeNode(docRef, "MySpace.myParentPage", 1);
    replayDefault();
    assertEquals(treeNodeTest.hashCode(), treeNode2.hashCode());
    verifyDefault();
  }

  private class TestTreeNode extends TreeNode {

    public TestTreeNode(DocumentReference docRef, String parent, Integer position) {
      super(docRef, parent, position);
    }

  }
}
