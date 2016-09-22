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

}
