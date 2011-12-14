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
package com.celements.web.classcollections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.CelementsClassCollection;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

@Component("celements.imageMap.classes")
public class ImageMapClasses extends CelementsClassCollection {
  
  private static Log mLogger = LogFactory.getFactory().getInstance(
      ImageMapClasses.class);

  public ImageMapClasses() { }
  
  @Override
  protected Log getLogger() {
    return mLogger;
  }

  @Override
  protected void initClasses(XWikiContext context) throws XWikiException {
    getImageMapConfigClass(context);
    getImageMapClass(context);
  }
  
  protected BaseClass getImageMapConfigClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = new DocumentReference(context.getDatabase(), "Classes",
      "ImageMapConfigClass");

    try {
      doc = context.getWiki().getDocument(classRef, context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(classRef);
    needsUpdate |= bclass.addTextField("map_id", "Map Identifier", 30);
    needsUpdate |= bclass.addTextField("lang", "Language", 30);
    needsUpdate |= bclass.addTextAreaField("map", "Map Code", 80, 15);
    
    if(!"internal".equals(bclass.getCustomMapping())){
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }
    
    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }
  
  protected BaseClass getImageMapClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = new DocumentReference(context.getDatabase(), "Classes",
      "ImageMapClass");
    
    try {
      doc = context.getWiki().getDocument(classRef, context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(classRef);
    needsUpdate |= bclass.addTextField("map_id", "Map Identifier", 30);
    
    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  public String getConfigName() {
    return "imageMap";
  }

}
