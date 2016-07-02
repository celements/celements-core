package com.celements.navigation.service;

import org.xwiki.component.annotation.ComponentRole;

import com.celements.navigation.NavigationConfig;

@ComponentRole
public interface JavaNavigationConfigurator {

  public NavigationConfig getNavigationConfig();

}
