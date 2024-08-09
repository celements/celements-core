package com.celements.css;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.script.service.ScriptService;

import com.celements.web.css.CSS;
import com.celements.web.plugin.cmd.CssCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component("css")
public class CssScriptService implements ScriptService {

  public static final String CELEMENTS_CSSCOMMAND = "com.celements.web.CssCommand";

  public static final Logger LOGGER = LoggerFactory.getLogger(CssScriptService.class);

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  public List<CSS> getAllCSS() {
    try {
      return getCssCmd().getAllCSS(getContext());
    } catch (XWikiException e) {
      LOGGER.error("Call to CssComman.getAllCss failed.", e);
      return List.of();
    }
  }

  public String displayAllCSS() {
    try {
      return getCssCmd().displayAllCSS(getContext());
    } catch (XWikiException e) {
      LOGGER.error("Call to CssCommand.displayAllCss failed.", e);
      return "";
    }
  }

  public List<CSS> getRTEContentCSS() {
    try {
      return getCssCmd().getRTEContentCSS(getContext());
    } catch (XWikiException e) {
      LOGGER.error("Call to CssCommand.getRTEContentCSS failed.", e);
      return List.of();
    }
  }

  public void includeCSSPage(String css) {
    getCssCmd().includeCSSPage(css, getContext());
  }

  public void includeCSSAfterPreferences(String css) {
    try {
      getCssCmd().includeCSSAfterPreferences(css, getContext());
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
    getCssCmd().includeCSSAfterSkin(css, getContext());
  }

  private CssCommand getCssCmd() {
    if (!getContext().containsKey(CELEMENTS_CSSCOMMAND)) {
      getContext().put(CELEMENTS_CSSCOMMAND, new CssCommand());
    }
    return (CssCommand) getContext().get(CELEMENTS_CSSCOMMAND);
  }
}
