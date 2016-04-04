package com.celements.configuration;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.configuration.internal.CommonsConfigurationSource;
import org.xwiki.container.Container;

@Component("celementsproperties")
public class CelementsPropertiesConfigurationSource extends CommonsConfigurationSource
    implements Initializable {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(
      CelementsPropertiesConfigurationSource.class);

  private static final String CELEMENTS_PROPERTIES_FILE = "/WEB-INF/celements.properties";

  @Requirement
  private Container container;

  @Override
  public void initialize() throws InitializationException {
    URL propertiesURL;
    try {
      propertiesURL = container.getApplicationContext().getResource(
          CELEMENTS_PROPERTIES_FILE);
      setConfiguration(new PropertiesConfiguration(propertiesURL));
    } catch (ConfigurationException | MalformedURLException exc) {
      LOGGER.warn("Failed to load configuration file '{}'", CELEMENTS_PROPERTIES_FILE,
          exc);
      setConfiguration(new BaseConfiguration());
    }
  }
}
