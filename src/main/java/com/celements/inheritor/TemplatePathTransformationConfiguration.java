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
package com.celements.inheritor;

import java.util.Properties;

import org.xwiki.component.annotation.ComponentRole;

/**
 * Configuration properties for the celements Template inheritance Path transformation.
 * <p>
 * You can override the default values for each of the configuration properties
 * below by defining them in XWiki's global configuration file using a prefix of
 * "celRendering.transformation.templatePath" followed by the property name. For example:
 * <code>celRendering.transformation.templatePath.mappings = ...</code>
 * 
 */
@ComponentRole
public interface TemplatePathTransformationConfiguration {
  /**
   * @return the mappings between a set of characters representing a path on disk (eg
   *         ":Templates.", ":Ajax.") and a path on disk (eg "celTemplates/", "celAjax/")
   */
  Properties getMappings();
}
