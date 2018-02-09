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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.migrations.SubSystemHibernateMigrationManager;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.migration.XWikiDBVersion;
import com.xpn.xwiki.web.Utils;

@Component("TreeNodeRelativeParent_Database")
public class TreeNodeRelativeParent_Database extends AbstractCelementsHibernateMigrator {

  private static Logger _LOGGER = LoggerFactory.getLogger(
      TreeNodeRelativeParent_Database.class);

  @Requirement
  private QueryManager queryManager;

  @Requirement
  IWebUtilsService webUtilsService;

  EntityReference getRelativeParentReference(String parentFN) {
    @SuppressWarnings("unchecked")
    EntityReferenceResolver<String> relativResolver = Utils.getComponent(
        EntityReferenceResolver.class, "relative");
    return relativResolver.resolve(parentFN, EntityType.DOCUMENT);
  }

  @Override
  public void migrate(SubSystemHibernateMigrationManager manager, XWikiContext context
      ) throws XWikiException {
    Query theQuery;
    try {
      theQuery = queryManager.createQuery("from doc.object(Celements2.MenuItem) as mItem"
          + " where doc.parent like :buggyParent", Query.XWQL);
      theQuery.bindValue("buggyParent", context.getDatabase() + ":%");
      List<String> result = theQuery.execute();
      _LOGGER.info("found [" + ((result != null) ? result.size() : result)
          + "] documents to migrate.");
      for (String fullName : result) {
        XWikiDocument doc = context.getWiki().getDocument(
            webUtilsService.resolveDocumentReference(fullName), context);
        String parentFN = webUtilsService.getRefLocalSerializer().serialize(
            doc.getParentReference());
        doc.setParentReference(getRelativeParentReference(parentFN));
        _LOGGER.debug("migrating TreeNodes parent on [" + fullName + "] "
            + doc.isMetaDataDirty() + ", " + doc.isContentDirty());
        // save directly over store method to prevent observation manager executing events.
        context.getWiki().saveDocument(doc, "TreeNodeRelativeParent_Database Migration",
            context);
      }
    } catch (QueryException exp) {
      _LOGGER.error("cannot create query for TreeNodeRelativeParent_Database Migration ",
          exp);
      throw new XWikiException(XWikiException.MODULE_XWIKI_APP,
          XWikiException.MODULE_XWIKI, "Failed to execute migration"
          + " TreeNodeRelativeParent_Database", exp);
    }
  }

  public String getDescription() {
    return "'reduce document parents to relative parents for tree nodes'";
  }

  public String getName() {
    return "TreeNodeRelativeParent_Database";
  }

  /**
   * getVersion is using days since
   * 1.1.2010 until the day of committing this migration
   * 1.3.2015 -> 1885
   * http://www.convertunits.com/dates/
   */
  public XWikiDBVersion getVersion() {
    return new XWikiDBVersion(1885);
  }

}
