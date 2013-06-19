package com.celements.web.service;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.user.api.XWikiRightNotFoundException;
import com.xpn.xwiki.user.impl.xwiki.XWikiRightServiceImpl;
import com.xpn.xwiki.web.Utils;

public class CelementsRightServiceImpl extends XWikiRightServiceImpl {
  /* Adds an optional check for publish and unpublish dates to determine whether or not a 
   * page can be viewed. */
  @Override
  public boolean checkRight(String userOrGroupName, XWikiDocument doc, String accessLevel,
      boolean user, boolean allow, boolean global, XWikiContext context
      ) throws XWikiRightNotFoundException, XWikiException {
    if(isPublishActive(context) && isRestrictedRightsAction(accessLevel)) {
      //default behaviour: no object -> published
      BaseObject obj = getPublishObject(doc);
      if((obj == null) || (isAfterStart(obj) && isBeforeEnd(obj))){
        return super.checkRight(userOrGroupName, doc, accessLevel, user, allow, global, 
            context);
      } else {
        return super.checkRight(userOrGroupName, doc, "edit", user, allow, global, 
            context);
      }
    } else {
      return super.checkRight(userOrGroupName, doc, accessLevel, user, allow, global, 
          context);
    }
  }

  BaseObject getPublishObject(XWikiDocument doc) {
    return doc.getXObject(getPublicationClassReference());
  }

  DocumentReference getPublicationClassReference() {
    return ((IWebUtilsService)Utils.getComponent(IWebUtilsService.class)
        ).resolveDocumentReference("Classes.DocumentPublication");
  }

  boolean isRestrictedRightsAction(String accessLevel) {
    return "view".equals(accessLevel) || "comment".equals(accessLevel);
  }

  boolean isPublishActive(XWikiContext context) {
    return context.getWiki().getWebPreferenceAsInt("publishdate_active", 0, context) == 1;
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