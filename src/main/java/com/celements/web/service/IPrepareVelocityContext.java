package com.celements.web.service;

import java.util.List;

import org.apache.velocity.VelocityContext;
import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.XWikiContext;

@ComponentRole
public interface IPrepareVelocityContext {

  public void prepareVelocityContext(VelocityContext vcontext);
  
  public static final String CEL_SUPPRESS_INVALID_LANG = "celSuppressInvalidLang";

  public static final String CFG_LANGUAGE_SUPPRESS_INVALID =
    "celements.language.suppressInvalid";

  public static final String ADD_LANGUAGE_COOKIE_DONE =
    "celements.addLanguageCookie.done";

  /**
   * @param context
   * 
   * @deprecated instead use prepareVelocityContext(VelocityContext)
   */
  @Deprecated
  public void prepareVelocityContext(XWikiContext context);

  public String getVelocityName();

  public int showRightPanels();

  public int showLeftPanels();

  public List<String> getRightPanels();

  public List<String> getLeftPanels();

}
