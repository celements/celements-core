package com.celements.web.service;

import java.net.URL;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.xpn.xwiki.web.XWikiURLFactory;

@Component
public class XWikiURLService implements UrlService {

  @Requirement
  private ModelContext context;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ICelementsWebServiceRole celementsweb;

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
    String ret = "";
    if (url != null) {
      ret = celementsweb.encodeUrlToUtf8(getUrlFactory().getURL(url, context.getXWikiContext()));
    }
    return ret;
  }

  private String toExternalString(URL url) {
    String ret = "";
    if (url != null) {
      ret = celementsweb.encodeUrlToUtf8(url.toString());
    }
    return ret;
  }

}
