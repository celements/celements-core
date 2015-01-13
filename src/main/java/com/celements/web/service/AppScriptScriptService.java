package com.celements.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.script.service.ScriptService;

import com.celements.appScript.IAppScriptService;
import com.xpn.xwiki.util.Util;

@Component("appscript")
public class AppScriptScriptService implements ScriptService {
  
  private static Logger _LOGGER  = LoggerFactory.getLogger(AppScriptScriptService.class);
  
  @Requirement
  IAppScriptService appScriptService;
  
  public String getCurrentPageURL(String queryString) {
    String ret;
    if(isAppScriptRequest()) {
      _LOGGER.debug("getCurrentPageURL: AppScript for query '" + queryString + "'");
      ret = getAppScriptURL(getScriptNameFromURL(), queryString);
    } else {
      _LOGGER.debug("getCurrentPageURL: query '" + queryString + "'");
      ret = Util.escapeURL("?" + queryString);
    }
    _LOGGER.debug("getCurrentPageURL: ret '" + ret + "' for query '" + queryString + "'");
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
}
