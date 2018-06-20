package com.celements.web.service;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.web.XWikiURLFactory;

@Component("url")
public class UrlScriptService implements ScriptService {

  private static Logger LOGGER = LoggerFactory.getLogger(UrlScriptService.class);

  @Requirement
  private ModelContext context;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ICelementsWebServiceRole celementsweb;

  public String getURL(DocumentReference docRef, String action, String queryString) {
    String spaceName = docRef.getLastSpaceReference().getName();
    String docName = docRef.getName();
    String wikiName = docRef.getWikiReference().getName();
    URL url = getUrlFactory().createURL(spaceName, docName, action, queryString, null, wikiName,
        context.getXWikiContext());
    return celementsweb.encodeUrlToUtf8(getUrlFactory().getURL(url, context.getXWikiContext()));
  }

  public String getExternalURL(AttachmentReference attrRef, String action) {
    return getExternalURL(attrRef, action, null);
  }

  public String getExternalURL(AttachmentReference attrRef, String action, String queryString) {
    String ret = "";
    DocumentReference attrDocRef = attrRef.getDocumentReference();
    try {
      XWikiAttachment attr = modelAccess.getDocument(attrDocRef).getAttachment(attrRef.getName());
      URL url = getUrlFactory().createAttachmentURL(attr.getFilename(),
          attrDocRef.getLastSpaceReference().getName(), attrDocRef.getName(), action, queryString,
          attrDocRef.getRoot().getName(), context.getXWikiContext());
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
