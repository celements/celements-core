package com.celements.css;

import java.util.List;

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

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch and
   * write the exception in a log-file
   */
  public List<CSS> getAllCSS() throws XWikiException {
    return getCssCmd().getAllCSS(getContext());
  }

  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch and
   * write the exception in a log-file
   */
  public String displayAllCSS() throws XWikiException {
    return getCssCmd().displayAllCSS(getContext());
  }

  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch and
   * write the exception in a log-file
   */
  public List<CSS> getRTEContentCSS() throws XWikiException {
    return getCssCmd().getRTEContentCSS(getContext());
  }

  public void includeCSSPage(String css) {
    getCssCmd().includeCSSPage(css, getContext());
  }

  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch and
   * write the exception in a log-file
   */
  public void includeCSSAfterPreferences(String css) throws XWikiException {
    getCssCmd().includeCSSAfterPreferences(css, getContext());
  }

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
