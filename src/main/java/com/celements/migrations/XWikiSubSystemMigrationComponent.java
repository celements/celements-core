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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.store.migration.hibernate.XWikiHibernateMigrationManager;

@Component("XWikiSubSystem")
public class XWikiSubSystemMigrationComponent implements ISubSystemMigrationManager {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      CelSubSystemMigrationCoordinator.class);
  XWikiHibernateMigrationManager injected_MigrationManager;

  public String getSubSystemName() {
    return "XWikiSubSystem";
  }

  public void startMigrations(XWikiContext context) throws XWikiException {
    XWikiHibernateMigrationManager xwikiMigrationManager = null;
    try {
      xwikiMigrationManager = getXWikiHibernateMigrationManager(context);
    } catch (XWikiException exp) {
      mLogger.error("Failed to instanciate XWikiHibernateMigrationManager!", exp);
    }
    if (xwikiMigrationManager != null) {
      xwikiMigrationManager.startMigrations(context);
    }
  }

  XWikiHibernateMigrationManager getXWikiHibernateMigrationManager(
      XWikiContext context) throws XWikiException {
    if (injected_MigrationManager != null) {
      return injected_MigrationManager;
    }
    return new XWikiHibernateMigrationManager(context);
  }

}
