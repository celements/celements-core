package com.celements.pagetype.java;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface IJavaPageTypeRole {

  /**
   * must be a unique name accross an installation. Two implementations with identical
   * names will randomly overwrite each other.
   */
  public String getName();

  public List<String> getCategories();

  public boolean hasPageTitle();

  public boolean displayInFrameLayout();

  public String getRenderTemplateForRenderMode(String renderMode);

  public boolean isVisible();

}
