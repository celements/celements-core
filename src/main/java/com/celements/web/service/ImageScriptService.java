package com.celements.web.service;

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;

import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.api.Attachment;

@Component("image")
public class ImageScriptService {
  
  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }
  
  public List<Attachment> getRandomImages(String fullName,
      int num) throws ClassNotFoundException{
    return WebUtils.getInstance().getRandomImages(fullName, num, getContext());
  }
  
  public boolean useImageAnimations() {
    return "1".equals(getContext().getWiki().getSpacePreference("celImageAnimation",
        "celements.celImageAnimation", "0", getContext()));
  }
}
