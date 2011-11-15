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
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.cells.cmd.PageDependentDocumentReferenceCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;

@Component("cells")
public class CellsScriptService implements ScriptService {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      CellsScriptService.class);

  @Requirement
  Execution execution;

  private PageDependentDocumentReferenceCommand injected_pageDepDocRefCmd;

  public DocumentReference getPageDependentDocRef(DocumentReference cellDocRef) {
    return getPageDepDocRefCmd().getDocumentReference(getContext().getDoc(), cellDocRef,
        getContext());
  }

  public DocumentReference getPageDependentDocRef(DocumentReference currentPageRef,
      DocumentReference cellDocRef) {
    try {
      return getPageDepDocRefCmd().getDocumentReference(getContext().getWiki(
          ).getDocument(currentPageRef, getContext()), cellDocRef, getContext());
    } catch (XWikiException exp) {
      mLogger.error("Failed to get xwiki document for [" + currentPageRef + "].", exp);
    }
    return currentPageRef;
  }

  public Document getPageDependentTranslatedDocument(Document currentDoc,
      DocumentReference cellDocRef) {
    try {
      return getPageDepDocRefCmd().getTranslatedDocument(getContext().getWiki(
        ).getDocument(currentDoc.getDocumentReference(), getContext()), cellDocRef,
         getContext()).newDocument(getContext());
    } catch (XWikiException exp) {
      mLogger.error("Failed to get xwiki document for ["
          + currentDoc.getDocumentReference() + "].", exp);
    }
    return currentDoc;
  }

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }
  
  PageDependentDocumentReferenceCommand getPageDepDocRefCmd() {
    if (injected_pageDepDocRefCmd != null) {
      return injected_pageDepDocRefCmd;
    }
    return new PageDependentDocumentReferenceCommand();
  }

  void inject_pageDepDocRefCmd(PageDependentDocumentReferenceCommand mockPageDepDocRefCmd
      ) {
    injected_pageDepDocRefCmd = mockPageDepDocRefCmd;
  }

}
