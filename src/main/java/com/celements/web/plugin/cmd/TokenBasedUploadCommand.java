package com.celements.web.plugin.cmd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.context.Execution;

import com.celements.web.token.TokenLDAPAuthServiceImpl;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class TokenBasedUploadCommand {

  private static final Log LOGGER = LogFactory.getLog(TokenBasedUploadCommand.class);

  private TokenLDAPAuthServiceImpl tokenAuthImpl = new TokenLDAPAuthServiceImpl();

  @Deprecated
  public int tokenBasedUpload(Document attachToDoc, String fieldName, String userToken
      ) throws XWikiException {
    String username = tokenAuthImpl.getUsernameForToken(userToken, getContext());
    if((username != null) && !username.equals("")){
      LOGGER.info("tokenBasedUpload: user " + username + " identified by userToken.");
      getContext().setUser(username);
      return attachToDoc.addAttachments(fieldName);
    } else {
      LOGGER.warn("tokenBasedUpload: username could not be identified by token");
    }
    return 0;
  }
  
  public int tokenBasedUpload(String attachToDocFN, String fieldName, String userToken, 
      Boolean createIfNotExists) throws XWikiException {
    String username = tokenAuthImpl.getUsernameForToken(userToken, getContext());
    if((username != null) && !username.equals("")){
      LOGGER.info("tokenBasedUpload: user " + username + " identified by userToken.");
      getContext().setUser(username);
      XWikiDocument doc = getContext().getWiki().getDocument(attachToDocFN, getContext());
      if (createIfNotExists || getContext().getWiki().exists(attachToDocFN,
          getContext())) {
        LOGGER.info("tokenBasedUpload: add attachment [" + fieldName + "] to doc ["
            + attachToDocFN + "].");
        return doc.newDocument(getContext()).addAttachments(fieldName);
      } else {
        LOGGER.warn("tokenBasedUpload: document " + attachToDocFN + " does not exist.");
      }
    } else {
      LOGGER.warn("tokenBasedUpload: username could not be identified by token");
    }
    return 0;
  }

  private Execution getExecution() {
    return Utils.getComponent(Execution.class);
  }

  private XWikiContext getContext() {
    return (XWikiContext)getExecution().getContext().getProperty("xwikicontext");
  }

}
