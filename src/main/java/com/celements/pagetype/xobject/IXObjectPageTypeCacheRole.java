package com.celements.pagetype.xobject;


import java.util.List;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.pagetype.PageTypeReference;


@ComponentRole
public interface IXObjectPageTypeCacheRole {

  public void invalidateCacheForDatabase(String database);

  public List<PageTypeReference> getPageTypesRefsForDatabase(String database);

}
