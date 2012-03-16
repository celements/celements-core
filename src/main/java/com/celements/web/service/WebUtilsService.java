package com.celements.web.service;

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.WikiReference;

import com.celements.web.utils.WebUtils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.XWikiMessageTool;

@Component
public class WebUtilsService implements IWebUtilsService {

  private static Log LOGGER = LogFactory.getFactory().getInstance(WebUtils.class);

  @Requirement
  Execution execution;

  @Requirement
  EntityReferenceResolver<String> referenceResolver;

  private XWikiContext getContext() {
    return (XWikiContext)execution.getContext().getProperty("xwikicontext");
  }

  public XWikiMessageTool getMessageTool(String adminLanguage) {
    if(adminLanguage != null) {
      if((getContext().getLanguage() != null) && getContext().getLanguage().equals(
          adminLanguage)) {
        return getContext().getMessageTool();
      } else {
        Locale locale = new Locale(adminLanguage);
        ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources",
            locale);
        if (bundle == null) {
            bundle = ResourceBundle.getBundle("ApplicationResources");
        }
        XWikiContext adminContext = (XWikiContext) getContext().clone();
        adminContext.putAll(getContext());
        adminContext.setLanguage(adminLanguage);
        return new XWikiMessageTool(bundle, adminContext);
      }
    } else {
      return null;
    }
  }

  public XWikiMessageTool getAdminMessageTool() {
    return getMessageTool(getAdminLanguage());
  }
  
  public String getAdminLanguage() {
    return getAdminLanguage(getContext().getUser());
  }

  public String getAdminLanguage(String userFullName) {
    String adminLanguage = null;
    try {
      DocumentReference userDocRef = resolveDocumentReference(userFullName);
      DocumentReference xwikiUsersClassRef = new DocumentReference(
          getContext().getDatabase(), "XWiki", "XWikiUsers");
      xwikiUsersClassRef.setWikiReference(userDocRef.getWikiReference());
      BaseObject userObj = getContext().getWiki().getDocument(userDocRef, getContext()
          ).getXObject(xwikiUsersClassRef);
      if (userObj != null) {
        adminLanguage = userObj.getStringValue("admin_language");
      }
    } catch (XWikiException e) {
      LOGGER.error("failed to get UserObject for " + getContext().getUser());
    }
    if ((adminLanguage == null) || ("".equals(adminLanguage))) {
      adminLanguage = getContext().getWiki().getWebPreference("admin_language",
          getContext().getLanguage(), getContext());
    }
    return adminLanguage;
  }

  private DocumentReference resolveDocumentReference(String collDocName) {
    DocumentReference eventRef = new DocumentReference(referenceResolver.resolve(
        collDocName, EntityType.DOCUMENT));
    eventRef.setWikiReference(new WikiReference(getContext().getDatabase()));
    LOGGER.debug("getDocRefFromFullName: for [" + collDocName + "] got reference ["
        + eventRef + "].");
    return eventRef;
  }

  public String getDefaultLanguage() {
    return getContext().getWiki().getWebPreference("default_language", getContext());
  }

}
