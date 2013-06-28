package com.celements.rights;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.model.reference.DocumentReference;

import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiRightNotFoundException;
import com.xpn.xwiki.user.impl.xwiki.XWikiRightServiceImpl;
import com.xpn.xwiki.web.Utils;

public class CelementsRightServiceImpl extends XWikiRightServiceImpl {
  public static int PUBLISHED = 1;
  public static int UNPUBLISHED = 2;
  
  private static Log LOGGER = LogFactory.getFactory().getInstance(
      CelementsRightServiceImpl.class);
  
  /* Adds an optional check for publish and unpublish dates to determine whether or not a 
   * page can be viewed. */
  @Override
  public boolean checkRight(String userOrGroupName, XWikiDocument doc, String accessLevel,
      boolean user, boolean allow, boolean global, XWikiContext context
      ) throws XWikiRightNotFoundException, XWikiException {
    if(isPublishActive(context) && isRestrictedRightsAction(accessLevel)) {
      //default behaviour: no object -> published
      List<BaseObject> objs = getPublishObjects(doc);
      if(!isPubOverride(context) && (isUnpubOverride(context) || isPublished(objs))) {
        LOGGER.info("Document published or not publish controlled.");
        return super.checkRight(userOrGroupName, doc, accessLevel, user, allow, global, 
            context);
      } else {
        LOGGER.info("Document not published, checking edit rights.");
        try {
          return super.checkRight(userOrGroupName, doc, "edit", user, allow, global, 
              context);
        } catch(XWikiRightNotFoundException xwrnfe) {
          LOGGER.info("Rights could not be determined.");
          //If rights can not be determined default to no rights.
          return false;
        }
      }
    } else {
      if(isPubUnpubOverride(context)) {
        LOGGER.warn("Needs CelementsRightServiceImpl for publish / unpublish to work");
      }
      return super.checkRight(userOrGroupName, doc, accessLevel, user, allow, global, 
          context);
    }
  }
  
  boolean isPubUnpubOverride(XWikiContext context) {
    String val = "";
    if(context.get("overridePubCheck") != null) {
      val = context.get("overridePubCheck").toString();
    }
    return (Integer.toString(PUBLISHED).equals(val)) 
        || (Integer.toString(UNPUBLISHED).equals(val));
  }
  
  boolean isPubOverride(XWikiContext context) {
    String val = "";
    if(context.get("overridePubCheck") != null) {
      val = context.get("overridePubCheck").toString();
    }
    return (Integer.toString(PUBLISHED).equals(val));
  }
  
  boolean isUnpubOverride(XWikiContext context) {
    String val = "";
    if(context.get("overridePubCheck") != null) {
      val = context.get("overridePubCheck").toString();
    }
    return (Integer.toString(UNPUBLISHED).equals(val));
  }

  List<BaseObject> getPublishObjects(XWikiDocument doc) {
    return doc.getXObjects(getPublicationClassReference());
  }

  DocumentReference getPublicationClassReference() {
    return ((IWebUtilsService)Utils.getComponent(IWebUtilsService.class)
        ).resolveDocumentReference("Classes.DocumentPublication");
  }

  boolean isRestrictedRightsAction(String accessLevel) {
    return "view".equals(accessLevel) || "comment".equals(accessLevel);
  }
  
  boolean isPublishActive(XWikiContext context) {
    return isPublishActive(context.getDoc().getDocumentReference(), context);
  }

  boolean isPublishActive(DocumentReference forDoc, XWikiContext context) {
    String isActive = context.getWiki().getSpacePreference("publishdate_active", 
        forDoc.getLastSpaceReference().getName(), "-1", context);
    if("-1".equals(isActive)) {
      isActive = context.getWiki().getXWikiPreference("publishdate_active", 
          "celements.publishdate.active", "0", context);
    }
    return "1".equals(isActive);
  }

  boolean isPublished(List<BaseObject> objs) {
    boolean isPublished = false;
    if((objs != null) && (!objs.isEmpty())) {
      for(BaseObject obj : objs) {
        if(obj != null) {
          isPublished |= isAfterStart(obj) && isBeforeEnd(obj);
        }
      }
    } else {
      isPublished = true; //no limits set mean always published
    }
    return isPublished;
  }
  
  boolean isAfterStart(BaseObject obj) {
    Calendar cal = GregorianCalendar.getInstance();
    Date pubDate = obj.getDateValue("publishDate");
    return (pubDate == null) || cal.getTime().after(pubDate);
  }

  boolean isBeforeEnd(BaseObject obj) {
    Calendar cal = GregorianCalendar.getInstance();
    Date unpubDate = obj.getDateValue("unpublishDate");
    return (unpubDate == null) || cal.getTime().before(unpubDate);
  }
}
