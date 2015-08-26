package com.celements.web.classcollections;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

@ComponentRole
public interface IOldCoreClassConfig {

  public static final String PHOTO_ALBUM_CLASS_DOC = "PhotoAlbumClass";
  public static final String PHOTO_ALBUM_CLASS_SPACE = "XWiki";
  public static final String PHOTO_ALBUM_CLASS = PHOTO_ALBUM_CLASS_SPACE + "."
        + PHOTO_ALBUM_CLASS_DOC;
  public static final String PHOTO_ALBUM_GALLERY_LAYOUT = "galleryLayout";

  public static final String XWIKI_USERS_CLASS_DOC = "XWikiUsers";
  public static final String XWIKI_USERS_CLASS_SPACE = "XWiki";
  public static final String XWIKI_USERS_CLASS = XWIKI_USERS_CLASS_SPACE + "."
        + XWIKI_USERS_CLASS_DOC;

  public static final String XWIKI_PREFERENCES_CLASS_DOC = "XWikiPreferences";
  public static final String XWIKI_PREFERENCES_CLASS_SPACE = "XWiki";
  public static final String XWIKI_PREFERENCES_CLASS = XWIKI_PREFERENCES_CLASS_SPACE + "."
        + XWIKI_PREFERENCES_CLASS_DOC;
  public static final String XWIKI_PREFERENCES_CELLOGIN_PROPERTY = "cellogin";

  public static final String IMPORT_CLASS_DOC = "ImportClass";
  public static final String IMPORT_CLASS_SPACE = "Classes";
  public static final String IMPORT_CLASS = IMPORT_CLASS_SPACE + "."
        + IMPORT_CLASS_DOC;

  public static final String FILEBASE_TAG_CLASS_DOC = "FilebaseTag";
  public static final String FILEBASE_TAG_CLASS_SPACE = "Classes";
  public static final String FILEBASE_TAG_CLASS = FILEBASE_TAG_CLASS_SPACE + "."
        + FILEBASE_TAG_CLASS_DOC;

  public static final String RTE_CONFIG_TYPE_CLASS_DOC = "RTEConfigTypeClass";
  public static final String RTE_CONFIG_TYPE_CLASS_SPACE = "Classes";
  public static final String RTE_CONFIG_TYPE_CLASS = RTE_CONFIG_TYPE_CLASS_SPACE + "."
        + RTE_CONFIG_TYPE_CLASS_DOC;

  public static final String KEY_VALUE_CLASS_DOC = "KeyValueClass";
  public static final String KEY_VALUE_CLASS_SPACE = "Classes";
  public static final String KEY_VALUE_CLASS = KEY_VALUE_CLASS_SPACE + "."
        + KEY_VALUE_CLASS_DOC;

  public static final String TOKEN_CLASS_DOC = "TokenClass";
  public static final String TOKEN_CLASS_SPACE = "Classes";
  public static final String TOKEN_CLASS = TOKEN_CLASS_SPACE + "." + TOKEN_CLASS_DOC;

  public static final String OVERLAY_CONFIG_CLASS_DOC = "OverlayConfigClass";
  public static final String OVERLAY_CONFIG_CLASS_SPACE = "Classes";
  public static final String OVERLAY_CONFIG_CLASS = OVERLAY_CONFIG_CLASS_SPACE + "."
      + OVERLAY_CONFIG_CLASS_DOC;

  public static final String PANEL_CONFIG_CLASS_DOC = "PanelConfigClass";
  public static final String PANEL_CONFIG_CLASS_SPACE = "Class";
  public static final String PANEL_CONFIG_CLASS = PANEL_CONFIG_CLASS_SPACE + "."
        + PANEL_CONFIG_CLASS_DOC;

  public static final String FORM_MAIL_CLASS_DOC = "FormMailClass";
  public static final String FORM_MAIL_CLASS_SPACE = "Celements2";
  public static final String FORM_MAIL_CLASS = FORM_MAIL_CLASS_SPACE + "."
        + FORM_MAIL_CLASS_DOC;

  public static final String FORM_ACTION_CLASS_DOC = "FormActionClass";
  public static final String FORM_ACTION_CLASS_SPACE = "Celements2";
  public static final String FORM_ACTION_CLASS = FORM_ACTION_CLASS_SPACE + "."
        + FORM_ACTION_CLASS_DOC;

  public static final String FORM_CONFIG_CLASS_DOC = "FormConfigClass";
  public static final String FORM_CONFIG_CLASS_SPACE = "Celements2";
  public static final String FORM_CONFIG_CLASS = FORM_CONFIG_CLASS_SPACE + "."
        + FORM_CONFIG_CLASS_DOC;

  public static final String ACTION_TYPE_CLASS_DOC = "ActionTypeClass";
  public static final String ACTION_TYPE_CLASS_SPACE = "Celements2";
  public static final String ACTION_TYPE_CLASS = ACTION_TYPE_CLASS_SPACE + "."
        + ACTION_TYPE_CLASS_DOC;

  public static final String ACTION_TYPE_PROP_CLASS_DOC = "ActionTypeProperties";
  public static final String ACTION_TYPE_PROP_CLASS_SPACE = "Celements2";
  public static final String ACTION_TYPE_PROP_CLASS = ACTION_TYPE_PROP_CLASS_SPACE + "."
        + ACTION_TYPE_PROP_CLASS_DOC;

  public static final String FORM_STORAGE_CLASS_DOC = "FormStorageClass";
  public static final String FORM_STORAGE_CLASS_SPACE = "Celements2";
  public static final String FORM_STORAGE_CLASS = FORM_STORAGE_CLASS_SPACE + "."
        + FORM_STORAGE_CLASS_DOC;

  public static final String RECEIVER_EMAIL_CLASS_DOC = "ReceiverEMail";
  public static final String RECEIVER_EMAIL_CLASS_SPACE = "Celements2";
  public static final String RECEIVER_EMAIL_CLASS = RECEIVER_EMAIL_CLASS_SPACE + "."
        + RECEIVER_EMAIL_CLASS_DOC;

  public static final String USER_CSS_CLASS_DOC = "UserCSS";
  public static final String USER_CSS_CLASS_SPACE = "Skins";
  public static final String USER_CSS_CLASS = USER_CSS_CLASS_SPACE + "."
        + USER_CSS_CLASS_DOC;

  public static final String JAVA_SCRIPTS_EXTERNAL_FILES_CLASS_DOC = "ExternalFiles";
  public static final String JAVA_SCRIPTS_EXTERNAL_FILES_CLASS_SPACE = "JavaScript";
  public static final String JAVA_SCRIPTS_EXTERNAL_FILES_CLASS =
      JAVA_SCRIPTS_EXTERNAL_FILES_CLASS_SPACE + "."
          + JAVA_SCRIPTS_EXTERNAL_FILES_CLASS_DOC;

  public DocumentReference getFromStorageClassRef(WikiReference wikiRef);

}
