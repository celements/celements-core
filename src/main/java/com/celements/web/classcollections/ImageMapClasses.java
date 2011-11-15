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

import com.celements.common.classes.CelementsClassCollection;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

public class ImageMapClasses extends CelementsClassCollection {
  
  private static ImageMapClasses instance;
  
  private static Log mLogger = LogFactory.getFactory().getInstance(
      ImageMapClasses.class);

  private ImageMapClasses() { }
  
  public static ImageMapClasses getInstance() {
    if (instance == null) {
      instance = new ImageMapClasses();
    }
    return instance;
  }

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
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;
    
    try {
      doc = xwiki.getDocument("Classes.ImageMapConfigClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Classes");
      doc.setName("ImageMapConfigClass");
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getxWikiClass();
    bclass.setName("Classes.ImageMapConfigClass");
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
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;
    
    try {
      doc = xwiki.getDocument("Classes.ImageMapClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Classes");
      doc.setName("ImageMapClass");
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getxWikiClass();
    bclass.setName("Classes.ImageMapClass");
    needsUpdate |= bclass.addTextField("map_id", "Map Identifier", 30);
    
    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }

  public String getConfigName() {
    return "imageMap";
  }

}
