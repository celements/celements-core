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
package com.celements.common.classes;

import org.apache.commons.logging.Log;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Extend CelementsClassCollection and make the implementor a named component.
 * Celements then will call your initClasses method on system start once or if it
 * is explicitly asked for.
 * 
 * @author fabian pichler
 * 
 * since 2.11.0
 */
public abstract class CelementsClassCollection
    implements ICelementsClassCollection {

  final public void runUpdate(XWikiContext context) throws XWikiException {
    if (isActivated(context)) {
      getLogger().debug("calling initClasses for database: " + context.getDatabase());
      initClasses(context);
    }
  }

  private boolean isActivated(XWikiContext context) {
    return ("," + context.getWiki().getXWikiPreference("activated_classcollections",
        context) + "," + context.getWiki().Param("celements.classcollections", "") + ","
        ).contains("," + getConfigName() + ",");
  }

  protected void setContentAndSaveClassDocument(XWikiDocument doc,
      boolean needsUpdate, XWikiContext context) throws XWikiException {
    String content = doc.getContent();
    if ((content == null) || (content.equals(""))) {
      needsUpdate = true;
      doc.setContent(" ");
    }
    
    if (needsUpdate){
      context.getWiki().saveDocument(doc, context);
    }
  }

  abstract protected void initClasses(XWikiContext context) throws XWikiException;

  abstract protected Log getLogger();
}
