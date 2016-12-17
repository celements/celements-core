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
package com.celements.pagetype.xobject;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.python.google.common.base.Strings;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.cells.attribute.AttributeBuilder;
import com.celements.pagetype.IPageTypeClassConfig;
import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.PageType;
import com.google.common.base.Optional;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class XObjectPageTypeConfig implements IPageTypeConfig {

  private static Log LOGGER = LogFactory.getFactory().getInstance(XObjectPageTypeConfig.class);

  PageType pageType;

  private XWikiContext getContext() {
    return (XWikiContext) getExecution().getContext().getProperty("xwikicontext");
  }

  private Execution getExecution() {
    return Utils.getComponent(Execution.class);
  }

  /**
   * @deprecated instead use XObjectPageTypeConfig(DocumentReference)
   */
  @Deprecated
  public XObjectPageTypeConfig(String pageTypeFN) {
    pageType = new PageType(pageTypeFN);
  }

  public XObjectPageTypeConfig(DocumentReference pageTypeDocRef) {
    pageType = new PageType(pageTypeDocRef);
  }

  @Override
  public boolean displayInFrameLayout() {
    return pageType.showFrame(getContext());
  }

  @Override
  public List<String> getCategories() {
    List<String> categories = pageType.getCategories(getContext());
    if (categories.isEmpty()) {
      LOGGER.debug("getCategories for [" + getName() + "] empty List found returning" + " [\"\"].");
      return Arrays.asList("");
    } else {
      LOGGER.debug("getCategories for [" + getName() + "] returning [" + Arrays.deepToString(
          categories.toArray()) + "]");
      return categories;
    }
  }

  @Override
  public String getName() {
    return pageType.getConfigName(getContext());
  }

  @Override
  public String getPrettyName() {
    return pageType.getPrettyName(getContext());
  }

  @Override
  public boolean hasPageTitle() {
    return pageType.hasPageTitle(getContext());
  }

  @Override
  public String getRenderTemplateForRenderMode(String renderMode) {
    try {
      return pageType.getRenderTemplate(renderMode, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get render template for pageType [" + pageType.getConfigName(
          getContext()) + "] and renderMode [" + renderMode + "].", exp);
    }
    return null;
  }

  @Override
  public boolean isVisible() {
    BaseObject pageTypePropertiesObj = pageType.getPageTypeProperties(getContext());
    if (pageTypePropertiesObj != null) {
      return (pageTypePropertiesObj.getIntValue(IPageTypeClassConfig.PAGETYPE_PROP_VISIBLE, 0) > 0);
    }
    return false;
  }

  @Override
  public boolean isUnconnectedParent() {
    BaseObject pageTypePropertiesObj = pageType.getPageTypeProperties(getContext());
    if (pageTypePropertiesObj != null) {
      return (pageTypePropertiesObj.getIntValue(
          IPageTypeClassConfig.PAGETYPE_PROP_IS_UNCONNECTED_PARENT, 0) > 0);
    }
    return false;
  }

  @Override
  public boolean useInlineEditorMode() {
    BaseObject pageTypePropertiesObj = pageType.getPageTypeProperties(getContext());
    if (pageTypePropertiesObj != null) {
      return (pageTypePropertiesObj.getIntValue(
          IPageTypeClassConfig.PAGETYPE_PROP_INLINE_EDITOR_MODE, 0) > 0);
    }
    return false;
  }

  @Override
  public Optional<String> defaultTagName() {
    BaseObject pageTypePropertiesObj = pageType.getPageTypeProperties(getContext());
    if (pageTypePropertiesObj != null) {
      return Optional.fromNullable(Strings.emptyToNull(pageTypePropertiesObj.getStringValue(
          IPageTypeClassConfig.PAGETYPE_PROP_TAG_NAME)));
    }
    return Optional.absent();
  }

  @Override
  public void collectAttributes(AttributeBuilder attrBuilder, DocumentReference cellDocRef) {
    // TODO CELDEV-344 : get attributes from pageTypeProperties
  }

}
