package com.celements.pagetype.java;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.pagetype.IPageTypeConfig;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Optional;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

/**
 * DefaultPageTypeConfig may be exposed to non privileged code (e.g. scripts)
 */
public class DefaultPageTypeConfig implements IPageTypeConfig {

  public static final String PRETTYNAME_DICT_PREFIX = "cel_pagetype_prettyname_";

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
    String dictNameKey = PRETTYNAME_DICT_PREFIX + getName();
    String dictionaryPrettyName = getWebUtilsService().getAdminMessageTool().get(dictNameKey);
    if (!dictNameKey.equals(dictionaryPrettyName)) {
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
    return new ArrayList<>(pageTypeImpl.getCategoryNames());
  }

  @Override
  public String getRenderTemplateForRenderMode(String renderMode) {
    DocumentReference localTemplateRef = getLocalTemplateRef(renderMode);
    return getWebUtilsService().getInheritedTemplatedPath(localTemplateRef);
  }

  private DocumentReference getLocalTemplateRef(String renderMode) {
    return new DocumentReference(getContext().getDatabase(), "Templates",
        pageTypeImpl.getRenderTemplateForRenderMode(renderMode));
  }

  @Override
  public boolean isVisible() {
    return pageTypeImpl.isVisible();
  }

  @Override
  public boolean isUnconnectedParent() {
    return pageTypeImpl.isUnconnectedParent();
  }

  @Override
  public boolean useInlineEditorMode() {
    return pageTypeImpl.useInlineEditorMode();
  }

  @Override
  public Optional<String> defaultTagName() {
    return pageTypeImpl.defaultTagName();
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    pageTypeImpl.collectAttributes(attrBuilder, cellDocRef);
  }

}
