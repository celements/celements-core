package com.celements.navigation.service;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.navigation.TreeNode;
import com.celements.navigation.filter.INavFilter;

@ComponentRole
public interface ITreeNodeService {

  public <T> List<TreeNode> getSubNodesForParent(String parent, String menuSpace,
      INavFilter<T> filter);

  /**
   * 
   * @deprecated since 2.14.0  use getSubNodesForParent instead
   */
  @Deprecated
  public <T> List<T> getSubMenuItemsForParent(String parent, String menuSpace,
      INavFilter<T> filter);

  /**
   * getSubNodesForParent
   * get all subnodes of a given parent document (by fullname).
   * 
   * @param parent
   * @param menuSpace (default: $doc.space)
   * @param menuPart 
   * @return (array of tree nodes)
   */
  public List<TreeNode> getSubNodesForParent(String parent, String menuSpace,
      String menuPart);

}
