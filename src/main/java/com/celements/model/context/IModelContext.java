package com.celements.model.context;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.WikiReference;

@ComponentRole
public interface IModelContext {

  public WikiReference getCurrentWiki();

}
