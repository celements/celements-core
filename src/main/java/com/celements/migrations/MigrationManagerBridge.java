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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.store.migration.AbstractXWikiMigrationManager;
import com.xpn.xwiki.store.migration.XWikiDBVersion;
import com.xpn.xwiki.store.migration.XWikiMigratorInterface;
import com.xpn.xwiki.web.Utils;

/**
 * MigrationManagerBridge implements a bridge for the XWikiMigrationManager hook xwiki.cfg
 * to any general Migration Manager implementing the ISubSystemMigrationCoordinator
 * component rule. It extends the AbstractXWikiMigrationManager for that only reason.
 * 
 * @author fabian
 *
 */
public class MigrationManagerBridge extends AbstractXWikiMigrationManager {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      MigrationManagerBridge.class);

  public MigrationManagerBridge(XWikiContext context) throws XWikiException {
    super(context);
  }

  @Override
  protected List<XWikiMigratorInterface> getAllMigrations(XWikiContext context)
      throws XWikiException {
    throw new UnsupportedOperationException();
  }

  @Override
  protected void setDBVersion(XWikiDBVersion arg0, XWikiContext arg1)
      throws XWikiException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void startMigrations(XWikiContext context) throws XWikiException {
    LOGGER.info("startCoordinater in MigrationManagerBridge");
    Utils.getComponent(ISubSystemMigrationCoordinator.class
        ).startSubSystemMigrations(context);
  }

}
