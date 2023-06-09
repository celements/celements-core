package com.celements.auth.user;

import java.util.Set;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Component;

import com.celements.pagetype.category.IPageTypeCategoryRole;
import com.celements.pagetype.java.AbstractJavaPageType;
import com.google.common.collect.Sets;

@Component(UserPageType.PAGETYPE_NAME)
public class UserPageType extends AbstractJavaPageType {

  public static final String PAGETYPE_NAME = "User";

  static final String VIEW_TEMPLATE_NAME = "UserView";

  static final String EDIT_TEMPLATE_NAME = "UserEdit";

  private final IPageTypeCategoryRole pageTypeCategory;

  @Inject
  public UserPageType(IPageTypeCategoryRole pageTypeCategory) {
    super();
    this.pageTypeCategory = pageTypeCategory;
  }

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

  @Override
  public boolean useInlineEditorMode() {
    return true;
  }

}
