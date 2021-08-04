package com.celements.ajax;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiAction;

public class AjaxAction extends XWikiAction {

  private static final String CEL_AJAX_CONTEXT_PROPERTY = "celAjax";
  private static final String ACTION_SCRIPT_ACTION = "ajax_mode";

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
    List<String> pathParts = Stream.of(path.split("/"))
        .filter(((Predicate<String>) String::isEmpty).negate()).collect(Collectors.toList());
    if (pathParts.size() > 2) {
      String celAjaxScript = Joiner.on("/").join(pathParts);
      LOGGER.error("ajax: found script path '{}' from path '{}'.", celAjaxScript, path);
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
    String page = Utils.getPage(context.getRequest(), ACTION_SCRIPT_ACTION);
    LOGGER.error("Ajax: render page '{}'", page);
    Utils.parseTemplate(page, !page.equals("direct"), context);
    return null;
  }

}
