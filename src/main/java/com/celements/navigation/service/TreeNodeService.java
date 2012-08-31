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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;

import com.celements.inheritor.InheritorFactory;
import com.celements.navigation.Navigation;
import com.celements.navigation.TreeNode;
import com.celements.navigation.filter.INavFilter;
import com.celements.navigation.filter.InternalRightsFilter;
import com.celements.web.service.IWebUtilsService;
import com.celements.web.service.WebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseCollection;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

/**
 * TreeNodeService
 * 
 * access a tree node structre throught the method of this service.
 * 
 * @author Fabian Pichler
 *
 */
@Component
public class TreeNodeService implements ITreeNodeService {

  private static Log mLogger = LogFactory.getFactory().getInstance(TreeNodeService.class);
  
  @Requirement
  Execution execution;

  @Requirement
  ITreeNodeCache treeNodeCache;
  
  @Requirement("default")
  EntityReferenceSerializer<String> serializer;
  
  private InheritorFactory injectedInheritorFactory;
  private IWebUtilsService webUtilsService;

  @Requirement(role = ITreeNodeProvider.class)
  Map<String, ITreeNodeProvider> nodeProviders;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }
    
  public int getActiveMenuItemPos(int menuLevel, String menuPart) {
    List<DocumentReference> parents = getWebUtilsService().getDocumentParentsList(
        getContext().getDoc().getDocumentReference(), true);
    if (parents.size() >= menuLevel) {
      return getMenuItemPos(parents.get(parents.size() - menuLevel), menuPart);
    }
    return -1;
  }

  public int getMenuItemPos(DocumentReference docRef, String menuPart) {
    try {
      DocumentReference parent = getParentRef(docRef);
      int pos = -1;
      for (TreeNode menuItem : getSubNodesForParent(parent, menuPart)) {
        pos = pos + 1;
        if (docRef.equals(menuItem.getDocumentReference())) {
          return pos;
        }
      }
    } catch (XWikiException e) {
      mLogger.error(e);
    }
    return -1;
  }
  
  /**
   * 
   * @deprecated use getSubNodesForParent(EntityReference, INavFilter)  or 
   * getSubNodesForParent(EntityReference, String) instead
   */
  @Deprecated
  public <T> List<TreeNode> getSubNodesForParent(String parent, String menuSpace,
      INavFilter<T> filter) {
    if("".equals(menuSpace)) {
      menuSpace = getContext().getDoc().getDocumentReference().getLastSpaceReference(
          ).getName();
    }
    String parentKey = getParentKey(parent, menuSpace);
    return getSubNodesForParent(getRef(parentKey), filter);
  }
  
  String getParentKey(String parent, String menuSpace) {
    String parentKey = "";
    if (parent != null) {
      parentKey = parent;
    }
    if(parentKey.indexOf('.') < 0) {
      parentKey = menuSpace + "." + parentKey;
    }
    if(parentKey.indexOf(':') < 0) {
      parentKey = getContext().getDatabase() + ":" + parentKey;
    }
    return parentKey;
  }
  
  String getParentKey(EntityReference reference, boolean withDataBase){
    String parentKey = "";
    if(reference==null){
      return parentKey;
    }
    else{
      parentKey = serializer.serialize(reference);
      if(!parentKey.contains(":") && !parentKey.endsWith(":")){
        parentKey += ":";
      } else if(!parentKey.contains(".") && !parentKey.endsWith(".")){
        parentKey += ".";
      }
      if(withDataBase){
        return parentKey;
      } else{
        return parentKey.substring(parentKey.indexOf(":")+1);        
      }
    }
  }

  
  public <T> List<TreeNode> getSubNodesForParent(EntityReference entRef,
      INavFilter<T> filter) {
    ArrayList<TreeNode> menuArray = new ArrayList<TreeNode>();
    for(TreeNode node : fetchNodesForParentKey(entRef)) {
      if((node!=null) && filter.includeTreeNode(node, getContext())) {
        // show only Menuitems of pages accessible to the current user
        menuArray.add(node);
      }
    }
    return menuArray;
  }
  
  /**
   * 
   * @deprecated use getSubNodesForParent(EntityReference, INavFilter)  or 
   * getSubNodesForParent(EntityReference, String) instead
   */
  @Deprecated
  public List<TreeNode> getSubNodesForParent(String parent, String menuSpace,
      String menuPart) {
    InternalRightsFilter filter = new InternalRightsFilter();
    filter.setMenuPart(menuPart);
    return getSubNodesForParent(parent, menuSpace, filter);
  }
  
  public List<TreeNode> getSubNodesForParent(EntityReference entRef,
      String menuPart) {
    InternalRightsFilter filter = new InternalRightsFilter();
    filter.setMenuPart(menuPart);
    return getSubNodesForParent(entRef, filter);
  }

  /**
   * fetchNodesForParentKey
   * @param parentKey
   * @param context
   * @return Collection keeps ordering of TreeNodes according to posId
   */
  List<TreeNode> fetchNodesForParentKey(EntityReference reference) {
    String parentKey = getParentKey(reference, true);
    long starttotal = System.currentTimeMillis();
    long start = System.currentTimeMillis();
    List<TreeNode> nodes = fetchNodesForParentKey_internal(parentKey, starttotal, start);
    if ((nodeProviders != null) && (nodeProviders.values().size() > 0)) {
      TreeMap<Integer, TreeNode> treeNodesMergedMap = new TreeMap<Integer, TreeNode>();
      for (TreeNode node : nodes) {
        treeNodesMergedMap.put(new Integer(node.getPosition()), node);
      }
      for (ITreeNodeProvider tnProvider : nodeProviders.values()) {
        try {
          for (TreeNode node : tnProvider.getTreeNodesForParent(parentKey)) {
            treeNodesMergedMap.put(new Integer(node.getPosition()), node);
          }
        } catch(Exception exp) {
          mLogger.warn("Failed on provider [" + tnProvider.getClass()
              + "] to get nodes for parentKey [" + parentKey + "].", exp);
        }
      }
      nodes = new ArrayList<TreeNode>(treeNodesMergedMap.values());
      long end = System.currentTimeMillis();
      mLogger.info("fetchNodesForParentKey: [" + parentKey + "] totaltime for list of ["
          + nodes.size() + "]: " + (end-starttotal));
    }
    return nodes;
  }

  private List<TreeNode> fetchNodesForParentKey_internal(String parentKey,
      long starttotal, long start) {
    List<TreeNode> notMappedmenuItems = treeNodeCache.getNotMappedMenuItemsForParentCmd(
        ).getTreeNodesForParentKey(parentKey, getContext());
    long end = System.currentTimeMillis();
    mLogger.debug("fetchNodesForParentKey_internal: time for getNotMappedMenuItemsFromDatabase: "
        + (end-start));
    start = System.currentTimeMillis();
    List<TreeNode> mappedTreeNodes = treeNodeCache.getMappedMenuItemsForParentCmd(
        ).getTreeNodesForParentKey(parentKey, getContext());
    end = System.currentTimeMillis();
    mLogger.debug("fetchNodesForParentKey_internal: time for getMappedMenuItemsForParentCmd: "
        + (end-start));
    start = System.currentTimeMillis();
    TreeMap<Integer, TreeNode> menuItemsMergedMap = null;
    if ((notMappedmenuItems == null) || (notMappedmenuItems.size() == 0)) {
      end = System.currentTimeMillis();
      mLogger.info("fetchNodesForParentKey_internal: [" + parentKey  + "] totaltime for list of ["
          + mappedTreeNodes.size() + "]: " + (end-starttotal));
      return mappedTreeNodes;
    } else if (mappedTreeNodes.size() == 0) {
      end = System.currentTimeMillis();
      mLogger.info("fetchNodesForParentKey_internal: [" + parentKey + "] totaltime for list of ["
          + notMappedmenuItems.size() + "]: " + (end-starttotal));
      return notMappedmenuItems;
    } else {
      menuItemsMergedMap = new TreeMap<Integer, TreeNode>();
      for (TreeNode node : notMappedmenuItems) {
        menuItemsMergedMap.put(new Integer(node.getPosition()), node);
      }
      for (TreeNode node : mappedTreeNodes) {
        menuItemsMergedMap.put(new Integer(node.getPosition()), node);
      }
      end = System.currentTimeMillis();
      mLogger.debug("fetchNodesForParentKey_internal: time for merging menu items: "
          + (end-start));
      ArrayList<TreeNode> menuItems = new ArrayList<TreeNode>(menuItemsMergedMap.values());
      mLogger.info("fetchNodesForParentKey_internal: [" + parentKey + "] totaltime for list of ["
          + menuItems.size() + "]: " + (end-starttotal));
      return menuItems;
    }
  }

  /**
   * 
   * @param parentKey
   * @param context
   * @return Collection keeps ordering of menuItems according to posId
   * 
   * @deprecated since 2.14.0  use new fetchNodesForParentKey instead
   */
  @Deprecated
  List<BaseObject> fetchMenuItemsForXWiki(String parentKey) {
    long starttotal = System.currentTimeMillis();
    List<BaseObject> menuItemList = new ArrayList<BaseObject>();
    EntityReference refParent = webUtilsService.resolveEntityReference(parentKey);
    for (TreeNode node : fetchNodesForParentKey(refParent)) {
      try {
        XWikiDocument itemdoc = getContext().getWiki().getDocument(node.getFullName(),
            getContext());
        BaseObject cobj = itemdoc.getObject("Celements2.MenuItem");
        if(cobj != null) {
          menuItemList.add(cobj);
        }
      } catch (XWikiException exp) {
        mLogger.error("failed to get doc for menuItem", exp);
      }
    }
    long end = System.currentTimeMillis();
    mLogger.info("fetchMenuItemsForXWiki: [" + parentKey + "] totaltime for list of ["
        + menuItemList.size() + "]: " + (end-starttotal));
    return menuItemList;
  }

  /**
   * 
   * @deprecated use getSubNodesForParent(EntityReference, INavFilter)  or 
   * getSubNodesForParent(EntityReference, String) instead
   */
  @Deprecated
  public <T> List<T> getSubMenuItemsForParent(String parent, String menuSpace,
      INavFilter<T> filter) {
    if("".equals(menuSpace)) {
      menuSpace = getContext().getDoc().getSpace();
    }
    String parentKey = getParentKey(parent, menuSpace);
    ArrayList<T> menuArray = new ArrayList<T>();
    for (BaseObject baseObj : fetchMenuItemsForXWiki(parentKey)) {
      if(filter.includeMenuItem(baseObj, getContext())) {
        // show only Menuitems of pages accessible to the current user
        menuArray.add(filter.convertObject(baseObj, getContext()));
      }
    }
    return menuArray;
  }

  public Integer getMaxConfiguredNavigationLevel() {
    try {
      BaseCollection navConfigObj = getInheritorFactory().getConfigDocFieldInheritor(
          Navigation.NAVIGATION_CONFIG_CLASS, getParentKey(
              getContext().getDoc().getDocumentReference(), false),
              getContext()).getObject("menu_element_name");
      if(navConfigObj!=null){
        XWikiDocument navConfigDoc = getContext().getWiki().getDocument(
            navConfigObj.getDocumentReference(), getContext());
        List<BaseObject> navConfigObjects = navConfigDoc.getXObjects(
            getRef(Navigation.NAVIGATION_CONFIG_CLASS_SPACE, 
                   Navigation.NAVIGATION_CONFIG_CLASS_DOC));
        int maxLevel = 0;
        if (navConfigObj != null) {
          for (BaseObject navObj : navConfigObjects) {
            if (navObj != null) {
              maxLevel = Math.max(maxLevel, navObj.getIntValue("to_hierarchy_level"));
            }
          }
        }
        return maxLevel;
      }
    } catch (XWikiException e) {
      mLogger.error("unable to get configDoc.", e);
    }
    return Navigation.DEFAULT_MAX_LEVEL;
  }
  
  public TreeNode getPrevMenuItem(DocumentReference docRef) throws XWikiException {
    return getSiblingMenuItem(docRef, true);
  }

  public TreeNode getNextMenuItem(DocumentReference docRef) throws XWikiException {
    return getSiblingMenuItem(docRef, false);
  }

  TreeNode getSiblingMenuItem(DocumentReference docRef, boolean previous)
      throws XWikiException {
    XWikiDocument doc = getContext().getWiki().getDocument(docRef, getContext());
    BaseObject menuItem = doc.getXObject(getRef("Celements2", "MenuItem"));
    if(menuItem!=null){
      DocumentReference parent;
      try {
        parent = getParentRef(docRef);
        List<TreeNode> subMenuItems = getSubNodesForParent(parent,
            menuItem.getStringValue("part_name"));
        mLogger.debug("getPrevMenuItem: " + subMenuItems.size()
            + " subMenuItems found for parent '" + parent + "'. "
            + Arrays.deepToString(subMenuItems.toArray()));
        int pos = getMenuItemPos(docRef, menuItem.getStringValue("part_name"));
        if(previous && (pos>0)){
          return subMenuItems.get(pos - 1);
        } else if (!previous && (pos < (subMenuItems.size() - 1))) {
          return subMenuItems.get(pos + 1);
        }
        mLogger.info("getPrevMenuItem: no previous MenuItem found for "
            + getParentKey(docRef, true));
      } catch (XWikiException e) {
        mLogger.error(e);
      }
    } else {
      mLogger.debug("getPrevMenuItem: no MenuItem Object found on doc "
          + getParentKey(docRef, true));
    }
    return null;
  }
  
  public List<TreeNode> getMenuItemsForHierarchyLevel(int menuLevel, String menuPart) {
    DocumentReference parent = new WebUtilsService().getParentForLevel(menuLevel);
    if (parent != null) {
      List<TreeNode> submenuItems = getSubNodesForParent(parent, menuPart);
      mLogger.debug("submenuItems for parent: " + parent + " ; " + submenuItems);
      return submenuItems;
    }
    mLogger.debug("parent is null");
    return new ArrayList<TreeNode>();
  }
  
  DocumentReference getRef(String spaceName, String pageName){
    return new DocumentReference(getContext().getDatabase(), spaceName, pageName);
  }
  
  DocumentReference getRef(String s){
    return getWebUtilsService().resolveDocumentReference(s);
  }
  
  private DocumentReference getParentRef(DocumentReference docRef) throws XWikiException {
    return getContext().getWiki().getDocument(docRef, getContext()).getParentReference();
  }
  
  void injectIWebUtilsService(IWebUtilsService wu){
    webUtilsService = wu;
  }
  
  private IWebUtilsService getWebUtilsService() {
    if(webUtilsService!=null){
      return webUtilsService;
    } else{
      return Utils.getComponent(IWebUtilsService.class);
    }
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

}
