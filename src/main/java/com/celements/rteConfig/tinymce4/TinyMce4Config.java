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
package com.celements.rteConfig.tinymce4;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.rteConfig.RteConfigRole;

@Component(TinyMce4Config.HINT)
public class TinyMce4Config implements RteConfigRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(TinyMce4Config.class);

  /**
   * CAUTION: do not change the HINT it will be used from the vm-scripts
   */
  public static final String HINT = "tinymce4";

  @Requirement
  private RteConfigRole rteConfig;

  @Override
  public List<DocumentReference> getRTEConfigsList() {
    final List<DocumentReference> rteConfigsList = rteConfig.getRTEConfigsList();
    LOGGER.debug("getRTEConfigsList: returning '{}'", rteConfigsList);
    return rteConfigsList;
  }

  @Override
  public String getRTEConfigField(String name) {
    final String rteConfigField = rteConfig.getRTEConfigField(name);
    LOGGER.debug("getRTEConfigField for '{}': returning '{}'", name, rteConfigField);
    return rteConfigField;
  }

}
