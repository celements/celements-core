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
package com.celements.pagetype.java;

import java.util.Set;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.google.common.base.Optional;

@ComponentRole
public interface IJavaPageTypeRole {

  /**
   * must be a unique name accross an installation. Two implementations with identical
   * names will randomly overwrite each other.
   */
  @NotNull
  public String getName();

  @NotNull
  public Set<IPageTypeCategoryRole> getCategories();

  @NotNull
  public Set<String> getCategoryNames();

  public boolean hasPageTitle();

  public boolean displayInFrameLayout();

  @NotNull
  public String getRenderTemplateForRenderMode(String renderMode);

  public boolean isVisible();

  public boolean isUnconnectedParent();

  @NotNull
  public Optional<String> defaultTagName();

  public void collectAttributes(@NotNull AttributeBuilder attrBuilder,
      @NotNull DocumentReference cellDocRef);

  public boolean useInlineEditorMode();

}
