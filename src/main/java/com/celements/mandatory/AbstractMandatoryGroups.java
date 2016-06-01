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
package com.celements.mandatory;

import java.util.Collections;
import java.util.List;

import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.web.plugin.cmd.CreateDocumentCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public abstract class AbstractMandatoryGroups implements IMandatoryDocumentRole {

  @Requirement
  Execution execution;

  public abstract void checkDocuments() throws XWikiException;

  public AbstractMandatoryGroups() {
    super();
  }

  protected XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  protected abstract String commitName();

  public List<String> dependsOnMandatoryDocuments() {
    return Collections.emptyList();
  }

  protected void checkGroup(DocumentReference groupRef) throws XWikiException {
    if (!getContext().getWiki().exists(groupRef, getContext())) {
      XWikiDocument editorGroupDoc = new CreateDocumentCommand().createDocument(groupRef,
          "UserGroup");
      if (editorGroupDoc != null) {
        editorGroupDoc.newXObject(getGroupClassRef(getContext().getDatabase()), getContext());
        getContext().getWiki().saveDocument(editorGroupDoc, "autocreate " + commitName()
            + " group.", getContext());
      }
    }
  }

  protected DocumentReference getGroupClassRef(String wikiName) {
    return new DocumentReference(wikiName, "XWiki", "XWikiGroups");
  }

}
