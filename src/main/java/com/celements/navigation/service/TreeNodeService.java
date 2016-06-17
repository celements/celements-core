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
package com.celements.navigation.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.inheritor.InheritorFactory;
import com.celements.iterator.XObjectIterator;
import com.celements.navigation.INavigationClassConfig;
import com.celements.navigation.Navigation;
import com.celements.navigation.TreeNode;
import com.celements.navigation.cmd.GetMappedMenuItemsForParentCommand;
import com.celements.navigation.filter.INavFilter;
import com.celements.navigation.filter.InternalRightsFilter;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseCollection;
import com.xpn.xwiki.objects.BaseObject;

/**
 * TreeNodeService access a tree node structure through the method of this service.
 *
 * @author Fabian Pichler
 */
@Component
public class TreeNodeService implements ITreeNodeService {

  private static Logger LOGGER = LoggerFactory.getLogger(TreeNodeService.class);

  public PageLayoutCommand pageLayoutCmd;

  @Requirement
  ITreeNodeCache treeNodeCache;

  @Requirement
  IWebUtilsService webUtilsService;

  private InheritorFactory injectedInheritorFactory;

  @Requirement
  Map<String, ITreeNodeProvider> nodeProviders;

  @Requirement
  private INavigationClassConfig navClassConfig;

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  private PageLayoutCommand getPageLayoutCmd() {
    if (pageLayoutCmd == null) {
      pageLayoutCmd = new PageLayoutCommand();
    }
    return pageLayoutCmd;
  }

  @Override
  public int getActiveMenuItemPos(int menuLevel, String menuPart) {
    List<DocumentReference> parents = webUtilsService.getDocumentParentsList(
        getContext().getDoc().getDocumentReference(), true);
    if (parents.size() >= menuLevel) {
      return getMenuItemPos(parents.get(parents.size() - menuLevel), menuPart);
    }
    return -1;
  }

  @Override
  public int getMenuItemPos(DocumentReference docRef, String menuPart) {
    try {
      EntityReference parent = getParentEntityRef(docRef);
      int pos = -1;
      for (TreeNode menuItem : getSubNodesForParent(parent, menuPart)) {
        pos = pos + 1;
        if (docRef.equals(menuItem.getDocumentReference())) {
          return pos;
        }
      }
    } catch (XWikiException exp) {
      LOGGER.error("getMenuItemPos failed on getParentEntityRef", exp);
    }
    return -1;
  }

  @Override
  public boolean isTreeNode(DocumentReference docRef) {
    // TODO move to ITreeNodeProvider and integrate over all nodeProviders
    try {
      XWikiDocument document = getContext().getWiki().getDocument(docRef, getContext());
      List<BaseObject> menuItems = document.getXObjects(navClassConfig.getMenuItemClassRef(
          getContext().getDatabase()));
      return ((menuItems != null) && !menuItems.isEmpty());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get document for reference [{}].", docRef, exp);
    }
    return false;
  }

  /**
   * @deprecated since 2.17.0 use getSubNodesForParent(EntityReference, INavFilter) or
   *             getSubNodesForParent(EntityReference, String) instead
   */
  @Deprecated
  @Override
  public <T> List<TreeNode> getSubNodesForParent(String parent, String menuSpace,
      INavFilter<T> filter) {
    if ("".equals(menuSpace)) {
      menuSpace = getContext().getDoc().getDocumentReference().getLastSpaceReference().getName();
    }
    String parentKey = getParentKey(parent, menuSpace);
    return getSubNodesForParent(resolveEntityReference(parentKey), filter);
  }

  String getParentKey(String parent, String menuSpace) {
    String parentKey = "";
    if (parent != null) {
      parentKey = parent;
    }
    if (parentKey.indexOf('.') < 0) {
      parentKey = menuSpace + "." + parentKey;
    }
    if (parentKey.indexOf(':') < 0) {
      parentKey = getContext().getDatabase() + ":" + parentKey;
    }
    return parentKey;
  }

  String getParentKey(EntityReference reference, boolean withDataBase) {
    String parentKey = "";
    if (reference != null) {
      parentKey = webUtilsService.getRefDefaultSerializer().serialize(reference);
      if (!parentKey.contains(":") && !parentKey.endsWith(":")) {
        parentKey += ":";
      } else if (!parentKey.contains(".") && !parentKey.endsWith(".")) {
        parentKey += ".";
      }
      if (!withDataBase) {
        parentKey = parentKey.substring(parentKey.indexOf(":") + 1);
      }
    }
    LOGGER.debug("getParentKey: returning [{}] for entityref [{}].", parentKey, reference);
    return parentKey;
  }

  @Override
  public <T> List<TreeNode> getSubNodesForParent(EntityReference entRef, INavFilter<T> filter) {
    LOGGER.trace("getSubNodesForParent: entRef {}] filter class [{}].", entRef, filter.getClass());
    ArrayList<TreeNode> menuArray = new ArrayList<TreeNode>();
    for (TreeNode node : fetchNodesForParentKey(entRef)) {
      if ((node != null) && filter.includeTreeNode(node, getContext())) {
        // show only Menuitems of pages accessible to the current user
        menuArray.add(node);
      } else {
        LOGGER.debug("getSubNodesForParent: omit [{}].", node);
      }
    }
    return menuArray;
  }

  /**
   * @deprecated since 2.17.0 use getSubNodesForParent(EntityReference, INavFilter) or
   *             getSubNodesForParent(EntityReference, String) instead
   */
  @Deprecated
  @Override
  public List<TreeNode> getSubNodesForParent(String parent, String menuSpace, String menuPart) {
    InternalRightsFilter filter = new InternalRightsFilter();
    filter.setMenuPart(menuPart);
    List<TreeNode> subNodesForParent = getSubNodesForParent(parent, menuSpace, filter);
    LOGGER.debug("getSubNodesForParent deprecated use: parent [{}] menuSpace [{}] menuPart [{}]"
        + " returning [{}].", parent, menuSpace, menuPart, subNodesForParent.size());
    return subNodesForParent;
  }

  @Override
  public List<TreeNode> getSubNodesForParent(EntityReference entRef, String menuPart) {
    InternalRightsFilter filter = new InternalRightsFilter();
    filter.setMenuPart(menuPart);
    return getSubNodesForParent(entRef, filter);
  }

  /**
   * fetchNodesForParentKey
   *
   * @param parentKey
   * @param context
   * @return Collection keeps ordering of TreeNodes according to posId
   */
  List<TreeNode> fetchNodesForParentKey(EntityReference parentRef) {
    String parentKey = getParentKey(parentRef, true);
    LOGGER.trace("fetchNodesForParentKey: parentRef [{}] parentKey [{}].", parentRef, parentKey);
    long starttotal = System.currentTimeMillis();
    long start = System.currentTimeMillis();
    List<TreeNode> nodes = fetchNodesForParentKey_internal(parentKey, starttotal, start);
    if ((nodeProviders != null) && (nodeProviders.values().size() > 0)) {
      TreeMap<Integer, TreeNode> treeNodesMergedMap = new TreeMap<Integer, TreeNode>();
      for (TreeNode node : nodes) {
        treeNodesMergedMap.put(node.getPosition(), node);
      }
      for (ITreeNodeProvider tnProvider : nodeProviders.values()) {
        try {
          for (TreeNode node : tnProvider.getTreeNodesForParent(parentKey)) {
            treeNodesMergedMap.put(node.getPosition(), node);
          }
        } catch (Exception exp) {
          LOGGER.warn("Failed on provider [{}] to get nodes for parentKey [{}].",
              tnProvider.getClass(), parentKey, exp);
        }
      }
      nodes = new ArrayList<TreeNode>(treeNodesMergedMap.values());
      long end = System.currentTimeMillis();
      LOGGER.info("fetchNodesForParentKey: [{}] totaltime for list of [{}]: {}", parentKey,
          nodes.size(), (end - starttotal));
    }
    return nodes;
  }

  private List<TreeNode> fetchNodesForParentKey_internal(String parentKey, long starttotal,
      long start) {
    List<TreeNode> notMappedmenuItems = treeNodeCache.getNotMappedMenuItemsForParentCmd().getTreeNodesForParentKey(
        parentKey);
    long end = System.currentTimeMillis();
    LOGGER.debug("fetchNodesForParentKey_internal: time for getNotMappedMenuItemsFromDatabase: {}",
        (end - start));
    start = System.currentTimeMillis();
    List<TreeNode> mappedTreeNodes = treeNodeCache.getMappedMenuItemsForParentCmd().getTreeNodesForParentKey(
        parentKey, getContext());
    end = System.currentTimeMillis();
    LOGGER.debug("fetchNodesForParentKey_internal: time for getMappedMenuItemsForParentCmd: {}",
        (end - start));
    start = System.currentTimeMillis();
    TreeMap<Integer, TreeNode> menuItemsMergedMap = null;
    if ((notMappedmenuItems == null) || (notMappedmenuItems.size() == 0)) {
      end = System.currentTimeMillis();
      LOGGER.info("fetchNodesForParentKey_internal: [{}] totaltime for list of [{}]: {}", parentKey,
          mappedTreeNodes.size(), (end - starttotal));
      return mappedTreeNodes;
    } else if (mappedTreeNodes.size() == 0) {
      end = System.currentTimeMillis();
      LOGGER.info("fetchNodesForParentKey_internal: [{}] totaltime for list of [{}]: {}", parentKey,
          notMappedmenuItems.size(), (end - starttotal));
      return notMappedmenuItems;
    } else {
      // TODO refactor GetNotMappedMenuItemsForParentCommand and
      // TODO GetMappedMenuItemsForParentCommandas to ITreeNodeProvider
      menuItemsMergedMap = new TreeMap<Integer, TreeNode>();
      for (TreeNode node : notMappedmenuItems) {
        menuItemsMergedMap.put(node.getPosition(), node);
      }
      for (TreeNode node : mappedTreeNodes) {
        menuItemsMergedMap.put(node.getPosition(), node);
      }
      end = System.currentTimeMillis();
      LOGGER.debug("fetchNodesForParentKey_internal: time for merging menu items: {}", (end
          - start));
      ArrayList<TreeNode> menuItems = new ArrayList<TreeNode>(menuItemsMergedMap.values());
      LOGGER.info("fetchNodesForParentKey_internal: [{}] totaltime for list of [{}]: {}", parentKey,
          menuItems.size(), (end - starttotal));
      return menuItems;
    }
  }

  /**
   * @param parentKey
   * @param context
   * @return Collection keeps ordering of menuItems according to posId
   * @deprecated since 2.14.0 use new fetchNodesForParentKey instead
   */
  @Deprecated
  List<BaseObject> fetchMenuItemsForXWiki(String parentKey) {
    long starttotal = System.currentTimeMillis();
    List<BaseObject> menuItemList = new ArrayList<BaseObject>();
    EntityReference refParent = resolveEntityReference(parentKey);
    for (TreeNode node : fetchNodesForParentKey(refParent)) {
      try {
        XWikiDocument itemdoc = getContext().getWiki().getDocument(node.getDocumentReference(),
            getContext());
        BaseObject cobj = itemdoc.getObject("Celements2.MenuItem");
        if (cobj != null) {
          menuItemList.add(cobj);
        }
      } catch (XWikiException exp) {
        LOGGER.error("failed to get doc for menuItem", exp);
      }
    }
    long end = System.currentTimeMillis();
    LOGGER.info("fetchMenuItemsForXWiki: [{}] totaltime for list of [{}]: {}", parentKey,
        menuItemList.size(), (end - starttotal));
    return menuItemList;
  }

  EntityReference resolveEntityReference(String name) {
    String wikiName = "", spaceName = "", docName = "";

    String[] name_a = name.split("\\.");
    String[] name_b = name_a[0].split(":");

    // wikiName:spaceName
    if (name_b.length > 1) {
      wikiName = name_b[0];
      spaceName = name_b[1];
      // wikiName:
    } else if (name_a[0].endsWith(":")) {
      if (name_b.length > 0) {
        wikiName = name_b[0];
      }
      // spaceName
    } else {
      wikiName = getContext().getDatabase();
      if (name_b.length > 0) {
        spaceName = name_b[0];
      }
    }
    if (name_a.length > 1) {
      docName = name_a[1];
    }

    EntityReference entRef = new EntityReference(wikiName, EntityType.WIKI);
    if (spaceName.length() > 0) {
      entRef = new EntityReference(spaceName, EntityType.SPACE, entRef);
      if (docName.length() > 0) {
        entRef = new EntityReference(docName, EntityType.DOCUMENT, entRef);
      }
    }
    LOGGER.debug("resolveEntityReference: for [{}] returning [{}].", name, entRef);
    return entRef;
  }

  /**
   * @deprecated since 2.14.0 use getSubNodesForParent(EntityReference, INavFilter) or
   *             getSubNodesForParent(EntityReference, String) instead
   */
  @Deprecated
  @Override
  public <T> List<T> getSubMenuItemsForParent(String parent, String menuSpace,
      INavFilter<T> filter) {
    if ("".equals(menuSpace)) {
      menuSpace = getContext().getDoc().getSpace();
    }
    String parentKey = getParentKey(parent, menuSpace);
    ArrayList<T> menuArray = new ArrayList<T>();
    for (BaseObject baseObj : fetchMenuItemsForXWiki(parentKey)) {
      if (filter.includeMenuItem(baseObj, getContext())) {
        // show only Menuitems of pages accessible to the current user
        menuArray.add(filter.convertObject(baseObj, getContext()));
      }
    }
    return menuArray;
  }

  @Override
  public Integer getMaxConfiguredNavigationLevel() {
    List<BaseObject> navConfigObjects = new Vector<BaseObject>();
    List<BaseObject> navConfigDocsObjs = getNavObjectsOnConfigDocs();
    if (navConfigDocsObjs != null) {
      navConfigObjects.addAll(navConfigDocsObjs);
    }
    List<BaseObject> navConfigLayoutObjs = getNavObjectsFromLayout();
    if (navConfigLayoutObjs != null) {
      navConfigObjects.addAll(navConfigLayoutObjs);
    }
    if ((navConfigObjects != null) && (!navConfigObjects.isEmpty())) {
      int maxLevel = 0;
      for (BaseObject navObj : navConfigObjects) {
        if (navObj != null) {
          maxLevel = Math.max(maxLevel, navObj.getIntValue(
              INavigationClassConfig.TO_HIERARCHY_LEVEL_FIELD));
        }
      }
      return maxLevel;
    }
    return Navigation.DEFAULT_MAX_LEVEL;
  }

  List<BaseObject> getNavObjectsFromLayout() {
    List<BaseObject> navObjects = new Vector<BaseObject>();
    SpaceReference layoutRef = getPageLayoutCmd().getPageLayoutForCurrentDoc();
    if (layoutRef != null) {
      XObjectIterator objectIterator = new XObjectIterator(getContext());
      List<String> layoutCellList = new Vector<String>();
      List<TreeNode> subNodesForParent = getSubNodesForParent(layoutRef, "");
      while (!subNodesForParent.isEmpty()) {
        List<TreeNode> newSubNodes = new Vector<TreeNode>();
        for (TreeNode node : subNodesForParent) {
          List<TreeNode> subNodes = getSubNodesForParent(node.getDocumentReference(), "");
          newSubNodes.addAll(subNodes);
          if (subNodes.isEmpty()) {
            layoutCellList.add(webUtilsService.getRefDefaultSerializer().serialize(
                node.getDocumentReference()));
          }
        }
        subNodesForParent = newSubNodes;
      }
      objectIterator.setDocList(layoutCellList);
      objectIterator.setClassName(INavigationClassConfig.NAVIGATION_CONFIG_CLASS);
      for (BaseObject navConfigObj : objectIterator) {
        navObjects.add(navConfigObj);
      }
    }
    return navObjects;
  }

  private List<BaseObject> getNavObjectsOnConfigDocs() {
    List<BaseObject> navConfigObjects2 = Collections.emptyList();
    try {
      BaseCollection navConfigObj = getInheritorFactory().getConfigDocFieldInheritor(
          INavigationClassConfig.NAVIGATION_CONFIG_CLASS, getParentKey(
              getContext().getDoc().getDocumentReference(), false), getContext()).getObject(
                  "menu_element_name");
      if (navConfigObj != null) {
        XWikiDocument navConfigDoc = getContext().getWiki().getDocument(
            navConfigObj.getDocumentReference(), getContext());
        String navConfigDocWikiName = navConfigDoc.getDocumentReference().getLastSpaceReference().getParent().getName();
        navConfigObjects2 = navConfigDoc.getXObjects(navClassConfig.getNavigationConfigClassRef(
            navConfigDocWikiName));
      } else {
        LOGGER.info("no config object found");
      }
    } catch (XWikiException exp) {
      LOGGER.error("unable to get configDoc.", exp);
    }
    return navConfigObjects2;
  }

  @Override
  public TreeNode getPrevMenuItem(DocumentReference docRef) throws XWikiException {
    return getSiblingMenuItem(docRef, true);
  }

  @Override
  public TreeNode getNextMenuItem(DocumentReference docRef) throws XWikiException {
    return getSiblingMenuItem(docRef, false);
  }

  TreeNode getSiblingMenuItem(DocumentReference docRef, boolean previous) throws XWikiException {
    XWikiDocument doc = getContext().getWiki().getDocument(docRef, getContext());
    BaseObject menuItem = doc.getXObject(getRef("Celements2", "MenuItem"));
    if (menuItem != null) {
      try {
        EntityReference parent = getParentEntityRef(docRef);
        List<TreeNode> subMenuItems = getSubNodesForParent(parent, menuItem.getStringValue(
            "part_name"));
        LOGGER.debug("getPrevMenuItem: {} subMenuItems found for parent '{}'. {}",
            subMenuItems.size(), parent, Arrays.deepToString(subMenuItems.toArray()));
        int pos = getMenuItemPos(docRef, menuItem.getStringValue("part_name"));
        if (previous && (pos > 0)) {
          return subMenuItems.get(pos - 1);
        } else if (!previous && (pos < (subMenuItems.size() - 1))) {
          return subMenuItems.get(pos + 1);
        }
        LOGGER.info("getPrevMenuItem: no previous MenuItem found for {}", getParentKey(docRef,
            true));
      } catch (XWikiException exp) {
        LOGGER.error("getSiblingMenuItem failed.", exp);
      }
    } else {
      LOGGER.debug("getPrevMenuItem: no MenuItem Object found on doc {}", getParentKey(docRef,
          true));
    }
    return null;
  }

  @Override
  public List<TreeNode> getMenuItemsForHierarchyLevel(int menuLevel, String menuPart) {
    DocumentReference parent = webUtilsService.getParentForLevel(menuLevel);
    if (parent != null) {
      List<TreeNode> submenuItems = getSubNodesForParent(parent, menuPart);
      LOGGER.debug("submenuItems for parent: {} ; {}", parent, submenuItems);
      return submenuItems;
    }
    LOGGER.debug("parent is null");
    return new ArrayList<TreeNode>();
  }

  @Override
  public void enableMappedMenuItems() {
    GetMappedMenuItemsForParentCommand cmd = new GetMappedMenuItemsForParentCommand();
    cmd.set_isActive(true);
    getContext().put(GetMappedMenuItemsForParentCommand.CELEMENTS_MAPPED_MENU_ITEMS_KEY, cmd);
  }

  DocumentReference getRef(String spaceName, String pageName) {
    return new DocumentReference(getContext().getDatabase(), spaceName, pageName);
  }

  /**
   * getParentReference returns only DocumentReference or SpaceReference even though the
   * return type is an EntityReference
   */
  @Override
  public EntityReference getParentReference(DocumentReference docRef) {
    try {
      return getParentEntityRef(docRef);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to getParentReference for [{}].", docRef, exp);
    }
    return null;
  }

  /**
   * getParentEntityRef returns only DocumentReference or SpaceReference even though the
   * return type is an EntityReference
   */
  @Override
  public EntityReference getParentEntityRef(DocumentReference docRef) throws XWikiException {
    EntityReference parentRef = getContext().getWiki().getDocument(docRef,
        getContext()).getParentReference();
    if ((parentRef == null) || (docRef.equals(parentRef))) {
      parentRef = docRef.getLastSpaceReference();
    }
    return parentRef;
  }

  /**
   * FOR TEST PURPOSES ONLY
   */
  public void injectInheritorFactory(InheritorFactory injectedInheritorFactory) {
    this.injectedInheritorFactory = injectedInheritorFactory;
  }

  private InheritorFactory getInheritorFactory() {
    if (injectedInheritorFactory != null) {
      return injectedInheritorFactory;
    }
    return new InheritorFactory();
  }

  /**
   * TODO write unit tests and test if working
   */
  @Override
  public void moveTreeDocAfter(DocumentReference moveDocRef, DocumentReference insertAfterDocRef)
      throws XWikiException {
    if (isTreeNode(moveDocRef)) {
      TreeNode insertAfterTreeNode = null;
      if (insertAfterDocRef != null) {
        insertAfterTreeNode = getTreeNodeForDocRef(insertAfterDocRef);
      }
      TreeNode moveTreeNode = getTreeNodeForDocRef(moveDocRef);
      ArrayList<TreeNode> newTreeNodes = moveTreeNodeAfter(moveTreeNode, insertAfterTreeNode);
      storeOrder(newTreeNodes);
    } else {
      LOGGER.info("Failed to moveTreeDocAfter for moveDocRef [{}] and insertAfterDocRef ["
          + "{}] because one of them is no TreeNode.", moveDocRef, insertAfterDocRef);
    }
  }

  ArrayList<TreeNode> moveTreeNodeAfter(TreeNode moveTreeNode, TreeNode insertAfterTreeNode) {
    List<TreeNode> treeNodes = new ArrayList<TreeNode>(fetchNodesForParentKey(
        moveTreeNode.getParentRef()));
    treeNodes.remove(moveTreeNode);
    ArrayList<TreeNode> newTreeNodes = new ArrayList<TreeNode>();
    int splitPos = 0;
    int maxPos = treeNodes.size();
    if (insertAfterTreeNode != null) {
      int newSplitPos = treeNodes.indexOf(insertAfterTreeNode);
      if (newSplitPos > -1) {
        splitPos = newSplitPos + 1;
      }
    }
    newTreeNodes.addAll(treeNodes.subList(0, splitPos));
    newTreeNodes.add(moveTreeNode);
    newTreeNodes.addAll(treeNodes.subList(splitPos, maxPos));
    return newTreeNodes;
  }

  @Override
  public TreeNode getTreeNodeForDocRef(DocumentReference docRef) throws XWikiException {
    EntityReference parentRef = getParentReference(docRef);
    TreeNode treeNode = null;
    XWikiDocument moveDoc = getContext().getWiki().getDocument(docRef, getContext());
    BaseObject menuItemObj = moveDoc.getXObject(navClassConfig.getMenuItemClassRef(
        getContext().getDatabase()));
    if (menuItemObj != null) {
      int pos = menuItemObj.getIntValue(INavigationClassConfig.MENU_POSITION_FIELD, -1);
      if (parentRef instanceof SpaceReference) {
        String menuPart = menuItemObj.getStringValue(INavigationClassConfig.MENU_PART_FIELD);
        treeNode = new TreeNode(docRef, parentRef, pos, menuPart);
      } else if (parentRef instanceof DocumentReference) {
        treeNode = new TreeNode(docRef, parentRef, pos);
      }
    }
    return treeNode;
  }

  @Override
  public List<TreeNode> getSiblingTreeNodes(DocumentReference moveDocRef) throws XWikiException {
    TreeNode moveTreeNode = getTreeNodeForDocRef(moveDocRef);
    List<TreeNode> siblingTreeNodes = getSubNodesForParent(moveTreeNode.getParentRef(),
        moveTreeNode.getPartName());
    return siblingTreeNodes;
  }

  @Override
  public void storeOrder(List<TreeNode> newTreeNodes) {
    storeOrder(newTreeNodes, false);
  }

  @Override
  public void storeOrder(List<TreeNode> newTreeNodes, boolean isMinorEdit) {
    int pos = -1;
    XWiki wiki = getContext().getWiki();
    for (TreeNode theNode : newTreeNodes) {
      DocumentReference theDocRef = theNode.getDocumentReference();
      try {
        XWikiDocument theDoc = wiki.getDocument(theDocRef, getContext());
        BaseObject menuItemObj = theDoc.getXObject(navClassConfig.getMenuItemClassRef(
            getContext().getDatabase()));
        if (menuItemObj != null) {
          pos++;
          int oldPos = menuItemObj.getIntValue(INavigationClassConfig.MENU_POSITION_FIELD, -1);
          if (oldPos != pos) {
            menuItemObj.setIntValue(INavigationClassConfig.MENU_POSITION_FIELD, pos);
            wiki.saveDocument(theDoc, "changed menu position from '" + oldPos + "' to '" + pos
                + "'.", isMinorEdit, getContext());
          }
        } else {
          LOGGER.error("storeOrder: failed to get menuItemObject of [{}].", theDocRef);
        }
      } catch (XWikiException exp) {
        LOGGER.error("storeOrder: Failed to get document [{}].", theDocRef);
      }
    }
  }

}
