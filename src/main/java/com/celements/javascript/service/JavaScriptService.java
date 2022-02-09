package com.celements.javascript.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Requirement;

import com.celements.filebase.uri.FileUriServiceRole;
import com.celements.pagelayout.LayoutServiceRole;

public class JavaScriptService implements JavaScriptServiceRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(JavaScriptService.class);

  @Requirement
  private LayoutServiceRole layoutService;

  @Requirement
  private FileUriServiceRole attUrlService;

  public String getAllExternalJavaScriptFiles() {
    // return layoutService.getHtmlHeadConfigurator(layoutService.getCurrentRenderingLayout())
    // .getAllInitialJavaScriptFiles().stream()
    // .map(this::generateScriptTag)
    // .forEach();
    return "";
  }

}
