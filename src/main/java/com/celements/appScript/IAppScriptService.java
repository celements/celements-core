package com.celements.appScript;

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

  boolean hasDocAppScript(String scriptName);

  boolean hasDocAppRecursiveScript(String scriptName);

  boolean hasLocalAppScript(String scriptName);

  boolean hasLocalAppRecursiveScript(String scriptName);

  boolean hasCentralAppScript(String scriptName);

  boolean hasCentralAppRecursiveScript(String scriptName);

  DocumentReference getAppScriptDocRef(String scriptName);

  DocumentReference getAppRecursiveScriptDocRef(String scriptName);

  DocumentReference getLocalAppScriptDocRef(String scriptName);

  DocumentReference getLocalAppRecursiveScriptDocRef(String scriptName);

  DocumentReference getCentralAppScriptDocRef(String scriptName);

  DocumentReference getCentralAppRecursiveScriptDocRef(String scriptName);

  String getAppScriptTemplatePath(String scriptName);

  boolean isAppScriptAvailable(String scriptName);

  String getAppRecursiveScript(String scriptName);

  String getAppScriptURL(String scriptName);

  String getAppScriptURL(String scriptName, String queryString);

  boolean isAppScriptCurrentPage(String scriptName);

  String getScriptNameFromDocRef(DocumentReference docRef);

  String getScriptNameFromURL();

  boolean isAppScriptRequest();

  boolean isAppScriptOverwriteDocRef(DocumentReference docRef);

}
