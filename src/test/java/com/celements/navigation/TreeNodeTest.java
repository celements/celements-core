package com.celements.navigation;


import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;

public class TreeNodeTest extends AbstractBridgedComponentTestCase {

  private DocumentReference docRef;
  private XWikiContext context;
  private XWiki xwiki;
  private TreeNode treeNode;
  private String parent;

  @Before
  public void setUp_TreeNodeTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    docRef = new DocumentReference(context.getDatabase(), "MySpace", "myPage");
    parent = "";
    treeNode = new TreeNode(docRef, parent, 1);
  }

  @Test
  public void testEquals() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(),
        "MySpace", "myPage"), "", 1);
    replayAll();
    assertEquals(treeNodeTest, treeNode);
    verifyAll();
  }

  @Test
  public void testHash() {
    TreeNode treeNodeTest = new TreeNode(new DocumentReference(context.getDatabase(),
        "MySpace", "myPage"), "", 1);
    replayAll();
    assertEquals(treeNodeTest.hashCode(), treeNode.hashCode());
    verifyAll();
  }


  private void replayAll(Object ... mocks) {
    replay(xwiki);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki);
    verify(mocks);
  }
}
