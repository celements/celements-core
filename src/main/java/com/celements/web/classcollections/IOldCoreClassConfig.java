package com.celements.web.classcollections;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.javascript.JavaScriptExternalFilesClass;
import com.celements.web.classes.KeyValueClass;

@ComponentRole
public interface IOldCoreClassConfig {

  String PHOTO_ALBUM_CLASS_DOC = "PhotoAlbumClass";
  String PHOTO_ALBUM_CLASS_SPACE = "XWiki";
  String PHOTO_ALBUM_CLASS = PHOTO_ALBUM_CLASS_SPACE + "."
      + PHOTO_ALBUM_CLASS_DOC;
  String PHOTO_ALBUM_TITLE = "title";
  String PHOTO_ALBUM_DESCRIPTION = "description";
  String PHOTO_ALBUM_GALLERY_LAYOUT = "galleryLayout";

  String XWIKI_USERS_CLASS_DOC = "XWikiUsers";
  String XWIKI_USERS_CLASS_SPACE = "XWiki";
  String XWIKI_USERS_CLASS = XWIKI_USERS_CLASS_SPACE + "."
      + XWIKI_USERS_CLASS_DOC;

  String XWIKI_PREFERENCES_CLASS_DOC = "XWikiPreferences";
  String XWIKI_PREFERENCES_CLASS_SPACE = "XWiki";
  String XWIKI_PREFERENCES_CLASS = XWIKI_PREFERENCES_CLASS_SPACE + "."
      + XWIKI_PREFERENCES_CLASS_DOC;
  String XWIKI_PREFERENCES_CELLOGIN_PROPERTY = "cellogin";

  String IMPORT_CLASS_DOC = "ImportClass";
  String IMPORT_CLASS_SPACE = "Classes";
  String IMPORT_CLASS = IMPORT_CLASS_SPACE + "." + IMPORT_CLASS_DOC;

  String FILEBASE_TAG_CLASS_DOC = "FilebaseTag";
  String FILEBASE_TAG_CLASS_SPACE = "Classes";
  String FILEBASE_TAG_CLASS = FILEBASE_TAG_CLASS_SPACE + "."
      + FILEBASE_TAG_CLASS_DOC;

  String RTE_CONFIG_TYPE_CLASS_DOC = "RTEConfigTypeClass";
  String RTE_CONFIG_TYPE_CLASS_SPACE = "Classes";
  String RTE_CONFIG_TYPE_CLASS = RTE_CONFIG_TYPE_CLASS_SPACE + "."
      + RTE_CONFIG_TYPE_CLASS_DOC;

  @Deprecated
  String KEY_VALUE_CLASS_DOC = KeyValueClass.DOC_NAME;
  @Deprecated
  String KEY_VALUE_CLASS_SPACE = KeyValueClass.SPACE_NAME;
  @Deprecated
  String KEY_VALUE_CLASS = KeyValueClass.CLASS_DEF_HINT;

  String TOKEN_CLASS_DOC = "TokenClass";
  String TOKEN_CLASS_SPACE = "Classes";
  String TOKEN_CLASS = TOKEN_CLASS_SPACE + "." + TOKEN_CLASS_DOC;

  String OVERLAY_CONFIG_CLASS_DOC = "OverlayConfigClass";
  String OVERLAY_CONFIG_CLASS_SPACE = "Classes";
  String OVERLAY_CONFIG_CLASS = OVERLAY_CONFIG_CLASS_SPACE + "."
      + OVERLAY_CONFIG_CLASS_DOC;

  String PANEL_CONFIG_CLASS_DOC = "PanelConfigClass";
  String PANEL_CONFIG_CLASS_SPACE = "Class";
  String PANEL_CONFIG_CLASS = PANEL_CONFIG_CLASS_SPACE + "."
      + PANEL_CONFIG_CLASS_DOC;

  String FORM_MAIL_CLASS_DOC = "FormMailClass";
  String FORM_MAIL_CLASS_SPACE = "Celements2";
  String FORM_MAIL_CLASS = FORM_MAIL_CLASS_SPACE + "." + FORM_MAIL_CLASS_DOC;

  String FORM_ACTION_CLASS_DOC = "FormActionClass";
  String FORM_ACTION_CLASS_SPACE = "Celements2";
  String FORM_ACTION_CLASS = FORM_ACTION_CLASS_SPACE + "."
      + FORM_ACTION_CLASS_DOC;

  @Deprecated
  /**
   * @deprecated since 3.8, instead use com.celements.form.classes.FormConfigClass.CLASS_NAME
   */
  String FORM_CONFIG_CLASS_DOC = "FormConfigClass";
  @Deprecated
  /**
   * @deprecated since 3.8, instead use com.celements.form.classes.FormClass.CLASS_SPACE
   */
  String FORM_CONFIG_CLASS_SPACE = "Celements2";
  @Deprecated
  /**
   * @deprecated since 3.8, instead use com.celements.form.classes.FormConfigClass.CLASS_DEF_HINT
   */
  String FORM_CONFIG_CLASS = FORM_CONFIG_CLASS_SPACE + "."
      + FORM_CONFIG_CLASS_DOC;

  String ACTION_TYPE_CLASS_DOC = "ActionTypeClass";
  String ACTION_TYPE_CLASS_SPACE = "Celements2";
  String ACTION_TYPE_CLASS = ACTION_TYPE_CLASS_SPACE + "."
      + ACTION_TYPE_CLASS_DOC;

  String ACTION_TYPE_PROP_CLASS_DOC = "ActionTypeProperties";
  String ACTION_TYPE_PROP_CLASS_SPACE = "Celements2";
  String ACTION_TYPE_PROP_CLASS = ACTION_TYPE_PROP_CLASS_SPACE + "."
      + ACTION_TYPE_PROP_CLASS_DOC;

  String FORM_STORAGE_CLASS_DOC = "FormStorageClass";
  String FORM_STORAGE_CLASS_SPACE = "Celements2";
  String FORM_STORAGE_CLASS = FORM_STORAGE_CLASS_SPACE + "."
      + FORM_STORAGE_CLASS_DOC;

  String RECEIVER_EMAIL_CLASS_DOC = "ReceiverEMail";
  String RECEIVER_EMAIL_CLASS_SPACE = "Celements2";
  String RECEIVER_EMAIL_CLASS = RECEIVER_EMAIL_CLASS_SPACE + "."
      + RECEIVER_EMAIL_CLASS_DOC;

  String USER_CSS_CLASS_DOC = "UserCSS";
  String USER_CSS_CLASS_SPACE = "Skins";
  String USER_CSS_CLASS = USER_CSS_CLASS_SPACE + "." + USER_CSS_CLASS_DOC;

  /**
   * @deprecated since 5.4, instead use {@link JavaScriptExternalFilesClass#DOC_NAME}
   */
  @Deprecated
  String JAVA_SCRIPTS_EXTERNAL_FILES_CLASS_DOC = "ExternalFiles";
  /**
   * @deprecated since 5.4, instead use {@link JavaScriptExternalFilesClass#SPACE_NAME}
   */
  @Deprecated
  String JAVA_SCRIPTS_EXTERNAL_FILES_CLASS_SPACE = "JavaScript";
  /**
   * @deprecated since 5.4, instead use {@link JavaScriptExternalFilesClass#CLASS_DEF_HINT}
   */
  @Deprecated
  String JAVA_SCRIPTS_EXTERNAL_FILES_CLASS = JAVA_SCRIPTS_EXTERNAL_FILES_CLASS_SPACE
      + "." + JAVA_SCRIPTS_EXTERNAL_FILES_CLASS_DOC;

  DocumentReference getFromStorageClassRef(WikiReference wikiRef);

  DocumentReference getXWikiUsersClassRef(WikiReference wikiRef);

  ClassReference getPhotoAlbumClassRef();

}
