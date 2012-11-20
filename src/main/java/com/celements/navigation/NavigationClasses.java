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

import com.celements.common.classes.AbstractClassCollection;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

@Component("celements.celNavigationClasses")
public class NavigationClasses extends AbstractClassCollection {

  public static final String NAVIGATION_CONFIG_PRESENTATION_TYPE = "presentation_type";
  public static final String NAVIGATION_CONFIG_CLASS_DOC = "NavigationConfigClass";
  public static final String NAVIGATION_CONFIG_CLASS_SPACE = "Celements2";
  public static final String NAVIGATION_CONFIG_CLASS = NAVIGATION_CONFIG_CLASS_SPACE
      + "." + NAVIGATION_CONFIG_CLASS_DOC;

  public static final String MENU_ITEM_CLASS_DOC = "MenuItem";
  public static final String MENU_ITEM_CLASS_SPACE = "Celements2";

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      NavigationClasses.class);

  public String getConfigName() {
    return "celNavigationClasses";
  }

  @Override
  protected Log getLogger() {
    return LOGGER;
  }

  @Override
  protected void initClasses() throws XWikiException {
    getMenuItemClass();
    getNewMenuItemClass();
    getMenuNameClass();
    getNavigationConfigClass();
  }

  public DocumentReference getMenuNameClassRef(String wikiName) {
    return new DocumentReference(wikiName, "Celements2", "MenuName");
  }

  BaseClass getMenuNameClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getMenuNameClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException e) {
      LOGGER.error(e);
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

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getNavigationConfigClassRef(String wikiName) {
    return new DocumentReference(wikiName, NAVIGATION_CONFIG_CLASS_SPACE,
        NAVIGATION_CONFIG_CLASS_DOC);
  }

  BaseClass getNavigationConfigClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getNavigationConfigClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException e) {
      LOGGER.error(e);
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
    needsUpdate |= bclass.addTextField("menu_space", "Menu Space Name (leave empty for "
        + "current space)", 30);
    needsUpdate |= bclass.addTextField("menu_part", "Menu Part Name", 30);
    needsUpdate |= bclass.addTextField("cm_css_class",
        "Context Menu CSS Class Name (empty for default)", 30);
    needsUpdate |= bclass.addTextField("data_type",
        "Navigation Source Data Type (empty for general Page Menu)", 30);
    needsUpdate |= bclass.addTextField("layout_type",
        "Navigation Layout Type (empty for html list)", 30);
    needsUpdate |= bclass.addTextField(NAVIGATION_CONFIG_PRESENTATION_TYPE,
        "Navigation Presentation Type (empty for menu name links)", 30);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getMenuItemClassRef(String wikiName) {
    return new DocumentReference(wikiName, MENU_ITEM_CLASS_SPACE, MENU_ITEM_CLASS_DOC);
  }

  BaseClass getMenuItemClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getMenuItemClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException e) {
      LOGGER.error(e);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(classRef);
    needsUpdate |= bclass.addNumberField("menu_position", "Position", 30, "integer");
    needsUpdate |= bclass.addTextField("part_name", "Menu Part Name", 30);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getNewMenuItemClassRef(String wikiName) {
    return new DocumentReference(wikiName, "Classes", "MenuItemClass");
  }

  BaseClass getNewMenuItemClass() throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = getContext().getWiki();
    boolean needsUpdate = false;
    DocumentReference classRef = getNewMenuItemClassRef(getContext().getDatabase());

    try {
      doc = xwiki.getDocument(classRef, getContext());
    } catch (XWikiException e) {
      LOGGER.error(e);
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

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

}
