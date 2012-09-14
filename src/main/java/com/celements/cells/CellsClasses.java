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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.AbstractClassCollection;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

@Component("celements.celCellsClasses")
public class CellsClasses extends AbstractClassCollection {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      CellsClasses.class);

  public static final String CELEMENTS_CELL_CLASS_SPACE = "Celements";
  public static final String CELEMENTS_CELL_CLASS_NAME = "CellClass";
  public static final String CELEMENTS_CELL_CLASS = CELEMENTS_CELL_CLASS_SPACE + "."
    + CELEMENTS_CELL_CLASS_NAME;

  public CellsClasses() {}

  @Override
  protected void initClasses() throws XWikiException {
    LOGGER.debug("entering initClasses for database: " + getContext().getDatabase());
    getCellClass();
    getPageLayoutPropertiesClass();
    getGroupCellClass();
    getPageDepCellConfigClass();
  }

  @Override
  protected Log getLogger() {
    return LOGGER;
  }

  public String getConfigName() {
    return "celCellsClasses";
  }

  public DocumentReference getCellClassRef(String wikiName) {
    return new DocumentReference(wikiName, CELEMENTS_CELL_CLASS_SPACE,
        CELEMENTS_CELL_CLASS_NAME);
  }

  BaseClass getCellClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference cellClassRef = getCellClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(cellClassRef, getContext());
    } catch (XWikiException e) {
      LOGGER.error(e);
      doc = new XWikiDocument(cellClassRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(cellClassRef);
    needsUpdate |= bclass.addTextField("idname", "id attribute", 30);
    needsUpdate |= bclass.addTextField("css_classes", "CSS Classes", 30);
    needsUpdate |= bclass.addTextAreaField("css_styles", "CSS Styles", 15, 20);
    
    if(!"internal".equals(bclass.getCustomMapping())){
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }
    
    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getPageLayoutPropertiesClassRef(String wikiName) {
    return new DocumentReference(wikiName, "Celements", "PageLayoutPropertiesClass");
  }

  BaseClass getPageLayoutPropertiesClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference pageLayoutPropertiesClassRef = getPageLayoutPropertiesClassRef(
        getContext().getDatabase());
    
    try {
      doc = getContext().getWiki().getDocument(pageLayoutPropertiesClassRef,
          getContext());
    } catch (XWikiException e) {
      LOGGER.error(e);
      doc = new XWikiDocument(pageLayoutPropertiesClassRef);
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(pageLayoutPropertiesClassRef);
    needsUpdate |= bclass.addTextField("prettyname", "Layout Pretty Name", 30);
    needsUpdate |= bclass.addBooleanField("isActive", "is active", "yesno");
    needsUpdate |= bclass.addTextField("authors", "Authors", 30);
    needsUpdate |= bclass.addTextAreaField("license", "License", 15, 30);
    needsUpdate |= bclass.addTextField("version", "Version", 30);
    
    if(!"internal".equals(bclass.getCustomMapping())){
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }
    
    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getGroupCellClassRef(String wikiName) {
    return new DocumentReference(wikiName, "Celements", "GroupCellClass");
  }

  BaseClass getGroupCellClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference groupCellClassRef = getGroupCellClassRef(getContext().getDatabase()
        );
    
    try {
      doc = getContext().getWiki().getDocument(groupCellClassRef, getContext());
    } catch (XWikiException e) {
      LOGGER.error(e);
      doc = new XWikiDocument(groupCellClassRef);
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(groupCellClassRef);
    needsUpdate |= bclass.addTextField("render_layout", "Render Layout", 30);
    
    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getPageDepCellConfigClassRef(String wikiName) {
    return new DocumentReference(wikiName, "Celements", "PageDepCellConfigClass");
  }

  BaseClass getPageDepCellConfigClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference pageDepCellConfigClassRef = getPageDepCellConfigClassRef(
        getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(pageDepCellConfigClassRef, getContext());
    } catch (XWikiException e) {
      LOGGER.error(e);
      doc = new XWikiDocument(pageDepCellConfigClassRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(pageDepCellConfigClassRef);
    needsUpdate |= bclass.addTextField("space_name", "Space Name", 30);
    needsUpdate |= bclass.addBooleanField("is_inheritable", "is inheritable", "yesno");

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

}
