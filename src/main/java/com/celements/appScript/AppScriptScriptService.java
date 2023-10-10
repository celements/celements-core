package com.celements.appScript;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.xpn.xwiki.util.Util;

@Component("appscript")
public class AppScriptScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppScriptScriptService.class);

  @Requirement
  IAppScriptService appScriptService;

  public String getCurrentPageURL(String queryString) {
    String ret;
    if (isAppScriptRequest()) {
      LOGGER.debug("getCurrentPageURL: AppScript for query '{}'", queryString);
      ret = getAppScriptURL(getScriptNameFromURL(), queryString);
    } else {
      LOGGER.debug("getCurrentPageURL: query '{}'", queryString);
      ret = Util.escapeURL("?" + queryString);
    }
    LOGGER.debug("getCurrentPageURL: ret '{}' for query '{}'", ret, queryString);
    return ret;
  }

  public boolean isAppScriptRequest() {
    return appScriptService.isAppScriptRequest();
  }

  public boolean isAppScriptCurrentPage(String scriptName) {
    return appScriptService.isAppScriptCurrentPage(scriptName);
  }

  public String getScriptNameFromURL() {
    return appScriptService.getScriptNameFromURL();
  }

  public String getAppScriptURL(String scriptName, String queryString) {
    return appScriptService.getAppScriptURL(scriptName, queryString);
  }

  public String getAppScriptURL(String scriptName) {
    return appScriptService.getAppScriptURL(scriptName);
  }

  public void setAppScriptService(IAppScriptService appScriptService) {
    this.appScriptService = appScriptService;
  }

  public boolean hasDocAppScript(String scriptName) {
    return appScriptService.hasDocAppScript(scriptName);
  }

  public DocumentReference getAppScriptDocRef(String scriptName) {
    return appScriptService.getAppScriptDocRef(scriptName);
  }

  public DocumentReference getAppRecursiveScriptDocRef(String scriptName) {
    return appScriptService.getAppRecursiveScriptDocRef(scriptName);
  }

  public boolean isAppScriptAvailable(String scriptName) {
    return appScriptService.isAppScriptAvailable(scriptName);
  }

  public String getAppRecursiveScript(String scriptName) {
    return appScriptService.getAppRecursiveScript(scriptName);
  }

  public String getAppScriptTemplatePath(String scriptName) {
    return appScriptService.getAppScriptTemplatePath(scriptName);
  }
}
