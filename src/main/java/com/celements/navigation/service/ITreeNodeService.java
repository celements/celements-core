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
   * since 2.24.0
   */
  public boolean isTreeNode(DocumentReference docRef);

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

  public EntityReference getParentReference(DocumentReference docRef);

  public EntityReference getParentEntityRef(DocumentReference docRef
      ) throws XWikiException;

  public void moveTreeDocAfter(DocumentReference moveDocRef,
      DocumentReference insertAfterDocRef) throws XWikiException;

  public TreeNode getTreeNodeForDocRef(DocumentReference moveDocRef
      ) throws XWikiException;

  public List<TreeNode> getSiblingTreeNodes(DocumentReference moveDocRef
      ) throws XWikiException;

  public List<TreeNode> getSiblingTreeNodes_internal(DocumentReference moveDocRef
      ) throws XWikiException;

  public void storeOrder(List<TreeNode> newTreeNodes);
  
  public void storeOrder(List<TreeNode> newTreeNodes, boolean isMinorEdit);

}
