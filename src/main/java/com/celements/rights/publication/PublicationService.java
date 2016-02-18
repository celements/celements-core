package com.celements.rights.publication;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.classes.IClassCollectionRole;
import com.celements.rights.CelementsRightServiceImpl.PubUnpub;
import com.celements.web.classcollections.DocumentDetailsClasses;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class PublicationService implements IPublicationServiceRole{

  public static final String OVERRIDE_PUB_CHECK = "overridePubCheck";

  private static Logger LOGGER = LoggerFactory.getLogger(PublicationService.class);

  @Requirement(DocumentDetailsClasses.NAME)
  IClassCollectionRole documentDetailsClasses;

  @Requirement
  IWebUtilsService webUtilsService;

  @Requirement
  private DocumentAccessBridge documentAccessBridge;

  @Requirement
  private Execution execution;

  private XWikiContext getContext() {
    return (XWikiContext) execution.getContext().getProperty(
        XWikiContext.EXECUTIONCONTEXT_KEY);
  }

  @Override
  public boolean isPubUnpubOverride() {
    PubUnpub val = getPubUnpubFromContext();
    return PubUnpub.PUBLISHED == val || PubUnpub.UNPUBLISHED == val;
  }
  
  @Override
  public boolean isPubOverride() {
    return PubUnpub.PUBLISHED == getPubUnpubFromContext();
  }
  
  @Override
  public boolean isUnpubOverride() {
    return PubUnpub.UNPUBLISHED == getPubUnpubFromContext();
  }
  
  PubUnpub getPubUnpubFromContext() {
    PubUnpub val = null;
    Object valObj = execution.getContext().getProperty(OVERRIDE_PUB_CHECK);
    if((valObj != null) && (valObj instanceof PubUnpub)) {
      val = (PubUnpub)execution.getContext().getProperty(OVERRIDE_PUB_CHECK);
    }
    return val;
  }

  @Override
  public void overridePubUnpub(PubUnpub value) {
    execution.getContext().setProperty(OVERRIDE_PUB_CHECK, value);
  }

  List<BaseObject> getPublishObjects(XWikiDocument doc) {
    String wikiName = webUtilsService.getWikiRef(doc.getDocumentReference()).getName();
    return doc.getXObjects(((DocumentDetailsClasses)documentDetailsClasses
        ).getDocumentPublicationClassRef(wikiName));
  }

  @Override
  public boolean isRestrictedRightsAction(String accessLevel) {
    return "view".equals(accessLevel) || "comment".equals(accessLevel);
  }
  
  @Override
  public boolean isPublishActive() {
    DocumentReference forDocRef = null;
    if(documentAccessBridge.getCurrentDocumentReference() != null) {
      forDocRef = documentAccessBridge.getCurrentDocumentReference();
    }
    return isPublishActive(forDocRef);
  }

  @Override
  public boolean isPublishActive(DocumentReference docRef) {
    String space = null;
    if(docRef != null) {
      space = docRef.getLastSpaceReference().getName();
    }
    String isActive = getContext().getWiki().getSpacePreference("publishdate_active", space,
        "-1", getContext());
    if("-1".equals(isActive)) {
      isActive = getContext().getWiki().getXWikiPreference("publishdate_active", 
          "celements.publishdate.active", "0", getContext());
    }
    return "1".equals(isActive);
  }

  @Override
  public boolean isPublished(XWikiDocument doc) {
    List<BaseObject> objs = getPublishObjects(doc);
    boolean isPublished = false;
    if((objs != null) && (!objs.isEmpty())) {
      for(BaseObject obj : objs) {
        if(obj != null) {
          isPublished |= isAfterStart(obj) && isBeforeEnd(obj);
        }
      }
    } else {
      LOGGER.debug("no publish objects found for '{}': no limits set means always"
          + " published", doc);
      isPublished = true;
    }
    return isPublished;
  }
  
  boolean isAfterStart(BaseObject obj) {
    Calendar cal = GregorianCalendar.getInstance();
    Date pubDate = obj.getDateValue(DocumentDetailsClasses.PUBLISH_DATE_FIELD);
    return (pubDate == null) || cal.getTime().after(pubDate);
  }

  boolean isBeforeEnd(BaseObject obj) {
    Calendar cal = GregorianCalendar.getInstance();
    Date unpubDate = obj.getDateValue(DocumentDetailsClasses.UNPUBLISH_DATE_FIELD);
    return (unpubDate == null) || cal.getTime().before(unpubDate);
  }

}
