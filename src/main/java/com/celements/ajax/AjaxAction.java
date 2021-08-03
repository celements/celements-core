package com.celements.ajax;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.celements.appScript.IAppScriptService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiAction;

public class AjaxAction extends XWikiAction {

  private static final String CEL_AJAX_CONTEXT_PROPERTY = "celAjax";

  private static final Logger LOGGER = LoggerFactory.getLogger(AjaxAction.class);

  /**
   * The identifier of the view action.
   */
  public static final String VIEW_ACTION = "view";

  /**
   * {@inheritDoc}
   *
   * @see XWikiAction#action(com.xpn.xwiki.XWikiContext)
   */
  @Override
  public boolean action(XWikiContext context) throws XWikiException {
    boolean shouldRender = true;
    context.put("action", VIEW_ACTION);
    context.put("ajaxAction", true);
    String path = context.getRequest().getPathInfo();
    if (getAppScriptService().getStartIndex(path) > 0) {
      String celAjaxScript = getAppScriptService().getScriptNameFromURL();
      LOGGER.debug("ajax: found script path '{}'.", celAjaxScript);
      context.put(CEL_AJAX_CONTEXT_PROPERTY, celAjaxScript);
      VelocityContext vcontext = (VelocityContext) context.get("vcontext");
      vcontext.put(CEL_AJAX_CONTEXT_PROPERTY, celAjaxScript);
    }
    return shouldRender;
  }

  /**
   * {@inheritDoc}
   *
   * @see XWikiAction#render(com.xpn.xwiki.XWikiContext)
   */
  @Override
  public String render(XWikiContext context) throws XWikiException {
    String page = Utils.getPage(context.getRequest(), IAppScriptService.APP_SCRIPT_XPAGE);
    Utils.parseTemplate(page, !page.equals("direct"), context);
    return null;
  }

  private IAppScriptService getAppScriptService() {
    return Utils.getComponent(IAppScriptService.class);
  }

}
