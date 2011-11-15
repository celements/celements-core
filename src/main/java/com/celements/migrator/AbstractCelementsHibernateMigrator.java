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
package com.celements.migrator;

import com.celements.migrations.ICelementsMigrator;
import com.celements.migrations.SubSystemHibernateMigrationManager;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.store.migration.XWikiDBVersion;
import com.xpn.xwiki.store.migration.XWikiMigrationManagerInterface;
import com.xpn.xwiki.store.migration.XWikiMigratorInterface;

public abstract class AbstractCelementsHibernateMigrator implements ICelementsMigrator {

  public void migrate(XWikiMigrationManagerInterface manager, XWikiContext context)
      throws XWikiException {
    migrate((SubSystemHibernateMigrationManager) manager, context);
  }

  /**
   * {@inheritDoc}
   * 
   * @see com.xpn.xwiki.store.migration.XWikiMigratorInterface#shouldExecute(XWikiDBVersion)
   */
  public boolean shouldExecute(XWikiDBVersion startupVersion) {
    return true;
  }

  /** @see XWikiMigratorInterface#migrate(XWikiMigrationManagerInterface,XWikiContext) */
  public abstract void migrate(SubSystemHibernateMigrationManager manager,
      XWikiContext context) throws XWikiException;
}
