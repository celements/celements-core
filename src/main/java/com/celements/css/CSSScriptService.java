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
public class CSSScriptService implements ScriptService{
  
  public static final String CELEMENTS_CSSCOMMAND = "com.celements.web.CssCommand";
  
  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }
  
  public List<CSS> getAllCSS() throws XWikiException{
    return getCssCmd().getAllCSS(getContext());
  }
  
  public String displayAllCSS() throws XWikiException{
    return getCssCmd().displayAllCSS(getContext());
  }
  
  public List<CSS> getRTEContentCSS() throws XWikiException{
    return getCssCmd().getRTEContentCSS(getContext());
  }
  
  public void includeCSSPage(String css) {
    getCssCmd().includeCSSPage(css, getContext());
  }
  
  public void includeCSSAfterPreferences(String css) throws XWikiException{
    getCssCmd().includeCSSAfterPreferences(css, getContext());
  }
  
  public void includeCSSAfterSkin(String css){
    getCssCmd().includeCSSAfterSkin(css, getContext());
  }
  
  private CssCommand getCssCmd() {
    if (!getContext().containsKey(CELEMENTS_CSSCOMMAND)) {
      getContext().put(CELEMENTS_CSSCOMMAND, new CssCommand());
    }
    return (CssCommand) getContext().get(CELEMENTS_CSSCOMMAND);
  }
}
