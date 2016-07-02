package com.celements.navigation.service;

import java.util.Map;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;

import com.celements.navigation.NavigationConfig;

@Component(JavaNavigationFactory.JAVA_NAV_FACTORY_HINT)
@InstantiationStrategy(ComponentInstantiationStrategy.SINGLETON)
@Singleton
public class JavaNavigationFactory extends AbstractNavigationFactory<String> {

  public static final String JAVA_NAV_FACTORY_HINT = "java";

  private final static Logger LOGGER = LoggerFactory.getLogger(JavaNavigationFactory.class);

  public static final String NAV_CONFIG_DEFAULT = "default";

  @Requirement
  private Map<String, JavaNavigationConfigurator> javaNavConfig;

  @Override
  protected String getDefaultConfigReference() {
    return NAV_CONFIG_DEFAULT;
  }

  @Override
  @NotNull
  public NavigationConfig getNavigationConfig(String configuratorHint) {
    if (hasNavigationConfig(configuratorHint)) {
      LOGGER.debug("java navigation configurator for '{}' found.", configuratorHint);
      return javaNavConfig.get(configuratorHint).getNavigationConfig();
    } else {
      LOGGER.info("java navigation configurator for '{}' NOT found.", configuratorHint);
      return NavigationConfig.DEFAULTS;
    }
  }

  @Override
  public boolean hasNavigationConfig(String configReference) {
    return javaNavConfig.containsKey(configReference);
  }

}
