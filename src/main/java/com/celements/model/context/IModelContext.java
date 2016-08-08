package com.celements.model.context;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.WikiReference;

@ComponentRole
public interface IModelContext {

  /**
   * @return the current wiki set in context
   */
  @NotNull
  public WikiReference getCurrentWiki();

  /**
   * @param wiki
   *          to be set in context
   * @return the wiki which was set before
   */
  @NotNull
  public WikiReference setCurrentWiki(@NotNull WikiReference wiki);

}
