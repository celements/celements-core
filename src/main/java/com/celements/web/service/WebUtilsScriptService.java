package com.celements.web.service;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.script.service.ScriptService;

import com.celements.auth.AuthenticationScriptService;
import com.celements.auth.IAuthenticationServiceRole;
import com.celements.rights.access.EAccessLevel;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Attachment;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.web.XWikiMessageTool;

@Component("webUtils")
public class WebUtilsScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationScriptService.class);

  @Requirement
  IWebUtilsService webUtilsService;

  @Requirement
  IAuthenticationServiceRole authService;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty("xwikicontext");
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

  public Attachment getAttachment(AttachmentReference attRef) {
    try {
      if (webUtilsService.hasAccessLevel(attRef, EAccessLevel.VIEW)) {
        return webUtilsService.getAttachmentApi(attRef);
      }
    } catch (XWikiException xwe) {
      LOGGER.error("failed getting attachment for ref '{}'", attRef, xwe);
    }
    return null;
  }

  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch and
   * write the exception in a log-file
   */
  public List<Attachment> getAttachmentListSorted(Document doc, String comparator)
      throws ClassNotFoundException {
    return webUtilsService.getAttachmentListSorted(doc, comparator);
  }

  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch and
   * write the exception in a log-file
   */
  public List<Attachment> getAttachmentListSorted(Document doc, String comparator,
      boolean imagesOnly, int start, int nb) throws ClassNotFoundException {
    return webUtilsService.getAttachmentListSorted(doc, comparator, imagesOnly, start, nb);
  }

  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch and
   * write the exception in a log-file
   */
  public String getAttachmentListSortedAsJSON(Document doc, String comparator, boolean imagesOnly)
      throws ClassNotFoundException {
    return getAttachmentListSortedAsJSON(doc, comparator, imagesOnly, 0, 0);
  }

  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch and
   * write the exception in a log-file
   */
  public String getAttachmentListSortedAsJSON(Document doc, String comparator, boolean imagesOnly,
      int start, int nb) throws ClassNotFoundException {
    return webUtilsService.getAttachmentListSortedAsJSON(doc, comparator, imagesOnly, start, nb);
  }

  public List<DocumentReference> getDocumentParentsDocRefList(DocumentReference docRef,
      boolean includeDoc) {
    return webUtilsService.getDocumentParentsList(docRef, includeDoc);
  }

  public List<Attachment> getAttachmentsForDocs(List<String> docsFN) {
    List<Attachment> attachments = Collections.emptyList();
    if (hasProgrammingRights()) {
      LOGGER.info("getAttachmentsForDocs: fetching attachments...");
      attachments = webUtilsService.getAttachmentsForDocs(docsFN);
    } else {
      LOGGER.info("getAttachmentsForDocs: no programming rights");
    }
    return attachments;
  }

  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch and
   * write the exception in a log-file
   */
  public String getDocSectionAsJSON(String regex, DocumentReference fullName, int part)
      throws XWikiException {
    return webUtilsService.getDocSectionAsJSON(regex, fullName, part);
  }

  /*
   * TODO: Please get rid of throwing an exception to the view (client), use try/catch and
   * write the exception in a log-file
   */
  public int countSections(String regex, DocumentReference fullName) throws XWikiException {
    return webUtilsService.countSections(regex, fullName);
  }

  public List<String> getAllowedLanguages() {
    return webUtilsService.getAllowedLanguages();
  }

  public List<String> getAllowedLanguages(String spaceName) {
    return webUtilsService.getAllowedLanguages(spaceName);
  }

  public List<String> getAllowedLanguages(SpaceReference spaceRef) {
    return webUtilsService.getAllowedLanguages(spaceRef);
  }

  public String getParentSpace() {
    return webUtilsService.getParentSpace();
  }

  public String getJSONContent(Document contentDoc) {
    return webUtilsService.getJSONContent(contentDoc.getDocument());
  }

  public String getJSONContent(DocumentReference docRef) {
    try {
      if (authService.hasAccessLevel("view", getContext().getUser(), true, docRef)) {
        return webUtilsService.getJSONContent(docRef);
      }
    } catch (Exception exp) {
      LOGGER.warn("getJSONContent failed for docRef[" + docRef + "]. ", exp);
    }
    return "{}";
  }

  /**
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

}
