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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;

import com.celements.pagetype.IPageTypeClassConfig;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("celements.mandatory.wikipreferences")
public class XWikiXWikiPreferences extends AbstractMandatoryDocument {

  private static final Logger LOGGER = LoggerFactory.getLogger(XWikiXWikiPreferences.class);

  @Requirement
  private IPageTypeClassConfig pageTypeClassConfig;

  @Override
  public List<String> dependsOnMandatoryDocuments() {
    return Collections.emptyList();
  }

  @Override
  public String getName() {
    return "CelementsXWikiPreferences";
  }

  @Override
  protected DocumentReference getDocRef() {
    return new DocumentReference(getWiki(), "XWiki", "XWikiPreferences");
  }

  @Override
  protected boolean skip() {
    return getContext().getWiki().ParamAsLong("celements.mandatory.skipWikiPreferences", 0) == 1L;
  }

  @Override
  protected boolean checkDocuments(XWikiDocument doc) throws XWikiException {
    boolean dirty = checkPageType(doc);
    dirty |= checkWikiPreferences(doc);
    return dirty;
  }

  @Override
  protected boolean checkDocumentsMain(XWikiDocument doc) throws XWikiException {
    boolean dirty = checkPageType(doc);
    dirty |= checkWikiPreferencesForMainWiki(doc);
    return dirty;
  }

  private boolean checkWikiPreferences(XWikiDocument wikiPrefDoc) throws XWikiException {
    boolean dirty = false;
    BaseObject prefsObj = wikiPrefDoc.getXObject(getDocRef(), false, getContext());
    if (prefsObj == null) {
      prefsObj = wikiPrefDoc.newXObject(getDocRef(), getContext());
      prefsObj.set("editor", "Text", getContext());
      prefsObj.set("renderXWikiRadeoxRenderer", 0, getContext());
      prefsObj.set("pageWidth", "default", getContext());
      LOGGER.debug("XWikiPreferences missing wiki preferences object added for" + " database ["
          + getWiki() + "].");
      dirty = true;
    }
    if (prefsObj.getIntValue("multilingual", -1) < 0) {
      prefsObj.setIntValue("multilingual", 1);
      LOGGER.debug("XWikiPreferences missing multilingual configuration added for" + " database ["
          + getWiki() + "].");
      dirty = true;
    }
    if (prefsObj.getIntValue("authenticate_edit", -1) < 0) {
      prefsObj.set("authenticate_edit", 1, getContext());
      LOGGER.debug("XWikiPreferences missing authenticate_edit configuration added for"
          + " database [" + getWiki() + "].");
      dirty = true;
    }
    if (prefsObj.getIntValue("authenticate_view", -1) < 0) {
      prefsObj.set("authenticate_view", 0, getContext());
      LOGGER.debug("XWikiPreferences missing authenticate_view configuration added for"
          + " database [" + getWiki() + "].");
      dirty = true;
    }
    if (prefsObj.getLongValue("upload_maxsize") <= 0) {
      prefsObj.set("upload_maxsize", 104857600L, getContext());
      LOGGER.debug("XWikiPreferences missing upload_maxsize configuration added for" + " database ["
          + getWiki() + "].");
      dirty = true;
    }
    String documentBundles = prefsObj.getStringValue("documentBundles");
    if (StringUtils.isEmpty(documentBundles) || !documentBundles.contains(
        "celements2web:Celements2.Dictionary")) {
      if (StringUtils.isEmpty(documentBundles)) {
        documentBundles = "celements2web:Celements2.Dictionary";
      } else {
        documentBundles = documentBundles + ",celements2web:Celements2.Dictionary";
      }
      prefsObj.setStringValue("documentBundles", documentBundles);
      LOGGER.debug("XWikiPreferences added missing Celements2.Dictionary for" + " database ["
          + getWiki() + "].");
      dirty = true;
    }
    String centralfilebaseConfig = prefsObj.getStringValue("cel_centralfilebase");
    if (StringUtils.isEmpty(centralfilebaseConfig)) {
      prefsObj.set("cel_centralfilebase", "Content_attachments.FileBaseDoc", getContext());
      LOGGER.debug("XWikiPreferences missing cel_centralfilebase configuration added for"
          + " database [" + getWiki() + "].");
      dirty = true;
    }
    return dirty;
  }

  private boolean checkWikiPreferencesForMainWiki(XWikiDocument wikiPrefDoc) throws XWikiException {
    boolean dirty = false;
    BaseObject prefsObj = wikiPrefDoc.getXObject(getDocRef(), false, getContext());
    if (prefsObj == null) {
      prefsObj = wikiPrefDoc.newXObject(getDocRef(), getContext());
      prefsObj.set("editor", "Text", getContext());
      prefsObj.set("renderXWikiRadeoxRenderer", 1, getContext());
      prefsObj.set("pageWidth", "default", getContext());
      LOGGER.debug("XWikiPreferences missing wiki preferences object added for" + " database ["
          + getWiki() + "].");
      dirty = true;
    }
    if (prefsObj.getIntValue("multilingual", -1) < 0) {
      prefsObj.setIntValue("multilingual", 1);
      LOGGER.debug("XWikiPreferences missing multilingual configuration added for" + " database ["
          + getWiki() + "].");
      dirty = true;
    }
    if (prefsObj.getIntValue("authenticate_edit", -1) < 0) {
      prefsObj.set("authenticate_edit", 1, getContext());
      LOGGER.debug("XWikiPreferences missing authenticate_edit configuration added for"
          + " database [" + getWiki() + "].");
      dirty = true;
    }
    if (prefsObj.getIntValue("authenticate_view", -1) < 0) {
      prefsObj.set("authenticate_view", 1, getContext());
      LOGGER.debug("XWikiPreferences missing authenticate_view configuration added for"
          + " database [" + getWiki() + "].");
      dirty = true;
    }
    if (prefsObj.getLongValue("upload_maxsize") <= 0) {
      prefsObj.set("upload_maxsize", 104857600L, getContext());
      LOGGER.debug("XWikiPreferences missing upload_maxsize configuration added for" + " database ["
          + getWiki() + "].");
      dirty = true;
    }
    return dirty;
  }

  private boolean checkPageType(XWikiDocument wikiPrefDoc) throws XWikiException {
    boolean dirty = false;
    DocumentReference pageTypeClassRef = pageTypeClassConfig.getPageTypeClassRef(
        webUtilsService.getWikiRef());
    BaseObject pageTypeObj = wikiPrefDoc.getXObject(pageTypeClassRef, false, getContext());
    if (pageTypeObj == null) {
      pageTypeObj = wikiPrefDoc.newXObject(pageTypeClassRef, getContext());
      pageTypeObj.setStringValue("page_type", "WikiPreference");
      LOGGER.debug("XWikiPreferences missing page type object fixed for database [" + getWiki()
          + "].");
      dirty = true;
    }
    return dirty;
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

}
