package com.celements.web.service;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.google.common.base.Strings;
import com.xpn.xwiki.web.XWikiURLFactory;

@Component
public class XWikiUrlService implements UrlService {

  @Requirement
  private ModelContext context;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Override
  public String getURL(DocumentReference docRef) {
    return getURL(docRef, "view");
  }

  @Override
  public String getURL(DocumentReference docRef, String action) {
    return getURL(docRef, action, null);
  }

  @Override
  public String getURL(DocumentReference docRef, String action, String queryString) {
    return toInternalString(createURLObject(docRef, action, queryString));
  }

  @Override
  public String getURL(AttachmentReference attRef) {
    return getURL(attRef, "view");
  }

  @Override
  public String getURL(AttachmentReference attRef, String action) {
    return getURL(attRef, action, null);
  }

  @Override
  public String getURL(AttachmentReference attRef, String action, String queryString) {
    return toInternalString(createURLObject(attRef, action, queryString));
  }

  @Override
  public String getExternalURL(DocumentReference docRef) {
    return getExternalURL(docRef, "view");
  }

  @Override
  public String getExternalURL(DocumentReference docRef, String action) {
    return getExternalURL(docRef, action, null);
  }

  @Override
  public String getExternalURL(DocumentReference docRef, String action, String queryString) {
    return toExternalString(createURLObject(docRef, action, queryString));
  }

  @Override
  public String getExternalURL(AttachmentReference attRef) {
    return getExternalURL(attRef, "download");
  }

  @Override
  public String getExternalURL(AttachmentReference attrRef, String action) {
    return getExternalURL(attrRef, action, null);
  }

  @Override
  public String getExternalURL(AttachmentReference attRef, String action, String queryString) {
    return toExternalString(createURLObject(attRef, action, queryString));
  }

  private XWikiURLFactory getUrlFactory() {
    return context.getXWikiContext().getURLFactory();
  }

  private URL createURLObject(DocumentReference docRef, String action, String queryString) {
    if (docRef != null) {
      String spaceName = docRef.getLastSpaceReference().getName();
      String docName = docRef.getName();
      String wikiName = docRef.getWikiReference().getName();
      return getUrlFactory().createURL(spaceName, docName, action, queryString, null, wikiName,
          context.getXWikiContext());
    }
    return null;
  }

  private URL createURLObject(AttachmentReference attRef, String action, String queryString) {
    if (attRef != null) {
      DocumentReference attrDocRef = attRef.getDocumentReference();
      String fileName = attRef.getName();
      String spaceName = attrDocRef.getLastSpaceReference().getName();
      String docName = attrDocRef.getName();
      String wikiName = attrDocRef.getWikiReference().getName();
      return getUrlFactory().createAttachmentURL(fileName, spaceName, docName, action, queryString,
          wikiName, context.getXWikiContext());
    }
    return null;
  }

  private String toInternalString(URL url) {
    return getUrlFactory().getURL(url, context.getXWikiContext());
  }

  private String toExternalString(URL url) {
    return Objects.toString(url, "");
  }

  @Override
  public String encodeUrl(String url) {
    return encodeUrl(url, StandardCharsets.UTF_8);
  }

  @Override
  public String encodeUrl(String url, Charset encoding) {
    String scheme = "";
    int schemeIndex = Strings.nullToEmpty(url).indexOf("://");
    if (schemeIndex > 0) {
      scheme = url.substring(0, schemeIndex + 3);
      url = url.substring(schemeIndex + 3);
    }
    try {
      return scheme + URLEncoder.encode(url, encoding.name()).replaceAll("%2F", "/");
    } catch (UnsupportedEncodingException exp) {
      throw new RuntimeException("Failed to encode: " + encoding.name(), exp);
    }
  }

}
