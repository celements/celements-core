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
package com.celements.parents;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component(XDocParents.DOC_PROVIDER_NAME)
public class XDocParents implements IDocParentProviderRole {

  private static Logger _LOGGER = LoggerFactory.getLogger(XDocParents.class);

  public static final String DOC_PROVIDER_NAME = "xwiki";

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  @Override
  public List<DocumentReference> getDocumentParentsList(DocumentReference docRef) {
    ArrayList<DocumentReference> docParents = new ArrayList<>();
    try {
      DocumentReference nextParent = getParentRef(docRef);
      while ((nextParent != null) && getContext().getWiki().exists(nextParent, getContext())
          && !docParents.contains(nextParent)) {
        docParents.add(nextParent);
        nextParent = getParentRef(nextParent);
      }
    } catch (XWikiException exp) {
      _LOGGER.error("Failed to get parent reference. ", exp);
    }
    return docParents;
  }

  private DocumentReference getParentRef(DocumentReference docRef) throws XWikiException {
    return getContext().getWiki().getDocument(docRef, getContext()).getParentReference();
  }

}
