package com.celements.ajax;

import static com.google.common.base.Predicates.*;
import static com.google.common.collect.ImmutableList.*;
import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
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
    getAjaxScript(context).ifPresent(celAjaxScript -> {
      context.put(CEL_AJAX_CONTEXT_PROPERTY, celAjaxScript);
      VelocityContext vcontext = (VelocityContext) context.get("vcontext");
      vcontext.put(CEL_AJAX_CONTEXT_PROPERTY, celAjaxScript);
    });
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
    final String celAjaxScript = getAjaxScript(context).orElse(page);
    LOGGER.info("Ajax: render page '{}', script: '{}'", page, celAjaxScript);
    Utils.parseTemplate(celAjaxScript, !page.equals("direct"), context);
    return null;
  }

  private Optional<String> getAjaxScript(XWikiContext context) {
    List<String> pathParts = parseRequestPath(context)
        .limit(3).collect(toImmutableList());
    if ((pathParts.size() > 2) && pathParts.get(0).equals(AJAX_SCRIPT_ACTION)) {
      String celAjaxScript = "/" + Stream.concat(
          Stream.of(CEL_AJAX_SCRIPT_DIR_PROPERTY),
          parseRequestPath(context).skip(1))
          .collect(joining("/"));
      LOGGER.debug("ajax: found script path '{}'", celAjaxScript);
      return Optional.of(celAjaxScript);
    }
    return Optional.empty();
  }

  private Stream<String> parseRequestPath(XWikiContext context) {
    return Splitter.on('/')
        .splitToStream(context.getRequest().getPathInfo())
        .filter(not(String::isEmpty));
  }

}
