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
package com.celements.web.plugin.cmd;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.navigation.TreeNode;
import com.celements.navigation.service.ITreeNodeService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class EmptyCheckCommand {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      EmptyCheckCommand.class);

  private Set<DocumentReference> visitedDocRefs;

  ITreeNodeService treeNodeService;

  public DocumentReference getNextNonEmptyChildren(DocumentReference documentRef) {
    visitedDocRefs = new HashSet<DocumentReference>();
    return getNextNonEmptyChildren_internal(documentRef);
  }

  private DocumentReference getNextNonEmptyChildren_internal(
      DocumentReference documentRef) {
    if (isEmptyRTEDocument(documentRef)) {
      List<TreeNode> children = getTreeNodeService().getSubNodesForParent(
          getFullNameForRef(documentRef), getSpaceName(documentRef), "");
      if (children.size() > 0) {
        visitedDocRefs.add(documentRef);
        DocumentReference nextChild = children.get(0).getDocumentReference();
        if (!visitedDocRefs.contains(nextChild)) {
          return getNextNonEmptyChildren_internal(nextChild);
        } else {
          mLogger.warn("getNextNonEmptyChildren_internal: recursion detected on ["
              + nextChild + "].");
        }
      }
    }
    return documentRef;
  }

  private String getFullNameForRef(DocumentReference docRef) {
    return getSpaceName(docRef) + "." + docRef.getName();
  }

  private String getSpaceName(DocumentReference docRef) {
    return docRef.getLastSpaceReference().getName();
  }

  /**
   * @deprecated since 2.9.4 use instead isEmptyRTEDocument(DocumentReference)
   **/
  @Deprecated
  public boolean isEmptyRTEDocument(String fullname, XWikiContext context) {
    DocumentReference docRef = new DocumentReference(context.getDatabase(),
        fullname.split("\\.")[0], fullname.split("\\.")[1]);
    return isEmptyRTEDocumentDefault(docRef, context)
        && isEmptyRTEDocumentTranslated(docRef);
  }
  
  public boolean isEmptyRTEDocument(DocumentReference docRef) {
    return isEmptyRTEDocumentDefault(docRef, getContext())
        && isEmptyRTEDocumentTranslated(docRef);
  }
  
  public boolean isEmptyRTEDocumentTranslated(DocumentReference docRef) {
    try {
      return isEmptyRTEDocument(getContext().getWiki(
          ).getDocument(docRef, getContext()).getTranslatedDocument(getContext()));
    } catch (XWikiException e) {
      mLogger.error(e);
    }
    return true;
  }
  
  public boolean isEmptyRTEDocumentDefault(DocumentReference docRef,
      XWikiContext context) {
    try {
      return isEmptyRTEDocument(context.getWiki(
          ).getDocument(docRef, context));
    } catch (XWikiException e) {
      mLogger.error(e);
    }
    return true;
  }
  
  public boolean isEmptyRTEDocument(XWikiDocument localdoc) {
    return isEmptyRTEString(localdoc.getContent());
  }

  public boolean isEmptyRTEString(String rteContent) {
    return "".equals(rteContent.replaceAll(
        "(<p>)?(<span.*?>)?(\\s*(&nbsp;|<br\\s*/>))*\\s*(</span>)?(</p>)?", "").trim());
  }

  private XWikiContext getContext() {
    return (XWikiContext)Utils.getComponent(Execution.class).getContext().getProperty(
        "xwikicontext");
  }


  ITreeNodeService getTreeNodeService() {
    if (treeNodeService != null) {
      return treeNodeService;
    }
    return Utils.getComponent(ITreeNodeService.class);
  }
}
