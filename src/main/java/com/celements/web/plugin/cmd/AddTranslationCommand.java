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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class AddTranslationCommand {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      AddTranslationCommand.class);

  /**
   * Adds a Translation document in given language if it does not already exist.
   * The document must already be created in the default language.
   * If the Translation already exists, the Translation flag is forced to 1 anyhow.
   * 
   * @param docRef
   * @param language
   * @return isNew for translation document
   */
  public boolean addTranslation(DocumentReference docRef, String language) {
    boolean successful = false;
    if (getContext().getWiki().exists(docRef, getContext())) {
      try {
        XWikiDocument mainDoc = getContext().getWiki().getDocument(docRef, getContext());
        if (!language.equals(mainDoc.getDefaultLanguage())) {
          XWikiDocument transDoc = createTranslationDoc(mainDoc, language);
          LOGGER.debug("Successfully added translation for [" + docRef
              + "] for language [" + language + "] and doc default language is ["
              + transDoc.getDefaultLanguage() + "] - translation? ["
              + transDoc.getTranslation() + "].");
          //TODO fix history entry.
          boolean transDocWasNew = transDoc.isNew();
          getContext().getWiki().saveDocument(transDoc, getContext());
          successful = transDocWasNew;
        } else {
          LOGGER.debug("failed to add translation in document default language ["
              + mainDoc.getDefaultLanguage() + "] for document [" + docRef + "].");
        }
      } catch (XWikiException exp) {
        LOGGER.debug("failed to add translation because cannot get main document ["
            + docRef + "].", exp);
      }
    } else {
      LOGGER.debug("failed to add translation because document [" + docRef
          + "] does not exist");
    }
    return successful;
  }

  /**
   * Adds a Translation document in given language if it does not already exist.
   * The document must already be created in the default language.
   * If the Translation already exists, the Translation flag is forced to 1 anyhow.
   * 
   * @param fullName
   * @param language
   * @param context
   * @return isNew for translation document
   * 
   * @deprecated since 2.18.0 instead use addTranslation(DocumentReference, String)
   */
  @Deprecated
  public boolean addTranslation(String fullName, String language, XWikiContext context) {
    DocumentReference docRef = getWebUtilsService().resolveDocumentReference(fullName);
    return addTranslation(docRef, language);
  }

  /**
   * Details of this methods are taken from XWiki's SaveAction
   * @param mainDoc
   * @param language
   * @return
   * @throws XWikiException
   */
  XWikiDocument createTranslationDoc(XWikiDocument mainDoc,
      String language) throws XWikiException {
    XWikiDocument transDoc = mainDoc.getTranslatedDocument(language, getContext());
    if ((transDoc == mainDoc) && getContext().getWiki().isMultiLingual(getContext())) {
      transDoc = new XWikiDocument(mainDoc.getDocumentReference());
      transDoc.setLanguage(language);
      transDoc.setDefaultLanguage(mainDoc.getDefaultLanguage());
      transDoc.setStore(mainDoc.getStore());
      transDoc.setContent(mainDoc.getContent());
      applyCreationDateFix(transDoc);
    } else if (transDoc != mainDoc) {
      LOGGER.debug("Translation document [" + mainDoc.getDocumentReference() + "] , "
          + language + "] already exists.");
    }
    transDoc.setTranslation(1);
    // Make sure we have at least the meta data dirty status
    transDoc.setMetaDataDirty(true);
    return transDoc;
  }

  void applyCreationDateFix(XWikiDocument doc) {
    //FIXME Should be done when xwiki saves a new document. Unfortunately it is done
    //      when you first get a document and than cached.
    if(doc.isNew()) {
      doc.setCreationDate(new Date());
      doc.setCreator(getContext().getUser());
    }
  }

  public XWikiDocument getTranslatedDoc(XWikiDocument mainDoc, String language
      ) throws XWikiException {
    XWikiDocument transDoc = mainDoc.getTranslatedDocument(getContext().getLanguage(),
        getContext());
    if ((transDoc == mainDoc) && !getContext().getLanguage().equals(
        mainDoc.getDefaultLanguage())) {
      LOGGER.info("creating new " + getContext().getLanguage() + " Translation for ["
          + mainDoc.getDocumentReference() + "] (defult [" + mainDoc.getDefaultLanguage()
          + "])");
      transDoc = createTranslationDoc(mainDoc, language);
    }
    return transDoc;
  }

  private IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

  private XWikiContext getContext() {
    return (XWikiContext)getExecution().getContext().getProperty("xwikicontext");
  }

  private Execution getExecution() {
    return Utils.getComponent(Execution.class);
  }

}
