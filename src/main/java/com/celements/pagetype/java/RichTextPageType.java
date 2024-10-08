package com.celements.pagetype.java;

import java.util.Set;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.celements.pagetype.category.IPageTypeCategoryRole;

@Component
public class RichTextPageType extends AbstractJavaPageType {

  public static final String NAME = "RichText";

  private final Set<IPageTypeCategoryRole> categories;

  @Inject
  public RichTextPageType(IPageTypeCategoryRole pageTypeCategory) {
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
    return ("edit".equals(renderMode)) ? "RichTextEdit" : "RichTextView";
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
