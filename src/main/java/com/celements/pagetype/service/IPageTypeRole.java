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
package com.celements.pagetype.service;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.google.common.base.Optional;
import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface IPageTypeRole {

  public void resetTypeNameToCatCache();

  public IPageTypeConfig getPageTypeConfig(String pageTypeName);

  @Nullable
  public IPageTypeConfig getPageTypeConfigForPageTypeRef(@NotNull PageTypeReference pageTypeRef);

  /**
   * @deprecated since 3.1 instead use {@link #getPageTypeReference(String)}
   */
  @Nullable
  @Deprecated
  public PageTypeReference getPageTypeRefByConfigName(@Nullable String pageTypeName);

  @NotNull
  public Optional<PageTypeReference> getPageTypeReference(@Nullable String pageTypeName);

  public List<String> getPageTypesConfigNamesForCategories(Set<String> catList,
      boolean onlyVisible);

  public List<PageTypeReference> getPageTypeRefsForCategories(Set<String> catList,
      boolean onlyVisible);

  public boolean setPageType(XWikiDocument doc, PageTypeReference pageTypeRef);

  public Optional<IPageTypeCategoryRole> getTypeCategoryForCatName(String categoryName);

  public List<String> getTypesForCategory(String categoryName, boolean onlyVisible);

}
