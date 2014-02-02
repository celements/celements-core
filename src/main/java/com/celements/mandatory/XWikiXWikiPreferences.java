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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.pagetype.PageTypeClasses;
import com.celements.web.plugin.cmd.CreateDocumentCommand;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component("celements.mandatory.wikipreferences")
public class XWikiXWikiPreferences implements IMandatoryDocumentRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      XWikiXWikiPreferences.class);

  @Requirement
  IWebUtilsService webUtils;

  @Requirement("celements.celPageTypeClasses")
  IClassCollectionRole pageTypeClasses;

  @Requirement
  Execution execution;

  protected XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  private PageTypeClasses getPageTypeClasses() {
    return (PageTypeClasses) pageTypeClasses;
  }

  public List<String> dependsOnMandatoryDocuments() {
    return Arrays.asList("celements.MandatoryGroups");
  }


  public void checkDocuments() throws XWikiException {
    LOGGER.debug("starting mandatory checkXWikiPreferences for database ["
        + getContext().getDatabase() + "], noMainWiki [" + noMainWiki()
        + "], skipCelementsParam [" + isSkipCelementsWikiPreferences() + "].");
    if (!isSkipCelementsWikiPreferences()) {
      if (noMainWiki()) {
        checkXWikiPreferences();
      } else {
        checkXWikiPreferencesMainWiki();
      }
    } else {
      LOGGER.info("skip mandatory checkXWikiPreferences for database ["
          + getContext().getDatabase() + "], noMainWiki [" + noMainWiki()
          + "], skipCelementsParam [" + isSkipCelementsWikiPreferences() + "].");
    }
    LOGGER.trace("end checkDocuments in XWikiXWikiPreferences for database ["
        + getContext().getDatabase() + "].");
  }

  boolean isSkipCelementsWikiPreferences() {
    boolean isSkip = getContext().getWiki().ParamAsLong(
        "celements.mandatory.skipWikiPreferences", 0) == 1L;
    LOGGER.trace("skipCelementsWikiPreferences for database ["
        + getContext().getDatabase() + "] returning [" + isSkip + "].");
    return isSkip;
  }

  boolean noMainWiki() {
    String wikiName = getContext().getDatabase();
    LOGGER.trace("noMainWiki for database [" + wikiName + "].");
    return (wikiName != null) && !wikiName.equals(getContext().getMainXWiki());
  }

  void checkXWikiPreferences() throws XWikiException {
    DocumentReference xWikiPreferencesRef = getXWikiPreferencesRef(
        getContext().getDatabase());
    XWikiDocument wikiPrefDoc = getXWikiPreferencesDocument(xWikiPreferencesRef);
    if (wikiPrefDoc != null) {
      boolean dirty = checkPageType(wikiPrefDoc);
      dirty |= checkAccessRights(wikiPrefDoc);
      dirty |= checkWikiPreferences(wikiPrefDoc);
      if (dirty) {
        LOGGER.info("XWikiPreferencesDocument updated for [" + getContext().getDatabase()
            + "].");
        getContext().getWiki().saveDocument(wikiPrefDoc, "autocreate"
            + " XWiki.XWikiPreferences.", getContext());
      } else {
        LOGGER.debug("XWikiPreferencesDocument not saved. Everything uptodate. ["
            + getContext().getDatabase() + "].");
      }
    } else {
      LOGGER.trace("skip checkXWikiPreferences because wikiPrefDoc is null! ["
          + getContext().getDatabase() + "]");
    }
  }

  void checkXWikiPreferencesMainWiki() throws XWikiException {
    DocumentReference xWikiPreferencesRef = getXWikiPreferencesRef(
        getContext().getDatabase());
    XWikiDocument wikiPrefDoc = getXWikiPreferencesDocument(xWikiPreferencesRef);
    if (wikiPrefDoc != null) {
      boolean dirty = checkPageType(wikiPrefDoc);
      dirty |= checkAccessRights(wikiPrefDoc);
      dirty |= checkWikiPreferencesForMainWiki(wikiPrefDoc);
      if (dirty) {
        LOGGER.info("XWikiPreferencesDocument updated for [" + getContext().getDatabase()
            + "].");
        getContext().getWiki().saveDocument(wikiPrefDoc, "autocreate"
            + " XWiki.XWikiPreferences.", getContext());
      } else {
        LOGGER.debug("XWikiPreferencesDocument not saved. Everything uptodate. ["
            + getContext().getDatabase() + "].");
      }
    } else {
      LOGGER.trace("skip checkXWikiPreferences because wikiPrefDoc is null! ["
          + getContext().getDatabase() + "]");
    }
  }

  private XWikiDocument getXWikiPreferencesDocument(DocumentReference xWikiPreferencesRef
      ) throws XWikiException {
    XWikiDocument wikiPrefDoc;
    if (!getContext().getWiki().exists(xWikiPreferencesRef, getContext())) {
      LOGGER.debug("XWikiPreferencesDocument is missing that we create it. ["
          + getContext().getDatabase() + "]");
      wikiPrefDoc = new CreateDocumentCommand().createDocument(xWikiPreferencesRef,
          "WikiPreference");
    } else {
      wikiPrefDoc = getContext().getWiki().getDocument(xWikiPreferencesRef, getContext());
      LOGGER.trace("XWikiPreferencesDocument already exists. ["
          + getContext().getDatabase() + "]");
    }
    return wikiPrefDoc;
  }

  boolean checkWikiPreferences(XWikiDocument wikiPrefDoc) throws XWikiException {
    String wikiName = getContext().getDatabase();
    boolean dirty = false;
    BaseObject prefsObj = wikiPrefDoc.getXObject(getXWikiPreferencesRef(wikiName),
        false, getContext());
    if (prefsObj == null) {
      prefsObj = wikiPrefDoc.newXObject(getXWikiPreferencesRef(wikiName), getContext());
      prefsObj.set("editor", "Text", getContext());
      prefsObj.set("renderXWikiRadeoxRenderer", 0, getContext());
      prefsObj.set("pageWidth", "default", getContext());
      LOGGER.debug("XWikiPreferences missing wiki preferences object added for"
          + " database [" + getContext().getDatabase() + "].");
      dirty = true;
    }
    if (prefsObj.getIntValue("multilingual", -1) < 0) {
      prefsObj.setIntValue("multilingual", 1);
      LOGGER.debug("XWikiPreferences missing multilingual configuration added for"
          + " database [" + getContext().getDatabase() + "].");
      dirty = true;
    }
    if (prefsObj.getIntValue("authenticate_edit", -1) < 0) {
      prefsObj.set("authenticate_edit", 1, getContext());
      LOGGER.debug("XWikiPreferences missing authenticate_edit configuration added for"
          + " database [" + getContext().getDatabase() + "].");
      dirty = true;
    }
    if (prefsObj.getIntValue("authenticate_view", -1) < 0) {
      prefsObj.set("authenticate_view", 0, getContext());
      LOGGER.debug("XWikiPreferences missing authenticate_view configuration added for"
          + " database [" + getContext().getDatabase() + "].");
      dirty = true;
    }
    if (prefsObj.getLongValue("upload_maxsize") <= 0) {
      prefsObj.set("upload_maxsize", 104857600L, getContext());
      LOGGER.debug("XWikiPreferences missing upload_maxsize configuration added for"
          + " database [" + getContext().getDatabase() + "].");
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
      LOGGER.debug("XWikiPreferences added missing Celements2.Dictionary for"
          + " database [" + getContext().getDatabase() + "].");
      dirty = true;
    }
    String centralfilebaseConfig = prefsObj.getStringValue("cel_centralfilebase");
    if (StringUtils.isEmpty(centralfilebaseConfig)) {
      prefsObj.set("cel_centralfilebase", "Content_attachments.FileBaseDoc",
          getContext());
      LOGGER.debug("XWikiPreferences missing cel_centralfilebase configuration added for"
          + " database [" + getContext().getDatabase() + "].");
      dirty = true;
    }
    return dirty;
  }

  boolean checkWikiPreferencesForMainWiki(XWikiDocument wikiPrefDoc) throws XWikiException {
    String wikiName = getContext().getDatabase();
    boolean dirty = false;
    BaseObject prefsObj = wikiPrefDoc.getXObject(getXWikiPreferencesRef(wikiName),
        false, getContext());
    if (prefsObj == null) {
      prefsObj = wikiPrefDoc.newXObject(getXWikiPreferencesRef(wikiName), getContext());
      prefsObj.set("editor", "Text", getContext());
      prefsObj.set("renderXWikiRadeoxRenderer", 1, getContext());
      prefsObj.set("pageWidth", "default", getContext());
      LOGGER.debug("XWikiPreferences missing wiki preferences object added for"
          + " database [" + getContext().getDatabase() + "].");
      dirty = true;
    }
    if (prefsObj.getIntValue("multilingual", -1) < 0) {
      prefsObj.setIntValue("multilingual", 1);
      LOGGER.debug("XWikiPreferences missing multilingual configuration added for"
          + " database [" + getContext().getDatabase() + "].");
      dirty = true;
    }
    if (prefsObj.getIntValue("authenticate_edit", -1) < 0) {
      prefsObj.set("authenticate_edit", 1, getContext());
      LOGGER.debug("XWikiPreferences missing authenticate_edit configuration added for"
          + " database [" + getContext().getDatabase() + "].");
      dirty = true;
    }
    if (prefsObj.getIntValue("authenticate_view", -1) < 0) {
      prefsObj.set("authenticate_view", 1, getContext());
      LOGGER.debug("XWikiPreferences missing authenticate_view configuration added for"
          + " database [" + getContext().getDatabase() + "].");
      dirty = true;
    }
    if (prefsObj.getLongValue("upload_maxsize") <= 0) {
      prefsObj.set("upload_maxsize", 104857600L, getContext());
      LOGGER.debug("XWikiPreferences missing upload_maxsize configuration added for"
          + " database [" + getContext().getDatabase() + "].");
      dirty = true;
    }
    return dirty;
  }

  boolean checkAccessRights(XWikiDocument wikiPrefDoc)
      throws XWikiException {
    String wikiName = getContext().getDatabase();
    BaseObject editRightsObj = wikiPrefDoc.getXObject(getGlobalRightsRef(wikiName),
        false, getContext());
    if (editRightsObj == null) {
      editRightsObj = wikiPrefDoc.newXObject(getGlobalRightsRef(wikiName), getContext());
      editRightsObj.set("groups", "XWiki.ContentEditorsGroup", getContext());
      editRightsObj.set("levels", "edit,delete,undelete", getContext());
      editRightsObj.set("users", "", getContext());
      editRightsObj.set("allow", 1, getContext());
      BaseObject adminRightsObj = wikiPrefDoc.newXObject(getGlobalRightsRef(
          wikiName), getContext());
      adminRightsObj.set("groups", "XWiki.XWikiAdminGroup", getContext());
      adminRightsObj.set("levels", "admin,edit,comment,delete,undelete,register",
          getContext());
      adminRightsObj.set("users", "", getContext());
      adminRightsObj.set("allow", 1, getContext());
      LOGGER.debug("XWikiPreferences missing access rights fixed for database ["
          + getContext().getDatabase() + "].");
      return true;
    }
    return false;
  }

  boolean checkPageType(XWikiDocument wikiPrefDoc) throws XWikiException {
    DocumentReference pageTypeClassRef = getPageTypeClasses().getPageTypeClassRef(
        getContext().getDatabase());
    BaseObject pageTypeObj = wikiPrefDoc.getXObject(pageTypeClassRef, false,
        getContext());
    if (pageTypeObj == null) {
      pageTypeObj = wikiPrefDoc.newXObject(pageTypeClassRef, getContext());
      pageTypeObj.setStringValue("page_type", "WikiPreference");
      LOGGER.debug("XWikiPreferences missing page type object fixed for database ["
          + getContext().getDatabase() + "].");
      return true;
    }
    return false;
  }

  private DocumentReference getXWikiPreferencesRef(String wikiName) {
    return new DocumentReference(wikiName, "XWiki", "XWikiPreferences");
  }

  private DocumentReference getGlobalRightsRef(String wikiName) {
    return new DocumentReference(wikiName, "XWiki", "XWikiGlobalRights");
  }

}
