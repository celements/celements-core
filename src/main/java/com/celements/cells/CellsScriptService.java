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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.script.service.ScriptService;

import com.celements.cells.cmd.PageDependentDocumentReferenceCommand;
import com.celements.web.plugin.cmd.PageLayoutCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("cells")
public class CellsScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CellsScriptService.class);

  @Requirement
  Execution execution;

  private PageDependentDocumentReferenceCommand injected_pageDepDocRefCmd;

  public DocumentReference getPageDependentDocRef(DocumentReference cellDocRef) {
    return getPageDepDocRefCmd().getDocumentReference(getContext().getDoc().getDocumentReference(),
        cellDocRef);
  }

  public DocumentReference getPageDependentDocRef(DocumentReference currentPageRef,
      DocumentReference cellDocRef) {
    return getPageDepDocRefCmd().getDocumentReference(currentPageRef, cellDocRef);
  }

  public DocumentReference getPageDependentDocRef(DocumentReference currentPageRef,
      DocumentReference cellDocRef, boolean isInheritable) {
    return getPageDepDocRefCmd().getDocumentReference(currentPageRef, cellDocRef, isInheritable);
  }

  public Document getPageDependentTranslatedDocument(Document currentDoc,
      DocumentReference cellDocRef) {
    try {
      return getPageDepDocRefCmd().getTranslatedDocument(getCurrentXWikiDoc(currentDoc),
          cellDocRef).newDocument(getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get xwiki document for [" + currentDoc.getDocumentReference() + "].",
          exp);
    }
    return currentDoc;
  }

  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch and
   * write the exception in a log-file
   */
  private XWikiDocument getCurrentXWikiDoc(Document currentDoc) throws XWikiException {
    return getCurrentXWikiDocDef(currentDoc).getTranslatedDocument(currentDoc.getLanguage(),
        getContext());
  }

  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch and
   * write the exception in a log-file
   */
  private XWikiDocument getCurrentXWikiDocDef(Document currentDoc) throws XWikiException {
    return getContext().getWiki().getDocument(currentDoc.getDocumentReference(), getContext());
  }

  public String getDepCellSpaceSuffix(DocumentReference cellDocRef) {
    try {
      return getPageDepDocRefCmd().getDepCellSpace(cellDocRef);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get depCellSpaceSuffix for cellDocRef [" + cellDocRef + "].", exp);
    }
    return "";
  }

  public boolean isInheritable(DocumentReference cellDocRef) {
    return getPageDepDocRefCmd().isInheritable(cellDocRef);
  }

  public DocumentReference getLayoutDefaultDocRef(SpaceReference currLayoutRef,
      String depCellSpace) {
    return getPageDepDocRefCmd().getLayoutDefaultDocRef(currLayoutRef, depCellSpace);
  }

  public DocumentReference getLayoutDefaultDocRef(SpaceReference currLayoutRef,
      DocumentReference cellDocRef) {
    return getPageDepDocRefCmd().getLayoutDefaultDocRef(currLayoutRef, cellDocRef);
  }

  public DocumentReference getWikiDefaultDocRef(DocumentReference docRef,
      DocumentReference cellDocRef) {
    return getPageDepDocRefCmd().getWikiDefaultDocRef(docRef, cellDocRef);
  }

  public DocumentReference getWikiDefaultDocRef(String depCellSpaceSuffix) {
    return getPageDepDocRefCmd().getWikiDefaultDocRef(depCellSpaceSuffix);
  }

  public DocumentReference getSpaceDefaultDocRef(DocumentReference docRef,
      DocumentReference cellDocRef) {
    return getPageDepDocRefCmd().getSpaceDefaultDocRef(docRef, cellDocRef);
  }

  public DocumentReference getSpaceDefaultDocRef(SpaceReference depDocumentSpaceRef) {
    return getPageDepDocRefCmd().getSpaceDefaultDocRef(depDocumentSpaceRef);
  }

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  PageDependentDocumentReferenceCommand getPageDepDocRefCmd() {
    if (injected_pageDepDocRefCmd != null) {
      return injected_pageDepDocRefCmd;
    }
    PageDependentDocumentReferenceCommand pageDepDocRefCmd = new PageDependentDocumentReferenceCommand();
    pageDepDocRefCmd.setCurrentLayoutRef(new PageLayoutCommand().getCurrentRenderingLayout());
    return pageDepDocRefCmd;
  }

  void inject_pageDepDocRefCmd(PageDependentDocumentReferenceCommand mockPageDepDocRefCmd) {
    injected_pageDepDocRefCmd = mockPageDepDocRefCmd;
  }

}
