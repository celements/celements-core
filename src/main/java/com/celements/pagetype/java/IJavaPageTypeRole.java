package com.celements.pagetype.java;

import java.util.Set;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.google.common.base.Optional;

@ComponentRole
public interface IJavaPageTypeRole {

  /**
   * must be a unique name accross an installation. Two implementations with identical
   * names will randomly overwrite each other.
   */
  public String getName();

  public Set<IPageTypeCategoryRole> getCategories();

  public Set<String> getCategoryNames();

  public boolean hasPageTitle();

  public boolean displayInFrameLayout();

  public String getRenderTemplateForRenderMode(String renderMode);

  public boolean isVisible();

  public boolean isUnconnectedParent();

  public Optional<String> defaultTagName();

}
