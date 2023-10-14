package com.celements.appScript;

import java.util.Optional;

import javax.annotation.Nullable;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

@ComponentRole
public interface IAppScriptService {

  static final String APP_SCRIPT_ACTION_NAME_CONF_PROPERTY = "celements.appScript.actionName";
  static final String APP_SCRIPT_XPAGE = "app";
  static final String APP_SCRIPT_XWPREF_OVERW_DOCS = "appScriptOverwriteDocs";
  static final String APP_SCRIPT_CONF_OVERW_DOCS = "com.celements.appScript.overwriteDocs";
  static final String APP_SCRIPT_SPACE_NAME = "AppScripts";
  static final String APP_RECURSIVE_SCRIPT_SPACE_NAME = "AppRecursiveScripts";

  int getStartIndex(String path);

  String getAppActionName();

  boolean hasDocAppScript(@Nullable String scriptName);

  boolean hasLocalAppScript(@Nullable String scriptName);

  boolean hasLocalAppRecursiveScript(@Nullable String scriptName);

  boolean hasCentralAppScript(@Nullable String scriptName);

  boolean hasCentralAppRecursiveScript(@Nullable String scriptName);

  Optional<DocumentReference> getAppScriptDocRef(String scriptName);

  Optional<DocumentReference> getAppRecursiveScriptDocRef(String scriptName);

  Optional<DocumentReference> getLocalAppScriptDocRef(String scriptName);

  Optional<DocumentReference> getLocalAppRecursiveScriptDocRef(String scriptName);

  Optional<DocumentReference> getCentralAppScriptDocRef(String scriptName);

  Optional<DocumentReference> getCentralAppRecursiveScriptDocRef(String scriptName);

  String getAppScriptTemplatePath(String scriptName);

  boolean isAppScriptAvailable(String scriptName);

  Optional<String> getAppRecursiveScript(String scriptName);

  String getAppScriptURL(String scriptName);

  String getAppScriptURL(String scriptName, String queryString);

  boolean isAppScriptCurrentPage(String scriptName);

  String getScriptNameFromDocRef(DocumentReference docRef);

  String getScriptNameFromURL();

  boolean isAppScriptRequest();

  boolean isAppScriptOverwriteDocRef(DocumentReference docRef);

}
