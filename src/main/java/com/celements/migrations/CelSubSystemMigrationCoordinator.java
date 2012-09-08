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
package com.celements.migrations;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

/**
 * CelSubSystemMigrationCoordinator implements a general Migration coordinator
 * for several subsystems.
 *  
 * @author fabian
 *
 */
@Component
public class CelSubSystemMigrationCoordinator implements ISubSystemMigrationCoordinator {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      CelSubSystemMigrationCoordinator.class);
  
  @Requirement
  private Map<String, ISubSystemMigrationManager> subSysMigrationManagerMap;
  
  public CelSubSystemMigrationCoordinator(){}

  public void startSubSystemMigrations(XWikiContext context) throws XWikiException {
    if ((subSysMigrationManagerMap != null) && (subSysMigrationManagerMap.size() > 0)) {
      LOGGER.debug("Found [" + subSysMigrationManagerMap.size()
          + "] SubSystemMigrationManagers [" + subSysMigrationManagerMap.keySet() + "].");
      String[] subSysMigConfig = context.getWiki().getConfig().getPropertyAsList(
          "celements.subsystems.migration.manager.order");
      if (subSysMigConfig.length == 0) {
        subSysMigConfig = new String[]{"XWikiSubSystem"};
      }
      LOGGER.info("executing following subsystem migration manager in this order: "
          + Arrays.deepToString(subSysMigConfig));
      for (String subSystemHintName : subSysMigConfig) {
        ISubSystemMigrationManager subSystemMigrationManager =
          subSysMigrationManagerMap.get(subSystemHintName);
        if ("1".equals(context.getWiki().Param("celements.subsystems." + subSystemHintName
            + ".migration", "0"))) {
          LOGGER.info("starting migration for ["
              + subSystemMigrationManager.getSubSystemName() + "].");
          subSystemMigrationManager.startMigrations(context);
          LOGGER.info("finished migration for ["
              + subSystemMigrationManager.getSubSystemName() + "].");
        } else {
          LOGGER.info("skipping migration for [" + subSystemHintName + "].");
        }
      }
    } else {
      LOGGER.fatal("allSubMigrationManagers is empty. Expecting at least the"
          + " xwikiSubSystem migration manager. " + subSysMigrationManagerMap);
    }
  }

}
