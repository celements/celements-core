package com.celements.web.service;

import static com.google.common.base.MoreObjects.*;

import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.script.service.ScriptService;

import com.celements.model.context.ModelContext;

@Component("url")
public class UrlScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UrlScriptService.class);

  @Requirement
  private UrlService urlService;

  @Requirement
  private ModelContext context;

  public String getURL() {
    return getURL(null);
  }

  public String getURL(EntityReference ref) {
    return getURL(ref, null);
  }

  public String getURL(EntityReference ref, String action) {
    return getURL(ref, action, null);
  }

  public String getURL(EntityReference ref, String action, String queryString) {
    try {
      return urlService.getURL(firstNonNull(ref, getCurrentReference()), action, queryString);
    } catch (Exception iae) {
      LOGGER.debug("getURL - failed for [{}], [{}], [{}]", ref, action, queryString);
      return null;
    }
  }

  public String getExternalURL() {
    return getExternalURL(null);
  }

  public String getExternalURL(EntityReference ref) {
    return getExternalURL(ref, null);
  }

  public String getExternalURL(EntityReference ref, String action) {
    return getExternalURL(ref, action, null);
  }

  public String getExternalURL(EntityReference ref, String action, String queryString) {
    try {
      return urlService.getExternalURL(firstNonNull(ref, getCurrentReference()), action,
          queryString);
    } catch (Exception iae) {
      LOGGER.debug("getExternalURL - failed for [{}], [{}], [{}]", ref, action, queryString);
      return null;
    }
  }

  public UriBuilder createURIBuilder() {
    return createURIBuilder(null);
  }

  UriBuilder createURIBuilder(EntityReference ref) {
    return createURIBuilder(ref, null);
  }

  UriBuilder createURIBuilder(EntityReference ref, String action) {
    try {
      return urlService.createURIBuilder(firstNonNull(ref, getCurrentReference()), action);
    } catch (Exception iae) {
      LOGGER.debug("createURIBuilder - failed for [{}], [{}]", ref, action);
      return null;
    }
  }

  private EntityReference getCurrentReference() {
    return context.getDocRef()
        .map(EntityReference.class::cast)
        .orElseGet(context::getWikiRef);
  }

}
