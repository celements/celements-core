package com.celements.navigation.service;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractBridgedComponentTestCase;
import com.celements.inheritor.InheritorFactory;
import com.celements.navigation.Navigation;
import com.celements.navigation.TreeNode;
import com.celements.navigation.cmd.GetMappedMenuItemsForParentCommand;
import com.celements.navigation.cmd.GetNotMappedMenuItemsForParentCommand;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.user.api.XWikiRightService;
import com.xpn.xwiki.web.Utils;

public class TreeNodeServiceTest extends AbstractBridgedComponentTestCase {

  private XWikiContext context;
  private XWiki wiki;
  private XWikiStoreInterface mockStore;
  private TreeNodeService treeNodeService;
  private ITreeNodeCache mockTreeNodeCache;
  private GetNotMappedMenuItemsForParentCommand mockGetNotMenuItemCommand;
  private GetMappedMenuItemsForParentCommand mockGetMenuItemCommand;

  @Before
  public void setUp_TreeNodeServiceTest() throws Exception {
    context = getContext();
    wiki = createMock(XWiki.class);
    context.setWiki(wiki);
    mockStore = createMock(XWikiStoreInterface.class);
    treeNodeService = (TreeNodeService) Utils.getComponent(ITreeNodeService.class);
    expect(wiki.getStore()).andReturn(mockStore).anyTimes();
    mockTreeNodeCache = createMock(ITreeNodeCache.class);
    treeNodeService.treeNodeCache = mockTreeNodeCache;
    treeNodeService.execution = getComponentManager().lookup(Execution.class);
    treeNodeService.serializer = getComponentManager().lookup(
        EntityReferenceSerializer.class);
    mockGetNotMenuItemCommand = createMock(GetNotMappedMenuItemsForParentCommand.class);
    expect(mockTreeNodeCache.getNotMappedMenuItemsForParentCmd()).andReturn(
        mockGetNotMenuItemCommand).anyTimes();
    mockGetMenuItemCommand = createMock(
        GetMappedMenuItemsForParentCommand.class);
    expect(mockTreeNodeCache.getMappedMenuItemsForParentCmd()).andReturn(
        mockGetMenuItemCommand).anyTimes();
  }

  @Test
  public void testGetSubNodesForParent() throws Exception {
    String
      wikiName = "myWiki",
      spaceName = "mySpace",
      docName = "myDoc",
      parentKey = wikiName+":"+spaceName+".";    
    context.setDatabase(wikiName);
    EntityReference spaceRef = new SpaceReference(spaceName,
        new WikiReference(context.getDatabase()));
    TreeNode treeNode = createTreeNode(spaceName, docName, spaceName, "", 1);
    List<TreeNode> mockTreeNodeList = Arrays.asList(treeNode, null);
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey),
        same(context))).andReturn(mockTreeNodeList);
    List<TreeNode> emptyList = Collections.emptyList();
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey),
        same(context))).andReturn(emptyList);
    XWikiRightService mockRightService = createMock(XWikiRightService.class);
    expect(wiki.getRightService()).andReturn(mockRightService).anyTimes();
    expect(mockRightService.hasAccessLevel(eq("view"), eq("XWiki.XWikiGuest"),
        eq(spaceName+"."+docName), same(context))).andReturn(true);
    replayAll(mockRightService);
    List<TreeNode> resultList = treeNodeService.getSubNodesForParent(spaceRef, "");
    assertEquals(1, resultList.size());
    assertTrue(resultList.contains(treeNode));
    verifyAll(mockRightService);
  }

  @Test
  public void testFetchNodesForParentKey_mergeCombinedResult(){
    String
      wikiName = "myWiki",
      spaceName = "mySpace",
      docName = "myDoc",
      parentKey = wikiName+":"+spaceName+"."+docName;    
    context.setDatabase(wikiName);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), spaceName, 
        docName);
    
    TreeNode menuItem1 = createTreeNode(spaceName, "myDoc1", spaceName, docName, 1);
    TreeNode menuItem2 = createTreeNode(spaceName, "myDoc2", spaceName, docName, 2);
    TreeNode menuItem3 = createTreeNode(spaceName, "myDoc1", spaceName, docName, 3);
    TreeNode menuItem5 = createTreeNode(spaceName, "myDoc5", spaceName, docName, 5);
    List<TreeNode>
      mappedList = Arrays.asList(menuItem1, menuItem5),
      notMappedList = Arrays.asList(menuItem2, menuItem3),
      expectedList = Arrays.asList(menuItem1, menuItem2, menuItem3, menuItem5);
    
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))
        ).andReturn(mappedList).once();
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey),
        same(context))).andReturn(notMappedList).once();
    
    replayAll();
    List<TreeNode> menuItemsMerged = treeNodeService.fetchNodesForParentKey(docRef);
    assertEquals("result array does not match expected size.", expectedList.size(),
        menuItemsMerged.size());
    int pos = 0;
    for (TreeNode menuItem : menuItemsMerged) {
      TreeNode expectedMenuitem = expectedList.get(pos++);
      assertEquals("Array compare failed on item " + pos, expectedMenuitem.getPosition(),
          menuItem.getPosition());
      assertSame("Array compare failed on item " + pos, expectedMenuitem, menuItem);
    }
    verifyAll();
  }

  @Test
  public void testFetchNodesForParentKey_onlyOldArray() throws Exception {
    String
      wikiName = "myWiki",
      spaceName = "mySpace",
      docName = "myDoc",
      parentKey = wikiName+":"+spaceName+"."+docName;    
    context.setDatabase(wikiName);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), spaceName, 
        docName);
  
    TreeNode menuItem2 = createTreeNode(spaceName, "myDoc2", spaceName, docName, 2);
    TreeNode menuItem3 = createTreeNode(spaceName, "myDoc1", spaceName, docName, 3);
    List<TreeNode> oldNotMappedList = Arrays.asList(menuItem2, menuItem3);
    List<TreeNode> mappedList = Collections.emptyList();
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))
        ).andReturn(mappedList).once();
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context)
        )).andReturn(oldNotMappedList).once();
    replayAll();
    List<TreeNode> menuItemsMerged = treeNodeService.fetchNodesForParentKey(docRef);
    assertSame("expecting old notMapped list.", oldNotMappedList, menuItemsMerged);
    verifyAll();
  }

  @Test
  public void testFetchNodesForParentKey_onlyNewMappedList() {
    String
      wikiName = "myWiki",
      spaceName = "mySpace",
      docName = "myDoc",
      parentKey = wikiName+":"+spaceName+"."+docName;    
    context.setDatabase(wikiName);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), spaceName, 
        docName);
    
    List<TreeNode> oldMenuItems = Collections.emptyList();
    TreeNode menuItem1 = createTreeNode(spaceName, "myDoc1", spaceName, docName, 1);
    TreeNode menuItem5 = createTreeNode(spaceName, "myDoc5", spaceName, docName, 5);
    List<TreeNode> mappedList = Arrays.asList(menuItem1, menuItem5);
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))
        ).andReturn(mappedList).once();
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey),
        same(context))).andReturn(oldMenuItems).once();
    replayAll();
    List<TreeNode> menuItemsMerged = treeNodeService.fetchNodesForParentKey(docRef);
    assertSame("expecting old notMapped list.", mappedList, menuItemsMerged);
    verifyAll();
  }
  
  @Test
  public void testFetchNodesForParentKey_noMenuItems_NPE() {
    String
      wikiName = "myWiki",
      spaceName = "mySpace",
      docName = "myDoc",
      parentKey = wikiName+":"+spaceName+"."+docName;    
    context.setDatabase(wikiName);
    DocumentReference docRef = new DocumentReference(context.getDatabase(), spaceName, 
        docName);
    List<TreeNode> mappedList = Collections.emptyList();
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context))
        ).andReturn(mappedList).once();
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(eq(parentKey), same(context)
        )).andReturn(null).once();
    replayAll();
    List<TreeNode> menuItemsMerged = treeNodeService.fetchNodesForParentKey(docRef);
    assertNotNull("expecting not null.", menuItemsMerged);
    assertEquals("expecting empty list.", 0, menuItemsMerged.size());
    verifyAll();
  }


  private TreeNode createTreeNode(String docSpace, String docName, String parentDocSpace,
      String parentDocName, int pos) {
    return new TreeNode(new DocumentReference(context.getDatabase(), docSpace, docName),
        parentDocSpace + "." + parentDocName, pos);
  }
  
  @Test
  public void testGetMaxConfiguredNavigationLevel_twoParents() throws Exception {
    InheritorFactory inheritorFact = new InheritorFactory();
    PageLayoutCommand mockPageLayoutCmd = createMock(PageLayoutCommand.class);
    inheritorFact.injectPageLayoutCmd(mockPageLayoutCmd);
    treeNodeService.injectInheritorFactory(inheritorFact);
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    expect(mockPageLayoutCmd.getPageLayoutForDoc(eq(doc.getFullName()), same(context))
        ).andReturn(null).atLeastOnce();
    context.setDoc(doc);
    XWikiDocument webPrefDoc = new XWikiDocument();
    webPrefDoc.setFullName("MySpace.WebPreferences");
    expect(wiki.getDocument(eq("MySpace.WebPreferences"), eq(context))).andReturn(
        webPrefDoc).atLeastOnce();
    Vector<BaseObject> navObjects = new Vector<BaseObject>();
    navObjects.add(createNavObj(5, webPrefDoc));
    navObjects.add(createNavObj(4, webPrefDoc));
    navObjects.add(createNavObj(8, webPrefDoc));
    navObjects.add(createNavObj(3, webPrefDoc));
    expect(wiki.getDocument(eq(new DocumentReference("xwikidb", "MySpace", 
        "WebPreferences")), same(context))).andReturn(webPrefDoc).atLeastOnce();
    webPrefDoc.setObjects(Navigation.NAVIGATION_CONFIG_CLASS, navObjects);
    expect(wiki.getWebPreference(eq("skin"), same(context))).andReturn("Skins.MySkin"
        ).atLeastOnce();
    replayAll(mockPageLayoutCmd);
    int maxLevel = treeNodeService.getMaxConfiguredNavigationLevel();
    verifyAll(mockPageLayoutCmd);
    assertEquals("Max to Level in navConfigs is 8.", 8, maxLevel);
  }

  @Test
  public void testGetMaxConfiguredNavigationLevel_deletedObject_NPE() throws Exception {
    InheritorFactory inheritorFact = new InheritorFactory();
    PageLayoutCommand mockPageLayoutCmd = createMock(PageLayoutCommand.class);
    inheritorFact.injectPageLayoutCmd(mockPageLayoutCmd);
    treeNodeService.injectInheritorFactory(inheritorFact);
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    expect(mockPageLayoutCmd.getPageLayoutForDoc(eq(doc.getFullName()), same(context))
        ).andReturn(null).atLeastOnce();
    context.setDoc(doc);
    XWikiDocument webPrefDoc = new XWikiDocument();
    webPrefDoc.setFullName("MySpace.WebPreferences");
    expect(wiki.getDocument(eq("MySpace.WebPreferences"), eq(context))
        ).andReturn(webPrefDoc).atLeastOnce();
    Vector<BaseObject> navObjects = new Vector<BaseObject>();
    navObjects.add(createNavObj(5, webPrefDoc));
    navObjects.add(null); // deleting an object can lead to a null pointer
                          // in the object list
    navObjects.add(createNavObj(8, webPrefDoc));
    navObjects.add(createNavObj(3, webPrefDoc));
    webPrefDoc.setObjects(Navigation.NAVIGATION_CONFIG_CLASS, navObjects);
    expect(wiki.getDocument(eq(new DocumentReference("xwikidb", "MySpace", 
        "WebPreferences")), same(context))).andReturn(webPrefDoc).atLeastOnce();
    expect(wiki.getWebPreference(eq("skin"), same(context))).andReturn("Skins.MySkin"
      ).atLeastOnce();
    replayAll(mockPageLayoutCmd);
    int maxLevel = treeNodeService.getMaxConfiguredNavigationLevel();
    verifyAll(mockPageLayoutCmd);
    assertEquals("Max to Level in navConfigs is 8.", 8, maxLevel);
  }

  @Test
  public void testGetMaxConfiguredNavigationLevel_noObjectFound_NPE() throws Exception {
    InheritorFactory inheritorFact = new InheritorFactory();
    PageLayoutCommand mockPageLayoutCmd = createMock(PageLayoutCommand.class);
    inheritorFact.injectPageLayoutCmd(mockPageLayoutCmd);
    treeNodeService.injectInheritorFactory(inheritorFact);
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    expect(mockPageLayoutCmd.getPageLayoutForDoc(eq(doc.getFullName()), same(context))
        ).andReturn(null).atLeastOnce();
    context.setDoc(doc);
    XWikiDocument webPrefDoc = new XWikiDocument();
    webPrefDoc.setFullName("MySpace.WebPreferences");
    expect(wiki.getDocument(eq("MySpace.WebPreferences"), eq(context))
      ).andReturn(webPrefDoc).atLeastOnce();
    XWikiDocument xwikiPrefDoc = new XWikiDocument();
    webPrefDoc.setFullName("XWiki.XWikiPreferences");
    expect(wiki.getDocument(eq("XWiki.XWikiPreferences"), eq(context))
      ).andReturn(xwikiPrefDoc).atLeastOnce();
    XWikiDocument skinDoc = new XWikiDocument();
    skinDoc.setFullName("Skins.MySkin");
    expect(wiki.getDocument(eq("Skins.MySkin"), eq(context))
      ).andReturn(skinDoc).atLeastOnce();
    expect(wiki.getWebPreference(eq("skin"), same(context))).andReturn("Skins.MySkin"
      ).atLeastOnce();
    replayAll(mockPageLayoutCmd);
    int maxLevel = treeNodeService.getMaxConfiguredNavigationLevel();
    verifyAll(mockPageLayoutCmd);
    assertEquals("Expecting default max level.", Navigation.DEFAULT_MAX_LEVEL, maxLevel);
  }

  @Test
  public void testGetMaxConfiguredNavigationLevel_threeParents() throws Exception {
    InheritorFactory inheritorFact = new InheritorFactory();
    PageLayoutCommand mockPageLayoutCmd = createMock(PageLayoutCommand.class);
    inheritorFact.injectPageLayoutCmd(mockPageLayoutCmd);
    treeNodeService.injectInheritorFactory(inheritorFact);
    XWikiDocument doc = new XWikiDocument();
    doc.setFullName("MySpace.MyDocument");
    expect(mockPageLayoutCmd.getPageLayoutForDoc(eq(doc.getFullName()), same(context))
        ).andReturn(null).atLeastOnce();
    context.setDoc(doc);
    XWikiDocument webPrefDoc = new XWikiDocument();
    webPrefDoc.setFullName("MySpace.WebPreferences");
    expect(wiki.getDocument(eq("MySpace.WebPreferences"), eq(context))
        ).andReturn(webPrefDoc).atLeastOnce();
    expect(wiki.getWebPreference(eq("skin"), same(context))).andReturn("Skins.MySkin"
      ).atLeastOnce();
    Vector<BaseObject> navObjects = new Vector<BaseObject>();
    navObjects.add(createNavObj(5, webPrefDoc));
    navObjects.add(createNavObj(4, webPrefDoc));
    navObjects.add(createNavObj(3, webPrefDoc));
    webPrefDoc.setObjects(Navigation.NAVIGATION_CONFIG_CLASS, navObjects);
    expect(wiki.getDocument(eq(new DocumentReference("xwikidb", "MySpace", 
        "WebPreferences")), same(context))).andReturn(webPrefDoc).atLeastOnce();
    replayAll(mockPageLayoutCmd);
    int maxLevel = treeNodeService.getMaxConfiguredNavigationLevel();
    verifyAll(mockPageLayoutCmd);
    assertEquals("Parents are a.b, b.c and c.d therefor maxlevel must be 5.",
        5, maxLevel);
  }
  
  private BaseObject createNavObj(int toLevel, XWikiDocument doc) {
    BaseObject navObj = new BaseObject();
    navObj.setClassName(Navigation.NAVIGATION_CONFIG_CLASS);
    navObj.setStringValue("menu_element_name", "mainMenu");
    navObj.setIntValue("to_hierarchy_level", toLevel);
    navObj.setName(doc.getFullName());
    navObj.setDocumentReference(doc.getDocumentReference());
    return navObj;
  }
  
  @Test
  public void getSiblingMenuItem_previous() throws XWikiException {
    String
      db = "siblingPrevious",
      space = "Celements2",
      celDocName = "MenuItem",
      fullName = db+":"+space+"."+celDocName;
    
    context.setDatabase(db);
    
    EntityReference entRefCel = new EntityReference(celDocName, EntityType.DOCUMENT, 
        new EntityReference(space, EntityType.SPACE,
            new EntityReference(db, EntityType.WIKI)));
    DocumentReference
      mItemDocRef = new DocumentReference(context.getDatabase(),"mySpace","myMenuItemDoc"),   
      docRefPrev = new DocumentReference(context.getDatabase(),"mySpace","DocPrev"),
      docRefNext = new DocumentReference(context.getDatabase(),"mySpace","DocNext");
    XWikiDocument 
      doc = new XWikiDocument(mItemDocRef),
      docPrev = new XWikiDocument(docRefPrev),
      docNext = new XWikiDocument(docRefNext);
    BaseObject
      menuItemItemDoc = new BaseObject(),
      menuItemPrev = new BaseObject(),
      menuItemNext = new BaseObject();
    
    doc.setParentReference(entRefCel);
    docPrev.setParentReference(entRefCel);
    docNext.setParentReference(entRefCel);
    
    menuItemItemDoc.setDocumentReference(mItemDocRef);
    menuItemPrev.setDocumentReference(docRefPrev);
    menuItemNext.setDocumentReference(docRefNext);
    menuItemItemDoc.setXClassReference(entRefCel);
    menuItemPrev.setXClassReference(entRefCel);
    menuItemNext.setXClassReference(entRefCel);
    doc.setXObject(0, menuItemItemDoc);
    docPrev.setXObject(0, menuItemPrev);
    docNext.setXObject(0, menuItemNext);
    
    context.setDoc(doc);
    
    expect(wiki.getDocument(eq(mItemDocRef), same(context))).andReturn(doc).anyTimes();
    expect(wiki.getDocument(eq(docRefPrev), same(context))).andReturn(docPrev).anyTimes();
    expect(wiki.getDocument(eq(docRefNext), same(context))).andReturn(docNext).anyTimes();
    
    TreeNode
      tnPrev = new TreeNode(docRefPrev, fullName, 0),
      tnItem = new TreeNode(mItemDocRef, fullName, 1),
      tnNext = new TreeNode(docRefNext, fullName, 2);  
    List<TreeNode> nodes = new ArrayList<TreeNode>();
    nodes.add(tnPrev); nodes.add(tnItem); nodes.add(tnNext);
    
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(
        eq(fullName), same(context))).andReturn(nodes).times(2);
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(
        eq(fullName), same(context))
        ).andReturn(new ArrayList<TreeNode>()).times(2);

    XWikiRightService rightServiceMock = createMock(XWikiRightService.class);
    expect(wiki.getRightService()).andReturn(rightServiceMock).anyTimes();
    expect(rightServiceMock.hasAccessLevel(eq("view"), isA(String.class),
        isA(String.class), same(context))).andReturn(true).anyTimes();
    replayAll(rightServiceMock);
    TreeNode prevMenuItem = treeNodeService.getSiblingMenuItem(mItemDocRef, true);
    assertEquals("MySpace.DocPrev TreeNode expected.", tnPrev, prevMenuItem);
    verifyAll(rightServiceMock);
  }

  @Test
  public void getSiblingMenuItem_next() throws XWikiException {
    String
      db = "siblingPrevious",
      space = "Celements2",
      celDocName = "MenuItem",
      fullName = db+":"+space+"."+celDocName;
    
    context.setDatabase(db);
    
    EntityReference entRefCel = new EntityReference(celDocName, EntityType.DOCUMENT, 
        new EntityReference(space, EntityType.SPACE,
            new EntityReference(db, EntityType.WIKI)));
    DocumentReference
      mItemDocRef = new DocumentReference(context.getDatabase(),"mySpace","myMenuItemDoc"),   
      docRefPrev = new DocumentReference(context.getDatabase(),"mySpace","DocPrev"),
      docRefNext = new DocumentReference(context.getDatabase(),"mySpace","DocNext");
    XWikiDocument 
      doc = new XWikiDocument(mItemDocRef),
      docPrev = new XWikiDocument(docRefPrev),
      docNext = new XWikiDocument(docRefNext);
    BaseObject
      menuItemItemDoc = new BaseObject(),
      menuItemPrev = new BaseObject(),
      menuItemNext = new BaseObject();
    
    doc.setParentReference(entRefCel);
    docPrev.setParentReference(entRefCel);
    docNext.setParentReference(entRefCel);
    
    menuItemItemDoc.setDocumentReference(mItemDocRef);
    menuItemPrev.setDocumentReference(docRefPrev);
    menuItemNext.setDocumentReference(docRefNext);
    menuItemItemDoc.setXClassReference(entRefCel);
    menuItemPrev.setXClassReference(entRefCel);
    menuItemNext.setXClassReference(entRefCel);
    doc.setXObject(0, menuItemItemDoc);
    docPrev.setXObject(0, menuItemPrev);
    docNext.setXObject(0, menuItemNext);
    
    context.setDoc(doc);
    
    expect(wiki.getDocument(eq(mItemDocRef), same(context))).andReturn(doc).anyTimes();
    expect(wiki.getDocument(eq(docRefPrev), same(context))).andReturn(docPrev).anyTimes();
    expect(wiki.getDocument(eq(docRefNext), same(context))).andReturn(docNext).anyTimes();
    
    TreeNode
      tnPrev = new TreeNode(docRefPrev, fullName, 0),
      tnItem = new TreeNode(mItemDocRef, fullName, 1),
      tnNext = new TreeNode(docRefNext, fullName, 2);  
    List<TreeNode> nodes = new ArrayList<TreeNode>();
    nodes.add(tnPrev); nodes.add(tnItem); nodes.add(tnNext);
    
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(
        eq(fullName), same(context))).andReturn(nodes).times(2);
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(
        eq(fullName), same(context))
        ).andReturn(new ArrayList<TreeNode>()).times(2);

    XWikiRightService rightServiceMock = createMock(XWikiRightService.class);
    expect(wiki.getRightService()).andReturn(rightServiceMock).anyTimes();
    expect(rightServiceMock.hasAccessLevel(eq("view"), isA(String.class),
        isA(String.class), same(context))).andReturn(true).anyTimes();
    replayAll(rightServiceMock);
    TreeNode prevMenuItem = treeNodeService.getSiblingMenuItem(mItemDocRef, false);
    assertEquals("MySpace.DocNext TreeNode expected.", tnNext, prevMenuItem);
    verifyAll(rightServiceMock);
  }
  
  @Test
  public void getSiblingMenuItem_next_docNotInContextSpace() throws XWikiException {
      String
      db = "siblingPrevious",
      space = "Celements2",
      celDocName = "MenuItem",
      fullName = db+":"+space+"."+celDocName;
    
    context.setDatabase(db);
    
    EntityReference entRefCel = new EntityReference(celDocName, EntityType.DOCUMENT, 
        new EntityReference(space, EntityType.SPACE,
            new EntityReference(db, EntityType.WIKI)));
    DocumentReference
      mItemDocRef = new DocumentReference(context.getDatabase(),"mySpace","myMenuItemDoc"),   
      docRefPrev = new DocumentReference(context.getDatabase(),"mySpace","DocPrev"),
      docRefNext = new DocumentReference(context.getDatabase(),"mySpace","DocNext");
    XWikiDocument 
      doc = new XWikiDocument(mItemDocRef),
      docPrev = new XWikiDocument(docRefPrev),
      docNext = new XWikiDocument(docRefNext);
    BaseObject
      menuItemItemDoc = new BaseObject(),
      menuItemPrev = new BaseObject(),
      menuItemNext = new BaseObject();
    
    doc.setParentReference(entRefCel);
    docPrev.setParentReference(entRefCel);
    docNext.setParentReference(entRefCel);
    
    menuItemItemDoc.setDocumentReference(mItemDocRef);
    menuItemPrev.setDocumentReference(docRefPrev);
    menuItemNext.setDocumentReference(docRefNext);
    menuItemItemDoc.setXClassReference(entRefCel);
    menuItemPrev.setXClassReference(entRefCel);
    menuItemNext.setXClassReference(entRefCel);
    doc.setXObject(0, menuItemItemDoc);
    docPrev.setXObject(0, menuItemPrev);
    docNext.setXObject(0, menuItemNext);
    
    context.setDoc(doc);
    
    expect(wiki.getDocument(eq(mItemDocRef), same(context))).andReturn(doc).anyTimes();
    expect(wiki.getDocument(eq(docRefPrev), same(context))).andReturn(docPrev).anyTimes();
    expect(wiki.getDocument(eq(docRefNext), same(context))).andReturn(docNext).anyTimes();
    
    TreeNode
      tnPrev = new TreeNode(docRefPrev, fullName, 0),
      tnItem = new TreeNode(mItemDocRef, fullName, 1);
    List<TreeNode> nodes = new ArrayList<TreeNode>();
    nodes.add(tnPrev); nodes.add(tnItem);
    
    expect(mockGetNotMenuItemCommand.getTreeNodesForParentKey(
        eq(fullName), same(context))).andReturn(nodes).times(2);
    expect(mockGetMenuItemCommand.getTreeNodesForParentKey(
        eq(fullName), same(context))
        ).andReturn(new ArrayList<TreeNode>()).times(2);
  
    XWikiRightService rightServiceMock = createMock(XWikiRightService.class);
    expect(wiki.getRightService()).andReturn(rightServiceMock).anyTimes();
    expect(rightServiceMock.hasAccessLevel(eq("view"), isA(String.class),
        isA(String.class), same(context))).andReturn(true).anyTimes();
    replayAll(rightServiceMock);
    TreeNode prevMenuItem = treeNodeService.getSiblingMenuItem(mItemDocRef, false);
    assertEquals("null expected.", null, prevMenuItem);
    verifyAll(rightServiceMock);
  }
  
  @Test
  public void testGetParentKey() throws XWikiException { 
    String
      wikiName = context.getDatabase(),
      spaceName = "mySpace",
      docName = "myDoc";
    
    WikiReference wikiRef = new WikiReference(context.getDatabase());
    SpaceReference spaceRef = new SpaceReference("mySpace", wikiRef);
    DocumentReference docRef = new DocumentReference(docName, spaceRef);
    assertEquals(wikiName+":", treeNodeService.getParentKey(wikiRef, true));
    assertEquals(wikiName+":"+spaceName+".",
        treeNodeService.getParentKey(spaceRef, true));
    assertEquals(wikiName+":"+spaceName+"."+docName,
        treeNodeService.getParentKey(docRef, true));
    assertEquals("", treeNodeService.getParentKey(wikiRef, false));
    assertEquals(spaceName+".",
        treeNodeService.getParentKey(spaceRef, false));
    assertEquals(spaceName+"."+docName,
        treeNodeService.getParentKey(docRef, false));
  }
  
  private void replayAll(Object ... mocks) {
    replay(mockStore, wiki, mockTreeNodeCache, mockGetNotMenuItemCommand,
        mockGetMenuItemCommand);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(mockStore, wiki, mockTreeNodeCache, mockGetNotMenuItemCommand,
        mockGetMenuItemCommand);
    verify(mocks);
  }

}
