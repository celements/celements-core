/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.pagetype.java;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.model.context.ModelContext;
import com.celements.model.util.References;
import com.celements.pagetype.IPageTypeConfig;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.xpn.xwiki.web.Utils;

/**
 * DefaultPageTypeConfig may be exposed to non privileged code (e.g. scripts)
 */
public class DefaultPageTypeConfig implements IPageTypeConfig {

  public static final String PRETTYNAME_DICT_PREFIX = "cel_pagetype_prettyname_";

  private IJavaPageTypeRole pageTypeImpl;

  private ModelContext getContext() {
    return Utils.getComponent(ModelContext.class);
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
    return Strings.nullToEmpty(getWebUtilsService().getInheritedTemplatedPath(localTemplateRef));
  }

  private DocumentReference getLocalTemplateRef(String renderMode) {
    DocumentReference localTemplateRef = null;
    String templateName = pageTypeImpl.getRenderTemplateForRenderMode(renderMode);
    if (!Strings.isNullOrEmpty(templateName)) {
      localTemplateRef = References.create(DocumentReference.class, templateName,
          getTemplateSpaceRef());
    }
    return localTemplateRef;
  }

  private SpaceReference getTemplateSpaceRef() {
    return References.create(SpaceReference.class, TEMPLATE_SPACE_NAME, getContext().getWikiRef());
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
