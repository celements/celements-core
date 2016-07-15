package com.celements.navigation.factories;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.navigation.NavigationConfig;
import com.celements.pagetype.PageTypeReference;

@ComponentRole
public interface JavaNavigationConfigurator {

  /**
   * <code>getNavigationConfig</code> returns the java default values for the given
   * PageTypeReference, if <code>handles</code> returns for the same <code>PageTypeReference</code>
   * true, else it must return the NavigatinConfig.DEFAULTS object.
   *
   * @param configReference
   * @return
   */
  @NotNull
  public NavigationConfig getNavigationConfig(@NotNull PageTypeReference configReference);

  /**
   * <code>handles</code> returns true for the <code>PageTypeReference</code> where the
   * <code>getNavigationConfig</code> should be called.
   *
   * @param configReference
   * @return
   */
  public boolean handles(@NotNull PageTypeReference configReference);

}
