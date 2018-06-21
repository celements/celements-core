package com.celements.web.service;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

@Component("url")
public class UrlScriptService implements ScriptService {

  @Requirement
  private UrlService urlService;

  public String getURL(DocumentReference docRef) {
    return urlService.getURL(docRef);
  }

  public String getURL(DocumentReference docRef, String action) {
    return urlService.getURL(docRef, action);
  }

  public String getURL(DocumentReference docRef, String action, String queryString) {
    return urlService.getURL(docRef, action, queryString);
  }

  public String getExternalURL(DocumentReference docRef) {
    return urlService.getExternalURL(docRef);
  }

  public String getExternalURL(DocumentReference docRef, String action) {
    return urlService.getExternalURL(docRef, action);
  }

  public String getExternalURL(DocumentReference docRef, String action, String queryString) {
    return urlService.getExternalURL(docRef, action, queryString);
  }

  public String getURL(AttachmentReference attRef) {
    return urlService.getURL(attRef);
  }

  public String getURL(AttachmentReference attrRef, String action) {
    return urlService.getURL(attrRef, action);
  }

  public String getURL(AttachmentReference attrRef, String action, String queryString) {
    return urlService.getURL(attrRef, action, queryString);
  }

  public String getExternalURL(AttachmentReference attRef) {
    return urlService.getExternalURL(attRef);
  }

  public String getExternalURL(AttachmentReference attrRef, String action) {
    return urlService.getExternalURL(attrRef, action);
  }

  public String getExternalURL(AttachmentReference attrRef, String action, String queryString) {
    return urlService.getExternalURL(attrRef, action, queryString);
  }

}
