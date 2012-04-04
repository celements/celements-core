package com.celements.navigation.service;

import java.util.List;
import java.util.Vector;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.navigation.Navigation;
import com.celements.navigation.TreeNode;
import com.celements.navigation.filter.INavFilter;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseCollection;
import com.xpn.xwiki.objects.BaseObject;

@ComponentRole
public interface ITreeNodeService {

  public <T> List<TreeNode> getSubNodesForParent(String parent, String menuSpace,
      INavFilter<T> filter);

  /**
   * 
   * @deprecated use getSubNodesForParent instead
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
  
  public Integer getMaxConfiguredNavigationLevel(XWikiContext context);

}
