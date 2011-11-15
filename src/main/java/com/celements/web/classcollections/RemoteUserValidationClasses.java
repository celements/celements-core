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

public class RemoteUserValidationClasses extends CelementsClassCollection {
  
  private static Log mLogger = LogFactory.getFactory().getInstance(
      RemoteUserValidationClasses.class);
  
  private static RemoteUserValidationClasses instance;
  
  public void initClasses(XWikiContext context) throws XWikiException {
    getRemoteUserValidationClass(context);
  }
  
  private RemoteUserValidationClasses() {
  }
  
  public static RemoteUserValidationClasses getInstance() {
    if (instance == null) {
      instance = new RemoteUserValidationClasses();
    }
    return instance;
  }
  
  protected BaseClass getRemoteUserValidationClass(XWikiContext context) throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = context.getWiki();
    boolean needsUpdate = false;
    
    try {
      doc = xwiki.getDocument("Classes.RemoteUserValidationClass", context);
    } catch (XWikiException e) {
      mLogger.error(e);
      doc = new XWikiDocument();
      doc.setSpace("Classes");
      doc.setName("RemoteUserValidationClass");
      needsUpdate = true;
    }
    
    BaseClass bclass = doc.getxWikiClass();
    bclass.setName("Classes.RemoteUserValidationClass");
    needsUpdate |= bclass.addTextField("host", "Allow from host", 30);
    needsUpdate |= bclass.addTextField("secret", "Secret for host", 30);
    
    setContentAndSaveClassDocument(doc, needsUpdate, context);
    return bclass;
  }
  
  public String getConfigName() {
    return "remoteValidator";
  }
  
  @Override
  protected Log getLogger() {
    return mLogger;
  }
}
