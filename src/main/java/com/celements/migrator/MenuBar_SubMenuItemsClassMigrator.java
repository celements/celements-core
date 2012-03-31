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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.classes.ICelementsClassCollection;
import com.celements.iterator.XObjectIterator;
import com.celements.menu.IMenuService;
import com.celements.menu.MenuClasses;
import com.celements.migrations.SubSystemHibernateMigrationManager;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.migration.XWikiDBVersion;

@Component("MenuBar_SubMenuItemsClass")
public class MenuBar_SubMenuItemsClassMigrator
    extends AbstractCelementsHibernateMigrator {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      MenuBar_SubMenuItemsClassMigrator.class);

  @Requirement
  IMenuService menuService;

  @Requirement
  EntityReferenceResolver<String> referenceResolver;

  @Requirement
  Execution execution;

  @Requirement("celements.celMenuClasses")
  ICelementsClassCollection menuClassCollection;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  @Override
  public boolean shouldExecute(XWikiDBVersion startupVersion) {
    return getMenuClasses().isActivated(getContext());
  }

  private MenuClasses getMenuClasses() {
    return (MenuClasses) menuClassCollection;
  }

  @Override
  public void migrate(SubSystemHibernateMigrationManager manager, XWikiContext context
      ) throws XWikiException {
    getMenuClasses().runUpdate(getContext());
    List<String> result = context.getWiki().search(
        "select distinct o.name from BaseObject o"
        + " where o.className = 'Celements2.MenuBarSubItem'", context);
    LOGGER.info("found [" + ((result != null) ? result.size() : result)
        + "] documents to migrate.");
    for (Object fullName : result) {
      XWikiDocument doc = context.getWiki().getDocument(resolveDocument(
          fullName.toString()), context);
      XObjectIterator menuBarSubItemIterator = new XObjectIterator(getContext());
      menuBarSubItemIterator.setClassName("Celements2.MenuBarSubItem");
      menuBarSubItemIterator.setDocList(Arrays.asList(fullName.toString()));
      for (BaseObject oldSubItemObj : menuBarSubItemIterator) {
        migrateSubItem(oldSubItemObj, doc);
      }
      LOGGER.debug("migrating MenuBarSubItem on [" + doc.getDocumentReference() + "] "
          + doc.isMetaDataDirty() + ", " + doc.isContentDirty());
      // save directly over store method to prevent observation manager executing events.
      context.getWiki().getStore().saveXWikiDoc(doc, context);
    }
  }

  private void migrateSubItem(BaseObject oldSubItemObj, XWikiDocument doc
      ) throws XWikiException {
    BaseObject newSubItemObj = doc.newXObject(menuService.getMenuBarSubItemClassRef(),
        getContext());
    newSubItemObj.setStringValue("name", oldSubItemObj.getStringValue("name"));
    newSubItemObj.setIntValue("header_id", oldSubItemObj.getIntValue("header_id"));
    newSubItemObj.setIntValue("itempos", oldSubItemObj.getIntValue("id"));
    newSubItemObj.setStringValue("link", oldSubItemObj.getStringValue("link"));
    newSubItemObj.setStringValue("css_classes", "");
    LOGGER.trace("migrating [" + oldSubItemObj.getStringValue("name") + "]");
  }

  private DocumentReference resolveDocument(String docFullName) {
    DocumentReference eventRef = new DocumentReference(referenceResolver.resolve(
        docFullName, EntityType.DOCUMENT));
    eventRef.setWikiReference(new WikiReference(getContext().getDatabase()));
    LOGGER.debug("getDocRefFromFullName: for [" + docFullName + "] got reference ["
        + eventRef + "].");
    return eventRef;
  }

  public String getDescription() {
    return "'Migrating Celements2.MenuBarSubItem to Celements.MenuBarSubItemClass'";
  }

  public String getName() {
    return "MenuBar_SubMenuItemsClass";
  }

  /**
   * getVersion is using days since
   * 1.1.2010 until the day of committing this migration
   * 31.3.2012 -> 820
   * http://www.convertunits.com/dates/
   */
  public XWikiDBVersion getVersion() {
    return new XWikiDBVersion(820);
  }

}
