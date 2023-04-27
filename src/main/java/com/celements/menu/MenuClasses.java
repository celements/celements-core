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
package com.celements.menu;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.CelementsClassCollection;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

@Component("celements.celMenuClasses")
public class MenuClasses extends CelementsClassCollection {

  public MenuClasses() {}

  @Override
  protected void initClasses(XWikiContext context) throws XWikiException {
    getMenuBarHeaderItemClass(context);
    getMenuBarSubItemClass(context);
  }

  @Override
  public String getConfigName() {
    return "celMenuClasses";
  }

  protected BaseClass getMenuBarHeaderItemClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = new DocumentReference(context.getDatabase(), "Celements",
        "MenuBarHeaderItemClass");

    try {
      doc = context.getWiki().getDocument(classRef, context);
    } catch (XWikiException e) {
      LOGGER.error("failed", e);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addTextField("name", "Name (dictionary possible)", 30);
    needsUpdate |= bclass.addNumberField("header_id", "Header Id", 10, "integer");
    needsUpdate |= bclass.addNumberField("pos", "Position", 10, "integer");

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  protected BaseClass getMenuBarSubItemClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;
    DocumentReference classRef = new DocumentReference(context.getDatabase(), "Celements",
        "MenuBarSubItemClass");

    try {
      doc = xwiki.getDocument(classRef, context);
    } catch (XWikiException e) {
      LOGGER.error("failed", e);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addTextField("name", "Name (dictionary possible)", 30);
    needsUpdate |= bclass.addNumberField("header_id", "Header Id", 10, "integer");
    needsUpdate |= bclass.addNumberField("itempos", "Position", 10, "integer");
    needsUpdate |= bclass.addTextField("link", "Link (velocity possible)", 30);
    needsUpdate |= bclass.addTextField("css_classes", "CSS Classes", 30);

    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

}
