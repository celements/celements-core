package com.celements.css;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.script.service.ScriptService;

import com.celements.execution.XWikiExecutionProp;
import com.celements.web.css.CSS;
import com.celements.web.plugin.cmd.CssCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component("css")
public class CssScriptService implements ScriptService {

  public static final Logger LOGGER = LoggerFactory.getLogger(CssScriptService.class);

  @Inject
  private Execution execution;

  @Inject
  private CssCommand cssCommand;

  private XWikiContext getContext() {
    return execution.getContext().get(XWikiExecutionProp.XWIKI_CONTEXT).orElseThrow();
  }

  public List<CSS> getAllCSS() {
    try {
      return cssCommand.getAllCSS(getContext());
    } catch (XWikiException e) {
      LOGGER.error("Call to CssComman.getAllCss failed.", e);
      return List.of();
    }
  }

  public String displayAllCSS() {
    try {
      return cssCommand.displayAllCSS(getContext());
    } catch (XWikiException e) {
      LOGGER.error("Call to CssCommand.displayAllCss failed.", e);
      return "";
    }
  }

  public List<CSS> getRTEContentCSS() {
    try {
      return cssCommand.getRTEContentCSS(getContext());
    } catch (XWikiException e) {
      LOGGER.error("Call to CssCommand.getRTEContentCSS failed.", e);
      return List.of();
    }
  }

  public void includeCSSPage(String css) {
    cssCommand.includeCSSPage(css, getContext());
  }

  public void includeCSSAfterPreferences(String css) {
    try {
      cssCommand.includeCSSAfterPreferences(css, getContext());
    } catch (XWikiException e) {
      LOGGER.error("Call to CssCommand.includeCSSAfterPreferences failed.", e);
    }
  }

  /**
   * @param css
   * @param context
   * @return
   * @deprecated dropped because skin support was dropped in 6.0
   */
  @Deprecated(since = "6.7", forRemoval = true)
  public void includeCSSAfterSkin(String css) {
    cssCommand.includeCSSAfterSkin(css, getContext());
  }
}
