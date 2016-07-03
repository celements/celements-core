package com.celements.navigation.factories;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.navigation.NavigationConfig;
import com.celements.pagetype.PageTypeReference;

@ComponentRole
public interface JavaNavigationConfigurator {

  @NotNull
  public NavigationConfig getNavigationConfig(@NotNull PageTypeReference configReference);

  public boolean handles(@NotNull PageTypeReference configReference);

}
