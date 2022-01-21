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
package com.celements.cells;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.classes.GroupCellClass;
import com.celements.cells.classes.PageDepCellConfigClass;
import com.celements.cells.classes.PageLayoutPropertiesClass;
import com.celements.common.classes.AbstractClassCollection;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

@Component("celements.celCellsClasses")
public class CellsClasses extends AbstractClassCollection {

  @Requirement
  ICellsClassConfig cellsClassConfig;

  /**
   * @deprecated since 24-07-2014 instead use ICellsClassConfig
   */
  @Deprecated
  public static final String CELEMENTS_CELL_CLASS_SPACE = ICellsClassConfig.CELEMENTS_CELL_CLASS_SPACE;
  /**
   * @deprecated since 24-07-2014 instead use ICellsClassConfig
   */
  @Deprecated
  public static final String CELEMENTS_CELL_CLASS_NAME = ICellsClassConfig.CELEMENTS_CELL_CLASS_NAME;
  /**
   * @deprecated since 24-07-2014 instead use ICellsClassConfig
   */
  @Deprecated
  public static final String CELEMENTS_CELL_CLASS = ICellsClassConfig.CELEMENTS_CELL_CLASS;
  /**
   * @deprecated since 24-07-2014 instead use ICellsClassConfig
   */
  @Deprecated
  public static final String CELLCLASS_IDNAME_FIELD = ICellsClassConfig.CELLCLASS_IDNAME_FIELD;

  public CellsClasses() {}

  @Override
  protected void initClasses() throws XWikiException {
    LOGGER.debug("entering initClasses for database: {}", getContext().getDatabase());
    // getPageLayoutPropertiesClass();
    // getGroupCellClass();
    // getPageDepCellConfigClass();
    getTranslationBoxCellConfigClass();
  }

  @Override
  public String getConfigName() {
    return "celCellsClasses";
  }

  /**
   * @deprecated since 24-07-2014 instead use ICellsClassConfig.getCellClassRef
   */
  @Deprecated
  public DocumentReference getCellClassRef(String wikiName) {
    return new DocumentReference(wikiName, CELEMENTS_CELL_CLASS_SPACE, CELEMENTS_CELL_CLASS_NAME);
  }

  @Deprecated
  BaseClass getCellClass() throws XWikiException {
    boolean needsUpdate = false;
    DocumentReference cellClassRef = cellsClassConfig.getCellClassRef(getContext().getDatabase());
    XWikiDocument doc = modelAccess.getOrCreateDocument(cellClassRef);
    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(cellClassRef);
    needsUpdate |= bclass.addTextField(ICellsClassConfig.CELLCLASS_TAGNAME_FIELD, "tag name", 30);
    needsUpdate |= bclass.addTextField(ICellsClassConfig.CELLCLASS_IDNAME_FIELD, "id attribute",
        30);
    needsUpdate |= bclass.addTextField("css_classes", "CSS Classes", 30);
    needsUpdate |= bclass.addTextAreaField("css_styles", "CSS Styles", 15, 20);

    if (!"internal".equals(bclass.getCustomMapping())) {
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  /**
   * @deprecated since 24-07-2014 instead use ICellsClassConfig.getPageLayoutPropertiesClassRef
   */
  @Deprecated
  public DocumentReference getPageLayoutPropertiesClassRef(String wikiName) {
    return new DocumentReference(wikiName, "Celements", "PageLayoutPropertiesClass");
  }

  /**
   * @deprecated since 5.4 instead use {@link PageLayoutPropertiesClass}
   */
  @Deprecated
  public void getPageLayoutPropertiesClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference pageLayoutPropertiesClassRef = cellsClassConfig
        .getPageLayoutPropertiesClassRef(getContext().getDatabase());
    doc = modelAccess.getOrCreateDocument(pageLayoutPropertiesClassRef);
    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(pageLayoutPropertiesClassRef);
    needsUpdate |= bclass.addTextField("prettyname", "Layout Pretty Name", 30);
    needsUpdate |= bclass.addBooleanField("isActive", "is active", "yesno");
    needsUpdate |= bclass.addTextField("authors", "Authors", 30);
    needsUpdate |= bclass.addTextAreaField("license", "License", 15, 30);
    needsUpdate |= bclass.addTextField("version", "Version", 30);
    needsUpdate |= bclass.addStaticListField(ICellsClassConfig.LAYOUT_DOCTYPE_FIELD, "Doctype", 1,
        false, HtmlDoctype.XHTML + "|" + HtmlDoctype.HTML5, "select");
    needsUpdate |= bclass.addStaticListField(ICellsClassConfig.LAYOUT_TYPE_FIELD, "Layout Type", 1,
        false, ICellsClassConfig.PAGE_LAYOUT_VALUE + "|" + ICellsClassConfig.EDITOR_LAYOUT_VALUE,
        "select");

    if (!"internal".equals(bclass.getCustomMapping())) {
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }

    LOGGER.debug("getPageLayoutPropertiesClass for db '{}' needs update '{}'.",
        getContext().getDatabase(), needsUpdate);
    setContentAndSaveClassDocument(doc, needsUpdate);
  }

  /**
   * @deprecated since 24-07-2014 instead use ICellsClassConfig.getGroupCellClassRef
   */
  @Deprecated
  public DocumentReference getGroupCellClassRef(String wikiName) {
    return new DocumentReference(wikiName, "Celements", "GroupCellClass");
  }

  /**
   * @deprecated since 5.4 instead use {@link GroupCellClass}
   */
  @Deprecated
  public void getGroupCellClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference groupCellClassRef = getGroupCellClassRef(getContext().getDatabase());
    doc = modelAccess.getOrCreateDocument(groupCellClassRef);
    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(groupCellClassRef);
    needsUpdate |= bclass.addTextField("render_layout", "Render Layout", 30);

    setContentAndSaveClassDocument(doc, needsUpdate);
  }

  /**
   * @deprecated since 24-07-2014 instead use ICellsClassConfig.getPageDepCellConfigClassRef
   */
  @Deprecated
  public DocumentReference getPageDepCellConfigClassRef(String wikiName) {
    return new DocumentReference(wikiName, "Celements", "PageDepCellConfigClass");
  }

  /**
   * @deprecated since 5.4 instead use {@link PageDepCellConfigClass}
   */
  @Deprecated
  public void getPageDepCellConfigClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference pageDepCellConfigClassRef = getPageDepCellConfigClassRef(
        getContext().getDatabase());
    doc = modelAccess.getOrCreateDocument(pageDepCellConfigClassRef);
    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(pageDepCellConfigClassRef);
    needsUpdate |= bclass.addTextField("space_name", "Space Name", 30);
    needsUpdate |= bclass.addBooleanField("is_inheritable", "is inheritable", "yesno");

    setContentAndSaveClassDocument(doc, needsUpdate);
  }

  private void getTranslationBoxCellConfigClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference translationBoxCellConfigClassRef = cellsClassConfig
        .getTranslationBoxCellConfigClassRef(getContext().getDatabase());
    doc = modelAccess.getOrCreateDocument(translationBoxCellConfigClassRef);
    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(translationBoxCellConfigClassRef);
    needsUpdate |= bclass.addTextField("page_exceptions", "Page Exceptions (FullNames"
        + " comma separated)", 30);
    needsUpdate |= bclass.addTextField("pagetype_exceptions", "Page Type Exceptions"
        + " (FullNames comma separated)", 30);

    setContentAndSaveClassDocument(doc, needsUpdate);
  }

}
