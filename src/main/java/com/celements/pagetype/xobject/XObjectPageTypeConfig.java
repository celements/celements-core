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
import org.xwiki.context.Execution;

import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.PageType;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.web.Utils;

public class XObjectPageTypeConfig implements IPageTypeConfig {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      XObjectPageTypeConfig.class);

  PageType pageType;

  private XWikiContext getContext() {
    return (XWikiContext) getExecution().getContext().getProperty("xwikicontext");
  }

  private Execution getExecution() {
    return Utils.getComponent(Execution.class);
  }

  public XObjectPageTypeConfig(String pageTypeFN) {
    pageType = new PageType(pageTypeFN);
  }

  public boolean displayInFrameLayout() {
    return pageType.showFrame(getContext());
  }

  public List<String> getCategories() {
    List<String> categories = pageType.getCategories(getContext());
    if (categories.isEmpty()) {
      LOGGER.debug("getCategories for [" + getName() + "] empty List found returning"
          + " [\"\"].");
      return Arrays.asList("");
    } else {
      LOGGER.debug("getCategories for [" + getName() + "] returning ["
          + Arrays.deepToString(categories.toArray()) + "]");
      return categories;
    }
  }

  public String getName() {
    return pageType.getConfigName(getContext());
  }

  public String getPrettyName() {
    return pageType.getPrettyName(getContext());
  }

  public boolean hasPageTitle() {
    return pageType.hasPageTitle(getContext());
  }

  public String getRenderTemplateForRenderMode(String renderMode) {
    try {
      return pageType.getRenderTemplate(renderMode, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get render template for pageType ["
          + pageType.getConfigName(getContext()) + "] and renderMode [" + renderMode
          + "].", exp);
    }
    return null;
  }

  public boolean isVisible() {
    return (pageType.getPageTypeProperties(getContext()).getIntValue("visible", 0) > 0);
  }

}
