package com.celements.captcha;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.script.service.ScriptService;

import com.celements.web.plugin.cmd.CaptchaCommand;
import com.xpn.xwiki.XWikiContext;

@Component("captcha")
public class CaptchaScriptService implements ScriptService {

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
  }

  public boolean checkCaptcha() {
    return new CaptchaCommand().checkCaptcha(getContext());
  }

  public String getCaptchaId() {
    return new CaptchaCommand().getCaptchaId(getContext());
  }
}
