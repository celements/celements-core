package com.celements.appScript;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;

@ComponentRole
public interface IAppScriptService {

  public static final String APP_SCRIPT_ACTION_NAME_CONF_PROPERTY
      = "celements.appScript.actionName";
  public static final String APP_SCRIPT_XPAGE = "app";

  public int getStartIndex(String path);

  public String getAppActionName();

  public boolean hasDocAppScript(String scriptName);

  public boolean hasLocalAppScript(String scriptName);

  public boolean hasCentralAppScript(String scriptName);

  public DocumentReference getAppScriptDocRef(String scriptName);

  public DocumentReference getLocalAppScriptDocRef(String scriptName);

  public DocumentReference getCentralAppScriptDocRef(String scriptName);

  public String getAppScriptTemplatePath(String scriptName);

  public boolean isAppScriptAvailable(String scriptName);

  public String getAppScriptURL(String scriptName);

  public String getAppScriptURL(String scriptName, String queryString);

  public boolean isAppScriptCurrentPage(String scriptName);

  public String getScriptNameFromURL();

  public boolean isAppScriptRequest();

}
