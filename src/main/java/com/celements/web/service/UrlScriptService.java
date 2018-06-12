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

  @Requirement
  private ICelementsWebServiceRole celementsweb;

  public String getURL(DocumentReference docRef, String action, String queryString) {
    URL url = context.getXWikiContext().getURLFactory().createURL(
        docRef.getLastSpaceReference().getName(), docRef.getName(), action, queryString, null,
        docRef.getRoot().getName(), context.getXWikiContext());
    return celementsweb.encodeUrlToUtf8(context.getXWikiContext().getURLFactory().getURL(url,
        context.getXWikiContext()));
  }

  public String getExternalAttachmentURL(Attachment attr, String action) {
    return getExternalAttachmentURL(attr, action, null);
  }

  public String getExternalAttachmentURL(Attachment attr, String action, String queryString) {
    DocumentReference attrDocRef = attr.getDocument().getDocumentReference();
    URL url = context.getXWikiContext().getURLFactory().createAttachmentURL(attr.getFilename(),
        attrDocRef.getLastSpaceReference().getName(), attrDocRef.getName(), action, queryString,
        attrDocRef.getRoot().getName(), context.getXWikiContext());
    return celementsweb.encodeUrlToUtf8(url.toString());
  }

}
