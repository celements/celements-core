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
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

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
 * since 2.18.0
 */
public abstract class AbstractClassCollection implements IClassCollectionRole {

  @Requirement
  protected Execution execution;

  protected XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  final public void runUpdate() throws XWikiException {
    if (isActivated()) {
      getLogger().debug("calling initClasses for database: " + getContext().getDatabase()
          );
      initClasses();
    }
  }

  public boolean isActivated() {
    return ("," + getContext().getWiki().getXWikiPreference("activated_classcollections",
        getContext()) + "," + getContext().getWiki().Param("celements.classcollections",
            "") + ",").contains("," + getConfigName() + ",");
  }

  protected void setContentAndSaveClassDocument(XWikiDocument doc,
      boolean needsUpdate) throws XWikiException {
    String content = doc.getContent();
    if ((content == null) || (content.equals(""))) {
      needsUpdate = true;
      doc.setContent(" ");
    }
    
    if (needsUpdate){
      getContext().getWiki().saveDocument(doc, getContext());
    }
  }

  abstract protected void initClasses() throws XWikiException;

  abstract protected Log getLogger();
}
