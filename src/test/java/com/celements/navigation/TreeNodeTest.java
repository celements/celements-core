package com.celements.navigation;


import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWikiContext;

public class TreeNodeTest extends AbstractBridgedComponentTestCase {

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
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(),
        "MySpace", "myPage"), new SpaceReference("MySpace", new WikiReference(
            context.getDatabase())), null, 1);
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
    assertEquals("", treeNodeTest.getPartName(getContext()));
    verifyDefault();
  }

  @Test
  public void test_ConstructorParentSpaceRef_partName_empty() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(),
        "MySpace", "myPage"), new SpaceReference("MySpace", new WikiReference(
            context.getDatabase())), "", 1);
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
    assertEquals("", treeNodeTest.getPartName(getContext()));
    verifyDefault();
  }

  @Test
  public void test_ConstructorParentSpaceRef_partName() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(),
        "MySpace", "myPage"), new SpaceReference("MySpace", new WikiReference(
            context.getDatabase())), "mainNav", 1);
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
    assertEquals("mainNav", treeNodeTest.getPartName(getContext()));
    verifyDefault();
  }

 @Test
  public void testEquals() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(),
        "MySpace", "myPage"), "", 1);
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
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
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(),
        "MySpace", "myPage"), "", 1);
    replayDefault();
    assertEquals(treeNodeTest.hashCode(), treeNode.hashCode());
    verifyDefault();
  }

  @Test
  public void testHash_null_position() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(),
        "MySpace", "myPage"), "", null);
    treeNode.setPosition(null);
    replayDefault();
    assertEquals(treeNodeTest.hashCode(), treeNode.hashCode());
    verifyDefault();
  }

  @Test
  public void testEquals_parentSpaceRefConstructor() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(),
        "MySpace", "myPage"), new SpaceReference("MySpace", new WikiReference(
            context.getDatabase())), "", 1);
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
    verifyDefault();
  }

  @Test
  public void testHash_parentSpaceRefConstructor() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(),
        "MySpace", "myPage"), new SpaceReference("MySpace", new WikiReference(
            context.getDatabase())), "", 1);
    replayDefault();
    assertEquals(treeNodeTest.hashCode(), treeNode.hashCode());
    verifyDefault();
  }

  @Test
  public void testGetParentRef_spaceRef() {
    SpaceReference parentSpaceRef = new SpaceReference("MySpace", new WikiReference(
        context.getDatabase()));
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(),
        "MySpace", "myPage"), parentSpaceRef, "", 1);
    replayDefault();
    assertEquals(parentSpaceRef, treeNodeTest.getParentRef());
    verifyDefault();
  }

  @Test
  public void testGetParentRef_docRef() {
    DocumentReference parentDocRef = new DocumentReference(context.getDatabase(), "myParentPage",
        "MySpace");
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(),
        "MySpace", "myPage"), parentDocRef, 1);
    replayDefault();
    assertEquals(parentDocRef, treeNodeTest.getParentRef());
    verifyDefault();
  }

  @Test
  public void testEquals_parentSpaceRefConstructorWithPartName() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(),
        "MySpace", "myPage"), new SpaceReference("MySpace", new WikiReference(
            context.getDatabase())), "mainNav", 1);
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
    verifyDefault();
  }

  @Test
  public void testHash_parentSpaceRefConstructorWithPartName() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(),
        "MySpace", "myPage"), new SpaceReference("MySpace", new WikiReference(
            context.getDatabase())), "mainNav", 1);
    replayDefault();
    assertEquals(treeNodeTest.hashCode(), treeNode.hashCode());
    verifyDefault();
  }

  @Test
  public void testEquals_parentDocRefConstructor() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(),
        "MySpace", "myPage"), new DocumentReference(context.getDatabase(), "myParentPage",
            "MySpace"), 1);
    treeNode.setParent("MySpace.myParentPage");
    replayDefault();
    assertTrue(treeNode.equals(treeNodeTest));
    verifyDefault();
  }

  @Test
  public void testHash_parentDocRefConstructor() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(),
        "MySpace", "myPage"), new DocumentReference(context.getDatabase(), "myParentPage",
            "MySpace"), 1);
    treeNode.setParent("MySpace.myParentPage");
    replayDefault();
    assertEquals(treeNodeTest.hashCode(), treeNode.hashCode());
    verifyDefault();
  }

}
