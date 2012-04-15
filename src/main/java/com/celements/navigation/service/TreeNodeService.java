package com.celements.navigation.service;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.celements.navigation.TreeNode;
import com.celements.navigation.filter.INavFilter;
import com.celements.navigation.filter.InternalRightsFilter;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

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

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public <T> List<TreeNode> getSubNodesForParent(String parent, String menuSpace,
      INavFilter<T> filter) {
    if("".equals(menuSpace)) {
      menuSpace = getContext().getDoc().getDocumentReference().getLastSpaceReference(
          ).getName();
    }
    String parentKey = getParentKey(parent, menuSpace);
    ArrayList<TreeNode> menuArray = new ArrayList<TreeNode>();
    for (TreeNode node : fetchNodesForParentKey(parentKey)) {
      if((node != null) && filter.includeTreeNode(node, getContext())) {
        // show only Menuitems of pages accessible to the current user
        menuArray.add(node);
      }
    }
    return menuArray;
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

  /**
   * fetchNodesForParentKey
   * @param parentKey
   * @param context
   * @return Collection keeps ordering of TreeNodes according to posId
   */
  List<TreeNode> fetchNodesForParentKey(String parentKey) {
    long starttotal = System.currentTimeMillis();
    long start = System.currentTimeMillis();
    List<TreeNode> notMappedmenuItems = treeNodeCache.getNotMappedMenuItemsForParentCmd(
        ).getTreeNodesForParentKey(parentKey, getContext());
    long end = System.currentTimeMillis();
    mLogger.debug("fetchNodesForParentKey: time for getNotMappedMenuItemsFromDatabase: "
        + (end-start));
    start = System.currentTimeMillis();
    List<TreeNode> mappedTreeNodes = treeNodeCache.getMappedMenuItemsForParentCmd(
        ).getTreeNodesForParentKey(parentKey, getContext());
    end = System.currentTimeMillis();
    mLogger.debug("fetchNodesForParentKey: time for getMappedMenuItemsForParentCmd: "
        + (end-start));
    start = System.currentTimeMillis();
    TreeMap<Integer, TreeNode> menuItemsMergedMap = null;
    if ((notMappedmenuItems == null) || (notMappedmenuItems.size() == 0)) {
      end = System.currentTimeMillis();
      mLogger.info("fetchNodesForParentKey: [" + parentKey  + "] totaltime for list of ["
          + mappedTreeNodes.size() + "]: " + (end-starttotal));
      return mappedTreeNodes;
    } else if (mappedTreeNodes.size() == 0) {
      end = System.currentTimeMillis();
      mLogger.info("fetchNodesForParentKey: [" + parentKey + "] totaltime for list of ["
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
      mLogger.debug("fetchNodesForParentKey: time for merging menu items: "
          + (end-start));
      ArrayList<TreeNode> menuItems = new ArrayList<TreeNode>(menuItemsMergedMap.values());
      mLogger.info("fetchNodesForParentKey: [" + parentKey + "] totaltime for list of ["
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
   * @deprecated use new fetchNodesForParentKey instead
   */
  @Deprecated
  List<BaseObject> fetchMenuItemsForXWiki(String parentKey) {
    long starttotal = System.currentTimeMillis();
    List<BaseObject> menuItemList = new ArrayList<BaseObject>();
    for (TreeNode node : fetchNodesForParentKey(parentKey)) {
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
   * @deprecated use getSubNodesForParent instead
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

  /* (non-Javadoc)
   * @see com.celements.web.utils.IWebUtils#getSubMenuItemsForParent_internal(java.lang.String, java.lang.String, java.lang.String, com.xpn.xwiki.XWikiContext)
   */
  public List<TreeNode> getSubNodesForParent(String parent, String menuSpace,
      String menuPart) {
    InternalRightsFilter filter = new InternalRightsFilter();
    filter.setMenuPart(menuPart);
    return getSubNodesForParent(parent, menuSpace, filter);
  }

}
