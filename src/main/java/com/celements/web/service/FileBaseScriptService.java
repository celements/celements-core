package com.celements.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.web.plugin.cmd.TokenBasedUploadCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;

@Component("filebase")
public class FileBaseScriptService implements ScriptService{
  
  @Requirement
  IWebUtilsService webUtilsService;
  
  private static Logger _LOGGER  = LoggerFactory.getLogger(FileBaseScriptService.class);
  
  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }
  
  public int tokenBasedUpload(DocumentReference attachToDocRef, String fieldName, 
      String userToken) {
    return tokenBasedUpload(attachToDocRef, fieldName, userToken, false);
  }
  
  public int tokenBasedUpload(DocumentReference attachToDocRef, String fieldName, 
      String userToken, Boolean createIfNotExists) {
    try {
      return new TokenBasedUploadCommand().tokenBasedUpload(webUtilsService.
          getRefLocalSerializer().serialize(attachToDocRef), fieldName, userToken,
          createIfNotExists, getContext());
    } catch (XWikiException exp) {
      _LOGGER.error("token based attachment upload failed: ", exp);
    }
    return 0;
  }
  
//  private IWebUtilsService getWebUtilsService() {
//    return Utils.getComponent(IWebUtilsService.class);
//  }
}
