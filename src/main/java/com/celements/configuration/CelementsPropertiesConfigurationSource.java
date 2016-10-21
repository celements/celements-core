/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.configuration;

import java.net.URL;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.configuration.internal.CommonsConfigurationSource;
import org.xwiki.container.ApplicationContext;
import org.xwiki.container.Container;

@Component(CelementsPropertiesConfigurationSource.NAME)
public class CelementsPropertiesConfigurationSource extends CommonsConfigurationSource implements
    Initializable {

  public static final String NAME = "celementsproperties";

  private static final Logger LOGGER = LoggerFactory.getLogger(
      CelementsPropertiesConfigurationSource.class);

  static final String CELEMENTS_PROPERTIES_FILE = "/WEB-INF/celements.properties";

  @Requirement
  Container container;

  @Override
  public void initialize() throws InitializationException {
    Configuration config;
    try {
      ApplicationContext appContext = container.getApplicationContext();
      URL propertiesURL = appContext.getResource(CELEMENTS_PROPERTIES_FILE);
      config = new PropertiesConfiguration(propertiesURL);
    } catch (Exception exc) {
      LOGGER.warn("Failed to load configuration file '{}'", CELEMENTS_PROPERTIES_FILE, exc);
      config = new BaseConfiguration();
    }
    setConfiguration(config);
  }
}
