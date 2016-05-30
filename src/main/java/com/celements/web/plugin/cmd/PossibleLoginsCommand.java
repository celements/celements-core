package com.celements.web.plugin.cmd;

import org.xwiki.context.Execution;

import com.celements.web.classcollections.OldCoreClasses;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;

public class PossibleLoginsCommand {

  public String getPossibleLogins() {
    String possibleLogins = getContext().getWiki().getXWikiPreference(
        OldCoreClasses.XWIKI_PREFERENCES_CELLOGIN_PROPERTY, "celements.login.userfields",
        "loginname", getContext());
    if ((possibleLogins == null) || "".equals(possibleLogins.trim())) {
      possibleLogins = "loginname";
    }
    return possibleLogins;
  }

  private XWikiContext getContext() {
    return (XWikiContext) getExecution().getContext().getProperty("xwikicontext");
  }

  private Execution getExecution() {
    return Utils.getComponent(Execution.class);
  }

}
