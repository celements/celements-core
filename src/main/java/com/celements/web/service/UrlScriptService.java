package com.celements.web.service;

import static com.google.common.base.MoreObjects.*;

import javax.ws.rs.core.UriBuilder;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.script.service.ScriptService;

import com.celements.model.context.ModelContext;

@Component("url")
public class UrlScriptService implements ScriptService {

  @Requirement
  private UrlService urlService;

  @Requirement
  private ModelContext context;

  public String getURL() {
    return urlService.getURL(getCurrentReference());
  }

  public String getURL(EntityReference ref) {
    return urlService.getURL(firstNonNull(ref, getCurrentReference()));
  }

  public String getURL(EntityReference ref, String action) {
    return urlService.getURL(firstNonNull(ref, getCurrentReference()), action);
  }

  public String getURL(EntityReference ref, String action, String queryString) {
    return urlService.getURL(firstNonNull(ref, getCurrentReference()), action, queryString);
  }

  public String getExternalURL() {
    return urlService.getExternalURL(getCurrentReference());
  }

  public String getExternalURL(EntityReference ref) {
    return urlService.getExternalURL(firstNonNull(ref, getCurrentReference()));
  }

  public String getExternalURL(EntityReference ref, String action) {
    return urlService.getExternalURL(firstNonNull(ref, getCurrentReference()), action);
  }

  public String getExternalURL(EntityReference ref, String action, String queryString) {
    return urlService.getExternalURL(firstNonNull(ref, getCurrentReference()), action, queryString);
  }

  public UriBuilder createURIBuilder() {
    return urlService.createURIBuilder(getCurrentReference());
  }

  UriBuilder createURIBuilder(EntityReference ref) {
    return urlService.createURIBuilder(firstNonNull(ref, getCurrentReference()));
  }

  UriBuilder createURIBuilder(EntityReference ref, String action) {
    return urlService.createURIBuilder(firstNonNull(ref, getCurrentReference()), action);
  }

  private EntityReference getCurrentReference() {
    if (context.getDoc() != null) {
      return context.getDoc().getDocumentReference();
    } else {
      return context.getWikiRef();
    }
  }

}
