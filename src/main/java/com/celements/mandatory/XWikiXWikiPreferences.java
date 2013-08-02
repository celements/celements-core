package com.celements.mandatory;

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

  public void checkDocuments() throws XWikiException {
    LOGGER.trace("Start checkDocuments in XWikiXWikiPreferences for database ["
          + getContext().getDatabase() + "].");
    if (noMainWiki() && !isSkipCelementsWikiPreferences()) {
      LOGGER.trace("before checkXWikiPreferences for database ["
          + getContext().getDatabase() + "].");
      checkXWikiPreferences();
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
        LOGGER.debug("XWikiPreferencesDocument not saved. Everything uptodat. ["
            + getContext().getDatabase() + "].");
      }
    } else {
      LOGGER.trace("skip checkXWikiPreferences because wikiPrefDoc is null! ["
          + getContext().getDatabase() + "]");
    }
  }

  boolean checkWikiPreferences(XWikiDocument wikiPrefDoc) throws XWikiException {
    String wikiName = getContext().getDatabase();
    BaseObject prefsObj = wikiPrefDoc.getXObject(getXWikiPreferencesRef(wikiName),
        false, getContext());
    if (prefsObj == null) {
      prefsObj = wikiPrefDoc.newXObject(getXWikiPreferencesRef(wikiName), getContext());
      prefsObj.set("multilingual", 1, getContext());
      prefsObj.set("authenticate_edit", 1, getContext());
      prefsObj.set("authenticate_view", 0, getContext());
      prefsObj.set("editor", "Text", getContext());
      prefsObj.set("upload_maxsize", 104857600L, getContext());
      prefsObj.set("renderXWikiRadeoxRenderer", 0, getContext());
      prefsObj.set("pageWidth", "default", getContext());
      prefsObj.set("documentBundles", "celements2web:Celements2.Dictionary",
          getContext());
      prefsObj.set("cel_centralfilebase", "Content_attachments.FileBaseDoc",
          getContext());
      LOGGER.debug("XWikiPreferences missing fields in wiki preferences object fixed for"
          + " database [" + getContext().getDatabase() + "].");
      return true;
    }
    return false;
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
