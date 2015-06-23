package com.celements.pagetype.java;

import java.util.List;

import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.pagetype.IPageTypeConfig;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

/**
 * DefaultPageTypeConfig may be exposed to non privileged code (e.g. scripts)
 */
public class DefaultPageTypeConfig implements IPageTypeConfig {

  private IJavaPageTypeRole pageTypeImpl;

  private XWikiContext getContext() {
    Execution execution = Utils.getComponent(Execution.class);
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  private IWebUtilsService getWebUtilsService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

  public DefaultPageTypeConfig(IJavaPageTypeRole pageTypeImpl) {
    this.pageTypeImpl = pageTypeImpl;
  }

  @Override
  public String getName() {
    return pageTypeImpl.getName();
  }

  @Override
  public String getPrettyName() {
    String dictNameKey = "cel_pagetype_prettyname_" + getName();
    String dictionaryPrettyName = getWebUtilsService().getAdminMessageTool().get(
        dictNameKey);
    if (dictNameKey.equals(dictionaryPrettyName)) {
      return dictionaryPrettyName;
    }
    return getName();
  }

  @Override
  public boolean hasPageTitle() {
    return pageTypeImpl.hasPageTitle();
  }

  @Override
  public boolean displayInFrameLayout() {
    return pageTypeImpl.displayInFrameLayout();
  }

  @Override
  public List<String> getCategories() {
    return pageTypeImpl.getCategories();
  }

  @Override
  public String getRenderTemplateForRenderMode(String renderMode) {
    DocumentReference localTemplateRef = getLocalTemplateRef(renderMode);
    return getWebUtilsService().getInheritedTemplatedPath(localTemplateRef);
  }

  private DocumentReference getLocalTemplateRef(String renderMode) {
    return new DocumentReference(getContext().getDatabase(),
        "Templates", pageTypeImpl.getRenderTemplateForRenderMode(renderMode));
  }

  @Override
  public boolean isVisible() {
    return pageTypeImpl.isVisible();
  }

  @Override
  public boolean isUnconnectedParent() {
    return pageTypeImpl.isUnconnectedParent();
  }
}
