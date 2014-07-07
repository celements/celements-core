package com.celements.web.service;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.web.XWikiMessageTool;

@Component("webUtils")
public class WebUtilsScriptService implements ScriptService {
  
  private static Log LOGGER = LogFactory.getFactory().getInstance(
      AuthenticationScriptService.class);

  @Requirement
  IWebUtilsService webUtilsService;
  
  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public String getDefaultLanguage() {
    return webUtilsService.getDefaultLanguage();
  }
  
  public XWikiMessageTool getMessageTool(String adminLanguage) {
    return webUtilsService.getMessageTool(adminLanguage);
  }

  public String getDefaultLanguage(String spaceName) {
    return webUtilsService.getDefaultLanguage(spaceName);
  }

  public List<Attachment> getAttachmentListSorted(Document doc, String comparator
      ) throws ClassNotFoundException{
    return webUtilsService.getAttachmentListSorted(doc, comparator);
  }

  public List<Attachment> getAttachmentListSorted(Document doc, String comparator,
      boolean imagesOnly, int start, int nb) throws ClassNotFoundException{
    return webUtilsService.getAttachmentListSorted(doc, comparator, imagesOnly, start,
        nb);
  }

  public String getAttachmentListSortedAsJSON(Document doc, String comparator,
      boolean imagesOnly) throws ClassNotFoundException{
    return getAttachmentListSortedAsJSON(doc, comparator, imagesOnly, 0, 0);
  }

  public String getAttachmentListSortedAsJSON(Document doc, String comparator,
      boolean imagesOnly, int start, int nb) throws ClassNotFoundException{
    return webUtilsService.getAttachmentListSortedAsJSON(doc, comparator, imagesOnly,
        start, nb);
  }

  public List<DocumentReference> getDocumentParentsDocRefList(DocumentReference docRef,
      boolean includeDoc) {
    return webUtilsService.getDocumentParentsList(docRef, includeDoc);
  }
  
  public List<Attachment> getAttachmentsForDocs(List<String> docsFN) {
    List<Attachment> attachments = Collections.emptyList();
    if (hasProgrammingRights()){
      LOGGER.info("getAttachmentsForDocs: fetching attachments...");
      attachments = webUtilsService.getAttachmentsForDocs(docsFN);
    }
    else {
      LOGGER.info("getAttachmentsForDocs: no programming rights");
    }
    return attachments;
  }
  
  public String getDocSectionAsJSON(String regex, DocumentReference fullName, int part
      ) throws XWikiException {
    return webUtilsService.getDocSectionAsJSON(regex, fullName, part);
  }
  
  public int countSections(String regex, DocumentReference fullName) 
      throws XWikiException {
    return webUtilsService.countSections(regex, fullName);
  }
  
  public List<String> getAllowedLanguages() {
    return webUtilsService.getAllowedLanguages();
  }
  
  public List<String> getAllowedLanguages(String spaceName) {
    return webUtilsService.getAllowedLanguages(spaceName);
  }
  
  public String getParentSpace() {
    return webUtilsService.getParentSpace();
  }
  
  public String getJSONContent(Document contentDoc) {
    return webUtilsService.getJSONContent(contentDoc.getDocument());
  }
  
  /**
   * 
   * @param authorDocName
   * @return returns the name of the user in the form "lastname, first name"
   */
  public String getUserNameForDocRef(DocumentReference userDocRef) {
    try {
      return webUtilsService.getUserNameForDocRef(userDocRef);
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get user document [" + userDocRef + "].", exp);
    }
    return "N/A";
  }
  
  public String getMajorVersion(Document doc) {
    return webUtilsService.getMajorVersion(doc.getDocument());
  }
  
  private boolean hasProgrammingRights() {
    return getContext().getWiki().getRightService().hasProgrammingRights(getContext());
  }

  private boolean hasAdminRights() {
    return getContext().getWiki().getRightService().hasAdminRights(getContext());
  }
}
