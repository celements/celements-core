package com.celements.pagetype.java;

import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.celements.pagetype.category.IPageTypeCategoryRole;

@Component
public class CodePageType extends AbstractJavaPageType {

  public static final String NAME = "Code";

  private final Set<IPageTypeCategoryRole> categories;

  @Inject
  public CodePageType(IPageTypeCategoryRole pageTypeCategory) {
    super();
    this.categories = Set.of(pageTypeCategory);
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Set<IPageTypeCategoryRole> getCategories() {
    return categories;
  }

  @Override
  public String getRenderTemplateForRenderMode(String renderMode) {
    return ("edit".equals(renderMode)) ? "CodeEdit" : "";
  }

  @Override
  public boolean isVisible() {
    return true;
  }

  @Override
  public boolean displayInFrameLayout() {
    return true;
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
  public boolean useInlineEditorMode() {
    return false;
  }

}
