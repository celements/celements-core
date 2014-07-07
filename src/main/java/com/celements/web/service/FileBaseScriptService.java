package com.celements.web.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.script.service.ScriptService;

import com.celements.web.plugin.cmd.TokenBasedUploadCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component("filebase")
public class FileBaseScriptService implements ScriptService{
  
  private static Log LOGGER = LogFactory.getFactory().getInstance(
      FileBaseScriptService.class);
  
  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }
  
  public int tokenBasedUpload(String attachToDocFN, String fieldName, String userToken) {
    return tokenBasedUpload(attachToDocFN, fieldName, userToken, false);
  }
  
  public int tokenBasedUpload(String attachToDocFN, String fieldName, String userToken,
      Boolean createIfNotExists) {
    try {
      return new TokenBasedUploadCommand().tokenBasedUpload(attachToDocFN, fieldName, userToken,
          createIfNotExists, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("token based attachment upload failed: ", exp);
    }
    return 0;
  }
}
