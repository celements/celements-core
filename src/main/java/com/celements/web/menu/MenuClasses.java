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
package com.celements.web.menu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.celements.common.classes.CelementsClassCollection;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

public class MenuClasses extends CelementsClassCollection {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      MenuClasses.class);
  private static MenuClasses instance;

  private MenuClasses() {
  }

  public static MenuClasses getInstance() {
    if (instance == null) {
      instance = new MenuClasses();
    }
    return instance;
  }

  @Override
  protected void initClasses(XWikiContext context) throws XWikiException {
    getMenuBarHeaderItemClass(context);
    getMenuBarSubItemClass(context);
  }

  @Override
  protected Log getLogger() {
    return mLogger;
  }

  public String getConfigName() {
    return "celMenuClasses";
  }

  protected BaseClass getMenuBarHeaderItemClass(XWikiContext context
      ) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;
    
    try {
      doc = xwiki.getDocument("Celements.MenuBarHeaderItemClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Celements");
      doc.setName("MenuBarHeaderItemClass");
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getxWikiClass();
    bclass.setName("Celements.MenuBarHeaderItemClass");
    needsUpdate |= bclass.addTextField("name", "Name (dictionary possible)", 30);
    needsUpdate |= bclass.addNumberField("header_id", "Header Id", 10, "integer");
    needsUpdate |= bclass.addNumberField("pos", "Position", 10, "integer");
    
    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  protected BaseClass getMenuBarSubItemClass(XWikiContext context
      ) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;
    
    try {
      doc = xwiki.getDocument("Celements.MenuBarSubItemClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Celements");
      doc.setName("MenuBarSubItemClass");
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getxWikiClass();
    bclass.setName("Celements.MenuBarSubItemClass");
    needsUpdate |= bclass.addTextField("name", "Name (dictionary possible)", 30);
    needsUpdate |= bclass.addNumberField("header_id", "Header Id", 10, "integer");
    needsUpdate |= bclass.addNumberField("itempos", "Position", 10, "integer");
    needsUpdate |= bclass.addTextField("link", "Link (velocity possible)", 30);
    needsUpdate |= bclass.addTextField("css_classes", "CSS Classes", 30);
    
    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

}
