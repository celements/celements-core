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
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.classes.AbstractClassCollection;
import com.celements.javascript.JavaScriptExternalFilesClass;
import com.celements.rteConfig.classes.RTEConfigClasses;
import com.celements.web.classes.KeyValueClass;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

@Component("celements.oldCoreClasses")
public class OldCoreClasses extends AbstractClassCollection {

  @Deprecated
  public static final String PHOTO_ALBUM_CLASS_DOC = IOldCoreClassConfig.PHOTO_ALBUM_CLASS_DOC;
  @Deprecated
  public static final String PHOTO_ALBUM_CLASS_SPACE = IOldCoreClassConfig.PHOTO_ALBUM_CLASS_SPACE;
  @Deprecated
  public static final String PHOTO_ALBUM_CLASS = PHOTO_ALBUM_CLASS_SPACE + "."
      + PHOTO_ALBUM_CLASS_DOC;
  @Deprecated
  public static final String PHOTO_ALBUM_GALLERY_LAYOUT = IOldCoreClassConfig.PHOTO_ALBUM_GALLERY_LAYOUT;

  @Deprecated
  public static final String XWIKI_USERS_CLASS_DOC = IOldCoreClassConfig.XWIKI_USERS_CLASS_DOC;
  @Deprecated
  public static final String XWIKI_USERS_CLASS_SPACE = IOldCoreClassConfig.XWIKI_USERS_CLASS_SPACE;
  @Deprecated
  public static final String XWIKI_USERS_CLASS = XWIKI_USERS_CLASS_SPACE + "."
      + XWIKI_USERS_CLASS_DOC;

  @Deprecated
  public static final String XWIKI_PREFERENCES_CLASS_DOC = IOldCoreClassConfig.XWIKI_PREFERENCES_CLASS_DOC;
  @Deprecated
  public static final String XWIKI_PREFERENCES_CLASS_SPACE = IOldCoreClassConfig.XWIKI_PREFERENCES_CLASS_SPACE;
  @Deprecated
  public static final String XWIKI_PREFERENCES_CLASS = XWIKI_PREFERENCES_CLASS_SPACE + "."
      + XWIKI_PREFERENCES_CLASS_DOC;
  @Deprecated
  public static final String XWIKI_PREFERENCES_CELLOGIN_PROPERTY = IOldCoreClassConfig.XWIKI_PREFERENCES_CELLOGIN_PROPERTY;

  @Deprecated
  public static final String IMPORT_CLASS_DOC = IOldCoreClassConfig.IMPORT_CLASS_DOC;
  @Deprecated
  public static final String IMPORT_CLASS_SPACE = IOldCoreClassConfig.IMPORT_CLASS_SPACE;
  @Deprecated
  public static final String IMPORT_CLASS = IMPORT_CLASS_SPACE + "." + IMPORT_CLASS_DOC;

  @Deprecated
  public static final String FILEBASE_TAG_CLASS_DOC = IOldCoreClassConfig.FILEBASE_TAG_CLASS_DOC;
  @Deprecated
  public static final String FILEBASE_TAG_CLASS_SPACE = IOldCoreClassConfig.FILEBASE_TAG_CLASS_SPACE;
  @Deprecated
  public static final String FILEBASE_TAG_CLASS = FILEBASE_TAG_CLASS_SPACE + "."
      + FILEBASE_TAG_CLASS_DOC;

  @Deprecated
  public static final String RTE_CONFIG_TYPE_CLASS_DOC = "RTEConfigTypeClass";
  @Deprecated
  public static final String RTE_CONFIG_TYPE_CLASS_SPACE = "Classes";
  @Deprecated
  public static final String RTE_CONFIG_TYPE_CLASS = RTE_CONFIG_TYPE_CLASS_SPACE + "."
      + RTE_CONFIG_TYPE_CLASS_DOC;

  @Deprecated
  public static final String RTE_CONFIG_TYPE_PRPOP_CLASS_DOC = RTEConfigClasses.RTE_CONFIG_TYPE_PRPOP_CLASS_DOC;
  @Deprecated
  public static final String RTE_CONFIG_TYPE_PRPOP_CLASS_SPACE = RTEConfigClasses.RTE_CONFIG_TYPE_PRPOP_CLASS_SPACE;
  @Deprecated
  public static final String RTE_CONFIG_TYPE_PRPOP_CLASS = RTEConfigClasses.RTE_CONFIG_TYPE_PRPOP_CLASS;

  @Deprecated
  public static final String KEY_VALUE_CLASS_DOC = KeyValueClass.DOC_NAME;
  @Deprecated
  public static final String KEY_VALUE_CLASS_SPACE = KeyValueClass.SPACE_NAME;
  @Deprecated
  public static final String KEY_VALUE_CLASS = KeyValueClass.CLASS_DEF_HINT;

  @Deprecated
  public static final String TOKEN_CLASS_DOC = "TokenClass";
  @Deprecated
  public static final String TOKEN_CLASS_SPACE = "Classes";
  @Deprecated
  public static final String TOKEN_CLASS = TOKEN_CLASS_SPACE + "." + TOKEN_CLASS_DOC;

  @Deprecated
  public static final String OVERLAY_CONFIG_CLASS_DOC = "OverlayConfigClass";
  @Deprecated
  public static final String OVERLAY_CONFIG_CLASS_SPACE = "Classes";
  @Deprecated
  public static final String OVERLAY_CONFIG_CLASS = OVERLAY_CONFIG_CLASS_SPACE + "."
      + OVERLAY_CONFIG_CLASS_DOC;

  @Deprecated
  public static final String PANEL_CONFIG_CLASS_DOC = "PanelConfigClass";
  @Deprecated
  public static final String PANEL_CONFIG_CLASS_SPACE = "Class";
  @Deprecated
  public static final String PANEL_CONFIG_CLASS = PANEL_CONFIG_CLASS_SPACE + "."
      + PANEL_CONFIG_CLASS_DOC;

  @Deprecated
  public static final String FORM_MAIL_CLASS_DOC = "FormMailClass";
  @Deprecated
  public static final String FORM_MAIL_CLASS_SPACE = "Celements2";
  @Deprecated
  public static final String FORM_MAIL_CLASS = FORM_MAIL_CLASS_SPACE + "." + FORM_MAIL_CLASS_DOC;

  @Deprecated
  public static final String FORM_ACTION_CLASS_DOC = "FormActionClass";
  @Deprecated
  public static final String FORM_ACTION_CLASS_SPACE = "Celements2";
  @Deprecated
  public static final String FORM_ACTION_CLASS = FORM_ACTION_CLASS_SPACE + "."
      + FORM_ACTION_CLASS_DOC;

  @Deprecated
  public static final String FORM_CONFIG_CLASS_DOC = "FormConfigClass";
  @Deprecated
  public static final String FORM_CONFIG_CLASS_SPACE = "Celements2";
  @Deprecated
  public static final String FORM_CONFIG_CLASS = FORM_CONFIG_CLASS_SPACE + "."
      + FORM_CONFIG_CLASS_DOC;

  @Deprecated
  public static final String ACTION_TYPE_CLASS_DOC = "ActionTypeClass";
  @Deprecated
  public static final String ACTION_TYPE_CLASS_SPACE = "Celements2";
  @Deprecated
  public static final String ACTION_TYPE_CLASS = ACTION_TYPE_CLASS_SPACE + "."
      + ACTION_TYPE_CLASS_DOC;

  @Deprecated
  public static final String ACTION_TYPE_PROP_CLASS_DOC = "ActionTypeProperties";
  @Deprecated
  public static final String ACTION_TYPE_PROP_CLASS_SPACE = "Celements2";
  @Deprecated
  public static final String ACTION_TYPE_PROP_CLASS = ACTION_TYPE_PROP_CLASS_SPACE + "."
      + ACTION_TYPE_PROP_CLASS_DOC;

  @Deprecated
  public static final String FORM_STORAGE_CLASS_DOC = "FormStorageClass";
  @Deprecated
  public static final String FORM_STORAGE_CLASS_SPACE = "Celements2";
  @Deprecated
  public static final String FORM_STORAGE_CLASS = FORM_STORAGE_CLASS_SPACE + "."
      + FORM_STORAGE_CLASS_DOC;

  @Deprecated
  public static final String RECEIVER_EMAIL_CLASS_DOC = "ReceiverEMail";
  @Deprecated
  public static final String RECEIVER_EMAIL_CLASS_SPACE = "Celements2";
  @Deprecated
  public static final String RECEIVER_EMAIL_CLASS = RECEIVER_EMAIL_CLASS_SPACE + "."
      + RECEIVER_EMAIL_CLASS_DOC;

  @Deprecated
  public static final String USER_CSS_CLASS_DOC = "UserCSS";
  @Deprecated
  public static final String USER_CSS_CLASS_SPACE = "Skins";
  @Deprecated
  public static final String USER_CSS_CLASS = USER_CSS_CLASS_SPACE + "." + USER_CSS_CLASS_DOC;

  /**
   * @deprecated since 4.0 instead use IOldCoreClassConfig.JAVA_SCRIPTS_EXTERNAL_FILES_CLASS_DOC
   */
  @Deprecated
  public static final String JAVA_SCRIPTS_EXTERNAL_FILES_CLASS_DOC = "ExternalFiles";
  /**
   * @deprecated since 4.0 instead use IOldCoreClassConfig.JAVA_SCRIPTS_EXTERNAL_FILES_CLASS_SPACE
   */
  @Deprecated
  public static final String JAVA_SCRIPTS_EXTERNAL_FILES_CLASS_SPACE = "JavaScript";
  /**
   * @deprecated since 4.0 instead use IOldCoreClassConfig.JAVA_SCRIPTS_EXTERNAL_FILES_CLASS
   */
  @Deprecated
  public static final String JAVA_SCRIPTS_EXTERNAL_FILES_CLASS = JAVA_SCRIPTS_EXTERNAL_FILES_CLASS_SPACE
      + "." + JAVA_SCRIPTS_EXTERNAL_FILES_CLASS_DOC;

  @Override
  public String getConfigName() {
    return "oldCoreClasses";
  }

  @Override
  protected void initClasses() throws XWikiException {
    // old core xwiki
    getContext().getWiki().getPrefsClass(getContext());
    getContext().getWiki().getUserClass(getContext());
    getContext().getWiki().getTagClass(getContext());
    getContext().getWiki().getGroupClass(getContext());
    getContext().getWiki().getRightsClass(getContext());
    getContext().getWiki().getCommentsClass(getContext());
    getContext().getWiki().getSkinClass(getContext());
    getContext().getWiki().getGlobalRightsClass(getContext());
    getContext().getWiki().getSheetClass(getContext());
    // old core celements
    getExtended_XWikiPreferencesClass();
    getXWikiUsersClass();
    // getFormMailClass();
    // getUserCSSClass();
    getImportClass();
    getPhotoAlbumClass();
    getFilebaseTagClass();
    getFormActionClass();
    getActionTypeClass();
    getActionTypePropertiesClass();
    getFormStorageClass();
    getReceiverEMailClass();
    // getJavaScriptExternalFilesClass();
    getContextMenuItemClass();
    getPanelConfigClass();
    getRTEConfigTypeClass();
    getTokenClass();
    getOverlayConfigClass();
    getRedirectClass();
  }

  public DocumentReference getRedirectClassRef(String wikiName) {
    return new DocumentReference(wikiName, "Celements2", "Redirect");
  }

  private BaseClass getRedirectClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getRedirectClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get Celements2.Redirect class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addTextField("redirect", "Redirect", 30);
    needsUpdate |= bclass.addTextField("querystr", "Query String", 30);
    needsUpdate |= addBooleanField(bclass, "show_included", "Show Included", "yesno", 0);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public static ClassReference getContextMenuItemClassRef() {
    return new ClassReference("Celements2", "ContextMenuItemClass");
  }

  public DocumentReference getContextMenuItemClassRef(String wikiName) {
    return getContextMenuItemClassRef().getDocRef(new WikiReference(wikiName));
  }

  private BaseClass getContextMenuItemClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getContextMenuItemClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get Celements2.ContextMenuItemClass class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addTextAreaField("cmi_link", "CMItem Link (velocity code)", 80, 20);
    needsUpdate |= bclass.addTextField("cmi_text", "CMItem Link Name", 30);
    needsUpdate |= bclass.addTextField("cmi_icon", "optional CMItem icon", 30);
    needsUpdate |= bclass.addTextField("cmi_shortcut", "optional shortcut definition", 30);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getPanelConfigClassRef(String wikiName) {
    return new DocumentReference(wikiName, PANEL_CONFIG_CLASS_SPACE, PANEL_CONFIG_CLASS_DOC);
  }

  private BaseClass getPanelConfigClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getPanelConfigClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + PANEL_CONFIG_CLASS + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addStaticListField("config_name", "Panel Config Type", 1, false,
        "leftPanels|rightPanels", "select");
    needsUpdate |= bclass.addBooleanField("show_panels", "Display the panel column", "yesno");
    needsUpdate |= bclass.addTextField("panels", "Panels displayed", 60);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getXWikiPreferencesClassRef(String wikiName) {
    return new DocumentReference(wikiName, XWIKI_PREFERENCES_CLASS_SPACE,
        XWIKI_PREFERENCES_CLASS_DOC);
  }

  private BaseClass getExtended_XWikiPreferencesClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getXWikiPreferencesClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + XWIKI_PREFERENCES_CLASS + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addTextField("cel_centralfilebase", "Central Filebase", 30);
    needsUpdate |= bclass.addStaticListField("celements_version", "Celements Version Mode", 1,
        false, "---|celements2|celements3", "select");
    needsUpdate |= bclass.addTextField("cel_edittabs_config", "Tab Config", 30);
    needsUpdate |= bclass.addTextField(XWIKI_PREFERENCES_CELLOGIN_PROPERTY,
        "login data fields for usercredntials", 30);
    needsUpdate |= bclass.addTextField("admin_language", "Admin Language", 30);
    // plugins property works only for the main wiki.
    // needsUpdate |= bclass.addTextField("plugins" , "Additional Plugins", 30);
    needsUpdate |= bclass.addTextField("activated_classcollections", "Activated Class Collections",
        30);
    needsUpdate |= bclass.addTextField("celGoogleAnalyticsAccount",
        "Google Analytics Account Number", 30);

    // Ensure all needed Sendmail Plugin config fields are available
    needsUpdate |= bclass.addTextField("admin_email", "Admin eMail", 30);
    needsUpdate |= bclass.addTextField("smtp_server", "SMTP Server", 30);
    needsUpdate |= bclass.addTextField("smtp_port", "SMTP Port", 5);
    needsUpdate |= bclass.addTextField("smtp_server_username", "Server username (optional)", 30);
    needsUpdate |= bclass.addTextField("smtp_server_password", "Server password (optional)", 30);
    needsUpdate |= bclass.addTextAreaField("javamail_extra_props", "Additional JavaMail properties",
        60, 6);
    needsUpdate |= bclass.addBooleanField("use_navigation_images", "Use Images for Navigation",
        "yesno");
    needsUpdate |= bclass.addBooleanField("publishdate_active", "Activate Publication Date",
        "yesno");
    needsUpdate |= bclass.addBooleanField("extract_active", "Activate Document Extracts", "yesno");

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getXWikiUsersClassRef(String wikiName) {
    return new DocumentReference(wikiName, XWIKI_USERS_CLASS_SPACE, XWIKI_USERS_CLASS_DOC);
  }

  private BaseClass getXWikiUsersClass() throws XWikiException {
    XWikiDocument doc;
    XWiki xwiki = getContext().getWiki();
    boolean needsUpdate = false;
    String wikiName = getContext().getDatabase();
    DocumentReference classRef = getXWikiUsersClassRef(wikiName);

    try {
      doc = xwiki.getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + XWIKI_USERS_CLASS + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addBooleanField("force_pwd_change", "force_pwd_change", "yesno");
    needsUpdate |= bclass.addTextField("admin_language", "User Edit-Interface Language", 4);
    needsUpdate |= addBooleanField(bclass, "suspended", "Suspended", "yesno", 0);

    LOGGER.debug("checking XWikiUsers Class for " + getContext().getDatabase() + " update needed: "
        + needsUpdate);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  @Deprecated
  public DocumentReference getFormMailClassRef(String wikiName) {
    return new DocumentReference(wikiName, FORM_MAIL_CLASS_SPACE, FORM_MAIL_CLASS_DOC);
  }

  private BaseClass getFormMailClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getFormMailClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + FORM_MAIL_CLASS + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addTextField("name", "name", 30);
    needsUpdate |= bclass.addTextField("emailFrom", "emailFrom", 30);
    needsUpdate |= bclass.addTextField("emailFields", "emailFields", 30);
    needsUpdate |= bclass.addTextField("userEmailFields", "userEmailFields", 30);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getPhotoAlbumClassRef(String wikiName) {
    return new DocumentReference(wikiName, PHOTO_ALBUM_CLASS_SPACE, PHOTO_ALBUM_CLASS_DOC);
  }

  private BaseClass getPhotoAlbumClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getPhotoAlbumClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + PHOTO_ALBUM_CLASS + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addTextField("title", "title", 30);
    needsUpdate |= bclass.addTextAreaField("description", "description", 30, 15);
    needsUpdate |= bclass.addNumberField("height", "humbnail Height", 30, "integer");
    needsUpdate |= bclass.addNumberField("thumbWidth", "Thumbnail Width", 30, "integer");
    needsUpdate |= bclass.addNumberField("height2", "Photo Height", 30, "integer");
    needsUpdate |= bclass.addNumberField("photoWidth", "Photo Width", 30, "integer");
    needsUpdate |= bclass.addNumberField("id", "id", 30, "integer");
    needsUpdate |= bclass.addBooleanField("hasOverview", "hasOverview", "yesno");
    needsUpdate |= bclass.addStaticListField("theme", "theme",
        "grey|black|red|green|blue|gold|orange");
    needsUpdate |= bclass.addTextField(PHOTO_ALBUM_GALLERY_LAYOUT, "image gallery slideshow layout",
        30);
    needsUpdate |= bclass.addNumberField("initialdelay", "initialdelay", 30, "long");
    needsUpdate |= bclass.addNumberField("frequency", "frequency", 30, "long");
    needsUpdate |= bclass.addBooleanField("thumbDescription", "Thumbnail Description", "yesno");
    needsUpdate |= addBooleanField(bclass, "showDescription", "is description shown", "yesno", 1);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getFilebaseTagClassRef(String wikiName) {
    return new DocumentReference(wikiName, FILEBASE_TAG_CLASS_SPACE, FILEBASE_TAG_CLASS_DOC);
  }

  private BaseClass getFilebaseTagClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getFilebaseTagClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + FILEBASE_TAG_CLASS + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addTextField("attachment", "attachment", 30);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getImportClassRef(String wikiName) {
    return new DocumentReference(wikiName, IMPORT_CLASS_SPACE, IMPORT_CLASS_DOC);
  }

  private BaseClass getImportClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getImportClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + IMPORT_CLASS + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addTextField("filebaseTagDocName", "filebaseTagDocName", 30);
    needsUpdate |= bclass.addTextField("preimport_link", "preimport_link", 30);
    needsUpdate |= bclass.addTextField("allowed_file_extentions", "allowed_file_extentions", 30);
    needsUpdate |= bclass.addTextAreaField("js_preimport_function",
        "js_preimport_function (optional)", 80, 15);
    needsUpdate |= bclass.addTextField("encoding", "encoding (optional)", 30);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getUserCssClassRef(String wikiName) {
    return new DocumentReference(wikiName, USER_CSS_CLASS_SPACE, USER_CSS_CLASS_DOC);
  }

  public DocumentReference getFormActionClassRef(String wikiName) {
    return new DocumentReference(wikiName, FORM_ACTION_CLASS_SPACE, FORM_ACTION_CLASS_DOC);
  }

  private BaseClass getFormActionClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getFormActionClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + FORM_ACTION_CLASS + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addTextField("doc_fullName", "doc_fullName", 30);
    needsUpdate |= bclass.addTextAreaField("completeRuleSnippet", "is Form complete Rule", 80, 15);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  /**
   * @deprecated since 3.8, instead use FormConfigClass.getDocRef(WikiReference wikiRef)
   */
  @Deprecated
  public DocumentReference getFormConfigClassRef(String wikiName) {
    return new DocumentReference(wikiName, FORM_CONFIG_CLASS_SPACE, FORM_CONFIG_CLASS_DOC);
  }

  public DocumentReference getActionTypeClassRef(String wikiName) {
    return new DocumentReference(wikiName, ACTION_TYPE_CLASS_SPACE, ACTION_TYPE_CLASS_DOC);
  }

  private BaseClass getActionTypeClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getActionTypeClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + ACTION_TYPE_CLASS + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addTextField("action_type", "Action Type", 30);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getActionTypePropertiesClassRef(String wikiName) {
    return new DocumentReference(wikiName, ACTION_TYPE_PROP_CLASS_SPACE,
        ACTION_TYPE_PROP_CLASS_DOC);
  }

  private BaseClass getActionTypePropertiesClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getActionTypePropertiesClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + ACTION_TYPE_PROP_CLASS + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addTextField("action_exec", "Action Exec Template", 30);
    needsUpdate |= bclass.addTextField("action_name", "Action Pretty Name", 30);
    needsUpdate |= bclass.addTextField("action_edit", "Action Edit Template", 30);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  /**
   * @Deprecated instead use IOldCoreClassConfig.getFormStorageClassRef
   */
  @Deprecated
  public DocumentReference getFromStorageClassRef(String wikiName) {
    return new DocumentReference(wikiName, FORM_STORAGE_CLASS_SPACE, FORM_STORAGE_CLASS_DOC);
  }

  private BaseClass getFormStorageClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getFromStorageClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + FORM_STORAGE_CLASS + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addTextField("storageLocation", "Storage Location", 30);
    needsUpdate |= bclass.addTextField("storageClassname", "Storage Classname", 30);
    needsUpdate |= bclass.addTextAreaField("storeMapping", "Store Mapping", 80, 15);
    needsUpdate |= bclass.addTextAreaField("exportMapping", "exportMapping", 80, 15);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getReceiverEmailClassRef(String wikiName) {
    return new DocumentReference(wikiName, RECEIVER_EMAIL_CLASS_SPACE, RECEIVER_EMAIL_CLASS_DOC);
  }

  private BaseClass getReceiverEMailClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getReceiverEmailClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + RECEIVER_EMAIL_CLASS + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addTextField("email", "email", 30);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getJavaScriptExternalFilesClassRef(String wikiName) {
    return new DocumentReference(wikiName, JAVA_SCRIPTS_EXTERNAL_FILES_CLASS_SPACE,
        JAVA_SCRIPTS_EXTERNAL_FILES_CLASS_DOC);
  }

  /**
   * @deprecated since 5.4, instead use {@link JavaScriptExternalFilesClass#getClassRef()}
   */
  @Deprecated
  public DocumentReference getRTEConfigTypeClassRef(String wikiName) {
    return new DocumentReference(wikiName, RTE_CONFIG_TYPE_CLASS_SPACE, RTE_CONFIG_TYPE_CLASS_DOC);
  }

  private BaseClass getRTEConfigTypeClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getRTEConfigTypeClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + RTE_CONFIG_TYPE_CLASS + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addTextField("rteconfig", "RichTextEditor Config Document", 30);

    if (!"internal".equals(bclass.getCustomMapping())) {
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  /**
   * @deprecated instead use IRTEConfigClassConfig.getRTEConfigTypePropertiesClassRef
   */
  @Deprecated
  public DocumentReference getRTEConfigTypePropertiesClassRef(String wikiName) {
    return new DocumentReference(wikiName, RTE_CONFIG_TYPE_PRPOP_CLASS_SPACE,
        RTE_CONFIG_TYPE_PRPOP_CLASS_DOC);
  }

  @Deprecated
  public DocumentReference getTagValueClassRef(String wikiName) {
    return new DocumentReference(wikiName, KEY_VALUE_CLASS_SPACE, KEY_VALUE_CLASS_DOC);
  }

  public DocumentReference getTokenClassRef(String wikiName) {
    return new DocumentReference(wikiName, TOKEN_CLASS_SPACE, TOKEN_CLASS_DOC);
  }

  private BaseClass getTokenClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getTokenClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + TOKEN_CLASS + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addPasswordField("tokenvalue", "Token value", 30);
    needsUpdate |= bclass.addDateField("validuntil", "Valid until");

    if (!"internal".equals(bclass.getCustomMapping())) {
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  public DocumentReference getOverlayConfigClassRef(String wikiName) {
    return new DocumentReference(wikiName, OVERLAY_CONFIG_CLASS_SPACE, OVERLAY_CONFIG_CLASS_DOC);
  }

  private BaseClass getOverlayConfigClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getOverlayConfigClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + OVERLAY_CONFIG_CLASS + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addBooleanField("loadAdminMenuBar", "Load admin menu bar", "yesno");
    needsUpdate |= bclass.addBooleanField("showOverlayLanguageBar", "Show overlay language bar",
        "yesno");
    needsUpdate |= bclass.addBooleanField("showCloseOverlayButton", "Show close overlay button",
        "yesno");
    needsUpdate |= bclass.addTextField("documentToLoadInOverlay", "Document to load in overlay",
        30);
    needsUpdate |= bclass.addTextAreaField("overlayCSS", "Overlay CSS", 80, 3);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

}
