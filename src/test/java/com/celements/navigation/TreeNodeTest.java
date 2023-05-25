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
import static org.easymock.EasyMock.*;
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

  @Before
  public void setUp_TreeNodeTest() throws Exception {
    context = getContext();
    docRef = new DocumentReference(context.getDatabase(), "MySpace", "myPage");
    treeNode = new TreeNode(docRef, null, 1);
  }

  @Test
  public void test_ConstructorParentSpaceRef_partName_null() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), new SpaceReference("MySpace", new WikiReference(context.getDatabase())), 1,
        (String) null);
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
    assertEquals("", treeNodeTest.getPartName());
    verifyDefault();
  }

  @Test
  public void test_ConstructorParentSpaceRef_partName_empty() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), new SpaceReference("MySpace", new WikiReference(context.getDatabase())), 1, "");
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
    assertEquals("", treeNodeTest.getPartName());
    verifyDefault();
  }

  @Test
  public void test_ConstructorParentSpaceRef_strategy() {
    PartNameGetter partNameGetterMock = createDefaultMock(PartNameGetter.class);
    DocumentReference docRef2 = new DocumentReference(context.getDatabase(), "MySpace", "myPage");
    TreeNode treeNodeTest = new TreeNode(docRef2, new SpaceReference("MySpace", new WikiReference(
        context.getDatabase())), 1, partNameGetterMock);
    expect(partNameGetterMock.getPartName(eq(docRef2))).andReturn("mainPart").once();
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
    assertEquals("expecting a call to getPartName", "mainPart", treeNodeTest.getPartName());
    assertEquals("expecting only one call to getPartName", "mainPart", treeNodeTest.getPartName());
    verifyDefault();
  }

  @Test
  public void test_ConstructorParentSpaceRef_strategy_null() {
    PartNameGetter partNameGetterMock = createDefaultMock(PartNameGetter.class);
    DocumentReference docRef2 = new DocumentReference(context.getDatabase(), "MySpace", "myPage");
    TreeNode treeNodeTest = new TreeNode(docRef2, new SpaceReference("MySpace", new WikiReference(
        context.getDatabase())), 1, partNameGetterMock);
    expect(partNameGetterMock.getPartName(eq(docRef2))).andReturn((String) null).once();
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
    assertEquals("expecting a call to getPartName", "", treeNodeTest.getPartName());
    assertEquals("expecting only one call to getPartName", "", treeNodeTest.getPartName());
    verifyDefault();
  }

  @Test
  public void test_ConstructorParentSpaceRef_strategy_lazy() {
    PartNameGetter partNameGetterMock = createDefaultMock(PartNameGetter.class);
    DocumentReference docRef2 = new DocumentReference(context.getDatabase(), "MySpace", "myPage");
    TreeNode treeNodeTest = new TreeNode(docRef2, new SpaceReference("MySpace", new WikiReference(
        context.getDatabase())), 1, partNameGetterMock);
    expect(partNameGetterMock.getPartName(eq(docRef2))).andThrow(new RuntimeException(
        "partNameGetter must be called lazily for"
            + " NotMappedMenuItems for performance reasons.")).anyTimes();
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
    verifyDefault();
  }

  @Test
  public void test_ConstructorParentSpaceRef_partName() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), new SpaceReference("MySpace", new WikiReference(context.getDatabase())), 1,
        "mainNav");
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
    assertEquals("mainNav", treeNodeTest.getPartName());
    verifyDefault();
  }

  @Test
  public void testEquals() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), null, 1);
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
    verifyDefault();
  }

  @Test
  public void testEquals_position_notSameInteger() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), null, new Integer(1));
    TreeNode treeNodeTest2 = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), null, new Integer(1));
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
        "myPage"), null, 1);
    replayDefault();
    assertEquals(treeNodeTest.hashCode(), treeNode.hashCode());
    verifyDefault();
  }

  @Test
  public void testHash_null_position() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), null, null);
    TreeNode treeNodeTest2 = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), null, null);
    replayDefault();
    assertEquals(treeNodeTest.hashCode(), treeNodeTest2.hashCode());
    verifyDefault();
  }

  @Test
  public void testEquals_parentSpaceRefConstructor() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), new SpaceReference("MySpace", new WikiReference(context.getDatabase())), 1, "");
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
    verifyDefault();
  }

  @Test
  public void testHash_parentSpaceRefConstructor() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), new SpaceReference("MySpace", new WikiReference(context.getDatabase())), 1, "");
    replayDefault();
    assertEquals(treeNodeTest.hashCode(), treeNode.hashCode());
    verifyDefault();
  }

  @Test
  public void testGetParentRef_spaceRef() {
    SpaceReference parentSpaceRef = new SpaceReference("MySpace", new WikiReference(
        context.getDatabase()));
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), parentSpaceRef, 1, "");
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
        "myPage"), new SpaceReference("MySpace", new WikiReference(context.getDatabase())), 1,
        "mainNav");
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
    verifyDefault();
  }

  @Test
  public void testHash_parentSpaceRefConstructorWithPartName() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(), "MySpace",
        "myPage"), new SpaceReference("MySpace", new WikiReference(context.getDatabase())), 1,
        "mainNav");
    replayDefault();
    assertEquals(treeNodeTest.hashCode(), treeNode.hashCode());
    verifyDefault();
  }

}
