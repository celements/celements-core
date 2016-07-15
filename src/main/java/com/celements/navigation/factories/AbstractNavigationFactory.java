package com.celements.navigation.factories;

import javax.validation.constraints.NotNull;

import com.celements.navigation.INavigation;
import com.celements.navigation.Navigation;
import com.celements.navigation.NavigationConfig;

public abstract class AbstractNavigationFactory<T> implements NavigationFactory<T> {

  @NotNull
  abstract protected T getDefaultConfigReference();

  @Override
  @NotNull
  public INavigation createNavigation() {
    return createNavigation(getDefaultConfigReference());
  }

  @Override
  @NotNull
  public INavigation createNavigation(@NotNull T configReference) {
    final INavigation nav = Navigation.createNavigation();
    nav.loadConfig(getNavigationConfig(configReference));
    return nav;
  }

  @Override
  @NotNull
  public NavigationConfig getNavigationConfig() {
    return getNavigationConfig(getDefaultConfigReference());
  }

  @Override
  public boolean hasNavigationConfig() {
    return hasNavigationConfig(getDefaultConfigReference());
  }

}
