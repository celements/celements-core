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
public class CelementsPropertiesConfigurationSource extends CommonsConfigurationSource implements
    Initializable {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      CelementsPropertiesConfigurationSource.class);

  static final String CELEMENTS_PROPERTIES_FILE = "/WEB-INF/celements.properties";

  @Requirement
  Container container;

  @Override
  public void initialize() throws InitializationException {
    URL propertiesURL;
    try {
      propertiesURL = container.getApplicationContext().getResource(CELEMENTS_PROPERTIES_FILE);
      setConfiguration(new PropertiesConfiguration(propertiesURL));
    } catch (ConfigurationException | MalformedURLException exc) {
      LOGGER.warn("Failed to load configuration file '{}'", CELEMENTS_PROPERTIES_FILE, exc);
      setConfiguration(new BaseConfiguration());
    }
  }
}
