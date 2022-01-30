package com.celements.javascript.service;

import java.util.Map;

import org.xwiki.component.annotation.Requirement;

import com.celements.rendering.head.HtmlHeadConfiguratorRole;

public class JavaScriptService implements JavaScriptServiceRole {

  @Requirement
  private Map<String, HtmlHeadConfiguratorRole> htmlHeadConfiguratorMap;

}
