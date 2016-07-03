package com.celements.navigation.factories;

import java.util.List;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;

import com.celements.navigation.NavigationConfig;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;

@Component(JavaNavigationFactory.JAVA_NAV_FACTORY_HINT)
@InstantiationStrategy(ComponentInstantiationStrategy.SINGLETON)
@Singleton
public class JavaNavigationFactory extends AbstractNavigationFactory<PageTypeReference> {

  public static final String JAVA_NAV_FACTORY_HINT = "java";

  private final static Logger LOGGER = LoggerFactory.getLogger(JavaNavigationFactory.class);

  @Requirement
  private List<JavaNavigationConfigurator> javaNavConfigList;

  @Requirement
  IPageTypeResolverRole pageTypeResolver;

  @Override
  protected PageTypeReference getDefaultConfigReference() {
    return pageTypeResolver.getPageTypeRefForCurrentDoc();
  }

  @Override
  @NotNull
  public NavigationConfig getNavigationConfig(@NotNull PageTypeReference configReference) {
    for (JavaNavigationConfigurator javaNavConfig : javaNavConfigList) {
      if (javaNavConfig.handles(configReference)) {
        LOGGER.debug("java navigation configurator for '{}' found.", configReference);
        return javaNavConfig.getNavigationConfig(configReference);
      }
    }
    LOGGER.info("java navigation configurator for '{}' NOT found.", configReference);
    return NavigationConfig.DEFAULTS;
  }

  @Override
  public boolean hasNavigationConfig(PageTypeReference configReference) {
    for (JavaNavigationConfigurator javaNavConfig : javaNavConfigList) {
      if (javaNavConfig.handles(configReference)) {
        return true;
      }
    }
    return false;
  }

}
