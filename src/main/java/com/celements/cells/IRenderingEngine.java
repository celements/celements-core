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

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.SpaceReference;

import com.celements.navigation.TreeNode;

public interface IRenderingEngine {

  void renderCell(@Nullable TreeNode node);

  /**
   * @deprecated instead use {@link #renderLayout(SpaceReference)})
   */
  @Deprecated(since = "6.7", forRemoval = true)
  void renderPageLayout(@NotNull SpaceReference spaceRef);

  void renderLayout(@NotNull SpaceReference layoutSpaceRef);

  void renderLayoutPartial(@NotNull TreeNode startNode);
}
