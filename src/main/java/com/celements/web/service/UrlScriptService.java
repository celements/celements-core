package com.celements.web.service;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.model.context.ModelContext;
import com.xpn.xwiki.api.Attachment;

@Component("url")
public class UrlScriptService implements ScriptService {

  private static Logger LOGGER = LoggerFactory.getLogger(UrlScriptService.class);

  @Requirement
  private ModelContext context;

  public String getExternalAttachmentURL(Attachment attr, String action) {
    return getExternalAttachmentURL(attr, action, null);
  }

  public String getExternalAttachmentURL(Attachment attr, String action, String queryString) {
    DocumentReference attrDocRef = attr.getDocument().getDocumentReference();
    URL url = context.getXWikiContext().getURLFactory().createAttachmentURL(attr.getFilename(),
        attrDocRef.getLastSpaceReference().getName(), attrDocRef.getName(), action, queryString,
        context.getXWikiContext().getDatabase(), context.getXWikiContext());
    return url.toString();
  }

}
