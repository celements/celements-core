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

import com.celements.common.classes.CelementsClassCollection;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

public class CellsClasses extends CelementsClassCollection {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      CellsClasses.class);
  private static CellsClasses instance;

  private CellsClasses() {
  }

  public static CellsClasses getInstance() {
    if (instance == null) {
      instance = new CellsClasses();
    }
    return instance;
  }

  @Override
  protected void initClasses(XWikiContext context) throws XWikiException {
    mLogger.debug("entering initClasses for database: " + context.getDatabase());
    getCellClass(context);
    getPageLayoutPropertiesClass(context);
    getGroupCellClass(context);
    getPageDepCellConfigClass(context);
  }

  @Override
  protected Log getLogger() {
    return mLogger;
  }

  public String getConfigName() {
    return "celCellsClasses";
  }

  protected BaseClass getCellClass(XWikiContext context
      ) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;
    
    try {
      doc = xwiki.getDocument("Celements.CellClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Celements");
      doc.setName("CellClass");
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getxWikiClass();
    bclass.setName("Celements.CellClass");
    needsUpdate |= bclass.addTextField("idname", "id attribute", 30);
    needsUpdate |= bclass.addTextField("css_classes", "CSS Classes", 30);
    needsUpdate |= bclass.addTextAreaField("css_styles", "CSS Styles", 15, 20);
    
    if(!"internal".equals(bclass.getCustomMapping())){
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }
    
    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  protected BaseClass getPageLayoutPropertiesClass(XWikiContext context
      ) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;
    
    try {
      doc = xwiki.getDocument("Celements.PageLayoutPropertiesClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Celements");
      doc.setName("PageLayoutPropertiesClass");
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getxWikiClass();
    bclass.setName("Celements.PageLayoutPropertiesClass");
    needsUpdate |= bclass.addTextField("prettyname", "Layout Pretty Name", 30);
    needsUpdate |= bclass.addBooleanField("isActive", "is active", "yesno");
    needsUpdate |= bclass.addTextField("authors", "Authors", 30);
    needsUpdate |= bclass.addTextAreaField("license", "License", 15, 30);
    needsUpdate |= bclass.addTextField("version", "Version", 30);
    
    if(!"internal".equals(bclass.getCustomMapping())){
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }
    
    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  protected BaseClass getGroupCellClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;
    
    try {
      doc = xwiki.getDocument("Celements.GroupCellClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Celements");
      doc.setName("GroupCellClass");
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getxWikiClass();
    bclass.setName("Celements.GroupCellClass");
    needsUpdate |= bclass.addTextField("render_layout", "Render Layout", 30);
    
    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  protected BaseClass getPageDepCellConfigClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;

    try {
      doc = xwiki.getDocument("Celements.PageDepCellConfigClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Celements");
      doc.setName("PageDepCellConfigClass");
      needsUpdate = true;
    }

    BaseClass bclass = doc.getxWikiClass();
    bclass.setName("Celements.PageDepCellConfigClass");
    needsUpdate |= bclass.addTextField("space_name", "Space Name", 30);
    needsUpdate |= bclass.addBooleanField("is_inheritable", "is inheritable", "yesno");

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

}
