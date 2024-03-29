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

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.AbstractClassCollection;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

@Component("celements.celNavigationClasses")
public class NavigationClasses extends AbstractClassCollection {

  /**
   * @deprecated instead use constants from INavigationClassConfig
   */
  @Deprecated
  public static final String MENU_NAME_CLASS_DOC = "MenuName";
  @Deprecated
  public static final String MENU_NAME_CLASS_SPACE = "Celements2";
  @Deprecated
  public static final String MENU_NAME_CLASS = MENU_NAME_CLASS_SPACE + "." + MENU_NAME_CLASS_DOC;
  @Deprecated
  public static final String MENU_NAME_FIELD = "menu_name";
  @Deprecated
  public static final String MENU_NAME_IMAGE_FIELD = "image";
  @Deprecated
  public static final String MENU_NAME_TOOLTIP_FIELD = "tooltip";
  @Deprecated
  public static final String MENU_NAME_LANG_FIELD = "lang";

  @Deprecated
  public static final String MAPPED_MENU_ITEM_CLASS_SPACE = "Classes";
  @Deprecated
  public static final String MAPPED_MENU_ITEM_CLASS_DOC = "MenuItemClass";
  @Deprecated
  public static final String MAPPED_MENU_ITEM_CLASS = MAPPED_MENU_ITEM_CLASS_SPACE + "."
      + MAPPED_MENU_ITEM_CLASS_DOC;

  @Deprecated
  public static final String NAVIGATION_CONFIG_CLASS_DOC = "NavigationConfigClass";
  @Deprecated
  public static final String NAVIGATION_CONFIG_CLASS_SPACE = MENU_NAME_CLASS_SPACE;
  @Deprecated
  public static final String NAVIGATION_CONFIG_CLASS = NAVIGATION_CONFIG_CLASS_SPACE + "."
      + NAVIGATION_CONFIG_CLASS_DOC;

  @Deprecated
  public static final String MENU_ELEMENT_NAME_FIELD = "menu_element_name";
  @Deprecated
  public static final String FROM_HIERARCHY_LEVEL_FIELD = "from_hierarchy_level";
  @Deprecated
  public static final String TO_HIERARCHY_LEVEL_FIELD = "to_hierarchy_level";
  @Deprecated
  public static final String SHOW_INACTIVE_TO_LEVEL_FIELD = "show_inactive_to_level";
  @Deprecated
  public static final String MENU_SPACE_FIELD = "menu_space";
  @Deprecated
  public static final String MENU_PART_FIELD = "menu_part";
  @Deprecated
  public static final String CM_CSS_CLASS_FIELD = "cm_css_class";
  @Deprecated
  public static final String LAYOUT_TYPE_FIELD = "layout_type";
  @Deprecated
  public static final String PRESENTATION_TYPE_FIELD = "presentation_type";

  @Deprecated
  public static final String MENU_ITEM_CLASS_DOC = "MenuItem";
  @Deprecated
  public static final String MENU_ITEM_CLASS_SPACE = MENU_NAME_CLASS_SPACE;
  @Deprecated
  public static final String MENU_ITEM_CLASS = MENU_ITEM_CLASS_SPACE + "." + MENU_ITEM_CLASS_DOC;
  @Deprecated
  public static final String MENU_POSITION_FIELD = "menu_position";
  @Deprecated
  public static final String PART_NAME_FIELD = "part_name";

  @Requirement
  INavigationClassConfig navClassCfg;

  @Override
  public String getConfigName() {
    return "celNavigationClasses";
  }

  @Override
  protected void initClasses() throws XWikiException {
    getMenuItemClass();
    getNewMenuItemClass();
    getMenuNameClass();
    getNavigationConfigClass();
  }

  /**
   * @deprecated instead use NavigationClassConfig().getMenuNameClassRef(String)
   */
  @Deprecated
  public DocumentReference getMenuNameClassRef(String wikiName) {
    return new DocumentReference(wikiName, MENU_NAME_CLASS_SPACE, MENU_NAME_CLASS_DOC);
  }

  BaseClass getMenuNameClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getMenuNameClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + MENU_NAME_CLASS + " class document.", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addTextField(MENU_NAME_FIELD, "Multilingual MenuName", 30);
    needsUpdate |= bclass.addTextField(MENU_NAME_LANG_FIELD, "Language", 5);
    needsUpdate |= bclass.addTextField(MENU_NAME_TOOLTIP_FIELD, "Tool Tip", 30);
    needsUpdate |= bclass.addTextField(MENU_NAME_IMAGE_FIELD, "Background Image", 30);

    if (!"internal".equals(bclass.getCustomMapping())) {
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  /**
   * @deprecated instead use NavigationClassConfig().getNavigationConfigClassRef(String)
   */
  @Deprecated
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
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + NAVIGATION_CONFIG_CLASS + " class document.", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addTextField(MENU_ELEMENT_NAME_FIELD, "Menu Element Name", 30);
    needsUpdate |= bclass.addNumberField(FROM_HIERARCHY_LEVEL_FIELD, "From Hierarchy Level", 30,
        "integer");
    needsUpdate |= bclass.addNumberField(TO_HIERARCHY_LEVEL_FIELD, "To Hierarchy Level", 30,
        "integer");
    needsUpdate |= bclass.addNumberField(SHOW_INACTIVE_TO_LEVEL_FIELD,
        "Always Show Inactive To Level", 30, "integer");
    needsUpdate |= bclass.addTextField(MENU_SPACE_FIELD, "Menu Space Name (leave empty"
        + " for current space)", 30);
    needsUpdate |= bclass.addTextField(MENU_PART_FIELD, "Menu Part Name", 30);
    needsUpdate |= bclass.addTextField(CM_CSS_CLASS_FIELD,
        "Context Menu CSS Class Name (empty for default)", 30);
    needsUpdate |= bclass.addTextField("data_type",
        "Navigation Source Data Type (empty for general Page Menu)", 30);
    needsUpdate |= bclass.addTextField(LAYOUT_TYPE_FIELD,
        "Navigation Layout Type (empty for html list)", 30);
    needsUpdate |= bclass.addTextField(PRESENTATION_TYPE_FIELD,
        "Navigation Presentation Type (empty for menu name links)", 30);
    needsUpdate |= bclass.addNumberField(INavigationClassConfig.ITEMS_PER_PAGE, "Number"
        + " of items showed per page when using pageing", 30, "integer");

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  /**
   * @deprecated instead use NavigationClassConfig().getMenuItemClassRef(String)
   */
  @Deprecated
  public DocumentReference getMenuItemClassRef(String wikiName) {
    return new DocumentReference(wikiName, MENU_ITEM_CLASS_SPACE, MENU_ITEM_CLASS_DOC);
  }

  BaseClass getMenuItemClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = navClassCfg.getMenuItemClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + INavigationClassConfig.MENU_ITEM_CLASS + " class document.",
          exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addNumberField(INavigationClassConfig.MENU_POSITION_FIELD, "Position", 30,
        "integer");
    needsUpdate |= bclass.addTextField(INavigationClassConfig.PART_NAME_FIELD, "Menu Part Name",
        30);
    needsUpdate |= bclass.addTextField(INavigationClassConfig.TARGET_FIELD, "Link Target", 30);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  /**
   * @deprecated instead use NavigationClassConfig().getNewMenuItemClassRef(String)
   */
  @Deprecated
  public DocumentReference getNewMenuItemClassRef(String wikiName) {
    return new DocumentReference(wikiName, MAPPED_MENU_ITEM_CLASS_SPACE,
        MAPPED_MENU_ITEM_CLASS_DOC);
  }

  BaseClass getNewMenuItemClass() throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = getContext().getWiki();
    boolean needsUpdate = false;
    DocumentReference classRef = navClassCfg.getNewMenuItemClassRef(getContext().getDatabase());

    try {
      doc = xwiki.getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + INavigationClassConfig.MAPPED_MENU_ITEM_CLASS
          + " class document.", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addNumberField(INavigationClassConfig.MENU_POSITION_FIELD, "Position", 30,
        "integer");
    needsUpdate |= bclass.addTextField(INavigationClassConfig.PART_NAME_FIELD, "Menu Part Name",
        30);

    if (!"internal".equals(bclass.getCustomMapping())) {
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

}
