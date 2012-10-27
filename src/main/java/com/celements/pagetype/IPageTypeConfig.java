package com.celements.pagetype;

import java.util.List;

public interface IPageTypeConfig {

  public String getName();

  public String getPrettyName();

  public boolean hasPageTitle();

  public boolean displayInFrameLayout();

  public List<String> getCategories();

  public String getRenderTemplateForRenderMode(String renderMode);

  public boolean isVisible();

}
