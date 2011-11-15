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
package com.celements.web.plugin.cmd;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.navigation.TreeNode;
import com.celements.web.utils.IWebUtils;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public class EmptyCheckCommandTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki xwiki;
  private EmptyCheckCommand emptyChildCheckCmd;
  private IWebUtils celUtils;

  @Before
  public void setUp_EmptyCheckCommandTest() throws Exception {
    context = getContext();
    xwiki = createMock(XWiki.class);
    context.setWiki(xwiki);
    emptyChildCheckCmd = new EmptyCheckCommand();
    celUtils = createMock(IWebUtils.class);
    emptyChildCheckCmd.celUtils = celUtils;
  }

  @Test
  public void testIsEmptyRTEDocument_empty() {
    assertTrue(emptyChildCheckCmd.isEmptyRTEDocument(getTestDoc("")));
  }

  @Test
  public void testIsEmptyRTEDocument_cel2_standard_oldRTE_2Space() {
    assertTrue("Lonly non breaking spaces (2) with break should be"
        + " treated as empty", emptyChildCheckCmd.isEmptyRTEDocument(getTestDoc(
            "<p>&nbsp;&nbsp;</p>")));
  }

  @Test
  public void testIsEmptyRTEDocument_cel2_standard_oldRTE_1Break() {
    assertTrue("Lonly non breaking spaces (2) with break should be"
        + " treated as empty", emptyChildCheckCmd.isEmptyRTEDocument(getTestDoc(
            "<p><br /></p>")));
  }

  @Test
  public void testIsEmptyRTEDocument_manualizer_example() {
    assertTrue("Paragraph with span surrounding break should be"
        + " treated as empty", emptyChildCheckCmd.isEmptyRTEDocument(getTestDoc(
        "<p><span style=\"line-height: normal; font-size: 10px;\"><br /></span></p>")));
  }
  
  @Test
  public void testIsEmptyRTEDocument_cel2_standard_oldRTE_2Space1Break() {
    assertTrue("Lonly non breaking spaces (2) with break should be"
        + " treated as empty", emptyChildCheckCmd.isEmptyRTEDocument(getTestDoc(
            "<p>&nbsp;&nbsp; <br /></p>")));
  }

  @Test
  public void testIsEmptyRTEDocument_cel2_standard_oldRTE_3Space1Break() {
    assertTrue("Lonly non breaking spaces (3) with break should be"
        + " treated as empty", emptyChildCheckCmd.isEmptyRTEDocument(getTestDoc(
            "<p>&nbsp;&nbsp;&nbsp;<br /></p>")));
  }


  @Test
  public void testIsEmptyRTEDocument_cel2_standard_oldRTE_1Space2Break() {
    assertTrue("Lonly non breaking spaces with break (2) should be"
        + " treated as empty", emptyChildCheckCmd.isEmptyRTEDocument(getTestDoc(
            "<p>&nbsp;<br /><br /></p>")));
  }

  @Test
  public void testIsEmptyRTEDocument_cel2_standard_oldRTE_REGULAR_TEXT() {
    assertFalse("Regular Text (2) should not be treated as empty.",
        emptyChildCheckCmd.isEmptyRTEDocument(getTestDoc(
            "<p>adsf  &nbsp; <br />sadf</p>")));
  }

  @Test
  public void testIsEmptyRTEDocument_nbsp() {
    assertTrue("Lonly non breaking spaces should be treated as empty",
        emptyChildCheckCmd.isEmptyRTEDocument(getTestDoc("&nbsp;")));
    assertTrue("Non breaking spaces in a paragraph should be treated as empty",
        emptyChildCheckCmd.isEmptyRTEDocument(getTestDoc("<p>&nbsp;</p>")));
    assertTrue("Non breaking spaces in a paragraph with white spaces"
        + " should be treated as empty",
        emptyChildCheckCmd.isEmptyRTEDocument(getTestDoc("<p>  &nbsp; </p>")));
    assertFalse("Regular Text should not be treated as empty.",
        emptyChildCheckCmd.isEmptyRTEDocument(getTestDoc("<p>adsf  &nbsp; </p>")));
  }

  @Test
  public void testGetNextNonEmptyChildren_notEmpty() throws Exception {
    DocumentReference documentRef = new DocumentReference(context.getDatabase(),
        "mySpace", "MyDoc");
    XWikiDocument myXdoc = new XWikiDocument(documentRef);
    myXdoc.setContent("test content not empty");
    expect(xwiki.getDocument(eq(documentRef), same(context))).andReturn(myXdoc).once();
    replayAll();
    assertEquals(documentRef, emptyChildCheckCmd.getNextNonEmptyChildren(documentRef,
        context));
    verifyAll();
  }

  @Test
  public void testGetNextNonEmptyChildren_empty_but_noChildren() throws Exception {
    DocumentReference emptyDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "MyEmptyDoc");
    XWikiDocument myXdoc = createEmptyDoc(emptyDocRef);
    List<TreeNode> noChildrenList = Collections.emptyList();
    expect(celUtils.getSubNodesForParent(eq("mySpace.MyEmptyDoc"), eq("mySpace"), eq(""),
        same(context))).andReturn(noChildrenList).once();
    replayAll(myXdoc);
    assertEquals(emptyDocRef, emptyChildCheckCmd.getNextNonEmptyChildren(emptyDocRef,
        context));
    verifyAll(myXdoc);
  }

  @Test
  public void testGetNextNonEmptyChildren_empty_with_nonEmptyChildren() throws Exception {
    DocumentReference emptyDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "MyEmptyDoc");
    XWikiDocument myXdoc = createEmptyDoc(emptyDocRef);
    List<TreeNode> childrenList = Arrays.asList(new TreeNode(new DocumentReference(
        context.getDatabase(), "mySpace", "myChild"), "mySpace.MyEmptyDoc", 0),
        new TreeNode(new DocumentReference(context.getDatabase(), "mySpace", "myChild2"),
            "mySpace.MyEmptyDoc", 1));
    expect(celUtils.getSubNodesForParent(eq("mySpace.MyEmptyDoc"), eq("mySpace"), eq(""),
        same(context))).andReturn(childrenList).once();
    DocumentReference expectedChildDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myChild");
    XWikiDocument childXdoc = new XWikiDocument(expectedChildDocRef);
    childXdoc.setContent("non empty child content");
    expect(xwiki.getDocument(eq(expectedChildDocRef), same(context))).andReturn(childXdoc 
        ).once();
    replayAll(myXdoc);
    assertEquals(expectedChildDocRef, emptyChildCheckCmd.getNextNonEmptyChildren(
        emptyDocRef, context));
    verifyAll(myXdoc);
  }

  @Test
  public void testGetNextNonEmptyChildren_empty_recurse_on_EmptyChildren(
      ) throws Exception {
    DocumentReference emptyDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "MyEmptyDoc");
    XWikiDocument myXdoc = createEmptyDoc(emptyDocRef);
    List<TreeNode> childrenList = Arrays.asList(new TreeNode(new DocumentReference(
        context.getDatabase(), "mySpace", "myChild"), "mySpace.MyEmptyDoc", 0),
        new TreeNode(new DocumentReference(context.getDatabase(), "mySpace", "myChild2"),
            "mySpace.MyEmptyDoc", 1));
    expect(celUtils.getSubNodesForParent(eq("mySpace.MyEmptyDoc"), eq("mySpace"), eq(""),
        same(context))).andReturn(childrenList).once();
    DocumentReference childDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myChild");
    XWikiDocument childXdoc = createEmptyDoc(childDocRef);
    List<TreeNode> childrenList2 = Arrays.asList(new TreeNode(new DocumentReference(
        context.getDatabase(), "mySpace", "myChildChild"), "mySpace.MyEmptyDoc", 0),
        new TreeNode(new DocumentReference(context.getDatabase(), "mySpace",
            "myChildChild2"), "mySpace.MyEmptyDoc", 1));
    expect(celUtils.getSubNodesForParent(eq("mySpace.myChild"), eq("mySpace"), eq(""),
        same(context))).andReturn(childrenList2).once();
    DocumentReference expectedChildDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myChildChild");
    XWikiDocument childChildXdoc = new XWikiDocument(expectedChildDocRef);
    childChildXdoc.setContent("non empty child content");
    expect(xwiki.getDocument(eq(expectedChildDocRef), same(context))).andReturn(
        childChildXdoc).once();
    replayAll(myXdoc, childXdoc);
    assertEquals(expectedChildDocRef, emptyChildCheckCmd.getNextNonEmptyChildren(
        emptyDocRef, context));
    verifyAll(myXdoc, childXdoc);
  }

  @Test
  public void testGetNextNonEmptyChildren_empty_recurse_on_EmptyChildren_reconize_loop(
      ) throws Exception {
    DocumentReference emptyDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "MyEmptyDoc");
    XWikiDocument myXdoc = createEmptyDoc(emptyDocRef);
    List<TreeNode> childrenList = Arrays.asList(new TreeNode(new DocumentReference(
        context.getDatabase(), "mySpace", "myChild"), "mySpace.MyEmptyDoc", 0),
        new TreeNode(new DocumentReference(context.getDatabase(), "mySpace", "myChild2"),
            "mySpace.MyEmptyDoc", 1));
    //if called more than once the recursion detection is very likely broken!
    expect(celUtils.getSubNodesForParent(eq("mySpace.MyEmptyDoc"), eq("mySpace"), eq(""),
        same(context))).andReturn(childrenList).once();
    DocumentReference childDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myChild");
    XWikiDocument childXdoc = createEmptyDoc(childDocRef);
    List<TreeNode> childrenList2 = Arrays.asList(new TreeNode(new DocumentReference(
        context.getDatabase(), "mySpace", "myChildChild"), "mySpace.MyEmptyDoc", 0),
        new TreeNode(new DocumentReference(
            context.getDatabase(), "mySpace", "myChildChild2"), "mySpace.MyEmptyDoc", 1));
    expect(celUtils.getSubNodesForParent(eq("mySpace.myChild"), eq("mySpace"), eq(""),
        same(context))).andReturn(childrenList2).once();
    DocumentReference expectedChildDocRef = new DocumentReference(context.getDatabase(),
        "mySpace", "myChildChild");
    XWikiDocument childChildXdoc = createEmptyDoc(expectedChildDocRef);
    List<TreeNode> childrenList3 = Arrays.asList(new TreeNode(new DocumentReference(
        context.getDatabase(), "mySpace", "MyEmptyDoc"), "mySpace.MyEmptyDoc", 0));
    expect(celUtils.getSubNodesForParent(eq("mySpace.myChildChild"), eq("mySpace"),
        eq(""), same(context))).andReturn(childrenList3).once();
    replayAll(myXdoc, childXdoc, childChildXdoc);
    assertEquals(expectedChildDocRef, emptyChildCheckCmd.getNextNonEmptyChildren(
        emptyDocRef, context));
    verifyAll(myXdoc, childXdoc, childChildXdoc);
  }

  //*****************************************************************
  //*                  H E L P E R  - M E T H O D S                 *
  //*****************************************************************/

  private XWikiDocument createEmptyDoc(DocumentReference emptyDocRef)
      throws XWikiException {
    XWikiDocument myXdoc = createMock(XWikiDocument.class);
    XWikiDocument myXTdoc = new XWikiDocument(emptyDocRef);
    expect(myXdoc.getContent()).andReturn("").atLeastOnce();
    expect(myXdoc.getTranslatedDocument(same(context))).andReturn(myXTdoc).atLeastOnce();
    expect(xwiki.getDocument(eq(emptyDocRef), same(context))).andReturn(myXdoc
        ).atLeastOnce();
    return myXdoc;
  }

  private XWikiDocument getTestDoc(String inStr) {
    DocumentReference testDocRef = new DocumentReference("xwiki", "testSpace", "testDoc");
    XWikiDocument doc = new XWikiDocument(testDocRef );
    doc.setContent(inStr);
    return doc;
  }

  private void replayAll(Object ... mocks) {
    replay(xwiki, celUtils);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(xwiki, celUtils);
    verify(mocks);
  }

}
