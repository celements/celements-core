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

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.AbstractClassCollection;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

@Component(DocumentDetailsClasses.NAME)
public class DocumentDetailsClasses extends AbstractClassCollection {

  public static final String NAME = "celements.documentDetails";

  public static final String UNPUBLISH_DATE_FIELD = "unpublishDate";

  public static final String PUBLISH_DATE_FIELD = "publishDate";

  public static final String FIELD_DOC_EXTRACT_CONTENT = "extract";
  public static final String FIELD_DOC_EXTRACT_LANGUAGE = "language";

  @Override
  protected void initClasses() throws XWikiException {
    getDocumentPublicationClass();
    getDocumentExtractClass();
  }

  public DocumentReference getDocumentPublicationClassRef(String wikiName) {
    return new DocumentReference(wikiName, "Classes", "DocumentPublication");
  }

  BaseClass getDocumentPublicationClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getDocumentPublicationClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("failed", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(classRef);
    needsUpdate |= bclass.addDateField(PUBLISH_DATE_FIELD, "Publish Date (dd.MM.yyyy HH:mm)",
        "dd.MM.yyyy HH:mm", 0);
    needsUpdate |= bclass.addDateField(UNPUBLISH_DATE_FIELD, "Unpublish Date (dd.MM.yyyy "
        + "HH:mm)", "dd.MM.yyyy HH:mm", 0);

    if (!"internal".equals(bclass.getCustomMapping())) {
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getDocumentExtractClassRef(String wikiName) {
    return new DocumentReference(wikiName, "Classes", "DocumentExtract");
  }

  BaseClass getDocumentExtractClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getDocumentExtractClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("failed", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setDocumentReference(classRef);
    needsUpdate |= bclass.addTextField(FIELD_DOC_EXTRACT_LANGUAGE, "Language", 30);
    needsUpdate |= bclass.addTextAreaField(FIELD_DOC_EXTRACT_CONTENT, "Extract", 80, 7);

    if (!"internal".equals(bclass.getCustomMapping())) {
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  @Override
  public String getConfigName() {
    return "documentDetails";
  }

}
