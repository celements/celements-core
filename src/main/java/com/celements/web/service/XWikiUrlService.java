package com.celements.web.service;

import static com.google.common.base.MoreObjects.*;
import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Strings.*;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.ws.rs.core.UriBuilder;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

import com.celements.model.context.ModelContext;
import com.xpn.xwiki.web.XWikiURLFactory;

@Component
public class XWikiUrlService implements UrlService {

  @Requirement
  private ModelContext context;

  @Override
  public String getURL(EntityReference ref) {
    return getURL(ref, null);
  }

  @Override
  public String getURL(EntityReference ref, String action) {
    return getURL(ref, action, null);
  }

  @Override
  public String getURL(EntityReference ref, String action, String queryString) {
    checkArgument(ref != null, "reference may not be null");
    checkArgument(ref.extractReference(EntityType.SPACE) != null,
        "reference [%s] must contain space for relative url", ref);
    return createURLObject(ref, action, queryString).getFile();
  }

  @Override
  public String getExternalURL(EntityReference ref) {
    return getExternalURL(ref, null);
  }

  @Override
  public String getExternalURL(EntityReference ref, String action) {
    return getExternalURL(ref, action, null);
  }

  @Override
  public String getExternalURL(EntityReference ref, String action, String queryString) {
    checkArgument(ref != null, "reference may not be null");
    return createURLObject(ref, action, queryString).toExternalForm();
  }

  @Override
  public UriBuilder createURIBuilder(EntityReference ref) {
    return createURIBuilder(ref, null);
  }

  @Override
  public UriBuilder createURIBuilder(EntityReference ref, String action) {
    checkArgument(ref != null, "reference may not be null");
    try {
      return UriBuilder.fromUri(createURLObject(ref, action, null).toURI());
    } catch (URISyntaxException exc) {
      throw new IllegalArgumentException("illegal reference provided: " + ref, exc);
    }
  }

  private URL createURLObject(EntityReference ref, String action, String queryString) {
    URL url;
    String wikiName = extractName(ref, EntityType.WIKI);
    checkArgument(!wikiName.isEmpty(), "reference [%s] must contain wiki", ref);
    String spaceName = extractName(ref, EntityType.SPACE);
    String docName = extractName(ref, EntityType.DOCUMENT);
    String fileName = extractName(ref, EntityType.ATTACHMENT);
    if (spaceName.isEmpty()) {
      url = createWikiURL(wikiName, queryString);
    } else if (fileName.isEmpty()) {
      action = firstNonNull(emptyToNull(action), "view");
      url = getUrlFactory().createURL(spaceName, docName, action, queryString, null, wikiName,
          context.getXWikiContext());
    } else {
      action = firstNonNull(emptyToNull(action), "download");
      url = getUrlFactory().createAttachmentURL(fileName, spaceName, docName, action, queryString,
          wikiName, context.getXWikiContext());
    }
    return url;
  }

  private String extractName(EntityReference ref, EntityType type) {
    EntityReference extractedRef = ref.extractReference(type);
    return (extractedRef != null) ? extractedRef.getName() : "";
  }

  private URL createWikiURL(String wikiName, String queryString) {
    try {
      URL url = context.getXWikiContext().getWiki().getServerURL(wikiName,
          context.getXWikiContext());
      checkArgument(url != null, "wiki [%s] does not exist", wikiName);
      UriBuilder builder = UriBuilder.fromUri(url.toURI());
      builder.replaceQuery(queryString);
      return builder.build().toURL();
    } catch (MalformedURLException | URISyntaxException exc) {
      throw new IllegalArgumentException("illegal wiki name provided: " + wikiName, exc);
    }
  }

  private XWikiURLFactory getUrlFactory() {
    return context.getXWikiContext().getURLFactory();
  }

}
