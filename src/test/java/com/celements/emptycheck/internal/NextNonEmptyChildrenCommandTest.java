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
package com.celements.emptycheck.internal;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.emptycheck.service.IEmptyCheckRole;
import com.celements.navigation.TreeNode;
import com.celements.navigation.service.ITreeNodeService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class NextNonEmptyChildrenCommandTest extends AbstractComponentTest {

  private XWikiContext context;
  private XWiki xwiki;
  private NextNonEmptyChildrenCommand nextNonEmptChildCmd;
  private ITreeNodeService treeNodeService;
  private ComponentDescriptor<ITreeNodeService> treeNodeServiceDesc;
  private ITreeNodeService savedTreeNodeServiceDesc;

  @Before
  public void setUp_EmptyCheckCommandTest() throws Exception {
    context = getContext();
    xwiki = getWikiMock();
    nextNonEmptChildCmd = new NextNonEmptyChildrenCommand();
    treeNodeService = createDefaultMock(ITreeNodeService.class);
    treeNodeServiceDesc = getComponentManager().getComponentDescriptor(ITreeNodeService.class,
        "default");
    savedTreeNodeServiceDesc = Utils.getComponent(ITreeNodeService.class);
    getComponentManager().unregisterComponent(ITreeNodeService.class, "default");
    getComponentManager().registerComponent(treeNodeServiceDesc, treeNodeService);
    context.setLanguage("de");
  }

  @After
  public void shutdown_EmptyCheckCommandTest() throws Exception {
    getComponentManager().unregisterComponent(ITreeNodeService.class, "default");
    getComponentManager().registerComponent(treeNodeServiceDesc, savedTreeNodeServiceDesc);
  }

  @Test
  public void testGetNextNonEmptyChildren_notEmpty() throws Exception {
    DocumentReference documentRef = new DocumentReference(context.getDatabase(), "mySpace",
        "MyDoc");
    XWikiDocument myXdoc = new XWikiDocument(documentRef);
    myXdoc.setContent("test content not empty");
    expect(xwiki.getDocument(eq(documentRef), same(context))).andReturn(myXdoc).once();
    expect(xwiki.getXWikiPreference(eq(IEmptyCheckRole.EMPTYCHECK_MODULS_PREF_NAME), eq(
        "celements.emptycheckModuls"), eq("default"), same(context))).andReturn(
            "default").anyTimes();
    replayDefault();
    assertEquals(documentRef, nextNonEmptChildCmd.getNextNonEmptyChildren(documentRef));
    verifyDefault();
  }

  @Test
  public void testGetNextNonEmptyChildren_empty_but_noChildren() throws Exception {
    DocumentReference emptyDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "MyEmptyDoc");
    createEmptyDoc(emptyDocRef);
    expect(treeNodeService.getSubNodesForParent(eq(emptyDocRef), eq(""))).andReturn(
        Collections.<TreeNode>emptyList()).once();
    expect(xwiki.getXWikiPreference(eq(IEmptyCheckRole.EMPTYCHECK_MODULS_PREF_NAME), eq(
        "celements.emptycheckModuls"), eq("default"), same(context))).andReturn(
            "default").anyTimes();
    replayDefault();
    assertNull(nextNonEmptChildCmd.getNextNonEmptyChildren(emptyDocRef));
    verifyDefault();
  }

  @Test
  public void testGetNextNonEmptyChildren_empty_with_nonEmptyChildren() throws Exception {
    DocumentReference emptyDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "MyEmptyDoc");
    createEmptyDoc(emptyDocRef);
    List<TreeNode> childrenList = Arrays.asList(new TreeNode(new DocumentReference(
        context.getDatabase(), "mySpace", "myChild"), emptyDocRef, 0), new TreeNode(
            new DocumentReference(context.getDatabase(), "mySpace", "myChild2"), emptyDocRef, 1));
    expect(treeNodeService.getSubNodesForParent(eq(emptyDocRef), eq(""))).andReturn(
        childrenList).once();
    DocumentReference expectedChildDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myChild");
    XWikiDocument childXdoc = new XWikiDocument(expectedChildDocRef);
    childXdoc.setContent("non empty child content");
    expect(xwiki.getDocument(eq(expectedChildDocRef), same(context))).andReturn(childXdoc).once();
    expect(xwiki.getXWikiPreference(eq(IEmptyCheckRole.EMPTYCHECK_MODULS_PREF_NAME), eq(
        "celements.emptycheckModuls"), eq("default"), same(context))).andReturn(
            "default").anyTimes();
    replayDefault();
    assertEquals(expectedChildDocRef, nextNonEmptChildCmd.getNextNonEmptyChildren(emptyDocRef));
    verifyDefault();
  }

  @Test
  public void testGetNextNonEmptyChildren_empty_with_nonEmptyChildren_notFirstChildren()
      throws Exception {
    context.setLanguage("fr");
    DocumentReference emptyDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "MyEmptyDoc");
    createEmptyDoc(emptyDocRef);
    DocumentReference expectedChildDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myChild2");
    DocumentReference emptyChildDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myChild");
    List<TreeNode> childrenList = Arrays.asList(new TreeNode(emptyChildDocRef, emptyDocRef, 0),
        new TreeNode(expectedChildDocRef, emptyDocRef, 1));
    expect(treeNodeService.getSubNodesForParent(eq(emptyDocRef), eq(""))).andReturn(
        childrenList).once();
    expect(treeNodeService.getSubNodesForParent(eq(emptyChildDocRef), eq(""))).andReturn(
        Collections.<TreeNode>emptyList()).once();
    XWikiDocument childEmptyXdoc = createDefaultMock(XWikiDocument.class);
    expect(childEmptyXdoc.getContent()).andReturn("").once();
    expect(xwiki.getDocument(eq(emptyChildDocRef), same(context))).andReturn(childEmptyXdoc).times(
        2);
    XWikiDocument childEmptyXTdoc = new XWikiDocument(emptyChildDocRef);
    childEmptyXTdoc.setContent("");
    expect(childEmptyXdoc.getTranslatedDocument(eq("fr"), same(context))).andReturn(
        childEmptyXTdoc).once();
    XWikiDocument childXdoc = new XWikiDocument(expectedChildDocRef);
    childXdoc.setContent("non empty child content");
    expect(xwiki.getDocument(eq(expectedChildDocRef), same(context))).andReturn(childXdoc).once();
    expect(xwiki.getXWikiPreference(eq(IEmptyCheckRole.EMPTYCHECK_MODULS_PREF_NAME), eq(
        "celements.emptycheckModuls"), eq("default"), same(context))).andReturn(
            "default").anyTimes();
    replayDefault();
    assertEquals(expectedChildDocRef, nextNonEmptChildCmd.getNextNonEmptyChildren(emptyDocRef));
    verifyDefault();
  }

  @Test
  public void testGetNextNonEmptyChildren_empty_recurse_on_EmptyChildren() throws Exception {
    DocumentReference emptyDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "MyEmptyDoc");
    createEmptyDoc(emptyDocRef);
    List<TreeNode> childrenList = Arrays.asList(new TreeNode(new DocumentReference(
        context.getDatabase(), "mySpace", "myChild"), emptyDocRef, 0), new TreeNode(
            new DocumentReference(context.getDatabase(), "mySpace", "myChild2"), emptyDocRef, 1));
    expect(treeNodeService.getSubNodesForParent(eq(emptyDocRef), eq(""))).andReturn(
        childrenList).once();
    DocumentReference childDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myChild");
    createEmptyDoc(childDocRef);
    List<TreeNode> childrenList2 = Arrays.asList(new TreeNode(new DocumentReference(
        context.getDatabase(), "mySpace", "myChildChild"), emptyDocRef, 0), new TreeNode(
            new DocumentReference(context.getDatabase(), "mySpace", "myChildChild2"), emptyDocRef,
            1));
    expect(treeNodeService.getSubNodesForParent(eq(childDocRef), eq(""))).andReturn(
        childrenList2).once();
    DocumentReference expectedChildDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myChildChild");
    XWikiDocument childChildXdoc = new XWikiDocument(expectedChildDocRef);
    childChildXdoc.setContent("non empty child content");
    expect(xwiki.getDocument(eq(expectedChildDocRef), same(context))).andReturn(
        childChildXdoc).once();
    expect(xwiki.getXWikiPreference(eq(IEmptyCheckRole.EMPTYCHECK_MODULS_PREF_NAME), eq(
        "celements.emptycheckModuls"), eq("default"), same(context))).andReturn(
            "default").anyTimes();
    replayDefault();
    assertEquals(expectedChildDocRef, nextNonEmptChildCmd.getNextNonEmptyChildren(emptyDocRef));
    verifyDefault();
  }

  @Test
  public void testGetNextNonEmptyChildren_empty_recurse_on_EmptyChildren_reconize_loop()
      throws Exception {
    DocumentReference emptyDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "MyEmptyDoc");
    createEmptyDoc(emptyDocRef);
    DocumentReference childDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myChild");
    createEmptyDoc(childDocRef);
    DocumentReference child2DocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myChild2");
    createEmptyDoc(child2DocRef);
    List<TreeNode> childrenList = Arrays.asList(new TreeNode(childDocRef, emptyDocRef, 0),
        new TreeNode(child2DocRef, emptyDocRef, 1));
    // if called more than once the recursion detection is very likely broken!
    expect(treeNodeService.getSubNodesForParent(eq(emptyDocRef), eq(""))).andReturn(
        childrenList).once();
    DocumentReference childChildDocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myChildChild");
    createEmptyDoc(childChildDocRef);
    DocumentReference childChild2DocRef = new DocumentReference(context.getDatabase(), "mySpace",
        "myChildChild2");
    createEmptyDoc(childChild2DocRef);
    List<TreeNode> childrenList2 = Arrays.asList(new TreeNode(childChildDocRef, emptyDocRef, 0),
        new TreeNode(childChild2DocRef, emptyDocRef, 1));
    expect(treeNodeService.getSubNodesForParent(eq(childDocRef), eq(""))).andReturn(
        childrenList2).once();
    List<TreeNode> childrenList3 = Arrays.asList(new TreeNode(new DocumentReference(
        context.getDatabase(), "mySpace", "MyEmptyDoc"), emptyDocRef, 0));
    expect(treeNodeService.getSubNodesForParent(eq(childChildDocRef), eq(""))).andReturn(
        childrenList3).once();
    expect(treeNodeService.getSubNodesForParent(eq(child2DocRef), eq(""))).andReturn(
        Collections.<TreeNode>emptyList()).once();
    expect(treeNodeService.getSubNodesForParent(eq(childChild2DocRef), eq(""))).andReturn(
        Collections.<TreeNode>emptyList()).once();
    expect(xwiki.getXWikiPreference(eq(IEmptyCheckRole.EMPTYCHECK_MODULS_PREF_NAME), eq(
        "celements.emptycheckModuls"), eq("default"), same(context))).andReturn(
            "default").anyTimes();
    replayDefault();
    assertNull(nextNonEmptChildCmd.getNextNonEmptyChildren(emptyDocRef));
    verifyDefault();
  }

  // *****************************************************************
  // * H E L P E R - M E T H O D S *
  // *****************************************************************/

  private XWikiDocument createEmptyDoc(DocumentReference emptyDocRef) throws XWikiException {
    XWikiDocument myXdoc = createDefaultMock(XWikiDocument.class);
    XWikiDocument myXTdoc = new XWikiDocument(emptyDocRef);
    myXTdoc.setDefaultLanguage("de");
    myXTdoc.setLanguage("fr");
    expect(myXdoc.getContent()).andReturn("").atLeastOnce();
    expect(myXdoc.getTranslatedDocument(eq(""), same(context))).andReturn(myXdoc).anyTimes();
    expect(myXdoc.getTranslatedDocument(eq("de"), same(context))).andReturn(myXdoc).anyTimes();
    expect(myXdoc.getTranslatedDocument(eq("fr"), same(context))).andReturn(myXTdoc).anyTimes();
    expect(myXdoc.getLanguage()).andReturn("").anyTimes();
    expect(myXdoc.getDefaultLanguage()).andReturn("de").anyTimes();
    expect(xwiki.getDocument(eq(emptyDocRef), same(context))).andReturn(myXdoc).atLeastOnce();
    return myXdoc;
  }

}
