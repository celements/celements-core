package com.celements.web.plugin.cmd;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.web.pagetype.PageTypeCommand;
import com.celements.web.service.IWebUtilsService;
import com.celements.web.utils.IWebUtils;
import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

//TODO Add unittests!!!
public class CreateDocumentCommand {

  private static Log LOGGER = LogFactory.getFactory().getInstance(
      CreateDocumentCommand.class);

  IWebUtils webUtils = WebUtils.getInstance();

  /**
   * createDocument creates a new document if it does not exist.
   * @param docRef 
   * 
   * @return
   */
  public XWikiDocument createDocument(DocumentReference docRef, String pageType) {
    if (getContext().getWiki().exists(docRef, getContext())) {
      try {
        XWikiDocument theNewDoc = getContext().getWiki().getDocument(docRef,
            getContext());
        initNewXWikiDocument(theNewDoc);
        if (pageType != null) {
          DocumentReference pageTypeClassRef = new DocumentReference(
              getContext().getDatabase(), PageTypeCommand.PAGE_TYPE_CLASS_SPACE,
              PageTypeCommand.PAGE_TYPE_CLASS_DOC);
          BaseObject pageTypeObj = theNewDoc.getXObject(pageTypeClassRef, true,
              getContext());
          pageTypeObj.setStringValue("page_type", pageType);
        } else {
          pageType = "";
        }
        getContext().getWiki().saveDocument(theNewDoc, "init " + pageType +" document",
            false, getContext());
      } catch (XWikiException exp) {
        LOGGER.error("Failed to get document [" + docRef + "].", exp);
      }
    }
    return null;
  }

  private void initNewXWikiDocument(XWikiDocument theNewDoc) {
    Date creationDate = new Date();
    theNewDoc.setDefaultLanguage(getWebService().getDefaultLanguage());
    theNewDoc.setLanguage("");
    theNewDoc.setCreationDate(creationDate);
    theNewDoc.setContentUpdateDate(creationDate);
    theNewDoc.setDate(creationDate);
    theNewDoc.setCreator(getContext().getUser());
    theNewDoc.setAuthor(getContext().getUser());
    theNewDoc.setTranslation(0);
    theNewDoc.setContent("");
    theNewDoc.setMetaDataDirty(true);
    LOGGER.info("initNewXWikiDocument:  doc ["
        + theNewDoc.getDocumentReference() + "], defaultLang ["
        + theNewDoc.getDefaultLanguage() + "] isNew saving");
  }

  private IWebUtilsService getWebService() {
    return Utils.getComponent(IWebUtilsService.class);
  }

  private Execution getExecution() {
    return Utils.getComponent(Execution.class);
  }

  private XWikiContext getContext() {
    return (XWikiContext)getExecution().getContext().getProperty("xwikicontext");
  }
}
