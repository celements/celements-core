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

  public SpaceReference getSpaceReference();

  public String getMenuPart(TreeNode node);

  public void startRenderCell(TreeNode node, boolean isFirstItem, boolean isLastItem);

  public void startRenderChildren(EntityReference parentRef);

  public void endRenderChildren(EntityReference parentRef);

  public void endRenderCell(TreeNode node, boolean isFirstItem, boolean isLastItem);

  public boolean isRenderSubCells(EntityReference parentRef);

  public String getAsString();

  public void renderEmptyChildren(TreeNode node);

  public void setSpaceReference(SpaceReference spaceReference);

}
