package com.celements.web.service;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.web.XWikiURLFactory;

public class UrlService implements IUrlService {

  private static Logger LOGGER = LoggerFactory.getLogger(UrlService.class);

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
    String spaceName = docRef.getLastSpaceReference().getName();
    String docName = docRef.getName();
    String wikiName = docRef.getWikiReference().getName();
    URL url = getUrlFactory().createURL(spaceName, docName, action, queryString, null, wikiName,
        context.getXWikiContext());
    return celementsweb.encodeUrlToUtf8(getUrlFactory().getURL(url, context.getXWikiContext()));
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
    String spaceName = docRef.getLastSpaceReference().getName();
    String docName = docRef.getName();
    String wikiName = docRef.getWikiReference().getName();
    URL url = getUrlFactory().createExternalURL(spaceName, docName, action, queryString, null,
        wikiName, context.getXWikiContext());
    return celementsweb.encodeUrlToUtf8(url.toString());
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
  public String getExternalURL(AttachmentReference attrRef, String action, String queryString) {
    String ret = "";
    DocumentReference attrDocRef = attrRef.getDocumentReference();
    try {
      XWikiAttachment att = modelAccess.getDocument(attrDocRef).getAttachment(attrRef.getName());
      String fileName = att.getFilename();
      String spaceName = attrDocRef.getLastSpaceReference().getName();
      String docName = attrDocRef.getName();
      String wikiName = attrDocRef.getWikiReference().getName();
      URL url = getUrlFactory().createAttachmentURL(fileName, spaceName, docName, action,
          queryString, wikiName, context.getXWikiContext());
      ret = celementsweb.encodeUrlToUtf8(url.toString());
    } catch (DocumentNotExistsException exp) {
      LOGGER.warn("Document for docRef '{}' not found", attrDocRef);
    }
    return ret;
  }

  private XWikiURLFactory getUrlFactory() {
    return context.getXWikiContext().getURLFactory();
  }

}
