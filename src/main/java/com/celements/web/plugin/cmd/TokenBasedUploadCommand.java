package com.celements.web.plugin.cmd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.celements.web.service.IAttachmentServiceRole;
import com.celements.web.token.TokenLDAPAuthServiceImpl;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;

public class TokenBasedUploadCommand {

  private static final Log LOGGER = LogFactory.getLog(TokenBasedUploadCommand.class);

  private TokenLDAPAuthServiceImpl tokenAuthImpl = new TokenLDAPAuthServiceImpl();

  private IAttachmentServiceRole getAttService() {
    return Utils.getComponent(IAttachmentServiceRole.class);
  }

  @Deprecated
  public int tokenBasedUpload(Document attachToDoc, String fieldName, String userToken,
      XWikiContext context) throws XWikiException {
    String username = tokenAuthImpl.getUsernameForToken(userToken, context);
    if((username != null) && !username.equals("")){
      LOGGER.info("tokenBasedUpload: user " + username + " identified by userToken.");
      context.setUser(username);
      return attachToDoc.addAttachments(fieldName);
    } else {
      LOGGER.warn("tokenBasedUpload: username could not be identified by token");
    }
    return 0;
  }
  
  public int tokenBasedUpload(String attachToDocFN, String fieldNamePrefix,
      String userToken,  Boolean createIfNotExists, XWikiContext context
      ) throws XWikiException {
    String username = tokenAuthImpl.getUsernameForToken(userToken, context);
    if((username != null) && !username.equals("")){
      LOGGER.info("tokenBasedUpload: user " + username + " identified by userToken.");
      context.setUser(username);
      XWikiDocument doc = context.getWiki().getDocument(attachToDocFN, context);
      if (createIfNotExists || context.getWiki().exists(attachToDocFN,
          context)) {
        LOGGER.info("tokenBasedUpload: add attachment [" + fieldNamePrefix + "] to doc ["
            + attachToDocFN + "].");
        if (LOGGER.isTraceEnabled()) {
          for (XWikiAttachment origAttach : doc.getAttachmentList()) {
            LOGGER.trace("tokenBasedUpload - origialDoc before addAttachments: "
                + origAttach.getFilename() + ", " + origAttach.getVersion());
          }
        }
        return getAttService().uploadMultipleAttachments(doc, fieldNamePrefix);
      } else {
        LOGGER.warn("tokenBasedUpload: document " + attachToDocFN + " does not exist.");
      }
    } else {
      LOGGER.warn("tokenBasedUpload: username could not be identified by token");
    }
    return 0;
  }

}
