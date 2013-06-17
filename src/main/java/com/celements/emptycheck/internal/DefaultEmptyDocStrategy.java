package com.celements.emptycheck.internal;

import javax.inject.Singleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.emptycheck.service.IEmptyDocStrategyRole;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component("default")
@Singleton
public class DefaultEmptyDocStrategy implements IEmptyDocStrategyRole,
    IDefaultEmptyDocStrategyRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      DefaultEmptyDocStrategy.class);

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty(
        "xwikicontext");
  }

  public boolean isEmptyRTEDocument(DocumentReference docRef) {
    return isEmptyRTEDocumentDefault(docRef)
        && isEmptyRTEDocumentTranslated(docRef);
  }

  public boolean isEmptyDocument(DocumentReference docRef) {
    return isEmptyDocumentDefault(docRef)
        && isEmptyDocumentTranslated(docRef);
  }

  /**
   * check if content of default language version for docRef is empty
   */
  public boolean isEmptyRTEDocumentDefault(DocumentReference docRef) {
    try {
      return isEmptyRTEDocument(getContext().getWiki().getDocument(docRef, getContext()));
    } catch (XWikiException exp) {
      LOGGER.error("Failed to check if content of default language version for docRef ["
          + docRef + "] is empty.", exp);
    }
    return true;
  }
  
  /**
   * check if content of translated (context.language) version for docRef is empty
   */
  public boolean isEmptyRTEDocumentTranslated(DocumentReference docRef) {
    try {
      return isEmptyRTEDocument(getContext().getWiki(
          ).getDocument(docRef, getContext()).getTranslatedDocument(
              getContext().getLanguage(), getContext()));
    } catch (XWikiException e) {
      LOGGER.error(e);
    }
    return true;
  }

  public boolean isEmptyRTEDocument(XWikiDocument localdoc) {
    return isEmptyRTEString(localdoc.getContent());
  }

  public boolean isEmptyRTEString(String rteContent) {
    return "".equals(rteContent.replaceAll(
        "(<p>)?(<span.*?>)?(\\s*(&nbsp;|<br\\s*/>))*\\s*(</span>)?(</p>)?", "").trim());
  }

  public boolean isEmptyDocumentDefault(DocumentReference docRef) {
    try {
      return isEmptyDocument(getContext().getWiki(
          ).getDocument(docRef, getContext()));
    } catch (XWikiException e) {
      LOGGER.error(e);
    }
    return true;
  }

  public boolean isEmptyDocumentTranslated(DocumentReference docRef) {
    try {
      return isEmptyDocument(getContext().getWiki(
          ).getDocument(docRef, getContext()).getTranslatedDocument(
              getContext().getLanguage(), getContext()));
    } catch (XWikiException e) {
      LOGGER.error(e);
    }
    return true;
  }

  boolean isEmptyDocument(XWikiDocument localdoc) {
    return "".equals(localdoc.getContent());
  }

}
