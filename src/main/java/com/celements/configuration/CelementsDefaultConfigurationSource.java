package com.celements.configuration;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.configuration.internal.CompositeConfigurationSource;

/**
 * Composite Configuration Source that looks in the following sources in that order:
 * <ul>
 * <li>space preferences wiki page</li>
 * <li>wiki preferences wiki page</li>
 * <li>celements properties file (celements.properties)</li>
 * <li>xwiki properties file (xwiki.properties)</li>
 * </ul>
 * Should be used when a configuration should not be overriden by the user in his/her
 * profile (in which case the {@link CelementsAllConfigurationSource} should be used.
 */
@Component
public class CelementsDefaultConfigurationSource extends CompositeConfigurationSource
    implements Initializable {

  @Requirement("xwikiproperties")
  private ConfigurationSource xwikiPropertiesSource;

  @Requirement("celementsproperties")
  private ConfigurationSource celementsPropertiesSource;

  @Requirement("wiki")
  private ConfigurationSource wikiPreferencesSource;

  @Requirement("space")
  private ConfigurationSource spacePreferencesSource;

  @Override
  public void initialize() throws InitializationException {
    // First source is looked first when a property value is requested.
    addConfigurationSource(this.spacePreferencesSource);
    addConfigurationSource(this.wikiPreferencesSource);
    addConfigurationSource(this.celementsPropertiesSource);
    addConfigurationSource(this.xwikiPropertiesSource);
  }

}
