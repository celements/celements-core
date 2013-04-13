package com.celements.web.service;

import javax.inject.Singleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.web.plugin.cmd.NextNonEmptyChildrenCommand;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
@Singleton
public class EmptyCheckService implements IEmptyCheckRole {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      EmptyCheckService.class);

  @Requirement
  Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty(
        "xwikicontext");
  }

  public DocumentReference getNextNonEmptyChildren(DocumentReference documentRef) {
    DocumentReference nonEmptyChildRef = new NextNonEmptyChildrenCommand(
        ).getNextNonEmptyChildren(documentRef);
    if (nonEmptyChildRef != null) {
      return nonEmptyChildRef;
    }
    return documentRef;
  }

  public boolean isEmptyRTEDocument(DocumentReference docRef) {
    return isEmptyRTEDocumentDefault(docRef)
        && isEmptyRTEDocumentTranslated(docRef);
  }

  public boolean isEmptyRTEDocumentDefault(DocumentReference docRef) {
    try {
      return isEmptyRTEDocument(getContext().getWiki(
          ).getDocument(docRef, getContext()));
    } catch (XWikiException e) {
      LOGGER.error(e);
    }
    return true;
  }
  
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

}
