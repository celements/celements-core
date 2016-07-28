package com.celements.navigation.factories;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.navigation.INavigation;
import com.celements.navigation.NavigationConfig;

@ComponentRole
public interface NavigationFactory<T> {

  /**
   * createNavigation with configuration related to current execution context
   *
   * @return new Navigation object
   */
  @NotNull
  public INavigation createNavigation();

  /**
   * createNavigation with configuration related to the configReference which type is implementation
   * dependent.
   *
   * @return new Navigation object
   */
  @NotNull
  public INavigation createNavigation(@NotNull T configReference);

  /**
   * getNavigationConfig related to current execution context.
   *
   * @return NavigationConfig object
   */
  @NotNull
  public NavigationConfig getNavigationConfig();

  /**
   * getNavigationConfig related to the configReference which type is implementation
   * dependent.
   *
   * @return NavigationConfig object
   */
  @NotNull
  public NavigationConfig getNavigationConfig(@NotNull T configReference);

  public boolean hasNavigationConfig();

  public boolean hasNavigationConfig(@NotNull T configReference);

}
