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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.pagetype.PageTypeClasses;
import com.celements.web.plugin.cmd.CreateDocumentCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("celements.mandatory.htmlwebpreferences")
public class HTMLWebPreferences implements IMandatoryDocumentRole {

  private static final String _SPACE_PREFERENCE_PAGE_TYPE = "SpacePreference";

  private static Log LOGGER = LogFactory.getFactory().getInstance(HTMLWebPreferences.class);

  @Requirement("celements.celPageTypeClasses")
  IClassCollectionRole pageTypeClasses;

  @Requirement
  Execution execution;

  protected XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  private PageTypeClasses getPageTypeClasses() {
    return (PageTypeClasses) pageTypeClasses;
  }

  @Override
  public List<String> dependsOnMandatoryDocuments() {
    return Collections.emptyList();
  }

  @Override
  public void checkDocuments() throws XWikiException {
    LOGGER.trace("Start checkDocuments in HTMLwebPreferences for database ["
        + getContext().getDatabase() + "].");
    if (!isSkipCelementsHTMLwebPreferences()) {
      LOGGER.trace("before checkHTMLwebPreferences for database [" + getContext().getDatabase()
          + "].");
      checkHTMLwebPreferences();
    } else {
      LOGGER.info("skip mandatory checkHTMLwebPreferences for database ["
          + getContext().getDatabase() + "], skipCelementsParam ["
          + isSkipCelementsHTMLwebPreferences() + "].");
    }
    LOGGER.trace("end checkDocuments in HTMLwebPreferences for database ["
        + getContext().getDatabase() + "].");
  }

  boolean isSkipCelementsHTMLwebPreferences() {
    boolean isSkip = getContext().getWiki().ParamAsLong(
        "celements.mandatory.skipHTMLwebPreferences", 0) == 1L;
    LOGGER.trace("skipCelementsHTMLwebPreferences for database [" + getContext().getDatabase()
        + "] returning [" + isSkip + "].");
    return isSkip;
  }

  void checkHTMLwebPreferences() throws XWikiException {
    DocumentReference htmlWebPreferencesRef = getHTMLwebPreferencesRef(getContext().getDatabase());
    XWikiDocument wikiPrefDoc;
    if (!getContext().getWiki().exists(htmlWebPreferencesRef, getContext())) {
      LOGGER.debug("HTMLwebPreferencesDocument is missing that we create it. ["
          + getContext().getDatabase() + "]");
      wikiPrefDoc = new CreateDocumentCommand().createDocument(htmlWebPreferencesRef,
          _SPACE_PREFERENCE_PAGE_TYPE);
    } else {
      wikiPrefDoc = getContext().getWiki().getDocument(htmlWebPreferencesRef, getContext());
      LOGGER.trace("HTMLwebPreferencesDocument already exists. [" + getContext().getDatabase()
          + "]");
    }
    if (wikiPrefDoc != null) {
      boolean dirty = checkPageType(wikiPrefDoc);
      dirty |= checkHTMLwebPreferences(wikiPrefDoc);
      if (dirty) {
        LOGGER.info("HTMLwebPreferencesDocument updated for [" + getContext().getDatabase() + "].");
        getContext().getWiki().saveDocument(wikiPrefDoc, "autocreate" + " HTML.WebPreferences.",
            getContext());
      } else {
        LOGGER.debug("HTMLwebPreferencesDocument not saved. Everything uptodate. ["
            + getContext().getDatabase() + "].");
      }
    } else {
      LOGGER.trace("skip checkHTMLwebPreferences because wikiPrefDoc is null! ["
          + getContext().getDatabase() + "]");
    }
  }

  boolean checkHTMLwebPreferences(XWikiDocument wikiPrefDoc) throws XWikiException {
    String wikiName = getContext().getDatabase();
    BaseObject prefsObj = wikiPrefDoc.getXObject(getXWikiPreferencesRef(wikiName), false,
        getContext());
    if (prefsObj == null) {
      prefsObj = wikiPrefDoc.newXObject(getXWikiPreferencesRef(wikiName), getContext());
      prefsObj.set("skin", "htmlskin", getContext());
      LOGGER.debug("XWikiPreferences missing fields in wiki preferences object fixed for"
          + " database [" + getContext().getDatabase() + "].");
      return true;
    }
    return false;
  }

  boolean checkPageType(XWikiDocument wikiPrefDoc) throws XWikiException {
    DocumentReference pageTypeClassRef = getPageTypeClasses().getPageTypeClassRef(
        getContext().getDatabase());
    BaseObject pageTypeObj = wikiPrefDoc.getXObject(pageTypeClassRef, false, getContext());
    if (pageTypeObj == null) {
      pageTypeObj = wikiPrefDoc.newXObject(pageTypeClassRef, getContext());
      pageTypeObj.setStringValue("page_type", _SPACE_PREFERENCE_PAGE_TYPE);
      LOGGER.debug("HTML.WebPreferences missing page type object fixed for database ["
          + getContext().getDatabase() + "].");
      return true;
    }
    return false;
  }

  private DocumentReference getXWikiPreferencesRef(String wikiName) {
    return new DocumentReference(wikiName, "XWiki", "XWikiPreferences");
  }

  private DocumentReference getHTMLwebPreferencesRef(String wikiName) {
    return new DocumentReference(wikiName, "HTML", "WebPreferences");
  }

}
