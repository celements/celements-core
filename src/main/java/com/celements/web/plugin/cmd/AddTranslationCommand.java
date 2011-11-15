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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

public class AddTranslationCommand {

  private static Log mLogger = LogFactory.getFactory().getInstance(
      AddTranslationCommand.class);

  /**
   * Adds a Translation document in given language if it does not already exist.
   * The document must already be created in the default language.
   * If the Translation already exists, the Translation flag is forced to 1 anyhow.
   * 
   * @param fullName
   * @param language
   * @param context
   * @return isNew for translation document
   */
  public boolean addTranslation(String fullName, String language, XWikiContext context) {
    boolean successful = false;
    if (context.getWiki().exists(fullName, context)) {
      try {
        XWikiDocument mainDoc = context.getWiki().getDocument(fullName, context);
        if (!language.equals(mainDoc.getDefaultLanguage())) {
          XWikiDocument transDoc = createTranslationDoc(mainDoc, language,
              context);
          mLogger.debug("Successfully added translation for [" + fullName
              + "] for language [" + language + "] and doc default language is ["
              + transDoc.getDefaultLanguage() + "] - translation? ["
              + transDoc.getTranslation() + "].");
          //TODO fix history entry.
          boolean transDocWasNew = transDoc.isNew();
          context.getWiki().saveDocument(transDoc, context);
          successful = transDocWasNew;
        } else {
          mLogger.debug("failed to add translation in document default language ["
              + mainDoc.getDefaultLanguage() + "] for document [" + fullName + "].");
        }
      } catch (XWikiException exp) {
        mLogger.debug("failed to add translation because cannot get main document ["
            + fullName + "].", exp);
      }
    } else {
      mLogger.debug("failed to add translation because document [" + fullName
          + "] does not exist");
    }
    return successful;
  }

  /**
   * Details of this methods are taken from XWiki's SaveAction
   * @param mainDoc
   * @param language
   * @param context
   * @return
   * @throws XWikiException
   */
  XWikiDocument createTranslationDoc(XWikiDocument mainDoc,
      String language, XWikiContext context) throws XWikiException {
    XWikiDocument transDoc = mainDoc.getTranslatedDocument(language, context);
    if ((transDoc == mainDoc) && context.getWiki().isMultiLingual(context)) {
      transDoc = new XWikiDocument(mainDoc.getSpace(), mainDoc.getName());
      transDoc.setLanguage(language);
      transDoc.setDefaultLanguage(mainDoc.getDefaultLanguage());
      transDoc.setStore(mainDoc.getStore());
      transDoc.setContent(mainDoc.getContent());
    } else if (transDoc != mainDoc) {
      mLogger.debug("Translation document [" + mainDoc.getFullName() + ", " + language
          + "] already exists.");
    }
    transDoc.setTranslation(1);
    // Make sure we have at least the meta data dirty status
    transDoc.setMetaDataDirty(true);
    return transDoc;
  }

}
