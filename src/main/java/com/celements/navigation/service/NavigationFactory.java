package com.celements.navigation.service;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.navigation.INavigation;

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

}
