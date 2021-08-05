package com.celements.ajax;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

  private static final String CEL_AJAX_CONTEXT_PROPERTY = "celAjaxScript";
  private static final String CEL_AJAX_SCRIPT_DIR_PROPERTY = "celAjax";
  private static final String AJAX_SCRIPT_ACTION = "ajax";

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
    final Optional<String> celAjaxScriptOpt = getAjaxScript(context);
    if (celAjaxScriptOpt.isPresent()) {
      final String celAjaxScript = celAjaxScriptOpt.get();
      LOGGER.error("ajax: found script path '{}'.", celAjaxScript);
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
    String page = Utils.getPage(context.getRequest(), AJAX_SCRIPT_ACTION);
    final Optional<String> celAjaxScriptOpt = getAjaxScript(context);
    final String celAjaxScript = celAjaxScriptOpt.orElse(page);
    LOGGER.error("Ajax: render page '{}', script: '{}'", page, celAjaxScript);
    Utils.parseTemplate(celAjaxScript, !page.equals("direct"), context);
    return null;
  }

  private Optional<String> getAjaxScript(XWikiContext context) {
    String path = context.getRequest().getPathInfo();
    List<String> pathParts = Stream.of(path.split("/"))
        .filter(((Predicate<String>) String::isEmpty).negate()).collect(Collectors.toList());
    if ((pathParts.size() > 2) && pathParts.get(0).equals(AJAX_SCRIPT_ACTION)) {
      List<String> scriptPathList = new ArrayList<>();
      scriptPathList.add(CEL_AJAX_SCRIPT_DIR_PROPERTY);
      scriptPathList.addAll(pathParts.subList(1, pathParts.size()));
      String celAjaxScript = "/" + Joiner.on("/").join(scriptPathList);
      LOGGER.debug("ajax: found script path '{}' from path '{}'.", celAjaxScript, path);
      return Optional.of(celAjaxScript);
    }
    return Optional.empty();
  }

}
