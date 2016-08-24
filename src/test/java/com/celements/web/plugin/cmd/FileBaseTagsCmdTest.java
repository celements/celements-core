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

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.XWikiDocumentCreator;
import com.celements.navigation.INavigationClassConfig;
import com.celements.navigation.TreeNode;
import com.celements.navigation.filter.InternalRightsFilter;
import com.celements.navigation.service.ITreeNodeService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.web.Utils;

public class FileBaseTagsCmdTest extends AbstractComponentTest {

  private FileBaseTagsCmd fileBaseTagCmd;
  private XWikiContext context;
  private XWiki xwiki;
  private ITreeNodeService mockTreeNodeSrv;
  private INavigationClassConfig navClassConfig;

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    registerComponentMock(XWikiDocumentCreator.class);
    context = getContext();
    xwiki = getWikiMock();
    mockTreeNodeSrv = registerComponentMock(ITreeNodeService.class);
    navClassConfig = Utils.getComponent(INavigationClassConfig.class);
    fileBaseTagCmd = new FileBaseTagsCmd();
  }

  @Deprecated
  @Test
  public void testGetTagSpaceName() {
    String celFileBaseName = "Content_attachments.FileBaseDoc";
    expect(xwiki.getSpacePreference(eq("cel_centralfilebase"), eq(""), same(context))).andReturn(
        celFileBaseName);
    replayDefault();
    assertEquals("Content_attachments", fileBaseTagCmd.getTagSpaceName(context));
    verifyDefault();
  }

  @Deprecated
  @Test
  public void testGetTagSpaceName_onlySpaceName() {
    String celFileBaseName = "Content_attachments";
    expect(xwiki.getSpacePreference(eq("cel_centralfilebase"), eq(""), same(context))).andReturn(
        celFileBaseName);
    replayDefault();
    assertEquals("Content_attachments", fileBaseTagCmd.getTagSpaceName(context));
    verifyDefault();
  }

  @Test
  public void testGetTagSpaceRef() {
    context.setDatabase("myWiki");
    String celFileBaseName = "Content_attachments.FileBaseDoc";
    expect(xwiki.getSpacePreference(eq("cel_centralfilebase"), eq(""), same(context))).andReturn(
        celFileBaseName);
    replayDefault();
    SpaceReference tagSpaceRef = fileBaseTagCmd.getTagSpaceRef();
    assertEquals("Content_attachments", tagSpaceRef.getName());
    assertEquals("myWiki", tagSpaceRef.getParent().getName());
    verifyDefault();
  }

  @Test
  public void testGetTagSpaceRef_emptyString() throws Exception {
    ConfigurationSource xwikiPropConfigMock = registerComponentMock(ConfigurationSource.class,
        "xwikiproperties");
    context.setDatabase("myWiki");
    expect(xwikiPropConfigMock.getProperty(eq("model.reference.default.space"), eq(
        "Main"))).andReturn("DefaultSpace").anyTimes();
    expect(xwiki.getSpacePreference(eq("cel_centralfilebase"), eq(""), same(context))).andReturn(
        "");
    replayDefault();
    SpaceReference tagSpaceRef = fileBaseTagCmd.getTagSpaceRef();
    assertEquals("DefaultSpace_attachments", tagSpaceRef.getName());
    assertEquals("myWiki", tagSpaceRef.getParent().getName());
    verifyDefault();
  }

  @Test
  public void testGetTagSpaceRef_onlySpaceName() {
    context.setDatabase("myWiki");
    String celFileBaseName = "Content_attachments";
    expect(xwiki.getSpacePreference(eq("cel_centralfilebase"), eq(""), same(context))).andReturn(
        celFileBaseName);
    replayDefault();
    SpaceReference tagSpaceRef = fileBaseTagCmd.getTagSpaceRef();
    assertEquals("Content_attachments", tagSpaceRef.getName());
    assertEquals("myWiki", tagSpaceRef.getParent().getName());
    verifyDefault();
  }

  @Deprecated
  @Test
  public void testGetTagDocument_docExists_without_MenuItem() throws Exception {
    context.setDatabase("myWiki");
    String celFileBaseName = "Content_attachments";
    SpaceReference celFileBaseRef = new SpaceReference(celFileBaseName, new WikiReference(
        context.getDatabase()));
    expect(xwiki.getSpacePreference(eq("cel_centralfilebase"), eq(""), same(context))).andReturn(
        celFileBaseName).anyTimes();
    DocumentReference tagDocRef = new DocumentReference(context.getDatabase(), celFileBaseName,
        "tag0");
    expect(xwiki.exists(eq(tagDocRef), same(context))).andReturn(true).atLeastOnce();
    expect(mockTreeNodeSrv.getSubNodesForParent(eq(celFileBaseRef), isA(
        InternalRightsFilter.class))).andReturn(Collections.<TreeNode>emptyList());
    expect(xwiki.getDocument(eq(tagDocRef), same(context))).andReturn(new XWikiDocument(
        tagDocRef)).once();
    replayDefault();
    assertNotNull("docAlready exists: expecting existing doc", fileBaseTagCmd.getTagDocument("tag0",
        false, context));
    verifyDefault();
  }

  @Deprecated
  @Test
  public void testGetTagDocument_docExists() throws Exception {
    context.setDatabase("myWiki");
    String celFileBaseName = "Content_attachments";
    SpaceReference celFileBaseRef = new SpaceReference(celFileBaseName, new WikiReference(
        context.getDatabase()));
    expect(xwiki.getSpacePreference(eq("cel_centralfilebase"), eq(""), same(context))).andReturn(
        celFileBaseName).anyTimes();
    DocumentReference tagDocRef = new DocumentReference(context.getDatabase(), celFileBaseName,
        "tag0");
    expect(xwiki.exists(eq(tagDocRef), same(context))).andReturn(true).atLeastOnce();
    expect(mockTreeNodeSrv.getSubNodesForParent(eq(celFileBaseRef), isA(
        InternalRightsFilter.class))).andReturn(Arrays.asList(new TreeNode(tagDocRef, null, 2)));
    XWikiDocument existingTagDoc = new XWikiDocument(tagDocRef);
    existingTagDoc.setNew(false);
    BaseObject expectedMenuItemObj = new BaseObject();
    expectedMenuItemObj.setXClassReference(navClassConfig.getMenuItemClassRef());
    expectedMenuItemObj.setIntValue(INavigationClassConfig.MENU_POSITION_FIELD, 2);
    existingTagDoc.addXObject(expectedMenuItemObj);
    expect(xwiki.getDocument(eq(tagDocRef), same(context))).andReturn(existingTagDoc).once();
    replayDefault();
    XWikiDocument tagDocument = fileBaseTagCmd.getTagDocument("tag0", false, context);
    assertEquals("docAlready exists: expecting existing doc", tagDocRef,
        tagDocument.getDocumentReference());
    assertFalse("docAlready exists: expecting existing doc not new", tagDocument.isNew());
    BaseObject menuItemObj = tagDocument.getXObject(navClassConfig.getMenuItemClassRef());
    assertNotNull("expecting attached object", menuItemObj);
    assertEquals("expecting attached object", 2, menuItemObj.getIntValue(
        INavigationClassConfig.MENU_POSITION_FIELD));
    verifyDefault();
  }

  @Deprecated
  @Test
  public void testGetTagDocument_docExists_addingMenuItem() throws Exception {
    context.setDatabase("myWiki");
    String celFileBaseName = "Content_attachments";
    SpaceReference celFileBaseRef = new SpaceReference(celFileBaseName, new WikiReference(
        context.getDatabase()));
    expect(xwiki.getSpacePreference(eq("cel_centralfilebase"), eq(""), same(context))).andReturn(
        celFileBaseName).anyTimes();
    DocumentReference tagDocRef = new DocumentReference(context.getDatabase(), celFileBaseName,
        "tag0");
    expect(xwiki.exists(eq(tagDocRef), same(context))).andReturn(true).atLeastOnce();
    DocumentReference tagDocRef2 = new DocumentReference(context.getDatabase(), celFileBaseName,
        "tag1");
    expect(mockTreeNodeSrv.getSubNodesForParent(eq(celFileBaseRef), isA(
        InternalRightsFilter.class))).andReturn(Arrays.asList(new TreeNode(tagDocRef2, null,
            0))).atLeastOnce();
    XWikiDocument existingTagDoc = new XWikiDocument(tagDocRef);
    existingTagDoc.setNew(false);
    expect(xwiki.getDocument(eq(tagDocRef), same(context))).andReturn(existingTagDoc).once();
    BaseClass menuItemBaseClass = createMockAndAddToDefault(BaseClass.class);
    expect(xwiki.getXClass(eq(navClassConfig.getMenuItemClassRef()), same(context))).andReturn(
        menuItemBaseClass).once();
    BaseObject expectedMenuItemObj = new BaseObject();
    expectedMenuItemObj.setXClassReference(navClassConfig.getMenuItemClassRef());
    expect(menuItemBaseClass.newCustomClassInstance(same(context))).andReturn(expectedMenuItemObj);
    Capture<XWikiDocument> savedDocCapture = new Capture<>();
    xwiki.saveDocument(capture(savedDocCapture), isA(String.class), eq(false), same(context));
    expectLastCall().once();
    expect(xwiki.getDocument(eq(tagDocRef), same(context))).andReturn(existingTagDoc).once();
    replayDefault();
    XWikiDocument tagDocument = fileBaseTagCmd.getTagDocument("tag0", true, context);
    assertEquals("docAlready exists: expecting existing doc", tagDocRef,
        tagDocument.getDocumentReference());
    assertFalse("docAlready exists: expecting existing doc not new", tagDocument.isNew());
    XWikiDocument savedTagDocument = savedDocCapture.getValue();
    BaseObject menuItemObj = savedTagDocument.getXObject(navClassConfig.getMenuItemClassRef());
    assertNotNull("expecting attached object", menuItemObj);
    assertEquals("expecting attached object", 1, menuItemObj.getIntValue(
        INavigationClassConfig.MENU_POSITION_FIELD));
    verifyDefault();
  }

  @Test
  public void testGetOrCreateTagDocument_docExists_without_MenuItem() throws Exception {
    context.setDatabase("myWiki");
    String celFileBaseName = "Content_attachments";
    SpaceReference celFileBaseRef = new SpaceReference(celFileBaseName, new WikiReference(
        context.getDatabase()));
    expect(xwiki.getSpacePreference(eq("cel_centralfilebase"), eq(""), same(context))).andReturn(
        celFileBaseName).anyTimes();
    DocumentReference tagDocRef = new DocumentReference(context.getDatabase(), celFileBaseName,
        "tag0");
    expect(xwiki.exists(eq(tagDocRef), same(context))).andReturn(true).atLeastOnce();
    expect(mockTreeNodeSrv.getSubNodesForParent(eq(celFileBaseRef), isA(
        InternalRightsFilter.class))).andReturn(Collections.<TreeNode>emptyList());
    expect(xwiki.getDocument(eq(tagDocRef), same(context))).andReturn(new XWikiDocument(
        tagDocRef)).once();
    replayDefault();
    assertNotNull("docAlready exists: expecting existing doc",
        fileBaseTagCmd.getOrCreateTagDocument("tag0", false));
    verifyDefault();
  }

  @Test
  public void testGetOrCreateTagDocument_docExists() throws Exception {
    context.setDatabase("myWiki");
    String celFileBaseName = "Content_attachments";
    SpaceReference celFileBaseRef = new SpaceReference(celFileBaseName, new WikiReference(
        context.getDatabase()));
    expect(xwiki.getSpacePreference(eq("cel_centralfilebase"), eq(""), same(context))).andReturn(
        celFileBaseName).anyTimes();
    DocumentReference tagDocRef = new DocumentReference(context.getDatabase(), celFileBaseName,
        "tag0");
    expect(xwiki.exists(eq(tagDocRef), same(context))).andReturn(true).atLeastOnce();
    expect(mockTreeNodeSrv.getSubNodesForParent(eq(celFileBaseRef), isA(
        InternalRightsFilter.class))).andReturn(Arrays.asList(new TreeNode(tagDocRef, null, 0)));
    XWikiDocument existingTagDoc = new XWikiDocument(tagDocRef);
    existingTagDoc.setNew(false);
    BaseObject expectedMenuItemObj = new BaseObject();
    expectedMenuItemObj.setXClassReference(navClassConfig.getMenuItemClassRef());
    expectedMenuItemObj.setIntValue(INavigationClassConfig.MENU_POSITION_FIELD, 2);
    existingTagDoc.addXObject(expectedMenuItemObj);
    expect(xwiki.getDocument(eq(tagDocRef), same(context))).andReturn(existingTagDoc).once();
    replayDefault();
    XWikiDocument tagDocument = fileBaseTagCmd.getOrCreateTagDocument("tag0", false);
    assertEquals("docAlready exists: expecting existing doc", tagDocRef,
        tagDocument.getDocumentReference());
    assertFalse("docAlready exists: expecting existing doc not new", tagDocument.isNew());
    BaseObject menuItemObj = tagDocument.getXObject(navClassConfig.getMenuItemClassRef());
    assertNotNull("expecting attached object", menuItemObj);
    assertEquals("expecting attached object", 2, menuItemObj.getIntValue(
        INavigationClassConfig.MENU_POSITION_FIELD));
    verifyDefault();
  }

  @Test
  public void testGetOrCreateTagDocument_docExists_addingMenuItem() throws Exception {
    context.setDatabase("myWiki");
    String celFileBaseName = "Content_attachments";
    SpaceReference celFileBaseRef = new SpaceReference(celFileBaseName, new WikiReference(
        context.getDatabase()));
    expect(xwiki.getSpacePreference(eq("cel_centralfilebase"), eq(""), same(context))).andReturn(
        celFileBaseName).anyTimes();
    DocumentReference tagDocRef = new DocumentReference(context.getDatabase(), celFileBaseName,
        "tag0");
    expect(xwiki.exists(eq(tagDocRef), same(context))).andReturn(true).atLeastOnce();
    DocumentReference tagDocRef2 = new DocumentReference(context.getDatabase(), celFileBaseName,
        "tag1");
    expect(mockTreeNodeSrv.getSubNodesForParent(eq(celFileBaseRef), isA(
        InternalRightsFilter.class))).andReturn(Arrays.asList(new TreeNode(tagDocRef2, null,
            0))).atLeastOnce();
    XWikiDocument existingTagDoc = new XWikiDocument(tagDocRef);
    existingTagDoc.setNew(false);
    expect(xwiki.getDocument(eq(tagDocRef), same(context))).andReturn(existingTagDoc).once();
    BaseClass menuItemBaseClass = createMockAndAddToDefault(BaseClass.class);
    expect(xwiki.getXClass(eq(navClassConfig.getMenuItemClassRef()), same(context))).andReturn(
        menuItemBaseClass).once();
    BaseObject expectedMenuItemObj = new BaseObject();
    expectedMenuItemObj.setXClassReference(navClassConfig.getMenuItemClassRef());
    expect(menuItemBaseClass.newCustomClassInstance(same(context))).andReturn(expectedMenuItemObj);
    Capture<XWikiDocument> savedDocCapture = new Capture<>();
    xwiki.saveDocument(capture(savedDocCapture), isA(String.class), eq(false), same(context));
    expectLastCall().once();
    expect(xwiki.getDocument(eq(tagDocRef), same(context))).andReturn(existingTagDoc).once();
    replayDefault();
    XWikiDocument tagDocument = fileBaseTagCmd.getOrCreateTagDocument("tag0", true);
    assertEquals("docAlready exists: expecting existing doc", tagDocRef,
        tagDocument.getDocumentReference());
    assertFalse("docAlready exists: expecting existing doc not new", tagDocument.isNew());
    XWikiDocument savedTagDocument = savedDocCapture.getValue();
    BaseObject menuItemObj = savedTagDocument.getXObject(navClassConfig.getMenuItemClassRef());
    assertNotNull("expecting attached object", menuItemObj);
    assertEquals("expecting attached object", 1, menuItemObj.getIntValue(
        INavigationClassConfig.MENU_POSITION_FIELD));
    verifyDefault();
  }

  @Test
  public void testGetOrCreateTagDocument_docNotExists_Exception() throws Exception {
    context.setDatabase("myWiki");
    String celFileBaseName = "Content_attachments";
    SpaceReference celFileBaseRef = new SpaceReference(celFileBaseName, new WikiReference(
        context.getDatabase()));
    expect(xwiki.getSpacePreference(eq("cel_centralfilebase"), eq(""), same(context))).andReturn(
        celFileBaseName).anyTimes();
    DocumentReference tagDocRef = new DocumentReference(context.getDatabase(), celFileBaseName,
        "tag0");
    expect(xwiki.exists(eq(tagDocRef), same(context))).andReturn(false).atLeastOnce();
    DocumentReference tagDocRef2 = new DocumentReference(context.getDatabase(), celFileBaseName,
        "tag1");
    expect(mockTreeNodeSrv.getSubNodesForParent(eq(celFileBaseRef), isA(
        InternalRightsFilter.class))).andReturn(Arrays.asList(new TreeNode(tagDocRef2, null,
            0))).atLeastOnce();
    XWikiDocument existingTagDoc = new XWikiDocument(tagDocRef);
    existingTagDoc.setNew(false);
    expect(getMock(XWikiDocumentCreator.class).create(eq(tagDocRef))).andReturn(existingTagDoc);
    BaseClass menuItemBaseClass = createMockAndAddToDefault(BaseClass.class);
    expect(xwiki.getXClass(eq(navClassConfig.getMenuItemClassRef()), same(context))).andReturn(
        menuItemBaseClass).once();
    BaseObject expectedMenuItemObj = new BaseObject();
    expectedMenuItemObj.setXClassReference(navClassConfig.getMenuItemClassRef());
    expect(menuItemBaseClass.newCustomClassInstance(same(context))).andReturn(expectedMenuItemObj);
    Capture<XWikiDocument> savedDocCapture = new Capture<>();
    xwiki.saveDocument(capture(savedDocCapture), isA(String.class), eq(false), same(context));
    expectLastCall().once();
    replayDefault();
    try {
      fileBaseTagCmd.getOrCreateTagDocument("tag0", true);
      fail("FailedToCreateTagException expected");
    } catch (FailedToCreateTagException exp) {
      // expected
    }
    XWikiDocument savedTagDocument = savedDocCapture.getValue();
    BaseObject menuItemObj = savedTagDocument.getXObject(navClassConfig.getMenuItemClassRef());
    assertNotNull("expecting attached object", menuItemObj);
    assertEquals("expecting attached object", 1, menuItemObj.getIntValue(
        INavigationClassConfig.MENU_POSITION_FIELD));
    verifyDefault();
  }

}
