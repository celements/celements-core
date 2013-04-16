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

import org.xwiki.model.reference.DocumentReference;

import com.celements.emptycheck.service.IEmptyCheckRole;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

/**
 * @deprecated since 2.29.0 use instead IEmptyCheckRole component
 **/
@Deprecated
public class EmptyCheckCommand {

  /**
   * @deprecated since 2.29.0 use instead IEmptyCheckRole component
   **/
  @Deprecated
  public DocumentReference getNextNonEmptyChildren(DocumentReference documentRef) {
    return getEmptyCheckService().getNextNonEmptyChildren(documentRef);
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
  
  /**
   * @deprecated since 2.29.0 use instead IEmptyCheckRole component
   **/
  @Deprecated
  public boolean isEmptyRTEDocument(DocumentReference docRef) {
    return getEmptyCheckService().isEmptyRTEDocument(docRef);
  }
  
  /**
   * @deprecated since 2.29.0 use instead IEmptyCheckRole component
   **/
  @Deprecated
  public boolean isEmptyRTEDocumentTranslated(DocumentReference docRef) {
    return getEmptyCheckService().isEmptyRTEDocumentTranslated(docRef);
  }
  
  /**
   * @deprecated since 2.29.0 use instead IEmptyCheckRole component
   **/
  @Deprecated
  public boolean isEmptyRTEDocumentDefault(DocumentReference docRef,
      XWikiContext context) {
    return getEmptyCheckService().isEmptyRTEDocumentDefault(docRef);
  }
  
  /**
   * @deprecated since 2.29.0 use instead IEmptyCheckRole component
   **/
  @Deprecated
  public boolean isEmptyRTEDocument(XWikiDocument localdoc) {
    return getEmptyCheckService().isEmptyRTEDocument(localdoc);
  }

  /**
   * @deprecated since 2.29.0 use instead IEmptyCheckRole component
   **/
  @Deprecated
  public boolean isEmptyRTEString(String rteContent) {
    return getEmptyCheckService().isEmptyRTEString(rteContent);
  }

  IEmptyCheckRole getEmptyCheckService() {
    return Utils.getComponent(IEmptyCheckRole.class);
  }

}
