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
package com.celements.navigation.presentation;

import java.util.Set;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.celements.pagetype.java.AbstractJavaPageType;
import com.google.common.collect.Sets;

@Component(PresentationPageType.PAGETYPE_NAME)
public class PresentationPageType extends AbstractJavaPageType {

  public static final String PAGETYPE_NAME = "Presentation";

  static final String VIEW_TEMPLATE_NAME = "PresentationView";

  static final String EDIT_TEMPLATE_NAME = "PresentationEdit";

  @Requirement
  private IPageTypeCategoryRole pageTypeCategory;

  @Override
  public String getName() {
    return PAGETYPE_NAME;
  }

  @Override
  public boolean displayInFrameLayout() {
    return true;
  }

  @Override
  public Set<IPageTypeCategoryRole> getCategories() {
    return Sets.newHashSet(pageTypeCategory);
  }

  @Override
  public boolean hasPageTitle() {
    return false;
  }

  @Override
  public boolean isUnconnectedParent() {
    return false;
  }

  @Override
  public boolean isVisible() {
    return true;
  }

  String getViewTemplateName() {
    return VIEW_TEMPLATE_NAME;
  }

  String getEditTemplateName() {
    return EDIT_TEMPLATE_NAME;
  }

  @Override
  public @NotNull String getRenderTemplateForRenderMode(String renderMode) {
    if ("edit".equals(renderMode)) {
      return getEditTemplateName();
    } else {
      return getViewTemplateName();
    }
  }

}
