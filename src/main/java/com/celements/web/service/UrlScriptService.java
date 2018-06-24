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

  public String getURL(EntityReference ref) {
    return (ref != null) ? urlService.getURL(ref) : "";
  }

  public String getURL(EntityReference ref, String action) {
    return (ref != null) ? urlService.getURL(ref, action) : "";
  }

  public String getURL(EntityReference ref, String action, String queryString) {
    return (ref != null) ? urlService.getURL(ref, action, queryString) : "";
  }

  public String getExternalURL(EntityReference ref) {
    return (ref != null) ? urlService.getExternalURL(ref) : "";
  }

  public String getExternalURL(EntityReference ref, String action) {
    return (ref != null) ? urlService.getExternalURL(ref, action) : "";
  }

  public String getExternalURL(EntityReference ref, String action, String queryString) {
    return (ref != null) ? urlService.getExternalURL(ref, action, queryString) : "";
  }

  UriBuilder createURIBuilder(EntityReference ref) {
    return (ref != null) ? urlService.createURIBuilder(ref) : null;
  }

  UriBuilder createURIBuilder(EntityReference ref, String action) {
    return (ref != null) ? urlService.createURIBuilder(ref, action) : null;
  }

}
