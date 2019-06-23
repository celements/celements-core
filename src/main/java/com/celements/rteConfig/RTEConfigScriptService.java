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
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.emptycheck.internal.IDefaultEmptyDocStrategyRole;
import com.google.common.base.MoreObjects;

@Component("rteconfig")
public class RTEConfigScriptService implements ScriptService {

  private static final String RTE_CONFIG_ACTIV_HINT = "RTE_CONFIG_ACTIV_HINT";

  @Requirement
  private IDefaultEmptyDocStrategyRole defaultEmptyDocStrategyRole;

  @Requirement
  private Map<String, RteConfigRole> rteConfigMap;

  @Requirement
  private Execution execution;

  private static Logger LOGGER = LoggerFactory.getLogger(RTEConfigScriptService.class);

  @NotNull
  public String getRTEConfigField(@NotNull String name) {
    try {
      return getActiveRteConfig().getRTEConfigField(name);
    } catch (Exception exp) {
      LOGGER.error("getRTEConfigField for name [" + name + "] failed.", exp);
    }
    return "";
  }

  public boolean isEmptyRTEString(@NotNull String rteContent) {
    return defaultEmptyDocStrategyRole.isEmptyRTEString(rteContent);
  }

  @NotNull
  public List<DocumentReference> getRTEConfigsList() {
    try {
      return getActiveRteConfig().getRTEConfigsList();
    } catch (Exception exp) {
      LOGGER.error("getRTEConfigsList failed.", exp);
    }
    return Collections.emptyList();
  }

  public void setRteConfigHint(@Nullable String rteConfigHint) {
    execution.getContext().setProperty(RTE_CONFIG_ACTIV_HINT, rteConfigHint);
  }

  @NotNull
  public String getRteConfigHint() {
    return (String) MoreObjects.firstNonNull(execution.getContext().getProperty(
        RTE_CONFIG_ACTIV_HINT), "default");
  }

  @Nullable
  private RteConfigRole getActiveRteConfig() {
    return rteConfigMap.get(getRteConfigHint());
  }

}
