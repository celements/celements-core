package com.celements.navigation.service;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.navigation.TreeNode;
import com.celements.navigation.filter.INavFilter;
import com.xpn.xwiki.XWikiException;

@ComponentRole
public interface ITreeNodeService {
    
  public int getActiveMenuItemPos(int menuLevel, String menuPart);

  public int getMenuItemPos(DocumentReference docRef, String menuPart);

  /**
   * 
   * @deprecated since 2.17.0 use getSubNodesForParent(EntityReference, INavFilter)  or 
   * getSubNodesForParent(EntityReference, String) instead
   */
  @Deprecated
  public <T> List<TreeNode> getSubNodesForParent(String parent, String menuSpace,
      INavFilter<T> filter);
  
  public <T> List<TreeNode> getSubNodesForParent(EntityReference entRef,
      INavFilter<T> filter);
  
  /**
   * 
   * since 2.17.0 use getSubNodesForParent(EntityReference, INavFilter)  or 
   * getSubNodesForParent(EntityReference, String) instead
   *
   * get all subnodes of a given parent document (by fullname).
   * 
   * @param parent
   * @param menuSpace (default: $doc.space)
   * @param menuPart 
   * @return (array of tree nodes)
   */
  @Deprecated
  public List<TreeNode> getSubNodesForParent(String parent, String menuSpace,
      String menuPart);
  
  public List<TreeNode> getSubNodesForParent(EntityReference entRef, String menuPart);

  /**
   * 
   * @deprecated since 2.14.0 use getSubNodesForParent(EntityReference, INavFilter)  or 
   * getSubNodesForParent(EntityReference, String) instead
   */
  @Deprecated
  public <T> List<T> getSubMenuItemsForParent(String parent, String menuSpace,
      INavFilter<T> filter);
  
  public Integer getMaxConfiguredNavigationLevel();
  
  public TreeNode getPrevMenuItem(DocumentReference docRef) throws XWikiException;
  
  public TreeNode getNextMenuItem(DocumentReference docRef) throws XWikiException;
  
  public List<TreeNode> getMenuItemsForHierarchyLevel(int menuLevel, String menuPart); 

}
