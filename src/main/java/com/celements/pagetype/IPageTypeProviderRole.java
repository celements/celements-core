package com.celements.pagetype;

import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface IPageTypeProviderRole {

  public List<PageTypeReference> getPageTypes();

  public IPageTypeConfig getPageTypeByReference(PageTypeReference pageTypeRef);

}
