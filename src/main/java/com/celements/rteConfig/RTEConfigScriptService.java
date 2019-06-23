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
package com.celements.rteConfig;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.emptycheck.internal.IDefaultEmptyDocStrategyRole;

@Component("rteconfig")
public class RTEConfigScriptService implements ScriptService {

  @Requirement
  IDefaultEmptyDocStrategyRole defaultEmptyDocStrategyRole;

  @Requirement
  RteConfigRole rteConfig;

  private static Logger LOGGER = LoggerFactory.getLogger(RTEConfigScriptService.class);

  public String getRTEConfigField(String name) {
    try {
      return rteConfig.getRTEConfigField(name);
    } catch (Exception exp) {
      LOGGER.error("getRTEConfigField for name [" + name + "] failed.", exp);
    }
    return "";
  }

  public boolean isEmptyRTEString(String rteContent) {
    return defaultEmptyDocStrategyRole.isEmptyRTEString(rteContent);
  }

  public List<DocumentReference> getRTEConfigsList() {
    try {
      return rteConfig.getRTEConfigsList();
    } catch (Exception exp) {
      LOGGER.error("getRTEConfigsList failed.", exp);
    }
    return Collections.emptyList();
  }
}
