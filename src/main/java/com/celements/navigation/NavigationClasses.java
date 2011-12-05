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
package com.celements.navigation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.CelementsClassCollection;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

@Component("celements.celNavigationClasses")
public class NavigationClasses extends CelementsClassCollection {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      NavigationClasses.class);

  public String getConfigName() {
    return "celNavigationClasses";
  }

  @Override
  protected Log getLogger() {
    return mLogger;
  }

  @Override
  protected void initClasses(XWikiContext context) throws XWikiException {
    getMenuItemClass(context);
    getNewMenuItemClass(context);
    getMenuNameClass(context);
    getNavigationConfigClass(context);
  }

  protected BaseClass getMenuNameClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = new DocumentReference(context.getDatabase(),
        "Celements2", "MenuName");

    try {
      doc = context.getWiki().getDocument(classRef, context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(classRef);
    needsUpdate |= bclass.addTextField("menu_name", "Multilingual MenuName", 30);
    needsUpdate |= bclass.addTextField("lang", "Language", 5);
    needsUpdate |= bclass.addTextField("tooltip", "Tool Tip", 30);
    needsUpdate |= bclass.addTextField("image", "Background Image", 30);

    if (!"internal".equals(bclass.getCustomMapping())) {
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  protected BaseClass getNavigationConfigClass(XWikiContext context)
      throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = new DocumentReference(context.getDatabase(),
        "Celements2", "NavigationConfigClass");

    try {
      doc = context.getWiki().getDocument(classRef, context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(classRef);
    needsUpdate |= bclass.addTextField("menu_element_name", "Menu Element Name", 30);
    needsUpdate |= bclass.addNumberField("from_hierarchy_level", "From Hierarchy Level",
        30, "integer");
    needsUpdate |= bclass.addNumberField("to_hierarchy_level", "To Hierarchy Level", 30,
        "integer");
    needsUpdate |= bclass.addNumberField("show_inactive_to_level",
        "Always Show Inactive To Level", 30, "integer");
    needsUpdate |= bclass.addTextField("menu_part", "Menu Part Name", 30);
    needsUpdate |= bclass.addTextField("cm_css_class",
        "Context Menu CSS Class Name (empty for default)", 30);
    needsUpdate |= bclass.addTextField("data_type",
        "Navigation Source Data Type (empty for general Page Menu)", 30);
    needsUpdate |= bclass.addTextField("layout_type",
        "Navigation Layout Type (empty for html list)", 30);

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  protected BaseClass getMenuItemClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = new DocumentReference(context.getDatabase(),
        "Celements2", "MenuItem");

    try {
      doc = context.getWiki().getDocument(classRef, context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(classRef);
    needsUpdate |= bclass.addNumberField("menu_position", "Position", 30, "integer");
    needsUpdate |= bclass.addTextField("part_name", "Menu Part Name", 30);

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  protected BaseClass getNewMenuItemClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;
    DocumentReference classRef = new DocumentReference(context.getDatabase(),
        "Classes", "MenuItemClass");

    try {
      doc = xwiki.getDocument(classRef, context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(classRef);
    needsUpdate |= bclass.addNumberField("menu_position", "Position", 30, "integer");
    needsUpdate |= bclass.addTextField("part_name", "Menu Part Name", 30);

    if (!"internal".equals(bclass.getCustomMapping())) {
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

}
