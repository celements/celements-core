package com.celements.pagetype.xobject;


import java.util.List;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.WikiReference;

import com.celements.pagetype.PageTypeReference;


@ComponentRole
public interface IXObjectPageTypeCacheRole {

  public void invalidateCacheForWiki(WikiReference wikiRef);

  public List<PageTypeReference> getPageTypesRefsForWiki(WikiReference wikiRef);

}
