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
package com.celements.cells;

import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.navigation.TreeNode;

public interface IRenderStrategy {

  public boolean isRenderCell(TreeNode node);

  public void startRendering();

  public void endRendering();

  /**
   * @param fullName
   * @return
   * 
   * @deprecated since 2.14.0  use getSpaceReference instead
   */
  @Deprecated
  public String getMenuSpace(String fullName);

  /**
   * @deprecated since 2.18.0  use getParentReference(String) instead
   */
  @Deprecated
  public SpaceReference getSpaceReference();

  public EntityReference getParentReference(String parent);

  public String getMenuPart(String parent);

  public void startRenderCell(TreeNode node, boolean isFirstItem, boolean isLastItem);

  public void startRenderChildren(String parent);

  public void endRenderChildren(String parent);

  public void endRenderCell(TreeNode node, boolean isFirstItem, boolean isLastItem);

  public boolean isRenderSubCells(String parent);

  public String getAsString();

  public void renderEmptyChildren(String parent);

  /**
   * @param spaceName
   * 
   * @deprecated since 2.14.0  use setSpaceReference instead
   */
  @Deprecated
  public void setSpaceName(String spaceName);

  public void setSpaceReference(SpaceReference spaceReference);

}
