package com.celements.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.script.service.ScriptService;

@Component("deprecated")
public class DeprecatedUsageScriptService implements ScriptService {

  private static Logger _LOGGER = LoggerFactory.getLogger(DeprecatedUsageScriptService.class);

  public void logVelocityScript(String logMessage) {
    _LOGGER.warn("deprecated usage of velocity Script: " + logMessage);
  }

}
