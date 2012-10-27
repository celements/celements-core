package com.celements.pagetype;

import java.util.ArrayList;
import java.util.List;

public class PageTypeReference {

  private String configName;

  private String providerHint;

  private List<String> categories;

  public PageTypeReference(String configName, String providerHint,
      List<String> categories) {
    this.configName = configName;
    this.providerHint = providerHint;
    this.categories = new ArrayList<String>(categories);
  }

  public String getConfigName() {
    return configName;
  }

  public List<String> getCategories() {
    return new ArrayList<String>(categories);
  }

  public String getProviderHint() {
    return providerHint;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof PageTypeReference)) {
      return false;
    }
    PageTypeReference thePageTypeRef = (PageTypeReference) obj;
    return thePageTypeRef.getConfigName().equals(this.getConfigName());
  }

  @Override
  public int hashCode() {
    return this.getConfigName().hashCode();
  }

}
