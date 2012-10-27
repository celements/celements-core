package com.celements.pagetype.xobject;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.context.Execution;

import com.celements.pagetype.IPageTypeConfig;
import com.celements.pagetype.PageType;
import com.celements.pagetype.cmd.PageTypeCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.web.Utils;

public class XObjectPageTypeConfig implements IPageTypeConfig {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      XObjectPageTypeConfig.class);

  PageType pageType;
  PageTypeCommand pageTypeCmd = new PageTypeCommand();

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
    return pageType.getCategories(getContext());
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
    try {
      return pageTypeCmd.isVisible(pageType.getTemplateDocument(getContext()),
          getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get isVisible for pageType [" + pageType.getConfigName(
          getContext()) + "].", exp);
    }
    return false;
  }

}
