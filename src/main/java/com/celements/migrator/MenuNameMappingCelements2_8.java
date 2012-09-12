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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;

import com.celements.migrations.SubSystemHibernateMigrationManager;
import com.celements.navigation.NavigationClasses;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.migration.XWikiDBVersion;

@Component("MenuNameMappingCelements2_8")
public class MenuNameMappingCelements2_8 extends AbstractCelementsHibernateMigrator {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      MenuNameMappingCelements2_8.class);

  @Override
  public void migrate(SubSystemHibernateMigrationManager manager, XWikiContext context
      ) throws XWikiException {
    manager.updateSchema(context);
    new NavigationClasses().runUpdate();
    List<Object> result = context.getWiki().search(
        "select o.name from BaseObject o, StringProperty s"
        + " where o.className = 'Celements2.MenuName' and o.id = s.id"
        + " and s.name = 'menu_name'", context);
    mLogger.info("found [" + ((result != null) ? result.size() : result)
        + "] documents to migrate.");
    for (Object fullName : result) {
      XWikiDocument doc = context.getWiki().getDocument(fullName.toString(), context);
   // we do not want a new history entry. Thus we cancel MetaData and Content Dirty flags
      doc.setMetaDataDirty(false);
      doc.setContentDirty(false);
      mLogger.debug("migrating MenuName on [" + doc.getFullName() + "] "
          + doc.isMetaDataDirty() + ", " + doc.isContentDirty());
      // save directly over store method to prevent observation manager executing events.
      context.getWiki().getStore().saveXWikiDoc(doc, context);
    }
  }

  public String getDescription() {
    return "'Adding HBM Mapping for MenuName'";
  }

  public String getName() {
    return "MenuNameMappingCelements2_8";
  }

  /**
   * getVersion is using days since
   * 1.1.2010 until the day of committing this migration
   * 21.6.2011 -> 536
   */
  public XWikiDBVersion getVersion() {
    return new XWikiDBVersion(536);
  }

}
