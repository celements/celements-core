package com.celements.web.service;

import javax.ws.rs.core.UriBuilder;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.script.service.ScriptService;

@Component("url")
public class UrlScriptService implements ScriptService {

  @Requirement
  private UrlService urlService;

  public String getURL() {
    return urlService.getURL();
  }

  public String getURL(EntityReference ref) {
    return urlService.getURL(ref);
  }

  public String getURL(EntityReference ref, String action) {
    return urlService.getURL(ref, action);
  }

  public String getURL(EntityReference ref, String action, String queryString) {
    return urlService.getURL(ref, action, queryString);
  }

  public String getExternalURL() {
    return urlService.getExternalURL();
  }

  public String getExternalURL(EntityReference ref) {
    return urlService.getExternalURL(ref);
  }

  public String getExternalURL(EntityReference ref, String action) {
    return urlService.getExternalURL(ref, action);
  }

  public String getExternalURL(EntityReference ref, String action, String queryString) {
    return urlService.getExternalURL(ref, action, queryString);
  }

  UriBuilder createURIBuilder(EntityReference ref) {
    return urlService.createURIBuilder(ref);
  }

  UriBuilder createURIBuilder(EntityReference ref, String action) {
    return urlService.createURIBuilder(ref, action);
  }

}
